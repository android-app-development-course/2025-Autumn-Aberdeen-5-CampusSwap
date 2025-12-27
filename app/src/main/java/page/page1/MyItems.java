package page.page1;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static page.page1.LoginMainActivity.post_userid;

public class MyItems extends AppCompatActivity {

    private static final String TABLENAME = "iteminfo";
    private SQLiteDatabase db;
    private List<Map<String, Object>> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (post_userid == null || post_userid.equals("")) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseHelper database = new DatabaseHelper(this);
        db = database.getWritableDatabase();

        ListView listView = findViewById(R.id.show_fabu);
        data = new ArrayList<>();

        // 只查当前用户在售的商品 (status = 0 或 status IS NULL)
        Cursor cursor = db.query(
                TABLENAME,
                null,
                "userid=? AND (status=0 OR status IS NULL)",
                new String[]{post_userid},
                null,
                null,
                "time DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Map<String, Object> item = new HashMap<>();
                item.put("id", cursor.getInt(0));
                item.put("userid", cursor.getString(1));
                item.put("title", cursor.getString(2));
                item.put("kind", cursor.getString(3));
                item.put("info", cursor.getString(4));
                item.put("price", cursor.getString(5));

                byte[] imageData = cursor.getBlob(6);
                if (imageData != null && imageData.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    item.put("image", bitmap);
                } else {
                    item.put("image", R.drawable.pic_myself); // 占位图
                }

                data.add(item);
            } while (cursor.moveToNext());

            cursor.close();
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                data,
                R.layout.activity_my_fabu,
                new String[]{"image", "title", "kind", "info", "price"},
                new int[]{R.id.item_image, R.id.title, R.id.kind, R.id.info, R.id.price}
        );

        adapter.setViewBinder((view, value, text) -> {
            if (view instanceof ImageView) {
                ImageView iv = (ImageView) view;
                if (value instanceof Bitmap) {
                    iv.setImageBitmap((Bitmap) value);
                } else if (value instanceof Integer) {
                    iv.setImageResource((Integer) value);
                }
                return true;
            }
            return false;
        });

        listView.setAdapter(adapter);

        // 返回首页
        Button btnHome = findViewById(R.id.but1);
        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(MyItems.this, main_page.class));
            finish();
        });

        // 刷新当前页面
        Button btnRefresh = findViewById(R.id.but2);
        btnRefresh.setOnClickListener(v -> recreate());

        // 长按弹出操作菜单
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            showItemOptionsDialog(position);
            return true;
        });
    }

    private void showItemOptionsDialog(int position) {
        String itemId = data.get(position).get("id").toString();
        String title = data.get(position).get("title").toString();

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(new String[]{"标记为已卖出", "删除商品"}, (dialog, which) -> {
                    if (which == 0) {
                        markAsSold(itemId);
                    } else if (which == 1) {
                        deleteItem(itemId);
                    }
                })
                .show();
    }

    private void markAsSold(String itemId) {
        new AlertDialog.Builder(this)
                .setTitle("确认标记")
                .setMessage("确定将此商品标记为已卖出吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    ContentValues values = new ContentValues();
                    values.put("status", 1);
                    int result = db.update(TABLENAME, values, "id=?", new String[]{itemId});
                    if (result > 0) {
                        Toast.makeText(this, "已标记为已卖出", Toast.LENGTH_SHORT).show();
                        recreate();
                    } else {
                        Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteItem(String itemId) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除此商品吗？此操作不可恢复。")
                .setPositiveButton("删除", (dialog, which) -> {
                    int result = db.delete(TABLENAME, "id=?", new String[]{itemId});
                    if (result > 0) {
                        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                        recreate();
                    } else {
                        Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
