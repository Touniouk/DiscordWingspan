package game.components.meta;

import lombok.Getter;

public enum Action {
    PLAY_BIRD("Play a bird"),
    GAIN_FOOD("Gain food"),
    LAY_EGGS("Lay eggs"),
    DRAW_CARDS("Draw cards");

    @Getter
    private final String label;

    Action(String label) {
        this.label = label;
    }
}
