package phamthuc.android.eatitserver.ui.food_list;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import phamthuc.android.eatitserver.Common.Common;
import phamthuc.android.eatitserver.Model.FoodModel;

public class FoodListViewModel extends ViewModel {

    private MutableLiveData<List<FoodModel>> mutableLiveDataFoodList;

    public FoodListViewModel() {
    }

    public MutableLiveData<List<FoodModel>> getMutableLiveDataFoodList() {
        if(mutableLiveDataFoodList == null)
            mutableLiveDataFoodList = new MutableLiveData<>(  );
        mutableLiveDataFoodList.setValue( Common.categorySelected.getFoods());
        return mutableLiveDataFoodList;
    }
}