package com.syzygy.translogic.translogicble;

import android.app.Application;
import android.content.Intent;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MainApplication extends Application {
    public void onCreate() {
        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
    }

    private void handleUncaughtException(Thread thread, Throwable e) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"s.denysov@szg-tech.com"});
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        intent.putExtra(Intent.EXTRA_TEXT, sw.toString());
        startActivity(intent);
        System.exit(1);
    }
}

