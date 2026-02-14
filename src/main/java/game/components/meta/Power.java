package game.components.meta;

import game.components.enums.PowerColour;
import lombok.Getter;
import lombok.Setter;

/**
 * A bird's power ability, with a colour indicating when it activates (white = once-between-turns,
 * brown = when-activated, pink = other-players'-turns, teal = end-of-game, yellow = game-end scoring).
 */
@Getter
@Setter
public class Power {
    private PowerColour colour;
    private String powerText;
}
