package com.tampir.jlastpower.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.lyft.android.scissors.CropView;
import com.tampir.jlastpower.App;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.utils.Const;
import com.tampir.jlastpower.utils.ContentJson;
import com.tampir.jlastpower.utils.HttpConnection;

import java.io.File;
import java.util.Date;

import dmax.dialog.SpotsDialog;
import rx.Observable;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static android.graphics.Bitmap.CompressFormat.JPEG;
import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

public class CropPicture extends AppCompatActivity {
    private MenuItem mnDone;
    private CropView cropView;
    private String request;

    SpotsDialog pdialog;
    HttpConnection.Task mAuthTask = null;

    CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_crop);

        request = getIntent().getExtras().getString("request");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Date rDate = new Date();
        cropView = (CropView) findViewById(R.id.crop_view);
        cropView.extensions().load(getIntent().getData() + "?time" + rDate.getTime());
        pdialog = new SpotsDialog(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        mnDone = menu.findItem(R.id.action_done);
        mnDone.setEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                closeIntent();
                return true;
            case R.id.action_done :
                final File croppedFile = new File(Const.IMAGE_PATH, "temp_crop.jpg");

                Observable<Void> onSave = Observable.from(cropView.extensions()
                        .crop()
                        .quality(100)
                        .format(JPEG)
                        .into(croppedFile))
                        .subscribeOn(io())
                        .observeOn(mainThread());

                subscriptions.add(onSave
                        .subscribe(new Action1<Void>() {
                            @Override
                            public void call(Void nothing) {
                                if (request.matches("profile")) {
                                    uploadFotoProfile(croppedFile);
                                }else if(request.matches("fotoktp")){
                                    croppedFile.renameTo(new File(Const.IMAGE_PATH, "tmp_fotoktp.jpg"));
                                    setResult(RESULT_OK);
                                    finish();
                                }else if(request.matches("fotoface")){
                                    croppedFile.renameTo(new File(Const.IMAGE_PATH, "tmp_fotoface.jpg"));
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            }
                        }));
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    private void uploadFotoProfile(File croppedFile){
        final ContentJson user = App.storage.getCurrentUser();
        croppedFile.renameTo(new File(Const.IMAGE_PATH, user.getString("id") + ".jpg"));

        new HttpConnection.UploadFileToServer(croppedFile,"UploadFotoFileProfile", null, new HttpConnection.OnTaskFinishListener(){
            @Override
            public void onStart() {
                pdialog.show();
            }

            @Override
            public void onFinished(String jsonString, HttpConnection.Error err) {
                pdialog.dismiss();
                if (err==null) {
                    ContentJson cj = new ContentJson(jsonString);
                    if (cj.getInt("status") == 1) {
                        user.put("foto", cj.get("data").getString("foto"));
                        App.storage.removeCurrentUser();
                        App.storage.setCurrentUser(user.toString());
                        setResult(RESULT_OK);
                        finish();
                    }else {
                        Toast.makeText(getBaseContext(), cj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getBaseContext(),err.Message,Toast.LENGTH_SHORT).show();
                }
            }
        }).execute();
    }

    private void alert(String msg){
        alert(msg,"Warning");
    }
    private void alert(String msg,String title){
        new AlertDialog.Builder(CropPicture.this)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            closeIntent();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void closeIntent() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
    }
}
