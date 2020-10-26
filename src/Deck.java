import java.util.ArrayList;
import java.util.Collections;

/**
 * Class represents a Deck of cards.
 */
public class Deck {

    private final ArrayList<Card> deck = new ArrayList<>();   // holds the cards in the deck

    public Deck() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(rank, suit));
            }
        }
    }

    /**
     * Returns the first card of the deck.  This method is only used to fill the deckHolder.
     * @return
     */
    public Card dealCard() {
        Card card = deck.get(0);
        deck.remove(card);
        return card;
    }

    /**
     * Number of cards in the deck.
     * @return
     */
    public int size() {
        return deck.size();
    }
}
