package com.tampir.jlastpower.main.screen.profile_screen;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.tampir.jlastpower.App;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.main.screen.BaseContainerFragment;
import com.tampir.jlastpower.main.screen.BaseFragment;
import com.tampir.jlastpower.utils.ContentJson;
import com.tampir.jlastpower.utils.General;
import com.tampir.jlastpower.utils.HttpConnection;
import com.tampir.jlastpower.utils.Interface;
import com.tampir.jlastpower.utils.ParameterHttpPost;
import com.tampir.jlastpower.views.ButtonProgress;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangePassword extends BaseFragment {
    View fragment;
    HttpConnection.Task mAuthTask = null;
    @BindView(R.id.btnSave) public ButtonProgress btnSave;
    @BindView(R.id.inputNewPassword) public EditText inputNewPassword;
    @BindView(R.id.inputReNewPassword) public EditText inputReNewPassword;
    @BindView(R.id.inputOldPassword) public EditText inputOldPassword;
    @BindView(R.id.chkPassword) public CheckBox chkPassword;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleBar("Ubah Password");
        setSubTitleBar("Profile");
        if (fragment==null){
            fragment = inflater.inflate(R.layout.main_screen_profile_changepassword, null);
            ButterKnife.bind(this,fragment);

            chkPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()){
                        inputNewPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputReNewPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputOldPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    }else{
                        inputNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        inputReNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        inputOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }
            });
        }
        return fragment;
    }

    @OnClick(R.id.btnSave)
    public void buttonClick(View view) {
        switch (view.getId()){
            case R.id.btnSave :
                if (inputNewPassword.getText().toString().trim().length()==0 || inputOldPassword.getText().toString().trim().length()==0){
                    General.alertOK("Lengkapi form isian terlebih dahulu.", getContext());
                    return;
                }
                if (inputNewPassword.getText().toString().matches(inputReNewPassword.getText().toString())) {
                    final ButtonProgress btn = (ButtonProgress) view;
                    ContentJson user = App.storage.getCurrentUser();
                    String urlParameters = new ParameterHttpPost()
                            .val("id", user.getString("id"))
                            .val("newpassword", inputNewPassword.getText().toString())
                            .val("oldpassword", inputOldPassword.getText().toString())
                            .build();
                    mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "ChangePassword", urlParameters, new HttpConnection.OnTaskFinishListener() {
                        @Override
                        public void onStart() {
                            btn.startProgress();
                            inputNewPassword.setEnabled(false);
                            inputReNewPassword.setEnabled(false);
                            inputOldPassword.setEnabled(false);
                            Interface.hideKeyboard(btn);
                        }

                        @Override
                        public void onFinished(String jsonString, HttpConnection.Error err) {
                            btn.stopProgress();
                            inputNewPassword.setEnabled(true);
                            inputReNewPassword.setEnabled(true);
                            inputOldPassword.setEnabled(true);
                            if (err == null) {
                                ContentJson cj = new ContentJson(jsonString);
                                if (cj.getInt("status") == 1) {
                                    General.alertOK(cj.getString("message"), getContext(), new General.OnButtonClick() {
                                        @Override
                                        public void onClick(int button) {
                                            ((BaseContainerFragment) getParentFragment()).popFragment();
                                        }
                                    });
                                } else {
                                    General.alertOK(cj.getString("message"), getContext());
                                }
                            } else {
                                Toast.makeText(getContext(), err.Message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    mAuthTask.execute();
                }else{
                    General.alertOK("\"Ulangi password baru\" tidak sama.", getContext());
                }
                break;
        }
    }
}
