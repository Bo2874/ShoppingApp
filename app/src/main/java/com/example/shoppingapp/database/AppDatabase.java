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

@Database(entities = {User.class, Category.class, Product.class, Order.class, OrderDetail.class}, version = 2, exportSchema = false)
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
                                    "shopping_db")
                            .fallbackToDestructiveMigration()
                            .addCallback(seedCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback seedCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseExecutor.execute(() -> {
                AppDatabase database = INSTANCE;

                // === Seed Users ===
                UserDao userDao = database.userDao();
                userDao.insert(new User("admin", "123456", "Quản trị viên", "0901234567"));
                userDao.insert(new User("nguyenvana", "123456", "Nguyễn Văn A", "0912345678"));
                userDao.insert(new User("tranthib", "123456", "Trần Thị B", "0923456789"));

                // === Seed Categories ===
                CategoryDao categoryDao = database.categoryDao();
                categoryDao.insert(new Category("Rau lá",
                        "Các loại rau lá xanh tươi ngon",
                        "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400"));
                categoryDao.insert(new Category("Củ quả",
                        "Các loại củ quả tươi sạch",
                        "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400"));
                categoryDao.insert(new Category("Trái cây",
                        "Trái cây tươi ngon mỗi ngày",
                        "https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=400"));
                categoryDao.insert(new Category("Nấm",
                        "Các loại nấm tươi sạch",
                        "https://images.unsplash.com/photo-1504545102780-26774c1bb073?w=400"));
                categoryDao.insert(new Category("Gia vị",
                        "Gia vị và thảo mộc tươi",
                        "https://images.unsplash.com/photo-1466637574441-749b8f19452f?w=400"));
                categoryDao.insert(new Category("Đậu & Hạt",
                        "Các loại đậu và hạt dinh dưỡng",
                        "https://images.unsplash.com/photo-1515543904323-bce18976ad37?w=400"));

                // === Seed Products ===
                ProductDao productDao = database.productDao();

                // Rau lá (categoryId = 1)
                productDao.insert(new Product("Rau cải xanh",
                        "Rau cải xanh tươi, giàu vitamin C và chất xơ",
                        15000, "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400", "bó", 1));
                productDao.insert(new Product("Xà lách",
                        "Xà lách giòn tươi, thích hợp làm salad",
                        12000, "https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=400", "bó", 1));
                productDao.insert(new Product("Rau muống",
                        "Rau muống xanh non, luộc hoặc xào đều ngon",
                        10000, "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400", "bó", 1));
                productDao.insert(new Product("Bắp cải",
                        "Bắp cải tươi xanh, giàu chất xơ",
                        20000, "https://images.unsplash.com/photo-1594282486552-05b4d80fbb9f?w=400", "kg", 1));

                // Củ quả (categoryId = 2)
                productDao.insert(new Product("Cà rốt",
                        "Cà rốt tươi, giàu beta-carotene tốt cho mắt",
                        25000, "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400", "kg", 2));
                productDao.insert(new Product("Khoai tây",
                        "Khoai tây sạch, thích hợp chiên, nướng, hầm",
                        18000, "https://images.unsplash.com/photo-1518977676601-b28d4a6ea6f4?w=400", "kg", 2));
                productDao.insert(new Product("Cà chua",
                        "Cà chua chín đỏ, giàu lycopene",
                        30000, "https://images.unsplash.com/photo-1546470427-0d4db154ceb8?w=400", "kg", 2));
                productDao.insert(new Product("Hành tây",
                        "Hành tây tươi, thơm nồng đặc trưng",
                        22000, "https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?w=400", "kg", 2));
                productDao.insert(new Product("Ớt chuông",
                        "Ớt chuông đủ màu, giòn ngọt tự nhiên",
                        45000, "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=400", "kg", 2));

                // Trái cây (categoryId = 3)
                productDao.insert(new Product("Chuối",
                        "Chuối chín vàng, giàu kali và vitamin B6",
                        20000, "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400", "nải", 3));
                productDao.insert(new Product("Cam",
                        "Cam ngọt mọng nước, giàu vitamin C",
                        35000, "https://images.unsplash.com/photo-1547514701-42782101795e?w=400", "kg", 3));
                productDao.insert(new Product("Táo",
                        "Táo đỏ giòn ngọt, nhập khẩu chất lượng",
                        55000, "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400", "kg", 3));
                productDao.insert(new Product("Dâu tây",
                        "Dâu tây Đà Lạt tươi, thơm ngon tự nhiên",
                        85000, "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400", "hộp", 3));
                productDao.insert(new Product("Nho",
                        "Nho xanh không hạt, ngọt thanh mát",
                        65000, "https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=400", "kg", 3));

                // Nấm (categoryId = 4)
                productDao.insert(new Product("Nấm rơm",
                        "Nấm rơm tươi, thích hợp nấu canh, xào",
                        40000, "https://images.unsplash.com/photo-1504545102780-26774c1bb073?w=400", "kg", 4));
                productDao.insert(new Product("Nấm đùi gà",
                        "Nấm đùi gà trắng, giòn dai thơm ngon",
                        50000, "https://images.unsplash.com/photo-1552825897-bb05e1365cca?w=400", "kg", 4));

                // Gia vị (categoryId = 5)
                productDao.insert(new Product("Gừng",
                        "Gừng tươi, vị cay nồng, tốt cho sức khỏe",
                        30000, "https://images.unsplash.com/photo-1615485500704-8e990f9900f7?w=400", "kg", 5));
                productDao.insert(new Product("Húng quế",
                        "Húng quế thơm, dùng ăn kèm phở, bún",
                        5000, "https://images.unsplash.com/photo-1466637574441-749b8f19452f?w=400", "bó", 5));
                productDao.insert(new Product("Sả",
                        "Sả tươi thơm, gia vị không thể thiếu",
                        8000, "https://images.unsplash.com/photo-1595855759920-86582396756a?w=400", "bó", 5));

                // Đậu & Hạt (categoryId = 6)
                productDao.insert(new Product("Đậu phộng",
                        "Đậu phộng rang muối, giàu protein",
                        35000, "https://images.unsplash.com/photo-1515543904323-bce18976ad37?w=400", "kg", 6));
                productDao.insert(new Product("Đậu đen",
                        "Đậu đen hữu cơ, nấu chè, nấu cháo",
                        28000, "https://images.unsplash.com/photo-1551462147-ff29053bfc14?w=400", "kg", 6));
            });
        }
    };
}
