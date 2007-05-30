/*
 * Created on Mar 29, 2006 by rob
 */
package viz;

import java.util.Random;

public class BodyList {
    
//    private static int[] translation;
    private static int[] translation;
    private static boolean inited;
    
    
    private float[] bodies;
    int iteration;
    long runTime;
    
    BodyList(float[] bodies, int iteration, long runTime) {
        this.bodies = new float[bodies.length];
        this.iteration = iteration;
        this.runTime = runTime;

        
        if(!isInited()) {
            init(bodies.length);
        }
        
        for(int i=0; i<bodies.length; i++) {
            this.bodies[i] = bodies[translation[i]];
        }
        
        /*
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
        */
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
    
    static synchronized boolean isInited() {
        return inited;
    }
    
    static synchronized void init(int size) {
        int nbodies = size/3;
        int[] tmpTranslation = new int[nbodies];
        
        for(int i=0; i<nbodies; i++) {
            tmpTranslation[i] = i;
        }
        
        Random r = new Random(); 
        
        // now do a number of random swaps
        for(int i=0; i< nbodies*10; i++) {
            int index1 = r.nextInt(nbodies);
            int index2 = r.nextInt(nbodies);
            
            int tmp = tmpTranslation[index1];
            tmpTranslation[index1] = tmpTranslation[index2];
            tmpTranslation[index2] = tmp;
        }
        
        translation = new int[size];
        for(int i=0; i<nbodies; i++) {
            translation[i*3+0] = tmpTranslation[i] + 0;
            translation[i*3+1] = tmpTranslation[i] + 1;
            translation[i*3+2] = tmpTranslation[i] + 2;
        }
        
        inited = true;
    }
}
