import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class Player implements Runnable {

    private static final int TWENTY_ONE = 21;
    private static final int MINIMUM_STAKE = 1;
    boolean playersHaveTwentyOne;
    boolean dealerHasTwentyOne;
    private final Game game;
    private BufferedReader input;
    private PrintWriter output;
    private final ArrayList<GameHand> playerHands = new ArrayList<>();
    private GameHand thisPlayerHand;
    private String choice;
    private boolean receivedChoice = false;
    private CountDownLatch startLatch;
    private CountDownLatch betLatch;
    private CountDownLatch dealLatch;
    private CountDownLatch hitStandLatch;
    private boolean continuePlaying = false;
    private boolean dealer;
    private boolean naturalTwentyOne;
    private boolean naturalTwentyOneLastRound;
    private final int PlayerID;
    private int stakes;
    private boolean playerBust;

    public Player(Socket socket, Game game, int money, int playerID) {
        this.game = game;
        naturalTwentyOne = false;
        naturalTwentyOneLastRound = false;
        this.PlayerID = playerID;
        stakes = money;
        System.out.println("Player ID: " + playerID + " has connected.");
        try {
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());     // input stream reader from socket
            input = new BufferedReader(isr);
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void run() {
        output.println("SERVERMESSAGE--WELCOME--" + PlayerID);
        do {
            playTwentyOne();
        } while (continuePlaying);
        output.println("SERVERMESSAGE--GAMEOVER--" + stakes + "--" + game.getPlayerList().size());
    }

    /**
     * This method runs a round of Twenty One.  It uses CountDownLatches in conjunction with the Game object's playTwentyOne method.
     */
    private void playTwentyOne() {
        setupPlayer();
        try {
            startLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        canPlay();
        try {
            betLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            dealLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gameStatusCheck();
        sendRoundInformation();
        try {
            hitStandLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        output.println("SERVERMESSAGE--SENDRESULT");
        for (GameHand hand : playerHands) {
            sendResult(hand);
        }
        getContinuePlaying();
    }

    private void setupPlayer() {
        playerHands.clear();
        thisPlayerHand = new GameHand();
        playerHands.add(thisPlayerHand);
        receivedChoice = false;
        continuePlaying = false;
        naturalTwentyOne = false;
        playerBust = false;
        dealer = false;
        startLatch = new CountDownLatch(1);
        betLatch = new CountDownLatch(1);
        dealLatch = new CountDownLatch(1);
        hitStandLatch = new CountDownLatch(1);
        output.println("SERVERMESSAGE--WAITING--WELCOME");
    }

    private void canPlay() {
        output.println("SERVERMESSAGE--GETBET--" + stakes + "--" + MINIMUM_STAKE);
        if (stakes >= 1) {
            game.eligibleStakesLatch();
        } else {
            continuePlaying = false;
        }
    }

    /**
     * This method updates the Client's view with his cards and other players cards and also updates the Client message labels with whether or not the player, any player and the dealer have a natural twenty one.
     */
    private void sendRoundInformation() {

        output.println("SERVERMESSAGE--NEWROUND--" + stakes);
        for (Player player : game.getPlayerList()) {
            output.println("SERVERMESSAGE--NEWHAND--0" + "--" + player.getPlayerID() + "--" + player.isDealer());
        }

        for (Player player : game.getPlayerList()) {
            for (int i = 0; i < player.thisPlayerHand.size(); i++) {
                output.println("SERVERMESSAGE--NEWPLAYERCARD--0--" + player.getPlayerID() + "--" + player.getThisPlayerHand().getCard(i));
            }
        }

        output.println("SERVERMESSAGE--HANDVALUE--" + PlayerID + "--" + thisPlayerHand.handValue());

        if (thisPlayerHand.handValue() == TWENTY_ONE && game.getDealer().getThisPlayerHand().handValue() == TWENTY_ONE && getPlayerID() != game.getDealer().getPlayerID()) {
            output.println("SERVERMESSAGE--TWENTYONE--PLAYERANDDEALER");
            naturalTwentyOne = true;

        } else if (game.getDealer().getThisPlayerHand().handValue() == TWENTY_ONE) {
            output.println("SERVERMESSAGE--TWENTYONE--DEALER");

        } else if (game.getDealer().getThisPlayerHand().handValue() != TWENTY_ONE) {
            output.println("SERVERMESSAGE--TWENTYONE--DEALERNOTWENTYONE");

        } else if (thisPlayerHand.handValue() == TWENTY_ONE) {
            naturalTwentyOne = true;
        }

        game.turnLatchCountDown();
        if (game.numPlayers() > 1) {
            output.println("SERVERMESSAGE--WAITING--TURN");
        }
    }


    /**
     * This method uses the startHitStand, a Switch statement and the hitStand method to carry out the players turn in conjunction with input from the Client.
     * @param hand
     */
    void takeTurn(GameHand hand) {

        startHitStand(hand);

        switch (choice) {
            case "Hit":
            case "Stand":
                hitStand(hand);
                break;
        }
        if (game.numPlayers() > 1 && !naturalTwentyOne && !dealerHasTwentyOne && hand == playerHands.get(playerHands.size() - 1)) {
            output.println("SERVERMESSAGE--WAITING--TURN");
        }
    }

    /**
     * This method begins the hit stand portion of the turn and gets the users initial choice which will be used in a Switch statement in takeTurn.
     * @param hand
     */

    private void startHitStand(GameHand hand) {
        receivedChoice = false;
        do {
            output.println("SERVERMESSAGE--HANDVALUE--" + PlayerID + "--" + hand.handValue());
            output.println("SERVERMESSAGE--TURNOPTION--HITSTAND--" + playerHands.indexOf(hand) + "--" + PlayerID);
            getPlayerChoice();
            if (!choice.equals("Hit") && !choice.equals("Stand")) {
                output.println("SERVERMESSAGE--TURNOPTIONERROR--" + getPlayerID());
                receivedChoice = false;
            }
        } while (!receivedChoice);
    }

    /**
     *
     * This method handles the Hit Stand portion of the player's turn.
     * @param hand
     */
    private void hitStand(GameHand hand) {
        if (choice.equals("Hit")) {
            Card newCard = game.dealCard();
            hand.addCard(newCard);
            output.println("SERVERMESSAGE--NEWPLAYERCARD--" + playerHands.indexOf(hand) + "--" + PlayerID + "--" + newCard);
            while (choice.equals("Hit") && hand.handValue() <= TWENTY_ONE) {
                startHitStand(hand);
                if (choice.equals("Hit")) {
                    newCard = game.dealCard();
                    hand.addCard(newCard);
                    output.println("SERVERMESSAGE--NEWPLAYERCARD--" + playerHands.indexOf(hand) + "--" + PlayerID + "--" + newCard);
                }
            }
        }
        output.println("SERVERMESSAGE--HANDVALUE--" + PlayerID + "--" + hand.handValue());
        if (hand.handValue() > TWENTY_ONE) {
            setPlayerBust(true);
            output.println("SERVERMESSAGE--BUST--" + getPlayerID());
        }
    }


    /**
     * This method updates the status of the round after the initial deal.  It checks whether any players have a natural 21 or if the dealer has a natural 21.  It also sets the players' naturalTwentyOneLastRound boolean to true.
     */

    private void gameStatusCheck() {
        for (Player player : game.getPlayerList()) {
            if (player.getThisPlayerHand().handValue() == TWENTY_ONE && !player.isDealer() && player.getThisPlayerHand().size() == 2) {
                player.naturalTwentyOneLastRound = true;
                playersHaveTwentyOne = true;
            }

            if (player.isDealer() && player.getThisPlayerHand().handValue() == TWENTY_ONE && player.getThisPlayerHand().size() == 2) {
                player.naturalTwentyOneLastRound = true;
                dealerHasTwentyOne = true;
            }

            if (player.getThisPlayerHand().handValue() > TWENTY_ONE) {
                player.setPlayerBust(true);
            }
        }

        if(playersHaveTwentyOne || dealerHasTwentyOne){
            game.setPlayersHaveNatural21(true);
        }
    }


    /**
     * Method sends the end result of the round to the Client.  This includes the stakes and then updates the Client's stakes label in the view.
     * @param hand
     */

    private void sendResult(GameHand hand) {
        output.println("SERVERMESSAGE--HANDVALUE--" + PlayerID + "--" + hand.handValue());
        for (Player player : game.getPlayerList()) {
            for (int i = 0; i < player.thisPlayerHand.size(); i++) {
                output.println("SERVERMESSAGE--NEWPLAYERCARDREVEALED--0--" + player.getPlayerID() + "--" + player.getThisPlayerHand().getCard(i));
            }
        }

        if (!playersHaveTwentyOne && !dealerHasTwentyOne) {

            if (isDealer()) {
                if (!isPlayerBust()) {
                    int stakesToBeRemoved = 0;
                    int stakesToBeAdded = 0;
                    for (Player player : game.getPlayerList()) {
                        if (player.getThisPlayerHand().handValue() > hand.handValue() && !player.isPlayerBust()) {
                            stakesToBeRemoved++;
                        }
                        if (player.getThisPlayerHand().handValue() < hand.handValue() || player.isPlayerBust()) {
                            stakesToBeAdded++;
                        }
                    }
                    stakes -= stakesToBeRemoved;
                    stakes += stakesToBeAdded;
                    output.println("SERVERMESSAGE--ROUNDRESULT--NORMAL--DEALER--" + PlayerID + "--" + stakes);
                } else if (isPlayerBust()) {
                    int stakesToBeRemoved = 0;
                    for (Player player : game.getPlayerList()) {
                        if (!player.isPlayerBust() && !player.equals(this)) {
                            stakesToBeRemoved++;
                        }
                    }
                    stakes -= stakesToBeRemoved;
                    output.println("SERVERMESSAGE--ROUNDRESULT--NORMAL--DEALER--" + PlayerID + "--" + stakes);
                }

            } else {
                if (!isPlayerBust()) {
                    int originalStakes = stakes;
                    if (game.getDealer().isPlayerBust()) {
                        stakes += 1;

                    } else if (hand.handValue() > game.getDealer().getThisPlayerHand().handValue()) {
                        stakes += 1;

                    } else if (hand.handValue() == game.getDealer().getThisPlayerHand().handValue()) {
                        this.stakes = originalStakes;

                    } else if (hand.handValue() < game.getDealer().getThisPlayerHand().handValue()) {
                        stakes -= 1;
                    }
                } else {
                    stakes -= 1;
                }

                output.println("SERVERMESSAGE--ROUNDRESULT--NORMAL--PLAYER--" + PlayerID + "--" + stakes);
            }

        } else if (playersHaveTwentyOne && dealerHasTwentyOne) {

            if (isDealer()) {
                int numPlayers = game.numPlayers();
                for (Player player : game.getPlayerList()) {
                    if (!player.hasNaturalTwentyOne()) {
                        numPlayers--;
                    }
                }
                stakes += numPlayers * 2;

            } else {
                giveStakes();
            }
            output.println("SERVERMESSAGE--ROUNDRESULT--TWENTYONE--PLAYERANDDEALER--" + PlayerID + "--" + stakes);

        } else if (!playersHaveTwentyOne) {

            if (isDealer()) {
                int numPlayers = game.numPlayers();
                stakes += numPlayers * 2;
                output.println("SERVERMESSAGE--ROUNDRESULT--TWENTYONE--DEALER--" + PlayerID + "--" + stakes);
            }

        } else {
            giveStakes();
            output.println("SERVERMESSAGE--ROUNDRESULT--TWENTYONE--PLAYER--" + PlayerID + "--" + stakes);
        }
    }



    /**
     * Method calculates how many stakes need to be added or removed.
     */

    private void giveStakes() {
        if (hasNaturalTwentyOne()) {
            int stakesToAdd = game.numPlayers();
            for (Player player : game.getPlayerList()) {
                if (player.hasNaturalTwentyOne()) {
                    stakesToAdd--;
                }
            }
            stakes += stakesToAdd * 2;

        } else {
            int stakesToLose = 0;
            for (Player player : game.getPlayerList()) {
                if (player.hasNaturalTwentyOne()) {
                    stakesToLose++;
                }
            }
            stakes -= stakesToLose * 2;
        }
    }

    /**
     * This method checks whether the Client wants to continue playing at the end of the round.
     */

    private void getContinuePlaying() {
        if (stakes >= game.minimumBet()) {
            receivedChoice = false;
            do {
                output.println("SERVERMESSAGE--GETCONTINUEPLAYING");
                getPlayerChoice();
                if (!choice.equals("Yes") && !choice.equals("No")) {
                    output.println("SERVERMESSAGE--CONTINUEPLAYINGRESPONSE--ERROR");
                    receivedChoice = false;
                }
            } while (!receivedChoice);
            if (choice.equals("Yes")) {
                continuePlaying = true;
                output.println("SERVERMESSAGE--CONTINUEPLAYINGRESPONSE--CONTINUE--" + game.getPlayerList().size());
            } else {
                game.removePlayer(this);
            }
        } else {
            game.removePlayer(this);
        }
        game.continuePlayingLatchCountDown();
    }


    /**
     * This method gets the sendClientMessage result from the Client.
     */

    private void getPlayerChoice() {
        try {
            while (!receivedChoice) {
                String clientMessage;
                if ((clientMessage = input.readLine()) != null) {
                    choice = clientMessage;
                    receivedChoice = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        String output = "";
        output += "Player ID: " + PlayerID;
        if (isDealer()) {
            output += ", this player is the dealer.";
        }
        output += " Player Hand: " + thisPlayerHand.toString();
        return output;
    }



    public void startLatchCountDown() {
        startLatch.countDown();
    }

    public void dealLatchCountDown() {
        dealLatch.countDown();
    }

    public void betLatchCountDown() {
        betLatch.countDown();
    }


    public void hitStandLatchCountDown() {
        hitStandLatch.countDown();
    }




    //Getters and Setters

    public int getPlayerID() {
        return PlayerID;
    }

    public boolean hadNaturalTwentyOneLastRound() {
        return naturalTwentyOneLastRound;
    }

    public void setNaturalTwentyOneLastRound(boolean naturalTwentyOneLastRound) {
        this.naturalTwentyOneLastRound = naturalTwentyOneLastRound;
    }

    public boolean isPlayerBust() {
        return playerBust;
    }

    public void setPlayerBust(boolean playerBust) {
        this.playerBust = playerBust;
    }

    public GameHand getThisPlayerHand() {
        return thisPlayerHand;
    }

    public boolean isDealer() {
        return dealer;
    }

    public void setDealer(boolean b) {
        this.dealer = b;
    }

    public GameHand thisPlayerHand() {
        return thisPlayerHand;
    }

    public boolean hasNaturalTwentyOne() {
        if (thisPlayerHand.size() == 2 && thisPlayerHand.handValue() == TWENTY_ONE) {
            naturalTwentyOne = true;
        }
        return naturalTwentyOne;
    }
}

