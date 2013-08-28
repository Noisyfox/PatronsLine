package org.foxteam.noisyfox.patronsline;

import java.io.File;

import android.app.Application;
import android.os.Environment;

public class PatronsLineApplication extends Application {

	public final static String PATH_APP_WORKDIR = Environment
			.getExternalStorageDirectory().getPath() + "/.patronsline";

	static PatronsLineApplication mInstance = null;

	SessionManager mSessionManager = new SessionManager();

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;

		File f = new File(PATH_APP_WORKDIR);
		f.mkdirs();
	}

	public static PatronsLineApplication getApplication() {
		return mInstance;
	}

	public SessionManager getSessionManager() {
		return mSessionManager;
	}

}
