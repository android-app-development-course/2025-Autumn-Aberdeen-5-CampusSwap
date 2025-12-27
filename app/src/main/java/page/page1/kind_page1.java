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
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class kind_page1 extends AppCompatActivity {
    String TABLENAME = "iteminfo";
    byte[] imagedata;
    Bitmap imagebm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kind_page1);

        // 返回按钮
        ImageView btnBack = (ImageView) findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // 底部导航栏
        RadioButton btn1 = (RadioButton) findViewById(R.id.button_1);
        RadioButton btnMsg = (RadioButton) findViewById(R.id.button_msg);
        RadioButton btn3 = (RadioButton) findViewById(R.id.button_3);
        RadioButton btnMore = (RadioButton) findViewById(R.id.button_more);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(kind_page1.this, main_page.class);
                startActivity(intent);
                finish();
            }
        });

        btnMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "消息功能开发中", Toast.LENGTH_SHORT).show();
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(kind_page1.this, MyselfActivity.class);
                startActivity(intent);
            }
        });

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(kind_page1.this, AboutMainActivity.class);
                startActivity(intent);
            }
        });

        // 商品列表
        DatabaseHelper dbtest = new DatabaseHelper(this);
        final SQLiteDatabase db = dbtest.getWritableDatabase();
        ListView listView = (ListView) findViewById(R.id.kind_list1);

        final List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        // 查询体育用品
        Cursor cursor = db.query(TABLENAME, null, "kind=?", new String[]{"体育用品"}, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("id", cursor.getInt(0));
                item.put("userid", cursor.getString(1));
                item.put("title", cursor.getString(2));
                item.put("kind", cursor.getString(3));
                item.put("info", cursor.getString(4));
                item.put("price", cursor.getString(5));

                // --- 修复开始：安全获取图片 ---
                imagedata = cursor.getBlob(6);
                if (imagedata != null && imagedata.length > 0) {
                    try {
                        imagebm = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
                        item.put("image", imagebm);
                    } catch (Exception e) {
                        item.put("image", null);
                    }
                } else {
                    item.put("image", null);
                }
                // --- 修复结束 ---

                data.add(item);
                cursor.moveToNext();
            }
            cursor.close(); // 记得关闭cursor
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, data, R.layout.listitem,
                new String[]{"image", "title", "kind", "info", "price"},
                new int[]{R.id.item_image, R.id.title, R.id.kind, R.id.info, R.id.price});

        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView) {
                    ImageView iv = (ImageView) view;
                    if (data instanceof Bitmap) {
                        iv.setImageBitmap((Bitmap) data);
                        return true;
                    } else if (data == null) {
                        // 如果没有图片，显示默认图标
                        iv.setImageResource(android.R.drawable.ic_menu_gallery);
                        return true;
                    }
                }
                return false;
            }
        });

        listView.setAdapter(simpleAdapter);

        // 商品点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(kind_page1.this, item_info.class);
                intent.putExtra("id", data.get(position).get("id").toString());
                startActivity(intent);
            }
        });
    }
}