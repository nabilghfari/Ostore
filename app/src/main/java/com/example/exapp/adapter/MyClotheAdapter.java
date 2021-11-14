package com.example.exapp.adapter;

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
import com.example.exapp.listener.ICartLoadListener;
import com.example.exapp.listener.IRecyclerViewClickListener;
import com.example.exapp.model.CartModel;
import com.example.exapp.model.ClotheModel;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyClotheAdapter extends RecyclerView.Adapter<MyClotheAdapter.MyClotheViewHolder> {

    private Context context;
    private List<ClotheModel> clotheModelList;
    private ICartLoadListener iCartLoadListener;

    public MyClotheAdapter(Context context, List<ClotheModel> clotheModelList, ICartLoadListener iCartLoadListener) {
        this.context = context;
        this.clotheModelList = clotheModelList;
        this.iCartLoadListener = iCartLoadListener;
    }

    @NonNull
    @Override
    public MyClotheViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyClotheViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_clothe_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyClotheViewHolder holder, int position) {
        Glide.with(context)
                .load(clotheModelList.get(position).getImage())
                .into(holder.imageView);
        holder.txtPrice.setText(new StringBuilder("Rp.").append(clotheModelList.get(position).getPrice()));
        holder.txtName.setText(new StringBuilder().append(clotheModelList.get(position).getName()));

        holder.setListener((view, adapterPosition) -> {
            addToCart(clotheModelList.get(position));
        });
    }

    private void addToCart(ClotheModel clotheModel) {
        DatabaseReference userCart = FirebaseDatabase
                .getInstance()
                .getReference("Cart")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userCart.child(clotheModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            CartModel cartModel = snapshot.getValue(CartModel.class);
                            cartModel.setQuantity(cartModel.getQuantity()+1);
                            Map<String,Object> updateData = new HashMap<>();
                            updateData.put("quantity",cartModel.getQuantity());
                            updateData.put("totalPrice",cartModel.getQuantity()*Integer.parseInt(cartModel.getPrice()));
                            userCart.child(clotheModel.getKey())
                                    .updateChildren(updateData)
                                    .addOnSuccessListener(unused -> {
                                       iCartLoadListener.onCartLoadFailed("Add to Cart Success");
                                    })
                            .addOnFailureListener(e -> iCartLoadListener.onCartLoadFailed(e.getMessage()));
                        }
                        else{
                            CartModel cartModel = new CartModel();
                            cartModel.setName(clotheModel.getName());
                            cartModel.setImage(clotheModel.getImage());
                            cartModel.setKey(clotheModel.getKey());
                            cartModel.setPrice(clotheModel.getPrice());
                            cartModel.setQuantity(1);
                            cartModel.setTotalPrice(Integer.parseInt(clotheModel.getPrice()));

                            userCart.child(clotheModel.getKey())
                                    .setValue(cartModel)
                                    .addOnSuccessListener(unused -> {
                                iCartLoadListener.onCartLoadFailed("Add to Cart Success");
                            })
                                    .addOnFailureListener(e -> iCartLoadListener.onCartLoadFailed(e.getMessage()));

                        }
                        EventBus.getDefault().postSticky(new MyUpdateCartEvent());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        iCartLoadListener.onCartLoadFailed(error.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return clotheModelList.size();
    }

    public class MyClotheViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.imageView)
        ImageView imageView;
        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtPrice)
        TextView txtPrice;

        IRecyclerViewClickListener listener;

        public void setListener(IRecyclerViewClickListener listener) {
            this.listener = listener;
        }

        private Unbinder unbinder;
        public MyClotheViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onRecyclerClick(view, getAdapterPosition());
        }
    }
}
