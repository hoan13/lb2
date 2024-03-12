package hoanhqph30066.fpoly.executerserviceapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private ArrayList<Product> productList;
    private Context context;

    public ProductAdapter(ArrayList<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        final Product product = productList.get(position);
        holder.idsp.setText(product.getId());
        holder.tensp.setText(product.getName());
        holder.brandsp.setText(product.getBrand());
        holder.giasp.setText(String.valueOf(product.getPrice()));

        // Xử lý sự kiện khi nút Xóa được nhấn
        holder.xoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String productId = product.getId(); // Lấy _id của sản phẩm

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xác nhận xóa");
                builder.setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?");
                builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Người dùng đã xác nhận xóa, tiến hành gửi yêu cầu xóa sản phẩm
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.submit(new DeleteProductRunnable(productId));
                        executorService.shutdown();
                    }
                });
                builder.setNegativeButton("Hủy", null);
                builder.show();
            }

        });

        // Xử lý sự kiện khi nút Sửa được nhấn
        holder.sua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SuaSpActivity.class);
                intent.putExtra("PRODUCT_ID", product.getId());
                intent.putExtra("PRODUCT_NAME", product.getName());
                intent.putExtra("PRODUCT_PRICE", product.getPrice());
                intent.putExtra("PRODUCT_BRAND", product.getBrand());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tensp, brandsp, giasp, idsp;
        ImageView xoa, sua;

        public ProductViewHolder(View itemView) {
            super(itemView);
            idsp = itemView.findViewById(R.id.tvIdsp);
            tensp = itemView.findViewById(R.id.tvTensp);
            brandsp = itemView.findViewById(R.id.tvBrand);
            giasp = itemView.findViewById(R.id.tvGia);
            xoa = itemView.findViewById(R.id.imgXoa);
            sua = itemView.findViewById(R.id.imgSua);
        }
    }

    public void removeProductById(String productId) {
        for (int i = 0; i < productList.size(); i++) {
            Product product = productList.get(i);
            if (product.getId().equals(productId)) {
                productList.remove(i);
                notifyDataSetChanged(); // Cập nhật lại toàn bộ danh sách sản phẩm
                break; // Break out of loop after removing the item
            }
        }
    }

    private class DeleteProductRunnable implements Runnable {
        private String productId;

        public DeleteProductRunnable(String productId) {
            this.productId = productId;
        }

        @Override
        public void run() {
            try {
                // Gửi yêu cầu DELETE tới máy chủ để xóa sản phẩm
                String urlString = "http://192.168.0.110:3000/delete/" + productId;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Nếu xóa thành công trên máy chủ, tiến hành xóa sản phẩm khỏi RecyclerView trên client
                            removeProductById(productId);
                            Toast.makeText(context, "Xóa thành công thành công", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Xử lý lỗi nếu cần thiết
                    // Ví dụ: Hiển thị thông báo cho người dùng
                    Log.e("DeleteProductRunnable", "Failed to delete product, HTTP response code: " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                // Xử lý lỗi nếu cần thiết
                // Ví dụ: Hiển thị thông báo cho người dùng
                Log.e("DeleteProductRunnable", "Error deleting product: " + e.getMessage());
            }
        }
    }
}

