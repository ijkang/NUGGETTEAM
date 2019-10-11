package jy.demo.tesseract.android;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class OpenActivity extends AppCompatActivity {

    private Button btn_camera;
    private EditText mEditOcrResult;
    private String datapath = "";
    private String lang = "";

    private int ACTIVITY_REQUEST_CODE = 1;

    static TessBaseAPI sTess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);


        // 뷰 선언
        btn_camera = (Button) findViewById(R.id.btn_camera);
        mEditOcrResult = (EditText) findViewById(R.id.edit_ocrresult);
        sTess = new TessBaseAPI();


        // Tesseract 인식 언어를 한국어로 설정 및 초기화
        lang = "kor";
        datapath = getFilesDir()+ "/tesseract";

        if(checkFile(new File(datapath+"/tessdata")))
        {
            sTess.init(datapath, lang);
        }

        btn_camera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // 버튼 클릭 시

                // Camera 화면 띄우기
                Intent mIttCamera = new Intent(OpenActivity.this, CameraSurfaceView.class);
                startActivityForResult(mIttCamera, ACTIVITY_REQUEST_CODE);

            }
        });
    }



    boolean checkFile(File dir)
    {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if(!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if(dir.exists()) {
            String datafilepath = datapath + "/tessdata/" + lang + ".traineddata";
            File datafile = new File(datafilepath);
            if(!datafile.exists()) {
                copyFiles();
            }
        }
        return true;
    }

    void copyFiles()
    {
        AssetManager assetMgr = this.getAssets();

        InputStream is = null;
        OutputStream os = null;

        try {
            is = assetMgr.open("tessdata/"+lang+".traineddata");

            String destFile = datapath + "/tessdata/" + lang + ".traineddata";

            os = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            is.close();
            os.flush();
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            if(requestCode== ACTIVITY_REQUEST_CODE)
            {
                // 받아온 OCR 결과 출력
                mEditOcrResult.setText(data.getStringExtra("STRING_OCR_RESULT"));
            }
        }
    }
}