package edu.yu.dbimpl.parse;

/** Defines the core set of token categories that must be supported by a
 * PetiteDB lexer.
 *
 * The "lexer" lecture defines the set of PetiteDB Token types.  The TokenType
 * enum below corresponds to what's defined in lecture and adds an EOF value.

 * Students MAY NOT modify this class in any way!.
 *
 * Design note: the lexer API is deliberately very unconstrained to allow for
 * maximum implementation freedom subject to the constraint of testability.
 * Implementors should consider adding "consume specific token type" APIs on
 * behalf of the parser.
 *
 * Semantics note: the parser and lexer should fail-early-and-fatally after
 * throwing a BadSyntaxException.  Do not try to recover from the error, let
 * the client fix it and try again.  The behavior of subsequent API invocations
 * on the input by this parser or lexer instance is undefined.
 *
 * @author Avraham Leff
 */

        import java.util.*;

public abstract class LexerBase {

    /** Define an enum that specifies the set of valid token types
     */
    public enum TokenType {
        DELIMITER, EOF, ID, NUMERIC_CONSTANT, KEYWORD, STRING_CONSTANT
    };

    /** An immutable holder class: because immutable, clients get direct analysis
     * to encapsulated state.
     *
     * IMPORTANT: even if the token type doesn't correspond to a String (e.g., an
     * NUMERIC_CONSTANT), Token.value contains the String version of that number.

     *  May review immutability design if profiling shows performance problems as
     *  large number of Tokens are created during parsing.
     */
    public static class Token {
        /** Constructor
         *
         * @param TokenType type
         * @param String value
         */
        public Token(final TokenType type, final String value) {
            assert type != null: "type can't be null";
            assert value != null : "value can't be null";
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return "{type="+type.toString()+", value="+value.toString()+"}";
        }

        @Override
        public int hashCode() { return Objects.hash(type, value); }

        @Override
        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof Token)) {
                return false;
            }

            final Token that = (Token) o;
            return this.type == that.type && this.value.equals(that.value);
        }

        public final TokenType type;
        public final String value;
    } // static inner class

    /** Constructor: creates a new lexical analyzer to be used for parsing a
     * PetiteDB SQL statement.  Upon completion of the constructor invocation,
     * the lexer must have consumed one token, and is able to return that token
     * via firstToken().
     *
     * See lecture discussion for the Lexer requirements.
     *
     * @param s the statement to be lex'd
     * @throws BadSyntaxException if a problem occurs when reading the input for
     * the first token.
     * @see firstToken
     */
    public LexerBase(String s) {
        // fill me in in your implementation class!
    }

    /** Returns the first token that the constructor extracted from the input
     * (even if the client makes subsequent calls to next(), this method will
     * return the first token extracted).  The method advances the internal state
     * of the lexer such that a subsequent invocation of next() will return the
     * second token.  Subsequent invocations of firstToken() do NOT reset the
     * lexer state, but will simply return the token returned by the first
     * invocation of this method.
     *
     * @return Token first token in the input
     * @throws BadSyntaxException if a problem occurs when reading the input for
     * the first token.
     */
    abstract public Token firstToken();

    /** Extracts the next token from the input.  When all input has been
     * consumed, the token type of that last token must be EOF.  Subsequent
     * invocations of nextToken() will continue to return the EOF token type.
     *
     * @throws BadSyntaxException if the lexer runs into trouble processing the
     * input
     */
    public abstract Token nextToken() throws BadSyntaxException;

    /** Defines the set of valid PetiteDB keywords as an unmodifiable List
     */
    public final static List<String> keywords = Collections.
            unmodifiableList(Arrays.
                    asList("insert", "into", "values", "delete",
                            "int", "varchar", "view",
                            "update", "set", "select", "from", "where", "and",
                            "as", "index", "on", "create", "table"));

    /** Implementations must return this variable to inform clients that the
     * input has been consumed
     */
    public static Token EOF_Token =
            new Token(TokenType.EOF, String.valueOf(TokenType.EOF));
}