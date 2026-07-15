# ============================================
# Reglas ProGuard para nuxnamdeep
# ============================================

# ============================================
# Reglas básicas para Android
# ============================================

# Keep all Android framework classes
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class * extends android.widget.BaseAdapter
-keep public class * extends android.os.AsyncTask

# Keep all view constructors
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep all parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep all serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep all R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep all annotations
-keepattributes *Annotation*

# ============================================
# Reglas para Kotlin
# ============================================

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Kotlin reflection
-keep class kotlin.reflect.** { *; }

# Keep Kotlin internal classes
-keep class kotlin.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.internal.** { *; }
-keep class kotlinx.coroutines.scheduling.** { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# Keep Kotlin standard library
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ============================================
# Reglas para Gson
# ============================================

# Keep all classes with @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep all classes used by Gson
-keep class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep all model classes
-keep class com.nuxnamdeep.models.** { *; }

# Gson generic types
-keep class com.google.gson.** { *; }
-keepclassmembers class com.google.gson.** {
    *;
}
-dontwarn com.google.gson.**

# ============================================
# Reglas para MIDI (android.media.midi)
# ============================================

# Keep MIDI classes
-keep class android.media.midi.** { *; }
-keepclassmembers class android.media.midi.** {
    *;
}
-dontwarn android.media.midi.**

# ============================================
# Reglas para permisos y servicios
# ============================================

# Keep FileProvider
-keep class androidx.core.content.FileProvider { *; }
-keepclassmembers class androidx.core.content.FileProvider {
    *;
}

# Keep PermissionX
-keep class com.guolindev.permissionx.** { *; }
-dontwarn com.guolindev.permissionx.**

# ============================================
# Optimizaciones
# ============================================

# Optimize aggressively
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively

# Keep source file and line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep signature for generic types
-keepattributes Signature

# Keep exceptions
-keepattributes Exceptions

# ============================================
# Reglas para reducir el tamaño del APK
# ============================================

# Remove unused code
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

# ============================================
# Reglas específicas del proyecto
# ============================================

# Keep main activity
-keep class com.nuxnamdeep.MainActivity { *; }

# Keep converter classes
-keep class com.nuxnamdeep.converter.** { *; }

# Keep service classes
-keep class com.nuxnamdeep.services.** { *; }

# Keep utility classes
-keep class com.nuxnamdeep.utils.** { *; }

# ============================================
# Reglas para JSON (archivos NAM)
# ============================================

# Keep all fields in data classes
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep all data classes
-keep class com.nuxnamdeep.models.** {
    *;
}
