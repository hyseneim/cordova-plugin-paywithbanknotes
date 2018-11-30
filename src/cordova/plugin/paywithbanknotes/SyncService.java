package cordova.plugin.paywithbanknotes;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();

    private static SyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
    	Log.i(getClass().getSimpleName(), "Sync service created");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
    	Log.i(getClass().getSimpleName(), "Sync service binded");
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
