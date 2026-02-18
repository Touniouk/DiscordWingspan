package game.components;

import game.components.subcomponents.BirdCard;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Bird card deck with a face-up tray of 3 birds that players can pick from.
 */
public class BirdDeck extends CardDeck<BirdCard> {

    @Getter
    private final BirdCard[] tray;

    public BirdDeck(List<BirdCard> cardDeck, Random random) {
        super(cardDeck, random);
        tray = new BirdCard[3];
        refillTray();
    }

    /** Fills empty tray slots by drawing from the deck. */
    public void refillTray() {
        for (int i = 0; i < tray.length; i++) {
            if (tray[i] == null) {
                tray[i] = drawCard();
            }
        }
    }

    /**
     * Removes birds from the tray at the given indices and replaces them with new draws.
     *
     * @param indexes tray positions to take from
     * @return the birds that were removed
     */
    public List<BirdCard> getTrayBirds(List<Integer> indexes) {
        List<BirdCard> birds = new ArrayList<>();
        for (int index : indexes) {
            // TODO: Error check
            birds.add(tray[index]);
            tray[index] = this.drawCard();
        }
        return birds;
    }
}
