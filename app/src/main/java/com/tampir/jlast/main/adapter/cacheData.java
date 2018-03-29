package com.tampir.jlast.main.adapter;


import com.tampir.jlast.utils.ContentJson;

/**
 * Created by rahmatul on 8/30/16.
 */
public class cacheData {
    public static final int STYLE_QUESTION_BOX = 1;
    public static final int STYLE_PINNED = 2;
    public int style = 0;

    ContentJson data;
    public cacheData(){}

    public cacheData setData(ContentJson data){
        this.data = data;
        return this;
    }
    public ContentJson getData(){
        return this.data;
    }
    public cacheData setStyle(int style){
        this.style = style;
        return this;
    }
    public int getStyle(){
        return style;
    }
    public int getStyleDefault(){
        return STYLE_QUESTION_BOX;
    }
}
