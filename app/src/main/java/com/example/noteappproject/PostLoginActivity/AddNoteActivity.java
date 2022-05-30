package com.example.noteappproject.PostLoginActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.noteappproject.Models.NoteItem;
import com.example.noteappproject.Models.Settings;
import com.example.noteappproject.R;
import com.example.noteappproject.RoomDatabase.RoomDB;
import com.example.noteappproject.databinding.ActivityAddNoteBinding;
import com.example.noteappproject.utilities.StringUlti;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AddNoteActivity extends AppCompatActivity implements OnClickListener{
    private EditText label, subtitle, textContent;
    private ImageView imageBack, imageSave, imageNote;
    private TextView textDateTime;
    private View viewSubtitleIndicator;
    private TextView textWebURL;
    private LinearLayout layoutWebURL;
    private AlertDialog dialogURL;
    private VideoView videoView;
    private String selectedNoteColor, fontSizeDB, fontStyleDB;
    private LinearLayout layoutDeleteVideo;
    private Uri imageUri, videoUri;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 2;
    private static final int REQUEST_CODE_STORAGE_VIDEO_PERMISSION = 3;

    private static final int REQUEST_CODE_SELECT_IMAGE = 4;
    private static final int REQUEST_CODE_SELECT_VIDEO = 5;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageTask mUploadTask;

    private String imageUriTask, videoUriTask;


    private ActivityAddNoteBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        this.mAuth = FirebaseAuth.getInstance();

        bindingView();
        setupDatabase();
        setOnClickEvent();
        initMiscellaneous();
        setSubtitleIndicator();
    }


    private void bindingView() {
        label = this.binding.label;
        subtitle = this.binding.subtitle;
        textContent = this.binding.textContent;

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd/MM/yyyy HH:mm a");
        Date date = new Date();
        textDateTime = this.binding.textDateTime;
        textDateTime.setText(formatter.format(date));

        viewSubtitleIndicator = this.binding.viewSubtitleIndicator;

        textWebURL = this.binding.textWebURL;
        layoutWebURL = this.binding.layoutWebURL;
        layoutDeleteVideo = this.binding.layoutDeleteVideo;

        imageNote = this.binding.imageNote;
        imageBack = this.binding.imageBack;

        imageSave = this.binding.imageSave;
        videoView = this.binding.videoView;

        selectedNoteColor = "#333333";

    }

    private void setupDatabase() {
        final String userEmail = StringUlti.getSubEmailName(Objects.requireNonNull(Objects.requireNonNull(this.mAuth.getCurrentUser()).getEmail()));
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userEmail).child("NoteItems");
    }


    private void setOnClickEvent() {
        label.setOnClickListener(this);

        imageBack.setOnClickListener(this);

        imageSave.setOnClickListener(this);

        videoView.setOnPreparedListener(mp -> mp.setLooping(true));

        // Event khi nhấn vào remove hình, webURL, video...
        this.binding.imageRemoveWebURL.setOnClickListener(v -> {
            textWebURL.setVisibility(View.GONE);
            layoutWebURL.setVisibility(View.GONE);
        });

        this.binding.imageRemoveImage.setOnClickListener(v -> {
            Picasso.get().load((Uri) null).into(imageNote);
            imageNote.setVisibility(View.GONE);
            this.binding.imageRemoveImage.setVisibility(View.GONE);
        });

        this.binding.imageRemoveVideo.setOnClickListener(v -> {
            videoView.setVideoURI(null);
            videoView.setVisibility(View.GONE);
            layoutDeleteVideo.setVisibility(View.GONE);
            v.setVisibility(View.GONE);
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageBack:
                finish();
                break;
            case R.id.imageSave:
                sendDataToDatabase();
                break;
        }
    }

    private void sendDataToDatabase() {
        String labelValue = label.getText().toString().trim();
        String subtitleValue = subtitle.getText().toString().trim();
        String textContentValue = textContent.getText().toString().trim();
        String dateTimeValue = textDateTime.getText().toString().trim();

        if (labelValue.isEmpty()) {
            label.setError("Label must not be empty");
            label.requestFocus();
            return;
        }

        final String userEmail = StringUlti.getSubEmailName(Objects.requireNonNull(Objects.requireNonNull(this.mAuth.getCurrentUser()).getEmail()));
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        databaseReference = rootNode.getReference("Users").child(userEmail);
        this.databaseReference.child("Settings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Settings settings = snapshot.getValue(Settings.class);

                if (settings != null) {
                    fontSizeDB = settings.getFontSize();
                    fontStyleDB = settings.getFontStyle();
                }

                switch (fontSizeDB) {
                    case "Small":
                        textContent.setTextSize(10);
                        break;
                    case "Medium":
                        textContent.setTextSize(15);
                        break;
                    case "Big":
                        textContent.setTextSize(20);
                        break;
                    case "Very Big":
                        textContent.setTextSize(25);
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddNoteActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });


        NoteItem noteItem = new NoteItem();

        // Nếu có thêm web URL
        if (layoutWebURL.getVisibility() == View.VISIBLE) {
            noteItem.setWebLink(textWebURL.getText().toString());
        }

        // Không up hình lẫn video
        if ( (imageNote.getDrawable() == null || imageNote.getVisibility() == View.GONE ) && videoView.getVisibility() == View.GONE ) {
            noteItem.setImagePath("");
            noteItem.setVideoPath("");

            noteItem.setLabel(labelValue);
            noteItem.setSubtitle(subtitleValue);
            noteItem.setText_content(textContentValue);
            noteItem.setDate(dateTimeValue);
            noteItem.setColor(selectedNoteColor);
            noteItem.setPasswordNote("");

            // Lấy current time làm ID
            long created_at = System.currentTimeMillis();
            noteItem.setCreated_at(created_at);

            final String currentTimeStamp = String.valueOf(created_at);
            databaseReference.child("NoteItems").child(currentTimeStamp).setValue(noteItem);

            RoomDB.getInstance(AddNoteActivity.this).noteDAO().insert(noteItem);
            Toast.makeText(AddNoteActivity.this, "Add note successful", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.putExtra(NoteActivity.KEY_SENDING_NOTE_ITEM, noteItem);
            intent.putExtra(NoteActivity.KEY_REQUEST_NOTE_OPERATION, NoteActivity.VALUE_REQUEST_ADD_NOTE);

            finish();
        }

        // Up hình và video
        if ( (imageNote.getDrawable() != null && imageNote.getVisibility() != View.GONE ) && videoView.getVisibility() != View.GONE ){
            // Up Uri hình lên firebase
            StorageReference storageImageReference = FirebaseStorage.getInstance().getReference("images");

            StorageReference imageReference = storageImageReference.child(System.currentTimeMillis() +
                    "." + getFileExtension(imageUri));

            imageReference.putFile(imageUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                return imageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    imageUriTask = task.getResult().toString();
                    noteItem.setImagePath(imageUriTask);

                    // Upload videoUri lên firebase
                    StorageReference storageVideoReference = FirebaseStorage.getInstance().getReference("videos");

                    StorageReference videoReference = storageVideoReference.child(System.currentTimeMillis() +
                            "." + getFileExtension(videoUri));

                    videoReference.putFile(videoUri).continueWithTask(task1 -> {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }

                        return videoReference.getDownloadUrl();
                    }).addOnCompleteListener(task1 -> {
                        if (task.isSuccessful()) {
                            videoUriTask = task.getResult().toString();
                            noteItem.setVideoPath(videoUriTask);

                            noteItem.setLabel(labelValue);
                            noteItem.setSubtitle(subtitleValue);
                            noteItem.setText_content(textContentValue);
                            noteItem.setDate(dateTimeValue);
                            noteItem.setColor(selectedNoteColor);
                            noteItem.setPasswordNote("");

                            // Lấy current time làm ID
                            long created_at = System.currentTimeMillis();
                            noteItem.setCreated_at(created_at);

                            final String currentTimeStamp = String.valueOf(created_at);
                            databaseReference.child("NoteItems").child(currentTimeStamp).setValue(noteItem);

                            RoomDB.getInstance(AddNoteActivity.this).noteDAO().insert(noteItem);
                            Toast.makeText(AddNoteActivity.this, "Add note successful", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            intent.putExtra(NoteActivity.KEY_SENDING_NOTE_ITEM, noteItem);
                            intent.putExtra(NoteActivity.KEY_REQUEST_NOTE_OPERATION, NoteActivity.VALUE_REQUEST_ADD_NOTE);

                            finish();
                        }
                    });
                }
            });
        }


        // Up hình không video
        if ( (imageNote.getDrawable() != null && imageNote.getVisibility() != View.GONE ) && videoView.getVisibility() == View.GONE ){
            StorageReference storageImageReference = FirebaseStorage.getInstance().getReference("images");

            StorageReference imageReference = storageImageReference.child(System.currentTimeMillis() +
                    "." + getFileExtension(imageUri));

            imageReference.putFile(imageUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                return imageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    imageUriTask = task.getResult().toString();
                    noteItem.setImagePath(imageUriTask);
                    noteItem.setVideoPath("");

                    noteItem.setLabel(labelValue);
                    noteItem.setSubtitle(subtitleValue);
                    noteItem.setText_content(textContentValue);
                    noteItem.setDate(dateTimeValue);
                    noteItem.setColor(selectedNoteColor);
                    noteItem.setPasswordNote("");

                    // Lấy current time làm ID
                    long created_at = System.currentTimeMillis();
                    noteItem.setCreated_at(created_at);

                    final String currentTimeStamp = String.valueOf(created_at);
                    databaseReference.child("NoteItems").child(currentTimeStamp).setValue(noteItem);

                    RoomDB.getInstance(AddNoteActivity.this).noteDAO().insert(noteItem);
                    Toast.makeText(AddNoteActivity.this, "Add note successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent();
                    intent.putExtra(NoteActivity.KEY_SENDING_NOTE_ITEM, noteItem);
                    intent.putExtra(NoteActivity.KEY_REQUEST_NOTE_OPERATION, NoteActivity.VALUE_REQUEST_ADD_NOTE);

                    finish();
                }
            });
        }

        // Up video không up hình
        if ( (imageNote.getDrawable() == null || imageNote.getVisibility() == View.GONE ) && videoView.getVisibility() != View.GONE ){
            // Upload videoUri lên firebase
            StorageReference storageVideoReference = FirebaseStorage.getInstance().getReference("videos");

            StorageReference videoReference = storageVideoReference.child(System.currentTimeMillis() +
                    "." + getFileExtension(videoUri));

            videoReference.putFile(videoUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                return videoReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    videoUriTask = task.getResult().toString();
                    noteItem.setVideoPath(videoUriTask);
                    noteItem.setImagePath("");

                    noteItem.setLabel(labelValue);
                    noteItem.setSubtitle(subtitleValue);
                    noteItem.setText_content(textContentValue);
                    noteItem.setDate(dateTimeValue);
                    noteItem.setColor(selectedNoteColor);
                    noteItem.setPasswordNote("");

                    // Lấy current time làm ID
                    long created_at = System.currentTimeMillis();
                    noteItem.setCreated_at(created_at);

                    final String currentTimeStamp = String.valueOf(created_at);
                    databaseReference.child("NoteItems").child(currentTimeStamp).setValue(noteItem);

                    RoomDB.getInstance(AddNoteActivity.this).noteDAO().insert(noteItem);
                    Toast.makeText(AddNoteActivity.this, "Add note successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent();
                    intent.putExtra(NoteActivity.KEY_SENDING_NOTE_ITEM, noteItem);
                    intent.putExtra(NoteActivity.KEY_REQUEST_NOTE_OPERATION, NoteActivity.VALUE_REQUEST_ADD_NOTE);

                    finish();
                }
            });
        }
    }

    private void setViewColor(String selectedNoteColor, int indexImageResource,  ImageView[] imageColors){
        this.selectedNoteColor = selectedNoteColor;

        for ( int i = 0 ; i < imageColors.length; i++ ){
            if ( i == indexImageResource ){
                imageColors[i].setImageResource(R.drawable.ic_check);
            } else {
                imageColors[i].setImageResource(0);
            }
        }

        setSubtitleIndicator();
    }

    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);

        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        final ImageView imageColor1 = layoutMiscellaneous.findViewById(R.id.imageColor1);
        final ImageView imageColor2 = layoutMiscellaneous.findViewById(R.id.imageColor2);
        final ImageView imageColor3 = layoutMiscellaneous.findViewById(R.id.imageColor3);
        final ImageView imageColor4 = layoutMiscellaneous.findViewById(R.id.imageColor4);
        final ImageView imageColor5 = layoutMiscellaneous.findViewById(R.id.imageColor5);

        ImageView[] imageColors = { imageColor1, imageColor2, imageColor3, imageColor4, imageColor5};

        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(v -> setViewColor("#333333", 0, imageColors));

        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(v -> setViewColor("#FDBE3B", 1, imageColors));

        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(v -> setViewColor("#FF4842", 2, imageColors));

        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(v -> setViewColor("#3A52Fc", 3, imageColors));

        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(v -> setViewColor("#000000", 4, imageColors));

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddNoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
            }
            else {
                selectImage();
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddURLDialog();
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddVideo).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddNoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_VIDEO_PERMISSION);
            }
            else {
                selectVideo();
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddVoice).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            addVoice();
        });
    }

    private void setSubtitleIndicator() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_CODE_STORAGE_VIDEO_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectVideo();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    private void selectVideo() {
        startActivityForResult(Intent.createChooser(new Intent().
                                setAction(Intent.ACTION_GET_CONTENT).
                                setType("video/mp4"),
                        "Select a video"),
                REQUEST_CODE_SELECT_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                if(imageUri != null) {
                    try {

                        Picasso.get().load(imageUri).into(imageNote);

                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);

                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Picasso.get().cancelRequest(imageNote);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
            }
        }
        else if (requestCode == REQUEST_CODE_SELECT_VIDEO && resultCode == RESULT_OK) {
            if (data != null) {
                videoUri = data.getData();
                if (videoUri != null) {
                    try {
                        layoutDeleteVideo.setVisibility(View.VISIBLE);
                        videoView.setVisibility(View.VISIBLE);
                        videoView.setVideoURI(videoUri);
                        videoView.start();

                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mine = MimeTypeMap.getSingleton();
        return mine.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    private void showAddURLDialog() {
        if (dialogURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url,
                    findViewById(R.id.layoutAddUrlContainer));
            builder.setView(view);
            dialogURL = builder.create();
            if (dialogURL.getWindow() != null) {
                dialogURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(v -> {
                if (inputURL.getText().toString().trim().isEmpty()) {
                    Toast.makeText(AddNoteActivity.this, "Please enter URL", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                    Toast.makeText(AddNoteActivity.this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
                } else {
                    textWebURL.setText(inputURL.getText().toString());
                    layoutWebURL.setVisibility(View.VISIBLE);
                    dialogURL.dismiss();
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogURL.dismiss());
        }
        dialogURL.show();
    }

    private void addVoice() {
        Toast.makeText(AddNoteActivity.this, "This function is developing", Toast.LENGTH_SHORT).show();
    }
}