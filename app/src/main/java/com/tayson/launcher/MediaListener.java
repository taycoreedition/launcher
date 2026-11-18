package com.tayson.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver opcional para notificações de pacotes/armazenamento.
 * A Activity também regista um receiver em execução para atualizar os banners.
 */
public class MediaListener extends BroadcastReceiver {
    public static final String ACTION_APPS_CHANGED = "com.tayson.launcher.APPS_CHANGED";

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)
                || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_REPLACED.equals(action)
                || Intent.ACTION_MEDIA_MOUNTED.equals(action)
                || Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
            Intent update = new Intent(ACTION_APPS_CHANGED);
            update.setPackage(context.getPackageName());
            context.sendBroadcast(update);
        }
    }
}
