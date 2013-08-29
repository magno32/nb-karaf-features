/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.summit.nb.karaf.impl;

import com.summit.nb.karaf.KarafFeatureNode;
import com.summit.nb.karaf.KarafFeaturesFeatureNodeActionFactory;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author justin
 */
@ServiceProvider(service = KarafFeaturesFeatureNodeActionFactory.class)
public class KarafFeatureInstallActionFactory implements KarafFeaturesFeatureNodeActionFactory {

    private FeaturesService featuresService = Lookup.getDefault().lookup(FeaturesService.class);
    private static final Logger logger = LoggerFactory.getLogger(KarafFeatureInstallActionFactory.class);
    
    @Override
    public Action[] getKarafNodeActions(KarafFeatureNode node) {
        List<Action> retVal = new LinkedList<Action>();
        retVal.add(new InstallKarafFeatureAction(node.getFeature()));
        return retVal.toArray(new Action[retVal.size()]);
    }

    @Override
    public int getPosition() {
        return Integer.MIN_VALUE;
    }

    public class InstallKarafFeatureAction extends AbstractAction {

        private Feature feature;
        private Logger logger = LoggerFactory.getLogger(InstallKarafFeatureAction.class);

        public InstallKarafFeatureAction(Feature feature) {
            super("Install Feature");
            this.feature = feature;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                featuresService.installFeature(feature, null);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
