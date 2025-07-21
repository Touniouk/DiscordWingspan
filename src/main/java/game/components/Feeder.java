package game.components;

import game.components.subcomponents.Die;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Feeder {

    private final List<Die> diceOutOfFeeder;
    private final List<Die> diceInFeeder;

    public Feeder(boolean withNectar) {
        this.diceInFeeder = IntStream.range(1, 5).mapToObj(i -> new Die(withNectar)).collect(Collectors.toList());
        this.diceOutOfFeeder = new ArrayList<>();
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
     * Re-rolls every dice in the feeder
     */
    public void reRollFeeder() {
        diceInFeeder.addAll(diceOutOfFeeder);
        diceOutOfFeeder.clear();
        diceInFeeder.forEach(Die::rollDie);
    }
}
