# 静心自律 ProGuard 规则

# 保留 Hilt 生成的组件
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# 保留 Room 实体
-keep class com.quietdiscipline.app.data.local.entity.** { *; }
