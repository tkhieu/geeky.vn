package tgm.geeky.vn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import tgm.geeky.vn.util.Crypto;
import tgm.geeky.vn.util.MyBase64;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

public class MainActivity extends Activity {

	String uid;
	String pwd;
	String fbid;
	String secret;
	String udid;
	long epoch;
	long minuteEpoch;
	String resultFrom;
	int clickCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		// scaleImage();

		ImageButton imageButtonWolf = (ImageButton) findViewById(R.id.imageButtonWolf);
		imageButtonWolf.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clickCount++;
				if (clickCount % 4 == 0) {
					Toast.makeText(getApplicationContext(),
							"Yay! Nothing Happens!", Toast.LENGTH_SHORT).show();
				}

				callPostRequest();
			}
		});

		Session.openActiveSession(this, true, new Session.StatusCallback() {

			@Override
			public void call(Session session, SessionState state,
					Exception exception) {
				// TODO Auto-generated method stub
				// make request to the /me API
				Request.executeMeRequestAsync(session,
						new Request.GraphUserCallback() {

							// callback after Graph API response with user
							// object

							@Override
							public void onCompleted(GraphUser user,
									Response response) {
								// TODO Auto-generated method stub
								// Log.d("Geeky", user.getName());
								if (user != null) {
									TextView welcome = (TextView) findViewById(R.id.textViewFbName);
									welcome.setText("Hello " + user.getName()
											+ "!");

									Button button = (Button) findViewById(R.id.buttonSendRequest);
									button.setEnabled(true);
									fbid = user.getId();
									button.setOnClickListener(clickListener_SendRequest);
								} else {
									Toast.makeText(getApplicationContext(),
											"Have an authentication error !!!",
											Toast.LENGTH_LONG).show();
								}
							}
						});
			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	// Set hàm call back khi nhấn Send Request
	OnClickListener clickListener_SendRequest = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			TextView textViewTest = (TextView) findViewById(R.id.textViewTest);
			
			callPostRequest();

		}
	};;

	class RequestTask extends AsyncTask<Map<String, String>, String, String> {

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			resultFrom = result;
		}

		@Override
		protected String doInBackground(Map<String, String>... params) {
			// TODO Auto-generated method stub
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			String responseString = null;
			String url = params[0].get(Config.DICT_KEY_URL);

			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Authorization", params[0].get("Authorization"));
			try {
				response = httpclient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();

				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				// TODO Handle problems..
			} catch (IOException e) {
				// TODO Handle problems..
			}
			return responseString;
		}

	}

	public String getId() {
		String id = android.provider.Settings.System.getString(
				this.getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);
		return id;
	}

	public void callPostRequest() {
		Map<String, String> params = new HashMap<String, String>();

		// Hash uid from fbid

		Long long_fbid = Long.parseLong(fbid);
		Long uid_temp = ((((long_fbid + Config.KEY_X) * 3) + Config.KEY_Y) * 7 + Config.KEY_Z);
		uid = MyBase64.encodeToString(uid_temp.toString().getBytes(), false);

		epoch = System.currentTimeMillis() / 1000;
		long pwd_temp = Long.parseLong(fbid) + epoch/60;
		try {

			pwd = Crypto.getMd5(String.valueOf(pwd_temp));

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		params.put(Config.DICT_KEY_URL, Config.SERVER);
		String source = uid + "id:" + pwd;
		String ret = "Basic "
				+ MyBase64.encodeToString(source.getBytes(), false);
		params.put("Authorization", ret);
		// Put sceret and udid into POST

		String md5_salt1 = null;
		try {
			md5_salt1 = Crypto.getMd5(Config.salt1);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String md5_salt2 = null;
		try {
			md5_salt2 = Crypto.getMd5(Config.salt2);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			secret = md5_salt1 + Crypto.getMd5(String.valueOf(epoch))
					+ md5_salt2;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		params.put("secret", secret);
		udid = getId();
		params.put("udid", udid);

		try {

			AsyncTask<Map<String, String>, String, String> a = new RequestTask()
					.execute(params);
			String result = a.get();
			System.out.print(result);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}