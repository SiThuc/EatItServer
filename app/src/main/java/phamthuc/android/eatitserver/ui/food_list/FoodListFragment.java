package phamthuc.android.eatitserver.ui.food_list;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import phamthuc.android.eatitserver.Adapter.MyFoodListAdapter;
import phamthuc.android.eatitserver.Common.Common;
import phamthuc.android.eatitserver.Common.MySwipeHelper;
import phamthuc.android.eatitserver.EventBus.AddonSizeEditEvent;
import phamthuc.android.eatitserver.EventBus.ChangeMenuClick;
import phamthuc.android.eatitserver.EventBus.ToastEvent;
import phamthuc.android.eatitserver.Model.FoodModel;
import phamthuc.android.eatitserver.R;
import phamthuc.android.eatitserver.SizeAddonEditActivity;

public class FoodListFragment extends Fragment {

    //Image upload
    private static final int PICK_IMAGE_REQUEST = 1234;
    private ImageView img_food;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;


    private FoodListViewModel foodListViewModel;

    private List<FoodModel> foodModelList;


    Unbinder unbinder;
    @BindView( R.id.recycler_food_list )
    RecyclerView recycler_food_list;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;
    private Uri imageUri=null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                ViewModelProviders.of( this ).get( FoodListViewModel.class );
        View root = inflater.inflate( R.layout.fragment_food_list, container, false );
        unbinder = ButterKnife.bind( this, root );
        initViews();
        foodListViewModel.getMutableLiveDataFoodList().observe( getViewLifecycleOwner(), foodModels -> {
            foodModelList = foodModels;
            adapter = new MyFoodListAdapter( getContext(), foodModelList );
            recycler_food_list.setAdapter( adapter );
            recycler_food_list.setLayoutAnimation( layoutAnimationController );
        } );
        return root;
    }

    private void initViews() {
        dialog = new SpotsDialog.Builder().setContext( getContext() ).setCancelable( false ).build();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle( Common.categorySelected.getName() );

        recycler_food_list.setHasFixedSize( true );
        recycler_food_list.setLayoutManager( new LinearLayoutManager( getContext() ) );

        layoutAnimationController = AnimationUtils.loadLayoutAnimation( getContext(), R.anim.layout_item_from_left );

        //Get Size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics( displayMetrics );
        int width = displayMetrics.widthPixels;


        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_food_list, width/6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton( getContext(), "Delete", 30, 0, Color.parseColor("#9b0000"),
                        pos -> {
                            if(foodModelList != null){
                                Common.selectedFood = foodModelList.get( pos );
                                AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );
                                builder.setTitle( "DELETE" )
                                        .setMessage( "Do you want to delete this food?" )
                                        .setNegativeButton( "CANCEL", (dialogInterface, which) -> {
                                            dialogInterface.dismiss();
                                        } )
                                        .setPositiveButton( "DELETE", (dialogInterface, which) -> {
                                            Common.categorySelected.getFoods().remove( pos );
                                            updateFood(Common.categorySelected.getFoods(), true);
                                        } );
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }

                        }));
                buf.add(new MyButton( getContext(), "Update", 30, 0, Color.parseColor("#560027"),
                        pos -> {
                            showUpdateDialog(pos);

                        }));
                buf.add(new MyButton( getContext(), "Size", 30, 0, Color.parseColor("#12005e"),
                        pos -> {
                            Common.selectedFood = foodModelList.get( pos );
                            startActivity( new Intent( getContext(), SizeAddonEditActivity.class ) );
                            EventBus.getDefault().postSticky( new AddonSizeEditEvent(false, pos) );


                        }));

                buf.add(new MyButton( getContext(), "Addon", 30, 0, Color.parseColor("#336699"),
                        pos -> {
                            Common.selectedFood = foodModelList.get( pos );
                            startActivity( new Intent( getContext(), SizeAddonEditActivity.class ) );
                            EventBus.getDefault().postSticky( new AddonSizeEditEvent(true, pos) );
                        }));
            }
        };
    }

    private void showUpdateDialog(int pos) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder( getContext() );
        builder.setTitle( "Update" );
        builder.setMessage( "Please fill information" );

        View itemView = LayoutInflater.from( getContext() ).inflate( R.layout.layout_update_food, null );
        EditText edt_food_name = (EditText)itemView.findViewById( R.id.edt_food_name );
        EditText edt_food_price = (EditText)itemView.findViewById( R.id.edt_food_price );
        EditText edt_food_description = (EditText)itemView.findViewById( R.id.edt_food_description );
        img_food = (ImageView)itemView.findViewById( R.id.img_food_image );

        //Set data
        edt_food_name.setText( new StringBuilder( "" )
            .append( Common.categorySelected.getFoods().get( pos ).getName() ));
        edt_food_price.setText( new StringBuilder( "" )
            .append( Common.categorySelected.getFoods().get( pos ).getPrice() ));
        edt_food_description.setText( new StringBuilder( "" )
            .append( Common.categorySelected.getFoods().get( pos ).getDescription() ));

        Glide.with( getContext() ).load( Common.categorySelected.getFoods().get( pos ).getImage() ).into( img_food );

        //Set event
        img_food.setOnClickListener( v -> {
            Intent intent = new Intent(  );
            intent.setType( "image/*" );
            intent.setAction( Intent.ACTION_GET_CONTENT );
            startActivityForResult( Intent.createChooser( intent, "Select Picture" ), PICK_IMAGE_REQUEST );
        } );

        builder.setNegativeButton( "CANCEL", (dialogInterface, which) -> {
            dialog.dismiss();
        } )
                .setPositiveButton( "UPDATE", (dialogInterface, which) -> {
            FoodModel updateFood = Common.categorySelected.getFoods().get( pos );
            updateFood.setName( edt_food_name.getText().toString() );
            updateFood.setDescription( edt_food_description.getText().toString() );
            updateFood.setPrice( TextUtils.isEmpty( edt_food_price.getText() )? 0:
                    Long.parseLong( edt_food_price.getText().toString() ));

            if(imageUri != null){
                // In this, we will use Firebase Storage to upload Image
                dialog.setMessage( "Uploading..." );
                dialog.show();

                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);
                //StorageReference imageFolder = storageReference.child( "images/"+ imageUri.getLastPathSegment() );

                imageFolder.putFile( imageUri )
                        .addOnFailureListener( e -> {
                            dialog.dismiss();
                            Toast.makeText( getContext(), "Loi tai day "+e.getMessage(), Toast.LENGTH_SHORT ).show();
                        } ).addOnCompleteListener( task -> {
                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener( uri -> {
                        updateFood.setImage( uri.toString() );
                        Common.categorySelected.getFoods().set( pos, updateFood );
                        updateFood(Common.categorySelected.getFoods(), false);
                    } );

                } ).addOnProgressListener( taskSnapshot ->{
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    dialog.setMessage( new StringBuilder( "Uploading: " ).append( progress ).append( "%" ) );
                } );
            }else{
                Common.categorySelected.getFoods().set( pos, updateFood );
                updateFood(Common.categorySelected.getFoods(), false);
            }
        } );

        builder.setView( itemView );
        androidx.appcompat.app.AlertDialog updateDialog = builder.create();
        updateDialog.show();
    }

    private void updateFood(List<FoodModel> foods, boolean isDeleted) {
        Map<String, Object> updateData = new HashMap<>(  );
        updateData.put( "foods", foods );

        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child( Common.categorySelected.getMenu_id() )
                .updateChildren( updateData )
                .addOnFailureListener( e -> {
                    Toast.makeText( getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
                } )
                .addOnCompleteListener( task -> {
                    if(task.isSuccessful()){
                        foodListViewModel.getMutableLiveDataFoodList();
                        EventBus.getDefault().postSticky( new ToastEvent( !isDeleted , true) );
                    }
                } );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            if(data != null && data.getData() != null){
                imageUri = data.getData();
                img_food.setImageURI( imageUri );
            }
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky( new ChangeMenuClick(true) );
        super.onDestroy();
    }
}