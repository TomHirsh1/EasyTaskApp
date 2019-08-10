package tomhirsh2.gmail.com.easytaskapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class TaskDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "ToDoDBHelper.db";
    public static final String CONTACTS_TABLE_NAME = "todo";

    public TaskDBHelper(Context context){
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "CREATE TABLE "+CONTACTS_TABLE_NAME +
                        "(id INTEGER PRIMARY KEY, task TEXT, dateStr INTEGER, timeStr INTEGER, priority TEXT" +
                        ", location TEXT, latitude REAL, longitude REAL)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS "+CONTACTS_TABLE_NAME);
        onCreate(db);
    }

    private long getDate(String day){
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd/MM/yyyy", Locale.getDefault());
        Date date = new Date();
        try {
            date = dateFormat.parse(day);
        } catch (ParseException e) {}
        return date.getTime();
    }

    private long getTime(String hour){
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "kk:mm", Locale.getDefault());
        Date time = new Date();
        try {
            time = dateFormat.parse(hour);
        } catch (ParseException e) {}
        return time.getTime();
    }


    public boolean insertContact(String task, String dateStr, String timeStr, String priority, String location, String latitude, String longitude){
        //Date date;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("task", task);
        contentValues.put("dateStr", getDate(dateStr));
        contentValues.put("timeStr", getTime(timeStr));
        contentValues.put("priority", priority);
        contentValues.put("location", location);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);

        db.insert(CONTACTS_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean updatePriority(String id, String priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("priority", priority);
        db.update(CONTACTS_TABLE_NAME, contentValues, "id = ? ", new String[] { id } );
        return true;
    }

    public boolean updateLocation(String id, String location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("location", location);
        db.update(CONTACTS_TABLE_NAME, contentValues, "id = ? ", new String[] { id } );
        return true;
    }

    public boolean updateContact(String id, String task, String dateStr, String timeStr, String priority, String location, String latitude, String longitude){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("task", task);
        contentValues.put("dateStr", getDate(dateStr));
        contentValues.put("timeStr", getTime(timeStr));
        contentValues.put("priority", priority);
        contentValues.put("location", location);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);

        db.update(CONTACTS_TABLE_NAME, contentValues, "id = ? ", new String[] { id } );
        return true;
    }

    public boolean deleteTask(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(CONTACTS_TABLE_NAME, "id = ? ", new String[] { id }) > 0;
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from "+CONTACTS_TABLE_NAME+" order by id desc", null);
        return res;
    }

    public Cursor getDataSpecific(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from "+CONTACTS_TABLE_NAME+" WHERE id = '"+id+"' order by id desc", null);
        return res;
    }

    public String getDataLocation(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        //Cursor res =  db.rawQuery("select location from "+CONTACTS_TABLE_NAME+" WHERE id = '"+id+"' order by id desc", null);

        String sql = "select location from " + CONTACTS_TABLE_NAME;
        //String sql = "select location from " + CONTACTS_TABLE_NAME + " where id = '"+id+"'";
        //String sql = "select location from " + CONTACTS_TABLE_NAME + " where id ="+i;
        //String sql = "select location from CONTACTS_TABLE_NAME where id='"+id+"';";
        Cursor cursor = db.rawQuery(sql, null);
        //Cursor cursor = getReadableDatabase().rawQuery(sql, new String[] {id});
        String str = "try";
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            //str = cursor.getString(cursor.getColumnIndex("location"));
            str = cursor.getString(0);
            //str = "works";
        }
        cursor.close();
        return str;
        //return res;
    }

    public double getDataLatitudeValue(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        double latitudeVal = 0;
        //String sql = "select latitude from " + CONTACTS_TABLE_NAME + " where id = '"+id+"'";
        //Cursor cursor = db.rawQuery(sql, null);
        Cursor cursor =  db.rawQuery("select latitude from "+CONTACTS_TABLE_NAME+" WHERE id = '"+id+"' order by id desc", null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            latitudeVal = cursor.getDouble(0);
        }
        cursor.close();
        return latitudeVal;
    }

    public double getDataLongitudeValue(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        double longitudeVal = 0;
        Cursor cursor =  db.rawQuery("select longitude from "+CONTACTS_TABLE_NAME+" WHERE id = '"+id+"' order by id desc", null);
        if(cursor.moveToFirst()) {
            longitudeVal = cursor.getDouble(0);
            longitudeVal = 3;
        }
        cursor.close();
        return longitudeVal;
    }

    public Cursor getDataOverDue(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from "+CONTACTS_TABLE_NAME+
                " WHERE date(datetime(dateStr / 1000 , 'unixepoch', 'localtime')) < date('now', 'localtime') order by id desc", null);
        return res;
    }

    public Cursor getDataToday(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from "+CONTACTS_TABLE_NAME+
                " WHERE date(datetime(dateStr / 1000 , 'unixepoch', 'localtime')) = date('now', 'localtime') order by id desc", null);
        return res;
    }

    public Cursor getDataTomorrow(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from "+CONTACTS_TABLE_NAME+
                " WHERE date(datetime(dateStr / 1000 , 'unixepoch', 'localtime')) = date('now', '+1 day', 'localtime')  order by id desc", null);
        return res;
    }

    public Cursor getDataUpcoming(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from "+CONTACTS_TABLE_NAME+
                " WHERE date(datetime(dateStr / 1000 , 'unixepoch', 'localtime')) > date('now', '+1 day', 'localtime') order by id desc", null);
        return res;
    }

    public int getSize(){
        //SQLiteDatabase db = this.getReadableDatabase();
        int taskCount = 0;
        String sql = "select count(*) from " + CONTACTS_TABLE_NAME;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            taskCount = cursor.getInt(0);
        }
        cursor.close();
        return taskCount;
    }
}