package com.bignerdranch.criminalintent.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.*;

import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.bignerdranch.criminalintent.Util.PictureUtils;
import com.bignerdranch.criminalintent.data.Crime;
import com.bignerdranch.criminalintent.data.CrimeLab;
import com.bignerdranch.criminalintent.R;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final int REQUEST_PHOTO = 4;
    private static final int REQUEST_PHONE_NUMBER = 3;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_TIME_PICKER = 1;
    private static final int REQUEST_DATE_PICKER = 0;
    private static boolean PHONE_NUMBER_PERMISSION_GRANTED = false;
    private Crime mCrime;
    private File mCrimePhotoPath;
    private EditText mTitleEdit;
    private CheckBox mIsSolvedCheckBox;
    private ImageView mCrimeImage;
    private Button mDateButton;
    private Button mTimeButton;
    private Button mSendCrimeButton;
    private Button mSuspectButton;
    private Button mCallSuspectButton;
    private ImageButton mCrimeCamera;
    private Callbacks mCallbacks;

    public interface Callbacks{
        void onCrimeUpdated(Crime c);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        UUID id = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.getInstance(getContext()).getCrime(id);
        mCrimePhotoPath = CrimeLab.getInstance(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        init(v);
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.getInstance(getContext()).updateCrime(mCrime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private void init(View v){
        mCrimeImage = v.findViewById(R.id.crime_photo);
        Bitmap bitMap = PictureUtils.getScaledBitmap(mCrimePhotoPath.getPath(), getActivity());
        mCrimeImage.setImageBitmap(bitMap);

        mCrimeCamera = v.findViewById(R.id.crime_camera);
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canCapture = mCrimePhotoPath != null &&
                captureIntent.resolveActivity(getActivity().getPackageManager()) != null;
        mCrimeCamera.setEnabled(canCapture);
        mCrimeCamera.setOnClickListener(l->{
            Uri uri = FileProvider.getUriForFile(getActivity(), "com.bignerdranch.criminalintent.fileprovider", mCrimePhotoPath);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            List<ResolveInfo> cameraActivities = getActivity().getPackageManager().
                    queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for(ResolveInfo activity:cameraActivities){
                getActivity().grantUriPermission(activity.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            startActivityForResult(captureIntent, REQUEST_PHOTO);
        });

        mCallSuspectButton = v.findViewById(R.id.call_suspect);
        mCallSuspectButton.setEnabled(mCrime.getSuspect()!=null);
        mCallSuspectButton.setOnClickListener(l->{
            int hasContactPermission = ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.READ_CONTACTS);
            if(hasContactPermission == PackageManager.PERMISSION_GRANTED){
                PHONE_NUMBER_PERMISSION_GRANTED = true;
            }
            else
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_PHONE_NUMBER);
            if(PHONE_NUMBER_PERMISSION_GRANTED){
                dialPhone();
            }
        });

        mSuspectButton = v.findViewById(R.id.crime_suspect);
        if(mCrime.getSuspect() != null)
            mSuspectButton.setText(mCrime.getSuspect());
        mSuspectButton.setOnClickListener(l ->{
            Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(i, REQUEST_CONTACT);
        });

        mSendCrimeButton = v.findViewById(R.id.crime_report);
        mSendCrimeButton.setOnClickListener(l->{
            Intent intent = ShareCompat.IntentBuilder.from(getActivity()).getIntent();
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
            intent.putExtra(Intent.EXTRA_SUBJECT, R.string.crime_report_subject);
            intent = Intent.createChooser(intent, getString(R.string.send_report));
            startActivity(intent);
        });

        mDateButton = v.findViewById(R.id.crimeDateButton);
        mDateButton.setOnClickListener(l ->{
            DatePickerFragment dpf = DatePickerFragment.getInstance(mCrime.getDate());
            dpf.setTargetFragment(this, REQUEST_DATE_PICKER);
            FragmentManager fm = getFragmentManager();
            dpf.show(fm, DIALOG_DATE);
        });
        updateDateButton();

        mTimeButton = v.findViewById(R.id.crime_time_button);
        mTimeButton.setOnClickListener(l -> {
            TimePickerFragment tpf = TimePickerFragment.getInstance(mCrime.getDate());
            tpf.setTargetFragment(this, REQUEST_TIME_PICKER);
            FragmentManager fm = getFragmentManager();
            tpf.show(fm, DIALOG_TIME);
        });
        updateTimeButton();

        mIsSolvedCheckBox = v.findViewById(R.id.solvedCheckBox);
        mIsSolvedCheckBox.setChecked(mCrime.isSolved());
        mIsSolvedCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    mCrime.setSolved(isChecked);
                    updateCrime();
                });
        mTitleEdit = v.findViewById(R.id.titleEditText);
        mTitleEdit.setText(mCrime.getTitle());
        mTitleEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void updateDateButton() {
        String date = DateFormat.format("dd.MM.yyyy (EEEE)", mCrime.getDate()).toString();
        mDateButton.setText(date);
    }

    private void updateTimeButton(){
        String time = DateFormat.format("kk:mm", mCrime.getDate()).toString();
        mTimeButton.setText(time);
    }

    public static Fragment getInstance(UUID crimeID){
        Bundle b = new Bundle();
        b.putSerializable(ARG_CRIME_ID, crimeID);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_DATE_PICKER)
            if(resultCode == Activity.RESULT_OK) {
                Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                mCrime.setDate(date);
                updateDateButton();
                updateCrime();
                return;
            }
        if(requestCode == REQUEST_TIME_PICKER)
            if(resultCode == Activity.RESULT_OK) {
                Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
                mCrime.setDate(date);
                updateTimeButton();
                updateCrime();
            }
        if(requestCode == REQUEST_PHOTO){
            Uri uri = FileProvider.getUriForFile(getContext(),
                    "com.bignerdranch.criminalintent.fileprovider", mCrimePhotoPath);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Bitmap bitMap = PictureUtils.getScaledBitmap(mCrimePhotoPath.getPath(), getActivity());
            mCrimeImage.setImageBitmap(bitMap);
        }
        if(requestCode == REQUEST_CONTACT && data != null){
            Uri contacts = data.getData();
            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
            Cursor c = getActivity().getContentResolver().query(contacts, queryFields, null, null, null);
            try{
                if(c.getCount() == 0) return;
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
            }
            finally {
                c.close();
            }
            mCallSuspectButton.setEnabled(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.remove_crime:
                CrimeLab cl = CrimeLab.getInstance(getActivity());
                cl.removeCrime(mCrime);
                getActivity().finish();
                return true;
            default:return super.onOptionsItemSelected(item);
        }
    }

    public String getCrimeReport(){
        String solvedString = (mCrime.isSolved())?
                getString(R.string.crime_report_solved):getString(R.string.crime_report_unsolved);
        String dateString = DateFormat.format("EEE, MMM dd", mCrime.getDate()).toString();
        String suspectString = mCrime.getSuspect();
        suspectString = (suspectString==null)?
                getString(R.string.crime_report_no_suspect):getString(R.string.crime_report_suspect, suspectString);
        return getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspectString);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PHONE_NUMBER){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                PHONE_NUMBER_PERMISSION_GRANTED = true;
            }
            else
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void dialPhone(){
        String contactId;
        String phoneNumber;
        Uri contacts = ContactsContract.Contacts.CONTENT_URI;
        String selection = ContactsContract.Contacts.DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{mCrime.getSuspect()};
        String[] display = new String[]{ContactsContract.Contacts._ID};
        Cursor c = getActivity().getContentResolver().query(contacts, display, selection, selectionArgs, null);
        try{
            c.moveToFirst();
            contactId = c.getString(0);
        }
        finally {
            c.close();
        }
        Uri phoneURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] phoneNumberQueryFields = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        String phoneWhereClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "= ?";
        String[] phoneQueryParameters = new String[]{contactId};
        c = getActivity().getContentResolver()
                .query(phoneURI,
                        phoneNumberQueryFields,
                        phoneWhereClause,
                        phoneQueryParameters,
                        null);
        try{
                c.moveToFirst();
                phoneNumber = c.getString(0);
        }
        finally {
            c.close();
        }
        Uri numberUri = Uri.parse("tel:" + phoneNumber);
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(numberUri);
        startActivity(intent);
    }

    private void updateCrime(){
        CrimeLab.getInstance(getContext()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }
}
