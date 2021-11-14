package com.example.exapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.exapp.R;
import com.example.exapp.eventbus.MyUpdateCartEvent;
import com.example.exapp.model.CartModel;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MyCartViewHolder> {

    private Context context;
    private List<CartModel> cartModelList;

    public MyCartAdapter(Context context, List<CartModel> cartModelList) {
        this.context = context;
        this.cartModelList = cartModelList;
    }

    @NonNull
    @Override
    public MyCartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyCartViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_cart_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyCartViewHolder holder, int position) {
        Glide.with(context)
                .load(cartModelList.get(position).getImage())
                .into(holder.imageView);
        holder.txtPrice.setText(new StringBuilder("Rp.").append(cartModelList.get(position).getPrice()));
        holder.txtName.setText(new StringBuilder().append(cartModelList.get(position).getName()));
        holder.txtQuantity.setText(new StringBuilder().append(cartModelList.get(position).getQuantity()));
        
        //Event
        holder.btnMinus.setOnClickListener(view -> {
            minusCartItem(holder,cartModelList.get(position));
        });
        holder.btnPlus.setOnClickListener(view -> {
            plusCartItem(holder,cartModelList.get(position));
        });
        holder.btnDelete.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(context,R.style.MyDialogTheme)
                    .setTitle("Delete Item")
                    .setMessage("Do you sure want to delete item?")
                    .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss())
                    .setPositiveButton("SURE", (dialogInterface, i) -> {
                        //Temp remove
                        notifyItemRemoved(position);
                        deleteFromFirebase(cartModelList.get(position));
                        dialogInterface.dismiss();
                    }).create();
            dialog.show();
        });
    }

    private void deleteFromFirebase(CartModel cartModel) {
        FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(cartModel.getKey())
                .removeValue()
                .addOnSuccessListener(unused -> EventBus.getDefault().postSticky(new MyUpdateCartEvent()));
    }

    private void plusCartItem(MyCartViewHolder holder, CartModel cartModel) {
        cartModel.setQuantity(cartModel.getQuantity()+1);
        cartModel.setTotalPrice(cartModel.getQuantity()*Integer.parseInt(cartModel.getPrice()));

        holder.txtQuantity.setText(new StringBuilder().append(cartModel.getQuantity()));
        updateFirebase(cartModel);
    }

    private void minusCartItem(MyCartViewHolder holder, CartModel cartModel) {
        if(cartModel.getQuantity()>1){
            cartModel.setQuantity(cartModel.getQuantity()-1);
            cartModel.setTotalPrice(cartModel.getQuantity()*Integer.parseInt(cartModel.getPrice()));
            
            //update quantity
            holder.txtQuantity.setText(new StringBuilder().append(cartModel.getQuantity()));
            updateFirebase(cartModel);
        }
            
    }

    private void updateFirebase(CartModel cartModel) {
        FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(cartModel.getKey())
                .setValue(cartModel)
                .addOnSuccessListener(unused -> EventBus.getDefault().postSticky(new MyUpdateCartEvent()));
    }

    @Override
    public int getItemCount() {
        return cartModelList.size();
    }

    public class MyCartViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.btnMinus)
        ImageView btnMinus;
        @BindView(R.id.btnPlus)
        ImageView btnPlus;
        @BindView(R.id.btnDelete)
        ImageView btnDelete;
        @BindView(R.id.imageView)
        ImageView imageView;
        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtPrice)
        TextView txtPrice;
        @BindView(R.id.txtQuantity)
        TextView txtQuantity;

        Unbinder unbinder;


        public MyCartViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
