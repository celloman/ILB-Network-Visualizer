/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.uidaho.softeng1;

/**
 *
 * @author Jon
 */
public class NetworkPath {
    private int ID; //starts at 1
    private double capacity;
    private double weight;
    private String nodeAIP, nodeBIP;   // used in the input...
    
     public NetworkPath( int id , double capacity, double weight ){
        if( id < 0 ){
            throw new IllegalArgumentException();
        } else {
            this.ID = id;
        }
        if( capacity < 0 ){
            throw new IllegalArgumentException();
        } else {
            this.capacity = capacity;
        }
        if( weight < 0 ){
            throw new IllegalArgumentException();
        } else {
            this.weight = weight;
        }
    }
     
     /*
      * this constructor is used by an edgefactory, as a temp object, and hence
      * does not need a unique ID, using -1 to indicate this for now
      */
     public NetworkPath( double capacity, double weight ){        
        if( capacity < 0 ){
            throw new IllegalArgumentException();
        } else {
            this.capacity = capacity;
        }
        if( weight < 0 ){
            throw new IllegalArgumentException();
        } else {
            this.weight = weight;
        }
        this.ID = -1;
    }
  
     public void setNodeA( String IP ){
         if( IP == null ) throw new IllegalArgumentException();
         else {
             this.nodeAIP = IP;
         }
     }
     
     public void setNodeB( String IP ){
         if( IP == null ) throw new IllegalArgumentException();
         else {
             this.nodeBIP = IP;
         }
     }
     
     public String getNodeAIP(){
         return nodeAIP;
     }
     
     public String getNodeBIP(){
         return nodeBIP;
     }
     
     public void setWeight( double w ){
         this.weight = w;
     }
     
     public void setCapacity( double c ){
         this.capacity = c;
     }
     
     public double getCapacity(){
         return this.capacity;
     }
     
     public double getWeight(){
         return this.weight;
     }
     
     public int getID(){
         return this.ID;
     }
        
    @Override
    public String toString(){
        return "NP" + Integer.toString(ID);
    }
    
}

