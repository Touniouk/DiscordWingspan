package game.ui.discord.commands;

import game.Game;
import game.Player;
import game.components.meta.BoardAction;
import game.components.meta.GameAction;
import game.exception.GameInputException;
import game.service.DiscordBotService;
import game.service.GameService;
import game.ui.discord.enumeration.Constants;
import game.ui.discord.enumeration.DiscordObject;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
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
                .addOption(OptionType.STRING, PARAM_GAME_ID, "The game id", true, true);
    }

    @Override
    public void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        SlashCommand.autoCompleteGameId(event);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String gameId = Objects.requireNonNull(event.getOption(PARAM_GAME_ID)).getAsString();
        Game currentGame;
        Player currentPlayer;
        try {
            currentGame = DiscordBotService.getInstance().getGameFromId(event, gameId);
            currentPlayer = currentGame.getPlayerById(event.getUser().getIdLong());
        } catch (GameInputException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
            return;
        }

        // Check if we are allowed to take a turn
        if (!GameService.getInstance().isPlayerAllowedAction(currentPlayer, GameAction.TAKE_TURN)) {
            event.reply("It is not your turn, it is currently " + currentGame.getCurrentPlayer().getUser().getName() + "'s turn, please be patient")
                    .setEphemeral(true).queue();
            return;
        }

        StringSelectMenu pickActionMenu = StringSelectMenu.create(DiscordObject.TAKE_TURN_ACTION_CHOICE_SELECT_MENU.name() + ":" + gameId)
                .setPlaceholder("Action")
                .addOptions(Arrays.stream(BoardAction.values()).map(a -> SelectOption.of(a.getLabel(), a.name())).toList())
                .build();
        event.reply("\n" + Constants.PICK_ACTION + "\n\n")
                .setEphemeral(true)
                .addActionRow(pickActionMenu)
                .queue();
    }
}
