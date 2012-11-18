/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.uidaho.softeng1;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import java.awt.Dimension;
import javax.swing.JFrame;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.apache.commons.collections15.Transformer;


/**
 *
 * @author Jon
 */
public class UserInterface {
    private static final String CLASS_NAME = "UserInterface";
    private DataLogger logger = null;
    private NetworkDataPayload netdata = null;
    
    private int nodeCount = 0;
    private int edgeCount = 0;
    private int NUM_NODES = 7;  // some example numbers to create a temp graph
    private int NUM_PATHS = 8;
    private int WIDTH, HEIGHT;
    private boolean setLabels; //used for toggleing edge/node labels
    
    private static JTextArea extraNodeInfo;
    private static JTextField nodeNumber;
    
    private XMLSAXParser parser;
    
    /*
     * The nodeArray and pathArray arrays contain the nodes and edges
     * Note: the array is 0-based, and the indexing scheme is 1-based
     * This relationship must always hold true, so element zero in the nodeArray
     * should correspond to Node 1, and so forth.
     */
    private NetworkNode[] nodeArray;
    private NetworkPath[] pathArray;
    
    private Graph<NetworkNode, NetworkPath> netGraph;
    private Layout<NetworkNode, NetworkPath> graphLayout;
    private VisualizationViewer<NetworkNode, NetworkPath> view; 
    private JFrame graphFrame;
    
    //algorithm menu objects
    private static final String MAIN_MEN_STR = "Menu";
    private static final String ALG_MEN_STR = "Algorithms";
    private static final String HELP_MEN_STR = "Help";
    private JMenuBar menuBar;   // the menu bar
    private JMenu algMenu;    // the algorithms menu
    private JMenuItem algMenu_unwPath, algMenu_wPath;   // the weighted/unweighted algorithm menu items
    
    //file menu objects
    private static final String FILE_MEN_STR = "File";
    private JMenu fileMenu; // the save menu
    private JMenuItem fileMenu_Save;
    private JMenuItem fileMenu_Open;
    private JMenuItem fileMenu_sessionLog;
    
    private JMenu helpMenu;
    private JMenuItem helpMenu_usrManual;
    
    private JMenuItem fileMenu_Quit;
    
    public UserInterface( DataLogger entry_logger, NetworkDataPayload net_data ){
        if( entry_logger == null ){
            throw new IllegalArgumentException( "Datalogger was not succesfully passed to the user interface" );
        } else if( net_data == null ){
            throw new IllegalArgumentException( "NetworkDataPayload was not succesfully passed to the user interface" );
        }
        logger = entry_logger;
        netdata = net_data;
        nodeArray = new NetworkNode[NUM_NODES];
        pathArray = new NetworkPath[NUM_PATHS];
        edgeCount = NUM_PATHS;
        nodeCount = NUM_NODES;
        WIDTH = HEIGHT = 800;
        setLabels = true;
     
        logger.info(CLASS_NAME, "Instantiated new UserInterface");
    }
        
    /**
     * This creates an example graph with some interconnecting nodes some with different weights to test the algorithms
     * 
     * We still need some method of keeping track of the node connections
     * For example: how will we know when node1 has already been connected to node2
     * when node1 and node2 say they are connected to each other specified from the input file,
     * we don't want to have two connections
     * since this is undirected
     * Dan has this part still
     */
    private void constructGraph(){
        logger.info(CLASS_NAME, "Generating Graph");
        // set up the graph
        netGraph = new SparseMultigraph<>(); 
        
        File inputFile = instantiateOpenDialog();
        
        parser = new XMLSAXParser(netdata, logger, inputFile.getPath());

        logger.showLogger();
        
        netdata.processNodes();        
        
        netdata.pipeToGrahp(netGraph);
        
        nodeArray = netdata.getNodeArray();
        pathArray = netdata.getPathArray();
        
        nodeCount = nodeArray.length;
        edgeCount = pathArray.length;
        
        logger.info(CLASS_NAME, "Graph Generation Complete");
    }
    
    private void construcLayout(){
        logger.info(CLASS_NAME, "Constructing Graph Layout");
        //graphLayout = new CircleLayout<>(this.netGraph);
        //graphLayout = new KKLayout<>(this.netGraph);
        graphLayout = new FRLayout<>(this.netGraph);
        graphLayout.setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
        
         //Graph visualizer
        logger.info(CLASS_NAME, "Initializing Vizualizer");
        view = new VisualizationViewer<>(graphLayout);
        
        
        //setting up graph mouse mode
        final DefaultModalGraphMouse<NetworkNode, NetworkPath> graphMouse = new DefaultModalGraphMouse<>();
        view.setGraphMouse(graphMouse); 
        
        //modebox for changing graph mouse modes
        JComboBox modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(((DefaultModalGraphMouse<NetworkNode, NetworkPath>)view.getGraphMouse()).getModeListener());
        modeBox.setMaximumSize(new Dimension(100, 50));
        
        //reset and nametoggle buttons
        JButton resetButton = new JButton("Reset");
        JButton ntButton = new JButton("Toggle Labels");
        
        //give buttons an action
        //reset graph
        resetButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Layout<NetworkNode, NetworkPath> layout = view.getGraphLayout();
                    layout.initialize();
                    Relaxer relaxer = view.getModel().getRelaxer();
                    if(relaxer != null) {
                        relaxer.stop();
                        relaxer.prerelax();
                        relaxer.relax();
                    } 
                    logger.info(CLASS_NAME, "Reset Visualizer");
                }
            });
        //toggle edge/pathnames
        ntButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //if labels are already set, remove
                    if( setLabels ) {
                        logger.info(CLASS_NAME, "Removed Labels");
                        //remove vertext labels
                        view.getRenderContext().setVertexLabelTransformer(new Transformer<NetworkNode, String>() {
                        @Override
                            public String transform(NetworkNode e) {
                                return "";
                            }
                        });
                        //remove edge labels
                        view.getRenderContext().setEdgeLabelTransformer(new Transformer<NetworkPath, String>() {
                        @Override
                            public String transform(NetworkPath e) {
                                return "";
                            }
                        });
                        //update the graph
                        view.updateUI();
                        
                        setLabels = false;
                    }
                    else {
                        logger.info(CLASS_NAME, "Placed Labels");
                        //place labels back on graph
                        view.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
                        view.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
                        
                        //update graph
                        view.updateUI();
                        
                        setLabels = true;
                    }
                     
                }
            });
        
        //panel for modebox and buttons on graph frame
        JPanel topPanel = new JPanel();
        JPanel utilityPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(250, this.HEIGHT));
        sidePanel.setOpaque(true);
        
        // Panel for node information to go in
        JPanel sideTopPanel = new JPanel(new BorderLayout());
        sideTopPanel.setPreferredSize(new Dimension(250, 75));
        sideTopPanel.setOpaque(true);
        sidePanel.add(sideTopPanel, BorderLayout.NORTH);
        
        JPanel nodeInfoEntry = new JPanel(new BorderLayout());
        sideTopPanel.add(nodeInfoEntry, BorderLayout.NORTH);
        
        JLabel nodeInfo = new JLabel("Enter node number to get info");
        nodeInfoEntry.add(nodeInfo, BorderLayout.NORTH);
        sideTopPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        
        nodeNumber = new JTextField(8);
        nodeInfoEntry.add(nodeNumber, BorderLayout.CENTER);
        
        JButton getNodeInfo = new JButton("Get Node Info");
        sideTopPanel.add(getNodeInfo, BorderLayout.SOUTH);
        
        // Panel for other node information to go in
        //JPanel bottomRightPanel = new JPanel(new BorderLayout());
        JPanel sideBottomPanel = new JPanel(new BorderLayout());//new GridLayout(2,1));
        sideBottomPanel.setPreferredSize(new Dimension(250, (this.HEIGHT)/2));
        sideBottomPanel.setOpaque(true);
        sideBottomPanel.setBackground(Color.WHITE);
        //bottomRightPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
               
        // Create Overall graph info panel
        JPanel graphInfo = new JPanel();
        JTextArea graphInfoTextArea = new JTextArea();
        graphInfoTextArea.setBorder(null);
        
        //add static graph info to text area
        graphInfoTextArea.append("Number of Nodes in Graph: " + nodeArray.length + "\n");
        graphInfoTextArea.append("Number of Paths in Graph: " + pathArray.length + "\n"); 
        graphInfoTextArea.setEditable(false);
        graphInfo.setVisible(true);
        graphInfo.setBackground(Color.WHITE);
        graphInfo.add(graphInfoTextArea);
        graphInfo.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        graphInfo.setPreferredSize(new Dimension(250, 100));

        sidePanel.add(sideBottomPanel);
        
        //setting up text area for node info
        extraNodeInfo = new JTextArea(5, 25);
        extraNodeInfo.setEditable(false);
        JScrollPane scrollingNodeInfo = new JScrollPane(extraNodeInfo);
        scrollingNodeInfo.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollingNodeInfo.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //extraNodeInfo.setBorder(BorderFactory.createLineBorder(Color.black));
        //bottomRightPanel.add(extraNodeInfo);
        sideBottomPanel.add(scrollingNodeInfo);
        sideBottomPanel.add(graphInfo, BorderLayout.SOUTH);
        
        //add buttons and modebox to panel
        topPanel.add(utilityPanel);
        utilityPanel.add(resetButton);
        utilityPanel.add(modeBox);
        utilityPanel.add(ntButton);
        
        ActionListener getNodeInfoListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                getNodeInfo(nodeNumber.getText(), extraNodeInfo);
            }
        };
        
        getNodeInfo.addActionListener(getNodeInfoListener);
        
        //colored vertexes
        view.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<>(view.getPickedVertexState(), Color.red, Color.yellow));
        
        //vertex/edge labels and render position
        logger.info(CLASS_NAME, "Rendering Visualizer");
        view.getRenderContext().setVertexLabelTransformer(new ToStringLabeller()); 
        view.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller()); 
        view.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
        
        //window frame
        logger.info(CLASS_NAME, "Allocating Window Frame");
        graphFrame = new JFrame(CLASS_NAME);
        graphFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        graphFrame.setLayout(new BorderLayout());
        graphFrame.getContentPane().add(view, BorderLayout.CENTER);
        graphFrame.add(topPanel, BorderLayout.NORTH);
        graphFrame.add(sidePanel, BorderLayout.EAST);
        this.createMenus();
        graphFrame.pack();
        graphFrame.setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
        
        logger.info(CLASS_NAME, "UserInterface setup complete");
        logger.hideLogger();
        
        graphFrame.setVisible(true);
    }
    
    private void createMenus(){
        // create the menu bar
        menuBar = new JMenuBar();
        
        // build the save menu
        fileMenu = new JMenu( FILE_MEN_STR );
        fileMenu.getAccessibleContext().setAccessibleDescription("File Menu");
        menuBar.add(fileMenu);
        
        // construct the different save menu items
        //fileMenu_Open = new JMenuItem ("Open Input File");
        fileMenu_Save = new JMenuItem ("Save Graph as JPG");
        
        //construct session log viewer
        fileMenu_sessionLog = new JMenuItem("View Session Log");
        
        //construct help system viewer
        fileMenu_Quit = new JMenuItem("Quit");
        
        // build the algorithm menu      
        algMenu = new JMenu( ALG_MEN_STR );
        algMenu.setMnemonic(KeyEvent.VK_A);
        algMenu.getAccessibleContext().setAccessibleDescription("Algorithm Menu");
        menuBar.add(algMenu);
        
        // construct the different algorithm menu items
        algMenu_unwPath = new JMenuItem( "Unweighted Shortest Path" ); 
        algMenu_wPath = new JMenuItem( "Weighted Shortest Path" );
        
        //construct help system viewer
        helpMenu = new JMenu( HELP_MEN_STR );
        menuBar.add(helpMenu);
        helpMenu_usrManual = new JMenuItem("User Manual");

        /*
         * This sets up a keyboard shortcut, alt+2, but is disabled due
         * to conflicting with the dialog. The dialog input is catching
         * the '2' character from the alt-2 shortcut, I will address this.
         */
        //algMenu_unwPath.setAccelerator(KeyStroke.getKeyStroke(
         //   KeyEvent.VK_2, ActionEvent.ALT_MASK));
        
        // set up the actionlisteners for selecting the menu items
        ActionListener algUnwListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                instantiateUnweightedDialog();
            }            
        };
        
        ActionListener algWListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                instantiateWeightedDialog();
            }            
        };
        
        ActionListener saveListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                instantiateSaveDialog();
            }
        };
        
        ActionListener helpListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                instantiateHelpDialog();
            }
        };

       /* ActionListener openListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                instantiateOpenDialog();
            }
        };*/
        
        ActionListener viewSessionLogListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.showLogger();
            }
        };
        
        ActionListener quitApplicationListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info(CLASS_NAME, "File > Quit menu item selected, Exiting Application");
                System.exit(0);
            }
        };
                
        // add the menu item listeners so the menu knows what to do when an item is selected
        algMenu_unwPath.addActionListener(algUnwListener);
        algMenu_wPath.addActionListener(algWListener);
        fileMenu_Save.addActionListener(saveListener);
        //fileMenu_Open.addActionListener(openListener);
        fileMenu_sessionLog.addActionListener(viewSessionLogListener);

        helpMenu_usrManual.addActionListener(helpListener);

        fileMenu_Quit.addActionListener(quitApplicationListener);
        
//        saveMenu_Save.addActionListener(saveListener);
        
        // add the menu items
        algMenu.add(algMenu_unwPath);
        algMenu.addSeparator();
        algMenu.add(algMenu_wPath);
        //fileMenu.add(fileMenu_Open);
        //fileMenu.addSeparator();
        fileMenu.add(fileMenu_Save);
        fileMenu.addSeparator();
        fileMenu.add(fileMenu_sessionLog);
        
        //Add User Manual to Help Menu
        helpMenu.add(helpMenu_usrManual);
        fileMenu.addSeparator();
        fileMenu.add(fileMenu_Quit);
        
        graphFrame.setJMenuBar(menuBar);    // set the menu bar
    }
    
    /*
     * instantiateUnweightedDialog()
     * 
     * This method instantiates an independent pop-up dialog for getting
     * a users selection for source/destination nodes to perform the algorithm
     * on. The result is then sent back to the dialog to be displayed.
     */
    private void instantiateUnweightedDialog(){
        String result = "";
        
        // create a new dialog and wait for user input
        AlgorithmDialog ad = new AlgorithmDialog( this.graphFrame, true, "Unweighted Shortest Path" );
        
        // once the user has pressed 'OK' the diaglog will return, now perform the algorithm based on the users input
        result = unweightedShortestPath( nodeArray[ad.getSourceNodeIndex()-1], nodeArray[ad.getDestNodeIndex()-1] );
        
        // send the results back to the dialog
        ad.setResults(result);
        
        // set the dialog to be visible so the results can be read
        ad.setVisible(true);
        
        // once the user clicks 'OK' again, the dialog will return
        // disposing the dialog will release its resources
        ad.dispose();
    }
    
     /*
     * instantiateWeightedDialog()
     * 
     * This method instantiates an independent pop-up dialog for getting
     * a users selection for source/destination nodes to perform the algorithm
     * on. The result is then sent back to the dialog to be displayed.
     */
    private void instantiateWeightedDialog(){
        String result = "";
        
        // create a new dialog and wait for user input
        AlgorithmDialog ad = new AlgorithmDialog( this.graphFrame, true, "Weighted Shortest Path" );
        
        // once the user has pressed 'OK' the diaglog will return, now perform the algorithm based on the users input
        result = weightedShortestPath( nodeArray[ad.getSourceNodeIndex()-1], nodeArray[ad.getDestNodeIndex()-1] );
        
        // send the results back to the dialog
        ad.setResults(result);
        
        // set the dialog to be visible so the results can be read
        ad.setVisible(true);
        
        // once the user clicks 'OK' again, the dialog will return
        // disposing the dialog will release its resources
        ad.dispose();
    }
    
     /*
     * instantiateSaveDialog()
     * 
     * Method for save code
     */
    private void instantiateSaveDialog(){
        
        
        view.setDoubleBuffered(false);
        
        int width = view.getWidth();
        int height = view.getHeight();
        
        JFrame saveFrame = new JFrame();
        JFileChooser fileDialog = new JFileChooser(".");
        int saveChoice = fileDialog.showSaveDialog(saveFrame);        
        File selectedFile = null;
        
        
        if (saveChoice == JFileChooser.APPROVE_OPTION)
        {
            selectedFile = fileDialog.getSelectedFile();
        }

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bi.createGraphics();
        view.paint(graphics);
        graphics.dispose();
        
        try {
             ImageIO.write(bi, "jpg", selectedFile);
             logger.info(CLASS_NAME, "Successfully created JPG image of graph");
        } catch (Exception e) {
             e.printStackTrace();
             logger.severe(CLASS_NAME, "Error creating image: exception thrown while writing JPG");
        }  
        view.setDoubleBuffered(true);
    }
    
     /*
     * instantiateOpenDialog()
     * 
     * Method for displaying file opener dialogue at program instantiation
     */
    public File instantiateOpenDialog(){
        JFrame openFrame = new JFrame();
        JFileChooser fileDialog = new JFileChooser(".");
        int openChoice = fileDialog.showOpenDialog(openFrame);        
        File selectedFile = null;
        
        
        if (openChoice == JFileChooser.APPROVE_OPTION)
        {
            selectedFile = fileDialog.getSelectedFile();
            logger.info(CLASS_NAME, "Input File Specified as \"" + selectedFile.getName() + "\".");
        }
        else
        {
            selectedFile = new File("nodes.xml");
            logger.info(CLASS_NAME, "Default input file specified (input.xml)");
        }
        
        return selectedFile;
    }    
    
    // Method for attempting to open User Manual (stored in program directory)
    private void instantiateHelpDialog () {
        if (Desktop.isDesktopSupported()) {
            try {
                 File myFile = new File("./UserManual.pdf");
                 if(!myFile.exists()) {
                     logger.warning(CLASS_NAME, "**User Manual Missing from Program Directory**\n"
                             + "***************** UserManual.pdf should reside in " + System.getProperty("user.dir") + "\n"
                             + "***************** available at project website **************");
                     logger.showLogger();
                     return;
                 }
                 Desktop.getDesktop().open(myFile);
                 logger.info(CLASS_NAME, "User accessed user manual");
            } catch (IOException ex) {
                // no application registered for PDFs
                logger.warning(CLASS_NAME, "User does not have appropriate pdf reader installed");
            }
        }
    }
    
    private void getNodeInfo(String ID, JTextArea nodeInfo) {
        Integer Integer = new Integer(0);
        int i, id;
        int currID = 0, j = 0;
        String infoNodeIP;
        
        try {
            id = Integer.parseInt(ID);
        }
        catch(NumberFormatException e) {
            nodeInfo.setText("Invalid Node ID: Must Be A Number");
            logger.info(CLASS_NAME, "Non-Integer entered for ID quary in Get Node Info");
            return;
        }
        
        if(id < 1 || id > nodeArray.length) {
            nodeInfo.setText("Invalid Node ID: Out of Range");
            logger.info(CLASS_NAME, "User entered invalid node ID in Get Node Info");
            return;
        }
        
        
        
        id = id - 1;
        
        nodeInfo.setText("");
        nodeInfo.append("Node ID: " + nodeArray[id].getID() + "\n");
        nodeInfo.append("IP Address: " + nodeArray[id].getIP() + "\n");
        nodeInfo.append("Neighboring Nodes:\n");
        
        infoNodeIP = nodeArray[id].getIP();
        
        for(i=0; i<pathArray.length; i++) {
            if(pathArray[i].getNodeAIP().equals(infoNodeIP)) {
                for(j = 0; j<nodeArray.length; j++) {
                    if(nodeArray[j].getIP().equals(pathArray[i].getNodeBIP())) {
                        currID = nodeArray[j].getID();
                        break;
                    }
                }
                nodeInfo.append("   ID: " + currID + " IP Address: " + pathArray[i].getNodeBIP() + "\n");
                nodeInfo.append("        Weight: " + pathArray[i].getWeight() + "\n");
                nodeInfo.append("        Capacity: " + pathArray[i].getCapacity() + "\n");
            }
            else if(pathArray[i].getNodeBIP().equals(infoNodeIP)) {
                for(j = 0; j<nodeArray.length; j++) {
                    if(nodeArray[j].getIP().equals(pathArray[i].getNodeAIP())) {
                        currID = nodeArray[j].getID();
                        break;
                    }
                }
                nodeInfo.append("   ID: " + currID + " IP Address: " + pathArray[i].getNodeAIP() + "\n");
                nodeInfo.append("        Weight: " + pathArray[i].getWeight() + "\n");
                nodeInfo.append("        Capacity: " + pathArray[i].getCapacity() + "\n");
            }
        }
        nodeInfo.setCaretPosition(0);
        
        logger.info(CLASS_NAME, "Successful Node ID query");
    }
    
    public void initGui(){
        this.constructGraph();
        this.construcLayout();        
    }
    
    /*
     * This method calculates the DijkstraShortestPath(unweighted) between any
     * two connected nodes in the graph
     * 
     * Returns a semi-formatted string to be displayed in a text area, containing
     * the path from n1 to n2, if one exist
     */
    private String unweightedShortestPath( NetworkNode n1, NetworkNode n2 ){
        DijkstraShortestPath<NetworkNode,NetworkPath> alg = new DijkstraShortestPath(netGraph);
        List<NetworkPath> pList = alg.getPath( n1, n2 );        
        //
        return "*Shortest Unweighted Path*\nFrom: " + n1.toString() + "\nTo: " + n2.toString() + "\nIs: " +
                pList.toString();
    }
    
    /*
     * This method calculates the DijkstraShortestPath(weighted) between any
     * two connected nodes in the graph
     * 
     * Returns a semi-formatted string to be displayed in a text area, containing
     * the path from n1 to n2, and the length(weight) of the path, if one exist.
     */
    private String weightedShortestPath( NetworkNode n1, NetworkNode n2 ){
        Transformer<NetworkPath, Double> wtTransf = new Transformer<NetworkPath, Double>(){
            @Override
            public Double transform( NetworkPath path ){
                return path.getWeight();
            }
        };        
        DijkstraShortestPath<NetworkNode,NetworkPath> alg = new DijkstraShortestPath(netGraph, wtTransf);
        List<NetworkPath> pList = alg.getPath( n1, n2 );
        Number dist = alg.getDistance(n1, n2);
        
        //
        return "*Shortest Weighted Path*\nFrom: " + n1.toString() + "\nTo: " + n2.toString() + "\nIs: " +
                pList.toString() + "\nPath Length: " + dist.toString();        
    }    
    
    
    /*
     * A helper class to handle the algorithm dialogs. This class will create
     * a dialog window with source/destination inputs and a text area to display
     * the results of the calculation to be performed.
     */
    private class AlgorithmDialog extends JDialog {

        private javax.swing.JLabel algLabel;    // the label for the algorithm being performed
        private String algName;                 // the name of the algorithm being performed, displayed in its label
        private javax.swing.JLabel sourceLabel; // the label for the source input
        private javax.swing.JButton jbtOK;      // the 'OK' button
        private javax.swing.JTextField sourceInput; // the source node input field
        private String sourceString = "0";      // string that contains the chars input into the source text field
        private javax.swing.JLabel destLabel;   // the destination node label
        private javax.swing.JTextField destInput;   // the destination node input field
        private String destString = "0";        // the destination input string
        private String results = "";            // string to hold the results
        private javax.swing.JTextArea resultOutput; // text area to display the results
        private javax.swing.JScrollPane resultScroll;   // in case result is too big, add scroll function to text area

        public AlgorithmDialog(JFrame frame, boolean modal, String alg ){
           super(frame, modal); // call parent, setting modal so that the dialog blocks user input to top level window
            algName = alg;  // set the algorithm name
            initComponents();   // sets up the dialog
            pack(); // set up the dialog window so everything looks nice
            setLocationRelativeTo(frame);
            setVisible(true);
        }

        /*
         * Sets up the dialog components
         * All the layouts are done absolutely, if you have trouble with linking
         * the layout methods, go to project properties, libraries, add libraries,
         * and select the Absolute Layout library.
         */
        private void initComponents(){
            
            // create all the components
            jbtOK = new javax.swing.JButton();
            sourceInput = new javax.swing.JTextField();
            sourceLabel = new javax.swing.JLabel();
            destInput = new javax.swing.JTextField();
            destLabel = new javax.swing.JLabel();
            algLabel = new javax.swing.JLabel();
            resultOutput = new javax.swing.JTextArea(5,22);   
                        
            setLayout(new BorderLayout());      // set the layout
                        
            // set the ok button text, and listener metho
            jbtOK.setText("OK");
            jbtOK.addActionListener(new java.awt.event.ActionListener() {
                    @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    OKActionPerformed(evt);
                }
            });
            
            // set the location/dimensions of the ok button
            add(jbtOK, BorderLayout.SOUTH);

            // set the source node input field listener method
            sourceInput.addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                public void keyReleased(java.awt.event.KeyEvent evt) {
                    sourceInputKeyReleased(evt);
                }
            });
            
            // set the location/dimension of the source node input field
            JPanel topPanel = new JPanel(new BorderLayout());            
            // set up the source/destination text fields
            JPanel line1 = new JPanel();
            sourceLabel.setText("Source Node (ie 1):");
            line1.add(sourceLabel, BorderLayout.WEST);
            sourceInput.setPreferredSize(new Dimension(50, 20));
            line1.add(sourceInput, BorderLayout.EAST);
            topPanel.add(line1, BorderLayout.NORTH);
            add(topPanel, BorderLayout.NORTH);
            
            // do the same for the destination node input field
            destInput.addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                public void keyReleased(java.awt.event.KeyEvent evt) {
                    destInputKeyReleased(evt);
                }
            });
            
            
            JPanel line2 = new JPanel();
            destLabel.setText("Destination Node (ie 5):");
            line2.add(destLabel, BorderLayout.WEST);
            destInput.setPreferredSize(new Dimension(50, 20));
            line2.add(destInput, BorderLayout.EAST);
            topPanel.add(line2, BorderLayout.SOUTH);

            // display the algorithm name
            algLabel.setText(algName);
            add(algLabel);
            
            resultOutput.setEditable(false);    // disable editing the result text area
            
            // set the text area to wrap words
            resultOutput.setLineWrap(true);
            resultOutput.setWrapStyleWord(true);
            
            // set the text area to be scrollable
            resultScroll = new javax.swing.JScrollPane( resultOutput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
            add(resultScroll);                
        }

        /*
         * This method gets called when the user presses 'OK', it tells the dialog
         * to no longer be visible and returns back to the caller so that either an
         * operation(alg) can be performed or to close the window after getting the results
         */
        private void OKActionPerformed(java.awt.event.ActionEvent evt){
            this.setVisible(false);
        }
                
        /*
         * This method gets called evertime a new char is entered into the source
         * input field. The character is checked to ensure it is a digit, if it is
         * the the char is concatenated to the source string. If the char is not a digit
         * then source string is set to '1' to insure fail-safe operation.
         */
        private void sourceInputKeyReleased(java.awt.event.KeyEvent evt){
            if( Character.isDigit(evt.getKeyChar()) ){
                sourceString += evt.getKeyChar();
            } else {
                sourceString = "1";
            }
        }
        
        /*
         * This method gets called evertime a new char is entered into the destination
         * input field. The character is checked to ensure it is a digit, if it is
         * the the char is concatenated to the destination string. If the char is not a digit
         * then destination string is set to '1' to ensure fail-safe operation.
         */
        private void destInputKeyReleased(java.awt.event.KeyEvent evt){
            if( Character.isDigit(evt.getKeyChar()) ){
                destString += evt.getKeyChar();
            } else {
                destString = "1";
            }
        }

        /*
         * The nodes are a 1-based index, rather than 0-based, so we only want
         * to return a value of 1 or greater, 1 corresponding to node 0 in the array.
         * To force the node index to be greater than 0, convert the string to an integer
         * and check if the value is less than 1, if so then return 1 for a fail-safe return.
         */
        
        /*
         * This method returns the integer form of the source node user input. If
         * the user entered a negative number then return 1 for fail-safe operation.
         */
        public int getSourceNodeIndex(){
            int ret = Integer.parseInt(sourceString);
            if( ret < 1 ) {
                ret = 1;
            }
            return ret;
        }
        
        /*
         * This method returns the integer form of the destination node user input. If
         * the user entered a negative number then return 1 for fail-safe operation.
         */
        public int getDestNodeIndex(){
            int ret = Integer.parseInt(destString);
            if( ret < 1 ){
                ret = 1;
            }
            return ret;
        }
        
        /*
         * This method sets the result string to be displayed in the diaglog
         * results text area. The result is constructed by the algorithms.
         */
        public void setResults( String result ){
            this.results = result;
            resultOutput.setText(results);
            resultOutput.setCaretPosition(0);
        }

    }
}
