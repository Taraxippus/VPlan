package com.taraxippus.vplan;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper
{
	public DBHelper(Context context)
	{
		super(context, "Database.db" , null, 1);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(
			"create table vplan" +
			"(id integer primary key, grade text, period integer, content text, type integer)"
		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS vplan");
		onCreate(db);
	}
	
	public ArrayList<String> getGrades(int type)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT * FROM vplan WHERE type = ?", new String[] {"" + type});
		if (res.getCount() <= 0)
		{
			res.close();
			return new ArrayList<String>();
		}

		res.moveToFirst();
		ArrayList<String> grades = new ArrayList<>();
		String grade;
		do
		{
			grade = res.getString(res.getColumnIndex("grade"));
			if (!grades.contains(grade))
				grades.add(grade);
		}
		while (res.moveToNext());
		
		res.close();
		return grades;
	}
	
	public ArrayList<String[]> getEntries(String grade, int type)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT * FROM vplan WHERE grade = ? AND type = ? ORDER BY period", new String[] {grade, "" + type});
		if (res.getCount() <= 0)
		{
			res.close();
			return new ArrayList<String[]>();
		}

		res.moveToFirst();
		ArrayList<String[]> entries = new ArrayList<>();
		do
		{
			entries.add(new String[] {res.getInt(res.getColumnIndex("period")) + ".", res.getString(res.getColumnIndex("content"))});
		}
		while (res.moveToNext());

		res.close();
		return entries;
	}
	
	public void delete(int type)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("vplan", "type = ?", new String[]{"" + type});
	}
	
	public boolean exists(String grade, int period, int type)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT * FROM vplan WHERE grade = ? AND period = ? AND type = ?", new String[] {grade, "" + period, "" + type});
		if (res.getCount() <= 0)
		{
			res.close();
			return false;
		}

		res.close();
		return true;
	}
	
	public String getContent(String grade, int period, int type)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery( "SELECT * FROM vplan WHERE grade = ? AND period = ? AND type = ?", new String[] {grade, "" + period, "" + type});
		if (res.getCount() <= 0)
		{
			res.close();
			return "";
		}
			
		res.moveToFirst();
		String content = res.getString(res.getColumnIndex("content"));
		res.close();
		return content;
	}
	
	public void add(String grade, int period, String text, int type)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		if (exists(grade, period, type))
		{
			String old = getContent(grade, period, type);
			contentValues.put("content", old.isEmpty() ? text : old + "\\" + text);
			db.update("vplan", contentValues, "grade = ? AND period = ? AND type = ?", new String[] {grade, "" + period, "" + type});
		}
		else
		{
			contentValues.put("grade", grade);
			contentValues.put("period", period);
			contentValues.put("content", text);	
			contentValues.put("type", type);
			db.insert("vplan", null, contentValues);
		}
	}
}
