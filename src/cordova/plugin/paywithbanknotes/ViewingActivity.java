package cordova.plugin.paywithbanknotes;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class ViewingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.view_activity);

		if (getIntent().getData() != null) {
			Cursor cursor = getContentResolver().query(getIntent().getData(),
					null, null, null, null);
			if (cursor.moveToNext()) {
                String link = "https://bank-notes.com/scan?sendByForm=true&contactId=" + cursor.getString(cursor.getColumnIndex("DATA7"));
				Log.i(getClass().getSimpleName(), "Link: " + link);
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
				if (i.resolveActivity(getPackageManager()) != null) {
					startActivity(i);
				}
				else {
					Log.i(getClass().getSimpleName(), "No activity found for add contact");
				}
			}
			cursor.close();
		} else {
			// How did we get here without data?
			finish();
		}
	}

}
