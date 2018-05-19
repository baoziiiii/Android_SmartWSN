package com.example.qq452651705.Account;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.example.qq452651705.Global.MyLog;
import com.example.qq452651705.R;
import com.example.qq452651705.SharedPreferences.Config;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 注册/忘记密码复用页面。注册/忘记密码用到相同的组件：用户名、密码、再次输入密码、验证码
 */

public class RegisterActivity extends AppCompatActivity {

    Boolean isRegPage=true; //true：注册页面，false：忘记密码页面

    //四个输入框：手机号、密码、再次输入密码、验证码
    EditText ET_username = null;
    EditText ET_password = null;
    EditText ET_password2 = null;
    EditText ET_vericode = null;
    String username = "";
    String password = "";
    String password2 = "";
    String vericode = "";

    //请求验证码间隔不能小于60秒
    Integer clock = 60;

    //输入框过滤器，防止在用户名或密码输入框中输入空格等非法字符
    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source.equals(" ") || source.toString().contentEquals("\n")) return "";
            else return null;
        }
    };

    //焦点改变事件，四个输入框在失去焦点（脱离选中状态）时，立即检测输入的合法性
    View.OnFocusChangeListener ofcl = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (v.equals(ET_username)) {
                if (!hasFocus) {
                    checkUsername();
                }
            } else if (v.equals(ET_password)) {
                if (!hasFocus) {
                    checkPassword();
                    if(!password2.isEmpty())
                        checkPassword2();
                }
            } else if (v.equals(ET_password2)) {
                if (!hasFocus) {
                    checkPassword2();
                }
            } else if (v.equals(ET_vericode)) {
                if (!hasFocus) {
                    checkVericode();
                }
            }
        }
    };

    //检测手机号输入
    private Boolean checkUsername() {
        username = ET_username.getText().toString();
        //验证是否为11位数字
        if (!username.matches("\\d{11}")) {
            ET_username.setError("要求11位手机号！");
            return false;
        }
        return true;
    }

    //检测密码输入
    private Boolean checkPassword() {
        password = ET_password.getText().toString();
        //检测密码是否为空
        if (password.isEmpty()) {
            ET_password.setError("密码不能为空！");
            return false;
        }
        //检测密码强度是否符合要求：8～16个字符，至少包含1个大写字母，一个小写字母和1个数字
        else if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[\\S]{8,16}$")) {
            ET_password.setError("要求8-16个字符，至少包含1个大写字母，1个小写字母和1个数字");
            return false;
        }
        return true;
    }

    //检测再次密码输入
    private Boolean checkPassword2() {
        password = ET_password.getText().toString();
        password2 = ET_password2.getText().toString();
        //检测两次输入密码是否一致
        if (!password.equals(password2)) {
            ET_password2.setError("两次密码不一致！");
            return false;
        }
        return true;
    }

    //检测验证码格式
    private Boolean checkVericode() {
        vericode = ET_vericode.getText().toString();
        //检测验证码是否为6位数字
        if (!vericode.matches("\\d{6}")) {
            ET_vericode.setError("请输入6位数字验证码！");
            return false;
        }
        return true;
    }

    //页面初始化
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //初始化输入框。添加输入过滤、焦点改变事件处理器
        ET_username = findViewById(R.id.reg_username);
        ET_password = findViewById(R.id.reg_password);
        ET_password2 = findViewById(R.id.reg_password2);
        ET_vericode = findViewById(R.id.vericode);
        ET_username.setOnFocusChangeListener(ofcl);
        ET_username.setFilters(new InputFilter[]{filter});
        ET_password.setOnFocusChangeListener(ofcl);
        ET_password.setFilters(new InputFilter[]{filter});
        ET_password2.setOnFocusChangeListener(ofcl);
        ET_password2.setFilters(new InputFilter[]{filter});
        ET_vericode.setOnFocusChangeListener(ofcl);

        //注册/忘记密码提交按钮
        Button confirm = findViewById(R.id.bt_reg_confirm);

        //判断是注册页面还是忘记密码页面
        if("register".equals(getIntent().getStringExtra(
                "intent"))){
            //注册页面
            setTitle("注册");
            isRegPage=true;
            confirm.setText("注册");

        }else{
            //忘记密码页面
            setTitle("忘记密码");
            confirm.setText("提交");
            isRegPage=false;
        }

        //注册/忘记密码按钮点击事件
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //检测四个输入框的格式
                if (!(checkUsername() && checkPassword() && checkPassword2() && checkVericode())) {
                    Toast.makeText(RegisterActivity.this, "填写信息有误!", Toast.LENGTH_SHORT).show();
                } else {

                    //注册/修改密码请求回调：处理向服务器请求注册/修改密码的结果
                    Callback registerCallback = new Callback() {

                        //网络连接故障
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            MyLog.e("bwq", "【RegisterResponse】:" + "CALL_BACK_ON_FAILURE");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterActivity.this, "无法连接服务器，请检查网络情况", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        //成功获得服务器响应结果
                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        //提取响应内容
                                        String result = response.body().string();
                                        MyLog.e("bwq", "【RegisterResponse】:" + result);

                                        //结果为成功
                                        if (result.equals("SUCCESS")) {
                                            //注册成功/修改密码成功
                                            if(isRegPage)
                                                Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_LONG).show();
                                            else
                                                Toast.makeText(RegisterActivity.this, "密码修改成功！", Toast.LENGTH_LONG).show();
                                            //将手机号存入本地配置信息，便于下次登陆
                                            Config.getInstance(RegisterActivity.this).setUsername(username);
                                            //返回登陆页面，结果：成功
                                            setResult(RESULT_OK);
                                            finish();
                                        } else if (result.equals("Wrong Vericode")) {
                                            //验证码错误
                                            Toast.makeText(RegisterActivity.this, "验证码错误或已经过期！请重新获取验证码！", Toast.LENGTH_LONG).show();
                                        } else {
                                            //其他错误：注册页面：用户名已存在。忘记密码页面：用户名不存在。
                                            if(isRegPage)
                                                Toast.makeText(RegisterActivity.this, "用户名已存在！", Toast.LENGTH_LONG).show();
                                            else {
                                                Toast.makeText(RegisterActivity.this, "用户名不存在！请先注册！", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    };

                    //提交注册/忘记密码请求至服务器，见AccountManager
                    AccountManager.getInstance().Register(username, password, vericode, registerCallback,isRegPage);
                }
            }
        });

        //请求验证码按钮，请求间隔不能小于60秒：用户点击一次后，按钮将关闭，等待60秒后，按钮重新打开，用户才能再次请求验证码
        final CircularProgressButton verifycode = findViewById(R.id.bt_reg_vericode);
        verifycode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = ET_username.getText().toString();

                //60秒计时线程的更新回调函数
                final Handler countHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            //倒计时更新文字
                            verifycode.setText(clock + " 秒");
                        } else {
                            //倒计时结束，打开按钮，重置文字
                            verifycode.setEnabled(true);
                            verifycode.setText("获取验证码");
                        }
                    }
                };
                //验证输入的手机号是否为11位数字
                if (username.matches("\\d{11}")) {

                    //验证码请求回调函数：处理向服务器请求验证码的结果
                    Callback vericodeCallback = new Callback() {

                        //网络连接故障
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("bwq", "【VericodeResponse】:" + "CALL_BACK_ON_FAILURE");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterActivity.this, "无法连接服务器，请检查网络情况！", Toast.LENGTH_LONG).show();
                                }
                            });
                            e.printStackTrace();

                        }

                        //成功获得服务器响应
                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            String result = response.body().string();
                            Log.e("bwq", "【VericodeResponse】:" + result);
                            if (result.equals("Sending vericode")) {
                                //服务器正在发送验证码
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "验证码已发送，请耐心等待！", Toast.LENGTH_LONG).show();
                                        //关闭请求验证码按钮
                                        verifycode.setEnabled(false);
                                    }
                                });
                                //开启60秒计时线程，每过1秒，通知之前定义的countHandler更新按钮文字，60秒计时结束后打开请求验证码按钮
                                Thread countThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (clock > 0) {
                                            try {
                                                Thread.currentThread().sleep(1000);
                                                clock--;
                                                Message message = Message.obtain();
                                                message.what = 1;
                                                countHandler.sendMessage(message);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        //计时完毕
                                        clock=60;
                                        Message message = Message.obtain();
                                        message.what = 2;
                                        countHandler.sendMessage(message);
                                    }
                                });
                                //开启计时线程
                                countThread.start();
                            } else {
                                //服务器无法发送验证码
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "未知错误，无法发送验证码！", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    };
                    //提交验证码请求至服务器，见AccountManager
                    AccountManager.getInstance().requestVericode(username, vericodeCallback);
                } else {
                    Toast.makeText(RegisterActivity.this, "请输入11位手机号！", Toast.LENGTH_LONG).show();
                }
            }
        });

        //关闭页面按钮
        Button close = findViewById(R.id.reg_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
