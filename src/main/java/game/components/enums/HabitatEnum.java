package game.components.enums;

import game.ui.discord.enumeration.EmojiEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The three habitats on a player's board: forest, grassland, and wetland.
 */
public enum HabitatEnum {
    FOREST("Forest", EmojiEnum.FOREST),
    GRASSLAND("Grassland", EmojiEnum.GRASSLAND),
    WETLAND("Wetland", EmojiEnum.WETLAND);

    @Getter
    private final String jsonValue;
    @Getter
    private final EmojiEnum emoji;

    private static final Map<String, HabitatEnum> jsonValueToEnum = Arrays.stream(HabitatEnum.values())
            .collect(Collectors.toMap(HabitatEnum::getJsonValue, h -> h));

    HabitatEnum(String jsonValue, EmojiEnum emoji) {
        this.jsonValue = jsonValue;
        this.emoji = emoji;
    }

    public static HabitatEnum getHabitatFromJsonValue(String jsonValue) {
        return jsonValueToEnum.getOrDefault(jsonValue, null);
    }
}
