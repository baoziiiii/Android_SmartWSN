package com.example.qq452651705.Account;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.qq452651705.Global.MyLog;
import com.example.qq452651705.R;
import com.example.qq452651705.SharedPreferences.Config;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Response;

public class LoginAcitivity extends AppCompatActivity {
    private final static String url = "http://47.94.168.153:8080/JsonTest/Login";
    private final static int REQUEST_FOR_REGISTER = 0;
    private final static int REQUEST_FOR_FORGET = 1;

    EditText ET_username;
    EditText ET_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);

        ET_username = findViewById(R.id.username);
        ET_password = findViewById(R.id.password);
        ET_username.setText(Config.getInstance(this).getUsername());
        setTitle("登陆");
        final Button login = findViewById(R.id.bt_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = ET_username.getText().toString();
                final String password = ET_password.getText().toString();
                Callback loginCallback = new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("bwq", "【LoginResponse】:" + "CALL_BACK_ON_FAILURE");
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginAcitivity.this, "无法连接服务器，请检查网络情况", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        final String result = response.body().string();
                        Headers headers = response.headers();
                        Log.e("bwq", "【LoginResponse】:" + result);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (result.equals("SUCCESS")) {
                                        Toast.makeText(LoginAcitivity.this, "登陆成功", Toast.LENGTH_LONG).show();
                                        Config.getInstance(LoginAcitivity.this).setUsername(username);
                                        AccountManager.loginStatus = true;
                                        AccountManager.nowUsername = username;
                                        AccountManager.nowPassword = password;
                                        setResult(RESULT_OK);

                                        Headers headers = response.headers();
                                        List<String> cookies = headers.values("Set-Cookie");
                                        Config config = Config.getInstance(LoginAcitivity.this);
                                        config.setCookies(cookies.get(0));
                                        finish();
                                    } else {
                                        Toast.makeText(LoginAcitivity.this, "登陆失败", Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                };
                AccountManager.getInstance().Login(username, password, loginCallback);
            }
        });

        final Button register = findViewById(R.id.bt_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("intent", "register");
                intent.setClass(LoginAcitivity.this, RegisterActivity.class);
                startActivityForResult(intent, REQUEST_FOR_REGISTER);
            }
        });

        final Button forget = findViewById(R.id.bt_forget);
        forget.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          Intent intent = new Intent();
                                          intent.putExtra("intent", "forget");
                                          intent.setClass(LoginAcitivity.this, RegisterActivity.class);
                                          startActivityForResult(intent, REQUEST_FOR_FORGET);
                                      }
                                  }
        );

        final Button close = findViewById(R.id.login_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_FOR_REGISTER:
                if (resultCode == RESULT_OK) {
                    ET_username = findViewById(R.id.username);
                    ET_username.setText(Config.getInstance(this).getUsername());
                    break;
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
