# Solina Files

<p align="center">
  <a href="https://github.com/Solina-Projects/Solina-Files/actions/workflows/build.yml"><img src="https://github.com/Solina-Projects/Solina-Files/actions/workflows/build.yml/badge.svg" alt="Build status"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-AGPL--3.0-2456B3" alt="License: GNU AGPL v3.0 or later"></a>
  <a href="https://github.com/Solina-Projects/Solina-Files/releases"><img src="https://img.shields.io/github/v/release/Solina-Projects/Solina-Files?color=6750A4&label=latest%20release" alt="Latest release"></a>
</p>

<p align="center"><b>Languages:</b> <a href="#english">English</a> · <a href="#فارسی">فارسی</a> · <a href="#العربية">العربية</a></p>

---

## English

**Solina Files** is an open-source file manager for Android, developed by **Solina**. It is built for everyday file work — and for the advanced jobs that usually need several different apps: browsing and organising files, working with archives and APKs, editing code, connecting to remote storage, and keeping private files in an encrypted vault.

- **Project:** Solina
- **Application:** Solina Files
- **Android application ID:** `ir.solina.files`
- **License:** GNU AGPL v3.0 or later

### Features

#### Browse and organise

- **Dual-pane and single-pane modes** — work with two folders side by side, or one folder at a time. Switch from the mode bar above the path bar.
- **Home screen** — storage overview with categories (images, videos, documents, music, apps), a storage-usage bar, and quick access to your files.
- **List and grid views** with three density sizes (compact, comfortable, large), so the layout fits your screen and your taste.
- **Folder statistics** — folder size and item count, calculated once when you open a folder and shown next to each folder.
- **Fast search** — search by name, including regular-expression search, with results as you type.
- **Quick create** — a new folder, text file, HTML file, or project, right from the floating button.
- **Sorting and filtering** — by name, date, size, and more, in both directions.
- **Bookmarks** for the folders you open most.

#### File operations

- Copy, move, rename, delete, and select multiple items.
- **Trash** — deleted files go to the trash first, so you can restore them instead of losing them.
- **Compare files** side by side.
- Share files to other apps, and open files with the app of your choice.

#### Archives

- Work with archives: browse their contents, extract files, and create new archives in supported formats.

#### Android power tools

- **APK tools** — inspect APK files, view app details, DEX code, resources (ARSC), signing keys, and properties.
- **Nine-patch editor** for Android nine-patch images.
- **Hex editor** for viewing and editing files byte by byte.
- **App manager** — browse installed apps and their details.

#### Edit and view

- **Built-in code and text editor** (Sora) with syntax highlighting.
- **Markdown** viewing and editing.
- **PDF viewer** powered by Pdfium.
- **Media** — browse and play images, audio, and video.

#### Network and remote storage

- Connect to **SMB, FTP, SFTP/SSH, and WebDAV** servers and browse them like local folders.

#### Privacy and security

- **Encrypted vault** — keep private files protected with strong client-side encryption (Argon2id + XChaCha20-Poly1305). Only you hold the key.

#### Advanced access

- **Termux integration** — run commands in Termux from the file manager.
- **Plugins** — extend the app with additional tools.
- **Root / Shizuku** — advanced access on supported devices.

#### Design and language

- **Material 3 Expressive** design with the Solina purple theme and dynamic colours.
- **Persian (Farsi) and English interface**, with full right-to-left support and the Vazirmatn font. Fresh installs start in Persian.
- Light and dark themes, with wallpaper colours.
- Customisable home screen, bottom bar, and floating actions — arrange the app the way you work.

### Download

Check the repository's [Releases](https://github.com/Solina-Projects/Solina-Files/releases) for published versions, and the [Actions](https://github.com/Solina-Projects/Solina-Files/actions) page for automated build results and development artifacts.

Choose the APK matching your device architecture. ARM64 (`arm64-v8a`) is suitable for most modern 64-bit Android devices, while ARMv7 (`armeabi-v7a`) is for compatible 32-bit ARM devices. A universal APK supports a wider range of devices but is larger. Release builds are signed; development builds from Actions may differ in signing and optimisation.

**Requirements:** Android 7.0 (API 24) or later.

### Build from source

#### Requirements

- Android SDK and Android NDK
- JDK 17
- Rust toolchain with Android targets
- `cargo-ndk`

#### Build steps

From the repository root, prepare the Rust targets and native dependencies, then run the Gradle task:

```bash
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
cargo install cargo-ndk --locked
native/fetch-pdfium.sh
native/build.sh
./gradlew :app:assembleDebug
```

The build environment may require additional setup depending on the host operating system and the native dependencies used by the selected build.

### Contributing and reporting issues

Bug reports, reproducible build failures, and focused improvement proposals are welcome through the repository's [Issues](https://github.com/Solina-Projects/Solina-Files/issues) page.

When reporting a problem, include the app version, Android version, device details, steps to reproduce, and relevant logs. Do not include passwords, private files, access tokens, or other sensitive information.

### License

Solina Files is distributed under the [GNU Affero General Public License v3.0 or later](LICENSE). Third-party components may have their own license terms; consult [THIRD_PARTY_NOTICES](THIRD_PARTY_NOTICES) and the relevant component notices for details.

---

<div dir="rtl" lang="fa">

## فارسی

**مدیریت فایل سولینا (Solina Files)** یک فایل‌منیجر متن‌باز برای اندروید است که توسط **Solina** توسعه داده می‌شود. این برنامه برای کارهای روزمره با فایل ساخته شده — و برای کارهای پیشرفته‌ای که معمولاً به چند برنامهٔ جدا نیاز دارند: مرور و مرتب‌کردن فایل‌ها، کار با آرشیو و فایل‌های APK، ویرایش کد، اتصال به حافظه‌های راه‌دور، و نگهداری فایل‌های خصوصی در یک گاوصندوق رمزنگاری‌شده.

- **پروژه:** Solina
- **برنامه:** Solina Files
- **شناسهٔ برنامه در اندروید:** `ir.solina.files`
- **مجوز:** GNU AGPL نسخهٔ ۳ یا جدیدتر

### امکانات

#### مرور و سازمان‌دهی

- **حالت تک‌پنجره و دوپنجره** — با دو پوشه کنار هم کار کنید، یا یک پوشه در هر لحظه. جابه‌جایی از نوار حالت بالای نوار مسیر انجام می‌شود.
- **صفحهٔ خانه** — نمای کلی حافظه با دسته‌بندی‌ها (عکس، ویدیو، سند، موزیک، برنامه‌ها)، نوار میزان مصرف حافظه، و دسترسی سریع به فایل‌ها.
- **نمایش لیستی و شبکه‌ای** با سه اندازهٔ تراکم (فشرده، راحت، بزرگ)، تا چیدمان با صفحه و سلیقهٔ شما جور شود.
- **آمار پوشه** — حجم و تعداد آیتم‌های هر پوشه، یک بار موقع باز کردن پوشه محاسبه می‌شود و کنار هر پوشه نمایش داده می‌شود.
- **جستجوی سریع** — جستجو بر اساس نام، از جمله جستجو با عبارت باقاعده (regex)، با نمایش نتیجه هنگام تایپ.
- **ایجاد سریع** — پوشهٔ جدید، فایل متنی، فایل HTML یا پروژه، مستقیم از دکمهٔ شناور.
- **مرتب‌سازی و فیلتر** — بر اساس نام، تاریخ، حجم و موارد دیگر، در هر دو جهت.
- **نشانک‌ها** برای پوشه‌هایی که بیشتر بازشان می‌کنید.

#### عملیات روی فایل

- کپی، انتقال، تغییر نام، حذف و انتخاب چند آیتم.
- **سطل زباله** — فایل‌های حذف‌شده اول به سطل زباله می‌روند، تا بتوانید برگردانیدشان به‌جای اینکه از دستشان بدهید.
- **مقایسهٔ فایل‌ها** کنار هم.
- اشتراک‌گذاری فایل با برنامه‌های دیگر و باز کردن فایل با برنامهٔ دلخواه.

#### آرشیو

- کار با فایل‌های آرشیو: دیدن محتوای داخلشان، استخراج فایل‌ها و ساخت آرشیو جدید در فرمت‌های پشتیبانی‌شده.

#### ابزارهای پیشرفتهٔ اندروید

- **ابزارهای APK** — بررسی فایل‌های APK، دیدن جزئیات برنامه، کد DEX، منابع (ARSC)، کلیدهای امضا و ویژگی‌ها.
- **ویرایشگر ناین‌پچ** برای تصاویر ناین‌پچ اندروید.
- **ویرایشگر هگز** برای دیدن و ویرایش فایل بایت‌به‌بایت.
- **مدیریت برنامه‌ها** — مرور برنامه‌های نصب‌شده و جزئیاتشان.

#### ویرایش و نمایش

- **ویرایشگر داخلی کد و متن** (Sora) با برجسته‌سازی نحوی.
- **مارک‌داون** — نمایش و ویرایش.
- **نمایشگر PDF** با موتور Pdfium.
- **رسانه** — مرور و پخش عکس، صدا و ویدیو.

#### شبکه و حافظهٔ راه‌دور

- اتصال به سرورهای **SMB ،FTP ،SFTP/SSH و WebDAV** و مرورشان مثل پوشه‌های محلی.

#### حریم خصوصی و امنیت

- **گاوصندوق رمزنگاری‌شده** — فایل‌های خصوصی با رمزنگاری قوی سمت کاربر محافظت می‌شوند (Argon2id + XChaCha20-Poly1305). کلید فقط دست خودتان است.

#### دسترسی پیشرفته

- **یکپارچگی با Termux** — اجرای دستورها در Termux از داخل فایل‌منیجر.
- **افزونه‌ها** — گسترش برنامه با ابزارهای بیشتر.
- **روت / Shizuku** — دسترسی پیشرفته در دستگاه‌های پشتیبانی‌شده.

#### طراحی و زبان

- طراحی **Material 3 Expressive** با تم بنفش سولینا و رنگ‌های پویا.
- **رابط کاربری فارسی و انگلیسی**، با پشتیبانی کامل راست‌به‌چپ و فونت وزیرمتن. نصب‌های تازه با فارسی شروع می‌شوند.
- تم روشن و تیره، با رنگ‌های کاغذدیواری.
- صفحهٔ خانه، نوار پایین و دکمه‌های شناور قابل سفارشی‌سازی‌اند — برنامه را همان‌طور که کار می‌کنید بچینید.

### دانلود

نسخه‌های منتشرشده را در صفحهٔ [Releases](https://github.com/Solina-Projects/Solina-Files/releases) ببینید، و نتایج ساخت خودکار و فایل‌های توسعه را در صفحهٔ [Actions](https://github.com/Solina-Projects/Solina-Files/actions).

فایل APK متناسب با معماری دستگاهتان را انتخاب کنید. ARM64 (`arm64-v8a`) برای بیشتر دستگاه‌های ۶۴ بیتی امروزی مناسب است و ARMv7 (`armeabi-v7a`) برای دستگاه‌های ۳۲ بیتی سازگار. نسخهٔ یونیورسال دستگاه‌های بیشتری را پوشش می‌دهد ولی حجیم‌تر است. نسخه‌های منتشرشده امضاشده‌اند؛ بیلدهای توسعه از Actions ممکن است از نظر امضا و بهینه‌سازی متفاوت باشند.

**نیازمندی:** اندروید ۷٫۰ (API 24) یا جدیدتر.

### ساخت از روی سورس

#### نیازمندی‌ها

- Android SDK و Android NDK
- JDK 17
- ابزار Rust با تارگت‌های اندروید
- `cargo-ndk`

#### مراحل ساخت

از ریشهٔ مخزن، تارگت‌های Rust و وابستگی‌های نیتیو را آماده کنید، بعد تسک Gradle را اجرا کنید:

```bash
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
cargo install cargo-ndk --locked
native/fetch-pdfium.sh
native/build.sh
./gradlew :app:assembleDebug
```

بسته به سیستم‌عامل میزبان و وابستگی‌های نیتیو، ممکن است تنظیمات بیشتری لازم باشد.

### مشارکت و گزارش مشکل

گزارش باگ، خطاهای قابل تکرار در ساخت و پیشنهادهای بهبود متمرکز، از طریق صفحهٔ [Issues](https://github.com/Solina-Projects/Solina-Files/issues) مخزن پذیرفته می‌شوند.

هنگام گزارش مشکل، نسخهٔ برنامه، نسخهٔ اندروید، جزئیات دستگاه، مراحل تکرار مشکل و لاگ‌های مرتبط را بنویسید. رمز عبور، فایل خصوصی، توکن دسترسی یا اطلاعات حساس دیگر نفرستید.

### مجوز

Solina Files تحت [مجوز GNU Affero General Public License نسخهٔ ۳ یا جدیدتر](LICENSE) منتشر می‌شود. اجزای شخص ثالث ممکن است مجوزهای خودشان را داشته باشند؛ به [THIRD_PARTY_NOTICES](THIRD_PARTY_NOTICES) و اطلاعیه‌های مربوط به هر جزء مراجعه کنید.

</div>

---

<div dir="rtl" lang="ar">

## العربية

**مدير ملفات سولينا (Solina Files)** هو مدير ملفات مفتوح المصدر لنظام أندرويد، طوّرته **Solina**. صُمم للأعمال اليومية مع الملفات — وللمهام المتقدمة التي تتطلب عادةً عدة تطبيقات مختلفة: تصفح الملفات وتنظيمها، والعمل مع الأرشيفات وملفات APK، وتحرير الشيفرات، والاتصال بالتخزين البعيد، وحفظ الملفات الخاصة في خزنة مشفّرة.

- **المشروع:** Solina
- **التطبيق:** Solina Files
- **معرّف التطبيق في أندرويد:** `ir.solina.files`
- **الترخيص:** GNU AGPL الإصدار 3 أو أحدث

### المميزات

#### التصفح والتنظيم

- **وضع النافذة الواحدة والنافذتين** — اعمل مع مجلدين جنبًا إلى جنب، أو مجلد واحد في كل مرة. التبديل يتم من شريط الوضع أعلى شريط المسار.
- **الشاشة الرئيسية** — نظرة عامة على التخزين مع التصنيفات (الصور، الفيديوهات، المستندات، الموسيقى، التطبيقات)، وشريط استهلاك التخزين، ووصول سريع إلى ملفاتك.
- **عرض القائمة والشبكة** بثلاثة أحجام للكثافة (مضغوط، مريح، كبير)، ليناسب التخطيط شاشتك وذوقك.
- **إحصاءات المجلدات** — حجم كل مجلد وعدد العناصر فيه، تُحسب مرة واحدة عند فتح المجلد وتُعرض بجانب كل مجلد.
- **بحث سريع** — بحث بالاسم، بما في ذلك البحث بالتعابير النمطية (regex)، مع عرض النتائج أثناء الكتابة.
- **إنشاء سريع** — مجلد جديد، أو ملف نصي، أو ملف HTML، أو مشروع، مباشرة من الزر العائم.
- **الترتيب والتصفية** — حسب الاسم أو التاريخ أو الحجم وغيرها، في كلا الاتجاهين.
- **إشارات مرجعية** للمجلدات التي تفتحها كثيرًا.

#### عمليات الملفات

- النسخ، النقل، إعادة التسمية، الحذف، وتحديد عدة عناصر.
- **سلة المهملات** — تذهب الملفات المحذوفة إلى سلة المهملات أولًا، لتتمكن من استعادتها بدل فقدانها.
- **مقارنة الملفات** جنبًا إلى جنب.
- مشاركة الملفات مع التطبيقات الأخرى، وفتحها بالتطبيق الذي تختاره.

#### الأرشيفات

- العمل مع ملفات الأرشيف: تصفح محتوياتها، واستخراج الملفات منها، وإنشاء أرشيفات جديدة بالصيغ المدعومة.

#### أدوات أندرويد المتقدمة

- **أدوات APK** — فحص ملفات APK، وعرض تفاصيل التطبيقات، وشيفرة DEX، والموارد (ARSC)، ومفاتيح التوقيع، والخصائص.
- **محرر Nine-patch** لصور Nine-patch في أندرويد.
- **محرر Hex** لعرض الملفات وتحريرها بايتًا بايتًا.
- **مدير التطبيقات** — تصفح التطبيقات المثبتة وتفاصيلها.

#### التحرير والعرض

- **محرر شيفرات ونصوص مدمج** (Sora) مع تلوين الصياغة.
- **Markdown** — عرض وتحرير.
- **عارض PDF** بمحرك Pdfium.
- **الوسائط** — تصفح الصور والصوت والفيديو وتشغيلها.

#### الشبكة والتخزين البعيد

- الاتصال بخوادم **SMB وFTP وSFTP/SSH وWebDAV** وتصفحها كما لو كانت مجلدات محلية.

#### الخصوصية والأمان

- **خزنة مشفّرة** — تُحمى الملفات الخاصة بتشفير قوي على جهازك (Argon2id + XChaCha20-Poly1305). المفتاح بيدك وحدك.

#### الوصول المتقدم

- **التكامل مع Termux** — تنفيذ الأوامر في Termux من داخل مدير الملفات.
- **الإضافات (Plugins)** — توسيع التطبيق بأدوات إضافية.
- **Root / Shizuku** — وصول متقدم على الأجهزة المدعومة.

#### التصميم واللغة

- تصميم **Material 3 Expressive** مع سمة سولينا البنفسجية وألوان ديناميكية.
- **واجهة بالفارسية والإنجليزية**، مع دعم كامل للاتجاه من اليمين إلى اليسار وخط وزيرمتن. تبدأ التثبيتات الجديدة بالفارسية.
- سمتان فاتحة وداكنة، مع ألوان خلفية الشاشة.
- الشاشة الرئيسية والشريط السفلي والأزرار العائمة قابلة للتخصيص — رتّب التطبيق كما تعمل.

### التنزيل

اطّلع على الإصدارات المنشورة في صفحة [Releases](https://github.com/Solina-Projects/Solina-Files/releases)، وعلى نتائج البناء الآلي وملفات التطوير في صفحة [Actions](https://github.com/Solina-Projects/Solina-Files/actions).

اختر ملف APK المناسب لمعمارية جهازك. ARM64 (`arm64-v8a`) مناسب لمعظم أجهزة أندرويد الحديثة 64-بت، بينما ARMv7 (`armeabi-v7a`) للأجهزة المتوافقة 32-بت. النسخة الشاملة (Universal) تدعم أجهزة أكثر لكنها أكبر حجمًا. إصدارات النشر موقّعة؛ أما نسخ التطوير من Actions فقد تختلف في التوقيع والتحسين.

**المتطلبات:** أندرويد 7.0 (API 24) أو أحدث.

### البناء من المصدر

#### المتطلبات

- Android SDK وAndroid NDK
- JDK 17
- أدوات Rust مع أهداف أندرويد
- `cargo-ndk`

#### خطوات البناء

من جذر المستودع، جهّز أهداف Rust والاعتماديات الأصلية، ثم نفّذ مهمة Gradle:

```bash
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
cargo install cargo-ndk --locked
native/fetch-pdfium.sh
native/build.sh
./gradlew :app:assembleDebug
```

قد تحتاج بيئة البناء إلى إعدادات إضافية حسب نظام التشغيل المضيف والاعتماديات الأصلية المستخدمة في البناء المختار.

### المساهمة والإبلاغ عن المشاكل

تقارير الأخطاء، وأخطاء البناء القابلة للتكرار، واقتراحات التحسين المركّزة مرحّب بها عبر صفحة [Issues](https://github.com/Solina-Projects/Solina-Files/issues) في المستودع.

عند الإبلاغ عن مشكلة، أرفق إصدار التطبيق، وإصدار أندرويد، وتفاصيل الجهاز، وخطوات إعادة إنتاج المشكلة، والسجلات ذات الصلة. لا ترسل كلمات مرور أو ملفات خاصة أو رموز وصول أو أي معلومات حساسة أخرى.

### الترخيص

يُوزَّع Solina Files بموجب [رخصة GNU Affero General Public License الإصدار 3 أو أحدث](LICENSE). قد تكون للمكونات الخارجية شروط ترخيص خاصة بها؛ راجع [THIRD_PARTY_NOTICES](THIRD_PARTY_NOTICES) وإشعارات المكونات ذات الصلة للتفاصيل.

</div>
