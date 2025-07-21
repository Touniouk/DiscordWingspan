package game.components.subcomponents;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BonusCard extends Card {
    private boolean automaCompatible;
    private boolean automaExclusive;
    private String condition;
    private String explanatoryText;
}
