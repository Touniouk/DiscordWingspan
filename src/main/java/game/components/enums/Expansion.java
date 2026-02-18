package game.components.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Wingspan game expansions and promo packs. Used to filter cards by expansion during game setup.
 */
@Getter
public enum Expansion {
    // Expansions
    BASE("Base Game", "originalcore"),
    SWIFT_START("Swift Start", "swiftstart"),
    EUROPE("Europe", "european"),
    OCEANIA("Oceania", "oceania"),
    ASIA("Asia", "asia"),
    AMERICAS("Americas", "americas"),
    // Promo packs
    PROMO_ASIA("Promo Asia", "promoAsia"),
    PROMO_CA("Promo CA", "promoCA"),
    PROMO_EUROPE("Promo Europe", "promoEurope"),
    PROMO_NZ("Promo NZ", "promoNZ"),
    PROMO_UK("Promo UK", "promoUK"),
    PROMO_US("Promo US", "promoUS");

    private final String label;
    private final String jsonName;
    private static final Map<String, Expansion> jsonNameMap = Arrays.stream(Expansion.values())
            .collect(Collectors.toMap(e -> e.jsonName, e -> e));

    Expansion(String label, String jsonName) {
        this.label = label;
        this.jsonName = jsonName;
    }

    public boolean isPromo() {
        return name().startsWith("PROMO_");
    }

    public static Expansion fromJsonName(String jsonName) {
        return jsonNameMap.get(jsonName);
    }
}
