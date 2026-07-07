# Swap Fix

مود Fabric بيحل مشكلة التأخير العشوائي في تبديل الهوت بار (سلوت 8 ↔ سلوت 9)
عن طريق معالجة ضغطات الكيبورد فور حدوثها (raw input) بدل ما تستنى الـ tick
العادي بتاع ماين كرافت.

- الإصدار: Minecraft 1.21.11 (Fabric)
- Java 21

## خطوات الرفع على GitHub (مرة واحدة بس)

1. اعمل ريبو جديد وفاضي على GitHub (من غير README ولا .gitignore).
2. من جوه المجلد ده، افتح Terminal ونفذ:

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/USERNAME/REPO_NAME.git
git push -u origin main
```

(غيّر `USERNAME/REPO_NAME` باسم حسابك واسم الريبو).

## هيحصل إيه بعد الـ push؟

GitHub Actions هيشتغل لوحده تلقائيًا (الملف بتاعه موجود في
`.github/workflows/build.yml`) وهيعمل build كامل للمود. تقدر تتابعه من
تبويب **Actions** فوق في صفحة الريبو.

- لو الـ build نجح: هتلاقي ملف الـ `.jar` جاهز تحت تبويب Actions →
  اختار آخر run → **Artifacts** في الآخر.
- لو الـ build فشل: افتح الـ log وابعتلي رسالة الخطأ، غالبًا هيكون
  اسم method أو field اتغير شوية عن اللي كتبته (ده بيحصل أحيانًا بين
  نسخ ماين كرافت الفرعية المختلفة)، وهظبطه فورًا.

## لو عايز Release جاهز للتحميل مباشرة (بدل ما تدور في Actions)

```bash
git tag v1.0.0
git push origin v1.0.0
```

ده هيخلي الـ workflow يعمل صفحة **Release** فيها ملف الـ jar جاهز
للتحميل مباشرة من صفحة الريبو، بدون ما تدخل تبويب Actions أصلاً.

## تركيب المود بعد التحميل

حط ملف الـ `.jar` جوه مجلد `mods` بتاع Fabric Loader (لازم يكون عندك
Fabric Loader + Fabric API متثبتين للإصدار 1.21.11).

## اختبار محلي قبل ما تعتمد عليه في اللعب الحقيقي (اختياري)

لو عايز تجرب المود على جهازك مباشرة قبل ما تستخدمه فعليًا:
```bash
./gradlew runClient
```
(محتاج JDK 21 مثبت على جهازك).
