/*
  MultiColorLamp - Example to use with Amarino
  Copyright (c) 2009 Bonifaz Kaufmann. 
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.grizcorp.sparklelamp;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

@SuppressWarnings("unused")
public class Sliders extends Activity implements OnSeekBarChangeListener {

	private static final String TAG = "SparkleLamp";

	/*
	 * TODO: change the address to the address of your Bluetooth module and
	 * ensure your device is added to Amarino
	 */
	private static final String DEVICE_ADDRESS = "00:06:66:04:B0:33";

	final int DELAY = 150;
	SeekBar blueSB;
	SeekBar redSB;
	SeekBar greenSB;
	View colorIndicator;

	int red, green, blue;
	long lastChange;

	// button attempting to connect bt device ___griz
	private Button buttonRed;
	// used to display proximity sensor reading from arduino
	private TextView mValueTV;
	//private GraphView mGraph;
	
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sliders);

		Amarino.connect(this, DEVICE_ADDRESS);
		
		//mGraph = (GraphView)findViewById(R.id.graph);
		//mGraph.setMaxValue(1024);
		mValueTV = (TextView) findViewById(R.id.value);
		// in order to receive broadcasted intents we need to register our receiver
		registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));

		// get references to views defined in our main.xml layout file
		blueSB = (SeekBar) findViewById(R.id.SeekBarBlue);
		redSB = (SeekBar) findViewById(R.id.SeekBarRed);
		greenSB = (SeekBar) findViewById(R.id.SeekBarGreen);

		colorIndicator = findViewById(R.id.ColorIndicator);

		// register listeners
		blueSB.setOnSeekBarChangeListener(this);
		redSB.setOnSeekBarChangeListener(this);
		greenSB.setOnSeekBarChangeListener(this);

		/*
		 * // RED BT 1/2 // Capture our button from layout Button button =
		 * (Button)findViewById(R.id.buttonRed); // Register the onClick
		 * listener with the implementation above
		 * button.setOnClickListener(buttonRedConnectBT);
		 */

	}

	/*
	 * cannot get this button to register disconnect address // RED BT 2/2
	 * 
	 * // Create an anonymous implementation of OnClickListener private
	 * OnClickListener buttonRedConnectBT = new OnClickListener() {
	 * 
	 * private View buttonRed;
	 * 
	 * public void onClick(View v) { // do something when the button is clicked
	 * Amarino.connect(this, DEVICE_ADDRESS); }
	 * 
	 * 
	 * 
	 * 
	 * 
	 * };
	 */

	@Override
	protected void onStart() {
		super.onStart();

		// load last state
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		blue = prefs.getInt("blue", 0);
		red = prefs.getInt("red", 0);
		green = prefs.getInt("green", 0);

		// set seekbars and feedback color according to last state
		blueSB.setProgress(blue);
		redSB.setProgress(red);
		greenSB.setProgress(green);
		colorIndicator.setBackgroundColor(Color.rgb(red, green, blue));
		new Thread() {
			public void run() {
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
				}
				Log.d(TAG, "update colors");
				updateAllColors();
			}
		}.start();

	}

	@Override
	protected void onStop() {
		super.onStop();
		// save state
		PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(
				"red", red).putInt("blue", blue).putInt("green", green)
				.commit();

		// stop Amarino's background service, we don't need it any more
		Amarino.disconnect(this, DEVICE_ADDRESS);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// do not send to many updates, Arduino can't handle so much
		if (System.currentTimeMillis() - lastChange > DELAY) {
			updateState(seekBar);
			lastChange = System.currentTimeMillis();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		lastChange = System.currentTimeMillis();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		updateState(seekBar);
	}

	private void updateState(final SeekBar seekBar) {

		switch (seekBar.getId()) {
		case R.id.SeekBarBlue:
			blue = seekBar.getProgress();
			updateBlue();
			break;
		case R.id.SeekBarRed:
			red = seekBar.getProgress();
			updateRed();
			break;
		case R.id.SeekBarGreen:
			green = seekBar.getProgress();
			updateGreen();
			break;
		}

		// provide user feedback
		colorIndicator.setBackgroundColor(Color.rgb(red, green, blue));
	}

	private void updateAllColors() {
		// send state to Arduino

		updateBlue();
		updateRed();
		updateGreen();
	}

	// I have chosen random small letters for the flag 'o' for red, 'p' for
	// green and 'q' for blue
	// you could select any small letter you want
	// however be sure to match the character you register a function for your
	// in Arduino sketch

	private void updateBlue() {
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'b', blue);
	}

	private void updateRed() {
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'r', red);
	}

	private void updateGreen() {
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'g', green);
	}
	
	public class ArduinoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String data = null;
			
			// the device address from which the data was sent, we don't need it here but to demonstrate how you retrieve it
			
			final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
			
			// the type of data which is added to the intent
			final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
			
			// we only expect String data though, but it is better to check if really string was sent
			// later Amarino will support different data types, so far data comes always as string and
			// you have to parse the data to the type you have sent from Arduino, like it is shown below
			if (dataType == AmarinoIntent.STRING_EXTRA){
				data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
				
				if (data != null){
					mValueTV.setText(data);
					try {
						// since we know that our string value is an int number we can parse it to an integer
						final int sensorReading = Integer.parseInt(data);
						//mGraph.addDataPoint(sensorReading);
					} 
					catch (NumberFormatException e) { /* oh data was not an integer */ }
				}
			}
		}
	}

}
