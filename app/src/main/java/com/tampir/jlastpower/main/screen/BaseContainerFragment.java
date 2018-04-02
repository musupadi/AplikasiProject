package com.tampir.jlastpower.main.screen;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.view.MenuItem;
import android.widget.ImageView;

import com.tampir.jlastpower.R;
import com.tampir.jlastpower.activity.Main;
import com.tampir.jlastpower.helper.FragmentTransition;

public class BaseContainerFragment extends Fragment {
    int counter = 0;
    String fragement_tag = "randomfragment";
    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        replaceFragment(fragment,addToBackStack,null);
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack, ImageView view) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (addToBackStack) {
            transaction.addToBackStack(null);
            ((Main) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((Main) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }else{
            ((Main) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
            ((Main) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        if (view!=null){
            fragment.setSharedElementEnterTransition(new FragmentTransition());
            fragment.setSharedElementReturnTransition(new FragmentTransition());
/*
            transaction.addSharedElement(view, "imgTrans");*/
            transaction.addSharedElement(view, ViewCompat.getTransitionName(view));
        }else{
            transaction.setCustomAnimations(0,0, R.anim.fixed_position, R.anim.exit_to_right);
        }

        transaction.replace(R.id.container_framelayout, fragment, fragement_tag + counter);
        transaction.commit();
        getChildFragmentManager().executePendingTransactions();
        counter++;
    }

    public boolean popFragment() {
        boolean isPop = false;
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            isPop = true;
            //getChildFragmentManager().popBackStack();
            getChildFragmentManager().popBackStackImmediate();
            counter--;
        }
        return isPop;
    }

    public boolean isHaveChild() {
        boolean isChild = false;
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            isChild=true;
        }
        return isChild;
    }

    public void exitAllChild(){
        boolean isPop = true;
        while (isPop){
            isPop = popFragment();
        }
    }

    public void scrollTop(){
        BaseFragment fg = (BaseFragment) getCurrentFragment();
        if (fg!=null){
            fg.scrollTop();
        }
    }
    public void pageReset(){
        BaseFragment fg = (BaseFragment) getCurrentFragment();
        if (fg!=null){
            fg.pageReset();
        }
    }
    public void removeFragmentListItem(int index){
        BaseFragment fg = (BaseFragment) getCurrentFragment();
        if (fg!=null){
            fg.removeFragmentListItem(index);
        }
    }

    private Fragment getCurrentFragment() {
        String tag;
        if (getChildFragmentManager().getBackStackEntryCount()>0) {
           // FragmentManager.BackStackEntry backStackEntryAt = getChildFragmentManager().getBackStackEntryAt(getChildFragmentManager().getBackStackEntryCount()-1);
            //tag = backStackEntryAt.getName();
        }else{
           // tag = fragement_tag + 0;
        }
        tag = fragement_tag + (counter-1);

        return getChildFragmentManager().findFragmentByTag(tag);
        //return getChildFragmentManager().getFragments().get(getChildFragmentManager().getBackStackEntryCount()); //deprecated access public
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        BaseFragment fg = (BaseFragment) getCurrentFragment();
        if (fg!=null){
            fg.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BaseFragment fg = (BaseFragment) getCurrentFragment();
        if (fg!=null){
            fg.onOptionsItemSelected(item);
        }
        return true;
    }

    public boolean onBackPressed() {
        BaseFragment fg = (BaseFragment) getCurrentFragment();
        if (fg!=null){
            return fg.onBackPressed();
        }else{
            return true;
        }
    }
}