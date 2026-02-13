package game.components;

import game.components.enums.FoodType;
import game.components.subcomponents.BirdCard;
import game.components.subcomponents.BonusCard;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class Hand {
    private final List<BirdCard> birdCards;
    private final List<BonusCard> bonusCards;
    private final Map<FoodType, Integer> pantry;
    private final Map<FoodType, Integer> tempPantrySpentFood;
    private final Map<FoodType, Integer> tempPantryAvailableFood;
    private final Map<BirdCard, Integer> tempEggsToLay;

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
    }

    public void addBird(BirdCard birdCard) {
        this.birdCards.add(birdCard);
    }

    public void addBonus(BonusCard bonusCard) {
        this.bonusCards.add(bonusCard);
    }

    public void resetTempPantry() {
        tempPantrySpentFood.forEach((k, v) -> tempPantrySpentFood.put(k, 0));
        pantry.forEach((k, v) -> tempPantryAvailableFood.put(k, pantry.get(k)));
    }

    public void resetPantry() {
        pantry.forEach((k, v) -> pantry.put(k, 0));
    }

    public Optional<BirdCard> getBirdByName(@NonNull String birdName) {
        return birdCards.stream().filter(bird -> birdName.equals(bird.getName())).findAny();
    }

    public void confirmSpentFood() {
        tempPantryAvailableFood.forEach((k, v) -> pantry.put(k, tempPantryAvailableFood.get(k)));
        resetTempPantry();
    }

    public void resetTempEggs() {
        tempEggsToLay.clear();
    }

    public void addTempEgg(BirdCard bird) {
        tempEggsToLay.merge(bird, 1, Integer::sum);
    }

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

    public void confirmLayEggs() {
        tempEggsToLay.forEach((bird, count) ->
                bird.getNest().setNumberOfEggs(bird.getNest().getNumberOfEggs() + count));
        resetTempEggs();
    }
}
