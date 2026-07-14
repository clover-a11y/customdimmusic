# Custom Dimension Music

Mod Fabric phát nhạc MP3 của riêng bạn theo từng chiều không gian (Overworld / Nether / The End).

## Cách hoạt động

Khi mod chạy lần đầu, nó tự tạo trong **instance** của bạn:

```
<instance>/config/customdimmusic/
├── overworld/   <- bỏ file .mp3 vào đây để phát khi ở Overworld
├── nether/      <- bỏ file .mp3 vào đây để phát khi ở Nether
└── end/         <- bỏ file .mp3 vào đây để phát khi ở The End
└── settings.json
```

Bỏ 1 hoặc nhiều file `.mp3` vào đúng thư mục dimension mong muốn. Vào game, nhấn
phím **O** (mặc định, đổi được trong *Controls > Custom Dimension Music*) để mở
màn hình cài đặt, **bật "Enable custom music"**, sau đó đi vào dimension tương
ứng — nhạc sẽ tự phát và lặp lại. Nếu có nhiều file, mod sẽ phát nối tiếp
(shuffle nếu bạn bật "Shuffle").

## Vì sao không có sẵn file .jar?

Môi trường mình dùng để soạn code này không có kết nối mạng, nên không thể tải
Minecraft/Fabric/Gradle về để build ra `.jar` được. Bạn cần tự build ở máy có
mạng theo các bước dưới đây (chỉ mất khoảng 2-3 phút, không cần biết lập trình
sâu).

## Cách build KHÔNG CẦN máy mạnh (dùng GitHub Actions - khuyên dùng)

Máy bạn boot 1 phút, chậm thì làm theo cách này — build hoàn toàn diễn ra trên máy
chủ của GitHub (miễn phí), máy bạn chỉ cần trình duyệt web, không cần cài gì.

1. Tạo tài khoản tại https://github.com (nếu chưa có) — miễn phí.
2. Bấm nút "+" góc trên phải > **New repository** > đặt tên bất kỳ (vd
   `customdimmusic`) > chọn **Public** hoặc **Private** đều được > **Create
   repository**.
3. Trong trang repo vừa tạo, chọn **"uploading an existing file"** (hoặc
   `Add file > Upload files`).
4. **Giải nén** file zip mình gửi ra máy trước, sau đó **kéo thả toàn bộ nội
   dung bên trong thư mục `customdimmusic`** (không phải kéo cả thư mục
   `customdimmusic`, mà kéo các file/thư mục *bên trong* nó: `build.gradle`,
   `gradle.properties`, `src/`, `.github/`...) vào ô upload của GitHub.
5. Bấm **Commit changes** ở cuối trang.
6. Vào tab **Actions** ở trên cùng repo — GitHub sẽ tự động chạy build (mất
   khoảng 3-5 phút). Đợi tới khi thấy dấu tích xanh ✅.
7. Bấm vào lần chạy đó > kéo xuống mục **Artifacts** > tải file
   `customdimmusic-mod.zip` về > giải nén ra là có file `.jar` để cài vào
   `mods`.

Nếu bấm vào build mà thấy dấu ❌ đỏ (lỗi), bấm vào để xem log lỗi, chụp màn
hình gửi cho mình, mình sẽ đọc và sửa code giúp bạn — bạn chỉ cần lặp lại bước
4-6 (upload đè file đã sửa) là GitHub tự build lại, không cần làm gì trên máy
yếu của bạn cả.

## Cách build tại máy (nếu máy đủ khỏe)

**Yêu cầu:** Java 25 (bản JDK, không phải chỉ JRE) đã cài trên máy.

1. Cài **IntelliJ IDEA** (bản Community miễn phí là đủ).
2. Mở thư mục `customdimmusic` này bằng IntelliJ (`File > Open`).
3. IntelliJ sẽ tự nhận Gradle project và tải Gradle wrapper (`gradlew`) + mọi
   dependency (Minecraft, Fabric Loader, Fabric API, JLayer) — quá trình này
   cần internet và có thể mất vài phút trong lần đầu.
4. Mở tab **Gradle** ở bên phải > `Tasks > build > build`. Hoặc mở Terminal
   trong IntelliJ và gõ:
   ```
   ./gradlew build
   ```
   (Windows: `gradlew.bat build`)
5. File mod hoàn chỉnh nằm ở: `build/libs/customdimmusic-1.0.0.jar`

## Lưu ý quan trọng về phiên bản

- Minecraft không có bản "1.26.1" — từ 2026 Mojang đổi cách đặt số phiên bản
  thành kiểu "26.1", "26.2"... Project này đang trỏ tới **26.1**
  (`gradle.properties` -> `minecraft_version`). Nếu bạn thực ra muốn bản khác
  (26.2, 26.3...), chỉ cần sửa dòng đó và dòng `fabric_api_version` cho khớp
  (kiểm tra bản Fabric API đúng tại https://modrinth.com/mod/fabric-api).
- Vì bản Minecraft 26.x rất mới, một vài tên hàm liên quan tới API âm lượng
  (`getSoundVolumeOption`) trong `CustomDimMusicClient.java` **có thể** đã đổi
  tên so với những gì mình biết. Nếu build báo lỗi ở đúng 2 dòng đó, bạn (hoặc
  ai đó rành Java) chỉ cần sửa lại tên hàm cho khớp — phần logic phát nhạc
  chính (MusicManager, VolumeAudioDevice) không phụ thuộc vào API này nên vẫn
  chạy được kể cả khi bạn xoá hẳn phần mute nhạc gốc.

## Cài vào instance

1. Cài **Fabric Loader 0.19.3** cho Minecraft đúng phiên bản (26.1/26.2...)
   qua Fabric Installer (https://fabricmc.net/use/installer/).
2. Tải **Fabric API** đúng bản Minecraft, bỏ vào thư mục `mods` của instance.
3. Bỏ file `customdimmusic-1.0.0.jar` vừa build vào chung thư mục `mods`.
4. Mở game, vào world, bỏ mp3 vào thư mục tương ứng, nhấn **O** để bật mod.

## Cấu trúc project

```
customdimmusic/
├── build.gradle              # cấu hình build, dependency (Fabric + JLayer)
├── gradle.properties         # số phiên bản MC/Loader/Fabric API
├── settings.gradle
└── src/main/
    ├── resources/fabric.mod.json
    └── java/net/customdimmusic/
        ├── CustomDimMusicClient.java  # điểm khởi động, phát hiện đổi dimension
        ├── MusicManager.java          # quét & phát playlist mp3
        ├── VolumeAudioDevice.java     # chỉnh âm lượng khi phát mp3 (JLayer)
        ├── ModConfig.java             # lưu/đọc settings.json
        └── ConfigScreen.java          # màn hình cài đặt trong game
```
