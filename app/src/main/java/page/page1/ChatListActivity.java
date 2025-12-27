package page.page1;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天列表页面 - 显示所有会话
 */
public class ChatListActivity extends AppCompatActivity {

    private ListView lvChatList;
    private LinearLayout layoutEmpty;
    private RadioGroup radioGroup;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private List<Conversation> conversationList;
    private ChatListAdapter adapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // 获取当前登录用户ID
        currentUserId = LoginMainActivity.post_userid;

        initViews();
        initDatabase();
        setupListeners();
        loadConversations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations(); // 每次返回时刷新列表
    }

    private void initViews() {
        lvChatList = findViewById(R.id.lv_chat_list);
        layoutEmpty = findViewById(R.id.layout_empty);
        radioGroup = findViewById(R.id.radioGroup);

        conversationList = new ArrayList<>();
        adapter = new ChatListAdapter(this, conversationList, currentUserId);
        lvChatList.setAdapter(adapter);
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
    }

    private void setupListeners() {
        // 返回按钮
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // 列表点击事件 - 进入聊天页面
        lvChatList.setOnItemClickListener((parent, view, position, id) -> {
            Conversation conversation = conversationList.get(position);
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            // 传递对方用户ID
            String otherUserId = conversation.userId1.equals(currentUserId)
                    ? conversation.userId2 : conversation.userId1;
            intent.putExtra("otherUserId", otherUserId);
            startActivity(intent);
        });

        // 底部导航栏
        RadioButton btnHome = findViewById(R.id.button_1);
        RadioButton btnMsg = findViewById(R.id.button_msg);
        RadioButton btnMy = findViewById(R.id.button_3);
        RadioButton btnMore = findViewById(R.id.button_more);

        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, main_page.class));
            finish();
        });

        btnMy.setOnClickListener(v -> {
            startActivity(new Intent(this, MyselfActivity.class));
            finish();
        });

        btnMore.setOnClickListener(v -> {
            startActivity(new Intent(this, AboutMainActivity.class));
            finish();
        });
    }

    /**
     * 从数据库加载会话列表
     */
    private void loadConversations() {
        conversationList.clear();

        // 查询当前用户的所有会话
        String query = "SELECT * FROM conversations WHERE userId1 = ? OR userId2 = ? ORDER BY lastTime DESC";
        Cursor cursor = db.rawQuery(query, new String[]{currentUserId, currentUserId});

        while (cursor.moveToNext()) {
            Conversation conv = new Conversation();
            conv.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            conv.userId1 = cursor.getString(cursor.getColumnIndexOrThrow("userId1"));
            conv.userId2 = cursor.getString(cursor.getColumnIndexOrThrow("userId2"));
            conv.lastMessage = cursor.getString(cursor.getColumnIndexOrThrow("lastMessage"));
            conv.lastTime = cursor.getString(cursor.getColumnIndexOrThrow("lastTime"));
            conv.unreadCount = cursor.getInt(cursor.getColumnIndexOrThrow("unreadCount"));
            conversationList.add(conv);
        }
        cursor.close();

        // 更新UI
        adapter.notifyDataSetChanged();

        if (conversationList.isEmpty()) {
            lvChatList.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            lvChatList.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * 会话数据类
     */
    public static class Conversation {
        public int id;
        public String userId1;
        public String userId2;
        public String lastMessage;
        public String lastTime;
        public int unreadCount;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}
