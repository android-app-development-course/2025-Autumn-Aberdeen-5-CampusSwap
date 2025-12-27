package page.page1;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MyselfActivity extends AppCompatActivity {

    private RadioButton button1;
    private RadioButton buttonMsg;
    private RadioButton button3;
    private RadioButton buttonMore;
    private Button myself;
    private Button myshow;
    private Button changepwd;
    private Button about;
    private Button login;
    private TextView myId;
    protected Intent intent;
    private String a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myself);

        // 返回按钮
        ImageView btnBack = (ImageView) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 底部导航栏
        button1 = (RadioButton) findViewById(R.id.button_1);
        buttonMsg = (RadioButton) findViewById(R.id.button_msg);
        button3 = (RadioButton) findViewById(R.id.button_3);
        buttonMore = (RadioButton) findViewById(R.id.button_more);

        // 功能按钮
        myself = (Button) findViewById(R.id.myself);
        myshow = (Button) findViewById(R.id.myShow);
        changepwd = (Button) findViewById(R.id.changepwd);
        about = (Button) findViewById(R.id.about);
        login = (Button) findViewById(R.id.login);
        myId = (TextView) findViewById(R.id.myId);

        // 新布局的点击区域
        LinearLayout layoutMyPublish = (LinearLayout) findViewById(R.id.layout_my_publish);
        LinearLayout layoutSettings = (LinearLayout) findViewById(R.id.layout_settings);
        LinearLayout layoutAbout = (LinearLayout) findViewById(R.id.layout_about);

        a = LoginMainActivity.post_userid;
        if (a == null || a.equals("")) {
            myId.setText("未登录");
            login.setText("登录");
        } else {
            myId.setText(a);
            login.setText("退出登录");
        }

        Log.i("123", a != null ? a : "null");

        // 跳转到主页
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MyselfActivity.this, main_page.class);
                startActivity(intent);
                finish();
            }
        });

        // 消息页面
        buttonMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "消息功能开发中", Toast.LENGTH_SHORT).show();
            }
        });

        // 我的页面（当前页面）
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 已在当前页面
            }
        });

        // 更多页面
        buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MyselfActivity.this, AboutMainActivity.class);
                startActivity(intent);
            }
        });

        // 跳转到个人信息页面 (编辑按钮)
        myself.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (a == null || a.equals("")) {
                    Toast.makeText(getApplicationContext(), "请先登录！", Toast.LENGTH_SHORT).show();
                    intent = new Intent(MyselfActivity.this, LoginMainActivity.class);
                    startActivity(intent);
                    return;
                }
                Log.i("123", "111111111");
                intent = new Intent(MyselfActivity.this, userMsgActivity.class);
                startActivity(intent);
            }
        });

        // 我发布的 (新布局点击区域)
        if (layoutMyPublish != null) {
            layoutMyPublish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (a == null || a.equals("")) {
                        Toast.makeText(getApplicationContext(), "请先登录！", Toast.LENGTH_SHORT).show();
                        intent = new Intent(MyselfActivity.this, LoginMainActivity.class);
                        startActivity(intent);
                    } else {
                        intent = new Intent(MyselfActivity.this, MyItems.class);
                        startActivity(intent);
                    }
                }
            });
        }

        // 跳转到个人发布页面 (兼容旧按钮)
        myshow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (a == null || a.equals("")) {
                    Toast.makeText(getApplicationContext(), "请先登录！", Toast.LENGTH_SHORT).show();
                    intent = new Intent(MyselfActivity.this, LoginMainActivity.class);
                    startActivity(intent);
                } else {
                    intent = new Intent(MyselfActivity.this, MyItems.class);
                    startActivity(intent);
                }
            }
        });

        // 账号设置 (新布局点击区域)
        if (layoutSettings != null) {
            layoutSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (a == null || a.equals("")) {
                        Toast.makeText(getApplicationContext(), "请先登录！", Toast.LENGTH_SHORT).show();
                        intent = new Intent(MyselfActivity.this, LoginMainActivity.class);
                        startActivity(intent);
                    } else {
                        intent = new Intent(MyselfActivity.this, changepwdActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }

        // 跳转到修改密码页面 (兼容旧按钮)
        changepwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (a == null || a.equals("")) {
                    Toast.makeText(getApplicationContext(), "请先登录！", Toast.LENGTH_SHORT).show();
                    intent = new Intent(MyselfActivity.this, LoginMainActivity.class);
                    startActivity(intent);
                }
                intent = new Intent(MyselfActivity.this, changepwdActivity.class);
                startActivity(intent);
            }
        });

        // 关于校园圈 (新布局点击区域)
        if (layoutAbout != null) {
            layoutAbout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent = new Intent(MyselfActivity.this, AboutMainActivity.class);
                    startActivity(intent);
                }
            });
        }

        // 跳转到关于页面 (兼容旧按钮)
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MyselfActivity.this, AboutMainActivity.class);
                startActivity(intent);
            }
        });

        // 登录/退出登录按钮
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (a == null || a.equals("")) {
                    intent = new Intent(MyselfActivity.this, LoginMainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "退出成功", Toast.LENGTH_SHORT).show();
                    LoginMainActivity.post_userid = "";
                    intent = new Intent(MyselfActivity.this, LoginMainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 返回首页
        intent = new Intent(MyselfActivity.this, main_page.class);
        startActivity(intent);
        finish();
    }
}
