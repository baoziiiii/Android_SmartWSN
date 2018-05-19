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


/**
 * 登陆页面
 */

public class LoginAcitivity extends AppCompatActivity {

    //获取注册、忘记密码的请求码
    private final static int REQUEST_FOR_REGISTER = 0;
    private final static int REQUEST_FOR_FORGET = 1;

    //输入框组件
    EditText ET_username;
    EditText ET_password;

    /**
     * 登陆页面初始化
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);

        //设置页面标题
        setTitle("登陆");

        ET_username = findViewById(R.id.username);
        ET_password = findViewById(R.id.password);
        //自动设置上次记录的用户名
        ET_username.setText(Config.getInstance(this).getUsername());

        final Button login = findViewById(R.id.bt_login);
        //登陆按钮事件
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入框的用户名密码
                final String username = ET_username.getText().toString();
                final String password = ET_password.getText().toString();

                //登陆请求回调：处理向服务器请求登陆的结果
                Callback loginCallback = new Callback() {

                    //网络连接故障
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

                    //服务器发回响应
                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        //提取服务器响应的内容
                        final String result = response.body().string();
                        Headers headers = response.headers();
                        Log.e("bwq", "【LoginResponse】:" + result);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (result.equals("SUCCESS")) {
                                        //验证用户名密码正确
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
                                        //用户名密码错误
                                        Toast.makeText(LoginAcitivity.this, "登陆失败", Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                };

                //提交登陆请求至服务器，见AccountManager
                AccountManager.getInstance().Login(username, password, loginCallback);
            }
        });

        //注册按钮
        final Button register = findViewById(R.id.bt_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开注册页面，并取得注册结果
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
                                          //打开忘记密码页面，并取得忘记密码结果
                                          Intent intent = new Intent();
                                          intent.putExtra("intent", "forget");
                                          intent.setClass(LoginAcitivity.this, RegisterActivity.class);
                                          startActivityForResult(intent, REQUEST_FOR_FORGET);
                                      }
                                  }
        );

        //右上方关闭页面按钮
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
            //注册页面结果
            case REQUEST_FOR_REGISTER:
                if (resultCode == RESULT_OK) {
                    //注册成功
                    ET_username = findViewById(R.id.username);
                    //将注册成功的用户名自动写入用户名输入框
                    ET_username.setText(Config.getInstance(this).getUsername());
                    break;
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
