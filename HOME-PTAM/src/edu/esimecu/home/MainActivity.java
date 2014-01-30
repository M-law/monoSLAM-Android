package edu.esimecu.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.esimecu.calibra.CameraCalibrationActivity;
import edu.esimecu.ptam.camarainicio;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button sensores = (Button) findViewById(R.id.sensores);
		Button calibra = (Button) findViewById(R.id.calibra);	

	sensores.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Intent i = new Intent();
			i.setClass(MainActivity.this, camarainicio.class);
			startActivity(i);
			
		}

	});
	calibra.setOnClickListener(new OnClickListener (){
		
		public void onClick(View v) {
			Intent o = new Intent();
			o.setClass(MainActivity.this, CameraCalibrationActivity.class);
			
			startActivity(o);
		}
	});
	}
	}

