package viz;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.net.Socket;

public class CommunicatingBodyProducer implements BodyProducer {
    
    private String targetHost;
    private int targetPort;
            
    private boolean connected = false;
    private Socket socket; 
    private DataInputStream in; 
        
    private Visualization viz;
    
    CommunicatingBodyProducer(Visualization viz) {
        this.viz = viz;
        getProperties();
        connect();
    }
    
    private void getProperties() {
        try {         
            targetHost = System.getProperty("nbody.host");
            targetPort = Integer.parseInt(System.getProperty("nbody.port"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to find target host properties");    
        }
    }
            
    private void connect() { 
     
        while (!connected) { 
            try { 
                System.out.println("Setting up connection to " + targetHost 
                        + ":" + targetPort);

                socket = new Socket(targetHost, targetPort);
                socket.setReceiveBufferSize(128*1024);
                socket.setTcpNoDelay(true);
                
                in = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream(), 128*1024));
                
                connected = true;
            } catch (Exception e) {
                System.out.println("Failed to connect to application, retry..");

                try { 
                    Thread.sleep(500);                    
                } catch (Exception x) {
                    // ignore
                }
            }
        } 
    }
    
    private void close() {        
        try { 
            in.close();
        } catch (Exception e) {
            // ignore
        }
        
        try { 
            socket.close();
        } catch (Exception e) {
            // ignore
        }        
    }
        
    public BodyList getBodies(float[] old) {
        //System.out.println("Receiving!");
                
        do { 
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
            } catch (Exception e) {
                viz.resetHistory();
                System.out.println("Lost connection while receiving," 
                        + " reconnecting");
                close();                                
                connected = false;
                connect();
            }        
            
        } while (true);
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
