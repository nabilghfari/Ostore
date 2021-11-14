package com.example.exapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.exapp.adapter.MyCartAdapter;
import com.example.exapp.eventbus.MyUpdateCartEvent;
import com.example.exapp.listener.ICartLoadListener;
import com.example.exapp.model.CartModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CartActivity extends AppCompatActivity implements ICartLoadListener {

    private Button purchasebtn;

    private FirebaseAnalytics firebaseAnalytics;

    @BindView(R.id.recycler_cart)
    RecyclerView recyclerCart;
    @BindView(R.id.mainLayout)
    RelativeLayout mainLayout;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTotal)
    TextView txtTotal;

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
        loadCartFromFirebase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        init();
        loadCartFromFirebase();

        //Initialize
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

        //Set Page
        bottomNavigationView.setSelectedItemId(R.id.cartact);

        //ItemSelectListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.storeact:
                        startActivity(new Intent(getApplicationContext(),NavigationActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.cartact:
                        return true;
                    case R.id.profact:
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        purchasebtn = (Button) findViewById(R.id.btnPurchase);
        purchasebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.METHOD, "User Purchase");
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle);
                startActivity(new Intent(CartActivity.this,PurchaseActivity.class));
            }
        });
    }

    private void loadCartFromFirebase() {
        List<CartModel> cartModels = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            purchasebtn.setVisibility(View.VISIBLE);
                            for(DataSnapshot cartSnapshot:snapshot.getChildren()){
                                CartModel cartModel = cartSnapshot.getValue(CartModel.class);
                                cartModel.setKey(cartSnapshot.getKey());
                                cartModels.add(cartModel);
                            }
                            cartLoadListener.onCartLoadSuccess(cartModels);
                        }
                        else {
                            cartLoadListener.onCartLoadFailed("Cart Empty");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        cartLoadListener.onCartLoadFailed(error.getMessage());
                    }
                });
    }

    private void init(){
        ButterKnife.bind(this);

        cartLoadListener = this;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerCart.setLayoutManager(layoutManager);
        recyclerCart.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        //btnBack.setOnClickListener(view -> finish());
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CartActivity.this,NavigationActivity.class));
            }
        });
    }

    @Override
    public void onCartLoadSuccess(List<CartModel> cartModelList) {
        double sum = 0;
        for(CartModel cartModel : cartModelList){
            sum+=cartModel.getTotalPrice();
        }
        txtTotal.setText(new StringBuilder("Rp.").append(sum));
        MyCartAdapter adapter = new MyCartAdapter(this, cartModelList);
        recyclerCart.setAdapter(adapter);
    }

    @Override
    public void onCartLoadFailed(String message) {
        Snackbar.make(mainLayout,message,Snackbar.LENGTH_LONG).show();
    }
}