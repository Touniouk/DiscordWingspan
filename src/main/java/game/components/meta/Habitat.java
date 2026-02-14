package game.components.meta;

import game.components.subcomponents.BirdCard;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for a board habitat row. Each habitat holds up to
 * {@value #numberOfSpaceInHabitat} bird cards.
 */
@Getter
public class Habitat {

    public static final int numberOfSpaceInHabitat = 5;

    private final List<BirdCard> birds = new ArrayList<>();
    private final boolean nectarBoard;

    public Habitat(boolean nectarBoard) {
        this.nectarBoard = nectarBoard;
    }

    public boolean isHabitatFull() {
        return birds.size() >= numberOfSpaceInHabitat;
    }

    /**
     * Returns the egg cost to play a bird in this habitat, based on how many birds are already placed.
     * 0 birds = free, 1-2 birds = 1 egg, 3+ birds = 2 eggs.
     */
    public int getNumberOfEggsToSpend() {
        if (birds.size() == 0) return 0;
        if (birds.size() < 3) return 1;
        return 2;
    }

    public void addBird(BirdCard birdToPlay) {
        birds.add(birdToPlay);
    }
}
