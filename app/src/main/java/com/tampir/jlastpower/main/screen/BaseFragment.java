package com.tampir.jlastpower.main.screen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;

import com.tampir.jlastpower.R;
import com.tampir.jlastpower.activity.Main;

public class BaseFragment extends Fragment {
    private String titleBar = "";
    private String subTitleBar = "";
    private boolean showTitle = true;
    private boolean showToobar = true;
    private boolean showSaldoIDR = false;

    public void scrollTop(){}
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
    public void showSaldoIDR(){
        this.showSaldoIDR=true;
        ((Main) getActivity()).showSaldoIDR(showSaldoIDR);
    }
    public boolean onBackPressed() {
        return true;
    }

    public void onGetTitleBar(){
        ((Main) getActivity()).setTitleApp(titleBar);
        ((Main) getActivity()).setSubTitleApp(subTitleBar);
        ((Main) getActivity()).setShowTitle(showTitle);
        ((Main) getActivity()).hideToolbar(showToobar);
        ((Main) getActivity()).showSaldoIDR(showSaldoIDR);
    }
    public void Logout(){
        ((Main) getActivity()).logOut();
    }

    @Override
    public void onResume(){
        super.onResume();
        onGetTitleBar();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_general).setVisible(false);
    }
}