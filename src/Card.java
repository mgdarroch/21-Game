public class Card {
    private final Rank RANK;    // rank of the card
    private final Suit SUIT;    // suit of the card

    public Card(Rank rank, Suit suit) {
        RANK = rank;
        SUIT = suit;
    }

    public int value() {
        return RANK.value;
    }

    public Rank rank() {
        return RANK;
    }

    @Override
    public String toString() {
        return RANK + "_of_" + SUIT;
    }

    public enum Rank {
        ACE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), JACK(10), QUEEN(10), KING(10);

        private final int value;  // value of the rank

        Rank(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}