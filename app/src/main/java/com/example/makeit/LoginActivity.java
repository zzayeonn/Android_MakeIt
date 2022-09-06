package com.example.makeit;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

//구글 로그인 import
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

//카카오 로그인 import
import com.kakao.sdk.user.UserApiClient;

//네이버 로그인 import
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.NidOAuthLogin;
import com.navercorp.nid.oauth.OAuthLoginCallback;
import com.navercorp.nid.oauth.view.NidOAuthLoginButton;
import com.navercorp.nid.profile.NidProfileCallback;
import com.navercorp.nid.profile.data.NidProfileResponse;


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



    //홈 화면 접근을 위한 임시 로그인 방법(삭제 예정)----------------------------------------------------------------------------------
    int version = 1;
    DatabaseOpenHelper helper;
    SQLiteDatabase database;

    EditText idEditText, pwEditText;
    Button btnLogin, btnJoin;

    String sql;
    Cursor cursor;

    SharedPreferences pref; // 프리퍼런스
    //SharedPreferences.Editor editor; // 에디터
    TextView tv_name_pre, tv_date_pre;
    String name_pre; // 이전 이름
    String date_pre; // 이전 시간
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
        btn_naver.setOAuthLoginCallback(new OAuthLoginCallback() { //카카오 로그인 버튼 클릭
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


        //홈 화면 접근을 위한 임시 로그인 방법(삭제 예정)------------------------------------------------------------------------------
        idEditText = findViewById(R.id.et_name);
        pwEditText = findViewById(R.id.et_password);

        btnLogin = findViewById(R.id.btn_login);
        btnJoin = findViewById(R.id.btn_regist);

        helper = new DatabaseOpenHelper(LoginActivity.this, DatabaseOpenHelper.tableName, null, version);
        database = helper.getWritableDatabase();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = idEditText.getText().toString();
                String pw = pwEditText.getText().toString();

                if (id.length() == 0 || pw.length() == 0) {
                    //정보 미입력
                    Toast toast = Toast.makeText(LoginActivity.this, "이름과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                sql = "SELECT id FROM " + helper.tableName + " WHERE id = '" + id + "'";
                cursor = database.rawQuery(sql, null);

                if (cursor.getCount() != 1) {
                    //이름 오류
                    Toast toast = Toast.makeText(LoginActivity.this, "존재하지 않는 이름입니다.", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                sql = "SELECT pw FROM " + helper.tableName + " WHERE id = '" + id + "'";
                cursor = database.rawQuery(sql, null);

                cursor.moveToNext();
                if (!pw.equals(cursor.getString(0))) {
                    //비밀번호 오류
                    Toast toast = Toast.makeText(LoginActivity.this, "이름이나 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    //로그인 성공, 인텐트 생성 및 호출
                    Toast toast = Toast.makeText(LoginActivity.this, id + "님이 로그인 하셨습니다.", Toast.LENGTH_SHORT);
                    toast.show();
                    Intent intent_login = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent_login);
                    finish();
                }
                cursor.close();
            }
        });

        btnJoin.setOnClickListener(new View.OnClickListener() {
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
