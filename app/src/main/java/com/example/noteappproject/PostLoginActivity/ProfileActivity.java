package com.example.noteappproject.PostLoginActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.noteappproject.Models.User;
import com.example.noteappproject.R;
import com.example.noteappproject.databinding.ActivityProfileBinding;
import com.example.noteappproject.utilities.StringUlti;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    private FirebaseUser user;
    private DatabaseReference reference;

    private String imageAvatarUriTask;
    private Uri imageAvatarUri;

    private static final int REQUEST_CODE_STORAGE_AVATAR_PERMISSION = 2;
    private static final int REQUEST_CODE_SELECT_IMAGE_AVATAR = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);


        this.user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = null;
        if (this.user != null) {
            userEmail = StringUlti.getSubEmailName(Objects.requireNonNull(this.user.getEmail()));
        }
        if (userEmail != null) {
            this.reference = FirebaseDatabase.getInstance().getReference("Users").child(userEmail);
        }

        this.reference.child("Account").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfileValue = snapshot.getValue(User.class);

                if (userProfileValue != null) {
                    String fullName = userProfileValue.getFullName();
                    String email = userProfileValue.getEmail();
                    String imageAvatar = userProfileValue.getAvatarPath();

                    binding.fullName.setText(fullName);
                    binding.emailAddress.setText(email);

                    if (imageAvatar != null && !imageAvatar.trim().isEmpty()) {
                        Picasso.get().load(imageAvatar).into(binding.imageAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        this.binding.avatarFAB.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_AVATAR_PERMISSION);
            }
            else {
                selectImageAvatar();
            }
        });

        this.binding.imageEditName.setOnClickListener(v -> binding.fullName.setEnabled(true));

        binding.isActivated.setText(user.isEmailVerified() ? "Activated" : "Not activated");

        if ( user.isEmailVerified() ){
            this.binding.imageButtonActiveAccount.setActivated(false);
            this.binding.imageButtonActiveAccount.setImageResource(R.drawable.ic_is_activated);
        } else {
            this.binding.imageButtonActiveAccount.setActivated(true);
            this.binding.imageButtonActiveAccount.setImageResource(R.drawable.ic_not_activated);
            this.binding.imageButtonActiveAccount.setOnClickListener(view -> {
                // Create the object of
                // AlertDialog Builder class
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                // Set Alert Title
                builder.setTitle("Active account !");
                // Set the message show for the Alert time
                builder.setMessage("Your account haven't been activated !");
                // Set Cancelable false
                // for when the user clicks on the outside
                // the Dialog Box then it will remain show
                builder.setCancelable(false);

                // Set the positive button with yes name
                // OnClickListener method is use of
                // DialogInterface interface.
                builder.setPositiveButton( "Send activated email", (dialog, which) -> {
                    // When the user click yes button
                    // then app will close
                    user.sendEmailVerification()
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, "Please check your mail to active account !", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
                            });
                });

                builder.setNegativeButton("Later", (dialog, which) -> dialog.dismiss());

                // Create the Alert dialog
                AlertDialog alertDialog = builder.create();

                // Show the Alert Dialog box
                alertDialog.show();
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveProfileBtn :
                saveProfileData();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }



    private void saveProfileData() {

        String fullName = binding.fullName.getText().toString().trim();

        if (binding.imageAvatar.getDrawable() == null || imageAvatarUri == null) {
            reference.child("Account").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    snapshot.getRef().child("fullName").setValue(fullName);
                    snapshot.getRef().child("avatarPath").setValue("");

                    Toast.makeText(ProfileActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            StorageReference storageImageAvatarReference = FirebaseStorage.getInstance().getReference("avatar");
            StorageReference imageCoverPhotoReference = storageImageAvatarReference.child(System.currentTimeMillis() +
                    "." + getFileExtension(imageAvatarUri));
            imageCoverPhotoReference.putFile(imageAvatarUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                return imageCoverPhotoReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    imageAvatarUriTask = task.getResult().toString();
                    reference.child("Account").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            snapshot.getRef().child("fullName").setValue(fullName);
                            snapshot.getRef().child("avatarPath").setValue(imageAvatarUriTask);

                            Toast.makeText(ProfileActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        }

        this.binding.fullName.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_AVATAR_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageAvatar();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectImageAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE_AVATAR);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_IMAGE_AVATAR && resultCode == RESULT_OK) {
            if (data != null) {
                imageAvatarUri = data.getData();
                if (imageAvatarUri != null) {
                    try {
                        Picasso.get().load(imageAvatarUri).into(binding.imageAvatar);

                        binding.imageAvatar.setVisibility(View.VISIBLE);

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
}