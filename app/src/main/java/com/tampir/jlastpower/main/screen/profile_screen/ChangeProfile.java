package com.tampir.jlastpower.main.screen.profile_screen;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.tampir.jlastpower.App;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.main.adapter.cacheSpinner;
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
import dmax.dialog.SpotsDialog;

public class ChangeProfile extends BaseFragment {
    View fragment;
    HttpConnection.Task mAuthTask = null;
    SpotsDialog pdialog;

    @BindView(R.id.spnProvinsi) Spinner spnProvinsi;
    @BindView(R.id.spnKota) Spinner spnKota;
    @BindView(R.id.inputFullname) EditText tFullname;
    @BindView(R.id.inputEmail) EditText tEmail;
    @BindView(R.id.inputMsisdn) EditText tMsisdn;
    @BindView(R.id.inputAlamat) EditText tAlamat;
    @BindView(R.id.inputKTP) EditText tKTP;
    @BindView(R.id.btnSave) ButtonProgress btnSave;

    cacheSpinner listProvinsi;
    cacheSpinner listKabupaten;
    private int selected_provinsi = 0;
    private int selected_kabupaten = 0;
    private ContentJson user = App.storage.getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleBar("Ubah Profile");
        setSubTitleBar("Profile");
        if (fragment==null){
            fragment = inflater.inflate(R.layout.main_screen_profile_changeprofile, null);
            ButterKnife.bind(this,fragment);

            String media = General.readTextFile(getResources().openRawResource(getResources().getIdentifier("provinsi", "raw", getActivity().getPackageName())));
            ContentJson data = new ContentJson(media);

            listProvinsi = new cacheSpinner();
            int i = 0;
            while (data.get("data",i)!=null){
                if (data.get("data",i).getString("id").matches(user.getString("provinsi_id"))) selected_provinsi = i;
                listProvinsi.add(data.get("data",i).getString("id"),data.get("data",i).getString("value"));
                i++;
            }

            pdialog = new SpotsDialog(getContext());

            ArrayAdapter<String> adapterProvinsi = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, listProvinsi.getList());
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
                                    Toast.makeText(getContext(), err.Message, Toast.LENGTH_SHORT).show();
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

            tAlamat.setFilters(new InputFilter[] {new InputFilter.AllCaps(),new InputFilter.LengthFilter(250)});
            tFullname.setFilters(new InputFilter[] {new InputFilter.AllCaps(),new InputFilter.LengthFilter(30)});
            showData();

            tKTP.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        checkKTP();
                    }
                }
            });
        }
        return fragment;
    }

    String strKabupaten = "";
    String strProvinsi = "";
    private void checkKTP(){
        strProvinsi = tKTP.getText().toString().substring(0,2);
        strKabupaten = tKTP.getText().toString().substring(0,4);
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
            if (data.get("data",i).getString("id").matches(user.getString("kabupaten_id"))) selected_kabupaten = i;
            listKabupaten.add(data.get("data",i).getString("id"),data.get("data",i).getString("value"));
            if (data.get("data",i).getString("id").matches(strKabupaten)) i_selected = i;
            i++;
        }

        ArrayAdapter<String> adapterKabupaten = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, listKabupaten.getList());
        adapterKabupaten.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnKota.setAdapter(adapterKabupaten);
        spnKota.setSelection(selected_kabupaten);
        spnKota.setSelection(i_selected);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.storage.removeDataStartWith("kabupaten");
    }

    private void showData(){
        tFullname.setText(user.getString("name"));
        tEmail.setText(user.getString("email"));
        tMsisdn.setText(user.getString("msisdn"));
        tAlamat.setText(user.getString("address"));
        tKTP.setText(user.getString("ktp"));
        spnProvinsi.setSelection(selected_provinsi);
    }

    @OnClick({R.id.btnSave})
    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.btnSave:
                if (!validasiData()){
                    General.alertOK("Semua informasi harus diisi!", getContext());
                }else{
                    createDialogPassword();
                }
                break;
        }
    }

    private boolean validasiData(){
        if (!tFullname.getText().toString().matches("") &&
                !tEmail.getText().toString().matches("") &&
                !tMsisdn.getText().toString().matches("") &&
                !tAlamat.getText().toString().matches("") &&
                !tKTP.getText().toString().matches("")
                ) {
            return true;
        }else{
            return false;
        }
    }

    private void createDialogPassword(){
        int pad = General.dpToPx(16);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lParams.setMargins(pad,0,pad,0);

        final EditText vEdtPassword = new EditText(getContext());
        vEdtPassword.setMaxLines(1);
        vEdtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        vEdtPassword.setHint("Kata Sandi");
        vEdtPassword.setLayoutParams(lParams);

        LinearLayout view = new LinearLayout(getContext());
        view.setOrientation(LinearLayout.VERTICAL);
        view.addView(vEdtPassword);
        final AlertDialog dgConfirm = new AlertDialog.Builder(view.getContext())
                .setTitle("Ubah Profile")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Proses",
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int arg1) {
                                String urlParametersRegistrasi = new ParameterHttpPost()
                                        .val("id", user.getString("id"))
                                        .val("fullname", tFullname.getText().toString())
                                        .val("email", tEmail.getText().toString())
                                        .val("msisdn", tMsisdn.getText().toString())
                                        .val("card_id", tKTP.getText().toString())
                                        .val("alamat", tAlamat.getText().toString())
                                        .val("password", vEdtPassword.getText().toString())
                                        .val("provinsi_id", listProvinsi.getCode(spnProvinsi.getSelectedItemPosition()))
                                        .val("kabupaten_id", listKabupaten.getCode(spnKota.getSelectedItemPosition()))
                                        .build();
                                mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "EditProfile", urlParametersRegistrasi, new HttpConnection.OnTaskFinishListener() {
                                    @Override
                                    public void onStart() {
                                        btnSave.startProgress();
                                        Interface.hideKeyboard(vEdtPassword);
                                        tFullname.setEnabled(false);
                                        tEmail.setEnabled(false);
                                        tMsisdn.setEnabled(false);
                                        tAlamat.setEnabled(false);
                                        spnProvinsi.setEnabled(false);
                                        spnKota.setEnabled(false);
                                        tKTP.setEnabled(false);
                                    }

                                    @Override
                                    public void onFinished(String jsonString, HttpConnection.Error err) {
                                        btnSave.stopProgress();
                                        tFullname.setEnabled(true);
                                        tEmail.setEnabled(true);
                                        tMsisdn.setEnabled(true);
                                        tAlamat.setEnabled(true);
                                        spnProvinsi.setEnabled(true);
                                        spnKota.setEnabled(true);
                                        tKTP.setEnabled(true);
                                        if (err == null) {
                                            ContentJson cj = new ContentJson(jsonString);
                                            if (cj.getInt("status") == 1) {
                                                //App.storage.removeCurrentUser();
                                                //App.storage.setCurrentUser(cj.getString("data"));
                                                App.storage.setData("active", "is_updateuser");
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
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int arg1) {
                        Interface.hideKeyboard(vEdtPassword);
                        dialog.dismiss();
                    }
                })
                .create();
        dgConfirm.show();
        vEdtPassword.requestFocus();
        vEdtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dgConfirm.getButton(dgConfirm.BUTTON_POSITIVE).setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        if (vEdtPassword.getText().toString().trim().length()==0) dgConfirm.getButton(dgConfirm.BUTTON_POSITIVE).setEnabled(false);
    }
}
