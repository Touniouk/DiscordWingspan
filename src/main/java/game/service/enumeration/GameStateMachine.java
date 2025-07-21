package game.service.enumeration;

import game.Game;

public class GameStateMachine {

    public static boolean canTransition(GameState from, GameState to) {
        return switch (from) {
            case CREATED -> to == GameState.STARTING_HANDS_SENT;
            case STARTING_HANDS_SENT -> to == GameState.GAME_STARTED;
            default -> false;
        };
    }

    public static void transition(Game game, GameState to) {
        if (canTransition(game.getState(), to)) {
            game.setState(to);
        } else {
            throw new IllegalStateException("Invalid transition: " + game.getState() + " -> " + to);
        }
    }
}
