/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.uidaho.softeng1;

/**
 *
 * @author Jon
 */
public class NetworkNode {
    private int ID; // 1 is first
    private String IP_ADDRESS;
    
    
    public NetworkNode( int id , String ip_addy ){
        if( id < 0 ){
            throw new IllegalArgumentException();
        } else {
            this.ID = id;
        }
        if( ip_addy == null ){
            throw new IllegalArgumentException();
        } else {
            this.IP_ADDRESS = ip_addy;
        }
    }   
        
    public int getID(){
         return this.ID;
     }
    
    public String getIP(){
        return this.IP_ADDRESS;
    }
    
    @Override
    public String toString(){
        return "NN" + Integer.toString(ID) + "-" + IP_ADDRESS;
    }
    
}
