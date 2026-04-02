# 🛍️ Shopping App - Hướng dẫn Sử dụng Các Tính Năng Mới

## 📱 Các Tính Năng Đã Thêm

### 1. 🏪 Quản Lý Tồn Kho

#### Hiển Thị Số Lượng Tồn Kho
- Mở trang chi tiết sản phẩm → Xem "Kho: X" ở phía trên cùng bên phải
- Nếu `stockQuantity ≤ 0` → Hiển thị nhãn **"SOLD OUT"** và sản phẩm bị làm mờ

#### Tự Động Trừ Kho
- Khi nhấn "Thanh toán" → Hệ thống tự động giảm `stockQuantity`
- Ví dụ: Mua 3 cái Air Max 90 → Stock giảm từ 10 → 7

#### Validate Giỏ Hàng
- Cố gắng thêm quá số lượng tồn kho → Toast: "Không thể thêm quá số lượng tồn kho!"
- Trong checkout, nếu stock không đủ → Hiện thông báo lỗi

---

### 2. ⭐ Hệ Thống Đánh Giá

#### Viết Đánh Giá
1. Mở trang chi tiết sản phẩm
2. Scroll xuống tab "Đánh giá"
3. Nhấn nút "Đánh giá" (nếu chưa đăng nhập → tự động yêu cầu)
4. Chọn số sao (1-5)
5. Viết bình luận (tùy chọn)
6. Nhấn "Gửi"

#### Xem Rating
- Rating trung bình hiển thị ở phía trên cùng: **4.8 sao (156 đánh giá)**
- Danh sách reviews hiển thị dưới, sắp xếp từ mới nhất

#### Dữ Liệu Mẫu
- Air Max 90: 4.8★ từ 156 đánh giá
- Superstar: 4.7★ từ 98 đánh giá
- Old Skool: 4.5★ từ 212 đánh giá
- Ultraboost Light: 4.9★ từ 342 đánh giá

---

### 3. 💳 Luồng Thanh Toán

#### Phương Thức Thanh Toán
| Phương Thức | Trạng Thái Đơn |
|-------------|--|
| COD (Thanh toán khi nhận) | **Delivering** (Đang giao) |
| Chuyển khoản ngân hàng | **Paid** (Đã thanh toán) |
| Ví điện tử | **Paid** (Đã thanh toán) |

#### Quy Trình
1. Xem giỏ hàng → Nhấn "Thanh toán"
2. Nhập địa chỉ giao hàng
3. Chọn phương thức thanh toán
4. Nhấn "Thanh toán"
5. → Nếu OK → Đơn hàng được xử lý & kho giảm tự động
6. → Hiển thị màn hình thành công

---

### 4. 🔐 Đăng Ký & Bảo Mật

#### Form Đăng Ký Mới
- **Họ và tên** (bắt buộc)
- **Tên đăng nhập** (min 3 ký tự)
- **Mật khẩu** (min 6 ký tự)
- **Xác nhận mật khẩu** ← **MỚI**: Phải khớp với mật khẩu
- **Số điện thoại** (min 10 ký tự)

#### Validation
```
Mật khẩu: "123456"
Xác nhận: "123456" ✅ OK
Xác nhận: "654321" ❌ Lỗi: "Mật khẩu xác nhận không khớp"
```

---

## 🧪 Test Cases

### Test 1: SOLD OUT Product
```
1. Mở "Superstar" (stock = 0)
2. → Nhìn thấy "SOLD OUT" & sản phẩm mờ
3. → Không thể click vào
4. → Nút "Thêm vào giỏ" bị disable
```

### Test 2: Inventory Reduction
```
1. Thêm "Air Max 90" (stock = 10) vào giỏ 3 cái
2. Thanh toán → OrderSuccess screen
3. Quay lại & mở "Air Max 90" again
4. → Kho bây giờ = 7
```

### Test 3: Review System
```
1. Mở "Old Skool" → Tab "Đánh giá"
2. → Hiển thị 3 reviews mẫu
3. Nhấn "Đánh giá" → Dialog
4. Chọn 5 sao + "Sản phẩm rất tốt!"
5. → Review được thêm
6. → Rating TB cập nhật
```

### Test 4: Stock Validation in Cart
```
1. Thêm "Air Max 90" vào giỏ: 5 cái
2. Thêm 5 cái nữa trong giỏ → OK (10 total)
3. Cố thêm 1 cái nữa → Toast: "Không thể thêm quá tồn kho!"
```

### Test 5: Payment Status
```
- Chọn COD → Order Status = "Delivering"
- Chọn Bank/Wallet → Order Status = "Paid"
→ Xem hóa đơn để verify
```

---

## 📊 Database Schema

### Products Table
| Trường | Kiểu | Mô Tả |
|--------|------|-------|
| id | INT | Primary Key |
| name | TEXT | Tên sản phẩm |
| price | DOUBLE | Giá |
| **stockQuantity** | INT | ← **MỚI**: Số lượng tồn kho |
| rating | FLOAT | Rating trung bình |
| reviewCount | INT | Số lượt review |

### Reviews Table ← **MỚI**
| Trường | Kiểu | Mô Tả |
|--------|------|-------|
| id | INT | Primary Key |
| userId | INT | Foreign Key (Users) |
| productId | INT | Foreign Key (Products) |
| rating | FLOAT | 1-5 sao |
| comment | TEXT | Bình luận |
| date | TEXT | Ngày review |
| userName | TEXT | Tên người review |

---

## 🔧 Configuration

### App Constants
```java
// src/main/java/com/example/shoppingapp/Constant.java
public static final String DEFAULT_STOCK = "50";
public static final int MIN_STOCK_WARNING = 5;
public static final int DELIVERY_DAYS = "3-5";
```

### Seed Data
File: `AppDatabase.java` → seedData() method

Sửa seed data:
```java
p = new Product("Tên", "Brand", "Mô tả", 1000000, "res://drawable/image", "đôi", categoryId);
p.setStockQuantity(20);  // ← Thay số lượng
p.setRating(4.5f);        // ← Thay rating
p.setReviewCount(100);     // ← Thay số review
productDao.insert(p);
```

---

## 📚 API & Methods

### ProductDao
```java
// Lấy stock
int stock = db.productDao().getProductById(id).getStockQuantity();

// Trừ stock
db.productDao().reduceStock(productId, quantity);
```

### ReviewDao
```java
// Tính rating trung bình
float avgRating = db.reviewDao().getAverageRating(productId);

// Đếm reviews
int count = db.reviewDao().getReviewCount(productId);

// Lấy danh sách reviews
List<Review> reviews = db.reviewDao().getReviewsByProduct(productId);
```

### SessionManager
```java
// Lấy tên đầy đủ người dùng (cho review)
String fullName = sessionManager.getUserFullName();
```

---

## ❗ Troubleshooting

| Vấn đề | Nguyên Nhân | Giải Pháp |
|--------|-----------|----------|
| SOLD OUT không hiển thị | stockQuantity = 0 không được set | Kiểm tra seed data |
| Review không lưu | Chưa đăng nhập | Yêu cầu login trước |
| Stock không giảm | Không click "Thanh toán" | Click nút thanh toán chính xác |
| Rating không cập nhật | ReviewCount = 0 ban đầu | Dữ liệu mẫu đã có reviews |
| rvCart crash | ID sai | Kiểm tra `R.id.rvCartItems` |

---

## 🚀 Deployment Checklist

- [ ] Build thành công (BUILD SUCCESSFUL)
- [ ] Test SOLD OUT products
- [ ] Test inventory reduction
- [ ] Test review system
- [ ] Test payment status
- [ ] Test confirm password
- [ ] Test cartfragment không crash

---

**Happy Testing! 🎉**

