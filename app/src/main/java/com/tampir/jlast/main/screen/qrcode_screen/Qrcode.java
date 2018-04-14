package com.tampir.jlast.main.screen.qrcode_screen;

import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.tampir.jlast.App;
import com.tampir.jlast.R;
import com.tampir.jlast.activity.Main;
import com.tampir.jlast.main.screen.BaseContainerFragment;
import com.tampir.jlast.main.screen.BaseFragment;
import com.tampir.jlast.main.screen.member_screen.MemberInfo;
import com.tampir.jlast.utils.ContentJson;
import com.tampir.jlast.utils.HttpConnection;
import com.tampir.jlast.utils.ParameterHttpPost;
import com.tampir.jlast.utils.Storage;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

import static android.content.Context.VIBRATOR_SERVICE;

public class Qrcode extends BaseFragment {
    View fragment;
    SpotsDialog pdialog;
    @BindView(R.id.qrdecoderview)
    QRCodeReaderView qrCodeReaderView;

    HttpConnection.Task mAuthTask = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleBar("Scane QRCode");
        hideToolbar();
        if (fragment==null){
            fragment = inflater.inflate(R.layout.main_screen_qrcode, null);
            ButterKnife.bind(this,fragment);

            pdialog = new SpotsDialog(getContext());

            //qrCodeReaderView.setOnQRCodeReadListener(this);
            qrCodeReaderView.setQRDecodingEnabled(true);
            qrCodeReaderView.setOnQRCodeReadListener(new QRCodeReaderView.OnQRCodeReadListener(){
                @Override
                public void onQRCodeRead(String text, PointF[] points) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        ((Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        ((Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE)).vibrate(150);
                    }
                    checkMemberInfo(text);
                }
            });

            // Use this function to change the autofocus interval (default is 5 secs)
            qrCodeReaderView.setAutofocusInterval(2000L);
            qrCodeReaderView.setTorchEnabled(false);
            //qrCodeReaderView.setFrontCamera();
            qrCodeReaderView.setBackCamera();
        }
        return fragment;
    }

    private void checkMemberInfo(String member_code){
        ContentJson user = App.storage.getCurrentUser();
        String urlParameters= new ParameterHttpPost()
                .val("id",user.getString("id"))
                .val("member_code", member_code)
                .val("sessionlogin",user.getString("ses"))
                .build();
        mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "PostMemberGreat", urlParameters, new HttpConnection.OnTaskFinishListener() {
            @Override
            public void onStart() {
                qrCodeReaderView.stopCamera();
                pdialog.show();
            }
            @Override
            public void onFinished(String jsonString, HttpConnection.Error err) {
                pdialog.dismiss();
                if (err==null) {
                    ContentJson cj = new ContentJson(jsonString);
                    if (cj.getInt("status") == 1) {
                        ContentJson data = new ContentJson(cj.getString("data"));
                        ContentJson member = new ContentJson()
                                .put("id",data.getString("id"))
                                .put("member_code",data.getString("member_code"))
                                .put("fullname",data.getString("fullname"))
                                .put("msisdn",data.getString("msisdn"))
                                .put("foto",data.getString("foto"));
                        ((BaseContainerFragment) getParentFragment()).replaceFragment(MemberInfo.instance(member), true);
                        App.storage.setData("active", "is_newgreat");

                        //check jumlah greet
                        ContentJson info = App.storage.getData(Storage.ST_SALDOMEMBER);
                        ContentJson configure = App.storage.getData(Storage.ST_CONFIG).get("data");
                        if (info.getInt("greet_count")<configure.getInt("jumlah_greet")) {
                            if (data.getInt("greet_count_me")>=configure.getInt("jumlah_greet")) {
                                ((Main) getActivity()).greetSuccess();
                            }
                        }
                        info.putInt("greet_count",data.getInt("greet_count_me"));
                        App.storage.setDataReplace(info.toString(), Storage.ST_SALDOMEMBER);
                        Toast.makeText(getContext(),cj.getString("message"), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(),cj.getString("message"),Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(),err.Message,Toast.LENGTH_SHORT).show();
                    //qrCodeReaderView.startCamera();
                }
            }
        });
        mAuthTask.execute();
    }

    @Override
    public void scrollTop(){
        qrCodeReaderView.refreshDrawableState();
        qrCodeReaderView.startCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((Main) getActivity()).fetchPoinInfo();
        qrCodeReaderView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        qrCodeReaderView.stopCamera();
    }
}
