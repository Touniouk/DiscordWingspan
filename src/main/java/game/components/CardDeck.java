package game.components;

import game.Game;
import game.components.subcomponents.Card;

import java.util.*;

public abstract class CardDeck<T extends Card> {
    private final Random random = new Random(Game.GAME_SEED);
    private Deque<T> drawDeck;
    private final Deque<T> discardDeck;

    public CardDeck(List<T> cardDeck) {
        drawDeck = new ArrayDeque<>(cardDeck);
        discardDeck = new ArrayDeque<>();
    }

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

    public void discard(T card) {
        discardDeck.addLast(card);
    }

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
