import java.util.ArrayList;
import java.util.Collections;

public class DeckHolder {

    private final ArrayList<Card> deckHolder = new ArrayList<>();   // holds the cards in the shoe

    public DeckHolder(int numDecks) {
        for (int i = 0; i < numDecks; i++) {
            addDeck(new Deck());
        }
    }


    /**
     * Adds each card of a given deck to the deckHolder.
     * @param deck
     */
    private void addDeck(Deck deck) {
        for (int i = 0; i < deck.size(); i++) {
            deckHolder.add(deck.dealCard());
        }
    }

    /**
     * Shuffles the cards in the deckHolder
     */
    public void shuffle() {
        Collections.shuffle(deckHolder);
    }

    /**
     * Returns the first card of the deckHolder and removes it from the deckHolder.
     * @return
     */
    public Card dealCard() {
        Card card = deckHolder.get(0);    // last card in the shoe
        deckHolder.remove(card);
        return card;
    }

    /**
     * The number of remaining cards.
     * @return
     */
    public int remainingCards() {
        return deckHolder.size();
    }
}
