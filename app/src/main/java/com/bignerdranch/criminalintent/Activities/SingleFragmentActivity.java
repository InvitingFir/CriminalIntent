package com.bignerdranch.criminalintent.Activities;

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.bignerdranch.criminalintent.R;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResLayout());
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.fragment_container);
        if(f == null){
            fm.beginTransaction().add(R.id.fragment_container, createFragment()).commit();
        }
    }

    protected abstract Fragment createFragment();

    @LayoutRes
    protected int getResLayout(){
        return R.layout.activity_fragment;
    }
}
