package com.example.makeit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {
    //파이어베이스----------------------------------------------------------------------------------------------------------------
    private SignInButton btn_google; //구글 로그인 버튼
    private FirebaseAuth auth; //파이어베이스 인증 객체
    private GoogleSignInClient googleSignInClient; //구글 API 클라이언트 객체
    private static final int RC_SIGN_IN = 9001; //구글 로그인 결과 코드
    String TAG = "LoginActivity";
    //--------------------------------------------------------------------------------------------------------------------------


    int version = 1;
    DatabaseOpenHelper helper;
    SQLiteDatabase database;

    EditText idEditText, pwEditText;
    Button btnLogin, btnJoin;

    String sql;
    Cursor cursor;

    SharedPreferences pref; // 프리퍼런스
    SharedPreferences.Editor editor; // 에디터
    TextView tv_name_pre, tv_date_pre;
    String name_pre; // 이전 이름
    String date_pre; // 이전 시간

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //클라이언트 ID "207224749118-logeqhbo1oo4ec24c8c9bvc7itu064l7.apps.googleusercontent.com"
        //파이어베이스------------------------------------------------------------------------------------------------------------
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

        btn_google.setOnClickListener(new View.OnClickListener() { //구글 로그인 버튼 클릭했을 때
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

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

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

    }


    //파이어베이스----------------------------------------------------------------------------------------------------------------
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
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                //GoogleSignInAccount 객체에서 ID 토큰을 가져와서 firebaseAuthWithGoogle함수로 전달
                firebaseAuthWithGoogle(account.getIdToken(), account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
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
                            Log.d(TAG, account.getDisplayName());
                            Log.d(TAG, account.getEmail());
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this,"구글 로그인 실패",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //--------------------------------------------------------------------------------------------------------------------------
}
