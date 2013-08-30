/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.summit.nb.karaf;

import com.summit.nb.karaf.KarafFeaturesRootNodeActionFactory;
import com.summit.nb.karaf.impl.FeaturesServiceImpl;
import com.summit.nb.karaf.impl.KarafFeaturesRootChildren;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author justin
 */
@ServicesTabNodeRegistration(
        displayName = "#LBL_KARAF_REPOS",
        iconResource = KarafFeaturesRootNode.ICON_BASE,
        name = "#LBL_KARAF_REPOS", position = Integer.MIN_VALUE)
public class KarafFeaturesRootNode extends AbstractNode {

    public static final String ICON_BASE = "com/summit/nb/karaf/karaf-logo_16.png";
    private FeaturesServiceImpl featuresService = Lookup.getDefault().lookup(FeaturesServiceImpl.class);

    public KarafFeaturesRootNode() {
        super(Children.LEAF);
        setChildren(Children.create(new KarafFeaturesRootChildren(featuresService.getLookup()), true));
        setName(NbBundle.getMessage(KarafFeaturesRootNode.class, "LBL_KARAF_REPOS"));
        setDisplayName(NbBundle.getMessage(KarafFeaturesRootNode.class, "LBL_KARAF_REPOS"));
        setIconBaseWithExtension(ICON_BASE);
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> retVal = new LinkedList<Action>();

        List<? extends KarafFeaturesRootNodeActionFactory> factories = new ArrayList(
                Lookup.getDefault().lookupAll(KarafFeaturesRootNodeActionFactory.class));
        Collections.sort(factories, KarafFeaturesRootNodeActionFactory.COMPARATOR);
        for (KarafFeaturesRootNodeActionFactory factory : factories) {
            retVal.addAll(Arrays.asList(factory.getKarafNodeActions(this)));
        }

        return retVal.toArray(new Action[retVal.size()]);
    }
}
