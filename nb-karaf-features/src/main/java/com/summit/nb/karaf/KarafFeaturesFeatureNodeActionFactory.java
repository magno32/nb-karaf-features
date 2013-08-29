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
public interface KarafFeaturesFeatureNodeActionFactory extends PositionedNodeActionFactory {

    public Action[] getKarafNodeActions(KarafFeatureNode node);
}
