/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.summit.nb.karaf.impl;

import com.summit.nb.karaf.KarafFeatureNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.Repository;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author justin
 */
public class KarafFeaturesRepoChildren extends ChildFactory<Feature> {

    private Repository repo;

    public KarafFeaturesRepoChildren(Repository repo) {
        this.repo = repo;
    }

    @Override
    protected boolean createKeys(List<Feature> toPopulate) {
        //TODO localize
        ProgressHandle progress = ProgressHandleFactory.createHandle(String.format("Grabbing Features: %s",repo.getName()));
        try {
            progress.start();
            progress.switchToIndeterminate();
            toPopulate.addAll(Arrays.asList(repo.getFeatures()));
            Collections.sort(toPopulate, new Comparator<Feature>() {
                @Override
                public int compare(Feature o1, Feature o2) {
                    return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
                }
            });
            return true;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }finally{
            progress.finish();
        }
    }

    @Override
    protected Node createNodeForKey(Feature key) {
        return new KarafFeatureNode(key);
    }
}
