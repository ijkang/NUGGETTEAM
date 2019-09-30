package com.example.carpic_3;

import android.Manifest;
import android.app.Activity;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

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

/*[촬영화면 Activity]*/
public class MainActivity extends AppCompatActivity {

    //이미지 전송관련
    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    //private static final int REQUEST_TAKE_ALBUM = 3333; //앨범에서 가져오는 부분(불필요)
    private static final int REQUEST_IMAGE_CROP = 4444;

    String mCurrentPhotoPath;
    Uri imageUri;
    Uri photoURI, albumURI;

    // btnCapture : 촬영버튼
    private Button btnCapture;
    private TextureView textureView;
    private static final String TAG = "MainActivity";

    // btnList : 조회버튼
    private Button btnList;

//    //각도이지않을까생각을했었는데
//    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
//    static{
//        ORIENTATIONS.append(Surface.ROTATION_0,90);
//        ORIENTATIONS.append(Surface.ROTATION_90,0);
//        ORIENTATIONS.append(Surface.ROTATION_180,270);
//        ORIENTATIONS.append(Surface.ROTATION_270,180);
//    }
//
//    private String cameraId;
//    private CameraDevice cameraDevice;
//    private CameraCaptureSession cameraCaptureSessions;
//    private CaptureRequest.Builder captureRequestBuilder;
//    private Size imageDimension;
//    private ImageReader imageReader;
//
//    //이미지파일 저장
//    private File file;
//    private static final int REQUEST_CAMERA_PERMISSION = 200;
//    private boolean mFlashSupported;
//    private Handler mBackgroundHandler;
//    private HandlerThread mBackgroundThread;
//
//    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public void onOpened(@NonNull CameraDevice camera) {
//            cameraDevice = camera;
//            //createCameraPreview();
//        }
//
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
//            cameraDevice.close();
//        }
//
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public void onError(@NonNull CameraDevice cameraDevice, int i) {
//            if(cameraDevice != null) {
//                cameraDevice.close();
//                cameraDevice = null;
//            }
//        }
//    }; //이미지파일 저장 끝

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate started in MainActivity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //textureView = (TextureView)findViewById(R.id.textureView);
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

            //[1.사진촬영]
            captureCamera();
            //takePicture(); 구카메라

            //[2.네트워크 연결] kanginji server
            Log.e(TAG, "MakeNetworkCall.execute in MainActivity");
            new MakeNetworkCall().execute("http://3.16.54.45:80/http.php?getcarnum=1", "Get");

            //[3.다이얼로그]
            //carInfoDialog("get car number");
            }
        }); //촬영버튼 이벤트 끝

        //[4.카메라권한확인]
        checkPermission();
    }


/* 나중에쓸지도모르는 customdialog
    //customDialog
    public void carInfoDialog() {
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);

        builderSingle.setTitle(title);
        builderSingle.setMessage(message);

        builderSingle.setNegativeButton(
                "재촬영",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "재촬영 완료", Toast.LENGTH_LONG).show();
                    }
            });

        builderSingle.setPositiveButton(
                "차량등록",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "차량등록 완료", Toast.LENGTH_LONG).show();
                    }
                    });

        builderSingle.setNeutralButton(
                "취소",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "취소완료", Toast.LENGTH_LONG).show();
                    }
                });


        builderSingle.show();

    }

//
*/


    //[3.다이얼로그]
    public void carInfoDialog(String carNumber) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

        dialog.setTitle("인식결과");
        dialog.setMessage(carNumber);
        // EditText 삽입하기
        final EditText et = new EditText(MainActivity.this);
        dialog.setView(et);

        dialog.setPositiveButton("차량등록", new DialogInterface.OnClickListener() {
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
                });

        dialog.setNegativeButton("재촬영", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "재촬영", Toast.LENGTH_LONG).show();
                    }

                });
        dialog.setNeutralButton("취소",null);
        dialog.show();

        Log.d(TAG,"재촬영");

    } //다이얼로그 끝


//    //[2. 사진촬영] 상세
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void takePicture() {
//        Log.d(TAG, "takePicture Start");
//
//        // 2-1. 이미지 저장
//        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
//        {
//            ActivityCompat.requestPermissions(this,new String[]{
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//            },REQUEST_CAMERA_PERMISSION);
//            return;
//        }
//        //
//        Log.d(TAG,"이미지 저장");
//
//        if(cameraDevice == null)
//            return;
//        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
//        try{
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
//            Size[] jpegSizes = null;
//            if(characteristics != null)
//                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
//                        .getOutputSizes(ImageFormat.JPEG);
//
//            //2-2. 사이즈에 맞게 이미지 촬영
//            int width = 640;
//            int height = 480;
//            if(jpegSizes != null && jpegSizes.length > 0)
//            {
//                width = jpegSizes[0].getWidth();
//                height = jpegSizes[0].getHeight();
//            }
//            final ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
//            List<Surface> outputSurface = new ArrayList<>(2);
//            outputSurface.add(reader.getSurface());
//            outputSurface.add(new Surface(textureView.getSurfaceTexture()));
//
//            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            captureBuilder.addTarget(reader.getSurface());
//            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//
//            //2.3 이미지 회전상태 확인
//            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));
//
//            //2.4 파일생성(경로)
//            file = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+UUID.randomUUID().toString()+".jpg");
//            Log.d(TAG,"내장메모리 DCIM 저장됨");
//
//            Log.d(TAG, String.valueOf(file));
//            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader imageReader) {
//                    Image image = null;
//                    try{
//                        image = reader.acquireLatestImage();
//                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                        byte[] bytes = new byte[buffer.capacity()];
//                        buffer.get(bytes);
//                        save(bytes);
//
//                        Log.d(TAG, "이미지 저장 완료");
//
//                    }
//                    catch (FileNotFoundException e)
//                    {
//                        e.printStackTrace();
//                        Log.d(TAG, String.valueOf(e));
//                    }
//                    catch (IOException e)
//                    {
//                        e.printStackTrace();
//                        Log.d(TAG, String.valueOf(e));
//                    }
//                    finally {
//                        {
//                            if(image != null)
//                                image.close();
//                        }
//                    }
//                }
//                private void save(byte[] bytes) throws IOException {
//                    OutputStream outputStream = null;
//                    try{
//                        outputStream = new FileOutputStream(file);
//                        outputStream.write(bytes);
//                    }finally {
//                        if(outputStream != null)
//                            outputStream.close();
//                    }
//                }
//            };
//
//            reader.setOnImageAvailableListener(readerListener,mBackgroundHandler);
//            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
//                @Override
//                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
//                    super.onCaptureCompleted(session, request, result);
//                    Toast.makeText(MainActivity.this, "Saved "+file, Toast.LENGTH_SHORT).show();
//                    createCameraPreview();
//                    Log.d(TAG, "toast comp");
//                }
//            };
//
//            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    try{
//                        cameraCaptureSession.capture(captureBuilder.build(),captureListener,mBackgroundHandler);
//                    } catch (CameraAccessException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//
//                }
//            },mBackgroundHandler);
//
//
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void createCameraPreview() {
//        try{
//            SurfaceTexture texture = textureView.getSurfaceTexture();
//            assert  texture != null;
//            texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
//            Surface surface = new Surface(texture);
//            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            captureRequestBuilder.addTarget(surface);
//            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    if(cameraDevice == null)
//                        return;
//                    cameraCaptureSessions = cameraCaptureSession;
//                    updatePreview();
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Toast.makeText(MainActivity.this, "Changed", Toast.LENGTH_SHORT).show();
//                }
//            },null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void updatePreview() {
//        if(cameraDevice == null)
//            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
//        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
//        try{
//            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void openCamera() {
//        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
//        try{
//            cameraId = manager.getCameraIdList()[0];
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            assert map != null;
//            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
//            //Check realtime permission if run higher API 23
//            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
//            {
//                ActivityCompat.requestPermissions(this,new String[]{
//                        Manifest.permission.CAMERA,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        Manifest.permission.READ_EXTERNAL_STORAGE
//                },REQUEST_CAMERA_PERMISSION);
//                return;
//            }
//            manager.openCamera(cameraId,stateCallback,null);
//
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
//            openCamera();
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//            return false;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//
//        }
//    };
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if(requestCode == REQUEST_CAMERA_PERMISSION)
//        {
//            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
//            {
//                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    protected void onResume() {
//        super.onResume();
//        startBackgroundThread();
//        if(textureView.isAvailable())
//            openCamera();
//        else
//            textureView.setSurfaceTextureListener(textureListener);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//    @Override
//    protected void onPause() {
//        stopBackgroundThread();
//        super.onPause();
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//    private void stopBackgroundThread() {
//        mBackgroundThread.quitSafely();
//        try{
//            mBackgroundThread.join();
//            mBackgroundThread= null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void startBackgroundThread() {
//        mBackgroundThread = new HandlerThread("Camera Background");
//        mBackgroundThread.start();
//        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
//    }


    //[1. 사진촬영] 상세
    //1-1 사진촬영
    private void captureCamera(){

    String state = Environment.getExternalStorageState();

    //1-2 외장 메모리 검사
    if (Environment.MEDIA_MOUNTED.equals(state)) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.d("촬영", "완료2" );

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = createImageFile();
                Log.d("촬영", "완료3" );
            } catch (IOException ex) {
                Log.e("captureCamera Error", ex.toString());
            }

            if (photoFile != null) {
                // getUriForFile의 두 번째 인자는 Manifest provier의 authorites와 일치해야 함
                Log.d("촬영", "완료3" );

                Uri providerURI = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                imageUri = providerURI;
                Log.d("촬영", "완료4`" );

                // 인텐트에 전달할 때는 FileProvier의 Return값인 content://로만!!
                // providerURI의 값에 카메라 데이터를 넣어 보냄
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    } else {
        Toast.makeText(this, "저장공간이 접근 불가능한 기기입니다", Toast.LENGTH_SHORT).show();
        Log.d("촬영", "완료4" );
        return;
    }
}

    //1-3 이미지 파일 생성
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


    //1-4 갤러리에 저장
    private void galleryAddPic(){
        Log.i("galleryAddPic", "Call");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        // 해당 경로에 있는 파일을 객체화(새로 파일을 만든다는 것으로 이해하면 안 됨)
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        Toast.makeText(this, "사진이 앨범에 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    //1-5 이미지 CROP
    public void cropImage(){
        Log.i("cropImage", "Call");
        Log.i("cropImage", "photoURI : " + photoURI + " / albumURI : " + albumURI);

        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        // 50x50픽셀미만은 편집할 수 없다는 문구 처리 + 갤러리, 포토 둘다 호환하는 방법
        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoURI, "image/*");
        cropIntent.putExtra("outputX", 200); // crop한 이미지의 x축 크기, 결과물의 크기
        cropIntent.putExtra("outputY", 200); // crop한 이미지의 y축 크기
        cropIntent.putExtra("aspectX", 1); // crop 박스의 x축 비율, 1&1이면 정사각형
        cropIntent.putExtra("aspectY", 1); // crop 박스의 y축 비율
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("output", albumURI); // 크랍된 이미지를 해당 경로에 저장
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Log.i("REQUEST_TAKE_PHOTO", "OK");
                        galleryAddPic();
                        //iv_view.setImageURI(imageUri); //이미지뷰 삭제(190924)
                        uploadFile(mCurrentPhotoPath);
                    } catch (Exception e) {
                        Log.e("REQUEST_TAKE_PHOTO", e.toString());
                    }
                } else {
                    Toast.makeText(MainActivity.this, "사진찍기를 취소하였습니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_IMAGE_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    galleryAddPic();
                    //iv_view.setImageURI(albumURI); //이미지뷰 삭제(190924)
                    uploadFile(mCurrentPhotoPath);
                }
                break;
        }
    }

    //1-6 권한확인
    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 처음 호출시엔 if()안의 부분은 false로 리턴 됨 -> else{..}의 요청으로 넘어감
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) ||
                    (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_CAMERA:
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {
                        Toast.makeText(MainActivity.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
        }
    }
    public void uploadFile(String filePath){
        String url = "http://3.16.54.45/imgupload.php";
        try {
            UploadFile uploadFile = new UploadFile(MainActivity.this);
            uploadFile.setPath(filePath);
            uploadFile.execute(url);
        } catch (Exception e){
            Log.e("UploadFile", String.valueOf(e));
        }
    }



    // 2019.09.17 add =>
    //[2. 네트워크 연결] 상세
    //2-1 GET입력
    String str = null;
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
            Log.e(TAG, "ByGetMethod started in MainActivity, response = " + response);
            if (response == HttpURLConnection.HTTP_OK) {
                DataInputStream = cc.getInputStream();
                Log.e(TAG, "ByGetMethod, DataInputStream = " + DataInputStream);

                int i;
                StringBuffer buffer = new StringBuffer();
                byte[] b = new byte[1024];
                while( (i = DataInputStream.read(b)) != -1) {
                    buffer.append(new String(b, 0, i));
                }
                str = buffer.toString();
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        carInfoDialog(str);
                    }
                }, 0);
                Log.e(TAG, "ByGetMethod, str = " + str);
            }
            else {
                Log.e(TAG, "ByGetMethod, response = " + response);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in ByGetMethod, e = " + e);
        }

        Log.e(TAG, "ByGetMethod ended in HttpExampleActivity");

        return DataInputStream;
    }

    //2-2 post입력
    InputStream ByPostMethod(String ServerURL) {

        Log.e(TAG, "ByPostMethod started in HttpExampleActivity");

        InputStream DataInputStream = null;
        try {
            //String PostParam = "first_name=dong&last_name=gam";
            String PostParam = "first_name=soyun&last_name=son";

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

    public void DisplayMessage(String a) {
        //TxtResult = (TextView) findViewById(R.id.response);
        //TxtResult.setText(a);
    }

    //2-4 AsyncTask
    private class MakeNetworkCall extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DisplayMessage("Please Wait ...");
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
                is = ByGetMethod(URL);
            } else {
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

            //DisplayMessage(result);
            Log.e(TAG, "onPostExecute, result: " + result);
        }
    } // 2019.09.17 add <=

}
