package util;

import game.ui.discord.enumeration.EmojiEnum;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Check if a string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Return a string representation of an array using a given separator<br/>
     * Calls {@link #getListAsString(List, String)}
     */
    public static String getListAsString(String[] arr, String separator) {
        if (arr == null) {
            return "";
        }
        return getListAsString(List.of(arr), separator, p -> p);
    }

    /**
     * Return a string representation of a list using a given separator<br/>
     * ex: ["item1", "item2"] with sep ", " returns "item1, item2"
     */
    public static String getListAsString(List<String> list, String separator) {
        return getListAsString(list, separator, p -> p);
    }

    /**
     * Return a string representation of a stream using a given separator<br/>
     * ex: ["item1", "item2"] with sep ", " returns "item1, item2"
     */
    public static String getListAsString(Stream<String> stream, String separator) {
        return getListAsString(stream.collect(Collectors.toList()), separator, p -> p);
    }

    /**
     * Return a string representation of a list using a given separator and a mapping function<br/>
     * ex: ["item1", "item2"] with sep ", " and mapping function p -> p.toUpperCase() returns "ITEM1, ITEM2"
     */
    public static String getListAsString(List<String> list, String separator, Function<String, String> mappingFunction) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        if (mappingFunction == null) {
            mappingFunction = p -> p;
        }
        separator = separator == null ? "" : separator;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) == null || list.get(i).isEmpty()) continue;
            builder.append(mappingFunction.apply(list.get(i))).append(separator);
        }
        builder.append(mappingFunction.apply(list.get(list.size()-1)));
        return builder.toString();
    }

    /**
     * Replace placeholders in [] with the appropriate emoji
     * ex: [seed] gets replaced with the seed emoji
     */
    public static String replacePlaceholders(String text) {
        Pattern pattern = Pattern.compile("\\[([^]]+)]");
        Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1); // the text inside [ ]
            String replacement = EmojiEnum.getEmojiIdFromPlaceholder(key, matcher.group(0)); // leave as-is if missing
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
