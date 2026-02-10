package game.ui.discord.commands;

import game.Game;
import game.components.Feeder;
import game.components.subcomponents.Die;
import game.exception.GameInputException;
import game.service.DiscordBotService;
import game.ui.discord.enumeration.EmojiEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import util.StringUtil;

import java.util.stream.Collectors;

public class SeeBirdFeeder implements SlashCommand {

    private static final String name = "see_bird_feeder";
    private static final String description = "See the birdfeeder dice";

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
        try {
            gameId = SlashCommand.resolveGameId(event, PARAM_GAME_ID);
            currentGame = DiscordBotService.getInstance().getGameFromId(event, gameId);
        } catch (GameInputException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
            return;
        }

        event.replyEmbeds(getFeederEmbed(currentGame.getFeeder()).build())
                .setEphemeral(true)
                .queue();
    }

    private EmbedBuilder getFeederEmbed(Feeder feeder) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(StringUtil.replacePlaceholders("**\uD83C\uDFB2 Birdfeeder **"));
        embed.setColor(0x1abc9c);

        String inFeeder = feeder.getDiceInFeeder().stream()
                .map(die -> "[" + EmojiEnum.getEmojiFromDieFace(die.getVisibleFace()) + "]")
                .collect(Collectors.joining("  "));

        String outOfFeeder = feeder.getDiceOutOfFeeder().stream()
                .map(die -> "[" + EmojiEnum.getEmojiFromDieFace(die.getVisibleFace()) + "]")
                .collect(Collectors.joining("  "));

        String description = "**In feeder (" + feeder.getDiceInFeeder().size() + "):**\n" +
                (inFeeder.isEmpty() ? "Empty" : inFeeder) + "\n\n" +
                "**Out of feeder (" + feeder.getDiceOutOfFeeder().size() + "):**\n" +
                (outOfFeeder.isEmpty() ? "None" : outOfFeeder);

        embed.setDescription(description);
        return embed;
    }
}
