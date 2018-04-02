package com.tampir.jlastpower.main.adapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahmatul on 8/30/16.
 */
public class cacheSpinner {
    private List<String> code = new ArrayList();
    private List<String> caption = new ArrayList();

    public cacheSpinner add(String code, String caption){
        this.code.add(code);
        this.caption.add(caption);
        return this;
    }

    public List getList(){
        return caption;
    }

    public String getCode(int id){
        return code.get(id);
    }
}
