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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main_page extends AppCompatActivity implements View.OnClickListener{

    String TABLENAME = "iteminfo";
    Intent intent;
    byte[] imagedata;
    Bitmap imagebm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
        DatabaseHelper database = new DatabaseHelper(this);
        final SQLiteDatabase db = database.getWritableDatabase();
        ListView listView = (ListView)findViewById(R.id.listView);
        Map<String, Object> item;
        final List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        Cursor cursor = db.query(TABLENAME,null,null,null,null,null,null,null);
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                item = new HashMap<String, Object>();
                item.put("id",cursor.getInt(0));
                item.put("userid",cursor.getString(1));
                item.put("title",cursor.getString(2));
                item.put("kind",cursor.getString(3));
                item.put("info",cursor.getString(4));
                item.put("price",cursor.getString(5));
                imagedata = cursor.getBlob(6);
                imagebm = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
                item.put("image",imagebm);
                cursor.moveToNext();
                data.add(item);
            }
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, data, R.layout.listitem, new String[] { "image", "title", "kind", "info", "price" },
                new int[] { R.id.item_image, R.id.title, R.id.kind, R.id.info, R.id.price });
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(view instanceof ImageView  && data instanceof Bitmap){
                    ImageView iv = (ImageView)view;
                    iv.setImageBitmap( (Bitmap)data );
                    return true;
                }else{
                    return false;
                }
            }
        });
        listView.setAdapter(simpleAdapter);

        // 分类标签点击事件
        TextView tabAll = (TextView) findViewById(R.id.tab_all);
        TextView tabSports = (TextView) findViewById(R.id.tab_sports);
        TextView tabLife = (TextView) findViewById(R.id.tab_life);
        TextView tabDigital = (TextView) findViewById(R.id.tab_digital);
        TextView tabStudy = (TextView) findViewById(R.id.tab_study);

        tabAll.setOnClickListener(this);
        tabSports.setOnClickListener(this);
        tabLife.setOnClickListener(this);
        tabDigital.setOnClickListener(this);
        tabStudy.setOnClickListener(this);

        // 保留旧的kind点击事件兼容性
        ImageView kind1 = (ImageView) findViewById(R.id.kind1);
        kind1.setOnClickListener(this);
        ImageView kind2 = (ImageView) findViewById(R.id.kind2);
        kind2.setOnClickListener(this);
        ImageView kind3 = (ImageView) findViewById(R.id.kind3);
        kind3.setOnClickListener(this);
        ImageView kind4 = (ImageView) findViewById(R.id.kind4);
        kind4.setOnClickListener(this);

        // 商品列表点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intent = new Intent(main_page.this, item_info.class);
                intent.putExtra("id", data.get(position).get("id").toString());
                startActivity(intent);
            }
        });

        // 发布闲置浮动按钮
        ImageView fabAdd = (ImageView) findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(this);

        // 底部导航栏
        RadioButton btn1 = (RadioButton)findViewById(R.id.button_1);
        RadioButton btnMsg = (RadioButton)findViewById(R.id.button_msg);
        RadioButton btn3 = (RadioButton)findViewById(R.id.button_3);
        RadioButton btnMore = (RadioButton)findViewById(R.id.button_more);

        btn1.setOnClickListener(this);
        btnMsg.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btnMore.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        int id = v.getId();

        // 分类标签点击
        if (id == R.id.tab_sports || id == R.id.kind1) {
            Intent KindIntent1 = new Intent(this, kind_page1.class);
            startActivity(KindIntent1);
        } else if (id == R.id.tab_life || id == R.id.kind2) {
            Intent KindIntent2 = new Intent(this, kind_page2.class);
            startActivity(KindIntent2);
        } else if (id == R.id.tab_digital || id == R.id.kind3) {
            Intent KindIntent3 = new Intent(this, kind_page3.class);
            startActivity(KindIntent3);
        } else if (id == R.id.tab_study || id == R.id.kind4) {
            Intent KindIntent4 = new Intent(this, kind_page4.class);
            startActivity(KindIntent4);
        } else if (id == R.id.tab_all) {
            // 刷新当前页面显示全部商品
            Intent button1 = new Intent(main_page.this, main_page.class);
            startActivity(button1);
            finish();
        }
        // 底部导航栏点击
        else if (id == R.id.button_1) {
            // 已在首页，不需要操作
        } else if (id == R.id.button_msg) {
            // 消息页面 - 暂时提示
            Toast.makeText(this, "消息功能开发中", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.button_3) {
            Intent button3 = new Intent(this, MyselfActivity.class);
            startActivity(button3);
        } else if (id == R.id.button_more) {
            Intent buttonMore = new Intent(this, AboutMainActivity.class);
            startActivity(buttonMore);
        }
        // 发布闲置按钮
        else if (id == R.id.fab_add) {
            String userId = LoginMainActivity.post_userid;
            if (userId == null || userId.equals("")) {
                Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(this, LoginMainActivity.class);
                startActivity(loginIntent);
            } else {
                Intent addIntent = new Intent(this, AddItem.class);
                startActivity(addIntent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // 在首页按返回键退出应用
        moveTaskToBack(true);
    }
}
