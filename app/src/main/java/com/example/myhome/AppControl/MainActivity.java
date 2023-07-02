package com.example.myhome.AppControl;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myhome.R;
import com.example.myhome.maincontroller.DashboardFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String DEVICE_ADDRESS = "00:20:04:BD:4D:0C"; // Bluetooth module address
    private static final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int PERMISSION_REQUEST_CODE = 1;
private DashboardFragment dashboardFragment;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private TextView textViewStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        Button buttonOn = findViewById(R.id.button_on);
        Button buttonOff = findViewById(R.id.button_off);
        textViewStatus = findViewById(R.id.text_status);

        buttonOn.setOnClickListener(v -> sendData("1"));

        buttonOff.setOnClickListener(v -> sendData("0"));

        connectBluetooth();
    }

    private void connectBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Bluetooth is not supported on this device
            return;
        }

        if (checkBluetoothPermissions()) {
            performBluetoothConnection(bluetoothAdapter);
        } else {
            requestBluetoothPermissions();
        }
    }

    private boolean checkBluetoothPermissions() {
        int bluetoothPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
        int bluetoothAdminPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN);

        return bluetoothPermission == PackageManager.PERMISSION_GRANTED
                && bluetoothAdminPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
            }, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                performBluetoothConnection(bluetoothAdapter);
            } else {
                Toast.makeText(this, "Bluetooth permissions are required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void performBluetoothConnection(BluetoothAdapter bluetoothAdapter) {
        try {
            if (!bluetoothAdapter.isEnabled()) {
                // Bluetooth is disabled, request to enable it
                bluetoothAdapter.enable();
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            BluetoothDevice device = null;

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice pairedDevice : pairedDevices) {
                    if (pairedDevice.getAddress().equals(DEVICE_ADDRESS)) {
                        device = pairedDevice;
                        break;
                    }
                }
            }

            if (device == null) {
                // Device not found or not paired with Bluetooth module
                return;
            }

            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();

            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

            // Start a separate thread to listen for incoming data from Arduino
            ReceiveDataThread receiveDataThread = new ReceiveDataThread();
            receiveDataThread.start();
//            dashboardFragment = new DashboardFragment();
//            dashboardFragment.setMainActivity(MainActivity.this);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
            // Handle SecurityException or IOException here
        }
    }

    private void sendData(String data) {
        try {
            if (outputStream != null) {
                outputStream.write(data.getBytes());
            } else {
                Toast.makeText(this, "Output stream is not initialized.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void updateStatus(final String status) {
        runOnUiThread(() -> textViewStatus.setText(status));
    }

    private class ReceiveDataThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    if (inputStream != null) {
                        bytes = inputStream.read(buffer);
                        final String data = new String(buffer, 0, bytes);
                        updateStatus(data);
                    } else {
                        Toast.makeText(MainActivity.this, "Input stream is not initialized.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
