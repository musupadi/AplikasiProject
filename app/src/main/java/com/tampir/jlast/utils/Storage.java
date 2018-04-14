package com.tampir.jlast.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahmatul on 6/19/16.
 */
public class Storage {
    private static SqlConnection appDB;
    public Storage(SqlConnection appDB){
        this.appDB = appDB;
    }
    public void setCurrentUser(String content){
        setData(content,ST_USER);
    }
    public ContentJson getCurrentUser(){
        return getData(ST_USER);
    }
    public void removeCurrentUser(){
        removeData(ST_USER);
    }
    public ContentJson getContent(String key){
        String content = appDB.getContent(key);
        if (content!=null){
            return new ContentJson(content);
        }
        return null;
    }

    public void setData(String content, String key){
        appDB.setContent(content,key);
    }
    public void setDataReplace(String content, String key){
        removeData(key);
        appDB.setContent(content,key);
    }

    public ContentJson getData(String key){
        return getContent(key);
    }
    public ArrayList<ContentJson> getDataAll(String key){
        List list = appDB.getContentStartWith(key);
        if (list!=null){
            ArrayList<ContentJson> contents = new ArrayList<ContentJson>();
            for (int i=0;i<list.size();i++){
                contents.add(new ContentJson(list.get(i).toString()));
            }
            return contents;
        }
        return null;
    }
    public void removeData(String key){
        appDB.removeContentAll(key);
    }
    public void removeDataStartWith(String key){
        appDB.removeContentAllStartWith(key);
    }
    public void removeDataBeforeToday(String key){
        appDB.removeContentBeforeAllStartWith(key);
    }
    public void clearAllData(){
        removeData(ST_USER);
    }

    public static final String ST_USER = "user";
    public static final String ST_BANNER= "banner";
    public static final String ST_PM= "PrivateMessage";
    public static final String ST_SCANEBARCODE= "greet_member";
    public static final String ST_SALDOMEMBER= "saldo_member";
    public static final String ST_ADS= "iklan";
    public static final String ST_CONFIG = "configure";
    public static final String ST_VIDEO = "video";

    /* PRIVATE MESSAGE */
    public void saveChat(String content,ContentJson member){
        Long tsLong = System.currentTimeMillis()/1000;
        appDB.setContent(content,ST_PM + "_" + member.getString("id") , tsLong.toString());

        appDB.removeContent(ST_PM + "_member", member.getString("id"));
        appDB.setContent(member.toString(),ST_PM + "_member", member.getString("id"));
    }
    public ArrayList<ContentJson> getChatList(ContentJson member){
        List list = appDB.getContentAll(ST_PM + "_" + member.getString("id"));
        if (list!=null){
            ArrayList<ContentJson> contents = new ArrayList<ContentJson>();
            for (int i=0;i<list.size();i++){
                contents.add(new ContentJson(list.get(i).toString()));
            }
            return contents;
        }
        return null;
    }
    public ArrayList<ContentJson> getChatMemberList(){
        List list = appDB.getContentAll(ST_PM + "_member");
        if (list!=null){
            ArrayList<ContentJson> contents = new ArrayList<ContentJson>();
            for (int i=0;i<list.size();i++){
                contents.add(new ContentJson(list.get(i).toString()));
            }
            return contents;
        }
        return null;
    }

    public void clearChat(ContentJson member){
        appDB.removeContentAll(ST_PM + "_" + member.getString("id"));
        appDB.removeContent(ST_PM + "_member", member.getString("id"));
    }

}