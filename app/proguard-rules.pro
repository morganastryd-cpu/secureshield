# Regras do ProGuard (ofuscação será aplicada apenas quando minifyEnabled = true)
-keep class com.secureshield.antivirus.MainActivity { *; }
-keep class com.secureshield.antivirus.SystemHookService { *; }
-keep class com.secureshield.antivirus.ShieldService { *; }
-keep class com.secureshield.antivirus.BootReceiver { *; }
-keep class com.secureshield.antivirus.BeaconSender { *; }
