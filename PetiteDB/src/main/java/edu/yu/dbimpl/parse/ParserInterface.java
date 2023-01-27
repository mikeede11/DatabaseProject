package edu.yu.dbimpl.parse;

import edu.yu.dbimpl.query.Constant;
import edu.yu.dbimpl.query.Expression;
import edu.yu.dbimpl.query.Predicate;
import edu.yu.dbimpl.query.Term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ParserInterface extends ParserInterfaceBase {
    private String s;
    private LexerBase lexLuthor;
    private LexerBase.Token currToken;
    /**
     * Constructor: stores the supplied string so as to be able to parse later
     * per the client's explicit subsequent "typing" invocations.  The
     * constructor MAY NOT throw a BadSyntaxException even if the string is not
     * recognized by the PetiteDB grammar.
     * <p>
     * See lecture discussion for the parser requirements.
     *
     * @param s the string to be parsed
     */
    public ParserInterface(String s) {
        super(s);
        this.s = s;
        this.lexLuthor = new Lexer(s);
        this.currToken = lexLuthor.firstToken();
    }

    //NOTE YOU MAY ONLY CALL NEXT AFTER YOU CONFIRMED THE IDENTITY OF THE TOKEN
    @Override
    public String field() {
        if(currToken.type != LexerBase.TokenType.ID) {throw new BadSyntaxException("Lexer did NOT match an ID");}

        return currToken.value;
    }

    @Override
    public Constant constant() {
        if(currToken.type != LexerBase.TokenType.NUMERIC_CONSTANT && currToken.type != LexerBase.TokenType.STRING_CONSTANT) {throw new BadSyntaxException("Lexer did NOT match a Constant");}

        return new Constant(currToken.value);
    }

    @Override
    public Expression expression() {
        String field = "";
        try {
            field = field();
            return new Expression(field);
        }catch(BadSyntaxException bse){
            //ok it wasnt a field lets try constant
        }
        Constant c = constant();//if not constant willthrow bse
        return new Expression(c);
    }

    @Override
    public Term term() {
        Expression e1 = expression();
        this.currToken = lexLuthor.nextToken();
        if (this.currToken.type != LexerBase.TokenType.DELIMITER || !this.currToken.value.equals("=")) {
            throw new BadSyntaxException("Invalid term");
        }
        this.currToken = lexLuthor.nextToken();

        Expression e2 = expression();
        return new Term(e1, e2);
    }

    @Override
    public Predicate predicate() {
        Predicate p = new Predicate(term());
        recPred(p);
        return p;
    }

    private void recPred(Predicate p){
        this.currToken = lexLuthor.nextToken();
        if(currToken.type != LexerBase.TokenType.KEYWORD || !currToken.value.equals("and")){ return ;}
        this.currToken = lexLuthor.nextToken();
        p.add(term());
        recPred(p);
    }
    @Override
    public ParseQueryBase query() {
        if(currToken.type != LexerBase.TokenType.KEYWORD || !currToken.value.equals("select")){throw  new  BadSyntaxException("Expected keyword SELECT - incorrect syntax for a SELECT query");}
        this.currToken = lexLuthor.nextToken();
        selectList();
        if(currToken.type != LexerBase.TokenType.KEYWORD || !currToken.value.equals("from")){throw  new  BadSyntaxException("Expected keyword FROM - incorrect syntax for a SELECT query");}
        this.currToken = lexLuthor.nextToken();
        tableList();
        predicateList();
        return new ParseQuery(fields, tables, bigPredicate);
        //FIXME I noticed the production for a query technically called for the possibility of many WHERE statements [ WHERE PREDICATE ]
        // nvrm its intentional look at the other productions ... but how will it be one predicate Answer: you can add predicates

    }


    List<String> fields = new ArrayList<>();
    Collection<String> tables = new ArrayList<>();
    Predicate bigPredicate = new Predicate();
    private void selectList(){
        fields.add(field());
        this.currToken = lexLuthor.nextToken();
        if(this.currToken.type == LexerBase.TokenType.DELIMITER && this.currToken.value.equals(",")){
            this.currToken = lexLuthor.nextToken();
            selectList();
        }
    }

    private void tableList(){
        tables.add(field());
        this.currToken = lexLuthor.nextToken();
        if(this.currToken.type == LexerBase.TokenType.DELIMITER && this.currToken.value.equals(",")){
            this.currToken = lexLuthor.nextToken();
            tableList();
        }
    }

    private void predicateList(){
        if(currToken.type != LexerBase.TokenType.KEYWORD || !currToken.value.equals("where")){return;}
        this.currToken = lexLuthor.nextToken();
        bigPredicate.add(predicate().terms());
        predicateList();
        //FIXME Notemy implementation throws a BadSytaxException if it encounters a WHERE keyword without a Predicate
        // I believe this is correct as it would not conform to the CFG
    }
}
