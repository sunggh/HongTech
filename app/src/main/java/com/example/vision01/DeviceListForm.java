package com.example.vision01;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vision01.Device.Device;
import com.example.vision01.Device.DeviceAdpt;
import com.example.vision01.Device.DeviceManager;
import com.example.vision01.Sqlite.DbDevice;
import com.example.vision01.Sqlite.SqliteDb;

import java.util.ArrayList;
import java.util.Set;

public class DeviceListForm extends AppCompatActivity {
    SqliteDb sqliteDb = SqliteDb.getInstance();
    ArrayList<Device> devices;
    ListView lvDeviceList;
    TextView textState;
    Button btnMakeNotification;
    DeviceAdpt deviceAdpt;
    Device selectedDevice; //선택된 제품 (삭제하거나 rssi 찾을 때 사용)
    int stateFlag = 0;  //0 ->찾기 1 -> 추가 2 -> 수정 3-> 삭제
    private BluetoothAdapter BTAdapter;
    public static Context mContext;

    private final static int REQUEST_ENABLE_BT = 1;

    public static DeviceListForm dlf;


    //private DeviceAdpt.Preview mPreview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list_form);
        InitializeDevices();
        checkLocationPermissions();
        enableBluetooth();
        dlf=this;
        initControl();


        mContext = this;
    }
    public void initControl(){
        lvDeviceList = (ListView)findViewById(R.id.listView);
        textState = (TextView)findViewById(R.id.text_state);
        btnMakeNotification = (Button)findViewById(R.id.btn_make_notification);
        deviceAdpt = new DeviceAdpt(this, devices);

        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        lvDeviceList.setAdapter(deviceAdpt);

        btnMakeNotification.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                makeNotification();
                startService();
//                Intent passedIntent = getIntent();
//                processCommand(passedIntent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        InitializeDevices();
        initControl();
    }

    private void getRSSIList(){
        //나중에 ARCamera로 바꾸면 됨
        Log.e("BLE", "get RSSI LIST");
        BTAdapter.startDiscovery();
        Log.e("BLE", "startDiscovery");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.device_popup_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        Toast toast = Toast.makeText(getApplicationContext(),"", Toast.LENGTH_LONG);

        switch(item.getItemId())
        {
            case R.id.action_add:
                //stateFlag = 1;
                AddDevice();
                break;
            case R.id.action_modify:
                textState.setText("물건 수정");
                stateFlag = 2;
                break;
            case R.id.action_delete:
                textState.setText("물건 삭제");
                stateFlag = 3;
                break;
        }
        toast.show();

        return super.onOptionsItemSelected(item);
    }


    public boolean ConfirmState(Device device) {
        //선택된 제품의 정보를 저장
        selectedDevice = device;

        //찾기
        switch (stateFlag) {
            case 0:
                //찾기
                getRSSIList();
                return true;
            case 2:
                //수정
                return false;
            case 3:
                //삭제
                DeleteDevice();
                return false;
            default: return false;
        }

    }
    private void AddDevice(){
        Intent intent = new Intent(getApplicationContext(), AddDeviceForm.class);
        startActivity(intent);
    }
    private void DeleteDevice() {

        int count, checked ;
        count = deviceAdpt.getCount() ;

        if (count > 0) {
            // 현재 선택된 아이템의 position 획득.
            //checked = lvDeviceList.getCheckedItemPosition();
            checked = selectedDevice.getPosition();
            if (checked > -1 && checked < count) {
                Device chkDevice = devices.get(checked);
                // 아이템 삭제
                //items.remove(checked) ;
                DbDevice.removeDevice(chkDevice);

                // listview 선택 초기화.
                lvDeviceList.clearChoices();

                // listview 갱신.
                deviceAdpt.notifyDataSetChanged();
            }
            else{
                Toast.makeText(getApplicationContext(),"삭제할 물건을 선택하세요.",Toast.LENGTH_SHORT).show();
            }
        }
        //textState.setText("물건 찾기");
        stateFlag = 0;  //다시 찾기 상태
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String sn = intent.getStringExtra(BluetoothDevice.EXTRA_UUID);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                Log.e("rssi", "name : " + device.getName() + "(sn."+ device.getAddress() +") => " + rssi + "dBm\n");
                MatchDevice(device.getAddress(),rssi);
            }
        }
    };
    private boolean MatchDevice(String those_num, int rssi){
        String my_num = selectedDevice.getSerialNum();
        //검색된 넘버를 replace하기
        those_num = those_num.replace(":","");
        Log.e("replace sn", those_num + " my device : " + my_num);
        //일치 한다면 toast하고 true
        if(my_num.equals(those_num)){
            Toast.makeText(getApplicationContext(),"물건이 주위에 있습니다. -> " + rssi + "dBm",Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    // 추후에 내부 디비 연동해서 불러오기.
    public void InitializeDevices()
    {

        DeviceManager.devices = sqliteDb.dbDevice.getDevices();
        devices = DeviceManager.devices;

        /*devices.add(new Device("차키", "sn.1234567",true));
        devices.add(new Device("에어팟", "sn.1234568",true));
        devices.add(new Device("지갑", "sn.1234569",false));*/
        Log.e("sql log", "devices : " + DeviceManager.devices);
    }
    private void checkLocationPermissions(){
        // Get permission
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(this, permission_list,  1);
    }
    private void enableBluetooth(){
        // Enable bluetooth
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!BTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    private void startService() {
        // 서비스 시작하기
        Intent intent = new Intent(
                getApplicationContext(),//현재제어권자
                TheftModeService.class); // 이동할 컴포넌트
        startService(intent); // 서비스 시작
    }
// 이와 같이 모든 경우에 서비스로부터 받은 인텐트가 처리 될 수 있도록한다.
// 이제 processCommand() 메서드 정의.

//    private void processCommand(Intent intent) {
//        if (intent != null) {
//            String command = intent.getStringExtra("command");
//            String name = intent.getStringExtra("name");
//
//            Toast.makeText(this, "서비스로부터 전달받은 데이터: " + command + ", " + name, Toast.LENGTH_LONG).show();
//        }
//    }
    private void makeNotification(){
        //Notification 인스턴스를 만들고 builder를 통해 세부 내용을 설정한다.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        //빌더를 통해 아이콘, 타이틀, 내용을 설정한다.
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("도난의심")
                .setContentText("현재 물건이 근처에 존재하지 않습니다. 확인해보시기 바랍니다.")
                .setVibrate(new long[]{1000,2000,1000,3000,1000,4000});
        //컬러를 설정한다.
        builder.setColor(Color.RED);
        // 사용자가 탭을 클릭하면 자동 제거
        builder.setAutoCancel(true);
        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "어디갔노 채널",
                    NotificationManager.IMPORTANCE_DEFAULT));
        }
        // id값은
        // 정의해야하는 각 알림의 고유한 int값
        notificationManager.notify(1, builder.build());
    }
    public void getFindForm() {
        this.BTAdapter.cancelDiscovery();
        Intent intent = new Intent(getApplicationContext(), FindForm.class);
        startActivity(intent);
    }
}