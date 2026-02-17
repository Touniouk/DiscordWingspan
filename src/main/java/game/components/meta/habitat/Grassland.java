package game.components.meta.habitat;

import game.components.meta.Habitat;

public class Grassland extends Habitat {
    public Grassland(boolean nectarBoard) {
        super(nectarBoard);
    }

    /**
     * Number of eggs a player can lay based on birds in grassland.
     */
    public int getNumberOfEggsToLay() {
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
                case 0, 1 -> 2;
                case 2, 3 -> 3;
                case 4, 5 -> 4;
                default -> throw new IllegalStateException("Unexpected value: " + getBirds().size());
            };
        }
    }

    @Override
    public int getNumberOfResourcesToDiscard() {
        if (isNectarBoard()) {
            return switch (getBirds().size()) {
                case 1, 5 -> 0;
                case 0, 2, 4 -> 1;
                case 3 -> 2;
                default -> throw new IllegalStateException("Unexpected value: " + getBirds().size());
            };
        } else {
            return switch (getBirds().size()) {
                case 1, 3, 5 -> 1;
                case 0, 2, 4 -> 0;
                default -> throw new IllegalStateException("Unexpected value: " + getBirds().size());
            };
        }
    }
}
