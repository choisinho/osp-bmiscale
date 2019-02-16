package app.bqlab.bmiscale;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothService;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {

    //constants
    final String REQUEST_HEIGHT = "1";
    final String REQUEST_WEIGHT = "2";
    //variables
    String data, today;
    int height, weight, bmi;
    boolean heightConnected, weightConnected;
    //objects
    BluetoothSPP mBluetooth;
    SharedPreferences mHeightPref, mWeightPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        connectToDevice();
    }

    @SuppressLint("SimpleDateFormat")
    private void init() {
        //initialing
        mBluetooth = new BluetoothSPP(this);
        mHeightPref = getSharedPreferences("height", MODE_PRIVATE);
        mWeightPref = getSharedPreferences("weight", MODE_PRIVATE);
        today = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        //setting
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_height).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (heightConnected) {
                    mBluetooth.send(REQUEST_HEIGHT, true);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("신장 등록")
                            .setMessage("오늘의 신장을 등록합니다. 장치의 전원을 키고 다음 버튼을 누르세요.")
                            .setPositiveButton("다음", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String s = data + "가 맞다면 확인 버튼을 누르세요.";
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage(s)
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String s = data + "cm";
                                                    height = Integer.parseInt(data);
                                                    mHeightPref.edit().putString(today, data).apply();
                                                    ((Button) findViewById(R.id.main_height)).setText(s);
                                                    if (weight != 0) {
                                                        bmi = weight * (height * height);
                                                        ((TextView)findViewById(R.id.main_bmi)).setText(bmi);
                                                    }
                                                }
                                            }).show();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                } else {
                    connectToDevice();
                }
            }
        });
        findViewById(R.id.main_weight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (heightConnected) {
                    mBluetooth.send(REQUEST_WEIGHT, true);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("체중 등록")
                            .setMessage("오늘의 체중을 등록합니다. 장치의 전원을 키고 다음 버튼을 누르세요.")
                            .setPositiveButton("다음", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String s = data + "가 맞다면 확인 버튼을 누르세요.";
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage(s)
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String s = data + "cm";
                                                    weight = Integer.parseInt(data);
                                                    mWeightPref.edit().putString(today, data).apply();
                                                    ((Button) findViewById(R.id.main_weight)).setText(s);
                                                    if (height != 0) {
                                                        bmi = weight * (height * height);
                                                        ((TextView)findViewById(R.id.main_bmi)).setText(bmi);
                                                    }
                                                }
                                            }).show();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                } else {
                    connectToDevice();
                }
            }
        });
    }

    private void connectToDevice() {
        if (!mBluetooth.isBluetoothAvailable()) {
            Toast.makeText(this, "지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        } else if (!mBluetooth.isBluetoothEnabled()) {
            Toast.makeText(this, "블루투스가 활성화되지 않았습니다.", Toast.LENGTH_LONG).show();
        } else if (!mBluetooth.isServiceAvailable()) {
            mBluetooth.setupService();
            mBluetooth.startService(BluetoothState.DEVICE_OTHER);
            startActivity(new Intent(this, DeviceList.class));
        }
    }
}
