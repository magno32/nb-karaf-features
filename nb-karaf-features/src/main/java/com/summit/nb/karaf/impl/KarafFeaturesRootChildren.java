/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.summit.nb.karaf.impl;

import com.summit.nb.karaf.KarafFeaturesRepoNode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.karaf.features.Repository;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author justin
 */
public class KarafFeaturesRootChildren extends ChildFactory<Repository> implements LookupListener {

    Lookup repoLookup;
    Lookup.Result<Repository> repos;

    @SuppressWarnings("LeakingThisInConstructor")
    public KarafFeaturesRootChildren(Lookup lookup) {
        this.repoLookup = lookup;
        repos = repoLookup.lookupResult(Repository.class);
        repos.addLookupListener(this);
    }

    @Override
    protected Node createNodeForKey(Repository key) {
        return new KarafFeaturesRepoNode(repoLookup,key);
    }
    
    @Override
    public void resultChanged(LookupEvent ev) {
        refresh(false);
    }


    @Override
    protected boolean createKeys(List<Repository> toPopulate) {
        toPopulate.addAll(repos.allInstances());
        Collections.sort(toPopulate, new Comparator<Repository>(){

            @Override
            public int compare(Repository o1, Repository o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            }
        });
        return true;
    }
    
}
