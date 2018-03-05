package com.example.materialdesign;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.materialdesign.BLE.BLEActivity;
import com.example.materialdesign.BLE.BLECommunication;
import com.example.materialdesign.BLE.BLEDeviceInfo;
import com.example.materialdesign.BLE.BLEDeviceManager;
import com.example.materialdesign.BLE.BluetoothLeService;
import com.example.materialdesign.Global.LocalDeviceInfo;
import com.example.materialdesign.Sensor.SensorData;
import com.example.materialdesign.Global.MyLog;
import com.example.materialdesign.Global.PermissionHandler;
import com.example.materialdesign.NFC.ReadTextActivity;
import com.example.materialdesign.NFC.WriteTextActivity;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.materialdesign.Sensor.SensorData.SENSOR_TEMPERATURE;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_FOR_PERMISSION = 0;
    //    private final static int REQUEST_FOR_QR_RESULT = 1;
    private final static int TAKE_PHOTO_FOR_HEAD_ICON = 2;
    private final static int OPEN_ALBUM_FOR_HEAD_ICON = 3;
    private final static int REQUEST_FOR_PERMISSION_PHONE = 4;
    public final static int REQUEST_FOR_QR_RESULT = 5;
    public final static int REQUEST_FOR_NFC_RESULT = 6;
    public final static int REQUEST_FOR_BLE_SCAN_RESULT = 7;

    private final static String SOURCE_ACTIVITY = MainActivity.class.getName();

    private DrawerLayout mDrawerLayout;
    public static String tabname[] = {"连接", "监测", "控制"};
    private Boolean isDrag = false;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    private List<Fragment> fragmentList;
    private FloatingActionButton fab;
    private NavigationView navigationView;
    private static Uri uri_headicon;
    private ImageView iv_headicon;
    private SharedPreferences.Editor editor;
    private SharedPreferences config;
    private File imageCache;
    private File cachefile;
    private Fragment_1 fragment_1;
    private Fragment_2 fragment_2;
    private Fragment_3 fragment_3;
    private Fragment_4 fragment_4;
    private Fragment_5 fragment_5;
    private Intent sourceIntent;
    private Context context;
    public Fragment fragment_graph;
    public SensorData sensorData = SensorData.getSensorData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        sourceIntent = getIntent();

        editor = getSharedPreferences("config", MODE_PRIVATE).edit();
        config = getSharedPreferences("config", MODE_PRIVATE);
        setCachefile();
        createFloatingButton();
        createToolBar();
        createTabLayout();
        createNavigationView();
        createHeadIcon(config.getBoolean("isFromCamera", false), false);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        MyLog.d(MyLog.TAG, "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


        PermissionHandler.checkPermission(this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE}
                        , REQUEST_FOR_PERMISSION);
    }

    public void setCachefile() {
        cachefile = getExternalCacheDir();
        imageCache = new File(cachefile + "/images/", "headicon.jpg");
        imageCache.mkdirs();
        if (Build.VERSION.SDK_INT >= 24) {
            uri_headicon = FileProvider.getUriForFile(MainActivity.this, "com.example.materialdesign", imageCache);
        } else {
            uri_headicon = Uri.fromFile(imageCache);
        }
    }

    /**
     * 头像设置
     *
     * @param isFromCamera   true:从相机获取，需要进行旋转处理 false:从相册获取
     * @param useNewHeadIcon true:发生更换头像事件 false:重载头像
     */
    public void createHeadIcon(Boolean isFromCamera, Boolean useNewHeadIcon) {
        if (useNewHeadIcon) {
            editor.putBoolean("useNewHeadIcon", useNewHeadIcon);
            editor.apply();
        }
        if (config.getBoolean("useNewHeadIcon", false)) {
            editor.putBoolean("isFromCamera", isFromCamera);
            editor.apply();
            Bitmap temp;
            try {
                temp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri_headicon));
                Matrix matrix = new Matrix();
                if (isFromCamera)
                    matrix.setRotate(90);
                Bitmap bitmap_headicon = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), matrix, true);
                iv_headicon.setImageBitmap(bitmap_headicon);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "头像获取失败", Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                Toast.makeText(this, "头像获取失败", Toast.LENGTH_SHORT).show();
            }
        } else
            iv_headicon.setImageResource(R.drawable.nav_icon);
    }

    /**
     * 创建标题栏
     */
    public void createToolBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_menu);
        }
    }

    /**
     * 创建导航视图
     */

    private String email;
    private String username;

    public void createNavigationView() {


        mDrawerLayout = findViewById(R.id.drawerlayout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_call);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        View nav_header = navigationView.getHeaderView(0);

        final EditText et_username = nav_header.findViewById(R.id.username);
        final EditText et_email = nav_header.findViewById(R.id.mail);
        iv_headicon = nav_header.findViewById(R.id.icon_image);

        email = config.getString("email", "");
        username = config.getString("username", "");
        et_email.setText(email);
        et_username.setText(username);

        iv_headicon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.inflate(R.menu.headicon);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.fromcamera: {
                                if (PermissionHandler.checkPermission(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_FOR_PERMISSION)) {
                                    Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    openCamera.putExtra(MediaStore.EXTRA_OUTPUT, uri_headicon);
                                    startActivityForResult(openCamera, TAKE_PHOTO_FOR_HEAD_ICON);
                                }
                            }
                            break;
                            case R.id.fromgallery:
                                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                    if (PermissionHandler.checkPermission(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_FOR_PERMISSION)) {
                                        Intent openalbum = new Intent(Intent.ACTION_PICK);
                                        openalbum.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                        startActivityForResult(openalbum, OPEN_ALBUM_FOR_HEAD_ICON);
                                    }
                                } else
                                    Toast.makeText(MainActivity.this, "获取相册失败!", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            }
        });


        et_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_username.setCursorVisible(true);
            }
        });
        et_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_email.setCursorVisible(true);
            }
        });

        final Button bt_save = nav_header.findViewById(R.id.bt_save);
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = et_username.getText().toString();
                email = et_email.getText().toString();
                username.replaceAll("\\s", "");
                email.replaceAll("\\s", "");
                et_username.setText(username);
                et_email.setText(email);
                if (email.matches("\\w+@\\w+.((com)|(cn)|(gov)|(net)|(org)|(com.cn)|(net.cn))")) {
                    et_email.setBackgroundResource(R.color.colorPrimary);
                    Toast.makeText(MainActivity.this, "保存成功!", Toast.LENGTH_LONG).show();

                    editor.putString("email", email);
                    editor.putString("username", username);
                    editor.apply();

                    et_email.setCursorVisible(false);
                    et_username.setCursorVisible(false);
                } else {
                    email = "";
                    et_email.setBackgroundColor(Color.RED);
                    Toast.makeText(MainActivity.this, "请检查email的格式！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 创建标签及滑动页面
     */
    public void createTabLayout() {
        fragment_1 = new Fragment_1();
        fragment_2 = new Fragment_2();
        fragment_3 = new Fragment_3();
//        fragment_4 = new Fragment_4();
//        fragment_5 = new Fragment_5();
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.content);
        fragmentList = new ArrayList<>();
        fragmentList.add(fragment_1);
        fragmentList.add(fragment_2);
        fragmentList.add(fragment_3);
//        fragmentList.add(fragment_4);
//        fragmentList.add(fragment_5);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        fragmentManager = getFragmentManager();
        FragmentAdapter fragmentAdapter = new FragmentAdapter(fragmentManager, fragmentList);

        viewPager.setAdapter(fragmentAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).select();
    }

    /**
     * 创建浮动按钮
     */
    public void createFloatingButton() {
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDrag == false) {
                    BLEDeviceInfo currentDevice=BLEDeviceManager.getCurrentBLEDevice();
                    if (currentDevice== null) {
                        Toast.makeText(MainActivity.this, "没有设备可供连接", Toast.LENGTH_SHORT).show();
                    } else {
                        if (currentDevice.Switch) {
                            currentDevice.Switch = false;
                            fab.setImageResource(R.drawable.fab_ble_disconnected);
                        } else {
                            currentDevice.Switch = true;
                            fab.setImageResource(R.drawable.fab_ble_connected);
                            beginBLELoop();
                        }
                    }
                } else
                    isDrag = false;
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isDrag = true;
                return false;
            }
        });

        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isDrag == true) {
                    float x;
                    float y;
                    Point point = new Point();
                    MainActivity.this.getWindowManager().getDefaultDisplay().getSize(point);
                    float screen_height = point.y;
                    float screen_width = point.x;
                    if ((x = event.getRawX() - 100) < 20)
                        x = 20;
                    if ((y = event.getRawY() - 200) < 20)
                        y = 20;
                    if (x > screen_width - 170)
                        x = screen_width - 170;
                    if (y > screen_height - 250)
                        y = screen_height - 250;
                    v.setX(x);
                    v.setY(y);
                }
                return false;
            }
        });
    }

    /**
     * 创建标题栏菜单
     *
     * @param menu 菜单
     * @Return Boolean  返回true显示
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    /**
     * 处理标题栏菜单的点击事件
     *
     * @param item 点击的菜单条目
     * @Return Boolean  返回true表示点击事件已处理
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        } else if ("扫描二维码".equals(item.getTitle())) {
            if (PermissionHandler.checkPermission(this, new String[]{Manifest.permission.CAMERA}, REQUEST_FOR_PERMISSION)) {
                startActivityForResult(new Intent(this, CaptureActivity.class), REQUEST_FOR_QR_RESULT);
            }
        } else if ("NFC".equals(item.getTitle())) {
            if (PermissionHandler.checkPermission(this, new String[]{Manifest.permission.NFC}, REQUEST_FOR_PERMISSION)) {
                Intent NFCIntent = new Intent(this, ReadTextActivity.class);
                NFCIntent.putExtra("source", SOURCE_ACTIVITY);
                startActivityForResult(NFCIntent, REQUEST_FOR_NFC_RESULT);
            }
        } else if ("蓝牙连接".equals(item.getTitle())) {
            if (PermissionHandler.checkPermission(this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_FOR_PERMISSION)) {
                startActivityForResult(new Intent(this, BLEActivity.class), REQUEST_FOR_BLE_SCAN_RESULT);
            }
        } else if ("WriteNFC".equals(item.getTitle())) {
            if (PermissionHandler.checkPermission(MainActivity.this, new String[]{Manifest.permission.NFC}, REQUEST_FOR_PERMISSION)) {
                startActivity(new Intent(this, WriteTextActivity.class));
            }
        }
        return true;
    }

    /**
     * 取得二维码扫描结果
     *
     * @param requestCode 申请码
     * @param resultCode  结果码
     * @param data        扫描结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_FOR_QR_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    String MACAddress = data.getExtras().getString("result");
                    if(MACAddress!=null&&MACAddress.startsWith(WriteTextActivity.prefix)) {
                        MACAddress=MACAddress.replace(WriteTextActivity.prefix,"");
                        BLEDeviceManager.addBLEDevice(new BLEDeviceInfo(null, MACAddress));
                        BLEDeviceManager.setCurrentBLEDevice(MACAddress);
                        MyLog.i(MyLog.TAG, MACAddress);
                        Toast.makeText(context, BLEDeviceManager.getCurrentBLEDevice().MACAddress, Toast.LENGTH_SHORT).show();
                        beginBLELoop();
                    }else{
                        Toast.makeText(context, "无效的二维码", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "二维码扫描失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_FOR_NFC_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    beginBLELoop();
                } else {
                    Toast.makeText(context, "NFC获取数据失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_FOR_BLE_SCAN_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                     beginBLELoop();
                } else {
                    Toast.makeText(context, "BLE设备获取失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case TAKE_PHOTO_FOR_HEAD_ICON:
                if (resultCode == RESULT_OK) {
                    createHeadIcon(true, true);
                } else {
                    Toast.makeText(this, "获取失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case OPEN_ALBUM_FOR_HEAD_ICON:
                if (resultCode == RESULT_OK) {
                    Uri albumUrl = data.getData();
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(albumUrl));
                        FileOutputStream fos = new FileOutputStream(imageCache);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                        fos.flush();
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    createHeadIcon(false, true);
                } else {
                    Toast.makeText(this, "获取失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 处理权限申请结果
     *
     * @param permissions 多个权限
     * @Return Boolean  所有权限通过才返回true，否则返回false
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FOR_PERMISSION:
                    if(checkSelfPermission(Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED){
                        getPhoneInfo();
                    }
//                for (int grantResult : grantResults) {
//                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
////                        Toast.makeText(this, "你拒绝了权限！", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    Toast.makeText(this, "权限申请成功！", Toast.LENGTH_SHORT).show();
//                }
                break;
//            case REQUEST_FOR_PERMISSION_PHONE:
//                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    getPhoneInfo();
//                    return;
//                }
//                break;
            default:
        }
    }

    public Boolean getPhoneInfo(){
        try {
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//            String deviceid = tm.getDeviceId();//获取智能设备唯一编号 //因为新安全机制，已经无法获取。
            String tel = tm.getLine1Number();//获取本机号码
            String sim = tm.getSimSerialNumber();//获得SIM卡的序号
//            if (deviceid != null) {
//                editor.putString(LocalDeviceInfo.LOCALDEVICE_IMEI, deviceid);
//            }
            if (tel != null) {
                editor.putString(LocalDeviceInfo.LOCALDEVICE_NUMBER, tel);
            }
            if (sim != null) {
                editor.putString(LocalDeviceInfo.LOCALDEVICE_SIM, sim);
            }
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }

    public BluetoothLeService mBluetoothLeService;

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

    // Code to manage Service lifecycle.
    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize())
                MyLog.e(MyLog.TAG, "Unable to initialize Bluetooth");
            else
                MyLog.i(MyLog.TAG, "mBluetoothLeService is okay");

            if (sourceIntent != null) {
                String NFCResult = sourceIntent.getStringExtra(ReadTextActivity.EXTRA_NFCRESULT);
                MyLog.i(MyLog.TAG, "NFCResult:" + NFCResult);
                if (NFCResult != null) {
                    beginBLELoop();
                }
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public void beginBLELoop() {

        final BLEDeviceInfo currentDevice=BLEDeviceManager.getCurrentBLEDevice();
        currentDevice.Switch = true;
        fab.setImageResource(R.drawable.fab_ble_connected);
        deviceListUpdate();
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    if (currentDevice.TryingToConnect) {
                        currentDevice.TryingToConnectCount++;
                    } else {
                        currentDevice.TryingToConnectCount = 0;
                    }
                    if (currentDevice.TryingToConnectCount >= 4) { //连续4次连接失败
                        currentDevice.Switch = false;
                        Toast.makeText(context, "找不到设备:" + currentDevice.Name, Toast.LENGTH_SHORT).show();
                        currentDevice.TryingToConnect = false;
                        currentDevice.TryingToConnectCount = 0;
                    }
                    MyLog.i(MyLog.TAG, "beginBLELoop");
                    if (currentDevice.MACAddress != null && mBluetoothLeService != null &&
                            mBluetoothLeService.connect(currentDevice.MACAddress)) {
                        currentDevice.TryingToConnect = true;
                    } else {
                        deviceListUpdate();
                    }
                }
                super.handleMessage(msg);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (currentDevice.Switch) {
                    Message message = null;
                    try {
                        message = handler.obtainMessage();
                        message.what = 1;
                        handler.sendMessage(message);
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                currentDevice.Status=false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "已断开设备:" + currentDevice.Name, Toast.LENGTH_SHORT).show();
                        fab.setImageResource(R.drawable.fab_ble_disconnected);
                        deviceListUpdate();
                    }
                });

            }
        }).start();
    }

    public void disconnectBLEConnection() {
        while (BLEDeviceManager.getCurrentBLEDevice().Connected) {
            MyLog.i(MyLog.TAG, "disconnectBLEConnection");
            if (mBluetoothLeService != null) {
                mBluetoothLeService.disconnect();
            } else {
                MyLog.w(MyLog.TAG, "BluetoothLeService is null?");
            }
            deviceListUpdate();
        }
    }

    public void deviceListUpdate() {
        MyLog.i(MyLog.TAG, "deviceListUpdate");
        try {
            fragment_1.deviceListAdapter.setDeviceList(BLEDeviceManager.deviceList);
            fragment_1.deviceListAdapter.notifyDataSetChanged();
        } catch (NullPointerException e) {
            MyLog.e(MyLog.TAG, "NULLPOINTER");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        //this.unregisterReceiver(mGattUpdateReceiver);
        //unbindService(mServiceConnection);
        if (mBluetoothLeService != null) {
            mBluetoothLeService.close();
            mBluetoothLeService = null;
        }
        MyLog.d(MyLog.TAG, "We are in destroy");
    }

// Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device. This can be a
// result of read or notification operations.

    /**
     * @var hasReiceved:  True when the result has been received in current connection
     * set to false when disconnected
     */
    private Boolean hasReceived = false;


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            BLEDeviceInfo currentDevice=BLEDeviceManager.getCurrentBLEDevice();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                MyLog.d(MyLog.TAG, "ACTION_GATT_CONNECTED");
                if (currentDevice.Switch) {
                    currentDevice.Connected = true;
                    currentDevice.TryingToConnect = false;
                    deviceListUpdate();
                } else {
                    deviceListUpdate();
                    Log.e(MyLog.TAG, "bwq");
                    disconnectBLEConnection();
                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //�Ͽ�����
                MyLog.d(MyLog.TAG, "ACTION_GATT_DISCONNECTED");
                hasReceived = false;
                currentDevice.Connected = false;

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //���Կ�ʼ�ɻ���
            {
                MyLog.d(MyLog.TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                if (mBluetoothLeService != null) {
                    BLECommunication.requestSensorData(SENSOR_TEMPERATURE, true);
                    BLECommunication.sendRequest(mBluetoothLeService);
                }
                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mBluetoothLeService != null) {
                                    mBluetoothLeService.disconnect();
                                }
                            }
                        }, 100); //100ms强制断开
                    }
                }).start();

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                if (data != null && data.length != 1 && (!hasReceived)) {
                    if (currentDevice.Status==false) {
                        Toast.makeText(MainActivity.this, "成功连接" + currentDevice.Name, Toast.LENGTH_SHORT).show();
                        currentDevice.Status=true;
                    }
                    MyLog.i(MyLog.TAG, "Data Received");
                    String string = BLECommunication.parseRawData(data);
                    MyLog.i(MyLog.TAG, "Data Received:" + string);
                    hasReceived = true;
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
}
