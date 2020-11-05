// Generated code from Butter Knife. Do not modify!
package phamthuc.android.eatitclient.ui.fooddetail;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.lang.IllegalStateException;
import java.lang.Override;
import phamthuc.android.eatitclient.R;

public class FoodDetailFragment_ViewBinding implements Unbinder {
  private FoodDetailFragment target;

  private View view7f090069;

  private View view7f09006d;

  private View view7f09006a;

  private View view7f0900f8;

  @UiThread
  public FoodDetailFragment_ViewBinding(final FoodDetailFragment target, View source) {
    this.target = target;

    View view;
    target.img_food = Utils.findRequiredViewAsType(source, R.id.img_food, "field 'img_food'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.btnCart, "field 'btnCart' and method 'onCartItemAdd'");
    target.btnCart = Utils.castView(view, R.id.btnCart, "field 'btnCart'", CounterFab.class);
    view7f090069 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onCartItemAdd();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rating, "field 'btn_rating' and method 'onRatingButtonClick'");
    target.btn_rating = Utils.castView(view, R.id.btn_rating, "field 'btn_rating'", FloatingActionButton.class);
    view7f09006d = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onRatingButtonClick();
      }
    });
    target.food_name = Utils.findRequiredViewAsType(source, R.id.food_name, "field 'food_name'", TextView.class);
    target.food_decripstion 