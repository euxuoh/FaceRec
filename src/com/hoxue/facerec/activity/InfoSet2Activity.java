package com.hoxue.facerec.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import com.houxue.facerec.utils.PreProc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @Title: InfoSet2Activity.java
 * @Package: com.houxue.facerec.activity
 * @Description: ע����Ϣ���ý���
 * @author Hou Xue
 * @Date 2015.4.19
 * @version 1.0
 */
public class InfoSet2Activity extends Activity {

	private final String TAG = "InfoSet2Activity";
	private String classifier = null;

	// ����ͼƬ
	private Bitmap bitmap = null;
	private Bitmap normalBitmap = null;

	// ���ֿؼ�
	private TextView info_tips = null;
	private ImageView info_img = null;
	private EditText et_name = null;
	private EditText et_psw = null;
	private EditText et_psw2 = null;
	private Button bt_enter = null;

	private boolean isEsitFace = false;

	private String strCaptureFilePath = Environment
			.getExternalStorageDirectory() + "/frec/";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infosetting);

		getClassifier();

		initView();

		Intent intent = getIntent();
		if (intent != null) {
			// ��ȡ��Ƭ����ʾ
			String imgPath = intent.getStringExtra("imgPath");
			bitmap = BitmapFactory.decodeFile(imgPath);

			if (bitmap != null) {
				info_img.setImageBitmap(bitmap);
			} else {
				info_tips.setText("Picture Not Found!!!");
			}
		}

		// �������
		localDetectFace(bitmap);

		if (isEsitFace) {
			bt_enter.setEnabled(true);
		} else {
			// δ��⵽����
			info_tips.setText("δ��⵽���������������գ�����");
		}

		// ���ȷ����ע�������Ϣ
		bt_enter.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {

				// �����û���Ϣ
				String userName = et_name.getText().toString();
				String psw = et_psw.getText().toString();
				String psw2 = et_psw2.getText().toString();

				if (!psw.equals("") && psw.equals(psw2)) {
					// -----------------------����face++
					// API---------------------------------
					/*
					 * // ����������⣬��ȡ������Ϣ FaceppDetect faceppDetect = new
					 * FaceppDetect(); faceppDetect.setDetectCallback(new
					 * DetectCallback() { public void detectResult(JSONObject
					 * rst) { try { final int count = rst.getJSONArray("face")
					 * .length(); if (count != 0) { //
					 * info_tips.setText("ע��ɹ�^_^"); } } catch (JSONException e)
					 * { e.printStackTrace(); Toast toast = Toast.makeText(
					 * InfoSetActivity.this, "������󣡣���", Toast.LENGTH_SHORT);
					 * toast.show(); } } }); faceppDetect.detect(bitmap,
					 * userName, InfoSetActivity.this);
					 */
					// ---------------------------����face++
					// API-------------------------------------

					store(normalBitmap);

					// �����û�����
					SharedPreferences sp = getSharedPreferences("userinfo",
							MODE_PRIVATE);
					Editor editor = sp.edit();
					editor.putString("name", userName);
					editor.putString("password", psw);
					editor.commit();

					// ע��ɹ���Activity��ת
					Intent intent = new Intent(InfoSet2Activity.this,
							IndexActivity.class);
					intent.putExtra("status", "ע��ɹ�^_^");
					startActivity(intent);
					finish();
				} else {
					Toast toast = Toast.makeText(InfoSet2Activity.this,
							"�������󣡣���", Toast.LENGTH_SHORT);
					toast.show();
				}
			}// onClick()
		});
	}

	public void initView() {
		info_tips = (TextView) findViewById(R.id.info_tips);
		info_img = (ImageView) findViewById(R.id.info_img);
		et_name = (EditText) findViewById(R.id.info_name);
		et_psw = (EditText) findViewById(R.id.info_psw);
		et_psw2 = (EditText) findViewById(R.id.info_psw2);
		bt_enter = (Button) findViewById(R.id.info_enter);
		bt_enter.setEnabled(false);
	}

	/**
	 * ��ȡ�������ļ���·��
	 * 
	 *  @param void
	 *  
	 *  @return void
	 */
	public void getClassifier() {
		try {
			InputStream is = getResources().openRawResource(
					R.raw.haarcascade_frontalface_alt_tree);
			File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
			File mCascadeFile = new File(cascadeDir, "haar_face.xml");
			FileOutputStream fos = new FileOutputStream(mCascadeFile);

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
			is.close();
			fos.close();

			classifier = mCascadeFile.getAbsolutePath();
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * �������
	 * 
	 * @param bm ����ͼ��
	 * 
	 * @return void
	 */
	public void localDetectFace(Bitmap bm) {
		Mat srcMat = new Mat();
		Mat normalMat = new Mat();
		PreProc pp = new PreProc(classifier);

		Utils.bitmapToMat(bm, srcMat);
		normalMat = pp.NormalImg(srcMat);

		if (normalMat == null) {
			normalMat = srcMat;
			Log.i(TAG, "normalMat is empty");
		} else {
			isEsitFace = true;
		}
		normalBitmap = Bitmap.createBitmap(normalMat.cols(), normalMat.rows(),
				Config.RGB_565);
		try {
			Utils.matToBitmap(normalMat, normalBitmap);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * �洢���򻯺������ͼ��
	 * 
	 * @param bm ���򻯺������ͼ��
	 * 
	 * @return void
	 */
	public void store(Bitmap bm) {
		File myCaptureFile = new File(strCaptureFilePath);
		if (!myCaptureFile.exists()) {
			myCaptureFile.mkdirs();
		}
		// �����ļ�
		File imgPath = new File(strCaptureFilePath, "normal.jpg");

		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(imgPath));

			// ����ѹ��ת������
			bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);

			// ����flush()����������BufferStream
			bos.flush();

			// ����OutputStream
			bos.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File not found.");
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}

	}
}
