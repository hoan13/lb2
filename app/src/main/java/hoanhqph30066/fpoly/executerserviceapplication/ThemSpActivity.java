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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThemSpActivity extends AppCompatActivity {
    Button quaylai, them;
    EditText edTen, edGia, edBrand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_sp);
        quaylai = findViewById(R.id.btnQl);
        them = findViewById(R.id.btnThem);
        edTen = findViewById(R.id.edThemTen);
        edGia = findViewById(R.id.edThemGia);
        edBrand = findViewById(R.id.edThemBrand);

        quaylai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        them.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edTen.getText().toString().isEmpty() || edGia.getText().toString().isEmpty() || edBrand.getText().toString().isEmpty()) {
                    Toast.makeText(ThemSpActivity.this, "Vui lòng điền đầy đủ thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ThemSpActivity.this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    // Tạo một executor service
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    // Submit runnable để thực hiện
                    executorService.submit(new PostProductRunnable());
                    // Shutdown executor service sau khi hoàn thành
                    executorService.shutdown();
                }
            }

        });
    }

    private class PostProductRunnable implements Runnable {
        @Override
        public void run() {
            try {
                // Lấy dữ liệu từ các EditText
                String productName = edTen.getText().toString();
                double productPrice = Double.parseDouble(edGia.getText().toString());
                String productBrand = edBrand.getText().toString();

                // Tạo JSON object chứa dữ liệu sản phẩm
                JSONObject productJson = new JSONObject();
                productJson.put("name", productName);
                productJson.put("price", productPrice);
                productJson.put("brand", productBrand);

                // Tạo yêu cầu HTTP POST
                URL url = new URL("http://192.168.0.110:3000/product/post");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
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
                    Intent intent = new Intent(ThemSpActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Xử lý phản hồi không thành công từ server
                    Log.e("PostProductRunnable", "Server response code: " + responseCode);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e("PostProductRunnable", "MalformedURLException: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("PostProductRunnable", "IOException: " + e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("PostProductRunnable", "JSONException: " + e.getMessage());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Log.e("PostProductRunnable", "NumberFormatException: " + e.getMessage());
            }
        }
    }
}