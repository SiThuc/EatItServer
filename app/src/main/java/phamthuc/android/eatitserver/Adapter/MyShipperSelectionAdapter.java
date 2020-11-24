package phamthuc.android.eatitserver.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import phamthuc.android.eatitserver.Callback.IRecyclerClickListener;
import phamthuc.android.eatitserver.Model.ShipperModel;
import phamthuc.android.eatitserver.R;

public class MyShipperSelectionAdapter extends RecyclerView.Adapter<MyShipperSelectionAdapter.MyViewHolder> {
    private Context context;
    List<ShipperModel> shipperModelList;
    private ImageView lastCheckedImageView = null;
    private ShipperModel selectedShipper = null;

    public MyShipperSelectionAdapter(Context context, List<ShipperModel> shipperModelList) {
        this.context = context;
        this.shipperModelList = shipperModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_shipper_selected, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_name.setText(new StringBuilder(shipperModelList.get(position).getName()));
        holder.txt_phone.setText(new StringBuilder(shipperModelList.get(position).getPhone()));
        holder.setiRecyclerClickListener((view, pos) -> {
            if(lastCheckedImageView != null){
                lastCheckedImageView.setImageResource(0);
                holder.img_checked.setVisibility(View.GONE);
                lastCheckedImageView = null;
                selectedShipper = null;
            }else {
                holder.img_checked.setImageResource(R.drawable.ic_check_24);
                holder.img_checked.setVisibility(View.VISIBLE);
                lastCheckedImageView = holder.img_checked;
                selectedShipper = shipperModelList.get(pos);
            }
        });

    }

    public ShipperModel getSelectedShipper() {
        return selectedShipper;
    }

    @Override
    public int getItemCount() {
        return shipperModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;

        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_phone)
        TextView txt_phone;
        @BindView(R.id.img_checked)
        ImageView img_checked;
        IRecyclerClickListener iRecyclerClickListener;

        public void setiRecyclerClickListener(IRecyclerClickListener iRecyclerClickListener) {
            this.iRecyclerClickListener = iRecyclerClickListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerClickListener.onItemClickListener(v, getAdapterPosition());
        }
    }
}
