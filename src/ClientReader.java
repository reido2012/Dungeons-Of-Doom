import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Defines the properties and behaviours of a Client Reader.
 * @Author oreid
 * @Release 24/02/2016
 */
public class ClientReader extends Thread {
    //Fields
    private final PrintWriter out;
    private static BufferedReader stdIn;

    /**
     * Constructor. Sets field values.
     * @param out
     *      The PrintWriter that will send messages to the server
     */
    public ClientReader(PrintWriter out){
        this.out = out;
    }

    @Override
    public void run() {
        String userInput;

        try{
            //Input from user
            stdIn = new BufferedReader(new InputStreamReader(System.in));

            //Reads user input when buffered reader has something to read
            //It then sends it to the server
            while(true){
                    if (stdIn.ready()){
                        userInput = stdIn.readLine();
                        out.println(userInput);
                    }
            }

        }catch (IOException e){
            System.err.println("You've been disconnected.");
        }
    }

    /**
     * Closes the buffered reader.
     */
    public static void closeInput(){
        try {
            stdIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
