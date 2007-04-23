package util;

/**
 *  
 * This is a random number generator ported from the Suel code.  
 *  
 * @author Jason Maassen
 * @version 1.0 Apr 15, 2005
 * @since 1.0
 * 
 */

public class RandomNumber {

    private static final int rnMult = 1103515245;

    private static final int rnAdd = 12345;

    private static final int rnMask = 0x7fffffff;

    private static final double rnTwoTo31 = 2147483648.0;

    private int rnA;
    private int rnB;
    private int rnRandX;
    private int rnLastRand;
 
    public RandomNumber() {
        setSeed(123);
    }

    public RandomNumber(int seed) {
        setSeed(seed);
    }
   
    public void setSeed(int Seed) {
        rnA = 1;
        rnB = 0;
        rnRandX = ((rnA * Seed + rnB) & rnMask);
        rnA = ((rnMult * rnA) & rnMask);
        rnB = ((rnMult * rnB + rnAdd) & rnMask);
    }

    public double pRand() {
        rnLastRand = rnRandX;
        rnRandX = ((rnA * rnRandX + rnB) & rnMask);
        return rnLastRand / rnTwoTo31;
    }

    public double xRand(double low, double high) {
        return (low + (high - low) * pRand());
    }

    public void pickShell(Vector point, double Radius) {

        double rsq, rsc;

        do {

            point.x = xRand(-1.0, 1.0);
            point.y = xRand(-1.0, 1.0);
            point.z = xRand(-1.0, 1.0);

            rsq = point.x * point.x + point.y * point.y + point.z * point.z;

        } while (rsq > 1.0);

        rsc = Radius / Math.sqrt(rsq);

        point.x *= rsc;
        point.y *= rsc;
        point.z *= rsc;
    }
}