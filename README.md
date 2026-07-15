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

lần đầu làm mod mọi người thông cảm
