package com.example.myapplication.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.Fragment.Notification2Fragment;
import com.example.myapplication.Fragment.NotificationFragment;
import com.example.myapplication.Fragment.RequestFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private String[] titles = new String[] {"Notification", "Requests"};

    public ViewPagerAdapter(@NonNull NotificationFragment fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new Notification2Fragment();
            case 1: return new RequestFragment();
        }
        return new Notification2Fragment();
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
