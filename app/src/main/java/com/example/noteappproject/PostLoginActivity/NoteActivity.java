package com.example.noteappproject.PostLoginActivity;

import  android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.noteappproject.CustomAdapter.CustomGridViewAdapter;
import com.example.noteappproject.CustomAdapter.CustomListViewAdapter;
import com.example.noteappproject.Models.NoteItem;
import com.example.noteappproject.R;
import com.example.noteappproject.RoomDatabase.RoomDB;
import com.example.noteappproject.databinding.ActivityNoteBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NoteActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private ActivityNoteBinding binding;
    private FirebaseAuth mAuth;
    private String userID;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;

    NoteItem selectedNote;

    private boolean passwordVisible;

    // RecyclerView
    private List<NoteItem> list_NoteItem;
    private CustomGridViewAdapter customGridViewAdapter;
    private CustomListViewAdapter customListViewAdapter;

    private String idNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityNoteBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        this.binding.imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NoteActivity.this, AddNoteActivity.class);
                startActivityForResult(i, 101);
            }
        });

//        InitializeListView();
        InitializeGridView();
        DatabaseSetup();
        SetupGridView();
        SearchViewInputText();
    }

    private void DatabaseSetup() {
        this.mAuth = FirebaseAuth.getInstance();
        this.userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("NoteItems");

        this.databaseReference.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                NoteItem noteItem = snapshot.getValue(NoteItem.class);
                if (noteItem != null) {
                    list_NoteItem.add(noteItem);
                    customGridViewAdapter.notifyDataSetChanged();
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
                        break;
                    }
                }
                customGridViewAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                NoteItem noteItem = snapshot.getValue(NoteItem.class);
                if (noteItem == null || list_NoteItem == null || list_NoteItem.isEmpty()) {
                    return;
                }
                for (int i = 0; i < list_NoteItem.size(); i++) {
                    if (noteItem.getLabel().equals(list_NoteItem.get(i).getLabel())) {
                        list_NoteItem.remove(list_NoteItem.get(i));
                        break;
                    }
                }
                customGridViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void SetUpListView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.binding.recycleView.setLayoutManager(layoutManager);
        this.binding.recycleView.setHasFixedSize(true);
        this.binding.recycleView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.binding.recycleView.setAdapter(this.customListViewAdapter);
    }

    private void SetupGridView() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL);
        this.binding.recycleView.setLayoutManager(layoutManager);
        this.binding.recycleView.setHasFixedSize(true);
        this.binding.recycleView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.binding.recycleView.setAdapter(this.customGridViewAdapter);
    }

    private void SearchViewInputText() {
        this.binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterGridView(newText);
                filterListView(newText);
                return true;
            }
        });
    }

    private void filterGridView(String newText) {
        List<NoteItem> noteItemList = new ArrayList<>();

        for (NoteItem noteItem : list_NoteItem) {
            if (noteItem.getLabel().toLowerCase().contains(newText.toLowerCase()) ||
                    noteItem.getSubtitle().toLowerCase().contains(newText.toLowerCase()) ||
                    noteItem.getText_content().toLowerCase().contains(newText.toLowerCase())) {
                noteItemList.add(noteItem);
            }
        }

        customGridViewAdapter.filterListInGridView(noteItemList);
    }

    private void filterListView(String newText) {
        List<NoteItem> noteItemList = new ArrayList<>();

        for (NoteItem noteItem : list_NoteItem) {
            if (noteItem.getLabel().toLowerCase().contains(newText.toLowerCase()) ||
                    noteItem.getSubtitle().toLowerCase().contains(newText.toLowerCase()) ||
                    noteItem.getText_content().toLowerCase().contains(newText.toLowerCase())) {
                noteItemList.add(noteItem);
            }
        }

        customListViewAdapter.filterListInListView(noteItemList);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == AddNoteActivity.ADD_NOTE) {
                NoteItem new_notes = (NoteItem) data.getSerializableExtra("note");
                RoomDB.getInstance(this).mainDAO().insert(new_notes);
                list_NoteItem.clear();
                list_NoteItem.addAll(RoomDB.getInstance(this).mainDAO().getAll());
                customGridViewAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == 102) {
            if (resultCode == UpdateActivity.UPDATE_NOTE) {
                NoteItem new_notes = (NoteItem) data.getSerializableExtra("note");
                RoomDB.getInstance(this).mainDAO().update(new_notes.getID(), new_notes.getLabel(), new_notes.getSubtitle(), new_notes.getText_content(), new_notes.getDate(), new_notes.getColor(), new_notes.getImagePath(), new_notes.getWebLink());
                list_NoteItem.clear();
                list_NoteItem.addAll(RoomDB.getInstance(this).mainDAO().getAll());
                customGridViewAdapter.notifyDataSetChanged();
            }
            else if (resultCode == UpdateActivity.SET_PASSWORD) {
                NoteItem new_notes = (NoteItem) data.getSerializableExtra("note");
                RoomDB.getInstance(this).mainDAO().updatePasswordNote(new_notes.getID(), new_notes.getPasswordNote());
                list_NoteItem.clear();
                list_NoteItem.addAll(RoomDB.getInstance(this).mainDAO().getAll());
                customGridViewAdapter.notifyDataSetChanged();
            }
            else if (resultCode == UpdateActivity.REMOVE_PASSWORD) {
                NoteItem new_notes = (NoteItem) data.getSerializableExtra("note");
                RoomDB.getInstance(this).mainDAO().updatePasswordNote(new_notes.getID(), new_notes.getPasswordNote());
                list_NoteItem.clear();
                list_NoteItem.addAll(RoomDB.getInstance(this).mainDAO().getAll());
                customGridViewAdapter.notifyDataSetChanged();
            }
        }
    }

    private void InitializeListView() {
        this.list_NoteItem = new ArrayList<>();
        this.customListViewAdapter = new CustomListViewAdapter(this, this.list_NoteItem, new CustomListViewAdapter.IItemClick() {

            @Override
            public void onClick(NoteItem noteItem) {
                if (noteItem.getPasswordNote().isEmpty()) {
                    Intent intent = new Intent(NoteActivity.this, UpdateActivity.class);
                    intent.putExtra("noteItems", noteItem);
                    startActivityForResult(intent, 102);
                } else {
                    showPasswordInputDialog(noteItem);
                }

            }

            @Override
            public void onLongClick(NoteItem noteItem, CardView cardView) {
                selectedNote = new NoteItem();
                selectedNote = noteItem;
                showPopUp(cardView);
            }
        });
    }

    private void InitializeGridView() {
        this.list_NoteItem = new ArrayList<>();
        this.customGridViewAdapter = new CustomGridViewAdapter(this, this.list_NoteItem, new CustomGridViewAdapter.IItemClick() {

            @Override
            public void onClick(NoteItem noteItem) {
                if (noteItem.getPasswordNote().isEmpty()) {
                    Intent intent = new Intent(NoteActivity.this, UpdateActivity.class);
                    intent.putExtra("noteItems", noteItem);
                    startActivityForResult(intent, 102);
                } else {
                    showPasswordInputDialog(noteItem);
                }

            }

            @Override
            public void onLongClick(NoteItem noteItem, CardView cardView) {
                selectedNote = new NoteItem();
                selectedNote = noteItem;
                showPopUp(cardView);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showPasswordInputDialog(NoteItem noteItem) {

        this.mAuth = FirebaseAuth.getInstance();
        this.userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("NoteItems");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object value = snapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final EditText passwordNote = new EditText(this);
        passwordNote.setHint("Enter password here");
        passwordNote.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordNote.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);

        passwordNote.setOnTouchListener((View view, @SuppressLint("ClickableViewAccessibility") MotionEvent motionEvent) -> {
            final int Right = 2;
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (motionEvent.getRawX() >= passwordNote.getRight() - passwordNote.getCompoundDrawables()[Right].getBounds().width()) {
                    int selection = passwordNote.getSelectionEnd();
                    if (passwordVisible) {
                        passwordNote.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);
                        passwordNote.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        passwordVisible = false;
                    } else {
                        passwordNote.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0);
                        passwordNote.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        passwordVisible = true;
                    }
                    passwordNote.setSelection(selection);
                    return true;
                }
            }
            return false;
        });

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        ll.addView(passwordNote);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Please enter your password")
                .setView(ll)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String passwordValue = passwordNote.getText().toString();

                        if (passwordValue.equals(noteItem.getPasswordNote())) {
                            Intent intent = new Intent(NoteActivity.this, UpdateActivity.class);
                            intent.putExtra("noteItems", noteItem);
                            startActivityForResult(intent, 102);
                        } else {
                            Toast.makeText(NoteActivity.this, "Password incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showPopUp(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.inflate(R.menu.my_menu_item_click);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.pin :
                if (selectedNote.isPinned()) {
                    selectedNote.setPinned(false);
                    RoomDB.getInstance(this).mainDAO().pin(selectedNote.getID(), false);
                    Toast.makeText(NoteActivity.this, "Unpinned", Toast.LENGTH_SHORT).show();
                } else {
                    selectedNote.setPinned(true);
                    RoomDB.getInstance(this).mainDAO().pin(selectedNote.getID(), true);
                    Toast.makeText(NoteActivity.this, "Pinned", Toast.LENGTH_SHORT).show();
                }
                customGridViewAdapter.notifyDataSetChanged();
                return true;
            case R.id.delete:
                onClickDeleteItem(selectedNote);
                RoomDB.getInstance(this).mainDAO().delete(selectedNote);
                list_NoteItem.remove(selectedNote);
                customGridViewAdapter.notifyDataSetChanged();
                return true;
            default:
                return false;
        }
    }

    private void onClickDeleteItem(NoteItem noteItem) {

        idNote = noteItem.getLabel();

        userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("NoteItems");

        storage = FirebaseStorage.getInstance();

        if (noteItem.getImagePath() != null && !noteItem.getImagePath().trim().isEmpty()) {
            StorageReference imageReference = storage.getReferenceFromUrl(noteItem.getImagePath());
            imageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    databaseReference.child(idNote).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                        }
                    });
                }
            });
        }
        else {
            databaseReference.child(idNote).removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                }
            });
        }
         if (noteItem.getVideoPath() != null && !noteItem.getVideoPath().trim().isEmpty()) {
            StorageReference videoReference = storage.getReferenceFromUrl(noteItem.getVideoPath());
            videoReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    databaseReference.child(idNote).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                        }
                    });
                }
            });
        }
         else {
             databaseReference.child(idNote).removeValue(new DatabaseReference.CompletionListener() {
                 @Override
                 public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                 }
             });
         }

        Toast.makeText(NoteActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.listViewShow :
                SetUpListView();
                break;
            case R.id.gridViewShow:
                SetupGridView();
                break;
            case R.id.settingBtn:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

}