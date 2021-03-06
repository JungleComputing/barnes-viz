package viz;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class BodySender extends Thread {

    private int maxLen = 10;
    private int port;
    
    private ServerSocket server;
    
    private boolean haveClient = false;
    private Socket client;    
    private DataOutputStream out;
    
    
    private LinkedList list = new LinkedList();
    
    public BodySender() {         
        getProperties();  
        start();
    }
    
    private void getProperties() {
        try {         
            port = Integer.parseInt(System.getProperty("nbody.port", "9889"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to find host properties");    
        }
    }

    private void createServerSocket() { 
        
        while (server == null) { 
            try { 
                server = new ServerSocket(port);
            } catch (Exception e) {
                System.out.println("Failed to create server socket, retry");
                
                try { 
                    Thread.sleep(1000);
                } catch (Exception x) {
                    // ignore
                }
            }
        }
    }
        
    private void close() { 
        try { 
            out.close();
        } catch (Exception e) {
            // ignore            
        }
        
        try { 
            client.close();
        } catch (Exception e) {
            // ignore
        }
        
        haveClient = false;
    }
    
    private void accept() { 
        
        while (!haveClient) {
            try { 
                client = server.accept();
                client.setSendBufferSize(64*1024);
                client.setTcpNoDelay(true);
                
                out = new DataOutputStream(
                        new BufferedOutputStream(client.getOutputStream()));
                
                haveClient = true;
            } catch (Exception e) {                
                System.out.println("Failed to accept client, retry");                
                close();
            }
        } 
    }
    
    public synchronized void sendBodies(float [] bodies) {
        
        if (list.size() > maxLen) { 
            list.removeFirst();            
            System.out.println("dropping bodies");
        }
                
        list.addLast(bodies);
        notifyAll();
    }        

    private synchronized float [] getBodies() { 
        
        while (list.size() == 0) { 
            try { 
                wait();
            } catch (Exception e) {
                // ignore
            }
        }
           
        return (float []) list.removeFirst();
    }
    
    private void doSend() { 
                        
        try { 
          //  System.out.println("Sending");
            
            float [] bodies = getBodies();
            
            out.writeInt(bodies.length/3);
            
            for (int i=0;i<bodies.length;i++) { 
                out.writeFloat(bodies[i]);
            }
            
            out.flush();
            
         //   System.out.println("Sending Done");
            
            
        } catch (Exception e) {
            System.out.println("Lost connection during send!");                
            close();
        }
    }
        
    public void run() { 
 
        createServerSocket();
     
        while (true) {
            
            if (!haveClient) { 
                accept();
            }
            
            doSend();
        }        
    }    
}
