import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * This class defines properties and methods of the client listener for the bot.
 *
 * @Author oreid
 * @Release 02/03/2016
 */
public class BotClientListener extends Thread {
    private BufferedReader in;
    public ArrayList<String> replyQueue = new ArrayList<>();

    /**
     * Constructor. Sets field values.
     * @param in
     *  Buffered Reader that is receiving input from the server
     */
    public BotClientListener (BufferedReader in){
        this.in = in;
    }

    /**
     * This method reads a line from the buffered reader(server) and adds it to a queue to be sent to the Bot
     */
    @Override
    public void run() {
        String receivedMsg;

        while(true){
            try{
                //Reads input from server
                receivedMsg = in.readLine();
                //Adds input to reply queue
                addReplyToQueue(receivedMsg);

            }catch(SocketException se){
                System.err.println("Lost connection to server");
                System.exit(0);
            }catch (IOException e){
                System.err.println(e);
            }
        }
    }

    /**
     * Adds a string(received message from server) to a Queue.
     * @param s
     *  The string to be added to the queue.
     */
    public void addReplyToQueue(String s){
        replyQueue.add(s);
    }

    /**
     * Get the messages sent from server
     * @return
     *  The messages sent from server as one string.
     */
    public String getReply(){
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String toSend = "";
        for (int i = 0; i < replyQueue.size(); i++) {
            if(i < replyQueue.size() - 1){

                toSend += replyQueue.get(i) + System.lineSeparator();
            }else{
                toSend += replyQueue.get(i);
            }
        }

        replyQueue.removeAll(replyQueue);

        return toSend;
    }


}
