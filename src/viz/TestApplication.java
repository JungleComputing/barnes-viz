package viz;

public class TestApplication {

    private static SimpleBodyProducer producer;
    private static BodySender sender;
        
    private static void sendBodies() {      
        sender.sendBodies(producer.getBodies(null).getBodies());
    }
    
    public static void main(String[] args) {
     
        int bodies = 1000;       
        
        if (args.length == 1) { 
            bodies = Integer.parseInt(args[0]);
        }
        
        producer = new SimpleBodyProducer(bodies);
        sender = new BodySender();
                
        while (true) { 
            sendBodies();        
        }
    }   
}
