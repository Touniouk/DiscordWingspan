package game.ui.discord.processors;

import game.ui.discord.commands.*;
import game.ui.discord.commands.example.ExampleEmbed;
import game.ui.discord.commands.example.ExampleModal;
import game.ui.discord.commands.example.ExampleSelectMenu;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Routes incoming slash command events to the appropriate {@link SlashCommand} implementation.
 * All slash commands must be registered in the static initializer.
 */
public class SlashCommandProcessor {

    private static final Map<String, SlashCommand> slashCommandList;

    static {
        slashCommandList = Stream.of(
                // Add every new command here
                new ExampleModal(),
                new ExampleEmbed(),
                new ExampleSelectMenu(),
                new CreateGame(),
                new PickStartingHand(),
                new TakeTurn(),
                new GetActiveGames(),
                new SeeBoard(),
                new SeeBirdFeeder(),
                new SeeTray()
        ).collect(Collectors.toMap(SlashCommand::getName, c -> c));
    }

    public static void handleCommand(SlashCommandInteractionEvent event) {
        slashCommandList.get(event.getName()).handle(event);
    }

    public static void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        slashCommandList.get(event.getName()).handleAutoComplete(event);
    }

    /** Returns the command data for all registered slash commands, used for guild registration. */
    public static List<CommandData> getCommandsData() {
        return slashCommandList.values().stream().map(SlashCommand::getCommandData).collect(Collectors.toList());
    }
}
