package io.jimmyray.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class of static String utilities.
 *
 * @author jimmyray
 * @version 3.0
 */
public final class Strings {

    public static final String EMPTY_STRING = "";

    private Strings() {
        // disable utility class constructor
    }

    /**
     * Replace Null value string.
     *
     * @param in
     *          String
     * @param def
     *          String
     * @return String
     */
    public static String nullValue(final String in, final String def) {
        if (null == in) {
            return def;
        }
        return in;
    }

    /**
     * Replace Null value string.
     *
     * @param in
     *          Object
     * @param def
     *          String
     * @return String
     */
    public static String nullValue(final Object in, final String def) {
        if (null == in) {
            return def;
        }
        return in.toString();
    }

    /**
     * Replace empty value string.
     *
     * @param in
     * @param def
     * @return String
     */
    public static String emptyValue(final String in, final String def) {
        if (null == in) {
            throw new IllegalArgumentException("Argument was null.");
        }
        if (in.length() == 0) {
            return def;
        }
        return in;
    }

    /**
     * Null/Empty String value replacement
     *
     * @param in
     * @param def
     * @return
     */
    public static String nullEmptyValue(final String in, final String def) {
        if (null == in || in.length() == 0) return def;
        return in;
    }

    /**
     * Safe method to get a property String
     *
     * @param property
     * @param properties
     * @param defaultValue
     * @return
     */
    public static String getPropertyString(final String property,
                                           final Properties properties,
                                           final String defaultValue) {
        if (properties == null) return defaultValue;
        String value = properties.getProperty(property);
        return (value == null) ? defaultValue : value;
    }

    /**
     * Safe method to get a property int
     *
     * @param property
     * @param properties
     * @param defaultValue
     * @return
     */
    public static int getPropertyInt(final String property, final Properties properties, final int defaultValue) {
        if (properties == null) return defaultValue;
        String value = properties.getProperty(property);
        return (value == null) ? defaultValue : Integer.parseInt(value);
    }

    /**
     * Left pad string.
     *
     * @param in
     * @param pad
     * @param size
     * @param confine
     * @return
     */
    public static String lpad(final String in, final char pad, final int size,
                              final boolean confine) {
        if (null == in) {
            throw new IllegalArgumentException("Argument was null.");
        }

        StringBuilder builder = new StringBuilder(in);

        if (confine) {
            while (builder.length() < size) {
                builder.insert(0, pad);
            }
        } else {
            for (int x = 0; x < size; x++) {
                builder.insert(0, pad);
            }
        }

        return builder.toString();
    }

    /**
     * Overloaded lpad method to use default behavior of no size limits.
     *
     * @param in
     * @param pad
     * @param size
     * @return
     */
    public static String lpad(final String in, final char pad, final int size) {
        return Strings.lpad(in, pad, size, false);
    }

    /**
     *
     * @param in
     * @param pad
     * @param size
     * @param confine
     *          Limit size of string to size value (boolean)
     * @return
     */
    public static String rpad(final String in, final char pad, final int size,
                              final boolean confine) {
        if (null == in) {
            throw new IllegalArgumentException("Argument was null.");
        }

        StringBuilder builder = new StringBuilder(in);

        if (confine) {
            while (builder.length() < size) {
                builder.append(pad);
            }
        } else {
            for (int x = 0; x < size; x++) {
                builder.append(pad);
            }
        }

        return builder.toString();
    }

    /**
     * Overloaded rpad method to use default behavior of no size limits.
     *
     * @param in
     * @param pad
     * @param size
     * @return
     */
    public static String rpad(final String in, final char pad, final int size) {
        return Strings.rpad(in, pad, size, false);
    }

    /**
     * Frames a string with chars, evenly on each side if it can, odd size will
     * see more on the right then left, by default.
     *
     * @param in
     * @param pad
     * @param size
     * @return
     */
    public static String frame(final String in, final char pad, final int size,
                               final char side) {
        if (null == in) {
            throw new IllegalArgumentException("Argument was null.");
        }

        int target = size - in.length();
        if (target <= 0) {
            return in;
        }
        int start = target / 2;
        int end = start + target % 2;
        char[] prepads;
        char[] postpads;
        switch (side) {
            case 'r':
                prepads = new char[start];
                postpads = new char[end];
                break;
            default:
                prepads = new char[end];
                postpads = new char[start];
        }

        java.util.Arrays.fill(prepads, pad);
        java.util.Arrays.fill(postpads, pad);
        StringBuilder builder = new StringBuilder(size);
        builder.append(prepads);
        builder.append(in);
        builder.append(postpads);
        return builder.toString();
    }

    /**
     * Overloaded method to frame strings with chars, assumes that odd side will
     * be right (trailing vs. leading).
     *
     * @param in
     * @param pad
     * @param size
     * @return
     */
    public static String frame(final String in, final char pad, final int size) {
        return Strings.frame(in, pad, size, 'r');
    }

    /**
     * Joins an array of objects (counts on toString() method of each object).
     * This implementation ignores nulls.
     *
     * @param token
     *          String
     * @param objects
     *          Object[]
     * @return String
     */
    public static String join(final String token, final Object[] objects) {
        if (null == token) {
            throw new IllegalArgumentException("Token argument was null.");
        }
        if (null == objects) {
            throw new IllegalArgumentException("Object[] argument was null.");
        }

        StringBuilder builder = new StringBuilder();

        for (Object object : objects) {
            if (null != object) {
                if (builder.length() != 0) {
                    builder.append(token);
                }
                builder.append(object.toString());
            }
        }

        return (builder.toString());
    }

    /**
     * Converts a string with separators to a <code>List<String></code>.
     *
     * @param stringList
     * @param separator
     *          must be RegEx ready
     * @return
     */
    public static List<String> stringToList(final String stringList,
                                            final String separator) {
        return new ArrayList<String>(Arrays.asList(stringList.split(separator)));
    }

    /**
     * Reverses a string with a <code>StringBuilder</code> implementation.
     *
     * @param in
     * @return
     */
    public static String reverse(final String in) {
        if (null == in) {
            throw new IllegalArgumentException("Argument was null.");
        }

        StringBuilder builder = new StringBuilder(in);
        return builder.reverse().toString();
    }

    /**
     * Returns the first index where two strings differ.
     *
     * @param one
     * @param two
     * @return
     */
    public static long differenceIndex(final String one, final String two) {

        long ret = -1;

        if (null == one) {
            throw new IllegalArgumentException("Argument 1 was null.");
        }
        if (null == two) {
            throw new IllegalArgumentException("Argument 2 was null.");
        }
        if (one.length() == 0) {
            throw new IllegalArgumentException("Argument 1 was empty.");
        }
        if (two.length() == 0) {
            throw new IllegalArgumentException("Argument 2 was empty.");
        }

        char[] chars1 = one.toCharArray();
        char[] chars2 = two.toCharArray();

        int x = 0;
        for (; x < chars1.length; x++) {
            if (x < chars2.length) {
                if (chars1[x] != chars2[x]) {
                    return (long) x;
                }
            } else {
                return (long) x;
            }
        }

        if (chars1.length < chars2.length) {
            return (long) x;
        }

        return ret;
    }

    /**
     * Return true if <code>outer</code> String contains <code>inner</code>
     * (overloaded).
     *
     * @param outer
     * @param inner
     * @param ignoreCase
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean contains(final String outer, final String inner,
                                   final boolean ignoreCase) {
        if (null == outer) {
            throw new IllegalArgumentException("Outer argument was null.");
        }
        if (null == inner) {
            throw new IllegalArgumentException("Inner argument was null.");
        }

        String outerProperCase = outer;
        String innerProperCase = inner;
        if (ignoreCase) {
            outerProperCase = outer.toLowerCase();
            innerProperCase = inner.toLowerCase();
        }

        return (outerProperCase.indexOf(innerProperCase) >= 0);
    }

    /**
     * Return true if <code>outer</code> String contains <code>inner</code>. This
     * method is case sensitive. (overloaded)
     *
     * @param outer
     * @param inner
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean contains(final String outer, final String inner) {
        return Strings.contains(outer, inner, false);
    }

    /**
     * Return true if <code>outer</code> String starts with <code>inner</code>.
     *
     * @param outer
     * @param inner
     * @param ignoreCase
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean startsWith(final String outer, final String inner,
                                     final boolean ignoreCase) {

        if (null == outer) {
            throw new IllegalArgumentException("Outer argument was null.");
        }
        if (null == inner) {
            throw new IllegalArgumentException("Inner argument was null.");
        }

        String outerProperCase = outer;
        String innerProperCase = inner;
        if (ignoreCase) {
            outerProperCase = outer.toLowerCase();
            innerProperCase = inner.toLowerCase();
        }

        return (outerProperCase.indexOf(innerProperCase) == 0);
    }

    /**
     * Overloaded: Return true if <code>outer</code> String starts with
     * <code>inner</code>. Assumes case sensitive.
     *
     * @param outer
     * @param inner
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean startsWith(final String outer, final String inner) {
        return startsWith(outer, inner, false);
    }

    /**
     * Overloaded: Returns true is <code>outer</code> String ends with
     * <code>inner</code>.
     *
     * @param outer
     * @param inner
     * @param ignoreCase
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean endsWith(final String outer, final String inner,
                                   final boolean ignoreCase) {

        if (null == outer) {
            throw new IllegalArgumentException("Outer argument was null.");
        }
        if (null == inner) {
            throw new IllegalArgumentException("Inner argument was null.");
        }

        String outerReversed = new StringBuilder(outer).reverse().toString();
        String innerReversed = new StringBuilder(inner).reverse().toString();

        if (ignoreCase) {
            outerReversed = outerReversed.toLowerCase();
            innerReversed = inner.toLowerCase();
        }

        return (outerReversed.indexOf(innerReversed) == 0);
    }

    /**
     * Overloaded: Returns true is <code>outer</code> String ends with
     * <code>inner</code>. Assumes case sensitive.
     *
     * @param outer
     * @param inner
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean endsWith(final String outer, final String inner) {
        return endsWith(outer, inner, false);
    }

    /**
     * Returns true if input string is Alpha-Numeric (overloaded).
     *
     * @param in
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean isAlphaNumeric(final String in) {
        return Strings.isAlphaNumeric(in, false);
    }

    /**
     * Returns true if input string is Alpha-Numeric (overloaded). This version
     * uses a boolean argument to allow white space.
     *
     * @param in
     * @param whiteSpace
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean isAlphaNumeric(final String in, final boolean whiteSpace) {
        if (null == in) {
            throw new IllegalArgumentException("Argument was null.");
        }

        if (whiteSpace) {
            return in.matches(CommonRegExPatternsEnum.ALPHA_NUMERIC_SPACES
                    .getPattern());
        }
        return in.matches(CommonRegExPatternsEnum.ALPHA_NUMERIC.getPattern());
    }

    /**
     * Returns true if input string is Alpha characters (overloaded).
     *
     * @param in
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean isAlpha(final String in) {
        return Strings.isAlpha(in, false);
    }

    /**
     * Returns true if input string is Alpha characters (overlaoded). This version
     * uses a boolean argument to allow white space.
     *
     * @param in
     * @param whiteSpace
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean isAlpha(final String in, final boolean whiteSpace) {
        if (null == in) {
            throw new IllegalArgumentException("Argument was null.");
        }

        if (whiteSpace) {
            return in.matches(CommonRegExPatternsEnum.ALPHA_SPACES.getPattern());
        }
        return in.matches(CommonRegExPatternsEnum.ALPHA.getPattern());
    }

    /**
     * Returns true if input string is a simple number.
     *
     * @param in
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean isNumber(final String in) {
        if (null == in) {
            throw new IllegalArgumentException("Argument was null.");
        }

        return in.matches(CommonRegExPatternsEnum.NUMBER.getPattern());
    }

    /**
     * Returns true if input string is Numeric.
     *
     * @param in
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean isNumeric(final String in) {
        if (null == in) {
            throw new IllegalArgumentException("Argument was null.");
        }

        return in.matches(CommonRegExPatternsEnum.NUMERIC.getPattern());
    }

    /**
     * Convenience method to use RegEx matches against Strings. This method is
     * deprecated and <code>String.matches()</code> should be used.
     *
     * @param in
     * @param pattern
     * @return
     * @throws IllegalArgumentException
     */
    @Deprecated
    public static boolean matchRegEx(final String in, final String pattern) {
        if (null == in) {
            throw new IllegalArgumentException("In argument was null.");
        }

        if (null == pattern) {
            throw new IllegalArgumentException("Pattern argument was null.");
        }

        Pattern compliedPattern = Pattern.compile(pattern);
        Matcher matcher = compliedPattern.matcher(in);

        return (matcher.matches());
    }

    /**
     * Returns the stacktrace as a <code>String</code>.
     *
     * @param throwable
     * @return
     * @throws IllegalArgumentException
     */
    public static String getStackTraceAsString(final Throwable throwable) {

        if (null == throwable) {
            throw new IllegalArgumentException("Throwable was null.");
        }

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        return stringWriter.getBuffer().toString();
    }
}

