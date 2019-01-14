package com.linkedpipes.plugin.transformer.mustachechunked;

class EscapeForJson {

    private StringBuilder builder = new StringBuilder();

    public String escape(String string) {
        builder.setLength(0);
        for (int index = 0; index < string.length(); ++index) {
            char character = string.charAt(index);
            switch (character) {
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                default:
                    builder.append(character);
                    break;
            }
        }
        return builder.toString();
    }

}
