/*
 * Copyright (C) 2014 Spherical Elephant GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.diamonddogs.example.http.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.RecoverySystem.ProgressListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;
import at.diamonddogs.example.http.R;
import at.diamonddogs.service.net.HttpServiceAssister;
import at.diamonddogs.service.net.WebRequestMap;
import at.diamonddogs.service.processor.ImageLoader;
import at.diamonddogs.ui.annotation.UiAnnotationProcessor;
import at.diamonddogs.ui.receiver.IndeterminateProgressControl;
import at.diamonddogs.ui.receiver.ProgressReceiver;

/**
 * Demonstrates how to use the {@link ImageLoader}. The {@link ImageLoader}
 * automates loading images onto {@link ImageView}s and allows retrying image
 * downloads if something goes wrong. Make sure to disable networking to try the
 * retry feature!
 */
public class ImageLoaderExampleActivity extends Activity implements IndeterminateProgressControl {
	private static final String URL = "http://imgs.xkcd.com/comics/ballmer_peak.png";

	/** our good buddy the {@link HttpServiceAssister} */
	private HttpServiceAssister assister;
	/** the actual {@link ImageLoader} used in this example */
	private ImageLoader imageLoader;
	/** The {@link ProgressListener} */
	private ProgressReceiver progressReceiver;

	private boolean progressShowing = false;

	/**
	 * The {@link ImageView} that will be used to load the image on
	 */
	private ImageView image;

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Toast.makeText(ImageLoaderExampleActivity.this, "Normal Click Working Again", Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// allow showing of indeterminate progress
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.imageloaderexampleactivity);
		assister = new HttpServiceAssister(this);
		progressReceiver = new ProgressReceiver(this);
		image = (ImageView) findViewById(R.id.imageloaderexampleactivity_image);

		imageLoader = new ImageLoader(assister, new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				Toast.makeText(ImageLoaderExampleActivity.this, "Image has been loaded!", Toast.LENGTH_SHORT).show();
				return true; // return true if you want to mark the message as
								// handled!
			}
		});

		loadImage();
	}

	private void loadImage() {
		// This call might look scary, a quick explanation on why we need all
		// those parameters: the first parameter is the imageview, the second
		// parameter is the scaletype of this imageview, the third parameter is
		// the original onClickListener of the image View, the forth parameter
		// is the image to load and the last parameter is the default image that
		// will also be shown if the image download was not successful. We need
		// to store the initial scaletype and clicklistener so that we can
		// restore it (listview / retry stuff)
		imageLoader.loadImage(image, ScaleType.CENTER_INSIDE, onClickListener, URL,
			R.drawable.ic_menu_refresh);
	}

	@Override
	protected void onResume() {
		super.onResume();
		WebRequestMap.registerBroadcastReceiver(this, progressReceiver);
		assister.bindService();
	}

	@Override
	protected void onPause() {
		super.onPause();
		WebRequestMap.unregisterBroadcastReceiver(this, progressReceiver);
		assister.unbindService();
	}

	@Override
	public void processUiAnnotations(UiAnnotationProcessor processor) throws IllegalAccessException, IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	/**
	 * A simple callback that will be called whenever the indeterminate progress
	 * should be shown
	 */
	@Override
	public void showIndeterminateProgress() {
		setProgressBarIndeterminate(true);
		setProgressBarIndeterminateVisibility(true);
		progressShowing = true;
	}

	/**
	 * A simple callback that will be called whenever the indeterminate progress
	 * should be hidden
	 */
	@Override
	public void hideIndeterminateProgress() {
		setProgressBarIndeterminate(false);
		setProgressBarIndeterminateVisibility(false);
		progressShowing = false;
	}

	/**
	 * returns the flag which indicates if the indeterminate progress is shown
	 */
	@Override
	public boolean isIndeterminateProgressShowing() {
		return progressShowing;
	}
}
