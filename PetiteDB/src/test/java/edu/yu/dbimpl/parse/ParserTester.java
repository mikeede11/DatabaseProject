package edu.yu.dbimpl.parse;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;

public class ParserTester {

    @Test
    public void parserTest1(){
        ParserInterface pi = new ParserInterface("SelecT col1, col2, col3 FRoM Table_One WHERE col3 = 'love123' AND col1 = 5.68 WHERE col2 = 7");
        ParseQueryBase pqb = pi.query();
        List<String> fields = pqb.fields();
        Collection<String> tables = pqb.tables();
        Assert.assertEquals("col1",fields.get(0) );
        Assert.assertEquals("col2", fields.get(1));
        Assert.assertEquals("col3", fields.get(2));
        Assert.assertEquals(1, tables.size());
        Assert.assertEquals(tables.iterator().next(), "table_one");
    }

    @Test
    public void parseFailTest(){
        ParserInterface pi1 = new ParserInterface("777");
        try{
            pi1.field();
            fail();
        }catch(BadSyntaxException bse){
            Assert.assertTrue(true);
        }
        ParserInterface pi2 = new ParserInterface("hello");
        try{
            pi2.constant();
            fail();
        }catch(BadSyntaxException bse){
            Assert.assertTrue(true);
        }
        ParserInterface pi3 = new ParserInterface("=======");
        try{
            pi3.expression();
            fail();
        }catch(BadSyntaxException bse){
            Assert.assertTrue(true);
        }
        ParserInterface pi4 = new ParserInterface("6 . 6");
        try{
            pi4.term();
            fail();
        }catch(BadSyntaxException bse){
            Assert.assertTrue(true);
        }
        ParserInterface pi5 = new ParserInterface("5 == hello");
        try{
            pi5.predicate();
            fail();
        }catch(BadSyntaxException bse){
            Assert.assertTrue(true);
        }
        ParserInterface pi6 = new ParserInterface("SELECT col1, col2 from table1 where x = 5 where x AND y = 5");
        try{
            pi6.query();
            fail();
        }catch(BadSyntaxException bse){
            Assert.assertTrue(true);
        }
    }
}
