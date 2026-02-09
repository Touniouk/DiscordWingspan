package game.ui.discord.commands;

import game.Player;
import game.components.subcomponents.BirdCard;
import game.exception.GameInputException;
import game.service.DiscordBotService;
import game.ui.discord.enumeration.EmojiEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import util.StringUtil;

import java.util.Objects;
import java.util.Optional;

public class SeeBoard implements SlashCommand {

    private static final String name = "see_board";
    private static final String description = "See a player's board";

    private static final String PARAM_GAME_ID = "game_id";
    private static final String PARAM_USER = "user";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(name, description)
                .addOption(OptionType.STRING, PARAM_GAME_ID, "The game id", true, true)
                .addOption(OptionType.USER, PARAM_USER, "The player board to see (yours by default)", false);
    }

    @Override
    public void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        SlashCommand.autoCompleteGameId(event);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String gameId = Objects.requireNonNull(event.getOption(PARAM_GAME_ID)).getAsString();
        Optional<User> userOptional = Optional.ofNullable(event.getOption(PARAM_USER)).map(OptionMapping::getAsUser);
        User user = userOptional.orElseGet(event::getUser);
        boolean showHiddenInfo = user.getIdLong() == event.getUser().getIdLong();

        Player currentPlayer;
        try {
            currentPlayer = DiscordBotService.getInstance().getPlayerFromGame(event, gameId);
        } catch (GameInputException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(getBoardEmbed(currentPlayer, showHiddenInfo).build())
                .setEphemeral(true)
                .queue();
    }

    private EmbedBuilder getBoardEmbed(Player player, boolean showHiddenInfo) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(StringUtil.replacePlaceholders("**" + player.getUser().getName() + "️'s Board: **"));
        embed.setColor(0x1abc9c);

        String boardString = "**Played Birds: **\n" +
                EmojiEnum.FOREST.getEmoteId() + ": " + StringUtil.getListAsString(player.getBoard().getForest().getBirds().stream().map(BirdCard::toString), ", ") + "\n" +
                EmojiEnum.GRASSLAND.getEmoteId() + ": " + StringUtil.getListAsString(player.getBoard().getGrassland().getBirds().stream().map(BirdCard::toString), ", ") + "\n" +
                EmojiEnum.WETLAND.getEmoteId() + ": " + StringUtil.getListAsString(player.getBoard().getWetland().getBirds().stream().map(BirdCard::toString), ", ") + "\n" +
                "\n" +
                "**Food: **\n" + EmojiEnum.getFoodAsEmojiList(player.getHand().getPantry()) + "\n\n" +
                "**Birds: **\n" +
                (showHiddenInfo ?
                        StringUtil.getListAsString(player.getHand().getBirdCards().stream().map(BirdCard::toString), ", ") :
                        StringUtil.getListAsString(player.getHand().getBirdCards().stream().map(c -> EmojiEnum.CARD.getEmoteId()), " ")) + "\n\n" +
                "**Bonuses: **\n" +
                (showHiddenInfo ?
                        StringUtil.getListAsString(player.getHand().getBonusCards().stream().map(c -> EmojiEnum.BONUS.getEmoteId() + " " + c.getName()), ", ") :
                        StringUtil.getListAsString(player.getHand().getBonusCards().stream().map(c -> EmojiEnum.BONUS.getEmoteId()), " "));

        embed.setDescription(boardString);
        return embed;
    }
}
