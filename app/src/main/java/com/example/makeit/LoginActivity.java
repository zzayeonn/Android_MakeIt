package com.example.makeit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {
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
}
