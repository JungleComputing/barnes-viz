package util;

import util.Output;

/**
 *  
 * Contains the data of a single particle. 
 *  
 * @author Jason Maassen
 * @version 1.0 Mar 31, 2005
 * @since 1.0
 * 
 */
public class Particle implements java.io.Serializable {
    
    public int number;
    public double mass;
    
    public double positionX;
    public double positionY;
    public double positionZ;
    
    public double velocityX;
    public double velocityY;
    public double velocityZ;

    transient double accelerationX;
    transient double accelerationY;
    transient double accelerationZ;
    
    transient double jerkX;
    transient double jerkY;
    transient double jerkZ;
        
    Particle() {
        // this space is intentionally left blank.
    }
    
    Particle(int number, double mass, 
             double positionX, double positionY, double positionZ,    
             double velocityX, double velocityY, double velocityZ) { 
        
        this.number = number;        
        this.mass = mass;
        
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;                
    }
    
    Particle(Particle original) { 
        
        number = original.number;        
        mass = original.mass;
        
        positionX = original.positionX;
        positionY = original.positionY;
        positionZ = original.positionZ;
        
        velocityX = original.velocityX;
        velocityY = original.velocityY;
        velocityZ = original.velocityZ;                
    }

    void copy(Particle original) { 
        
        // Note: The number and mass should be the same        
        if (number != original.number) {
            throw new Error("Eek: illegal particle copy detected!");
        }
        
        positionX = original.positionX;
        positionY = original.positionY;
        positionZ = original.positionZ;
        
        velocityX = original.velocityX;
        velocityY = original.velocityY;
        velocityZ = original.velocityZ;
        
        accelerationX = original.accelerationX;
        accelerationY = original.accelerationY;
        accelerationZ = original.accelerationZ;
        
        jerkX = original.jerkX;
        jerkY = original.jerkY;
        jerkZ = original.jerkZ;
    }
    
    void predictStep(double dt) {         
        positionX += velocityX*dt + accelerationX*dt*dt/2 + jerkX*dt*dt*dt/6; 
        velocityX += accelerationX*dt + jerkX*dt*dt/2;
        
        positionY += velocityY*dt + accelerationY*dt*dt/2 + jerkY*dt*dt*dt/6; 
        velocityY += accelerationY*dt + jerkY*dt*dt/2;
        
        positionZ += velocityZ*dt + accelerationZ*dt*dt/2 + jerkZ*dt*dt*dt/6; 
        velocityZ += accelerationZ*dt + jerkZ*dt*dt/2;
    }
    
    void correctStep(Particle o, double dt) {
        
        velocityX = o.velocityX + (o.accelerationX + accelerationX)*dt/2   
                                + (o.jerkX - jerkX)*dt*dt/12;        
        positionX = o.positionX + (o.velocityX + velocityX)*dt/2
                                + (o.accelerationX - accelerationX)*dt*dt/12;

        velocityY = o.velocityY + (o.accelerationY + accelerationY)*dt/2   
                                + (o.jerkY - jerkY)*dt*dt/12;        
        positionY = o.positionY + (o.velocityY + velocityY)*dt/2
                                + (o.accelerationY - accelerationY)*dt*dt/12;

        velocityZ = o.velocityZ + (o.accelerationZ + accelerationZ)*dt/2   
        	                    + (o.jerkZ - jerkZ)*dt*dt/12;        
        positionZ = o.positionZ + (o.velocityZ + velocityZ)*dt/2
                                + (o.accelerationZ - accelerationZ)*dt*dt/12; 
    }
    
    void reset() { 
        accelerationX = 0.0;
        accelerationY = 0.0;
        accelerationZ = 0.0;  
        
        jerkX = 0.0;
        jerkY = 0.0;
        jerkZ = 0.0;        
    }
    
    void print(Output out) {         
        out.print(number);
        out.print(" ");
        out.print(mass, 0, 16);
        out.print(" ");
        out.print(positionX, 0, 16);
        out.print(" ");
        out.print(positionY, 0, 16);
        out.print(" ");
        out.print(positionZ, 0, 16);
        out.print(" ");
        out.print(velocityX, 0, 16);
        out.print(" ");
        out.print(velocityY, 0, 16);
        out.print(" ");
        out.print(velocityZ, 0, 16);
        out.print(" ");
        out.print(accelerationX, 0, 16);
        out.print(" ");
        out.print(accelerationY, 0, 16);
        out.print(" ");
        out.print(accelerationZ, 0, 16);
        out.print(" ");
        out.print(jerkX, 0, 16);
        out.print(" ");
        out.print(jerkY, 0, 16);
        out.print(" ");
        out.print(jerkZ, 0, 16);        
    }
    
    void printShort(Output out) {
        out.print(mass, 0, 16);
        out.print(" ");        
        out.print(positionX, 0, 16);
        out.print(" ");
        out.print(positionY, 0, 16);
        out.print(" ");
        out.print(positionZ, 0, 16);
        out.print(" ");
        out.print(velocityX, 0, 16);
        out.print(" ");
        out.print(velocityY, 0, 16);
        out.print(" ");
        out.print(velocityZ, 0, 16);     
    }
}
