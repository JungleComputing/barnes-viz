package util;

/**
 *  
 * Simple 3-dimensional Vector 
 *  
 * @author Jason Maassen
 * @version 1.0 Apr 15, 2005
 * @since 1.0
 * 
 */
public class Vector {
    public double x, y, z;
    
    public Vector() {     
    }
    
    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
        
    public void sub(Vector v) { 
        x -= v.x;
        y -= v.y;
        z -= v.z;        
    }
    
    public void add(Vector v) {
        x += v.x;
        y += v.y;
        z += v.z;
    }
    
    public void div(double d) {
        if (d == 0.0) {
            throw new ArithmeticException("Division by zero");           
        }
        
        double recip = 1.0 / d;
        x *= recip;
        y *= recip;
        z *= recip;        
    }        
}
