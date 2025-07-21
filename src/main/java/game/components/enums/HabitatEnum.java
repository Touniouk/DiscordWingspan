package game.components.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum HabitatEnum {
    FOREST("Forest"),
    GRASSLAND("Grassland"),
    WETLAND("Wetland");

    @Getter
    private final String jsonValue;

    private static final Map<String, HabitatEnum> jsonValueToEnum = Arrays.stream(HabitatEnum.values())
            .collect(Collectors.toMap(HabitatEnum::getJsonValue, h -> h));

    HabitatEnum(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public static HabitatEnum getHabitatFromJsonValue(String jsonValue) {
        return jsonValueToEnum.getOrDefault(jsonValue, null);
    }
}
