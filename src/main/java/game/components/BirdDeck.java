package game.components;

import game.components.subcomponents.BirdCard;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BirdDeck extends CardDeck<BirdCard> {

    @Getter
    private final BirdCard[] tray;

    public BirdDeck(List<BirdCard> cardDeck) {
        super(cardDeck);
        tray = new BirdCard[3];
        refillTray();
    }

    public void refillTray() {
        for (int i = 0; i < tray.length; i++) {
            if (tray[i] == null) {
                tray[i] = drawCard();
            }
        }
    }

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
