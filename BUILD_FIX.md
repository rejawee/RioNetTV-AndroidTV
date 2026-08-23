# Build fix — RC1

تم تثبيت توافق سلسلة البناء الخاصة بنسخة Android TV:

- Android Gradle Plugin: 8.10.1 (يدعم compileSdk 36)
- Gradle: 8.11.1
- JDK: 17
- Compose BOM: 2026.06.00 (بدلاً من 2026.08.00 الذي انتقل إلى compileSdk 37 / AGP 9)
- إضافة activity-ktx صراحةً لاستخدام viewModels
- استخدام Compose Material3 TextField في شاشة الإعدادات مع إبقاء TV Material للأزرار والنصوص

الهدف: بناء APK تجريبي ثابت على API 36 قبل الانتقال إلى AGP 9 / API 37.
