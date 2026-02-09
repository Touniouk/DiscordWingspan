package game.service;

import game.Game;
import game.Player;
import game.components.enums.FoodType;
import game.components.enums.HabitatEnum;
import game.components.meta.GameAction;
import game.components.subcomponents.BirdCard;
import game.exception.GameInputException;
import game.service.enumeration.PlayerState;
import game.service.enumeration.PlayerStateMachine;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import util.LogLevel;
import util.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameService {

    // Logger
    private final Logger logger = new Logger(GameService.class, LogLevel.ALL);
    private static final GameService INSTANCE = new GameService();

    @Getter
    private Map<String, Game> activeGames = new HashMap<>();

    private GameService() {}

    public static GameService getInstance() {
        return INSTANCE;
    }

    public Game createGame(TextChannel gameChannel, List<User> playerList) {
        Game game = new Game(gameChannel, activeGames.values().size(), playerList.toArray(new User[0]));
        activeGames.put(game.getGameId(), game);
        game.startGame();
        return game;
    }

    public void confirmStartingHandPick(String gameId, long userId) throws GameInputException {
        Game game = activeGames.get(gameId);
        game.confirmStartingHandPick(userId);
    }

    public Game getGame(String gameId) {
        return activeGames.get(gameId);
    }

    private void startFirstTurn(Game game) {
        // Send message to say every player is ready and the game is starting
        logger.info("Game " + game.getGameId() + " is starting");
        game.getPlayers().forEach(player -> PlayerStateMachine.transition(player, PlayerState.WAITING_FOR_TURN));
        Player firstPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());
        startTurnForPlayer(game, firstPlayer);
    }

    private void startTurnForPlayer(Game game, Player player) {
        logger.info("Starting turn for player " + player.getUser().getName());
        PlayerStateMachine.transition(player, PlayerState.PLAYING_TURN);
        DiscordBotService.getInstance().sendMessage(game.getGameChannel(),
                player.getUser().getAsMention() + " please take your turn (turn " + game.getTurnCounter() + ") with the `take_turn` action and game id `" + game.getGameId() + "`");
    }

    public void checkAllPlayersReady(Game game) {
        if (game.allPlayersReady()) {
            startFirstTurn(game);
        }
    }

    public List<Game> getActiveGames(long playerId) {
        return activeGames
                .values()
                .stream()
                .filter(g -> g.getPlayers().stream().anyMatch(p -> p.getUser().getIdLong() == playerId))
                .collect(Collectors.toList());
    }

    /**
     * Check if we used enough food to play a bird
     * @param foodCosts the food cost of the bird we want to play, it's a list to account for slashes
     * @param spentFood the food we are spending
     * @return negative if we spent too little, positive if we spent too much, 0 if OK
     */
    public int checkFoodCost(List<List<FoodType>> foodCosts, Map<FoodType, Integer> spentFood) {
        if (foodCosts.size() == 1) {
            return checkFoodCostNoSlash(foodCosts.get(0), new HashMap<>(spentFood));
        }
        int res = 10000;
        for (List<FoodType> foodCost : foodCosts) {
            res = checkFoodCostNoSlash(foodCost, new HashMap<>(spentFood));
            if (res == 0) {
                return 0;
            }
        }
        return res;
    }

    public int checkFoodCostNoSlash(List<FoodType> foodCost, Map<FoodType, Integer> spentFood) {
        int leftoverFood = 0; // Food still unpaid from the food cost
        int unspentFood = 0; // Food we still have in hand after paying the cost
        int wildFood = 0;
        for (FoodType foodInCost : foodCost) {
            if (foodInCost == FoodType.WILD) {
                wildFood++;
            } else if (spentFood.getOrDefault(foodInCost, 0) > 0) {
                spentFood.put(foodInCost, spentFood.get(foodInCost) - 1);
            } else {
                leftoverFood++;
            }
        }
        unspentFood += spentFood.values().stream().mapToInt(i -> i).sum();
        while (wildFood > 0 && unspentFood > 0) {
            unspentFood--;
            wildFood--;
        }
        leftoverFood += wildFood;
        while (unspentFood > 1 && leftoverFood > 0) {
            unspentFood -= 2;
            leftoverFood--;
        }
        return unspentFood - leftoverFood;
    }

    public void confirmPlayBird(Game currentGame, Player currentPlayer, BirdCard birdToPlay, HabitatEnum habitatEnum, List<BirdCard> birdsToRemoveEggsFrom, int eggsToRemove) {
        // Process the food
        currentPlayer.getHand().confirmSpentFood();

        // Remove the eggs
        birdsToRemoveEggsFrom.forEach(b -> b.getNest().setNumberOfEggs(b.getNest().getNumberOfEggs() - eggsToRemove));

        // Remove the bird from hand and put it in the habitat
        currentPlayer.getHand().getBirdCards().remove(birdToPlay);
        currentPlayer.getBoard().getHabitat(habitatEnum).addBird(birdToPlay);

        // Send message
        currentGame.getGameChannel().sendMessage(
                currentPlayer.getUser().getAsMention() + " played " + birdToPlay.getName() + " in their " + habitatEnum.getJsonValue()
        ).queue();

        // End turn
        endTurn(currentGame, currentPlayer);
    }

    public void endTurn(Game currentGame, Player currentPlayer) {
        logger.info("Ending turn for player " + currentPlayer.getUser().getName());
        PlayerStateMachine.transition(currentPlayer, PlayerState.WAITING_FOR_TURN);
        currentGame.advanceTurn();
        Player nextPlayer = currentGame.getPlayers().get(currentGame.getCurrentPlayerIndex());
        startTurnForPlayer(currentGame, nextPlayer);
    }

    /**
     * Check if a player taking an action is allowed to take said action
     */
    public boolean isPlayerAllowedAction(Player player, GameAction action) {
        return switch (action) {
            case TAKE_TURN -> player.getState() == PlayerState.PLAYING_TURN;
            default -> false;
        };
    }
}
