package test;

import game.components.enums.FoodType;
import game.service.GameService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameServiceTest {

    // ***********************************
    // checkFoodCost - simple list
    // ***********************************

    @Test
    void testCheckFoodCostNoSlash_basicMatch() {
        List<FoodType> foodCost = List.of(FoodType.SEED, FoodType.FISH);
        Map<FoodType, Integer> spentFood = new HashMap<>();
        spentFood.put(FoodType.SEED, 1);
        spentFood.put(FoodType.FISH, 1);

        int result = GameService.getInstance().checkFoodCostNoSlash(foodCost, spentFood);
        assertEquals(0, result); // Perfect match
    }

    @Test
    void testCheckFoodCostNoSlash_insufficientFood() {
        List<FoodType> foodCost = List.of(FoodType.SEED, FoodType.FISH);
        Map<FoodType, Integer> spentFood = new HashMap<>();
        spentFood.put(FoodType.SEED, 1); // Missing FISH

        int result = GameService.getInstance().checkFoodCostNoSlash(foodCost, spentFood);
        assertEquals(-1, result); // One food unpaid
    }

    @Test
    void testCheckFoodCostNoSlash_withWild() {
        List<FoodType> foodCost = List.of(FoodType.SEED, FoodType.WILD);
        Map<FoodType, Integer> spentFood = new HashMap<>();
        spentFood.put(FoodType.SEED, 1);
        spentFood.put(FoodType.FISH, 1);

        int result = GameService.getInstance().checkFoodCostNoSlash(foodCost, spentFood);
        assertEquals(0, result); // 1 WILD unpaid, but still have 1 FISH unspent → 0
    }

    @Test
    void testCheckFoodCostNoSlash_overpaidSameFood() {
        List<FoodType> foodCost = List.of(FoodType.SEED);
        Map<FoodType, Integer> spentFood = new HashMap<>();
        spentFood.put(FoodType.SEED, 2); // Overpaying

        int result = GameService.getInstance().checkFoodCostNoSlash(foodCost, spentFood);
        assertEquals(1, result); // 1 extra SEED
    }

    @Test
    void testCheckFoodCostNoSlash_overpaidDifferentFood() {
        List<FoodType> foodCost = List.of(FoodType.SEED);
        Map<FoodType, Integer> spentFood = new HashMap<>();
        spentFood.put(FoodType.SEED, 1);
        spentFood.put(FoodType.FISH, 1); // Overpaying

        int result = GameService.getInstance().checkFoodCostNoSlash(foodCost, spentFood);
        assertEquals(1, result); // 1 extra FISH
    }

    @Test
    void testCheckFoodCostNoSlash_overpaidEmptyInput() {
        List<FoodType> foodCost = List.of();
        Map<FoodType, Integer> spentFood = new HashMap<>();
        spentFood.put(FoodType.SEED, 1);

        int result = GameService.getInstance().checkFoodCostNoSlash(foodCost, spentFood);
        assertEquals(1, result); // Nothing to pay, some food in hand
    }

    @Test
    void testCheckFoodCostNoSlash_emptyInput() {
        List<FoodType> foodCost = List.of();
        Map<FoodType, Integer> spentFood = new HashMap<>();

        int result = GameService.getInstance().checkFoodCostNoSlash(foodCost, spentFood);
        assertEquals(0, result); // Nothing to pay, no food in hand
    }

    // ***********************************
    // checkFoodCost - list of list
    // ***********************************

    @Test
    void testSingleFoodCostList_exactMatch() {
        List<List<FoodType>> foodCosts = List.of(List.of(FoodType.SEED, FoodType.FISH));
        Map<FoodType, Integer> spentFood = Map.of(
                FoodType.SEED, 1,
                FoodType.FISH, 1
        );

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertEquals(0, result);
    }

    @Test
    void testSingleFoodCostList_partialMatch() {
        List<List<FoodType>> foodCosts = List.of(List.of(FoodType.SEED, FoodType.FISH));
        Map<FoodType, Integer> spentFood = Map.of(
                FoodType.SEED, 1
        );

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertTrue(result < 0);
    }

    @Test
    void testMultipleFoodCostLists_oneMatchesExactly() {
        List<List<FoodType>> foodCosts = List.of(
                List.of(FoodType.SEED, FoodType.INVERTEBRATE),
                List.of(FoodType.FISH, FoodType.INVERTEBRATE)
        );
        Map<FoodType, Integer> spentFood = Map.of(
                FoodType.FISH, 1,
                FoodType.INVERTEBRATE, 1
        );

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertEquals(0, result); // One combination matches exactly
    }

    @Test
    void testMultipleFoodCostLists_noneMatches() {
        List<List<FoodType>> foodCosts = List.of(
                List.of(FoodType.SEED, FoodType.INVERTEBRATE),
                List.of(FoodType.FISH, FoodType.SEED)
        );
        Map<FoodType, Integer> spentFood = Map.of(
                FoodType.FISH, 1
        );

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertTrue(result < 0); // None match fully, should return non-zero
    }

    @Test
    void testEmptyCostList() {
        List<List<FoodType>> foodCosts = new ArrayList<>();
        Map<FoodType, Integer> spentFood = Map.of(FoodType.SEED, 1);

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertTrue(result > 0); // Default value, nothing to compare
    }

    @Test
    void testEmptySpentFood() {
        List<List<FoodType>> foodCosts = List.of(List.of(FoodType.SEED, FoodType.INVERTEBRATE));
        Map<FoodType, Integer> spentFood = new HashMap<>();

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertTrue(result < 0); // Missing all food
    }

    @Test
    void testWithWildFoodType_onlyWild() {
        List<List<FoodType>> foodCosts = List.of(List.of(FoodType.WILD));
        Map<FoodType, Integer> spentFood = Map.of(
                FoodType.SEED, 1
        );

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertEquals(0, result); // Should be able to pay wild with anything
    }

    @Test
    void testWithWildFoodType_onlyWild_tooMuchSpent() {
        List<List<FoodType>> foodCosts = List.of(List.of(FoodType.WILD));
        Map<FoodType, Integer> spentFood = Map.of(
                FoodType.SEED, 2
        );

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertTrue(result > 0); // We used too much food
    }

    @Test
    void testWithWildFoodType_twoDifferentCosts() {
        List<List<FoodType>> foodCosts = List.of(
                List.of(FoodType.WILD),
                List.of(FoodType.RODENT));
        Map<FoodType, Integer> spentFood = Map.of(
                FoodType.SEED, 1
        );

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertEquals(0, result); // Should be able to pay wild with anything
    }

    @Test
    void testTwoInOneConversion_sameFood() {
        List<List<FoodType>> foodCosts = List.of(List.of(FoodType.FISH, FoodType.FISH, FoodType.FISH));
        Map<FoodType, Integer> spentFood = Map.of(
                FoodType.FRUIT, 6
        );

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertEquals(0, result); // Should be able to pay wild with anything
    }

    @Test
    void testTwoInOneConversion_diffFood() {
        List<List<FoodType>> foodCosts = List.of(List.of(FoodType.FISH, FoodType.FISH, FoodType.FISH));
        Map<FoodType, Integer> spentFood = Map.of(
                FoodType.FRUIT, 3,
                FoodType.RODENT, 3
        );

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertEquals(0, result); // Should be able to pay wild with anything
    }

    @Test
    void testTwoInOneConversion_mixedCost_overpaid() {
        List<List<FoodType>> foodCosts = List.of(List.of(FoodType.WILD, FoodType.FISH, FoodType.FISH));
        Map<FoodType, Integer> spentFood = Map.of(
                FoodType.FRUIT, 3,
                FoodType.RODENT, 3
        );

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertTrue(result > 0);
    }

    @Test
    void testTwoInOneConversion_mixedCost_underpaid() {
        List<List<FoodType>> foodCosts = List.of(List.of(FoodType.WILD, FoodType.FISH, FoodType.FISH));
        Map<FoodType, Integer> spentFood = Map.of(
                FoodType.FRUIT, 3,
                FoodType.RODENT, 1
        );

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertTrue(result < 1);
    }

    @Test
    void testTwoInOneConversion_mixedCost_ok() {
        List<List<FoodType>> foodCosts = List.of(List.of(FoodType.WILD, FoodType.FISH, FoodType.FISH));
        Map<FoodType, Integer> spentFood = Map.of(
                FoodType.FRUIT, 3,
                FoodType.RODENT, 2
        );

        int result = GameService.getInstance().checkFoodCost(foodCosts, spentFood);
        assertEquals(result, 0);
    }
}
