package page.page1;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 聊天列表适配器
 */
public class ChatListAdapter extends BaseAdapter {

    private Context context;
    private List<ChatListActivity.Conversation> conversationList;
    private String currentUserId;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public ChatListAdapter(Context context, List<ChatListActivity.Conversation> conversationList, String currentUserId) {
        this.context = context;
        this.conversationList = conversationList;
        this.currentUserId = currentUserId;
        this.dbHelper = new DatabaseHelper(context);
        this.db = dbHelper.getReadableDatabase();
    }

    @Override
    public int getCount() {
        return conversationList.size();
    }

    @Override
    public Object getItem(int position) {
        return conversationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_list, parent, false);
            holder = new ViewHolder();
            holder.tvUsername = convertView.findViewById(R.id.tv_username);
            holder.tvLastMessage = convertView.findViewById(R.id.tv_last_message);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            holder.tvUnreadCount = convertView.findViewById(R.id.tv_unread_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ChatListActivity.Conversation conversation = conversationList.get(position);

        // 确定对方用户ID
        String otherUserId = conversation.userId1.equals(currentUserId)
                ? conversation.userId2 : conversation.userId1;

        // 获取对方用户名
        String username = getUserName(otherUserId);
        holder.tvUsername.setText(TextUtils.isEmpty(username) ? otherUserId : username);

        // 显示最后一条消息
        holder.tvLastMessage.setText(conversation.lastMessage);

        // 格式化时间显示
        holder.tvTime.setText(formatTime(conversation.lastTime));

        // 显示未读消息数
        if (conversation.unreadCount > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(conversation.unreadCount > 99 ? "99+" : String.valueOf(conversation.unreadCount));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        return convertView;
    }

    /**
     * 获取用户名
     */
    private String getUserName(String userId) {
        String name = "";
        try {
            Cursor cursor = db.rawQuery("SELECT name FROM users WHERE userId = ?", new String[]{userId});
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 格式化时间显示
     */
    private String formatTime(String timeStr) {
        if (TextUtils.isEmpty(timeStr)) return "";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(timeStr);

            if (date == null) return timeStr;

            Date now = new Date();
            long diff = now.getTime() - date.getTime();

            // 今天：显示时间
            if (diff < 24 * 60 * 60 * 1000 && isSameDay(date, now)) {
                return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
            }
            // 昨天
            else if (diff < 48 * 60 * 60 * 1000) {
                return "昨天";
            }
            // 更早：显示日期
            else {
                return new SimpleDateFormat("MM-dd", Locale.getDefault()).format(date);
            }
        } catch (ParseException e) {
            return timeStr;
        }
    }

    /**
     * 判断是否同一天
     */
    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return fmt.format(date1).equals(fmt.format(date2));
    }

    private static class ViewHolder {
        TextView tvUsername;
        TextView tvLastMessage;
        TextView tvTime;
        TextView tvUnreadCount;
    }
}
