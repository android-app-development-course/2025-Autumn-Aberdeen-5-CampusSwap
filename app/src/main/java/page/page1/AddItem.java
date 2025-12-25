package page.page1;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static page.page1.LoginMainActivity.post_userid;

public class AddItem extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 1;
    private DatabaseHelper dbHelper;
    private Spinner sp;
    private ImageButton imageButton;
    private byte[] image;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null) loadImage(selectedImage);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_m1);

        dbHelper = new DatabaseHelper(this);
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ", Locale.CHINA);

        // 初始化 Spinner
        sp = findViewById(R.id.m1_style);
        String[] ctype = new String[]{"生活用品", "学习用品", "电子产品", "体育用品"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ctype);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);

        imageButton = findViewById(R.id.m1_image);
        imageButton.setOnClickListener(v -> checkPermissionAndOpenGallery());

        // 发布逻辑
        Button fabu = findViewById(R.id.fabu);
        fabu.setOnClickListener(v -> {
            EditText title = findViewById(R.id.m1_title);
            EditText price = findViewById(R.id.m1_price);
            EditText phone = findViewById(R.id.m1_phone); // 现在不再报错了
            EditText nr = findViewById(R.id.m1_nr);

            // 实时获取 Spinner 选中的值
            String kind = sp.getSelectedItem().toString();

            if (title.getText().toString().isEmpty() || price.getText().toString().isEmpty()) {
                Toast.makeText(this, "请填写标题和价格", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("title", title.getText().toString());
            values.put("userId", post_userid);
            values.put("kind", kind);
            values.put("time", formatter.format(new Date()));
            values.put("price", price.getText().toString());
            values.put("contact", phone.getText().toString());
            values.put("info", nr.getText().toString());
            values.put("image", image);

            db.insert("iteminfo", null, values);
            Toast.makeText(getApplicationContext(), "发布成功", Toast.LENGTH_SHORT).show();

            // 返回主页或清空
            finish();
        });

        // 导航按钮
        findViewById(R.id.but1_m1).setOnClickListener(v -> finish());
        findViewById(R.id.but2_m1).setOnClickListener(v -> {
            startActivity(new Intent(AddItem.this, MyItems.class));
        });
    }

    private void checkPermissionAndOpenGallery() {
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_PERMISSION);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void loadImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bm = BitmapFactory.decodeStream(is);
            if (bm != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                image = baos.toByteArray();
                imageButton.setImageBitmap(bm);
            }
            if (is != null) is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}