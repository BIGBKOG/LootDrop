# ProGuard & R8 Optimization Rules for LootDrop

# Preserve SourceFile and LineNumberTable for useful crash traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Moshi Codegen Adapters (@JsonClass(generateAdapter = true))
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepnames @com.squareup.moshi.JsonClass class *
-keep class **JsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}

# Retrofit Service Interfaces (method signatures & generic return types)
-keep,allowshrinking,allowobfuscation interface com.example.data.remote.GamerPowerApiService {
    <methods>;
}
-keep,allowshrinking,allowobfuscation interface com.example.data.remote.CheapSharkApiService {
    <methods>;
}

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ViewModels & AndroidViewModel reflection instantiation (ViewModelProvider)
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    public <init>(android.app.Application);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>();
}

# Suppress benign missing-reference warnings from networking/image libraries
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn coil.**

# R8 Bytecode & DEX Optimization Rules
-repackageclasses ''
-allowaccessmodification

# Strip verbose, debug, and info logging in release builds to reduce DEX bytecode
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
