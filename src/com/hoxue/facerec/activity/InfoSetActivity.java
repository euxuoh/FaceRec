package com.hoxue.facerec.activity;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @Title: InfoSetActivity.java
 * @Package: com.houxue.facerec.activity
 * @Description: ע����Ϣ���ý���
 * @author Hou Xue
 * @Date 2015.4.19
 * @version 1.0
 */
public class InfoSetActivity extends Activity {

	private static final String TAG = "InfoSetActivity.";

	//
	HttpRequests httpRequests = null;
	JSONObject result = null;

	// ����ͼƬ
	private Bitmap bitmap = null;

	// ���ֿؼ�
	private TextView info_tips = null;
	private ImageView info_img = null;
	private EditText et_name = null;
	private EditText et_psw = null;
	private EditText et_psw2 = null;
	private Button bt_enter = null;

	// Handler�Ŀ�����
	private final int DETECT_EXIST = 1;
	private final int DETECT_NOT_EXIST = 2;
	private final int TRAIN_OK = 3;
	private final int NET_ERROR = 4;
	
	// �û�����
	private String userName = null;
	private String psw = null;
	private String existedName = null;
	
	@SuppressLint("HandlerLeak")
	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch (msg.what) {
			case DETECT_NOT_EXIST:
				info_tips.setText("δ��⵽����������������...");
				// �ش�����
				Intent rtnIntent = new Intent();
				rtnIntent.putExtra("result", "FAILED");
				setResult(RESULT_OK, rtnIntent);
				break;
				
			case DETECT_EXIST:
				info_tips.setText("�����������Ϣ��");
				bt_enter.setEnabled(true);
				break;
				
			case TRAIN_OK:
				// �����û�����
				SharedPreferences sp = getSharedPreferences("userinfo",
						MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.putString("name", userName);
				editor.putString("password", psw);
				editor.commit();
				
				// �ش�����
				Intent rtnIntent2 = new Intent();
				rtnIntent2.putExtra("result", "OK");
				setResult(RESULT_OK, rtnIntent2);

				// ע��ɹ���Activity��ת
				Intent intent = new Intent(InfoSetActivity.this,
						IndexActivity.class);
				intent.putExtra("status", "ע��ɹ�^_^");
				startActivity(intent);
				finish();
				break;
				
			case NET_ERROR:
				Toast.makeText(InfoSetActivity.this, "�������", Toast.LENGTH_SHORT).show();
				break;
				
			default:
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infosetting);

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

		// ���ȷ����ע�������Ϣ
		bt_enter.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				
				SharedPreferences sp = getSharedPreferences("userinfo", MODE_PRIVATE);
				existedName = sp.getString("name", "");

				// �����û���Ϣ
				userName = et_name.getText().toString();
				psw = et_psw.getText().toString();
				String psw2 = et_psw2.getText().toString();

				if (existedName.equals(userName)) {
					Toast.makeText(InfoSetActivity.this, "�û��Ѵ��ڣ�", Toast.LENGTH_SHORT).show();
				} else {
					if (!psw.equals("") && psw.equals(psw2)) {
						// ����face++ API
						train(userName);
					} else {
						Toast.makeText(InfoSetActivity.this, "�������󣡣���",
								Toast.LENGTH_SHORT).show();
					}
				}
				
			}// onClick()
		});
	}

	public void onResume() {
		super.onResume();
		// �������
		localDetectFace(bitmap);
	}

	/**
	 * �ؼ���ʼ��
	 */
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
	 * �������
	 * 
	 * @param bm ����ͼ��
	 * 
	 * @return void
	 */
	public void localDetectFace(final Bitmap bm) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				// ���¹���BitMap
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				byte[] array = stream.toByteArray();

				try {
					httpRequests = new HttpRequests(
							"13307dabf988e178af502b1e3851af1d",
							"T-8pBfIQI8XJTMi3BHSruL7iIrO49Ket", true, false);
					// ���
					result = httpRequests.detectionDetect(new PostParameters()
							.setImg(array));

					// ���ݼ��������handler������Ϣ
					if (result.getJSONArray("face").length() > 0) {
						Message msg = new Message();
						msg.what = DETECT_EXIST;
						InfoSetActivity.this.myHandler.sendMessage(msg);
					} else {
						Message msg = new Message();
						msg.what = DETECT_NOT_EXIST;
						InfoSetActivity.this.myHandler.sendMessage(msg);
					}
				} catch (FaceppParseException e) {
					Log.e(TAG + "++localDetectFace().FaceppEx", e.getMessage());
					Message msg = new Message();
					msg.what = NET_ERROR;
					InfoSetActivity.this.myHandler.sendMessage(msg);
				} catch (JSONException e) {
					Log.e(TAG + "localDetectFace().JSONEx", e.getMessage());
				}
			}
		}).start();
	}
	
	/**
	 * ����ѵ��
	 * 
	 * @param name ����
	 * 
	 * @return void
	 */
	public void train(final String name) {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// ����person
					httpRequests.personCreate(new PostParameters()
							.setPersonName(name));

					// Ϊperson���face
					httpRequests.personAddFace(new PostParameters()
							.setPersonName(name).setFaceId(
									result.getJSONArray("face")
											.getJSONObject(0)
											.getString("face_id")));
					// ѵ��person
					JSONObject sync = httpRequests
							.trainVerify(new PostParameters()
									.setPersonName(name));
					if (sync.getString("session_id") != null) {
						Message msg = new Message();
						msg.what = TRAIN_OK;
						InfoSetActivity.this.myHandler.sendMessage(msg);
					}
					System.out.println(sync);
				} catch (FaceppParseException e) {
					Log.e(TAG + "train()", e.getMessage());
				} catch (JSONException e) {
					Log.e(TAG + "train()", e.getMessage());
				}
			}
		}).start();
	}

}
