package game.components;

import game.components.subcomponents.BirdCard;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class BirdDeck extends CardDeck<BirdCard> {

    @Getter
    private final Deque<BirdCard> tray;

    public BirdDeck(List<BirdCard> cardDeck) {
        super(cardDeck);
        tray = new ArrayDeque<>(3);
        refillTray();
    }

    public void refillTray() {
        while (tray.size() < 3) {
            tray.add(drawCard());
        }
    }
}
