package app.bqlab.bmiscale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    String data, today;
    int height, weight, bmi;
    boolean isConnected;
    //objects
    BluetoothSPP mBluetooth;
    SharedPreferences mHeightPref, mWeightPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "장치와 연결되는 중입니다.", Toast.LENGTH_LONG).show();
            mBluetooth.connect(Objects.requireNonNull(data));
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void init() {
        //initialing
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
            }

            @Override
            public void onDeviceConnectionFailed() {
                Toast.makeText(MainActivity.this, "장치와 연결할 수 없습니다.", Toast.LENGTH_LONG).show();
            }
        });
        mBluetooth.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                MainActivity.this.data = message;
            }
        });
        findViewById(R.id.main_height).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetooth.getServiceState() == BluetoothState.STATE_CONNECTED) {
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
                                                        bmi = weight / (height * height);
                                                        ((TextView) findViewById(R.id.main_bmi)).setText(bmi);
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
                if (isConnected) {
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
                                                        ((TextView) findViewById(R.id.main_bmi)).setText(bmi);
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
        } else
            startActivity(new Intent(this, DeviceList.class));
    }
}
