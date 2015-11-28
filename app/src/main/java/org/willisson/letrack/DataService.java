package org.willisson.letrack;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by pace on 11/28/15.
 */
public class DataService extends IntentService {
    public final String TAG = "LEtrack";
    static boolean running;

    public DataService() {
        super("DataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (running) {
            Log.i(TAG, "strange: trying to restart data service");
            return;
        }
        Log.i(TAG, "starting data service");
        running = true;

        while (true) {
            get_datapoint();
            int interval = 10; /* seconds */

            try {
                Thread.sleep(interval * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    void get_datapoint() {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        Log.i (TAG, "timestamp " + ts);
    }
}
