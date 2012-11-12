/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.uidaho.softeng1;

import java.io.File;

/**
 *
 * @author Jon
 */
public class Entry {
    private static final String CLASS_NAME = "Entry";
    private static final int EXIT_SUCCESS = 0;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DataLogger logger;
        NetworkAnalyzer netstats;
        UserInterface gui;
        NetworkDataPayload netdata;
        
        // the order of instantiation matters here...
        logger = new DataLogger();
        logger.info(CLASS_NAME, "Initializing VMware Network Vizualizer");
        netdata = new NetworkDataPayload( logger );
        netstats = new NetworkAnalyzer( logger , netdata);
        gui = new UserInterface( logger , netdata );
        
//        File inputFile = gui.instantiateOpenDialog();
//        logger.showLogger();
        
        //test the logging
        //logger.info( CLASS_NAME, "Hey this is a debug message");
        //logger.severe( CLASS_NAME, "Ah no more beer, severe situation!");
        
        logger.info(CLASS_NAME, "Starting the JUNG goodness...");
        gui.initGui();
                
        //gui = null;        
        //netstats = null;
        //netdata = null;
        //logger = null;
        
        //System.exit( EXIT_SUCCESS );
    }
    
}
