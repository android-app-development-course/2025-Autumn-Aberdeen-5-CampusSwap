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
        SQLiteDatabase db = database.getWritableDatabase();

        ListView listView = findViewById(R.id.show_fabu);
        List<Map<String, Object>> data = new ArrayList<>();

        // 只查当前用户
        Cursor cursor = db.query(
                TABLENAME,
                null,
                "userid=?",
                new String[]{post_userid},
                null,
                null,
                null
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

        // 刷新当前页面（正确方式）
        Button btnRefresh = findViewById(R.id.but2);
        btnRefresh.setOnClickListener(v -> {
            recreate();
        });

        // 长按删除
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String delId = data.get(position).get("id").toString();
            int result = db.delete(TABLENAME, "id=?", new String[]{delId});
            if (result > 0) {
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                recreate();
                return true;
            }
            return false;
        });
    }
}
