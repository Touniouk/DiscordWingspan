package game.exception;

/**
 * Thrown when a player's input is invalid (e.g. wrong food selection, not their turn).
 * The message is displayed directly to the user in Discord.
 */
public class GameInputException extends Exception {
    public GameInputException(String errorMessage) {
        super(errorMessage);
    }
}
