package game.ui.discord.commands;

import game.Player;
import game.components.subcomponents.BirdCard;
import game.service.DiscordBotService;
import game.ui.discord.enumeration.Constants;
import game.ui.discord.enumeration.EmojiEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import util.StringUtil;

import java.util.Optional;

/**
 * Slash command: {@code /see_board} - Shows a player's board with played birds, food, and hand.
 * Can optionally show another player's board (with hidden info redacted).
 */
public class SeeBoard implements SlashCommand {

    private static final String name = "see_board";
    private static final String description = "See a player's board";

    private static final String PARAM_GAME_ID = Constants.GAME_ID;
    private static final String PARAM_USER = "user";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(name, description)
                .addOption(OptionType.STRING, PARAM_GAME_ID, "The game id (defaults to your active game)", false, true)
                .addOption(OptionType.USER, PARAM_USER, "The player board to see (yours by default)", false);
    }

    @Override
    public void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        SlashCommand.autoCompleteGameId(event);
        // TODO: Clicking the see board button should trigger a selector for the player
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Optional<User> userOptional = Optional.ofNullable(event.getOption(PARAM_USER)).map(OptionMapping::getAsUser);
        User user = userOptional.orElseGet(event::getUser);
        boolean showHiddenInfo = user.getIdLong() == event.getUser().getIdLong();

        Optional<DiscordBotService.GameContext> gameContextOptional = DiscordBotService.resolveGameContext(event);
        if (gameContextOptional.isEmpty()) return;
        DiscordBotService.GameContext gameContext = gameContextOptional.get();

        seeBoard(event, gameContext.player(), showHiddenInfo);
    }

    public static void seeBoard(IReplyCallback event, Player currentPlayer) {
        seeBoard(event, currentPlayer, true);
    }

    public static void seeBoard(IReplyCallback event, Player currentPlayer, boolean showHiddenInfo) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(StringUtil.replacePlaceholders("**" + currentPlayer.getUser().getName() + "️'s Board: **"));
        embed.setDescription(currentPlayer.getBoard().isNectarBoard() ? "Oceania board" : "Base board");
        embed.setColor(0x1abc9c);

        String boardString = "**Played Birds: **\n" +
                EmojiEnum.FOREST.getEmoteId() + ": " + StringUtil.getListAsString(currentPlayer.getBoard().getForest().getBirds().stream().map(BirdCard::toString), ", ") + "\n" +
                EmojiEnum.GRASSLAND.getEmoteId() + ": " + StringUtil.getListAsString(currentPlayer.getBoard().getGrassland().getBirds().stream().map(BirdCard::toString), ", ") + "\n" +
                EmojiEnum.WETLAND.getEmoteId() + ": " + StringUtil.getListAsString(currentPlayer.getBoard().getWetland().getBirds().stream().map(BirdCard::toString), ", ") + "\n" +
                "\n" +
                "**Food: **\n" + EmojiEnum.getFoodAsEmojiList(currentPlayer.getHand().getPantry()) + "\n\n" +
                "**Birds: **\n" +
                (showHiddenInfo ?
                        StringUtil.getListAsString(currentPlayer.getHand().getBirdCards().stream().map(BirdCard::toString), ", ") :
                        StringUtil.getListAsString(currentPlayer.getHand().getBirdCards().stream().map(c -> EmojiEnum.CARD.getEmoteId()), " ")) + "\n\n" +
                "**Bonuses: **\n" +
                (showHiddenInfo ?
                        StringUtil.getListAsString(currentPlayer.getHand().getBonusCards().stream().map(c -> EmojiEnum.BONUS.getEmoteId() + " " + c.getName()), ", ") :
                        StringUtil.getListAsString(currentPlayer.getHand().getBonusCards().stream().map(c -> EmojiEnum.BONUS.getEmoteId()), " "));

        embed.setDescription(boardString);

        event.replyEmbeds(embed.build())
                .setEphemeral(true)
                .queue();
    }
}
