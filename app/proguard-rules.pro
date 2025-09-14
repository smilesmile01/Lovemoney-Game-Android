# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep JavaScript interface
-keepclassmembers class com.lovemoney.lovemoney.GameJSInterface {
   public *;
}

# Keep WebView related classes
-keep class android.webkit.** { *; }
-keep class com.lovemoney.lovemoney.GameActivity$GameWebViewClient { *; }
-keep class com.lovemoney.lovemoney.GameActivity$GameWebChromeClient { *; }

# Keep crash handler and logger for debugging
-keep class com.lovemoney.lovemoney.utils.CrashHandler { *; }
-keep class com.lovemoney.lovemoney.utils.Logger { *; }

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep annotations
-keepattributes *Annotation*

# Keep main activities
-keep public class * extends android.app.Activity
-keep public class * extends androidx.appcompat.app.AppCompatActivity

# WebView optimizations
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, int);
}