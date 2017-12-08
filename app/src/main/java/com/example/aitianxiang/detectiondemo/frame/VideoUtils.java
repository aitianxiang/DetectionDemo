package com.example.aitianxiang.detectiondemo.frame;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class VideoUtils {
	public VideoUtils() {
		super();

	}

	public List<String> getFrams(String viedioPath, String potoPath) {

		List<String> mThumbPaths = new ArrayList<String>();
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(viedioPath);
		// 取得视频的长度(单位为毫秒)
		String time = retriever
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		// 取得视频的长度(单位为秒)
		int seconds = Integer.valueOf(time) / 1000;
		// 获得想要的帧数
		@SuppressWarnings("unused")
		int num = seconds * 24;
		// 得到每一秒时刻的bitmap比如第一秒,第二秒
		if (!new File(potoPath).exists()) {
			new File(potoPath).mkdirs();
		}

		for (int i = 1; i <= seconds; i++) {
			// 获取的是微秒
			//Bitmap bitmap = retriever.getFrameAtTime(i * 500 * 1000,
			//		MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
			//String path = potoPath + File.separator + i*0.5 + ".jpg";
			Bitmap bitmap1 = retriever.getFrameAtTime(i * 1000 * 1000,
					MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
			String path1 = potoPath + File.separator + i + ".jpg";

			//mThumbPaths.add(path);
			mThumbPaths.add(path1);
			//FileOutputStream fos = null;
			FileOutputStream fos1 = null;
			try {
			//	fos = new FileOutputStream(path);
			//	bitmap.compress(CompressFormat.JPEG, 100, fos);
				fos1 = new FileOutputStream(path1);
				bitmap1.compress(CompressFormat.JPEG, 100, fos1);
			//	fos.close();
				fos1.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mThumbPaths;

	}

	public String getFrams(String viedioPath, String potoPath, int number) {
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(viedioPath);

		if (!new File(potoPath).exists()) {
			new File(potoPath).mkdirs();
		}
		// 获取的是微秒
		Bitmap bitmap = retriever.getFrameAtTime(number * 1000 * 1000,
				MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
		String path = potoPath + File.separator + number + ".jpg";
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path);
			bitmap.compress(CompressFormat.JPEG, 100, fos);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return path;

	}

}
