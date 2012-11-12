package sample.application.livewallpaper;

import com.example.livewallpaper.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.EditText;

public class Preferences extends PreferenceActivity 
			implements SharedPreferences.OnSharedPreferenceChangeListener{
	
	public SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.main);
		
		this.getPreferenceManager().setSharedPreferencesName("SlideshowWallpaperPrefs");
		this.addPreferencesFromResource(R.xml.preferences);
		this.prefs = this.getSharedPreferences("SlideshowWallpaperPrefs", MODE_PRIVATE);
		
		((EditText)this.findViewById(R.id.editText1)).setText(this.prefs.getString("Folder", ""));
		
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		Editor editor = this.prefs.edit();
		editor.putString("Folder", ((EditText)this.findViewById(R.id.editText1)).getText().toString());
		editor.commit();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		((EditText)this.findViewById(R.id.editText1)).setText(prefs.getString("Folder", ""));
		
	}
	
	public void onClick(View v){
		Intent intent = new Intent(this, FilePicker.class);
		startActivity(intent);
	}
	

}
