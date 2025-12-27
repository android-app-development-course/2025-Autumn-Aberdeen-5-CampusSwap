package page.page1;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 聊天会话页面 - 与特定用户的对话
 */
public class ChatActivity extends AppCompatActivity {

    private ListView lvMessages;
    private EditText etMessage;
    private ImageView btnSend;
    private TextView tvChatTitle;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private List<Message> messageList;
    private MessageAdapter adapter;
    private String currentUserId;  // 当前登录用户
    private String otherUserId;    // 聊天对方用户
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL = 3000; // 3秒刷新一次

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 获取当前登录用户和聊天对方ID
        currentUserId = LoginMainActivity.post_userid;
        otherUserId = getIntent().getStringExtra("otherUserId");

        if (TextUtils.isEmpty(otherUserId)) {
            Toast.makeText(this, "聊天对象无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initDatabase();
        setupListeners();
        loadUserInfo();
        loadMessages();
        markMessagesAsRead();
        startAutoRefresh();
    }

    private void initViews() {
        lvMessages = findViewById(R.id.lv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        tvChatTitle = findViewById(R.id.tv_chat_title);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList, currentUserId);
        lvMessages.setAdapter(adapter);
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
    }

    private void setupListeners() {
        // 返回按钮
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // 发送按钮
        btnSend.setOnClickListener(v -> sendMessage());

        // 用户信息按钮
        ImageView btnInfo = findViewById(R.id.btn_info);
        btnInfo.setOnClickListener(v -> {
            // 可以扩展：查看对方用户详细信息
            Toast.makeText(this, "用户ID: " + otherUserId, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 加载对方用户信息
     */
    private void loadUserInfo() {
        String query = "SELECT name FROM users WHERE userId = ?";
        Cursor cursor = db.rawQuery(query, new String[]{otherUserId});
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            tvChatTitle.setText(TextUtils.isEmpty(name) ? otherUserId : name);
        } else {
            tvChatTitle.setText(otherUserId);
        }
        cursor.close();
    }

    /**
     * 加载聊天消息
     */
    private void loadMessages() {
        messageList.clear();

        // 查询两个用户之间的所有消息
        String query = "SELECT * FROM messages WHERE " +
                "(senderId = ? AND receiverId = ?) OR (senderId = ? AND receiverId = ?) " +
                "ORDER BY time ASC";
        Cursor cursor = db.rawQuery(query, new String[]{
                currentUserId, otherUserId, otherUserId, currentUserId
        });

        while (cursor.moveToNext()) {
            Message msg = new Message();
            msg.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            msg.senderId = cursor.getString(cursor.getColumnIndexOrThrow("senderId"));
            msg.receiverId = cursor.getString(cursor.getColumnIndexOrThrow("receiverId"));
            msg.content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            msg.time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            msg.isRead = cursor.getInt(cursor.getColumnIndexOrThrow("isRead")) == 1;
            messageList.add(msg);
        }
        cursor.close();

        adapter.notifyDataSetChanged();

        // 滚动到底部
        if (!messageList.isEmpty()) {
            lvMessages.setSelection(messageList.size() - 1);
        }
    }

    /**
     * 发送消息
     */
    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "请输入消息内容", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取当前时间
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        // 插入消息到数据库
        ContentValues values = new ContentValues();
        values.put("senderId", currentUserId);
        values.put("receiverId", otherUserId);
        values.put("content", content);
        values.put("time", currentTime);
        values.put("isRead", 0);

        long result = db.insert("messages", null, values);

        if (result != -1) {
            // 更新或创建会话
            updateConversation(content, currentTime);

            // 清空输入框
            etMessage.setText("");

            // 刷新消息列表
            loadMessages();
        } else {
            Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 更新会话记录
     */
    private void updateConversation(String lastMessage, String lastTime) {
        // 检查会话是否存在
        String query = "SELECT id FROM conversations WHERE " +
                "(userId1 = ? AND userId2 = ?) OR (userId1 = ? AND userId2 = ?)";
        Cursor cursor = db.rawQuery(query, new String[]{
                currentUserId, otherUserId, otherUserId, currentUserId
        });

        if (cursor.moveToFirst()) {
            // 更新现有会话
            int convId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            ContentValues values = new ContentValues();
            values.put("lastMessage", lastMessage);
            values.put("lastTime", lastTime);
            db.update("conversations", values, "id = ?", new String[]{String.valueOf(convId)});
        } else {
            // 创建新会话
            ContentValues values = new ContentValues();
            values.put("userId1", currentUserId);
            values.put("userId2", otherUserId);
            values.put("lastMessage", lastMessage);
            values.put("lastTime", lastTime);
            values.put("unreadCount", 0);
            db.insert("conversations", null, values);
        }
        cursor.close();
    }

    /**
     * 将对方发送的消息标记为已读
     */
    private void markMessagesAsRead() {
        ContentValues values = new ContentValues();
        values.put("isRead", 1);
        db.update("messages", values,
                "senderId = ? AND receiverId = ? AND isRead = 0",
                new String[]{otherUserId, currentUserId});

        // 重置会话未读数
        String query = "SELECT id FROM conversations WHERE " +
                "(userId1 = ? AND userId2 = ?) OR (userId1 = ? AND userId2 = ?)";
        Cursor cursor = db.rawQuery(query, new String[]{
                currentUserId, otherUserId, otherUserId, currentUserId
        });
        if (cursor.moveToFirst()) {
            int convId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            ContentValues convValues = new ContentValues();
            convValues.put("unreadCount", 0);
            db.update("conversations", convValues, "id = ?", new String[]{String.valueOf(convId)});
        }
        cursor.close();
    }

    /**
     * 开始自动刷新消息
     */
    private void startAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadMessages();
                markMessagesAsRead();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }

    /**
     * 停止自动刷新
     */
    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    /**
     * 消息数据类
     */
    public static class Message {
        public int id;
        public String senderId;
        public String receiverId;
        public String content;
        public String time;
        public boolean isRead;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMessages();
        markMessagesAsRead();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRefresh();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}
