import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ClientModel {

    private static final int MESSAGE_WAIT_TIME = 500;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private final ArrayList<HandPanel> playerHandPanels = new ArrayList<>();
    private int playerID;


    public ClientModel(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
        } catch (IOException e) {
            System.err.println("No server running on port " + serverPort + " at address " + serverAddress);
            System.exit(1);
        }
        try {
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());    // input stream reader from socket
            input = new BufferedReader(isr);
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the input from the server.  Returns the message to the SwingWorker in Client.
     * @return
     */
    public String getServerMessage() {
        String serverMessage = null;
        try {
            Thread.sleep(MESSAGE_WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (serverMessage == null) {
            try {
                serverMessage = input.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return serverMessage;
    }

    /**
     * Sends Client choice to the Player class
     * @param clientMessage
     */
    public void sendClientMessage(String clientMessage) {
        output.println(clientMessage);
    }


    public void addPlayerHandPanel(HandPanel playerHandPanel) {
        playerHandPanels.add(playerHandPanel);
    }

    public HandPanel getPlayerHandPanelByID(int ID) {
        for (HandPanel panel : playerHandPanels) {
            if (panel.getPanelPlayerID() == ID) {
                return panel;
            }
        }
        return null;
    }

    /**
     * Sets the card image.
     * @param cardName
     * @return
     */
    public JLabel getCardImage(String cardName) {
        JLabel cardLabel;


        // DISABLE THIS
        cardLabel = new JLabel();
        String path = "res/" + cardName + ".png";
        ImageIcon cardPicture = new ImageIcon(getClass().getResource(path));
        cardLabel.setIcon(cardPicture);
        cardLabel.setSize(25,40);

        //ENABLE THIS TO REPLACE IMAGES WITH TEXT  OR VICE VERSA
//        cardLabel = new JLabel(cardName);
//        cardLabel.setForeground(Color.WHITE);

        // LEAVE THIS
        return cardLabel;
    }

    public void reset() {
        playerHandPanels.clear();
    }

    public void clearOthers(){
        for (HandPanel handPanel: playerHandPanels) {
            if(handPanel instanceof OtherPlayerHandPanel){
                ((OtherPlayerHandPanel) handPanel).clearCardPanel();
            }
        }
    }

    public void quitGame() {
        sendClientMessage("CLIENTMESSAGE--QUITGAME");
        try {
            socket.close();
        } catch (SocketException e) {
            System.out.println("Socket Closed");
        } catch (IOException e1) {
            System.out.println("Socket Closed");
        }
        System.exit(0);
    }

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }
}
