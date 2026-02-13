package game.ui.discord.commands;

import game.Game;
import game.Player;
import game.exception.GameInputException;
import game.service.DiscordBotService;
import game.service.GameService;
import game.ui.discord.enumeration.Constants;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;
import java.util.Optional;

public interface SlashCommand {

    String getName();

    CommandData getCommandData();

    void handle(SlashCommandInteractionEvent event);

    default void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {}

    static void autoCompleteGameId(CommandAutoCompleteInteractionEvent event) {
        List<Game> activeGames = GameService.getInstance().getActiveGames(event.getUser().getIdLong());
        List<Command.Choice> choices = activeGames.stream()
                .map(game -> new Command.Choice(game.getGameId(), game.getGameId()))
                .toList();
        event.replyChoices(choices).queue();
    }

    record GameContext(String gameId, Game game, Player player) {}

    static Optional<GameContext> resolveGameContext(SlashCommandInteractionEvent event) {
        String gameId;
        Game currentGame;
        Player currentPlayer;
        try {
            gameId = SlashCommand.resolveGameId(event, Constants.GAME_ID);
            currentGame = DiscordBotService.getInstance().getGameFromId(event, gameId);
            currentPlayer = currentGame.getPlayerById(event.getUser().getIdLong());
            return Optional.of(new GameContext(gameId, currentGame, currentPlayer));
        } catch (GameInputException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
            return Optional.empty();
        }
    }

    /**
     * Resolves the game ID from the command option, or falls back to the user's first active game.
     * @throws GameInputException if no game ID is provided and the user has no active games
     */
    static String resolveGameId(SlashCommandInteractionEvent event, String paramName) throws GameInputException {
        String gameId = Optional.ofNullable(event.getOption(paramName))
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
