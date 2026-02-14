package game.components;

import game.Game;
import game.components.subcomponents.Card;

import java.util.*;

/**
 * Generic card deck with draw and discard piles. When the draw pile is exhausted,
 * the discard pile is shuffled back in.
 *
 * @param <T> the card type
 */
public abstract class CardDeck<T extends Card> {
    private final Random random = new Random(Game.GAME_SEED);
    private Deque<T> drawDeck;
    private final Deque<T> discardDeck;

    public CardDeck(List<T> cardDeck) {
        drawDeck = new ArrayDeque<>(cardDeck);
        discardDeck = new ArrayDeque<>();
    }

    /**
     * Draws the top card from the deck. If the draw pile is empty, shuffles the discard pile back in.
     *
     * @return the drawn card
     * @throws ArrayIndexOutOfBoundsException if both piles are empty
     */
    public T drawCard() {
        if (drawDeck.size() == 0) {
            if (discardDeck.size() > 0) {
                shuffleDiscard();
            } else {
                throw new ArrayIndexOutOfBoundsException("Deck is empty");
            }
        }
        return drawDeck.poll();
    }

    /** Places a card into the discard pile. */
    public void discard(T card) {
        discardDeck.addLast(card);
    }

    /** Shuffles the discard pile back into the draw pile. */
    public void shuffleDeck() {
        shuffleDiscard();
    }

    private void shuffleDiscard() {
        while (discardDeck.size() > 0) {
            drawDeck.add(discardDeck.poll());
        }
        shuffleDraw();
    }

    private void shuffleDraw() {
        List<T> cardList = new ArrayList<>(drawDeck);
        Collections.shuffle(cardList, random);
        drawDeck = new ArrayDeque<>(cardList);
    }
}
