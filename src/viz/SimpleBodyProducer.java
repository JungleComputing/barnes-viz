package viz;

public class SimpleBodyProducer implements BodyProducer {
            
    protected static final float DEFAULT_TIMESTEP = 0.4f;  

    protected int iteration = 0; 
    protected int previous = 0;
    protected int current = 1;
    
    protected float timestep;
        
    private final int numBodies;    
    
    private float [] coordinates; 
    private float [] vector; 
    
    SimpleBodyProducer(int num) {
        this(num, DEFAULT_TIMESTEP);
    }
    
    SimpleBodyProducer(int numBodies, float timestep) {       
        this.numBodies = numBodies;
        this.timestep = timestep;
        
        coordinates = new float[3*numBodies];
        vector = new float[3*numBodies];
        
        // Initialize the positions and vectors       
        for (int i=0;i<3*numBodies;i++) {            
            coordinates[i] = (float) (Math.random()-0.5);
            vector[i] = (float) (0.1*(Math.random()-0.5));            
        }                
        
        // Cheat a little 
        coordinates[0] = 1.0f;
        coordinates[1] = 1.0f;
        coordinates[2] = 1.0f;
    }

    private void move() {
        
        for (int i=0;i<3*numBodies;i++) {
            coordinates[i] += vector[i] * timestep;
            
            // Let the particles bounce of the walls!
            if (coordinates[i] > 1.0) {
                coordinates[i] = (float) (1.0 - (coordinates[i]-1.0));                                                
                vector[i] = -vector[i];               
            }
            
            if (coordinates[i] < -1.0) {
                coordinates[i] = (float) (-1.0 + (coordinates[i]+1.0));                                                
                vector[i] = -vector[i];               
            }
            
            // Add some random noise...
            vector[i] += 0.01 * (Math.random()-0.5);
            
            // Limit the speed of the particles
            if (vector[i] > 0.1f) { 
                vector[i] = 0.1f;
            }
            
            if (vector[i] < -0.1f) { 
                vector[i] = -0.1f;
            }            
        }
    }
    
    public float getMostExtreme() {
        return 1.0f;
    }
            
    public BodyList getBodies(float[] old) {
        
        try { 
            Thread.sleep(50);
        } catch (Exception e) {
            // TODO: handle exception
        }
        
        move();

        if (old == null) {
            old = new float[3*numBodies];
        } 
        
        System.arraycopy(coordinates, 0, old, 0, coordinates.length);
        return new BodyList(old, 0, 0);
    }

    public boolean fixedExtreme() {       
        return true;
    }
    public void end() {
        // do nothing
    }

}
