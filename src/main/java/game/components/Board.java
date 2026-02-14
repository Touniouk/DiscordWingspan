package game.components;

import game.components.enums.HabitatEnum;
import game.components.meta.Habitat;
import game.components.meta.habitat.Forest;
import game.components.meta.habitat.Grassland;
import game.components.meta.habitat.Wetland;
import game.components.subcomponents.BirdCard;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a player's board with three habitats: forest, grassland, and wetland.
 */
public class Board {
    private final Forest forest;
    private final Grassland grassland;
    private final Wetland wetland;

    public Board() {
        this.forest = new Forest(false);
        this.grassland = new Grassland(false);
        this.wetland = new Wetland(false);
    }

    public Board(boolean nectarBoard) {
        this.forest = new Forest(nectarBoard);
        this.grassland = new Grassland(nectarBoard);
        this.wetland = new Wetland(nectarBoard);
    }

    public Forest getForest() {
        return forest;
    }

    public Grassland getGrassland() {
        return grassland;
    }

    public Wetland getWetland() {
        return wetland;
    }

    /**
     * Returns the habitat matching the given enum.
     */
    public Habitat getHabitat(HabitatEnum habitatEnum) {
        return switch (habitatEnum) {
            case FOREST -> forest;
            case GRASSLAND -> grassland;
            case WETLAND -> wetland;
        };
    }

    /**
     * Returns all birds played across all three habitats.
     */
    public List<BirdCard> getPlayedBirds() {
        return Stream.of(forest.getBirds(), grassland.getBirds(), wetland.getBirds())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Returns all played birds that currently have at least one egg on them.
     */
    public List<BirdCard> getPlayedBirdsWithEggs() {
        return Stream.of(forest.getBirds(), grassland.getBirds(), wetland.getBirds())
                .flatMap(Collection::stream)
                .filter(b -> b.getNest().getNumberOfEggs() > 0)
                .collect(Collectors.toList());
    }
}
