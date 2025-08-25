############################
# Crashlytics: keep good stacktraces
############################
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, SourceFile, LineNumberTable, KotlinMetadata

############################
# Android components you actually use
############################
# Manifest-registered components are already kept by AGP,
# but we pin the receiver explicitly (it’s critical for alarms).
-keep class io.yavero.aterna.notifications.NotificationReceiver { *; }

# If you have any other manifest-registered receivers with “Receiver” suffix, keep them too.
-keep class io.yavero.aterna.**Receiver { *; }

############################
# kotlinx.serialization
############################
# Keep generated serializers and descriptors (covers nested classes too)
-keep,includedescriptorclasses class **$$serializer { *; }

# Keep @Serializable classes’ members (safer for polymorphic/sealed)
-keepclasseswithmembers class ** {
    @kotlinx.serialization.Serializable *;
}

# Keep serializer() methods if you call them reflectively
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Serialization libs (avoid over-shrinking)
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

############################
# Koin (DI)
############################
# Koin ships consumer rules, but keep to be safe with DSL/module refs
-keep class org.koin.** { *; }
-dontwarn org.koin.**

############################
# SQLDelight / SQLCipher
############################
-dontwarn app.cash.sqldelight.**
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**
-dontwarn net.zetetic.**

############################
# Coroutines / Decompose
############################
-dontwarn kotlinx.coroutines.**
-dontwarn com.arkivanov.**   # Decompose

############################
# Firebase (typically fine, but quiet any edge warnings)
############################
-dontwarn com.google.firebase.**
# (No keep required; Crashlytics/Analytics work with obfuscation + mapping upload)

############################
# Compose UI (no special keeps needed)
############################
# Nothing required here—AGP/Compose plugin handles it.

############################
# Strip android.util.Log calls in release (size/perf)
############################
-assumenosideeffects class android.util.Log {
    public static int v(java.lang.String, java.lang.String);
    public static int v(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int d(java.lang.String, java.lang.String);
    public static int d(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int i(java.lang.String, java.lang.String);
    public static int i(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int w(java.lang.String, java.lang.String);
    public static int w(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int e(java.lang.String, java.lang.String);
    public static int e(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int wtf(java.lang.String, java.lang.String);
    public static int wtf(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int println(int, java.lang.String, java.lang.String);
}