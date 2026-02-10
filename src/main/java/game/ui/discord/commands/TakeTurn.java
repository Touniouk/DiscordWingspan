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
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.Arrays;

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
                .addOption(OptionType.STRING, PARAM_GAME_ID, "The game id (defaults to your active game)", false, true);
    }

    @Override
    public void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        SlashCommand.autoCompleteGameId(event);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String gameId;
        Game currentGame;
        Player currentPlayer;
        try {
            gameId = SlashCommand.resolveGameId(event, PARAM_GAME_ID);
            currentGame = DiscordBotService.getInstance().getGameFromId(event, gameId);
            currentPlayer = currentGame.getPlayerById(event.getUser().getIdLong());
        } catch (GameInputException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
            return;
        }

        takeTurn(event, currentGame, currentPlayer);
    }

    public static void takeTurn(SlashCommandInteractionEvent event, Game currentGame, Player currentPlayer) {
        // Check if we are allowed to take a turn
        if (!GameService.getInstance().isPlayerAllowedAction(currentPlayer, GameAction.TAKE_TURN)) {
            event.reply("It is not your turn, it is currently " + currentGame.getCurrentPlayer().getUser().getName() + "'s turn, please be patient")
                    .setEphemeral(true).queue();
            return;
        }

        StringSelectMenu pickActionMenu = StringSelectMenu.create(DiscordObject.TAKE_TURN_ACTION_CHOICE_SELECT_MENU.name() + ":" + currentGame.getGameId())
                .setPlaceholder("Action")
                .addOptions(Arrays.stream(BoardAction.values()).map(a -> SelectOption.of(a.getLabel(), a.name())).toList())
                .build();
        event.reply("\n" + Constants.PICK_ACTION + "\n\n")
                .setEphemeral(true)
                .addActionRow(pickActionMenu)
                .queue();
    }

    public static void takeTurn(ButtonInteractionEvent event, Game currentGame, Player currentPlayer) {
        if (!GameService.getInstance().isPlayerAllowedAction(currentPlayer, GameAction.TAKE_TURN)) {
            event.reply("It is not your turn, it is currently " + currentGame.getCurrentPlayer().getUser().getName() + "'s turn, please be patient")
                    .setEphemeral(true).queue();
            return;
        }

        StringSelectMenu pickActionMenu = StringSelectMenu.create(DiscordObject.TAKE_TURN_ACTION_CHOICE_SELECT_MENU.name() + ":" + currentGame.getGameId())
                .setPlaceholder("Action")
                .addOptions(Arrays.stream(BoardAction.values()).map(a -> SelectOption.of(a.getLabel(), a.name())).toList())
                .build();
        event.reply("\n" + Constants.PICK_ACTION + "\n\n")
                .setEphemeral(true)
                .addActionRow(pickActionMenu)
                .queue();
    }
}
