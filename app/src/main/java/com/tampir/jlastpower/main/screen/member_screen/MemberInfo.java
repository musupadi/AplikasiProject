package com.tampir.jlastpower.main.screen.member_screen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.main.screen.BaseContainerFragment;
import com.tampir.jlastpower.main.screen.BaseFragment;
import com.tampir.jlastpower.utils.ContentJson;
import com.tampir.jlastpower.utils.General;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MemberInfo extends BaseFragment {
    View fragment;
    @BindView(R.id.lb_membername) TextView lbMembername;
    @BindView(R.id.lb_memberid) TextView lbMemberCode;
    @BindView(R.id.imgPreview) ImageView imgPreview;

    public static MemberInfo instance(ContentJson member) {
        MemberInfo fg = new MemberInfo();
        Bundle bundle = new Bundle();
        bundle.putString("member", member.toString());
        fg.setArguments(bundle);
        return fg;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentJson json = new ContentJson(getArguments().getString("member", ""));
        setTitleBar(json.getString("fullname"));
        setSubTitleBar("Info");
        if (fragment==null){
            fragment = inflater.inflate(R.layout.main_screen_member_info, null);
            ButterKnife.bind(this,fragment);

            lbMemberCode.setText(json.getString("member_code"));
            lbMembername.setText(json.getString("fullname"));

            ViewCompat.setTransitionName(imgPreview, "imgTrans" + json.getString("position_item"));
            Glide.with(imgPreview.getContext())
                    .load(json.getString("foto"))
                    .fitCenter()
                    .error(R.drawable.jimage)
                    .into(imgPreview);
        }
        return fragment;
    }

    @OnClick({R.id.btnPrivateChat, R.id.btnCall})
    public void buttonClick(View view) {
        final ContentJson member = new ContentJson(getArguments().getString("member", ""));
        switch (view.getId()) {
            case R.id.btnPrivateChat:
                ((BaseContainerFragment) getParentFragment()).replaceFragment(MemberChat.instance(member), true);
                break;
            case R.id.btnCall:
                General.alertOKCancel("Telepon " + member.getString("fullname"), getContext(), new General.OnButtonClick(){
                    @Override
                    public void onClick(int button) {
                        if (button == AlertDialog.BUTTON_POSITIVE) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + member.getString("msisdn")));
                            getActivity().startActivity(intent);
                        }
                    }
                });
                break;
        }
    }

}
