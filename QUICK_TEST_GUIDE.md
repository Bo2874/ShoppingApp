# 🚀 Quick Start - Test Guide

## ⚡ 5-Minute Test

### Setup
1. Build: `./gradlew build`
2. Run app on device/emulator
3. Login: 
   - Username: `nguyenvana`
   - Password: `123456`

---

## ✅ Feature Tests

### Test 1: Inventory Management (2 min)
```
1. Home → Tap "Air Max 90" (stock=10)
   ✓ See "Kho: 10" at top right
   
2. Home → Tap "Superstar" (stock=0)
   ✓ See "SOLD OUT" badge + grayed out
   ✓ Can't tap to open details
   
3. Add "Old Skool" (25 stock) → 25 pieces
   ✓ Toast: "Không thể thêm quá tồn kho!"
   ✓ Still shows 24 max
   
4. Checkout → Success
   ✓ Go back to "Old Skool"
   ✓ Stock now = 0 (25-25)
   ✓ Shows "SOLD OUT" now
```

### Test 2: Review System (2 min)
```
1. Open "Old Skool" → Scroll to "Đánh giá"
   ✓ Shows 4.5★ (212 reviews)
   ✓ List shows 3 reviews below
   
2. Tap "Đánh giá"
   ✓ Dialog appears with 5-star selector
   
3. Select 5 stars + type comment
   ✓ Tap "Gửi"
   ✓ Review added to list
   ✓ Rating recalculated (should increase slightly)
   
4. Reload page
   ✓ New review persists
   ✓ Rating count increased
```

### Test 3: Checkout Payment (1 min)
```
1. Add product → Cart → "Thanh toán"
   
2. Select "COD"
   ✓ Submit → Order status = "Delivering"
   ✓ Tap "Xem đơn hàng" → Shows "Thanh toán khi nhận"
   
3. Reorder → Select "Chuyển khoản"
   ✓ Submit → Order status = "Paid"
   ✓ Shows "Chuyển khoản ngân hàng"
```

### Test 4: Registration (1 min)
```
1. Logout → Tap "Đăng ký"
   
2. Fill form:
   - Họ tên: "Trần Văn B"
   - Username: "tranvanb"
   - Mật khẩu: "123456"
   - Xác nhận: "654321"
   ✓ Error: "Mật khẩu xác nhận không khớp"
   
3. Fix xác nhận = "123456"
   ✓ Success → Back to login
   ✓ Login with new account works
```

### Test 5: Bug Fixes (1 min)
```
1. Cart Fragment
   ✓ No crash when opening
   ✓ Can delete items
   
2. Images
   ✓ Product images load correctly
   ✓ No placeholder ghost images
   
3. Build
   ✓ `./gradlew build` → BUILD SUCCESSFUL
   ✓ No compile errors
   ✓ No lint errors
```

---

## 📊 Data Check

### Seed Data Verification
```sql
-- Check products
SELECT name, stockQuantity, rating, reviewCount 
FROM products;

-- Output:
-- Air Max 90, 10, 4.8, 156
-- Superstar, 0, 4.7, 98
-- Old Skool, 25, 4.5, 212
-- Ultraboost Light, 15, 4.9, 342

-- Check reviews
SELECT COUNT(*) FROM reviews;
-- Output: 9 seed reviews
```

---

## 🎯 Pass/Fail Criteria

| Test | Pass | Fail |
|------|------|------|
| SOLD OUT shows label | ✓ | ❌ |
| Stock decreases after checkout | ✓ | ❌ |
| Review saves & rating updates | ✓ | ❌ |
| Payment status correct | ✓ | ❌ |
| Confirm password validates | ✓ | ❌ |
| No crashes | ✓ | ❌ |
| Build successful | ✓ | ❌ |

**Must Pass All 7 Tests** ✅

---

## 🔍 Debug Tips

### View Database (Android Studio)
```
1. Device File Explorer → /data/data/com.example.shoppingapp/databases/
2. Download shopping_db
3. Open with SQLite Browser
4. Check: products, reviews, orders tables
```

### View Logs
```bash
./gradlew build -i  # Verbose logging
adb logcat | grep ShoppingApp  # Device logs
```

### Force Refresh
```
Settings → Apps → ShoppingApp → Storage → Clear Data
→ App restart → Fresh seed data
```

---

## 📞 Support Issues

| Issue | Solution |
|-------|----------|
| "Kho: 0" doesn't show SOLD OUT | Check if isSoldOut() called |
| Review dialog doesn't open | Must be logged in |
| Stock doesn't decrease | Check if reduceStock() called in processOrder() |
| Images don't load | Check drawable folder names match |
| Cart crashes | Check R.id.rvCartItems exists in XML |

---

**Happy Testing! 🎉**

