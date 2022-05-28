package com.example.noteappproject.PostLoginActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteappproject.CustomAdapter.RecyclerViewLabelCustomAdapter;
import com.example.noteappproject.Models.NoteLabel;
import com.example.noteappproject.R;
import com.example.noteappproject.databinding.ActivityLabelManagerBinding;
import com.example.noteappproject.utilities.StringUlti;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LabelManagerActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityLabelManagerBinding binding;

    private List<NoteLabel> noteLabelList;
    private RecyclerViewLabelCustomAdapter recyclerViewLabelCustomAdapter;

    private DatabaseReference databaseReference;

    private int mCurrentItemPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityLabelManagerBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        InitializeFields();
        DatabaseSetup();
        SetOnClickEvent();
        SetUpNoteRecyclerView();
    }

    private void InitializeFields() {
        this.noteLabelList = new ArrayList<>();
        this.recyclerViewLabelCustomAdapter = new RecyclerViewLabelCustomAdapter(LabelManagerActivity.this, this.noteLabelList);
    }

    private void DatabaseSetup() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

        if (userEmail == null){
            Toast.makeText(LabelManagerActivity.this, "Error please login first", Toast.LENGTH_LONG).show();
            finish();
        }

        assert userEmail != null;
        userEmail = StringUlti.getSubEmailName(userEmail);
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userEmail).child("Label");

        this.databaseReference.get().addOnCompleteListener(task -> {
                    if ( !task.isSuccessful() ){
                        return;
                    }

                    String labelListFormat = Objects.requireNonNull(task.getResult().getValue()).toString();

                    String[] labelList = labelListFormat.split("\\|");

                    for (String label : labelList){
                        noteLabelList.add(new NoteLabel(label));
                    }
                });
    }

    private void SetOnClickEvent() {
        this.binding.imageBack.setOnClickListener(this);
        this.binding.imageViewClearNoteLabelEditText.setOnClickListener(this);
        this.binding.imageViewSaveLabel.setOnClickListener(this);
    }

    private void SetUpNoteRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.binding.recycleViewLabelList.setLayoutManager(layoutManager);
        this.binding.recycleViewLabelList.setHasFixedSize(true);
        this.binding.recycleViewLabelList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.binding.recycleViewLabelList.setAdapter(this.recyclerViewLabelCustomAdapter);

        registerForContextMenu(this.binding.recycleViewLabelList);

        // Context menu
        this.recyclerViewLabelCustomAdapter.setOnLongItemClickListener((v, position) -> {
            mCurrentItemPosition = position;
            v.showContextMenu();
        });
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.imageBack:
                finish();
                break;
            case R.id.imageView_ClearNoteLabelEditText:
                this.binding.editTextNoteLabel.setText("");
                break;
            case R.id.imageView_SaveLabel:
                saveLabel();
                break;
            default:
                break;
        }
    }

    private void saveLabel() {
        String label = this.binding.editTextNoteLabel.getText().toString();

        if (label.equals("")){
            Toast.makeText(LabelManagerActivity.this, "Label can't be empty ! Please enter an valid label !", Toast.LENGTH_SHORT).show();
            return;
        }

        this.noteLabelList.add(0, new NoteLabel(label));
        this.recyclerViewLabelCustomAdapter.notifyItemInserted(0);

        saveToFirebase();
    }

    private void saveToFirebase() {
        StringBuilder stringBuffer = new StringBuilder();
        for( NoteLabel noteLabel : this.noteLabelList ){
            stringBuffer.append(noteLabel.getLabelName());
            stringBuffer.append('|');
        }

        stringBuffer.deleteCharAt(stringBuffer.lastIndexOf("|"));
        String labelSaveToFirebase = stringBuffer.toString();

        this.databaseReference.setValue(labelSaveToFirebase);
    }


    // Create Context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
//        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
//        int eventPosition = adapterContextMenuInfo.position;
        getMenuInflater().inflate(R.menu.context_menu_label_operation, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.contextMenu_DeleteLabel:
                deleteLabel(mCurrentItemPosition);
                break;
            case R.id.contextMenu_EditLabel:
                editLabel(mCurrentItemPosition);
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void editLabel(int labelPosition) {

    }

    private void deleteLabel(int labelPosition) {
        AlertDialog.Builder alertDialog_Builder = new AlertDialog.Builder(this);

        alertDialog_Builder.setTitle("Confirm Remove Label !");
        alertDialog_Builder.setMessage("Are you sure you want to remove this label !");
        alertDialog_Builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            noteLabelList.remove(labelPosition);
            recyclerViewLabelCustomAdapter.notifyItemRemoved(labelPosition);
            recyclerViewLabelCustomAdapter.notifyItemRangeChanged(labelPosition, noteLabelList.size());
            saveToFirebase();
        });

        alertDialog_Builder.setNegativeButton("No", null);
        alertDialog_Builder.setCancelable(false);

        Dialog dialog = alertDialog_Builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}