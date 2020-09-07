package com.bignerdranch.criminalintent.Activities;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.bignerdranch.criminalintent.Fragments.CrimeFragment;
import com.bignerdranch.criminalintent.Fragments.CrimeListFragment;
import com.bignerdranch.criminalintent.R;
import com.bignerdranch.criminalintent.data.Crime;

public class CrimeListActivity extends SingleFragmentActivity
        implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks{

    @Override
    public void onCrimeSelected(Crime crime) {
        if(findViewById(R.id.detail_fragment_container) == null ){
            Intent intent  = CrimePagerActivity.newIntent(this, crime.getID());
            startActivity(intent);
        }
        else{
            Fragment fragment = CrimeFragment.getInstance(crime.getID());
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.detail_fragment_container, fragment).commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime c) {
        CrimeListFragment fragment = (CrimeListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        fragment.updateUI();
    }

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getResLayout() {
        return R.layout.activity_masterdetail;
    }
}
