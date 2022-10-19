package com.example.makeit;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;


public class HomeFragment extends Fragment implements View.OnClickListener {
    ImageView iv_profile;
    Button btn_profile;
    ImageButton btn_nickname;
    TextView tv_nickname;
    Dialog dialog_profile;

    String userNickname, userID;
    private static final String URL_UPLOAD = "http://192.168.75.235/profile_makeit.php";
    private final static String TAG_P = "프로필사진";
    Uri uri; //Uniform resource identifier
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

        //MainActivity에서 전달한 번들 저장
        Bundle bundle = getArguments();

        //번들 안의 텍스트 불러오기
        userID = bundle.getString("userID");
        Log.d("user", userID);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    if(success){
                        String userProfile = jsonObject.getString("userProfile");
                        String userNickname = jsonObject.getString("userNickname");
                        Log.d("user", "userProfile");
                        Log.d("user", "userNickname");
                        Bitmap bitmapProfile = StringToBitmap(userProfile);
                        Glide.with(getActivity().getApplicationContext())
                                .asBitmap() //Glide가 bitmap을 읽을 수 있도록 도움
                                .load(bitmapProfile)
                                .override(500, 500)
                                .into(iv_profile);
                        tv_nickname.setText(userNickname);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Log.d("user", "5");
        HomeRequest homeRequest = new HomeRequest(userID, responseListener);
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(homeRequest);

        btn_profile.setOnClickListener(new View.OnClickListener(){ //갤러리 실행
            @Override
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                String test_string = intent.toUri(Intent.URI_INTENT_SCHEME);
                Log.d(TAG_P, test_string); //인텐트 정보
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
                Glide.with(getActivity().getApplicationContext())
                        .load(data.getData())
                        .override(500, 500)
                        .into(iv_profile);
                Log.d(TAG_P, data.getData().toString()); //이미지 경로
                //String userProfile = data.getData().toString();
                //uploadPicture(userID, userProfile);
                uri = data.getData();
                try{
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        Bitmap bitmapProfile = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getActivity().getContentResolver(),uri));
                        //bitmap = resize(bitmap);
                        String userProfile = BitmapToString(bitmapProfile);
                        //Log.d("비트맵", String.valueOf(uri));
                        Response.Listener<String> responseListener = new Response.Listener<String>() { //이 메소드에서 이미지 인코딩에 썼던 데이터를 다 가져옴

                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    boolean success = jsonObject.getBoolean("success");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    return;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        ProfileRequest profileRequest = new ProfileRequest(userID, userProfile, responseListener);
                        RequestQueue queue = Volley.newRequestQueue(getContext());
                        queue.add(profileRequest);
                    }
                }catch (Exception e){
                    Log.d("이미지 인코딩 오류",e.toString());
                }
            }
        }
    }

    //String형을 BitMap으로 변환시켜주는 함수
    public static String BitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);
        byte[] bytes = baos.toByteArray();
        String image = Base64.encodeToString(bytes, Base64.DEFAULT);
        return image;
    }

    //이미지 String형을 Bitmap으로 변환시켜주는 함수
    public static Bitmap StringToBitmap(String image) {
        try {
            byte[] encodeByte = Base64.decode(image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    //이미지의 크기가 너무 크면 오류가 나기 때문에 리사이징이 필요
    private Bitmap resize(Bitmap bm){ //이미지의 크기가 너무 크면 오류가 나기 때문에 리사이징이 필요
        Configuration config=getResources().getConfiguration();
        if(config.smallestScreenWidthDp>=800)
            bm = Bitmap.createScaledBitmap(bm, 400, 240, true);
        else if(config.smallestScreenWidthDp>=600)
            bm = Bitmap.createScaledBitmap(bm, 300, 180, true);
        else if(config.smallestScreenWidthDp>=400)
            bm = Bitmap.createScaledBitmap(bm, 200, 120, true);
        else if(config.smallestScreenWidthDp>=360)
            bm = Bitmap.createScaledBitmap(bm, 180, 108, true);
        else
            bm = Bitmap.createScaledBitmap(bm, 160, 96, true);
        return bm;
    }

    public void showDialog_change(){
        EditText et_nickname_enter = dialog_profile.findViewById(R.id.et_nickname_enter);

        dialog_profile.show();

        Button btn_change = dialog_profile.findViewById(R.id.btn_change);
        Button btn_cancel = dialog_profile.findViewById(R.id.btn_cancel);
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userNickname = et_nickname_enter.getText().toString();
                Log.d("user", userNickname);
                dialog_profile.dismiss();
                tv_nickname.setText(userNickname);
                Toast.makeText(getActivity(), "수정되었습니다.", Toast.LENGTH_SHORT).show();

                Response.Listener<String> responseListener = new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                NicknameRequest nicknameRequest = new NicknameRequest(userID, userNickname, responseListener);
                RequestQueue queue = Volley.newRequestQueue(getContext());
                queue.add(nicknameRequest);
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