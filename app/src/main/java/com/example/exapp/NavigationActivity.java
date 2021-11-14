package com.example.exapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.exapp.adapter.MyClotheAdapter;
import com.example.exapp.eventbus.MyUpdateCartEvent;
import com.example.exapp.listener.ICartLoadListener;
import com.example.exapp.listener.IClotheLoadListener;
import com.example.exapp.model.CartModel;
import com.example.exapp.model.ClotheModel;
import com.example.exapp.utils.SpaceItemDecoration;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nex3z.notificationbadge.NotificationBadge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavigationActivity extends AppCompatActivity implements IClotheLoadListener, ICartLoadListener {

    @BindView(R.id.recycler_clothes)
    RecyclerView recyclerClothes;
    @BindView(R.id.mainLayout)
    RelativeLayout mainLayout;
    @BindView(R.id.badge)
    NotificationBadge badge;
    @BindView(R.id.btnCart)
    FrameLayout btnCart;

    IClotheLoadListener clotheLoadListener;
    ICartLoadListener cartLoadListener;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if(EventBus.getDefault().hasSubscriberForEvent(MyUpdateCartEvent.class))
            EventBus.getDefault().removeStickyEvent(MyUpdateCartEvent.class);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onUpdateCart(MyUpdateCartEvent event){
        countCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        init();
        loadClotheFromFirebase();
        countCartItem();

        //Initialize
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

        //Set Page
        bottomNavigationView.setSelectedItemId(R.id.storeact);

        //ItemSelectListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.storeact:
                        return true;
                    case R.id.cartact:
                        startActivity(new Intent(getApplicationContext(),CartActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.profact:
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    private void loadClotheFromFirebase() {
        List<ClotheModel> clotheModels = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference("Clothes")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for(DataSnapshot clotheSnapshot:snapshot.getChildren()){
                                ClotheModel clotheModel = clotheSnapshot.getValue(ClotheModel.class);
                                clotheModel.setKey(clotheSnapshot.getKey());
                                clotheModels.add(clotheModel);
                            }
                            clotheLoadListener.onClotheLoadSuccess(clotheModels);
                        }
                        else{
                            clotheLoadListener.onClotheLoadFailed("Can't Find Clothe");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        clotheLoadListener.onClotheLoadFailed(error.getMessage());
                    }
                });
    }

    private void init() {
        ButterKnife.bind(this);
        clotheLoadListener = this;
        cartLoadListener = this;

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerClothes.setLayoutManager(gridLayoutManager);
        recyclerClothes.addItemDecoration(new SpaceItemDecoration());

        btnCart.setOnClickListener(view -> startActivity(new Intent(this,CartActivity.class)));
    }

    @Override
    public void onClotheLoadSuccess(List<ClotheModel> clotheModelList) {
        MyClotheAdapter adapter = new MyClotheAdapter(this,clotheModelList,cartLoadListener);
        recyclerClothes.setAdapter(adapter);
    }

    @Override
    public void onClotheLoadFailed(String message) {
        Snackbar.make(mainLayout,message,Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onCartLoadSuccess(List<CartModel> cartModelList) {
        int cartSum = 0;
        for(CartModel cartModel: cartModelList)
            cartSum += cartModel.getQuantity();
        badge.setNumber(cartSum);

    }

    @Override
    public void onCartLoadFailed(String message) {
        Snackbar.make(mainLayout,message,Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        countCartItem();
    }

    private void countCartItem() {
        List<CartModel> cartModels = new ArrayList<>();
        FirebaseDatabase
                .getInstance().getReference("Cart")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot cartSnapshot:snapshot.getChildren()){
                            CartModel cartModel = cartSnapshot.getValue(CartModel.class);
                            cartModel.setKey(cartSnapshot.getKey());
                            cartModels.add(cartModel);
                        }
                        cartLoadListener.onCartLoadSuccess(cartModels);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        cartLoadListener.onCartLoadFailed(error.getMessage());
                    }
                });
    }
}