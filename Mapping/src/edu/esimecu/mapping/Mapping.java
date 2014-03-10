package edu.esimecu.mapping;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

public class Mapping extends Activity implements CvCameraViewListener2,
		OnTouchListener {
	private static final String TAG = "Captura::Activity";
	private int counter;
	private Mat mGray;
	private Mat mView;
	private Mat mObject;
	String fileName = Environment.getExternalStorageDirectory().getPath()
			+ "/Pictures/Frame" + counter + ".jpg";
	private Tracer mTracer;

	private MappingView mOpenCvCameraView;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				mOpenCvCameraView.setOnTouchListener(Mapping.this);
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public Mapping() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_mapping);
		mOpenCvCameraView = (MappingView) findViewById(R.id.activity_surface_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.enableFpsMeter();

		counter = 0;

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {

		mGray = new Mat();
		mView = new Mat();
		mObject = new Mat();
		mTracer = new Tracer(fileName);

	}

	public void onCameraViewStopped() {
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		final Mat mRgba = inputFrame.rgba();
		if (counter > 0) {
			mTracer.apply(mRgba, mRgba);
			return mRgba;
		}
		return inputFrame.rgba();
	}

	public Mat detecta(Mat inputFrame) {
		mGray = inputFrame;
		mObject = new Mat();
		mObject = Highgui.imread(fileName, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		mView = mGray.clone();

		FeatureDetector myFeatureDetector = FeatureDetector
				.create(FeatureDetector.FAST);

		MatOfKeyPoint keypoints = new MatOfKeyPoint();
		myFeatureDetector.detect(mGray, keypoints);

		MatOfKeyPoint objectkeypoints = new MatOfKeyPoint();
		myFeatureDetector.detect(mObject, objectkeypoints);

		DescriptorExtractor Extractor = DescriptorExtractor
				.create(DescriptorExtractor.ORB);
		Mat sourceDescriptors = new Mat();
		Mat objectDescriptors = new Mat();
		Extractor.compute(mGray, keypoints, sourceDescriptors);
		Extractor.compute(mGray, objectkeypoints, objectDescriptors);
		DescriptorMatcher matcher = DescriptorMatcher
				.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

		MatOfDMatch matches = new MatOfDMatch();
		matcher.match(sourceDescriptors, objectDescriptors, matches);

		Features2d.drawMatches(mGray, keypoints, mObject, objectkeypoints,
				matches, mView);
		Imgproc.resize(mView, mView, mGray.size());

		return mView;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.i(TAG, "onTouch event");
		if (counter == 0) {
			mOpenCvCameraView.takePicture(fileName);

			counter++;
		}
		return false;
	}
}