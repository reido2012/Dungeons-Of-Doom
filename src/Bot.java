import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Defines the properties and behaviours of the Bot.
 */

public class Bot extends PlayGame implements ServerMessageHandler{
	//Fields
	private Random random;
	private Socket socket;
	private PrintWriter outToServer;
	private BotClientListener listener;
	private char[][] scannedLookWindow;
	private int goldCollected;
	boolean findingExit = false;
	private int goldNeeded;
	private int[] positionToFind;
	private int[] myPos = {2,2};

	private static final char [] DIRECTIONS = {'N','S','E','W'};

	/**
	 * Constructor. Set field values.
	 */
	public Bot(){
		super();
		random = new Random();

	}

	/**
	 * Takes input from server and depending on the input it makes different decisions.
	 * @param lastAnswer
	 * 		The string response to our previous move from the server.
	 * @return
	 * 		The move that we are going to send to the server.
     */
	private String botAction(String lastAnswer){
		String answer;
		switch (lastAnswer.split(" ")[0]){
			case "You":
			case "":
				answer = "HELLO";
				break;
			case "GOLD:":
				//If the first part of the last answer contains GOLD:
				//It means that the next part will contain the amount of gold we need to win
				goldNeeded = Integer.parseInt(lastAnswer.split(" ")[1]);
				System.out.println("Gold Needed: " + goldNeeded);
			case "FAIL":
				answer = "LOOK";
				break;
			case "SUCCESS,":
				//The response from the server indicates that we picked up gold
				goldCollected++;
			default:
				//Analyze the map
				look();
				answer = findThing();
				System.out.println("sending answer: " + answer);

		}

		return answer;
	}

	/**
	 * Gets the map from the server, and analyzes it to find points of interest.
	 * @return
	 * 	returns the look window
     */
	public String look(){
		sendToServer("LOOK");
		String lookWindow = listener.getReply();
		scanLookReply(lookWindow);
		return lookWindow;
	}

	/**
	 * Handles communications between bot and server
	 */
	public void update(){
		String answer = "";
		while (socket.isConnected()){

			//Get string command from bot
			answer = botAction(answer);
			sendToServer(answer);

			//Get response from server
			answer = listener.getReply();

			printAnswer(answer);

			if(answer.contains("Congratulations!!!")){
				return;
			}
			//Analyze response
			scanLookReply(answer);

			try {
				//Makes sure the Bot moves at a speed similar to humans
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String [] args) {
		Bot game = new Bot();
		game.connectToServer(game, PlayGame.chatWindow);
		game.update();
	}

	/**
	 * This method connects the bot/client to the server
	 */

	@Override
	public void connectToServer(ServerMessageHandler handler, ServerChatHandler chatHandler) {
		String hostName = "localhost";
		int portNumber = 40004;

		try{
			//Creates a socket
			socket = new Socket(hostName, portNumber);

			//Buffered reader that listens to the server
			BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outToServer = new PrintWriter(socket.getOutputStream(), true);

			//Creates a thread for listening to the server
			listener = new BotClientListener(serverIn);
			listener.start();


		}catch (UnknownHostException e){
			System.err.println("Don't know about host " + hostName);
			System.exit(1);
		} catch (SocketException e){
			System.err.println("Lost connection to server");
			System.exit(1);
		} catch (IOException e ){
			System.err.println("Couldn't get I/O for the connection to "  + hostName);
			System.exit(1);
		}
	}

	/**
	 * Sends command to server
	 * @param botInput
	 * 		The string that the bot wants to send to the server
     */
	public void sendToServer(String botInput){
		outToServer.println(botInput);
	}

	/**
	 * Takes the map and creates a character array from it so that we can look at individual tiles.
	 * @param map
     */
	public void scanLookReply(String map){
		//Put everything in string array
		//Split the string wherever there are new lines.
		String[] mapRows = map.split("\n");
		scannedLookWindow= new char[mapRows.length][mapRows[0].length()];

		for (int i = 0; i < mapRows.length; i++) {
			scannedLookWindow[i] = mapRows[i].toCharArray();
		}
	}

	/**
	 * @return
	 * 	The coordinates of gold within the look window
     */
	public int[] positionOfGold(){
		return findCharacter('G');
	}

	/**
	 * @return
	 * 	The coordinates of gold within the look window
	 */
	public int[] positionOfExit(){
		return findCharacter('E');
	}

	/**
	 * Searches the scanned look window for the character we are looking for
	 * @param characterToFind
	 * 		The character the bot is searching for
	 * @return
	 * 		The coordinates of the character in the look window
     */
	private int[] findCharacter(char characterToFind) {
		for (int i = 0; i <scannedLookWindow.length; i++) {
			for (int j = 0; j < scannedLookWindow[i].length; j++) {
				if(scannedLookWindow[i][j] == characterToFind){
					int[] position = {i, j};
					return position;
				}
			}
		}
		return null;
	}

	/**
	 * This method moves the bot towards the item it wants to find in the look window.
	 * If there isn't anything it wants to find in the look window it will move randomly.
	 * @return
	 * 	The command it wants to send to the server. The move it wants to make.
     */
	public String findThing(){
		String command = "";
		if(goldCollected != goldNeeded){
			//Searches for gold until it has all the gold.
			positionToFind = positionOfGold();
		}else{
			findingExit = true;
			positionToFind = positionOfExit();

		}

		int xDiff;
		int yDiff;

		if(positionToFind == null){
			//Do a random move
			System.out.println("position chosen at random.");
			System.out.print(System.lineSeparator());
			command = "MOVE " + DIRECTIONS[random.nextInt(4)];
			return command;

		}else{
			//Distance between the thing the bot wants to find and its own position
			yDiff = (positionToFind[0] - myPos[0]);
			xDiff = (positionToFind[1] - myPos[1]);

			if(!findingExit) {
				//If we are looking for gold this executes
				//We need to do pickups on gold but not on exits.
				if (xDiff == 0 && yDiff == 1) {
					//Gold is below bot
					System.out.println("We're at the gold almost");
					System.out.print(System.lineSeparator());
					//Pickup gold
					sendToServer("MOVE S");
					listener.getReply();
					command = "PICKUP";
					return command;
				}

				if (xDiff == 0 && yDiff == -1) {
					//Gold is above bot
					System.out.println("We're at the gold almost");
					System.out.print(System.lineSeparator());
					//Pickup gold
					sendToServer("MOVE N");
					listener.getReply();
					command = "PICKUP";
					return command;
				}

				if (xDiff == 1 && yDiff == 0) {
					//Gold is to the right of bot
					System.out.println("We're at the gold almost");
					System.out.print(System.lineSeparator());
					//Pickup gold
					sendToServer("MOVE E");
					listener.getReply();
					command = "PICKUP";
					return command;
				}

				if (xDiff == -1 && yDiff == 0) {
					//Gold is to the left of bot
					System.out.println("We're at the gold almost");
					System.out.print(System.lineSeparator());
					//Pickup gold
					sendToServer("MOVE W");
					listener.getReply();
					command = "PICKUP";
					return command;
				}
			}

			if (yDiff>=1){
				System.out.println("Useful thing is below me");
				System.out.println("xDiff: " + xDiff + ", yDiff: " + yDiff);
				System.lineSeparator();
				command = "MOVE S";
				return command;
			}

			if (yDiff <0 ){
				System.out.println("Useful thing above me");
				System.out.println("xDiff: " + xDiff + ", yDiff: " + yDiff);
				System.out.print(System.lineSeparator());
				command =  "MOVE N";
				return command;
			}

			if (xDiff>=1 ){

				System.out.println("Useful thing  is to the right of me");
				System.out.println("xDiff: " + xDiff + ", yDiff: " + yDiff);
				System.out.print(System.lineSeparator());
				command =   "MOVE E";
				return command;
			}

			if (xDiff<0){
				System.out.println("Useful thing  is to the left of me");
				System.out.println("xDiff: " + xDiff + ", yDiff: " + yDiff);
				System.out.print(System.lineSeparator());
				command = "MOVE W";
				return command;
			}


		}

		return  command;
	}


	@Override
	public void processMessage(String s) {

	}

}