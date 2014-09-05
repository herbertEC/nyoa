package net.htjs.mobile.nyoa.subview;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import net.htjs.mobile.nyoa.R;
import net.htjs.mobile.nyoa.util.L;
import net.htjs.mobile.nyoa.util.Util;
import net.htjs.mobile.nyoa.util.UtilIntent;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FileDownLoad extends Activity {

	private TextView downloadUrl;
	private TextView downloadFileName;
	// private EditText downloadThreadNum;
	private Button downloadBt;
	private Button downloadFh;
	private Button downloadOpen;
	private ProgressBar downloadProgressBar;
	private TextView progressMessage;
	private int downloadedSize = 0;
	private long fileSize = 0;
	private String fileType = "";

	// 获取SD卡目录
	private static String dowloadDir = Environment
			.getExternalStorageDirectory() + "/ideasdownload/";
	private String filePath = "";

	ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.filedowdload);

		Bundle bundle = this.getIntent().getExtras();
		String URL = bundle.getString(L.FileDownLoad.URL);
		String fileName = bundle.getString(L.FileDownLoad.FILENAME);
		fileType = bundle.getString(L.FileDownLoad.FILETYPE);
		downloadUrl = (TextView) findViewById(R.id.path);
		fileName += fileType;
		downloadFileName = (TextView) findViewById(R.id.downloadFileName);
		// downloadThreadNum = //(EditText)
		// findViewById(R.id.downloadThreadNum);
		progressMessage = (TextView) findViewById(R.id.downloadmsg);

		downloadUrl.setText(URL);
		downloadFileName.setText(fileName);
		downloadBt = (Button) findViewById(R.id.downloadBt);
		downloadFh = (Button) findViewById(R.id.downloadfh);
		downloadOpen = (Button) findViewById(R.id.downloadopen);
		downloadProgressBar = (ProgressBar) findViewById(R.id.downloadbar);//
		downloadProgressBar.setVisibility(View.VISIBLE);
		downloadProgressBar.setMax(100);
		downloadProgressBar.setProgress(0);
		downloadBt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				filePath = dowloadDir + downloadFileName.getText();
				File fileP = new File(filePath);
				if (fileP.exists()) {
					Util.dialog(FileDownLoad.this, "文件已经存在，是否重新下载？", "重新下载",
							"取消", FileDownLoad.this, "download", null);
					// dialog("文件已经存在，是否重新下载？", "重新下载", "取消");
				} else
					download();
			}
		});
		downloadFh.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		progressDialog = new ProgressDialog(FileDownLoad.this);// 生成一个进度条
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle("打开文件失败");
		progressDialog.setMessage("请点击下载文件!");

		downloadOpen.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				filePath = dowloadDir + downloadFileName.getText();
				File fileP = new File(filePath);
				if (!fileP.exists()) {
					progressMessage.setText("文件不存在，请下载文件");
				} else {
					try {
						Intent intent = UtilIntent.getWordFileIntent(filePath);
						startActivity(intent);
					} catch (Exception e) {
						progressMessage.setText("文件打开失败，请正确安装手机版word软件");
					}

				}
			}
		});
	}

	public void download() {

		File file = new File(dowloadDir);
		// 创建下载目录
		if (!file.exists()) {
			file.mkdirs();
		}

		// 读取下载线程数，如果为空，则单线程下载
		int downloadTN = 2; // Integer.valueOf("".equals(downloadThreadNum.getText()
		// .toString()) ? "1" : downloadThreadNum.getText().toString());
		// 如果下载文件名为空则获取Url尾为文件名
		int fileNameStart = downloadUrl.getText().toString().lastIndexOf("/");
		String fileName = "".equals(downloadFileName.getText().toString()) ? downloadUrl
				.getText().toString().substring(fileNameStart)
				: downloadFileName.getText().toString();

		// 开始下载前把下载按钮设置为不可用
		downloadBt.setClickable(false);
		// 进度条设为0
		downloadProgressBar.setProgress(0);
		// 启动文件下载线程
		new downloadTask(downloadUrl.getText().toString(),
				Integer.valueOf(downloadTN), dowloadDir + fileName).start();
		filePath = dowloadDir + fileName;
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 当收到更新视图消息时，计算已完成下载百分比，同时更新进度条信息
			Bundle b = msg.getData();
			int progress = (Double.valueOf((b.getString("processvalue"))))
					.intValue();
			if (progress == 100) {
				downloadBt.setClickable(true);
				progressMessage.setText("下载完成！");
			} else {
				progressMessage.setText("当前进度:" + progress + "%");
			}
			downloadProgressBar.setProgress(progress);
		}

	};

	class GetListHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			progressDialog.dismiss();// 关闭进度条
		}
	}

	/**
	 * @author ideasandroid 主下载线程
	 */
	public class downloadTask extends Thread {
		private int blockSize, downloadSizeMore;
		private int threadNum = 5;
		String urlStr, threadNo, fileName;

		public downloadTask(String urlStr, int threadNum, String fileName) {
			this.urlStr = urlStr;
			this.threadNum = threadNum;
			this.fileName = fileName;
		}

		@Override
		public void run() {
			FileDownloadThread[] fds = new FileDownloadThread[threadNum];
			try {
				HttpPost request = new HttpPost(urlStr);
				DefaultHttpClient client = new DefaultHttpClient();
				HttpResponse httpResponse = client.execute(request);
				// 得到应答的字符串，这也是一个 JSON 格式保存的数据

				File file = new File(fileName);
				RandomAccessFile fos = new RandomAccessFile(file, "rw");
				fos.write(EntityUtils.toByteArray(httpResponse.getEntity()));
				byte[] ab = new byte[1024];
				fos.read(ab, 0, 1024);
				System.out.println(new String(ab));
				fos.close();
				
				 URL url = new URL(urlStr); URLConnection conn =
				  url.openConnection(); //获取下载文件的总大小 
				 fileSize =httpResponse
				  .getEntity().getContentLength(); //计算每个线程要下载的数据量
				 blockSize =  (int) (fileSize / threadNum); // 解决整除后百分比计算误差
				  downloadSizeMore = (int) (fileSize % threadNum);
				  
				  for (int i = 0; i < threadNum; i++) { //启动线程，分别下载自己需要下载的部分
					  FileDownloadThread fdt = new FileDownloadThread(url, file, i*blockSize, (i + 1) * blockSize - 1); fdt.setName("Thread" +  i);
					  fdt.start(); 
					  fds[i] = fdt; 
					  } 
				  boolean finished = false;
				 
			} catch (Exception e) {
				}
	}

}

class FileDownloadThread extends Thread {
	private static final int BUFFER_SIZE = 1024;
	private URL url;
	private File file;
	private int startPosition;
	private int endPosition;
	private int curPosition;
	private boolean finished = false;
	private int downloadSize = 0;

	// 分块构造函数
	public FileDownloadThread(URL url, File file, int startPosition,
			int endPosition) {
		this.url = url;
		this.file = file;
		this.startPosition = startPosition;
		this.curPosition = startPosition;
		this.endPosition = endPosition;
	}

	public void run() {

		BufferedInputStream bis = null;
		RandomAccessFile fos = null;
		byte[] buf = new byte[BUFFER_SIZE];
		URLConnection con = null;
		try {
			// 打开URL连接
			con = url.openConnection();
			con.setAllowUserInteraction(true);
			// 判断是否该文件存在，如果存在且下载完成，直接返回。
			if ((file.length() + startPosition) == endPosition) {
				this.finished = true;
			}
			// 文件未下载完成，获取到当前指针位置，继续下载。
			else {
				con.setRequestProperty("Range", "bytes="
						+ (startPosition + file.length()) + "-" + endPosition);
				fos = new RandomAccessFile(file, "rw");
				fos.seek(file.length());
				bis = new BufferedInputStream(con.getInputStream());
				while (curPosition < endPosition) {
					int len = bis.read(buf, 0, BUFFER_SIZE);
					if (len == -1) {
						break;
					}
					fos.write(buf, 0, len);
					curPosition = curPosition + len;
					if (curPosition > endPosition) {
						downloadSize += len - (curPosition - endPosition) + 1;
					} else {
						downloadSize += len;
					}
				}
				this.finished = true;
				bis.close();
				fos.close();
			}
		} catch (IOException e) {
			System.out.println(getName() + " Error:" + e.getMessage());
		}
	}

	public boolean isFinished() {
		return finished;
	}

	public int getDownloadSize() {
		return downloadSize;
	}
}
}