package com.tampir.jlast.main.screen.home_screen;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tampir.jlast.App;
import com.tampir.jlast.R;
import com.tampir.jlast.main.screen.BaseChildFragment;
import com.tampir.jlast.main.screen.BaseFragment;
import com.tampir.jlast.utils.ContentJson;
import com.tampir.jlast.utils.Storage;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChampionProduct extends BaseFragment {
    View fragment;
    private AdapterChampion adapterChampion;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.tabsevent) TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (fragment==null){
            ContentJson infosaldo = App.storage.getContent(Storage.ST_SALDOMEMBER);
            setTitleBar("Champion Product");
            setSubTitleBar("J-LAST");

            fragment = inflater.inflate(R.layout.main_screen_champion, null);
            ButterKnife.bind(this,fragment);

            setupViewPager(viewPager);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }
        return fragment;
    }

    private void setupViewPager(ViewPager viewPager) {
        adapterChampion = new AdapterChampion(getChildFragmentManager());
        adapterChampion.addFragment(new ChampionProductBuy(), "Beli Produk");
        adapterChampion.addFragment(new ChampionProductMe(), "Penukaran");
        viewPager.setAdapter(adapterChampion);
    }

    public void selectPager(int i){
        viewPager.setCurrentItem(i);
    }
    public void refreshPager(int i){
        ((BaseChildFragment) adapterChampion.getItem(i)).pageReset();
    }

    private class AdapterChampion extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public AdapterChampion(FragmentManager fm) {
            super(fm);
        }
        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    @Override
    public void scrollTop(){
        ((BaseChildFragment) adapterChampion.getItem(viewPager.getCurrentItem())).scrollTop();
    }

}
