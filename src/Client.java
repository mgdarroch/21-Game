import javax.swing.*;
import java.util.concurrent.ExecutionException;

public class Client {
    SwingWorker swingWorker;
    private final String serverAddress;
    private final int serverPort;
    private ClientModel model;
    private ClientView view;
    private int playerID;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 8888;
        Client client = new Client(serverAddress, serverPort);
        client.start();
    }

    public void start() {
        System.out.println("Starting client...\nServer address: " + serverAddress + "\nServer port: " + serverPort);
        model = new ClientModel(serverAddress, serverPort);
        view = new ClientView(this);
        getServerMessage();
    }

    /**
     * Retrieves the servermessage from the model's method.  SwingWorker so it constantly acquires in the background.
     */
    private void getServerMessage() {
        swingWorker = new SwingWorker<String, String>() {
            @Override
            public String doInBackground() {
                return model.getServerMessage();
            }

            @Override
            public void done() {
                try {
                    changeView(get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        swingWorker.execute();
    }

    /**
     * Sends the client message to the server.  The message is the choice "Hit", "Stand", "Yes", "No",
     * @param clientMessage
     */
    public void sendClientMessage(String clientMessage) {
        model.sendClientMessage(clientMessage);
    }

    public void quitGame() {
        model.quitGame();
        System.out.println("Client Disconnected");
    }

    /**
     * Gets the player ID
     * @return
     */
    public int getPlayerID() {
        return model.getPlayerID();
    }

    /**
     * Changes the ClientView depending on the incoming message from the server.
     * @param serverMessage
     */
    private void changeView(String serverMessage) {
        System.out.println(serverMessage);
        String[] serverMessageComponents = serverMessage.split("--");   // array containing the components of the server message
        switch (serverMessageComponents[1]) {
            case "WELCOME":
                this.playerID = Integer.parseInt(serverMessageComponents[2]);
                model.setPlayerID(playerID);
                System.out.println("CLIENT ID: " + Integer.parseInt(serverMessageComponents[2]));
                view.showWelcomePanel();
                view.repaint();
                getServerMessage();
                break;

            case "GETBET":
                view.setWelcomeWaiting(false);
                view.setContinuePlayingWaiting(false);
                getServerMessage();
                break;

            case "NEWROUND":
                view.showTurnPanel();
                view.setTurnMoneyLabel(serverMessageComponents[2]);
                getServerMessage();
                break;
            case "TWENTYONE":
                switch (serverMessageComponents[2]) {
                    case "PLAYERANDDEALER":
                        view.setTwentyOneLabel("You and the dealer both have Twenty One!");
                        getServerMessage();
                        break;
                    case "PLAYER":
                        view.setTwentyOneLabel(serverMessageComponents[3] + " players have Twenty One.");
                        getServerMessage();
                        break;
                    case "DEALER":
                        view.setTwentyOneLabel("The dealer has Twenty One!");
                        getServerMessage();
                        break;
                    case "DEALERNOTWENTYONE":
                        view.setTwentyOneLabel("The dealer does not have Twenty One.");
                        getServerMessage();
                        break;
                }
                break;

            case "TAKETURN":
                view.setTurnWaiting(false);
                view.removeInsuranceBetInfo();
                getServerMessage();
                break;
            case "NEWHAND":
                if (Integer.parseInt(serverMessageComponents[3]) == playerID) {
                    model.addPlayerHandPanel(new PlayerHandPanel(this, Integer.parseInt(serverMessageComponents[3]), Boolean.parseBoolean(serverMessageComponents[4])));
                } else {
                    model.addPlayerHandPanel(new OtherPlayerHandPanel(Integer.parseInt(serverMessageComponents[3]), Boolean.parseBoolean(serverMessageComponents[4])));
                }

                if (Integer.parseInt(serverMessageComponents[3]) == model.getPlayerID()) {
                    if (model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[3])) instanceof PlayerHandPanel) {
                        view.addClientHandPanel((PlayerHandPanel) model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[3])));
                    }
                } else {
                    if (model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[3])) instanceof OtherPlayerHandPanel) {
                        view.addPlayerHandPanel((OtherPlayerHandPanel) model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[3])));
                    }
                }
                getServerMessage();
                break;

            case "HANDVALUE":
                model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[2])).setHandValueLabel(serverMessageComponents[3]);
                getServerMessage();
                break;
            case "TURNTWENTYONE":
                switch (serverMessageComponents[2]) {
                    case "PLAYERANDDEALER":
                        view.setTwentyOneLabel("You and the dealer both have Twenty One!");
                        getServerMessage();
                        break;
                    case "PLAYER":
                        view.setTwentyOneLabel("You have Twenty One!");
                        getServerMessage();
                        break;
                    case "DEALER":
                        view.setTwentyOneLabel("The dealer has Twenty One!");
                        getServerMessage();
                        break;
                }
                break;
            case "NEWPLAYERCARD":
                if (Integer.parseInt(serverMessageComponents[3]) == model.getPlayerID()) {
                    model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[3])).addCard(model.getCardImage(serverMessageComponents[4]));
                } else {
                    model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[3])).addCard(model.getCardImage(serverMessageComponents[4]));
                }

                getServerMessage();
                break;

            case "NEWPLAYERCARDREVEALED":
                model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[3])).addCard(model.getCardImage(serverMessageComponents[4]));
                getServerMessage();
                break;

            case "TURNOPTION":
                switch (serverMessageComponents[2]) {
                    case "HITSTAND":
                        model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[4])).enableHitStand();
                        getServerMessage();
                        break;
                }
                break;
            case "TURNOPTIONERROR":
                model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[2])).turnError();
                getServerMessage();
                break;

            case "BUST":
                model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[2])).bust();
                getServerMessage();
                break;

            case "SENDRESULT":
                view.setTurnWaiting(false);
                getServerMessage();
                break;

            case "ROUNDRESULT":
                switch (serverMessageComponents[2]) {
                    case "NORMAL":
                        switch (serverMessageComponents[3]) {
                            case "DEALER":
                                model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[4])).setHandMessageLabel("Dealer updated.");
                                view.setTurnMoneyLabel(serverMessageComponents[5]);
                                getServerMessage();
                                break;
                            case "PLAYER":
                                model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[4])).setHandMessageLabel("Player updated.");
                                view.setTurnMoneyLabel(serverMessageComponents[5]);
                                getServerMessage();
                                break;
                        }
                        break;
                    case "TWENTYONE":
                        switch (serverMessageComponents[3]) {
                            case "DEALER":
                                model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[4])).setHandMessageLabel("Dealer has natural Twenty One");
                                view.setTurnMoneyLabel(serverMessageComponents[5]);
                                getServerMessage();
                                break;
                            case "PLAYER":
                                model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[4])).setHandMessageLabel("Players have natural Twenty One.");
                                view.setTurnMoneyLabel(serverMessageComponents[5]);
                                getServerMessage();
                                break;
                            case "PLAYERANDDEALER":
                                model.getPlayerHandPanelByID(Integer.parseInt(serverMessageComponents[4])).setHandMessageLabel("Players and Dealer have natural Twenty One.");
                                view.setTurnMoneyLabel(serverMessageComponents[5]);
                                getServerMessage();
                                break;

                        }
                        break;
                }
                break;
            case "GETCONTINUEPLAYING":
                view.enableContinuePlaying();
                getServerMessage();
                break;
            case "CONTINUEPLAYINGRESPONSE":
                switch (serverMessageComponents[2]) {
                    case "ERROR":
                        view.continuePlayingError();
                        getServerMessage();
                        break;
                    case "CONTINUE":
                        view.reset();
                        model.reset();
                        view.showContinuePlayingPanel();
                        getServerMessage();
                        break;
                }
                break;
            case "GAMEOVER":
                view.showContinuePlayingPanel();
                view.setContinuePlayingMoneyLabel(serverMessageComponents[2]);
                view.gameOver();
                quitGame();
                break;
            case "WAITING":
                switch (serverMessageComponents[2]) {
                    case "WELCOME":
                        view.setWelcomeWaiting(true);
                        view.setContinuePlayingWaiting(true);
                        getServerMessage();
                        break;

                    case "TURN":
                        view.setTurnWaiting(true);
                        getServerMessage();
                        break;
                }
                break;
            default:
                System.err.println("Unknown message received from server: \"" + serverMessage + "\"");
                break;
        }
    }
}
