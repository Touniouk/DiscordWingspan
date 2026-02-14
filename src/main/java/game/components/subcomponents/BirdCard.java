package game.components.subcomponents;

import game.components.enums.FoodType;
import game.components.enums.HabitatEnum;
import game.components.meta.Nest;
import game.components.meta.Power;
import game.ui.discord.enumeration.EmojiEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a bird card with its stats: food cost, habitats, nest, wingspan, power, and victory points.
 * The {@code foodCost} field is a list of alternative costs (outer list) where each alternative
 * is a list of required food types -- multiple alternatives represent "/" (or) costs.
 */
@Getter
@Setter
public class BirdCard extends Card {
    private String scientificName;
    private Power power;
    private String trivia; // TODO: Not included
    private int wingspan;
    private int featherPoints;
    private Nest nest;
    private List<HabitatEnum> habitats;
    private boolean isPredator;
    private boolean isFlocking;
    private boolean isBonus;
    private List<List<FoodType>> foodCost;
    private List<String> languageBonusCards;
    private boolean facingLeft;
    private boolean facingRight;

    @Override
    public String toString() {
        return EmojiEnum.CARD.getEmoteId() + " " + getName() + " " + String.valueOf(EmojiEnum.EGG.getEmoteId()).repeat(Math.max(0, getNest().getNumberOfEggs()));
    }
}
