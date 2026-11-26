# Release build: shrink + optimise, but keep readable names so crash reports stay readable (they are uploaded to the admin dashboard).
-dontobfuscate
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions, EnclosingMethod, SourceFile, LineNumberTable

# kotlinx.serialization (the app's data classes are decoded from the server's JSON)
-dontwarn kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.example.tattle.**$$serializer { *; }
-keepclassmembers class com.example.tattle.** { *** Companion; }
-keepclasseswithmembers class com.example.tattle.** { kotlinx.serialization.KSerializer serializer(...); }

# Ktor discovers its HTTP engine through ServiceLoader
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**
-dontwarn org.jetbrains.annotations.**
-dontwarn java.lang.management.**
-dontwarn kotlinx.coroutines.debug.**
-dontwarn javax.annotation.**
-dontwarn org.apache.log4j.**
-dontwarn org.conscrypt.**
-dontwarn org.eclipse.jetty.**
-dontwarn reactor.blockhound.**

# Koin, Kamel, multiplatform-settings
-keep class org.koin.** { *; }
-dontwarn org.koin.**
-dontwarn io.kamel.**
-dontwarn com.russhwolf.settings.**

# WorkManager (notifications while the app is closed) keeps its jobs in a Room database that is created by reflection
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class androidx.work.impl.** { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-dontwarn androidx.room.paging.**
