package phamthuc.android.eatitserver.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import phamthuc.android.eatitserver.Common.Common;
import phamthuc.android.eatitserver.Model.OrderModel;
import phamthuc.android.eatitserver.R;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.MyViewHolder> {
    Context context;
    List<OrderModel> orderModelList;
    SimpleDateFormat simpleDateFormat;

    public MyOrderAdapter(Context context, List<OrderModel> orderModelList) {
        this.context = context;
        this.orderModelList = orderModelList;
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder( LayoutInflater.from( context ).inflate( R.layout.layout_order_item, viewGroup, false ) );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        Glide.with( context )
                .load( orderModelList.get( i ).getCartItemList().get( 0 ).getFoodImage() )
                .into( holder.img_food_image );

        holder.txt_order_number.setText( orderModelList.get( i ).getKey() );
        Common.setSpanStringColor("Order date ", simpleDateFormat.format( orderModelList.get( i ).getCreatedDate() ),
                holder.txt_time, Color.parseColor( "#333639" ) );
        Common.setSpanStringColor("Order status ", Common.convertStatusToString(orderModelList.get( i ).getOrderStatus()),
                holder.txt_order_status, Color.parseColor( "#00579A" ) );
        Common.setSpanStringColor("Name ", orderModelList.get( i ).getUserName(),
                holder.txt_name, Color.parseColor( "#00574B" ) );
        Common.setSpanStringColor("Num of items ", orderModelList.get( i ).getCartItemList() == null? "0":
                String.valueOf( orderModelList.get( i ).getCartItemList().size() ),
                holder.txt_num_item, Color.parseColor( "#4B647D" ) );

    }

    @Override
    public int getItemCount() {
        return orderModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        @BindView( R.id.img_food_image )
        ImageView img_food_image;
        @BindView( R.id.txt_name )
        TextView txt_name;
        @BindView( R.id.txt_time )
        TextView txt_time;
        @BindView( R.id.txt_order_status )
        TextView txt_order_status;
        @BindView( R.id.txt_order_number )
        TextView txt_order_number;
        @BindView( R.id.txt_num_item )
        TextView txt_num_item;

        private Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super( itemView );
            unbinder = ButterKnife.bind( this, itemView );
        }
    }
}
