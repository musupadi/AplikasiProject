
package com.tampir.jlastpower.main.screen;

import android.support.v4.app.Fragment;

public class BaseChildFragment extends Fragment {
    private String titleBar = "";
    private String subTitleBar = "";
    private boolean showTitle = true;
    private boolean showToobar = true;
    private String actionLabel = null;
    private int actionIcon = 0;

    public void scrollTop(){}
    public boolean actionFragment(){return true;}
    public void actionFragment(boolean action){}
    public void hideBehindButton(){}
    public void pageReset(){}
    public void removeFragmentListItem(int index){}
    public void setTitleBar(String titleBar){this.titleBar=titleBar;}
    public void setShowTitle(boolean showTitle){this.showTitle=showTitle;}
    public void setSubTitleBar(String subTitleBar){this.subTitleBar=subTitleBar;}
    public void hideToolbar(){
        this.showToobar=false;
    }
    public void showToolbar(){
        this.showToobar=true;
    }
    public void setAction(String label, int icon){
        this.actionLabel=label;
        this.actionIcon=icon;
    }

    public void onGetTitleBar(){}
    public void Logout(){
        ((BaseFragment) getParentFragment()).Logout();
    }
}