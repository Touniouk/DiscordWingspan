package game.components.meta.habitat;

import game.components.meta.Habitat;

public class Forest extends Habitat {

    public Forest(boolean nectarBoard) {
        super(nectarBoard);
    }

    /**
     * Number of food a player can gain based on birds in forest.
     */
    public int getNumberOfFoodToGain() {
        if (isNectarBoard()) {
            return switch (getBirds().size()) {
                case 0 -> 1;
                case 1, 2, 3 -> 2;
                case 4 -> 3;
                case 5 -> 4;
                default -> throw new IllegalStateException("Unexpected value: " + getBirds().size());
            };
        } else {
            return switch (getBirds().size()) {
                case 0, 1 -> 1;
                case 2, 3 -> 2;
                case 4, 5 -> 3;
                default -> throw new IllegalStateException("Unexpected value: " + getBirds().size());
            };
        }
    }
}
