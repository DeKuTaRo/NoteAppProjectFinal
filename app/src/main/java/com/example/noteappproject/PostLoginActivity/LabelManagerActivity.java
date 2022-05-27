package com.example.noteappproject.PostLoginActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteappproject.CustomAdapter.RecyclerViewLabelCustomAdapter;
import com.example.noteappproject.Models.NoteItem;
import com.example.noteappproject.Models.NoteLabel;
import com.example.noteappproject.ReLoginActivity.RegisterUser;
import com.example.noteappproject.databinding.ActivityLabelManagerBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LabelManagerActivity extends AppCompatActivity {

    private ActivityLabelManagerBinding binding;

    private List<NoteLabel> noteLabelList;
    private RecyclerViewLabelCustomAdapter recyclerViewLabelCustomAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityLabelManagerBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        InitializeFields();
        DatabaseSetup();
        SetUpNoteRecyclerView();
        SearchLabelInputText();
    }

    private void InitializeFields() {
        this.noteLabelList = new ArrayList<>();
        this.recyclerViewLabelCustomAdapter = new RecyclerViewLabelCustomAdapter(LabelManagerActivity.this, this.noteLabelList);
    }

    private void DatabaseSetup() {
        this.mAuth = FirebaseAuth.getInstance();
        String userEmail = RegisterUser.getSubEmailName(this.mAuth.getCurrentUser().getEmail());

        if (userEmail == null){
            FancyToast.makeText(LabelManagerActivity.this, "Error please login first", FancyToast.LENGTH_LONG, FancyToast.ERROR, false);
        }
        userEmail = RegisterUser.getSubEmailName(userEmail);

        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userEmail).child("NoteItems");

//        this.databaseReference.addChildEventListener(new ChildEventListener() {
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                NoteItem noteItem = snapshot.getValue(NoteItem.class);
//                if (noteItem != null) {
//                    list_NoteItem.add(0, noteItem);
//                    recyclerViewNoteCustomAdapter.notifyItemInserted(0);
//                }
//            }
//
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                NoteItem noteItem = snapshot.getValue(NoteItem.class);
//
//                if (noteItem == null || list_NoteItem == null || list_NoteItem.isEmpty()) {
//                    return;
//                }
//                for (int i = 0; i < list_NoteItem.size(); i++) {
//                    if (noteItem.getLabel().equals(list_NoteItem.get(i).getLabel())) {
//                        list_NoteItem.set(i, noteItem);
//                        recyclerViewNoteCustomAdapter.notifyItemChanged(i);
//                        break;
//                    }
//                }
//            }
//
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//                NoteItem noteItem = snapshot.getValue(NoteItem.class);
//                if (noteItem == null || list_NoteItem == null || list_NoteItem.isEmpty()) {
//                    return;
//                }
//                for (int i = 0; i < list_NoteItem.size(); i++) {
//                    if (noteItem.getLabel().equals(list_NoteItem.get(i).getLabel())) {
//                        list_NoteItem.remove(list_NoteItem.get(i));
//                        recyclerViewNoteCustomAdapter.notifyItemRemoved(i);
//                        recyclerViewNoteCustomAdapter.notifyItemRangeChanged(i, list_NoteItem.size()- 1);
//                        break;
//                    }
//                }
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    private void SetUpNoteRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.binding.recycleViewLabelList.setLayoutManager(layoutManager);
        this.binding.recycleViewLabelList.setHasFixedSize(true);
        this.binding.recycleViewLabelList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.binding.recycleViewLabelList.setAdapter(this.recyclerViewLabelCustomAdapter);
    }

    private void SearchLabelInputText() {
    }

}