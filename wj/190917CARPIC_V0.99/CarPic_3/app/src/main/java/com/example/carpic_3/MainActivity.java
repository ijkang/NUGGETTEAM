package com.example.carpic_3;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

//촬영화면 Activity
public class MainActivity extends AppCompatActivity {

    // btnCapture : 촬영버튼
    private Button btnCapture;
    private TextureView textureView;
    private static final String TAG = "MainActivity";

    // btnList : 조회버튼
    private Button btnList;

    //뭐지?
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;

    //이미지파일 저장
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice=null;
        }
    }; //이미지파일 저장 끝




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = (TextureView)findViewById(R.id.textureView);
        btnList = (Button)findViewById(R.id.btnList);

        // 조회버튼 이벤트 : 클릭 시 조회화면으로 이동
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                MainActivity.this.startActivity(registerIntent);
            }
        }); //조회버튼 이벤트 끝


        // 촬영버튼 이벤트
        btnCapture = (Button)findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {

                //[1.네트워크 연결]
                new MakeNetworkCall().execute("http://3.15.39.106/http.php?get=1", "Get");

                //[2.사진촬영]
                takePicture();

                //[3.ASYNCTASK]
            }
        }); //촬영버튼 이벤트 끝

    }

    //[1. 네트워크 연결 상세
    //1-1 GET입력
    InputStream ByGetMethod(String ServerURL) {

        Log.e(TAG, "ByGetMethod started in MainActivity");

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

            if (response == HttpURLConnection.HTTP_OK) {
                DataInputStream = cc.getInputStream();
                Log.e(TAG, "ByGetMethod, DataInputStream = " + DataInputStream);
            }
            else {
                Log.e(TAG, "ByGetMethod, response = " + response);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in ByGetMethod", e);
        }

        Log.e(TAG, "ByGetMethod ended in MainActivity");

        return DataInputStream;
    }

    //1-2 post입력
    InputStream ByPostMethod(String ServerURL) {

        Log.e(TAG, "ByPostMethod started in MainActivity");

        InputStream DataInputStream = null;
        try {
            String PostParam = "first_name=dong&last_name=gam";

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

        Log.e(TAG, "ByPostMethod ended in MainActivity");

        return DataInputStream;
    }

    //1-3 string변환
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

//    //1-4 메세지 출력 (190910현재 불필요_삭제금지)
//    TextView TxtResult;
//    public void DisplayMessage(String response) {
//        TxtResult = (TextView) findViewById(R.id.response);
//        TxtResult.setText(response);
//    }

    //1-5 AsyncTask
    private class MakeNetworkCall extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //DisplayMessage("Please Wait ...");
        }

        @Override
        protected String doInBackground(String... arg) {
            InputStream is = null;
            String URL = arg[0];
            Log.e(TAG, "doInBackground started in MainActivity, URL: " + URL);
            String res = "";

            Log.e(TAG, "doInBackground, arg[1] = " + arg[1]);
            if (arg[1].equals("Get")) {
                is = ByGetMethod(URL);
            }
            else if (arg[1].equals("Post")) {
                is = ByPostMethod(URL);
            }
            else {
                Log.e(TAG, "doInBackground, do nothing, arg[1] = " + arg[1]);
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
            String dialres = result;
            carInfoDialog(dialres);
            //DisplayMessage(result);
            Log.e(TAG, "onPostExecute, result: " + result);
        }
    }

//customDialog (190910현재 불필요)

//    public void carInfoDialog() {
//        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
//
//        builderSingle.setTitle(title);
//        builderSingle.setMessage(message);
//
//        builderSingle.setNegativeButton(
//                "재촬영",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(getApplicationContext(), "재촬영 완료", Toast.LENGTH_LONG).show();
//                    }
//            });
//
//        builderSingle.setPositiveButton(
//                "차량등록",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Toast.makeText(getApplicationContext(), "차량등록 완료", Toast.LENGTH_LONG).show();
//                    }
//                    });
//
//        builderSingle.setNeutralButton(
//                "취소",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Toast.makeText(getApplicationContext(), "취소완료", Toast.LENGTH_LONG).show();
//                    }
//                });
//
//
//        builderSingle.show();
//
//    } //customDialog 끝

//

    //다이얼로그
    private void carInfoDialog(String result) {

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("인식 결과")
                .setMessage(result)
                .setPositiveButton("차량등록", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // 촬영버튼 클릭시 toast
                        Toast.makeText(getApplicationContext(), "차량등록 완료", Toast.LENGTH_LONG).show();

                        Log.d(TAG,"촬영버튼 토스트");

                        // 촬영버튼 클릭시 조회화면으로 이동
                        Intent registerIntent = new Intent(MainActivity.this,RegisterActivity.class);
                        MainActivity.this.startActivity(registerIntent);

                        Log.d(TAG,"촬영버튼 조회화면 이동");
                    }
                })

                .setNegativeButton("재촬영", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "재촬영", Toast.LENGTH_LONG).show();
                    }

                })
                .setNeutralButton("취소",null)
                .show();
        Log.d(TAG,"재촬영");
    } //다이얼로그 끝


    //[2. 사진촬영] 상세
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void takePicture() {
        Log.d(TAG, "takePicture Start");

        // 2-1. 이미지 저장 (190910_정확한 경로 설정 필요)
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },REQUEST_CAMERA_PERMISSION);
            return;
        } //
        Log.d(TAG,"이미지 저장");

        if(cameraDevice == null)
            return;
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if(characteristics != null)
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);

            //2-2. 사이즈에 맞게 이미지 촬영
            int width = 640;
            int height = 480;
            if(jpegSizes != null && jpegSizes.length > 0)
            {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            final ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //2.3 이미지 회전상태 확인
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));

            //2.4 파일생성
            file = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+UUID.randomUUID().toString()+".jpg");
            Log.d(TAG,"내장메모리 DCIM 저장됨");

            Log.d(TAG, String.valueOf(file));
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    try{
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);

                        Log.d(TAG, "이미지 저장 완료");

                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                        Log.d(TAG, String.valueOf(e));
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Log.d(TAG, String.valueOf(e));
                    }
                    finally {
                        {
                            if(image != null)
                                image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream outputStream = null;
                    try{
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    }finally {
                        if(outputStream != null)
                            outputStream.close();
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener,mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(MainActivity.this, "Saved "+file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                    Log.d(TAG, "toast comp");
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try{
                        cameraCaptureSession.capture(captureBuilder.build(),captureListener,mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            },mBackgroundHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreview() {
        try{
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert  texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updatePreview() {
        if(cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera() {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            //Check realtime permission if run higher API 23
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId,stateCallback,null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    //촬영화면 view
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread= null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    } //촬영화면 view 끝


}
