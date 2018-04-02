package com.tampir.jlastpower.main.screen.home_screen;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tampir.jlastpower.App;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.utils.ContentJson;
import com.tampir.jlastpower.utils.Storage;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GreetSuccessDialog extends DialogFragment {
    View fragment;

    @BindView(R.id.layGreet) View layGreet;
    @BindView(R.id.lbGreet) TextView lbGreet;
    @BindView(R.id.lbInfo) TextView lbInfo;

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        if(getDialog() != null) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = inflater.inflate(R.layout.dialog_greetsuccess, container, false);
        ButterKnife.bind(this,fragment);

        //check jumlah greet
        lbInfo.setText("Kumpulkan greet untuk mendapatkan poin card dari iklan dan akses fitur video");
        lbGreet.setText("Belum Ada Greet Hari Ini");
        layGreet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        ContentJson info = App.storage.getData(Storage.ST_SALDOMEMBER);
        if (info!=null) {
            ContentJson configure = App.storage.getData(Storage.ST_CONFIG).get("data");
            if (info.getInt("greet_count") < configure.getInt("jumlah_greet")) {
                lbGreet.setText(info.getInt("greet_count") + " Greet");
                lbInfo.setText("Kumpulkan "+configure.getInt("jumlah_greet")+" greet untuk mendapatkan poin card dari iklan dan akses fitur video");
            }else{
                lbGreet.setText("Greet Success");
                lbInfo.setText("Kamu telah berhasil mengumpulkan "+info.getInt("greet_count")+" greet");
            }
        }

        return fragment;
    }

}
