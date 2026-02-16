package game;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import game.components.BirdDeck;
import game.components.BonusDeck;
import game.components.Feeder;
import game.components.enums.*;
import game.components.meta.Nest;
import game.components.meta.Power;
import game.components.subcomponents.BirdCard;
import game.components.subcomponents.BonusCard;
import game.exception.GameInputException;
import game.service.enumeration.GameState;
import game.service.enumeration.GameStateMachine;
import game.service.enumeration.PlayerState;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import util.LogLevel;
import util.Logger;
import util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
public class Game {

    // Seed
    public static long GAME_SEED;

    // Components
    public List<BirdCard> birdCards;
    public List<BonusCard> bonusCards;
    private final List<Player> players;
    private final BirdDeck birdDeck;
    private final BonusDeck bonusDeck;
    private final Feeder feeder;

    // Game dynamic parameters
    @Setter
    private GameState state;
    private int currentPlayerIndex;
    private int turnCounter = 1;

    // Game static parameters
    private final String gameId;
    private final TextChannel gameChannel;
    private final List<Expansion> expansions;
    private final int startingBirdHandSize;
    private final int startingBonusHandSize;

    // Logger
    private final Logger logger = new Logger(Game.class, LogLevel.ALL);

    public Game(TextChannel gameChannel, int gameId, User... players) {
        this(gameChannel, gameId, List.of(Expansion.values()), players);
    }

    public Game(TextChannel gameChannel, int gameId, List<Expansion> expansions, User... players) {
        this(gameChannel, 0, gameId, 5, 2, expansions.contains(Expansion.OCEANIA), expansions, players);
    }

    public Game(TextChannel gameChannel, long seed, int gameId, User... players) {
        this(gameChannel, seed, gameId, 5, 2, true, List.of(Expansion.values()), players);
    }

    public Game(TextChannel gameChannel, long seed, int gameId, int startingBirdHandSize, int startingBonusHandSize, boolean withNectar, List<Expansion> expansions, User... playerUsers) {
        if (playerUsers.length == 0) {
            throw new IllegalArgumentException("Must provide at least one player name");
        }

        GAME_SEED = seed == 0 ? new Random().nextLong() : seed;
        logger.info("Setting up Game with seed : " + GAME_SEED);

        this.gameId = "game_id-" + gameId;
        this.state = GameState.CREATED;
        this.gameChannel = gameChannel;
        this.startingBirdHandSize = startingBirdHandSize;
        this.startingBonusHandSize = startingBonusHandSize;

        logger.debug(String.format("Parameters:\nSeed : %s\nstartingBirdHandSize : %s\nstartingBonusHandSize : %s\nwithNectar : %s\nexpansions : %s\nplayers : %s",
                GAME_SEED, startingBirdHandSize, startingBonusHandSize, withNectar, expansions, StringUtil.getListAsString(Arrays.stream(playerUsers).map(User::getName).toList(), ", ")));

        logger.unnecessary("Setting up players");
        players = Arrays.stream(playerUsers)
                .map(player -> new Player(player, withNectar))
                .collect(Collectors.toList());
        this.expansions = expansions;

        logger.unnecessary("Setup feeder");
        this.feeder = new Feeder(withNectar);

        logger.unnecessary("Setup bird deck");
        getAllBirdCards();
        birdDeck = new BirdDeck(birdCards.stream()
                .filter(c -> expansions.contains(c.getExpansion()))
                .collect(Collectors.toList()));
        birdDeck.shuffleDeck();

        logger.unnecessary("Setup bonus deck");
        getAllBonusCards();
        bonusDeck = new BonusDeck(bonusCards.stream()
                .filter(c -> expansions.contains(c.getExpansion()))
                .filter(c -> !c.isAutomaExclusive())
                .collect(Collectors.toList()));
        bonusDeck.shuffleDeck();

        // TODO: Setup EOR

        logger.unnecessary("Game setup completed");
    }

    /**
     * Send starting hands
     */
    public void startGame() {
        logger.unnecessary("Send starting hands");
        players.forEach(p -> {
            List<BirdCard> startingBirdHand = IntStream.range(0, startingBirdHandSize).mapToObj(i -> birdDeck.drawCard()).toList();
            startingBirdHand.forEach(p::addBirdInHand);
            List<BonusCard> startingBonusHand = IntStream.range(0, startingBonusHandSize).mapToObj(i -> bonusDeck.drawCard()).toList();
            startingBonusHand.forEach(p::addBonusInHand);
        });

        GameStateMachine.transition(this, GameState.STARTING_HANDS_SENT);
    }

    /**
     * Confirms a player's starting hand, discards unselected birds and bonuses back to their decks,
     * and transitions the game to GAME_STARTED.
     *
     * @throws GameInputException if the player's selection is invalid
     */
    public void confirmStartingHandPick(long userId) throws GameInputException {
        Player player = getPlayerById(userId);
        player.confirmStartingHandPick();

        // Discard unselected bird cards
        for (BirdCard birdCard : player.getHand().getBirdCards().stream().filter(b -> !b.isSelected()).toList()) {
            player.getHand().getBirdCards().remove(birdCard);
            birdDeck.discard(birdCard);
        }

        // Discard unselected bonus cards
        for (BonusCard bonusCard : player.getHand().getBonusCards().stream().filter(b -> !b.isSelected()).toList()) {
            player.getHand().getBonusCards().remove(bonusCard);
            bonusDeck.discard(bonusCard);
        }

        GameStateMachine.transition(this, GameState.GAME_STARTED);
    }

    /**
     * Get <b>all</b> the bonus cards from the big bonus json
     */
    private void getAllBonusCards() {
        bonusCards = new ArrayList<>();
        try (InputStream is = Game.class.getClassLoader().getResourceAsStream(Constants.BONUS_JSON)) {
            JsonNode root = new ObjectMapper().readTree(is);
            Iterator<JsonNode> iter = root.elements();
            while (iter.hasNext()) {
                JsonNode bonusNode = iter.next();
                BonusCard bonusCard = new BonusCard();
                bonusCard.setId(bonusNode.get("id").asInt());
                bonusCard.setName(bonusNode.get("Name").asText());
                bonusCard.setCondition(bonusNode.get("Condition").asText());
                bonusCard.setExpansion(Expansion.fromJsonName(bonusNode.get("Expansion").asText()));
                bonusCard.setExplanatoryText(bonusNode.get("Explanatory text").asText());
                bonusCard.setAutomaCompatible(bonusNode.get("Automa").asBoolean());
                bonusCard.setAutomaExclusive(bonusCard.getName().contains("[automa]"));
                bonusCards.add(bonusCard);
            }
        } catch (IOException e) {
            logger.error(String.format("Couldn't get all bonus cards : %s", e.getMessage()));
        }
    }

    /**
     * Get <b>all</b> the bird cards from the big bird json
     */
    private void getAllBirdCards() {
        birdCards = new ArrayList<>();
        try (InputStream is = Game.class.getClassLoader().getResourceAsStream(Constants.BIRD_JSON)) {
            JsonNode root = new ObjectMapper().readTree(is);
            Iterator<JsonNode> iter = root.elements();
            while (iter.hasNext()) {
                JsonNode birdNode = iter.next();
                BirdCard birdCard = new BirdCard();
                birdCard.setName(birdNode.get("Common name").asText());
                birdCard.setScientificName(birdNode.get("Scientific name").asText());
                birdCard.setExpansion(Expansion.fromJsonName(birdNode.get("Expansion").asText()));
                birdCard.setPower(new Power());
                birdCard.getPower().setPowerText(birdNode.get("Power text").asText());
                birdCard.setPredator(birdNode.get("Predator").asText().equals("X"));
                birdCard.setFlocking(birdNode.get("Flocking").asText().equals("X"));
                birdCard.setBonus(birdNode.get("Bonus card").asText().equals("X"));
                birdCard.setFeatherPoints(birdNode.get("Victory points").asInt());
                NestType nestType = NestType.fromJsonName(birdNode.get("Nest type").asText());
                int capacity = birdNode.get("Egg capacity").asInt();
                birdCard.setNest(new Nest(capacity, nestType));
                birdCard.setWingspan(birdNode.get("Wingspan").asInt());
                birdCard.setHabitats(new ArrayList<>());
                if (birdNode.get(HabitatEnum.FOREST.getJsonValue()).asText().equals("X")) birdCard.getHabitats().add(HabitatEnum.FOREST);
                if (birdNode.get(HabitatEnum.GRASSLAND.getJsonValue()).asText().equals("X")) birdCard.getHabitats().add(HabitatEnum.GRASSLAND);
                if (birdNode.get(HabitatEnum.WETLAND.getJsonValue()).asText().equals("X")) birdCard.getHabitats().add(HabitatEnum.WETLAND);
                boolean or = birdNode.get("/ (food cost)").asText().equals("X");
                if (!or) {
                    List<FoodType> foodCost = new ArrayList<>();
                    Arrays.stream(FoodType.values()).forEach(foodType -> {
                        int amount = birdNode.get(foodType.getJsonName()).asInt();
                        IntStream.range(0, amount).forEach(i -> foodCost.add(foodType));
                    });
                    birdCard.setFoodCost(List.of(foodCost));
                } else {
                    List<List<FoodType>> foodCost = Arrays.stream(FoodType.values())
                            .filter(foodType -> birdNode.get(foodType.getJsonName()).asInt() > 0)
                            .map(List::of)
                            .toList();
                    birdCard.setFoodCost(foodCost);
                }
                birdCard.setLanguageBonusCards(Stream.of("Anatomist", "Cartographer", "Historian", "Photographer")
                        .filter(s -> birdNode.get(s).asText().equals("X"))
                        .collect(Collectors.toList()));
                birdCards.add(birdCard);
            }
        } catch (IOException e) {
            logger.error(String.format("Couldn't get all bird cards : %s", e.getMessage()));
        }
    }

    /**
     * Confirm the selection of drawn birds. This includes the selected tray birds as well as the birds drawn from the deck
     */
    public int confirmDrawBirdSelection(Player player, List<Integer> selectedTrayIndexes) {
        int drawnCards = player.getHand().getTempDrawnBirds().size() + selectedTrayIndexes.size();
        player.getHand().getBirdCards().addAll(player.getHand().getTempDrawnBirds());
        player.getHand().getBirdCards().addAll(this.getBirdDeck().getTrayBirds(selectedTrayIndexes));
        player.getHand().getTempDrawnBirds().clear();
        return drawnCards;
    }

    //*****************************************************************
    // STATIC MEMBERS
    //*****************************************************************

    /** Finds a bird card by its ID from the full card list. */
    public BirdCard getBirdCardById(int birdId) {
        return birdCards.stream().filter(b -> b.getId() == birdId).findAny().orElseThrow(IllegalArgumentException::new);
    }

    /** Finds a bonus card by its ID from the full card list. */
    public BonusCard getBonusCardById(int bonusId) {
        return bonusCards.stream().filter(b -> b.getId() == bonusId).findAny().orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Finds a player in this game by their Discord user ID.
     *
     * @throws GameInputException if no player with that ID exists in this game
     */
    public Player getPlayerById(long userId) throws GameInputException {
        return this.players.stream().filter(p -> p.getUser().getIdLong() == userId).findAny()
                .orElseThrow(() -> new GameInputException("This userId is not part of game `" + gameId + "`"));
    }

    /** Returns true if every player has confirmed their starting hand. */
    public boolean allPlayersReady() {
        return players.stream().allMatch(p -> p.getState() == PlayerState.READY);
    }

    /** Advances to the next player, incrementing the turn counter when wrapping around. */
    public void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        if (currentPlayerIndex == 0) {
            turnCounter++;
        }
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
}
