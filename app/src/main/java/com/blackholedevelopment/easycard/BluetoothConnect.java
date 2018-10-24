package com.blackholedevelopment.easycard;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnect extends AppCompatActivity {

    private Button btnBluetooth;

    private String MAC_ADDRESS;
    private static final int REQUEST_CONNECTION_BT = 2;
    private static final int MESSAGE_READ = 3;

    private Boolean connected = false;

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDevice bluetoothDevice = null;
    private BluetoothSocket bluetoothSocket = null;

    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    ConnectedThread connectedThread;

    Handler mHandler;
    StringBuilder bluetoothData = new StringBuilder();

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btnBluetooth = findViewById(R.id.btnBluetooth);

        if (!connected){
            //MOSTRA BOTAO DE CONECTAR
        }else{
            //mostra tela de esperando rfid
        }

        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothAdapter.isEnabled()) {
                    if (!connected) {
                        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                        Set<BluetoothDevice> unpairedDevices = new HashSet<BluetoothDevice>(mDeviceList);//
                        unpairedDevices.removeAll(pairedDevices);

                        if ((unpairedDevices == null || unpairedDevices.size() == 0) && (pairedDevices == null || pairedDevices.size() == 0)) {
                            Toast.makeText(BluetoothConnect.this, "Nenhum dispositivo encontrado", Toast.LENGTH_SHORT).show();
                        } else {
                            ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

                            list.addAll(pairedDevices);

                            Intent intent = new Intent(BluetoothConnect.this, DevicesList.class);

                            intent.putParcelableArrayListExtra("device.list", list);

                            startActivityForResult(intent, REQUEST_CONNECTION_BT);
                        }
                    }else{
                        try {
                            bluetoothSocket.close();
                            connected = false;
                        } catch (IOException e) {

                        }
                    }
                }
            }
        });

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_READ){
                    String recived = (String)msg.obj;
                    bluetoothData.append(recived);

                    int informationEnds = bluetoothData.indexOf("}");
                    if (informationEnds > 0){
                        String allData = bluetoothData.substring(0,informationEnds);
                        int dataSize = allData.length();
                        if (bluetoothData.charAt(0) == '{'){
                            String finalData = bluetoothData.substring(1,dataSize);
                            Toast.makeText(BluetoothConnect.this, finalData, Toast.LENGTH_SHORT).show();
                            if (finalData.contains("easycard")){
                                //intent levando pra outra tela
                            }
                        }
                        bluetoothData.delete(0,bluetoothData.length());
                    }

                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode){
            case REQUEST_CONNECTION_BT:
                if (resultCode == RESULT_OK){
                    MAC_ADDRESS = data.getExtras().getString(DevicesList.MAC_ADDRESS);
                    bluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC_ADDRESS);
                    try {
                        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                        bluetoothSocket.connect();
                        connectedThread = new ConnectedThread(bluetoothSocket);
                        connectedThread.start();
                        Toast.makeText(this, "Conectado a: " + MAC_ADDRESS, Toast.LENGTH_SHORT).show();
                        connected = true;
                    }catch (IOException erro){
                        Toast.makeText(this, "Falha ao conectar-se", Toast.LENGTH_SHORT).show();
                        connected = false;
                    }
                }
                break;
        }

    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);

                    String btData = new String(buffer,0, bytes);

                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, btData)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void sendData(String data) {
            byte [] msgBuffer = data.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) { }
        }

    }
}
