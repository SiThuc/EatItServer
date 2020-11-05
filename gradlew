package phamthuc.android.eatitclient.ui.cart;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import phamthuc.android.eatitclient.Adapter.MyCartAdapter;
import phamthuc.android.eatitclient.Common.Common;
import phamthuc.android.eatitclient.Common.MySwipeHelper;
import phamthuc.android.eatitclient.Database.CartDataSource;
import phamthuc.android.eatitclient.Database.CartDatabase;
import phamthuc.android.eatitclient.Database.CartItem;
import phamthuc.android.eatitclient.Database.LocalCartDataSource;
import phamthuc.android.eatitclient.EventBus.CounterCartEvent;
import phamthuc.android.eatitclient.EventBus.HideFABCart;
import phamthuc.android.eatitclient.EventBus.UpdateItemInCart;
import phamthuc.android.eatitclient.R;

public class CartFragment extends Fragment {
    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;
    @BindView( R.id.recycler_cart )
    RecyclerView recycler_cart;
    @BindView( R.id.txt_total_price )
    TextView txt_total_price;
    @BindView( R.id.txt_empty_cart )
    TextView txt_empty_cart;
    @BindView( R.id.group_place_holder )
    CardView group_place_holder;

    private Unbinder unbinder;

    private CartViewModel mViewModel;
    private MyCartAdapter adapter;

    public static CartFragment newInstance() {
        return new CartFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get( CartViewModel.class );
        View root = inflater.inflate( R.layout.cart_fragment, container, false );
        mViewModel.initCartDataSource( getContext() );
        mViewModel.getMutableLiveDataCartItems().observe( getViewLifecycleOwner(), new Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> cartItems) {
                if(cartItems == null || cartItems.isEmpty()){
                    recycler_cart.setVisibility( View.GONE );
                    group_place_holder.setVisibility( View.GONE );
                    txt_empty_cart.setVisibility( View.VISIBLE );
                }else{
                    recycler_cart.setVisibility( View.VISIBLE );
                    group_place_holder.setVisibility( View.VISIBLE );
                    txt_empty_cart.setVisibility( View.GONE );

                    adapter = new MyCartAdapter( getContext(), cartItems );
                    recycler_cart.setAdapter( adapter );
                }
            }
        } );

        unbinder = ButterKnife.bind( this, root );
        initViews();
        return root;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem( R.id.action_settings ).setVisible( false ); // Hide home menu already inflate
        super.onPrepareOptionsMenu( menu );
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate( R.menu.cart_menu, menu );
        super.onCreateOptionsMenu( menu, inflater );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_clear_cart){
            cartDataSource.cleanCart( Common.currentUser.getUid() )
                    .subscribeOn( Schedulers.io() )
                    .observeOn( AndroidSchedulers.mainThread() )
                    .subscribe( new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            Toast.makeText( getContext(), "Clear Cart Success", Toast.LENGTH_SHORT ).show();
                            EventBus.getDefault().postSticky( new CounterCartEvent( true ) );

                        }

                        @Override
                        public void onError(Throwable e) 