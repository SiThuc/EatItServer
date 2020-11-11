package phamthuc.android.eatitserver.ui.order;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import phamthuc.android.eatitserver.Adapter.MyOrderAdapter;
import phamthuc.android.eatitserver.Common.BottomSheetOrderFragment;
import phamthuc.android.eatitserver.Common.Common;
import phamthuc.android.eatitserver.Common.MySwipeHelper;
import phamthuc.android.eatitserver.EventBus.AddonSizeEditEvent;
import phamthuc.android.eatitserver.EventBus.ChangeMenuClick;
import phamthuc.android.eatitserver.EventBus.LoadOrderEvent;
import phamthuc.android.eatitserver.Model.FoodModel;
import phamthuc.android.eatitserver.Model.OrderModel;
import phamthuc.android.eatitserver.R;
import phamthuc.android.eatitserver.SizeAddonEditActivity;

public class OrderFragment extends Fragment {
    @BindView( R.id.recycler_order )
    RecyclerView recycler_order;

    Unbinder unbinder;
    LayoutAnimationController layoutAnimationController;
    MyOrderAdapter adapter;

    @BindView( R.id.txt_order_filter )
    TextView txt_order_filter;

    private OrderViewModel orderViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        orderViewModel = ViewModelProviders.of( this ).get( OrderViewModel.class );
        View root = inflater.inflate( R.layout.fragment_order, container, false );
        unbinder = ButterKnife.bind( this, root );
        initViews();

        orderViewModel.getMessageError().observe( getViewLifecycleOwner(), s->{
            Toast.makeText( getContext(), s, Toast.LENGTH_SHORT ).show();
        } );
        orderViewModel.getOrderModelMutableLiveData().observe( getViewLifecycleOwner(), orderModelList -> {
            if(orderModelList != null){
                adapter = new MyOrderAdapter( getContext(), orderModelList );
                recycler_order.setAdapter( adapter );
                recycler_order.setLayoutAnimation( layoutAnimationController );

                txt_order_filter.setText( new StringBuilder( "Number of Orders (" )
                .append( orderModelList.size())
                .append(")"));
            }
        } );

        return root;
    }

    private void initViews() {
        setHasOptionsMenu( true );

        recycler_order.setHasFixedSize( true );
        recycler_order.setLayoutManager( new LinearLayoutManager( getContext() ) );
        layoutAnimationController = AnimationUtils.loadLayoutAnimation( getContext(), R.anim.layout_item_from_left );

        //Get Size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics( displayMetrics );
        int width = displayMetrics.widthPixels;

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_order, width/6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton( getContext(), "Directions", 30, 0, Color.parseColor("#9b0000"),
                        pos -> {


                        }));
                buf.add(new MyButton( getContext(), "Call", 30, 0, Color.parseColor("#560027"),
                        pos -> {
                            Dexter.withActivity(getActivity())
                                    .withPermission( Manifest.permission.CALL_PHONE )
                                    .withListener( new PermissionListener() {
                                        @Override
                                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                            OrderModel orderModel = adapter.getItemAtPosition(pos);
                                            Intent intent = new Intent(  );
                                            intent.setAction( Intent.ACTION_DIAL );
                                            intent.setData( Uri.parse(new StringBuilder( "tel: " )
                                            .append( orderModel.getUserPhone() ).toString()) );
                                            startActivity( intent );
                                        }

                                        @Override
                                        public void onPermissionDenied(PermissionDeniedResponse response) {
                                            Toast.makeText( getContext(), "You have to acceppt"+response.getPermissionName(), Toast.LENGTH_SHORT ).show();
                                        }

                                        @Override
                                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                                        }
                                    } ).check();


                        }));
                buf.add(new MyButton( getContext(), "Remove", 30, 0, Color.parseColor("#12005e"),
                        pos -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );
                            builder.setTitle( "Delete ?" )
                                    .setMessage( "Do you want to delete this order ?" )
                                    .setNegativeButton( "CANCEL", (dialogInterface, which) -> dialogInterface.dismiss() )
                                    .setPositiveButton( "OK", (dialogInterface, which) -> {
                                        OrderModel orderModel = adapter.getItemAtPosition( pos );
                                        FirebaseDatabase.getInstance()
                                                .getReference(Common.ORDER_REF)
                                                .child( orderModel.getKey() )
                                                .removeValue()
                                                .addOnFailureListener( e -> {
                                                    Toast.makeText( getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
                                                } )
                                                .addOnSuccessListener( aVoid -> {
                                                    adapter.removeItem(pos);
                                                    adapter.notifyItemRemoved( pos );
                                                    txt_order_filter.setText( new StringBuilder( "Orders (")
                                                    .append( adapter.getItemCount() )
                                                    .append( ")" ));
                                                    dialogInterface.dismiss();
                                                    Toast.makeText(getContext(), "Order has been delete", Toast.LENGTH_SHORT ).show();
                                                } );
                                    } );
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            Button negativeButton = dialog.getButton( DialogInterface.BUTTON_NEGATIVE );
                            negativeButton.setTextColor( Color.GRAY );
                            Button positiveButton = dialog.getButton( DialogInterface.BUTTON_POSITIVE );
                            positiveButton.setTextColor( Color.RED );

                        }));

                buf.add(new MyButton( getContext(), "Edit", 30, 0, Color.parseColor("#336699"),
                        pos -> {

                        }));
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate( R.menu.order_filter_menu, menu );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_filter){
            BottomSheetOrderFragment bottomSheetOrderFragment = BottomSheetOrderFragment.getInstance();
            bottomSheetOrderFragment.show( getActivity().getSupportFragmentManager(), "OrderFilter" );
            return true;
        }else{
            return super.onOptionsItemSelected( item );
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered( this ))
            EventBus.getDefault().register( this );
    }

    @Override
    public void onStop() {
        if(EventBus.getDefault().hasSubscriberForEvent( LoadOrderEvent.class ))
            EventBus.getDefault().removeStickyEvent( LoadOrderEvent.class );
        if(EventBus.getDefault().isRegistered( this ))
            EventBus.getDefault().unregister( this );
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky( new ChangeMenuClick( true ) );
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadOrderEvent(LoadOrderEvent event){
        orderViewModel.loadOrderByStatus( event.getStatus() );
    }
}