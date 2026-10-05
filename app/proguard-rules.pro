# Questlog — ProGuard / R8 rules

# --- Retrofit ---
# Retrofit interfaces are reflectively instantiated.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# --- Gson ---
# Keep generic signatures for Gson deserialization.
-keepattributes Signature
-keep class com.questlog.app.core.network.rawg.** { *; }
-keep class com.google.gson.** { *; }

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- Hilt ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# --- ML Kit ---
-keep class com.google.mlkit.** { *; }

# --- Solana / Mobile Wallet Adapter ---
-keep class com.solanamobile.** { *; }
-keep class com.solana.** { *; }
-keep class com.funkatronics.** { *; }
-keep class io.github.funkatronics.** { *; }
-dontwarn com.solanamobile.**
-dontwarn com.solana.**
-dontwarn com.funkatronics.**
-dontwarn io.github.funkatronics.**

# --- Kotlin coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- Kotlin serialization (if used) ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# --- Coil ---
-dontwarn coil3.**
