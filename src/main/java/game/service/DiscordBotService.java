package game.service;

import game.Game;
import game.Player;
import game.exception.GameInputException;
import game.ui.discord.enumeration.Constants;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import util.LogLevel;
import util.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Singleton service bridging Discord interactions and game logic.
 * Provides utilities to resolve game context from interaction events.
 */
public class DiscordBotService {

    // Logger
    private final Logger logger = new Logger(DiscordBotService.class, LogLevel.ALL);
    private static final DiscordBotService INSTANCE = new DiscordBotService();

    @Getter
    private JDA jda;

    private DiscordBotService() {}

    public static DiscordBotService getInstance() {
        return INSTANCE;
    }

    public void setDiscordBot(JDA jda) {
        this.jda = jda;
    }

    public void sendMessage(TextChannel gameChannel, String message) {
        gameChannel.sendMessage(message).queue();
    }

    /**
     * Looks up a game by ID and verifies the interacting user is a participant.
     *
     * @throws GameInputException if the game doesn't exist or the user isn't in it
     */
    public Game getGameFromId(GenericInteractionCreateEvent event, String gameId) throws GameInputException {
        Game currentGame = GameService.getInstance().getGame(gameId);
        if (currentGame == null) {
            throw new GameInputException("No active game found with game id `" + gameId + "`");
        } else if (currentGame.getPlayerById(event.getUser().getIdLong()) == null) {
            throw new GameInputException("You are not part of game `" + gameId + "`");
        }
        return currentGame;
    }

    /** Holds resolved context from a Discord interaction: the game, player, and component metadata. */
    public record GameContext(String componentId, String gameId, Game game, Player player) {}

    /**
     * Resolves the game context from a slash command event, replying with an error if resolution fails.
     *
     * @return the resolved context, or empty if the game/player could not be found
     */
    public static Optional<GameContext> resolveGameContext(SlashCommandInteractionEvent event) {
        String gameId;
        Game currentGame;
        Player currentPlayer;
        try {
            gameId = DiscordBotService.resolveGameId(event);
            currentGame = DiscordBotService.getInstance().getGameFromId(event, gameId);
            currentPlayer = currentGame.getPlayerById(event.getUser().getIdLong());
            return Optional.of(new GameContext("", gameId, currentGame, currentPlayer));
        } catch (GameInputException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
            return Optional.empty();
        }
    }

    /**
     * Resolves the game context from a component interaction event (button/select menu).
     * Parses the component ID to extract the game ID.
     *
     * @return the resolved context, or empty if the game/player could not be found
     */
    public static Optional<GameContext> resolveGameContext(GenericComponentInteractionCreateEvent event) {
        String[] arr = event.getComponentId().split(":");
        String componentId = arr[0];
        String gameId = arr[1];
        Game currentGame;
        Player currentPlayer;
        try {
            currentGame = DiscordBotService.getInstance().getGameFromId(event, gameId);
            currentPlayer = currentGame.getPlayerById(event.getUser().getIdLong());
            return java.util.Optional.of(new GameContext(componentId, gameId, currentGame, currentPlayer));
        } catch (GameInputException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
            return Optional.empty();
        }
    }

    /**
     * Resolves the game ID from the command option, or falls back to the user's first active game.
     * @throws GameInputException if no game ID is provided and the user has no active games
     */
    private static String resolveGameId(SlashCommandInteractionEvent event) throws GameInputException {
        String gameId = Optional.ofNullable(event.getOption(Constants.GAME_ID))
                .map(OptionMapping::getAsString)
                .orElse(null);
        if (gameId != null) {
            return gameId;
        }
        List<Game> activeGames = GameService.getInstance().getActiveGames(event.getUser().getIdLong());
        if (activeGames.isEmpty()) {
            throw new GameInputException("You have no active games. Please provide a game ID or create a game first.");
        }
        return activeGames.get(0).getGameId();
    }
}
