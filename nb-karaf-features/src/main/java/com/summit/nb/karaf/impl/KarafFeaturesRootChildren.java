/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.summit.nb.karaf.impl;

import com.summit.nb.karaf.KarafFeaturesRepoNode;
import java.util.ArrayList;
import java.util.List;
import org.apache.karaf.features.Repository;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author justin
 */
public class KarafFeaturesRootChildren extends Children.Keys<Repository> implements LookupListener {

    Lookup repoLookup;
    Lookup.Result<Repository> repos;

    @SuppressWarnings("LeakingThisInConstructor")
    public KarafFeaturesRootChildren(Lookup lookup) {
        this.repoLookup = lookup;
        repos = repoLookup.lookupResult(Repository.class);
        repos.addLookupListener(this);
    }

    @Override
    protected Node[] createNodes(Repository key) {
        return new Node[]{new KarafFeaturesRepoNode(key)};
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        setKeys(repos.allInstances());
    }

    @Override
    protected void addNotify() {
        resultChanged(null);
    }
}
