/**
 * Defines the properties and behaviour of the game's logic
 * @Author oreid
 * @Release 25/02/2016
 */

import java.io.File;
import java.util.*;


public class GameLogic implements IGameLogic{

	private GameMap map = null;
	private boolean active;
	private boolean playerHasWon = false;
	private int currentPlayer = 1;
	public HashMap<Integer, Player> playerMap;
	ArrayList<int[]> playerPositions = new ArrayList<>();

	/**
	 * Constructor. Sets field values.
	 */
	public GameLogic(){
		playerMap = new HashMap<>();
		map = new GameMap();
	}

	/**
	 * Sets the map for the game
	 * @param file
	 * 		File to read the map from.
     */
	public void setMap(File file) {
		map.readMap(file);
		active = true;
	}

	/**
	 * Finds players positions.
	 * Loops over the hash map of players and compares the current player's id.
	 * @param y
	 * 		The y-coordinate of the player's position.
	 * @param x
	 * 		The x-coordinate of the player's position.
     * @return
	 * 		true, if players have the same position and false if they don't
     */
	public synchronized boolean samePlayerPosition(int y, int x){
		//Sets up iterator so we can iterate over HashMap
		Iterator it  = playerMap.entrySet().iterator();

		//While there are still players on the map
		while (it.hasNext()){
			Map.Entry entry = (Map.Entry) it.next();
			Player p = (Player)entry.getValue();

			if (p.getPosy() == y && p.getPosx() == x){
				//There is a player occupying the place we want to go
				return true;
			}
		}

		return false;
	}

	/**
	 * Determines whether or not other players are in the vicinity of the player.
	 * So that we can add them to the map.
	 *
	 * @param y
	 * 		y coordinate of current player on their visible map.
	 * @param x
	 * 		x coordinate of current player on their map.
     */
	public void otherPlayersOnMap(int y, int x){

		//iterate over HashMap and check if other player has same y or x coordinates
		Iterator it2  = playerMap.entrySet().iterator();

		//Clear the current positions so we don't get duplicates.
		playerPositions.removeAll(playerPositions);


		while (it2.hasNext()){
			Map.Entry entry = (Map.Entry) it2.next();
			Player p = (Player)entry.getValue();

			//If a player is in the look window add coordinates to player positions
			int[] posDiff = new int[2];
			if(p.getIdentity() != currentPlayer){
				posDiff[0] = p.getPosy() - y;
				posDiff[1] = p.getPosx() - x;
				playerPositions.add(posDiff);
			}

		}
	}

	/**
	 * Prints how much gold is still required to win!
	 */
	public String hello() {
		Player player = playerMap.get(currentPlayer);
		return "GOLD: " + (map.getWin() - player.getCollectedGold());
	}

	/**
	 * By proving a character direction from the set of {N,S,E,W} the GameLogic
	 * checks if this location can be visited by the player.
	 * If it is true, the player is moved to the new location.
	 * Prevents collision between players.
	 *
	 * @return If the move was executed Success is returned. If the move could not execute Fail is returned.
	 */
	public String move(char direction) {
		Player player = playerMap.get(currentPlayer);
		
		int[] newPosition = player.getPosition().clone();
		switch (direction){
		case 'N':
			newPosition[0] -=1;
			break;
		case 'E':
			newPosition[1] +=1;
			break;
		case 'S':
			newPosition[0] +=1;
			break;
		case 'W':
			newPosition[1] -=1;

			break;
		default:
			return "FAIL";
		}

		//Avoid collision with other players or wall
		//Prevents player collision (Checks if player is in the position it wants to move to)
		if(map.lookAtTile(newPosition[0], newPosition[1]) != '#' && !samePlayerPosition(newPosition[0], newPosition[1])){
			player.setPosy(newPosition[0]);
			player.setPosx(newPosition[1]);
			
			if (checkWin()) {
				quitGame();
			}
			
			return "SUCCESS";
		} else {
			return "FAIL";
		}
	}

	/**
	 * This method allows player to pickup an item in the dungeon.
	 * @return
	 * 		A string indicating whether the pickup was a success or failure
     */
	public String pickup() {
		Player player = playerMap.get(currentPlayer);
		if (map.lookAtTile(player.getPosy(), player.getPosx()) == 'G') {
			player.incCollectedGold();
			map.decrementGoldLeftOnMap();
			map.replaceTile(player.getPosy(), player.getPosx(), '.');
			return "SUCCESS, GOLD COINS: " + player.getCollectedGold();
		}

		return "FAIL" + "\n" + "There is nothing to pick up...";
	}

	public String goldAvailable(){
		return "GOLD AVAILABLE: " + map.getGoldLeftOnMap();
	}

	/**
	 * The method shows the dungeon around the player location
	 */
	public String look() {
		Player player = playerMap.get(currentPlayer);

		String output = "";
		char [][] lookReply = map.lookWindow(player.getPosy(), player.getPosx(), 5);
		lookReply[2][2] = 'P';
		//Find other players positions call otherPlayersOnMap() compare to main player of client
		otherPlayersOnMap(player.getPosy(), player.getPosx());

		boolean isPlayer = false;

		//Prints an O(Opponent) for other players in the map if they are in the current players look window
		for (int i=0;i<lookReply.length;i++){
			for (int j=0;j<lookReply[0].length;j++){
				for (int k = 0; k < playerPositions.size(); k++) {
					if(playerPositions.get(k)[0] + 2 == i && playerPositions.get(k)[1] + 2 == j){
						output += 'O';
						isPlayer = true;
						break;
					}
				}
				if(!isPlayer){
					output += lookReply[j][i];
				}
				isPlayer = false;
			}
			output += System.lineSeparator();
		}
		return output;
	}


	/**
	 * finds a random valid position for the player in the map.
	 *
	 * @return Return null; if no position is found or a position vector [y,x]
	 */
	public synchronized void initiatePlayer(int id) {

		int[] pos = new int[2];
		Random rand = new Random();
		Player player;
		do{
			pos[0]=rand.nextInt(map.getMapHeight());
			pos[1]=rand.nextInt(map.getMapWidth());

			player = new Player(id, pos[0], pos[1] );
			int counter = 1;
			while ((map.lookAtTile(player.getPosy(), player.getPosx()) == '#' || samePlayerPosition(player.getPosy(), player.getPosx())) &&  counter <( map.getMapHeight() * map.getMapWidth())) {
				player.setPosy((int) ( pos[0] * Math.cos(counter)));
				player.setPosx((int) ( pos[1] * Math.sin(counter)));
				counter++;
			}

		} while(player.getPosy() < 1 || player.getPosx() < 1);

		System.out.println(player.getPosy() + "," + player.getPosx());
		playerMap.put(player.getIdentity(), player);
	}


	/**
	 * checks if the player collected all GOLD and is on the exit tile
	 *
	 * @return True if all conditions are met, false otherwise
	 */
	protected boolean checkWin() {
		Player player = playerMap.get(currentPlayer);
		playerHasWon = true;
		if (player.getCollectedGold() >= map.getWin() &&
				map.lookAtTile(player.getPosy(), player.getPosx()) == 'E') {
			System.out.println("Congratulations!!!\n You have escaped the Dungeon of Dooom!!!!!! \n"
					+ "Thank you for playing!");
			return true;
		}
		return false;
	}

	/**
	 * Quits the game  and removes player from map when called.
	 * If a player has won it will shut down game server
	 */
	public void quitGame() {
		System.out.println("The game will now exit.");

		if(playerHasWon){
			//Might want to send message to all players on server
			GameServer.playerHasWonMessage(currentPlayer);
			System.exit(0);
		}else{
			//You want to close connection between client and server
			GameServer.disconnectFromServer(currentPlayer);
		}

		playerMap.remove(currentPlayer);
	}


	/**
	 * Boolean to check if game is running or not.
	 * @return
	 * 		active, if the game is running or not.
     */
	public boolean gameRunning(){
		return active;
	}

	/**
	 * Takes in a string from the client. Executes a method depending on input received from user.
	 *
	 * @param readUserInput
	 * 		The input from the user
	 * @param id
	 * 		The id of the player who's requesting a move
     * @return
     */
	protected synchronized String parseCommand(String readUserInput, int id) {
		currentPlayer = id;
		//For debugging
		System.out.println(readUserInput);

		readUserInput = readUserInput.toUpperCase();
		String [] command = readUserInput.trim().split(" ");
		String answer = "FAIL";

		switch (command[0].toUpperCase()){
			case "HELLO":
				answer = hello();
				break;
			case "AVAILABLE":
				answer = goldAvailable();
				break;
			case "MOVE":
				if (command.length == 2 )
					answer = move(command[1].charAt(0));
				break;
			case "PICKUP":
				answer = pickup();
				break;
			case "LOOK":
				answer = look();
				break;
			case "QUIT":
				quitGame();

			default:
				answer = "FAIL";
		}

		return answer;
	}


}
