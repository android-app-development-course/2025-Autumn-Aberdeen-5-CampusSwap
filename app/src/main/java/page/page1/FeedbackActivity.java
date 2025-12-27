package page.page1;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static page.page1.LoginMainActivity.post_userid;

public class FeedbackActivity extends AppCompatActivity {

    private RadioGroup rgFeedbackType;
    private EditText etContent, etContact;
    private Button btnSubmit;
    private LinearLayout llHistory;
    private TextView tvHistoryTitle;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        dbHelper = new DatabaseHelper(this);
        initView();
        setupListeners();
        loadFeedbackHistory();
    }

    private void initView() {
        // 返回按钮
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rgFeedbackType = findViewById(R.id.rg_feedback_type);
        etContent = findViewById(R.id.et_content);
        etContact = findViewById(R.id.et_contact);
        btnSubmit = findViewById(R.id.btn_submit);
        llHistory = findViewById(R.id.ll_history);
        tvHistoryTitle = findViewById(R.id.tv_history_title);
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        String content = etContent.getText().toString().trim();
        String contact = etContact.getText().toString().trim();

        // 验证内容
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入反馈内容", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.length() < 10) {
            Toast.makeText(this, "反馈内容至少10个字符", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取反馈类型
        String type = getSelectedType();

        // 获取当前时间
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // 保存到数据库
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userId", post_userid != null ? post_userid : "anonymous");
        values.put("type", type);
        values.put("content", content);
        values.put("contact", contact);
        values.put("time", time);
        values.put("status", 0);

        long result = db.insert("feedback", null, values);
        db.close();

        if (result != -1) {
            Toast.makeText(this, "反馈提交成功，感谢您的反馈！", Toast.LENGTH_SHORT).show();
            // 清空输入框
            etContent.setText("");
            etContact.setText("");
            // 刷新历史记录
            loadFeedbackHistory();
        } else {
            Toast.makeText(this, "提交失败，请稍后重试", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSelectedType() {
        int selectedId = rgFeedbackType.getCheckedRadioButtonId();
        if (selectedId == R.id.rb_suggestion) {
            return "功能建议";
        } else if (selectedId == R.id.rb_bug) {
            return "BUG反馈";
        } else if (selectedId == R.id.rb_other) {
            return "其他";
        }
        return "功能建议";
    }

    private void loadFeedbackHistory() {
        if (post_userid == null || post_userid.isEmpty()) {
            tvHistoryTitle.setVisibility(View.GONE);
            llHistory.setVisibility(View.GONE);
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("feedback", null, "userId=?",
                new String[]{post_userid}, null, null, "time DESC", "10");

        llHistory.removeAllViews();

        if (cursor.getCount() > 0) {
            tvHistoryTitle.setVisibility(View.VISIBLE);
            llHistory.setVisibility(View.VISIBLE);

            while (cursor.moveToNext()) {
                View itemView = createHistoryItemView(cursor);
                llHistory.addView(itemView);
            }
        } else {
            tvHistoryTitle.setVisibility(View.GONE);
            llHistory.setVisibility(View.GONE);
        }

        cursor.close();
        db.close();
    }

    private View createHistoryItemView(Cursor cursor) {
        // 创建历史记录项的卡片视图
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(8));
        cardView.setCardElevation(dpToPx(2));
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));

        // 内容布局
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));

        // 类型和状态行
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvType = new TextView(this);
        tvType.setText(cursor.getString(cursor.getColumnIndexOrThrow("type")));
        tvType.setTextSize(14);
        tvType.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        tvType.setPadding(dpToPx(8), dpToPx(2), dpToPx(8), dpToPx(2));
        tvType.setBackgroundResource(R.drawable.tag_background);
        headerLayout.addView(tvType);

        // 状态
        int status = cursor.getInt(cursor.getColumnIndexOrThrow("status"));
        TextView tvStatus = new TextView(this);
        tvStatus.setText(status == 0 ? "待处理" : "已处理");
        tvStatus.setTextSize(12);
        tvStatus.setTextColor(status == 0 ?
                ContextCompat.getColor(this, R.color.text_secondary) :
                ContextCompat.getColor(this, R.color.colorPrimary));
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        statusParams.setMarginStart(dpToPx(8));
        tvStatus.setLayoutParams(statusParams);
        headerLayout.addView(tvStatus);

        contentLayout.addView(headerLayout);

        // 反馈内容
        TextView tvContent = new TextView(this);
        String feedbackContent = cursor.getString(cursor.getColumnIndexOrThrow("content"));
        if (feedbackContent.length() > 50) {
            feedbackContent = feedbackContent.substring(0, 50) + "...";
        }
        tvContent.setText(feedbackContent);
        tvContent.setTextSize(14);
        tvContent.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        contentParams.setMargins(0, dpToPx(8), 0, 0);
        tvContent.setLayoutParams(contentParams);
        contentLayout.addView(tvContent);

        // 时间
        TextView tvTime = new TextView(this);
        tvTime.setText(cursor.getString(cursor.getColumnIndexOrThrow("time")));
        tvTime.setTextSize(12);
        tvTime.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        timeParams.setMargins(0, dpToPx(4), 0, 0);
        tvTime.setLayoutParams(timeParams);
        contentLayout.addView(tvTime);

        cardView.addView(contentLayout);
        return cardView;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
