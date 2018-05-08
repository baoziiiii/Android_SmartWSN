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


public class RegisterActivity extends AppCompatActivity {

    Boolean isRegPage=true;

    EditText ET_username = null;
    EditText ET_password = null;
    EditText ET_password2 = null;
    EditText ET_vericode = null;
    String username = "";
    String password = "";
    String password2 = "";
    String vericode = "";

    Integer clock = 60;

    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source.equals(" ") || source.toString().contentEquals("\n")) return "";
            else return null;
        }
    };

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

    private Boolean checkUsername() {
        username = ET_username.getText().toString();
        if (!username.matches("\\d{11}")) {
            ET_username.setError("要求11位手机号！");
            return false;
        }
        return true;
    }

    private Boolean checkPassword() {
        password = ET_password.getText().toString();
        if (password.isEmpty()) {
            ET_password.setError("密码不能为空！");
            return false;
        } else if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[\\S]{8,16}$")) {
            ET_password.setError("要求8-16个字符，至少包含1个大写字母，1个小写字母和1个数字");
            return false;
        }
        return true;
    }


    private Boolean checkPassword2() {
        password = ET_password.getText().toString();
        password2 = ET_password2.getText().toString();
        if (!password.equals(password2)) {
            ET_password2.setError("两次密码不一致！");
            return false;
        }
        return true;
    }


    private Boolean checkVericode() {
        vericode = ET_vericode.getText().toString();
        if (!vericode.matches("\\d{6}")) {
            ET_vericode.setError("请输入6位数字验证码！");
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        Button confirm = findViewById(R.id.bt_reg_confirm);
        if("register".equals(getIntent().getStringExtra(
                "intent"))){
            setTitle("注册");
            isRegPage=true;
            confirm.setText("注册");

        }else{
            setTitle("找回密码");
            confirm.setText("提交");
            isRegPage=false;
        }


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(checkUsername() && checkPassword() && checkPassword2() && checkVericode())) {
                    Toast.makeText(RegisterActivity.this, "填写信息有误!", Toast.LENGTH_SHORT).show();
                } else {
                    Callback registerCallback = new Callback() {
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

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String result = response.body().string();
                                        MyLog.e("bwq", "【RegisterResponse】:" + result);
                                        if (result.equals("SUCCESS")) {
                                            if(isRegPage)
                                                Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_LONG).show();
                                            else
                                                Toast.makeText(RegisterActivity.this, "密码修改成功！", Toast.LENGTH_LONG).show();
                                            Config.getInstance(RegisterActivity.this).setUsername(username);
                                            setResult(RESULT_OK);
                                            finish();
                                        } else if (result.equals("Wrong Vericode")) {
                                            Toast.makeText(RegisterActivity.this, "验证码错误或已经过期！请重新获取验证码！", Toast.LENGTH_LONG).show();
                                        } else {
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
                    AccountManager.getInstance().Register(username, password, vericode, registerCallback,isRegPage);
                }
            }
        });

        final CircularProgressButton verifycode = findViewById(R.id.bt_reg_vericode);

        verifycode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = ET_username.getText().toString();
                final Handler countHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            verifycode.setText(clock + " 秒");
                        } else {
                            verifycode.setEnabled(true);
                            verifycode.setText("获取验证码");
                        }
                    }
                };
                if (username.matches("\\d{11}")) {
                    Callback vericodeCallback = new Callback() {
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

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            String result = response.body().string();
                            Log.e("bwq", "【VericodeResponse】:" + result);
                            if (result.equals("Sending vericode")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "验证码已发送，请耐心等待！", Toast.LENGTH_LONG).show();
                                        verifycode.setEnabled(false);
                                    }
                                });
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
                                        clock=60;
                                        Message message = Message.obtain();
                                        message.what = 2;
                                        countHandler.sendMessage(message);
                                    }
                                });
                                countThread.start();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "未知错误，无法发送验证码！", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    };
                    AccountManager.getInstance().requestVericode(username, vericodeCallback);

                } else {
                    Toast.makeText(RegisterActivity.this, "请输入11位手机号！", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button close = findViewById(R.id.reg_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
