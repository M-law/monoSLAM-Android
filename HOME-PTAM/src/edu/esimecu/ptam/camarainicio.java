package edu.esimecu.ptam;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import edu.esimecu.calibra.CalibrationResult;
import edu.esimecu.calibra.CameraCalibrator;
import edu.esimecu.home.R;

public class camarainicio extends Activity implements CvCameraViewListener2, SensorEventListener {
    private static final String TAG = "PTAM::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private CameraCalibrator mCalibrator;
    private OnCameraFrameRender mOnCameraFrameRender;
    private SensorManager mSensorManager;
    private Mat	   				   mat;
    private MatOfKeyPoint 	       points;
    private Scalar                 redcolor;
    private FeatureDetector        fast;
    private Mat                    mRgba;
    private int mWidth;
    private int mHeight;
    

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
            case LoaderCallbackInterface.SUCCESS:
            {
                Log.i(TAG, "OpenCV loaded successfully");
                mOpenCvCameraView.enableView();
            } break;
            default:
            {
                super.onManagerConnected(status);
            } break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.calibrado);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mLoaderCallback);
        super.onResume();

		Ini_Sensores();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
		Parar_Sensores();

		super.onDestroy();
    }
    
    public void onCameraViewStarted(int width, int height) {
 
    	if (mWidth != width || mHeight != height) {
          mWidth = width;
          mHeight = height;
            mCalibrator = new CameraCalibrator(mWidth, mHeight);
            if (CalibrationResult.tryLoad(this, mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients())) {
                mCalibrator.setCalibrated();
            }
            mOnCameraFrameRender = new OnCameraFrameRender(new UndistortionFrameRender(mCalibrator));

    	}
    	
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        return faster(mOnCameraFrameRender.render(inputFrame));
    }
    public Mat faster(Mat inputFrame) {
		points = new MatOfKeyPoint();


      mat =  inputFrame;
      fast = FeatureDetector.create(FeatureDetector.FAST);
      fast.detect(mat, points);


  redcolor = new Scalar(0,255,0);
  mRgba= mat.clone();
  Imgproc.cvtColor(mat, mRgba, Imgproc.COLOR_RGBA2RGB,4);

  Features2d.drawKeypoints(mRgba, points, mRgba, redcolor, 3);


  return mRgba;
}
    protected void Ini_Sensores() {

		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);


		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

		private void Parar_Sensores() {

			mSensorManager.unregisterListener(this,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
			
			mSensorManager.unregisterListener(this,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		@Override
		public void onSensorChanged(SensorEvent event) {

			synchronized (this) {
				switch (event.sensor.getType()) {
				case Sensor.TYPE_ACCELEROMETER:
				    Log.d("Coordenada X: ",event.values[0]+"");
				    Log.d("Coordenada Y: ",event.values[1]+"");
				    Log.d("Coordenada Z: ",event.values[2]+"");
				    Log.d("############# ","######################");
					break;

			case Sensor.TYPE_ROTATION_VECTOR:
			    Log.d("Angulo X: ",event.values[0]+"");
			    Log.d("Angulo Y: ",event.values[1]+"");
			    Log.d("Angulo Z: ",event.values[2]+"");
			    Log.d("############# ","######################");

				break;
				}
				}
		}
		protected void onStop() {

			Parar_Sensores();

			super.onStop();
		}
		
		@Override
		protected void onRestart() {
			// TODO Auto-generated method stub

			Ini_Sensores();

			super.onRestart();
		}
}