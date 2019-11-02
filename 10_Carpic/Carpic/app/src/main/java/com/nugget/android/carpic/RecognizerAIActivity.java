package com.nugget.android.carpic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
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
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

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
    private ImageView mEditButton = null;

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
//
        initCamera();
//

//        mSurfaceView = findViewById(R.id.surfaceView);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        deviceOrientation = new DeviceOrientation();
        
        mCarNumberEditView = (EditText)findViewById(R.id.car_number_editview);
        mCarNumberEditView.setText("00가0000");
        mEditButton = (ImageView)findViewById(R.id.edit_button);
    }

    @Override
    protected void onResume() {
        Log.e(LOG_TAG, "onResume started in RecognizerAIActivity");
        super.onResume();

//        mSensorManager.registerListener(deviceOrientation.getEventListener(), mAccelerometer, SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(deviceOrientation.getEventListener(), mMagnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        Log.e(LOG_TAG, "onPause started in RecognizerAIActivity");
        super.onPause();

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

        // 작업을 위해 잠시 멈춘다
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

    public void onClickCameraTakeBtn(View view)
    {
        Log.e(LOG_TAG, "onClickCameraTakeBtn started in RecognizerAIActivity");
        takePicture();
    }

    public void onClickCameraFinishBtn(View view)
    {
        Log.e(LOG_TAG, "onClickCameraFinishBtn started in RecognizerAIActivity");
    }

    public void onClickCameraReTakeBtn(View view)
    {
        Log.e(LOG_TAG, "onClickCameraReTakeBtn started in RecognizerAIActivity");
    }

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

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
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
        }

        return stringUrl;
    }

    private class SaveImageTask extends AsyncTask<Bitmap, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(RecognizerAIActivity.this, "사진을 저장하였습니다.", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Bitmap... data) {
            Bitmap bitmap = null;
            try {
                bitmap = getRotatedBitmap(data[0], mDeviceRotation);
            } catch (Exception e) {
                e.printStackTrace();
            }
            insertImage(getContentResolver(), bitmap, ""+System.currentTimeMillis(), "");

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
}
