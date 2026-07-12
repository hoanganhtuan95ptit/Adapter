# Adapter

Thư viện Android giúp quản lý nhiều loại item trong `RecyclerView` một cách gọn gàng — sử dụng **KSP** để tự động đăng ký adapter, không cần viết boilerplate.

---

## Cách hoạt động

```
Annotation @Adapter
      ↓
KSP sinh ra {TênModule}ViewItemAdapterProvider lúc compile
      ↓
AutoRegister tự phát hiện provider khi app khởi động
      ↓
MultiAdapter nạp các adapter class theo tên thông qua reflection
      ↓
MultiRecyclerView hiển thị đúng adapter cho từng ViewItem
```

---

## Cài đặt

### Cách 1 — JitPack (production)

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

```groovy
// build.gradle (module)
plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    // groupId trên JitPack = com.github.{username}.{RepoName}
    implementation 'com.github.hoanganhtuan95ptit.Adapter:adapter:1.2.1.10'
    ksp         'com.github.hoanganhtuan95ptit.Adapter:adapter-processor:1.2.1.10'
}
```

### Cách 2 — mavenLocal (test local)

Publish lên máy trước: `./gradlew publishToMavenLocal`

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal() // phải đặt trước google/mavenCentral để ưu tiên resolve local
        google()
        mavenCentral()
    }
}
```

```groovy
// build.gradle (module)
plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    // groupId local = com.github.hoanganhtuan95ptit.Adapter
    implementation 'com.github.hoanganhtuan95ptit.Adapter:adapter:1.2.1.10'
    ksp         'com.github.hoanganhtuan95ptit.Adapter:adapter-processor:1.2.1.10'
}
```

---

## Cách dùng

### Bước 1 — Tạo ViewItem

`ViewItem` là model dữ liệu cho từng dòng trong danh sách. Cần implement 2 hàm để `DiffUtil` so sánh chính xác:

```kotlin
data class TestViewItem(
    val id: String = "",
    val text: String = ""
) : ViewItem {

    // Xác định tính duy nhất của item — tương tự primary key
    override fun areItemsTheSame(): List<Any> = listOf(id)

    // Theo dõi thay đổi từng trường — mỗi cặp gồm (giá trị, tag)
    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        text to "text"
    )
}
```

### Bước 2 — Tạo ViewItemAdapter

Đánh dấu class bằng `@Adapter`. KSP sẽ tự động đăng ký lúc compile, không cần khai báo thêm gì.

```kotlin
@Adapter
class TestAdapter : ViewItemAdapter<TestViewItem, AdapterItemNoneBinding>() {

    override val viewItemClass: Class<TestViewItem> by lazy {
        TestViewItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): AdapterItemNoneBinding {
        return AdapterItemNoneBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<AdapterItemNoneBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)

        viewHolder.itemView.setOnClickListener {

            val item = (viewHolder.bindingAdapter as ListAdapter<*, *>).currentList.getOrNull(viewHolder.absoluteAdapterPosition) as? TestViewItem ?: return@setOnClickListener

            // todo gửi sự kiện
        }

        return viewHolder
    }

    override fun onBindViewHolder(binding: AdapterItemNoneBinding, viewType: Int, position: Int, item: TestViewItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)

        if (payloads.isEmpty() || payloads.contains("text")) {
            // todo set text
        }
    }
}
```

`payloads` chứa các tag từ `getContentsCompare()` của những trường thực sự thay đổi — dùng để rebind một phần thay vì vẽ lại toàn bộ item.

### Bước 3 — Hiển thị bằng MultiRecyclerView

**Cách 1 — Dùng trực tiếp với AutoRegisterManager:**

```kotlin
suspend fun test(fragment: Fragment) {

    val testList = arrayListOf(
        TestViewItem()
    )

    val recyclerView = MultiRecyclerView(fragment.requireContext())
    recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())

    AutoRegisterManager.subscribe(ViewItemAdapterProvider::class.java).map { it.flatMap { it.provider() } }.collect {

        recyclerView.submitListAndAwait(viewItemList = testList, adapterList = it, isAnimation = true)
    }
}
```

**Cách 2 — Dùng với LiveData + `attachAdapter()`:**

```kotlin
suspend fun testWithLivedata(fragment: Fragment) {

    val viewItemListLiveData = MutableLiveData(arrayListOf(TestViewItem()))

    val recyclerView = MultiRecyclerView(fragment.requireContext())
    recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())

    viewItemListLiveData.asFlow().attachAdapter().collect { (viewItemList, adapterList) ->

        recyclerView.submitListAndAwait(viewItemList = viewItemList, adapterList = adapterList, isAnimation = true)
    }
}
```

`attachAdapter()` là extension function trên `Flow<List<ViewItem>>`, tự động combine với danh sách adapter đã đăng ký — trả về `Flow<Pair<List<ViewItem>, List<String>>>`. Cách này phù hợp khi dữ liệu đến từ `LiveData` hoặc `StateFlow` trong ViewModel.

---

## Tài liệu API

### `ViewItem`

| Hàm | Mục đích |
|---|---|
| `areItemsTheSame(): List<Any>` | Các trường định danh duy nhất item (giống primary key) |
| `getContentsCompare(): List<Pair<Any, String>>` | Các trường cần theo dõi thay đổi, kèm tag để nhận biết trong `payloads` |

### `ViewItemAdapter<VI, VB>`

| Hàm | Mục đích |
|---|---|
| `viewItemClass` | Khai báo `Class<VI>` mà adapter này xử lý |
| `createViewBinding(...)` | Inflate và trả về ViewBinding |
| `createViewHolder(...)` | Override để gắn click listener hoặc trang trí thêm |
| `onBindViewHolder(binding, viewType, position, item, payloads)` | Bind dữ liệu; dùng `payloads` để cập nhật một phần |

### `submitListAndAwait`

```kotlin
suspend fun RecyclerView.submitListAndAwait(
    viewItemList: List<ViewItem>,
    adapterList: List<String>,
    isAnimation: Boolean = false,
    ignoreTransitionViewId: List<Int> = emptyList()
)
```

`adapterList` là danh sách tên class adapter (lấy từ `ViewItemAdapterProvider`) để `MultiAdapter` biết cần nạp những adapter nào. Suspend cho đến khi `DiffUtil` tính toán xong và toàn bộ animation kết thúc. Khi `isAnimation = true`, dùng `AutoTransition` để item thay đổi mượt mà. Truyền danh sách view ID vào `ignoreTransitionViewId` để loại trừ các view không cần animation.

### `attachAdapter`

```kotlin
fun Flow<List<ViewItem>>.attachAdapter(): Flow<Pair<List<ViewItem>, List<String>>>
```

Extension function trên `Flow<List<ViewItem>>`, tự combine với `adapterStateFlow` (danh sách adapter đã đăng ký qua AutoRegister). Trả về `Flow<Pair<List<ViewItem>, List<String>>>` — destructure thành `(viewItemList, adapterList)` để truyền vào `submitListAndAwait`.

---

## Code được sinh tự động

Khi build, KSP quét toàn bộ class có `@Adapter` và sinh ra một provider class cho mỗi module:

```kotlin
// Generated by AdapterProcessor. DO NOT EDIT.
@AutoRegister(apis = [ViewItemAdapterProvider::class])
public class AdapterViewItemAdapterProvider : ViewItemAdapterProvider() {
    override fun provider(): List<String> = listOf(
        NoneAdapter::class.java.name,
    )
}
```

Tên class được suy ra từ tên Gradle module — ví dụ module `feature-payment` sẽ tạo ra `FeaturePaymentViewItemAdapterProvider`. Không cần cấu hình gì thêm.

---

## Giấy phép

```
Copyright 2024 hoanganhtuan95ptit

Licensed under the Apache License, Version 2.0
```
