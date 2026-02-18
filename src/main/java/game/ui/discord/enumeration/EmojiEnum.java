package game.ui.discord.enumeration;

import game.components.enums.FoodType;
import game.components.subcomponents.DieFace;
import lombok.Getter;
import util.LogLevel;
import util.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public enum EmojiEnum {
    // Food types
    SEED("seed", "<:seed:1471704305122672680>"),
    WORM("invertebrate", "<:invertebrate:1471704292330049677>"),
    FISH("fish", "<:fish:1471704282263851008>"),
    RODENT("rodent", "<:rodent:1471704303180714147>"),
    FRUIT("fruit", "<:fruit:1471704286827253842>"),
    NECTAR("nectar", "<:nectar:1471704294423134342>"),
    WILD("wild", "<:wild:1471704309229158420>"),
    NO_FOOD("nofood", "<:nofood:1471704296394461307>"),

    // Game objects
    DIE("die", "<:die:1471704277729677484>"),
    CARD("card", "<:card:1471704274445537444>"),
    EGG("egg", "<:egg:1471704279868768316>"),
    BIRD("bird", "<:bird:1471704270079266876>"),
    BONUS("bonus-card", "<:bonuscard:1471704271417512093>"),
    CUBE("cube", "<:cube:1473493065544106127>"),

    // Nest types
    CAVITY("cavity", "<:cavity:1471704276098093199>"),
    BOWL("bowl", "<:bowl:1471704272721940531>"),
    PLATFORM("platform", "<:platform:1471704298034434058>"),
    GROUND("ground", "<:ground:1471704290509979689>"),
    STAR("star", "<:star:1471704306376904805>"),

    // Habitat
    WETLAND("wetland", "<:wetland:1471704307291131916>"),
    GRASSLAND("grassland", "<:grassland:1471704289037647937>"),
    FOREST("forest", "<:forest:1471704285430415410>"),

    // BEAK
    BEAK_RIGHT("beak-right", "<:beakright:1471704268527501393>"),
    BEAK_LEFT("beak-left", "<:beakleft:1471704266673750088>"),

    // POWERS
    PREDATOR("predator", "<:predator:1471704301494603886>"),
    FLOCKING("flocking", "<:flocking:1471704283404701830>"),

    // OTHERS
    FEATHER_POINTS("point", "<:point:1471704299808489625>");

    private static final Logger logger = new Logger(EmojiEnum.class, LogLevel.ALL);

    private final String placeholder;
    private final String emoteId;

    private static final Map<String, String> placeholderToEmoteMap;

    static {
        placeholderToEmoteMap = Arrays.stream(EmojiEnum.values()).collect(Collectors.toMap(e -> e.placeholder, e -> e.emoteId));
    }

    EmojiEnum(String placeholder, String emoteId) {
        this.placeholder = placeholder;
        this.emoteId = emoteId;
    }

    public static String getEmojiIdFromPlaceholder(String s, String def) {
        return placeholderToEmoteMap.getOrDefault(s, def);
    }

    public static String getFoodAsEmojiList(Map<FoodType, Integer> pantry) {
        StringBuilder builder = new StringBuilder();
        pantry.forEach((k, v) -> IntStream.range(0, v).forEach(i -> builder.append(k.getEmoji().getEmoteId()).append(", ")));
        return builder.length() > 1 ? builder.substring(0, builder.length() - 2) : builder.toString();
    }

    /**
     * Returns just the first emoji for a die face (e.g. for WORM_SEED returns the worm emoji only).
     * Useful for button display where only one emoji is allowed.
     */
    public static String getFirstEmojiFromDieFace(DieFace dieFace) {
        return getEmojiFromDieFace(dieFace).split("/")[0];
    }

    public static String getEmojiFromDieFace(DieFace dieFace) {
        return switch (dieFace) {
            case WORM -> WORM.emoteId;
            case SEED -> SEED.emoteId;
            case WORM_SEED -> WORM.emoteId + "/" + SEED.emoteId;
            case FRUIT -> FRUIT.emoteId;
            case MOUSE -> RODENT.emoteId;
            case FISH -> FISH.emoteId;
            case SEED_NECTAR -> SEED.emoteId + "/" + NECTAR.emoteId;
            case FRUIT_NECTAR -> FRUIT.emoteId + "/" + NECTAR.emoteId;
        };
    }
}
