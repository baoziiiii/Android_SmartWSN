package com.example.qq452651705;

import com.example.qq452651705.Account.AccountManager;
import com.example.qq452651705.BLE.BLEConnHistory;
import com.google.gson.Gson;

import org.junit.Test;

import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Random;

import javax.crypto.Cipher;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private static final String AT_CMGF="AT+CMGF=0";
    private static final String AT_CMGS="AT+CMGS=?";
    private static final String PDU_PREFIX="0011000D91";
    private static final String PDU_CONTENT="欢迎您使用智能农业传感器网络系统，您的验证码为:032493";
    private String vericode="888888";
    private String vericode2="888887";
    private RSAPublicKey rsaPublicKey;
    private RSAPrivateKey rsaPrivateKey;

    @Test
    public void addition_isCorrect() throws Exception {
        Random random=new Random(System.currentTimeMillis());
        for (int i = 0; i <10 ; i++) {

            String vericode="";
            for (int j = 0; j <6 ; j++) {
                vericode=vericode+(random.nextInt(10));
            }
            System.out.println(vericode);
        }
    }
}