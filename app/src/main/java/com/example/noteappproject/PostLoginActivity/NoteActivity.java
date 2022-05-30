package com.example.noteappproject.PostLoginActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import com.example.noteappproject.AdvancedFunction.TrashBinActivity;
import com.example.noteappproject.CustomAdapter.RecyclerViewNoteCustomAdapter;
import com.example.noteappproject.Models.NoteItem;
import com.example.noteappproject.R;
import com.example.noteappproject.RoomDatabase.RoomDB;
import com.example.noteappproject.databinding.ActivityNoteBinding;
import com.example.noteappproject.utilities.StringUlti;
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

import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class NoteActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{
    // KEY AND VALUE USE WHEN SENDING DATA THOUGHT INTENT
    public static final String KEY_REQUEST_NOTE_OPERATION = "REQUEST_NOTE_OPERATION";
    public static final int VALUE_REQUEST_ADD_NOTE = 101;
    public static final int VALUE_REQUEST_UPDATE_NOTE = 102;
    public static final int VALUE_REQUEST_DELETE_NOTE = 103;

    public static final String KEY_SENDING_NOTE_ITEM = "NOTE_ITEM";
    public static final String KEY_SENDING_POSITION_NOTE_ITEM = "POSITION_NOTE_ITEM";

    // Check is user account is activated
    private boolean isActivated;

    private ActivityNoteBinding binding;
    private DatabaseReference databaseReference;

    // Lưu trạng thái của item được long click
    private NoteItem selectedNote;
    private int selectedPosition;

    private boolean passwordVisible;
    private String userEmail;
    // RecyclerView
    private List<NoteItem> list_NoteItem;
    private RecyclerViewNoteCustomAdapter recyclerViewNoteCustomAdapter;

    private List<String> listLabel;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if ( user == null ){
            // Chưa đăng nhập không cho dùng

            // Create the object of
            // AlertDialog Builder class
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
            // Set Alert Title
            builder.setTitle("Authentication Error !");
            // Set the message show for the Alert time
            builder.setMessage("You need to login first ? Please close this app completely and open again !");
            // Set Cancelable false
            // for when the user clicks on the outside
            // the Dialog Box then it will remain show
            builder.setCancelable(false);

            // Set the positive button with yes name
            // OnClickListener method is use of
            // DialogInterface interface.
            builder.setPositiveButton( "Yes", (dialog, which) -> {
                // When the user click yes button
                // then app will close
                finish();
            });

            // Create the Alert dialog
            AlertDialog alertDialog = builder.create();

            // Show the Alert Dialog box
            alertDialog.show();
        }

        if (user != null) {
            this.isActivated = user.isEmailVerified();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityNoteBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        InitializeNoteRecyclerView();
        DatabaseSetup();
        SetOnClickEvent();
        SetUpNoteRecyclerView();
        SearchViewInputText();
        ShowEmptyView();
    }

    private void ShowEmptyView(){
        if (this.recyclerViewNoteCustomAdapter.getItemCount() == 0) {
            this.binding.linearLayoutEmptyView.setVisibility(View.VISIBLE);
            this.binding.recycleView.setVisibility(View.GONE);
        }else {
            this.binding.linearLayoutEmptyView.setVisibility(View.GONE);
            this.binding.recycleView.setVisibility(View.VISIBLE);
        }
    }

    private void InitializeNoteRecyclerView() {
        this.list_NoteItem = new ArrayList<>();

        this.recyclerViewNoteCustomAdapter = new RecyclerViewNoteCustomAdapter(this, this.list_NoteItem, new RecyclerViewNoteCustomAdapter.IItemClick() {
            @Override
            public void onClick(NoteItem noteItem, int position) {
                // Khi nhấn vào một item thì check nếu note đó có yêu cầu password thì hiện dialog để check pass.
                selectedPosition = position;
                if (noteItem.getPasswordNote().isEmpty()) {
                    // Truyền Note item cần update sang
                    Intent intent = new Intent(NoteActivity.this, UpdateActivity.class);
                    intent.putExtra(NoteActivity.KEY_SENDING_NOTE_ITEM, noteItem);
                    intent.putExtra(NoteActivity.KEY_REQUEST_NOTE_OPERATION, NoteActivity.VALUE_REQUEST_UPDATE_NOTE);
                    activityResultLauncher.launch(intent);
                } else {
                    // Nếu Item được yêu cầu mật khẩu thì mở dialog ra
                    showPasswordInputDialog(noteItem);
                }
            }

            @Override
            public void onLongClick(NoteItem noteItem, CardView cardView, int position) {
                selectedNote = noteItem;
                selectedPosition = position;
                showPopUp(cardView);
            }
        });
    }

    private void DatabaseSetup() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        }

        if (userEmail == null){
            return;
        }

        userEmail = StringUlti.getSubEmailName(userEmail);

        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userEmail);

        this.databaseReference.child("NoteItems").addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                NoteItem noteItem = snapshot.getValue(NoteItem.class);
                if (noteItem != null) {
                    list_NoteItem.add(0, noteItem);
                    recyclerViewNoteCustomAdapter.notifyItemInserted(0);
                    binding.recycleView.scrollToPosition(0);
                }
                ShowEmptyView();
                sortPinned();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                NoteItem noteItem = snapshot.getValue(NoteItem.class);

                if (noteItem == null || list_NoteItem == null || list_NoteItem.isEmpty()) {
                    return;
                }
//                for (int i = 0; i < list_NoteItem.size(); i++) {
//                    if (noteItem.getCreated_at() == list_NoteItem.get(i).getCreated_at() ) {
//                        list_NoteItem.set(i, noteItem);
//                        recyclerViewNoteCustomAdapter.notifyItemChanged(i);
//                        break;
//                    }
//                }

                list_NoteItem.set(selectedPosition, noteItem);
                recyclerViewNoteCustomAdapter.notifyItemChanged(selectedPosition);
                sortPinned();
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
                        recyclerViewNoteCustomAdapter.notifyItemRemoved(i);
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

        DatabaseReference databaseReferenceLabel = FirebaseDatabase.getInstance().getReference("Users").child(userEmail).child("Label");

        databaseReferenceLabel.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listLabel = new ArrayList<>();
                String labelListFormat = snapshot.getValue(String.class);
                String[] availableLabel = NoteActivity.this.getResources().getStringArray(R.array.available_label);

                if ( labelListFormat == null || labelListFormat.isEmpty()){
                    listLabel = new ArrayList<>(Arrays.asList(availableLabel));
                    return;
                }

                String[] labelListFromDatabase = labelListFormat.split("\\|");
                listLabel.addAll(new ArrayList<>(Arrays.asList(availableLabel)));
                listLabel.addAll(new ArrayList<>(Arrays.asList(labelListFromDatabase)));

                Log.e("TEST","LOAD LABEL HERE " + listLabel.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SetOnClickEvent() {
        // Open add note activity
        // Nếu chưa kích hoạt thì không cho phép tạo quá 5 note
        this.binding.imageAddNoteMain.setOnClickListener(v -> {
            if ( !this.isActivated && this.list_NoteItem.size() >= 5 ){

                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                builder.setTitle("Add Note Alert !");
                builder.setMessage("Please active your account to add more note ?");
                builder.setCancelable(false);

                builder.setPositiveButton( "Send activated mail", (dialog, which) -> Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).sendEmailVerification()
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(NoteActivity.this, "Please check your mail to active account !", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        }));
                builder.setNegativeButton("Later", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                Intent intent = new Intent(NoteActivity.this, AddNoteActivity.class);
                intent.putExtra(NoteActivity.KEY_REQUEST_NOTE_OPERATION, NoteActivity.VALUE_REQUEST_ADD_NOTE);
                activityResultLauncher.launch(intent);
            }
        });

        this.binding.imageTrashBin.setOnClickListener(v -> {
            Intent intent = new Intent(NoteActivity.this, TrashBinActivity.class);
            startActivity(intent);
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

        for (NoteItem noteItem : this.list_NoteItem) {
            if (noteItem.getLabel().toLowerCase().contains(newText.toLowerCase()) ||
                    noteItem.getSubtitle().toLowerCase().contains(newText.toLowerCase()) ||
                    noteItem.getText_content().toLowerCase().contains(newText.toLowerCase())) {
                noteItemList.add(noteItem);
            }
        }

        this.recyclerViewNoteCustomAdapter.filter(noteItemList);
    }

    // Receive result from edit or add activity
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if ( result.getResultCode() == Activity.RESULT_OK && result.getData() != null ){
                        Intent intentData = result.getData();

                        NoteItem new_notes = (NoteItem) intentData.getSerializableExtra(NoteActivity.KEY_SENDING_NOTE_ITEM);

                        Log.e("TEST", "Result: " + new_notes.toString());
                        if ( new_notes != null ){
                            int requestOperation = intentData.getIntExtra(NoteActivity.KEY_REQUEST_NOTE_OPERATION, -1);

                            Log.e("TEST", "requestOperation: " + requestOperation);

                            switch (requestOperation){
                                case NoteActivity.VALUE_REQUEST_ADD_NOTE:
                                    list_NoteItem.add(0, new_notes);
                                    recyclerViewNoteCustomAdapter.notifyItemInserted(0);
                                    ShowEmptyView();
                                    break;
                                case NoteActivity.VALUE_REQUEST_UPDATE_NOTE:
                                    int resultCode = intentData.getIntExtra(UpdateActivity.KEY_SENDING_RESULT_CODE, -1);
                                    updateNote(new_notes, resultCode);
                                    break;
                                default:
                                    break;

                            }
                        }
                    } else {
                        //Toast.makeText(NoteActivity.this, "Error: Can't receive result !", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @SuppressLint("NotifyDataSetChanged")
    private void updateNote(NoteItem new_notes, int resultCode) {
        if (resultCode == -1){
            RoomDB.getInstance(this)
                    .noteDAO()
                    .update(new_notes.getID(), new_notes.getLabel(), new_notes.getSubtitle()
                            , new_notes.getText_content(), new_notes.getDate()
                            , new_notes.getColor(), new_notes.getImagePath(), new_notes.getWebLink());
            this.recyclerViewNoteCustomAdapter.notifyDataSetChanged();
            return;
        }

        if (resultCode == UpdateActivity.REMOVE_PASSWORD || resultCode == UpdateActivity.SET_PASSWORD ){
            RoomDB.getInstance(this).noteDAO().updatePasswordNote(new_notes.getID(), new_notes.getPasswordNote());
            this.recyclerViewNoteCustomAdapter.notifyDataSetChanged();
            return;
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
                        intent.putExtra(NoteActivity.KEY_SENDING_NOTE_ITEM, noteItem);
                        intent.putExtra(NoteActivity.KEY_SENDING_POSITION_NOTE_ITEM, selectedPosition);
                        activityResultLauncher.launch(intent);
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
                selectedNote.setPinned(!selectedNote.isPinned());
                selectedNote.setPin_at(new Date().getTime());

                RoomDB.getInstance(this).noteDAO().pin(selectedNote.getID(), selectedNote.isPinned());

                Toast.makeText(NoteActivity.this, selectedNote.isPinned() ? "Pinned" : "Unpinned", Toast.LENGTH_SHORT).show();

                this.databaseReference.child("NoteItems").child(String.valueOf(selectedNote.getCreated_at())).setValue(selectedNote);
                return true;
            case R.id.delete:
                deleteNoteStorage(selectedNote);

                RoomDB.getInstance(this).noteDAO().deleteByCreatedAt(selectedNote.getCreated_at());

                this.list_NoteItem.remove(selectedNote);
                this.databaseReference.child("NoteItems").child(String.valueOf(selectedNote.getCreated_at())).removeValue();


                selectedNote.setDeleted_at(String.valueOf(new Date().getTime()));
                this.databaseReference.child("NoteItemsTrashBin").child(String.valueOf(selectedNote.getDeleted_at())).setValue(selectedNote);
                recyclerViewNoteCustomAdapter.notifyDataSetChanged();

                return true;

            case R.id.Add_Remove_Label:
                addRemoveLabel(selectedNote, selectedPosition);
                return true;
            case R.id.shareNote:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, selectedNote.toString());
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, "Note");
                startActivity(shareIntent);
            default:
                return false;
        }
    }

    private void sortPinned() {
        List<NoteItem> pinned = new ArrayList<>();
        List<NoteItem> unpinned = new ArrayList<>();

        for(NoteItem noteItem: this.list_NoteItem){
            if (noteItem.isPinned()) {
                pinned.add(noteItem);
            } else {
                unpinned.add(noteItem);
            }
        }

        Collections.sort(pinned, new Comparator<NoteItem>() {
            @Override
            public int compare(NoteItem noteItem1, NoteItem noteItem2) {
                if ( noteItem1.getPin_at() >= noteItem2.getPin_at() ){
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        Collections.sort(unpinned, new Comparator<NoteItem>() {
            @Override
            public int compare(NoteItem noteItem1, NoteItem noteItem2) {
                if ( noteItem1.getCreated_at() >= noteItem2.getCreated_at() ){
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        this.list_NoteItem.clear();
        this.list_NoteItem.addAll(pinned);
        this.list_NoteItem.addAll(unpinned);
        this.recyclerViewNoteCustomAdapter.notifyDataSetChanged();
    }

    private void addRemoveLabel(NoteItem selectedNote, int selectedPosition) {


        if ( this.listLabel != null ){
            // Lưu vị trí các Label đã check trước đó
            final boolean[] checkedItems = new boolean[listLabel.size()];
            // Mảng label đã check
            final String[] checkedLabel = selectedNote.getLabel().split("\\|");

            final List<String> selectedLabel = new ArrayList<>();

            final String[] currentListLabel = new String[listLabel.size()];

            int k = 0;
            for (String label : listLabel){
                currentListLabel[k] = label;
                k++;
            }

            // Lặp để kiểm tra các các label đã chọn
            if (checkedLabel.length != 0){
                for ( int i = 1; i < checkedLabel.length ; i++ ){
                    for ( int j = 0 ; j < this.listLabel.size() ; j++ ){
                        if ( checkedLabel[i].equals(currentListLabel[j]) ){
                            checkedItems[j] = true;
                        }
                    }
                    selectedLabel.add(checkedLabel[i]);
                }
            }

            // initialise the alert dialog builder
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);

            // set the title for the alert dialog
            builder.setTitle("Choose Note Label");

            // set the icon for the alert dialog
            builder.setIcon(R.drawable.ic_baseline_label_important_24);

            // now this is the function which sets the alert dialog for multiple item selection ready
            builder.setMultiChoiceItems(currentListLabel, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    selectedLabel.add(currentListLabel[which]);
                } else {
                    selectedLabel.remove(currentListLabel[which]);
                }
            });

            // alert dialog shouldn't be cancellable
            builder.setCancelable(false);

            // handle the positive button of the dialog
            builder.setPositiveButton("Save", (dialog, which) -> {
                if (checkedLabel.length == 0){
                    StringBuilder baseLabel = new StringBuilder(selectedNote.getLabel());

                    Log.e("TEST","SELECTED : " + Arrays.toString(selectedLabel.toArray()));

                    for (String label : selectedLabel) {
                        baseLabel.append("|").append(label);
                    }

                    selectedNote.setLabel(baseLabel.toString());
                } else {
                    StringBuilder baseLabel = new StringBuilder(checkedLabel[0]);

                    for (String label : selectedLabel) {
                        baseLabel.append("|").append(label);
                    }

                    selectedNote.setLabel(baseLabel.toString());
                }

                recyclerViewNoteCustomAdapter.notifyItemChanged(selectedPosition);

                long noteItemID = selectedNote.getCreated_at();

                databaseReference.child("NoteItems").child(String.valueOf(noteItemID)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        snapshot.getRef().child("label").setValue(selectedNote.getLabel());
                        Toast.makeText(NoteActivity.this, "Change label successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(NoteActivity.this, "Change label failed", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            // handle the negative button of the alert dialog
            builder.setNegativeButton("Later", (dialog, which) -> dialog.dismiss());

            // handle the neutral button of the dialog to clear
            // the selected items boolean checkedItem
            builder.setNeutralButton("Clear all", (dialog, which) -> Arrays.fill(checkedItems, false));

            // create the builder
            builder.create();

            // create the alert dialog with the
            // alert dialog builder instance
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            Toast.makeText(NoteActivity.this, "Can't manage note label ! Please try again !", Toast.LENGTH_SHORT).show();
        }


    }

    private void deleteNoteStorage(NoteItem noteItem) {
        String noteID = String.valueOf(noteItem.getCreated_at());

        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Maybe delete by ID then all relevant stuff is deleted too ???

        if (noteItem.getImagePath() != null && !noteItem.getImagePath().trim().isEmpty()) {
            StorageReference imageReference = storage.getReferenceFromUrl(noteItem.getImagePath());
            imageReference.delete().addOnSuccessListener(unused -> databaseReference.child("NoteItems").child(noteID).removeValue());
        }

        if (noteItem.getVideoPath() != null && !noteItem.getVideoPath().trim().isEmpty()) {
            StorageReference videoReference = storage.getReferenceFromUrl(noteItem.getVideoPath());
            videoReference.delete().addOnSuccessListener(unused -> databaseReference.child("NoteItems").child(noteID).removeValue());

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
            // Mở activity settings
            case R.id.profileBtn:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            // Mở activity settings
            case R.id.optionMenuItem_LabelManager:
                startActivity(new Intent(this, LabelManagerActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NotifyDataSetChanged")
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