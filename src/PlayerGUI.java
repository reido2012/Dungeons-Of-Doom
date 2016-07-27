import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Defines properties and behaviours of the Player GUI
 * Some properties of layout are held in the PlayerGUI.form file
 *
 * @Author oreid
 * @Release 16/03/2016
 */
public class PlayerGUI extends JFrame implements ServerMessageHandler {
    //Set up Image Icons for JLabels
    ImageIcon floor;
    ImageIcon wall = new ImageIcon("..\\res\\wall.jpg");
    ImageIcon playerFloor = new ImageIcon("..\\res\\player_floor.jpg");
    ImageIcon opponentFloor = new ImageIcon("..\\res\\opp_floor.jpg");
    ImageIcon goldFloor = new ImageIcon("..\\res\\floor_gold.jpg");
    ImageIcon exitFloor = new ImageIcon("..\\res\\floor_exit.jpg");
    //JPanels
    private JPanel panel1;
    private JPanel gameViewer;
    private JPanel controlPanel;
    private JPanel movementPanel;
    private JPanel lookWindow;
    //JButtons
    private JButton pickupButton;
    private JButton helloButton;
    private JButton quitButton;
    private JButton moveNorthButton;
    private JButton moveSouthButton;
    private JButton moveEastButton;
    private JButton moveWestButton;
    private JButton chatButton;
    //JLabels
    private JLabel notification;
    private JLabel[][] labelGrid;
    private JLabel goldToWin;
    private JLabel playerGold;
    private JProgressBar gameProgress;
    private JLabel progressLabel;
    private JLabel controlLabel;

    private JPanel gameFrame;
    private JLabel statusLabel;

    private GridBagConstraints gbc = new GridBagConstraints();

    boolean containsRandomSpace;
    int windowCounter = 0;
    StringBuilder map = new StringBuilder();
    boolean containsMap;

    private int counter = 0;
    int goal;
    boolean initialGoal = true;


    /**
     * Constructor. Sets field values.
     * Sets up player GUI.
     *
     * @param game
     */
    public PlayerGUI(final PlayGame game) {
		floor = new ImageIcon("..\\res\\floor.jpg");
        panel1 = new JPanel();
        lookWindow = new JPanel();
        gameFrame = new JPanel();
        gameViewer = new JPanel();
        controlPanel = new JPanel();

        pickupButton = new JButton();
        helloButton = new JButton();
        quitButton = new JButton();
        moveNorthButton = new JButton();
        moveSouthButton = new JButton();
        moveEastButton = new JButton();
        moveWestButton = new JButton();
        chatButton = new JButton();

        notification = new JLabel();
        goldToWin = new JLabel();
        playerGold = new JLabel();
        labelGrid = new JLabel[5][5];

        $$$setupUI$$$();
        setContentPane(panel1);
        setVisible(true);
        pack();

        //Action listeners for each button
        //When button is pressed the required command is sent to the server
        moveNorthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.moveNorth();
            }
        });
        moveSouthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.moveSouth();
            }
        });
        moveEastButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.moveEast();
            }
        });
        moveWestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.moveWest();
            }
        });
        pickupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.pickup();
            }
        });
        helloButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.hello();
            }
        });
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.quit();
                game.closePlayerGUI();
            }
        });
        chatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Show/Hide chat GUI if chat button is clicked
                if (windowCounter % 2 == 0) {
                    game.chatWindow.setVisible(true);
                } else {
                    game.chatWindow.setVisible(false);
                }
                windowCounter++;

            }

        });
    }

    /**
     * This processes the input from the server.
     * It mainly makes sure that we get the whole map and stores it correctly.
     *
     * @param s The string we receive from the server
     */
    @Override
    public void processMessage(String s) {
        containsMap = true;
        containsRandomSpace = false;
        if (counter == 0) {
            map.setLength(0);
        }
        //Splits the string if there are any newline characters
        for (String line : s.split("\n")) {
            //If it starts with something that's not the map then handle it
            if (line.startsWith("S") || line.contains("GOLD") || line.startsWith("Y") || line.startsWith("F") || line.contains("There")) {
                if (line.contains("GOLD: ")) {
                    line = line.replaceAll("\\D+", "");
                    goldToWin.setText("Gold To Win: " + line);
                    //Sets up progress bar and amount of gold needed to win
                    if (initialGoal) {
                        goal = Integer.parseInt(line);
                        gameProgress.setMaximum(goal);
                        initialGoal = false;
                    }
                }

                //Updates the GUI when a player gets gold
                //Updates progress bar
                if (line.contains("SUCCESS, GOLD COINS: ")) {
                    line = line.replaceAll("\\D+", "");
                    playerGold.setText("Gold Collected: " + line);
                    gameProgress.setValue(percentageComplete(line));
                }

                containsMap = false;
                counter = 0;
                break;

            } else if (line.equals("")) {
                containsMap = false;
                containsRandomSpace = true;
                counter = 0;
                continue;

            } else {
                // The string contains a map line
                //Add string to a StringBuilder
                map.append(s);
                if (counter < 4) {
                    //Add the newline character back to the string we're building
                    map.append(System.lineSeparator());
                }
            }

        }

        if (!containsMap) {
            if (!containsRandomSpace) {
                //If the information is useful then append to a JLabel
                this.notification.setText(s);
            } else {
                //Do nothing
            }
            counter = 0;
        } else {
            counter++;
            if (counter == 5) {
                //Send map to the string
                setLabels(map.toString());
                counter = 0;
            }
        }

    }

    /**
     * Converts the string to integer so we can use it in our progress bar
     *
     * @param goldCollected The string that contains the amount of gold we have
     * @return The integer number of the amount of gold we have
     */
    private int percentageComplete(String goldCollected) {
        int percent = (Integer.parseInt(goldCollected));
        return percent;
    }

    /**
     * Set the labels on the GUI to the correct space in the grid.
     *
     * @param map The String map that we will use to assign positions for our icons
     */
    public void setLabels(String map) {

        String[] newMap = map.split("\n");
        lookWindow.removeAll();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                //Sets JLabel to the correct position
                labelGrid[i][j] = new JLabel();
                gbc.gridx = j;
                gbc.gridy = i;
                lookWindow.add(labelGrid[i][j], gbc);
                switch (newMap[i].charAt(j)) {
                    //Checks character and assigns specific image to JLabel
                    case '.':
                        labelGrid[i][j].setIcon(floor);
                        break;
                    case 'G':
                        labelGrid[i][j].setIcon(goldFloor);
                        break;
                    case 'P':
                        labelGrid[i][j].setIcon(playerFloor);
                        break;
                    case 'E':
                        labelGrid[i][j].setIcon(exitFloor);
                        break;
                    case 'O':
                        labelGrid[i][j].setIcon(opponentFloor);
                        break;
                    case '#':
                        labelGrid[i][j].setIcon(wall);
                    default:
                        break;
                }

            }
        }

    }


    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel1.setLayout(new BorderLayout(0, 0));
        gameViewer.setLayout(new GridBagLayout());
        panel1.add(gameViewer, BorderLayout.CENTER);
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gameViewer.add(controlPanel, gbc);
        pickupButton = new JButton();
        pickupButton.setMaximumSize(new Dimension(300, 41));
        pickupButton.setMinimumSize(new Dimension(200, 41));
        pickupButton.setPreferredSize(new Dimension(300, 41));
        pickupButton.setText("Pickup");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(pickupButton, gbc);
        helloButton = new JButton();
        helloButton.setMaximumSize(new Dimension(300, 41));
        helloButton.setPreferredSize(new Dimension(300, 41));
        helloButton.setText("Hello");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(helloButton, gbc);
        quitButton = new JButton();
        quitButton.setMaximumSize(new Dimension(300, 41));
        quitButton.setPreferredSize(new Dimension(300, 41));
        quitButton.setText("Quit");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(quitButton, gbc);
        statusLabel = new JLabel();
        statusLabel.setMaximumSize(new Dimension(300, 21));
        statusLabel.setPreferredSize(new Dimension(300, 21));
        statusLabel.setText("Status:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        controlPanel.add(statusLabel, gbc);
        playerGold = new JLabel();
        playerGold.setMaximumSize(new Dimension(300, 21));
        playerGold.setPreferredSize(new Dimension(300, 21));
        playerGold.setText("Gold Collected:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        controlPanel.add(playerGold, gbc);
        goldToWin = new JLabel();
        goldToWin.setMaximumSize(new Dimension(300, 21));
        goldToWin.setPreferredSize(new Dimension(300, 21));
        goldToWin.setText("Gold To Win:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        controlPanel.add(goldToWin, gbc);
        chatButton = new JButton();
        chatButton.setMaximumSize(new Dimension(300, 41));
        chatButton.setPreferredSize(new Dimension(300, 41));
        chatButton.setText("Chat");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(chatButton, gbc);
        final JLabel label1 = new JLabel();
        label1.setMaximumSize(new Dimension(300, 21));
        label1.setPreferredSize(new Dimension(300, 21));
        label1.setText("Notifications:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        controlPanel.add(label1, gbc);
        notification = new JLabel();
        notification.setMaximumSize(new Dimension(300, 41));
        notification.setPreferredSize(new Dimension(300, 41));
        notification.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        controlPanel.add(notification, gbc);
        movementPanel = new JPanel();
        movementPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(movementPanel, gbc);
        moveNorthButton = new JButton();
        moveNorthButton.setMaximumSize(new Dimension(300, 41));
        moveNorthButton.setPreferredSize(new Dimension(300, 41));
        moveNorthButton.setText("Move North");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        movementPanel.add(moveNorthButton, gbc);
        moveSouthButton = new JButton();
        moveSouthButton.setMaximumSize(new Dimension(300, 41));
        moveSouthButton.setPreferredSize(new Dimension(300, 41));
        moveSouthButton.setText("Move South");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        movementPanel.add(moveSouthButton, gbc);
        moveEastButton = new JButton();
        moveEastButton.setPreferredSize(new Dimension(150, 41));
        moveEastButton.setText("Move East");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        movementPanel.add(moveEastButton, gbc);
        moveWestButton = new JButton();
        moveWestButton.setMaximumSize(new Dimension(300, 41));
        moveWestButton.setPreferredSize(new Dimension(150, 41));
        moveWestButton.setText("Move West");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        movementPanel.add(moveWestButton, gbc);
        gameProgress = new JProgressBar();
        gameProgress.setPreferredSize(new Dimension(300, 41));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(gameProgress, gbc);
        progressLabel = new JLabel();
        progressLabel.setMaximumSize(new Dimension(36, 21));
        progressLabel.setPreferredSize(new Dimension(300, 41));
        progressLabel.setText("Progress:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.WEST;
        controlPanel.add(progressLabel, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.fill = GridBagConstraints.VERTICAL;
        controlPanel.add(spacer1, gbc);
        controlLabel = new JLabel();
        controlLabel.setPreferredSize(new Dimension(300, 41));
        controlLabel.setText("Controls:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.anchor = GridBagConstraints.WEST;
        controlPanel.add(controlLabel, gbc);
        gameFrame = new JPanel();
        gameFrame.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gameViewer.add(gameFrame, gbc);
        lookWindow = new JPanel();
        lookWindow.setLayout(new GridBagLayout());
        lookWindow.setPreferredSize(new Dimension(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gameFrame.add(lookWindow, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}

