package game.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import game.Game;
import game.components.enums.*;
import game.components.meta.Nest;
import game.components.meta.Power;
import game.components.subcomponents.BirdCard;
import game.components.subcomponents.BonusCard;
import util.LogLevel;
import util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Singleton registry that loads all bird and bonus cards once from JSON.
 * Provides lookup and search methods for slash commands like /bird_info and /bonus_info.
 */
public class CardRegistry {

    private static final CardRegistry INSTANCE = new CardRegistry();
    private final Logger logger = new Logger(CardRegistry.class, LogLevel.ALL);

    private final List<BirdCard> birdCards;
    private final List<BonusCard> bonusCards;

    private CardRegistry() {
        birdCards = loadBirdCards();
        bonusCards = loadBonusCards();
        logger.info("CardRegistry loaded " + birdCards.size() + " birds and " + bonusCards.size() + " bonus cards");
    }

    public static CardRegistry getInstance() {
        return INSTANCE;
    }

    public List<BirdCard> getAllBirdCards() {
        return Collections.unmodifiableList(birdCards);
    }

    public List<BonusCard> getAllBonusCards() {
        return Collections.unmodifiableList(bonusCards);
    }

    public Optional<BirdCard> findBirdByName(String name) {
        return birdCards.stream()
                .filter(b -> b.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public Optional<BonusCard> findBonusByName(String name) {
        return bonusCards.stream()
                .filter(b -> b.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Returns bird names matching the query (case-insensitive substring), capped at limit.
     */
    public List<String> searchBirdNames(String query, int limit) {
        String lower = query.toLowerCase();
        return birdCards.stream()
                .map(BirdCard::getName)
                .filter(name -> name.toLowerCase().contains(lower))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Returns bonus card names matching the query (case-insensitive substring), capped at limit.
     */
    public List<String> searchBonusNames(String query, int limit) {
        String lower = query.toLowerCase();
        return bonusCards.stream()
                .map(BonusCard::getName)
                .filter(name -> name.toLowerCase().contains(lower))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get <b>all</b> the bird cards from the big bird json
     */
    public List<BirdCard> loadBirdCards() {
        List<BirdCard> birdCards = new ArrayList<>();
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
        return birdCards;
    }

    /**
     * Get <b>all</b> the bonus cards from the big bonus json
     */
    public List<BonusCard> loadBonusCards() {
        List<BonusCard> cards = new ArrayList<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(Constants.BONUS_JSON)) {
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
                JsonNode vpNode = bonusNode.get("VP");
                if (vpNode != null && !vpNode.isNull()) {
                    bonusCard.setVp(vpNode.asText());
                }
                cards.add(bonusCard);
            }
        } catch (IOException e) {
            logger.error("Couldn't load bonus cards: " + e.getMessage());
        }
        return cards;
    }
}
