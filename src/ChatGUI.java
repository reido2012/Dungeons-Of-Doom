import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Defines properties and behaviours of the chat GUI
 *
 * @Author oreid
 * @Release 24/03/2016
 */
public class ChatGUI extends JFrame implements ServerChatHandler {
    //JPanel
    JPanel chatPanel = new JPanel();
    //JTextField
    JTextField textInput = new JTextField();
    //JTextArea
    JTextArea messageArea = new JTextArea();
    //JButton
    JButton submitButton = new JButton("Submit");

    //ArrayLists
    ArrayList<String> stringStore = new ArrayList<>();
    ArrayList<Pattern> regexStore = new ArrayList<>();
    ArrayList<Long> messageTimeStore = new ArrayList<>();

    StringBuilder cleanMessage = new StringBuilder();
    String rawMessage;
    String playerID;
    int messageCounter = 0;
    boolean isSpamming = false;

    /**
     * Constructor. Sets field values.
     * Sets up panels of the chat GUI
     *
     * @param game
     */
    public ChatGUI(final PlayGame game) {
        //Chat panel has box layout
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        setContentPane(chatPanel);

        //Adds regex patterns to arrayList
        initialiseSwearWordPatterns();

        //Sets the new text area
        messageArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageArea.setEditable(false);

        //Add scroll pane to GUI
        JScrollPane scroll = new JScrollPane(messageArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(400, 600));

        chatPanel.add(scroll);

        //Create a new font - This font allows for emoji's
        //Apply this font to the messageArea and TextField
        try {
            Font emojiFont = Font.createFont(Font.TRUETYPE_FONT, new File("..\\OpenSansEmoji-master\\OpenSansEmoji.ttf")).deriveFont(18f);
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("..\\OpenSansEmoji-master\\OpenSansEmoji.ttf")));
            messageArea.setFont(emojiFont);
            textInput.setFont(emojiFont);
        } catch (IOException | FontFormatException e) {
            //Handle exception
        }

        textInput.setAlignmentX(Component.CENTER_ALIGNMENT);
        //Add text Input
        chatPanel.add(textInput);

        textInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                //Action listener if enter key is pressed
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    //If one player spams only that player is affected
                    counteractSpam();

                    //Send message if player isn't spamming
                    if (!isSpamming) {
                        sendTextToServer(game);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        //Setting up submit button and addding an action listener
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.setPreferredSize(new Dimension(400, 41));
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //When button is pressed text is sent to the server
                sendTextToServer(game);
            }
        });
        chatPanel.add(submitButton);

        setVisible(true);
        pack();
    }


    /**
     * Gets string from the text field
     *
     * @return The string from the text field
     */
    public String getText() {
        String text = textInput.getText();
        if (text.equals("")) {
            return null;
        } else {
            return text;
        }
    }

    /**
     * Adds text to the message area
     *
     * @param in The string we want to add to the text area
     */
    public void addToTextArea(String in) {
        messageArea.append(in + System.lineSeparator());
    }

    /**
     * Clear and reset what's in the text field
     */
    public void textToTextArea() {
        textInput.selectAll();
        //text is visible, even if there's a selection in the area
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
        textInput.setText("");
    }

    /**
     * Parses the chat message we receive from the server
     *
     * @param message The message received by the chat GUI
     */
    public void parseChatMessage(String message) {
        // Features: emojis, swear word filtering,control spamming

        //Chat message from other players
        if (message.contains("CHAT: ") && message.contains(" FROM Player: ")) {
            //This is a message from another player so we
            for (String ret : message.split(" FROM Player: ")) {
                stringStore.add(ret);
            }

            //Get actal message from the string we received
            rawMessage = stringStore.get(0);
            rawMessage = rawMessage.replace("CHAT: ", "");

            //Get the id of the player
            playerID = stringStore.get(1);

            //removes profanity from the message
            rawMessage = removeProfanity(rawMessage);
            //Find emojis now
            String stringWithEmojis = parseEmoji(rawMessage);

            message = "Player " + playerID + ": " + stringWithEmojis;


        } else {
            //This is a message from us so we need
            rawMessage = message.replace("CHAT: ", "");
            rawMessage = removeProfanity(rawMessage);
            String stringWithEmojis = parseEmoji(rawMessage);
            message = "You: " + stringWithEmojis;

        }

        stringStore.clear();
        addToTextArea(message);

    }

    /**
     * Uses regex to remove swear words from the messages
     *
     * @param rawMessage The message with swear words in it
     * @return The message without swear words
     */
    private String removeProfanity(String rawMessage) {
        cleanMessage.setLength(0);
        boolean containsCurse;
        String lowerCaseWord;
        String asterisk;

        String[] wordsInMessage = rawMessage.split(" ");

        //Split the message by spaces check if each string has a swear word
        for (String word : wordsInMessage) {
            containsCurse = false;
            asterisk = "";

            lowerCaseWord = word.toLowerCase();
            //If the word is a swear word then change the word to **** then add it to the cleanMessage string builder
            // Use regex patterns to check for swear words
            for (Pattern p : regexStore) {

                //Check if word matches any of our swear word regex patterns
                if (p.matcher(lowerCaseWord).matches()) {
                    for (int i = 0; i < word.length(); i++) {
                        asterisk = asterisk + "*";
                    }

                    //Replace the word with *'s
                    word = word.replace(word, asterisk);
                    cleanMessage.append(word + " ");
                    containsCurse = true;
                }
            }

            if (!containsCurse) {
                cleanMessage.append(word + " ");
            }

        }
        return (cleanMessage.toString());
    }

    /**
     * Checks if there is an emoji in the message we want to send and replaces text with emoji.
     * There are 10 emoji to chose from.
     *
     * @param rawMessage The string that we are checking emoji's for
     * @return The string that contains the emoji
     */
    private String parseEmoji(String rawMessage) {
        //Smiling Face with Smiling Eyes
        if (rawMessage.contains(":)")) {
            //The bytes that represent the emoji
            byte[] emojiInBytes = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x8A};
            //Turn bytes into the String
            String emojiInString = new String(emojiInBytes, Charset.forName("UTF-8"));
            rawMessage = rawMessage.replace(":)", emojiInString);
        }

        //Winking Face
        if (rawMessage.contains(";)")) {
            byte[] emojiInBytes = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x89};
            String emojiInString = new String(emojiInBytes, Charset.forName("UTF-8"));
            rawMessage = rawMessage.replace(";)", emojiInString);
        }

        //Heart
        if (rawMessage.contains("<3")) {
            byte[] emojiInBytes = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x92, (byte) 0x93};
            String emojiInString = new String(emojiInBytes, Charset.forName("UTF-8"));
            rawMessage = rawMessage.replace("<3", emojiInString);
        }

        //Broken Heart
        if (rawMessage.contains("</3")) {
            byte[] emojiInBytes = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x92, (byte) 0x94};
            String emojiInString = new String(emojiInBytes, Charset.forName("UTF-8"));
            rawMessage = rawMessage.replace("</3", emojiInString);
        }

        //Thumbs Up
        if (rawMessage.contains("(y)")) {
            byte[] emojiInBytes = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x91, (byte) 0x8D};
            String emojiInString = new String(emojiInBytes, Charset.forName("UTF-8"));
            rawMessage = rawMessage.replace("(y)", emojiInString);
        }

        //Sad Face
        if (rawMessage.contains(":(")) {
            byte[] emojiInBytes = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x9E};
            String emojiInString = new String(emojiInBytes, Charset.forName("UTF-8"));
            rawMessage = rawMessage.replace(":(", emojiInString);
        }

        //Cry Face
        if (rawMessage.contains(":*")) {
            byte[] emojiInBytes = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0xA2};
            String emojiInString = new String(emojiInBytes, Charset.forName("UTF-8"));
            rawMessage = rawMessage.replace(":*", emojiInString);
        }


        //Einstein Face (Wink with tongue out)
        if (rawMessage.contains(";P")) {
            byte[] emojiInBytes = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x9C};
            String emojiInString = new String(emojiInBytes, Charset.forName("UTF-8"));
            rawMessage = rawMessage.replace(";P", emojiInString);
        }

        //Astonished Face
        if (rawMessage.contains(":o")) {
            byte[] emojiInBytes = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0xB2};
            String emojiInString = new String(emojiInBytes, Charset.forName("UTF-8"));
            rawMessage = rawMessage.replace(":o", emojiInString);
        }

        //Sunglasses Emoji
        if (rawMessage.contains("8)")) {
            byte[] emojiInBytes = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x8E};
            String emojiInString = new String(emojiInBytes, Charset.forName("UTF-8"));
            rawMessage = rawMessage.replace("8)", emojiInString);
        }
        return rawMessage;
    }

    /**
     * Receives chat message from the server
     *
     * @param s The message received from the server
     */
    @Override
    public void handleChat(String s) {
        //Formats the message and appends it to the text area.
        parseChatMessage(s);
    }

    /**
     * Sets up the regex patterns and adds them to a store.
     */
    public void initialiseSwearWordPatterns() {
        //Using regex patterns allows me to get swear words
        // even if users try and add extra characters or add random characters to the end
        Pattern a;
        Pattern b;
        Pattern c;
        Pattern d;
        Pattern e;
        Pattern f;
        Pattern g;
        Pattern h;
        Pattern i;
        Pattern j;
        Pattern k;
        Pattern l;
        Pattern m;

        a = Pattern.compile("f+u+c+k+\\s*\\w*");
        regexStore.add(a);

        b = Pattern.compile("s+h+i+t+\\s*\\w*");
        regexStore.add(b);

        c = Pattern.compile("f+a+g+\\s*\\w*");
        regexStore.add(c);

        d = Pattern.compile("f+a+g+g+o+t+\\s*\\w*");
        regexStore.add(d);

        e = Pattern.compile("b+i+t+c+h+\\s*\\w*");
        regexStore.add(e);

        f = Pattern.compile("p+u+s+s+y+\\s*\\w*");
        regexStore.add(f);

        g = Pattern.compile("f+u+c+k+i+n+g+\\s*\\w*");
        regexStore.add(g);

        h = Pattern.compile("n+i+g+g+e+r+\\s*\\w*");
        regexStore.add(h);

        i = Pattern.compile("n+i+g+g+a+\\s*\\w*");
        regexStore.add(i);

        j = Pattern.compile("r+a+p+e+\\s*\\w*");
        regexStore.add(j);

        k = Pattern.compile("d+i+c+k+\\s*\\w*");
        regexStore.add(k);

        l = Pattern.compile("c+u+n+t+\\s*\\w*");
        regexStore.add(l);

        m = Pattern.compile("t+w+a+t+\\s*\\w*");
        regexStore.add(m);


    }

    /**
     * Gets the text from the text field and sends it to server.
     *
     * @param game The game object so we can send chat command and message
     */
    public void sendTextToServer(PlayGame game) {
        String toSend = getText();
        //Message only sends if there's something to send
        if (toSend != null) {
            game.chat(toSend);
            textToTextArea();
        }
    }

    /**
     * Checks if messages have been sent too fast
     * If they have it is counted as spam and you can't send a message for 10 seconds.
     */
    public void counteractSpam() {
        messageTimeStore.add(System.currentTimeMillis());
        messageCounter++;

        if (messageCounter > 1) {
            int mcHandle = messageCounter;

            long recentSend = messageTimeStore.get(mcHandle - 1);
            long beforeSend = messageTimeStore.get(mcHandle - 2);
            long delay = recentSend - beforeSend;

            //If client sends two messages in 0.5 seconds then it is spam
            if (delay < 500) {
                messageArea.setText("Spam Detected: You were suspended\n");
                isSpamming = true;

                //Stop from sending message for 10 seconds
                while (true) {
                    if (System.currentTimeMillis() >= messageTimeStore.get(messageCounter - 1) + 10000) {
                        isSpamming = false;
                        break;
                    }
                }

            }
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        chatPanel = new JPanel();
        chatPanel.setLayout(new GridBagLayout());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return chatPanel;
    }
}
