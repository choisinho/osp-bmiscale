package app.bqlab.bmiscale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.AlgorithmConstraints;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothService;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {

    //constants
    final String REQUEST_HEIGHT = "1";
    final String REQUEST_WEIGHT = "2";
    //variables
    String today;
    int height, weight, bmi;
    //objects
    BluetoothSPP mBluetooth;
    SharedPreferences mHeightPref, mWeightPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        connectToDevice();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                mBluetooth.connect(data);
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void init() {
        //initialing
        if (mBluetooth != null) {
            mBluetooth.disconnect();
            mBluetooth.stopService();
            mBluetooth = null;
        }
        mBluetooth = new BluetoothSPP(this);
        mHeightPref = getSharedPreferences("height", MODE_PRIVATE);
        mWeightPref = getSharedPreferences("weight", MODE_PRIVATE);
        today = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        //setting
        setTitle(today);
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StatsActivity.class));
            }
        });
        mBluetooth.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            @Override
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(MainActivity.this, "장치와 연결되었습니다.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDeviceDisconnected() {
                Toast.makeText(MainActivity.this, "장치와의 연결이 끊겼습니다.", Toast.LENGTH_LONG).show();
                connectToDevice();
            }

            @Override
            public void onDeviceConnectionFailed() {
                Toast.makeText(MainActivity.this, "장치와 연결할 수 없습니다.", Toast.LENGTH_LONG).show();
                connectToDevice();
            }
        });
        mBluetooth.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                if (Integer.parseInt(message) >= 120) {
                    mHeightPref.edit().putString(getPreviousDay(0), message).apply();
                    ((TextView) findViewById(R.id.main_height)).setText(message);
                } else {
                    mWeightPref.edit().putString(getPreviousDay(0), message).apply();
                    ((TextView) findViewById(R.id.main_weight)).setText(message);
                }
                if (!Objects.equals(mHeightPref.getString(getPreviousDay(0), "0"), "0")
                        && !Objects.equals(mWeightPref.getString(getPreviousDay(0), "0"), "0")) {
                    int height = Integer.parseInt(Objects.requireNonNull(mHeightPref.getString(getPreviousDay(0), "0")));
                    int weight = Integer.parseInt(Objects.requireNonNull(mWeightPref.getString(getPreviousDay(0), "0")));
                    ((TextView) findViewById(R.id.main_bmi)).setText(String.format("%.2f",(float) weight / ((float) height * (float) height) * 10000f ));
                }
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private String getPreviousDay(int ago) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Calendar.getInstance().getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -ago);
        return new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
    }

    private void connectToDevice() {
        if (!this.mBluetooth.isBluetoothAvailable()) {
            Toast.makeText(this, "지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        } else if (!this.mBluetooth.isBluetoothEnabled()) {
            Toast.makeText(this, "블루투스를 활성화해야 합니다.", Toast.LENGTH_LONG).show();
            finishAffinity();
        } else if (!this.mBluetooth.isServiceAvailable()) {
            this.mBluetooth.setupService();
            this.mBluetooth.startService(BluetoothState.DEVICE_OTHER);
            connectToDevice();
        } else if (mBluetooth.getServiceState() != BluetoothState.STATE_CONNECTED) {
            startActivityForResult(new Intent(getApplicationContext(), DeviceList.class), BluetoothState.REQUEST_CONNECT_DEVICE);
            Toast.makeText(this, "연결할 디바이스를 선택하세요.", Toast.LENGTH_LONG).show();
        }
    }
}
