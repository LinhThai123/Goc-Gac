package com.ecommerce.gocgac.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Tiện ích sinh slug thân thiện URL từ chuỗi tiếng Việt (loại bỏ dấu).
 */
public final class SlugUtils {

    private static final Pattern NON_LATIN = Pattern.compile("[^a-z0-9\\s-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s-]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("(^-+|-+$)");

    private SlugUtils() {}

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String s = input.trim().toLowerCase(Locale.forLanguageTag("vi"));
        s = s.replace("đ", "d");
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        s = NON_LATIN.matcher(s).replaceAll("");
        s = WHITESPACE.matcher(s).replaceAll("-");
        s = EDGE_DASHES.matcher(s).replaceAll("");
        return s;
    }
}
