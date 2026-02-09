package game.components.meta;

import lombok.Getter;

public enum BoardAction {
    PLAY_BIRD("Play a bird"),
    GAIN_FOOD("Gain food"),
    LAY_EGGS("Lay eggs"),
    DRAW_CARDS("Draw cards");

    @Getter
    private final String label;

    BoardAction(String label) {
        this.label = label;
    }
}
