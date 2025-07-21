package game.components;

import game.components.subcomponents.BonusCard;

import java.util.List;

public class BonusDeck extends CardDeck<BonusCard> {
    public BonusDeck(List<BonusCard> cardDeck) {
        super(cardDeck);
    }
}
