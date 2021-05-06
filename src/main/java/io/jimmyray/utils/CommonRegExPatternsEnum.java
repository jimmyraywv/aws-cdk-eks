package io.jimmyray.utils;

/**
 * Common RegEx patterns used in the US.
 *
 * @author jimmyray
 * @version 1.0
 */

//NEXT 20 LINES SUPPRESS LineLength
public enum CommonRegExPatternsEnum {
    ALPHA("^[A-Za-z]+$"), //
    ALPHA_SPACES("^[A-Za-z\\s]+$"), //
    ALPHA_NUMERIC("^[A-Za-z0-9]+$"), //
    ALPHA_NUMERIC_SPACES("^[A-Za-z0-9\\s]+$"), //
    ZIP_CODE("^\\d{5}$|^\\d{5}-\\d{4}$"), //
    PHONE_NUMBER(
            "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$|^(\\d{3})[\\.](\\d{3})[\\.](\\d{4})$"), //
    NUMBER("^[0-9]+$"), //
    NUMERIC("^[-+]?\\d*\\.?\\d*$"), //
    CITY_NAME("^([a-zA-Z]+|[a-zA-Z]+\\s[a-zA-Z]+)$"), //
    SSN("^\\d{3}[- ]?\\d{2}[- ]?\\d{4}$"), //
    IP_ADDRESS(
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"), //
    BLANK_LINE("^$"), //
    DOLLARS("^\\$[0-9]*.[0-9][0-9]$"), //
    DATE(
            "^(([0][1-9])|([1][0-2]))\\/(([0][1-9])|([1][0-9])|([2][0-9])|([3][0-1]))\\/(([1]|[2])[0-9]{3})$"), //
    DATE_WEB(
            "^(([1]|[2])[0-9]{3})-(([0][1-9])|([1][0-2]))-(([0][1-9])|([1][0-9])|([2][0-9])|([3][0-1]))$"), //
    NAME("^[a-zA-Z]{1}\\'?[a-zA-Z]+[-]?[a-zA-Z]+$");

    private final String pattern;

    CommonRegExPatternsEnum(final String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return this.pattern;
    }
}
