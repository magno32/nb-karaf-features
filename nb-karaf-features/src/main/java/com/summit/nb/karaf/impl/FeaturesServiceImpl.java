/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.summit.nb.karaf.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.felix.utils.version.VersionRange;
import org.apache.felix.utils.version.VersionTable;
import org.apache.karaf.features.Dependency;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.features.Repository;
import org.apache.karaf.features.internal.FeatureValidationUtil;
import org.apache.karaf.features.internal.RepositoryImpl;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.osgi.framework.Version;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author justin
 */
@ServiceProviders(value = {
    @ServiceProvider(service = FeaturesService.class),
    @ServiceProvider(service = FeaturesServiceImpl.class)})
public class FeaturesServiceImpl implements FeaturesService, Lookup.Provider {

    private static final Logger logger = LoggerFactory.getLogger(FeaturesServiceImpl.class);
    private static final String MODULE_CACHE_DIR_NAME = "karaf";
    private static final File MODULE_CACHE_DIR = new File(Places.getUserDirectory(), MODULE_CACHE_DIR_NAME);

    static {
        if (!MODULE_CACHE_DIR.exists()) {
            MODULE_CACHE_DIR.mkdirs();
        }
    }
    private static final String MODULE_CACHE_PROPERTIES_FILE_NAME = "repos.dat";
    private static final File MODULE_CACHE_PROPERTIES_FILE = new File(MODULE_CACHE_DIR, MODULE_CACHE_PROPERTIES_FILE_NAME);
    //TODO use the lookup to manage all of this... no need for a map now. 
    private Map<URI, Repository> repos = new HashMap<URI, Repository>();
    private InstanceContent instanceContent = new InstanceContent();
    private Lookup lookup = new AbstractLookup(instanceContent);
    Lookup.Result<Feature> featuresResult = lookup.lookupResult(Feature.class);
    private Set<Feature> installed = new HashSet<Feature>();

    static {
        if (!MODULE_CACHE_PROPERTIES_FILE.exists()) {
            try {
                logger.info("Properties file did not exist, creating a new one.");
                MODULE_CACHE_PROPERTIES_FILE.createNewFile();
                storeProperties(new HashSet<URI>());
            } catch (IOException ex) {
                logger.warn("Unable to create properties file...", ex);
                Exceptions.printStackTrace(ex);
            }
        }
    }

    {
        if (MODULE_CACHE_PROPERTIES_FILE != null && MODULE_CACHE_PROPERTIES_FILE.exists()) {
            try {
                InputStream in = new FileInputStream(MODULE_CACHE_PROPERTIES_FILE);
                ObjectInputStream objInputStream = new ObjectInputStream(in);
                internalClearRepos();
                Set<URI> uris = (Set<URI>) objInputStream.readObject();
                for (URI uri : uris) {
                    internalAddRepository(uri);
                }
                in.close();
            } catch (Exception ex) {
                logger.warn("Unable to read properties file...");
                Exceptions.printStackTrace(ex);
            }
        }
        try {
            installLocalRepositories();
        } catch (Exception ex) {
            logger.warn("Unable to load local features...");
            Exceptions.printStackTrace(ex);
        }
        featuresResult.addLookupListener(new FeatureLookupListener(featuresResult));
    }

    private void storeProperties() throws IOException {
        storeProperties(new HashSet<URI>(repos.keySet()));
    }

    private static void storeProperties(Set<URI> setToStore) throws IOException {
        OutputStream out = new FileOutputStream(MODULE_CACHE_PROPERTIES_FILE);
        //TODO maybe add some defaults?
        ObjectOutputStream objOutputStream = new ObjectOutputStream(out);
        objOutputStream.writeObject(setToStore);
        out.flush();
        out.close();
    }

    @Override
    public void validateRepository(URI uri) throws Exception {
        FeatureValidationUtil.validate(uri);
    }

    @Override
    public void addRepository(URI uri) throws Exception {
        this.addRepository(uri, false);
    }

    @Override
    public void addRepository(URI uri, boolean install) throws Exception {
        if (!repos.containsKey(uri)) {
            internalAddRepository(uri);
            //TODO install if true
            storeProperties();
        }
    }

    private void internalClearRepos() throws Exception {
        for (URI uri : repos.keySet()) {
            internalRemoveRepository(uri);
        }
    }

    private void internalAddRepository(URI uri) throws Exception {
        logger.info("Adding repo: {}", new Object[]{uri});
        validateRepository(uri);
        RepositoryImpl retVal = new RepositoryImpl(uri);
        if (lookup.lookupAll(URI.class).contains(uri)) {
            logger.info("Repo already installed: {}", new Object[]{uri});
            return;
        }
        for (URI localUri : retVal.getRepositories()) {
            logger.info("Adding required repo: {}", new Object[]{localUri});
            internalAddRepository(localUri);
        }
        retVal.load();
        instanceContent.add(uri);
        instanceContent.add(retVal);
        repos.put(uri, retVal);
        for (Feature f : retVal.getFeatures()) {
            instanceContent.add(f);
        }
    }

    private void internalRemoveRepository(URI uri) throws Exception {
        instanceContent.remove(uri);
        instanceContent.remove(repos.get(uri));
        repos.remove(uri);
    }

    @Override
    public void removeRepository(URI uri) throws Exception {
        removeRepository(uri, false);
    }

    @Override
    public void removeRepository(URI uri, boolean uninstall) throws Exception {
        //TODO uninstall if true
        internalRemoveRepository(uri);
        storeProperties();
    }

    @Override
    public void restoreRepository(URI uri) throws Exception {
        addRepository(uri);
    }

    @Override
    public Repository[] listRepositories() {
        return lookup.lookupAll(Repository.class).toArray(new Repository[]{});
    }

    @Override
    public Repository getRepository(String repoName) {
        for (Repository repo : listRepositories()) {
            if (repoName.equals(repo.getName())) {
                return repo;
            }
        }
        return null;
    }

    @Override
    public void installFeature(String name) throws Exception {
        installFeature(name, org.apache.karaf.features.internal.model.Feature.DEFAULT_VERSION);
    }

    @Override
    public void installFeature(String name, EnumSet<Option> options) throws Exception {
        installFeature(name, org.apache.karaf.features.internal.model.Feature.DEFAULT_VERSION, options);
    }

    @Override
    public void installFeature(String name, String version) throws Exception {
        installFeature(name, version, EnumSet.noneOf(Option.class));
    }

    @Override
    public void installFeature(String name, String version, EnumSet<Option> options) throws Exception {
        Feature f = getFeature(name, version);
        if (f == null) {
            throw new Exception("No feature named '" + name
                    + "' with version '" + version + "' available");
        }
        installFeature(f, options);
    }

    @Override
    public void installFeature(Feature f, EnumSet<Option> options) throws Exception {
        installFeatures(Collections.singleton(f), options);
    }

    @Override
    public void installFeatures(Set<Feature> features, EnumSet<Option> options) throws Exception {
        boolean verbose = options.contains(FeaturesService.Option.Verbose);
        try {
            for (Feature f : features) {
                logger.info("Installing dependencies for \"{}\".", new Object[]{f.getName()});
                installDependencies(f.getDependencies(), options);
                logger.info("Done installing dependencies for \"{}\".", new Object[]{f.getName()});
            }
        } catch (Exception ex) {
            boolean noCleanIfFailure = options.contains(Option.NoCleanIfFailure);
            //cleanUpOnFailure(state, failure, noCleanIfFailure);
            throw ex;
        }
    }

    private void installDependencies(List<Dependency> deps, EnumSet<Option> options) throws Exception {
        Set<Feature> depFeatures = new HashSet<Feature>(deps.size());
        for (Dependency dep : deps) {
            logger.debug("Looking for feature dependency: \"{}\"", new Object[]{dep.getName()});
            Feature depFeature = getFeatureForDependency(dep);
            if (depFeature == null) {
                throw new Exception("Unable to find required dependency: " + dep.getName());
            }
            depFeatures.add(depFeature);
        }
        installFeatures(depFeatures, options);
    }

    /**
     * Returns the {@link Feature} that matches the {@link Dependency}.
     *
     * @param dependency
     * @return
     * @throws Exception
     */
    private Feature getFeatureForDependency(Dependency dependency) throws Exception {
        VersionRange range = org.apache.karaf.features.internal.model.Feature.DEFAULT_VERSION.equals(dependency.getVersion())
                ? VersionRange.ANY_VERSION : new VersionRange(dependency.getVersion(), true, true);
        Feature fi = null;
        for (Feature f : installed) {
            if (f.getName().equals(dependency.getName())) {
                Version v = VersionTable.getVersion(f.getVersion());
                if (range.contains(v)) {
                    if (fi == null || VersionTable.getVersion(fi.getVersion()).compareTo(v) < 0) {
                        fi = f;
                    }
                }
            }
        }
        if (fi == null) {
            Map<String, Feature> avail = getFeaturesAsVersionMap().get(dependency.getName());
            if (avail != null) {
                for (Feature f : avail.values()) {
                    Version v = VersionTable.getVersion(f.getVersion());
                    if (range.contains(v)) {
                        if (fi == null || VersionTable.getVersion(fi.getVersion()).compareTo(v) < 0) {
                            fi = f;
                        }
                    }
                }
            }
        }
        return fi;
    }

    @Override
    public void uninstallFeature(String name) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void uninstallFeature(String name, String version) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Feature[] listFeatures() throws Exception {
        Collection<? extends Feature> retVal = featuresResult.allInstances();
        return retVal.toArray(new Feature[retVal.size()]);
    }

    @Override
    public Feature[] listInstalledFeatures() {
        return installed.toArray(new Feature[installed.size()]);
    }

    @Override
    public boolean isInstalled(Feature f) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Feature getFeature(String name, String version) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Feature getFeature(String name) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    protected Map<String, Map<String, Feature>> getFeaturesAsVersionMap() throws Exception {
        //the outer map's key is feature name, the inner map's key is feature version       
        Map<String, Map<String, Feature>> retVal = new HashMap<String, Map<String, Feature>>();
        for (Feature f : featuresResult.allInstances()) {
            Map<String, Feature> featureByVersion = retVal.get(f.getName());
            if (featureByVersion == null) {
                featureByVersion = new HashMap<String, Feature>();
                retVal.put(f.getName(), featureByVersion);
            }
            featureByVersion.put(f.getVersion(), f);
        }
        return retVal;
    }

    private class FeatureLookupListener implements LookupListener {

        Lookup.Result<Feature> results;

        public FeatureLookupListener(Lookup.Result<Feature> results) {
            this.results = results;
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            FeaturesServiceImpl.this.installed.clear();
            for (Feature f : results.allInstances()) {
                if (isInstalled(f)) {
                    FeaturesServiceImpl.this.installed.add(f);
                }
            }
        }
    }

    /**
     * Looks on the class path for "META-INF/features/*-features.xml" files.
     * Will load them into the system.
     */
    public void installLocalRepositories() throws Exception {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().filterInputsBy(new FilterBuilder().excludePackage("META-INF/features"))
                .setUrls(ClasspathHelper.forPackage("META-INF/features/hasFeatures", FeaturesServiceImpl.class.getClassLoader()))
                .setScanners(new ResourcesScanner()));
        Set<String> urls = reflections.getResources(Pattern.compile(".*\\-features.xml"));

        for (String resource : urls) {
            final URL localUrl = FeaturesServiceImpl.class.getClassLoader().getResource(resource);
            if (localUrl != null) {

                logger.info("Autoload: {}", new Object[]{localUrl.toString()});
                addRepository(localUrl.toURI());
            }
        }
    }
}
