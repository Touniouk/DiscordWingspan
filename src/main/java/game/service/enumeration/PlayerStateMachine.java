package game.service.enumeration;

import game.Player;

/**
 * Controls valid player state transitions during the game lifecycle.
 */
public class PlayerStateMachine {

    /**
     * Checks whether the given player state transition is valid.
     */
    public static boolean canTransition(PlayerState from, PlayerState to) {
        return switch (from) {
            case WAITING_FOR_STARTING_HAND -> to == PlayerState.READY;
            case READY, PLAYING_TURN -> to == PlayerState.WAITING_FOR_TURN;
            case WAITING_FOR_TURN -> to == PlayerState.PLAYING_TURN;
            default -> false;
        };
    }

    /**
     * Transitions a player to a new state, throwing if the transition is invalid.
     *
     * @throws IllegalStateException if the transition is not allowed
     */
    public static void transition(Player player, PlayerState to) {
        if (canTransition(player.getState(), to)) {
            player.setState(to);
        } else {
            throw new IllegalStateException("Invalid transition: " + player.getState() + " -> " + to);
        }
    }
}
