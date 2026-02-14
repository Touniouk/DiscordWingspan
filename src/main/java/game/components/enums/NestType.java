package game.components.enums;

import game.ui.discord.enumeration.EmojiEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum NestType {
    STAR("Star", "Wild", EmojiEnum.STAR),
    CUP("Cup", "Bowl", EmojiEnum.STAR),
    GROUND("Ground", "Ground", EmojiEnum.STAR),
    PLATFORM("Platform", "Platform", EmojiEnum.STAR),
    CAVITY("Cavity", "Cavity", EmojiEnum.STAR);

    @Getter
    private final String label;
    private final String jsonName;
    @Getter
    private final EmojiEnum emoji;

    private static final Map<String, NestType> jsonNameMap = Arrays.stream(NestType.values())
            .collect(Collectors.toMap(e -> e.jsonName, e -> e));

    NestType(String label, String jsonName, EmojiEnum emoji) {
        this.label = label;
        this.jsonName = jsonName;
        this.emoji = emoji;
    }

    public static NestType fromJsonName(String jsonName) {
        return jsonNameMap.get(jsonName);
    }
}
