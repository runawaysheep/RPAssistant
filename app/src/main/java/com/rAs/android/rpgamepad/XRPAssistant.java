package com.rAs.android.rpgamepad;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import java.io.File;
import java.net.InetAddress;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XRPAssistant implements IXposedHookLoadPackage, PSGamepadHandler.OnGamepadStateChangeListener {
	
	private static final String APP_PACKAGE = "com.playstation.remoteplay";
    private static final boolean log = false;
    private static final String TAG = "RP_ASSISTANT";
    private static final String PREFS_PATH = "/data/data/com.rAs.android.rpgamepad/shared_prefs/com.rAs.android.rpgamepad_preferences.xml";

    private long prefsLoadMillis = 0;
	private static PSGamepadHandler psGamepadHandler;
    private Context context;

    public static void log(Object text){
    	if(!log) return;
    	
    	if(text instanceof Throwable) {
    		Throwable e = (Throwable)text;
    		Log.e(TAG, e.getClass().getName());
    		Log.e(TAG, e.getMessage());
    		Log.e(TAG, Log.getStackTraceString(e));
    	} else if(text == null) {
    		Log.w(TAG, "<< null >>");
    	} else {
    		Log.i(TAG, text.toString());
    	}
    }

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals(APP_PACKAGE)) return;
		
		log("remoteplay load package");



		Class<?> gamepadState = XposedHelpers.findClass("com.gaikai.client.GamepadState", lpparam.classLoader);
		Class<?> gamepadCallback = XposedHelpers.findClass("com.gaikai.client.GaikaiPlayerNativeCallbacks", lpparam.classLoader);
		
		XGamepadStateSender.init(gamepadState, gamepadCallback);
		
		
		final Class<?> activityClass = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader);

		XposedHelpers.findAndHookMethod(activityClass, "onResume", new Object[]{new XC_MethodHook() {
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Activity activity = (Activity)param.thisObject;

				XSharedPreferences prefs = new XSharedPreferences(XRPAssistant.class.getPackage().getName());
				String orientationStr = prefs.getString("screen_orientation", "-1");
				int orientation = -1;
				try{
					orientation = Integer.parseInt(orientationStr);
				} catch(Exception e) {}

				if(orientation != -1) {
					activity.setRequestedOrientation(orientation);
				}
			}
		}});
		
		XposedHelpers.findAndHookMethod(activityClass, "dispatchKeyEvent", new Object[]{KeyEvent.class, new XC_MethodHook() {
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if(psGamepadHandler == null) return;

				KeyEvent event = (KeyEvent)param.args[0];
				
				try{
					psGamepadHandler.setGamepadKeyState(event);
				} catch(Exception e) {
					log(e);    
				}
			}
		}});

		XposedHelpers.findAndHookMethod(activityClass, "dispatchGenericMotionEvent", new Object[]{MotionEvent.class, new XC_MethodHook() {
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if(psGamepadHandler == null) return;

				MotionEvent event = (MotionEvent)param.args[0];

				try{
					psGamepadHandler.setGamepadAxisState(event);
				} catch(Exception e) {
					log(e);
				}
			}
		}});

		
		// Show Info
		final Class<?> rpActivityMainClass = XposedHelpers.findClass("com.playstation.remoteplay.RpActivityMain", lpparam.classLoader);
		
		XposedHelpers.findAndHookMethod(rpActivityMainClass, "onResume", new Object[]{ new XC_MethodHook() {
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if(System.currentTimeMillis() - prefsLoadMillis < 10000)
					return;

				prefsLoadMillis = System.currentTimeMillis();

				//XposedBridge.log("prefs psGamepadHandler : " + (psGamepadHandler == null ? null : psGamepadHandler.hashCode()));

				if(psGamepadHandler != null) {
					psGamepadHandler.setOnGamepadStateChangeListener(null);
					psGamepadHandler = null;
				}

				Activity activity = (Activity) param.thisObject;
				String msg = "Load Profile Failed.";

				try {
					if (context == null)
						context = activity.getApplicationContext();

					File prefsFile = new File(PREFS_PATH);

					XposedBridge.log("prefs path = " + PREFS_PATH);

					if(prefsFile.exists()) {
						XposedBridge.log("prefs file exists.");
					} else {
						XposedBridge.log("prefs file not exists.");
					}

					XSharedPreferences prefs = new XSharedPreferences(prefsFile);

					Map<String, ?> prefsValues = prefs.getAll();
					int mappingCount = 0;

					for(String key : prefsValues.keySet()) {
						if(key.contains("analog_") || key.startsWith("button_") || key.startsWith("dpad_"))
							mappingCount++;

						XposedBridge.log("prefs [" + key + "] => [" + prefsValues.get(key) + "]");
					}

					XposedBridge.log("prefs mapping count = " + mappingCount);

					String lastProfile = null;
					if (mappingCount == 0) {
						msg = "No Mapping Profile.";
					} else {
						lastProfile = prefs.getString("last_profile", null);
						msg = lastProfile == null || lastProfile.isEmpty() ? "[ Default ]" : "[ " + lastProfile + " ]";

						psGamepadHandler = new PSGamepadHandler(activity, null, prefs);
						psGamepadHandler.setOnGamepadStateChangeListener(XRPAssistant.this);
					}

				} catch (Exception e) {
					log(e);
				} finally {
					Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();

				}
			}
		}});


		new XFakeWifiEnabler(lpparam).apply();
	}

	@Override
	public void onGamepadStateChange(boolean sensor) {
		XGamepadStateSender.applyGamepadState(sensor, psGamepadHandler);
	}

}
