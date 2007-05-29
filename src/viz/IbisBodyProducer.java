package viz;

import ibis.ipl.Ibis;
import ibis.ipl.IbisCapabilities;
import ibis.ipl.IbisFactory;
import ibis.ipl.PortType;
import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.Registry;

import java.util.Properties;

public class IbisBodyProducer implements BodyProducer {

    private boolean connected = false;

    private Visualization viz;

    private ReceivePort rport;
    private Ibis ibis;

    
    IbisBodyProducer(Visualization viz) {
        this.viz = viz;
        init();
    }

    private void init() {
        try {
            IbisCapabilities s = new IbisCapabilities(
                IbisCapabilities.ELECTIONS);
            PortType t = new PortType(PortType.SERIALIZATION_OBJECT,
                PortType.COMMUNICATION_RELIABLE,
                PortType.CONNECTION_ONE_TO_ONE,
                PortType.RECEIVE_EXPLICIT);

            Properties p = new Properties();
            p.setProperty("ibis.serialization", "ibis");
            p.setProperty("ibis.pool.name", "barnes-viz");
            p.setProperty("ibis.server.address", System.getProperty("nbody.host"));
            
            ibis = IbisFactory.createIbis(s, p, true, null, t);

            Registry registry = ibis.registry();
            registry.elect("barnesViz");

            rport = ibis.createReceivePort(t, "barnes-viz-port");
            rport.enableConnections();
            connected = true;
        } catch (Exception e) {
            System.err.println("failed to init ibis: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void close() {
        if(connected) {
            try {
                if(rport != null) rport.close(1000);
            } catch (Exception e) {
                // ignore
            }

            try {
                if(ibis != null) ibis.end();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public BodyList getBodies(float[] old) {
        //System.out.println("Receiving!");

        do {
            try {
                ReadMessage m = rport.receive();
                
                int numBodies = m.readInt();
                int iteration = m.readInt();
                long runTime = m.readLong();

                //System.out.println("Reading " + numBodies + " bodies");

                if (old == null || old.length != numBodies) {
                    old = new float[3 * numBodies];
                }

                for (int i = 0; i < 3 * numBodies; i++) {
                    old[i] = m.readFloat();
                }

                m.finish();
                
                return new BodyList(old, iteration, runTime);
            } catch (Exception e) {
                viz.resetHistory();
                System.err.println("Lost connection while receiving,"
                    + " reconnecting");
                close();
                connected = false;
                init();
            }

        } while (true);
    }

    public float getMostExtreme() {
        return Float.POSITIVE_INFINITY;
    }

    public boolean fixedExtreme() {
        return false;
    }
}
