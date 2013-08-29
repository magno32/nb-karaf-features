/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.summit.nb.karaf;

import java.util.Comparator;

/**
 *
 * @author justin
 */
public interface PositionedNodeActionFactory {

    int getPosition();
    public static final Comparator<PositionedNodeActionFactory> COMPARATOR = new PositionedNodeActionComparator();

    public static class PositionedNodeActionComparator implements Comparator<PositionedNodeActionFactory> {

        @Override
        public int compare(PositionedNodeActionFactory o1, PositionedNodeActionFactory o2) {
            return Integer.valueOf(o1.getPosition()).compareTo(o2.getPosition());
        }
    }
}
