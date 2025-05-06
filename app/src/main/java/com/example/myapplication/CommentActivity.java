package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.Adapter.CommentAdapter;
import com.example.myapplication.Model.Comment;
import com.example.myapplication.Model.Notification;
import com.example.myapplication.Model.Post;
import com.example.myapplication.databinding.ActivityCommentBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class CommentActivity extends AppCompatActivity {

    ActivityCommentBinding binding;
    Intent intent;
    String postId;
    String postedBy;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Comment> list =  new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        intent = getIntent();

        setSupportActionBar(binding.toolbar2);
        CommentActivity.this.setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        postId = intent.getStringExtra("postId");
        postedBy = intent.getStringExtra("postedBy");

        database.getReference().child("posts").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                Picasso.get()
                        .load(post.getPostImage())
                        .placeholder(R.mipmap.ic_camera)
                        .into(binding.postImage);

                binding.description.setText(post.getPostDescription());
                binding.like.setText(post.getPostLike()+"");
                binding.comment.setText(post.getCommentCount()+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    Picasso.get()
                            .load(user.getProfilePhoto())
                            .placeholder(R.mipmap.ic_camera)
                            .into(binding.profileImage);

                    binding.name.setText(user.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.commentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Comment comment = new Comment();
                comment.setCommentBody(binding.commentET.getText().toString());
                comment.setCommentedAt(new Date().getTime());
                comment.setCommentedBy(FirebaseAuth.getInstance().getUid());

                database.getReference()
                        .child("posts")
                        .child(postId)
                        .child("comments")
                        .push()
                        .setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference()
                                .child("posts")
                                .child(postId)
                                .child("commentCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int commentCount = 0;
                                        if (snapshot.exists()) {
                                            commentCount = snapshot.getValue(Integer.class);
                                        }
                                        database.getReference()
                                                .child("posts")
                                                .child(postId)
                                                .child("commentCount")
                                                .setValue(commentCount + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        binding.commentET.setText("");
                                                        Toast.makeText(CommentActivity.this, "Comment Posted", Toast.LENGTH_SHORT).show();

                                                        Notification notification = new Notification();
                                                        notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                        notification.setNotificationAt(new Date().getTime());
                                                        notification.setPostID(postId);
                                                        notification.setPostedBy(postedBy);
                                                        notification.setType("comment");

                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child("notification")
                                                                .child(postedBy)
                                                                .push()
                                                                .setValue(notification);
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }
                });
            }
        });

        CommentAdapter adapter = new CommentAdapter(this, list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.commentRV.setLayoutManager(layoutManager);
        binding.commentRV.setAdapter(adapter);

        database.getReference()
                .child("posts")
                .child(postId)
                .child("comments").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Comment comment = dataSnapshot.getValue(Comment.class);
                            list.add(comment);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}