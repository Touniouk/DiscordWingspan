package game.components.subcomponents;

import game.components.enums.Expansion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Card {
    private int id;
    private String name;
    private Expansion expansion;
    private boolean selected = false;
}
