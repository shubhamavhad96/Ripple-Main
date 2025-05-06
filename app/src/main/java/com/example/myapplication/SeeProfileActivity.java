package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivitySeeProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SeeProfileActivity extends AppCompatActivity {

    ActivitySeeProfileBinding binding;
    Intent intent;
    String userId;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeeProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        intent = getIntent();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        userId = intent.getStringExtra("userId");

        database.getReference()
                .child("Users")
                .child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            Picasso.get()
                                    .load(user.getProfilePhoto())
                                    .placeholder(R.mipmap.ic_camera)
                                    .into(binding.profileImage);

                            binding.userName.setText(user.getName());
                            binding.userBio.setText(user.getUsername());
                            binding.followersTV.setText(user.getFollowerCount()+"");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}