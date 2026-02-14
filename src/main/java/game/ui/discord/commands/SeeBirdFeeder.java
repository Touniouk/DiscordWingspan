package game.ui.discord.commands;

import game.components.Feeder;
import game.service.DiscordBotService;
import game.ui.discord.enumeration.Constants;
import game.ui.discord.enumeration.EmojiEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import util.StringUtil;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Slash command: {@code /see_bird_feeder} - Displays the current state of the birdfeeder dice.
 */
public class SeeBirdFeeder implements SlashCommand {

    private static final String name = "see_bird_feeder";
    private static final String description = "See the birdfeeder dice";

    private static final String PARAM_GAME_ID = Constants.GAME_ID;

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
        Optional<DiscordBotService.GameContext> gameContextOptional = DiscordBotService.resolveGameContext(event);
        if (gameContextOptional.isEmpty()) return;
        DiscordBotService.GameContext gameContext = gameContextOptional.get();

        seeBirdFeeder(event, gameContext);
    }

    public static void seeBirdFeeder(IReplyCallback event, DiscordBotService.GameContext gameContext) {
        event.replyEmbeds(getFeederEmbed(gameContext.game().getFeeder()).build())
                .setEphemeral(true)
                .queue();
    }

    private static EmbedBuilder getFeederEmbed(Feeder feeder) {
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
