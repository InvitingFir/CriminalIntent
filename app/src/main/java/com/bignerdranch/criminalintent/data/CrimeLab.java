package com.bignerdranch.criminalintent.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.bignerdranch.criminalintent.database.CrimeBaseHelper;
import com.bignerdranch.criminalintent.database.CrimeCursorWrapper;
import com.bignerdranch.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sSingleton;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private CrimeLab(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
    }

    public void addCrime(Crime c){
        ContentValues values = getContentValues(c);
        mDatabase.insert(CrimeTable.NAME, null, values);
    }

    public void updateCrime(Crime c){
        String uuid = c.getID().toString();
        ContentValues values = getContentValues(c);
        mDatabase.update(CrimeTable.NAME, values,
                CrimeTable.Cols.UUID + "= ?",
                new String[]{uuid});
    }

    public void removeCrime(Crime crime){
        String uuid = crime.getID().toString();
        mDatabase.delete(CrimeTable.NAME, CrimeTable.Cols.UUID + "= ?",
                new String[]{uuid});
    }

    public static CrimeLab getInstance(Context context){
        if(sSingleton == null) sSingleton = new CrimeLab(context);
        return sSingleton;
    }

    public List<Crime> getCrimes(){
        List<Crime> crimeList = new ArrayList<>();
        CrimeCursorWrapper cursorWrapper = queryCrimes(null, null);
        try{
            cursorWrapper.moveToFirst();
            while(!cursorWrapper.isAfterLast()){
                crimeList.add(cursorWrapper.getCrime());
                cursorWrapper.moveToNext();
            }
        }
        finally {
            cursorWrapper.close();
        }
        return crimeList;
    }

    public Crime getCrime(UUID id){
        CrimeCursorWrapper cursorWrapper =
                queryCrimes(CrimeTable.Cols.UUID + "= ?", new String[]{id.toString()});
        try{
            if(cursorWrapper.getCount() == 0){
                return null;
            }
            cursorWrapper.moveToFirst();
            return cursorWrapper.getCrime();
        }
        finally {
            cursorWrapper.close();
        }
    }


    private ContentValues getContentValues(Crime c){
        ContentValues contentValues = new ContentValues();
        contentValues.put(CrimeTable.Cols.UUID, c.getID().toString());
        contentValues.put(CrimeTable.Cols.TITLE, c.getTitle());
        contentValues.put(CrimeTable.Cols.DATE, c.getDate().getTime());
        contentValues.put(CrimeTable.Cols.SOLVED, c.isSolved() ? 1 : 0);
        contentValues.put(CrimeTable.Cols.SUSPECT, c.getSuspect());
        return contentValues;
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new CrimeCursorWrapper(cursor);
    }

    public File getPhotoFile(Crime c){
        File fileDir = mContext.getFilesDir();
        return new File(fileDir, c.getPhotoFilename());
    }
}
