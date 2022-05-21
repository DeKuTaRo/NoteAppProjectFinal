package com.example.noteappproject.AdvancedFunction;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noteappproject.databinding.ActivityTrashBinBinding;


public class TrashBinActivity extends AppCompatActivity {

    private ActivityTrashBinBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityTrashBinBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);
    }
}
