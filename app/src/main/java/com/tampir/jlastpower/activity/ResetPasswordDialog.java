package com.tampir.jlastpower.activity;

import android.app.Dialog;
import android.app.Service;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.tampir.jlastpower.App;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.utils.Const;
import com.tampir.jlastpower.utils.ContentJson;
import com.tampir.jlastpower.utils.General;
import com.tampir.jlastpower.utils.HttpConnection;
import com.tampir.jlastpower.utils.Interface;
import com.tampir.jlastpower.utils.LibFunction;
import com.tampir.jlastpower.utils.ParameterHttpPost;
import com.tampir.jlastpower.views.ButtonProgress;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class ResetPasswordDialog extends DialogFragment {
    View fragment;
    @BindView(R.id.btnClose) View btnClose;
    @BindView(R.id.btnResetPassword) ButtonProgress btnResetPassword;
    @BindView(R.id.btnSave) ButtonProgress btnSave;
    @BindView(R.id.btnReSendSms) ButtonProgress btnReSendSms;
    @BindView(R.id.viewInterSMSConfirmation) View viewInterSMSConfirmation;
    @BindView(R.id.viewSMSConfirmation) View viewSMSConfirmation;
    @BindView(R.id.lb_infosms) TextView lbInfoSms;
    @BindView(R.id.viewEntryUserID) View viewEntryUserID;
    @BindView(R.id.viewChangePassword) View viewChangePassword;
    @BindView(R.id.edt_userid) EditText tUserId;
    @BindView(R.id.txt_pin_entry) PinEntryEditText tPinEntry;
    @BindView(R.id.inputNewPassword) EditText tNewPassword;
    @BindView(R.id.inputReNewPassword) EditText tReNewPassword;
    @BindView(R.id.chkPassword) CheckBox chkPassword;

    HttpConnection.Task mAuthTask = null;
    FirebaseAuth mAuth;
    SpotsDialog pdialog;

    private String msisdn_for_firebase = null;
    private String user_login = null;
    private String user_id = null;
    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;

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
            //getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            btnResetPassword.setEnabled(false);
            btnSave.setEnabled(false);
            tUserId.addTextChangedListener(textWatcher);
            tNewPassword.addTextChangedListener(textWatcher);
            tReNewPassword.addTextChangedListener(textWatcher);

            mAuth = FirebaseAuth.getInstance();
            pdialog = new SpotsDialog(getContext());

            tPinEntry.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                @Override
                public void onPinEntered(CharSequence str) {
                    pdialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, str.toString());
                    signInWithPhoneAuthCredential(credential);
                }
            });
            btnReSendSms.setEnabled(false);

            chkPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()){
                        tNewPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        tReNewPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    }else{
                        tNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        tReNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }
            });
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void afterTextChanged(Editable s) {
            btnResetPassword.setEnabled(tUserId.getText().toString().trim().length()>0);
            btnSave.setEnabled(tNewPassword.getText().toString().trim().length()>0 && tReNewPassword.getText().toString().trim().length()>0);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = inflater.inflate(R.layout.dialog_resetpassword, container, false);
        ButterKnife.bind(this,fragment);
        return fragment;
    }

    @OnClick({R.id.btnClose,R.id.btnResetPassword,R.id.btnSave, R.id.btnReSendSms})
    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.btnClose:
                dismiss();
                break;
            case R.id.btnResetPassword:
                requestResetPassword();
                break;
            case R.id.btnSave:
                postNewPassword();
                break;
            case R.id.btnReSendSms:
                validasiMSISDN();
                break;
        }
    }

    private void requestResetPassword(){
        if (!tUserId.getText().toString().matches("")) {
            String urlParameters = new ParameterHttpPost()
                    .val("email", tUserId.getText().toString())
                    .build();
            mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "RequestResetPassword", urlParameters, new HttpConnection.OnTaskFinishListener() {
                @Override
                public void onStart() {
                    btnResetPassword.startProgress();
                    Interface.hideKeyboard(btnResetPassword);
                }

                @Override
                public void onFinished(String jsonString, HttpConnection.Error err) {
                    btnResetPassword.stopProgress();
                    if (err == null) {
                        ContentJson cj = new ContentJson(jsonString);
                        if (cj.getInt("status") == 1) {
                            msisdn_for_firebase = cj.get("data").getString("validasi_sms");
                            user_login = cj.get("data").getString("member_login");
                            user_id  = cj.get("data").getString("id");
                            validasiMSISDN();
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
            General.alertOK("Semua informasi harus diisi!", getContext());
        }
    }

    private void postNewPassword(){
        if (tNewPassword.getText().toString().matches(tReNewPassword.getText().toString())) {
            String urlParameters = new ParameterHttpPost()
                    .val("id", user_id)
                    .val("newpassword", tNewPassword.getText().toString())
                    .build();
            mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "ResetPassword", urlParameters, new HttpConnection.OnTaskFinishListener() {
                @Override
                public void onStart() {
                    btnResetPassword.startProgress();
                    Interface.hideKeyboard(btnResetPassword);
                }

                @Override
                public void onFinished(String jsonString, HttpConnection.Error err) {
                    btnResetPassword.stopProgress();
                    if (err == null) {
                        ContentJson cj = new ContentJson(jsonString);
                        if (cj.getInt("status") == 1) {
                            General.alertOK(cj.getString("message"), getContext(), new General.OnButtonClick() {
                                @Override
                                public void onClick(int button) {
                                    login();
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
            General.alertOK("Ulangi Kata sandi tidak sama", getContext());
        }
    }

    private void validasiMSISDN(){
        pdialog.show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                msisdn_for_firebase,    // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            Log.d(Const.TAG, "onVerificationCompleted:" + credential);
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            pdialog.dismiss();
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(getContext(),e.getMessage() + "",Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(getContext(),e.getMessage() + "",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getContext(),e.getMessage() + "",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
            pdialog.dismiss();
            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;
            // ...
            showValidationForm();
        }
    };

    private void showValidationForm(){
        viewSMSConfirmation.setVisibility(View.VISIBLE);
        viewInterSMSConfirmation.setTranslationY(LibFunction.dpToPx(128));
        viewInterSMSConfirmation.setAlpha(0.5f);
        viewInterSMSConfirmation.animate().translationY(0).alpha(1.0f).setDuration(500);
        lbInfoSms.setText("Masukan 6-Digit kode yang telah dikirim ke " + msisdn_for_firebase);
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                btnReSendSms.setText("KIRIM ULANG SMS ("+millisUntilFinished / 1000+")");
                btnReSendSms.setEnabled(false);
            }
            public void onFinish() {
                btnReSendSms.setText("KIRIM ULANG SMS");
                btnReSendSms.setEnabled(true);
            }
        }.start();
        tPinEntry.requestFocus();
        tPinEntry.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(tPinEntry, 0);
            }
        },200);
    }
    private void showNewPasswordForm(){
        viewChangePassword.setVisibility(View.VISIBLE);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pdialog.dismiss();
                        if (task.isSuccessful()) {
                            Log.d(Const.TAG, "signInWithCredential:success");
                            showNewPasswordForm();
                            //mAuth.getCurrentUser().delete();
                            mAuth.signOut();
                        } else {
                            Log.w(Const.TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                General.alertOK("Kode yang dimasukkan salah.", getContext());
                            }
                        }
                    }
                });
    }

    private void login(){
        String urlParameters = new ParameterHttpPost()
                .val("email", user_login)
                .val("password", tNewPassword.getText().toString())
                .build();
        mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "login", urlParameters, new HttpConnection.OnTaskFinishListener() {
            @Override
            public void onStart() {
                pdialog.show();
            }

            @Override
            public void onFinished(String jsonString, HttpConnection.Error err) {
                pdialog.hide();
                if (err == null) {
                    ContentJson cj = new ContentJson(jsonString);
                    if (cj.getInt("status") == 1) {
                        App.storage.setCurrentUser(cj.getString("data"));
                        ((Login) getActivity()).checkUserLogin();
                    } else {
                        Toast.makeText(getContext(), cj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), err.Message, Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }
        });
        mAuthTask.execute();
    }

}
