package game.components.subcomponents;

import game.components.enums.FoodType;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

public enum DieFace {
    WORM("Worm"),
    SEED("Seed"),
    WORM_SEED("Worm/Seed"),
    FRUIT("Fruit"),
    MOUSE("Rodent"),
    FISH("Fish"),
    SEED_NECTAR("Seed/Nectar"),
    FRUIT_NECTAR("Fruit/Nectar");

    @Getter
    private final String label;

    DieFace(String label) {
        this.label = label;
    }

    public static DieFace[] getNectarFaces() {
        return new DieFace[] { WORM, SEED_NECTAR, WORM_SEED, FRUIT_NECTAR, FISH, MOUSE };
    }

    public static DieFace[] getRegularFaces() {
        return new DieFace[] { WORM, WORM_SEED, FRUIT, MOUSE, SEED, FISH };
    }

    /**
     * Returns the single FoodType for this die face, or empty if it's a dual-food face.
     */
    public Optional<FoodType> getSingleFoodType() {
        return switch (this) {
            case WORM -> Optional.of(FoodType.WORM);
            case SEED -> Optional.of(FoodType.SEED);
            case FRUIT -> Optional.of(FoodType.FRUIT);
            case FISH -> Optional.of(FoodType.FISH);
            case MOUSE -> Optional.of(FoodType.RODENT);
            case WORM_SEED, SEED_NECTAR, FRUIT_NECTAR -> Optional.empty();
        };
    }

    public List<FoodType> getFoodType() {
        return switch (this) {
            case WORM -> List.of(FoodType.WORM);
            case SEED -> List.of(FoodType.SEED);
            case FRUIT -> List.of(FoodType.FRUIT);
            case FISH -> List.of(FoodType.FISH);
            case MOUSE -> List.of(FoodType.RODENT);
            case WORM_SEED -> List.of(FoodType.WORM, FoodType.SEED);
            case SEED_NECTAR -> List.of(FoodType.SEED, FoodType.NECTAR);
            case FRUIT_NECTAR -> List.of(FoodType.FRUIT, FoodType.NECTAR);
        };
    }

    public boolean isDualFood() {
        return getSingleFoodType().isEmpty();
    }
}
