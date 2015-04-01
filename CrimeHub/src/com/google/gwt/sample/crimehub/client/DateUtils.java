package com.google.gwt.sample.crimehub.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * WOGWT: copied from gwt-restlet to be used to parse and format
 * JSON dates in the format(s) required by ERRouteRequestHandler.
 * 
 * Date manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public final class DateUtils {

    /** Obsoleted HTTP date format (ANSI C asctime() format). */
    public static final List<String> FORMAT_ASC_TIME = unmodifiableList("EEE MMM dd HH:mm:ss yyyy");

    /** Obsoleted HTTP date format (RFC 1036). */
    public static final List<String> FORMAT_RFC_1036 = unmodifiableList("EEEE, dd-MMM-yy HH:mm:ss zzz");

    /** Preferred HTTP date format (RFC 1123). */
    public static final List<String> FORMAT_RFC_1123 = unmodifiableList("EEE, dd MMM yyyy HH:mm:ss zzz");

    /** W3C date format (RFC 3339). */
    public static final List<String> FORMAT_RFC_3339 = unmodifiableList("yyyy-MM-dd'T'HH:mm:ssz");

    /** Common date format (RFC 822). */
    public static final List<String> FORMAT_RFC_822 = unmodifiableList(
            "EEE, dd MMM yy HH:mm:ss z", "EEE, dd MMM yy HH:mm z",
            "dd MMM yy HH:mm:ss z", "dd MMM yy HH:mm z");


    /**
     * Compares two date with a precision of one second.
     * 
     * @param baseDate
     *            The base date
     * @param afterDate
     *            The date supposed to be after.
     * @return True if the afterDate is indeed after the baseDate.
     */
    public static boolean after(final Date baseDate, final Date afterDate) {
        if ((baseDate == null) || (afterDate == null)) {
            throw new IllegalArgumentException(
                    "Can't compare the dates, at least one of them is null");
        }

        final long baseTime = baseDate.getTime() / 1000;
        final long afterTime = afterDate.getTime() / 1000;
        return baseTime < afterTime;
    }

    /**
     * Compares two date with a precision of one second.
     * 
     * @param baseDate
     *            The base date
     * @param beforeDate
     *            The date supposed to be before.
     * @return True if the beforeDate is indeed before the baseDate.
     */
    public static boolean before(final Date baseDate, final Date beforeDate) {
        if ((baseDate == null) || (beforeDate == null)) {
            throw new IllegalArgumentException(
                    "Can't compare the dates, at least one of them is null");
        }

        final long baseTime = baseDate.getTime() / 1000;
        final long beforeTime = beforeDate.getTime() / 1000;
        return beforeTime < baseTime;
    }

    /**
     * Compares two date with a precision of one second.
     * 
     * @param baseDate
     *            The base date
     * @param otherDate
     *            The other date supposed to be equals.
     * @return True if both dates are equals.
     */
    public static boolean equals(final Date baseDate, final Date otherDate) {
        if ((baseDate == null) || (otherDate == null)) {
            throw new IllegalArgumentException(
                    "Can't compare the dates, at least one of them is null");
        }

        final long baseTime = baseDate.getTime() / 1000;
        final long otherTime = otherDate.getTime() / 1000;
        return otherTime == baseTime;
    }
    /**
     * Helper method to help initialize this class by providing unmodifiable
     * lists based on arrays.
     * 
     * @param <T>
     *            Any valid java object
     * @param array
     *            to be converted into an unmodifiable list
     * @return unmodifiable list based on the provided array
     */
    private static <T> List<T> unmodifiableList(final T... array) {
        return Collections.unmodifiableList(Arrays.asList(array));
    }

    public static native int timeZoneOffset() /*-{
        return new Date().getTimezoneOffset();
    }-*/;



}