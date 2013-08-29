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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.features.Repository;
import org.apache.karaf.features.internal.FeatureValidationUtil;
import org.apache.karaf.features.internal.RepositoryImpl;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
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
                logger.warn("Unable to read properties file...", ex);
                Exceptions.printStackTrace(ex);
            }
        }
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

    private Repository internalAddRepository(URI uri) throws Exception {

        validateRepository(uri);
        RepositoryImpl retVal = new RepositoryImpl(uri);
        retVal.load();
        instanceContent.add(uri);
        instanceContent.add(retVal);
        repos.put(uri, retVal);

        return retVal;
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Feature[] listInstalledFeatures() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
}
