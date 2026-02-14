package game.ui.discord.commands;

import game.Game;
import game.service.GameService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import util.StringUtil;

import java.util.List;

/**
 * Slash command: {@code /get_active_games} - Lists the user's currently active games.
 */
public class GetActiveGames implements SlashCommand {

    private static final String name = "get_active_games";
    private static final String description = "See your list of active games";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(name, description);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        List<Game> activeGames = GameService.getInstance().getActiveGames(event.getUser().getIdLong());
        event.reply("**Your active games are:**\n\n" + StringUtil.getListAsString(activeGames.stream().map(Game::getGameId), ", "))
                .setEphemeral(true)
                .queue();
    }
}
