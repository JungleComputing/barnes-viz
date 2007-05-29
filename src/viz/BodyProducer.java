package viz;

public interface BodyProducer {    
    BodyList getBodies(float [] old);
    
    boolean fixedExtreme();
    float getMostExtreme();    
    void end();
}
