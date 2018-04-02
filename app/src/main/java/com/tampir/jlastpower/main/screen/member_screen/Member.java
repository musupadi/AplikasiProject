package com.tampir.jlastpower.main.screen.member_screen;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tampir.jlastpower.R;
import com.tampir.jlastpower.main.screen.BaseChildFragment;
import com.tampir.jlastpower.main.screen.BaseContainerFragment;
import com.tampir.jlastpower.main.screen.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Member extends BaseFragment {
    View fragment;
    private AdapterMember adapterMember;
    @BindView(R.id.viewpager) ViewPager viewPager;
    @BindView(R.id.tabsevent) TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (fragment==null){
            setTitleBar("Member");

            fragment = inflater.inflate(R.layout.main_screen_member, null);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_general).setVisible(true);
        menu.findItem(R.id.action_general).setIcon(R.drawable.ic_chat_white);
        //menu.findItem(R.id.action_general).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.findItem(R.id.action_general).setTitle("");
        menu.findItem(R.id.action_general).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_general :
                ((BaseContainerFragment) getParentFragment()).replaceFragment(new MemberChat(), true);
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        adapterMember = new AdapterMember(getChildFragmentManager());
        adapterMember.addFragment(new MemberGreat(), "Teman Hari Ini");
        adapterMember.addFragment(new MemberNearest(), "Nearest");
        adapterMember.addFragment(new MemberAll(), "Semua");
        viewPager.setAdapter(adapterMember);
    }

    private class AdapterMember extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public AdapterMember(FragmentManager fm) {
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
        ((BaseChildFragment) adapterMember.getItem(viewPager.getCurrentItem())).scrollTop();
    }

}
