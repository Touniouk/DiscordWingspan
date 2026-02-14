package game.components.meta;

import lombok.Getter;

public enum GameAction {
    TAKE_TURN("Take a turn");

    @Getter
    private final String label;

    GameAction(String label) {
        this.label = label;
    }
}
