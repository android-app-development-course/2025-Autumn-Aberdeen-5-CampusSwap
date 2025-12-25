package page.page1;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main_page extends AppCompatActivity implements View.OnClickListener {

    String TABLENAME = "iteminfo";
    Intent intent;
    byte[] imagedata;
    Bitmap imagebm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page); // 确保这里对应的是包含ListView的布局文件名

        DatabaseHelper database = new DatabaseHelper(this);
        // 使用 getReadableDatabase 防止权限问题
        final SQLiteDatabase db = database.getReadableDatabase();
        ListView listView = (ListView) findViewById(R.id.listView);

        final List<Map<String, Object>> data = new ArrayList<>();

        // 查询数据库
        Cursor cursor = db.query(TABLENAME, null, null, null, null, null, null);

        // --- 核心修复区域 开始 ---
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", cursor.getInt(0));
                item.put("userid", cursor.getString(1));
                item.put("title", cursor.getString(2));
                item.put("kind", cursor.getString(3));
                item.put("info", cursor.getString(4));
                item.put("price", cursor.getString(5));

                // 获取图片二进制数据
                imagedata = cursor.getBlob(6);

                // !!!!!!!!!!!! 这里的判断是解决闪退的关键 !!!!!!!!!!!!
                if (imagedata != null && imagedata.length > 0) {
                    try {
                        imagebm = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
                        item.put("image", imagebm);
                    } catch (Exception e) {
                        e.printStackTrace();
                        item.put("image", null); // 解码失败也设为 null
                    }
                } else {
                    item.put("image", null); // 如果数据库里是 null，这里也存 null
                }

                data.add(item);
                cursor.moveToNext();
            }
            cursor.close(); // 记得关闭 cursor
        }
        db.close();
        // --- 核心修复区域 结束 ---

        // 适配器配置
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, data,
                R.layout.listitem, // 确保这里是你刚才新建的卡片式布局文件名
                new String[]{"image", "title", "kind", "info", "price"},
                new int[]{R.id.item_image, R.id.title, R.id.kind, R.id.info, R.id.price});

        // ViewBinder 处理图片显示逻辑
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView) {
                    ImageView iv = (ImageView) view;
                    if (data instanceof Bitmap) {
                        // 如果有图片，显示图片
                        iv.setImageBitmap((Bitmap) data);
                    } else {
                        // 如果数据是 null（没有图片），显示默认图标，防止报错
                        iv.setImageResource(android.R.drawable.ic_menu_gallery);
                        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                    return true;
                }
                return false;
            }
        });

        listView.setAdapter(simpleAdapter);

        // 初始化点击事件
        initClickEvents();

        // 列表点击跳转详情
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 增加安全性判断
                if (data.size() > position) {
                    intent = new Intent(main_page.this, item_info.class);
                    Object itemId = data.get(position).get("id");
                    intent.putExtra("id", itemId != null ? itemId.toString() : "0");
                    startActivity(intent);
                }
            }
        });
    }

    // 将点击事件封装，保持代码整洁
    private void initClickEvents() {
        // 分类标签
        setClick(R.id.tab_all);
        setClick(R.id.tab_sports);
        setClick(R.id.tab_life);
        setClick(R.id.tab_digital);
        setClick(R.id.tab_study);

        // 旧版分类图标 (如果有)
        setClick(R.id.kind1);
        setClick(R.id.kind2);
        setClick(R.id.kind3);
        setClick(R.id.kind4);

        // 功能按钮
        setClick(R.id.fab_add);
        setClick(R.id.button_1);
        setClick(R.id.button_msg);
        setClick(R.id.button_3);
        setClick(R.id.button_more);
    }

    // 辅助方法，防止 findViewById 找不到ID导致空指针
    private void setClick(int id) {
        View v = findViewById(id);
        if (v != null) {
            v.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.tab_sports || id == R.id.kind1) {
            startActivity(new Intent(this, kind_page1.class));
        } else if (id == R.id.tab_life || id == R.id.kind2) {
            startActivity(new Intent(this, kind_page2.class));
        } else if (id == R.id.tab_digital || id == R.id.kind3) {
            startActivity(new Intent(this, kind_page3.class));
        } else if (id == R.id.tab_study || id == R.id.kind4) {
            startActivity(new Intent(this, kind_page4.class));
        } else if (id == R.id.tab_all) {
            recreate(); // 刷新页面
        }
        else if (id == R.id.button_msg) {
            Toast.makeText(this, "消息功能开发中", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.button_3) {
            startActivity(new Intent(this, MyselfActivity.class));
        } else if (id == R.id.button_more) {
            startActivity(new Intent(this, AboutMainActivity.class));
        }
        else if (id == R.id.fab_add) {
            String userId = LoginMainActivity.post_userid;
            if (userId == null || userId.isEmpty()) {
                Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginMainActivity.class));
            } else {
                startActivity(new Intent(this, AddItem.class));
            }
        }
    }

    @Override
    public void onBackPressed() {
        // 返回键不退出应用，而是后台运行
        moveTaskToBack(true);
    }
}