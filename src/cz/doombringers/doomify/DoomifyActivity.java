package cz.doombringers.doomify;

import java.text.Format;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class DoomifyActivity extends Activity implements OnClickListener {
	private Cursor mCursor = null;

	private static final String[] COLS = new String[] {
	    CalendarContract.Events._ID,
			CalendarContract.Events.TITLE, 
			CalendarContract.Events.DTSTART,
			CalendarContract.Calendars.CALENDAR_DISPLAY_NAME };

	public Long start = 0L;
	

	public String textToShow;

	final Handler handler = new Handler();
	
	public TextView time;

	final Runnable doUpdateView = new Runnable() {
		@Override
        public void run() {
			Log.d("time", "tik");
			time.setText(textToShow);
		}
	};

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		loadCursor();

		String[] from = new String[] { CalendarContract.Events.TITLE };
		int[] to = new int[] { android.R.id.text1 };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, mCursor, from, to, 0);
		((ListView) findViewById(R.id.listView)).setAdapter(adapter);

		/*
		Button b = (Button) findViewById(R.id.next);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.previous);
		b.setOnClickListener(this);

		onClick(findViewById(R.id.previous));

		time = (TextView) findViewById(R.id.time);

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new UpdateTimeTask(), 100, 1000);
		*/

	}

	class UpdateTimeTask extends TimerTask {

		@Override
        public void run() {

			Time t = new Time();
			t.setToNow();

			Time t2 = new Time();
			t2.set(start);
			long cas = t2.toMillis(false) - t.toMillis(false);

			String text = String.format(
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
			textToShow = text;
			handler.post(doUpdateView);
			/*
			 * time.setText();
			 */
		}
	}

	private void loadCursor() {
		String selection = "" + CalendarContract.Events.DTSTART + " >= ?";
		Time t = new Time();
		t.setToNow();
		String dtStart = Long.toString(t.toMillis(false));

		// t.set(59, 59, 23, t.monthDay, t.month, t.year);
		// String dtEnd = Long.toString(t.toMillis(false));

		String[] selectionArgs = new String[] { dtStart };

		mCursor = getContentResolver().query(
				CalendarContract.Events.CONTENT_URI, COLS, selection,
				selectionArgs, CalendarContract.Events.DTSTART);
		mCursor.moveToFirst();
	}

	public void onClick(View v) {
		TextView tv = (TextView) findViewById(R.id.data);
		TextView time = (TextView) findViewById(R.id.time);
		String title = "N/A";
		String cal = "N/A";
		start = 0L;
		switch (v.getId()) {
		case R.id.next:
			if (!mCursor.isLast())
				mCursor.moveToNext();
			break;
		case R.id.previous:
			if (!mCursor.isFirst())
				mCursor.moveToPrevious();
			break;
		}
		Format df = DateFormat.getDateFormat(this);
		Format tf = DateFormat.getTimeFormat(this);
		try {
			title = mCursor.getString(0);
			cal = mCursor.getString(2);
			start = mCursor.getLong(1);
		} catch (Exception e) {
			// ignore
		}

		tv.setText(title + " on " + df.format(start) + " at "
				+ tf.format(start) + " in " + cal);

	}
}