package page.page1;

import android.content.Context;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String dbname = "mydb";
    private static final int DB_VERSION = 5; // 升级版本以添加收藏表
    private static final String PREF_NAME = "db_prefs";
    private static final String KEY_DATA_INITIALIZED = "data_initialized";

    // 预置数据配置
    private static final boolean ENABLE_PRELOAD_DATA = true; // 是否启用预置数据
    private static final int PRELOAD_TYPE = 2; // 1: 仅管理员, 2: 演示数据, 3: 完整数据

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, dbname, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 账号userId，密码passWord，姓名name，专业subject，电话phone，QQ号qq,地址address
        db.execSQL("create table if not exists users" +
                "(userId varchar(20) primary key," +
                "passWord varchar(20) not null," +
                "name varchar(20)," +
                "subject varchar(20)," +
                "phone varchar(15)," +
                "qq varchar(15)," +
                "address varchar(50))");

        // 商品编号id，发布者账号userId，标题title，类别kind，内容info，价格price，图片image，状态status(0在售,1已卖出)
        db.execSQL("create table if not exists iteminfo(" +
                "id integer primary key  AUTOINCREMENT," +
                "userId varchar(100)," +
                "title varchar(200)," +
                "kind varchar(100)," +
                "info varchar(1000)," +
                "price varchar(100)," +
                "image blob," +
                "time DATETIME," +
                "contact varchar(50)," +
                "status integer default 0)");

        // 评论者账号userId，评论商品编号itemId，评论内容comment，评论时间time
        db.execSQL("create table if not exists comments(" +
                "userId varchar(100)," +
                "itemId integer," +
                "comment varchar(1000)," +
                "time DATETIME)");

        // 聊天消息表：消息ID，发送者ID，接收者ID，消息内容，发送时间，是否已读
        db.execSQL("create table if not exists messages(" +
                "id integer primary key AUTOINCREMENT," +
                "senderId varchar(100)," +
                "receiverId varchar(100)," +
                "content varchar(2000)," +
                "time DATETIME," +
                "isRead integer default 0)");

        // 会话表：用于快速查询聊天列表
        db.execSQL("create table if not exists conversations(" +
                "id integer primary key AUTOINCREMENT," +
                "userId1 varchar(100)," +
                "userId2 varchar(100)," +
                "lastMessage varchar(500)," +
                "lastTime DATETIME," +
                "unreadCount integer default 0)");

        // 意见反馈表：反馈ID，用户ID，反馈类型，反馈内容，联系方式，提交时间，处理状态
        db.execSQL("create table if not exists feedback(" +
                "id integer primary key AUTOINCREMENT," +
                "userId varchar(100)," +
                "type varchar(50)," +
                "content varchar(2000)," +
                "contact varchar(100)," +
                "time DATETIME," +
                "status integer default 0)");

        // 收藏表：收藏ID，用户ID，商品ID，收藏时间
        db.execSQL("create table if not exists favorites(" +
                "id integer primary key AUTOINCREMENT," +
                "userId varchar(100)," +
                "itemId integer," +
                "time DATETIME," +
                "UNIQUE(userId, itemId))");

        // 插入预置数据
        if (ENABLE_PRELOAD_DATA) {
            insertPreloadedData(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 数据库升级：添加消息和会话表
        if (oldVersion < 2) {
            db.execSQL("create table if not exists messages(" +
                    "id integer primary key AUTOINCREMENT," +
                    "senderId varchar(100)," +
                    "receiverId varchar(100)," +
                    "content varchar(2000)," +
                    "time DATETIME," +
                    "isRead integer default 0)");

            db.execSQL("create table if not exists conversations(" +
                    "id integer primary key AUTOINCREMENT," +
                    "userId1 varchar(100)," +
                    "userId2 varchar(100)," +
                    "lastMessage varchar(500)," +
                    "lastTime DATETIME," +
                    "unreadCount integer default 0)");
        }
        // 数据库升级：添加商品状态字段
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE iteminfo ADD COLUMN status integer default 0");
        }
        // 数据库升级：添加意见反馈表
        if (oldVersion < 4) {
            db.execSQL("create table if not exists feedback(" +
                    "id integer primary key AUTOINCREMENT," +
                    "userId varchar(100)," +
                    "type varchar(50)," +
                    "content varchar(2000)," +
                    "contact varchar(100)," +
                    "time DATETIME," +
                    "status integer default 0)");
        }
        // 数据库升级：添加收藏表
        if (oldVersion < 5) {
            db.execSQL("create table if not exists favorites(" +
                    "id integer primary key AUTOINCREMENT," +
                    "userId varchar(100)," +
                    "itemId integer," +
                    "time DATETIME," +
                    "UNIQUE(userId, itemId))");
        }
    }

    /**
     * 插入预置数据（只在第一次创建数据库时执行）
     */
    private void insertPreloadedData(SQLiteDatabase db) {
        // 检查是否已经初始化过数据
        if (isDataInitialized()) {
            Log.d("DatabaseHelper", "数据已初始化，跳过预置数据插入");
            return;
        }

        Log.d("DatabaseHelper", "开始插入预置数据...");

        try {
            switch (PRELOAD_TYPE) {
                case 1:
                    insertAdminUser(db); // 仅管理员
                    break;
                case 2:
                    insertAdminUser(db);
                    insertTestUsers(db);
                    insertSampleItems(db);
                    insertSampleComments(db);
                    insertSampleFavorites(db);
                    break;
                case 3:
                    insertAdminUser(db);
                    insertTestUsers(db);
                    insertSampleItems(db);
                    insertSampleComments(db);
                    insertSampleFavorites(db);
                    insertSampleMessages(db);
                    insertSampleConversations(db);
                    insertSampleFeedback(db);
                    break;
            }

            // 标记数据已初始化
            setDataInitialized(true);

            Log.d("DatabaseHelper", "预置数据插入完成");

        } catch (Exception e) {
            Log.e("DatabaseHelper", "插入预置数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 插入管理员账户
     */
    private void insertAdminUser(SQLiteDatabase db) {
        try {
            ContentValues admin = new ContentValues();
            admin.put("userId", "admin");
            admin.put("passWord", "admin123");
            admin.put("name", "系统管理员");
            admin.put("subject", "计算机学院");
            admin.put("phone", "18888888888");
            admin.put("qq", "888888");
            admin.put("address", "行政楼101室");

            long result = db.insert("users", null, admin);
            if (result != -1) {
                Log.d("DatabaseHelper", "管理员账户创建成功: admin/admin123");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "插入管理员失败: " + e.getMessage());
        }
    }

    /**
     * 插入测试用户
     */
    private void insertTestUsers(SQLiteDatabase db) {
        String[][] testUsers = {
                // userId, password, name, subject, phone, qq, address
                {"s2021001", "123456", "张三", "计算机科学", "13800138001", "100001", "紫荆公寓A栋101"},
                {"s2021002", "123456", "李四", "软件工程", "13800138002", "100002", "紫荆公寓B栋201"},
                {"s2021003", "123456", "王五", "电子信息", "13800138003", "100003", "紫荆公寓C栋301"},
                {"s2021004", "123456", "赵六", "人工智能", "13800138004", "100004", "紫荆公寓D栋401"},
                {"s2021005", "123456", "孙七", "数据科学", "13800138005", "100005", "紫荆公寓E栋501"}
        };

        int count = 0;
        for (String[] user : testUsers) {
            try {
                ContentValues values = new ContentValues();
                values.put("userId", user[0]);
                values.put("passWord", user[1]);
                values.put("name", user[2]);
                values.put("subject", user[3]);
                values.put("phone", user[4]);
                values.put("qq", user[5]);
                values.put("address", user[6]);

                db.insert("users", null, values);
                count++;
            } catch (Exception e) {
                Log.e("DatabaseHelper", "插入用户" + user[0] + "失败: " + e.getMessage());
            }
        }
        Log.d("DatabaseHelper", "测试用户插入完成，共" + count + "条");
    }

    /**
     * 插入示例商品
     */
    private void insertSampleItems(SQLiteDatabase db) {
        String[][] sampleItems = {
                // userId, title, kind, info, price, time, contact, status
                {"s2021001", "Java编程思想（第四版）", "学习用品",
                        "经典Java书籍，九成新，无笔记划痕，适合初学者和进阶者。原价78，现价45。",
                        "45.00", "2024-03-20 10:00:00", "13800138001", "0"},

                {"s2021002", "小米蓝牙耳机Air2", "电子产品",
                        "使用半年，音质良好，配件齐全（含充电盒、数据线），续航约4小时。",
                        "120.00", "2024-03-19 14:30:00", "13800138002", "0"},

                {"s2021003", "瑜伽垫（加厚防滑）", "体育用品",
                        "TPE材质，10mm加厚，防滑效果好，使用三个月，保持干净。",
                        "35.00", "2024-03-18 09:15:00", "13800138003", "0"},

                {"s2021004", "护眼LED台灯", "生活用品",
                        "三档调光，USB充电，可折叠设计，适合宿舍使用，亮度可调节。",
                        "55.00", "2024-03-16 11:20:00", "13800138004", "0"},

                {"s2021001", "考研数学复习全书", "学习用品",
                        "李永乐复习全书，包含详细解析和真题，有少量笔记，不影响使用。",
                        "30.00", "2024-03-17 16:45:00", "13800138001", "1"}, // 已卖出

                {"s2021005", "斯伯丁篮球", "体育用品",
                        "7号标准球，打了几次，手感良好，适合日常锻炼和比赛。",
                        "60.00", "2024-03-15 13:30:00", "13800138005", "0"},

                {"s2021002", "罗技无线鼠标", "电子产品",
                        "M220静音鼠标，使用一年，性能稳定，电池耐用。",
                        "40.00", "2024-03-14 15:40:00", "13800138002", "0"},

                {"s2021003", "膳魔师保温杯", "生活用品",
                        "500ml容量，保温效果好，使用半年，无磕碰。",
                        "50.00", "2024-03-13 08:50:00", "13800138003", "0"},

                {"s2021004", "C++ Primer中文版", "学习用品",
                        "C++经典教材，第五版，有少量笔记，适合C++学习者。",
                        "40.00", "2024-03-12 10:25:00", "13800138004", "0"},

                {"s2021005", "羽毛球拍套装", "体育用品",
                        "包含两只拍子+一筒羽毛球，适合初学者，使用次数少。",
                        "80.00", "2024-03-11 14:10:00", "13800138005", "0"}
        };

        int count = 0;
        for (String[] item : sampleItems) {
            try {
                ContentValues values = new ContentValues();
                values.put("userId", item[0]);
                values.put("title", item[1]);
                values.put("kind", item[2]);
                values.put("info", item[3]);
                values.put("price", item[4]);
                values.put("time", item[5]);
                values.put("contact", item[6]);
                values.put("status", Integer.parseInt(item[7]));

                db.insert("iteminfo", null, values);
                count++;
            } catch (Exception e) {
                Log.e("DatabaseHelper", "插入商品" + item[1] + "失败: " + e.getMessage());
            }
        }
        Log.d("DatabaseHelper", "示例商品插入完成，共" + count + "条");
    }

    /**
     * 插入示例评论
     */
    private void insertSampleComments(SQLiteDatabase db) {
        String[][] sampleComments = {
                // userId, itemId, comment, time
                {"s2021002", "1", "书有几成新？有没有破损？可以便宜点吗？", "2024-03-20 11:00:00"},
                {"s2021001", "1", "九成新，没有任何破损，已经是底价了。", "2024-03-20 11:05:00"},
                {"s2021003", "2", "可以试听一下吗？电池续航还有多久？", "2024-03-19 15:30:00"},
                {"s2021002", "2", "可以试听，续航还有4小时左右。", "2024-03-19 15:35:00"},
                {"s2021005", "3", "瑜伽垫是什么材质的？厚度多少？", "2024-03-18 10:20:00"},
                {"s2021003", "3", "TPE材质，10mm加厚，防滑效果很好。", "2024-03-18 10:25:00"},
                {"s2021004", "4", "台灯可以折叠吗？有几个亮度档位？", "2024-03-16 12:30:00"},
                {"s2021001", "6", "篮球可以便宜点吗？气足不足？", "2024-03-15 14:30:00"},
                {"s2021005", "6", "气很足，可以小刀。", "2024-03-15 14:35:00"}
        };

        int count = 0;
        for (String[] comment : sampleComments) {
            try {
                ContentValues values = new ContentValues();
                values.put("userId", comment[0]);
                values.put("itemId", Integer.parseInt(comment[1]));
                values.put("comment", comment[2]);
                values.put("time", comment[3]);

                db.insert("comments", null, values);
                count++;
            } catch (Exception e) {
                Log.e("DatabaseHelper", "插入评论失败: " + e.getMessage());
            }
        }
        Log.d("DatabaseHelper", "示例评论插入完成，共" + count + "条");
    }

    /**
     * 插入示例收藏
     */
    private void insertSampleFavorites(SQLiteDatabase db) {
        String[][] sampleFavorites = {
                // userId, itemId, time
                {"s2021002", "1", "2024-03-20 11:10:00"},
                {"s2021003", "2", "2024-03-19 16:00:00"},
                {"s2021001", "3", "2024-03-18 09:30:00"},
                {"s2021005", "4", "2024-03-16 13:00:00"},
                {"s2021002", "3", "2024-03-18 10:15:00"},
                {"s2021004", "6", "2024-03-15 15:00:00"},
                {"s2021003", "7", "2024-03-14 16:20:00"}
        };

        int count = 0;
        for (String[] favorite : sampleFavorites) {
            try {
                ContentValues values = new ContentValues();
                values.put("userId", favorite[0]);
                values.put("itemId", Integer.parseInt(favorite[1]));
                values.put("time", favorite[2]);

                db.insert("favorites", null, values);
                count++;
            } catch (Exception e) {
                Log.e("DatabaseHelper", "插入收藏失败: " + e.getMessage());
            }
        }
        Log.d("DatabaseHelper", "示例收藏插入完成，共" + count + "条");
    }

    /**
     * 插入示例消息（可选）
     */
    private void insertSampleMessages(SQLiteDatabase db) {
        String[][] sampleMessages = {
                // senderId, receiverId, content, time, isRead
                {"s2021002", "s2021001", "你好，请问那本Java书还在吗？", "2024-03-20 10:35:00", "1"},
                {"s2021001", "s2021002", "还在的，需要的话可以今天下午来取", "2024-03-20 10:36:00", "1"},
                {"s2021002", "s2021001", "好的，我下午3点过来，紫荆公寓A栋对吗？", "2024-03-20 10:37:00", "1"},
                {"s2021003", "s2021002", "耳机可以便宜点吗？", "2024-03-19 14:25:00", "0"},
                {"s2021004", "s2021003", "瑜伽垫还在吗？", "2024-03-18 09:20:00", "1"}
        };

        int count = 0;
        for (String[] msg : sampleMessages) {
            try {
                ContentValues values = new ContentValues();
                values.put("senderId", msg[0]);
                values.put("receiverId", msg[1]);
                values.put("content", msg[2]);
                values.put("time", msg[3]);
                values.put("isRead", Integer.parseInt(msg[4]));

                db.insert("messages", null, values);
                count++;
            } catch (Exception e) {
                Log.e("DatabaseHelper", "插入消息失败: " + e.getMessage());
            }
        }
        Log.d("DatabaseHelper", "示例消息插入完成，共" + count + "条");
    }

    /**
     * 插入示例会话（可选）
     */
    private void insertSampleConversations(SQLiteDatabase db) {
        String[][] sampleConversations = {
                // userId1, userId2, lastMessage, lastTime, unreadCount
                {"s2021001", "s2021002", "好的，我下午3点过来，紫荆公寓A栋对吗？", "2024-03-20 10:37:00", "0"},
                {"s2021002", "s2021003", "耳机可以便宜点吗？", "2024-03-19 14:25:00", "1"},
                {"s2021003", "s2021004", "瑜伽垫还在吗？", "2024-03-18 09:20:00", "0"},
                {"s2021001", "s2021005", "篮球最低多少钱？", "2024-03-15 14:00:00", "1"}
        };

        int count = 0;
        for (String[] conv : sampleConversations) {
            try {
                ContentValues values = new ContentValues();
                values.put("userId1", conv[0]);
                values.put("userId2", conv[1]);
                values.put("lastMessage", conv[2]);
                values.put("lastTime", conv[3]);
                values.put("unreadCount", Integer.parseInt(conv[4]));

                db.insert("conversations", null, values);
                count++;
            } catch (Exception e) {
                Log.e("DatabaseHelper", "插入会话失败: " + e.getMessage());
            }
        }
        Log.d("DatabaseHelper", "示例会话插入完成，共" + count + "条");
    }

    /**
     * 插入示例反馈（可选）
     */
    private void insertSampleFeedback(SQLiteDatabase db) {
        String[][] sampleFeedback = {
                // userId, type, content, contact, time, status
                {"s2021001", "功能建议", "希望能增加搜索过滤功能，比如按价格排序和按发布时间排序", "13800138001", "2024-03-20 09:00:00", "1"},
                {"s2021002", "BUG反馈", "商品图片上传后显示不清晰，希望能优化图片压缩算法", "13800138002", "2024-03-19 10:30:00", "1"},
                {"s2021003", "其他", "建议增加求购功能，用户可以在平台上发布求购信息", "13800138003", "2024-03-18 14:20:00", "0"}
        };

        int count = 0;
        for (String[] feedback : sampleFeedback) {
            try {
                ContentValues values = new ContentValues();
                values.put("userId", feedback[0]);
                values.put("type", feedback[1]);
                values.put("content", feedback[2]);
                values.put("contact", feedback[3]);
                values.put("time", feedback[4]);
                values.put("status", Integer.parseInt(feedback[5]));

                db.insert("feedback", null, values);
                count++;
            } catch (Exception e) {
                Log.e("DatabaseHelper", "插入反馈失败: " + e.getMessage());
            }
        }
        Log.d("DatabaseHelper", "示例反馈插入完成，共" + count + "条");
    }

    /**
     * 检查数据是否已初始化
     */
    private boolean isDataInitialized() {
        try {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .getBoolean(KEY_DATA_INITIALIZED, false);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "检查初始化状态失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 标记数据已初始化
     */
    private void setDataInitialized(boolean initialized) {
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit();
            editor.putBoolean(KEY_DATA_INITIALIZED, initialized);
            editor.apply();
            Log.d("DatabaseHelper", "数据初始化标记已设置为: " + initialized);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "设置初始化标记失败: " + e.getMessage());
        }
    }

    /**
     * 重置数据初始化标记（用于开发调试）
     * 在其他Activity中调用：((DatabaseHelper)dbHelper).resetDataInitialization();
     */
    public void resetDataInitialization() {
        setDataInitialized(false);
        Log.d("DatabaseHelper", "数据初始化标记已重置，下次启动将重新插入预置数据");
    }

    /**
     * 获取数据库版本信息（可选）
     */
    public String getDatabaseInfo() {
        return "数据库版本: " + DB_VERSION + ", 预置数据: " +
                (ENABLE_PRELOAD_DATA ? "启用(" + PRELOAD_TYPE + ")" : "禁用");
    }
}

