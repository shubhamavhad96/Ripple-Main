package com.example.myapplication.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.Adapter.NotificationAdapter;
import com.example.myapplication.Model.Notification;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Notification2Fragment extends Fragment {

    RecyclerView notificationRV;
    ArrayList<Notification> list;
    FirebaseDatabase database;

    public Notification2Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification2, container, false);

        notificationRV = view.findViewById(R.id.notification2RV);
        list = new ArrayList<>();

//        list.add(new Notification(R.mipmap.girl1,"<b>Jen</b> mentioned you in comment","just now"));
//        list.add(new Notification(R.mipmap.girl2,"<b>max</b> liked your post","9 minutes ago"));
//        list.add(new Notification(R.mipmap.man1,"<b>sam</b> mentioned you in comment","1 day ago"));
//        list.add(new Notification(R.mipmap.man2,"<b>Jack</b> started following you","just now"));
//        list.add(new Notification(R.mipmap.profile_pic,"<b>Jen</b> mentioned you in comment","just now"));

        NotificationAdapter adapter = new NotificationAdapter(list,getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        notificationRV.setLayoutManager(layoutManager);
        notificationRV.setAdapter(adapter);

        database.getReference()
                .child("notification")
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Notification notification = dataSnapshot.getValue(Notification.class);
                            notification.setNotificationID(dataSnapshot.getKey());
                            list.add(notification);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        return view;
    }
}