/*
 * Created on Mar 29, 2006 by rob
 */
package viz;

public class BodyList {
    private float[] bodies;
    int iteration;
    long runTime;
    
    BodyList(float[] bodies, int iteration, long runTime) {
        this.bodies = bodies;
        this.iteration = iteration;
        this.runTime = runTime;
    }
    
    float[] getBodies() {
        return bodies;
    }
    
    int getIteration() {
        return iteration;
    }
    
    long getRuntime() {
        return runTime;
    }
}
