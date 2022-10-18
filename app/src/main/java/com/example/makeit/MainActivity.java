package com.example.makeit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Button btn_logout;
    AlertDialog dialog;
    HomeFragment homefragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //액션바 숨기기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        bottomNavigationView = findViewById(R.id.nav_main);
        btn_logout = findViewById(R.id.btn_logout);

        //아이디 정보 받기
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        Log.d("zn", userID);

        homefragment = new HomeFragment();

        //처음화면
        getSupportFragmentManager().beginTransaction().add(R.id.frame_main, homefragment).commit();
        bottomNavigationView.setSelectedItemId(R.id.item_fragment3);

        //번들객체 생성, userID값 저장
        Bundle bundle = new Bundle();
        bundle.putString("userID",userID);

        //HomeFragment로 번들 전달
        homefragment.setArguments(bundle);



        //바텀 네비게이션뷰 안의 아이템 설정
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_fragment1:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new CameraFragment()).commit();
                        break;
                    case R.id.item_fragment2:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new BoardFragment()).commit();
                        break;
                    case R.id.item_fragment3:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new HomeFragment()).commit();
                        break;
                    case R.id.item_fragment4:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new MapFragment()).commit();
                        break;
                    case R.id.item_fragment5:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new MessageFragment()).commit();
                        break;
                }
                return true;
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("logout", "로그아웃 버튼 클릭");
                AlertDialog.Builder builder=new AlertDialog.Builder( MainActivity.this );
                dialog=builder.setMessage("로그아웃 하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("취소", null)
                        .create();
                dialog.show();
            }
        });
    }
}