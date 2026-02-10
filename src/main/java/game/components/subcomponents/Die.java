package game.components.subcomponents;

import lombok.Getter;

import java.util.Random;

public class Die {

    private final Random random;
    @Getter
    private DieFace visibleFace;
    private final boolean nectarDie;

    public Die(boolean nectarDie, Random random) {
        this.nectarDie = nectarDie;
        this.random = random;
        rollDie();
    }

    /**
     * Resets the visible face of the die
     */
    public void rollDie() {
        visibleFace = nectarDie ?
                DieFace.getNectarFaces()[random.nextInt(DieFace.getNectarFaces().length)] :
                DieFace.getRegularFaces()[random.nextInt(DieFace.getRegularFaces().length)];
    }
}
