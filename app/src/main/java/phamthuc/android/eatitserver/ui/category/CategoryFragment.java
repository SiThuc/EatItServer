package phamthuc.android.eatitserver.ui.category;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import phamthuc.android.eatitserver.Adapter.MyCategoriesAdapter;
import phamthuc.android.eatitserver.Common.Common;
import phamthuc.android.eatitserver.Common.MySwipeHelper;
import phamthuc.android.eatitserver.Model.CategoryModel;
import phamthuc.android.eatitserver.R;

public class CategoryFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1234;

    private CategoryViewModel categoryViewModel;

    Unbinder unbinder;
    @BindView( R.id.recycler_menu )
    RecyclerView recycler_menu;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoriesAdapter adapter;

    List<CategoryModel> categoryModels;
    ImageView img_category;
    private Uri imageUri = null;

    FirebaseStorage storage;
    StorageReference storageReference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        categoryViewModel =
                ViewModelProviders.of( this ).get( CategoryViewModel.class );
        View root = inflater.inflate( R.layout.fragment_category, container, false );
        unbinder = ButterKnife.bind( this, root );
        initViews();
        categoryViewModel.getMessageError().observe( getViewLifecycleOwner(), s -> {
            Toast.makeText( getContext(), ""+s, Toast.LENGTH_SHORT ).show();
            dialog.dismiss();
        });
        categoryViewModel.getCategoryListMutable().observe( getViewLifecycleOwner(), categoryModelList -> {
            dialog.dismiss();
            categoryModels = categoryModelList;
            adapter = new MyCategoriesAdapter( getContext(), categoryModelList );
            recycler_menu.setAdapter( adapter );
            recycler_menu.setLayoutAnimation( layoutAnimationController );
        } );

        return root;
    }

    private void initViews() {

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext( getContext() ).setCancelable( false ).build();
        dialog.show();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation( getContext(), R.anim.layout_item_from_left );
        LinearLayoutManager layoutManager = new LinearLayoutManager( getContext());
        recycler_menu.setLayoutManager( layoutManager );
        recycler_menu.addItemDecoration( new DividerItemDecoration(getContext(), layoutManager.getOrientation()) );

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_menu, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton( getContext(), "Update", 30, 0, Color.parseColor("#560027"),
                        pos -> {
                            Common.categorySelected = categoryModels.get( pos );
                            showUpdateDialog();
                        }));

            }
        };
    }

    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder( getContext() );
        builder.setTitle( "Update" );
        builder.setMessage( "Please fill information" );

        View itemView = LayoutInflater.from( getContext() ).inflate( R.layout.layout_update_category, null );
        EditText edt_category_name = (EditText)itemView.findViewById( R.id.edt_category_name );
        img_category = (ImageView)itemView.findViewById( R.id.img_category );

        //Set Data
        edt_category_name.setText( new StringBuilder( "" ).append( Common.categorySelected.getName() ) );
        Glide.with(getContext()).load( Common.categorySelected.getImage() ).into( img_category );

        //Set Event
        img_category.setOnClickListener( v -> {
            Intent intent = new Intent(  );
            intent.setType( "image/*" );
            intent.setAction( Intent.ACTION_GET_CONTENT );
            startActivityForResult( Intent.createChooser( intent, "Select Picture" ), PICK_IMAGE_REQUEST );
        } );

        builder.setNegativeButton( "CANCEL", (dialogInterface, which) -> {
            dialogInterface.dismiss();
        } );
        builder.setPositiveButton( "UPDATE", (dialogInterface, which) -> {
            Map<String, Object> updateData = new HashMap<>(  );
            updateData.put( "name", edt_category_name.getText().toString() );

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
                                updateData.put( "image", uri.toString() );
                                updateCategory( updateData );
                            } );

                        } ).addOnProgressListener( taskSnapshot ->{
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            dialog.setMessage( new StringBuilder( "Uploading: " ).append( progress ).append( "%" ) );
                } );

            }else{
                updateCategory(updateData);
            }
        } );

        builder.setView( itemView );
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateCategory(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child( Common.categorySelected.getMenu_id() )
                .updateChildren( updateData )
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText( getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
                    }
                } ).addOnCompleteListener( task -> {
                    categoryViewModel.loadCategories();
                    Toast.makeText(getContext() , "Update success", Toast.LENGTH_SHORT ).show();
                } );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            if(data != null && data.getData() != null){
                imageUri = data.getData();
                img_category.setImageURI( imageUri );
            }
        }
    }
}