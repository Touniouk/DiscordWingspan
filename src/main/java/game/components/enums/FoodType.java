package game.components.enums;

import game.ui.discord.processors.StringSelectInteractionProcessor;
import lombok.Getter;
import util.LogLevel;
import util.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum FoodType {
    WORM("Invertebrate", "Worm"),
    SEED("Seed", "Seed"),
    FRUIT("Fruit", "Fruit"),
    FISH("Fish", "Fish"),
    RODENT("Rodent", "Rodent"),
    NECTAR("Nectar", "Nectar"),
    WILD("Wild (food)", "Wild");

    private static final Logger logger = new Logger(FoodType.class, LogLevel.ALL);

    @Getter
    private final String jsonName;
    @Getter
    private final String displayName;
    private static final Map<String, FoodType> jsonNameMap = Arrays.stream(FoodType.values())
            .collect(Collectors.toMap(e -> e.jsonName, e -> e));
    private static final Map<String, FoodType> displayNameMap = Arrays.stream(FoodType.values())
            .collect(Collectors.toMap(e -> e.displayName, e -> e));

    FoodType(String jsonName, String displayName) {
        this.jsonName = jsonName;
        this.displayName = displayName;
    }

    public static FoodType fromJsonName(String jsonName) {
        logger.debug("Getting food type from json name " + jsonName);
        return jsonNameMap.get(jsonName);
    }

    public static FoodType fromDisplayName(String displayName) {
        logger.debug("Getting food type from display name " + displayName);
        return displayNameMap.get(displayName);
    }

    public static List<FoodType> getStartingHandFoodTypes() {
        return List.of(WORM, SEED, FRUIT, FISH, RODENT);
    }
}
