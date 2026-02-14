package game.components.subcomponents;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a bonus card that grants end-of-game points based on a specific condition.
 */
@Getter
@Setter
public class BonusCard extends Card {
    private boolean automaCompatible;
    private boolean automaExclusive;
    private String condition;
    private String explanatoryText;
}
