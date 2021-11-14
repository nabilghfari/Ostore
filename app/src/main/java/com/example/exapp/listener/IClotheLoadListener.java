package com.example.exapp.listener;

import com.example.exapp.model.ClotheModel;

import java.util.List;

public interface IClotheLoadListener {
    void onClotheLoadSuccess(List<ClotheModel> clotheModelList);
    void onClotheLoadFailed(String message);
}
