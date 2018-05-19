package com.example.qq452651705;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ThumbnailUtils;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.example.qq452651705.Account.AccountManager;
import com.example.qq452651705.Account.LoginAcitivity;
import com.example.qq452651705.BLE.BLEActivity;
import com.example.qq452651705.BLE.BLECommunication;
import com.example.qq452651705.BLE.BLEDeviceInfo;
import com.example.qq452651705.BLE.BLEDeviceManager;
import com.example.qq452651705.BLE.BluetoothLeService;
import com.example.qq452651705.Sensor.SensorData;
import com.example.qq452651705.Global.MyLog;
import com.example.qq452651705.Global.PermissionHandler;
import com.example.qq452651705.NFC.ReadTextActivity;
import com.example.qq452651705.NFC.WriteTextActivity;
import com.example.qq452651705.SharedPreferences.Config;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.example.qq452651705.Sensor.SensorData.SENSOR_WIND;
import static com.example.qq452651705.Sensor.SensorData.SENSOR_HUMIDTY;
import static com.example.qq452651705.Sensor.SensorData.SENSOR_LIGHT;
import static com.example.qq452651705.Sensor.SensorData.SENSOR_TEMPERATURE;
import static com.example.qq452651705.Sensor.SensorData.SENSOR_WINDDIR;

public class MainActivity extends AppCompatActivity {

    private final static String SOURCE_ACTIVITY = MainActivity.class.getName();


    /**
     *  请求码。用于使用startActivityForResult打开其他页面获取结果，在回调方法onActivityResult中通过请求码区分结果的来源
     *
     */
    private final static int REQUEST_FOR_PERMISSION = 0;
    //    private final static int REQUEST_FOR_QR_RESULT = 1;
    private final static int TAKE_PHOTO_FOR_HEAD_ICON = 2;
    private final static int OPEN_ALBUM_FOR_HEAD_ICON = 3;
    private final static int REQUEST_FOR_PERMISSION_PHONE = 4;
    public final static int REQUEST_FOR_QR_RESULT = 5;
    public final static int REQUEST_FOR_NFC_RESULT = 6;
    public final static int REQUEST_FOR_BLE_SCAN_RESULT = 7;
    public final static int REQUEST_FOR_LOGIN = 8;
    public final static int TAKE_PHOTO_FOR_SHARE = 9;


    private DrawerLayout mDrawerLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    private List<Fragment> fragmentList;
    private FloatingActionButton fab;
    private NavigationView navigationView;
    private static Uri uri_headicon;
    private static Uri uri_capture;
    private ImageView iv_headicon;
    private File imageCache;
    private File cachefile;
    private Fragment_1 fragment_1;
    private Fragment_2 fragment_2;
    private Fragment_3 fragment_3;
    private Intent sourceIntent;
    private Context context;
    private Config config;
    private Thread countThread;
    private SensorData sensorData = SensorData.getSensorData();
    private CircularProgressButton bt_nav_login;
    private CircularProgressButton bt_delogin;
    private TextView welcometext;


    /**
     *  页面初始化，在打开该页面时调用
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        sourceIntent = getIntent();
        config = Config.getInstance(this);

        //设置缓存目录
        setCachefile();

        //初始化组件
        createFloatingButton();
        createToolBar();
        createTabLayout();
        createNavigationView();
        createHeadIcon(config.getHeadIconSource(false), false);

        //绑定蓝牙服务BluetoothLeService
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        MyLog.d(MyLog.TAG, "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


        //检查蓝牙、位置权限。Android 6.0以后，蓝牙功能要求同时具有位置权限
        PermissionHandler.checkPermission(this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.READ_PHONE_STATE,
//                        Manifest.permission.SEND_SMS
                }
                , REQUEST_FOR_PERMISSION);
//        getPhoneInfo();
    }

    /**
     * 设置头像缓存路径
     * @param
     */
    public void setCachefile() {
        cachefile = getExternalCacheDir();
        imageCache = new File(cachefile + "/images/", "headicon.jpg");
        imageCache.mkdirs();
        File imageCache2=new File(cachefile+"/images","image.jpg");
        if (Build.VERSION.SDK_INT >= 24) {
            uri_headicon = FileProvider.getUriForFile(MainActivity.this, "com.example.qq452651705", imageCache);
            uri_capture = FileProvider.getUriForFile(MainActivity.this, "com.example.qq452651705", imageCache2);
        } else {
            uri_headicon = Uri.fromFile(imageCache);
            uri_capture = Uri.fromFile(imageCache2);
        }

    }

    /**
     * 头像设置
     *
     * @param isFromCamera   true:从相机获取，需要进行旋转处理 false:从相册获取
     * @param useNewHeadIcon true:发生更换头像事件 false:载入存储头像
     */
    public void createHeadIcon(Boolean isFromCamera, Boolean useNewHeadIcon) {
        if (useNewHeadIcon) {
            config.setNewHeadIconFlag(useNewHeadIcon);
        }
        if (config.getNewHeadIconFlag(false)) {
            config.setHeadIconSource(isFromCamera);
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
     * 初始化侧滑视图
     */
    public void createNavigationView() {
        mDrawerLayout = findViewById(R.id.drawerlayout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_homepage);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        View nav_header = navigationView.getHeaderView(0);

        //头像
        iv_headicon = nav_header.findViewById(R.id.icon_image);

        //头像长按弹出菜单
        iv_headicon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.inflate(R.menu.headicon);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            //拍照获取头像
                            case R.id.fromcamera: {
                                if (PermissionHandler.checkPermission(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_FOR_PERMISSION)) {
                                    Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    openCamera.putExtra(MediaStore.EXTRA_OUTPUT, uri_headicon);
                                    startActivityForResult(openCamera, TAKE_PHOTO_FOR_HEAD_ICON);
                                }
                            }
                            break;
                            //相册获取头像
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

        //欢迎文字
        welcometext = nav_header.findViewById(R.id.welcome);
        welcometext.setText("欢迎，\n");
        welcometext.setVisibility(View.INVISIBLE);

        //登陆按钮
        bt_nav_login = nav_header.findViewById(R.id.bt_nav_login);
        bt_nav_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this,LoginAcitivity.class),REQUEST_FOR_LOGIN);
            }
        });

        //注销按钮
        bt_delogin = nav_header.findViewById(R.id.bt_delogin);
        bt_delogin.setVisibility(View.INVISIBLE);
        bt_delogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountManager.getInstance().deLogin();
                bt_delogin.setVisibility(View.INVISIBLE);
                bt_nav_login.setVisibility(View.VISIBLE);
                welcometext.setVisibility(View.INVISIBLE);
                welcometext.setText("欢迎，\n");
            }
        });



        //"分享"菜单
       MenuItem shareItem=navigationView.getMenu().findItem(R.id.nav_friends);
       shareItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
           @Override
           public boolean onMenuItemClick(MenuItem item) {
               //触发支持分享功能的其他app
               Intent intent=new Intent(Intent.ACTION_SEND);
               intent.setType("image/*");
               intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
               intent.putExtra(Intent.EXTRA_TEXT, "我下载了智能无线传感器网络APP！很好玩哦！");
               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               startActivity(Intent.createChooser(intent, getTitle()));
               return true;
           }
       });

       //"地图"菜单
       MenuItem mapItem=navigationView.getMenu().findItem(R.id.nav_location);
        mapItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //触发其他地图app
                Uri mapUri = Uri.parse("geo:31.057160,121.215740");
                Intent mapintent = new Intent(Intent.ACTION_VIEW, mapUri);
                startActivity(mapintent);
                return true;
            }
        });

        //"相机"菜单
        MenuItem cameraItem=navigationView.getMenu().findItem(R.id.nav_camera);
        cameraItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //触发相机功能，获取相机结果
                if (PermissionHandler.checkPermission(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_FOR_PERMISSION)) {
                    Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    openCamera.putExtra(MediaStore.EXTRA_OUTPUT, uri_capture);
                    startActivityForResult(openCamera, TAKE_PHOTO_FOR_SHARE);
                }
                return true;
            }
        });

        //"注册菜单"
        MenuItem homepage=navigationView.getMenu().findItem(R.id.nav_homepage);
        homepage.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //触发浏览器
                Uri uri = Uri.parse("http://www.dhu.edu.cn");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        });
    }

    /**
     *  侧滑菜单->相机的拍照结果进行分享
     */
    public void handleCameraCapture(){
        Bitmap temp;
        try {
            //从缓存中获取拍照图片，将图片分享给其他应用
            temp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri_capture));
            Intent share_intent = new Intent();
            share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
            share_intent.setType("image/*");  //设置分享内容的类型
            share_intent.putExtra(Intent.EXTRA_TEXT, "我下载了智能无线传感器网络APP！很好玩哦！");
            share_intent.putExtra(Intent.EXTRA_STREAM, uri_capture);
            startActivity(Intent.createChooser(share_intent, getTitle()));
        } catch (Exception e) {
            Toast.makeText(this, "图片获取失败", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 初始化主视图标签页及滑动功能
     */
    public void createTabLayout() {
        //初始化三个标签页
        fragment_1 = new Fragment_1();
        fragment_2 = new Fragment_2();
        fragment_3 = new Fragment_3();
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.content);
        fragmentList = new ArrayList<>();
        fragmentList.add(fragment_1);
        fragmentList.add(fragment_2);
        fragmentList.add(fragment_3);
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                viewPager.setCurrentItem(tab.getPosition());
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//            }
//        });c
        fragmentManager = getFragmentManager();
        FragmentAdapter fragmentAdapter = new FragmentAdapter(fragmentManager, fragmentList);
        viewPager.setAdapter(fragmentAdapter);
        tabLayout.setupWithViewPager(viewPager);

        //初始化底部标签栏
        setTabs(tabLayout, this.getLayoutInflater(), new int[]{R.drawable.control, R.drawable.control, R.drawable.control});
        //默认选中第一个标签页
        tabLayout.getTabAt(0).select();
    }

    //初始化底部标签栏
    private void setTabs(TabLayout tabLayout, LayoutInflater inflater, int[] tabImgs) {


//        tabLayout.getTabAt(0).setIcon(R.drawable.tab_connect);
//        tabLayout.getTabAt(1).setIcon(R.drawable.tab_monitor);
//        tabLayout.getTabAt(2).setIcon(R.drawable.tab_control);

        //初始化底部标签页图标
        View view1 = getLayoutInflater().inflate(R.layout.tab_custom_connect, null);
        ImageView imageView1 = view1.findViewById(R.id.img_tab_connect);
        imageView1.setImageResource(R.drawable.tab_connect);
        tabLayout.getTabAt(0).setCustomView(view1);

        View view2 = getLayoutInflater().inflate(R.layout.tab_custom_monitor, null);
        ImageView imageView2 = view2.findViewById(R.id.img_tab_monitor);
        imageView2.setImageResource(R.drawable.tab_monitor);
        tabLayout.getTabAt(1).setCustomView(view2);

        View view3 = getLayoutInflater().inflate(R.layout.tab_custom_control, null);
        ImageView imageView3 = view3.findViewById(R.id.img_tab_control);
        imageView3.setImageResource(R.drawable.tab_control);
        tabLayout.getTabAt(2).setCustomView(view3);

    }

    /**
     * 初始化浮动按钮，点击浮动按钮可以开关当前蓝牙连接。按钮在长按时进入拖动模式，可以改变按钮的位置。
     */


    private Boolean isDrag = false;//true：按钮处于拖动模式

    public void createFloatingButton() {
        fab = findViewById(R.id.fab);

        //点击事件用于蓝牙连接开关
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDrag == false) {
                    BLEDeviceInfo currentDevice = BLEDeviceManager.getCurrentBLEDevice();
                    if (currentDevice == null) {
                        Toast.makeText(MainActivity.this, "没有设备可供连接", Toast.LENGTH_SHORT).show();
                    } else {
                        if (currentDevice.Switch) {
                            //关闭蓝牙连接
                            currentDevice.Switch = false;
                            //图标设置为断开
                            fab.setImageResource(R.drawable.fab_ble_disconnected);
                        } else {
                            //打开蓝牙连接
                            currentDevice.Switch = true;
                            //图标设置为连接
                            fab.setImageResource(R.drawable.fab_ble_connected);
                            //开启蓝牙连接任务
                            beginBLELoop();
                        }
                    }
                } else
                    isDrag = false;
            }
        });

        //长按事件用于开启按钮拖动模式
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isDrag = true;
                return false;
            }
        });

        //触摸事件用于跟踪用户拖动位置
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
     * 初始化上方标题栏
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
     * 初始化标题栏菜单
     *
     * @param menu 菜单
     * @Return Boolean  返回true显示
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setIconEnable(menu, true);
        menu.add("扫描二维码").setIcon(R.drawable.qr_small_icon);
        menu.add("NFC").setIcon(R.drawable.nfc_small_icon);
        menu.add("蓝牙连接").setIcon(R.drawable.bluetooth_small_icon);
        menu.add("WriteNFC").setIcon(R.drawable.nfc_small_icon);
        return true;
    }

    //enable为true时，菜单添加图标有效，enable为false时无效。
    private void setIconEnable(Menu menu, boolean enable) {
        try {
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);
            m.invoke(menu, enable);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            //左上方图标用于打开侧滑页面
            mDrawerLayout.openDrawer(GravityCompat.START);
        } else if ("扫描二维码".equals(item.getTitle())) {
            //扫描二维码菜单
            if (PermissionHandler.checkPermission(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.BLUETOOTH}, REQUEST_FOR_PERMISSION)) {
                startActivityForResult(new Intent(this, CaptureActivity.class), REQUEST_FOR_QR_RESULT);
            }
        } else if ("NFC".equals(item.getTitle())) {
            //NFC菜单
            if (PermissionHandler.checkPermission(this, new String[]{Manifest.permission.NFC,Manifest.permission.BLUETOOTH}, REQUEST_FOR_PERMISSION)) {
                Intent NFCIntent = new Intent(this, ReadTextActivity.class);
                NFCIntent.putExtra("source", SOURCE_ACTIVITY);
                startActivityForResult(NFCIntent, REQUEST_FOR_NFC_RESULT);
            }
        } else if ("蓝牙连接".equals(item.getTitle())) {
            //蓝牙连接菜单
            if (PermissionHandler.checkPermission(this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_FOR_PERMISSION)) {
                startActivityForResult(new Intent(this, BLEActivity.class), REQUEST_FOR_BLE_SCAN_RESULT);
            }
        } else if ("格式化NFC".equals(item.getTitle())) {
            //格式化NFC菜单
            if (PermissionHandler.checkPermission(MainActivity.this, new String[]{Manifest.permission.NFC}, REQUEST_FOR_PERMISSION)) {
                startActivity(new Intent(this, WriteTextActivity.class));
            }
        }
        return true;
    }


    /**
     * 获取从该页面打开的所有页面返回的结果
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        扫描结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            //获取二维码扫描的结果
            case REQUEST_FOR_QR_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    String MACAddress = data.getExtras().getString("result");
                    if (MACAddress != null && MACAddress.startsWith(WriteTextActivity.prefix)) {
                        MACAddress = MACAddress.replace(WriteTextActivity.prefix, "");
                        BLEDeviceManager.addBLEDevice(new BLEDeviceInfo(null, MACAddress));
                        BLEDeviceManager.setCurrentBLEDevice(MACAddress);
                        MyLog.i(MyLog.TAG, MACAddress);
                        Toast.makeText(context, BLEDeviceManager.getCurrentBLEDevice().MACAddress, Toast.LENGTH_SHORT).show();
                        beginBLELoop();
                    } else {
                        Toast.makeText(context, "无效的二维码", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "二维码扫描失败", Toast.LENGTH_SHORT).show();
                }
                break;
            //获取NFC扫描的结果
            case REQUEST_FOR_NFC_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    beginBLELoop();
                } else {
                    Toast.makeText(context, "NFC获取数据失败", Toast.LENGTH_SHORT).show();
                }
                break;

            //获取蓝牙扫描的结果
            case REQUEST_FOR_BLE_SCAN_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    beginBLELoop();
                } else {
                    Toast.makeText(context, "BLE设备获取失败", Toast.LENGTH_SHORT).show();
                }
                break;

            //获取头像设置调用相机拍照的结果
            case TAKE_PHOTO_FOR_HEAD_ICON:
                if (resultCode == RESULT_OK) {
                    createHeadIcon(true, true);
                } else {
                    Toast.makeText(this, "获取失败", Toast.LENGTH_SHORT).show();
                }
                break;

            //获取侧滑页面相机菜单的拍照结果
            case TAKE_PHOTO_FOR_SHARE:
                if(resultCode==RESULT_OK){
                    handleCameraCapture();
                }else{
                    Toast.makeText(this, "图片获取失败", Toast.LENGTH_SHORT).show();
                }
                break;

            //获取头像设置调用相册的结果
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

            //获取打开登陆页面返回的结果
            case REQUEST_FOR_LOGIN:
                //登陆成功
                if(resultCode==RESULT_OK){
                    bt_nav_login.setVisibility(View.INVISIBLE);
                    bt_delogin.setVisibility(View.VISIBLE);
                    welcometext.setVisibility(View.VISIBLE);
                    welcometext.append(AccountManager.getNowUsername());
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

//    /**
//     * 处理权限申请结果
//     *
//     * @param permissions 多个权限
//     * @Return Boolean  所有权限通过才返回true，否则返回false
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_FOR_PERMISSION:
//                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
////                        getPhoneInfo();
////                        SmsManager.getDefault().sendTextMessage(MySMS.SERVERPHONE,
////                                null, "register", null, null);
//                }
////                for (int grantResult : grantResults) {
////                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
//////                        Toast.makeText(this, "你拒绝了权限！", Toast.LENGTH_SHORT).show();
////                        return;
////                    }
////                    Toast.makeText(this, "权限申请成功！", Toast.LENGTH_SHORT).show();
////                }
//                break;
////            case REQUEST_FOR_PERMISSION_PHONE:
////                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////                    getPhoneInfo();
////                    return;
////                }
////                break;
////            default:
//        }
//    }

//    private void getPhoneInfo(){
//        try {
//            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            String imei = tm.getDeviceId();
//            if (imei != null)
//                config.setIMEI(imei);
//        }catch (SecurityException e){}
//    }



    /**
     * 开始BLE会话任务（详见BLECommunication类）:
     * BLE会话开始后，以固定周期(BLELoopPeriod)与BLE设备进行短暂连接，100ms后自动断开。
     */

    Handler updateUIHandler =null ;

    public void beginBLELoop() {
        final BLEDeviceInfo currentDevice = BLEDeviceManager.getCurrentBLEDevice();
        if(!AccountManager.getLoginStatus()){
            Toast.makeText(this,"请先登陆！",Toast.LENGTH_LONG).show();
            return;
        }
        currentDevice.Switch = true;
        fab.setImageResource(R.drawable.fab_ble_connected);
        deviceListUpdate();
        updateUIHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.what==0){
                    Toast.makeText(MainActivity.this, "已断开设备:" + currentDevice.Name, Toast.LENGTH_SHORT).show();
                    fab.setImageResource(R.drawable.fab_ble_disconnected);
                    deviceListUpdate();
                }
                else if(msg.what==1){
                    deviceListUpdate();
                }
                return true;
            }
        });
        BLECommunication.BLELoopTask(this,context,mBluetoothLeService, updateUIHandler);
    }

    /**
     * 断开BLE连接
     */
    public void disconnectBLEConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count=5;
                while (BLEDeviceManager.getCurrentBLEDevice().Connected&&count-->0) {
                    MyLog.i(MyLog.TAG, "disconnectBLEConnection");
                    if (mBluetoothLeService != null) {
                        mBluetoothLeService.disconnect();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        MyLog.w(MyLog.TAG, "BluetoothLeService is null?");
                    }
                    Message message=Message.obtain();
                    message.what=1;
                    updateUIHandler.sendMessage(message);
                }
            }
        }).start();
    }

    //蓝牙BLE服务BluetoothLeService绑定
    public BluetoothLeService mBluetoothLeService;
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




// Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device. This can be a
// result of read or notification operations.

    //注册蓝牙连接、断开、发现蓝牙服务、新数据等广播通知
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

    /**
     * @var hasReiceved: true:本次连接已经一次传感器数据。否则:false
     */
    private Boolean hasReceived = false;


    /**
     * 蓝牙服务广播接受者
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            BLEDeviceInfo currentDevice = BLEDeviceManager.getCurrentBLEDevice();
            //成功连接
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                MyLog.d(MyLog.TAG, "ACTION_GATT_CONNECTED");
                if (currentDevice.Switch) {
                    currentDevice.Connected = true;
                    currentDevice.TryingToConnect = false;
                    deviceListUpdate();
                } else {
                    deviceListUpdate();
                    Log.e(MyLog.TAG, "CONNECTED when SWITCH IS OFF");
                    disconnectBLEConnection();
                }
            }
            //断开连接
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //�Ͽ�����
                MyLog.d(MyLog.TAG, "ACTION_GATT_DISCONNECTED");
                hasReceived = false;
                currentDevice.Connected = false;
            }
            //检测到BLE设备的SERVICES
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //���Կ�ʼ�ɻ���
            {
                MyLog.d(MyLog.TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                if (mBluetoothLeService != null) {
                    if(currentDevice.Switch==true){
                        currentDevice.TryingToDisconnect=false;
                    }
                    if(currentDevice.TryingToDisconnect==true){
                        BLECommunication.sendDisconnect();
                        BLECommunication.sendRequest(mBluetoothLeService);
                        currentDevice.TryingToDisconnect=false;
                        BLECommunication.cookie=':';
                    }
                    else if(currentDevice.Status==false&&currentDevice.Switch==true) {
//                        MyLog.w(MyLog.TAG,"sendIMEI");
//                        BLECommunication.sendIMEI(config);
                        BLECommunication.requestSensorData(SENSOR_TEMPERATURE, true);
                    }else {
                        MyLog.w(MyLog.TAG,"requestSensor");
                        BLECommunication.requestSensorData(SENSOR_TEMPERATURE, true);
                    }
                    BLECommunication.sendRequest(mBluetoothLeService);
                }
                final Handler handler = new Handler();
                //限制100ms的连接时间
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
            }
            //收到数据
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                if (data != null && data.length != 1 && (!hasReceived)) {
                    if (currentDevice.Status == false) {
                        Toast.makeText(MainActivity.this, "成功连接" + currentDevice.Name, Toast.LENGTH_SHORT).show();
                        currentDevice.Status = true;
                        MyLog.w(MyLog.TAG,"Status=true");
                    }
                    MyLog.i(MyLog.TAG, "Data Received");
                    String string = BLECommunication.parseRawData(data);
                    MyLog.i(MyLog.TAG, "Data Received:" + string);
                    hasReceived = true;
                }
            }
        }
    };

    /**
     * 蓝牙设备列表更新
     */
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(countThread!=null) {
            countThread.stop();
        }
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

}
