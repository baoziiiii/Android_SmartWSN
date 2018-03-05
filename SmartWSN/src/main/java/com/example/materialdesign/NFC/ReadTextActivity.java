package com.example.materialdesign.NFC;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;
import android.widget.Toast;

import com.example.materialdesign.BLE.BLEDeviceInfo;
import com.example.materialdesign.BLE.BLEDeviceManager;
import com.example.materialdesign.Fragment_1;
import com.example.materialdesign.Global.MyLog;
import com.example.materialdesign.MainActivity;
import com.example.materialdesign.NFC.BaseNfcActivity;
import com.example.materialdesign.R;

import java.util.Arrays;

/**
 * Created by B on 2018/2/11.
 */

public class ReadTextActivity extends BaseNfcActivity {

    public static final String EXTRA_NFCRESULT="NFCResult";
    private TextView mNfcText;
    private String mTagText;
    private String textRecord;
    private String source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readnfc);
        source = getIntent().getStringExtra("source");
        onNewIntent(getIntent());
    }
    @Override
    public void onNewIntent(Intent intent) {
        //1.获取Tag对象
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        //2.获取Ndef的实例
        if(detectedTag!=null) {
            Ndef ndef = Ndef.get(detectedTag);
            mTagText = ndef.getType() + "\nmaxsize:" + ndef.getMaxSize() + "bytes\n\n";
            readNfcTag(intent);

            if(textRecord.startsWith("G&QUIWHI|")){
                textRecord=textRecord.replace("G&QUIWHI|","");
                BLEDeviceManager.addBLEDevice(new BLEDeviceInfo(null,textRecord));
                BLEDeviceManager.setCurrentBLEDevice(textRecord);
                if(source==null) {
                    Intent newMain = new Intent(this, MainActivity.class);
                    newMain.putExtra(EXTRA_NFCRESULT, textRecord);
                    source=null;
                    startActivity(newMain);
                }else{
                    Intent data=new Intent();
                    data.putExtra(EXTRA_NFCRESULT,textRecord);
                    setResult(Activity.RESULT_OK,data);
                    finish();
                }
            }else{
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
//            BLEDeviceInfo.MACAddress = textRecord;
        }
    }


    /**
     * 读取NFC标签文本数据
     */
    private void readNfcTag(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            //标签可能存储了多个NdefMessage对象，一般情况下只有一个NdefMessage对象
            NdefMessage msgs[] = null;
            int contentSize = 0;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    contentSize += msgs[i].toByteArray().length;
                }
            }
            try {
                if (msgs != null) {
                    //程序中只考虑了1个NdefRecord对象，若是通用软件应该考虑所有的NdefRecord对象
                    NdefRecord record = msgs[0].getRecords()[0];
                    textRecord = parseTextRecord(record);
//                    Toast.makeText(this, textRecord + "\n\ntext\n" + contentSize + " bytes", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
            }
        }
    }
    /**
     * 解析NDEF文本数据，从第三个字节开始，后面的文本数据
     * @param ndefRecord
     * @return
     */
    public static String parseTextRecord(NdefRecord ndefRecord) {
        /**
         * 判断数据是否为NDEF格式
         */
        //判断TNF
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }
        //判断可变的长度的类型
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }
        try {
            //获得字节数组，然后进行分析
            byte[] payload = ndefRecord.getPayload();
            //下面代码分析payload：状态字节+ISO语言编码（ASCLL）+文本数据（UTF_8/UTF_16）
            //其中payload[0]放置状态字节：如果bit7为0，文本数据以UTF_8格式编码，如果为1则以UTF_16编码
            //bit6是保留位，默认为0
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
            //处理bit5-0。bit5-0表示语言编码长度（字节数）
            int languageCodeLength = payload[0] & 0x3f;
            //获取语言编码（从payload的第2个字节读取languageCodeLength个字节作为语言编码）
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            //下面开始NDEF文本数据后面的字节，解析出文本
            String textRecord = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return textRecord;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }
}
