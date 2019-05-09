package com.hongzhenw.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hongzhenw.androidlock9view.R;


public class MainActivity extends AppCompatActivity {

    private Lock9View mLock9View;
    private TextView mTvReset;
    private String pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initEvent();
    }

    private void initEvent() {
        mLock9View.setLock9ViewListener(new Lock9View.ILock9ViewListener() {

            @Override
            public void onSuccess(String password) {
                Toast.makeText(MainActivity.this, MainActivity.this.pwd != null ? "登录成功" : "设置成功", Toast.LENGTH_SHORT).show();
                pwd = password;
            }

            @Override
            public boolean comparePassWord(String password) {
                return pwd != null && pwd.equals(password);
            }

            @Override
            public boolean isSettingPassWord() {
                return pwd == null;
            }

            @Override
            public void onFailed(int passwordLength) {
                Toast.makeText(MainActivity.this, passwordLength < 4 ? "请至少连接4个圆" : "登录失败", Toast.LENGTH_SHORT).show();
            }
        });

        mTvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLock9View.reset();
            }
        });

    }

    private void initView() {
        mTvReset = findViewById(R.id.tv_reset);
        mLock9View = findViewById(R.id.lock9view);
    }

}