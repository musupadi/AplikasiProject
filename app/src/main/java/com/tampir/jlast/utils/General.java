package com.tampir.jlast.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.tampir.jlast.App;
import com.tampir.jlast.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;


/**
 * Created by rahmatul on 6/18/16.
 */
public class General {
    public static String K_RC4(){
        StringBuilder K = new StringBuilder();
        int[] pos = {10,0,15,11,16,7,5,18,4,0};
        for (int i=0;i<10;i++){
            K.append((char) (70+pos[i]+(i % 2)));
        }
        return K.toString();
    }
    public static String K_DB(){
        StringBuilder K = new StringBuilder();
        K.append("japp");
        for (int i=0;i<10;i++){
            K.append((char) 65+i);
        }
        return K.toString();
    }
    public static int V_DB(){
        return App.DbVersi;
    }
    public final static String md5(String s) {
        final String MD5 = "MD5";
        try {
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public final static String md5(String s, boolean raw) {
        final String MD5 = "MD5";
        try {
            if (s==null) s="";
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                hexString.append((char) (0xFF & aMessageDigest));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void ToastAlert(Context context, String msg){
        Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
    }

    public static int dpToPx(int dp){
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static void clickify(TextView view, String clickableText, OnClickListener listener, int color) {
        CharSequence text = view.getText();
        String string = text.toString();
        ClickSpan span = new ClickSpan(listener,color);

        int start = string.indexOf(clickableText);
        int end = start + clickableText.length();
        if (start == -1) return;

        if (text instanceof Spannable) {
            ((Spannable)text).setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            SpannableString s = SpannableString.valueOf(text);
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            view.setText(s);
        }

        MovementMethod m = view.getMovementMethod();
        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            view.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public static void setStatusTransparent(Activity context, boolean transparent){
        if (transparent) {
            setStatusTransparent(context, Color.TRANSPARENT);
        }else{
            setStatusTransparent(context, ContextCompat.getColor(context, R.color.colorPrimary));
        }
    }
    public static void setStatusTransparent(Activity context, int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = context.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    public static void alertOKCancel(String msg, Context context){
        alertOKCancel(msg, context, null);
    }
    public static void alertOKCancel(String msg, Context context,  final OnButtonClick callback){
        new AlertDialog.Builder(context)
                //.setTitle("Error")
                .setMessage(msg)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int arg1) {
                        if (callback!=null) callback.onClick(AlertDialog.BUTTON_POSITIVE);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int arg1) {
                        if (callback!=null) callback.onClick(AlertDialog.BUTTON_NEGATIVE);
                        dialog.dismiss();
                    }
                })
                .show();
    }
    public static void alertOK(String msg, Context context){
        alertOK(msg, context, null);
    }
    public static void alertOK(String msg, Context context,  final OnButtonClick callback){
        new AlertDialog.Builder(context)
                //.setTitle("Error")
                .setMessage(msg)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int arg1) {
                        if (callback!=null) callback.onClick(AlertDialog.BUTTON_POSITIVE);
                        dialog.dismiss();
                    }
                })
                .show();
    }
    public static void alertGreetSuccess(final Context context){
        int pad = General.dpToPx(16);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LibFunction.dpToPx(96));
        lParams.setMargins(pad,0,pad,0);

        ContentJson configure = App.storage.getData(Storage.ST_CONFIG).get("data");
        final TextView vTextView = new TextView(context);
        vTextView.setLayoutParams(lParams);
        vTextView.setText(configure.getString("jumlah_greet") + " Greet Success");

        vTextView.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary));
        vTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        vTextView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        vTextView.setTypeface(null, Typeface.BOLD);

        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        view.addView(vTextView);

        final AlertDialog dg = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();
                dg.show();

        YoYo.with(Techniques.BounceIn)
                .duration(1000)
                .playOn(vTextView);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        dg.dismiss();
                    }
                },
                2000);

    }
    public interface OnButtonClick {
        void onClick(int button);
    }

    public static File[] fileGetPath(final String starWith){
        File[] files = null;
        if (Const.IMAGE_PATH.exists()){
            if (Const.IMAGE_PATH.isDirectory()){
                files = Const.IMAGE_PATH.listFiles(
                        new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                if (name.startsWith(starWith)){
                                    return true;
                                }
                                return false;
                            }
                        }
                );
            }
        }
        if (files!=null){
            Arrays.sort(files, new Comparator<File>() { //sort asc
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });
        }
        return files;
    }

    public static void fileAllRename(final String fromname, final String toname){
        File[] files;
        if (Const.IMAGE_PATH.exists()){
            if (Const.IMAGE_PATH.isDirectory()){
                files = Const.IMAGE_PATH.listFiles(
                        new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                if (name.startsWith(fromname)){
                                    return true;
                                }
                                return false;
                            }
                        }
                );
                for (File file : files){
                    file.renameTo(new File(Const.IMAGE_PATH, file.getName().replaceFirst(fromname,toname)));
                }
            }
        }
    }

    public static String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }

    public static Spanned fromHtml(String html){
        Spanned result;
        // if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        //      result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        // } else {
        result = Html.fromHtml(html);
        // }
        return result;
    }
}
