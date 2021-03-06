package org.varunverma.aartisangrah;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.fragment.app.Fragment;

import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.HanuFragmentInterface;
import com.ayansh.hanudroid.HanuGestureAnalyzer;
import com.ayansh.hanudroid.HanuGestureListener;
import com.ayansh.hanudroid.Post;

public class PostDetailFragment extends Fragment implements HanuFragmentInterface, HanuGestureListener {

	private Post post;
	private WebView wv;
	private Callbacks activity = sDummyCallbacks;
	private int position;
	private Application app;
	
	public interface Callbacks {
		public void loadPostsByCategory(String taxonomy, String name);
		public boolean isDualPane();
	}
	
	private static Callbacks sDummyCallbacks = new Callbacks() {

		@Override
		public void loadPostsByCategory(String taxonomy, String name) {			
		}

		@Override
		public boolean isDualPane() {
			return false;
		}
		
    };
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		app = Application.getApplicationInstance();
		
		if(app.getPostList().isEmpty()){
			return;
		}
		
		if(getArguments() != null){
			if (getArguments().containsKey("PostId")) {
				int index = getArguments().getInt("PostId");
	        	if(index >= app.getPostList().size()){
	        		index = app.getPostList().size() - 1;
	        	}
	            post = app.getPostList().get(index);
	        }
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.post_detail, container, false);
		
		wv = (WebView) rootView.findViewById(R.id.webview);
		
		WebSettings webSettings = wv.getSettings();
		webSettings.setJavaScriptEnabled(true);
		wv.addJavascriptInterface(new PostJavaScriptInterface(), "Main");
		wv.setBackgroundColor(Color.TRANSPARENT);
		
		// Fling handling
		if(!activity.isDualPane()){
			
			final GestureDetector detector = new GestureDetector(getActivity().getApplicationContext(), new HanuGestureAnalyzer(this));
			wv.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View view, MotionEvent e) {
					detector.onTouchEvent(e);
					return false;
				}
			});
		}
		
		showPost();
		
		return rootView;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        this.activity = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = sDummyCallbacks;
    }
    
	@Override
	public void reloadUI() {
		// Reloading the UI
		post = app.getPostList().get(0);	
	}

	@Override
	public int getSelectedItem() {
		return position;
	}

	private void showPost() {
		
		String html = "";
		if(post != null){
			html = getHTMLCode(post);
		}
		wv.loadDataWithBaseURL("fake://not/needed", html, "text/html", "UTF-8", "");
		
	}

	static String getHTMLCode(Post post) {
		// Create HTML Code.
		
		String html = "<html>" +
				
				// HTML HEAD
				"<head>" +
				
				// Java Script
				"<script type=\"text/javascript\">" +
				"function loadPosts(taxonomy,name){Main.loadPosts(taxonomy,name);}" +
				"</script>" +
				
				// CSS
				"<style>" +
				"h2 {color:blue; font-family:arial,helvetica,sans-serif; text-align:center; font-size:30px;}" +
				"#content {color:black; font-family:arial,helvetica,sans-serif; font-size:26px;}" +
				"#footer {color:blue; font-family:verdana,geneva,sans-serif; font-size:12px;}" +
				"</style>" +
				
				"</head>" +
				
				// HTML Body
				"<body>" +
				
				// Heading
				"<h2>" + post.getTitle() + "</h2>" +
				
				// Content
				"<div id=\"content\">" +
				post.getContent(false) +
				"</div>" +
				
				// Footer
				"<br /><hr />" +
				"<div id=\"footer\">" +
				"Powered by <a href=\"http://hanu-droid.varunverma.org\">Hanu-Droid framework</a>" +
				"</div>" +
				
				"</body>" +
				"</html>";
		
		return html;
	}

	@Override
	public void swipeLeft() {
		// Show Next
		if (position == app.getPostList().size() - 1) {
			position = 0;
		} else {
			position++;
		}
		
		try{
			post = app.getPostList().get(position);
			showPost();
		}catch(Exception e){
			Log.e(Application.TAG, e.getMessage(), e);
		}
		
	}

	@Override
	public void swipeRight() {
		// Show Previous
		if(position == 0){
			position = app.getPostList().size() - 1;
		}
		else{
			position--;
		}
		
		try{
			post = app.getPostList().get(position);
			showPost();
		}catch(Exception e){
			Log.e(Application.TAG, e.getMessage(), e);
		}
	}

	@Override
	public void swipeUp() {
		//Nothing to do
	}
	
	@Override
	public void swipeDown() {
		//Nothing to do		
	}

	class PostJavaScriptInterface{
		@JavascriptInterface
		public void loadPosts(String t, String n){
			activity.loadPostsByCategory(t, n);
		}		
	}
}