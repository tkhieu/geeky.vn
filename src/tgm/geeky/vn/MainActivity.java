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
import android.widget.Button;
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
	String resultFrom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
									Toast.makeText(getApplicationContext(), "Have an authentication error !!!", Toast.LENGTH_LONG).show();
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
			textViewTest.setText("Đã nhấn rồi ");

			Map<String, String> params = new HashMap<String, String>();

			// Hash uid from fbid
			
			Long long_fbid = Long.parseLong(fbid);
			Long uid_temp = ((((long_fbid + Config.KEY_X) * 3) + Config.KEY_Y) * 7 + Config.KEY_Z);
			uid = MyBase64.encodeToString(uid_temp.toString().getBytes(), false);

			long epoch = System.currentTimeMillis() / 1000;
			long pwd_temp = Long.parseLong(fbid) + epoch;
			try {
				byte[] bytesOfMessage = String.valueOf(pwd_temp).getBytes(
						"UTF-8");
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] thedigest = md.digest(bytesOfMessage);

				pwd = thedigest.toString();

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			params.put(Config.DICT_KEY_URL, Config.SERVER);
			String source=uid+"id:"+pwd;
			 String ret="Basic "+MyBase64.encodeToString(source.getBytes(),false);
			 
			
			 
			 params.put("Authorization", ret);


			
			try {
				
				 
				
				 
				AsyncTask<Map<String, String>, String, String> a = new RequestTask().execute(params);
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
	};

	class RequestTask extends AsyncTask<Map<String,String>, String, String>{

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
			        httpPost.setHeader("Authorization",params[0].get("Authorization"));
			        try {
			            response = httpclient.execute(httpPost);
			            StatusLine statusLine = response.getStatusLine();
			            
			            ByteArrayOutputStream out = new ByteArrayOutputStream();
		                response.getEntity().writeTo(out);
		                out.close();
		                responseString = out.toString();
		                
			            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
			                
			            } else{
			                //Closes the connection.
			                response.getEntity().getContent().close();
			                throw new IOException(statusLine.getReasonPhrase());
			            }
			        } catch (ClientProtocolException e) {
			            //TODO Handle problems..
			        } catch (IOException e) {
			            //TODO Handle problems..
			        }
			        return responseString;
		}
		
		
	


	}

	
	private void scaleImage()
	{
	    // Get the ImageView and its bitmap
	    ImageView view = (ImageView) findViewById(R.id.imageButton1);
	    Drawable drawing = view.getDrawable();
	    if (drawing == null) {
	        return; // Checking for null & return, as suggested in comments
	    }
	    Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

	    // Get current dimensions AND the desired bounding box
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	    int bounding = dpToPx(250);
	    Log.i("Test", "original width = " + Integer.toString(width));
	    Log.i("Test", "original height = " + Integer.toString(height));
	    Log.i("Test", "bounding = " + Integer.toString(bounding));

	    // Determine how much to scale: the dimension requiring less scaling is
	    // closer to the its side. This way the image always stays inside your
	    // bounding box AND either x/y axis touches it.  
	    float xScale = ((float) bounding) / width;
	    float yScale = ((float) bounding) / height;
	    float scale = (xScale <= yScale) ? xScale : yScale;
	    Log.i("Test", "xScale = " + Float.toString(xScale));
	    Log.i("Test", "yScale = " + Float.toString(yScale));
	    Log.i("Test", "scale = " + Float.toString(scale));

	    // Create a matrix for the scaling and add the scaling data
	    Matrix matrix = new Matrix();
	    matrix.postScale(scale, scale);

	    // Create a new bitmap and convert it to a format understood by the ImageView 
	    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	    width = scaledBitmap.getWidth(); // re-use
	    height = scaledBitmap.getHeight(); // re-use
	    BitmapDrawable result = new BitmapDrawable(scaledBitmap);
	    Log.i("Test", "scaled width = " + Integer.toString(width));
	    Log.i("Test", "scaled height = " + Integer.toString(height));

	    // Apply the scaled bitmap
	    view.setImageDrawable(result);

	    // Now change ImageView's dimensions to match the scaled image
	    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams(); 
	    params.width = width;
	    params.height = height;
	    view.setLayoutParams(params);

	    Log.i("Test", "done");
	}

	private int dpToPx(int dp)
	{
	    float density = getApplicationContext().getResources().getDisplayMetrics().density;
	    return Math.round((float)dp * density);
	}
	
}