package com.hoxue.facerec.activity;

import java.io.IOException;

import org.json.JSONObject;

import com.houxue.facerec.utils.DetectCallback;
import com.houxue.facerec.utils.FaceppRec;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @Title: CheckActivity.java
 * @Package: com.houxue.facerec.activity
 * @Description: ������¼����
 * @author Hou Xue
 * @Date 2015.4.19
 * @version 1.0
 */
public class CheckActivity extends Activity {

	private final String TAG = "CheckActivity.";
	
	// ����ͷ���
	private final int FRONT_CAMERA = 1;
	// private final int REAR_CAMERA = 0;

	// ����ͷʵ��
	private Camera mCamera = null;
	// ����ͷԤ��ʵ��
	private CameraPreview mPreview = null;
	// ǰ������ͷlayout�Ƕ�
	private int orientionOfCamera;
	// ����ͼ��
	private Bitmap newBitmap = null;

	// ���ֿؼ�
	private TextView tips_edtx = null;
	private Button enter_btn = null;

	// �û�����
	private SharedPreferences sp = null;
	private String name = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check);

		sp = getSharedPreferences("userinfo", MODE_PRIVATE);
		name = sp.getString("name", "default");

		tips_edtx = (TextView) findViewById(R.id.check_tips);
		tips_edtx.setText("����������ͷ...");

		enter_btn = (Button) findViewById(R.id.check_ent);
		enter_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mCamera.takePicture(null, null, takePictureCallback);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// �������ͷ�Ƿ�ռ��
		if (checkCamera(this)) {
			// ���һ������ͷʵ��
			mCamera = getCameraInstance(FRONT_CAMERA);
		}

		// ��������ͷ�ĽǶ�
		setCameraDisplayOrientation(FRONT_CAMERA, mCamera);

		// �������ͷԤ��ʵ��
		mPreview = new CameraPreview(this, mCamera);

		// ��Ԥ������layout��
		FrameLayout previewFrameLayout = (FrameLayout) findViewById(R.id.camera_preview_check);
		previewFrameLayout.addView(mPreview);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mCamera != null) {
			mCamera.release();
			finish();
		}
	}

	/**
	 * �������ͷ�Ƿ����
	 * 
	 * @param context
	 *            ��Activity������
	 * 
	 * @return true ����ͷ���� false ����ͷ������
	 */
	private boolean checkCamera(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			Log.i(TAG + "checkCamera()", "����ͷ����");
			return true;
		} else {
			Log.w(TAG + "checkCamera()", "����ͷ������");
			return false;
		}
	}

	/**
	 * ��ȡһ������ͷʵ��
	 * 
	 * @param int ����ͷ���
	 * 
	 * @return Camera һ������ͷʵ��
	 */
	@SuppressLint("NewApi")
	public Camera getCameraInstance(int parmInt) {
		Camera camera = null;

		try {
			// 0���򿪺�������ͷ��1����ǰ������ͷ
			camera = Camera.open(parmInt);
		} catch (Exception e) {
			Log.w(TAG + "getCameraInstance()", "����ͷ��ռ��");
		}

		return camera;
	}

	/**
	 * ��������ͷ�ķ���һ��Ĭ���Ǻ����
	 * 
	 * @param int ����ͷ��� Camera ǰ�Ļ�ȡ������ͷʵ��
	 * 
	 * @return void
	 */
	@SuppressLint("NewApi")
	public void setCameraDisplayOrientation(int paramInt, Camera paramCamera) {
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(paramInt, info);
		// �����ʾ�����Ƕ�
		int rotation = ((WindowManager) getSystemService("window"))
				.getDefaultDisplay().getRotation();
		int degrees = 0;

		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		// �������ͷ�İ�װ��ת�Ƕ�
		orientionOfCamera = info.orientation;
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			// ������
			result = (360 - result) % 360;
		} else {
			// ��������ͷ
			result = (info.orientation - degrees + 360) % 360;
		}
		// ע��ǰ���õĴ���ǰ����ӳ���棬�ö���SDK�ĵ��ı�׼DEMO
		paramCamera.setDisplayOrientation(result);
	}

	// ��takepicture�е��õĻص�����֮һ������jpeg��ʽ��ͼ��
	private PictureCallback takePictureCallback = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {
			try {
				// ȡ����Ƭ
				Bitmap bm = BitmapFactory.decodeByteArray(_data, 0,
						_data.length);

				// ��������ͷ����ת�Ƕ�����BitMap
				float scale = Math.min(1,
						Math.min(600f / bm.getWidth(), 600f / bm.getHeight()));
				Matrix matrix = new Matrix();
				matrix.setRotate(orientionOfCamera - 360);
				matrix.postScale(scale, scale);
				newBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
						bm.getHeight(), matrix, true);

				// ����ʶ��
				FaceppRec faceppRec = new FaceppRec();
				faceppRec.setDetectCallback(new DetectCallback() {
					@Override
					public void detectResult(JSONObject rst) {
						try {
							// �ӷ��ص�JSON����У�ȡ��result�����Ŷ�
							int confidence = rst.getInt("confidence");
							boolean result = rst.getBoolean("is_same_person");

							if (result && confidence > 50) {
								// ��֤ͨ��
								Intent intent = new Intent(CheckActivity.this,
										IndexActivity.class);
								intent.putExtra("status", "��֤ͨ��^_^");
								startActivity(intent);
							} else {
								// ��֤δͨ��
								CheckActivity.this
										.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												Toast.makeText(
														CheckActivity.this,
														"��֤δͨ��������",
														Toast.LENGTH_SHORT)
														.show();
												Intent intent = new Intent(
														CheckActivity.this,
														MainActivity.class);
												startActivity(intent);
											}
										});
							}
						} catch (Exception e) {
							Log.e(TAG + "takePicture()", e.getMessage());
						}
					}
				});
				faceppRec.recognize(newBitmap, name, CheckActivity.this);

			} catch (Exception e) {
				Log.e(TAG + "takePicture()", e.getMessage());
			} finally {
				releaseCamera();
			}
		}
	};

	/**
	 * �ͷ�����ͷ
	 * 
	 * @param void
	 * 
	 * @return void
	 */
	public void releaseCamera() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}
}

/**
 * @Description������ͷԤ����
 * @author ũ�񲮲�
 * @Date 2015.4.18
 */
class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	
	private final String TAG = "CheckActivity.CameraPreview.";

	private SurfaceHolder mHolder;
	private Camera mCamera;

	@SuppressWarnings("deprecation")
	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;

		// ��װһ��SurfaceHolder.Callback���������������ٵײ�surfaceʱ�ܹ����֪ͨ��
		mHolder = getHolder();
		mHolder.addCallback(this);

		// �ѹ��ڵ����ã����汾����3.0��Android����Ҫ
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// surface�ѱ����������ڰ�Ԥ�������λ��֪ͨ����ͷ
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(TAG + "surfaceCreated()",
					"Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO::ע����activity���ͷ�����ͷԤ������
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

		// ���Ԥ���޷����Ļ���ת��ע��˴����¼�,ȷ�������Ż�����ʱֹͣԤ��
		if (mHolder.getSurface() == null) {
			// Ԥ��surface������
			Log.d(TAG + "surfaceChanged()", "surface������");
			return;
		}

		// ����ʱֹͣԤ��
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ���ԣ���ͼֹͣ�����ڵ�Ԥ��
		}

		// �ڴ˽������š���ת��������֯��ʽ,���µ���������Ԥ��
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch (Exception e) {
			Log.d(TAG + "surfaceChanged()",
					"Error starting camera preview: " + e.getMessage());
		}
	}
}
