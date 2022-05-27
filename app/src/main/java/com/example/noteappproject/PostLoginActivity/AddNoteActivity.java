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
import android.view.ViewGroup;
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
import com.example.noteappproject.R;
import com.example.noteappproject.ReLoginActivity.RegisterUser;
import com.example.noteappproject.databinding.ActivityAddNoteBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
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
    private String selectedNoteColor, selectedImagePath, selectedVideoPath;
    private LinearLayout layoutDeleteVideo;
    private Uri imageUri, videoUri;

    public static final int ADD_NOTE = 4;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 2;
    private static final int REQUEST_CODE_STORAGE_VIDEO_PERMISSION = 3;

    private static final int REQUEST_CODE_SELECT_IMAGE = 4;
    private static final int REQUEST_CODE_SELECT_VIDEO = 5;

    private FirebaseAuth mAuth;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private StorageReference storageImageReference, storageVideoReference;
    private StorageTask mUploadTask;

    private String imageUriTask, videoUriTask;
    private NoteItem noteItem;

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
        selectedImagePath = "";
        selectedVideoPath = "";
    }

    private void setOnClickEvent() {
        imageBack.setOnClickListener(this);

        imageSave.setOnClickListener(this);

        videoView.setOnPreparedListener(mp -> mp.setLooping(true));

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
            this.binding.imageRemoveVideo.setVisibility(View.GONE);
            selectedVideoPath = "";
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

        noteItem = new NoteItem();

        noteItem.setLabel(labelValue);
        noteItem.setText_content(textContentValue);
        noteItem.setDate(dateTimeValue);
        noteItem.setColor(selectedNoteColor);


        if (layoutWebURL.getVisibility() == View.VISIBLE) {
            noteItem.setWebLink(textWebURL.getText().toString());
        }

        String dateValue = noteItem.getDate();
        String colorValue = noteItem.getColor();
        String webLinkValue = noteItem.getWebLink();

        final String userEmail = RegisterUser.getSubEmailName(Objects.requireNonNull(this.mAuth.getCurrentUser()).getEmail());
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("Users").child(userEmail).child("NoteItems");

        final String currentTimeStamp = String.valueOf(System.currentTimeMillis());

        if (imageNote.getDrawable() == null) {
            noteItem = new NoteItem(labelValue, subtitleValue, textContentValue, dateValue, colorValue, "", videoUriTask, webLinkValue, "");
            reference.child(currentTimeStamp).setValue(noteItem);
        } else {
            storageImageReference = FirebaseStorage.getInstance().getReference("images");

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
                    noteItem = new NoteItem(labelValue, subtitleValue, textContentValue, dateValue, colorValue, imageUriTask, videoUriTask, webLinkValue, "");
                    reference.child(currentTimeStamp).setValue(noteItem);
                }
            });
        }

        if (videoView.getVisibility() == View.GONE) {
            noteItem = new NoteItem(labelValue, subtitleValue, textContentValue, dateValue, colorValue, imageUriTask, "", webLinkValue, "");
            reference.child(currentTimeStamp).setValue(noteItem);
        } else {
            storageVideoReference = FirebaseStorage.getInstance().getReference("videos");
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
                    noteItem = new NoteItem(labelValue, subtitleValue, textContentValue, dateValue, colorValue, imageUriTask, videoUriTask, webLinkValue, "");
                    reference.child(currentTimeStamp).setValue(noteItem);
                }
            });
        }

        Toast.makeText(AddNoteActivity.this, "Add successful", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent();
        intent.putExtra("note", noteItem);
        setResult(ADD_NOTE, intent);
        finish();
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
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer));
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