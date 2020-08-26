package sunmi.scan;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.sunmi.scan.Config;
import com.sunmi.scan.Image;
import com.sunmi.scan.ImageScanner;
import com.sunmi.scan.Symbol;
import com.sunmi.scan.SymbolSet;


public class MainActivity extends Activity implements SurfaceHolder.Callback {
	private Camera mCamera;
	private SurfaceHolder mHolder;
	private SurfaceView surface_view;
	private ImageScanner scanner;//声明扫描器
	private Handler autoFocusHandler;
	SoundUtils soundUtils;
	public boolean use_auto_focus = true;//T1/T2 mini定焦摄像头没有对焦功能,应该改为false
	public int decode_count = 0;
	private TextView textview;
	//预览分辨率设置，T1/T2 mini设置640x480，其他手持机可选取640x480,800x480,1280x720
	public static int previewSize_width = 640;
	public static int previewSize_height = 480;
	Image imgae_data = new Image(previewSize_width, previewSize_height, "Y800");
	StringBuilder sb = new StringBuilder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (soundUtils != null) {
			soundUtils.release();
		}
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	private void init() {
		surface_view = (SurfaceView) findViewById(R.id.surface_view);
		textview = (TextView) findViewById(R.id.textview);
		mHolder = surface_view.getHolder();
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mHolder.addCallback(this);

		scanner = new ImageScanner();//创建扫描器
		scanner.setConfig(Symbol.NONE, Config.ENABLE_MULTILESYMS, 0);//是否开启同一幅图一次解多个条码,0表示只解一个，1为多个,默认0：禁止
		scanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);//允许识读QR码，默认1:允许
		scanner.setConfig(Symbol.PDF417, Config.ENABLE, 0);//允许识读PDF417码，默认0：禁止
		scanner.setConfig(Symbol.DataMatrix, Config.ENABLE, 0);//允许识读DataMatrix码，默认0：禁止
		scanner.setConfig(Symbol.AZTEC, Config.ENABLE, 0);//允许识读AZTEC码，默认0：禁止

		if (use_auto_focus)
			autoFocusHandler = new Handler();
		decode_count = 0;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (mHolder.getSurface() == null) {
			return;
		}
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
		}
		try {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(previewSize_width, previewSize_height);  //设置预览分辨率
			if (use_auto_focus)
				parameters.setFocusMode(parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			mCamera.setParameters(parameters);
			mCamera.setDisplayOrientation(90);//手持机使用，竖屏显示,T1/T2 mini需要屏蔽掉
			mCamera.setPreviewDisplay(mHolder);
			mCamera.setPreviewCallback(previewCallback);
			mCamera.startPreview();
		} catch (Exception e) {
			Log.d("DBG", "Error starting camera preview: " + e.getMessage());
		}
	}

	/**
	 * 预览数据
	 */
	PreviewCallback previewCallback = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			imgae_data.setData(data);
			long startTimeMillis = System.currentTimeMillis();
			//解码，返回值为0代表失败，>0表示成功
			int nsyms = scanner.scanImage(imgae_data);
			long endTimeMillis = System.currentTimeMillis();
			long cost_time = endTimeMillis - startTimeMillis;
			sb.append("计数: " + decode_count++);
			sb.append("\n耗时: " + cost_time + " ms\n");

			if (nsyms != 0) {
				playBeepSoundAndVibrate();//解码成功播放提示音
				SymbolSet syms = scanner.getResults();//获取解码结果
				//如果允许识读多个条码，则解码结果可能不止一个
				for (Symbol sym : syms) {
					sb.append("码制: " + sym.getSymbolName() + "\n");
					sb.append("容量: " + sym.getDataLength() + "\n");
					sb.append("内容: " + sym.getResult());
				}
			}
			textview.setText(sb.toString());
			sb.delete(0, sb.length());
		}
	};

	/**
	 * 自动对焦回调
	 */
	AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};
	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (null == mCamera || null == autoFocusCallback) {
				return;
			}
			mCamera.autoFocus(autoFocusCallback);
		}
	};

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera = Camera.open();
		} catch (Exception e) {
			mCamera = null;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	private void initBeepSound() {
		if (soundUtils == null) {
			soundUtils = new SoundUtils(this, SoundUtils.RING_SOUND);
			soundUtils.putSound(0, R.raw.beep);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		initBeepSound();
	}

	private void playBeepSoundAndVibrate() {
		if (soundUtils != null) {
			soundUtils.playSound(0, SoundUtils.SINGLE_PLAY);
		}
	}
}
