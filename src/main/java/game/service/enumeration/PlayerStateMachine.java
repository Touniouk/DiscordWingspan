package game.service.enumeration;

import game.Player;

public class PlayerStateMachine {

    public static boolean canTransition(PlayerState from, PlayerState to) {
        return switch (from) {
            case WAITING_FOR_STARTING_HAND -> to == PlayerState.READY;
            case READY, PLAYING_TURN -> to == PlayerState.WAITING_FOR_TURN;
            case WAITING_FOR_TURN -> to == PlayerState.PLAYING_TURN;
            default -> false;
        };
    }

    public static void transition(Player player, PlayerState to) {
        if (canTransition(player.getState(), to)) {
            player.setState(to);
        } else {
            throw new IllegalStateException("Invalid transition: " + player.getState() + " -> " + to);
        }
    }
}
