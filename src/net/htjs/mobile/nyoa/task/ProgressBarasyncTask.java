package net.htjs.mobile.nyoa.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import net.htjs.mobile.nyoa.util.L;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
//生成该类的对象，并调用其execute方法之后
//首先执行的是onPreExecute方法
//其次是执行doInBackground方法
public class ProgressBarasyncTask extends AsyncTask<Integer, Integer, String>{
    private Handler mHandler;
    public ProgressBarasyncTask(Handler mHandler) {
    	this.mHandler = mHandler;
    }
    
    //该方法并不运行在UI线程内，所以在方法内不能对UI当中的控件进行设置和修改
    //主要用于进行异步操作
    @Override
    protected String doInBackground(Integer... params) {
            
            int i=0;
            for ( i=10; i <=100; i+=10) {
                //执行publishProgress()调用onProgressUpdate()方法
                publishProgress(i);
            }
            return i+params[0].intValue()+"";
        }

    //该方法运行在Ui线程内，可以对UI线程内的控件设置和修改其属性
    @Override
    protected void onPreExecute() {
    	String rtn=L.State.ERROR;
		HttpPost request1 = new HttpPost(L.notificationUrl);
		//Looper.prepare();
			try {
				 
				DefaultHttpClient client1 = new DefaultHttpClient();
				client1.getCookieStore()
						.addCookie(L.cookie);
				HttpResponse httpResponse1 = client1
						.execute(request1);
				String retSrc1 = EntityUtils
						.toString(httpResponse1.getEntity());
				System.out.println("______________________"
						+ retSrc1);
				JSONArray ja = new JSONArray(retSrc1.trim()
						.replaceAll("msg", "")
						.replaceAll("=", ""));
				Map m=new HashMap();
				for (int ii = 0; ii < ja.length(); ii++) {
					JSONObject jo = ja.getJSONObject(ii);
					m.put(
							jo.getString("mkxkmc"),
							jo.getString("count"));
				}
				L.notificationNum=m;
				
				rtn=L.State.SUCCESS;
				Message message = new Message();  
	            message.what = 1;  
	            mHandler.sendMessage(message);  
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Looper.loop();
    }

    //在doInBackground方法当中，每次调用publishProgrogress()方法之后，都会触发该方法
    @Override
    protected void onProgressUpdate(Integer... values) {
    }
    //在doInBackground方法执行结束后再运行，并且运行在UI线程当中
    //主要用于将异步操作任务执行的结果展示给用户
    @Override
    protected void onPostExecute(String result) {
    }
    
//    @Override
//    protected void onCancelled() {
//        
//    }
 
    
}