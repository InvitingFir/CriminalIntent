package com.bignerdranch.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import com.bignerdranch.criminalintent.data.Crime;

import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {
    public CrimeCursorWrapper(Cursor cursor){
        super(cursor);
    }

    public Crime getCrime(){
        String uuidString = getString(getColumnIndex(CrimeDbSchema.CrimeTable.Cols.UUID));
        String titleString = getString(getColumnIndex(CrimeDbSchema.CrimeTable.Cols.TITLE));
        long dateLong = getLong(getColumnIndex(CrimeDbSchema.CrimeTable.Cols.DATE));
        int isSolvedInt = getInt(getColumnIndex(CrimeDbSchema.CrimeTable.Cols.SOLVED));
        String suspectString = getString(getColumnIndex(CrimeDbSchema.CrimeTable.Cols.SUSPECT));

        Crime c = new Crime(UUID.fromString(uuidString));
        c.setTitle(titleString);
        c.setDate(new Date(dateLong));
        c.setSolved(isSolvedInt != 0);
        c.setSuspect(suspectString);
        return c;
    }
}
