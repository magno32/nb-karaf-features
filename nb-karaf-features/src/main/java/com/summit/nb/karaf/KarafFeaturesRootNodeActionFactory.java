/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.summit.nb.karaf;

import java.util.Comparator;
import javax.swing.Action;

/**
 *
 * @author justin
 */
public interface KarafFeaturesRootNodeActionFactory {

    public Action[] getKarafNodeActions(KarafFeaturesRootNode node);

    public int getPosition();
    public static final Comparator<KarafFeaturesRootNodeActionFactory> COMPARATOR = new KaraFeatureNodeActionComparator();

    public static class KaraFeatureNodeActionComparator implements Comparator<KarafFeaturesRootNodeActionFactory> {

        @Override
        public int compare(KarafFeaturesRootNodeActionFactory o1, KarafFeaturesRootNodeActionFactory o2) {
            return Integer.valueOf(o1.getPosition()).compareTo(o2.getPosition());
        }
    }
}
