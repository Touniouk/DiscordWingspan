package game.components.meta;

import game.components.enums.NestType;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Nest {
    private final int capacity;
    private final NestType type;
    @Setter
    private int numberOfEggs;

    public Nest(int capacity, NestType nestType) {
        this.capacity = capacity;
        this.type = nestType;
        this.numberOfEggs = 0;
    }
}
