package com.linkedpipes.plugin.transformer.mustachechunked;

import java.util.HashMap;
import java.util.Map;

/**
 * Unpack PREFIXES in Mustache template.
 */
class MustacheTemplatePrefixExpander {

    private MustacheTemplatePrefixExpander() {
    }

    private enum StatusComment {
        NONE,
        OPEN,
        OPEN_OPEN,
        COMMENT,
        COMMENT_CLOSE
    }

    private enum StatusPrefix {
        NONE,
        P,
        PR,
        PRE,
        PREF,
        PREFI,
        PREFIX,
        PREFIX_READ,
        PREFIX_SEMI,
        LINK_READ
    }

    private enum StatusReplace {
        NONE,
        OPEN,
        OPEN_OPEN,
        READ,
    }

    /**
     * Parse given query into the comment and executable part.
     *
     * @param queryString
     * @param commentBuffer
     * @param queryBuffer
     */
    private static void parseCommentQuery(String queryString,
            StringBuffer commentBuffer, StringBuffer queryBuffer) {
        StatusComment status = StatusComment.NONE;
        for (int index = 0; index < queryString.length(); index++) {
            final char character = queryString.charAt(index);
            // Check for special characters, we need to extract comments.
            switch (status) {
                case NONE:
                    if (character == '{') {
                        status = StatusComment.OPEN;
                    } else {
                        status = StatusComment.NONE;
                        queryBuffer.append(character);
                    }
                    break;
                case OPEN:
                    if (character == '{') {
                        status = StatusComment.OPEN_OPEN;
                    } else {
                        status = StatusComment.NONE;
                        queryBuffer.append("{");
                        queryBuffer.append(character);
                    }
                    break;
                case OPEN_OPEN:
                    if (character == '!') {
                        status = StatusComment.COMMENT;
                    } else {
                        status = StatusComment.NONE;
                        queryBuffer.append("{{");
                        queryBuffer.append(character);
                    }
                    break;
                case COMMENT:
                    if (character == '}') {
                        status = StatusComment.COMMENT_CLOSE;
                    } else {
                        status = StatusComment.COMMENT;
                        commentBuffer.append(character);
                    }
                    break;
                case COMMENT_CLOSE:
                    if (character == '}') {
                        status = StatusComment.NONE;
                        // Add space to separate comments.
                        commentBuffer.append(" ");
                    } else {
                        status = StatusComment.COMMENT;
                        // Add for the occurance in the COMMENT state.
                        commentBuffer.append("}");
                        commentBuffer.append(character);
                    }
                    break;
            }
        }
    }

    /**
     * Find definition of prefixes in given string.
     *
     * @param string
     * @return
     */
    private static Map<String, String> readPrefixes(String string) {
        final Map<String, String> prefixes = new HashMap<>();
        StatusPrefix status = StatusPrefix.NONE;
        final StringBuffer prefixBuffer = new StringBuffer(12);
        final StringBuffer iriBuffer = new StringBuffer(32);
        for (int index = 0; index < string.length(); index++) {
            final char character = string.charAt(index);
            switch (status) {
                case NONE:
                    if (character == 'p' || character == 'P') {
                        status = StatusPrefix.P;
                    }
                    break;
                case P:
                    if (character == 'r' || character == 'R') {
                        status = StatusPrefix.PR;
                    } else {
                        status = StatusPrefix.NONE;
                    }
                    break;
                case PR:
                    if (character == 'e' || character == 'E') {
                        status = StatusPrefix.PRE;
                    } else {
                        status = StatusPrefix.NONE;
                    }
                    break;
                case PRE:
                    if (character == 'f' || character == 'F') {
                        status = StatusPrefix.PREF;
                    } else {
                        status = StatusPrefix.NONE;
                    }
                    break;
                case PREF:
                    if (character == 'i' || character == 'I') {
                        status = StatusPrefix.PREFI;
                    } else {
                        status = StatusPrefix.NONE;
                    }
                    break;
                case PREFI:
                    if (character == 'x' || character == 'X') {
                        status = StatusPrefix.PREFIX;
                    } else {
                        status = StatusPrefix.NONE;
                    }
                    break;
                case PREFIX:
                    if (Character.isWhitespace(character)) {
                        status = StatusPrefix.PREFIX_READ;
                        prefixBuffer.setLength(0);
                    } else {
                        status = StatusPrefix.NONE;
                    }
                    break;
                case PREFIX_READ:
                    if (character == ':') {
                        status = StatusPrefix.PREFIX_SEMI;
                    } else {
                        prefixBuffer.append(character);
                    }
                    break;
                case PREFIX_SEMI:
                    if (character == '<') {
                        status = StatusPrefix.LINK_READ;
                    } else if (Character.isWhitespace(character)) {
                        // Do nothing here.
                    } else {
                        status = StatusPrefix.NONE;
                    }
                    break;
                case LINK_READ:
                    if (character == '>') {
                        status = StatusPrefix.NONE;
                        // Add prefix, require white space.
                        prefixes.put(prefixBuffer.toString().trim(),
                                iriBuffer.toString());
                        //
                        prefixBuffer.setLength(0);
                        iriBuffer.setLength(0);
                    } else {
                        iriBuffer.append(character);
                    }
                    break;
            }
        }
        return prefixes;
    }

    /**
     * Expand prefixes in templates.
     *
     * @param prefixes
     * @param string
     * @return
     */
    private static String expand(Map<String, String> prefixes, String string) {
        StatusReplace status = StatusReplace.NONE;
        final StringBuffer resultBuffer = new StringBuffer(512);
        final StringBuffer prefixBuffer = new StringBuffer(64);
        for (int index = 0; index < string.length(); index++) {
            final char character = string.charAt(index);
            switch (status) {
                case NONE:
                    resultBuffer.append(character);
                    if (character == '{') {
                        status = StatusReplace.OPEN;
                    }
                    break;
                case OPEN:
                    resultBuffer.append(character);
                    if (character == '{') {
                        status = StatusReplace.OPEN_OPEN;
                    } else {
                        status = StatusReplace.NONE;
                    }
                    break;
                case OPEN_OPEN:
                    // Start reading prefix.
                    prefixBuffer.setLength(0);
                    status = StatusReplace.READ;
                    // If first character is special skip it.
                    if (!Character.isAlphabetic(character)
                            && character != ':') {
                        resultBuffer.append(character);
                        break;
                    }
                case READ:
                    switch (character) {
                        case '}':
                            // No prefix here.
                            prefixBuffer.append(character);
                            resultBuffer.append(prefixBuffer);
                            status = StatusReplace.NONE;
                            break;
                        case ':':
                            final String prefix
                                    = prefixBuffer.toString().trim();
                            final String replacement = prefixes.get(prefix);
                            if (replacement == null) {
                                // It was not a known prefix.
                                resultBuffer.append(prefixBuffer);
                                resultBuffer.append(':');
                            } else {
                                resultBuffer.append(replacement);
                            }
                            status = StatusReplace.NONE;
                            break;
                        default:
                            prefixBuffer.append(character);
                            break;
                    }
                    break;
            }
        }
        return resultBuffer.toString();
    }

    public static String expand(String queryString) {
        // Parse query.
        final StringBuffer commentBuffer = new StringBuffer(64);
        final StringBuffer queryBuffer = new StringBuffer(queryString.length());
        parseCommentQuery(queryString, commentBuffer, queryBuffer);
        // Read prefixes from comments.
        final Map<String, String> prefixes = readPrefixes(
                commentBuffer.toString());
        // Replace in the template.
        return expand(prefixes, queryBuffer.toString());
    }

}
