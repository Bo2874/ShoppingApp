package com.example.shoppingapp.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.shoppingapp.database.dao.CategoryDao;
import com.example.shoppingapp.database.dao.FavoriteDao;
import com.example.shoppingapp.database.dao.OrderDao;
import com.example.shoppingapp.database.dao.OrderDetailDao;
import com.example.shoppingapp.database.dao.ProductDao;
import com.example.shoppingapp.database.dao.ReviewDao;
import com.example.shoppingapp.database.dao.UserDao;
import com.example.shoppingapp.database.entity.Category;
import com.example.shoppingapp.database.entity.Favorite;
import com.example.shoppingapp.database.entity.Order;
import com.example.shoppingapp.database.entity.OrderDetail;
import com.example.shoppingapp.database.entity.Product;
import com.example.shoppingapp.database.entity.Review;
import com.example.shoppingapp.database.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Category.class, Product.class, Order.class, OrderDetail.class, Favorite.class, Review.class}, version = 12, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract ProductDao productDao();
    public abstract OrderDao orderDao();
    public abstract OrderDetailDao orderDetailDao();
    public abstract FavoriteDao favoriteDao();
    public abstract ReviewDao reviewDao();

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

                // === Seed Users ===
                UserDao userDao = database.userDao();
                User u1 = new User("nguyenvana", "123456", "Nguyễn Văn A", "0912345678");
                u1.setEmail("nguyenvana@gmail.com");
                userDao.insert(u1);

                // === Seed Categories ===
                CategoryDao categoryDao = database.categoryDao();
                categoryDao.insert(new Category("Sneakers", "Giày thể thao thời trang", "res://drawable/cat_sneakers"));
                categoryDao.insert(new Category("Running", "Giày chạy bộ chuyên dụng", "res://drawable/cat_running"));
                categoryDao.insert(new Category("Basketball", "Giày bóng rổ cao cấp", "res://drawable/cat_basketball"));
                categoryDao.insert(new Category("Casual", "Giày đi hàng ngày thoải mái", "res://drawable/cat_casual"));
                categoryDao.insert(new Category("Boots", "Giày boot cá tính", "res://drawable/cat_boots"));

                // === Seed Products ===
                ProductDao productDao = database.productDao();
                Product p;

                // Sneakers (categoryId = 1)
                p = new Product("Air Max 90", "Nike", "Giày Nike Air Max 90 mang phong cách cổ điển với đệm Air Max ở gót chân, đế ngoài waffle bền bỉ.", 2890000, "res://drawable/prod_air_max_90", "đôi", 1);
                p.setSizes("38,39,40,41,42,43");
                p.setStockQuantity(15);
                p.setRating(4.8f);
                p.setReviewCount(234);
                productDao.insert(p);

                p = new Product("Superstar", "Adidas", "Adidas Superstar - biểu tượng đường phố từ thập niên 70. Mũi giày vỏ sò đặc trưng.", 2190000, "res://drawable/prod_superstar", "đôi", 1);
                p.setSizes("39,40,41,42,43");
                p.setStockQuantity(0);
                p.setRating(4.6f);
                p.setReviewCount(189);
                productDao.insert(p);

                p = new Product("Old Skool", "Vans", "Vans Old Skool - giày skate huyền thoại với sọc Jazz Stripe nổi bật.", 1690000, "res://drawable/prod_old_skool", "đôi", 1);
                p.setSizes("38,39,40,41,42,43,44");
                p.setStockQuantity(25);
                p.setRating(4.5f);
                p.setReviewCount(312);
                productDao.insert(p);

                p = new Product("Chuck Taylor", "Converse", "Converse Chuck Taylor All Star - đôi giày kinh điển nhất mọi thời đại.", 1490000, "res://drawable/prod_chuck_taylor", "đôi", 1);
                p.setSizes("37,38,39,40,41,42,43");
                p.setStockQuantity(40);
                p.setRating(4.7f);
                p.setReviewCount(456);
                productDao.insert(p);

                // Running (categoryId = 2)
                p = new Product("Ultraboost Light", "Adidas", "Adidas Ultraboost Light - công nghệ BOOST mang lại cảm giác đàn hồi tuyệt vời.", 4290000, "res://drawable/prod_ultraboost_light", "đôi", 2);
                p.setOriginalPrice(5290000);
                p.setSizes("39,40,41,42,43,44");
                p.setStockQuantity(12);
                p.setRating(4.9f);
                p.setReviewCount(178);
                productDao.insert(p);

                p = new Product("Pegasus 40", "Nike", "Nike Pegasus 40 - đôi giày chạy đáng tin cậy nhất. Zoom Air êm ái.", 3190000, "res://drawable/prod_pegasus_40", "đôi", 2);
                p.setSizes("39,40,41,42,43");
                p.setStockQuantity(20);
                p.setRating(4.7f);
                p.setReviewCount(267);
                productDao.insert(p);

                p = new Product("Fresh Foam 1080", "New Balance", "New Balance 1080v13 - đệm Fresh Foam X dày dặn, hoàn hảo cho chạy đường dài.", 3890000, "res://drawable/prod_fresh_foam_1080", "đôi", 2);
                p.setSizes("40,41,42,43,44");
                p.setStockQuantity(8);
                p.setRating(4.8f);
                p.setReviewCount(145);
                productDao.insert(p);

                // Basketball (categoryId = 3)
                p = new Product("LeBron XXI", "Nike", "Nike LeBron 21 - công nghệ Zoom Air kép cho khả năng bật nhảy vượt trội.", 5490000, "res://drawable/prod_lebron_21", "đôi", 3);
                p.setOriginalPrice(6490000);
                p.setSizes("40,41,42,43,44,45");
                p.setStockQuantity(5);
                p.setRating(4.9f);
                p.setReviewCount(89);
                productDao.insert(p);

                p = new Product("Curry 11", "Under Armour", "Under Armour Curry 11 - nhẹ nhàng, linh hoạt với UA Flow.", 4290000, "res://drawable/prod_curry_11", "đôi", 3);
                p.setSizes("40,41,42,43,44");
                p.setStockQuantity(0);
                p.setRating(4.6f);
                p.setReviewCount(67);
                productDao.insert(p);

                p = new Product("Harden Vol. 8", "Adidas", "Adidas Harden Vol. 8 - Boost cushioning cho bước di chuyển tự tin.", 3790000, "res://drawable/prod_harden_8", "đôi", 3);
                p.setSizes("40,41,42,43,44,45");
                p.setStockQuantity(18);
                p.setRating(4.5f);
                p.setReviewCount(54);
                productDao.insert(p);

                // Casual (categoryId = 4)
                p = new Product("Stan Smith", "Adidas", "Adidas Stan Smith - thiết kế tối giản, thanh lịch. Da premium mềm mại.", 2490000, "res://drawable/prod_stan_smith", "đôi", 4);
                p.setSizes("37,38,39,40,41,42,43");
                p.setStockQuantity(32);
                p.setRating(4.7f);
                p.setReviewCount(523);
                productDao.insert(p);

                p = new Product("Gazelle", "Adidas", "Adidas Gazelle - phong cách retro từ thập niên 90, da lộn mềm, đế gum classic.", 2290000, "res://drawable/prod_gazelle", "đôi", 4);
                p.setOriginalPrice(2690000);
                p.setSizes("38,39,40,41,42,43");
                p.setStockQuantity(22);
                p.setRating(4.6f);
                p.setReviewCount(287);
                productDao.insert(p);

                p = new Product("Club C 85", "Reebok", "Reebok Club C 85 - giày tennis cổ điển, da mềm trắng tinh.", 1890000, "res://drawable/prod_club_c_85", "đôi", 4);
                p.setSizes("38,39,40,41,42,43");
                p.setStockQuantity(0);
                p.setRating(4.4f);
                p.setReviewCount(198);
                productDao.insert(p);

                // Boots (categoryId = 5)
                p = new Product("6-Inch Premium", "Timberland", "Timberland 6-Inch Premium - biểu tượng boot vượt thời gian. Da nubuck chống nước.", 4890000, "res://drawable/prod_timberland_6inch", "đôi", 5);
                p.setSizes("39,40,41,42,43,44");
                p.setStockQuantity(11);
                p.setRating(4.8f);
                p.setReviewCount(345);
                productDao.insert(p);

                p = new Product("1460 Smooth", "Dr. Martens", "Dr. Martens 1460 - 8 lỗ xỏ dây kinh điển. Da Smooth bóng, đế AirWair đàn hồi.", 4290000, "res://drawable/prod_dr_martens_1460", "đôi", 5);
                p.setSizes("38,39,40,41,42,43");
                p.setStockQuantity(9);
                p.setRating(4.7f);
                p.setReviewCount(267);
                productDao.insert(p);

                p = new Product("Chelsea Boot", "Dr. Martens", "Dr. Martens 2976 Chelsea Boot - thiết kế slip-on tiện lợi, da nappa mềm.", 3990000, "res://drawable/prod_chelsea_boot", "đôi", 5);
                p.setOriginalPrice(4590000);
                p.setSizes("38,39,40,41,42,43");
                p.setStockQuantity(14);
                p.setRating(4.6f);
                p.setReviewCount(189);
                productDao.insert(p);

                // === Seed Reviews ===
                ReviewDao reviewDao = database.reviewDao();

                // Product 1: Air Max 90
                reviewDao.insert(new Review(1, 1, 5.0f, "Êm chân, đi cả ngày không mỏi.", "15/03/2025", "Nguyễn Văn A"));
                reviewDao.insert(new Review(1, 1, 4.5f, "Đẹp, phối đồ dễ.", "14/03/2025", "Trần Thị B"));

                // Product 2: Superstar
                reviewDao.insert(new Review(1, 2, 4.0f, "Form đẹp, chất liệu ổn.", "12/03/2025", "Lê Minh"));

                // Product 3: Old Skool
                reviewDao.insert(new Review(1, 3, 4.5f, "Đi rất chắc chân.", "13/03/2025", "Vũ Hương"));
                reviewDao.insert(new Review(1, 3, 4.0f, "Giá ổn trong tầm tiền.", "10/03/2025", "Phạm Dũng"));

                // Product 4: Chuck Taylor
                reviewDao.insert(new Review(1, 4, 5.0f, "Kinh điển, không bao giờ lỗi mốt.", "11/03/2025", "Hoàng Nam"));

                // Product 5: Ultraboost Light
                reviewDao.insert(new Review(1, 5, 5.0f, "Đệm BOOST cực kỳ êm.", "09/03/2025", "Nguyễn Khánh"));
                reviewDao.insert(new Review(1, 5, 4.5f, "Chạy bộ rất thích.", "08/03/2025", "Trần Long"));

                // Product 6: Pegasus 40
                reviewDao.insert(new Review(1, 6, 4.5f, "Giày chạy ổn định.", "07/03/2025", "Hà Linh"));

                // Product 7: Fresh Foam 1080
                reviewDao.insert(new Review(1, 7, 4.5f, "Đệm dày, chạy dài tốt.", "06/03/2025", "Ngọc Anh"));
                reviewDao.insert(new Review(1, 7, 4.0f, "Upper thoáng, nhẹ.", "05/03/2025", "Thanh Tùng"));

                // Product 8: LeBron XXI
                reviewDao.insert(new Review(1, 8, 5.0f, "Bám sân tốt, hỗ trợ bật nhảy.", "04/03/2025", "Quang Vinh"));

                // Product 9: Curry 11
                reviewDao.insert(new Review(1, 9, 4.5f, "Ôm chân, linh hoạt.", "03/03/2025", "Phúc An"));

                // Product 10: Harden Vol. 8
                reviewDao.insert(new Review(1, 10, 4.0f, "Êm, đi sân trong nhà tốt.", "02/03/2025", "Bảo Trân"));
                reviewDao.insert(new Review(1, 10, 4.5f, "Thiết kế cá tính.", "01/03/2025", "Tuấn Kiệt"));

                // Product 11: Stan Smith
                reviewDao.insert(new Review(1, 11, 5.0f, "Thanh lịch, mặc đồ nào cũng hợp.", "28/02/2025", "Minh Châu"));

                // Product 12: Gazelle
                reviewDao.insert(new Review(1, 12, 4.5f, "Retro đẹp, đế đi êm.", "27/02/2025", "Đức Hải"));
                reviewDao.insert(new Review(1, 12, 4.0f, "Màu rất dễ phối.", "26/02/2025", "An Nhi"));

                // Product 13: Club C 85
                reviewDao.insert(new Review(1, 13, 4.0f, "Nhẹ, đi hàng ngày ổn.", "25/02/2025", "Khánh Linh"));

                // Product 14: 6-Inch Premium
                reviewDao.insert(new Review(1, 14, 5.0f, "Boot cứng cáp, rất bền.", "24/02/2025", "Tuấn Anh"));
                reviewDao.insert(new Review(1, 14, 4.5f, "Da đẹp, chống nước tốt.", "23/02/2025", "Huyền My"));

                // Product 15: 1460 Smooth
                reviewDao.insert(new Review(1, 15, 4.5f, "Form đẹp, đi nổi bật.", "22/02/2025", "Thảo Nhi"));

                // Product 16: Chelsea Boot
                reviewDao.insert(new Review(1, 16, 4.5f, "Mang vào tiện, da mềm.", "21/02/2025", "Gia Hân"));
                reviewDao.insert(new Review(1, 16, 4.0f, "Đi làm rất hợp.", "20/02/2025", "Hải Đăng"));

                // Tính điểm trung bình và số lượt đánh giá thật từ bảng reviews
                for (Product product : productDao.getAllProducts()) {
                    float avg = reviewDao.getAverageRating(product.getId());
                    int count = reviewDao.getReviewCount(product.getId());
                    product.setRating(count > 0 ? avg : 0f);
                    product.setReviewCount(count);
                    productDao.update(product);
                }
            });
        }
    }
}
