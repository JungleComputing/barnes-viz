package viz;

public class Snapshot {

    BodyList[] history;

    double minX;

    double maxX;

    double minY;

    double maxY;

    double minZ;

    double maxZ;

    double mostMax;

    double mostMin;

    double mostExtreme;

    double centerX = 0.0;

    double centerY = 0.0;

    double centerZ = 0.0;

    boolean fixedExtreme;

    Snapshot(int historySize) {
        history = new BodyList[historySize];
        fixedExtreme = false;
    }

    Snapshot(int historySize, float mostExtreme) {
        this.mostExtreme = mostExtreme;

        minX = minY = minZ = -mostExtreme;
        maxX = maxY = maxZ = mostExtreme;

        history = new BodyList[historySize];
        fixedExtreme = true;
    }

    public void determineExtremes() {

        if (history == null || history[0] == null || fixedExtreme) {
            return;
        }

        minX = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        minZ = Double.MAX_VALUE;

        maxX = Double.MIN_VALUE;
        maxY = Double.MIN_VALUE;
        maxZ = Double.MIN_VALUE;

        for (int i = 0; i < history[0].getBodies().length; i += 3) {
            double x = history[0].getBodies()[i];
            double y = history[0].getBodies()[i + 1];
            double z = history[0].getBodies()[i + 2];

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);

            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }

        mostMax = Math.max(Math.max(maxX, maxY), maxZ);
        mostMin = Math.min(Math.min(minX, minY), minZ);

//        System.out.println("Most max = " + mostMax);
//        System.out.println("Most min = " + mostMin);

        mostExtreme = Math.max(Math.abs(mostMax), Math.abs(mostMin));

        centerX = minX + ((maxX - minX) / 2);
        centerY = minY + ((maxY - minY) / 2);
        centerZ = minZ + ((maxZ - minZ) / 2);

//        System.out.println("Most extreme = " + mostExtreme);
    }

    int getBodyCount() {
        int bodyCount = history[0].getBodies().length / 3;
        return bodyCount;
    }
    
    int getIteration() {
        return history[0].getIteration();
    }
    
    long getRuntime() {
        return history[0].getRuntime();
    }
}
