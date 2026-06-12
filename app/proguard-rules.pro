# ============================================================
# VremeaRomâniei - ProGuard / R8 Rules
# ============================================================

# ---- General Android ----
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exception
-keepattributes SourceFile, LineNumberTable
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations

# ---- BuildConfig ----
-keep class com.vremea.romaniei.BuildConfig { *; }

# ---- App models / DTOs (kotlinx.serialization) ----
# Keep all @Serializable classes and their companion serializers
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.vremea.romaniei.**$$serializer { *; }
-keepclassmembers class com.vremea.romaniei.** {
    *** Companion;
}
-keepclasseswithmembers class com.vremea.romaniei.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep all @Serializable annotated classes
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.Serializable
-keep,allowobfuscation,allowshrinking @kotlinx.serialization.Serializable class com.vremea.romaniei.** { *; }

# ---- Retrofit + OkHttp ----
-keep,allowobfuscation,allowshrinking interface com.vremea.romaniei.data.remote.*Api
-keep,allowobfuscation,allowshrinking interface retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ---- Room (KSP-generated) ----
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class com.vremea.romaniei.** { *; }
-keep @androidx.room.Dao interface com.vremea.romaniei.** { *; }
-keepclassmembers class com.vremea.romaniei.data.local.AppDatabase* { *; }
-keepclassmembers class com.vremea.romaniei.data.local.dao.* { *; }
-keepclassmembers class com.vremea.romaniei.data.local.entity.* { *; }

# ---- MapLibre Native (JNI + native libs) ----
# MapLibre uses JNI heavily; keep all native methods and entry points
-keep class org.maplibre.android.** { *; }
-keepclassmembers class org.maplibre.android.** {
    native <methods>;
}
-dontwarn org.maplibre.android.**
-dontwarn com.mapbox.mapboxsdk.**

# ---- WorkManager ----
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ForegroundInfo { *; }
-keep class * extends android.content.BroadcastReceiver { *; }

# ---- Coroutines ----
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }

# ---- Kotlin ----
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.reflect.** { *; }
-dontwarn kotlin.**

# ---- Coil ----
-keep class coil.** { *; }
-dontwarn coil.**

# ---- Google Play Services ----
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.**

# ---- DataStore ----
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ---- Compose ----
# Keep all Compose classes (framework + material3)
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ---- Lottie ----
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ---- Joda / ThreeTen ----
-dontwarn org.joda.**
-dontwarn org.threeten.**

# ---- Miscellaneous AndroidX ----
-dontwarn androidx.**
-dontnote androidx.**

# ---- Debug-specific (stripped in release) ----
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
