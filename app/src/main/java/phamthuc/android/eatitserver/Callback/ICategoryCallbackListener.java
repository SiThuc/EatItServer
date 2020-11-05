package phamthuc.android.eatitserver.Callback;

import java.util.List;

import phamthuc.android.eatitserver.Model.CategoryModel;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModelList);
    void onCategoryLoadFailed(String message);
}
