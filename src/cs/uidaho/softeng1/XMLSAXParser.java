/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.uidaho.softeng1;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author kato
 */
public class XMLSAXParser extends DefaultHandler {

    private static final String CLASS_NAME = "XMLSAXParser";
    private NetworkDataPayload netdata = null;
    private DataLogger logger = null;
    String nodeXmlFilePath = null;
    String tmpValue;
    String NodeValue;
    int NeighborCount = 0;
    List<String> NeighborValue = new ArrayList<>();
    List<Double> wVal = new ArrayList<>();
    List<Double> cVal = new ArrayList<>();

    
    public XMLSAXParser(NetworkDataPayload net_data, DataLogger entry_logger, String inputFilePath){
        if( entry_logger == null ){
            throw new IllegalArgumentException( "Datalogger was not succesfully passed to the user interface" );
        } else if( net_data == null ){
            throw new IllegalArgumentException( "NetworkDataPayload was not succesfully passed to the user interface" );
        }
        logger = entry_logger;
        
        netdata = net_data;
        if (inputFilePath != null){
            this.nodeXmlFilePath = inputFilePath;
        }
        else {
            logger.severe(CLASS_NAME, "Invalid input file, exiting program.");
            System.exit(-1);
        }
        logger.info(CLASS_NAME, "Starting parsing");
        parseDocument();
    }
    
    private void parseDocument(){
        // parse
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(nodeXmlFilePath, this);
        } catch (ParserConfigurationException e) {
            logger.severe(CLASS_NAME, "ParserConfig error");
            System.exit(-1);
        } catch (SAXException e) {
            logger.severe(CLASS_NAME, "SAXException : xml not well formed");
            System.exit(-1);
        } catch (IOException e) {
            logger.severe(CLASS_NAME, "IO error");
            System.exit(-1);
        }
    }
    
    
    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException{
        
    
        if (elementName.equalsIgnoreCase("neighbor")){
            logger.info(CLASS_NAME, "adding neighbor " + attributes.getValue("ip"));
            NeighborValue.add(attributes.getValue("ip"));
            
        }
    }
    
    @Override
    public void endElement(String s, String s1, String element) throws SAXException{

        if (element.equalsIgnoreCase("node")){
            String[] NV = new String[NeighborValue.size()];
            double[] CV = new double[cVal.size()];
            double[] WV = new double[wVal.size()];
            
            for(int i = 0; i < NeighborValue.size(); i++){
                NV[i] = NeighborValue.get(i);
            }
            for(int i = 0; i < cVal.size(); i++){
                CV[i] = cVal.get(i);
            }
            for(int i = 0; i < wVal.size(); i++){
                WV[i] =  wVal.get(i);
            }
            
            logger.info(CLASS_NAME, "pushing node " + NodeValue);
            netdata.addNodeInfo(NodeValue , NV, CV, WV);
            NodeValue = "";
            
            NeighborValue = new ArrayList<>();
            wVal = new ArrayList<>();
            cVal = new ArrayList<>();
            
        }
        
        if (element.equalsIgnoreCase("neighbor")){
            logger.info(CLASS_NAME, "end neighbor");
            NeighborCount++;
            // add a check for weight and cap
        }
        
        if (element.equalsIgnoreCase("id")){
            logger.info(CLASS_NAME, "new node " + tmpValue);
            NodeValue = tmpValue;
        }
        
        if (element.equalsIgnoreCase("weight")){
            logger.info(CLASS_NAME, "setting weight " + tmpValue);
            wVal.add(Double.parseDouble(tmpValue));
        }
 
        
        if (element.equalsIgnoreCase("capacity")){
            logger.info(CLASS_NAME, "setting capacity " + tmpValue);
            cVal.add(Double.parseDouble(tmpValue));
        }        
    }
    
    @Override
    public void endDocument(){
        logger.info(CLASS_NAME, "End of XML file, calling processNodes");
        netdata.processNodes();
    }
    
    @Override
    public void characters(char[] ac, int i, int j) throws SAXException{
        tmpValue = new String(ac, i, j);   
    }

}
