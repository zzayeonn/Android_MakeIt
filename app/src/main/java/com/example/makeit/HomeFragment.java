package com.example.makeit;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;


public class HomeFragment extends Fragment implements View.OnClickListener {
    ImageView iv_profile;
    Button btn_profile;
    ImageButton btn_nickname;
    TextView tv_nickname;
    Dialog dialog_profile;

    String Nickname;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);

        iv_profile = viewGroup.findViewById(R.id.iv_profile);
        btn_profile = viewGroup.findViewById(R.id.btn_profile);
        btn_nickname = viewGroup.findViewById(R.id.btn_nickname);
        tv_nickname = viewGroup.findViewById(R.id.tv_nickname);

        dialog_profile = new Dialog(getActivity());
        dialog_profile.requestWindowFeature(Window.FEATURE_NO_TITLE); //다이얼로그 타이틀 제거
        dialog_profile.setContentView(R.layout.activity_dialog);

        btn_profile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 0);
            }
        });

        btn_nickname.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showDialog_change();
            }
        });

        return viewGroup;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            if(resultCode == -1) {
                Glide.with(getActivity().getApplicationContext()).load(data.getData()).override(500, 500).into(iv_profile);
            }
        }
    }

    public void showDialog_change(){
        EditText et_nickname_enter = dialog_profile.findViewById(R.id.et_nickname_enter);

        dialog_profile.show();

        Button btn_change = dialog_profile.findViewById(R.id.btn_change);
        Button btn_cancel = dialog_profile.findViewById(R.id.btn_cancel);
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Nickname = et_nickname_enter.getText().toString();
                Log.d("Nickname", Nickname);
                dialog_profile.dismiss();
                tv_nickname.setText(Nickname);
                Toast.makeText(getActivity(), "수정되었습니다.", Toast.LENGTH_SHORT).show();

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                dialog_profile.dismiss();
            }
        });
    }

    @Override
    public void onClick(View view) {

    }
}