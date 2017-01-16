package com.syzygy.translogic.translogicble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothService bluetoothService;

    //        public static final String WEB_VIEW_LOAD_URL = "file:///android_asset/temp.html";
    public static final String WEB_VIEW_LOAD_URL = "http://shout-ed.com/itmstest";
    private static final String[] POSSIBLE_IDS = new String[]{"\"tread1\"", "\"tread2\"", "\"tread3\"", "\"tread4\"", "\"psi\""};
    private static final String ACTIVE_ELEMENT_ID_EQUALS_TEMPLATE = "activeElement.name === ";

    private WebView webView;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;


    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

//    class SafeHandler extends Handler {
//        private final WeakReference<MainActivity> mTarget;
//
//        SafeHandler(MainActivity target) {
//            mTarget = new WeakReference<MainActivity>(target);
//        }
//
//        void doSomething() {
//            MainActivity target = mTarget.get();
//            if (target != null) target.do();
//

            // The Handler that gets information back from the BluetoothChatService
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a 0string from the buffer
//                    String writeMessage = new String(writeBuf);
//                    mAdapter.notifyDataSetChanged();
//                    messageList.add(new androidRecyclerView.Message(counter++, writeMessage, "Me"));
                    break;
                case MESSAGE_READ:
//                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
//                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    mAdapter.notifyDataSetChanged();
//                    messageList.add(new androidRecyclerView.Message(counter++, readMessage, mConnectedDeviceName));
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
//                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                    Toast.makeText(getApplicationContext(), "Connected to "
//                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
//                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
//                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            bluetoothService.write(send);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (btAdapter == null) {
            finish();
            return;
        }
        if (webView == null) {
            webView = (WebView) findViewById(R.id.web_view);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url != null) {
                        url = url.replace("inspector.php", "inspector3.php");
                    }
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
//        unregisterReceiver(discoveringDevicesBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!btAdapter.isEnabled()) {
            sendRequestBTIntent();
        } else {
            bluetoothService = new BluetoothService(this, handler);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothService != null && bluetoothService.getState() == BluetoothService.STATE_NONE) {
            bluetoothService.start();
        }

        //FIXME add normal scan
        for (int i = 1; i <= 5; i++) {
            new Handler(Looper.getMainLooper()).postDelayed(this::getMockOutput, i * 10000);
        }
    }

    public void onScanButtonClick(View v) {
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    private void sendRequestBTIntent() {
        Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);
    }

    private void getMockOutput() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = btAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    bluetoothService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    bluetoothService = new BluetoothService(this, handler);
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
}