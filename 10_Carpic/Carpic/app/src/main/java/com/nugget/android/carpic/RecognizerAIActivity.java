package com.nugget.android.carpic;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class RecognizerAIActivity extends AppCompatActivity implements SurfaceHolder.Callback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String LOG_TAG = RecognizerAIActivity.class.getSimpleName();

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1001;

    private CameraDevice mCameraDevice;
    private Camera mCamera = null;
    private SurfaceView mCameraSurface = null;
    private SurfaceHolder mCameraSurfaceHolder = null;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mSession;
    private Handler mHandler;
    private ImageReader mImageReader;
    private int mDeviceRotation;
    private Sensor mMagnetometer;
    private Sensor mAccelerometer;
    private SensorManager mSensorManager;
    private DeviceOrientation deviceOrientation;
    int mDSI_height, mDSI_width;

    private EditText mCarNumberEditView;
    private Button mEditButton = null;

    String mCurrentPhotoPath;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_IMAGE_CROP = 4444;


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(ExifInterface.ORIENTATION_NORMAL, 0);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_90, 90);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_180, 180);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_270, 270);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(LOG_TAG, "onCreate started in RecognizerAIActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognizer_ai);

        // check android version for permission support or not after 6.0(marshmellow)
        checkVersion();

        // prepare screen
        prepareScreen();

        // init camera
        initCamera();

        // init surface view and register call back
        initSurfaceView();

        // do not use
//        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        deviceOrientation = new DeviceOrientation();

        mCarNumberEditView = (EditText)findViewById(R.id.car_number_editview);
        mCarNumberEditView.setText("00가0000");
        mEditButton = (Button)findViewById(R.id.edit_button);
    }

    @Override
    protected void onResume() {
        Log.e(LOG_TAG, "onResume started in RecognizerAIActivity");
        super.onResume();

        // do not use
//        mSensorManager.registerListener(deviceOrientation.getEventListener(), mAccelerometer, SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(deviceOrientation.getEventListener(), mMagnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        Log.e(LOG_TAG, "onPause started in RecognizerAIActivity");
        super.onPause();

        // do not use
//        mSensorManager.unregisterListener(deviceOrientation.getEventListener());
    }

    public void initSurfaceView() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDSI_height = displayMetrics.heightPixels;
        mDSI_width = displayMetrics.widthPixels;

        mCameraSurface = (SurfaceView)findViewById(R.id.camera_surfaceview);
        mCameraSurfaceHolder = mCameraSurface.getHolder();
        mCameraSurfaceHolder.addCallback(this);

//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        mDSI_height = displayMetrics.heightPixels;
//        mDSI_width = displayMetrics.widthPixels;
//
//
//        mCameraSurfaceHolder = mCameraSurface.getHolder();
//        mCameraSurfaceHolder.addCallback(new SurfaceHolder.Callback() {

//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                initCameraAndPreview();
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//
//                if (mCameraDevice != null) {
//                    mCameraDevice.close();
//                    mCameraDevice = null;
//                }
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//            }
//        });
    }

    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean hasPermissions(String[] permissions) {
        Log.e(LOG_TAG, "hasPermissions started in RecognizerAIActivity");
        int result;

        // check permissions
        for (String perms : permissions){
            result = ContextCompat.checkSelfPermission(this, perms);
            if (result == PackageManager.PERMISSION_DENIED){
                // there is a permission which is not permitted
                return false;
            }
        }

        // allowed all permission
        return true;
    }

    private void checkVersion() {
        Log.e(LOG_TAG, "checkVersion started in RecognizerAIActivity");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                Log.e(LOG_TAG, "request permission in checkVersion, RecognizerAIActivity");
                // request permission if not yet
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            } else {
                Log.e(LOG_TAG, "request permission in checkVersion, RecognizerAIActivity");
                // do nothing
//                Intent mainIntent = new Intent(RecognizerAIActivity.this, MainActivity.class);
//                startActivity(mainIntent);
//                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e(LOG_TAG, "onRequestPermissionsResult started in RecognizerAIActivity");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;
                    boolean diskPermissionAccepted = grantResults[1]
                            == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted || !diskPermissionAccepted) {
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                    } else {
                        Intent mainIntent = new Intent(RecognizerAIActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                    }
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        Log.e(LOG_TAG, "showDialogForPermission started in RecognizerAIActivity");
        AlertDialog.Builder builder = new AlertDialog.Builder( RecognizerAIActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }

    private void prepareScreen() {
        Log.e(LOG_TAG, "prepareScreen started in RecognizerAIActivity");
        // hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // keep on screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initCamera() {
        Log.e(LOG_TAG, "initCamera started in RecognizerAIActivity");
        mCamera = Camera.open();
        if (mCamera == null) {
            Log.e(LOG_TAG, "mCamera is null, in initCamera, RecognizerAIActivity");
            return;
        }
        mCamera.setDisplayOrientation(90);

//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        mDSI_height = displayMetrics.heightPixels;
//        mDSI_width = displayMetrics.widthPixels;
//
//        mCameraSurface = (SurfaceView)findViewById(R.id.camera_surfaceview);
//        mCameraSurfaceHolder = mCameraSurface.getHolder();
//        mCameraSurfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(LOG_TAG, "surfaceCreated started in RecognizerAIActivity");
        initCameraAndPreview();
//        try {
//            if (mCamera == null) {
//                mCamera.setPreviewDisplay(holder);
//                mCamera.startPreview();
//            }
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "Exception in surfaceCreated, RecognizerAIActivity, e = " + e);
//        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(LOG_TAG, "surfaceChanged started in RecognizerAIActivity");
        if (mCameraSurfaceHolder.getSurface() == null) {
            Log.e(LOG_TAG, "mCameraSurfaceHolder.getSurface() is null in surfaceChanged, RecognizerAIActivity");
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception in surfaceChanged, RecognizerAIActivity, e = " + e);
        }

        if (mCamera == null) {
            Log.e(LOG_TAG, "mCamera is null in surfaceChanged, RecognizerAIActivity");
            return;
        }
        // can't get parameters
//        Camera.Parameters parameters = mCamera.getParameters();
//        if (parameters != null) {
//            List<String> focusModes = parameters.getSupportedFocusModes();
//            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
//                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//            }
//            mCamera.setParameters(parameters);
//        }
//        else {
//            Log.e(LOG_TAG, "parameters is null in surfaceChanged, RecognizerAIActivity");
//        }

        // View 를 재생성한다.
        try {
            mCamera.setPreviewDisplay(mCameraSurfaceHolder);
            mCamera.startPreview();
        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(LOG_TAG, "surfaceDestroyed started in RecognizerAIActivity");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        else {
            Log.e(LOG_TAG, "mCamera is null in surfaceDestroyed, RecognizerAIActivity");
        }
    }

    //촬영 버튼 클릭
    public void onClickCameraTakeBtn(View view) {
        Log.e(LOG_TAG, "onClickCameraTakeBtn started in RecognizerAIActivity");
        mCarNumberEditView.setText("");
        takePicture();
    }

    //등록 버튼 클릭
    public void onClickRegisterBtn(View view) {

        // "차량등록" 클릭시 등록화면으로 이동
        Intent registerIntent = new Intent(RecognizerAIActivity.this, CarInsert.class);
        // 인식결과 insert 화면으로 보내기
        //registerIntent.putExtra("차번호", carNumber);

        RecognizerAIActivity.this.startActivity(registerIntent);
        Log.d(LOG_TAG,"onClickCameraFinishBtn started in RecognizerAIActivity");
    }



//    //완료 버튼 클릭
//    public void onClickCameraFinishBtn(View view) {
//        Log.e(LOG_TAG, "onClickCameraFinishBtn started in RecognizerAIActivity");
//
//        Log.e(LOG_TAG, "MakeNetworkCall.execute in MainActivity");
//        //new MakeNetworkCall().execute("http://3.16.54.45:80/http.php?post=1", "Post");
//        new MakeNetworkCall().execute("http://3.16.54.45:80/http.php?get=1", "Get");
//    }
//
//    //재촬영 버튼 클릭
//    public void onClickCameraReTakeBtn(View view) {
//        Log.e(LOG_TAG, "onClickCameraReTakeBtn started in RecognizerAIActivity");
//    }

    public void takePreview() throws CameraAccessException {
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewBuilder.addTarget(mCameraSurfaceHolder.getSurface());
        mCameraDevice.createCaptureSession(Arrays.asList(mCameraSurfaceHolder.getSurface(), mImageReader.getSurface()), mSessionPreviewStateCallback, mHandler);
    }

    private CameraCaptureSession.StateCallback mSessionPreviewStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mSession = session;

            try {
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                mSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Toast.makeText(RecognizerAIActivity.this, "카메라 구성 실패", Toast.LENGTH_SHORT).show();
        }
    };

    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            mSession = session;
            unlockFocus();
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            mSession = session;
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };

    @TargetApi(19)
    public void initCameraAndPreview() {
        Log.e(LOG_TAG, "initCameraAndPreview started in RecognizerAIActivity");
        HandlerThread handlerThread = new HandlerThread("CAMERA2");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        Handler mainHandler = new Handler(getMainLooper());
        try {
            String mCameraId = "" + CameraCharacteristics.LENS_FACING_FRONT; // 후면 카메라 사용

            CameraManager mCameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            Size largestPreviewSize = map.getOutputSizes(ImageFormat.JPEG)[0];
            Log.i("LargestSize", largestPreviewSize.getWidth() + " " + largestPreviewSize.getHeight());

            setAspectRatioTextureView(largestPreviewSize.getHeight(),largestPreviewSize.getWidth());

            mImageReader = ImageReader.newInstance(largestPreviewSize.getWidth(), largestPreviewSize.getHeight(), ImageFormat.JPEG,/*maxImages*/7);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mainHandler);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mCameraManager.openCamera(mCameraId, deviceStateCallback, mHandler);
        } catch (CameraAccessException e) {
            Toast.makeText(this, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            Image image = reader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            new SaveImageTask().execute(bitmap);
        }
    };

    private CameraDevice.StateCallback deviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            try {
                takePreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Toast.makeText(RecognizerAIActivity.this, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    };

    public void takePicture() {
        Log.e(LOG_TAG, "takePicture started in RecognizerAIActivity");
        try {
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            // 화면 회전 안되게 고정시켜 놓은 상태에서는 아래 로직으로 방향을 얻을 수 없어서
            // 센서를 사용하는 것으로 변경
            //deviceRotation = getResources().getConfiguration().orientation;
            mDeviceRotation = ORIENTATIONS.get(deviceOrientation.getOrientation());

            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mDeviceRotation);
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            mSession.capture(mCaptureRequest, mSessionCaptureCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getRotatedBitmap(Bitmap bitmap, int degrees) throws Exception {
        Log.e(LOG_TAG, "getRotatedBitmap started in RecognizerAIActivity");
        if(bitmap == null) return null;
        if (degrees == 0) return bitmap;

        Matrix m = new Matrix();
        m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private void unlockFocus() {
        Log.e(LOG_TAG, "unlockFocus started in RecognizerAIActivity");
        try {
            // Reset the auto-focus trigger
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            mSession.capture(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);
            // After this, the camera will go back to the normal state of preview.
            mSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

//    private String dateName(long dateTaken){
//        Date date = new Date(dateTaken);
//        SimpleDateFormat dateFormat =
//        new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
//        return dateFormat.format(date);
//    }

    public static final String insertImage(ContentResolver cr, Bitmap source, String title, String description) {
        Log.e(LOG_TAG, "insertImage started in RecognizerAIActivity");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        //Log.e(LOG_TAG, "System.currentTimeMillis() in insertImage, RecognizerAIActivity, System.currentTimeMillis() = " + System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null;    /* value to be returned */


        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Log.e(LOG_TAG, "url in insertImage, RecognizerAIActivity, uri = " + url);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                Log.e(LOG_TAG, "in insertImage, RecognizerAIActivity, stringUrl = " + url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
            //Log.e(LOG_TAG, "in insertImage, RecognizerAIActivity, stringUrl = " + stringUrl);
        }

        return stringUrl;
    }

    private class SaveImageTask extends AsyncTask<Bitmap, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(RecognizerAIActivity.this, "사진을 저장했습니다", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Bitmap... data) {
            Bitmap bitmap = null;
            try {
                bitmap = getRotatedBitmap(data[0], mDeviceRotation);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            insertImage(getContentResolver(), bitmap, "CARPIC_" + timeStamp, "");

            return null;
        }
    }

    private void setAspectRatioTextureView(int ResolutionWidth , int ResolutionHeight) {
        Log.e(LOG_TAG, "setAspectRatioTextureView started in RecognizerAIActivity");
        if(ResolutionWidth > ResolutionHeight){
            int newWidth = mDSI_width;
            int newHeight = ((mDSI_width * ResolutionWidth)/ResolutionHeight);
            updateTextureViewSize(newWidth,newHeight);

        }else {
            int newWidth = mDSI_width;
            int newHeight = ((mDSI_width * ResolutionHeight)/ResolutionWidth);
            updateTextureViewSize(newWidth,newHeight);
        }
    }

    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        Log.e(LOG_TAG, "updateTextureViewSize started in RecognizerAIActivity");
        mCameraSurface.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
    }

    //1-3 이미지 파일 생성
//    public File createImageFile() throws IOException {
//        // 이미지 파일명 생성
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "CARPIC_" + timeStamp + ".jpg";
//        File imageFile = null;
//        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "CARPIC");
//
//        if (!storageDir.exists()) {
//            Log.i("mCurrentPhotoPath1", storageDir.toString());
//            storageDir.mkdirs();
//        }
//
//        imageFile = new File(storageDir, imageFileName);
//        mCurrentPhotoPath = imageFile.getAbsolutePath();
//
//        return imageFile;
//    }
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case REQUEST_TAKE_PHOTO:
//
//                if (resultCode == Activity.RESULT_OK) {
//                    try {
//                        Log.i("REQUEST_TAKE_PHOTO", "OK");
//                        //galleryAddPic();
//                        uploadFile(mCurrentPhotoPath);
//                        Log.e(LOG_TAG, "MakeNetworkCall.execute in MainActivity");
//                        new MakeNetworkCall().execute("http://3.16.54.45:80/carnumrec/http.php?post=1", "Post");
//
//                    } catch (Exception e) {
//                        Log.e("REQUEST_TAKE_PHOTO", e.toString());
//                    }
//                } else {
//                    Toast.makeText(RecognizerAIActivity.this, "번호판 촬영을 취소했습니다", Toast.LENGTH_SHORT).show();
//                }
//                break;
//
//            case REQUEST_IMAGE_CROP:
//                if (resultCode == Activity.RESULT_OK) {
//                    //galleryAddPic();
//                    uploadFile(mCurrentPhotoPath);
//                }
//                break;
//        }
//    }
//
//    public void uploadFile(String filePath){
//        String url = "http://3.16.54.45/imgupload.php";
//        try {
//            UploadFile uploadFile = new UploadFile(this);
//            uploadFile.setPath(filePath);
//            uploadFile.execute(url);
//        } catch (Exception e){
//            Log.e("UploadFile", String.valueOf(e));
//        }
//    }

    // 2019.09.17 add =>
    //[2. 네트워크 연결] 상세
    //2-1 GET입력
    String str = null;

    InputStream ByGetMethod(String ServerURL) {
        Log.e(LOG_TAG, "ByGetMethod started in RecognizerAIActivity");

        InputStream DataInputStream = null;
        try { //서버연결
            URL url = new URL(ServerURL);
            HttpURLConnection cc = (HttpURLConnection)url.openConnection();
            cc.setReadTimeout(5000);
            cc.setConnectTimeout(5000);
            cc.setRequestMethod("GET");
            cc.setDoInput(true);
            cc.connect();

            int response = cc.getResponseCode();
            Log.e(LOG_TAG, "ByGetMethod started in RecognizerAIActivity, response = " + response);
            if (response == HttpURLConnection.HTTP_OK) {
                DataInputStream = cc.getInputStream();
                Log.e(LOG_TAG, "ByGetMethod, DataInputStream = " + DataInputStream);

                int i;
                StringBuffer buffer = new StringBuffer();
                byte[] b = new byte[1024];
                while( (i = DataInputStream.read(b)) != -1) {
                    buffer.append(new String(b, 0, i));
                }
                str = buffer.toString();
                Log.e(LOG_TAG, "ByGetMethod, str = " + str);
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //carInfoDialog(str);
                        mCarNumberEditView.setText(str);
                    }
                }, 0);
                if(mHandler !=null) {mHandler.removeMessages(0);}
            }
            else {
                Log.e(LOG_TAG, "ByGetMethod, response = " + response);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in ByGetMethod, e = " + e);
        }

        Log.e(LOG_TAG, "ByGetMethod ended in RecognizerAIActivity");

        return DataInputStream;
    }

    //2-2 post입력
    InputStream ByPostMethod(String ServerURL) {
        Log.e(LOG_TAG, "ByPostMethod started in RecognizerAIActivity");
        InputStream DataInputStream = null;
        try {
            //String PostParam = "first_name=dong&last_name=gam";
//            String PostParam = "first_name=soyeon&last_name=son";
            String PostPath[] = mCurrentPhotoPath.split("/");
            Log.i(LOG_TAG, "mCurrentPhotoPath"+mCurrentPhotoPath);
            Log.i(LOG_TAG, "mCurrentPhotoPath"+PostPath[6]);
            String PostParam = "path="+PostPath[6];
            URL url = new URL(ServerURL);

            HttpURLConnection cc = (HttpURLConnection)url.openConnection();
            if(cc == null) {
                Log.e(LOG_TAG, "ByPostMethod, cc is null");
            }
            cc.setReadTimeout(5000);
            cc.setConnectTimeout(5000);
            cc.setRequestMethod("POST");
            cc.setDoInput(true);
            cc.connect();

            DataOutputStream dos = new DataOutputStream(cc.getOutputStream());
            dos.writeBytes(PostParam);
            dos.flush();
            dos.close();

            int response = cc.getResponseCode();

            if (response == HttpURLConnection.HTTP_OK) {
                DataInputStream = cc.getInputStream();
                // 핸들러 시작
//                int i;
//                StringBuffer buffer = new StringBuffer();
//                byte[] b = new byte[1024];
//                while( (i = DataInputStream.read(b)) != -1) {
//                    buffer.append(new String(b, 0, i));
//                }
//                str = buffer.toString();
//                Handler mHandler = new Handler(Looper.getMainLooper());
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        carInfoDialog(str);
//                    }
//                }, 0);
////                if(mHandler !=null) {mHandler.removeMessages(0);}
//                // TODO: 2019-09-30 핸들러 종료 아직 못함
                // 핸들러 끝
//                Log.e(TAG, "ByGetMethod, str = " + str);
            }
            else {
                Log.e(LOG_TAG, "ByPostMethod, response = " + response);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in PostData, error = " + e);
        }

        Log.e(LOG_TAG, "ByPostMethod ended in HttpExampleActivity");

        return DataInputStream;
    }

    //2-3 string변환
    String ConvertStreamToString(InputStream stream) {
        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(isr);
        StringBuilder response = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error in ConvertStreamToString, error = " + e);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in ConvertStreamToString", e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error in ConvertStreamToString", e);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error in ConvertStreamToString", e);
            }
        }

        return response.toString();
    }

    private class MakeNetworkCall extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // do nothing
        }

        @Override
        protected String doInBackground(String... arg) {
            InputStream is = null;
            String URL = arg[0];
            Log.e(LOG_TAG, "doInBackground started in MainActivity, URL: " + URL);
            String res = "";

            Log.e(LOG_TAG, "doInBackground, arg[1] = " + arg[1]);
            if (arg[1].equals("Post")) {
                is = ByPostMethod(URL);
            } else if (arg[1].equals("Get")) {
                is = ByGetMethod(URL);
            } else {
                Log.e(LOG_TAG, "doInBackground, do nothing, arg[1] = " + arg[1]);
            }

            if (is != null) {
                res = ConvertStreamToString(is);
            } else {
                res = "Something went wrong";
            }
            return res;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.e(LOG_TAG, "onPostExecute, result: "+ result);
            // carInfoDialog(result);
            // 인식 결과를 EditView에 바로 갱신
            mCarNumberEditView.setText("");
            mCarNumberEditView.setText(result);
        }
    }
}
