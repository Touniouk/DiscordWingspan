package game.components;

import game.components.enums.FoodType;
import game.components.subcomponents.BirdCard;
import game.components.subcomponents.BonusCard;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a player's hand: bird cards, bonus cards, food pantry, and temporary state
 * used during turn actions (food spending, egg laying, card drawing).
 */
@Getter
public class Hand {
    private final List<BirdCard> birdCards;
    private final List<BonusCard> bonusCards;
    private final Map<FoodType, Integer> pantry;
    private final Map<FoodType, Integer> tempPantrySpentFood;
    private final Map<FoodType, Integer> tempPantryAvailableFood;
    private final Map<BirdCard, Integer> tempEggsToLay;
    private final List<BirdCard> tempDrawnBirds;

    public Hand(boolean nectar) {
        this.birdCards = new ArrayList<>();
        this.bonusCards = new ArrayList<>();
        this.pantry = Stream.of(FoodType.WORM, FoodType.SEED, FoodType.FRUIT, FoodType.RODENT, FoodType.FISH, FoodType.NECTAR)
                .collect(Collectors.toMap(food -> food, food -> 0));
        this.tempPantrySpentFood = Stream.of(FoodType.WORM, FoodType.SEED, FoodType.FRUIT, FoodType.RODENT, FoodType.FISH, FoodType.NECTAR)
                .collect(Collectors.toMap(food -> food, food -> 0));
        this.tempPantryAvailableFood = Stream.of(FoodType.WORM, FoodType.SEED, FoodType.FRUIT, FoodType.RODENT, FoodType.FISH, FoodType.NECTAR)
                .collect(Collectors.toMap(food -> food, food -> 0));
        this.tempEggsToLay = new HashMap<>();
        this.tempDrawnBirds = new ArrayList<>();
    }

    public void addBird(BirdCard birdCard) {
        this.birdCards.add(birdCard);
    }

    public void addBonus(BonusCard bonusCard) {
        this.bonusCards.add(bonusCard);
    }

    /**
     * Resets the temporary food-spending state, clearing spent food and
     * restoring available food to match the current pantry.
     */
    public void resetTempPantry() {
        tempPantrySpentFood.forEach((k, v) -> tempPantrySpentFood.put(k, 0));
        pantry.forEach((k, v) -> tempPantryAvailableFood.put(k, pantry.get(k)));
    }

    public void resetTempDrawnBirds() {
        tempDrawnBirds.clear();
    }

    public void resetPantry() {
        pantry.forEach((k, v) -> pantry.put(k, 0));
    }

    /**
     * Finds a bird card in hand by its common name.
     *
     * @param birdName the bird's common name
     * @return the matching bird card, or empty if not in hand
     */
    public Optional<BirdCard> getBirdByName(@NonNull String birdName) {
        return birdCards.stream().filter(bird -> birdName.equals(bird.getName())).findAny();
    }

    /**
     * Commits the temporary food spending to the actual pantry.
     */
    public void confirmSpentFood() {
        tempPantryAvailableFood.forEach((k, v) -> pantry.put(k, tempPantryAvailableFood.get(k)));
        resetTempPantry();
    }

    public void resetTempEggs() {
        tempEggsToLay.clear();
    }

    /**
     * Adds one temporary egg to a bird (pending confirmation via {@link #confirmLayEggs()}).
     */
    public void addTempEgg(BirdCard bird) {
        tempEggsToLay.merge(bird, 1, Integer::sum);
    }

    /**
     * Removes one temporary egg from a bird. If the bird has no temp eggs, it is removed from the map.
     */
    public void removeTempEgg(BirdCard bird) {
        int current = tempEggsToLay.getOrDefault(bird, 0);
        if (current <= 1) {
            tempEggsToLay.remove(bird);
        } else {
            tempEggsToLay.put(bird, current - 1);
        }
    }

    public int getTempEggsForBird(BirdCard bird) {
        return tempEggsToLay.getOrDefault(bird, 0);
    }

    public int getTotalTempEggs() {
        return tempEggsToLay.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Commits all temporary eggs to their respective bird nests and clears the temp state.
     */
    public void confirmLayEggs() {
        tempEggsToLay.forEach((bird, count) ->
                bird.getNest().setNumberOfEggs(bird.getNest().getNumberOfEggs() + count));
        resetTempEggs();
    }
}
