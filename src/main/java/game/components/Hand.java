package game.components;

import game.components.enums.FoodType;
import game.components.subcomponents.BirdCard;
import game.components.subcomponents.BonusCard;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class Hand {
    private final List<BirdCard> birdCards;
    private final List<BonusCard> bonusCards;
    private final Map<FoodType, Integer> pantry;
    private final Map<FoodType, Integer> tempPantrySpentFood;
    private final Map<FoodType, Integer> tempPantryAvailableFood;

    public Hand(boolean nectar) {
        this.birdCards = new ArrayList<>();
        this.bonusCards = new ArrayList<>();
        this.pantry = Stream.of(FoodType.WORM, FoodType.SEED, FoodType.FRUIT, FoodType.RODENT, FoodType.FISH, FoodType.NECTAR)
                .collect(Collectors.toMap(food -> food, food -> 0));
        this.tempPantrySpentFood = Stream.of(FoodType.WORM, FoodType.SEED, FoodType.FRUIT, FoodType.RODENT, FoodType.FISH, FoodType.NECTAR)
                .collect(Collectors.toMap(food -> food, food -> 0));
        this.tempPantryAvailableFood = Stream.of(FoodType.WORM, FoodType.SEED, FoodType.FRUIT, FoodType.RODENT, FoodType.FISH, FoodType.NECTAR)
                .collect(Collectors.toMap(food -> food, food -> 0));
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
}
