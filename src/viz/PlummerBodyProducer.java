package viz;

import util.Particle;
import util.Plummer;

public class PlummerBodyProducer implements BodyProducer {

    private final int numBodies;
    private final float [] coordinates;
    private int iteration = 0;
    
    PlummerBodyProducer(int numBodies) {        
        this.numBodies = numBodies;            
        this.coordinates = new float[3*numBodies];
        
        initPositionsAndVectors();
    }
        
    public void initPositionsAndVectors() { 
        // Initialize the positions and vectors
        Particle [] particles = Plummer.generate(numBodies);
        
        int index = 0;
        
        for (int i=0;i<numBodies;i++) {         
            Particle p = particles[i];                                  
            coordinates[index] = (float) p.positionX;
            coordinates[index+1] = (float) p.positionY;
            coordinates[index+2] = (float) p.positionZ;
            index += 3;
        }        
    }

    public BodyList getBodies(float[] old) {
        
        try { 
            Thread.sleep(1000);
        } catch (Exception e) {
            // TODO: handle exception
        }
            
        return new BodyList(coordinates, iteration++, 0);
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
