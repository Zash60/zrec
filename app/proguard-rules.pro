# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to the default
# ProGuard rules for Android.

# Keep MediaRecorder and MediaProjection related classes
-keep class android.media.MediaRecorder { *; }
-keep class android.media.projection.* { *; }

# Keep data models
-keep class com.zash60.zrec.data.model.** { *; }
