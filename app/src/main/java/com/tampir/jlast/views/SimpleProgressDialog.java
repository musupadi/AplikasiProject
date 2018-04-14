package com.tampir.jlast.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tampir.jlast.R;

/**
 * Dibuat oleh Muhammad Iqbal pada 03/08/17.
 * Mobile Apps Developer
 */

public class SimpleProgressDialog {

    private Activity context;
    private Dialog dialog;
    private TextView mTxtMessage;

    public SimpleProgressDialog(final Activity context, String message) {
        this.context = context;

        /*generate dialog*/
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view     = inflater.inflate(R.layout.layout_progress_dialog, null);
        mTxtMessage = (TextView) view.findViewById(R.id.m_txt_message);
        mTxtMessage.setText(message);
        dialog = new Dialog(context);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }

    public void show() {
        if (context != null && !context.isFinishing())
        dialog.show();
    }

    public void dismiss() {
        if (context != null && !context.isFinishing())
        dialog.dismiss();
    }

    public SimpleProgressDialog isCancelable(boolean flag) {
        dialog.setCancelable(flag);

        return this;
    }


    public void setMessage(String message){
        mTxtMessage.setText(message);
    }
}
