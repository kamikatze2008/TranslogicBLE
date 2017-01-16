package com.syzygy.translogic.translogicble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    //    private static final String DEVICE_NAME_PREFIX = "Trans-Logik";
    private static final String DEVICE_NAME_PREFIX = "Macbook Pro — Bogdan";

    //        public static final String WEB_VIEW_LOAD_URL = "file:///android_asset/temp.html";
    public static final String WEB_VIEW_LOAD_URL = "http://shout-ed.com/itmstest";
    private static final String[] POSSIBLE_IDS = new String[]{"\"tread1\"", "\"tread2\"", "\"tread3\"", "\"tread4\"", "\"psi\""};
    private static final String ACTIVE_ELEMENT_ID_EQUALS_TEMPLATE = "activeElement.name === ";
    private ConnectedThread connectedThread;

    private final BroadcastReceiver bluetoothEnablingBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if (btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    sendRequestBTIntent();
                }
            }
        }
    };

    private final BroadcastReceiver discoveringDevicesBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String deviceName = device.getName();
                    Log.d(TAG, deviceName + Arrays.toString(device.getUuids()));
                    if (deviceName != null && deviceName.startsWith(DEVICE_NAME_PREFIX)) {
                        try {
                            btAdapter.cancelDiscovery();
                            ParcelUuid[] parcelUuids = device.getUuids();
                            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                            if (parcelUuids != null && parcelUuids[0] != null) {
                                uuid = parcelUuids[0].getUuid();
                            }
                            BluetoothSocket bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                            bluetoothSocket.connect();
                            new ConnectedThread(bluetoothSocket).start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (btAdapter == null) {
            finish();
            return;
        }
        registerReceiver(bluetoothEnablingBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        IntentFilter discoveringIntentFilter = new IntentFilter();
        discoveringIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        discoveringIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        discoveringIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveringDevicesBroadcastReceiver, discoveringIntentFilter);
        if (webView == null) {
            webView = (WebView) findViewById(R.id.web_view);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    webView.loadUrl(url);
                    return true;
                }
            });
            webView.setWebChromeClient(new WebChromeClient());
            webView.loadUrl(WEB_VIEW_LOAD_URL);
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(bluetoothEnablingBroadcastReceiver);
        unregisterReceiver(discoveringDevicesBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!btAdapter.isEnabled()) {
            sendRequestBTIntent();
        } else {
            //FIXME add normal scan
            for (int i = 1; i <= 5; i++)
                new Handler(Looper.getMainLooper()).postDelayed(this::scanDevices, i * 10000);
        }
    }

    private void sendRequestBTIntent() {
        Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                scanDevices();
            } else {
                sendRequestBTIntent();
            }
        }
    }

    private void scanDevices() {
        Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();
        for (BluetoothDevice bluetoothDevice : bondedDevices) {
            if (bluetoothDevice.getName().startsWith(DEVICE_NAME_PREFIX)) {
                try {
                    ParcelUuid[] parcelUuids = bluetoothDevice.getUuids();
                    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                    if (parcelUuids != null && parcelUuids[0] != null) {
                        uuid = parcelUuids[0].getUuid();
                    }
                    BluetoothSocket bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                    bluetoothSocket.connect();
                    new ConnectedThread(bluetoothSocket).start();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        btAdapter.startDiscovery();
//        scanForDevice();
    }

    private void scanForDevice() {
        transferMessageToOutputString(new byte[]{
                (byte) (0xa2 & 0xff), 0x50, (byte) (0xa5 & 0xff), 0x30, 0x30, 0x30, 0x32, (byte) (0xa7 & 0xff)
        });
    }

    private void transferMessageToOutputString(byte[] bytes) {
        if (bytes != null) {
            insertValueIfPossible(new String(Arrays.copyOfRange(bytes, 3, 7)));
        }
    }

    private void insertValueIfPossible(String receivedValue) {
        webView.evaluateJavascript("(function (){\n" +
                "var activeElement = document.activeElement;" +
                "if(" + buildCheckIdString() + "){\n" +
                "activeElement.value=" + receivedValue + ";\n" +
                "}" +
                "return activeElement.name})()", value -> Log.d(TAG, value));
    }

    private String buildCheckIdString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < POSSIBLE_IDS.length; i++) {
            stringBuilder.append(ACTIVE_ELEMENT_ID_EQUALS_TEMPLATE)
                    .append(POSSIBLE_IDS[i]);
            if (i < POSSIBLE_IDS.length - 1) {
                stringBuilder.append(" || ");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    runOnUiThread(() -> transferMessageToOutputString(mmBuffer));
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}