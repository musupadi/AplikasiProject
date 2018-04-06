package com.tampir.jlast.utils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

public class ParameterHttpPost {
    public HashMap<String,String> data = new HashMap<String,String>();
    public ParameterHttpPost json(ContentJson json){
        Iterator iterator = json.keys();
        while(iterator.hasNext()){
            String key = (String)iterator.next();
            data.put(key, json.getString(key));
        }
        return this;
    }
    public ParameterHttpPost val(String key, String value){
        if (data.containsKey(key)) data.remove(key);
        data.put(key,value);
        return this;
    }
    public ParameterHttpPost val(String key, int value){
        if (data.containsKey(key)) data.remove(key);
        data.put(key,value+"");
        return this;
    }
    public String build(){
        StringBuilder str = new StringBuilder();
        try {
            for (String key : data.keySet()) {
                str.append((str.length()>0 ? "&" : "") + key + "=" + URLEncoder.encode(data.get(key), "UTF-8"));
            }
        }catch(Exception e){}
        return str.toString();
    }
    public ContentJson toJson(){
        ContentJson json = new ContentJson();
        for (String key : data.keySet()) {
            json.put(key,data.get(key));
        }
        return json;
    }
}
