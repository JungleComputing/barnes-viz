package util;

import util.Output;
import util.Vector;
import util.RandomNumber;

/**
 *  
 * Class that generates a plummer sphere containing a given number of particles.
 *  
 * @author Jason Maassen
 * @version 1.0 Apr 15, 2005
 * @since 1.0
 * 
 */
public class Plummer {

    // The offset used to calculate the second half of the particles.
    private static final double OFFSET = 4.0;
        
    public static Particle [] generate(int particles) {
        return generate(particles, new RandomNumber(), 1.0);
    } 
        
    public static Particle [] generate(int particles, RandomNumber random) {
        return generate(particles, random, 1.0);
    } 
    
    public static Particle [] generate(int particles, double totalMass) {
        return generate(particles, new RandomNumber(), totalMass);
    } 
    
    public static Particle [] generate(int particles, int seed, double totalMass) {
        return generate(particles, new RandomNumber(seed), totalMass);
    } 
       
    public static Particle [] generate(int particles, RandomNumber random, 
            double totalMass) {
              
        // Result of the plummer sphere generation
        Particle [] result = new Particle[particles];
                
        // Temporary objects used to store position and velocity        
        Vector pos = new Vector();            
        Vector vel = new Vector();
        
        // Temporary objects used to calculate average position and velocity        
        Vector cmr = new Vector();
        Vector cmv = new Vector();
        
        // Half of the particles (rounded up)
        final int halfParticles = (particles + 1) / 2;

        // Mass of each body
        final double mass = totalMass / particles;
        
        double rsc = 9.0 * Math.PI / 16.0;
        double vsc = Math.sqrt(1.0 / rsc);
                       
        // Now traverse over the particles. Note that the second half of 
        // the particles is a copy of the first half plus an offset.
        for (int i=0;i<halfParticles;i++) {

            double r;
        
            do {
                r = 1/Math.sqrt(Math.pow(random.xRand(0.0, 0.999),-2.0/3.0)-1);
            } while (r > 9.0);
                    
            random.pickShell(pos, rsc * r);
            cmr.add(pos);            
        
            double x, y;
        
            do {
                x = random.xRand(0.0, 1.0);
                y = random.xRand(0.0, 0.1);
            } while (y > x * x * Math.pow(1 - x * x, 3.5));

            double v = Math.sqrt(2.0) * x / Math.pow(1 + r * r, 0.25);

            random.pickShell(vel, vsc * v);
            cmv.add(vel);
            
            // Create a particle with the calculated position and velocity
            result[i] = new Particle(i, mass, 
                    pos.x, pos.y, pos.z, 
                    vel.x, vel.y, vel.z);

            // See if we can create another particle
            if (halfParticles + i < particles) { 
                
                // We can, so add an offset to the position ....
                pos.add(new Vector(OFFSET, OFFSET, OFFSET));
                
                // ... and use this to create another particle
                result[halfParticles+i] = new Particle(i, mass, 
                        pos.x, pos.y, pos.z, 
                        vel.x, vel.y, vel.z);
                
                // Not sure if these are right, but Suel does the same ...
                cmr.add(new Vector(3*pos.x, 2*pos.y, pos.z));
                cmv.add(new Vector(3*vel.x, 2*vel.y, vel.z));
            }
        } 

        // Now normalize all particles by calculating the average position and 
        // velocity, and substracting this from all particles.        
        cmr.div(particles);
        cmv.div(particles);

        for (int i=0;i<particles;i++) {
            result[i].positionX -= cmr.x;
            result[i].positionY -= cmr.y;
            result[i].positionZ -= cmr.z;

            result[i].velocityX -= cmv.x;
            result[i].velocityY -= cmv.y;
            result[i].velocityZ -= cmv.z;            
        } 
        
        // All done!
        return result;
    }	
    
    private static void usage() { 
        System.err.println("Usage: Plummer [number of particles] " + 
        	"<-s random seed> <-m total mass> -t <start time>");
        System.exit(1);   
    }
    
    public static void main(String[] args) {
        
        try { 
            int particles = Integer.parseInt(args[0]);
            int seed = 123;
            double mass = 1.0;
            double time = 0.0;
            
            for (int i=1;i<args.length;i+=2) { 
                if (args[i].equals("-s")) { 
                    seed = Integer.parseInt(args[i+1]);                    
                } else if (args[i].equals("-m")) { 
                    mass = Double.parseDouble(args[i+1]);
                } else if (args[i].equals("-t")) { 
                    time = Double.parseDouble(args[i+1]);
                }
            }
            
           Particle [] result = generate(particles, seed, mass);
           
           Output out = new Output(false);
           
           out.println(particles);
           out.println(time);
           
           for (int i=0;i<particles;i++) { 
               result[i].printShort(out);
               out.println();
           }
           
           out.close();
           
        } catch (Exception e) { 
            usage();
        }
    }
}
