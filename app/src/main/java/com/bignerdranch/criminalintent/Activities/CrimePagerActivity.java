package com.bignerdranch.criminalintent.Activities;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.bignerdranch.criminalintent.data.Crime;
import com.bignerdranch.criminalintent.data.CrimeLab;
import com.bignerdranch.criminalintent.Fragments.CrimeFragment;
import com.bignerdranch.criminalintent.R;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity
        implements CrimeFragment.Callbacks{
    public static final String EXTRA_CRIME_ID = "crime_id";

    private ViewPager mViewPager;
    private Button mFirstPageButton;
    private Button mLastPageButton;
    private List<Crime> mCrimes;
    private PagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);
        mCrimes = CrimeLab.getInstance(this).getCrimes();

        mFirstPageButton = findViewById(R.id.first_page_button);
        mFirstPageButton.setOnClickListener(l -> mViewPager.setCurrentItem(0));
        mLastPageButton = findViewById(R.id.last_page_button);
        mLastPageButton.setOnClickListener(l -> mViewPager.setCurrentItem(mCrimes.size()-1));
        mViewPager = findViewById(R.id.view_pager);
        FragmentManager fm = getSupportFragmentManager();
        mAdapter = new FragmentStatePagerAdapter(fm){
            @NonNull
            @Override
            public Fragment getItem(int position) {
                UUID id= mCrimes.get(position).getID();
                return CrimeFragment.getInstance(id);
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        };
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position == 0) mFirstPageButton.setVisibility(View.INVISIBLE);
                else mFirstPageButton.setVisibility(View.VISIBLE);
                if(position == mAdapter.getCount()-1) mLastPageButton.setVisibility(View.INVISIBLE);
                else mLastPageButton.setVisibility(View.VISIBLE);
            }

            public void onPageSelected(int position) { }

            public void onPageScrollStateChanged(int state) { }
        });

        UUID id = (UUID)getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        int position = 0;
        while(position < mCrimes.size() && !mCrimes.get(position).getID().equals(id)){
            position++;
        }
        mViewPager.setCurrentItem(position);
    }

    @Override
    public void onCrimeUpdated(Crime c) {

    }

    public static Intent newIntent(Context packageContext, UUID crimeID){
        Intent i = new Intent(packageContext, CrimePagerActivity.class);
        i.putExtra(EXTRA_CRIME_ID, crimeID);
        return i;
    }

}
