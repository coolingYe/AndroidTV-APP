# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile-keep class com.tencent.shadow.core.common.**{*;}
# 保护代码中的Annotation不被混淆
-keepattributes *Annotation*
# 避免混淆泛型, 这在JSON实体映射时非常重要
-keepattributes Signature
# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
#优化时允许访问并修改有修饰符的类和类的成员，这可以提高优化步骤的结果。

# 保持哪些类不被混淆
#继承activity,application,service,broadcastReceiver,contentprovider....不进行混淆
#-keep class android.**{*;}

# 保留support下的所有类及其内部类
#-keep class android.support.** {*;}

# 保留继承的
#-keep public class * extends android.support.v4.**
#-keep public class * extends android.support.v7.**
#-keep public class * extends android.support.annotation.**

# 保留R下面的资源
-keep class **.R$* {*;}



-keep class com.zee.guide.data.protocol.** { *; }
-keep class com.zee.launcher.login.data.protocol.** { *; }
-keep class com.zwn.launcher.data.protocol.** { *; }
-keep class com.zwn.launcher.data.model.** { *; }
-keep class com.zwn.launcher.data.layout.** { *; }
-keep class com.zeewain.base.data.protocol.** { *; }

-keep class com.zwn.lib_download.** { *; }
-keep class com.zee.manager.** { *; }
#-keep class com.zwn.launcher.host.** { *; }

-keep class com.google.zxing.** { *; }

#retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep public class * extends retrofit2.Converter {*;}

#gson
-keep class com.google.gson.stream.** { *; }

#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}
-keep interface okhttp3.**{*;}

#RxJava
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
  long producerIndex;
  long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
  rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
  rx.internal.util.atomic.LinkedQueueNode consumerNode;
}