package game.ui.discord.enumeration;

import game.components.enums.FoodType;
import game.components.subcomponents.DieFace;
import game.ui.discord.processors.StringSelectInteractionProcessor;
import lombok.Getter;
import util.LogLevel;
import util.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public enum EmojiEnum {
    // Food types
    SEED("seed", "<:seed:1394916027992768605>"),
    WORM("invertebrate", "<:invertebrate:1394915837965504584>"),
    FISH("fish", "<:fish:1394915631912190002>"),
    RODENT("rodent", "<:rodent:1394916078689452122>"),
    FRUIT("fruit", "<:fruit:1394915684370088006>"),
    NECTAR("nectar", "<:nectar:1394915864943530014>"),
    WILD("wild", "<:wild:1394915931792085022>"),

    // Game objects
    CARD("card", "<:card:1394915394711457824>"),
    EGG("egg", "<:egg:1394915577302351993>"),
    BIRD("bird", "<:bird:1394915269763403908>"),
    BONUS("bonus-card", "<:bonuscard:1394915304265613352>"),

    // Nest types
    CAVITY("cavity", "<:cavity:1394915439129399356>"),
    BOWL("bowl", "<:bowl:1394915359294750860>"),
    PLATFORM("platform", "<:platform:1394915905489604641>"),
    GROUND("ground", "<:ground:1394915786673623070>"),
    STAR("star", "<:star:1394916002617102356>"),

    // Habitat
    WETLAND("wetland", "<:wetland:1394915957394243694>"),
    GRASSLAND("grassland", "<:grassland:1394915762304454689>"),
    FOREST("forest", "<:forest:1394915726741082204>");

    private static final Logger logger = new Logger(EmojiEnum.class, LogLevel.ALL);

    private final String placeholder;
    private final String emoteId;

    private final static Map<String, String> placeholderToEmoteMap = Arrays.stream(EmojiEnum.values()).collect(Collectors.toMap(e -> e.placeholder, e -> e.emoteId));
    private static final Map<FoodType, EmojiEnum> foodTypeToEmoteMap = Arrays.stream(FoodType.values()).collect(Collectors.toMap(e -> e, e -> EmojiEnum.valueOf(e.name())));

    EmojiEnum(String placeholder, String emoteId) {
        this.placeholder = placeholder;
        this.emoteId = emoteId;
    }

    public static String getEmojiIdFromPlaceholder(String s, String def) {
        return placeholderToEmoteMap.getOrDefault(s, def);
    }

    public static String getFoodAsEmojiList(Map<FoodType, Integer> pantry) {
        StringBuilder builder = new StringBuilder();
        pantry.forEach((k, v) -> IntStream.range(0, v).forEach(i -> builder.append(getEmojiFromFoodType(k).getEmoteId()).append(", ")));
        return builder.length() > 1 ? builder.substring(0, builder.length() - 2) : builder.toString();
    }

    public static EmojiEnum getEmojiFromFoodType(FoodType foodType) {
        logger.debug("Getting emote for food type " + foodType.name());
        return foodTypeToEmoteMap.get(foodType);
    }

    public static String getEmoteIdFromFoodType(FoodType foodType) {
        logger.debug("Getting emote String for food type " + foodType.name());
        return foodTypeToEmoteMap.get(foodType).getEmoteId();
    }

    /**
     * Returns just the first emoji for a die face (e.g. for WORM_SEED returns the worm emoji only).
     * Useful for button display where only one emoji is allowed.
     */
    public static String getFirstEmojiFromDieFace(DieFace dieFace) {
        return getEmojiFromDieFace(dieFace).split("/")[0];
    }

    public static String getEmojiFromDieFace(DieFace dieFace) {
        return switch (dieFace) {
            case WORM -> WORM.emoteId;
            case SEED -> SEED.emoteId;
            case WORM_SEED -> WORM.emoteId + "/" + SEED.emoteId;
            case FRUIT -> FRUIT.emoteId;
            case MOUSE -> RODENT.emoteId;
            case FISH -> FISH.emoteId;
            case SEED_NECTAR -> SEED.emoteId + "/" + NECTAR.emoteId;
            case FRUIT_NECTAR -> FRUIT.emoteId + "/" + NECTAR.emoteId;
        };
    }
}
