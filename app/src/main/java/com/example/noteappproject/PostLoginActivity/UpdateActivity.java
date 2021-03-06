package com.example.noteappproject.PostLoginActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.noteappproject.Models.NoteItem;
import com.example.noteappproject.Models.Settings;
import com.example.noteappproject.R;
import com.example.noteappproject.databinding.ActivityUpdateBinding;
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
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class UpdateActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText label_update, subtitle_update, textContent_update;
    private TextView textDateTime_update, textWebURL_update;
    private View viewSubtitleIndicator_update;
    private ImageView imageBack, imageUpdate, imageSetPassword, imageNote_update, imageNotification;
    private LinearLayout layoutWebURL_update, layoutDeleteVideo_update;
    private VideoView videoView_update;

    private String selectedNoteColor;
    private String selectedImagePath;

    private AlertDialog dialogURL;
    private Uri imageUriUpdate, videoUriUpdate;
    private String imageUriTaskUpdate, videoUriTaskUpdate;


    final Calendar calendar = Calendar.getInstance();

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageImageReference, storageVideoReference;
    private FirebaseStorage storage;
    private String userID;

    private String idNote, label, subtitle, textContent, mdate, color, image, video, web;
    private NoteItem noteItem;

    private String fontSizeDB, fontStyleDB;

    private NotificationCompat managerCompat;
    private boolean passwordVisible;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 2;
    private static final int REQUEST_CODE_SELECT_IMAGE = 3;
    private static final int REQUEST_CODE_SELECT_VIDEO = 4;
    private static final int REQUEST_CODE_STORAGE_VIDEO_PERMISSION = 5;


    public static final int UPDATE_NOTE = 8;
    public static final int SET_PASSWORD = 9;
    public static final int REMOVE_PASSWORD = 10;
    public static final String KEY_SENDING_RESULT_CODE = "SENDING_RESULT_CODE";

    private ActivityUpdateBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityUpdateBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        noteItem = (NoteItem) getIntent().getSerializableExtra(NoteActivity.KEY_SENDING_NOTE_ITEM);

        bindingView();
        setupDatabase();
        setOnClickEvent();
        setValueIntent();
        initMiscellaneous();
        setSubtitleIndicator();
    }

    private void bindingView() {
        layoutWebURL_update = this.binding.layoutWebURLUpdate;

        label_update = this.binding.labelUpdate;
        subtitle_update = this.binding.subtitleUpdate;
        textContent_update = this.binding.textContentUpdate;
        textDateTime_update = this.binding.textDateTimeUpdate;
        textWebURL_update = this.binding.textWebURLUpdate;

        viewSubtitleIndicator_update = this.binding.viewSubtitleIndicatorUpdate;

        imageBack = this.binding.imageBack;


        imageUpdate = this.binding.imageUpdate;

        imageSetPassword = this.binding.imageSetPassword;

        imageNotification = this.binding.imageNotification;


        imageNote_update = this.binding.imageNoteUpdate;

        layoutDeleteVideo_update = this.binding.layoutDeleteVideoUpdate;
        videoView_update = this.binding.videoViewUpdate;

        selectedNoteColor = "#333333";
        selectedImagePath = "";
    }

    private void setupDatabase() {
        String userEmail = null;

        this.mAuth = FirebaseAuth.getInstance();

        if (this.mAuth.getCurrentUser() != null) {
            userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

            if (userEmail == null){
                return;
            }
        }

        if (userEmail != null) {
            userEmail = StringUlti.getSubEmailName(userEmail);
        }

        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userEmail).child("NoteItems");
        this.storage = FirebaseStorage.getInstance();
    }

    private void setOnClickEvent() {
        imageBack.setOnClickListener(this);
        imageUpdate.setOnClickListener(this);
        imageSetPassword.setOnClickListener(this);
        imageNotification.setOnClickListener(this);

        this.binding.imageRemoveWebURL.setOnClickListener(v -> {
            textWebURL_update.setVisibility(View.GONE);
            layoutWebURL_update.setVisibility(View.GONE);
            idNote = String.valueOf(noteItem.getCreated_at());
            textWebURL_update.setText("");

            Log.e("TEST", idNote);
            databaseReference.child(idNote).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    snapshot.getRef().child("webLink").setValue("").addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            noteItem.setWebLink("");
                            Toast.makeText(UpdateActivity.this, "Delete successfully", Toast.LENGTH_SHORT).show();
                        } else {

                            Toast.makeText(UpdateActivity.this, "Delete failure", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        });

        this.binding.imageRemoveImage.setOnClickListener(v -> {
            imageNote_update.setVisibility(View.GONE);
            findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
            Picasso.get().load((Uri) null).into(imageNote_update);
//                noteItem.setImagePath("");
            idNote = String.valueOf(noteItem.getCreated_at());

            StorageReference imageReference = storage.getReferenceFromUrl(noteItem.getImagePath());
            imageReference.delete().addOnSuccessListener(unused -> databaseReference.child(idNote).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    snapshot.getRef().child("imagePath").setValue("");
                    Toast.makeText(UpdateActivity.this, "Delete successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }));
        });

        this.binding.imageRemoveVideo.setOnClickListener(v -> {
            layoutDeleteVideo_update.setVisibility(View.GONE);
            videoView_update.setVideoURI(null);
            videoView_update.setVisibility(View.GONE);

            idNote = String.valueOf(noteItem.getCreated_at());

            StorageReference videoReference = storage.getReferenceFromUrl(noteItem.getVideoPath());
            videoReference.delete().addOnSuccessListener(unused -> databaseReference.child(idNote).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    snapshot.getRef().child("videoPath").setValue("");
                    Toast.makeText(UpdateActivity.this, "Delete successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }));
        });
    }

    private void setValueIntent() {

        final String userEmail = StringUlti.getSubEmailName(Objects.requireNonNull(Objects.requireNonNull(this.mAuth.getCurrentUser()).getEmail()));
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        databaseReference = rootNode.getReference("Users").child(userEmail);
        this.databaseReference.child("Settings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Settings settings = snapshot.getValue(Settings.class);

                if (settings != null) {
                    fontSizeDB = settings.getFontSize() == null ? "Medium" : settings.getFontSize();
                    fontStyleDB = settings.getFontStyle() == null ? "Normal" : settings.getFontStyle();
                } else {
                    fontSizeDB = "Medium";
                    fontStyleDB = "Normal";
                }

                switch (fontSizeDB) {
                    case "Small":
                        textContent_update.setTextSize(10);
                        break;
                    case "Medium":
                        textContent_update.setTextSize(15);
                        break;
                    case "Big":
                        textContent_update.setTextSize(20);
                        break;
                    case "Very Big":
                        textContent_update.setTextSize(25);
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        viewSubtitleIndicator_update.setBackgroundColor(Color.parseColor(noteItem.getColor()));
        label_update.setText(noteItem.getLabel());
        subtitle_update.setText(noteItem.getSubtitle());
        textContent_update.setText(noteItem.getText_content());
        textDateTime_update.setText(noteItem.getDate());

        if (noteItem.getImagePath() != null && !noteItem.getImagePath().trim().isEmpty()) {
            Picasso.get().load(noteItem.getImagePath())
                    .into(imageNote_update);
            imageNote_update.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
            selectedImagePath = noteItem.getImagePath();
        }

        if (noteItem.getWebLink() != null && !noteItem.getWebLink().trim().isEmpty()) {
            textWebURL_update.setText(noteItem.getWebLink());
            layoutWebURL_update.setVisibility(View.VISIBLE);
        }

        if (noteItem.getVideoPath() != null && !noteItem.getVideoPath().trim().isEmpty()) {
            layoutDeleteVideo_update.setVisibility(View.VISIBLE);
            videoView_update.setVisibility(View.VISIBLE);
            videoView_update.setVideoURI(Uri.parse(noteItem.getVideoPath()));
            videoView_update.start();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageBack:
                finish();
                break;
            case R.id.imageUpdate:
                updateData();
                break;
            case R.id.imageSetPassword :
                setPasswordNote();
                break;
            case R.id.imageNotification:
                setNotification();
                break;
        }
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

        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(v -> {
            selectedNoteColor = "#333333";
            imageColor1.setImageResource(R.drawable.ic_check);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicator();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(v -> {
            selectedNoteColor = "#FDBE3B";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(R.drawable.ic_check);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicator();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(v -> {
            selectedNoteColor = "#FF4842";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(R.drawable.ic_check);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicator();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(v -> {
            selectedNoteColor = "#3A52Fc";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(R.drawable.ic_check);
            imageColor5.setImageResource(0);
            setSubtitleIndicator();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(v -> {
            selectedNoteColor = "#000000";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(R.drawable.ic_check);
            setSubtitleIndicator();
        });

        if (noteItem != null && noteItem.getColor() != null && !noteItem.getColor().trim().isEmpty()) {
            switch (noteItem.getColor()) {
                case "#FDBE3B":
                    layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3A52Fc":
                    layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;
            }
        }

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(UpdateActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
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
                ActivityCompat.requestPermissions(UpdateActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
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
        ColorDrawable colorDrawable = (ColorDrawable) viewSubtitleIndicator_update.getBackground();

        colorDrawable.setColor(Color.parseColor(selectedNoteColor));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUriUpdate = data.getData();
                if(imageUriUpdate != null) {
                    try {
                        Picasso.get().load(imageUriUpdate).into(imageNote_update);

                        imageNote_update.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);

                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        else if (requestCode == REQUEST_CODE_SELECT_VIDEO && resultCode == RESULT_OK) {
            if (data != null) {
                videoUriUpdate = data.getData();
                if (videoUriUpdate != null) {
                    try {
                        layoutDeleteVideo_update.setVisibility(View.VISIBLE);
                        videoView_update.setVisibility(View.VISIBLE);
                        videoView_update.setVideoURI(videoUriUpdate);
                        videoView_update.start();

                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }


    private void showAddURLDialog() {
        if (dialogURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
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
                    Toast.makeText(UpdateActivity.this, "Please enter URL", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                    Toast.makeText(UpdateActivity.this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
                } else {
                    textWebURL_update.setText(inputURL.getText().toString());
                    layoutWebURL_update.setVisibility(View.VISIBLE);
                    dialogURL.dismiss();
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogURL.dismiss());
        }
        dialogURL.show();
    }

    @SuppressLint("ShowToast")
    public void updateData() {
        final String currentTimeStamp = String.valueOf(noteItem.getCreated_at());

        label = label_update.getText().toString().trim();
        textContent = textContent_update.getText().toString().trim();
        subtitle = subtitle_update.getText().toString().trim();
        mdate = textDateTime_update.getText().toString().trim();

        if (label.isEmpty()) {
            label_update.setError("Label must not be empty");
            label_update.requestFocus();
            return;
        }

        // N???u c?? th??m web URL
        if (layoutWebURL_update.getVisibility() == View.VISIBLE) {
            noteItem.setWebLink(textWebURL_update.getText().toString());
        }

        // Kh??ng up h??nh l???n video
        if ( (imageNote_update.getDrawable() == null || imageNote_update.getVisibility() == View.GONE ) && videoView_update.getVisibility() == View.GONE ) {
            noteItem.setImagePath("");
            noteItem.setVideoPath("");

            noteItem.setLabel(label);
            noteItem.setText_content(textContent);
            noteItem.setSubtitle(subtitle);
            noteItem.setDate(mdate);
            noteItem.setColor(selectedNoteColor);

            databaseReference.child(currentTimeStamp).setValue(noteItem);

            Log.e("TEST", "UPDATE1: "+ noteItem.toString());
            Toast.makeText(UpdateActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.putExtra(NoteActivity.KEY_SENDING_NOTE_ITEM, noteItem);
            intent.putExtra(NoteActivity.KEY_REQUEST_NOTE_OPERATION, NoteActivity.VALUE_REQUEST_UPDATE_NOTE);
            finish();
        }

        // Up h??nh v?? video
        if ( (imageNote_update.getDrawable() != null && imageNote_update.getVisibility() != View.GONE ) && videoView_update.getVisibility() != View.GONE ){
            // Up Uri h??nh l??n firebase
            StorageReference storageImageReference = FirebaseStorage.getInstance().getReference("images");

            StorageReference imageReference = storageImageReference.child(System.currentTimeMillis() +
                    "." + getFileExtension(imageUriUpdate));

            imageReference.putFile(imageUriUpdate).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                return imageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    imageUriTaskUpdate = task.getResult().toString();
                    noteItem.setImagePath(imageUriTaskUpdate);

                    // Upload videoUri l??n firebase
                    StorageReference storageVideoReference = FirebaseStorage.getInstance().getReference("videos");

                    StorageReference videoReference = storageVideoReference.child(System.currentTimeMillis() +
                            "." + getFileExtension(videoUriUpdate));

                    videoReference.putFile(videoUriUpdate).continueWithTask(task1 -> {
                        if (!task1.isSuccessful()) {
                            throw Objects.requireNonNull(task1.getException());
                        }

                        return videoReference.getDownloadUrl();
                    }).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            videoUriTaskUpdate = task1.getResult().toString();
                            noteItem.setVideoPath(videoUriTaskUpdate);

                            noteItem.setLabel(label);
                            noteItem.setText_content(textContent);
                            noteItem.setSubtitle(subtitle);
                            noteItem.setDate(mdate);
                            noteItem.setColor(selectedNoteColor);

                            databaseReference.child(currentTimeStamp).setValue(noteItem);
                            Log.e("TEST", "UPDATE2: "+ noteItem.toString());

                            Toast.makeText(UpdateActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            intent.putExtra(NoteActivity.KEY_SENDING_NOTE_ITEM, noteItem);
                            intent.putExtra(NoteActivity.KEY_REQUEST_NOTE_OPERATION, NoteActivity.VALUE_REQUEST_UPDATE_NOTE);
                            finish();
                        }
                    });
                }
            });
        }


        // Up h??nh kh??ng video
        if ( (imageNote_update.getDrawable() != null && imageNote_update.getVisibility() != View.GONE ) && videoView_update.getVisibility() == View.GONE ){
            StorageReference storageImageReference = FirebaseStorage.getInstance().getReference("images");

            StorageReference imageReference = storageImageReference.child(System.currentTimeMillis() +
                    "." + getFileExtension(imageUriUpdate));

            imageReference.putFile(imageUriUpdate).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                return imageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    imageUriTaskUpdate = task.getResult().toString();
                    noteItem.setImagePath(imageUriTaskUpdate);
                    noteItem.setVideoPath("");

                    noteItem.setLabel(label);
                    noteItem.setText_content(textContent);
                    noteItem.setSubtitle(subtitle);
                    noteItem.setDate(mdate);
                    noteItem.setColor(selectedNoteColor);

                    databaseReference.child(currentTimeStamp).setValue(noteItem);
                    Log.e("TEST", "UPDATE 3: "+ noteItem.toString());

                    Toast.makeText(UpdateActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent();
                    intent.putExtra(NoteActivity.KEY_SENDING_NOTE_ITEM, noteItem);
                    intent.putExtra(NoteActivity.KEY_REQUEST_NOTE_OPERATION, NoteActivity.VALUE_REQUEST_UPDATE_NOTE);
                    finish();
                }
            });
        }

        // Up video kh??ng up h??nh
        if ( (imageNote_update.getDrawable() == null || imageNote_update.getVisibility() == View.GONE ) && videoView_update.getVisibility() != View.GONE ){
            // Upload videoUri l??n firebase
            StorageReference storageVideoReference = FirebaseStorage.getInstance().getReference("videos");

            StorageReference videoReference = storageVideoReference.child(System.currentTimeMillis() +
                    "." + getFileExtension(videoUriUpdate));

            videoReference.putFile(videoUriUpdate).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                return videoReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    videoUriTaskUpdate = task.getResult().toString();
                    noteItem.setVideoPath(videoUriTaskUpdate);
                    noteItem.setImagePath("");

                    noteItem.setLabel(label);
                    noteItem.setText_content(textContent);
                    noteItem.setSubtitle(subtitle);
                    noteItem.setDate(mdate);
                    noteItem.setColor(selectedNoteColor);

                    Log.e("TEST", "UPDATE4: "+ noteItem.toString());

                    databaseReference.child(currentTimeStamp).setValue(noteItem);

                    Toast.makeText(UpdateActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent();
                    intent.putExtra(NoteActivity.KEY_SENDING_NOTE_ITEM, noteItem);
                    intent.putExtra(NoteActivity.KEY_REQUEST_NOTE_OPERATION, NoteActivity.VALUE_REQUEST_UPDATE_NOTE);
                    finish();
                }
            });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mine = MimeTypeMap.getSingleton();
        return mine.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @SuppressLint("SetTextI18n")
    private void setNotification() {

        final EditText mtimePicker = new EditText(this);
        mtimePicker.setHint("Choose time");
        mtimePicker.setClickable(false);
        mtimePicker.setFocusable(false);
        mtimePicker.setCursorVisible(false);
        mtimePicker.setFocusableInTouchMode(false);

        mtimePicker.setOnClickListener(view -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);

            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(UpdateActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,(timePicker, selectedHour, selectedMinute) ->
                    mtimePicker.setText( selectedHour + ":" + selectedMinute), hour, minute, true);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });


        final EditText mdatePicker = new EditText(this);
        mdatePicker.setHint("Choose date");
        mdatePicker.setClickable(false);
        mdatePicker.setFocusable(false);
        mdatePicker.setCursorVisible(false);
        mdatePicker.setFocusableInTouchMode(false);

        DatePickerDialog.OnDateSetListener date = (datePicker, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

            mdatePicker.setText(sdf.format(calendar.getTime()));
        };

        mdatePicker.setOnClickListener(view ->
                new DatePickerDialog(UpdateActivity.this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show());

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        ll.addView(mtimePicker);
        ll.addView(mdatePicker);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Set time and date for note")
                .setView(ll)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
//                        String time = mtimePicker.getText().toString();
//                        String date = mdatePicker.getText().toString();
                    Toast.makeText(UpdateActivity.this, "Get notification successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    Toast.makeText(UpdateActivity.this, "No notification", Toast.LENGTH_SHORT).show();

                    dialogInterface.dismiss();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void setPasswordNote() {
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
                .setTitle("Set password for note")
                .setView(ll)
                .setPositiveButton("Confirm", (dialogInterface, i) -> {
                    String passwordNoteValue = passwordNote.getText().toString().trim();

                    long noteItemID = noteItem.getCreated_at();
                    noteItem.setPasswordNote(passwordNoteValue);

                    databaseReference.child(String.valueOf(noteItemID)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            snapshot.getRef().child("passwordNote").setValue(passwordNoteValue);
                            Toast.makeText(UpdateActivity.this, "Set password successfully", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            intent.putExtra(NoteActivity.KEY_SENDING_NOTE_ITEM, noteItem);
                            intent.putExtra(NoteActivity.KEY_REQUEST_NOTE_OPERATION, NoteActivity.VALUE_REQUEST_UPDATE_NOTE);
                            intent.putExtra(UpdateActivity.KEY_SENDING_RESULT_CODE, UpdateActivity.SET_PASSWORD);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .setNeutralButton("Remove", (dialog, which) -> {

                    long noteItemID = noteItem.getCreated_at();
                    noteItem.setPasswordNote("");

                    databaseReference.child(String.valueOf(noteItemID)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            snapshot.getRef().child("passwordNote").setValue("");
                            Toast.makeText(UpdateActivity.this, "Remove password successfully", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            intent.putExtra(NoteActivity.KEY_SENDING_NOTE_ITEM, noteItem);
                            intent.putExtra(NoteActivity.KEY_REQUEST_NOTE_OPERATION, NoteActivity.VALUE_REQUEST_UPDATE_NOTE);
                            intent.putExtra(UpdateActivity.KEY_SENDING_RESULT_CODE, UpdateActivity.REMOVE_PASSWORD);

                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void addVoice() {
        Toast.makeText(UpdateActivity.this, "This function will come soon", Toast.LENGTH_SHORT).show();
    }

}