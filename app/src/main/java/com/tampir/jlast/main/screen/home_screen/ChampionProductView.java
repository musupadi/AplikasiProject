package com.tampir.jlast.main.screen.home_screen;

import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tampir.jlast.App;
import com.tampir.jlast.R;
import com.tampir.jlast.activity.Main;
import com.tampir.jlast.main.screen.BaseContainerFragment;
import com.tampir.jlast.main.screen.BaseFragment;
import com.tampir.jlast.utils.ContentJson;
import com.tampir.jlast.utils.General;
import com.tampir.jlast.utils.HttpConnection;
import com.tampir.jlast.utils.ParameterHttpPost;
import com.tampir.jlast.utils.ProvidersUtils;
import com.tampir.jlast.utils.Storage;
import com.tampir.jlast.views.ButtonProgress;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

import static java.lang.Integer.parseInt;

public class ChampionProductView extends BaseFragment {
    private static final String TAG = ChampionProductView.class.getSimpleName();

    View fragment;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.imgThumb) ImageView imgThumb;
    @BindView(R.id.item_title) TextView lbTitle;
    @BindView(R.id.item_nominal) TextView lbNominal;
    @BindView(R.id.item_brand) TextView lbBrand;
    @BindView(R.id.btnOrder) ButtonProgress btnOrder;
    @BindView(R.id.lb_noorder) TextView lbNoOrder;

    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.tabsevent)
    TabLayout tabLayout;

    SpotsDialog pdialog;

    public static String produk;

    public static ChampionProductView instance(ContentJson product) {
        ChampionProductView fg = new ChampionProductView();
        Bundle bundle = new Bundle();
        bundle.putString("product", product.toString());
        fg.setArguments(bundle);
        return fg;
    }

    @OnClick({R.id.btnOrder})
    public void buttonClick(View view) {
        final ContentJson data = new ContentJson(getArguments().getString("product", ""));
        switch (view.getId()){
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
                btnOrder.setVisibility(View.VISIBLE);
            }else{
                btnOrder.setVisibility(View.GONE);
            }
            produk = getArguments().getString("product", "");
        }
        return fragment;
    }

    private void setupViewPager(ViewPager viewPager) {
        ContentJson Product = new ContentJson(getArguments().getString("product", ""));
        AdapterProduct adapterProduct = new AdapterProduct(getChildFragmentManager());
        adapterProduct.addFragment(infoproduct.text(Product.getString("overview"), "overview"),
                "Overview");
        adapterProduct.addFragment(infoproduct.text(Product.getString("how_to_use"), "how to use"),
                "How to Use");
        adapterProduct.addFragment(infoproduct.text(Product.getString("tc"), "tc"),
                "T &  C");
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

        @BindView(R.id.lb_text) TextView lbText;
        @BindView(R.id.et_nohp) EditText noHp;
        @BindView(R.id.btnBuy) ButtonProgress beli;

        SpotsDialog pdialogInfo;

        public static infoproduct text(String text, String title) {
            infoproduct fg = new infoproduct();
            Bundle bundle = new Bundle();
            bundle.putString("text", text);
            bundle.putString("title", title);
            fg.setArguments(bundle);
            return fg;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.item_text_html, container, false);
            ButterKnife.bind(this, rootView);
            lbText.setText(getArguments().getString("text", ""));

            if (!getArguments().getString("title").equals("overview")) {
                noHp.setVisibility(View.GONE);
                beli.setVisibility(View.GONE);
            }
            pdialogInfo = new SpotsDialog(getContext(), false, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                }
            });
            return rootView;
        }

        @OnClick({R.id.btnBuy})
        public void onClick(View view) {
            final ContentJson data = new ContentJson(ChampionProductView.produk);
            switch (view.getId()) {
                case R.id.btnBuy:
                    General.alertOKCancel("Beli " + data.getString("nama_product"), getContext(), new General.OnButtonClick() {
                        @Override
                        public void onClick(int button) {
                            if (button == AlertDialog.BUTTON_POSITIVE) {
                                if (noHp.getText().toString().equals("")) {
                                    Toast.makeText(getContext(), "No Hp Tidak Boleh Kosong", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                ContentJson user = App.storage.getCurrentUser();
                                ContentJson saldo = App.storage.getData(Storage.ST_SALDOMEMBER);

                                if (parseInt(saldo.getString("poin_card")) < parseInt(data.getString("poin_card"))) {
                                    Toast.makeText(getContext(), "Point Tidak Cukup", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String isiPulsaParam = new ParameterHttpPost()
                                        .val("request_id", ProvidersUtils.getRequestID(user.getString("ktp")))
                                        .val("no_hp", noHp.getText().toString())//no hp
                                        .val("kode_pulsa", ProvidersUtils.getKodePulsa(data.getString("brand"), data.getString("poin_card")))
                                        .build();
                                HttpConnection.Task authTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "topuprequest",
                                        isiPulsaParam, new HttpConnection.OnTaskFinishListener() {
                                    @Override
                                    public void onStart() {
                                        pdialogInfo.show();
                                    }

                                    @Override
                                    public void onFinished(String jsonString, HttpConnection.Error err) {
                                        if (err == null) {
                                            ContentJson cj = new ContentJson(jsonString);
                                            if (cj.getInt("status") == 1) {
                                                topUp(data);
                                            } else {
                                                pdialogInfo.dismiss();
                                                Toast.makeText(getContext(), cj.getString("message"), Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            pdialogInfo.dismiss();
                                            Toast.makeText(getContext(), err.Message, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                authTask.execute();
                            }
                        }
                    });
                    break;
            }
        }

        private void topUp(ContentJson data) {
            ContentJson user = App.storage.getCurrentUser();
            String urlParameters = new ParameterHttpPost()
                    .val("id", user.getString("id"))
                    .val("product_id", data.getString("id"))
                    .val("sessionlogin", user.getString("ses"))
                    .build();
            HttpConnection.Task mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "ClaimProduct",
                    urlParameters, new HttpConnection.OnTaskFinishListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onFinished(String jsonString, HttpConnection.Error err) {
                    if (err == null) {
                        pdialogInfo.dismiss();
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

}
