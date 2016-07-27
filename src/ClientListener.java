import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Defines properties and behaviours of a ClientListener.
 * @Author oreid
 * @Release 24/02/2016
 */
public class ClientListener extends Thread{
    private BufferedReader in;
    private ServerMessageHandler handler;
    private ServerChatHandler chatHandler;
    /**
     * Constructor. Sets field values.
     * @param in
     *     The buffered reader we are using to listen to the server.
     */
    public ClientListener (BufferedReader in, ServerMessageHandler handler, ServerChatHandler chatHandler){
        this.in = in;
        this.handler = handler;
        this.chatHandler = chatHandler;
    }

    /**
     * Listens to the input from the server.
     */
    @Override
    public void run() {
        String receivedMsg;

        while(true){
            try{
                //Reads input from server
                //Get message from server and displays it on server terminal
                if(in.ready()){
                    receivedMsg = in.readLine();
                    //Parse received message check if it contains the keyword CHAT
                    //Sends message to GUI
                    if(receivedMsg.contains("CHAT: ")){
                        //Passes the message to the chatHandler if it is a message
                        this.chatHandler.handleChat(receivedMsg);
                    }else{
                        //Send string to be processed
                        this.handler.processMessage(receivedMsg);
                    }
                }
                try{
                    Thread.sleep(5);
                }catch (InterruptedException e){

                }
            } catch(SocketException se){
                //Prevents input from user.
                ClientReader.closeInput();
                return;
            }catch (IOException e){
                System.err.println(e);
            }
        }
    }
}
