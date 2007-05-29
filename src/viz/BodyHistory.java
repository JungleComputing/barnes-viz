package viz;

public class BodyHistory extends Thread {

    protected static final int DEFAULT_HISTORY_SIZE = 5;
    
    //private final int numBodies;    
    private final int historySize;        
    
    private int index = -1;
    private int available = 0;

    private boolean fromFile = false;
        
    private BodyList[] history;   
    
    private Snapshot snapshot;   
    
    private BodyProducer producer;

    private boolean haveNewBodies = false;
    
    BodyHistory(BodyProducer producer, boolean fromFile) {
        this(producer, DEFAULT_HISTORY_SIZE, fromFile);
    }
    
    BodyHistory(BodyProducer producer, int historySize, boolean fromFile) {        
        this.fromFile = fromFile;
        this.producer = producer;
        this.historySize = historySize;       
//        this.numBodies = producer.numberOfBodies();
        
        history = new BodyList[historySize];        
        
        if (producer.fixedExtreme()) { 
            snapshot = new Snapshot(historySize, producer.getMostExtreme());
        } else { 
            snapshot = new Snapshot(historySize);
        }
        
        this.start();
    }
               
    private synchronized BodyList add(BodyList bodies) {
        
        if (fromFile) {
            while (available == historySize) {
                try {
                    wait();
                } catch(Exception e) {
                    // ignored
                }
            }
        }

        index = (index + 1) % historySize;
    
        if (available < historySize) { 
            available++;
            notifyAll();
        }
        
        BodyList old = history[index]; 
        history[index] = bodies;

        haveNewBodies = true;
        
        return old;
    }
    
    public synchronized Snapshot getSnapShot() {
        if(available == 0) return null;
/*
        while (available == 0) { 
            try { 
                wait();
            } catch (Exception e) {
                // TODO: handle exception
            } 
        }
*/
        int targetIndex = 0;
        
        for (int i=0;i<available;i++) {
            int tmp = (index + historySize - i) % historySize;                    
            snapshot.history[targetIndex++] = history[tmp]; 
        }

        if (fromFile) {
            available--;
            notifyAll();
        }

        haveNewBodies = false;
        
        return snapshot;
    }

    public synchronized boolean haveNewBodies() { 
        return haveNewBodies;
    }
    
    
    synchronized void reset() {
        available = 0;
        index = -1;
        haveNewBodies = false;
        history = new BodyList[historySize];

        if (producer.fixedExtreme()) { 
            snapshot = new Snapshot(historySize, producer.getMostExtreme());
        } else { 
            snapshot = new Snapshot(historySize);
        }
    }
    
    public void run() { 
        
        BodyList newBodies = null;         
//        float [] oldBodies = null; 
        
        while (true) {             
            newBodies = producer.getBodies(null);            
            if (newBodies != null) {
                add(newBodies);                        
            } else if (fromFile) {
                break;
            }
        }        
    }    
}
