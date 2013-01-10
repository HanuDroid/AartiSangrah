package org.varunverma.aartisangrah;

import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;
import org.varunverma.hanu.Application.Application;
import org.varunverma.hanu.Application.HanuFragmentInterface;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

public class Main extends FragmentActivity implements PostListFragment.Callbacks, 
												PostDetailFragment.Callbacks, Invoker {

	private boolean dualPane;
	private Application app;
	private ProgressDialog dialog;
	private boolean firstUse;
	private boolean appClosing;
	private HanuFragmentInterface fragmentUI;
	private int postId;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if(savedInstanceState != null){
        	postId = savedInstanceState.getInt("PostId");
        }
        else{
        	postId = 0;
        }
        
        if (findViewById(R.id.post_list) != null) {
            dualPane = true;
        }
		else{
			dualPane = false;
		}
        
        // Tracking.
        EasyTracker.getInstance().activityStart(this);
        
        // Get Application Instance.
        app = Application.getApplicationInstance();
        
        // Set the context of the application
        app.setContext(getApplicationContext());

        // Accept my Terms
        if(!app.isEULAAccepted()){
        	// Show EULA.
        	Intent eula = new Intent(Main.this, DisplayFile.class);
        	eula.putExtra("File", "eula.html");
			eula.putExtra("Title", "End User License Aggrement: ");
			Main.this.startActivityForResult(eula, Application.EULA);
        }
        else{
        	// Start the Main Activity
       		startMainActivity();
        }
    }

    private void startMainActivity() {
		// Register application.
        app.registerAppForGCM();
                
        // Initialize app...
        if(app.isThisFirstUse()){
        	// This is the first run ! 
        	
        	String message = "Please wait while the application is initialized for first usage...";
    		dialog = ProgressDialog.show(Main.this, "", message, true);
    		app.initializeAppForFirstUse(this);
    		firstUse = true;
        }
        else{
        	firstUse = false;
        	app.initialize(this);
        	
        	// Start Main Activity.
        	startMainScreen();
        }

	}

	private void startMainScreen() {

		showWhatsNew();
		
		// Show Ad.
		AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
        adRequest.addTestDevice("E16F3DE5DF824FE222EDDA27A63E2F8A");
        AdView adView = (AdView) findViewById(R.id.adView);
        
        // Start loading the ad in the background.
        adView.loadAd(adRequest);

		// Load Posts.
		Application.getApplicationInstance().getAllPosts();

		// Create the Fragment.
		FragmentManager fm = this.getSupportFragmentManager();
		Fragment fragment;
		
		// Create Post List Fragment
		fragment = new PostListFragment();
		Bundle arguments = new Bundle();
		arguments.putInt("PostId", postId);
		arguments.putBoolean("DualPane", dualPane);
		fragment.setArguments(arguments);
				
		if(dualPane){
			arguments.putBoolean("ShowFirstItem", true);
			fm.beginTransaction().replace(R.id.post_list, fragment).commitAllowingStateLoss();
		}
		else{
			arguments.putBoolean("ShowFirstItem", false);
			fm.beginTransaction().replace(R.id.post_detail, fragment).commitAllowingStateLoss();
		}

		fragmentUI = (HanuFragmentInterface) fragment;
		
	}

	private void showWhatsNew() {
		
		if(firstUse){
			showHelp();
			return;
		}
		
		// Show what's new in this version.
		int oldFrameworkVersion = app.getOldFrameworkVersion();
		int newFrameworkVersion = app.getNewFrameworkVersion();
		
		int oldAppVersion = app.getOldAppVersion();
		int newAppVersion;
		try {
			newAppVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			newAppVersion = 0;
			Log.e(Application.TAG, e.getMessage(), e);
		}
		
		if(newAppVersion > oldAppVersion ||
			newFrameworkVersion > oldFrameworkVersion){
			
			app.updateVersion();
			
			Intent info = new Intent(Main.this, DisplayFile.class);
			info.putExtra("File", "NewFeatures.html");
			info.putExtra("Title", "What's New?");
			Main.this.startActivity(info);
			
		}
		
	}

	private void showHelp() {
		// Show Help
		EasyTracker.getTracker().trackView("/Help");
		Intent help = new Intent(Main.this, DisplayFile.class);
		help.putExtra("File", "help.html");
		help.putExtra("Title", "Help: ");
		Main.this.startActivity(help);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
	@Override
	public void onItemSelected(int id) {
		
		if (dualPane) {
            Bundle arguments = new Bundle();
            arguments.putInt("PostId", id);
            PostDetailFragment fragment = new PostDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.post_detail, fragment)
                    .commit();

        }
		else{
			Intent postDetail = new Intent(Main.this, PostDetailActivity.class);
			postDetail.putExtra("PostId", id);
			Main.this.startActivity(postDetail);
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(fragmentUI != null){
			outState.putInt("PostId", fragmentUI.getSelectedItem());
		}
	}
	
	@Override
	protected void onDestroy(){
		appClosing = true;
		app.close();
		super.onDestroy();
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()){
    	
    	case R.id.Help:
    		showHelp();
    		break;
    		
    	case R.id.About:
    		EasyTracker.getTracker().trackView("/About");
    		Intent info = new Intent(Main.this, DisplayFile.class);
			info.putExtra("File", "about.html");
			info.putExtra("Title", "About: ");
			Main.this.startActivity(info);
    		break;
    	
    	}
    	
    	return true;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	switch (requestCode) {
    	
    	case Application.EULA:
    		if(!app.isEULAAccepted()){
    			finish();
    		}
    		else{
    			// Start Main Activity
    			startMainActivity();
    		}
    		break;    	
    	}
    }
        
	@Override
	public void NotifyCommandExecuted(ResultObject result) {
		
		if(appClosing && result.getResultStatus() == ResultObject.ResultStatus.CANCELLED){
			app.close();
		}
		
		if(!result.isCommandExecutionSuccess()){
			
			if(result.getResultCode() == 420){
				// Application is not registered.
				String message = "This application is not registered with Hanu-Droid.\n" +
						"Please inform the developer about this error.";
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder
					.setTitle("Application not registered !")
					.setMessage(message)
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog,int id) {
													Main.this.finish();	}})
					.create()
					.show();
			}
			
			//Toast.makeText(getApplicationContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
		}
		
		if(firstUse){
			
			if(dialog.isShowing()){
				
				dialog.dismiss();
				startMainScreen();	// Start Main Activity.
			}
		}		
	}

	@Override
	public void ProgressUpdate(ProgressInfo progress) {
		// Show UI.
		if(progress.getProgressMessage().contentEquals("Show UI")){
			
			if(dialog.isShowing()){
				
				dialog.dismiss();
				startMainScreen();	// Start Main Activity.
			}
		}
		
		// Update UI.
		if(progress.getProgressMessage().contentEquals("Update UI")){
			app.getAllPosts();
			if(dualPane){
				fragmentUI.reloadUI();
			}
		}
		
	}

	@Override
	public void loadPostsByCategory(String taxonomy, String name) {
		
		if(taxonomy.contentEquals("category")){
			app.getPostsByCategory(name);
		}
		else if(taxonomy.contentEquals("post_tag")){
			app.getPostsByTag(name);
		}
		else if(taxonomy.contentEquals("author")){
			app.getPostsByAuthor(name);
		}
		
		this.runOnUiThread(new Runnable() {
		    public void run(){
		    	fragmentUI.reloadUI();
		    }
		});
	}
	
	@Override
	public boolean isDualPane(){
		return dualPane;
	}

}