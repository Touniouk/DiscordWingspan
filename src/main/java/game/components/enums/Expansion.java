package game.components.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Expansion {
    BASE("Base Game", "originalcore"),
    SWIFT_START("Swift Start", "swiftstart"),
    EUROPE("Europe", "european"),
    OCEANIA("Oceania", "oceania"),
    ASIA("Asia", "asia");

    private final String label;
    private final String jsonName;
    private static final Map<String, Expansion> jsonNameMap = Arrays.stream(Expansion.values())
            .collect(Collectors.toMap(e -> e.jsonName, e -> e));

    private Expansion(String label, String jsonName) {
        this.label = label;
        this.jsonName = jsonName;
    }

    public static Expansion fromJsonName(String jsonName) {
        return jsonNameMap.get(jsonName);
    }
}
