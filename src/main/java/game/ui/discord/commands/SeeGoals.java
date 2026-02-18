package game.ui.discord.commands;

import game.Game;
import game.components.meta.Round;
import game.components.subcomponents.Goal;
import game.service.DiscordBotService;
import game.ui.discord.enumeration.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import util.StringUtil;

import java.util.List;
import java.util.Optional;

/**
 * Slash command: {@code /see_goals} - Displays the end-of-round goals for the current game.
 */
public class SeeGoals implements SlashCommand {

    private static final String name = "see_goals";
    private static final String description = "See the end-of-round goals for this game";

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

        seeGoals(event, gameContext.game());
    }

    public static void seeGoals(IReplyCallback event, Game currentGame) {
        List<Goal> goals = currentGame.getRounds().stream().map(Round::getRoundEndGoal).toList();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("End-of-Round Goals");
        embed.setColor(0xe67e22);

        if (goals.isEmpty()) {
            embed.setDescription("No goals have been set for this game.");
        } else {
            for (int i = 0; i < goals.size(); i++) {
                Goal goal = goals.get(i);
                String condition = StringUtil.replacePlaceholders(goal.getCondition());
                embed.addField("Round " + (i + 1), condition, true);
            }
        }

        event.replyEmbeds(embed.build())
                .setEphemeral(true)
                .queue();
    }
}
