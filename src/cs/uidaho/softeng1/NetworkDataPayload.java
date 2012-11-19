/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.uidaho.softeng1;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;


/**
 *
 * @author Jon Lamb
 * 
 * 
 */
public class NetworkDataPayload {
    private static final String CLASS_NAME = "NetworkDataPayload";
    private DataLogger logger = null;
    private NetworkNode[] nodeArray;
    private NetworkPath[] pathArray;    // not allocated yet
    private int nodeCount;
    private int edgeCount;
    private LinkedList<NetworkNode> nodeList;
    private LinkedList<NetworkPath> pathList;
    private int curNodeId, curPathId;
    //private boolean readyToProc;
    
    //Collections.sort(nodeList,MakeComparator);
    //for ( Player aPlayer : players ){}
    
    public NetworkDataPayload( DataLogger entry_logger ){
        if( entry_logger == null ){
            throw new IllegalArgumentException( "Datalogger was not succesfully passed to the Network Data Payload object" );
        }
        //readyToProc = false;
        logger = entry_logger;
        logger.info(CLASS_NAME, "Instantiated new Network Data Payload");
        nodeCount = 0;
        edgeCount = 0;
        curNodeId = 1;  // ID starts at 1
        curPathId = 1;
        nodeList = new LinkedList<>();
        pathList = new LinkedList<>();        
    }
    
    /*
     * this method sorts the nodeArray based on ID and returns
     * the array
     */
    public NetworkNode[] getNodeArray(){
        logger.info(CLASS_NAME, "Attempting to sort and return the nodeArray");
        Collections.sort(nodeList, NodeComparator);
        nodeArray = (NetworkNode[]) nodeList.toArray(new NetworkNode[nodeList.size()]);        
        return nodeArray;
    }
    
    /*
     * this method sorts the pathArray based on ID and returns
     * the array
     */
    public NetworkPath[] getPathArray(){
        logger.info(CLASS_NAME, "Attempting to sort and return the patheArray");
        Collections.sort(pathList, PathComparator);
        pathArray = (NetworkPath[]) pathList.toArray( new NetworkPath[pathList.size()]);
        return pathArray;
    }
    
    /*
     * call this after adding all of the node data
     * 
     * this method will take all the data in the pathList and generate
     * the nodeList
     * 
     */
    public void processNodes(){
        logger.info(CLASS_NAME, "Attempting to processes paths");
        // do some sanity checks
        if( pathList.isEmpty() ){
            logger.warning(CLASS_NAME, "No paths to process");
            return;
        }
        
        // generate all of the node A's in the path list
        logger.info(CLASS_NAME, "Processing the A nodes");
        ListIterator pathItr = pathList.listIterator();
        while( pathItr.hasNext() ){
            NetworkPath pathA = (NetworkPath) pathItr.next();
            String nodeAIP = pathA.getNodeAIP();
            addNewNode( nodeAIP );
        }
        
        // generate all of the node B's in the path list
        logger.info(CLASS_NAME, "Processing the B nodes");
        pathItr = pathList.listIterator();
        while( pathItr.hasNext() ){
            NetworkPath pathB = (NetworkPath) pathItr.next();
            String nodeBIP = pathB.getNodeBIP();
            addNewNode( nodeBIP );
        }
        
        // log some details
        logger.info(CLASS_NAME, "Total number of paths to process: " + pathList.size() );
        logger.info(CLASS_NAME, "Total number of nodes processed: " + nodeList.size() );        
    }
    
    /*
     * 
     */
    public void pipeToGraph(Graph<NetworkNode, NetworkPath> netGraph){
        if( netGraph == null ) errorExit("Failed to pipe data to graph, netGraph is null");
        
        logger.info(CLASS_NAME, "Attempting to generate the graph edges and nodes");
        
        // iterate through the path list, adding to the graph where appropriate
        ListIterator pathItr = pathList.listIterator();
        while( pathItr.hasNext() ){
            NetworkPath path = (NetworkPath) pathItr.next();
            logger.info(CLASS_NAME, "Attempting to pipe path to graph - ID: " + path.getID() + " from " +
                    path.getNodeAIP() + " to " + path.getNodeBIP());
            NetworkNode nodeA = getNodeRef( path.getNodeAIP() );
            NetworkNode nodeB = getNodeRef( path.getNodeBIP() );
            netGraph.addEdge(path, nodeA, nodeB, EdgeType.UNDIRECTED );
        }
        logger.info(CLASS_NAME, "Successfully piped to graph");
    }
    
    /*
     * this method searches for the supplied IP address in the node list
     * it returns a refrence to that node
     */
    private NetworkNode getNodeRef( String nodeIP ){
        NetworkNode ret = null;
        ListIterator nodeItr = nodeList.listIterator();
        while( nodeItr.hasNext() ){
            NetworkNode node = (NetworkNode) nodeItr.next();
            if( nodeIP.compareTo(node.getIP()) == 0 ){
                logger.info(CLASS_NAME, "Found a reference to nodeIP: " + nodeIP + " node ID: " + node.getID());
                return (ret = node);
            }
        }
        if( ret == null ) errorExit("Failed to get the node reference on nodeIP: " + nodeIP);
        return ret;
    }
            
    /*
     * This method is used to populate the node and path lists
     * ipAddress is the IP address of the node being added
     * neighbors is an array of neighboring IP addresses connected to ipAddress
     * neighbWeights is an array of the weight values corresponding to paths between ipAddress and each neighbor node
     * neighbCapacity is similar, just for the capacities
     * 
     * Critial: if anything fails during this process the program should log the error and exit
     */
    public void addNodeInfo( String ipAddress, String[] neighbors, double[] neighbCapacity, double[] neighbWeights ){
        // do some sanity checks
        if( ipAddress == null ) errorExit("Failed to add node, ipAddress is null");
        if( neighbors == null ) errorExit("Failed to add node, neighbors array is null");
        if( neighbWeights == null ) errorExit("Failed to add node, neighbWeights array is null");
        if( neighbCapacity == null ) errorExit("Failed to add node, neighbCapacity array is null");
        if( neighbors.length != neighbWeights.length ) errorExit("Failed to add node, number of weights must equal the number of neighboring nodes");
                
        // add all of the paths from root(ipAddress) to each neighbor node if one doesnt already exist
        for( int i = 0; i < neighbors.length; i++ ){
            addNewPath( ipAddress, neighbors[i], neighbCapacity[i], neighbWeights[i] );
        }
    }
    
    /*
     * this method will add a new path to the pathList if a path doesn't already exist
     * between node A and node B
     * 
     * Critial: if anything fails during this process the program should log the error and exit
     */
    private void addNewPath( String nodeAIP, String nodeBIP, double capacity, double weight ){
        // do some sanity checks
        if( nodeAIP == null ) errorExit("Failed to add path, nodeAIP is null");
        if( nodeBIP == null ) errorExit("Failed to add path, nodeBIP is null");
        if( capacity <= 0.0 ) capacity = 1.0;
        if( weight <= 0.0 ) weight = 1.0;
        
        logger.info(CLASS_NAME, "Attempting to add a path from " + nodeAIP + " to " + nodeBIP);
        if( pathExist(nodeAIP, nodeBIP) == false ){
            NetworkPath path = new NetworkPath( curPathId++, capacity, weight );
            path.setNodeA(nodeAIP);
            path.setNodeB(nodeBIP);
            pathList.add(path);
            logger.info(CLASS_NAME, "Path ID: " + path.getID() + " from " + nodeAIP + " to " + nodeBIP + " was successfully created");
        } else {
            logger.warning(CLASS_NAME, "Path from " + nodeAIP + " to " + nodeBIP + " already exist");
        }
    }
    
    /*
     * this method will add a new node with the supplied IP address in the nodeList
     * if the node doesnt already exist
     */
    private void addNewNode( String nodeIP ){
        // do some sanity checks
        if( nodeIP == null ) errorExit("Failed to add node, nodeIP is null");
        
        logger.info(CLASS_NAME, "Attempting to add a new node, IP: " + nodeIP);
        if( nodeExist(nodeIP) == false ){
            NetworkNode node = new NetworkNode( curNodeId++, nodeIP );
            nodeList.add(node);
            logger.info(CLASS_NAME, "Node ID: " + node.getID() + " IP: " + nodeIP + " was successfully created");
        } else {
            logger.warning(CLASS_NAME, "Node IP: " + nodeIP + " already exist");
        }
    }
    
    /*
     * this method will test whether or not an undirected path between node A and B
     * exist in the pathList
     * 
     * Its pretty inefficient!
     * 
     */
    private boolean pathExist( String nodeAIP, String nodeBIP ){
        boolean exist = false;
        
        if( pathList.isEmpty() ) return exist;
        
        ListIterator pathItr = pathList.listIterator();
        while( pathItr.hasNext() ){
            NetworkPath tmp = (NetworkPath) pathItr.next();
            String tmpA = tmp.getNodeAIP();
            String tmpB = tmp.getNodeBIP();
            
            // if the same path exist, return true
            if( (nodeAIP.compareTo(tmpA) == 0) && (nodeBIP.compareTo(tmpB) == 0) ) return (exist = true);
            
            // if the path in the opposite direction exist, return true
            if( (nodeAIP.compareTo(tmpB) == 0) && (nodeBIP.compareTo(tmpA) == 0) ) return (exist = true);
        }
        
        return exist;
    }
    
    /*
     * this method will test whether or not a node exist in the nodeList with
     * the supplied IP address
     */
    private boolean nodeExist( String nodeIP ){
        boolean exist = false;
        
        if( nodeList.isEmpty() ) return exist;
        
        ListIterator nodeItr = nodeList.listIterator();
        while( nodeItr.hasNext() ){
            NetworkNode tmp = (NetworkNode) nodeItr.next();
            String tmpIP = tmp.getIP();            
            // if the IP address is already in the list, return true
            if( nodeIP.compareTo(tmpIP) == 0) return (exist = true);
        }
        
        return exist;
    }
    
    private void errorExit( String msg ){
        if( logger == null ) return;
        logger.severe(CLASS_NAME, msg);
        logger.errorExit();
    }
    
    
    // sorts on ID
    private static Comparator<NetworkNode> NodeComparator = new Comparator<NetworkNode>() {
        @Override
        public int compare(NetworkNode n1, NetworkNode n2) {
            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;
            
            if( n1.getID() < n2.getID() ) return BEFORE;
            if( n1.getID() > n2.getID() ) return AFTER;
            
            return EQUAL;
        }
        
    };
    
    private static Comparator<NetworkPath> PathComparator = new Comparator<NetworkPath>() {
        @Override
        public int compare(NetworkPath n1, NetworkPath n2) {
            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;
            
            if( n1.getID() < n2.getID() ) return BEFORE;
            if( n1.getID() > n2.getID() ) return AFTER;
            
            return EQUAL;
        }
        
    };
}
