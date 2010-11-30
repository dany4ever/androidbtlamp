package com.grizcorp.sparklelamp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SparkleLamp extends Activity implements OnClickListener{
    private Button sendToSliders;


	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sendToSliders=(Button)findViewById(R.id.buttonToSlider);
        sendToSliders.setOnClickListener(this);

    }

	public void onClick(View v) {
		if (v == this.sendToSliders){
			Intent sliderbuttons = new Intent(SparkleLamp.this,Sliders.class);
			SparkleLamp.this.startActivity(sliderbuttons);
		}
		
		
		// TODO Auto-generated method stub
		
	}
}