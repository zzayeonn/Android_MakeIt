package com.example.makeit;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class RegistActivity extends AppCompatActivity {


    //홈 화면 접근을 위한 임시 로그인 방법(삭제 예정)----------------------------------------------------------------------------------
    int version = 1;
    DatabaseOpenHelper helper;
    SQLiteDatabase database;

    EditText idEditText, pwEditText;
    Button btnJoin, btnBack;

    String sql;
    Cursor cursor;
    //--------------------------------------------------------------------------------------------------------------------------


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        //액션바 숨기기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();


        //홈 화면 접근을 위한 임시 로그인 방법(삭제 예정)------------------------------------------------------------------------------
        idEditText = findViewById(R.id.et_nameregist);
        pwEditText = findViewById(R.id.et_pwregist);

        btnJoin = findViewById(R.id.btn_yesregist);
        btnBack = findViewById(R.id.btn_noregist);

        helper = new DatabaseOpenHelper(RegistActivity.this, DatabaseOpenHelper.tableName, null, version);
        database = helper.getWritableDatabase();

        btnJoin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                String id = idEditText.getText().toString();
                String pw = pwEditText.getText().toString();

                if(id.length() == 0 || pw.length() == 0) {
                    //정보 미입력
                    Toast toast = Toast.makeText(RegistActivity.this, "이름과 핸드폰번호를 입력해주세요.", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                sql = "SELECT id FROM "+ helper.tableName + " WHERE id = '" + id + "'";
                cursor = database.rawQuery(sql, null);

                if(cursor.getCount() != 0){
                    //존재하는 이름입니다.
                    Toast toast = Toast.makeText(RegistActivity.this, "존재하는 이름입니다.", Toast.LENGTH_SHORT);
                    toast.show();
                }else{
                    helper.insertUser(database,id,pw);
                    Toast toast = Toast.makeText(RegistActivity.this, "가입이 완료되었습니다.", Toast.LENGTH_SHORT);
                    toast.show();
                    Intent intent_login = new Intent(RegistActivity.this, LoginActivity.class);
                    startActivity(intent_login);
                    finish();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener(){
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
