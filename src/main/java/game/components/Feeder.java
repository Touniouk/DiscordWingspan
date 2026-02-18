package game.components;

import game.Game;
import game.components.subcomponents.Die;
import game.exception.GameInputException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public class Feeder {

    private final Random random;
    private final List<Die> diceOutOfFeeder;
    private final List<Die> diceInFeeder;

    public Feeder(boolean withNectar, Random random) {
        this.diceInFeeder = IntStream.range(0, 5).mapToObj(i -> new Die(withNectar, random)).collect(Collectors.toList());
        this.diceOutOfFeeder = new ArrayList<>();
        this.random = random;
        reRollFeeder();
    }

    /**
     * Returns a string representation of the die in the feeder and their index
     */
    public String seeFeeder() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < diceInFeeder.size() - 1; i++) {
            builder.append(diceInFeeder.get(i).getVisibleFace().getLabel()).append(" (").append(i).append(") | ");
        }
        builder.append(diceInFeeder.get(diceInFeeder.size() - 1).getVisibleFace().getLabel()).append(" (").append(diceInFeeder.size() - 1).append(")");
        return builder.toString();
    }

    /**
     * Get a Die from the feeder. If the feeder is empty, reset the feeder
     * @param index the index in the diceInFeeder list
     */
    public Die getDie(int index) {
        Die die = diceInFeeder.remove(index);
        diceOutOfFeeder.add(die);
        if (diceInFeeder.size() == 0) {
            reRollFeeder();
        }
        return die;
    }

    /**
     * Get multiple dice from the feeder. Indices are removed from highest to lowest to avoid index shifting.
     * @param indices the indices of dice to remove
     * @return the removed dice
     */
    public List<Die> getDice(List<Integer> indices) {
        List<Integer> sorted = indices.stream().sorted((a, b) -> b - a).toList();
        List<Die> removed = new ArrayList<>();
        for (int index : sorted) {
            Die die = diceInFeeder.remove(index);
            diceOutOfFeeder.add(die);
            removed.add(die);
        }
        if (diceInFeeder.isEmpty()) {
            reRollFeeder();
        }
        return removed;
    }

    /**
     * Re-rolls every dice in the feeder
     */
    public void reRollFeeder() {
        diceInFeeder.addAll(diceOutOfFeeder);
        diceOutOfFeeder.clear();
        diceInFeeder.forEach(Die::rollDie);
    }

    /**
     * Check if the birdfeeder can be re-rolled
     * the birdfeeder can only be re-rolled if all visible dice face are the same
     */
    public boolean canBeRerolled() {
        if (diceInFeeder.size() == 0) {
            return true;
        }
        return diceInFeeder.stream()
                .map(Die::getVisibleFace)
                .noneMatch(f -> f != diceInFeeder.get(0).getVisibleFace());
    }
}
