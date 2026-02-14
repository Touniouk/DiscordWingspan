package game.components.enums;

import game.ui.discord.enumeration.EmojiEnum;
import lombok.Getter;
import util.LogLevel;
import util.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The food types used to pay bird costs and found in the birdfeeder.
 */
public enum FoodType {
    WORM("Invertebrate", "Worm", EmojiEnum.WORM),
    SEED("Seed", "Seed", EmojiEnum.SEED),
    FRUIT("Fruit", "Fruit", EmojiEnum.FRUIT),
    FISH("Fish", "Fish", EmojiEnum.FISH),
    RODENT("Rodent", "Rodent", EmojiEnum.RODENT),
    NECTAR("Nectar", "Nectar", EmojiEnum.NECTAR),
    WILD("Wild (food)", "Wild", EmojiEnum.WILD);

    private static final Logger logger = new Logger(FoodType.class, LogLevel.ALL);

    @Getter
    private final String jsonName;
    @Getter
    private final String displayName;
    @Getter
    private final EmojiEnum emoji;

    private static final Map<String, FoodType> jsonNameMap;
    private static final Map<String, FoodType> displayNameMap;

    static {
        jsonNameMap = Arrays.stream(FoodType.values())
                .collect(Collectors.toMap(e -> e.jsonName, e -> e));
        displayNameMap = Arrays.stream(FoodType.values())
                .collect(Collectors.toMap(e -> e.displayName, e -> e));
    }

    FoodType(String jsonName, String displayName, EmojiEnum emoji) {
        this.jsonName = jsonName;
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public static FoodType fromJsonName(String jsonName) {
        logger.debug("Getting food type from json name " + jsonName);
        return jsonNameMap.get(jsonName);
    }

    public static FoodType fromDisplayName(String displayName) {
        logger.debug("Getting food type from display name " + displayName);
        return displayNameMap.get(displayName);
    }

    /** Returns the food types available for starting hand selection (excludes nectar and wild). */
    public static List<FoodType> getStartingHandFoodTypes() {
        return List.of(WORM, SEED, FRUIT, FISH, RODENT);
    }
}
