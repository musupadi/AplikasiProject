package com.tampir.jlast.activity;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.tampir.jlast.App;
import com.tampir.jlast.BuildConfig;
import com.tampir.jlast.R;
import com.tampir.jlast.UploadFoto;
import com.tampir.jlast.main.adapter.cacheSpinner;
import com.tampir.jlast.utils.Const;
import com.tampir.jlast.utils.ContentJson;
import com.tampir.jlast.utils.General;
import com.tampir.jlast.utils.HttpConnection;
import com.tampir.jlast.utils.Interface;
import com.tampir.jlast.utils.LibFunction;
import com.tampir.jlast.utils.ParameterHttpPost;
import com.tampir.jlast.views.ButtonProgress;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

import static java.lang.Integer.parseInt;

public class Registrasi extends AppCompatActivity {
    SpotsDialog pdialog;
    @BindView(R.id.spnProvinsi) Spinner spnProvinsi;
    @BindView(R.id.spnKota) Spinner spnKota;
    @BindView(R.id.inputFullname) EditText tFullname;
    @BindView(R.id.inputEmail) EditText tEmail;
    @BindView(R.id.inputMsisdn) EditText tMsisdn;
    @BindView(R.id.inputAlamat) EditText tAlamat;
    @BindView(R.id.inputUsername) EditText tUsername;
    @BindView(R.id.inputPassword) EditText tPassword;
    @BindView(R.id.btnRegister) ButtonProgress btnRegister;
    @BindView(R.id.btnReSendSms) ButtonProgress btnReSendSms;
    @BindView(R.id.txt_pin_entry) PinEntryEditText tPinEntry;
    @BindView(R.id.inputKTP) EditText tKTP;
    @BindView(R.id.lb_infosms) TextView lbInfoSms;
    @BindView(R.id.viewSMSConfirmation) View viewSMSConfirmation;
    @BindView(R.id.viewInterSMSConfirmation) View viewInterSMSConfirmation;

    @BindView(R.id.frameFoto) View frameFoto;
    @BindView(R.id.imgFotoKtp) ImageView imgFotoKtp;
    @BindView(R.id.imgFotoWajah) ImageView imgFotoWajah;


    cacheSpinner listProvinsi;
    cacheSpinner listKabupaten;
    HttpConnection.Task mAuthTask = null;
    FirebaseAuth mAuth;

    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private String msisdn_for_firebase = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        pdialog = new SpotsDialog(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Registrasi");
        getSupportActionBar().setSubtitle("Data sesuai KTP / Kartu Pelajar");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        String media = General.readTextFile(getResources().openRawResource(getResources().getIdentifier("provinsi", "raw", getPackageName())));
        ContentJson data = new ContentJson(media);

        listProvinsi = new cacheSpinner();
        int i = 0;
        while (data.get("data",i)!=null){
            listProvinsi.add(data.get("data",i).getString("id"),data.get("data",i).getString("value"));
            i++;
        }

        ArrayAdapter<String> adapterProvinsi = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listProvinsi.getList());
        adapterProvinsi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnProvinsi.setAdapter(adapterProvinsi);

        spnProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                final String provinsi_code = listProvinsi.getCode(spnProvinsi.getSelectedItemPosition());
                if (App.storage.getContent("kabupaten" + provinsi_code)==null) {
                    new HttpConnection.Task(HttpConnection.METHOD_POST, "Area?type=2&parent=" + provinsi_code, null, new HttpConnection.OnTaskFinishListener() {
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
                                    App.storage.setData(jsonString, "kabupaten" + provinsi_code);
                                    showKabupaten(provinsi_code);
                                } else {

                                }
                            } else {
                                Toast.makeText(getBaseContext(), err.Message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).execute();
                }else{
                    showKabupaten(provinsi_code);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        tKTP.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkKTP();
                }
            }
        });

        setResult(RESULT_CANCELED);
        tAlamat.setFilters(new InputFilter[] {new InputFilter.AllCaps(),new InputFilter.LengthFilter(250)});
        tFullname.setFilters(new InputFilter[] {new InputFilter.AllCaps(),new InputFilter.LengthFilter(30)});

        tPinEntry.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
            @Override
            public void onPinEntered(CharSequence str) {
                pdialog.show();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, str.toString());
                signInWithPhoneAuthCredential(credential);
            }
        });
        btnReSendSms.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int mWidth = width - LibFunction.dpToPx(32);
        frameFoto.getLayoutParams().height = mWidth / 2;


        if (!Const.IMAGE_PATH.exists()) Const.IMAGE_PATH.mkdir();
    }

    String strKabupaten = "";
    String strProvinsi = "";
    private void checkKTP(){
        if (tKTP.getText().toString().length()<16) return;;

        strProvinsi = tKTP.getText().toString().substring(0,2);
        strKabupaten = tKTP.getText().toString().substring(0,4);

        String born = tKTP.getText().toString().substring(6,8);
        if (parseInt(born)<=31){
            ((ImageView) ButterKnife.findById(this,R.id.imgAvatarWajah)).setImageResource(R.drawable.avatar_men);
        }else{
            ((ImageView) ButterKnife.findById(this,R.id.imgAvatarWajah)).setImageResource(R.drawable.avatar_women);
        }

        //checkProvinsi
        spnProvinsi.getSelectedItemPosition();
        for (int i=0;i<spnProvinsi.getAdapter().getCount();i++){
            if (listProvinsi.getCode(i).matches(strProvinsi)){
                spnProvinsi.setSelection(i);
                break;
            }
        }

    }

    private void showKabupaten(String provinsi_code){
        ContentJson data = App.storage.getData("kabupaten" + provinsi_code);
        listKabupaten = new cacheSpinner();
        int i = 0;
        int i_selected = 0;
        while (data.get("data",i)!=null){
            listKabupaten.add(data.get("data",i).getString("id"),data.get("data",i).getString("value"));
            if (data.get("data",i).getString("id").matches(strKabupaten)) i_selected = i;
            i++;
        }

        ArrayAdapter<String> adapterKabupaten = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listKabupaten.getList());
        adapterKabupaten.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnKota.setAdapter(adapterKabupaten);
        spnKota.setSelection(i_selected);
    }

    View btnClicked;
    @OnClick({R.id.btnRegister, R.id.btnReSendSms, R.id.btnFotoKtp, R.id.btnFotoWajah})
    public void buttonClick(View view) {
        btnClicked = view;
        switch (view.getId()) {
            case R.id.btnRegister:
                validasiData();
                break;
            case R.id.btnReSendSms:
                validasiMSISDN();
                break;
            case R.id.btnFotoKtp :
                Uri photoURIktp = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        new File(Const.IMAGE_PATH, "tmp.jpg"));
                Intent cameraKtpIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraKtpIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURIktp);
                startActivityForResult(cameraKtpIntent, Const.INTENT_REQUEST_PICTURE);
                break;
            case R.id.btnFotoWajah :
                Uri photoURIface = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        new File(Const.IMAGE_PATH, "tmp.jpg"));
                Intent cameraFaceIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraFaceIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURIface);
                startActivityForResult(cameraFaceIntent, Const.INTENT_REQUEST_PICTURE );
                break;
        }
    }

    private void validasiMSISDN(){
        pdialog.show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                msisdn_for_firebase,             // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void showSmsValidationForm(){
        //lbInfoSms
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
                InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(tPinEntry, 0);
            }
        },200);

    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verificaiton without
            //     user action.
            Log.d(Const.TAG, "onVerificationCompleted:" + credential);
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            pdialog.dismiss();
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(Const.TAG, "onVerificationFailed", e);

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
                Toast.makeText(getBaseContext(),e.getMessage() + "",Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
                Toast.makeText(getBaseContext(),e.getMessage() + "",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getBaseContext(),e.getMessage() + "",Toast.LENGTH_SHORT).show();
            }

            // Show a message and update the UI
            // ...

        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
            pdialog.dismiss();
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(Const.TAG, "onCodeSent:ID|" + verificationId);
            Log.d(Const.TAG, "onCodeSent:Token|" + token);

            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;
            // ...
            showSmsValidationForm();
        }
    };

    private String urlParametersRegistrasi;
    private void validasiData(){
        if (!tFullname.getText().toString().matches("") &&
                !tEmail.getText().toString().matches("") &&
                !tMsisdn.getText().toString().matches("") &&
                !tAlamat.getText().toString().matches("") &&
                !tUsername.getText().toString().matches("") &&
                !tPassword.getText().toString().matches("") &&
                !tKTP.getText().toString().matches("")
                ) {
            File ktp = new File(Const.IMAGE_PATH, "tmp_fotoktp.jpg");
            File wajah = new File(Const.IMAGE_PATH, "tmp_fotoface.jpg");

            if (!ktp.exists()){
                General.alertOK("Masukkan Foto KTP!", this);
                return;
            }
            if (!wajah.exists()){
                General.alertOK("Masukkan Foto Wajah!", this);
                return;
            }

            urlParametersRegistrasi = new ParameterHttpPost()
                    .val("fullname", tFullname.getText().toString())
                    .val("email", tEmail.getText().toString())
                    .val("msisdn", tMsisdn.getText().toString())
                    .val("alamat", tAlamat.getText().toString())
                    .val("username", tUsername.getText().toString())
                    .val("password", tPassword.getText().toString())
                    .val("card_id", tKTP.getText().toString())
                    .val("provinsi_id", listProvinsi.getCode(spnProvinsi.getSelectedItemPosition()))
                    .val("kabupaten_id", listKabupaten.getCode(spnKota.getSelectedItemPosition()))
                    .build();
            mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "RegistrasiValidasi", urlParametersRegistrasi, new HttpConnection.OnTaskFinishListener() {
                @Override
                public void onStart() {
                    btnRegister.startProgress();
                    Interface.hideKeyboard(btnRegister);
                }

                @Override
                public void onFinished(String jsonString, HttpConnection.Error err) {
                    btnRegister.stopProgress();
                    if (err == null) {
                        ContentJson cj = new ContentJson(jsonString);
                        if (cj.getInt("status") == 1) {
                            msisdn_for_firebase = cj.get("data").getString("validasi_sms");
                            validasiMSISDN();
                        } else {
                            General.alertOK(cj.getString("message"), Registrasi.this);
                        }
                    } else {
                        Toast.makeText(getBaseContext(), err.Message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mAuthTask.execute();
        }else{
            General.alertOK("Semua informasi harus diisi!", this);
        }
    }

    private void registrasiData(){
        mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "Registrasi", urlParametersRegistrasi, new HttpConnection.OnTaskFinishListener() {
            @Override
            public void onStart() {
                Interface.hideKeyboard(btnRegister);
                pdialog.show();
            }

            @Override
            public void onFinished(String jsonString, HttpConnection.Error err) {
                pdialog.dismiss();
                if (err == null) {
                    ContentJson cj = new ContentJson(jsonString);
                    if (cj.getInt("status") == 1) {
                        final String id = cj.get("data").getString("id");
                        General.alertOK(cj.getString("message"), Registrasi.this, new General.OnButtonClick() {
                            @Override
                            public void onClick(int button) {
                                //format file [action]_[id]_[type]_.jpg
                                File ktp = new File(Const.IMAGE_PATH, "tmp_fotoktp.jpg");
                                File wajah = new File(Const.IMAGE_PATH, "tmp_fotoface.jpg");

                                ktp.renameTo(new File(Const.IMAGE_PATH, "reg_" + id + "_ktp_.jpg"));
                                wajah.renameTo(new File(Const.IMAGE_PATH, "reg_" + id +"_face_.jpg"));

                                //upload image
                                Intent iKtp = new Intent(Registrasi.this, UploadFoto.class);
                                iKtp.putExtra("filename", "reg_" + id + "_ktp_.jpg");
                                startService(iKtp);

                                Intent iFace = new Intent(Registrasi.this, UploadFoto.class);
                                iFace.putExtra("filename", "reg_" + id + "_face_.jpg");
                                startService(iFace);

                                login();
                            }
                        });
                    } else {
                        General.alertOK(cj.getString("message"), Registrasi.this);
                    }
                } else {
                    Toast.makeText(getBaseContext(), err.Message, Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAuthTask.execute();
    }

    private void login(){
        String urlParameters = new ParameterHttpPost()
                .val("email", tUsername.getText().toString())
                .val("password", tPassword.getText().toString())
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
                    } else {
                        Toast.makeText(getBaseContext(), cj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), err.Message, Toast.LENGTH_SHORT).show();
                }
                mAuth.signOut();
                setResult(RESULT_OK);
                finish();
            }
        });
        mAuthTask.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.storage.removeDataStartWith("kabupaten");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    pdialog.dismiss();
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(Const.TAG, "signInWithCredential:success");
                        //FirebaseUser user = task.getResult().getUser();
                        registrasiData();
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(Const.TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            General.alertOK("Kode yang dimasukkan salah.", Registrasi.this);
                        }
                    }
                }
            });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.INTENT_REQUEST_PICTURE){
            if (resultCode == Activity.RESULT_OK) {
                Intent iCrop = new Intent(this, CropPicture.class);
                iCrop.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                iCrop.setData(Uri.fromFile(new File(Const.IMAGE_PATH, "tmp.jpg")));
                if (btnClicked.getId() == R.id.btnFotoKtp){
                    iCrop.putExtra("request", "fotoktp");
                }else{
                    iCrop.putExtra("request", "fotoface");
                }
                startActivityForResult(iCrop, Const.INTENT_REQUEST_EDITPROFILE);
            }
        }else if(requestCode == Const.INTENT_REQUEST_EDITPROFILE){
            if (resultCode == Activity.RESULT_OK) {
                Date rDate = new Date();
                if (btnClicked.getId() == R.id.btnFotoKtp){
                    Glide.with(this)
                            .load(Uri.fromFile(new File(Const.IMAGE_PATH, "tmp_fotoktp.jpg")) + "?time" + rDate.getTime())
                            .fitCenter()
                            .crossFade()
                            .into(imgFotoKtp);
                }else{
                    Glide.with(this)
                            .load(Uri.fromFile(new File(Const.IMAGE_PATH, "tmp_fotoface.jpg")) + "?time" + rDate.getTime())
                            .fitCenter()
                            .crossFade()
                            .into(imgFotoWajah);
                }
            }
        }
    }

}
