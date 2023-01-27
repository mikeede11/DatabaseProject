package edu.yu.dbimpl.record;

import edu.yu.dbimpl.file.PageBase;

import java.io.*;
import java.util.LinkedHashMap;
import java.sql.Types;

import static edu.yu.dbimpl.parse.LexerBase.TokenType.EOF;

public class Tester {
    public static void main(String[] args) {

//        LinkedHashMap<String, Integer> fieldNamesToTypes = new LinkedHashMap<>();
//        fieldNamesToTypes.put("Total Fat", Types.INTEGER);
//        fieldNamesToTypes.put("Serving Size", Types.VARCHAR);
//        fieldNamesToTypes.put("Calories", Types.INTEGER);
//        fieldNamesToTypes.put("Protein", Types.INTEGER);
//        fieldNamesToTypes.put("Taste", Types.VARCHAR);
//        for (String fldName:fieldNamesToTypes.keySet()) {
//            System.out.println(fldName);
//        }
//        System.out.println(PageBase.maxLength(10));
        File file = null;
        try {
            file = File.createTempFile("query", "txt");
            FileWriter myWriter = new FileWriter("query.txt");
            System.out.println("SELECT col1, col2, 3col FROM Table1 WHERE col1 = 'h.i'");
            myWriter.write("SELECT col1, col2, 3col FROM tab_1 WHERE col1 = 'he&llo'");
            myWriter.close();

            StreamTokenizer st = new StreamTokenizer(new BufferedReader(new FileReader("query.txt")));
            st.lowerCaseMode(true);
            st.quoteChar('\'');
            st.ordinaryChar('.');
            st.ordinaryChar('&');

            st.wordChars('_', '_');
//            st.parseNumbers();
            int curToken = st.nextToken();
            while(curToken != StreamTokenizer.TT_EOF){
                if(st.ttype == StreamTokenizer.TT_WORD){
                    System.out.println("Word: " + st.sval);
                }
                else if ( st.ttype == StreamTokenizer.TT_NUMBER){
                    System.out.println(st.nval);
                }
                else if ( st.ttype == '\''){
                    System.out.println(st.sval);
                }
                else if(st.ttype == '.'){
                    System.out.println("Delimiter .: " + Character.toString(st.ttype));
                } else if (st.ttype == ',') {
                    System.out.println("Delimiter ,: " + Character.toString(st.ttype));
                } else if (st.ttype == '=') {
                    System.out.println("Delimiter =: " + Character.toString(st.ttype));
                } else if (st.ttype == '_') {
                    System.out.println("Delimiter =: " + Character.toString(st.ttype));
                }else {
                    System.out.println("This was neither: " + st.toString());
                }
                curToken = st.nextToken();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(file.getAbsolutePath());
        file.deleteOnExit();
    }
}
