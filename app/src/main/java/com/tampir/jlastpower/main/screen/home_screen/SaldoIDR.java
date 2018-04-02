package com.tampir.jlastpower.main.screen.home_screen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tampir.jlastpower.App;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.main.screen.BaseFragment;
import com.tampir.jlastpower.utils.ContentJson;
import com.tampir.jlastpower.utils.Storage;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SaldoIDR extends BaseFragment {
    View fragment;
    @BindView(R.id.lbIDR) TextView lbIDR;
    @BindView(R.id.lbPoinCard) TextView lbPoinCard;
    @BindView(R.id.lbCompare) TextView lbCompare;

    private FragmentCallback fragmentCallback;

    public interface FragmentCallback {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentCallback = (FragmentCallback) getContext();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentJson user = App.storage.getCurrentUser();
        ContentJson infosaldo = App.storage.getContent(Storage.ST_SALDOMEMBER);
        setTitleBar(user.getString("name"));
        setSubTitleBar(infosaldo.getString("member_code"));
        if (fragment==null){
            fragment = inflater.inflate(R.layout.main_screen_saldoidr, null);
            ButterKnife.bind(this,fragment);
        }
        lbIDR.setText(infosaldo.getString("saldo_idr"));
        lbPoinCard.setText(infosaldo.getString("poin_card"));
        lbCompare.setText("1 Poin Card = " + infosaldo.getString("idr_konversi") + " IDR");

        return fragment;
    }

    @OnClick({R.id.btnHistory})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnHistory:
                Toast.makeText(getContext(), "HISTORY", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
