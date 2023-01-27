package edu.yu.dbimpl.parse;

import org.junit.Assert;
import org.junit.Test;

public class LexerTester {

    @Test
    public void lexerTest1(){
        String query1 = "Select col1, col2, col3 FROM table1 WHERE col2 = 'manager' AND WHERE col3 = 10";
        LexerBase lexluthor = new Lexer(query1);
        LexerBase.Token currToken = lexluthor.firstToken();
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.KEYWORD, "select"), currToken);
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.ID, "col1"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.DELIMITER, ","), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.ID, "col2"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.DELIMITER, ","), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.ID, "col3"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.KEYWORD, "from"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.ID, "table1"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.KEYWORD, "where"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.ID, "col2"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.DELIMITER, "="), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.STRING_CONSTANT, "manager"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.KEYWORD, "and"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.KEYWORD, "where"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.ID, "col3"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.DELIMITER, "="), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.NUMERIC_CONSTANT, "10.0"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.EOF, "EOF"), lexluthor.nextToken());
    }

    @Test
    public void lexerTest2(){
        String query2 = "SeLect col FROM TA_BLE WHEre col50 = 'dog50' AnD WherE COP678 = .50.00.";
        LexerBase lexluthor = new Lexer(query2);
        LexerBase.Token currToken = lexluthor.firstToken();
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.KEYWORD, "select"), currToken);
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.ID, "col"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.KEYWORD, "from"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.ID, "ta_ble"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.KEYWORD, "where"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.ID, "col50"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.DELIMITER, "="), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.STRING_CONSTANT, "dog50"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.KEYWORD, "and"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.KEYWORD, "where"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.ID, "cop678"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.DELIMITER, "="), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.DELIMITER, "."), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.NUMERIC_CONSTANT, "50.0"), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.DELIMITER, "."), lexluthor.nextToken());
        Assert.assertEquals(new LexerBase.Token(LexerBase.TokenType.EOF, "EOF"), lexluthor.nextToken());

    }

    @Test
    public void test3Failiures(){
        String query3 = "";
        try {
            LexerBase lexluthor = new Lexer(query3);
        }catch(BadSyntaxException bse){
            Assert.assertTrue(true);
        }
        String query4 = "Select '&*^'";

        LexerBase lexluthor = new Lexer(query4);
        LexerBase.Token currToken = lexluthor.firstToken();
        try {
            lexluthor.nextToken();
        }catch(BadSyntaxException bse){
            Assert.assertTrue(true);
        }

        String query5 = "Select _identifier";

        LexerBase lexluthor2 = new Lexer(query5);
        LexerBase.Token currToken2 = lexluthor2.firstToken();
        try {
            lexluthor2.nextToken();
        }catch(BadSyntaxException bse){
            Assert.assertTrue(true);
        }
    }

}
