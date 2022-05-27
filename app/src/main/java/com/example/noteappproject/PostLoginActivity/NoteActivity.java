package com.example.noteappproject.PostLoginActivity;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteappproject.CustomAdapter.RecyclerViewNoteCustomAdapter;
import com.example.noteappproject.Models.NoteItem;
import com.example.noteappproject.R;
import com.example.noteappproject.ReLoginActivity.RegisterUser;
import com.example.noteappproject.RoomDatabase.RoomDB;
import com.example.noteappproject.databinding.ActivityNoteBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private boolean isActivated;

    private ActivityNoteBinding binding;
    private String userID;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;

    private NoteItem selectedNote;

    private boolean passwordVisible;

    // RecyclerView
    private List<NoteItem> list_NoteItem;
    private RecyclerViewNoteCustomAdapter recyclerViewNoteCustomAdapter;

    private String idNote;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if ( FirebaseAuth.getInstance().getCurrentUser() == null ){
            // Chưa đăng nhập không cho dùng
            finish();
        }

        // Check xem kích hoạt chưa
        this.isActivated = user.isEmailVerified();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityNoteBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        // Open add note activity
        this.binding.imageAddNoteMain.setOnClickListener(v -> {
            Intent i = new Intent(NoteActivity.this, AddNoteActivity.class);
            startActivityForResult(i, 101);
        });

        InitializeNoteRecyclerView();
        DatabaseSetup();
        SetUpNoteRecyclerView();
        SearchViewInputText();
    }

    private void InitializeNoteRecyclerView() {
        this.list_NoteItem = new ArrayList<>();

        this.recyclerViewNoteCustomAdapter = new RecyclerViewNoteCustomAdapter(this, this.list_NoteItem, new RecyclerViewNoteCustomAdapter.IItemClick() {
            @Override
            public void onClick(NoteItem noteItem) {
                // Khi nhấn vào một item thì check nếu note đó có yêu cầu password thì hiện dialog để check pass.
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
                selectedNote = noteItem;
                showPopUp(cardView);
            }
        });
    }

    private void DatabaseSetup() {
        this.mAuth = FirebaseAuth.getInstance();
        String userEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        userEmail = RegisterUser.getSubEmailName(userEmail);
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userEmail).child("NoteItems");

        this.databaseReference.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                NoteItem noteItem = snapshot.getValue(NoteItem.class);
                if (noteItem != null) {
                    list_NoteItem.add(0, noteItem);
                    recyclerViewNoteCustomAdapter.notifyItemInserted(0);
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
                        recyclerViewNoteCustomAdapter.notifyItemChanged(i);
                        break;
                    }
                }
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
                        recyclerViewNoteCustomAdapter.notifyItemRemoved(i);
                        recyclerViewNoteCustomAdapter.notifyItemRangeChanged(i, list_NoteItem.size()- 1);
                        break;
                    }
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SetUpNoteRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.binding.recycleView.setLayoutManager(layoutManager);
        this.binding.recycleView.setHasFixedSize(true);
        this.binding.recycleView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.binding.recycleView.setAdapter(this.recyclerViewNoteCustomAdapter);
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

        for (NoteItem noteItem : list_NoteItem) {
            if (noteItem.getLabel().toLowerCase().contains(newText.toLowerCase()) ||
                    noteItem.getSubtitle().toLowerCase().contains(newText.toLowerCase()) ||
                    noteItem.getText_content().toLowerCase().contains(newText.toLowerCase())) {
                noteItemList.add(noteItem);
            }
        }

        this.recyclerViewNoteCustomAdapter.filter(noteItemList);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == AddNoteActivity.ADD_NOTE) {
                assert data != null;
                NoteItem new_notes = (NoteItem) data.getSerializableExtra("note");
                RoomDB.getInstance(this).mainDAO().insert(new_notes);
//                list_NoteItem.clear();
//                list_NoteItem.addAll(RoomDB.getInstance(this).mainDAO().getAll());
//                recyclerViewNoteCustomAdapter.notifyDataSetChanged();
                list_NoteItem.add(0, new_notes);
                recyclerViewNoteCustomAdapter.notifyItemInserted(0);
            }
        } else if (requestCode == 102) {
            if (resultCode == UpdateActivity.UPDATE_NOTE) {
                assert data != null;
                NoteItem new_notes = (NoteItem) data.getSerializableExtra("note");
                RoomDB.getInstance(this).mainDAO().update(new_notes.getID(), new_notes.getLabel(), new_notes.getSubtitle(), new_notes.getText_content(), new_notes.getDate(), new_notes.getColor(), new_notes.getImagePath(), new_notes.getWebLink());
                list_NoteItem.clear();
                list_NoteItem.addAll(RoomDB.getInstance(this).mainDAO().getAll());
                recyclerViewNoteCustomAdapter.notifyDataSetChanged();
            }
            else if (resultCode == UpdateActivity.SET_PASSWORD) {
                assert data != null;
                NoteItem new_notes = (NoteItem) data.getSerializableExtra("note");
                RoomDB.getInstance(this).mainDAO().updatePasswordNote(new_notes.getID(), new_notes.getPasswordNote());
                list_NoteItem.clear();
                list_NoteItem.addAll(RoomDB.getInstance(this).mainDAO().getAll());
                recyclerViewNoteCustomAdapter.notifyDataSetChanged();
            }
            else if (resultCode == UpdateActivity.REMOVE_PASSWORD) {
                assert data != null;
                NoteItem new_notes = (NoteItem) data.getSerializableExtra("note");
                RoomDB.getInstance(this).mainDAO().updatePasswordNote(new_notes.getID(), new_notes.getPasswordNote());
                list_NoteItem.clear();
                list_NoteItem.addAll(RoomDB.getInstance(this).mainDAO().getAll());
                recyclerViewNoteCustomAdapter.notifyDataSetChanged();
            }
        }
    }

    private void showPopUp(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.inflate(R.menu.my_menu_item_click);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
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
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String passwordValue = passwordNote.getText().toString();

                    if (passwordValue.equals(noteItem.getPasswordNote())) {
                        Intent intent = new Intent(NoteActivity.this, UpdateActivity.class);
                        intent.putExtra("noteItems", noteItem);
                        startActivityForResult(intent, 102);
                    } else {
                        Toast.makeText(NoteActivity.this, "Password incorrect", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {

                });
        AlertDialog dialog = builder.create();
        dialog.show();
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
                this.recyclerViewNoteCustomAdapter.notifyDataSetChanged();
                return true;
            case R.id.delete:
                onClickDeleteItem(selectedNote);
                RoomDB.getInstance(this).mainDAO().delete(selectedNote);
                list_NoteItem.remove(selectedNote);
                this.recyclerViewNoteCustomAdapter.notifyDataSetChanged();
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
            imageReference.delete().addOnSuccessListener(unused -> databaseReference.child(idNote).removeValue((error, ref) -> {

            }));
        }
        else {
            databaseReference.child(idNote).removeValue((error, ref) -> {

            });
        }
        if (noteItem.getVideoPath() != null && !noteItem.getVideoPath().trim().isEmpty()) {
            StorageReference videoReference = storage.getReferenceFromUrl(noteItem.getVideoPath());
            videoReference.delete().addOnSuccessListener(unused -> databaseReference.child(idNote).removeValue((error, ref) -> {

            }));
        }
        else {
            databaseReference.child(idNote).removeValue((error, ref) -> {

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
            // Chuyển layout Grid/List
            case R.id.optionMenuItem_SwitchLayoutMode:
                SwitchLayout(item);
                break;
            // Mở activity settings
            case R.id.settingBtn:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void SwitchLayout(MenuItem item) {
        if ( this.recyclerViewNoteCustomAdapter.getType() == RecyclerViewNoteCustomAdapter.TYPE_LIST_VIEW ){
            this.recyclerViewNoteCustomAdapter.setType(RecyclerViewNoteCustomAdapter.TYPE_GRID_VIEW);
            item.setIcon(R.drawable.ic_baseline_grid_off_24);

            GridLayoutManager gridLayoutManager = new GridLayoutManager(NoteActivity.this, 2);
            this.binding.recycleView.setLayoutManager(gridLayoutManager);
            this.recyclerViewNoteCustomAdapter.notifyDataSetChanged();
            return;
        }

        if ( this.recyclerViewNoteCustomAdapter.getType() == RecyclerViewNoteCustomAdapter.TYPE_GRID_VIEW ){
            this.recyclerViewNoteCustomAdapter.setType(RecyclerViewNoteCustomAdapter.TYPE_LIST_VIEW);
            item.setIcon(R.drawable.ic_baseline_grid_on_24);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(NoteActivity.this, LinearLayoutManager.VERTICAL,false);
            this.binding.recycleView.setLayoutManager(layoutManager);
            this.recyclerViewNoteCustomAdapter.notifyDataSetChanged();
            return;
        }
    }
}