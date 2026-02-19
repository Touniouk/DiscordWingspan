package game.service;

import game.Game;
import game.GameLobby;
import game.Player;
import game.components.enums.Constants;
import game.components.enums.Expansion;
import game.components.enums.FoodType;
import game.components.enums.HabitatEnum;
import game.components.meta.GameAction;
import game.components.meta.Round;
import game.components.subcomponents.BirdCard;
import game.exception.GameInputException;
import game.service.enumeration.PlayerState;
import game.service.enumeration.PlayerStateMachine;
import game.ui.discord.enumeration.DiscordObject;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import util.LogLevel;
import util.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Singleton service managing the game lifecycle: creating games, starting turns,
 * validating player actions, and progressing through rounds.
 */
public class GameService {

    // Logger
    private final Logger logger = new Logger(GameService.class, LogLevel.ALL);
    private static final GameService INSTANCE = new GameService();

    @Getter
    private final Map<String, Game> activeGames = new HashMap<>();
    private final Map<String, GameLobby> activeLobbies = new HashMap<>();
    private int lobbyCounter = 0;

    private GameService() {}

    public static GameService getInstance() {
        return INSTANCE;
    }

    // ======================== LOBBY MANAGEMENT ========================

    public GameLobby createLobby(User creator, TextChannel channel) {
        String lobbyId = "lobby-" + lobbyCounter++;
        GameLobby lobby = new GameLobby(lobbyId, creator, channel);
        activeLobbies.put(lobbyId, lobby);
        logger.info("Created lobby " + lobbyId + " by " + creator.getName());
        return lobby;
    }

    public GameLobby getLobby(String lobbyId) {
        return activeLobbies.get(lobbyId);
    }

    public void removeLobby(String lobbyId) {
        activeLobbies.remove(lobbyId);
    }

    public Game createGameFromLobby(GameLobby lobby) {
        List<Expansion> expansions = lobby.getExpansions();
        boolean withNectar = lobby.isNectarBoard();
        long seed = lobby.getSeed();
        User[] playerUsers = lobby.getPlayers().toArray(new User[0]);

        Game game = new Game(lobby.getGameChannel(), seed, activeGames.values().size(),
                5, 2, withNectar, expansions, playerUsers);
        activeGames.put(game.getGameId(), game);
        removeLobby(lobby.getLobbyId());
        game.startGame();
        return game;
    }

    // ======================== GAME MANAGEMENT ========================

    /**
     * Confirms a player's starting hand selection in the specified game.
     *
     * @throws GameInputException if the selection is invalid
     */
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
        String gameId = game.getGameId();
        Button takeTurnButton = Button.success(DiscordObject.PROMPT_TAKE_TURN_BUTTON.name() + ":" + gameId, "\uD83C\uDFAF Take Turn");
        Button seeBoardButton = Button.secondary(DiscordObject.PROMPT_SEE_BOARD_BUTTON.name() + ":" + gameId, "\uD83D\uDCCB See Board");
        Button seeFeederButton = Button.secondary(DiscordObject.PROMPT_SEE_FEEDER_BUTTON.name() + ":" + gameId, "\uD83C\uDFB2 See Feeder");
        Button seeTrayButton = Button.secondary(DiscordObject.PROMPT_SEE_TRAY_BUTTON.name() + ":" + gameId, "\uD83D\uDC26 See Tray");
        Button seeGoalsButton = Button.secondary(DiscordObject.PROMPT_SEE_GOALS_BUTTON.name() + ":" + gameId, "\uD83C\uDFC6 See Goals");
        game.getGameChannel().sendMessage(
                player.getUser().getAsMention() + " please take your turn (turn " + game.getTurnCounter() + ")")
                .addActionRow(takeTurnButton, seeBoardButton, seeFeederButton, seeTrayButton, seeGoalsButton)
                .queue(message -> player.setTurnPromptMessageId(message.getIdLong()));
    }

    /**
     * Checks if all players in the game are ready, and if so, starts the first turn.
     */
    public void checkAllPlayersReady(Game game) {
        if (game.allPlayersReady()) {
            startFirstTurn(game);
        }
    }

    /**
     * Returns all active games that the given player is participating in.
     */
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

    /**
     * Checks a single (non-slash) food cost against the spent food.
     * Wild food types in the cost can be paid with any food. Excess food
     * can cover missing types at a 2-for-1 rate.
     *
     * @return negative if underpaid, positive if overpaid, 0 if exact
     */
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

    /**
     * Finalizes playing a bird: commits food, removes eggs, moves the bird from hand to habitat, and ends the turn.
     */
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

    /** Announces the egg-laying result and ends the player's turn. */
    public void confirmLayEggs(Game currentGame, Player currentPlayer, int totalEggs) {
        currentGame.getGameChannel().sendMessage(
                currentPlayer.getUser().getAsMention() + " laid " + totalEggs + " egg" + (totalEggs != 1 ? "s" : "")
        ).queue();

        endTurn(currentGame, currentPlayer);
    }

    /**
     * Ends the current player's turn, advances to the next player, and starts their turn.
     * If it's the end of the round or the end of the game, process that as well
     */
    public void endTurn(Game currentGame, Player currentPlayer) {
        logger.info("Ending turn for player " + currentPlayer.getUser().getName());

        // Remove the buttons from the previous turn message
        if (currentPlayer.getTurnPromptMessageId() != 0) {
            currentGame.getGameChannel().editMessageComponentsById(currentPlayer.getTurnPromptMessageId()).queue();
            currentPlayer.setTurnPromptMessageId(0);
        }

        currentGame.sendTurnSummaryMessage(currentPlayer);
        PlayerStateMachine.transition(currentPlayer, PlayerState.WAITING_FOR_TURN);
        currentGame.advanceTurn();
        Player nextPlayer = currentGame.getPlayers().get(currentGame.getCurrentPlayerIndex());

        // Check if it's the end of the round
        if (currentGame.getTurnCounter() >= currentGame.getCurrentRound().getNumberOfTurns()) {
            // If it is, move to next round
            endRound(currentGame, nextPlayer);
        } else {
            // If it's not, simply advance to the next player
            startTurnForPlayer(currentGame, nextPlayer);
        }
    }

    /**
     * End round, calculates round end points and starts the next round
     * If it's the end of the game, process that as well
     */
    public void endRound(Game currentGame, Player nextPlayer) {
        logger.info("Starting round " + currentGame.getRoundCounter());
        currentGame.advanceRound();
        Round nextRound = currentGame.getCurrentRound();

        // TODO: Activate round end powers

        // TODO: Calculate round end points

        // Remove cube
        if (!Constants.NO_GOAL.equals(nextRound.getRoundEndGoal().getName())) {
            nextRound.setNumberOfTurns(nextRound.getNumberOfTurns() - 1);
        }

        // TODO: Show round results
//        Button takeTurnButton = Button.success(DiscordObject.PROMPT_TAKE_TURN_BUTTON.name() + ":" + gameId, "\uD83C\uDFAF Take Turn");
//        Button seeBoardButton = Button.secondary(DiscordObject.PROMPT_SEE_BOARD_BUTTON.name() + ":" + gameId, "\uD83D\uDCCB See Board");
//        Button seeFeederButton = Button.secondary(DiscordObject.PROMPT_SEE_FEEDER_BUTTON.name() + ":" + gameId, "\uD83C\uDFB2 See Feeder");
//        Button seeTrayButton = Button.secondary(DiscordObject.PROMPT_SEE_TRAY_BUTTON.name() + ":" + gameId, "\uD83D\uDC26 See Tray");
//        Button seeGoalsButton = Button.secondary(DiscordObject.PROMPT_SEE_GOALS_BUTTON.name() + ":" + gameId, "\uD83C\uDFC6 See Goals");
//        game.getGameChannel().sendMessage(
//                        player.getUser().getAsMention() + " please take your turn (turn " + game.getTurnCounter() + ")")
//                .addActionRow(takeTurnButton, seeBoardButton, seeFeederButton, seeTrayButton, seeGoalsButton)
//                .queue();

        // Start turn
        startTurnForPlayer(currentGame, nextPlayer);
    }

    /**
     * Check if a player taking an action is allowed to take said action
     */
    public boolean isPlayerAllowedAction(Player player, GameAction action) {
        return switch (action) {
            case TAKE_TURN -> player.getState() == PlayerState.PLAYING_TURN;
        };
    }

    /** Announces the card-drawing result and ends the player's turn. */
    public void confirmDrawCards(Game currentGame, Player currentPlayer, int totalCards) {
        currentGame.getGameChannel().sendMessage(
                currentPlayer.getUser().getAsMention() + " drew " + totalCards + " card" + (totalCards != 1 ? "s" : "")
        ).queue();

        endTurn(currentGame, currentPlayer);
    }
}
