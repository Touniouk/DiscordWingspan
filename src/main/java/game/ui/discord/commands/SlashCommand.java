package game.ui.discord.commands;

import game.Game;
import game.service.GameService;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

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
}
