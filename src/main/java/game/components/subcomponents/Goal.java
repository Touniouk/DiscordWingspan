package game.components.subcomponents;

import game.components.enums.Expansion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Goal {
    private final String name;
    private final Expansion expansion;
    private final String condition;
    private final String explanatoryText;
    private final boolean duet;
}
