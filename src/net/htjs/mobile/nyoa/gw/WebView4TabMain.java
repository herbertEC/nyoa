package net.htjs.mobile.nyoa.gw;

import net.htjs.mobile.nyoa.util.L;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebView4TabMain extends ListGwWebView{

	
	public void onCreate(Bundle b){
		
		super.onCreate(b);
		// 设置webView客户端加载对象（为默认浏览器组件）
        webView.setWebViewClient(new WebViewClient() {
        		
        		
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                	 url = url.replace(L.dthwzUrl, L.nywzUrl);    
                	if(url.toLowerCase().startsWith("tel:")){
                    	callPhone(url);
                    	//Util.dialogProcess(getApplicationContext(), "拨打电话 "+url.replace("tel:", ""), "拨打", "取消", ListGwWebView.this, "callPhone", new Object[]{url}, null, null, null);
                    	return true;
                    }else 	if (url.contains("main.jsp")) {
                    	 

                        return true;
                    }else if(url.contains("/server/3g/index.jsp")){
                    	  

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
                               
                                return;
                        } else if (url.contains("main.jsp")) {
                             
                                return;
                        } else if (url.contains("fileDown.jsp")) {
                              
                                String urlAfter = url.substring(0, url.lastIndexOf("&"));
                                String id = urlAfter.substring(urlAfter.lastIndexOf("=") + 1);
                                L.l(id + "++++++++++++++++++++++++");

                                filePath = dowloadDir + id + ".doc";
                                
                                download(url, filePath);
                                return;
                        }else if(url.toLowerCase().endsWith(".pdf")){
                        	//String urlAfter = url.substring(0, url.lastIndexOf("&"));
                            String id = System.currentTimeMillis()+"";
                            L.l(id + "++++++++++++++++++++++++");

                            filePath = dowloadDir + id + ".pdf";
                            
                            download(url, filePath);
                        }
                        super.onPageStarted(view, url, favicon);
                }

                // 页面加载回调方法定义
                public void onPageFinished(WebView view, String url) {
                        // TODO Auto-generated method stub
                        new Thread(new Runnable() {

                                public void run() {
                                        // TODO Auto-generated method stub
                                        Message msg = handler.obtainMessage();
                                        handler.sendMessage(msg);
                                }

                        }).start();
                        super.onPageFinished(view, url);
                        //webView.loadUrl("javascript:" + "alert(123);");
                }

        });
       
	}
}
