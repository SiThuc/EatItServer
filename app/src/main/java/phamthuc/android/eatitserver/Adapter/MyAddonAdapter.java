package phamthuc.android.eatitserver.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import phamthuc.android.eatitserver.Callback.IRecyclerClickListener;
import phamthuc.android.eatitserver.EventBus.SelectAddonModel;
import phamthuc.android.eatitserver.EventBus.SelectSizeModel;
import phamthuc.android.eatitserver.EventBus.UpdateAddonModel;
import phamthuc.android.eatitserver.EventBus.UpdateSizeModel;
import phamthuc.android.eatitserver.Model.AddonModel;
import phamthuc.android.eatitserver.Model.SizeModel;
import phamthuc.android.eatitserver.R;

public class MyAddonAdapter extends RecyclerView.Adapter<MyAddonAdapter.MyViewHolder> {

    Context context;
    List<AddonModel> addonModelList;
    UpdateAddonModel updateAddonModel;
    int editPos;

    public MyAddonAdapter(Context context, List<AddonModel> addonModelList) {
        this.context = context;
        this.addonModelList = addonModelList;
        this.updateAddonModel = new UpdateAddonModel();
        this.editPos = -1;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder( LayoutInflater.from( context ).inflate( R.layout.layout_size_addon_display, viewGroup, false ) );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        holder.txt_name.setText( addonModelList.get( i ).getName() );
        holder.txt_price.setText( String.valueOf( addonModelList.get( i ).getPrice() ) );

        //Event
        holder.img_delete.setOnClickListener( v -> {
            //Delete item
            addonModelList.remove( i );
            notifyItemRemoved( i );
            updateAddonModel.setAddonModelList( addonModelList ); // Set for event
            EventBus.getDefault().postSticky( updateAddonModel ); // Send event
        } );
        holder.setListener( (view, pos) -> {
            editPos = i;
            EventBus.getDefault().postSticky( new SelectAddonModel(addonModelList.get( pos )) );
        } );
    }

    @Override
    public int getItemCount() {
        return addonModelList.size();
    }

    public void addNewAddon(AddonModel addonModel){
        addonModelList.add( addonModel );
        notifyItemInserted( addonModelList.size() -1 );
        updateAddonModel.setAddonModelList( addonModelList );
        EventBus.getDefault().postSticky( updateAddonModel );
    }

    public void editAddon(AddonModel addonModel) {
        if(editPos != -1){
            addonModelList.set( editPos, addonModel );
            notifyItemChanged( editPos );
            editPos = -1; // Reset variable after success
            //Send update
            updateAddonModel.setAddonModelList( addonModelList );
            EventBus.getDefault().postSticky( updateAddonModel );
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        @BindView( R.id.txt_name )
        TextView txt_name;
        @BindView( R.id.txt_price )
        TextView txt_price;
        @BindView( R.id.img_delete )
        ImageView img_delete;

        Unbinder unbinder;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super( itemView );
            unbinder = ButterKnife.bind( this, itemView );
            itemView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClickListener( v, getAdapterPosition() );
                }
            } );
        }
    }
}
