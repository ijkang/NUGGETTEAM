package com.example.carpic_3;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*[이미지전송부 Activity]*/
public class UploadFile extends AsyncTask<String, String, String> {

    Context context;                // 생성자 호출 시
    ProgressDialog mProgressDialog; // 진행 상태 다이얼로그
    String fileName;                // 파일 위치

    HttpURLConnection conn = null;  // 네트워크 연결 객체
    DataOutputStream dos = null;    // 서버 전송 시 데이터 작성한 뒤 전송

    String lineEnd = "\r\n";        // 구분자
    String twoHyphens = "--";
    String boundary = "*****";

    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1024;
    File sourceFile;
    int serverResponseCode;
    String TAG = "FileUpload";

    public UploadFile(Context context) {
        this.context = context;
    }

    public void setPath(String uploadFilePath){
        this.fileName = uploadFilePath;
        this.sourceFile = new File(uploadFilePath);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.e(TAG, "onPreExecute");
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle("Loading...");
        mProgressDialog.setMessage("Image uploading...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        if (!sourceFile.isFile()) {     // 해당 위치의 파일이 있는지 검사
            Log.e(TAG, "sourceFile(" + fileName + ") is Not A File");
            return null;
        } else {
            String success = "Success";
            Log.i(TAG, "sourceFile(" + fileName + ") is A File");

            // 이미지 리사이징
            Bitmap srcBmp = BitmapFactory.decodeFile(fileName);
            int iWidth   = 520;         // 축소시킬 너비
            int iHeight  = 520;         // 축소시킬 높이
            float fWidth  = srcBmp.getWidth();
            float fHeight = srcBmp.getHeight();

            // 원하는 넓이보다 클 경우의 설정
            if(fWidth > iWidth) {
                float mWidth = (float) (fWidth / 100);
                float fScale = (float) (iWidth / mWidth);
                fWidth *= (fScale / 100);
                fHeight *= (fScale / 100);

            // 원하는 높이보다 클 경우의 설정
            }else if (fHeight > iHeight) {
                float mHeight = (float) (fHeight / 100);
                float fScale = (float) (iHeight / mHeight);
                fWidth *= (fScale / 100);
                fHeight *= (fScale / 100);
            }
            FileOutputStream fosObj = null;

            try {
                // 리사이징된 이미지 덮어쓰기(동일 파일명 사용)
                Bitmap resizedBmp = Bitmap.createScaledBitmap(srcBmp, (int)fWidth, (int)fHeight, true);
                fosObj = new FileOutputStream(fileName);
                resizedBmp.compress(Bitmap.CompressFormat.JPEG, 100, fosObj);
                fosObj.flush();
                fosObj.close();
            } catch (Exception e){
                ;
            } // 덮어쓰기 끝

            try {
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(strings[0]);
                Log.i("strings[0]", strings[0]);


                // Open a HTTP connection to the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);          // Allow Inputs
                conn.setDoOutput(true);         // Allow Outputs
                conn.setUseCaches(false);       // Don't use a Cached Copy
                conn.setRequestMethod("POST");  // 전송 방식
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary); // boundary 기준으로 인자를 구분함
                conn.setRequestProperty("uploaded_file", fileName);
                Log.i(TAG, "fileName: " + fileName);

                // dataoutput은 outputstream이란 클래스를 가져오며,
                // outputStream는 FileOutputStream의 하위 클래스이다.
                // output은 쓰기, input은 읽기, 데이터를 전송할 때 전송할 내용을 적는 것으로 이해할 것
                dos = new DataOutputStream(conn.getOutputStream());

                // 사용자 이름으로 폴더를 생성하기 위해 사용자 이름을 서버로 전송한다.
                // 하나의 인자 전달 data1 = newImage
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"data1\"" + lineEnd); // name으 \ \ 안 인자가 php의 key
                dos.writeBytes(lineEnd);
                dos.writeBytes("newImage"); // newImage라는 값을 넘김
                dos.writeBytes(lineEnd);


                // 이미지 전송, 데이터 전달
                // uploadded_file라는 php key값에 저장되는 내용은 fileName
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...,
                // 마지막에 two~~ lineEnd로 마무리 (인자 나열이 끝났음을 알림)
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i(TAG, "[UploadImageToServer] HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {

                }


                // 결과 확인
                BufferedReader rd = null;

                rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line = null;
                while ((line = rd.readLine()) != null) {
                    Log.i("Upload State", line);
                }

                //close the streams
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (Exception e) {
                Log.e(TAG + " Error", e.toString());
            }
            mProgressDialog.dismiss();
            return success;
        }
    }
}