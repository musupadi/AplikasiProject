package com.tampir.jlastpower.main.screen.home_screen;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tampir.jlastpower.App;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.activity.Main;
import com.tampir.jlastpower.main.screen.BaseContainerFragment;
import com.tampir.jlastpower.main.screen.BaseFragment;
import com.tampir.jlastpower.utils.ContentJson;
import com.tampir.jlastpower.utils.General;
import com.tampir.jlastpower.utils.HttpConnection;
import com.tampir.jlastpower.utils.ParameterHttpPost;
import com.tampir.jlastpower.views.ButtonProgress;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class ChampionProductView extends BaseFragment {
    View fragment;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.imgThumb) ImageView imgThumb;
    @BindView(R.id.item_title) TextView lbTitle;
    @BindView(R.id.item_nominal) TextView lbNominal;
    @BindView(R.id.item_brand) TextView lbBrand;
    @BindView(R.id.btnBuy) ButtonProgress btnBuy;
    @BindView(R.id.btnOrder) ButtonProgress btnOrder;
    @BindView(R.id.lb_noorder) TextView lbNoOrder;

    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.tabsevent)
    TabLayout tabLayout;

    SpotsDialog pdialog;

    public static ChampionProductView instance(ContentJson product) {
        ChampionProductView fg = new ChampionProductView();
        Bundle bundle = new Bundle();
        bundle.putString("product", product.toString());
        fg.setArguments(bundle);
        return fg;
    }

    @OnClick({R.id.btnBuy,R.id.btnOrder})
    public void buttonClick(View view) {
        final ContentJson data = new ContentJson(getArguments().getString("product", ""));
        switch (view.getId()){
            case R.id.btnBuy:
                General.alertOKCancel("Beli " + data.getString("nama_product"), getContext(), new General.OnButtonClick() {
                    @Override
                    public void onClick(int button) {
                        if (button == AlertDialog.BUTTON_POSITIVE) {
                            ContentJson user = App.storage.getCurrentUser();
                            String urlParameters = new ParameterHttpPost()
                                    .val("id", user.getString("id"))
                                    .val("product_id", data.getString("id"))
                                    .val("sessionlogin", user.getString("ses"))
                                    .build();
                            HttpConnection.Task mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "ClaimProduct", urlParameters, new HttpConnection.OnTaskFinishListener() {
                                @Override
                                public void onStart() {
                                    pdialog.show();
                                }

                                @Override
                                public void onFinished(String jsonString, HttpConnection.Error err) {
                                    pdialog.dismiss();
                                    if (err == null) {
                                        ContentJson cj = new ContentJson(jsonString);
                                        if (cj.getInt("status") == 1) {
                                            General.alertOK(cj.getString("message"), getContext(), new General.OnButtonClick() {
                                                @Override
                                                public void onClick(int button) {

                                                }
                                            });
                                            //update Saldo IDR
                                            ((Main) getActivity()).fetchPoinInfo();
                                        } else {
                                            Toast.makeText(getContext(), cj.getString("message"), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), err.Message, Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                            mAuthTask.execute();
                        }
                    }
                });
                break;
            case R.id.btnOrder:
                ContentJson user = App.storage.getCurrentUser();
                String urlParameters = new ParameterHttpPost()
                        .val("id", user.getString("id"))
                        .val("product_id", data.getString("id"))
                        .val("sessionlogin", user.getString("ses"))
                        .build();
                HttpConnection.Task mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "GenerateOrderProduct", urlParameters, new HttpConnection.OnTaskFinishListener() {
                    @Override
                    public void onStart() {
                        btnOrder.startProgress();
                    }

                    @Override
                    public void onFinished(String jsonString, HttpConnection.Error err) {
                        btnOrder.stopProgress();
                        if (err == null) {
                            ContentJson cj = new ContentJson(jsonString);
                            if (cj.getInt("status") == 1) {
                                lbNoOrder.setVisibility(View.VISIBLE);
                                btnOrder.setVisibility(View.GONE);
                                lbNoOrder.setText(cj.get("data").getString("order_code"));
                            }
                        }

                    }
                });
                mAuthTask.execute();
                break;


        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (fragment==null){
            hideToolbar();
            fragment = inflater.inflate(R.layout.main_screen_champion_view, null);
            ButterKnife.bind(this,fragment);

            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            ContentJson Product = new ContentJson(getArguments().getString("product", ""));
            Glide.with(getContext())
                    .load(Product.getString("foto"))
                    .fitCenter()
                    .crossFade()
                    .placeholder(R.drawable.localdefault)
                    .into(imgThumb);

            lbTitle.setText(Product.getString("nama_product"));
            lbNominal.setText(Product.getString("nominal_rupiah"));
            lbBrand.setText(Product.getString("brand"));
            if (Product.getString("order_code")!=null){
                btnOrder.setVisibility(View.GONE);
                lbNoOrder.setVisibility(View.VISIBLE);
                lbNoOrder.setText(Product.getString("order_code"));
            }

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((BaseContainerFragment) getParentFragment()).popFragment();
                }
            });

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

            pdialog = new SpotsDialog(getContext());

            if (Product.has("status")){
                btnBuy.setVisibility(View.GONE);
                btnOrder.setVisibility(View.VISIBLE);
            }else{
                btnBuy.setVisibility(View.VISIBLE);
                btnOrder.setVisibility(View.GONE);
            }
        }
        return fragment;
    }

    private void setupViewPager(ViewPager viewPager) {
        ContentJson Product = new ContentJson(getArguments().getString("product", ""));
        AdapterProduct adapterProduct = new AdapterProduct(getChildFragmentManager());
        adapterProduct.addFragment(infoproduct.text(Product.getString("overview")), "Overview");
        adapterProduct.addFragment(infoproduct.text(Product.getString("how_to_use")), "How to Use");
        adapterProduct.addFragment(infoproduct.text(Product.getString("tc")), "T &  C");
        viewPager.setAdapter(adapterProduct);
    }

    private class AdapterProduct extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public AdapterProduct(FragmentManager fm) {
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

    public static class infoproduct extends Fragment {

        public static infoproduct text(String text) {
            infoproduct fg = new infoproduct();
            Bundle bundle = new Bundle();
            bundle.putString("text", text);
            fg.setArguments(bundle);
            return fg;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.item_text_html, container, false);
            ((TextView) rootView.findViewById(R.id.lb_text)).setText(getArguments().getString("text", ""));
            return rootView;
        }
    }

}
