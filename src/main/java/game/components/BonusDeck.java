package game.components;

import game.components.subcomponents.BonusCard;

import java.util.List;
import java.util.Random;

/**
 * Deck of bonus cards dealt during game setup.
 */
public class BonusDeck extends CardDeck<BonusCard> {
    public BonusDeck(List<BonusCard> cardDeck, Random random) {
        super(cardDeck, random);
    }
}
