package phamthuc.android.eatitserver.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import phamthuc.android.eatitserver.Adapter.MyOrderAdapter;
import phamthuc.android.eatitserver.Common.BottomSheetOrderFragment;
import phamthuc.android.eatitserver.EventBus.ChangeMenuClick;
import phamthuc.android.eatitserver.EventBus.LoadOrderEvent;
import phamthuc.android.eatitserver.R;

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
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate( R.menu.order_filter_menu, menu );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_filter:
                BottomSheetOrderFragment bottomSheetOrderFragment = BottomSheetOrderFragment.getInstance();
                bottomSheetOrderFragment.show( getActivity().getSupportFragmentManager(), "OrderFilter" );
                break;

        }
        return true;
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