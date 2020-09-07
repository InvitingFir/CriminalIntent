package com.bignerdranch.criminalintent.data;

import java.util.Date;
import java.util.UUID;

public class Crime {
    private String mTitle;
    private UUID mID;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;
    private boolean mRequiresPolice;

    public Crime(){
        this(UUID.randomUUID());
    }

    public Crime(UUID id){
        mID = id;
        mDate = new Date();
    }

    public String getTitle() { return mTitle; }

    public UUID getID() { return mID; }

    public Date getDate() { return mDate; }

    public boolean isSolved() { return mSolved; }

    public void setTitle(String title) { mTitle = title; }

    public void setDate(Date date) { mDate = date; }

    public void setSolved(boolean solved) { mSolved = solved; }

    public boolean isRequiresPolice() {
        return mRequiresPolice;
    }

    public String getSuspect() { return mSuspect; }

    public void setSuspect(String suspect) { mSuspect = suspect; }

    public String getPhotoFilename(){
        return "IMG_"+mID.toString() + ".jpg";
    }


}
