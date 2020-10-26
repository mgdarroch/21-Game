import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Game implements Runnable {

    private static final int TWENTY_ONE = 21;
    private ArrayList<Player> playerList = new ArrayList<>();
    private final int minimumStake;
    private final int numberOfDecks;
    private DeckHolder deckHolder;
    private CountDownLatch placedBetsLatch;
    private CountDownLatch turnLatch;
    private CountDownLatch continuePlayingLatch;
    private int roundCount;
    private final int startingStakes;
    private int stakeTotal;
    private final int startingNumPlayers;
    private boolean playersHaveNatural21;

    public Game(int minimumBet, int numberOfDecks, int startingStakes, int startingNumPlayers) {
        this.minimumStake = minimumBet;
        this.numberOfDecks = numberOfDecks;
        this.startingStakes = startingStakes;
        this.startingNumPlayers = startingNumPlayers;
        this.stakeTotal = setStakeTotal();
        roundCount = 0;
    }


    /**
     * This ensures there is only one dealer in the game at one time
     */

    private void verifyDealer() {
        int dealerCount = 0;
        for (Player player : playerList) {
            if (player.isDealer()) {
                dealerCount++;
            }
        }

        if (dealerCount != 1) {
            for (Player player : playerList) {
                player.setDealer(false);
            }
            if (roundCount == 0) {
                pickRandomDealer();
            }
        }
    }

    /**
     * These methods handle the picking and changing of the dealer in the game.
     */

    private void pickRandomDealer() {
        int randomNum = new Random().nextInt(numPlayers());
        System.out.println("Random number is: " + randomNum);
        playerList.get(randomNum).setDealer(true);
    }

    public void dealerChange() {
        boolean naturalTwentyOneChange = false;
        for (Player player : playerList) {
            if (player.hadNaturalTwentyOneLastRound()) {
                player.setDealer(true);
                naturalTwentyOneChange = true;
                clearNaturalTwentyOneBooleans();
                break;
            }
        }

        if (!naturalTwentyOneChange) {
            playerList.get(0).setDealer(true);
        }
    }


    @Override
    public void run() {
        deckHolder = new DeckHolder(numberOfDecks);
        deckHolder.shuffle();
        do {
            playTwentyOne();
        } while (numPlayers() > 1);
    }


    /**
     * This runs the game.  It uses a series of CountDownLatches to coordinate players in the playerList.
     */


    private void playTwentyOne() {
        setup();
        verifyDealer();
        System.out.println("Round is: " + roundCount);
        for (Player player : playerList) {
            player.startLatchCountDown();
        }
        try {
            placedBetsLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Player player : playerList) {
            player.betLatchCountDown();
        }
        dealInitialCards();
        for (Player player : playerList) {
            player.dealLatchCountDown();
        }
        printPlayers();
        try {
            turnLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (playersHaveNatural21) {
            for (Player player : playerList) {
                player.hitStandLatchCountDown();
            }
        } else {
            for (Player player : playerList) {
                player.takeTurn(player.thisPlayerHand());
            }

            for (Player player : playerList) {
                player.hitStandLatchCountDown();
            }
        }


        for (Player player : playerList) {
            player.hitStandLatchCountDown();
        }

        try {
            continuePlayingLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        roundCount++;
    }

    /**
     * This sets up the game at the start of each round.
     */

    private void setup() {

        //New deckHolder
        deckHolder = new DeckHolder(numberOfDecks);
        deckHolder.shuffle();
        System.out.println("The round is: " + roundCount);
        if (roundCount == 0) {
            pickRandomDealer();
        } else {
            dealerChange();
        }
        rearrangePlayers();
        System.out.println("Number of players: " + numPlayers());
        playersHaveNatural21 = false;
        placedBetsLatch = new CountDownLatch(numPlayers());
        turnLatch = new CountDownLatch(numPlayers());
        continuePlayingLatch = new CountDownLatch(numPlayers());
    }


    /**
     * Prints the players
     */

    private void printPlayers() {
        for (Player player : playerList) {
            System.out.println(player.toString());
        }
    }

    /**
     * This method rearranges the playerList array.  It ensures that the round begins with the player on the dealers left.
     * It ensures that the player immediately following the dealer is the first to go and that the dealer is the last to go.
     */
    public synchronized void rearrangePlayers() {
        Iterator<Player> playerIterator = playerList.iterator();
        ArrayList<Player> tempArrayUntilDealer = new ArrayList<>();
        ArrayList<Player> tempArrayAfterDealer = new ArrayList<>();
        boolean dealerFound = false;
        while (playerIterator.hasNext()) {
            Player player = playerIterator.next();
            if (player.isDealer()) {
                dealerFound = true;
            } else if (!player.isDealer() && !dealerFound) {
                tempArrayUntilDealer.add(player);
                playerIterator.remove();
            } else if (!player.isDealer() && dealerFound) {
                tempArrayAfterDealer.add(player);
                playerIterator.remove();
            }
        }
        ArrayList<Player> finalPlayerList = new ArrayList<>();
        finalPlayerList.addAll(tempArrayAfterDealer);
        finalPlayerList.addAll(tempArrayUntilDealer);
        finalPlayerList.addAll(playerList);
        playerList.clear();
        playerList = finalPlayerList;
    }

    /**
     * Deals a card
     *
     * @return
     */

    public Card dealCard() {
        if (deckHolder.remainingCards() == 0) {
            deckHolder = new DeckHolder(numberOfDecks);
            deckHolder.shuffle();
        }
        return deckHolder.dealCard();
    }

    /**
     * Deals the initial cards of the round
     */

    private void dealInitialCards() {
        for (int i = 0; i < 2; i++) {
            for (Player player : playerList) {
                player.thisPlayerHand().addCard(dealCard());
            }
        }
    }


    /**
     * The following methods deal with adding and removing players from the playerList
     */


    public void addPlayer(Player player) {
        playerList.add(player);
    }


    public void removePlayer(Player player) {
        playerList.remove(player);
    }

    public int numPlayers() {
        return playerList.size();
    }


    /**
     * The following are the countDownLatch countdown methods.
     */


    public void eligibleStakesLatch() {
        placedBetsLatch.countDown();
    }


    public void turnLatchCountDown() {
        turnLatch.countDown();
    }


    public void continuePlayingLatchCountDown() {
        continuePlayingLatch.countDown();
    }


    // GETTERS AND SETTERS

    private int setStakeTotal() {
        stakeTotal = startingStakes * startingNumPlayers;
        return stakeTotal;
    }

    public ArrayList<Player> getPlayerList() {
        return playerList;
    }


    public Player getDealer() {
        for (Player player : playerList) {
            if (player.isDealer()) {
                return player;
            }
        }
        return null;
    }

    public double minimumBet() {
        return minimumStake;
    }

    public void setPlayersHaveNatural21(boolean b) {
        playersHaveNatural21 = b;
    }

    public void clearNaturalTwentyOneBooleans() {
        for (Player player : playerList) {
            player.setNaturalTwentyOneLastRound(false);
        }
    }


    //Method does not work.

    public void removePlayerByID(int playerCount) {
        for (Player player : playerList) {
            if (player.getPlayerID() == playerCount) {
                removePlayer(player);
            }
        }
    }

}
