package game.components.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum FoodType {
    INVERTEBRATE("Invertebrate"),
    SEED("Seed"),
    FRUIT("Fruit"),
    FISH("Fish"),
    RODENT("Rodent"),
    NECTAR("Nectar"),
    WILD("Wild (food)");

    @Getter
    private final String jsonName;
    private static final Map<String, FoodType> jsonNameMap = Arrays.stream(FoodType.values())
            .collect(Collectors.toMap(e -> e.jsonName, e -> e));

    FoodType(String jsonName) {
        this.jsonName = jsonName;
    }

    public static FoodType fromJsonName(String jsonName) {
        return jsonNameMap.get(jsonName);
    }

    public static List<FoodType> getStartingHandFoodTypes() {
        return List.of(INVERTEBRATE, SEED, FRUIT, FISH, RODENT);
    }
}
