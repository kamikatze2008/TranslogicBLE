package com.syzygy.translogic.translogicble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothService bluetoothService;

//test client URL
//    public static final String WEB_VIEW_LOAD_URL = "http://www.shout-ed.com/itmstest/uploadtest.php";

//photo client URL
//    public static final String WEB_VIEW_LOAD_URL = "https://unsplash.polarr.co";

    //realURL
    public static final String WEB_VIEW_LOAD_URL = "http://app.itmsretail.net.au";

    private WebView webView;
    private CommandParser.Command command;

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
    private static final int REQUEST_PERMISSIONS = 3;
    private final static int CAPTURE_RESULTCODE = 4;

    private ValueCallback<Uri[]> mUploadMessage;
    private String filePath;

    private IncomingHandler handler = new IncomingHandler(this);

    static class IncomingHandler extends Handler {
        private final WeakReference<MainActivity> mainActivityWeakReference;

        IncomingHandler(MainActivity service) {
            mainActivityWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mainActivityWeakReference.get();
            if (mainActivity != null) {
                mainActivity.handleMessage(msg);
            }
        }
    }

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
                int bytes = msg.arg1;
                byte[] readBuf = Arrays.copyOfRange(((byte[]) msg.obj), 0, bytes);

                if (bytes == 1 && command == null) {
                    command = CommandParser.parseValue(readBuf);
                } else if (bytes > 0 && command != null && command != CommandParser.Command.UNKNOWN) {
                    try {
                        command.setValue(Double.valueOf(new String(readBuf)));
                    } catch (NumberFormatException e) {
                        command = null;
                        return;
                    }
                    CommandParser.Command tempCommand = command;
                    insertValueIfPossible(tempCommand);
                    command = null;
                } else {
                    command = null;
                }
                break;
            case MESSAGE_DEVICE_NAME:
                String connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                        + connectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                        Toast.LENGTH_SHORT).show();
                break;
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
            int phoneStatePermissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE);
            int readExternalStoragePermissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeExternalStoragePermissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (phoneStatePermissionCheck == PackageManager.PERMISSION_GRANTED &&
                    readExternalStoragePermissionCheck == PackageManager.PERMISSION_GRANTED &&
                    writeExternalStoragePermissionCheck == PackageManager.PERMISSION_GRANTED) {
                initWebView();
            }
        }
    }

    private void initWebView() {
        webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.contains("login.php")) {
                    Log.d(TAG, "start");
                    Log.d(TAG, url);
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    String uniqueId = "" + Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + telephonyManager.getDeviceId();
                    Log.d(TAG, uniqueId);
                    view.evaluateJavascript("javascript:(function(){" +
                            "var deviceName = document.getElementById(\"fingerprint\");" +
                            "if(deviceName!=null){" +
                            "deviceName.value=\"" + uniqueId + "\";" +
                            "}" +
                            "})()", value -> Log.d(TAG, "finish"));
                }
            }

//                @Override
//                public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                    if (url != null) {
//                        url = url.replace("inspector.php", "inspector3.php");
//                    }
//                    webView.loadUrl(url);
//                    return true;
//                }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadMessage = filePathCallback;
                File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_name));
                imageStorageDir.mkdirs();
                filePath = imageStorageDir + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                File file = new File(filePath);
                Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
//                MainActivity.this.startActivityForResult(captureIntent, CAPTURE_RESULTCODE);

                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(i, getString(R.string.chooser_intent_string));

                // Set camera intent to file chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                        , new Parcelable[]{captureIntent});

                // On select image call onActivityResult method of activity
                startActivityForResult(chooserIntent, CAPTURE_RESULTCODE);
                return true;
            }
        });
        webView.loadUrl(WEB_VIEW_LOAD_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!btAdapter.isEnabled()) {
            sendRequestBTIntent();
        } else if (bluetoothService == null) {
            bluetoothService = new BluetoothService(this, handler);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0) {
                boolean isAllGranted = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false;
                        break;
                    }
                }
                if (isAllGranted) {
                    initWebView();
                    return;
                }
            }
            System.exit(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int phoneStatePermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE);
        int readExternalStoragePermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeExternalStoragePermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (phoneStatePermissionCheck == PackageManager.PERMISSION_DENIED
                || readExternalStoragePermissionCheck == PackageManager.PERMISSION_DENIED
                || writeExternalStoragePermissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        }
        if (bluetoothService != null && bluetoothService.getState() == BluetoothService.STATE_NONE) {
            bluetoothService.reconnectIfPossibleOrStart(false);
        }

        //TODO remove mock on release
//        for (int i = 1; i <= 5; i++) {
//            new Handler(Looper.getMainLooper()).postDelayed(this::getMockOutput, i * 10000);
//        }
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
        CommandParser.Command command = CommandParser.Command.PRESSURE;
        command.setValue(24.56);
        insertValueIfPossible(command);
    }

    private void insertValueIfPossible(CommandParser.Command command) {
        webView.evaluateJavascript("(function (){\n" +
                "var activeElement = document.activeElement;" +
                "if(activeElement.name.indexOf(\"" + command.getTagName() + "\") == 0){\n" +
                "activeElement.value=" + command.getValue() + ";\n" +
                "var e = document.createEvent('HTMLEvents');\n" +
                "e.initEvent('keyup', false, true);\n" +
                "activeElement.dispatchEvent(e);" +
                "}" +
                "return activeElement.name})()", value -> Log.d(TAG, value));
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
                    bluetoothService.connect(device, false);
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

            case CAPTURE_RESULTCODE: {
                if (mUploadMessage != null) {
                    if (resultCode != RESULT_OK && !new File(filePath).exists()) {
                        this.mUploadMessage.onReceiveValue(null);
                    } else {
                        Uri uri = null;
                        if (data != null) {
                            uri = data.getData();
                        }
                        if (uri == null) {
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.DATA, this.filePath);
                            uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        }
                        mUploadMessage.onReceiveValue(new Uri[]{uri});
                    }
                    this.mUploadMessage = null;
                }
            }
            break;
        }
    }
}