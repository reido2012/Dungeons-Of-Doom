/**
 * Defines the properties and behaviours of a Player
 * @Author oreid
 * @Release 25/02/2016
 */
public class Player  {

    //Fields
    private int identity;
    private int[] pos = new int[2];
    private int collectedGold;

    /**
     * Constructor. Sets field values.
     * @param newIdentity
     *      The ID number of the player.
     * @param inposx
     *      The x coordinate of the player.
     * @param inposy
     *      The y coordinate of the player.
     */
    public Player(int newIdentity, int inposx, int inposy){
        super();
        this.identity= newIdentity;
        this.pos[1]= inposx;
        this.pos[0] = inposy;
        this.collectedGold = 0;
    }

    /**
     * @return
     *      amount of gold a player has collected
     */
    public int getCollectedGold() {
        return collectedGold;
    }


    /**
     * Increments the amount of gold a player has.
     */
    public void incCollectedGold() {
        this.collectedGold++;
    }

    /**
     * @return
     *      The ID(number) of the player.
     */
    public int getIdentity() {

        return identity;
    }

    /**
     * @return
     *      The x position of the player.
     */
    public int getPosx() {
        return pos[1];
    }

    /**
     * Sets the x position of the player
     * @param posx
     *      The x coordinate you want to give the player
     */
    public void setPosx(int posx) {
        this.pos[1] = posx;
    }

    /**
     * @return
     *      The y position of the player.
     */
    public int getPosy() {
        return pos[0];
    }

    /**
     * Sets the y position of the player
     * @param posy
     *      The y coordinate you want to give the player
     */
    public void setPosy(int posy) {
        this.pos[0] = posy;
    }

    /**
     * @return
     *  The x and y coordinates
     */
    public int[] getPosition(){
        return  pos;
    }
}
