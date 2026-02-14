package game.service.enumeration;

import game.Game;

/**
 * Controls valid game state transitions: CREATED -> STARTING_HANDS_SENT -> GAME_STARTED.
 */
public class GameStateMachine {

    /**
     * Checks whether the given state transition is valid.
     */
    public static boolean canTransition(GameState from, GameState to) {
        return switch (from) {
            case CREATED -> to == GameState.STARTING_HANDS_SENT;
            case STARTING_HANDS_SENT -> to == GameState.GAME_STARTED;
            default -> false;
        };
    }

    /**
     * Transitions the game to a new state, throwing if the transition is invalid.
     *
     * @throws IllegalStateException if the transition is not allowed
     */
    public static void transition(Game game, GameState to) {
        if (canTransition(game.getState(), to)) {
            game.setState(to);
        } else {
            throw new IllegalStateException("Invalid transition: " + game.getState() + " -> " + to);
        }
    }
}
