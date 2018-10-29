/**
 * Content of this file was changed for need of LinkedPipes ETL.
 *
 * Copyright 2012-2013 the Semargl contributors. See AUTHORS for more details.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.semarglproject.jsonld;

import org.semarglproject.rdf.ParseException;
import org.semarglproject.rdf.ProcessorGraphHandler;
import org.semarglproject.sink.CharSink;
import org.semarglproject.sink.Pipe;
import org.semarglproject.sink.QuadSink;
import org.semarglproject.sink.TripleSink;
import org.semarglproject.source.StreamProcessor;

import java.util.BitSet;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Implementation of streaming <a href="http://www.w3.org/TR/2013/WD-json-ld-20130411/">JSON-LD</a> parser.
 * Parser requires @id properties to be declared before predicates for each non-blank JSON-LD node.
 * <br>
 *     List of supported options:
 *     <ul>
 *         <li>{@link StreamProcessor#PROCESSOR_GRAPH_HANDLER_PROPERTY}</li>
 *         <li>{@link StreamProcessor#ENABLE_ERROR_RECOVERY}</li>
 *     </ul>
 */
public final class JsonLdParser extends Pipe<TripleSink> implements CharSink {

    /**
     * Class URI for errors produced by a parser
     */
    public static final String ERROR = "http://semarglproject.org/json-ld/Error";

    /**
     * Class URI for warnings produced by a parser
     */
    public static final String WARNING = "http://semarglproject.org/json-ld/Warning";

    private static final short PARSING_ARRAY_BEFORE_VALUE = 1;
    private static final short PARSING_OBJECT_BEFORE_KEY = 2;

    private static final short PARSING_OBJECT_BEFORE_VALUE = 3;

    private static final short PARSING_STRING = 4;

    private static final short PARSING_NUMBER = 5;

    private static final short PARSING_NAMED_LITERAL = 6;

    private static final short PARSING_OBJECT_BEFORE_COLON = 7;

    private static final short PARSING_OBJECT_BEFORE_COMMA = 8;

    private static final short PARSING_ARRAY_BEFORE_COMMA = 9;

    private static final BitSet WHITESPACE = new BitSet();

    private static final BitSet NAMED_LITERAL_CHAR = new BitSet();

    static {
        WHITESPACE.set('\t');
        WHITESPACE.set(' ');
        WHITESPACE.set('\r');
        WHITESPACE.set('\n');

        NAMED_LITERAL_CHAR.set('t');
        NAMED_LITERAL_CHAR.set('r');
        NAMED_LITERAL_CHAR.set('u');
        NAMED_LITERAL_CHAR.set('e');
        NAMED_LITERAL_CHAR.set('f');
        NAMED_LITERAL_CHAR.set('a');
        NAMED_LITERAL_CHAR.set('l');
        NAMED_LITERAL_CHAR.set('s');
        NAMED_LITERAL_CHAR.set('n');
        NAMED_LITERAL_CHAR.set('0');
        NAMED_LITERAL_CHAR.set('1');
        NAMED_LITERAL_CHAR.set('2');
        NAMED_LITERAL_CHAR.set('3');
        NAMED_LITERAL_CHAR.set('4');
        NAMED_LITERAL_CHAR.set('5');
        NAMED_LITERAL_CHAR.set('6');
        NAMED_LITERAL_CHAR.set('7');
        NAMED_LITERAL_CHAR.set('8');
        NAMED_LITERAL_CHAR.set('9');
        NAMED_LITERAL_CHAR.set('.');
        NAMED_LITERAL_CHAR.set('-');
        NAMED_LITERAL_CHAR.set('E');
        NAMED_LITERAL_CHAR.set('+');
    }

    private JsonLdContentHandler contentHandler;

    private ProcessorGraphHandler processorGraphHandler = null;
    private boolean ignoreErrors = false;

    private Deque<Short> stateStack = new LinkedList<Short>();
    private short parsingState;

    private int tokenStartPos;
    private short charsToEscape = 0;
    private StringBuilder addBuffer = null;

    private JsonLdParser(QuadSink sink) {
        super(sink);
        contentHandler = new JsonLdContentHandler(sink);
    }

    /**
     * Creates instance of JsonLdParser connected to specified sink.
     * @param sink sink to be connected to
     * @return instance of JsonLdParser
     */
    public static CharSink connect(QuadSink sink) {
        return new JsonLdParser(sink);
    }

    public void warning(String warningClass, String msg) {
        if (processorGraphHandler != null) {
            processorGraphHandler.warning(warningClass, msg);
        }
    }

    private void error(String msg) throws ParseException {
        if (processorGraphHandler != null) {
            processorGraphHandler.error(ERROR, msg);
        }
        if (!ignoreErrors) {
            throw new ParseException(msg);
        }
    }

    @Override
    public JsonLdParser process(String str) throws ParseException {
        return process(str.toCharArray(), 0, str.length());
    }

    @Override
    public JsonLdParser process(char ch) throws ParseException {
        char[] buffer = new char[1];
        buffer[0] = ch;
        return process(buffer, 0, 1);
    }

    @Override
    public JsonLdParser process(char[] buffer, int start, int count) throws ParseException {
        if (tokenStartPos != -1) {
            tokenStartPos = start;
        }
        int end = start + count;

        for (int pos = start; pos < end; pos++) {
            if (parsingState == PARSING_ARRAY_BEFORE_VALUE || parsingState == PARSING_OBJECT_BEFORE_VALUE
                    || parsingState == PARSING_OBJECT_BEFORE_KEY) {
                processValueChar(buffer, pos);
            } else if (parsingState == PARSING_STRING) {
                processStringChar(buffer, pos);
            } else if (parsingState == PARSING_OBJECT_BEFORE_COMMA) {
                if (buffer[pos] == ',') {
                    parsingState = PARSING_OBJECT_BEFORE_KEY;
                } else if (buffer[pos] == '}') {
                    parsingState = stateStack.pop();
                    contentHandler.onObjectEnd();
                    onValue();
                } else if (!WHITESPACE.get(buffer[pos])) {
                    error("Unexpected character '" + buffer[pos] + "'");
                }
            } else if (parsingState == PARSING_ARRAY_BEFORE_COMMA) {
                if (buffer[pos] == ',') {
                    parsingState = PARSING_ARRAY_BEFORE_VALUE;
                } else if (buffer[pos] == ']') {
                    parsingState = stateStack.pop();
                    contentHandler.onArrayEnd();
                    onValue();
                } else if (!WHITESPACE.get(buffer[pos])) {
                    error("Unexpected character '" + buffer[pos] + "'");
                }
            } else if (parsingState == PARSING_OBJECT_BEFORE_COLON) {
                if (buffer[pos] == ':') {
                    parsingState = PARSING_OBJECT_BEFORE_VALUE;
                } else if (!WHITESPACE.get(buffer[pos])) {
                    error("Unexpected character '" + buffer[pos] + "'");
                }
            } else if (parsingState == PARSING_NAMED_LITERAL || parsingState == PARSING_NUMBER) {
                if (!NAMED_LITERAL_CHAR.get(buffer[pos])) {
                    String value = unescape(extractToken(buffer, pos - 1, 0));
                    if (parsingState == PARSING_NAMED_LITERAL) {
                        if ("true".equals(value)) {
                            contentHandler.onBoolean(true);
                        } else if ("false".equals(value)) {
                            contentHandler.onBoolean(false);
                        } else if ("null".equals(value)) {
                            contentHandler.onNull();
                        } else {
                            error("Unexpected value '" + value + "'");
                        }
                    } else {
                        if (value.contains(".") || value.contains("E") || value.contains("e")) {
                            contentHandler.onNumber(Double.valueOf(value));
                        } else {
                            contentHandler.onNumber(Integer.valueOf(value));
                        }
                    }
                    parsingState = stateStack.pop();
                    if (parsingState == PARSING_ARRAY_BEFORE_VALUE) {
                        parsingState = PARSING_ARRAY_BEFORE_COMMA;
                    } else if (parsingState == PARSING_OBJECT_BEFORE_VALUE) {
                        parsingState = PARSING_OBJECT_BEFORE_COMMA;
                    }
                    pos--;
                }
            }
        }
        if (tokenStartPos != -1) {
            if (addBuffer == null) {
                addBuffer = new StringBuilder();
            }
            addBuffer.append(buffer, tokenStartPos, end - tokenStartPos);
        }
        return this;
    }

    private void processStringChar(char[] buffer, int pos) throws ParseException {
        if (charsToEscape > 0) {
            charsToEscape--;
        } else {
            if (buffer[pos] == '\"') {
                parsingState = stateStack.pop();
                String value = unescape(extractToken(buffer, pos, 1));
                if (parsingState == PARSING_OBJECT_BEFORE_KEY) {
                    contentHandler.onKey(value);
                    parsingState = PARSING_OBJECT_BEFORE_COLON;
                } else if (parsingState == PARSING_ARRAY_BEFORE_VALUE) {
                    contentHandler.onString(value);
                    parsingState = PARSING_ARRAY_BEFORE_COMMA;
                } else if (parsingState == PARSING_OBJECT_BEFORE_VALUE) {
                    contentHandler.onString(value);
                    parsingState = PARSING_OBJECT_BEFORE_COMMA;
                }
            } else if (buffer[pos] == '\\') {
                charsToEscape = 1;
            }
        }
    }

    private void processValueChar(char[] buffer, int pos) throws ParseException {
        switch (buffer[pos]) {
            case '{':
                stateStack.push(parsingState);
                parsingState = PARSING_OBJECT_BEFORE_KEY;
                contentHandler.onObjectStart();
                break;
            case '}':
                if (parsingState == PARSING_OBJECT_BEFORE_VALUE) {
                    error("Unexpected object end");
                }
                parsingState = stateStack.pop();
                contentHandler.onObjectEnd();
                onValue();
                break;
            case '[':
                stateStack.push(parsingState);
                parsingState = PARSING_ARRAY_BEFORE_VALUE;
                contentHandler.onArrayStart();
                break;
            case ']':
                parsingState = stateStack.pop();
                contentHandler.onArrayEnd();
                onValue();
                break;
            case 't':
            case 'f':
            case 'n':
                stateStack.push(parsingState);
                parsingState = PARSING_NAMED_LITERAL;
                tokenStartPos = pos;
                break;
            case '\"':
                stateStack.push(parsingState);
                parsingState = PARSING_STRING;
                tokenStartPos = pos;
                break;
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                stateStack.push(parsingState);
                parsingState = PARSING_NUMBER;
                tokenStartPos = pos;
                break;
            default:
                if (!WHITESPACE.get(buffer[pos])) {
                    error("Unexpected character '" + buffer[pos] + "'");
                }
        }
    }

    private void onValue() {
        if (parsingState == PARSING_ARRAY_BEFORE_VALUE) {
            parsingState = PARSING_ARRAY_BEFORE_COMMA;
        } else if (parsingState == PARSING_OBJECT_BEFORE_VALUE) {
            parsingState = PARSING_OBJECT_BEFORE_COMMA;
        }
    }

    @Override
    public void setBaseUri(String baseUri) {
        contentHandler.setBaseUri(baseUri);
    }

    @Override
    protected boolean setPropertyInternal(String key, Object value) {
        if (StreamProcessor.PROCESSOR_GRAPH_HANDLER_PROPERTY.equals(key) && value instanceof ProcessorGraphHandler) {
            processorGraphHandler = (ProcessorGraphHandler) value;
        } else if (StreamProcessor.ENABLE_ERROR_RECOVERY.equals(key) && value instanceof Boolean) {
            ignoreErrors = (Boolean) value;
        }
        return false;
    }

    private String extractToken(char[] buffer, int tokenEndPos, int trimSize) throws ParseException {
        String saved;
        if (addBuffer != null) {
            if (tokenEndPos - trimSize >= tokenStartPos) {
                addBuffer.append(buffer, tokenStartPos, tokenEndPos - tokenStartPos - trimSize + 1);
            }
            addBuffer.delete(0, trimSize);
            saved = addBuffer.toString();
            addBuffer = null;
        } else {
            saved = String.valueOf(buffer, tokenStartPos + trimSize, tokenEndPos - tokenStartPos + 1 - 2 * trimSize);
        }
        tokenStartPos = -1;
        return saved;
    }

    @Override
    public void startStream() throws ParseException {
        super.startStream();
        parsingState = PARSING_ARRAY_BEFORE_VALUE;
        contentHandler.onDocumentStart();
    }

    @Override
    public void endStream() throws ParseException {
        super.endStream();
        contentHandler.onDocumentEnd();
        if (tokenStartPos != -1 || !stateStack.isEmpty()) {
            error("Unexpected end of stream");
        }
    }

    private String unescape(String str) throws ParseException {
        int limit = str.length();
        StringBuilder result = new StringBuilder(limit);

        for (int i = 0; i < limit; i++) {
            char ch = str.charAt(i);
            if (ch != '\\') {
                result.append(ch);
                continue;
            }
            i++;
            if (i == limit) {
                break;
            }
            ch = str.charAt(i);
            switch (ch) {
                case '\\':
                case '/':
                case '\"':
                    result.append(ch);
                    break;
                case 'b':
                    result.append('\b');
                    break;
                case 'f':
                    result.append('\f');
                    break;
                case 'n':
                    result.append('\n');
                    break;
                case 'r':
                    result.append('\r');
                    break;
                case 't':
                    result.append('\t');
                    break;
                case 'u':
                    int sequenceLength = 4;
                    if (i + sequenceLength >= limit) {
                        error("Error parsing escape sequence '\\" + ch + "'");
                    }
                    String code = str.substring(i + 1, i + 1 + sequenceLength);
                    i += sequenceLength;

                    try {
                        int value = Integer.parseInt(code, 16);
                        result.append((char) value);
                    } catch (NumberFormatException nfe) {
                        error("Error parsing escape sequence '\\" + ch + "'");
                    }
                    break;
                default:
                    result.append(ch);
                    break;
            }
        }
        return result.toString();
    }

}