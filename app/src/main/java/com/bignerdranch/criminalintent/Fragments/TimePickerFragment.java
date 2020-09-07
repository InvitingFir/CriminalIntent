package com.bignerdranch.criminalintent.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.bignerdranch.criminalintent.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimePickerFragment extends DialogFragment {
    private static final String ARGS_TIME = "args date";
    public static final String EXTRA_TIME = "extra time";
    private TimePicker mTimePicker;
    private Date mTime;

    private TimePickerFragment(){};

    public static TimePickerFragment getInstance(Date date){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGS_TIME, date);
        TimePickerFragment tpf = new TimePickerFragment();
        tpf.setArguments(bundle);
        return tpf;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mTime = (Date)getArguments().getSerializable(ARGS_TIME);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mTime);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        View v = LayoutInflater.from(getContext()).inflate(R.layout.time_picker_layout, null, false);
        mTimePicker = v.findViewById(R.id.time_picker);
        mTimePicker.setIs24HourView(true);
        mTimePicker.setHour(hour);
        mTimePicker.setMinute(minutes);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle(R.string.time_picker_title);
        alertDialog.setView(v);
        alertDialog.setPositiveButton(android.R.string.ok, ((dialog, which) ->{
            Calendar c = new GregorianCalendar();
            c.setTime(mTime);
            c.set(Calendar.HOUR_OF_DAY, mTimePicker.getHour());
            c.set(Calendar.MINUTE, mTimePicker.getMinute());
            setResult(Activity.RESULT_OK, c.getTime());
        }));
        return alertDialog.create();
    }

    private void setResult(int ResultCode, Date time){
        if(time == null) return;
        Intent i = new Intent();
        i.putExtra(EXTRA_TIME, time);
        getTargetFragment().onActivityResult(getTargetRequestCode(), ResultCode, i);
    }
}
