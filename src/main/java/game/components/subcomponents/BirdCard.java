package game.components.subcomponents;

import game.components.enums.FoodType;
import game.components.enums.HabitatEnum;
import game.components.meta.Nest;
import game.components.meta.Power;
import game.ui.discord.enumeration.EmojiEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

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
    private String trivia;
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

    public String getVeryShortDescription() {
        return featherPoints + " point" + (featherPoints != 1 ? "s" : "") + " | " +
                habitats.stream().map(HabitatEnum::getJsonValue).collect(Collectors.joining(", ")) + " | " +
                nest.getCapacity() + " " + nest.getType().getLabel() + " nest";
    }

    public String getShortDescription() {
        return featherPoints + EmojiEnum.FEATHER_POINTS.getEmoteId() + " | " +
                habitats.stream().map(h -> h.getEmoji().getEmoteId()) + " | " +
                getFoodCostString() + " | " +
                nest.getCapacity() + nest.getType().getEmoji().getEmoteId() + " | " +
                getPowerCategoriesString() + " | " +
                getFacingString();
    }

    public String getFoodCostString() {
        return getFoodCost().stream()
                .map(alt -> alt.stream()
                        .map(f -> f.getEmoji().getEmoteId())
                        .collect(Collectors.joining("")))
                .collect(Collectors.joining(" / "));
    }

    private String getPowerCategoriesString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (isPredator) stringBuilder.append(EmojiEnum.PREDATOR.getEmoteId());
        if (isFlocking) stringBuilder.append(EmojiEnum.FLOCKING.getEmoteId());
        if (isBonus) stringBuilder.append(EmojiEnum.BONUS.getEmoteId());
        return stringBuilder.toString();
    }

    private String getFacingString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (facingLeft) stringBuilder.append(EmojiEnum.BEAK_LEFT.getEmoteId());
        if (facingRight) stringBuilder.append(EmojiEnum.BEAK_RIGHT.getEmoteId());
        return stringBuilder.toString();
    }
}
