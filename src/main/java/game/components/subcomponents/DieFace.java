package game.components.subcomponents;

import lombok.Getter;

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
}
