package page.page1;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
    private static final String dbname="mydb";
    private static final int DB_VERSION = 3; // 升级版本以添加商品状态字段

    public DatabaseHelper(Context context) {
        super(context, dbname, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //账号userId，密码passWord，姓名name，专业subject，电话phone，QQ号qq,地址address
        db.execSQL("create table if not exists users" +
                "(userId varchar(20) primary key," +
                "passWord varchar(20) not null," +
                "name varchar(20)," +
                "subject varchar(20)," +
                "phone varchar(15)," +
                "qq varchar(15)," +
                "address varchar(50))");
        //商品编号id，发布者账号userId，标题title，类别kind，内容info，价格price，图片image，状态status(0在售,1已卖出)
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
        //评论者账号userId，评论商品编号itemId，评论内容comment，评论时间time
        db.execSQL("create table if not exists comments(" +
                "userId varchar(100)," +
                "itemId integer," +
                "comment varchar(1000)," +
                "time DATETIME)");

        //聊天消息表：消息ID，发送者ID，接收者ID，消息内容，发送时间，是否已读
        db.execSQL("create table if not exists messages(" +
                "id integer primary key AUTOINCREMENT," +
                "senderId varchar(100)," +
                "receiverId varchar(100)," +
                "content varchar(2000)," +
                "time DATETIME," +
                "isRead integer default 0)");

        //会话表：用于快速查询聊天列表
        db.execSQL("create table if not exists conversations(" +
                "id integer primary key AUTOINCREMENT," +
                "userId1 varchar(100)," +
                "userId2 varchar(100)," +
                "lastMessage varchar(500)," +
                "lastTime DATETIME," +
                "unreadCount integer default 0)");
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
    }
}

