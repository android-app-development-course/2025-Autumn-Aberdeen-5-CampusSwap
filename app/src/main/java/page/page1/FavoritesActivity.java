package page.page1;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static page.page1.LoginMainActivity.post_userid;

public class FavoritesActivity extends AppCompatActivity {

    private ListView listView;
    private LinearLayout emptyView;
    private DatabaseHelper dbHelper;
    private List<Map<String, Object>> data;
    private SimpleAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        dbHelper = new DatabaseHelper(this);

        initView();
        loadFavorites();
    }

    private void initView() {
        // 返回按钮
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        listView = findViewById(R.id.favoriteListView);
        emptyView = findViewById(R.id.empty_view);

        // 列表点击跳转详情
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (data.size() > position) {
                    Intent intent = new Intent(FavoritesActivity.this, item_info.class);
                    Object itemId = data.get(position).get("id");
                    intent.putExtra("id", itemId != null ? itemId.toString() : "0");
                    startActivity(intent);
                }
            }
        });
    }

    private void loadFavorites() {
        if (post_userid == null || post_userid.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        data = new ArrayList<>();

        // 查询收藏的商品
        String sql = "SELECT i.id, i.userId, i.title, i.kind, i.info, i.price, i.image, f.time " +
                     "FROM favorites f " +
                     "INNER JOIN iteminfo i ON f.itemId = i.id " +
                     "WHERE f.userId = ? AND i.status = 0 " +
                     "ORDER BY f.time DESC";

        Cursor cursor = db.rawQuery(sql, new String[]{post_userid});

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", cursor.getInt(0));
                item.put("userid", cursor.getString(1));
                item.put("title", cursor.getString(2));
                item.put("kind", cursor.getString(3));
                item.put("info", cursor.getString(4));
                item.put("price", cursor.getString(5));

                byte[] imagedata = cursor.getBlob(6);

                if (imagedata != null && imagedata.length > 0) {
                    try {
                        Bitmap imagebm = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
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

        // 显示列表或空状态
        if (data.isEmpty()) {
            listView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 返回时刷新列表
        loadFavorites();
    }
}
