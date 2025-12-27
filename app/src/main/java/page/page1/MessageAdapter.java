package page.page1;

import android.content.Context;
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
 * 消息列表适配器 - 用于聊天会话页面
 */
public class MessageAdapter extends BaseAdapter {

    private static final int TYPE_SENT = 0;     // 发送的消息
    private static final int TYPE_RECEIVED = 1; // 接收的消息

    private Context context;
    private List<ChatActivity.Message> messageList;
    private String currentUserId;

    public MessageAdapter(Context context, List<ChatActivity.Message> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2; // 两种消息类型
    }

    @Override
    public int getItemViewType(int position) {
        ChatActivity.Message message = messageList.get(position);
        return message.senderId.equals(currentUserId) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatActivity.Message message = messageList.get(position);
        int type = getItemViewType(position);
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            if (type == TYPE_SENT) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            }
            holder.tvMessage = convertView.findViewById(R.id.tv_message);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 设置消息内容
        holder.tvMessage.setText(message.content);

        // 显示时间（每隔5分钟显示一次）
        boolean showTime = shouldShowTime(position);
        if (showTime) {
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvTime.setText(formatTime(message.time));
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }

        return convertView;
    }

    /**
     * 判断是否显示时间
     * 规则：第一条消息显示时间，之后每隔5分钟显示一次
     */
    private boolean shouldShowTime(int position) {
        if (position == 0) return true;

        ChatActivity.Message current = messageList.get(position);
        ChatActivity.Message previous = messageList.get(position - 1);

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date currentDate = format.parse(current.time);
            Date previousDate = format.parse(previous.time);

            if (currentDate != null && previousDate != null) {
                long diff = currentDate.getTime() - previousDate.getTime();
                return diff > 5 * 60 * 1000; // 5分钟
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
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

            // 今天：显示时间
            if (isSameDay(date, now)) {
                return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
            }
            // 昨天
            else if (isYesterday(date, now)) {
                return "昨天 " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
            }
            // 更早：显示完整日期时间
            else {
                return new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(date);
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

    /**
     * 判断是否昨天
     */
    private boolean isYesterday(Date date, Date now) {
        long diff = now.getTime() - date.getTime();
        return diff > 0 && diff < 48 * 60 * 60 * 1000 && !isSameDay(date, now);
    }

    private static class ViewHolder {
        TextView tvMessage;
        TextView tvTime;
    }
}
