package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.CommentActivity;
import com.example.myapplication.Model.Notification;
import com.example.myapplication.R;
import com.example.myapplication.User;
import com.example.myapplication.databinding.Notification2sampleBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.viewHolder> {

    ArrayList<Notification> list;
    Context context;

    public NotificationAdapter(ArrayList<Notification> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.notification2sample,parent,false);

        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        Notification notification = list.get(position);

        String type = notification.getType();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(notification.getNotificationBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            Picasso.get()
                                    .load(user.getProfilePhoto())
                                    .placeholder(R.mipmap.ic_camera)
                                    .into(holder.binding.profileImage);

                            if (type.equals("like")) {
                                holder.binding.notification.setText(Html.fromHtml("<b>" + user.getName() + "</b>" + " Liked your post"));
                            } else if (type.equals("comment")) {
                                holder.binding.notification.setText(Html.fromHtml("<b>" + user.getName() + "</b>" + " Commented on your post"));
                            } else {
                                holder.binding.notification.setText(Html.fromHtml("<b>" + user.getName() + "</b>" + " Started following you"));
                            }
                        } else {
                            // Handle the case where the user object is null
                            // You can display a default or placeholder value for the user's name
                            holder.binding.notification.setText("Unknown user");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.openNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String notificationId = notification.getNotificationID();
                String postedBy = notification.getPostedBy();
                String postId = notification.getPostID();
                if (postedBy != null && postId != null) {
                    // Use the postedBy and postId values
                    FirebaseDatabase.getInstance().getReference()
                            .child("notification")
                            .child(postedBy)
                            .child(notificationId)
                            .child("checkOpen")
                            .setValue(true);

                    Intent intent = new Intent(context, CommentActivity.class);
                    intent.putExtra("postId", postId);
                    intent.putExtra("postedBy", postedBy);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else {
                    // Handle the case where the postedBy or postId is null
                    Log.e("FirebaseError", "Notification data is incomplete");
                }
            }
        });
        Boolean checkOpen = notification.isCheckOpen();
        if (checkOpen == true) {
            holder.binding.openNotification.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        else {}
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        Notification2sampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = Notification2sampleBinding.bind(itemView);
        }
    }
}
