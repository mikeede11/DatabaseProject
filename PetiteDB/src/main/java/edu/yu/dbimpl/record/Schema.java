package edu.yu.dbimpl.record;

import edu.yu.dbimpl.file.PageBase;

import java.sql.Types;
import java.util.*;

public class Schema extends SchemaBase {
    LinkedHashMap<String, Integer> fldNameToType;
    HashMap<String, Integer> strFldToMaxLength;
    //TODO each length param soecifies the MAX length FOR THAT STR VALUE
    public Schema(){
        super();
        fldNameToType = new LinkedHashMap<>();
        strFldToMaxLength = new HashMap<>();
    }
    @Override
    public void addField(String fldname, int type, int length) {
        //TODO What if add duplicate fieldname?
        //TODO what if type is invalid

        fldNameToType.put(fldname,type);//if its a duplicate itll just update and replace
        if(type == Types.VARCHAR){
            strFldToMaxLength.put(fldname, length);
        }
    }

    @Override
    public void addIntField(String fldname) {
        fldNameToType.put(fldname, Types.INTEGER);//if its a duplicate itll just update and replace
    }

    @Override
    public void addStringField(String fldname, int length) {
        fldNameToType.put(fldname, Types.VARCHAR);//if its a duplicate itll just update and replace
        strFldToMaxLength.put(fldname, length);
    }

    @Override
    public void add(String fldname, SchemaBase sch) {
        if(!sch.hasField(fldname)){throw new IllegalArgumentException("This Schema does not contain this fieldName!");}//is this correct?
        fldNameToType.put(fldname, sch.type(fldname));
        if(sch.type(fldname) == Types.VARCHAR){
            strFldToMaxLength.put(fldname, sch.length(fldname));
        }
    }

    @Override
    public void addAll(SchemaBase sch) {
        int currentFldType = 0;
        for (String fieldName: sch.fields()) {
            currentFldType = sch.type(fieldName);
            if(currentFldType == Types.INTEGER){
                addIntField(fieldName);
            }
            else if(currentFldType == Types.VARCHAR){
                sch.addStringField(fieldName, currentFldType);
            }
        }
    }

    @Override
    public List<String> fields() {
        return new ArrayList<>(fldNameToType.keySet());
    }

    @Override
    public boolean hasField(String fldname) {
        return fldNameToType.containsKey(fldname);
    }

    @Override
    public int type(String fldname) {
        return fldNameToType.get(fldname);
    }

    @Override
    public int length(String fldname) {
        if(type(fldname) != Types.VARCHAR){throw new IllegalArgumentException("Not a String!!!");}

        return strFldToMaxLength.get(fldname);//MaxLength is for layout

    }
}
