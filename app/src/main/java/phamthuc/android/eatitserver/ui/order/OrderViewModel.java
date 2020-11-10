package phamthuc.android.eatitserver.ui.order;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import phamthuc.android.eatitserver.Callback.IOrderCallbackListener;
import phamthuc.android.eatitserver.Common.Common;
import phamthuc.android.eatitserver.Model.OrderModel;

public class OrderViewModel extends ViewModel implements IOrderCallbackListener {

    private MutableLiveData<List<OrderModel>> orderModelMutableLiveData;
    private MutableLiveData<String> messageError;

    private IOrderCallbackListener listener;

    public OrderViewModel() {
        orderModelMutableLiveData = new MutableLiveData<>(  );
        messageError = new MutableLiveData<>(  );
        listener = this;
    }

    public MutableLiveData<List<OrderModel>> getOrderModelMutableLiveData() {
        loadOrderMyStatus(0);
        return orderModelMutableLiveData;
    }

    private void loadOrderMyStatus(int status) {
        List<OrderModel> tempList = new ArrayList<>(  );
        Query orderRef = FirebaseDatabase.getInstance().getReference( Common.ORDER_REF)
                .orderByChild("orderStatus")
                .equalTo( status );
        orderRef.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot itemSnapShot : snapshot.getChildren()){
                    OrderModel orderModel = itemSnapShot.getValue(OrderModel.class);
                    orderModel.setKey( itemSnapShot.getKey() ); // Do not forget it
                    tempList.add(orderModel);
                }
                listener.onOrderLoadSuccess( tempList );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onOrderLoadFailed( error.getMessage() );
            }
        } );
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onOrderLoadSuccess(List<OrderModel> orderModelList) {
        if(orderModelList.size() > 0){
            Collections.sort( orderModelList,(orderModel, t1) -> {
                if(orderModel.getCreatedDate() < t1.getCreatedDate())
                    return -1;
                return orderModel.getCreatedDate() == t1.getCreatedDate() ? 0:1;
            } );
        }
        orderModelMutableLiveData.setValue( orderModelList );
    }

    @Override
    public void onOrderLoadFailed(String message) {
        messageError.setValue( message );
    }
}