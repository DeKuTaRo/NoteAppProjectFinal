package com.example.noteappproject.AdvancedFunction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteappproject.CustomAdapter.RecyclerViewNoteCustomAdapter;
import com.example.noteappproject.CustomAdapter.RecyclerViewTrashBinNoteCustomAdapter;
import com.example.noteappproject.Models.NoteItem;
import com.example.noteappproject.PostLoginActivity.LabelManagerActivity;
import com.example.noteappproject.PostLoginActivity.NoteActivity;
import com.example.noteappproject.PostLoginActivity.ProfileActivity;
import com.example.noteappproject.PostLoginActivity.SettingsActivity;
import com.example.noteappproject.PostLoginActivity.UpdateActivity;
import com.example.noteappproject.R;
import com.example.noteappproject.databinding.ActivityTrashBinBinding;
import com.example.noteappproject.utilities.StringUlti;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class TrashBinActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private ActivityTrashBinBinding binding;
    private RecyclerViewTrashBinNoteCustomAdapter recyclerViewTrashBinNoteCustomAdapter;
    private List<NoteItem> list_NoteItem;
    private NoteItem selectedNote;
    private int selectedPosition;

    private FirebaseAuth mAuth;
    private String userEmail;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityTrashBinBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        InitializeNoteRecyclerView();
        DatabaseSetup();
        SetUpNoteRecyclerView();
        SearchViewInputText();
    }

    private void DatabaseSetup() {
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        }

        if (userEmail == null){
            return;
        }

        userEmail = StringUlti.getSubEmailName(userEmail);

        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userEmail);

        this.databaseReference.child("NoteItemsTrashBin").addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                NoteItem noteItem = snapshot.getValue(NoteItem.class);
                if (noteItem != null) {
                    list_NoteItem.add(0, noteItem);
                    recyclerViewTrashBinNoteCustomAdapter.notifyItemInserted(0);
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                NoteItem noteItem = snapshot.getValue(NoteItem.class);

                if (noteItem == null || list_NoteItem == null || list_NoteItem.isEmpty()) {
                    return;
                }
                for (int i = 0; i < list_NoteItem.size(); i++) {
                    if (noteItem.getLabel().equals(list_NoteItem.get(i).getLabel())) {
                        list_NoteItem.set(i, noteItem);
                        recyclerViewTrashBinNoteCustomAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                NoteItem noteItem_Deleted = snapshot.getValue(NoteItem.class);
                if (noteItem_Deleted == null || list_NoteItem == null || list_NoteItem.isEmpty()) {
                    return;
                }
                for (int i = 0; i < list_NoteItem.size(); i++) {
                    if (noteItem_Deleted.getCreated_at() == (list_NoteItem.get(i).getCreated_at())) {
                        list_NoteItem.remove(list_NoteItem.get(i));
                        recyclerViewTrashBinNoteCustomAdapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                NoteItem noteItem_Deleted = snapshot.getValue(NoteItem.class);
//                if (noteItem_Deleted == null || list_NoteItem == null || list_NoteItem.isEmpty()) {
//                    return;
//                }
//                for (int i = 0; i < list_NoteItem.size(); i++) {
//                    if (noteItem_Deleted.getCreated_at() == list_NoteItem.get(i).getCreated_at()) {
//                        list_NoteItem.remove(list_NoteItem.get(i));
//                        recyclerViewTrashBinNoteCustomAdapter.notifyItemRemoved(i);
//                        break;
//                    }
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void InitializeNoteRecyclerView() {
        this.list_NoteItem = new ArrayList<>();

        this.recyclerViewTrashBinNoteCustomAdapter = new RecyclerViewTrashBinNoteCustomAdapter(this, this.list_NoteItem, new RecyclerViewTrashBinNoteCustomAdapter.IItemClick() {

            @Override
            public void onLongClick(NoteItem noteItem, CardView cardView, int position) {
                selectedNote = noteItem;
                selectedPosition = position;
                showPopUp(cardView);
            }
        });

    }

    private void showPopUp(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.inflate(R.menu.my_menu_item_trash_bin);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu_trash_bin, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Chuyển layout Grid/List
            case R.id.optionMenuItem_SwitchLayoutMode:
                SwitchLayout(item);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("NotifyDataSetChanged")
    private void SwitchLayout(MenuItem item) {
        if ( this.recyclerViewTrashBinNoteCustomAdapter.getType() == RecyclerViewTrashBinNoteCustomAdapter.TYPE_LIST_VIEW ){
            this.recyclerViewTrashBinNoteCustomAdapter.setType(RecyclerViewNoteCustomAdapter.TYPE_GRID_VIEW);
            item.setIcon(R.drawable.ic_baseline_grid_off_24);

            GridLayoutManager gridLayoutManager = new GridLayoutManager(TrashBinActivity.this, 2);
            this.binding.recycleView.setLayoutManager(gridLayoutManager);
            this.recyclerViewTrashBinNoteCustomAdapter.notifyDataSetChanged();
            return;
        }

        if ( this.recyclerViewTrashBinNoteCustomAdapter.getType() == RecyclerViewTrashBinNoteCustomAdapter.TYPE_GRID_VIEW ){
            this.recyclerViewTrashBinNoteCustomAdapter.setType(RecyclerViewNoteCustomAdapter.TYPE_LIST_VIEW);
            item.setIcon(R.drawable.ic_baseline_grid_on_24);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TrashBinActivity.this, LinearLayoutManager.VERTICAL,false);
            this.binding.recycleView.setLayoutManager(layoutManager);
            this.recyclerViewTrashBinNoteCustomAdapter.notifyDataSetChanged();
            return;
        }
    }

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.undo:
                undoNote(selectedNote);
                return true;
            case R.id.delete:
                deleteNote(selectedNote);
                return true;
        }
        return false;
    }

    private void deleteNote(NoteItem selectedNote) {
        final long created_at = selectedNote.getCreated_at();
        this.databaseReference.child("NoteItemsTrashBin").child(String.valueOf(created_at)).removeValue();
        Toast.makeText(this, "Delete successfully", Toast.LENGTH_SHORT).show();
    }

    private void undoNote(NoteItem selectedNote) {
        final long created_at = selectedNote.getCreated_at();
        this.databaseReference.child("NoteItems").child(String.valueOf(created_at)).setValue(selectedNote);
        this.databaseReference.child("NoteItemsTrashBin").child(String.valueOf(created_at)).removeValue();
        Toast.makeText(this, "Undo successfully", Toast.LENGTH_SHORT).show();
    }

    private void SetUpNoteRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.binding.recycleView.setLayoutManager(layoutManager);
        this.binding.recycleView.setHasFixedSize(true);
        this.binding.recycleView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.binding.recycleView.setAdapter(this.recyclerViewTrashBinNoteCustomAdapter);
    }

    private void SearchViewInputText() {
        this.binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNoteRecyclerView(newText);
                return true;
            }
        });
    }

    private void filterNoteRecyclerView(String newText) {
        List<NoteItem> noteItemList = new ArrayList<>();

        for (NoteItem noteItem : this.list_NoteItem) {
            if (noteItem.getLabel().toLowerCase().contains(newText.toLowerCase()) ||
                    noteItem.getSubtitle().toLowerCase().contains(newText.toLowerCase()) ||
                    noteItem.getText_content().toLowerCase().contains(newText.toLowerCase())) {
                noteItemList.add(noteItem);
            }
        }

        this.recyclerViewTrashBinNoteCustomAdapter.filter(noteItemList);
    }

}
