
package net.htjs.mobile.nyoa.gw;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Random;

import net.htjs.mobile.nyoa.BaseActivity;
import net.htjs.mobile.nyoa.LoginActivity;
import net.htjs.mobile.nyoa.R;
import net.htjs.mobile.nyoa.util.FileService;
import net.htjs.mobile.nyoa.util.L;
import net.htjs.mobile.nyoa.util.Util;
import net.htjs.mobile.nyoa.util.UtilIntent;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ListGwWebView extends BaseActivity {
	
        protected WebView webView;
        static String LX="";
        ProgressDialog progressDialog, xh_pDialog;
        private int xh_count;
        // 获取SD卡目录
       
        // 提示信息对话框控制对象
        final GetListHandler handler = new GetListHandler();
        
        protected static String dowloadDir = Environment.getExternalStorageDirectory() + "/ideasdownload/";
        
        protected String filePath = "";
        public boolean onKeyDown(int keyCode, KeyEvent event) {
    		if (event.getAction() == KeyEvent.ACTION_DOWN) {
    			switch (keyCode) {
    			case KeyEvent.KEYCODE_BACK:
    				if(webView.canGoBack()){
    					webView.goBack();
    					return true;
    				}
                 
                default:
                	break;
    			}
    		}
    		return super.onKeyDown(keyCode, event);
    	}
        public void callPhone(String url){
        	Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse(url));
    	    
        	ListGwWebView.this.startActivity(intent);
        }
		public void onCreate(Bundle b) {
                super.onCreate(b);
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                this.setContentView(R.layout.gw_webview);

                webView = (WebView) this.findViewById(R.id.webviewFirst);
                webView.getSettings().setJavaScriptEnabled(true);

                webView.getSettings().setPluginsEnabled(true);
                webView.getSettings().setAllowFileAccess(true);
                webView.getSettings().setPluginsEnabled(true);
                webView.getSettings().setPluginState(PluginState.ON);
                // 提示信息对话框初始化
                progressDialog = new ProgressDialog(ListGwWebView.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
               // progressDialog.setTitle("请稍等");
                progressDialog.setMessage("正在读取数据!");
                //progressDialog.set

                // 显示提示信息对话框
                progressDialog.show();

                // 设置webView客户端加载对象（为默认浏览器组件）
                webView.setWebViewClient(new WebViewClient() {
                		
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                
                        	if(url.toLowerCase().startsWith("tel:")){
                            	callPhone(url);
                            	//Util.dialogProcess(getApplicationContext(), "拨打电话 "+url.replace("tel:", ""), "拨打", "取消", ListGwWebView.this, "callPhone", new Object[]{url}, null, null, null);
                            	return true;
                            }else 	if (url.contains("main.jsp")) {
                                        finish();

                                        return true;
                            }else if(url.contains("/server/3g/index.jsp")){
                            	 Intent intent = new Intent();
                                 intent.setClass(getApplication(), LoginActivity.class);
                                 startActivity(intent); 
                                 finish();
                                 return true;
                            }
                                view.loadUrl(url);
                                return true;
                        }
                        // 页面加载前调方法定义
                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {

                                if (!progressDialog.isShowing())
                                        progressDialog.show();

                                L.l(url + " ------------------------------------");
                                
                                url = url.replace(L.dthwzUrl, L.nywzUrl);
                                
                                L.l(url + " ------------------------------------");
                                if(url.toLowerCase().startsWith("tel:")){
                                	callPhone(url);
                                	//Util.dialogProcess(getApplicationContext(), "拨打电话 "+url.replace("tel:", ""), "拨打", "取消", ListGwWebView.this, "callPhone", new Object[]{url}, null, null, null);
                                	return ;
                                }
                                if (url.contains("/server/3g/index.jsp")) {
                                        Intent intent = new Intent();
                                        intent.setClass(getApplication(), LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                        return;
                                } else if (url.contains("main.jsp")) {
	                                	Intent intent = new Intent();
	                                    intent.setClass(getApplication(), LoginActivity.class);
	                                    startActivity(intent);    
	                                	finish();
                                        // webView.onKeyDown(KeyEvent.KEYCODE_BACK, null);
                                        return;
                                } else if (url.contains("fileDown.jsp")) {
                                        
                                        String urlAfter = url.substring(url.lastIndexOf("&")+1);
                                        String id = urlAfter.substring(urlAfter.lastIndexOf("=") + 1);
                                        L.l(id + "++++++++++++++++++++++++");
                                        
                                        //android 4.0 时本应用安装于系统目录下面，不能按默认取路径方法读取，采取了常量路径的方法
                                        if(L.LocationToInstall){
                                			dowloadDir="/sdcard/ideasdownload/";
                                		}
                                        filePath = dowloadDir + id + ".doc";
                                        if(id.toLowerCase().indexOf("pdf")>=0){
                                        	filePath = dowloadDir + id;
                                        }
                                        download(url, filePath);
                                      
                                        return;
                                }else if (url.endsWith(".pdf")){
                                	String urlAfter = url;
                                    String id = urlAfter.substring(urlAfter.lastIndexOf("/") + 1);
                                    L.l(id + "++++++++++++++++++++++++");
                                    
                                    //android 4.0 时本应用安装于系统目录下面，不能按默认取路径方法读取，采取了常量路径的方法
                                    if(L.LocationToInstall){
                            			dowloadDir="/sdcard/ideasdownload/";
                            		}
                                    filePath = dowloadDir + id + ".pdf";
                                    if(id.toLowerCase().indexOf("pdf")>=0){
                                    	filePath = dowloadDir + id;
                                    }
                                    download(url, filePath);
                                    return;
                                }
                                super.onPageStarted(view, url, favicon);
                        }

                        // 页面加载回调方法定义
                        public void onPageFinished(WebView view, String url) {
                                new Thread(new Runnable() {
                                        public void run() {
                                                Message msg = handler.obtainMessage();
                                                handler.sendMessage(msg);
                                        }
                                }).start();
                                super.onPageFinished(view, url);
                        }
                });
               
                // 得到上个activity传入的参数信息
                Bundle bundle = this.getIntent().getExtras();
                String URL = "";
                String lx = bundle.getString(L.LX);
                LX=lx;
                Cookie sessionCookie=null;
                if(LX.equals(L.Nine.DZYX)){
                	sessionCookie = L.mailcookie;
                }else{
                	sessionCookie = L.cookie;
                }
                CookieSyncManager.createInstance(this);
                CookieManager cookieManager = CookieManager.getInstance();
                if (sessionCookie != null) {
                        String cookieString = sessionCookie.getName() + "="
                                        + sessionCookie.getValue() + "; domain="
                                        + sessionCookie.getDomain();
                        cookieManager.setCookie(L.baseDomain, cookieString);
                        CookieSyncManager.getInstance().sync();
                }
               
                if (L.Nine.GW.endsWith(lx) || L.Nine.XW.equals(lx)) {
                        String zt = bundle.getString(L.ZT);
                        URL = L.gwListWapUrl + "&ZT=" + zt + "&CZRY_DM="
                                        + L.status.get("CZRY_DM") + "&JSESSIONID="
                                        + L.status.get("JSESSIONID");
                } else if (L.Nine.DZGG.endsWith(lx)) {
                        URL = L.dzggUrl + "&CZRY_DM=" + L.status.get("CZRY_DM")
                                        + "&JSESSIONID=" + L.status.get("JSESSIONID");
                } else if (L.Nine.DZYX.endsWith(lx)) {
                        URL = L.dzyxUrl+L.Nine.MAILSESSIONID;
                } else if (L.Nine.GZZD.endsWith(lx)) {
                        URL = L.gzzdUrl + "&CZRY_DM=" + L.status.get("CZRY_DM")
                                        + "&JSESSIONID=" + L.status.get("JSESSIONID");
                } else if (L.Nine.TXL.endsWith(lx)) {
                        URL = L.txlUrl + "&CZRY_DM=" + L.status.get("CZRY_DM")
                                        + "&JSESSIONID=" + L.status.get("JSESSIONID");
                }else if (L.Nine.WZ.endsWith(lx)) {
                    URL = L.nywzUrl;
                }

                webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                webView.getSettings().setSupportZoom(true);
                webView.getSettings().setBuiltInZoomControls(true);
                webView.getSettings().setUseWideViewPort(true);
                webView.loadUrl(URL);
                System.out.println(L.cookie);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            menu.add(Menu.NONE, Menu.FIRST + 1, 2, "刷新" ).setIcon(R.drawable.refresh_menu);
            menu.add(Menu.NONE, Menu.FIRST + 2, 1,"关闭").setIcon(android.R.drawable.ic_delete);
            return true;

        }
        public void shutDown(){
        	apnutility.StopYidongApn();
            super.shutDown();
        }
        // 菜单项被选择事件
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) { 
            case Menu.FIRST + 1:
                Toast.makeText(this, "刷新菜单被点击了", Toast.LENGTH_LONG).show();
                webView.reload();
                break;
            case Menu.FIRST + 2:
                Toast.makeText(this, "关闭菜单被点击了", Toast.LENGTH_LONG).show();
                //this.finish();
                Util.dialog(ListGwWebView.this, "是否关闭应用", "关闭应用", "继续操作", ListGwWebView.this,
						"shutDown", null, ListGwWebView.this, null, null, false);
                break;
            case Menu.FIRST + 3:
                Toast.makeText(this, "帮助菜单被点击了", Toast.LENGTH_LONG).show();
                break;
            case Menu.FIRST + 4:
                Toast.makeText(this, "添加菜单被点击了", Toast.LENGTH_LONG).show();
                break;
            case Menu.FIRST + 5:
                Toast.makeText(this, "详细菜单被点击了", Toast.LENGTH_LONG).show();
                break;
            case Menu.FIRST + 6:
                Toast.makeText(this, "发送菜单被点击了", Toast.LENGTH_LONG).show();
                break;
            }
            return false;
        }

        // 选项菜单被关闭事件，菜单被关闭有三种情形，menu按钮被再次点击、back按钮被点击或者用户选择了某一个菜单项
        @Override
        public void onOptionsMenuClosed(Menu menu) {
            Toast.makeText(this, "选项菜单关闭了", Toast.LENGTH_LONG).show();
        }

        // 菜单被显示之前的事件
        @Override 
        public boolean onPrepareOptionsMenu(Menu menu) {
            Toast.makeText(this, "显示选项菜单", Toast.LENGTH_LONG).show();
            // 如果返回false，此方法就把用户点击menu的动作给消费了，onCreateOptionsMenu方法将不会被调用
            return true;
        }
        
        class GetListHandler extends Handler {
                @Override
                public void handleMessage(Message msg) {
                        if(progressDialog!=null)progressDialog.dismiss();// 关闭进度条
                }
        }

        public void openFile(String args, String fileName) {
                Intent intent = UtilIntent.getWordFileIntent(fileName);
                startActivity(intent);
        }
        
        public void download(final String url, String fileName) {

                /*
                 * 下载进度条初始化
                 */
                xh_count = 0;

                // 创建ProgressDialog对象
                xh_pDialog = new ProgressDialog(ListGwWebView.this);

                // 设置进度条风格，风格为长条形的
                xh_pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

                xh_pDialog.setTitle("下载提示");
                xh_pDialog.setMessage("下载文件进度");
                xh_pDialog.setIndeterminate(false);
                xh_pDialog.setProgress(100);
                xh_pDialog.setIcon(R.drawable.logonyoa);
                xh_pDialog.setCancelable(true);
                xh_pDialog.show();

                new Thread() {
                        @Override
                        public void run() {
                                try {
                                        while (xh_count>=0&&xh_count < 100) {
                                                // 由线程来控制进度
                                                xh_pDialog.setProgress(xh_count);
                                                Thread.sleep(100);
                                        }
                                        if(xh_count<0){
                                        	Util.dialog(ListGwWebView.this, "下载失败，是否重新下载？",
                                                    "重新下载", "取消", ListGwWebView.this, "download",
                                                    new String[] { url, filePath },
                                                    null, "openFile", new String[] {
                                                                    url, filePath }, false);
                                        }else{
                                        	xh_pDialog.cancel();
                                            Thread.sleep(500);
                                            
                                            Intent intent = null;
                                            if(filePath.toLowerCase().endsWith(".doc"))
                                            {
                                            	intent = UtilIntent.getWordFileIntent(filePath);
                                            }else{
                                            	intent = UtilIntent.getPdfFileIntent(filePath);
                                            	System.out.println("PPPPPPPPPPPPPPPPPPPPPPPP");
                                            }
                                            startActivity(intent);
                                        }
                                } catch (Exception e) {
                                        xh_pDialog.cancel();
                                }
                        }
                }.start();
                // 获取SD卡目录
                File file = new File(dowloadDir);
                // 创建下载目录
                if (!file.exists()) {
                        file.mkdirs();
                }

                // 读取下载线程数，如果为空，则单线程下载
                int downloadTN = 1;
                new downloadTask(url, Integer.valueOf(downloadTN), fileName).start();
        }

        /**
         * @author ideasandroid 主下载线程
         */
        public class downloadTask extends Thread {
                protected int blockSize, downloadSizeMore;
                private int threadNum = 5;
                String urlStr, threadNo, fileName;

                public downloadTask(String urlStr, int threadNum, String fileName) {
                        this.urlStr = urlStr;
                        this.threadNum = threadNum;
                        this.fileName = fileName;
                }
                @Override
                public void run() {
                		
                		Log.d("ListView", urlStr);
                		Log.d("ListView", fileName);
                        try {
                               
                                DefaultHttpClient client = new DefaultHttpClient();
                                HttpResponse httpResponse = null;
                                File file = new File(fileName);
                               
                                if(urlStr.endsWith(".pdf")){
                                	HttpGet get = new HttpGet(urlStr);
                                    httpResponse = client.execute(get);
	                                RandomAccessFile fos = new RandomAccessFile(file, "rw");
	                                fos.write(EntityUtils.toByteArray(httpResponse.getEntity()));
	                                fos.close();
                        		}else{
                        			 HttpPost request = new HttpPost(urlStr);
                        			 httpResponse= client.execute(request);
	                                /*
	                                 * if(!file.exists()){ file.createNewFile(); }
	                                 */
	                                RandomAccessFile fos = new RandomAccessFile(file, "rw");
	                                fos.write(EntityUtils.toByteArray(httpResponse.getEntity()));
	                               // System.out.println(new String(ab));
	                                fos.close();
                        		}
                                xh_count = new Random().nextInt(30) + 20;

                                sleep(300);
                                xh_count = new Random().nextInt(50) + 20;

                                sleep(300);
                                xh_count = new Random().nextInt(70) + 20;

                                sleep(300);
                                xh_count = 100;
                                sleep(300);

                        } catch (Exception e) {
                        		xh_count=-1;
                                e.printStackTrace();
                        }
                }
        }
}
