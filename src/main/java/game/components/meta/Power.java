package game.components.meta;

import game.components.enums.PowerColour;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Power {
    private PowerColour colour;
    private String powerText;
}
