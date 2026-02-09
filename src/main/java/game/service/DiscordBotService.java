package game.service;

import game.Game;
import game.Player;
import game.exception.GameInputException;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import util.LogLevel;
import util.Logger;

public class DiscordBotService {

    // Logger
    private final Logger logger = new Logger(GameService.class, LogLevel.ALL);
    private static final DiscordBotService INSTANCE = new DiscordBotService();

    @Getter
    private JDA jda;

    private DiscordBotService() {}

    public static DiscordBotService getInstance() {
        return INSTANCE;
    }

    public void setDiscordBot(JDA jda) {
        this.jda = jda;
    }

    public void sendMessage(TextChannel gameChannel, String message) {
        gameChannel.sendMessage(message).queue();
    }

    public Game getGameFromId(GenericInteractionCreateEvent event, String gameId) throws GameInputException {
        Game currentGame = GameService.getInstance().getGame(gameId);
        if (currentGame == null) {
            throw new GameInputException("No active game found with game id `" + gameId + "`");
        } else if (currentGame.getPlayerById(event.getUser().getIdLong()) == null) {
            throw new GameInputException("You are not part of game `" + gameId + "`");
        }
        return currentGame;
    }

    public Player getPlayerFromGame(GenericInteractionCreateEvent event, String gameId) throws GameInputException {
        Game currentGame = getGameFromId(event, gameId);
        return currentGame.getPlayerById(event.getUser().getIdLong());
    }
}
