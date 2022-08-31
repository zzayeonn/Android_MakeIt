package com.example.makeit;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
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
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;


public class LoginActivity extends AppCompatActivity {
    //구글 로그인-----------------------------------------------------------------------------------------------------------------
    private SignInButton btn_google; //구글 로그인 버튼
    private FirebaseAuth auth; //파이어베이스 인증 객체
    private GoogleSignInClient googleSignInClient; //구글 API 클라이언트 객체
    private static final int RC_SIGN_IN = 9001; //구글 로그인 결과 코드
    private final static String TAG_G = "google";
    //--------------------------------------------------------------------------------------------------------------------------

    //카카오 로그인---------------------------------------------------------------------------------------------------------------
    private ImageButton btn_kakao;
    public static Context mContext;
    private SharedPreferences sharedPreferences;
    private User currentUser;
    private String userImageString = "";
    private Bitmap mBitmap;
    SharedPreferences.Editor editor;
    private Boolean isTrue = false;
    private Boolean nextIntent = false;
    private String meetingId;
    private Intent intent;
    private final static String TAG_K = "kakao";
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

        //카카오 해시키 값 구하기 (import com.kakao.sdk.common.util.Utility; 필요)
        /*String keyHash = Utility.INSTANCE.getKeyHash(this);
        Log.e(TAG_K, keyHash);*/

        //구글 로그인-------------------------------------------------------------------------------------------------------------
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
                switch (view.getId()) {
                    case R.id.btn_google:
                        signIn();
                        break;
                }
            }
        });
        //----------------------------------------------------------------------------------------------------------------------

        //카카오 로그인-----------------------------------------------------------------------------------------------------------
        Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                if (oAuthToken != null) {
                    Log.i("user", oAuthToken.getAccessToken() + " " + oAuthToken.getRefreshToken());
                }
                if (throwable != null) {
                    Log.w(TAG_K, "invoke: " + throwable.getLocalizedMessage());
                }
                updateKakaoLoginUi();

                return null;
            }
        };

        btn_kakao = findViewById(R.id.btn_kakao);
        btn_kakao.setOnClickListener(new View.OnClickListener() {   //카카오 로그인 버튼 클릭
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this,"카카오 버튼 클릭",Toast.LENGTH_SHORT).show();

                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)) {
                    //카카오톡이 있을 경우
                    UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this, callback);
                } else {
                    UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, callback);
                }
            }
        });
        updateKakaoLoginUi();
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

    //구글 로그인-----------------------------------------------------------------------------------------------------------------
    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //구글로그인 인텐트에서 생성된 결과값이 리턴됨
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);

        if (requestCode == RC_SIGN_IN) { //requestCode를 받은 경우
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG_G, "firebaseAuthWithGoogle:" + account.getId());
                //GoogleSignInAccount 객체에서 ID 토큰을 가져와서 firebaseAuthWithGoogle함수로 전달
                firebaseAuthWithGoogle(account.getIdToken(), account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG_G, "Google sign in failed", e);
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
                            // Sign in success
                            Toast.makeText(LoginActivity.this,"구글 로그인 성공",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            intent.putExtra("UserName", account.getDisplayName());
                            intent.putExtra("UserEmail", account.getEmail());
                            Log.d(TAG_G, account.getDisplayName());
                            Log.d(TAG_G, account.getEmail());
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this,"구글 로그인 실패",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    //--------------------------------------------------------------------------------------------------------------------------

    //카카오 로그인---------------------------------------------------------------------------------------------------------------
    private void updateKakaoLoginUi() {
        // 카카오 UI 가져오는 메소드 (로그인 핵심 기능)
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null) {
                    // 유저 정보가 정상 전달 되었을 경우
                    Log.i(TAG_K, "id " + user.getId());   // 유저의 고유 아이디를 불러옵니다.
                    Log.i(TAG_K, "invoke: nickname=" + user.getKakaoAccount().getProfile().getNickname());  // 유저의 닉네임을 불러옵니다.
                    Log.i(TAG_K, "userimage " + user.getKakaoAccount().getProfile().getProfileImageUrl());    // 유저의 이미지 URL을 불러옵니다.

                    // 이 부분에는 로그인이 정상적으로 되었을 경우 어떤 일을 수행할 지 적으면 됩니다.
                }
                if (throwable != null) {
                    // 로그인 시 오류 났을 때
                    // 키해시가 등록 안 되어 있으면 오류 납니다.
                    Log.w(TAG_K, "invoke: " + throwable.getLocalizedMessage());
                }
                return null;
            }
        });
    }
    //--------------------------------------------------------------------------------------------------------------------------

}
