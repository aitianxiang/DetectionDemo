package com.example.aitianxiang.detectiondemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import com.example.aitianxiang.detectiondemo.ui.*;
import com.example.aitianxiang.detectiondemo.frame.*;
import com.example.aitianxiang.detectiondemo.env.Logger;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    //classify
    private String videoPath;
    private String photoPath;

    private TextView text1;
    private TextView text2;
    private Button button;
    private Button button1;
    private VideoView video;
    private ImageView image;
    private ImageView image2;
    private RelativeLayout rl;
    private MediaController mMediaController;
    private long lastProcessingTimeMs;

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";
    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/imagenet_comp_graph_label_strings.txt";

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

    //detection

    private Classifier detector;
    private static final Logger LOGGER = new Logger();
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/frozen_inference_graph_ssd_retrain2.pb";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/voc_labels.txt";
    //private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_v1_android_export.pb";
    //private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt";

    private Matrix cropToFrameTransform;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.loadLibrary("tensorflow_inference");
        Log.i(TAG,"import tensorflow successfully!!!");

        videoPath = RapidlyLoanInfoContents.videoPath;
        photoPath = RapidlyLoanInfoContents.photoPath;

        if (!Utils.checkFileExists(videoPath)) {
            Utils mUtils = new Utils(this);
            mUtils.copyFile("eloanvideo.mp4", videoPath);

        }

        initView();
        initTensorFlowAndLoadModel();
        detectImage();

    }

    private void initView() {
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(this);
        video = (VideoView) findViewById(R.id.video);
        image = (ImageView) findViewById(R.id.image);
        image2 = (ImageView) findViewById(R.id.imageView2);
        text1 = (TextView) findViewById(R.id.text1);
        text2 = (TextView) findViewById(R.id.text2);

        if (new File(videoPath).exists()) {
            video.setVideoURI(Uri.parse(videoPath));
        }
        rl = (RelativeLayout) findViewById(R.id.rl);


        mMediaController = new MediaController(this);

        ViewGroup mvView = (ViewGroup) mMediaController.getParent();

        mvView.removeView(mMediaController);

        LinearLayout.LayoutParams lpLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mMediaController.setLayoutParams(lpLayoutParams);

        rl.addView(mMediaController);
        video.setMediaController(mMediaController);
        video.requestFocus();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                // 获取帧
                VideoUtils mVideoUtils1 = new VideoUtils();
                // 获取帧
                String potop1 = mVideoUtils1.getFrams(videoPath, photoPath, 8);
                Log.i(TAG, "potop path:  "+potop1+" !!!!!");
                if (!new File(potop1).exists()) {
                    Log.i(TAG, "file not found!!!  ");
                }
                //image.setImageURI(Uri.parse(potop1));
                Bitmap bmp1 = BitmapFactory.decodeFile(new File(potop1).getAbsolutePath());
                if(bmp1 == null) {
                    Log.i(TAG, "bitmap is null!!!  ");
                }
                bmp1 = Bitmap.createScaledBitmap(bmp1, INPUT_SIZE, INPUT_SIZE, false);
                final long startTime = SystemClock.uptimeMillis();
                final List<Classifier.Recognition> results1 = detector.recognizeImage(bmp1);
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                Log.i(TAG,"results:--------"+results1.toString());
                Log.i(TAG,"time:--------"+lastProcessingTimeMs);
                Bitmap detectImageresult = drawImage(bmp1,results1);
                image.setImageBitmap(detectImageresult);
                text1.setText(results1.get(0).toString());
                break;
            case R.id.button1:
                // 获取帧
                VideoUtils mVideoUtils = new VideoUtils();
                // 获取帧
                String potop = mVideoUtils.getFrams(videoPath, photoPath, 5);
                Log.i(TAG, "potop path:  "+potop+" !!!!!");
                if (!new File(potop).exists()) {
                    Log.i(TAG, "file not found!!!  ");
                }
                image2.setImageURI(Uri.parse(potop));
                Bitmap bmp = BitmapFactory.decodeFile(new File(potop).getAbsolutePath());
                if(bmp == null) {
                    Log.i(TAG, "bitmap is null!!!  ");
                }
                bmp = Bitmap.createScaledBitmap(bmp, INPUT_SIZE, INPUT_SIZE, false);
                //image.setImageBitmap(bmp);
                final List<Classifier.Recognition> results = classifier.recognizeImage(bmp);

                Log.i(TAG,"results:--------"+results.toString());

                text2.setText(results.toString());

                //result = TensorFlowImageClassifier.Recognition();

                break;
            default:
                break;
        }

    }
    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                    //makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private void detectImage() {
        Log.i(TAG,"use TF_OD_API_MODEL_FILE successfully");
        int cropSize = TF_OD_API_INPUT_SIZE;
        try {
            detector = TensorFlowObjectDetectionAPIModel.create(
                    getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            cropSize = TF_OD_API_INPUT_SIZE;
            Log.i(TAG,"create detector successfully!");
        } catch (final IOException e) {
            LOGGER.e("Exception initializing classifier!", e);
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    private Bitmap drawImage(Bitmap sourceImage,List<Classifier.Recognition> dresult){
        Bitmap cropCopyBitmap = Bitmap.createBitmap(sourceImage);
        final Canvas canvas = new Canvas(cropCopyBitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(2.0f);

        float minimumConfidence = 0.12f;
        final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();

        //for (final Classifier.Recognition result : dresult) {
          //  final RectF location = result.getLocation();
            //if (location != null && result.getConfidence() >= minimumConfidence) {
             //   canvas.drawRect(location, paint);
            //}
        //}
        canvas.drawRect(dresult.get(0).getLocation(), paint);

        return cropCopyBitmap;

    }
}
