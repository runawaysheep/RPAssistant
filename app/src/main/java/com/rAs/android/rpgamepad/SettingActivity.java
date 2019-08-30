package com.rAs.android.rpgamepad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.rAs.android.rpgamepad.InputDialog.OnMotionEventListener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setPrefsReadable();
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsPreferenceFragment()).commit();
	}

	@Override
	protected void onPause() {
		super.onPause();
		setPrefsReadable();
	}

	private void setPrefsReadable() {
		File dataDir = new File(getApplicationInfo().dataDir);
		if(dataDir.exists()) {
			dataDir.setReadable(true, false);
			dataDir.setExecutable(true, false);

			File prefsDir = new File(dataDir, "shared_prefs");
			if(prefsDir.exists()) {
				prefsDir.setReadable(true,false);
				prefsDir.setExecutable(true, false);

				File prefsFile = new File(prefsDir, getClass().getPackage().getName() + "_preferences.xml");
				if(prefsFile.exists()) {
					prefsFile.setReadable(true, false);
				}
			}
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return super.dispatchKeyEvent(event);
		}
		
		return true;
	}
	
//	@Override
//	public boolean dispatchGenericMotionEvent(MotionEvent ev) {
//		return super.dispatchGenericMotionEvent(ev);
//	}
	
	public static class SettingsPreferenceFragment extends PreferenceFragment implements OnPreferenceClickListener, OnKeyListener, OnMotionEventListener  {

		private SharedPreferences pref;
	    private LayoutInflater layoutInflater;
	    private File profilesPath;
	    
	    private List<String> orientationValues;
	    
	    private PreferenceCategory profileCategory, generalCategory, controllerCategory, mappingCategory;
	    
	    private ListPreference loadProfilePreference, orientationPreference;
	    private EditTextPreference saveProfilePreference, deadzonePreference;
	    
		private InputDialog inputDialog;
		private TextView buttonInfoTextView;
		private CheckBox isWithCombineCheckBox, isOnlyAxisCheckBox;
		private Spinner profilesSpinner;

		private Preference currPreference;

		private ArrayAdapter<String> profilesSpinnerAdapter;

		private int code;
		private String name;
		private boolean isNegative, isAxisOnly;
		
        @SuppressLint("InflateParams")
		@Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.setting);
            
            pref = getPreferenceManager().getSharedPreferences();
    		layoutInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
    		profilesPath = getActivity().getExternalFilesDir(null);
    		
            profileCategory = (PreferenceCategory)getPreferenceScreen().getPreference(0);
			generalCategory = (PreferenceCategory)getPreferenceScreen().getPreference(1);
			controllerCategory = (PreferenceCategory)getPreferenceScreen().getPreference(2);
			mappingCategory = (PreferenceCategory)getPreferenceScreen().getPreference(3);



            // Profile Category
            loadProfilePreference = (ListPreference)profileCategory.getPreference(0);
            setLoadProfileEntries();
            loadProfilePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					loadProfilePreference.setValue(null);
					return false;
				}
			});
            loadProfilePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String name = (String)newValue;
					
					if(name == null || name.isEmpty()) return false;

					final File file = new File(profilesPath, name);
					
					if(file.exists()) {
						loadProfile(file);
					}
					
					return true;
				}
			});


            
            
            saveProfilePreference = (EditTextPreference)profileCategory.getPreference(1);
            saveProfilePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String newName = (String)newValue;
					
					if(newName == null || newName.isEmpty()) return false;
					
					final File file = new File(profilesPath, newName);
					
					if(file.exists()) {
						new AlertDialog.Builder(getActivity())
							.setMessage(R.string.msg_overwrite)
							.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									saveProfile(file);
								}
							})
							.setNegativeButton(android.R.string.no, null)
							.show();
					} else {
						saveProfile(file);
					}
					return true;
				}
			});

            // new profile
			profileCategory.getPreference(2).setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					new AlertDialog.Builder(getActivity())
							.setMessage(R.string.msg_new_profile)
							.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									pref.edit().clear().commit();
									setPrerenceValues();
								}
							})
							.setNegativeButton(android.R.string.no, null)
							.show();
					return false;
				}
			});



			// General Category
            orientationPreference = (ListPreference)generalCategory.getPreference(0);
            
            orientationValues = Arrays.asList(getResources().getStringArray(R.array.settings_entries_value_orientation));
            
    		orientationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					int orientationIdx = orientationValues.indexOf(newValue);
		            if(orientationIdx < 0) orientationIdx = 0;
		            preference.setSummary(getResources().getStringArray(R.array.settings_entries_title_orientation)[orientationIdx]);
		            
					return true;
				}
			});


    		// Controller Category
			deadzonePreference = (EditTextPreference) controllerCategory.getPreference(0);
			deadzonePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					try{
						preference.setSummary(Integer.parseInt(newValue.toString()) + "%");
						return true;
					} catch(Exception e) {
						return false;
					}

				}
			});
			deadzonePreference.getEditText().setFilters(new InputFilter[]{new InputFilter() {
				@Override
				public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
					try {
						int input = Integer.parseInt(dest.toString() + source.toString());
						if (isInRange(0, 100, input))
							return null;
					} catch (NumberFormatException nfe) { }
					return "";
				}
				private boolean isInRange(int a, int b, int c) {
					return b > a ? c >= a && c <= b : c >= b && c <= a;
				}
			}});

			Preference controllerTestPreference = controllerCategory.getPreference(1);
			controllerTestPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference) {
					startActivity(new Intent(getActivity(), TestActivity.class));
					return true;
				}
			});




			// Mapping Category
            for(int i = 0; i < mappingCategory.getPreferenceCount(); i++) {
            	Preference preference = mappingCategory.getPreference(i);
            	preference.setOnPreferenceClickListener(this);
            }

            
            setPrerenceValues();
            
            
            
            
            
            
            View inputDialogView = layoutInflater.inflate(R.layout.input_dialog, null);
			
            buttonInfoTextView = inputDialogView.findViewById(R.id.textViewButtonInfo);
            
			isWithCombineCheckBox = inputDialogView.findViewById(R.id.checkBoxWithCombineButton);
			
			isOnlyAxisCheckBox = inputDialogView.findViewById(R.id.checkBoxAxisOnly);
			isOnlyAxisCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					isAxisOnly = isChecked;
				}
			});

			profilesSpinner = inputDialogView.findViewById(R.id.spinnerProfiles);
			profilesSpinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, android.R.id.text1);
			profilesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			profilesSpinner.setAdapter(profilesSpinnerAdapter);

			inputDialog = new InputDialog(getActivity());
			inputDialog.setView(inputDialogView);
			inputDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(code != -1 && name != null) {
						String value = name + ((name.startsWith("AXIS_") && isNegative) ? "-" : "") + "|" + code;
						if(isWithCombineCheckBox.isChecked()) value = "@" + value;

						if(profilesSpinner.getVisibility() == View.VISIBLE) {
							value += "|" + profilesSpinner.getSelectedItem();
						}

						Editor editor = currPreference.getEditor();
						editor.putString(currPreference.getKey(), value);
						editor.apply();
						
						currPreference.setSummary(value);
					}
					inputDialog.dismiss();
				}
			});
			inputDialog.setOnKeyListener(this);
			inputDialog.setOnInputListener(this);
			inputDialog.setNegativeButton(android.R.string.cancel, null);
			inputDialog.setNeutralButton(R.string.delete,  new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Editor editor = currPreference.getEditor();
					editor.remove(currPreference.getKey());
					editor.apply();
					
					currPreference.setSummary(" ");
					inputDialog.dismiss();
				}
			});
			
			inputDialog.setCancelable(false);
			
        }

		private void setPrerenceValues() {
			saveProfilePreference.setText(pref.getString(saveProfilePreference.getKey(), ""));

			int orientationIdx = orientationValues.indexOf(pref.getString(orientationPreference.getKey(), "-1"));
            if(orientationIdx < 0) orientationIdx = 0;
    		orientationPreference.setSummary(getResources().getStringArray(R.array.settings_entries_title_orientation)[orientationIdx]);
			orientationPreference.setValueIndex(orientationIdx);

			String deadzone = pref.getString(deadzonePreference.getKey(), "0");
            deadzonePreference.setText(deadzone);
			deadzonePreference.setSummary(deadzone + "%");

            for(int i = 0; i < mappingCategory.getPreferenceCount(); i++) {
            	Preference preference = mappingCategory.getPreference(i);
            	preference.setSummary(pref.getString(preference.getKey(), " "));
            }
		}

		private void setLoadProfileEntries() {
			String[] entries = null;
			if(profilesPath.exists()) {
				File[] filesList = profilesPath.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return !pathname.isDirectory();
					}
				});
				entries = new String[filesList.length];
				for(int i = 0; i < filesList.length; i++) {
					entries[i] = filesList[i].getName();
				}
			}
			if(entries == null)
				entries = new String[0];

			Arrays.sort(entries);

			loadProfilePreference.setEntries(entries);
			loadProfilePreference.setEntryValues(entries);
		}

		@Override
		public boolean onPreferenceClick(Preference preference) {
			code = -1;
			name = null;
			isNegative = false;
			boolean isWithCombine = false;
			//boolean isAxis = false;
			boolean isAxisOnly = false;
			boolean isChangeProfile = preference.getKey().startsWith("switch_profile");
			boolean isButtonCombine = preference.getKey().equals(getText(R.string.settings_key_button_combine));
			String profileName = null;

			currPreference = preference;

			String value = "Wait...";
			String summary = (String)preference.getSummary();
			if(summary.contains("|")) {
				String[] values = summary.split("\\|");
				
				name = values[0];
				code = Integer.parseInt(values[1]);
				if(name.startsWith("@")) {
					isWithCombine = true;
					name = name.substring(1);
				}
				if(name.endsWith("-")) {
					isNegative = true;
					name = name.substring(0, name.length() - 1);
				}
				
				value = name;
				if(name.startsWith("AXIS_")) {
					value += isNegative ? "(-)" : "(+)";
					isAxisOnly = true;
				}
//				isAxis = preference.getKey().contains("analog");
//				if(!isAxis)
//					isAxisOnly = false;

				if(values.length >= 3) {
					profileName = values[2];
				}
			}
			
			inputDialog.setTitle(preference.getTitle() + "...");

			isWithCombineCheckBox.setVisibility(isButtonCombine ? View.INVISIBLE : View.VISIBLE);
			isWithCombineCheckBox.setChecked(isWithCombine);
            isOnlyAxisCheckBox.setVisibility(isButtonCombine ? View.INVISIBLE : View.VISIBLE);
			isOnlyAxisCheckBox.setChecked(isAxisOnly);

			profilesSpinner.setVisibility(isChangeProfile ? View.VISIBLE : View.GONE);
			if(isChangeProfile) {
				profilesSpinnerAdapter.clear();

				int idx = -1;
				for(int i = 0; i < loadProfilePreference.getEntryValues().length; i++) {
					String profile = (String)loadProfilePreference.getEntryValues()[i];
					if(profileName != null && profileName.equals(profile)) {
						idx = i;
					}
					profilesSpinnerAdapter.add(profile);
				}

				profilesSpinnerAdapter.notifyDataSetChanged();

				if(idx != -1) {
					profilesSpinner.setSelection(idx);
				}
			}
			
			buttonInfoTextView.setText(value);
			
			inputDialog.show();
			
			return false;
		}
		
		@Override
		public boolean onGenericMotionEvent(MotionEvent event) {
			if(isWithCombineCheckBox.getVisibility() == View.INVISIBLE) return true; // Combine은 AXIS로 안됨
			
			int action = event.getAction();
			
			if(action != KeyEvent.ACTION_MULTIPLE) return true;

	        @SuppressWarnings("deprecation")
			int pointerIndex = (action & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;

	        int code = -1;
	        float value = 0;
	        String name = null;
	    
	        for(int axis : PSGamepadHandler.AXISES) {
	        	float v = event.getAxisValue(axis, pointerIndex);
				if(axis == MotionEvent.AXIS_RX || axis == MotionEvent.AXIS_RY) {
					v = (v + 1) / 2f;
				} else if((axis == MotionEvent.AXIS_BRAKE || axis == MotionEvent.AXIS_GAS) && v < 0) {
					v = 0;
				}

	        	if(Math.abs(v) > 0.5f) {
	        		value = v;
	        		code = axis;
	        		for(Field f : MotionEvent.class.getFields()) {
	        			try {
	        				if(f.getName().startsWith("AXIS_") && (int)(Integer)f.get(null) == code) {
	        					name = f.getName();
	        					break;
	        				}
	        			} catch (Exception e) {
	        			}
	        		}
	        		break;
	        	}
	        }
	        
	        if(code == -1) return false;
	        
			if(name == null)
				name = "AXIS_" + "0x" + Integer.toHexString(code);
			
			isNegative = value < 0;
			buttonInfoTextView.setText(name + (isNegative ? "(-)" : "(+)"));

			this.code = code;
			this.name = name;
			//Log.w("PSR_TOUCH", name);

			return true;

		}

		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if(isAxisOnly) return true;
			
			if(event.getAction() != KeyEvent.ACTION_DOWN) return true;
			
			int code = event.getKeyCode();
			String name = null;
			
			for(Field f : KeyEvent.class.getFields()) {
				try {
					if(f.getName().startsWith("KEYCODE_") && (int)(Integer)f.get(null) == code) {
						name = f.getName().substring(8);
						break;
					}
				} catch (Exception e) {
				}
			}
			
			if(name == null)
				name = "KEYCODE_" + "0x" + Integer.toHexString(code);
			
			buttonInfoTextView.setText(name);
			
			this.code = code;
			this.name = name;
			
			//Log.w("PSR_TOUCH", name);
			
			return true;
		}

		private boolean loadProfile(File file) {
			BufferedReader br = null;
			try{
				Editor editor = pref.edit();
				editor.clear();
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line;
				while((line = br.readLine()) != null) {
					int idx = line.indexOf("=");
					if(idx > 0) {
						String key = line.substring(0, idx);
						String value = line.substring(idx + 1);
						if(getActivity().getText(R.string.settings_key_profile_save).equals(key)) {
							value = file.getName();
						}
						editor.putString(key, value);
					}
				}
				
				editor.apply();
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(getActivity(), R.string.msg_load_fail, Toast.LENGTH_SHORT).show();
				return false;
			} finally {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
			
			setPrerenceValues();

			saveProfilePreference.setText(file.getName());
			
			Toast.makeText(getActivity(), R.string.msg_load_ok, Toast.LENGTH_SHORT).show();
			
			return true;
		}
		
		private boolean saveProfile(File file) {
			Map<String, ?> prefValues = pref.getAll();
			PrintWriter pw = null;
			try{
				pw = new PrintWriter(file);
				for(String key : prefValues.keySet()) {
					pw.print(key);
					pw.print("=");
					pw.println(prefValues.get(key));
				}
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(getActivity(), R.string.msg_save_fail, Toast.LENGTH_SHORT).show();
				return false;
			} finally {
				try {
					pw.close();
				} catch (Exception e){
				}
			}
			
			setLoadProfileEntries();
			
			Toast.makeText(getActivity(), R.string.msg_save_ok, Toast.LENGTH_SHORT).show();
			return true;
		}
    }

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
