package hoanhqph30066.fpoly.executerserviceapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import android.os.Handler;


public class MainActivity extends AppCompatActivity {
    private Button them;
    private RecyclerView recyclerView;
    private ProductAdapter  adapter;
    private ArrayList<Product> productList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rcv);
        progressBar = findViewById(R.id.progressBar);
        them = findViewById(R.id.btnThem);

        productList = new ArrayList<>();
        adapter = new ProductAdapter( productList,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new GetProductsRunnable());
        executorService.shutdown();;

        them.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ThemSpActivity.class);
                startActivity(intent);
            }
        });

////        ExecutorService executorService = Executors.newSingleThreadExecutor();
//
////        ExecutorService executorService = Executors.newFixedThreadPool(3);
//        ExecutorService executorService = Executors.newCachedThreadPool();
//
//        for (int i =0;i<4;i++){
//            executorService.submit(new MyRunable(""+i));
//        }
//        Future<JSONArray> future = executorService.submit(new MyCallable());
//        try {
//            if(future.get()!=null){
//                Log.d("ExecuterService",future.get().toString());
//            }
//
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        executorService.shutdown();
    }

//    private JSONArray callAPI(String urlString) throws IOException {
//        // Tạo URL từ đường dẫn đã cho
//        URL url = new URL(urlString);
//
//        // Mở kết nối
//        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//
//        try {
//            // Đọc dữ liệu từ kết nối
//            InputStream in = urlConnection.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//            StringBuilder result = new StringBuilder();
//            String line;
//
//            while ((line = reader.readLine()) != null) {
//                result.append(line);
//            }
//
//            // Chuyển dữ liệu đọc được thành một JSONArray
//            return new JSONArray(result.toString());
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        } finally {
//            // Đóng kết nối
//            urlConnection.disconnect();
//        }
//    }

//    public class MyRunable implements Runnable {
//    String name;
//
//        public MyRunable(String name) {
//            this.name = name;
//        }
//
//        @Override
//        public void run() {
//            try {
//                Log.d("ExecuterService",name+"đang chạy");
//                Thread.sleep(200);
//                Log.d("ExecuterService",name+"chết");
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

    //    public class MyCallable implements Callable<JSONArray>{
//
//        @Override
//        public JSONArray call() throws Exception {
//            JSONArray jsonArray = callAPI("http://192.168.0.110:3000/get/alldata");
//            return jsonArray;
//        }
//    }
    private class GetProductsRunnable implements Runnable {
        @Override
        public void run() {
            try {
                // Hiển thị progressBar
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });

                // Lấy dữ liệu từ server
                String urlString = "http://192.168.0.110:3000/get/alldata";
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                // Chuyển đổi dữ liệu JSON thành danh sách sản phẩm
                JSONArray jsonArray = new JSONArray(result.toString());
                ArrayList<Product> products = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String productId = jsonObject.getString("_id");
                    String productName = jsonObject.getString("name");
                    double productPrice = jsonObject.getDouble("price");
                    String productBrand = jsonObject.getString("brand");
                    // Tạo đối tượng Product và thêm vào danh sách
                    Product product = new Product(productId, productName, productPrice, productBrand);
                    products.add(product);
                }

                // Cập nhật danh sách sản phẩm
                productList.clear();
                productList.addAll(products);

                // Đóng kết nối
                connection.disconnect();

                // Hiển thị dữ liệu lên RecyclerView
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        // Ẩn progressBar sau khi đã tải xong dữ liệu
                        progressBar.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Xử lý lỗi ở đây (ví dụ: hiển thị thông báo cho người dùng)
            }
        }
    }

}
