/*
 * Created on Mar 29, 2006 by rob
 */
package viz;

public class BodyList {
    
    private float[] bodies;
    int iteration;
    long runTime;
    
    BodyList(float[] bodies, int iteration, long runTime) {
        this.bodies = new float[bodies.length];
        this.iteration = iteration;
        this.runTime = runTime;

        int begin = 0;
        int end = bodies.length-1;
        int dest = 0;
        
        while(begin < end) {
            this.bodies[dest+0] = bodies[begin++]; // x
            this.bodies[dest+1] = bodies[begin++]; // y
            this.bodies[dest+2] = bodies[begin++]; // z
            
            this.bodies[dest+5] = bodies[end--]; // x 
            this.bodies[dest+4] = bodies[end--]; // y
            this.bodies[dest+3] = bodies[end--]; // z
            dest += 6;
        }
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
