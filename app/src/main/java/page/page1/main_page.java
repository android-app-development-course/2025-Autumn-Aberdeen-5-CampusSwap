package page.page1;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.OnBackPressedCallback;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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
    ListView listView;
    List<Map<String, Object>> data;
    SimpleAdapter simpleAdapter;
    String searchKeyword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        listView = findViewById(R.id.listView);

        // 加载数据
        loadItemData();

        // 初始化点击事件
        initClickEvents();

        // 设置返回键处理
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 返回键不退出应用，而是后台运行
                moveTaskToBack(true);
            }
        });

        // 列表点击跳转详情
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (data.size() > position) {
                    intent = new Intent(main_page.this, item_info.class);
                    Object itemId = data.get(position).get("id");
                    intent.putExtra("id", itemId != null ? itemId.toString() : "0");
                    startActivity(intent);
                }
            }
        });
    }

    // 加载商品数据
    private void loadItemData() {
        DatabaseHelper database = new DatabaseHelper(this);
        SQLiteDatabase db = database.getReadableDatabase();

        data = new ArrayList<>();

        // 构建查询条件
        String selection = "status = ?";
        String[] selectionArgs = new String[]{"0"};

        if (!TextUtils.isEmpty(searchKeyword)) {
            selection = "status = ? AND (title LIKE ? OR info LIKE ?)";
            selectionArgs = new String[]{"0", "%" + searchKeyword + "%", "%" + searchKeyword + "%"};
        }

        Cursor cursor = db.query(TABLENAME, null, selection, selectionArgs, null, null, "time DESC");

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", cursor.getInt(0));
                item.put("userid", cursor.getString(1));
                item.put("title", cursor.getString(2));
                item.put("kind", cursor.getString(3));
                item.put("info", cursor.getString(4));
                item.put("price", cursor.getString(5));

                imagedata = cursor.getBlob(6);

                if (imagedata != null && imagedata.length > 0) {
                    try {
                        imagebm = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
                        item.put("image", imagebm);
                    } catch (Exception e) {
                        e.printStackTrace();
                        item.put("image", null);
                    }
                } else {
                    item.put("image", null);
                }

                data.add(item);
                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();

        // 设置适配器
        simpleAdapter = new SimpleAdapter(this, data,
                R.layout.listitem,
                new String[]{"image", "title", "kind", "info", "price"},
                new int[]{R.id.item_image, R.id.title, R.id.kind, R.id.info, R.id.price});

        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView) {
                    ImageView iv = (ImageView) view;
                    if (data instanceof Bitmap) {
                        iv.setImageBitmap((Bitmap) data);
                    } else {
                        iv.setImageResource(android.R.drawable.ic_menu_gallery);
                        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                    return true;
                }
                return false;
            }
        });

        listView.setAdapter(simpleAdapter);
    }

    // 将点击事件封装，保持代码整洁
    private void initClickEvents() {
        // 搜索图标
        setClick(R.id.iv_search);

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

        if (id == R.id.iv_search) {
            showSearchDialog();
        } else if (id == R.id.tab_sports || id == R.id.kind1) {
            startActivity(new Intent(this, kind_page1.class));
        } else if (id == R.id.tab_life || id == R.id.kind2) {
            startActivity(new Intent(this, kind_page2.class));
        } else if (id == R.id.tab_digital || id == R.id.kind3) {
            startActivity(new Intent(this, kind_page3.class));
        } else if (id == R.id.tab_study || id == R.id.kind4) {
            startActivity(new Intent(this, kind_page4.class));
        } else if (id == R.id.tab_all) {
            searchKeyword = null; // 清除搜索关键词
            loadItemData(); // 重新加载所有数据
        }
        else if (id == R.id.button_msg) {
            // 跳转到聊天列表页面
            String userId = LoginMainActivity.post_userid;
            if (userId == null || userId.isEmpty()) {
                Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginMainActivity.class));
            } else {
                startActivity(new Intent(this, ChatListActivity.class));
            }
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

    // 显示搜索对话框
    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("搜索商品");

        final EditText input = new EditText(this);
        input.setHint("请输入商品名称或描述");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("搜索", (dialog, which) -> {
            searchKeyword = input.getText().toString().trim();
            if (TextUtils.isEmpty(searchKeyword)) {
                Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
                searchKeyword = null;
            } else {
                loadItemData();
                if (data.isEmpty()) {
                    Toast.makeText(this, "没有找到相关商品", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "找到 " + data.size() + " 件商品", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        builder.setNeutralButton("清除搜索", (dialog, which) -> {
            searchKeyword = null;
            loadItemData();
            Toast.makeText(this, "已显示全部商品", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }
}