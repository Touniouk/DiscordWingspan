package game.components.meta;

import game.components.subcomponents.BirdCard;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Habitat {

    public static final int numberOfSpaceInHabitat = 5;

    @Getter
    private List<BirdCard> birds = new ArrayList<>();

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
