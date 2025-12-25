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
        final SQLiteDatabase db = dbtest.getWritableDatabase();

        // 返回按钮
        ImageView btnBack = (ImageView) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 关闭当前页面，返回上一页
            }
        });

        ImageView image = (ImageView)findViewById(R.id.imageView);
        TextView price = (TextView)findViewById(R.id.item_price);
        TextView title = (TextView)findViewById(R.id.item_title);
        TextView info = (TextView)findViewById(R.id.item_info);
        TextView contact = (TextView)findViewById(R.id.contact);

        Cursor cursor = db.query(TABLENAME,null,"id=?",new String[]{intent.getStringExtra("id")},null,null,null,null);
        Log.i("商品的id是",intent.getStringExtra("id"));
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                imagedata = cursor.getBlob(6);
                imagebm = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
                image.setImageBitmap(imagebm);
                title.setText(cursor.getString(2));
                // 价格显示加上人民币符号
                price.setText("¥" + cursor.getString(5));
                info.setText(cursor.getString(4));
                contactInfo = cursor.getString(8);
                contact.setText(contactInfo);
                cursor.moveToNext();
            }
        }

        // 评论列表
        ListView commentList = (ListView)findViewById(R.id.commentList);
        Map<String, Object> item;
        final List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        Cursor cursor_ = db.query("comments",null,"itemId=?",new String[]{intent.getStringExtra("id")},null,null,null,null);
        if (cursor_.moveToFirst()){
            while (!cursor_.isAfterLast()){
                item = new HashMap<String, Object>();
                item.put("userId",cursor_.getString(0));
                item.put("comment",cursor_.getString(2));
                item.put("time",cursor_.getString(3));
                cursor_.moveToNext();
                data.add(item);
            }
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, data, R.layout.comment_item, new String[] { "userId", "comment", "time"},
                new int[] { R.id.userId, R.id.commentInfo, R.id.time });
        commentList.setAdapter(simpleAdapter);

        // 提交评论按钮
        Button submit = (Button)findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post_userid == null || post_userid.equals("")) {
                    Toast.makeText(getApplicationContext(), "请先登录！", Toast.LENGTH_SHORT).show();
                    return;
                }
                EditText comment = (EditText)findViewById(R.id.comment);
                String submit_comment = comment.getText().toString();
                if (submit_comment.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "请输入评论内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
                Date curDate = new Date(System.currentTimeMillis());
                String time = formatter.format(curDate);
                ContentValues values=new ContentValues();
                values.put("userId",post_userid);
                values.put("itemId",intent.getStringExtra("id"));
                values.put("comment",submit_comment);
                values.put("time",time);
                db.insert("comments",null,values);
                Log.i("1","评论成功");
                Toast.makeText(getApplicationContext(), "评论成功", Toast.LENGTH_SHORT).show();
                // 刷新页面
                Intent intent_=new Intent(item_info.this,item_info.class);
                intent_.putExtra("id",intent.getStringExtra("id"));
                startActivity(intent_);
                finish();
            }
        });

        // 议价按钮
        Button btnBargain = (Button) findViewById(R.id.btn_bargain);
        btnBargain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "议价功能开发中", Toast.LENGTH_SHORT).show();
            }
        });

        // 立即聊天按钮
        Button btnChat = (Button) findViewById(R.id.btn_chat);
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contactInfo != null && !contactInfo.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "联系卖家: " + contactInfo, Toast.LENGTH_LONG).show();
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
