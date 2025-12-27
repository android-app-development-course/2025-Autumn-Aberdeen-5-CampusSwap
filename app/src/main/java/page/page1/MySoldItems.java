package page.page1;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static page.page1.LoginMainActivity.post_userid;

public class MySoldItems extends AppCompatActivity {

    private static final String TABLENAME = "iteminfo";
    private SQLiteDatabase db;
    private List<Map<String, Object>> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sold_items);

        if (post_userid == null || post_userid.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseHelper database = new DatabaseHelper(this);
        db = database.getWritableDatabase();

        initViews();
        loadSoldItems();
    }

    private void initViews() {
        // 返回按钮
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // 刷新按钮
        Button btnRefresh = findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(v -> recreate());
    }

    private void loadSoldItems() {
        ListView listView = findViewById(R.id.list_sold_items);
        TextView tvEmpty = findViewById(R.id.tv_empty);
        data = new ArrayList<>();

        // 查询当前用户已卖出的商品 (status = 1)
        Cursor cursor = db.query(
                TABLENAME,
                null,
                "userid=? AND status=?",
                new String[]{post_userid, "1"},
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
                    item.put("image", R.drawable.pic_myself);
                }

                data.add(item);
            } while (cursor.moveToNext());

            cursor.close();
        }

        // 显示空状态或列表
        if (data.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

            SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    data,
                    R.layout.item_sold,
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

            // 长按可以重新上架
            listView.setOnItemLongClickListener((parent, view, position, id) -> {
                showRelistDialog(position);
                return true;
            });

            // 点击查看详情
            listView.setOnItemClickListener((parent, view, position, id) -> {
                String itemId = data.get(position).get("id").toString();
                Intent intent = new Intent(MySoldItems.this, item_info.class);
                intent.putExtra("id", itemId);
                startActivity(intent);
            });
        }
    }

    private void showRelistDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("操作")
                .setItems(new String[]{"重新上架", "删除商品"}, (dialog, which) -> {
                    String itemId = data.get(position).get("id").toString();
                    if (which == 0) {
                        // 重新上架
                        relistItem(itemId);
                    } else if (which == 1) {
                        // 删除商品
                        deleteItem(itemId);
                    }
                })
                .show();
    }

    private void relistItem(String itemId) {
        new AlertDialog.Builder(this)
                .setTitle("确认重新上架")
                .setMessage("确定要将此商品重新上架吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    android.content.ContentValues values = new android.content.ContentValues();
                    values.put("status", 0);
                    int result = db.update(TABLENAME, values, "id=?", new String[]{itemId});
                    if (result > 0) {
                        Toast.makeText(this, "已重新上架", Toast.LENGTH_SHORT).show();
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
                .setMessage("确定要永久删除此商品吗？此操作不可恢复。")
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
