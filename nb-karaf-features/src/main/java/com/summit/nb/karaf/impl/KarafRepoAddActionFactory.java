/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.summit.nb.karaf.impl;

import com.summit.nb.karaf.KarafFeaturesRootNode;
import com.summit.nb.karaf.KarafFeaturesRootNodeActionFactory;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.apache.karaf.features.FeaturesService;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author justin
 */
@ServiceProvider(service = KarafFeaturesRootNodeActionFactory.class)
public class KarafRepoAddActionFactory implements KarafFeaturesRootNodeActionFactory {

    public static final int POSITION = Integer.MIN_VALUE;
    private FeaturesService featuresService = Lookup.getDefault().lookup(FeaturesService.class);

    @Override
    public Action[] getKarafNodeActions(KarafFeaturesRootNode node) {
        //TODO Localize this
        return new Action[]{new AbstractAction("Add Repo") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String url = JOptionPane.showInputDialog("Input Repository URL:");
                    //TODO set this.
                    String version = null;
                    if (url != null) {
                        try {
                            String effectiveVersion = (version == null) ? "LATEST" : version;
                            URI uri = getUriFor(url, effectiveVersion);
                            if (uri == null) {
                                uri = new URI(url);
                            }
                            int selection = JOptionPane.showConfirmDialog(null, "Install all features?", "Install all?", JOptionPane.YES_NO_OPTION);
                            boolean installAll = selection == JOptionPane.YES_OPTION;

                            featuresService.addRepository(uri, installAll);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }};
    }

    @Override
    public int getPosition() {
        return POSITION;
    }
    ////////////////
    //Copied from features.commands
    ///////////////
    Map<String, String> nameToArtifactMap = new HashMap<String, String>();

    public URI getUriFor(String name, String version) {
        String coords = nameToArtifactMap.get(name);
        if (coords == null) {
            return null;
        }
        Artifact artifact = new Artifact(coords);
        return artifact.getPaxUrlForArtifact(version);
    }

    private class Artifact {

        String groupId;
        String artifactId;
        String version;
        String extension;
        String classifier;

        public Artifact(String coords) {
            String[] coordsAr = coords.split(":");
            this.groupId = coordsAr[0];
            this.artifactId = coordsAr[1];
            this.version = coordsAr[4];
            this.extension = coordsAr[2];
            this.classifier = coordsAr[3];
        }

        public Artifact(String coords, String version) {
            this(coords);
            this.version = version;
        }

        public URI getPaxUrlForArtifact(String version) {
            String uriSt = "mvn:" + this.groupId + "/" + this.artifactId + "/" + version + "/" + this.extension + "/" + this.classifier;
            try {
                return new URI(uriSt);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
