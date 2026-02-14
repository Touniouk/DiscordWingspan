package game.components.subcomponents;

import game.components.enums.Expansion;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for all cards (bird and bonus). Holds common fields: id, name, expansion, and selection state.
 */
@Getter
@Setter
public class Card {
    private int id;
    private String name;
    private Expansion expansion;
    private boolean selected = false;
}
