package phamthuc.android.eatitserver.Callback;

import java.util.List;

import phamthuc.android.eatitserver.Model.OrderModel;

public interface IOrderCallbackListener {
    void onOrderLoadSuccess(List<OrderModel> orderModelList);
    void onOrderLoadFailed(String message);
}
