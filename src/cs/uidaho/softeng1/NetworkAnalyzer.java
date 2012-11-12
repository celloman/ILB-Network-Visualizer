/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.uidaho.softeng1;

/**
 *
 * @author Jon Lamb
 */
public class NetworkAnalyzer {
    private static final String CLASS_NAME = "NetworkAnalyzer";
    private DataLogger logger = null;
    private NetworkDataPayload netdata = null;
    
    public NetworkAnalyzer( DataLogger entry_logger , NetworkDataPayload net_data){
        if( entry_logger == null ){
            throw new IllegalArgumentException( "Datalogger was not succesfully passed to the network analyzer" );
        } else if( net_data == null ){
            throw new IllegalArgumentException( "NetworkDataPayload was not succesfully passed to the network analyzer" );
        }
        logger = entry_logger;
        netdata = net_data;
        logger.info(CLASS_NAME, "Instantiated new NetworkAnalzer");
        
    }   
    
}
