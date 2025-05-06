package com.github.vvojtas.dailogi_server.service.util;

/**
 * Utility class for converting full URIs to relative URIs by stripping the protocol and host.
 */
public class UrlUtil {

    // Private constructor to prevent instantiation
    private UrlUtil() {
        throw new IllegalStateException("Utility class");
    }

    // Regex pattern to match protocol and host portion of a URI
    private static final String BASE_URI_PATTERN = "^[^:]+://[^/]+";

    /**
     * Converts a full URI with protocol and host to a relative URI by stripping the leading portion.
     *
     * @param fullUri The full URI to convert.
     * @return The relative URI path and query, or null if fullUri was null.
     */
    public static String toRelativeUri(String fullUri) {
        if (fullUri == null) {
            return null;
        }
        return fullUri.replaceFirst(BASE_URI_PATTERN, "");
    }
} 