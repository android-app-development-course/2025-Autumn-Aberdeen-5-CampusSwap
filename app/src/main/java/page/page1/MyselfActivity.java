package page.page1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import static page.page1.LoginMainActivity.post_userid;

public class MyselfActivity extends AppCompatActivity {

    private TextView tvName, tvSchoolInfo, btnEditProfile;
    private CardView cvAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myself);

        initView();
        setupMenuContent();
        setupListeners();
        setupBackPressedCallback();
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(MyselfActivity.this, main_page.class));
                finish();
            }
        });
    }

    private void initView() {
        // 返回按钮
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // 头部信息
        tvName = findViewById(R.id.tv_name);
        tvSchoolInfo = findViewById(R.id.tv_school_info);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        cvAvatar = findViewById(R.id.cv_avatar);
    }

    /**
     * 初始化菜单行的文字和图标
     */
    private void setupMenuContent() {
        setupMenuRow(findViewById(R.id.menu_published), android.R.drawable.ic_menu_send, "我发布的");
        setupMenuRow(findViewById(R.id.menu_sold), android.R.drawable.ic_menu_agenda, "已卖出");
        setupMenuRow(findViewById(R.id.menu_favorites), android.R.drawable.btn_star_big_on, "我的收藏");
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
        // 头像/用户名点击
        View.OnClickListener loginClick = v -> {
            if (isLogin()) {
                showUserDetailDialog();
            } else {
                startActivity(new Intent(this, LoginMainActivity.class));
            }
        };
        if (tvName != null) tvName.setOnClickListener(loginClick);
        if (cvAvatar != null) cvAvatar.setOnClickListener(loginClick);

        // 编辑资料按钮
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                if (redirectToLoginIfNeeded()) return;
                startActivity(new Intent(this, userMsgActivity.class));
            });
        }

        // 我发布的
        View menuPublished = findViewById(R.id.menu_published);
        if (menuPublished != null) {
            menuPublished.setOnClickListener(v -> {
                if (redirectToLoginIfNeeded()) return;
                startActivity(new Intent(this, MyItems.class));
            });
        }

        // 已卖出
        View menuSold = findViewById(R.id.menu_sold);
        if (menuSold != null) {
            menuSold.setOnClickListener(v -> {
                if (redirectToLoginIfNeeded()) return;
                startActivity(new Intent(this, MySoldItems.class));
            });
        }

        // 我的收藏
        View menuFavorites = findViewById(R.id.menu_favorites);
        if (menuFavorites != null) {
            menuFavorites.setOnClickListener(v -> {
                if (redirectToLoginIfNeeded()) return;
                startActivity(new Intent(this, FavoritesActivity.class));
            });
        }

        // 账号设置
        View menuSettings = findViewById(R.id.menu_settings);
        if (menuSettings != null) {
            menuSettings.setOnClickListener(v -> {
                if (redirectToLoginIfNeeded()) return;
                startActivity(new Intent(this, changepwdActivity.class));
            });
        }

        // 意见反馈
        View menuFeedback = findViewById(R.id.menu_feedback);
        if (menuFeedback != null) {
            menuFeedback.setOnClickListener(v ->
                    startActivity(new Intent(this, FeedbackActivity.class)));
        }

        // 关于我们
        View menuAbout = findViewById(R.id.menu_about);
        if (menuAbout != null) {
            menuAbout.setOnClickListener(v ->
                    startActivity(new Intent(this, AboutMainActivity.class)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserState();
    }

    /**
     * 刷新登录状态展示
     */
    private void refreshUserState() {
        if (!isLogin()) {
            if (tvName != null) tvName.setText("点击登录");
            if (tvSchoolInfo != null) tvSchoolInfo.setText("登录后体验更多功能");
            if (btnEditProfile != null) btnEditProfile.setText("去登录");
        } else {
            if (tvName != null) tvName.setText(post_userid);
            if (tvSchoolInfo != null) tvSchoolInfo.setText("华南师范大学");
            if (btnEditProfile != null) btnEditProfile.setText("编辑");
        }
    }

    private boolean isLogin() {
        return post_userid != null && !post_userid.isEmpty();
    }

    /**
     * 如果未登录则跳转到登录页面
     * @return true 表示未登录并已跳转，false 表示已登录
     */
    private boolean redirectToLoginIfNeeded() {
        if (!isLogin()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginMainActivity.class));
            return true;
        }
        return false;
    }

    private void showUserDetailDialog() {
        new AlertDialog.Builder(this)
                .setTitle("账号管理")
                .setItems(new String[]{"查看信息", "退出登录"}, (dialog, which) -> {
                    if (which == 0) {
                        startActivity(new Intent(this, userMsgActivity.class));
                    } else if (which == 1) {
                        performLogout();
                    }
                })
                .show();
    }

    private void performLogout() {
        post_userid = "";
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
        refreshUserState();
    }
}
