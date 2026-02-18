package game.components.meta;

import game.Player;
import game.components.subcomponents.Goal;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Round {
    @Setter
    private int numberOfTurns;
    private Goal roundEndGoal;
    @Setter
    private int playerStartingRound = 0;

    private List<Player> firstPlace = new ArrayList<>();
    private List<Player> secondPlace = new ArrayList<>();
    private List<Player> thirdPlace = new ArrayList<>();
    private List<Player> lastPlace = new ArrayList<>();

    public Round(int numberOfTurns, Goal roundEndGoal) {
        this.numberOfTurns = numberOfTurns;
        this.roundEndGoal = roundEndGoal;
    }

    public void advanceTurn() {

    }
}
