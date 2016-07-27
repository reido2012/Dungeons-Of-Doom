import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Defines the properties and behaviours of the Server.
 *
 * @Author oreid
 * @Release 18/02/2016
 */
public class GameServer implements Runnable {
    Socket listenAtSocket;
    protected GameLogic logic;
    protected int id;
    public static HashMap<Integer, Socket> socketStore = new HashMap<>();
    PrintWriter out;

    /**
     * Constructor. Sets field values.
     * Stores a record of the socket connection through which server can listen to clients.
     * @param newSocket
     *      The socket that connects to our client.
     * @param inLogic
     *      Game logic being passed to the client
     * @param id
     *      Unique number for client
     */
    GameServer(Socket newSocket, GameLogic inLogic, int id) {
        this.listenAtSocket = newSocket;
        this.logic = inLogic;
        this.id =  id;

    }

    public static void main(String[] args) throws IOException{
        //Creates a server socket at port 40004
        ServerSocket serverSocket = new ServerSocket(20004);
        //Creates an instance of game logic
        GameLogic game = new GameLogic();

        //id we assign to each client
        int counter = 0;
        System.out.println("Server is running..");
        //Uses example map
        game.setMap(new File("..\\maps\\example_map.txt"));

        //Continually searches for clients to accept
        //When they are accepted we store that socket with a PrintWriter and create a new thread for the client
        while (true) {
            Socket clientSocket = serverSocket.accept();
            game.initiatePlayer(counter);
            System.out.println("A player has connected");
            socketStore.put(counter, clientSocket);
            new Thread(new GameServer(clientSocket, game,counter++)).start();
        }
    }

    /**
     * This method is called when a thread starts.
     * Handles what is sent to the client and what we do when we receive input from client.
     */
    @Override
    public void run() {

        try{
            //Takes in input from client
            BufferedReader in = new BufferedReader(new InputStreamReader(listenAtSocket.getInputStream()));
            //Sends input to client
            out = new PrintWriter(listenAtSocket.getOutputStream(), true);
            out.println("You may now use MOVE, LOOK, QUIT and any other legal commands");

            //Continually reads input from client
            while(true){
                String s = in.readLine();

                if(s == null){
                    return;
                }

                if (s.equals("QUIT")){
                    out.println("The game will now exit.");
                    closeSocket(listenAtSocket);
                }else if(s.contains("CHAT: ")){
                    sendChatMessage(s, id);
                }else{
                    //Input from client is passed to game logic and returned value is written out to the user
                    out.println(logic.parseCommand(s, id ));
                }
            }

        }catch(SocketException se){
           System.err.println("Connection to client has been lost");
            closeSocket(listenAtSocket);
        }catch (IOException e){
            System.err.println("A player has quit.");
            System.err.println(e.getMessage());
            closeSocket(listenAtSocket);
        }

        finally {
            logic.playerMap.remove(id);
        }
    }

    /**
     * Closes a socket.
     * @param s
     *      The socket we're going to close.
     */
    public void closeSocket(Socket s){
        try {
            s.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Disconnects player from the server.
     * @param id
     *      The id number of the player we are removing from the server.
     */
    public static void disconnectFromServer(int id){
        try {
            Socket s = socketStore.get(id);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.println("You are being disconnected");
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a congratulatory message to the player who won the game.
     * @param id
     *      The id of the player who won the game
     */
    public static void playerHasWonMessage(int id){
        Socket s = socketStore.get(id);
        try {
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.println(System.lineSeparator() + "Congratulations!!!"+ System.lineSeparator() + "You have escaped the Dungeon of Dooom!!!!!!"+ System.lineSeparator()
                    + "Thank you for playing!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends message to each player currently playing the game
     * @param message
     *      The message a player wants to send to the other players
     * @param currentPlayerID
     *      The id of the client who has sent the message
     */
    public void sendChatMessage(String message, int currentPlayerID){
        //Loop over HashMap of each player's socket and get their id's
        Iterator iterator  = socketStore.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry)iterator.next();
            //Get socket of each player
            Socket socket = (Socket) me.getValue();
            //Players id is the key of the socket store
            int id = (int) me.getKey();

            //write to each players socket
            try {
                if (id == currentPlayerID){
                    //We don't need to change add any identifiers to the string
                    //We already have the PrintWriter so we don't need to make one
                    this.out.println(message);
                }else{
                    //Create a PrintWriter from the player's socket  and write to the socket
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(message + " FROM Player: " + id);
                }
            } catch (IOException e) {
                System.out.println("Player " + id + " has disconnected or doesn't exist - Message not sent.");
            }
        }



    }





}
