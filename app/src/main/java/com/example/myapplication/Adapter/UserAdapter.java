package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Model.Follow;
import com.example.myapplication.Model.Notification;
import com.example.myapplication.R;
import com.example.myapplication.SeeProfileActivity;
import com.example.myapplication.User;
import com.example.myapplication.databinding.UserRvSampleBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder> {

    Context context;
    ArrayList<User> list;

    public UserAdapter(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_rv_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        User user = list.get(position);
        Picasso.get()
                .load(user.getProfilePhoto())
                .placeholder(R.mipmap.ic_camera)
                .into(holder.binding.profileImage);
        holder.binding.name.setText(user.getName());
        holder.binding.userName.setText(user.getUsername());

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(user.getUserID())
                .child("followers")
                .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.binding.followBTN.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.follow_active_btn));
                    holder.binding.followBTN.setText("Following");
                    holder.binding.followBTN.setTextColor(context.getResources().getColor(R.color.black));
                    holder.binding.followBTN.setEnabled(true);

                    holder.binding.followBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Users")
                                    .child(user.getUserID())
                                    .child("followers")
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("Users")
                                                    .child(user.getUserID())
                                                    .child("followerCount")
                                                    .setValue(user.getFollowerCount() - 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            holder.binding.followBTN.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.follow_btn_bg));
                                                            holder.binding.followBTN.setText("Follow");
                                                            holder.binding.followBTN.setTextColor(context.getResources().getColor(R.color.white));
                                                            holder.binding.followBTN.setEnabled(true);
                                                            Toast.makeText(context, "You Unfollowed" + user.getName(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    });

                            FirebaseDatabase.getInstance().getReference()
                                    .child("Users")
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .child("following")
                                    .child(user.getUserID())
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("Users")
                                                    .child(FirebaseAuth.getInstance().getUid())
                                                    .child("followingCount")
                                                    .setValue(user.getFollowingCount() - 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Toast.makeText(context, " You are not following", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });
                }
                else {
                    holder.binding.followBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Follow follow = new Follow();
                            follow.setFollowedBy(FirebaseAuth.getInstance().getUid());
                            follow.setFollowedAt(new Date().getTime());
//                            follow.setFollowing(user.getUserID());

                                FirebaseDatabase.getInstance().getReference()
                                        .child("Users")
                                        .child(user.getUserID())
                                        .child("followers")
                                        .child(FirebaseAuth.getInstance().getUid())
                                        .setValue(follow).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("Users")
                                                        .child(user.getUserID())
                                                        .child("followerCount")
                                                        .setValue(user.getFollowerCount() + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                holder.binding.followBTN.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.follow_active_btn));
                                                                holder.binding.followBTN.setText("Following");
                                                                holder.binding.followBTN.setTextColor(context.getResources().getColor(R.color.black));
                                                                holder.binding.followBTN.setEnabled(true);
                                                                Toast.makeText(context, "You Followed" + user.getName(), Toast.LENGTH_SHORT).show();

                                                                Notification notification = new Notification();
                                                                notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                                notification.setNotificationAt(new Date().getTime());
                                                                notification.setType("follow");

                                                                FirebaseDatabase.getInstance().getReference()
                                                                        .child("notification")
                                                                        .child(user.getUserID())
                                                                        .push()
                                                                        .setValue(notification);
                                                            }
                                                        });
                                            }
                                        });
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Users")
                                        .child(FirebaseAuth.getInstance().getUid())
                                        .child("following")
                                        .child(user.getUserID())
                                        .setValue(follow).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("Users")
                                                        .child(FirebaseAuth.getInstance().getUid())
                                                        .child("followingCount")
                                                        .setValue(user.getFollowingCount() + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Toast.makeText(context, "Following", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.binding.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SeeProfileActivity.class);
                intent.putExtra("userId", user.getUserID());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        UserRvSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = UserRvSampleBinding.bind(itemView);
        }
    }
}
