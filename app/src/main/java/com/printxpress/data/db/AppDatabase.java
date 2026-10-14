package com.printxpress.data.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.util.Log;
import com.printxpress.data.entity.Address;
import com.printxpress.data.entity.CartItem;
import com.printxpress.data.entity.Category;
import com.printxpress.data.entity.CrashLog;
import com.printxpress.data.entity.Faq;
import com.printxpress.data.entity.Notification;
import com.printxpress.data.entity.Order;
import com.printxpress.data.entity.OrderItem;
import com.printxpress.data.entity.OrderStatusHistory;
import com.printxpress.data.entity.PaymentMethod;
import com.printxpress.data.entity.Product;
import com.printxpress.data.entity.ProductImage;
import com.printxpress.data.entity.ProductOption;
import com.printxpress.data.entity.Promotion;
import com.printxpress.data.entity.AdminActivityLog;
import com.printxpress.data.entity.SampleDesign;
import com.printxpress.data.entity.SavedDesign;
import com.printxpress.data.entity.SupportQuery;
import com.printxpress.data.entity.User;
import com.printxpress.data.entity.UserSettings;
import com.printxpress.data.entity.WishlistItem;
import com.printxpress.R;
import com.printxpress.util.PasswordUtil;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                User.class, Address.class, Category.class, Product.class,
                ProductOption.class, Order.class, OrderItem.class,
                OrderStatusHistory.class, SavedDesign.class, Notification.class,
                Promotion.class, Faq.class, SampleDesign.class, SupportQuery.class,
                CartItem.class, PaymentMethod.class, WishlistItem.class,
                ProductImage.class, UserSettings.class, CrashLog.class,
                AdminActivityLog.class
        },
        version = 14,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = "AppDatabase";

    public abstract UserDao userDao();
    public abstract CatalogDao catalogDao();
    public abstract OrderDao orderDao();
    public abstract CommonDao commonDao();
    public abstract SupportQueryDao supportQueryDao();
    public abstract CartDao cartDao();
    public abstract PaymentMethodDao paymentMethodDao();
    public abstract WishlistDao wishlistDao();
    public abstract ProductImageDao productImageDao();
    public abstract UserSettingsDao userSettingsDao();
    public abstract CrashLogDao crashLogDao();
    public abstract ActivityLogDao activityLogDao();

    public static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            try {
                db.execSQL("ALTER TABLE users ADD COLUMN hashAlgorithm TEXT DEFAULT 'SHA256'");
                db.execSQL("ALTER TABLE users ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0");
                db.execSQL("ALTER TABLE products ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1");
                db.execSQL("ALTER TABLE orders ADD COLUMN cancelReason TEXT DEFAULT ''");
                db.execSQL("ALTER TABLE orders ADD COLUMN promoCode TEXT DEFAULT ''");
                db.execSQL("ALTER TABLE orders ADD COLUMN discountAmount REAL NOT NULL DEFAULT 0.0");
                db.execSQL("ALTER TABLE support_queries ADD COLUMN ticketNumber TEXT DEFAULT ''");
                db.execSQL("ALTER TABLE saved_designs ADD COLUMN thumbnailPath TEXT DEFAULT ''");
                db.execSQL("ALTER TABLE notifications ADD COLUMN isDelivered INTEGER NOT NULL DEFAULT 0");

                db.execSQL("CREATE TABLE IF NOT EXISTS wishlist (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "userId INTEGER NOT NULL, " +
                        "productId INTEGER NOT NULL, " +
                        "addedAt INTEGER NOT NULL, " +
                        "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(productId) REFERENCES products(id) ON DELETE CASCADE)");
                db.execSQL("CREATE INDEX IF NOT EXISTS index_wishlist_userId ON wishlist(userId)");
                db.execSQL("CREATE INDEX IF NOT EXISTS index_wishlist_productId ON wishlist(productId)");
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_wishlist_userId_productId ON wishlist(userId, productId)");

                db.execSQL("CREATE TABLE IF NOT EXISTS product_images (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "productId INTEGER NOT NULL, " +
                        "imageUri TEXT, " +
                        "sortOrder INTEGER NOT NULL, " +
                        "FOREIGN KEY(productId) REFERENCES products(id) ON DELETE CASCADE)");
                db.execSQL("CREATE INDEX IF NOT EXISTS index_product_images_productId ON product_images(productId)");

                db.execSQL("CREATE TABLE IF NOT EXISTS user_settings (" +
                        "userId INTEGER PRIMARY KEY NOT NULL, " +
                        "notifyOrderUpdates INTEGER NOT NULL, " +
                        "notifyPromotions INTEGER NOT NULL, " +
                        "notifyNewProducts INTEGER NOT NULL, " +
                        "theme TEXT, " +
                        "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE)");

                db.execSQL("CREATE TABLE IF NOT EXISTS crash_logs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "message TEXT, " +
                        "stackTrace TEXT, " +
                        "userId INTEGER NOT NULL, " +
                        "timestamp INTEGER NOT NULL)");
            } catch (Exception e) {
                Log.e(TAG, "Migration 12->13 error", e);
            }
        }
    };

    public static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            try {
                db.execSQL("ALTER TABLE orders ADD COLUMN adminNotes TEXT DEFAULT ''");
                db.execSQL("ALTER TABLE orders ADD COLUMN rescheduleRequested INTEGER NOT NULL DEFAULT 0");
                db.execSQL("ALTER TABLE orders ADD COLUMN rescheduleDate INTEGER NOT NULL DEFAULT 0");
                db.execSQL("ALTER TABLE orders ADD COLUMN cancelApproved INTEGER NOT NULL DEFAULT 0");

                db.execSQL("CREATE TABLE IF NOT EXISTS admin_activity_logs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "adminUserId INTEGER NOT NULL, " +
                        "adminName TEXT, " +
                        "actionName TEXT, " +
                        "details TEXT, " +
                        "timestamp INTEGER NOT NULL)");
            } catch (Exception e) {
                Log.e(TAG, "Migration 13->14 error", e);
            }
        }
    };

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "printxpress_database"
                            )
                            .addMigrations(MIGRATION_12_13, MIGRATION_13_14)
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedDatabase() {
        databaseWriteExecutor.execute(() -> {
            if (INSTANCE == null) return;
            try {
                UserDao userDao = INSTANCE.userDao();
                CatalogDao catalogDao = INSTANCE.catalogDao();
                CommonDao commonDao = INSTANCE.commonDao();
                OrderDao orderDao = INSTANCE.orderDao();
                CartDao cartDao = INSTANCE.cartDao();
                PaymentMethodDao paymentMethodDao = INSTANCE.paymentMethodDao();
                SupportQueryDao supportQueryDao = INSTANCE.supportQueryDao();

                // 1. Seed Users (Admin & Customer)
                User adminUser = userDao.getUserByEmailSync("admin@printxpress.lk");
                if (adminUser == null) {
                    String salt = PasswordUtil.generateSalt();
                    String hash = PasswordUtil.hashPassword("admin123", salt);
                    adminUser = new User("System Administrator", "admin@printxpress.lk", "0770000000", hash, salt, System.currentTimeMillis(), "SUPER_ADMIN");
                    adminUser.id = userDao.insert(adminUser);
                }

                User customerUser = userDao.getUserByEmailSync("user@printxpress.lk");
                if (customerUser == null) {
                    String salt = PasswordUtil.generateSalt();
                    String hash = PasswordUtil.hashPassword("user123", salt);
                    customerUser = new User("John Doe", "user@printxpress.lk", "0771234567", hash, salt, System.currentTimeMillis(), "CUSTOMER");
                    customerUser.id = userDao.insert(customerUser);
                }

                long userId = customerUser.id;

                // 2. Seed Addresses
                long primaryAddressId = 1L;
                if (commonDao.getAddressCountSync() == 0 && userId > 0) {
                    Address defaultAddr = new Address(userId, "Home", "123 Galle Road", "Colombo 03", "Colombo", "00300", true);
                    Address officeAddr = new Address(userId, "Office", "45 Kandy Road", "Kadawatha", "Gampaha", "11850", false);
                    primaryAddressId = commonDao.insertAddress(defaultAddr);
                    commonDao.insertAddress(officeAddr);
                }

                // 3. Seed Categories, Products, ProductOptions, & SampleDesigns
                if (catalogDao.getCategoryCountSync() == 0) {
                    Category cat1 = new Category("Business Cards", R.drawable.ic_cat_business_card);
                    Category cat2 = new Category("Flyers", R.drawable.ic_cat_flyer);
                    Category cat3 = new Category("Posters", R.drawable.ic_cat_poster);
                    Category cat4 = new Category("Banners & Signs", R.drawable.ic_cat_banner);
                    Category cat5 = new Category("Stickers & Labels", R.drawable.ic_cat_sticker);
                    Category cat6 = new Category("Mugs", R.drawable.ic_cat_mug);
                    Category cat7 = new Category("T-Shirts", R.drawable.ic_cat_tshirt);
                    Category cat8 = new Category("Brochures", R.drawable.ic_cat_brochure);
                    Category cat9 = new Category("Calendars", R.drawable.ic_cat_calendar);
                    Category cat10 = new Category("Custom Order", R.drawable.ic_cat_custom);

                    long[] catIds = catalogDao.insertCategories(cat1, cat2, cat3, cat4, cat5, cat6, cat7, cat8, cat9, cat10);

                    // 33 Products with Relevant Category Drawables
                    Product p1 = new Product(catIds[0], "Standard Business Cards", "90x55 mm, 350gsm matte, single/double side. Premium quality.", 1500.00, R.drawable.ic_cat_business_card, "350gsm Matte", "90x55 mm");
                    Product p2 = new Product(catIds[0], "Premium Laminated Cards", "90x55 mm, 400gsm, matt or gloss lamination.", 2500.00, R.drawable.ic_cat_business_card, "400gsm Laminated", "90x55 mm");
                    Product p3 = new Product(catIds[0], "Spot UV Cards", "90x55 mm, 400gsm, spot UV finish for glossy contrast.", 3800.00, R.drawable.ic_cat_business_card, "400gsm Spot UV", "90x55 mm");
                    Product p4 = new Product(catIds[0], "Rounded Corner Cards", "90x55 mm, 350gsm, stylish rounded corners.", 2800.00, R.drawable.ic_cat_business_card, "350gsm Rounded", "90x55 mm");

                    Product p5 = new Product(catIds[1], "A5 Flyers Single Side", "148x210 mm, 130gsm art paper, vibrant colours.", 3500.00, R.drawable.ic_cat_flyer, "130gsm Art Paper", "A5");
                    Product p6 = new Product(catIds[1], "A5 Flyers Double Side", "148x210 mm, 130gsm art paper, double sided print.", 4500.00, R.drawable.ic_cat_flyer, "130gsm Art Paper", "A5");
                    Product p7 = new Product(catIds[1], "A4 Flyers", "210x297 mm, 150gsm gloss art paper.", 6000.00, R.drawable.ic_cat_flyer, "150gsm Gloss", "A4");
                    Product p8 = new Product(catIds[1], "DL Flyers", "99x210 mm, 130gsm rack cards.", 3000.00, R.drawable.ic_cat_flyer, "130gsm Art Paper", "DL");

                    Product p9 = new Product(catIds[2], "A4 Poster", "210x297 mm, 170gsm gloss finish.", 250.00, R.drawable.ic_cat_poster, "170gsm Gloss", "A4");
                    Product p10 = new Product(catIds[2], "A3 Poster", "297x420 mm, 170gsm high impact display.", 450.00, R.drawable.ic_cat_poster, "170gsm Gloss", "A3");
                    Product p11 = new Product(catIds[2], "A2 Poster", "420x594 mm, 200gsm heavy poster paper.", 900.00, R.drawable.ic_cat_poster, "200gsm Heavy", "A2");
                    Product p12 = new Product(catIds[2], "A1 Poster", "594x841 mm, 200gsm grand display poster.", 1800.00, R.drawable.ic_cat_poster, "200gsm Heavy", "A1");

                    Product p13 = new Product(catIds[3], "Vinyl Banner", "Custom size, outdoor vinyl, hemmed edges and eyelets.", 120.00, R.drawable.ic_cat_banner, "Heavy-duty Vinyl", "Custom");
                    Product p14 = new Product(catIds[3], "Roll-up Banner", "85x200 mm stand with high quality print included.", 6500.00, R.drawable.ic_cat_banner, "Stand + Banner", "85x200 cm");
                    Product p15 = new Product(catIds[3], "Flex Banner 3x6 ft", "440gsm flex banner, weatherproof.", 4500.00, R.drawable.ic_cat_banner, "440gsm Flex", "3x6 ft");
                    Product p16 = new Product(catIds[3], "Foam Board Sign", "2x3 ft, 5 mm rigid foam board sign.", 2500.00, R.drawable.ic_cat_banner, "5mm Foam Board", "2x3 ft");

                    Product p17 = new Product(catIds[4], "Vinyl Die-cut Stickers", "Custom shape, waterproof vinyl sticker sheet.", 2000.00, R.drawable.ic_cat_sticker, "Waterproof Vinyl", "Custom Shape");
                    Product p18 = new Product(catIds[4], "Round Label Sheet", "40 mm round labels on A4 sheets.", 1500.00, R.drawable.ic_cat_sticker, "Paper Sticker", "40mm Round");
                    Product p19 = new Product(catIds[4], "Waterproof Stickers", "Gloss or matt vinyl stickers for bottles & packaging.", 2800.00, R.drawable.ic_cat_sticker, "Gloss/Matt Vinyl", "Various");
                    Product p20 = new Product(catIds[4], "Product Label Roll", "Roll of 500 custom product labels.", 3500.00, R.drawable.ic_cat_sticker, "Roll Stock", "500 Labels");

                    Product p21 = new Product(catIds[5], "Classic White Mug", "11 oz ceramic mug, full wrap photo print.", 1200.00, R.drawable.ic_cat_mug, "Ceramic", "11 oz");
                    Product p22 = new Product(catIds[5], "Magic Colour-changing Mug", "11 oz mug, heat-reveal black to photo print.", 1800.00, R.drawable.ic_cat_mug, "Color Changing", "11 oz");
                    Product p23 = new Product(catIds[5], "Inner Colour Mug", "11 oz mug, coloured inside and handle matching.", 1500.00, R.drawable.ic_cat_mug, "Ceramic", "11 oz");
                    Product p24 = new Product(catIds[5], "Photo Mug 15 oz", "Large oversized ceramic mug for heavy coffee drinkers.", 1600.00, R.drawable.ic_cat_mug, "Large Ceramic", "15 oz");

                    Product p25 = new Product(catIds[6], "Cotton Round Neck T-shirt", "100% cotton, sizes S to XXL, black/white/navy/red.", 1800.00, R.drawable.ic_cat_tshirt, "100% Combed Cotton", "S - XXL");
                    Product p26 = new Product(catIds[6], "Polo T-shirt", "Cotton pique with chest print or embroidery.", 2400.00, R.drawable.ic_cat_tshirt, "Cotton Pique", "S - XL");
                    Product p27 = new Product(catIds[6], "Dri-fit Sports T-shirt", "Polyester activewear, full colour sublimation.", 2200.00, R.drawable.ic_cat_tshirt, "Polyester Sublimation", "S - XXL");
                    Product p28 = new Product(catIds[6], "Kids T-shirt", "100% cotton, sizes 2 to 12 years.", 1500.00, R.drawable.ic_cat_tshirt, "Cotton", "Ages 2-12");

                    Product p29 = new Product(catIds[7], "Tri-fold Brochure A4", "150gsm gloss art paper, 6 folded panels.", 9000.00, R.drawable.ic_cat_brochure, "150gsm Gloss", "A4 Tri-fold");
                    Product p30 = new Product(catIds[7], "Bi-fold Brochure A4", "150gsm gloss art paper, 4 panels.", 7500.00, R.drawable.ic_cat_brochure, "150gsm Gloss", "A4 Bi-fold");

                    Product p31 = new Product(catIds[8], "Desk Calendar", "12 pages custom desk calendar with wire-o and stand.", 800.00, R.drawable.ic_cat_calendar, "Cardstock + Stand", "Desktop");
                    Product p32 = new Product(catIds[8], "Wall Calendar", "12 pages wall calendar A3 size with hanging hook.", 1500.00, R.drawable.ic_cat_calendar, "Glossy Paper", "A3 Wall");

                    Product p33 = new Product(catIds[9], "Custom Print Request", "Describe your custom item, upload specs and request a quote.", 0.00, R.drawable.ic_cat_custom, "Custom", "Variable");

                    long[] prodIds = catalogDao.insertProducts(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, p25, p26, p27, p28, p29, p30, p31, p32, p33);

                    if (prodIds.length > 0) {
                        catalogDao.insertOptions(
                                new ProductOption(prodIds[0], "Paper Finish", "Matte Finish", 0.0),
                                new ProductOption(prodIds[0], "Paper Finish", "Gloss Lamination", 300.0),
                                new ProductOption(prodIds[0], "Corners", "Square Corners", 0.0),
                                new ProductOption(prodIds[0], "Corners", "Rounded Corners", 200.0)
                        );
                    }

                    catalogDao.insertSampleDesigns(
                            new SampleDesign(catIds[0], "Modern Corporate Card", R.drawable.ic_cat_business_card),
                            new SampleDesign(catIds[0], "Creative Studio Card", R.drawable.ic_cat_business_card),
                            new SampleDesign(catIds[0], "Minimalist Gold Card", R.drawable.ic_cat_business_card),
                            new SampleDesign(catIds[1], "Event Promo Flyer", R.drawable.ic_cat_flyer),
                            new SampleDesign(catIds[1], "Restaurant Menu Flyer", R.drawable.ic_cat_flyer),
                            new SampleDesign(catIds[2], "Event Showcase Poster", R.drawable.ic_cat_poster),
                            new SampleDesign(catIds[2], "Music Concert Poster", R.drawable.ic_cat_poster),
                            new SampleDesign(catIds[3], "Grand Opening Banner", R.drawable.ic_cat_banner),
                            new SampleDesign(catIds[3], "Roll-Up Stand Banner", R.drawable.ic_cat_banner),
                            new SampleDesign(catIds[4], "Die-Cut Brand Sticker", R.drawable.ic_cat_sticker),
                            new SampleDesign(catIds[4], "Round Packaging Seal", R.drawable.ic_cat_sticker),
                            new SampleDesign(catIds[5], "Personalized Name Mug", R.drawable.ic_cat_mug),
                            new SampleDesign(catIds[5], "Inspirational Quote Mug", R.drawable.ic_cat_mug),
                            new SampleDesign(catIds[6], "Typography Slogan Tee", R.drawable.ic_cat_tshirt),
                            new SampleDesign(catIds[6], "Team Number Uniform", R.drawable.ic_cat_tshirt),
                            new SampleDesign(catIds[7], "Tri-Fold Corporate Brochure", R.drawable.ic_cat_brochure),
                            new SampleDesign(catIds[8], "Custom Desk Calendar", R.drawable.ic_cat_calendar),
                            new SampleDesign(catIds[9], "Custom Print Specification", R.drawable.ic_cat_custom)
                    );
                }

                // 4. Seed FAQs
                if (commonDao.getFaqCountSync() == 0) {
                    commonDao.insertFaqs(
                            new Faq("What is the standard turnaround time?", "Standard printing takes 2-3 business days. Rush orders can be completed in 24 hours."),
                            new Faq("What file formats do you accept for design uploads?", "We accept PDF, PNG, JPG, and AI formats with a minimum resolution of 300 DPI."),
                            new Faq("Can I collect my order in person?", "Yes, select 'Store Pickup' during checkout to pick up your order directly from our print studio."),
                            new Faq("What are the bleed and safe margin requirements?", "Please keep all important text and graphics at least 3mm inside the cut line with a 2mm bleed."),
                            new Faq("Do you offer bulk order discounts?", "Yes! Orders of 500 units or more automatically receive a 25% bulk discount.")
                    );
                }

                // 5. Seed Promotions
                if (commonDao.getPromotionCountSync() == 0) {
                    long now = System.currentTimeMillis();
                    long oneMonth = 30L * 24 * 60 * 60 * 1000;
                    commonDao.insertPromotions(
                            new Promotion("First Order Special", "Get 15% off your very first order with PrintXpress!", 15.0, 1, now, now + oneMonth),
                            new Promotion("Avurudu Festive Package", "Celebrate Avurudu with 20% off all promotional flyers and banners!", 20.0, 10, now, now + oneMonth),
                            new Promotion("Christmas & New Year Mega Deal", "Save 25% on custom calendars, mugs, and business cards.", 25.0, 5, now, now + oneMonth),
                            new Promotion("Bulk Order Discount", "Save 25% when ordering 500 units or more.", 25.0, 500, now, now + oneMonth)
                    );
                }

                // 6. Seed CartItem
                if (cartDao.getCartCountSync() == 0 && userId > 0) {
                    CartItem sampleCartItem = new CartItem(
                            userId,
                            1L,
                            "Standard Business Cards",
                            1500.00,
                            2,
                            "350gsm Matte, Double-Sided",
                            "text",
                            "",
                            "John Doe - Software Engineer"
                    );
                    cartDao.insert(sampleCartItem);
                }

                // 6.5 Seed Payment Methods
                if (paymentMethodDao.getPaymentMethodCountSync() == 0 && userId > 0) {
                    paymentMethodDao.insert(new PaymentMethod(userId, "John Doe", "•••• •••• •••• 4242", "Visa", "12/28", true));
                    paymentMethodDao.insert(new PaymentMethod(userId, "John Doe", "•••• •••• •••• 8888", "Mastercard", "08/27", false));
                }

                // 7. Seed Orders, OrderItems, & OrderStatusHistory
                if (orderDao.getOrderCountSync() == 0 && userId > 0) {
                    long now = System.currentTimeMillis();
                    long tenDaysAgo = now - (10L * 24 * 60 * 60 * 1000);
                    long oneDayAgo = now - (1L * 24 * 60 * 60 * 1000);

                    // Order 1: Delivered
                    Order order1 = new Order(userId, "DELIVERED", "DELIVERY", primaryAddressId, tenDaysAgo, 3000.00, "Please deliver before 5 PM", tenDaysAgo);
                    long order1Id = orderDao.insertOrder(order1);

                    OrderItem order1Item = new OrderItem(order1Id, 1L, 2, "350gsm Matte", "John Doe", "", 1500.00);
                    orderDao.insertOrderItems(Collections.singletonList(order1Item));

                    orderDao.insertStatusHistory(new OrderStatusHistory(order1Id, "PENDING", tenDaysAgo));
                    orderDao.insertStatusHistory(new OrderStatusHistory(order1Id, "PROCESSING", tenDaysAgo + 3600000L));
                    orderDao.insertStatusHistory(new OrderStatusHistory(order1Id, "DELIVERED", tenDaysAgo + 86400000L));

                    // Order 2: Processing
                    Order order2 = new Order(userId, "PROCESSING", "DELIVERY", primaryAddressId, oneDayAgo, 4500.00, "Handle with care", oneDayAgo);
                    long order2Id = orderDao.insertOrder(order2);

                    OrderItem order2Item = new OrderItem(order2Id, 2L, 1, "400gsm Laminated", "PrintXpress Studio Design", "", 4500.00);
                    orderDao.insertOrderItems(Collections.singletonList(order2Item));

                    orderDao.insertStatusHistory(new OrderStatusHistory(order2Id, "PENDING", oneDayAgo));
                    orderDao.insertStatusHistory(new OrderStatusHistory(order2Id, "PROCESSING", oneDayAgo + 3600000L));
                }

                // 8. Seed SavedDesigns
                if (commonDao.getSavedDesignCountSync() == 0 && userId > 0) {
                    SavedDesign sampleDesign = new SavedDesign(
                            userId,
                            "My Business Card 2025",
                            "/templates/card_design1.pdf",
                            "{\"title\":\"John Doe\",\"role\":\"Developer\",\"phone\":\"0771234567\"}",
                            System.currentTimeMillis()
                    );
                    commonDao.insertSavedDesign(sampleDesign);
                }

                // 9. Seed Notifications
                if (commonDao.getNotificationCountSync() == 0 && userId > 0) {
                    long now = System.currentTimeMillis();
                    commonDao.insertNotification(new Notification(
                            userId,
                            "Welcome to PrintXpress!",
                            "Thank you for joining PrintXpress! Browse our catalog to place your first print order.",
                            "SYSTEM",
                            true,
                            now - 864000000L
                    ));
                    commonDao.insertNotification(new Notification(
                            userId,
                            "Order Delivered",
                            "Your order #1 for Standard Business Cards has been delivered successfully.",
                            "ORDER",
                            false,
                            now - 664000000L
                    ));
                }

                // 10. Seed SupportQueries
                if (supportQueryDao.getQueryCountSync() == 0 && userId > 0) {
                    SupportQuery sampleQuery = new SupportQuery(
                            userId,
                            "Bulk Order Discount Inquiry",
                            "Hello, do you provide special volume discounts for flyer printing over 10,000 units?",
                            System.currentTimeMillis() - 43200000L
                    );
                    supportQueryDao.insert(sampleQuery);
                }

                // 11. Seed Initial Sample Cart Items
                if (cartDao.getCartCountSync() == 0 && userId > 0) {
                    CartItem c1 = new CartItem(userId, 1L, "Standard Business Cards", 15.0, 100, "350gsm Matte Finish | Single Sided", "sample", "", "Acme Corp Executive Card");
                    CartItem c2 = new CartItem(userId, 6L, "Custom Ceramic Mug", 1200.0, 2, "11oz White Ceramic | Dishwasher Safe", "sample", "", "Colombo Studio Print Edition");
                    cartDao.insert(c1);
                    cartDao.insert(c2);
                }

            } catch (Exception e) {
                Log.e(TAG, "Database seeding failed", e);
            }
        });
    }

    private static final Callback sRoomDatabaseCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            seedDatabase();
        }
    };
}
