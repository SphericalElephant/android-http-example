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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.example.http.R;
import at.diamonddogs.example.http.fragment.TestFragment;
import at.diamonddogs.service.net.HttpServiceAssister;
import at.diamonddogs.service.net.WebRequestMap;
import at.diamonddogs.service.processor.DummyProcessor;
import at.diamonddogs.ui.annotation.ClearUiElementOnWebRequest;
import at.diamonddogs.ui.annotation.DisableUiElementOnWebRequest;
import at.diamonddogs.ui.annotation.HideUiElementOnWebRequest;
import at.diamonddogs.ui.annotation.SearchForUiElements;
import at.diamonddogs.ui.annotation.UiAnnotationProcessable;
import at.diamonddogs.ui.annotation.UiAnnotationProcessor;
import at.diamonddogs.ui.receiver.IndeterminateProgressControl;
import at.diamonddogs.ui.receiver.ProgressReceiver;

/**
 * This example illustrates two concepts: showing the indeterminate progress in
 * the {@link ActionBar} whenever {@link WebRequest}s are running and modifying
 * the ui according to {@link WebRequest} execution state
 */
public class UiAnnotationExampleActivity extends FragmentActivity implements UiAnnotationProcessable, IndeterminateProgressControl {
	private static final Logger LOGGER = LoggerFactory.getLogger(UiAnnotationExampleActivity.class.getSimpleName());

	/**
	 * We are using the {@link HttpServiceAssister} to run the
	 * {@link WebRequest}
	 */
	private HttpServiceAssister assister;

	/**
	 * This receiver is used for two things: 1) show the indeterminate progress
	 * bar when a {@link WebRequest} is running 2) take care of manipulating the
	 * UI when {@link WebRequest}s are running or have completed
	 */
	private ProgressReceiver progressReceiver;
	/**
	 * This flag stores information on weather the indeterminate progress is
	 * show, since android does not feature a getter for that yet!
	 */
	private boolean progressShowing = false;

	/**
	 * This button will be disabled while the {@link WebRequest} is running
	 */
	@DisableUiElementOnWebRequest
	private Button okButton;

	/**
	 * This {@link TextView} will be hidden while the {@link WebRequest} is
	 * running
	 */
	@HideUiElementOnWebRequest
	private TextView testText;

	/**
	 * This {@link EditText} will be cleared once the {@link WebRequest} starts
	 * running and it will not be refilled when the {@link WebRequest} has
	 * completed.
	 */
	@ClearUiElementOnWebRequest
	private EditText testEdit;

	/**
	 * This {@link EditText} will be disabled and cleared once the
	 * {@link WebRequest} starts running
	 */
	@DisableUiElementOnWebRequest
	@ClearUiElementOnWebRequest
	private EditText testEdit1;

	/**
	 * This {@link Fragment} will be searched for Ui annotations
	 */
	@SearchForUiElements
	private Fragment testFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// allow showing of indeterminate progress
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		// initializing the assister and progress receiver
		assister = new HttpServiceAssister(this);
		progressReceiver = new ProgressReceiver(this);

		setContentView(R.layout.uiannotationexampleactivity);

		testFragment = new TestFragment();
		getSupportFragmentManager().beginTransaction().add(R.id.uiannotationexampleactivity_container, testFragment).commit();

		testText = (TextView) findViewById(R.id.uiannotationexampleactivity_testText);
		testEdit = (EditText) findViewById(R.id.uiannotationexampleactivity_testEdit);
		testEdit1 = (EditText) findViewById(R.id.uiannotationexampleactivity_testEdit1);
		okButton = (Button) findViewById(R.id.uiannotationexampleactivity_okbutton);

		// clicking the button will fire a webrequest
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WebRequest wr = new WebRequest();
				wr.setUrl("http://google.com/");
				wr.setProcessorId(DummyProcessor.ID);
				assister.runWebRequest(new Callback() {

					@Override
					public boolean handleMessage(Message msg) {
						return true;
					}
				}, wr, new DummyProcessor());
			}
		});
	}

	/**
	 * This callback will be called every time a {@link WebRequest}t is running
	 * or has been completed, it's enough to call processor.process(this)
	 */
	@Override
	public void processUiAnnotations(UiAnnotationProcessor processor) throws IllegalAccessException, IllegalArgumentException {
		LOGGER.info("Processing UI information!");
		processor.process(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// registering the ProgressReceiver to start / stop webrequest events
		WebRequestMap.registerBroadcastReceiver(this, progressReceiver);
		assister.bindService();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// removing listener in onPause
		WebRequestMap.unregisterBroadcastReceiver(this, progressReceiver);
		assister.unbindService();
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
