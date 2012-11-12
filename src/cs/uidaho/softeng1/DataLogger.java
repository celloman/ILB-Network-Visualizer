/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.uidaho.softeng1;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.*;
import java.io.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Jon Lamb
 * This object is a thread-safe way to log to a file from all threads
 * Should make debugging easier as it isn't very helpful to print things
 * to stdout, messages are appended to the log file, their class name and
 * message level are appended to a timestamp
 * use the appropriate level for your log info/debugging -> severe, warning, info
 */
public class DataLogger {
    private static Logger logger;
    private static final String log_file = "log_file";  // log file name
    private static final String log_file_type = ".txt"; // log file extension
    private static final String CLASS_NAME = "DataLogger";

    //used in displaying current session's log info
    private static JFrame logFrame;
    private static JTextArea logTextArea;
    private static JScrollPane scrollPane;
    private static boolean logVisible = true;
    private static JButton hideLogButton;
    
    static {
        try {
            //Frame for session log display
            logFrame = new JFrame("Session Log");  
            logFrame.getContentPane().setLayout(new BorderLayout());
        
            // Create Scrolling Text Area
            logTextArea = new JTextArea(25, 50);
            //logTextArea.setLineWrap(true);
            //logTextArea.setWrapStyleWord(true);
            logTextArea.setEditable(false);
            scrollPane = new JScrollPane(logTextArea);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            
            //Create hide button
            hideLogButton = new JButton("Hide");
            hideLogButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    hideLogger();
                }
            });
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(hideLogButton);
            
            logFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
            logFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
            logFrame.pack();
//            logFrame.setVisible(false);
            
            boolean append = true;
            FileHandler fh = new FileHandler( log_file+log_file_type, append);
            fh.setFormatter(new Formatter() {   // set up the log file format
                public String format(LogRecord rec) {
                    StringBuilder buf = new StringBuilder(1000);
                    buf.append(new java.util.Date());
                    buf.append(' ');
                    buf.append(rec.getLevel());
                    buf.append(' ');
                    buf.append(formatMessage(rec));
                    buf.append('\n');
                    return buf.toString();
                    }
                });

            // tell the logger to not print to stdout
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            if (handlers[0] instanceof ConsoleHandler) {
                rootLogger.removeHandler(handlers[0]);
            }

            // set up the log file
            logger = Logger.getLogger(log_file);
            logger.addHandler(fh);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        logger.info( "Instantiated new DataLogger\r");
    }
        
    public synchronized void severe( String class_name, String msg ){
        if( logger != null ){
            logger.log( Level.SEVERE, " :{0}: {1}\r", new Object[]{class_name, msg});
            logTextArea.append("SEVERE :" + class_name + ": " + msg + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        }
    }
    
    public synchronized void warning( String class_name, String msg ){
        if( logger != null ){
            logger.log( Level.WARNING, " :{0}: {1}\r", new Object[]{class_name, msg});
            logTextArea.append("WARNING :" + class_name + ": " + msg + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        }
    }
    
    public synchronized void info( String class_name, String msg ){
        if( logger != null ){
            logger.log( Level.INFO, " :{0}: {1}\r", new Object[]{class_name, msg});
            logTextArea.append("INFO :" + class_name + ": " + msg + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        }
    }
    
    public static void hideLogger() {
        logVisible = false;
        logFrame.setVisible(logVisible);
    }
    
    public void showLogger() {
        if( logVisible == false ) {
            logVisible = true;
            logFrame.setVisible(logVisible);
        }
        else {
            logFrame.setVisible(logVisible);
        }
    }
}
