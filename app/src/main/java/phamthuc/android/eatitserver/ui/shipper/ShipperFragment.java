package phamthuc.android.eatitserver.ui.shipper;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import phamthuc.android.eatitserver.Adapter.MyShipperAdapter;
import phamthuc.android.eatitserver.Common.Common;
import phamthuc.android.eatitserver.EventBus.ChangeMenuClick;
import phamthuc.android.eatitserver.EventBus.UpdateShipperEvent;
import phamthuc.android.eatitserver.Model.ShipperModel;
import phamthuc.android.eatitserver.R;

public class ShipperFragment extends Fragment {

    private ShipperViewModel mViewModel;
    private Unbinder unbinder;

    @BindView(R.id.recycler_shipper)
    RecyclerView recycler_shipper;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyShipperAdapter adapter;
    List<ShipperModel> shipperModelList, saveShipperBeforeSearchList;

    public static ShipperFragment newInstance() {
        return new ShipperFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateShipperEvent.class))
            EventBus.getDefault().removeStickyEvent(UpdateShipperEvent.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.shipper_fragment, container, false);
        mViewModel = new ViewModelProvider(this).get(ShipperViewModel.class);
        unbinder = ButterKnife.bind(this, itemView);
        initViews();

        mViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        mViewModel.getShipperMutableList().observe(getViewLifecycleOwner(), shippers -> {
            dialog.dismiss();
            shipperModelList = shippers;
            if(saveShipperBeforeSearchList == null)
                saveShipperBeforeSearchList = shippers;

            adapter = new MyShipperAdapter(getContext(), shipperModelList);
            recycler_shipper.setAdapter(adapter);

            recycler_shipper.setLayoutAnimation(layoutAnimationController);
        });

        return itemView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.food_list_menu, menu);
        MenuItem menuItem = menu.findItem( R.id.action_search );

        SearchManager searchManager = (SearchManager)getActivity().getSystemService( Context.SEARCH_SERVICE );
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setSearchableInfo( searchManager.getSearchableInfo( getActivity().getComponentName() ) );

        //Event
        searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearchShipper(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        } );

        //Clear text when click to Clear button on Search View
        ImageView closeButton = (ImageView)searchView.findViewById( R.id.search_close_btn );
        closeButton.setOnClickListener( v -> {
            EditText ed = (EditText)searchView.findViewById( R.id.search_src_text );
            //Clear text
            ed.setText( "" );
            //Clear query
            searchView.setQuery( "", false );
            //Collapse the action view
            searchView.onActionViewCollapsed();
            //Collapse the search widget
            menuItem.collapseActionView();
            //Restore result to original
            if(saveShipperBeforeSearchList!= null)
                mViewModel.getShipperMutableList().setValue( saveShipperBeforeSearchList);
        } );
    }

    private void startSearchShipper(String query) {
        List<ShipperModel> resultShipper = new ArrayList<>();
        for(ShipperModel shipperModel: shipperModelList){
            if(shipperModel.getPhone().toLowerCase().contains(query.toLowerCase())){
                resultShipper.add(shipperModel);
            }
        }
        mViewModel.getShipperMutableList().setValue(resultShipper);
    }

    private void initViews() {
        setHasOptionsMenu(true);
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_shipper.setLayoutManager(layoutManager);
        recycler_shipper.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateShipperActive(UpdateShipperEvent event) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("active", event.isActive()); // Get state of button, not of shipper
        FirebaseDatabase.getInstance()
                .getReference(Common.SHIPPER)
                .child(event.getShipperModel().getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Update state to " + event.isActive() + " successfully!", Toast.LENGTH_SHORT).show();
                });
    }
}