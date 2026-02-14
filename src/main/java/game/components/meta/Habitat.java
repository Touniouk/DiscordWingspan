package game.components.meta;

import game.components.subcomponents.BirdCard;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

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

    public int getNumberOfEggsToSpend() {
        if (birds.size() == 0) return 0;
        if (birds.size() < 3) return 1;
        return 2;
    }

    public void addBird(BirdCard birdToPlay) {
        birds.add(birdToPlay);
    }
}
