
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Defines the properties and behaviours of the Client.
 */
public class PlayGame {
	//Fields
	protected GameLogic logic;
	protected Scanner userInput;
	protected Player player;
	protected Socket socket;
	protected PrintWriter outToServer;
	static PlayerGUI gameWindow;
	static ChatGUI chatWindow;


	/**
	 * Constructor. Sets field values.
	 */
	public PlayGame(){
		logic = new GameLogic();
		userInput = new Scanner(System.in);
	}
	
	/**
	 * @return
	 * 	The user input.
	 */
	public String readUserInput(){
		return userInput.nextLine();
	}

	/**
	 * Prints a string
	 * @param answer
	 * 		The string it prints.
     */
	public void printAnswer(String answer){System.out.println(answer);}

	/**
	 * Selects a map from it's file name
	 * @param mapName
	 * 		The name of the file that has the map
     */
	public void selectMap(String mapName){
		logic.setMap(new File(mapName));
	}

	public static void main(String [] args) {
		final PlayGame game = new PlayGame();

		// Creates a new thread for my GUI
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				gameWindow = new PlayerGUI(game);
				chatWindow = new ChatGUI(game);
				game.connectToServer(gameWindow, chatWindow) ;


				//Gives GUI some initial information
				game.guiLook();
				game.hello();

				//Set up some general properties of player GUI
				gameWindow.setTitle("Dungeons Of Doom");
				gameWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
				gameWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

				//Set up some general properties of chat GUI
				game.chatWindow.setTitle("Dungeons Of Doom - Chat");
				game.chatWindow.setSize(400, 800);
				game.chatWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);


				//Refresh the map for client so no need to type look to see what's going on
				Runnable MapRefresh  = new Runnable() {
					@Override
					public void run() {
						game.guiLook();
					}
				};

				ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
				executor.scheduleAtFixedRate(MapRefresh, 0, 1000 , TimeUnit.MILLISECONDS);
			}
		});


	}

	/**
	 * Connects the client to the server
	 */
	public void connectToServer(ServerMessageHandler handler, ServerChatHandler chatHanlder) {
		String hostName = "localhost";
		int portNumber = 20004;

		try{
			//Creates a socket
			socket = new Socket(hostName, portNumber);

			//A buffered reader for client to get input from server
			BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			//A PrintWriter for the client to send info to server
			outToServer = new PrintWriter(socket.getOutputStream(), true);

			//Creating client listener and reader threads
			//Allows for listening and reading at the "same time"
			ClientListener listener = new ClientListener(serverIn, handler, chatHanlder);
			listener.start();
//
//			ClientReader reader = new ClientReader(outToServer);
//			reader.start();


		}catch (UnknownHostException e){
			System.err.println("Don't know about host " + hostName);
			logic.quitGame();
		} catch (SocketException e){
			System.err.println("Lost connection to server");
			logic.quitGame();
		} catch (IOException e ){
			System.err.println("Couldn't get I/O for the connection to "  + hostName);
			logic.quitGame();
		}
	}

	/**
	 * Sends the command Move North to server
	 * Then updates map players map
	 */
	public void moveNorth(){
		this.outToServer.println("MOVE N");
		guiLook();
	}

	/**
	 * Sends the command Move South to server
	 * Then updates map players map
	 */
	public void moveSouth(){
		this.outToServer.println("MOVE S");
		guiLook();
	}

	/**
	 * Sends the command Move East to server
	 * Then updates map players map
	 */
	public void moveEast(){
		this.outToServer.println("MOVE E");
		guiLook();
	}

	/**
	 * Sends the command Move West to server
	 * Then updates map players map
	 */
	public void moveWest(){
		this.outToServer.println("MOVE W");
		guiLook();
	}

	/**
	 * Sends the command Quit to the server
	 * Then closes program
	 */
	public void quit(){
		this.outToServer.println("QUIT");
		System.exit(0);
	}
	/**
	 * Sends the command pickup to server
	 * Then updates map players map
	 */
	public void pickup(){
		this.outToServer.println("PICKUP");
		guiLook();
	}
	/**
	 * Sends the command Hello to server
	 * Then updates map players map
	 */
	public void hello(){
		this.outToServer.println("HELLO");
		guiLook();

	}
	/**
	 * Sends the command Chat to server
	 * Then updates map players map
	 */
	public void chat(String message){
		//Keyword for server to pick up on is CHAT
		this.outToServer.println("CHAT: " + message);
	}

	/**
	 * Sends the command Available to server
	 * Then updates map players map
	 */
	public void availableGold(){
		this.outToServer.println("AVAILABLE");
	}

	/**
	 * Sends the command look to server
	 */
	public void guiLook(){
		this.outToServer.println("LOOK");
	}

	/**
	 * Closes the player's GUI
	 */
	public void closePlayerGUI(){
		gameWindow.dispose();
	}



}
