package page.page1;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static page.page1.LoginMainActivity.post_userid;

public class item_info extends AppCompatActivity {
    String TABLENAME = "iteminfo";
    byte[] imagedata;
    Bitmap imagebm;
    private String contactInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_info);

        final DatabaseHelper dbtest = new DatabaseHelper(this);
        final Intent intent = getIntent();
        final String itemId = intent.getStringExtra("id");
        final SQLiteDatabase db = dbtest.getWritableDatabase();

        // 1. 初始化控件
        ImageView btnBack = (ImageView) findViewById(R.id.btn_back);
        ImageView image = (ImageView) findViewById(R.id.imageView);
        TextView price = (TextView) findViewById(R.id.item_price);
        TextView title = (TextView) findViewById(R.id.item_title);
        TextView info = (TextView) findViewById(R.id.item_info);
        TextView contact = (TextView) findViewById(R.id.contact);
        ListView commentList = (ListView) findViewById(R.id.commentList);
        Button submit = (Button) findViewById(R.id.submit);
        Button btnBargain = (Button) findViewById(R.id.btn_bargain);
        Button btnChat = (Button) findViewById(R.id.btn_chat);

        // 返回按钮
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // 2. 加载商品详情 (修复图片加载崩溃)
        if (itemId != null) {
            Cursor cursor = db.query(TABLENAME, null, "id=?", new String[]{itemId}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                // 获取图片 (列索引6)
                imagedata = cursor.getBlob(6);
                if (imagedata != null && imagedata.length > 0) {
                    try {
                        imagebm = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
                        image.setImageBitmap(imagebm);
                    } catch (Exception e) {
                        e.printStackTrace();
                        image.setImageResource(android.R.drawable.ic_menu_report_image); // 解码失败显示报错图
                    }
                } else {
                    image.setImageResource(android.R.drawable.ic_menu_gallery); // 无图片显示占位图
                }

                title.setText(cursor.getString(2));
                price.setText("¥" + cursor.getString(5));
                info.setText(cursor.getString(4));
                contactInfo = (cursor.getColumnCount() > 8) ? cursor.getString(8) : "暂无联系方式";
                contact.setText(contactInfo);

                cursor.close();
            }
        }

        // 3. 加载评论列表
        final List<Map<String, Object>> data = new ArrayList<>();
        Cursor cursor_ = db.query("comments", null, "itemId=?", new String[]{itemId}, null, null, "time DESC");
        if (cursor_ != null && cursor_.moveToFirst()) {
            while (!cursor_.isAfterLast()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("userId", cursor_.getString(0));
                itemMap.put("comment", cursor_.getString(2));
                itemMap.put("time", cursor_.getString(3));
                data.add(itemMap);
                cursor_.moveToNext();
            }
            cursor_.close();
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, data, R.layout.comment_item,
                new String[]{"userId", "comment", "time"},
                new int[]{R.id.userId, R.id.commentInfo, R.id.time});
        commentList.setAdapter(simpleAdapter);

        // 4. 提交评论逻辑
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post_userid == null || post_userid.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "请先登录！", Toast.LENGTH_SHORT).show();
                    return;
                }

                EditText commentEdit = (EditText) findViewById(R.id.comment);
                String submit_comment = commentEdit.getText().toString().trim();

                if (submit_comment.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "请输入评论内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

                ContentValues values = new ContentValues();
                values.put("userId", post_userid);
                values.put("itemId", itemId);
                values.put("comment", submit_comment);
                values.put("time", time);

                db.insert("comments", null, values);
                Toast.makeText(getApplicationContext(), "评论成功", Toast.LENGTH_SHORT).show();

                // 刷新页面数据
                recreate();
            }
        });

        // 5. 议价与聊天按钮
        btnBargain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "议价功能正在开发中...", Toast.LENGTH_SHORT).show();
            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contactInfo != null && !contactInfo.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "卖家联系方式: " + contactInfo, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "暂无卖家联系方式", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}