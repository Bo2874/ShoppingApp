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
import com.example.shoppingapp.database.dao.UserDao;
import com.example.shoppingapp.database.entity.Category;
import com.example.shoppingapp.database.entity.Favorite;
import com.example.shoppingapp.database.entity.Order;
import com.example.shoppingapp.database.entity.OrderDetail;
import com.example.shoppingapp.database.entity.Product;
import com.example.shoppingapp.database.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Category.class, Product.class, Order.class, OrderDetail.class, Favorite.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract ProductDao productDao();
    public abstract OrderDao orderDao();
    public abstract OrderDetailDao orderDetailDao();
    public abstract FavoriteDao favoriteDao();

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
                User u1 = new User("nguyenvana", "123456", "Nguyễn Văn A", "0912345678");
                u1.setEmail("nguyenvana@gmail.com");
                userDao.insert(u1);
                User u2 = new User("tranthib", "123456", "Trần Thị B", "0923456789");
                u2.setEmail("tranthib@gmail.com");
                userDao.insert(u2);

                // === Seed Categories ===
                CategoryDao categoryDao = database.categoryDao();
                categoryDao.insert(new Category("Sneakers", "Giày thể thao thời trang", "https://cdn-icons-png.flaticon.com/128/2589/2589903.png"));
                categoryDao.insert(new Category("Running", "Giày chạy bộ chuyên dụng", "https://cdn-icons-png.flaticon.com/128/2589/2589920.png"));
                categoryDao.insert(new Category("Basketball", "Giày bóng rổ cao cấp", "https://cdn-icons-png.flaticon.com/128/3163/3163038.png"));
                categoryDao.insert(new Category("Casual", "Giày đi hàng ngày thoải mái", "https://cdn-icons-png.flaticon.com/128/2589/2589910.png"));
                categoryDao.insert(new Category("Boots", "Giày boot cá tính", "https://cdn-icons-png.flaticon.com/128/2310/2310760.png"));

                // === Seed Products ===
                ProductDao productDao = database.productDao();
                Product p;

                // Sneakers (categoryId = 1)
                p = new Product("Air Max 90", "Nike", "Giày Nike Air Max 90 mang phong cách cổ điển với đệm Air Max ở gót chân, đế ngoài waffle bền bỉ. Thiết kế vượt thời gian phù hợp mọi outfit.", 2890000, "https://static.nike.com/a/images/t_PDP_1280_v1/f_auto,q_auto:eco/fd17b420-b388-4c8a-aaaa-e0a98ddf175f/air-max-90-shoes-kRsBnD.png", "đôi", 1);
                p.setSizes("38,39,40,41,42,43");
                p.setRating(4.8f);
                p.setReviewCount(234);
                productDao.insert(p);

                p = new Product("Superstar", "Adidas", "Adidas Superstar - biểu tượng đường phố từ thập niên 70. Mũi giày vỏ sò đặc trưng, da cao cấp, đế cao su chắc chắn.", 2190000, "https://assets.adidas.com/images/h_840,f_auto,q_auto,fl_lossy,c_fill,g_auto/7ed0855435194229a525aad6009a0497_9366/Superstar_Shoes_White_EG4958_01_standard.jpg", "đôi", 1);
                p.setSizes("39,40,41,42,43");
                p.setRating(4.6f);
                p.setReviewCount(189);
                productDao.insert(p);

                p = new Product("Old Skool", "Vans", "Vans Old Skool - giày skate huyền thoại với sọc Jazz Stripe nổi bật. Chất liệu canvas và da lộn bền bỉ, đế waffle chống trượt.", 1690000, "https://images.journeys.com/images/products/1_LARGE/773-1615017-BLACK-1702406133.jpg", "đôi", 1);
                p.setSizes("38,39,40,41,42,43,44");
                p.setRating(4.5f);
                p.setReviewCount(312);
                productDao.insert(p);

                p = new Product("Chuck Taylor All Star", "Converse", "Converse Chuck Taylor All Star - đôi giày kinh điển nhất mọi thời đại. Canvas bền, đế cao su vulcanized, phù hợp mọi phong cách.", 1490000, "https://www.converse.com/dw/image/v2/BCZC_PRD/on/demandware.static/-/Sites-cnv-master-catalog/default/dw1cb70a6c/images/a_107/M9166_A_107X1.jpg", "đôi", 1);
                p.setSizes("37,38,39,40,41,42,43");
                p.setRating(4.7f);
                p.setReviewCount(456);
                productDao.insert(p);

                // Running (categoryId = 2)
                p = new Product("Ultraboost Light", "Adidas", "Adidas Ultraboost Light - công nghệ BOOST mang lại cảm giác đàn hồi tuyệt vời. Primeknit ôm chân, hỗ trợ tối đa khi chạy.", 4290000, "https://assets.adidas.com/images/h_840,f_auto,q_auto,fl_lossy,c_fill,g_auto/c17d241f82c04e2b879faf1600f8ac0c_9366/Ultraboost_Light_Running_Shoes_Grey_HQ6339_01_standard.jpg", "đôi", 2);
                p.setOriginalPrice(5290000);
                p.setSizes("39,40,41,42,43,44");
                p.setRating(4.9f);
                p.setReviewCount(178);
                productDao.insert(p);

                p = new Product("Air Zoom Pegasus 40", "Nike", "Nike Pegasus 40 - đôi giày chạy đáng tin cậy nhất. Zoom Air êm ái, lưới thoáng khí, phù hợp cả chạy bộ và tập gym.", 3190000, "https://static.nike.com/a/images/t_PDP_1280_v1/f_auto,q_auto:eco/a72a18c2-2c18-4c2f-852d-c9a1e4e2ef65/pegasus-40-road-running-shoes-zDx9lM.png", "đôi", 2);
                p.setSizes("39,40,41,42,43");
                p.setRating(4.7f);
                p.setReviewCount(267);
                productDao.insert(p);

                p = new Product("Fresh Foam 1080v13", "New Balance", "New Balance 1080v13 - đệm Fresh Foam X dày dặn, Hypoknit ôm chân mềm mại. Hoàn hảo cho chạy đường dài.", 3890000, "https://nb.scene7.com/is/image/NB/m1080v13_nb_02_i?$pdpflexf2$&wid=440&hei=440", "đôi", 2);
                p.setSizes("40,41,42,43,44");
                p.setRating(4.8f);
                p.setReviewCount(145);
                productDao.insert(p);

                // Basketball (categoryId = 3)
                p = new Product("LeBron XXI", "Nike", "Nike LeBron 21 - công nghệ Zoom Air kép cho khả năng bật nhảy vượt trội. Thiết kế hầm hố, bám sân tối đa.", 5490000, "https://static.nike.com/a/images/t_PDP_1280_v1/f_auto,q_auto:eco/0d2d tried-ec3c-4ef5-932f-c8a99b106070/lebron-xxi-shoes.png", "đôi", 3);
                p.setOriginalPrice(6490000);
                p.setSizes("40,41,42,43,44,45");
                p.setRating(4.9f);
                p.setReviewCount(89);
                productDao.insert(p);

                p = new Product("Curry 11", "Under Armour", "Under Armour Curry 11 - nhẹ nhàng, linh hoạt với UA Flow không đế cao su. Traction tuyệt vời, hỗ trợ mắt cá chân.", 4290000, "https://underarmour.scene7.com/is/image/Underarmour/3026615-100_DEFAULT?rp=standard-30pad%7CpdpMainDesktop&scl=1&fmt=jpg&qlt=85&resMode=sharp2&cache=on%2Con&bgc=f0f0f0&wid=566&hei=566&size=536%2C536", "đôi", 3);
                p.setSizes("40,41,42,43,44");
                p.setRating(4.6f);
                p.setReviewCount(67);
                productDao.insert(p);

                p = new Product("Harden Vol. 8", "Adidas", "Adidas Harden Vol. 8 - Boost cushioning cho bước di chuyển tự tin. Đế ngoài Continental cho độ bám đỉnh cao.", 3790000, "https://assets.adidas.com/images/h_840,f_auto,q_auto,fl_lossy,c_fill,g_auto/e2e28bc9e26b4de2a903afee013e3730_9366/Harden_Vol._8_Shoes_Blue_IH2670_01_standard.jpg", "đôi", 3);
                p.setSizes("40,41,42,43,44,45");
                p.setRating(4.5f);
                p.setReviewCount(54);
                productDao.insert(p);

                // Casual (categoryId = 4)
                p = new Product("Stan Smith", "Adidas", "Adidas Stan Smith - thiết kế tối giản, thanh lịch. Da premium mềm mại, đế cupsole thoải mái, biểu tượng thời trang bền vững.", 2490000, "https://assets.adidas.com/images/h_840,f_auto,q_auto,fl_lossy,c_fill,g_auto/0849c8f0328a4f2f994aad6800b9b0ca_9366/Stan_Smith_Shoes_White_FX5502_01_standard.jpg", "đôi", 4);
                p.setSizes("37,38,39,40,41,42,43");
                p.setRating(4.7f);
                p.setReviewCount(523);
                productDao.insert(p);

                p = new Product("Gazelle", "Adidas", "Adidas Gazelle - phong cách retro từ thập niên 90 trở lại. Da lộn mềm, đế gum classic, phối màu đa dạng.", 2290000, "https://assets.adidas.com/images/h_840,f_auto,q_auto,fl_lossy,c_fill,g_auto/2e6e7e2a1b2e4c35b228aedd0113b230_9366/Gazelle_Shoes_Blue_IG2090_01_standard.jpg", "đôi", 4);
                p.setOriginalPrice(2690000);
                p.setSizes("38,39,40,41,42,43");
                p.setRating(4.6f);
                p.setReviewCount(287);
                productDao.insert(p);

                p = new Product("Club C 85", "Reebok", "Reebok Club C 85 - giày tennis cổ điển, da mềm trắng tinh, đế thấp thoải mái. Phù hợp phong cách smart casual.", 1890000, "https://images.reebok.eu/images/h_840,f_auto,q_auto,fl_lossy,c_fill,g_auto/e3a3d1c5e9d54f2e8d4fa6cb01174a42_9366/Club_C_85_Shoes_White_AR0456_01_standard.jpg", "đôi", 4);
                p.setSizes("38,39,40,41,42,43");
                p.setRating(4.4f);
                p.setReviewCount(198);
                productDao.insert(p);

                // Boots (categoryId = 5)
                p = new Product("6-Inch Premium Boot", "Timberland", "Timberland 6-Inch Premium - biểu tượng boot vượt thời gian. Da nubuck chống nước, đế lug chắc chắn, đệm êm cả ngày.", 4890000, "https://images.timberland.com/is/image/TimberlandEU/10061713-hero?wid=720&hei=720&fit=constrain,1&qlt=85,1&op_usm=1,1,6,0", "đôi", 5);
                p.setSizes("39,40,41,42,43,44");
                p.setRating(4.8f);
                p.setReviewCount(345);
                productDao.insert(p);

                p = new Product("1460 Smooth", "Dr. Martens", "Dr. Martens 1460 - 8 lỗ xỏ dây kinh điển. Da Smooth bóng, đế AirWair đàn hồi, welt vàng đặc trưng. Bền bỉ theo năm tháng.", 4290000, "https://i1.adis.ws/i/drmartens/11822006.88.jpg?$large$", "đôi", 5);
                p.setSizes("38,39,40,41,42,43");
                p.setRating(4.7f);
                p.setReviewCount(267);
                productDao.insert(p);

                p = new Product("Chelsea Boot", "Dr. Martens", "Dr. Martens 2976 Chelsea Boot - thiết kế slip-on tiện lợi với miếng đàn hồi hai bên. Da nappa mềm, đế bouncing sole.", 3990000, "https://i1.adis.ws/i/drmartens/22227001.88.jpg?$large$", "đôi", 5);
                p.setOriginalPrice(4590000);
                p.setSizes("38,39,40,41,42,43");
                p.setRating(4.6f);
                p.setReviewCount(189);
                productDao.insert(p);
            });
        }
    };
}
