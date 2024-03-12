package hoanhqph30066.fpoly.executerserviceapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SuaSpActivity extends AppCompatActivity {
    private EditText edSuaTen, edSuaGia, edSuaBrand;
    private Button btnQuayLai, btnSua;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sua_sp);

        edSuaTen = findViewById(R.id.edSuaTen);
        edSuaGia = findViewById(R.id.edSuaGia);
        edSuaBrand = findViewById(R.id.edSuaBrand);
        btnQuayLai = findViewById(R.id.btnSuaQl);
        btnSua = findViewById(R.id.btnSua);

        // Lấy dữ liệu sản phẩm cần sửa từ Intent
        String productName = getIntent().getStringExtra("PRODUCT_NAME");
        double productPrice = getIntent().getDoubleExtra("PRODUCT_PRICE", 0.0);
        String productBrand = getIntent().getStringExtra("PRODUCT_BRAND");


        // Hiển thị dữ liệu sản phẩm lên EditText
        edSuaTen.setText(productName);
        edSuaGia.setText(String.valueOf(productPrice));
        edSuaBrand.setText(productBrand);

        btnQuayLai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edSuaTen.getText().toString().isEmpty() || edSuaGia.getText().toString().isEmpty() || edSuaBrand.getText().toString().isEmpty()) {
                    Toast.makeText(SuaSpActivity.this, "Vui lòng điền đầy đủ thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                } else {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(new PutProductRunnable());
                    executorService.shutdown();
                }
            }
        });
    }

    private class PutProductRunnable implements Runnable {
        String productId = getIntent().getStringExtra("PRODUCT_ID");
        String productName = edSuaTen.getText().toString();
        double productPrice = Double.parseDouble(edSuaGia.getText().toString());
        String productBrand = edSuaBrand.getText().toString();

        @Override
        public void run() {
            try {
                // Tạo JSON object chứa dữ liệu sản phẩm
                JSONObject productJson = new JSONObject();
                productJson.put("name", productName);
                productJson.put("price", productPrice);
                productJson.put("brand", productBrand);

                // Tạo yêu cầu HTTP PUT
                URL url = new URL("http://192.168.0.110:3000/product/put/" + productId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);

                // Ghi dữ liệu sản phẩm vào luồng đầu ra
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(productJson.toString().getBytes("UTF-8"));
                outputStream.close();

                // Đọc phản hồi từ server
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Xử lý phản hồi thành công từ server
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SuaSpActivity.this, "Sửa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SuaSpActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    // Xử lý phản hồi không thành công từ server
                    Log.e("loiput", "Server response code: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Exception", e.toString());
            }
        }
    }
}