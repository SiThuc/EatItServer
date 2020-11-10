package phamthuc.android.eatitserver.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import phamthuc.android.eatitserver.Adapter.MyOrderAdapter;
import phamthuc.android.eatitserver.R;

public class OrderFragment extends Fragment {
    @BindView( R.id.recycler_order )
    RecyclerView recycler_order;

    Unbinder unbinder;
    LayoutAnimationController layoutAnimationController;
    MyOrderAdapter adapter;

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
            }
        } );

        return root;
    }

    private void initViews() {
        recycler_order.setHasFixedSize( true );
        recycler_order.setLayoutManager( new LinearLayoutManager( getContext() ) );
        layoutAnimationController = AnimationUtils.loadLayoutAnimation( getContext(), R.anim.layout_item_from_left );
    }
}