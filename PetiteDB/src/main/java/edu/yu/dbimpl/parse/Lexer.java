package edu.yu.dbimpl.parse;

import java.io.*;

public class Lexer extends LexerBase {
    private StreamTokenizer st;
    private String query;
    private Token firstToken;
    /**
     * Constructor: creates a new lexical analyzer to be used for parsing a
     * PetiteDB SQL statement.  Upon completion of the constructor invocation,
     * the lexer must have consumed one token, and is able to return that token
     * via firstToken().
     * <p>
     * See lecture discussion for the Lexer requirements.
     *
     * @param s the statement to be lex'd
     * @throws BadSyntaxException if a problem occurs when reading the input for
     *                            the first token.
     * @see //firstToken
     */
    public Lexer(String s) {
        super(s);
        this.query = s;
        //if not even one token throw exception
        this.st = new StreamTokenizer(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes()))));
        this.st.lowerCaseMode(true);
        this.st.quoteChar('\'');
        this.st.ordinaryChar('.');
        this.st.wordChars('_', '_');
        try {
            this.firstToken = createLexeme(st.nextToken());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Token firstToken() {
        return firstToken;
    }

    @Override
    public Token nextToken() throws BadSyntaxException {
        try {
            return createLexeme(st.nextToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EOF_Token;
    }

    private boolean isAlphaNumeric(String s) {
        return s != null && s.matches("^[a-zA-Z0-9_]*$");
    }
    private Token createLexeme(int token){
        if(token != StreamTokenizer.TT_EOF) {
            if (st.ttype == StreamTokenizer.TT_WORD) {
                //either a identifiier or a keyword
                if (keywords.contains(st.sval)) {
                    System.out.println("Keyword: " + st.sval);
                        return new Token(TokenType.KEYWORD, st.sval);
                }
                else if(isAlphaNumeric(st.sval) && !st.sval.startsWith("_")) {
                    System.out.println("Identifier: " + st.sval);
                    return new Token(TokenType.ID, st.sval);
                }
                else{
                    throw new BadSyntaxException("invalid identifier");
                }
            } else if (st.ttype == StreamTokenizer.TT_NUMBER) {
                System.out.println("Numeric_Constant: " + st.nval);
                return new Token(TokenType.NUMERIC_CONSTANT, String.valueOf(st.nval));
            } else if (st.ttype == '\'') {
                System.out.println("String_Constant: " + st.sval);
                if(isAlphaNumeric(st.sval)) {
                    return new Token(TokenType.STRING_CONSTANT, st.sval);
                }
            }
            else if(st.ttype == '.'){
                System.out.println("Delimiter .: " + (char)st.ttype);
                return new Token(TokenType.DELIMITER, Character.toString(st.ttype));
            } else if (st.ttype == ',') {
                System.out.println("Delimiter ,: " + st.toString());
                return new Token(TokenType.DELIMITER,  Character.toString(st.ttype));
            } else if (st.ttype == '=') {
                System.out.println("Delimiter =: " + st.toString());
                return new Token(TokenType.DELIMITER,  Character.toString(st.ttype));
            }else {
                System.out.println("This was neither: " + st.toString());
                throw new BadSyntaxException("Token Unrecognizable, please fix syntax");
            }
        }

        return EOF_Token;
    }
}
