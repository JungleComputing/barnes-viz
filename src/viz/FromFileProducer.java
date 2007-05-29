package viz;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class FromFileProducer implements BodyProducer {
    
    private DataInputStream in; 
        
    FromFileProducer(String filename) throws IOException {
        in = new DataInputStream(
                new BufferedInputStream(
                    new FileInputStream(filename), 128*1024));
    }
    
    private void close() {        
        try { 
            in.close();
        } catch (Exception e) {
            // ignore
        }
    }
        
    public BodyList getBodies(float[] old) {
        //System.out.println("Receiving!");
                
        try { 
            int numBodies = in.readInt();
            int iteration = in.readInt();
            long runTime = in.readLong();
            
            //System.out.println("Reading " + numBodies + " bodies");
            
            if (old == null || old.length != numBodies) { 
                old = new float[3*numBodies];
            }
            
            for (int i=0;i<3*numBodies;i++) { 
                old[i] = in.readFloat();
            }
            
            return new BodyList(old, iteration, runTime);
            
        } catch (EOFException e) {
            close();
            System.exit(0);
            return null; // avoid warning
        } catch (Exception e2) {
            System.out.println("got exception while reading: " + e2);
            close();
            return null;
        }        
    }

    public float getMostExtreme() {
        return Float.POSITIVE_INFINITY;
    }
    
    public boolean fixedExtreme() {
        return false;
    }       
    public void end() {
        // do nothing
    }

}
