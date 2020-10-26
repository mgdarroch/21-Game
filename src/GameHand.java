import java.util.ArrayList;

/**
 * This class represents a Hand.
 */

public class GameHand {

    public ArrayList<Card> hand = new ArrayList<>();   // holds the cards in the hand


    /**
     * Adds a card to the hand.
     * @param newCard
     */
    public void addCard(Card newCard) {
        hand.add(newCard);
    }

    /**
     * Gets how many cards are in the hand.
     * @return
     */
    public int size() {
        return hand.size();
    }

    /**
     * Gets a specific card in the hand
     * @param index
     * @return the card at index of the arraylist
     */
    public Card getCard(int index) {
        return hand.get(index);
    }

    /**
     * Returns the value of the hand.  In the case of there being an Ace added to the hand that would cause the player to Bust, the ace is changed to a 1.
     * @return
     */
    public int handValue() {
        int value = 0;
        for (Card card : hand) {
            value += card.value();
        }
        if (changeAceValue()) {
            value += 10;
        }
        return value;
    }

    private boolean hasAce() {
        for (Card card : hand) {
            if (card.value() == 1) {
                return true;
            }
        }
        return false;
    }

    public boolean changeAceValue() {
        int value = 0;
        for (Card card : hand) {
            value += card.value();
        }
        boolean returnValue = false;
        if(hasAce() && value < 12){
            returnValue = true;
        }
        return returnValue;
    }

    @Override
    public String toString() {
        String outputString = "Hand is " + hand.toString();
        return outputString;
    }
}
