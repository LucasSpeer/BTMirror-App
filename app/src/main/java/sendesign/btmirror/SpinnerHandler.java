package sendesign.btmirror;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by root on 12/10/17.
 */

public class SpinnerHandler extends ThreadPoolExecutor {
    public class spinnerHandler {
        spinnerHandler() {
            Handler spinHandler = new Handler(Looper.getMainLooper()) {
            };
        }
    }
}