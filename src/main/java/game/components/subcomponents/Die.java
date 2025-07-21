package game.components.subcomponents;

import game.Game;
import lombok.Getter;

import java.util.Random;

public class Die {

    private final Random random = new Random(Game.GAME_SEED);
    @Getter
    private DieFace visibleFace;
    private final boolean nectarDie;

    public Die(boolean nectarDie) {
        this.nectarDie = nectarDie;
        rollDie();
    }

    /**
     * Resets the visible face of the die
     */
    public void rollDie() {
        visibleFace = nectarDie ?
                DieFace.getNectarFaces()[random.nextInt(6)] :
                DieFace.getRegularFaces()[random.nextInt(6)];
    }
}
