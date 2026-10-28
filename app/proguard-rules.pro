# ProGuard & R8 Optimization Rules for LootDrop

# Preserve SourceFile and LineNumberTable for useful crash traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Moshi & JSON Data Models
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keepclassmembers class * {
    @com.squareup.moshi.JsonClass *;
}
-keep class com.example.data.model.** { *; }
-keep class com.example.data.remote.** { *; }

# Room Database & Entities
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.data.local.** { *; }
-dontwarn androidx.room.paging.**

# Retrofit & OkHttp
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Coil Image Loader
-keep class coil.** { *; }
-dontwarn coil.**

# Android App Widgets (RemoteViews Providers)
-keep class com.example.widget.** { *; }
