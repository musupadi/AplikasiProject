package com.tampir.jlast.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by rahmatul on 2/26/16.
 * Simple Json Parshing
 */
public class ContentJson extends JSONObject {
    JSONObject items;

    public ContentJson(String json){
        try {
            this.items = new JSONObject(json);
        }catch(Exception e){}

        //resign
        if (items==null){
            try {
                this.items = new JSONObject();
                this.items.put("data",new JSONArray(json));
                JSONArray m=new JSONArray(json);
            }catch(Exception e){}
        }
    }

    public ContentJson(){
        this.items = new JSONObject();
    }
    public ContentJson put(String tag, String value){
        try {
            this.items.put(tag,value);
        }catch(Exception e){}
        return this;
    }
    public ContentJson put(String tag, ContentJson value){
        try {
            this.items.put(tag,value.items);
        }catch(Exception e){}
        return this;
    }
    public ContentJson put(ContentJson value){
        try {
            if (!this.items.has("data")) this.items.put("data",new JSONArray());
            this.items.getJSONArray("data").put(value.items);
        }catch(Exception e){}
        return this;
    }
    public ContentJson putBoolean(String tag, boolean value){
        try {
            this.items.put(tag,value);
        }catch(Exception e){}
        return this;
    }
    public ContentJson putInt(String tag, int value){
        try {
            this.items.put(tag,value);
        }catch(Exception e){}
        return this;
    }

    public ContentJson(JSONObject items){
        this.items = items;
    }

    public Iterator keys(){
        return items.keys();
    }
    public boolean has(String str){
        return items.has(str);
    }
    /**
     * getArraySize
     *
     */
    public int getArraySize(String str){
        int size = 0;
        try {
            size = items.getJSONArray(str).length();
        }catch(Exception e){}
        return size;
    }

    /**
     * toString
     */
    public String toString(){
        if (items==null) return null;
        try {
            return items.toString();
        }catch(Exception e){ return null;}
    }

    /**
     *
     * get
     * author : rahmatul.hidayat@gmail.com
     *
     */
    public ContentJson get(String str){
        if (items==null) return null;
        try {
            return new ContentJson(items.getJSONObject(str));
        }catch(Exception e){ return null;}
    }
    public ContentJson getArr(String str){
        if (items==null) return null;
        try {
            return new ContentJson(items.getJSONArray(str).toString());
        }catch(Exception e){ return null;}
    }

    public ContentJson get(int index){
        if (items==null) return null;
        try {
            return new ContentJson(new JSONArray(items.toString()).getJSONObject(index));
        }catch(Exception e){ return null;}
    }

    /**
     *
     * get
     * author : rahmatul.hidayat@gmail.com
     *
     * return : JSONObject
     */
    public ContentJson get(String str, int index){
        if (items==null) return null;
        try {
            return new ContentJson(items.getJSONArray(str).getJSONObject(index));
        }catch(Exception e){ return null;}
    }

    /**
     *
     * getString
     * author : rahmatul.hidayat@gmail.com
     *
     */
    @Override
    public String getString(String str){
        try {
            return items.getString(str);
        }catch(Exception e){ return null;}
    }

    /**
     *
     * getInt
     * author : rahmatul.hidayat@gmail.com
     *
     */
    @Override
    public int getInt(String str){
        try {
            return items.getInt(str);
        }catch(Exception e){ return 0;}
    }

    /**
     *
     * getBoolean
     * author : rahmatul.hidayat@gmail.com
     *
     */
    @Override
    public boolean getBoolean(String str){
        try {
            return items.getBoolean(str);
        }catch(Exception e){ return false;}
    }
}
