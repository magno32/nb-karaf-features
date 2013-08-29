/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.summit.nb.karaf;

import com.summit.nb.karaf.impl.KarafFeaturesRepoChildren;
import java.awt.Image;
import org.apache.karaf.features.Repository;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 *
 * @author justin
 */
public class KarafFeaturesRepoNode extends AbstractNode{

    Repository repo;
    
    public KarafFeaturesRepoNode(Repository repo) {
        super(Children.create(new KarafFeaturesRepoChildren(repo),true));
        this.repo = repo;
    }

    @Override
    public String getDisplayName() {
        return repo.getName();
    }

    @Override
    public Image getIcon(int type) {
        //TODO use the karaf logo, but we'll fill it as a percentage of installed features.
        return ImageUtilities.loadImage("com/summit/nb/karaf/impl/remoterepo.png");
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
}
