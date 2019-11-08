package com.nugget.android.carpic;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    private CameraDevice mCameraDevice;
    private Camera mCamera = null;
    private SurfaceView mCameraSurface = null;
    private SurfaceHolder mCameraSurfaceHolder = null;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mSession;
    private Handler mHandler;
    private ImageReader mImageReader;
    private int mDeviceRotation;
//    private Sensor mMagnetometer;
//    private Sensor mAccelerometer;
    private SensorManager mSensorManager;
    private DeviceOrientation deviceOrientation;
    int mDSI_height, mDSI_width;
    private EditText mCarNumberEditView;
    private Button mEditButton = null;

    String mCurrentPhotoPath;
    private static final String TAG = "AI_OCR >";

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(ExifInterface.ORIENTATION_NORMAL, 0);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_90, 90);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_180, 180);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_270, 270);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate started in RecognizerAIActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognizer_ai);

        // check android version for permission support or not after 6.0(marshmellow)
        checkVersion();

        // prepare screen
        prepareScreen();

        // init surface view and register call back
        initSurfaceView();

        // init camera
        initCamera();

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
        Log.e(TAG, "onResume started in RecognizerAIActivity");
        super.onResume();

        // do not use
//        mSensorManager.registerListener(deviceOrientation.getEventListener(), mAccelerometer, SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(deviceOrientation.getEventListener(), mMagnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause started in RecognizerAIActivity");
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

        //camera status+surfaceView open. do not use
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        mDSI_height = displayMetrics.heightPixels;
//        mDSI_width = displayMetrics.widthPixels;
////
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
        Log.e(TAG, "hasPermissions started in RecognizerAIActivity");
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
        Log.e(TAG, "checkVersion started in RecognizerAIActivity");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                Log.e(TAG, "request permission in checkVersion, RecognizerAIActivity");
                // request permission if not yet
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            } else {
                Log.e(TAG, "request permission in checkVersion, RecognizerAIActivity");
                // do nothing
//                Intent mainIntent = new Intent(RecognizerAIActivity.this, MainActivity.class);
//                startActivity(mainIntent);
//                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e(TAG, "onRequestPermissionsResult started in RecognizerAIActivity");
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
        Log.e(TAG, "showDialogForPermission started in RecognizerAIActivity");
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
        Log.e(TAG, "prepareScreen started in RecognizerAIActivity");
        // hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // keep on screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initCamera() {
        Log.e(TAG, "initCamera started in RecognizerAIActivity");
        mCamera = Camera.open();
        if (mCamera == null) {
            Log.e(TAG, "mCamera is null, in initCamera, RecognizerAIActivity");
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
        Log.e(TAG, "surfaceCreated started in RecognizerAIActivity");
        initCameraAndPreview();
//        try {
//            if (mCamera == null) {
//                mCamera.setPreviewDisplay(holder);
//                mCamera.startPreview();
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "Exception in surfaceCreated, RecognizerAIActivity, e = " + e);
//        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged started in RecognizerAIActivity");
        if (mCameraSurfaceHolder.getSurface() == null) {
            Log.e(TAG, "mCameraSurfaceHolder.getSurface() is null in surfaceChanged, RecognizerAIActivity");
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            Log.e(TAG, "Exception in surfaceChanged, RecognizerAIActivity, e = " + e);
        }

        if (mCamera == null) {
            Log.e(TAG, "mCamera is null in surfaceChanged, RecognizerAIActivity");
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
//            Log.e(TAG, "parameters is null in surfaceChanged, RecognizerAIActivity");
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
        Log.e(TAG, "surfaceDestroyed started in RecognizerAIActivity");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        else {
            Log.e(TAG, "mCamera is null in surfaceDestroyed, RecognizerAIActivity");
        }
    }

    //촬영 버튼 클릭
    public void onClickCameraTakeBtn(View view) {
        Log.e(TAG, "onClickCameraTakeBtn started in RecognizerAIActivity");
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
        Log.d(TAG,"onClickCameraFinishBtn started in RecognizerAIActivity");
    }

    public void takePreview() throws CameraAccessException {
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW); // 카메라 열기
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
    //여긴 촬영 대기하는 미리보기 화면
    @TargetApi(19)
    public void initCameraAndPreview() {
        Log.e(TAG, "initCameraAndPreview started in RecognizerAIActivity");
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

            mImageReader = ImageReader.newInstance(mCameraSurface.getWidth(), mCameraSurface.getHeight(), ImageFormat.JPEG,/*maxImages*/7);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mainHandler);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mCameraManager.openCamera(mCameraId, deviceStateCallback, mHandler);
        } catch (CameraAccessException e) {
            Toast.makeText(this, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    //캡쳐된 데이터를 파일로 밀어넣는 부분
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {


            Image image = null;
            try {
                image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                save(bytes); //파일저장
                uploadFile(mCurrentPhotoPath); //업로드

                new MakeNetworkCall().execute("http://3.16.54.45:80/carnumrec/http.php?post=1", "Post"); //Main.py
//                new MakeNetworkCall().execute("http://3.16.54.45:80/http.php?post=1", "Post"); //car_non.py
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //contentresolver를 사용, 사진정보를 입력하는 SaveImageTask > insertImage를 쓰지않고 바로 저장으로 대체
//            Image image = reader.acquireNextImage();
//            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//            byte[] bytes = new byte[buffer.remaining()];
//
//            buffer.get(bytes);
//
//            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

//            new SaveImageTask().execute(bitmap);
        }
        private void save(byte[] bytes) throws IOException {

            OutputStream output = null;
            try {
                File photofile = createImageFile();
                output = new FileOutputStream(photofile);
                output.write(bytes);

            }
            finally {
                if(output!=null) output.close();
            }
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
    //캡처데이터를 얻는부분
    public void takePicture() {
        Log.e(TAG, "takePicture started in RecognizerAIActivity");
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
//
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
//        Log.e(TAG, "unlockFocus started in RecognizerAIActivity");
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


    private void setAspectRatioTextureView(int ResolutionWidth , int ResolutionHeight) {
        Log.e(TAG, "setAspectRatioTextureView started in RecognizerAIActivity");
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
        Log.e(TAG, "updateTextureViewSize started in RecognizerAIActivity");
        mCameraSurface.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
    }

    public void uploadFile(String filePath){
        String url = "http://3.16.54.45/imgupload.php";
        try {
            UploadFile uploadFile = new UploadFile(this);
            uploadFile.setPath(filePath);
            uploadFile.execute(url);
        } catch (Exception e){
            Log.e("UploadFile", String.valueOf(e));
        }
    }

    // 2019.09.17 add =>
    //[2. 네트워크 연결] 상세

    //2-2 post입력
    InputStream ByPostMethod(String ServerURL) {
        Log.e(TAG, "ByPostMethod started in RecognizerAIActivity");
        InputStream DataInputStream = null;
        try {
//            String PostParam = "first_name=soyeon&last_name=son";
            String PostPath[] = mCurrentPhotoPath.split("/");
            Log.i(TAG, "mCurrentPhotoPath"+mCurrentPhotoPath);
            Log.i(TAG, "mCurrentPhotoPath"+PostPath[6]);
            String PostParam = "path="+PostPath[6];
            URL url = new URL(ServerURL);

            HttpURLConnection cc = (HttpURLConnection)url.openConnection();
            if(cc == null) {
                Log.e(TAG, "ByPostMethod, cc is null");
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

            }
            else {
                Log.e(TAG, "ByPostMethod, response = " + response);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in PostData, error = " + e);
        }

        Log.e(TAG, "ByPostMethod ended in HttpExampleActivity");

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
            Log.e(TAG, "Error in ConvertStreamToString, error = " + e);
        } catch (Exception e) {
            Log.e(TAG, "Error in ConvertStreamToString", e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error in ConvertStreamToString", e);
            } catch (Exception e) {
                Log.e(TAG, "Error in ConvertStreamToString", e);
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
            Log.e(TAG, "doInBackground started in MainActivity, URL: " + URL);
            String res = "";

            Log.e(TAG, "doInBackground, arg[1] = " + arg[1]);
            if (arg[1].equals("Post")) {
                is = ByPostMethod(URL);
            } else if (arg[1].equals("Get")) {
//                is = ByGetMethod(URL);
            } else {
                Log.e(TAG, "doInBackground, do nothing, arg[1] = " + arg[1]);
            }

            if (is != null) {
                res = ConvertStreamToString(is);
            } else {
                Toast.makeText(RecognizerAIActivity.this, "연결상태를 확인해 주세요", Toast.LENGTH_SHORT).show();
                res = "Something went wrong";
            }
            return res;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.e(TAG, "onPostExecute, result: "+ result);
            // carInfoDialog(result);
            // 인식 결과를 EditView에 바로 갱신


            if (result.length() > 12) {
                //인식.py파일 exception 발생범위가 커서 결과값 길이로 대체
                Toast.makeText(RecognizerAIActivity.this, "다시 촬영해 주세요", Toast.LENGTH_SHORT).show();
                Log.e("RecogResultFalse", result);
            }
            else {
                Toast.makeText(RecognizerAIActivity.this, "사진을 저장했습니다", Toast.LENGTH_SHORT).show();
                Log.e("RecogResultTrue", result);
                mCarNumberEditView.setText("");
                mCarNumberEditView.setText(result);
            }
        }
    }

    public File createImageFile() throws IOException {
        // 이미지 파일명 생성
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "CARPIC_" + timeStamp + ".jpg";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "CARPIC");

        if (!storageDir.exists()) {
            Log.i("mCurrentPhotoPath1", storageDir.toString());
            storageDir.mkdirs();
        }

        imageFile = new File(storageDir, imageFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }
//    ===================미사용===========================
// 회전저장
//    public Bitmap getRotatedBitmap(Bitmap bitmap, int degrees) throws Exception {
//        Log.e(TAG, "getRotatedBitmap started in RecognizerAIActivity");
//        if(bitmap == null) return null;
//        if (degrees == 0) return bitmap;
//
//        Matrix m = new Matrix();
//        m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
//
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
//    }

    // 파일저장 메소드
//           public final String insertImage(ContentResolver cr, Bitmap source, String title, String description) {
//        Log.e(TAG, "insertImage started in RecognizerAIActivity");
//        // 이미지 파일명 생성
////        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
////        String imageFileName = "CARPIC_" + timeStamp + ".jpg";
////        File imageFile = null;
////        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "CARPIC");
////
////        if (!storageDir.exists()) {
////            Log.i("mCurrentPhotoPath1", storageDir.toString());
////            storageDir.mkdirs();
////        }
////
////        imageFile = new File(storageDir, imageFileName);
////        mCurrentPhotoPath = imageFile.getAbsolutePath();
//
//
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE, title);
//        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
//        values.put(MediaStore.Images.Media.DESCRIPTION, description);
//        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//        // Add the date meta data to ensure the image is added at the front of the gallery
//        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
//        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//        //Log.e(TAG, "System.currentTimeMillis() in insertImage, RecognizerAIActivity, System.currentTimeMillis() = " + System.currentTimeMillis());
//
//        Uri url = null;
//        String stringUrl = null;    /* value to be returned */
//
//
//        try {
//            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//            Log.e(TAG, "url in insertImage, RecognizerAIActivity, uri = " + url);
//
//            if (source != null) {
//
//                OutputStream imageOut  = cr.openOutputStream(url);
//
//                Log.e(TAG, "in insertImage, RecognizerAIActivity, stringUrl = " + url);
//                try {
//                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
//                } finally {
//                    imageOut.close();
//                }
//            } else {
//                cr.delete(url, null, null);
//                url = null;
//            }
//        } catch (Exception e) {
//            if (url != null) {
//                cr.delete(url, null, null);
//                url = null;
//            }
//        }
//
//        if (url != null) {
//            stringUrl = url.toString();
//            Log.e(TAG, "in insertImage, RecognizerAIActivity, stringUrl = " + stringUrl);
//        }
//
//        return stringUrl;
//    }

//    private class SaveImageTask extends AsyncTask<Bitmap, Void, Void> {
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            Toast.makeText(RecognizerAIActivity.this, "사진을 저장했습니다", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        protected Void doInBackground(Bitmap... data) {
//            Bitmap bitmap = null;
//            try {
//                bitmap = getRotatedBitmap(data[0], mDeviceRotation);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
////            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
////            mCurrentPhotoPath = insertImage(getContentResolver(), bitmap, "CARPIC", "");
//            Log.e("mCurrentPhotoPath", mCurrentPhotoPath);
//            return null;
//        }
//    }


    //2-1 GET입력
//    String str = null;
    //Get
//    InputStream ByGetMethod(String ServerURL) {
//        Log.e(TAG, "ByGetMethod started in RecognizerAIActivity");
//
//        InputStream DataInputStream = null;
//        try { //서버연결
//            URL url = new URL(ServerURL);
//            HttpURLConnection cc = (HttpURLConnection)url.openConnection();
//            cc.setReadTimeout(5000);
//            cc.setConnectTimeout(5000);
//            cc.setRequestMethod("GET");
//            cc.setDoInput(true);
//            cc.connect();
//
//            int response = cc.getResponseCode();
//            Log.e(TAG, "ByGetMethod started in RecognizerAIActivity, response = " + response);
//            if (response == HttpURLConnection.HTTP_OK) {
//                DataInputStream = cc.getInputStream();
//                Log.e(TAG, "ByGetMethod, DataInputStream = " + DataInputStream);
//
//                int i;
//                StringBuffer buffer = new StringBuffer();
//                byte[] b = new byte[1024];
//                while( (i = DataInputStream.read(b)) != -1) {
//                    buffer.append(new String(b, 0, i));
//                }
//                str = buffer.toString();
//                Log.e(TAG, "ByGetMethod, str = " + str);
//                Handler mHandler = new Handler(Looper.getMainLooper());
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //carInfoDialog(str);
//                        mCarNumberEditView.setText(str);
//                    }
//                }, 0);
//                if(mHandler !=null) {mHandler.removeMessages(0);}
//            }
//            else {
//                Log.e(TAG, "ByGetMethod, response = " + response);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error in ByGetMethod, e = " + e);
//        }
//
//        Log.e(TAG, "ByGetMethod ended in RecognizerAIActivity");
//
//        return DataInputStream;
//    }
}
