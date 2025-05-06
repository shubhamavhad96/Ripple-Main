package com.example.myapplication.Fragment;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.example.myapplication.Adapter.PostAdapter;
import com.example.myapplication.Adapter.StoryAdapter;
import com.example.myapplication.Model.Post;
import com.example.myapplication.Model.Story;
import com.example.myapplication.Model.UserStories;
import com.example.myapplication.R;
import com.example.myapplication.User;
import com.example.myapplication.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;


public class HomeFragment extends Fragment {

    RecyclerView storyRV;
    ShimmerRecyclerView dashboardRV;
    ArrayList<Story> storyList;
    ArrayList<Post> postList;
    FirebaseStorage storage;
    FirebaseDatabase database;
    FirebaseAuth auth;
    RoundedImageView addStoryImage;
    ActivityResultLauncher<String> galleryLauncher;
    ProgressDialog dialog;
    FragmentHomeBinding binding;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new ProgressDialog(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_home, container, false);
        binding = FragmentHomeBinding.inflate(inflater, container, false);

//        dashboardRV = view.findViewById(R.id.dashboardRV);
        binding.dashboardRV.showShimmerAdapter();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Story Uploading");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);

//        storyRV = view.findViewById(R.id.storyRV);

        storyList = new ArrayList<>();

        StoryAdapter adapter = new StoryAdapter(storyList, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.storyRV.setLayoutManager(linearLayoutManager);
        binding.storyRV.setNestedScrollingEnabled(false);
        binding.storyRV.setAdapter(adapter);

        database.getReference()
                .child("stories").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            storyList.clear();
                            for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                                Story story = new Story();
                                story.setStoryBy(storySnapshot.getKey());
                                story.setStoryAt(storySnapshot.child("postedBy").getValue(Long.class));

                                ArrayList<UserStories> stories = new ArrayList<>();
                                for (DataSnapshot snapshot1 : storySnapshot.child("userStories").getChildren()) {
                                    UserStories userStories = snapshot1.getValue(UserStories.class);
                                    stories.add(userStories);
                                }
                                story.setStories(stories);
                                storyList.add(story);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        postList = new ArrayList<>();

        PostAdapter postAdapter = new PostAdapter(postList,getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.dashboardRV.setLayoutManager(layoutManager);
        binding.dashboardRV.setNestedScrollingEnabled(false);

        database.getReference().child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    post.setPostId(dataSnapshot.getKey());
                    postList.add(post);
                }
                binding.dashboardRV.setAdapter(postAdapter);
                binding.dashboardRV.hideShimmerAdapter();
                postAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent()
                , new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {

//                        binding.profileImage.setImageURI(result);
                        dialog.show();

                        final StorageReference reference = storage.getReference()
                                .child("stories")
                                .child(FirebaseAuth.getInstance().getUid())
                                .child(new Date().getTime()+"");
                        reference.putFile(result).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Story story = new Story();
                                        story.setStoryAt(new Date().getTime());

                                        database.getReference()
                                                .child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .child("postedBy")
                                                .setValue(story.getStoryAt()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        UserStories stories = new UserStories(uri.toString(), story.getStoryAt());

                                                        database.getReference()
                                                                .child("stories")
                                                                .child(FirebaseAuth.getInstance().getUid())
                                                                .child("userStories")
                                                                .push()
                                                                .setValue(stories).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        dialog.dismiss();
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                            }
                        });
                    }
                });

        database.getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    Picasso.get()
                            .load(user.getProfilePhoto())
                            .placeholder(R.mipmap.ic_camera)
                            .into(binding.profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.addStoryImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryLauncher.launch("image/*");
            }
        });

        return binding.getRoot();
    }
}