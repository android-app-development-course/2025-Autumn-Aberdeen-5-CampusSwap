package page.page1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

// 假设你的登录状态保存在这个全局变量中
import static page.page1.LoginMainActivity.post_userid;

public class MyselfActivity extends AppCompatActivity {

    private TextView tvName, tvSchoolInfo, btnEditProfile;
    private CardView cvAvatar;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myself);

        initView();
        setupMenuContent();
        setupListeners();
    }

    private void initView() {
        // 头部
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tvName = findViewById(R.id.tv_name);
        tvSchoolInfo = findViewById(R.id.tv_school_info);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        cvAvatar = findViewById(R.id.cv_avatar);
    }

    /**
     * 初始化菜单行的文字和图标（因为include共用一个布局，必须在后端指定）
     */
    private void setupMenuContent() {
        // 使用之前定义的辅助方法
        setupMenuRow(findViewById(R.id.menu_published), android.R.drawable.ic_menu_send, "我发布的");
        setupMenuRow(findViewById(R.id.menu_sold), android.R.drawable.ic_menu_agenda, "已卖出");
        setupMenuRow(findViewById(R.id.menu_settings), android.R.drawable.ic_menu_manage, "账号设置");
        setupMenuRow(findViewById(R.id.menu_feedback), android.R.drawable.ic_menu_help, "意见反馈");
        setupMenuRow(findViewById(R.id.menu_about), android.R.drawable.ic_menu_info_details, "关于我们");
    }

    private void setupMenuRow(View menuRow, int iconRes, String title) {
        if (menuRow != null) {
            ImageView icon = menuRow.findViewById(R.id.item_icon);
            TextView text = menuRow.findViewById(R.id.item_text);
            if (icon != null) icon.setImageResource(iconRes);
            if (text != null) text.setText(title);
        }
    }

    private void setupListeners() {
        // 1. 登录/头像点击逻辑
        View.OnClickListener loginClick = v -> {
            if (isLogin()) {
                showUserDetailDialog(); // 已登录看详情或换头像
            } else {
                startActivity(new Intent(this, LoginMainActivity.class));
            }
        };
        tvName.setOnClickListener(loginClick);
        cvAvatar.setOnClickListener(loginClick);

        // 2. 编辑资料按钮
        btnEditProfile.setOnClickListener(v -> {
            if (!checkLoginOrGo()) return;
            startActivity(new Intent(this, changepwdActivity.class));
        });

        // 3. 统计项点击 (发布中/已卖出/收藏)
        // 提示：你可以通过 Intent 传参告诉目标 Activity 显示哪个 Tab
        findViewById(R.id.menu_published).setOnClickListener(v -> {
            if (!checkLoginOrGo()) return;
            startActivity(new Intent(this, MyItems.class).putExtra("type", "published"));
        });

        findViewById(R.id.menu_sold).setOnClickListener(v -> {
            if (!checkLoginOrGo()) return;
            startActivity(new Intent(this, MyItems.class).putExtra("type", "sold"));
        });

        // 4. 下方功能列表
        findViewById(R.id.menu_settings).setOnClickListener(v -> {
            if (!checkLoginOrGo()) return;
            startActivity(new Intent(this, changepwdActivity.class));
        });

        findViewById(R.id.menu_feedback).setOnClickListener(v -> {
            Toast.makeText(this, "反馈功能开发中...", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.menu_about).setOnClickListener(v -> {
            startActivity(new Intent(this, AboutMainActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserState();
        loadUserStats(); // 每次返回刷新数字（发布数/收藏数）
    }

    /**
     * 刷新登录状态展示
     */
    private void refreshUserState() {
        currentUser = post_userid;
        if (!isLogin()) {
            tvName.setText("点击登录");
            tvSchoolInfo.setText("登录后体验更多功能");
            btnEditProfile.setText("去登录");
        } else {
            tvName.setText(currentUser);
            tvSchoolInfo.setText("4.9 · 华南师范大学"); // 实际应从数据库读取
            btnEditProfile.setText("编辑资料");
        }
    }

    /**
     * 模拟加载统计数据（实际应通过 OkHttp/Retrofit 从服务器请求）
     */
    private void loadUserStats() {
        // 这里只是示例，实际你应该去数据库 count(*)
        // 然后 findViewById(R.id.xxx).setText(String.valueOf(count));
    }

    private boolean isLogin() {
        return post_userid != null && !post_userid.isEmpty();
    }

    private boolean checkLoginOrGo() {
        if (!isLogin()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginMainActivity.class));
            return false;
        }
        return true;
    }

    private void showUserDetailDialog() {
        new AlertDialog.Builder(this)
                .setTitle("账号管理")
                .setItems(new String[]{"查看大图", "退出登录"}, (dialog, which) -> {
                    if (which == 1) performLogout();
                })
                .show();
    }

    private void performLogout() {
        post_userid = ""; // 清空全局变量
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
        refreshUserState();
    }
}