package cz.doombringers.doomify;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;



public class HelloWidget extends AppWidgetProvider {

	private Cursor mCursor = null;

	private static final String[] COLS = new String[] {
			CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART,
			CalendarContract.Calendars.CALENDAR_DISPLAY_NAME };

	public Long start = 0L;
	public String title = "";
	
	public Boolean hasEvents = false;



	private void loadCursor(Context c) {
		
		
		SharedPreferences prefs = c.getSharedPreferences("PREFS", 0);
		long e = prefs.getLong("event", 0);
		
		
		String selection = CalendarContract.Events._ID + " = " + String.valueOf(e) + " AND "  + CalendarContract.Events.DTSTART + " >= ?";
		Time t = new Time();
		
		t.setToNow();
		String dtStart = Long.toString(t.toMillis(false));

		// t.set(59, 59, 23, t.monthDay, t.month, t.year);
		// String dtEnd = Long.toString(t.toMillis(false));
		

		String[] selectionArgs = new String[] { dtStart };

		mCursor = c.getContentResolver().query(
				CalendarContract.Events.CONTENT_URI, COLS, selection,
				selectionArgs, CalendarContract.Events.DTSTART);
		mCursor.moveToFirst();

		
		if (mCursor.getCount() > 0) {		
			hasEvents = true;
			start = mCursor.getLong(1);
			title = mCursor.getString(0);
		}
		


	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Log.d("Doomify", "onUpdate");

		loadCursor(context);

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new MyTime(context, appWidgetManager), 1,
				1000);

		/*
		 * Intent intent = new Intent(context, UpdateService.class);
		 * context.startService(intent);
		 */
	}

	private class MyTime extends TimerTask {
		RemoteViews remoteViews;
		AppWidgetManager appWidgetManager;
		ComponentName thisWidget;

		public MyTime(Context context, AppWidgetManager appWidgetManager) {
			this.appWidgetManager = appWidgetManager;
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget);
			thisWidget = new ComponentName(context, HelloWidget.class);
		}

		@Override
		public void run() {
			

			String text = "";
			
			if (hasEvents) {
				Time t = new Time();
				t.setToNow();
	
				Time t2 = new Time();
				t2.set(start);
				long cas = t2.toMillis(false) - t.toMillis(false);
	
				text = String.format(
						"%d d, %d h, %d min, %d sec",
						TimeUnit.MILLISECONDS.toDays(cas),
						TimeUnit.MILLISECONDS.toHours(cas)
								- TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS
										.toDays(cas)),
						TimeUnit.MILLISECONDS.toMinutes(cas)
								- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
										.toHours(cas)),
						TimeUnit.MILLISECONDS.toSeconds(cas)
								- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
										.toMinutes(cas)));
				Log.d("time", text);
				Log.d("time", "update");
			} else {
				text = "Calendar is empty";
			}


			//remoteViews.setTextViewText(R.id.widget_textview, text);
			remoteViews.setTextViewText(R.id.widget_title, title + "\n" + text);
			
			appWidgetManager.updateAppWidget(thisWidget, remoteViews);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d("Doomify", "onReceive");

		// v1.5 fix that doesn't call onDelete Action
		final String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else {
			super.onReceive(context, intent);
		}
	}

}