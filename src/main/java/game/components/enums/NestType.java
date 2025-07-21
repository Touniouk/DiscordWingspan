package game.components.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum NestType {
    STAR("Star", "Wild"),
    CUP("Cup", "Bowl"),
    GROUND("Ground", "Ground"),
    PLATFORM("Platform", "Platform"),
    CAVITY("Cavity", "Cavity");

    @Getter
    private final String label;
    private final String jsonName;
    private static final Map<String, NestType> jsonNameMap = Arrays.stream(NestType.values())
            .collect(Collectors.toMap(e -> e.jsonName, e -> e));

    NestType(String label, String jsonName) {
        this.label = label;
        this.jsonName = jsonName;
    }

    public static NestType fromJsonName(String jsonName) {
        return jsonNameMap.get(jsonName);
    }
}
