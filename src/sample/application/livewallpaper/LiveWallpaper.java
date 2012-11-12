package sample.application.livewallpaper;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.livewallpaper.R;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.service.wallpaper.WallpaperService.Engine;
import android.view.SurfaceHolder;

public class LiveWallpaper extends WallpaperService{
	
	private final Handler handler = new Handler();
	public int interval;
	public int counter;
	public String strFolder;
	public SharedPreferences prefs;

	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine();
	}
	
	public void init(){
		this.counter = 0;
		this.interval = Integer.parseInt(this.prefs.getString("Interval", "600000"));
		this.strFolder = this.prefs.getString("Folder", "");
	}
	
	public class WallpaperEngine extends Engine 
			implements SharedPreferences.OnSharedPreferenceChangeListener{
		
		public int[] resBitmap;
		public int w;
		public int h;
		public boolean mVisible;
		
		private final Runnable mWallpaper = new Runnable(){
			@Override
			public void run(){
				showWallpaper();
			}
		};

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			
			this.resBitmap = new int[]{R.drawable.img1,R.drawable.img2};
			prefs = getSharedPreferences("SlideshowWallpaperPrefs", MODE_PRIVATE);
			prefs.registerOnSharedPreferenceChangeListener(this);
			init();
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			
			handler.removeCallbacks(mWallpaper);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			super.onVisibilityChanged(visible);
			
			this.mVisible = visible;
			if(visible){
				this.showWallpaper();
			}else{
				handler.removeCallbacks(mWallpaper);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			this.w = width;
			this.h = height;
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
		}
		
		public void showWallpaper(){
			final SurfaceHolder mHolder = getSurfaceHolder();
			Bitmap bitmap;
			String fn = findImageFile();
			if("" == fn){
				bitmap = loadImageFromResource();
			}else{
				bitmap = loadImageFile(fn);
			}
			Canvas canvas = mHolder.lockCanvas();
			if(canvas != null){
				canvas.drawColor(Color.BLACK);
				canvas.drawBitmap(bitmap, 0, 0, null);
				mHolder.unlockCanvasAndPost(canvas);
			}
			counter++;
			handler.removeCallbacks(mWallpaper);
			if(mVisible){
				handler.postDelayed(mWallpaper, interval);
			}
		}
		
		public Bitmap loadImageFromResource(){
			
			Bitmap bitmap;
			if(counter >= resBitmap.length){
				counter = 0;
			}
			Resources res = getResources();
			bitmap = BitmapFactory.decodeResource(res, resBitmap[counter]);
			bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false);
			return bitmap;
		}
		
		public Bitmap loadImageFile(String path){
			
			Bitmap bitmap;
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			
			BitmapFactory.decodeFile(path, opt);
			int oh = opt.outHeight;
			int ow = opt.outWidth;
			
			BitmapFactory.Options opt2 = new BitmapFactory.Options();
			if(oh < this.h || ow < this.w){
				opt2.inSampleSize = Math.min(this.w/ow, this.h/oh);
			}else{
				opt2.inSampleSize = Math.max(ow/this.w, oh/this.h);
			}
			bitmap = BitmapFactory.decodeFile(path, opt2);
			
			if(this.h > this.w){
				Bitmap offbitmap = Bitmap.createBitmap(this.w, this.h, Bitmap.Config.ARGB_8888);
				Canvas offCanvas = new Canvas(offbitmap);
				bitmap = Bitmap.createScaledBitmap(bitmap, (int)(this.w)
						, (int)(this.w*(((double)oh)/((double)ow))),false);
				offCanvas.drawBitmap(bitmap, 0, (this.h - bitmap.getHeight())/2, null);
				bitmap = offbitmap;
			}else{
				Bitmap offbitmap = Bitmap.createBitmap(this.w, this.h, Bitmap.Config.ARGB_8888);
				Canvas offCanvas = new Canvas(offbitmap);
				bitmap = Bitmap.createScaledBitmap(bitmap
						, (int)(this.h*((double)ow/(double)oh)), h, false);
				offCanvas.drawBitmap(bitmap, (this.w - bitmap.getWidth())/2, (this.h - bitmap.getHeight())/2, null);
				bitmap = offbitmap;
			}
			
			return bitmap;
		}
		
		public String findImageFile(){
			String fn = "";
			FileFilter fFilter = new FileFilter(){
				public boolean accept(File file){
					Pattern p = Pattern.compile
							("\\.png$|\\.jpg$|\\.gif$|\\.jpeg$|\\.bmp$",Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(file.getName());
					boolean result = m.find()&&!file.isHidden();
					return result;
				}
			};
				
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				
				File folder = new File(strFolder);
				File[] fc = folder.listFiles(fFilter);
				
				if(null != fc){
					if(fc.length > 0){
						if(counter >= fc.length){
							counter = 0;
						}
						fn = fc[counter].toString();
					}
				}
			}
			return fn;
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			
			init();
			this.showWallpaper();
			
		}
	}
	

}
