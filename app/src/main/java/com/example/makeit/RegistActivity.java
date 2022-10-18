package com.example.makeit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistActivity extends AppCompatActivity {
    //MySQL DB 사용-------------------------------------------------------------------------------------------------------------
    EditText et_idregist, et_pwregist, et_nameregist, et_emailregist;
    Button btn_yesregist, btn_back, btn_checkID;
    private static String userID, userPW, userName, userEmail;
    private boolean overlap = false; //아이디 중복체크 후 버튼 변경을 위해(중복확인 -> 확인완료)
    private AlertDialog dialog;
    //--------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onDestroy(){
        super.onDestroy();
        dialog.dismiss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        //액션바 숨기기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();


        //MySQL DB 사용----------------------------------------------------------------------------------------------------------
        et_idregist = findViewById(R.id.et_idregist);
        et_pwregist = findViewById(R.id.et_pwregist);
        et_nameregist = findViewById(R.id.et_nameregist);
        et_emailregist = findViewById(R.id.et_emailregist);

        btn_yesregist = findViewById(R.id.btn_yesregist);
        btn_back = findViewById(R.id.btn_noregist);
        btn_checkID = findViewById(R.id.btn_checkID);

        //ID 중복체크
        btn_checkID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID=et_idregist.getText().toString();
                if(overlap) //아이디 입력 확인
                {
                    return;
                }
                if(userID.equals("")){ //아이디가 비어있을 경우
                    AlertDialog.Builder builder=new AlertDialog.Builder( RegistActivity.this );
                    dialog=builder.setMessage("아이디를 입력하세요.")
                            .setPositiveButton("확인",null)
                            .create();
                    dialog.show();
                    return;
                }
                Response.Listener<String> responseListener=new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try { //아이디가 사용가능할 경우
                            JSONObject jsonResponse=new JSONObject(response);
                            boolean success=jsonResponse.getBoolean("success");
                            if(success){
                                AlertDialog.Builder builder=new AlertDialog.Builder( RegistActivity.this );
                                dialog=builder.setMessage("사용 가능한 아이디입니다.")
                                        .setPositiveButton("확인",null)
                                        .create();
                                dialog.show();
                                et_idregist.setEnabled(false);
                                overlap=true;
                                btn_checkID.setText("확인완료");
                            }
                            else{ //아이디가 중복일 경우
                                AlertDialog.Builder builder=new AlertDialog.Builder( RegistActivity.this );
                                dialog=builder.setMessage("현재 사용 중인 아이디입니다.")
                                        .setPositiveButton("확인",null)
                                        .create();
                                dialog.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                OverlapRequest overlapRequest = new OverlapRequest(userID,responseListener);
                RequestQueue queue = Volley.newRequestQueue(RegistActivity.this);
                queue.add(overlapRequest);

            }
        });

        //회원가입 버튼 클릭
        btn_yesregist.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Log.d("regist", "버튼 클릭 완료");

                //현재 입력된 정보를 string으로 가져오기
                userID = et_idregist.getText().toString();
                userPW = et_pwregist.getText().toString();
                userName = et_nameregist.getText().toString();
                userEmail = et_emailregist.getText().toString();
                //Log.d("regist", "정보 업로드 완료");

                //isSBSettingEnabled 에러 해결 방법 - 쓰레드 사용
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //회원가입 절차 시작
                            Response.Listener<String> responseListener = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        //Log.d("regist", "회원가입 진입");
                                        //String으로 그냥 못 보냄으로 JSON Object 형태로 변형하여 전송
                                        //서버 통신하여 회원가입 성공 여부를 jsonResponse로 받음
                                        //Log.d("regist", response);
                                        JSONObject jsonResponse = new JSONObject(response);
                                        //Log.d("regist", "DB 접근");
                                        boolean success = jsonResponse.getBoolean("success");
                                        //Log.d("regist", "DB 회원가입 허가");
                                        if (success) { //회원가입 가능
                                            AlertDialog.Builder builder=new AlertDialog.Builder( RegistActivity.this );
                                            dialog=builder.setMessage("축하합니다. 회원가입이 완료되었습니다.")
                                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            Intent intent = new Intent(RegistActivity.this, LoginActivity.class);
                                                            startActivity(intent);
                                                            finish(); //액티비티를 종료
                                                        }
                                                    })
                                                    .create();
                                            dialog.show();
                                        } else { //회원가입 불가능
                                            AlertDialog.Builder builder=new AlertDialog.Builder( RegistActivity.this );
                                            dialog=builder.setMessage("회원가입에 실패했습니다. 다시 시도해 주세요.")
                                                    .setPositiveButton("확인",null)
                                                    .create();

                                            dialog.show();
                                            return;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            Log.d("regist", userID);
                            Log.d("regist", userPW);
                            Log.d("regist", userName);
                            Log.d("regist", userEmail);
                            //Volley 라이브러리를 이용해서 실제 서버와 통신을 구현하는 부분
                            RegisterRequest registerRequest = new RegisterRequest(userID, userPW, userName, userEmail, responseListener);
                            RequestQueue queue = Volley.newRequestQueue(RegistActivity.this);
                            queue.add(registerRequest);
                        }catch (Exception e){
                            System.out.println(e);
                        }
                    }
                });
                thread.start();
                try {
                    thread.join();
                }catch (Exception e){
                    System.out.println(e);
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //취소 버튼 클릭
                Toast toast = Toast.makeText(RegistActivity.this, "로그인 화면으로 돌아갑니다.", Toast.LENGTH_SHORT);
                toast.show();
                finish(); //현재 액티비티 종료
            }
        });
        //----------------------------------------------------------------------------------------------------------------------
    }
}
