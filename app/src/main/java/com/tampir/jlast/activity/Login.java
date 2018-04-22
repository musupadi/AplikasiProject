package com.tampir.jlast.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tampir.jlast.App;
import com.tampir.jlast.R;
import com.tampir.jlast.main.screen.player.ContentPlayer;
import com.tampir.jlast.utils.Const;
import com.tampir.jlast.utils.ContentJson;
import com.tampir.jlast.utils.General;
import com.tampir.jlast.utils.HttpConnection;
import com.tampir.jlast.utils.Interface;
import com.tampir.jlast.utils.LibFunction;
import com.tampir.jlast.utils.ParameterHttpPost;
import com.tampir.jlast.utils.ApiUtils;
import com.tampir.jlast.views.ButtonProgress;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Login extends AppCompatActivity {
    HttpConnection.Task mAuthTask = null;
    @BindView(R.id.btnLogin) public ButtonProgress btnLogin;
    @BindView(R.id.edt_name) public EditText inputUsername;
    @BindView(R.id.edt_password) public EditText inputPassword;
    @BindView(R.id.framePlayer) FrameLayout framePlayer;

    private ContentPlayer loginPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ApiUtils.callApiVideo();

        ButterKnife.bind(this);
        btnLogin.setEnabled(false);
        inputUsername.addTextChangedListener(textWatcher);
        inputPassword.addTextChangedListener(textWatcher);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int mWidth = width - LibFunction.dpToPx(32+16);
        framePlayer.getLayoutParams().height = mWidth * 9/16;

        loginPlayer = new ContentPlayer();
        FragmentTransaction fg = getSupportFragmentManager().beginTransaction();
        fg.add(loginPlayer, "loginplayer");
        fg.commit();
    }

    @Override
    public void onResume(){
        super.onResume();

        final ContentJson configure = App.storage.getData("configure").get("data");
        if (configure==null) return;
        if (configure.getString("streaming_url_login")==null) return;

        ViewGroup parent = (ViewGroup) loginPlayer.getView().getParent();
        if (parent != null) {
            parent.removeView(loginPlayer.getView());
        }
        framePlayer.addView(loginPlayer.getView(),0);
        ContentPlayer.params params = new ContentPlayer.params();
        params.setUrl(configure.getString("streaming_url_login"));
        params.setThumbnail(configure.getString("streaming_url_placeholder"));
        params.setOnVideoStatusListener(new ContentPlayer.params.OnVideoStatusListener() {
            @Override
            public void OnVideoLoaded() {
                loginPlayer.play();
            }
            @Override
            public void OnVideoEnded(){
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                loginPlayer.play();
                            }},3000);
            }
            @Override
            public void OnVideoPaused() {}
            @Override
            public void OnVideoPlayed() {}
            @Override
            public void OnVideoBuffered() {}
            @Override
            public void OnAdsVideoLoaded(){}
            @Override
            public void OnAdsVideoEnded(ContentJson data) {}
            @Override
            public void OnAdsVideoPaused() {}
            @Override
            public void OnAdsVideoPlayed() {}
            @Override
            public void OnAdsVideoBuffered() {}
        });
        loginPlayer.setParams(params).setup(true);
        loginPlayer.play();
    }

    @Override
    public void onPause(){
        super.onPause();
        loginPlayer.release();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginPlayer = null;
    }


    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void afterTextChanged(Editable s) {
            btnLogin.setEnabled(inputUsername.getText().toString().trim().length()>0 && inputPassword.getText().toString().trim().length()>0);
        }
    };

    @OnClick({R.id.btnLogin,R.id.btnRegister,R.id.btnResetPasword})
    public void buttonClick(View view) {
        switch (view.getId()){
            case R.id.btnLogin :
                final ButtonProgress btn = (ButtonProgress) view;
                String urlParameters = new ParameterHttpPost()
                        .val("email", inputUsername.getText().toString())
                        .val("password", inputPassword.getText().toString())
                        .build();
                mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "login", urlParameters, new HttpConnection.OnTaskFinishListener() {
                    @Override
                    public void onStart() {
                        btn.startProgress();
                        inputUsername.setEnabled(false);
                        inputPassword.setEnabled(false);
                        Interface.hideKeyboard(btn);
                    }

                    @Override
                    public void onFinished(String jsonString, HttpConnection.Error err) {
                        btn.stopProgress();
                        inputUsername.setEnabled(true);
                        inputPassword.setEnabled(true);
                        if (err == null) {
                            ContentJson cj = new ContentJson(jsonString);
                            if (cj.getInt("status") == 1) {
                                App.storage.setCurrentUser(cj.getString("data"));
                                Intent intent = new Intent(getBaseContext(), Main.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            } else {
                                General.alertOK(cj.getString("message"), Login.this);
                            }
                        } else {
                            Toast.makeText(getBaseContext(), err.Message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                mAuthTask.execute();
                break;
            case R.id.btnRegister :
                Intent intent=new Intent(getBaseContext(),Registrasi.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, Const.INTENT_REQUEST_REGISTER);
                break;
            case R.id.btnResetPasword :
                ResetPasswordDialog dialog = new ResetPasswordDialog();
                dialog.show(getSupportFragmentManager(), "dialog");
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.INTENT_REQUEST_REGISTER){
            if (resultCode==RESULT_OK){
                checkUserLogin();
            }
        }
    }

    public void checkUserLogin(){
        if (App.storage.getCurrentUser()!=null) {
            Intent intent = new Intent(getBaseContext(), Main.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

}
