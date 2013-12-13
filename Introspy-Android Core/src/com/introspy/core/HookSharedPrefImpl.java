package com.introspy.core;

import java.util.Map;

import android.content.SharedPreferences;
import android.util.Log;

class Intro_PREF_PARENT extends IntroHook {
	protected static boolean 
		_allPrefsAlreadyRetrieved = false;
}

class Intro_CHECK_SHARED_PREF extends IntroHook { 
	protected static boolean _onlyRetrievedPrefOnce = true;
	protected static boolean _prefRetrieved = true; // true means it's not dumped
	@SuppressWarnings("deprecation")
	public void execute(Object... args) {
		
		// this is noisy so only display data when there is a potential issue
		if (!_onlyRetrievedPrefOnce || 
				(_onlyRetrievedPrefOnce && !_prefRetrieved)) {
			String prefName = (String) args[0];
			_l.logParameter("Preference Name", args[0]);
			_l.logLine("### PREF:"+ApplicationConfig.getPackageName() + 
					":getSharedPref:"+prefName);
			// display the pref retrieved
			try {
				SharedPreferences prefs = (SharedPreferences) 
						_hookInvoke(args);
				if (prefs != null && prefs.getAll().size() > 0)
					_l.logFlush_I("-> " + prefs.getAll());
			} catch (Throwable e) {
				_l.logLine("-> not able to retrieve preferences");
			}
			_prefRetrieved = true;
		}
		
		// arg1 is the sharing modes
		Integer mode = (Integer) args[1];
		String smode = "";
		
		if (mode == android.content.Context.MODE_WORLD_READABLE)
			smode = "MODE_WORLD_READABLE";
		else if (mode == android.content.Context.MODE_WORLD_WRITEABLE)
			smode = "MODE_WORLD_WRITEABLE";
		
		if (!smode.isEmpty()) {
			_l.logParameter("Preference Name", args[0]);
			_l.logParameter("Mode: ", smode);
			_l.logFlush_W("Shared preference accessible to the WORLD. " +
					"(MODE: " + smode + ")");
		}
	}
}

class Intro_GET_SHARED_PREF extends IntroHook { 
	public void execute(Object... args) {		
			String prefName = (String) args[0]; // name of pref to retrieve
			if (prefName == null) {
				return;
			}
			// args[1] is the default value 
			
			_l.logParameter("Preference Name", args[0]);
			String out = "### PREF:"+_packageName + 
							":getSharedPref:"+ _methodName +
							"; name: [" + args[0] + "]" +
							", default: [" + args[1] + "]";
			
			Object o = null;
			try {
				o = _hookInvoke(args);
			} catch (Throwable e) {
				// this may throw if incorrect type specified in the code
				// Log.w("IntrospyLog", "error in Intro_GET_SHARED_PREF: "+e);
			}
			
			if (o != null) {
				out += "; retrieves: ["+o+"]";
				_l.logReturnValue("Value", o);
				_l.logFlush_I(out);
			}
			else {
				_l.logLine(out);
				_l.logFlush_I("-> Preference not found or incorrect type specified");
			}
	}
}

class Intro_PUT_SHARED_PREF extends IntroHook { 
	public void execute(Object... args) {
		
		String prefName = (String) args[0]; // name of pref to retrieve
		_l.logParameter("Preference Name", args[0]);
		_l.logParameter("Value", args[1]);
		String out = "### PREF:"+ApplicationConfig.getPackageName() + ":writeSharedPref:"
						+prefName+", value: "+args[1];
		_l.logFlush_I(out);
	}
}

class Intro_CONTAINS_SHARED_PREF extends IntroHook { 
	public void execute(Object... args) {
		String out = "";
		String prefName = (String) args[0]; // name of pref to retrieve

		try {
			boolean o = (Boolean) _hookInvoke(args);
			_l.logParameter("Preference Name", args[0]);
			if (o == false) {
				out = "### PREF:"+ApplicationConfig.getPackageName()+
						":contains:"+ prefName;
				_l.logReturnValue("Value", o);
				_l.logLine(out);
				_l.logFlush_W("Preference not found (Hidden pref?)");
			}
		} catch (Throwable e) {
			Log.i("IntrospyLog", "error in Intro_CONTAINS_SHARED_PREF: "+e);
		}
	}
}

class Intro_GET_ALL_SHARED_PREF extends Intro_PREF_PARENT { 
	public void execute(Object... args) {
		// display all the prefs retrieved
		// only do it once because it's too noisy
		if (_allPrefsAlreadyRetrieved)
			return;
		try {
			@SuppressWarnings("unchecked")
			Map<String,?> keys = (Map<String, ?>) _hookInvoke(args);
			if ( keys != null && keys.size() > 0) {
				_l.logLine("### PREF:"+ApplicationConfig.getPackageName()+":getAll:");
				for(Map.Entry<String,?> entry : keys.entrySet()){
		            _l.logLine("-> " + entry.getKey() + ": " + 
		                                   entry.getValue().toString());            
				}
			}
			_l.logFlush_I();
			_allPrefsAlreadyRetrieved = true;
		} catch (Throwable e) {
			Log.i(_TAG_ERROR, "-> not able to retrieve all " +
					"preferences (get All in " + 
					ApplicationConfig.getPackageName()+"). Error: " + e);
		}
	}
}