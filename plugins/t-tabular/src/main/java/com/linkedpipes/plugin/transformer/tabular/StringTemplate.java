package com.linkedpipes.plugin.transformer.tabular;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

/**
 * Implement first two levels of https://tools.ietf.org/html/rfc6570#section-2
 *
 * Token {{TABLE_URI}} can be used to reference table resource uri,
 * passed to the initialize function as a fist parameter.
 */
class StringTemplate {

    public static String TABLE_RESOURCE_REF = "__A83N48X1_TABLE_URI__";

    private static interface Token {

        public abstract String process(List<String> row);

    }

    /**
     * Return fixed string.
     */
    private static class TokenString implements Token {

        private final String string;

        private TokenString(String string) {
            this.string = string;
        }

        @Override
        public String process(List<String> row) {
            return string;
        }

    }

    /**
     * Level 1 template.
     * {VALUE} - string is encoded to not include URI characters.
     */
    private static class TokenSimpleExpanstion implements Token {

        /**
         * Index from row to pick up.
         */
        private final int index;

        private TokenSimpleExpanstion(int index) {
            this.index = index;
        }

        @Override
        public String process(List<String> row) {
            final String value = row.get(index);
            if (value == null) {
                return null;
            } else {
                return encodeString(value);
            }
        }
    }

    /**
     * Level 2 template.
     * {+VALUE} - values can include reserved URI characters.
     */
    private static class TokenReservedExpanstion implements Token {

        /**
         * Index from row to pick up.
         */
        private final int index;

        private TokenReservedExpanstion(int index) {
            this.index = index;
        }

        @Override
        public String process(List<String> row) {
            final String value = row.get(index);
            if (value == null) {
                return null;
            } else {
                return value;
            }
        }
    }

    /**
     * Level 2 template.
     * {#VALUE} - as a simple but # is kept in place.
     */
    private static class TokenFragmentExpanstion implements Token {

        /**
         * Index from row to pick up.
         */
        private final int index;

        private TokenFragmentExpanstion(int index) {
            this.index = index;
        }

        @Override
        public String process(List<String> row) {
            final String value = row.get(index);
            if (value == null) {
                return null;
            } else {
                return "#" + encodeString(value);
            }
        }
    }

    private final String template;

    /**
     * Contains information how to construct
     */
    private final List<Token> tokens = new LinkedList<>();

    StringTemplate(String template) {
        this.template = template;
    }

    /**
     * @param tableUri URI of currently parsed table.
     * @param header Names of columns headers.
     */
    public void initialize(String tableUri, List<String> header)
            throws InvalidTemplate {
        tokens.clear();
        // Parse inner template;
        String toParse = template;
        while (!toParse.isEmpty()) {
            int left = indexOfUnescape(toParse, '{');
            int right = indexOfUnescape(toParse, '}');
            if (left == -1 && right == -1) {
                tokens.add(new TokenString(toParse));
                break;
            }
            // There is { or } in the string.
            if (right == -1 || (left != -1 && left < right)) {
                // { -> string
                final String value = toParse.substring(0, left);
                toParse = toParse.substring(left + 1);
                //
                if (!value.isEmpty()) {
                    tokens.add(new TokenString(value));
                } else {
                    // It can be empty, if for example, string starts
                    // with { or there is }{ as substring
                }
            } else if (left == -1 || (right != -1 && right < left)) {
                // } --> name
                String name = toParse.substring(0, right);
                // Revert escaping of { } in the pattern.
                name = name.replaceAll("\\\\\\{", "\\{")
                        .replaceAll("\\\\}", "\\}");
                toParse = toParse.substring(right + 1);
                // Now name contains the pattern so we can create token.
                if (name.equals(TABLE_RESOURCE_REF)) {
                    // Special token with table resource URI.
                    tokens.add(new TokenString(tableUri));
                } else {
                    tokens.add(createToken(name, header));
                }
            } else {
                throw new InvalidTemplate(
                        "Invalid template '" + template + "'");
            }
        }
    }

    /**
     * @param row
     * @return Can be null.
     */
    public String process(List<String> row) {
        final StringBuilder result = new StringBuilder(20);
        for (Token token : tokens) {
            String newString = token.process(row);
            if (newString == null) {
                // If anyone return null, then we do not publish - ie. we
                // assume all to be mandatory.
                // TODO Implement optional
                return null;
            } else {
                result.append(newString);
            }
        }
        return result.toString();
    }

    /**
     * Return index of first un-escaped occurrence of given character in given
     * string.
     *
     * @param str
     * @param toFind
     * @return
     */
    private static int indexOfUnescape(String str, char toFind) {
        for (int i = 0; i < str.length(); ++i) {
            final char current = str.charAt(i);
            if (current == toFind) {
                // we find the one
                return i;
            } else if (current == '\\') {
                // skip next
                i++;
            }
            // continue the search
        }
        // not founded
        return -1;
    }

    private static String encodeString(String part) {
        try {
            return URLEncoder.encode(part, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported encoding", ex);
        }
    }

    /**
     * Create token that based on given template.
     *
     * @param template
     * @param header
     * @return
     */
    private static Token createToken(String template, List<String> header)
            throws InvalidTemplate {
        if (template.startsWith("+")) {
            return new TokenReservedExpanstion(getIndexForTemplate(
                    template.substring(1), header));
        } else if (template.startsWith("#")) {
            return new TokenFragmentExpanstion(getIndexForTemplate(
                    template.substring(1), header));
        } else {
            return new TokenSimpleExpanstion(getIndexForTemplate(
                    template, header));
        }
    }

    /**
     * Index of given name in the header.
     *
     * @param template
     * @param header
     * @return
     */
    private static int getIndexForTemplate(String template,
            List<String> header) throws InvalidTemplate {
        int value = header.indexOf(template);
        if (value == -1) {
            throw new InvalidTemplate("Missing template in header '"
                    + template + "'");
        } else {
            return value;
        }
    }

}
