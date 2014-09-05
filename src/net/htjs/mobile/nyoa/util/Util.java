package net.htjs.mobile.nyoa.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import net.htjs.mobile.nyoa.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

public final class Util {

	/**
	 * 判断sdcard是否存在；
	 * 
	 * 
	 * @return
	 */
	public static boolean isSdCardExsit() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static void Tag(String msg, Context c) {
		//Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
	}
	 public static DataInputStream Terminal(String command) throws Exception  
	    {  
	        Process process = Runtime.getRuntime().exec("su");  
	        //执行到这，Superuser会跳出来，选择是否允许获取最高权限  
	        OutputStream outstream = process.getOutputStream();  
	        DataOutputStream DOPS = new DataOutputStream(outstream);  
	        InputStream instream = process.getInputStream();  
	        DataInputStream DIPS = new DataInputStream(instream);  
	        String temp = command + "\n";  
	        //加回车  
	        DOPS.writeBytes(temp);  
	        //执行  
	        DOPS.flush();  
	        //刷新，确保都发送到outputstream  
	        DOPS.writeBytes("exit\n");  
	        //退出  
	        DOPS.flush();  
	        process.waitFor();  
	        return DIPS;  
	    }  
	public static boolean isRooted() {  
        //检测是否ROOT过  
        DataInputStream stream;  
        boolean flag=false;  
        try {  
            stream = Terminal("ls /data/");  
            //目录哪都行，不一定要需要ROOT权限的  
            if(stream.readLine()!=null)flag=true;  
            //根据是否有返回来判断是否有root权限  
        } catch (Exception e1) {  
            e1.printStackTrace();  
   
        }  
   
        return flag;  
    }  
	public static String getJsonFromUrl(String url, String status)
			throws ClientProtocolException, IOException {
		HttpPost request = new HttpPost(url);

		/*
		 * String random=(String)request.getParameter("random"); String
		 * signcert=(String)request.getParameter("signcert"); String
		 * randomsigndata=(String)request.getParameter("randomsigndata");
		 * 
		 * 
		 * map.put("signcert", signcert); map.put("signdata", randomsigndata);
		 * map.put("sourcedata", random); map.put("appcode", "1234");
		 */

		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse;
		if (status != null && status.length() > 0) {
			StringEntity se = new StringEntity(status);
			request.setEntity(se);
		}

		httpResponse = client.execute(request);
		// 得到应答的字符串，这也是一个 JSON 格式保存的数据
		String retSrc = EntityUtils.toString(httpResponse.getEntity());

		return retSrc;
	}

	public static String getJsonFromUrl(String url, List status)
			throws ClientProtocolException, IOException {
		HttpPost request = new HttpPost(url);

		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse;
		if (status != null) {
			request.setEntity(new UrlEncodedFormEntity(status, HTTP.UTF_8));
		}

		httpResponse = client.execute(request);
		// 得到应答的字符串，这也是一个 JSON 格式保存的数据
		String retSrc = EntityUtils.toString(httpResponse.getEntity());

		return retSrc;
	}

	public static boolean checkSDCard() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED))
			return true;
		else
			return false;
	}

	public static JSONObject getJOFromString(String s) {
		JSONObject jo = null;
		try {
			jo = new JSONObject(s);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jo;
	}

	public static JSONArray getJAFromString(String s) {
		JSONArray ja = null;
		try {
			ja = new JSONArray(s);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ja;
	}

	public static synchronized String run(String[] cmd, String workdirectory)
			throws IOException {
		StringBuffer result = new StringBuffer();
		try {
			// 创建操作系统进程（也可以由Runtime.exec()启动）
			// Runtime runtime = Runtime.getRuntime();
			// Process proc = runtime.exec(cmd);
			// InputStream inputstream = proc.getInputStream();
			ProcessBuilder builder = new ProcessBuilder(cmd);

			InputStream in = null;
			// 设置一个路径（绝对路径了就不一定需要）
			if (workdirectory != null) {
				// 设置工作目录（同上）
				builder.directory(new File(workdirectory));
				// 合并标准错误和标准输出
				builder.redirectErrorStream(true);
				// 启动一个新进程
				Process process = builder.start();

				// 读取进程标准输出流
				in = process.getInputStream();
				byte[] re = new byte[1024];
				while (in.read(re) != -1) {
					result = result.append(new String(re));
				}
			}
			// 关闭输入流
			if (in != null) {
				in.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result.toString();
	}

	public static boolean RootCmd(String cmd) {
		Process process = null;
		DataOutputStream os = null;
		DataInputStream is = null;
		try {
			process = Runtime.getRuntime().exec("sh");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			is = new DataInputStream(process.getInputStream());
			int a = is.available();
			byte[] b = new byte[a];
			is.read(b, 0, a);
			System.out.println(new String(b));
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				System.out.println(cmd);
				if (process != null)
				{
					process.destroy();
				}
			} catch (Exception e) {
			}
		}
		return true;
	}

	public static Object getProperty(Object owner, String fieldName)
			throws Exception {
		Class ownerClass = owner.getClass();

		Field field = ownerClass.getField(fieldName);

		Object property = field.get(owner);

		return property;
	}

	// 得到对象的静态属性
	public Object getStaticProperty(String className, String fieldName)
			throws Exception {
		Class ownerClass = Class.forName(className);

		Field field = ownerClass.getField(fieldName);

		Object property = field.get(ownerClass);

		return property;
	}

	public static Object invokeMethod(Object owner, String methodName,
			Object[] args) throws Exception {
		if(owner == null){
			return null;
		}
		Class ownerClass = owner.getClass();

		if (args == null) {
			Method method = ownerClass.getMethod(methodName, null);
			return method.invoke(owner, null);
		}

		Class[] argsClass = new Class[args.length];

		for (int i = 0, j = args.length; i < j; i++) {
			argsClass[i] = args[i].getClass();
		}
		Method method = ownerClass.getMethod(methodName, argsClass);
		return method.invoke(owner, args);
	}

	// 执行类静态方法
	public Object invokeStaticMethod(String className, String methodName,
			Object[] args) throws Exception {
		if (className == null || methodName == null) {
			return null;
		}
		Class ownerClass = Class.forName(className);
		Class[] argsClass = new Class[args.length];

		for (int i = 0, j = args.length; i < j; i++) {
			argsClass[i] = args[i].getClass();
		}

		Method method = ownerClass.getMethod(methodName, argsClass);

		return method.invoke(null, args);
	}

	public static String dialogOnlyCommit(Context c, String msg, String post,
			String nega, final Object owner, final String methodName,
			final Object[] args) {
		AlertDialog.Builder builder = new Builder(c);
		builder.setMessage(msg);
		builder.setTitle("提示");
		builder.setIcon(R.drawable.logonyoa);
		builder.setPositiveButton(post, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				try {
					invokeMethod(owner, methodName, args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		builder.create().show();
		return L.SUCCESS;
	}

	public static String dialog(Context c, String msg, String post,
			String nega, final Object owner, final String methodName,
			final Object[] args) {
		AlertDialog.Builder builder = new Builder(c);
		builder.setMessage(msg);
		builder.setTitle("提示");
		builder.setIcon(R.drawable.logonyoa);
		builder.setPositiveButton(post, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				try {
					invokeMethod(owner, methodName, args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		builder.setNegativeButton(nega, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
		return L.SUCCESS;
	}

	public static String dialog(Context c, String msg, String post,
			String nega, final Object owner, final String methodName,
			final Object[] args, final Object owner2, final String methodName2,
			final Object[] args2, final boolean is2after1) {
		AlertDialog.Builder builder = new Builder(c);
		builder.setMessage(msg);
		builder.setTitle("提示");

		builder.setPositiveButton(post, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				try {
					invokeMethod(owner, methodName, args);
					if (is2after1) {
						invokeMethod(owner2, methodName2, args2);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		builder.setNegativeButton(nega, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				try {
					invokeMethod(owner2, methodName2, args2);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		builder.create().show();
		return L.SUCCESS;
	}

	public static void dialogProcess(Context c, String msg, String post,
			String nega, final Object owner, final String methodName,
			final Object[] args, final Object owner2, final String methodName2,
			final Object[] args2) {
	}

}
