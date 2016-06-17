package in.codesmell.smss;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Sreejith on 17/6/16.
 */
public class SampleService extends Service {
    private final static String TAG = SampleService.class.getSimpleName();
    private final static boolean DEBUG = true;

    @Override
    public void onCreate() {
        super.onCreate();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
