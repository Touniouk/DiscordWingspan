package game.ui.discord.commands;

import game.Game;
import game.service.GameService;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

/**
 * Interface for all Discord slash commands. Each implementation provides its name,
 * command data (description and options), and a handler for execution.
 */
public interface SlashCommand {

    String getName();

    CommandData getCommandData();

    void handle(SlashCommandInteractionEvent event);

    default void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {}

    /**
     * Provides autocomplete suggestions for the game_id option, listing the user's active games.
     */
    static void autoCompleteGameId(CommandAutoCompleteInteractionEvent event) {
        List<Game> activeGames = GameService.getInstance().getActiveGames(event.getUser().getIdLong());
        List<Command.Choice> choices = activeGames.stream()
                .map(game -> new Command.Choice(game.getGameId(), game.getGameId()))
                .toList();
        event.replyChoices(choices).queue();
    }
}
