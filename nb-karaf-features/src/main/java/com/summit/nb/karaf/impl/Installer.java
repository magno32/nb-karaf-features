/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.summit.nb.karaf.impl;

import org.openide.modules.ModuleInstall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Installer extends ModuleInstall {

    private static final Logger logger = LoggerFactory.getLogger(Installer.class);
    
    private static final String REGISTERING_STRING = "Registering \"{}\" protocol.";
    private static final String REGISTERED_STRING = "Registered \"{}\" protocol.";
    private static final String MAVEN_URL_PROTOCOL = "mvn";
    
    @Override
    public void restored() {
        
        //logger.debug(REGISTERING_STRING,new Object[]{MAVEN_URL_PROTOCOL});
        //System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url.mvn");
        //logger.info(REGISTERED_STRING,new Object[]{MAVEN_URL_PROTOCOL});
    }
}
