/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.summit.nb.karaf.impl;

import com.summit.nb.karaf.InstalledFeature;
import org.apache.karaf.features.Feature;

/**
 *
 * @author justin
 */
public class InstalledFeatureImpl implements InstalledFeature {
    private Feature feature;

    public InstalledFeatureImpl(Feature feature) {
        this.feature = feature;
    }

    public InstalledFeatureImpl() {
    }

    @Override
    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
}
