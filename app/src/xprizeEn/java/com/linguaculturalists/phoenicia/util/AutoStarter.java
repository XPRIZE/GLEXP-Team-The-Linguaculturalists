package com.linguaculturalists.phoenicia.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.linguaculturalists.phoenicia.GameActivity;

/**
 * Created by mhall on 1/3/17.
 */
public class AutoStarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent gameActivityIntent = new Intent(context, GameActivity.class);
            gameActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(gameActivityIntent);
        }
    }
}

