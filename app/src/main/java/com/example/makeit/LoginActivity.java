package com.example.makeit;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kakao.sdk.user.UserApiClient;
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.NidOAuthLogin;
import com.navercorp.nid.oauth.OAuthLoginCallback;
import com.navercorp.nid.oauth.view.NidOAuthLoginButton;
import com.navercorp.nid.profile.NidProfileCallback;
import com.navercorp.nid.profile.data.NidProfileResponse;

import org.json.JSONException;
import org.json.JSONObject;



public class LoginActivity extends AppCompatActivity {
    //구글 로그인 초기화-----------------------------------------------------------------------------------------------------------
    private SignInButton btn_google; //구글 로그인 버튼
    private FirebaseAuth auth; //파이어베이스 인증 객체
    private GoogleSignInClient googleSignInClient; //구글 API 클라이언트 객체
    private static final int RC_SIGN_IN = 9001; //구글 로그인 결과 코드
    private final static String TAG_G = "google";
    //--------------------------------------------------------------------------------------------------------------------------

    //카카오 로그인 초기화---------------------------------------------------------------------------------------------------------
    private ImageButton btn_kakao;
    String token;
    private final static String TAG_K = "kakao";
    //--------------------------------------------------------------------------------------------------------------------------

    //네이버 로그인 초기화---------------------------------------------------------------------------------------------------------
    NidOAuthLoginButton btn_naver;
    private final static String TAG_N = "naver";
    //--------------------------------------------------------------------------------------------------------------------------

    //MySQL DB 사용-------------------------------------------------------------------------------------------------------------
    EditText et_id, et_password;
    Button btn_login, btn_regist;
    private AlertDialog dialog;
    //--------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //액션바 숨기기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //구글 로그인 onCreate----------------------------------------------------------------------------------------------------
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        auth = FirebaseAuth.getInstance(); //파이어베이스 인증 객체 초기화

        //다음 번에 앱 실행 시 바로 메인화면 실행
        /*if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
            finish();
        }*/

        btn_google = findViewById(R.id.btn_google);
        btn_google.setSize(SignInButton.SIZE_STANDARD);

        btn_google.setOnClickListener(new View.OnClickListener() { //구글 로그인 버튼 클릭
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        //----------------------------------------------------------------------------------------------------------------------

        //카카오 로그인 onCreate--------------------------------------------------------------------------------------------------
        //카카오 해시키 값 구하기 (import com.kakao.sdk.common.util.Utility; 필요)
        /*String keyHash = Utility.INSTANCE.getKeyHash(this);
        Log.e(TAG_K, keyHash);*/

        btn_kakao = findViewById(R.id.btn_kakao);
        btn_kakao.setOnClickListener(new View.OnClickListener() { //카카오 로그인 버튼 클릭
            @Override
            public void onClick(View v) {
                Log.d(TAG_K, "카카오 로그인 버튼 클릭");
                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)){
                    login();
                    Log.d(TAG_K, "카카오톡 앱으로 로그인 시작");
                }
                else {
                    accountLogin();
                    Log.d(TAG_K, "카카오톡 웹으로 로그인 시작");
                }
                Log.d(TAG_K, "카카오 로그인에서 다음으로 진행");
            }
        });
        //----------------------------------------------------------------------------------------------------------------------

        //네이버 로그인 onCreate--------------------------------------------------------------------------------------------------
        //네이버 아이디로 객체 초기화
        NaverIdLoginSDK.INSTANCE.initialize(this, getString(R.string.naver_client_id) , getString(R.string.naver_client_secret), getString(R.string.app_name));


        btn_naver = findViewById(R.id.btn_naver);
        btn_naver.setOAuthLoginCallback(new OAuthLoginCallback() { //네이버 로그인 버튼 클릭
            @Override
            public void onSuccess() {
                Log.d(TAG_K, "네이버 로그인 버튼 클릭");
                Log.d(TAG_N, "토큰 정보 : " + NaverIdLoginSDK.INSTANCE.getAccessToken());
                naver_profile();
                Log.d(TAG_K, "네이버 로그인에서 다음으로 진행");
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
                Log.d(TAG_K, "네이버 로그인 버튼 클릭");
                Log.d(TAG_N, "네이버 로그인 실패" + s);
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.d(TAG_K, "네이버 로그인 버튼 클릭");
                Log.d(TAG_N, "네이버 로그인 에러" + s);
            }
        });
        //----------------------------------------------------------------------------------------------------------------------

        //MySQL DB 사용----------------------------------------------------------------------------------------------------------
        et_id = findViewById(R.id.et_id);
        et_password = findViewById(R.id.et_password);

        btn_login = findViewById(R.id.btn_login);
        btn_regist = findViewById(R.id.btn_regist);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //EditText에 현재 입력되어있는 값을 가져오기
                String userID = et_id.getText().toString();
                String userPW = et_password.getText().toString();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Response.Listener<String> responseListener = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        //Log.d("login", "로그인 진입");
                                        JSONObject jsonObject = new JSONObject(response);
                                        //Log.d("login", "DB 접근");
                                        boolean success = jsonObject.getBoolean("success");
                                        //Log.d("login", "DB 로그인 허가");
                                        if (success) { //로그인 성공
                                            String userID = jsonObject.getString("userID");
                                            String userPW = jsonObject.getString("userPW");

                                            Log.d("login", userID);
                                            Log.d("login", userPW);

                                            Toast.makeText(getApplicationContext(),"로그인하였습니다.",Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.putExtra("login", userID);
                                            intent.putExtra("login", userPW);
                                            startActivity(intent);
                                        } else { //로그인 실패
                                            AlertDialog.Builder builder=new AlertDialog.Builder( LoginActivity.this );
                                            dialog=builder.setMessage("아이디와 비밀번호를 다시 입력해주세요.")
                                                    .setPositiveButton("확인",null)
                                                    .create();
                                            dialog.show();
                                            return;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            LoginRequest loginRequest = new LoginRequest(userID, userPW, responseListener);
                            RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                            queue.add(loginRequest);
                        }
                        catch (Exception e){
                            System.out.println(e);
                        }
                    }
                });
                thread.start();
                try{
                    thread.join();
                }
                catch (Exception e){
                    System.out.println(e);
                }
            }
        });

        btn_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //회원가입 버튼 클릭
                Intent intent_join = new Intent(LoginActivity.this, RegistActivity.class);
                startActivity(intent_join);
                //finish();
            }
        });
        //----------------------------------------------------------------------------------------------------------------------
    }

    //구글 로그인 함수-------------------------------------------------------------------------------------------------------------
    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //signInIntent에서 생성된 결과값이 리턴됨
        if (requestCode == RC_SIGN_IN) { //requestCode를 받은 경우
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //구글 로그인이 성공한 경우, 파이어베이스로 토큰 인증
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG_G, "토큰 정보 : " + account.getId());
                //GoogleSignInAccount 객체에서 ID 토큰을 가져와서 firebaseAuthWithGoogle 함수로 전달
                firebaseAuthWithGoogle(account.getIdToken(), account);
            } catch (ApiException e) {
                //구글 로그인이 실패한 경우, UI를 즉시 업데이트
                Log.d(TAG_G, "구글 로그인 실패(토큰 미전달)");
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken, GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //구글 로그인에 성공했을 경우
                            //Toast.makeText(LoginActivity.this,"구글 로그인 성공",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            intent.putExtra("사용자 이름 : ", account.getDisplayName());
                            intent.putExtra("사용자 이메일 : ", account.getEmail());
                            Log.d(TAG_G, "사용자 이름 : " + account.getDisplayName());
                            Log.d(TAG_G, "사용자 이메일 : " + account.getEmail());
                            startActivity(intent);
                        } else {
                            //구글 로그인에 실패했을 경우
                            Log.d(TAG_G, "구글 로그인 실패(토큰 정보 업데이트 완료)");
                            //Toast.makeText(LoginActivity.this,"구글 로그인 실패",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    //--------------------------------------------------------------------------------------------------------------------------

    //카카오 로그인 함수-----------------------------------------------------------------------------------------------------------
    public void login(){ //카카오톡 앱이 설치된 경우
        UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this,(oAuthToken, error) -> {
            if(error != null){
                Log.e(TAG_K, "앱 로그인 실패", error);
            }
            else if(oAuthToken != null){
                Log.i(TAG_K, "앱 로그인 성공(토큰): " + oAuthToken.getAccessToken());
                token = oAuthToken.getAccessToken();
                getUserInfo();
            }
            return null;
        });
    }
    public void accountLogin(){ //카카오톡 앱 미설치된 경우
        UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this,(oAuthToken, error) -> {
            if(error != null){
                Log.e(TAG_K, "웹 로그인 실패", error);
            }
            else if(oAuthToken != null){
                Log.i(TAG_K, "웹 로그인 성공(토큰): " + oAuthToken.getAccessToken());
                token = oAuthToken.getAccessToken();
                getUserInfo();
            }
            return null;
        });
    }
    public void getUserInfo(){
        UserApiClient.getInstance().me((user, meError)->{
            if(meError != null){
                Log.e(TAG_K, "사용자 정보 요청 실패", meError);
            }
            else{
                Log.d(TAG_K, "사용자 정보 요청 후 로그인 완료");
                Log.d(TAG_K, "사용자 정보 : " +user.getId());
                Log.d(TAG_K, "사용자 이메일 : " +user.getKakaoAccount().getEmail());
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
            return null;
        });
    }
    //--------------------------------------------------------------------------------------------------------------------------

    //네이버 로그인 함수-----------------------------------------------------------------------------------------------------------
    private void naver_profile() {
        NidOAuthLogin authLogin = new NidOAuthLogin();
        authLogin.callProfileApi(new NidProfileCallback<NidProfileResponse>() {
            @Override
            public void onSuccess(NidProfileResponse res) {
                Log.d(TAG_N,"사용자 정보 업데이트 후 네이버 로그인 완료");
                Log.d(TAG_N, "사용자 이름 : " + res.getProfile().getName());
                Log.d(TAG_N, "사용자 이메일 : " + res.getProfile().getEmail());

                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra("사용자 이름 : ", res.getProfile().getName());
                intent.putExtra("사용자 이메일 : ", res.getProfile().getEmail());
                startActivity(intent);
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
                Log.d(TAG_N, "onFailure: " + s);
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.d(TAG_N, "onError: "  + s);
            }
        });
    }
    //--------------------------------------------------------------------------------------------------------------------------


}
