/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.summit.nb.karaf;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author justin
 */
public class KarafFeatureNode extends AbstractNode {

    private Feature feature;
    private FeaturesService featuresService = Lookup.getDefault().lookup(FeaturesService.class);

    public KarafFeatureNode(Feature feature) {
        super(Children.LEAF);
        this.feature = feature;
    }

    @Override
    public String getDisplayName() {
        return feature.getName();
    }

    /**
     * @return the feature
     */
    public Feature getFeature() {
        return feature;
    }

    /**
     * @param feature the feature to set
     */
    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    @Override
    public Image getIcon(int type) {
        if (!featuresService.isInstalled(feature)) {
            return ImageUtilities.loadImage("com/summit/nb/karaf/karaf-logo_16.png");
        } else {
            return ImageUtilities.loadImage("com/summit/nb/karaf/karaf-logo_16_half.png");
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> retVal = new LinkedList<Action>();

        List<? extends KarafFeaturesFeatureNodeActionFactory> factories = new ArrayList(
                Lookup.getDefault().lookupAll(KarafFeaturesFeatureNodeActionFactory.class));
        Collections.sort(factories, PositionedNodeActionFactory.COMPARATOR);
        for (KarafFeaturesFeatureNodeActionFactory factory : factories) {
            retVal.addAll(Arrays.asList(factory.getKarafNodeActions(this)));
        }

        return retVal.toArray(new Action[retVal.size()]);
    }
}
