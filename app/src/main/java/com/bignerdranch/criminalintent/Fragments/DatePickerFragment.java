package com.bignerdranch.criminalintent.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.bignerdranch.criminalintent.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment {
    public static final String ARGS_DATE = "args date";
    public static final String EXTRA_DATE = "extra date";
    private Date mDate;


    private DatePickerFragment(){}

    public static DatePickerFragment getInstance(Date date){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGS_DATE, date);
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setArguments(bundle);
        return datePickerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDate = (Date) this.getArguments().getSerializable(ARGS_DATE);
        Calendar c = Calendar.getInstance();
        c.setTime(mDate);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        View v = LayoutInflater.from(getContext()).inflate(R.layout.date_picker_layout, null);
        DatePicker datePicker = v.findViewById(R.id.date_picker);
        datePicker.init(year, month, day, null);

        Button OKButton = v.findViewById(R.id.date_picker_button);
        OKButton.setOnClickListener(l->{
            Date date = new GregorianCalendar( datePicker.getYear(),  datePicker.getMonth(), datePicker.getDayOfMonth()).getTime();
            sendResult(Activity.RESULT_OK, date);
        });
        return v;
    }

    private void sendResult(int resultCode, Date date){
        if(this.getTargetFragment() == null) return;
        Intent i = new Intent();
        i.putExtra(EXTRA_DATE, date);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
        this.onStop();
    }
}
