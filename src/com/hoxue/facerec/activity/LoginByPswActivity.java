package com.hoxue.facerec.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @Title: LoginByPswActivity.java
 * @Package: com.houxue.facerec.activity
 * @Description: �����¼����
 * @author Hou Xue
 * @Date 2015.4.17
 * @version 1.0
 */
public class LoginByPswActivity extends Activity {
	
	// private static final String TAG = "LoginByPswActivity.";

	// ����
	private String password = null;
	// ��¼��ť
	private Button login = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginbypsw);
		
		SharedPreferences sp = getSharedPreferences("userinfo", MODE_PRIVATE);
		final String name = sp.getString("name", "default");
		final String password2 = sp.getString("password", "default");
		
		if (name.equals("default")) {
			Toast.makeText(LoginByPswActivity.this, "�״�ʹ�ã�����ע�ᣡ", Toast.LENGTH_SHORT).show();
		}
		
		// ��¼�¼�����
		login = (Button)this.findViewById(R.id.loginbypsw);
		login.setOnClickListener(new OnClickListener() {
			
			// �������֤�����Ƿ���ȷ
			public void onClick(View arg) {
				EditText psw = (EditText)findViewById(R.id.input_psw);
				password = psw.getText().toString();
				
				// ������ȷ��ת��Ӧ����ҳ
				if (password.equals(password2)) {
					// ����ǰ���ش�����
					setResult(RESULT_OK);
					
					Intent indexIntent = new Intent(LoginByPswActivity.this, IndexActivity.class);
					indexIntent.putExtra("status", "��¼�ɹ�^_^");
					startActivity(indexIntent);
					
					finish();
				} else {
					Toast.makeText(LoginByPswActivity.this, "������󣡣���", Toast.LENGTH_SHORT).show();
				}
			}
		});		
	}
}
