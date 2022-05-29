package com.example.noteappproject.PostLoginActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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
        ShowEmptyView();
    }

    private void ShowEmptyView(){
        if (this.recyclerViewLabelCustomAdapter.getItemCount() == 0) {
            this.binding.linearLayoutEmptyLabel.setVisibility(View.VISIBLE);
            this.binding.recycleViewLabelList.setVisibility(View.GONE);
        }else {
            this.binding.linearLayoutEmptyLabel.setVisibility(View.GONE);
            this.binding.recycleViewLabelList.setVisibility(View.VISIBLE);
        }
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

                    recyclerViewLabelCustomAdapter.notifyDataSetChanged();
                    ShowEmptyView();
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
        final EditText editLabel = new EditText(this);
        editLabel.setHint(this.noteLabelList.get(labelPosition).getLabelName());

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        ll.addView(editLabel);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Edit note label")
                .setView(ll)
                .setPositiveButton("Confirm", (dialogInterface, i) -> {
                    String newLabel = editLabel.getText().toString().trim();

                    if (newLabel.equals("")){
                        editLabel.setError("Label can't be empty !");
                        editLabel.requestFocus();
                        return;
                    }

                    noteLabelList.get(labelPosition).setLabelName(newLabel);
                    recyclerViewLabelCustomAdapter.notifyItemChanged(labelPosition);
                    saveToFirebase();
                    dialogInterface.dismiss();
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
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
            ShowEmptyView();
            dialogInterface.dismiss();
        });

        alertDialog_Builder.setNegativeButton("No", null);
        alertDialog_Builder.setCancelable(false);

        Dialog dialog = alertDialog_Builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.label_manager_option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Set on option item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.optionMenu_itemRemoveAll:
                optionMenuItem_RemoveAll();
                break;
            case R.id.optionMenu_itemRemoveSelected:
                optionMenuItem_RemoveSelected();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void optionMenuItem_RemoveSelected() {
        AlertDialog.Builder alertDialog_Builder = new AlertDialog.Builder(this);

        alertDialog_Builder.setTitle("Confirm Remove Selected !");
        alertDialog_Builder.setMessage("Are you sure you want to remove selected label !");
        alertDialog_Builder.setPositiveButton("Yes", (dialogInterface, index) -> {

            ArrayList<Integer> indexRemoved = new ArrayList<>();
            int count = 0;
            for ( int i = 0 ; i < noteLabelList.size() ; i++ ){
                if ( noteLabelList.get(i).isCheck() ){
                    indexRemoved.add(i);
                    noteLabelList.remove(i);
                    i--;
                    count++;
                }
            }
            if ( count == 0 ){
                Toast.makeText(LabelManagerActivity.this,"There is no selected item Can't remove !!!", Toast.LENGTH_SHORT).show();
            } else {
                for (Integer integer : indexRemoved) {
                    recyclerViewLabelCustomAdapter.notifyItemRemoved(integer);
                    recyclerViewLabelCustomAdapter.notifyItemRangeChanged(integer, noteLabelList.size() - integer);
                }
                ShowEmptyView();
                Toast.makeText(LabelManagerActivity.this,"Remove selected label successfully !", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog_Builder.setNegativeButton("No", null);
        alertDialog_Builder.setCancelable(false);

        Dialog dialog = alertDialog_Builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void optionMenuItem_RemoveAll() {

        AlertDialog.Builder alertDialog_Builder = new AlertDialog.Builder(this);

        alertDialog_Builder.setTitle("Confirm Remove All !");
        alertDialog_Builder.setMessage("Are you sure you want to remove all label !");
        alertDialog_Builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            this.noteLabelList.clear();
            this.recyclerViewLabelCustomAdapter.notifyDataSetChanged();
            ShowEmptyView();
            Toast.makeText(LabelManagerActivity.this,"Remove all note label successfully !", Toast.LENGTH_SHORT).show();
        });

        alertDialog_Builder.setNegativeButton("No", null);
        alertDialog_Builder.setCancelable(false);

        Dialog dialog = alertDialog_Builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}