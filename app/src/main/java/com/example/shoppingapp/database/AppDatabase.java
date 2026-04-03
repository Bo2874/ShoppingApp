package com.example.shoppingapp.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.shoppingapp.database.dao.CategoryDao;
import com.example.shoppingapp.database.dao.OrderDao;
import com.example.shoppingapp.database.dao.OrderDetailDao;
import com.example.shoppingapp.database.dao.ProductDao;
import com.example.shoppingapp.database.dao.UserDao;
import com.example.shoppingapp.database.entity.Category;
import com.example.shoppingapp.database.entity.Order;
import com.example.shoppingapp.database.entity.OrderDetail;
import com.example.shoppingapp.database.entity.Product;
import com.example.shoppingapp.database.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Category.class, Product.class, Order.class, OrderDetail.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract ProductDao productDao();
    public abstract OrderDao orderDao();
    public abstract OrderDetailDao orderDetailDao();

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "fruit_app_db")
                            .fallbackToDestructiveMigration(true)
                            .addCallback(new SeedCallback())
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class SeedCallback extends Callback {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            seedData();
        }

        @Override
        public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
            super.onDestructiveMigration(db);
            seedData();
        }

        private void seedData() {
            databaseExecutor.execute(() -> {
                AppDatabase database = INSTANCE;
                if (database == null) return;
                if (database.productDao().getProductCount() > 0) return;

                // === Users ===
                UserDao userDao = database.userDao();
                userDao.insert(new User("admin", "123456", "Quản trị viên", "0901234567"));
                userDao.insert(new User("nguyenvana", "123456", "Nguyễn Văn A", "0912345678"));
                userDao.insert(new User("tranthib", "123456", "Trần Thị B", "0923456789"));

                // === Categories ===
                CategoryDao categoryDao = database.categoryDao();
                categoryDao.insert(new Category("Trái cây nhiệt đới", "Các loại trái cây nhiệt đới tươi ngon", ""));
                categoryDao.insert(new Category("Trái cây nhập khẩu", "Trái cây nhập khẩu cao cấp", ""));
                categoryDao.insert(new Category("Trái cây theo mùa", "Trái cây theo mùa, tươi mới mỗi ngày", ""));
                categoryDao.insert(new Category("Trái cây sấy khô", "Trái cây sấy khô, tiện lợi bảo quản", ""));

                // === Products ===
                // Thay URL bằng ảnh thật của bạn
                ProductDao productDao = database.productDao();

                // Nhiệt đới (categoryId = 1)
                productDao.insert(new Product("Xoài cát Hòa Lộc", "Xoài cát Hòa Lộc chín cây, thịt vàng óng, ngọt đậm đà, thơm tự nhiên.", 65000, "https://picsum.photos/seed/xoai/400/400", "kg", 1));
                productDao.insert(new Product("Sầu riêng Ri6", "Sầu riêng Ri6 hạt lép, cơm vàng, béo ngậy, thơm nồng đặc trưng.", 120000, "https://picsum.photos/seed/saurieng/400/400", "kg", 1));
                productDao.insert(new Product("Măng cụt", "Măng cụt tươi, vỏ tím đậm, ruột trắng ngọt thanh mát.", 55000, "https://picsum.photos/seed/mangcut/400/400", "kg", 1));
                productDao.insert(new Product("Thanh long ruột đỏ", "Thanh long ruột đỏ, ngọt mát, giàu vitamin C và chất xơ.", 35000, "https://picsum.photos/seed/thanhlong/400/400", "kg", 1));
                productDao.insert(new Product("Dừa xiêm", "Dừa xiêm nước ngọt thanh, giải khát tự nhiên.", 15000, "https://picsum.photos/seed/dua/400/400", "trái", 1));

                // Nhập khẩu (categoryId = 2)
                productDao.insert(new Product("Nho xanh Mỹ", "Nho xanh không hạt nhập khẩu từ Mỹ, giòn ngọt tự nhiên.", 150000, "https://picsum.photos/seed/nhoxanh/400/400", "kg", 2));
                productDao.insert(new Product("Táo Envy New Zealand", "Táo Envy vỏ đỏ đậm, giòn ngọt, thơm hương đặc trưng.", 89000, "https://picsum.photos/seed/taoenvy/400/400", "kg", 2));
                productDao.insert(new Product("Cherry Úc", "Cherry Úc tươi, đỏ mọng, vị ngọt chua hài hòa.", 250000, "https://picsum.photos/seed/cherry/400/400", "kg", 2));
                productDao.insert(new Product("Kiwi vàng", "Kiwi vàng Zespri New Zealand, ngọt thanh, giàu vitamin.", 180000, "https://picsum.photos/seed/kiwi/400/400", "kg", 2));
                productDao.insert(new Product("Lê Hàn Quốc", "Lê Hàn Quốc trái to, giòn mọng nước, ngọt mát.", 95000, "https://picsum.photos/seed/lehanquoc/400/400", "kg", 2));

                // Theo mùa (categoryId = 3)
                productDao.insert(new Product("Vải thiều", "Vải thiều Bắc Giang, quả tròn, cùi dày, ngọt lịm.", 45000, "https://picsum.photos/seed/vaithieu/400/400", "kg", 3));
                productDao.insert(new Product("Nhãn lồng", "Nhãn lồng Hưng Yên, cùi dày giòn, ngọt thanh.", 50000, "https://picsum.photos/seed/nhanlong/400/400", "kg", 3));
                productDao.insert(new Product("Chôm chôm", "Chôm chôm Java, quả to, cùi dày tách hạt, ngọt đậm.", 30000, "https://picsum.photos/seed/chomchom/400/400", "kg", 3));
                productDao.insert(new Product("Mận hậu", "Mận hậu Sơn La, quả tím đỏ, giòn ngọt đặc trưng vùng Tây Bắc.", 40000, "https://picsum.photos/seed/manhau/400/400", "kg", 3));

                // Sấy khô (categoryId = 4)
                productDao.insert(new Product("Xoài sấy dẻo", "Xoài sấy dẻo tự nhiên, giữ nguyên hương vị, không chất bảo quản.", 85000, "https://picsum.photos/seed/xoaisay/400/400", "kg", 4));
                productDao.insert(new Product("Mít sấy giòn", "Mít sấy chân không giòn tan, thơm ngon, tiện lợi mang đi.", 95000, "https://picsum.photos/seed/mitsay/400/400", "kg", 4));
                productDao.insert(new Product("Chuối sấy", "Chuối sấy giòn tự nhiên, vị ngọt nhẹ, ăn vặt lành mạnh.", 60000, "https://picsum.photos/seed/chuoisay/400/400", "kg", 4));
            });
        }
    }
}
