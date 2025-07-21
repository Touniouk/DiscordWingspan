package game.ui.discord.commands;

import game.components.meta.Action;
import game.ui.discord.enumeration.Constants;
import game.ui.discord.enumeration.DiscordObject;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.Arrays;
import java.util.Objects;

public class TakeTurn implements SlashCommand {

    private static final String name = "take_turn";
    private static final String description = "A player takes a turn";

    private static final String PARAM_GAME_ID = "game_id";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(name, description)
                .addOption(OptionType.STRING, PARAM_GAME_ID, "The game id", true);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String gameId = Objects.requireNonNull(event.getOption(PARAM_GAME_ID)).getAsString();

        StringSelectMenu pickActionMenu = StringSelectMenu.create(DiscordObject.TAKE_TURN_ACTION_CHOICE_SELECT_MENU.name() + ":" + gameId)
                .setPlaceholder("Action")
                .addOptions(Arrays.stream(Action.values()).map(a -> SelectOption.of(a.getLabel(), a.name())).toList())
                .build();
        event.reply("\n" + Constants.PICK_ACTION + "\n\n")
                .setEphemeral(true)
                .addActionRow(pickActionMenu)
                .queue();
    }
}
