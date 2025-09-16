# proguard-rules.pro
-dontwarn **
-ignorewarnings
-dontnote **

-keep class kotlinx.coroutines.swing.*
-keep class com.arkivanov.decompose.**
-keep class io.ktor.** { *; }

-keepattributes Annotation, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt

# Keep Serializers
# https://github.com/Kotlin/kotlinx.serialization/issues/1105
-keep,includedescriptorclasses class **.data.model.* { *; }
-keepclassmembers class **.data.model.* {
    *** Companion;
}
-keepclasseswithmembers class **.data.model.* {
    kotlinx.serialization.KSerializer serializer(...);
}

# When kotlinx.serialization.json.JsonObjectSerializer occurs

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# a few steps for optimization
-optimizationpasses 7