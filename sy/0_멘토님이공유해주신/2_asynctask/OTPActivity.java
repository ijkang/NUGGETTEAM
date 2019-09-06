package com.example.joongwon.m_a;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/* OTP 권한*/

public class OTPActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<PermissionListItem> items = new ArrayList<>();


    private static final String TAG = "OTPActivity";
    private String TAG_JSON = "webnautes", mJsonString;

    ArrayList<String> dbid = new ArrayList<>();
    ArrayList<String> dbname = new ArrayList<>();
    ArrayList<String> dbbirthday = new ArrayList<>();
    ArrayList<String> dbotp_type = new ArrayList<>();

    OTPAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        findViewById();
        adapter = new OTPAdapter(); // adapter 초기화
        OTPList otpList = new OTPList();
        otpList.execute("12");

        Log.e(TAG, "onCreate in OTPActivity");
    }


    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final int index = (int) parent.getItemIdAtPosition(position);
            if (dbotp_type.get(index).equals("0")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OTPActivity.this);
                builder.setTitle("OTP 권한 승인");
                builder.setMessage(dbname.get(index) + " 님의 OTP 권한을 생성하시겠습니까?");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        OTP otp = new OTP();
                        otp.execute(dbid.get(index), "1");
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } /*else {
                AlertDialog.Builder builder = new AlertDialog.Builder(OTPActivity.this);
                builder.setTitle("OTP 권한 취소");
                builder.setMessage(dbname.get(index) + " 님의 OTP 권한을 취소하시겠습니까?");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        OTP otp = new OTP();
                        otp.execute(dbid.get(index), "0");
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }*/
        }
    };

    private void findViewById() {
        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(itemClickListener);
    }

    class OTPAdapter extends BaseAdapter {
        // BaseAdapter -> Alt+Enter -> implement methods
        // 모든 필요한 method 가 아래와 같이 나타남
        @Override
        public int getCount() {
            // 현재 Guest 의 수를 반환
            return items.size();
        }

        public void addItem(PermissionListItem item) {
            // PermissionListItem 추가
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            // 특정한 Guest 를 반환할 수 있도록 함
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            // 그대로 position 반환하면 됨
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Guest 에 대한 view 를 보여줌
            PermissionListItemView view = new PermissionListItemView(getApplicationContext());
            PermissionListItem item = items.get(position);

            Log.e(TAG, "id = " + item.getId() + ", " + "name = " + item.getName() + ", " + "birthdaty = " + item.getBirthday() + ", " + "permission = " + item.getPermission());
            // item 에 있는 Guest 의 Id, Name, Birthday, Permission 가져옴
            view.setText1(item.getId());
            view.setText2(item.getName());
            view.setText3(item.getBirthday());
            view.setText4(item.getPermission());

            // 해당 view 반환
            return view;
        }
    }

    class OTPList extends AsyncTask<String, Void, String> {

        // AsyncTask -> 간단하게 새로운 스레드를 생성해서 작업할 수 있음

        //   ㅁ(메인 스레드) 일을 하다가
        //    |
        //    v  ㅡ> ㅁ(일하는 스레드) 사용자가 다른일을 해야 하는 경우
        //    |      |
        //    v      v (doInBackground 시점)
        // (서로 자기 일을 함) -> 총 일이 2개
        //    |       |
        //    v       v (doInBackground 시점) -> 일이 다 끝나서 return 을 해주면
        //   ㅁ <ㅡㅡㅡ
        //   위 시점에서 다시 만나는 순간이 옴 -> onPostExecute

        String message = "";
        boolean ok = false, error = false;

        @Override
        protected void onPreExecute() {
            // 메인 스레드에서 실행
            super.onPreExecute();
            items.clear();
            dbid.clear();
            dbotp_type.clear();
            dbbirthday.clear();
            dbname.clear();
        }

        @Override
        protected void onPostExecute(String s) {
            // 메인 스레드에서 실행
            super.onPostExecute(s);
            if (ok == true) {
                for (int i = 0; i < dbid.size(); i++) {
                    if (dbotp_type.get(i).equals("1")) {
                        adapter.addItem(new PermissionListItem(dbid.get(i), dbname.get(i), dbbirthday.get(i), "승인"));
                        listView.setAdapter(adapter);
                    } else {
                        adapter.addItem(new PermissionListItem(dbid.get(i), dbname.get(i), dbbirthday.get(i), "미승인"));
                        listView.setAdapter(adapter);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            if (error == true) {
                Toast toast1 = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
                toast1.setGravity(Gravity.CENTER, 0, 0);
                toast1.show();
                finish();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            // 새로 만든 스레드에 있는 영역
            String postParameters = "name=" + strings[0];
            try { // 서버 연결
                URL url = new URL("https://yoosongmi95.cafe24.com/otp_request.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(3000);
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                // 안드로이드 -> 서버 파라미터 값 전달
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                // 서버 -> 안드로이드 파라미터 값 전달
                InputStream is;
                BufferedReader in;
                String data;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line ;
                StringBuffer buffer = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                data = buffer.toString().trim();
                // Log.e("d", data);
                mJsonString = data;
                showResult();

                ok = true;
            } catch (Exception e) {
                e.printStackTrace();
                error = true;
                message = "네트워크 연결 실패\n3G/4G WIFI 연결을 확인해주세요.";
                return null;
            }
            return null;
        }

        private void showResult() { // JSON 으로 만든 리스트형태의 DB 를 가져옴
            try {
                JSONObject jsonObject = new JSONObject(mJsonString);
                JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    dbid.add(item.getString("userID"));
                    dbname.add(item.getString("userName"));
                    dbbirthday.add(item.getString("userBirthday"));
                    dbotp_type.add(item.getString("otp_permission"));
                }
                //Log.e("완료되고", "0");
            } catch (JSONException e) {
            }
        }
    }

    class OTP extends AsyncTask<String, Void, String> {

        // AsyncTask -> 간단하게 새로운 스레드를 생성해서 작업할 수 있음

        //   ㅁ(메인 스레드) 일을 하다가
        //    |
        //    v  ㅡ> ㅁ(일하는 스레드) 사용자가 다른일을 해야 하는 경우
        //    |      |
        //    v      v (doInBackground 시점)
        // (서로 자기 일을 함) -> 총 일이 2개
        //    |       |
        //    v       v (doInBackground 시점) -> 일이 다 끝나서 return 을 해주면
        //   ㅁ <ㅡㅡㅡ
        //   위 시점에서 다시 만나는 순간이 옴 -> onPostExecute

        String message = "", permission = "";
        boolean ok = false, error = false;

        @Override
        protected void onPreExecute() {
            // 메인 스레드에서 실행
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            // 메인 스레드에서 실행
            super.onPostExecute(s);
            if (ok == true) {
                if (permission.equals("1")) {
                    Toast toast1 = Toast.makeText(getApplicationContext(), "OTP 권한 부여함", Toast.LENGTH_SHORT);
                    toast1.setGravity(Gravity.CENTER, 0, 0);
                    toast1.show();

                    // OTP 권한이 생성되면 OTP 권한 부여함 이라는 토스트 메시지를 띄우고
                    // OTPList 를 새걸로 바꿈 -> 즉 없애버림 (보안유지 차원으로)
                    OTPList otpList = new OTPList();
                    otpList.execute("12");

                } /*else {
                    Toast toast1 = Toast.makeText(getApplicationContext(), "OTP 권한 삭제함", Toast.LENGTH_SHORT);
                    toast1.setGravity(Gravity.CENTER, 0, 0);
                    toast1.show();
                    OTPList otpList = new OTPList();
                    otpList.execute("12");
                }*/

            }
            if (error == true) {
                Toast toast1 = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
                toast1.setGravity(Gravity.CENTER, 0, 0);
                toast1.show();
                finish();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            // 새로 만든 스레드에 있는 영역
            permission = strings[1];
            String postParameters = "id=" + strings[0] + "&permission=" + strings[1];

            try { // 서버 연결
                URL url = new URL("https://yoosongmi95.cafe24.com/otp_permission.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(3000);
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                // 안드로이드 -> 서버 파라미터 값 전달
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                // 서버 -> 안드로이드 파라미터 값 전달
                InputStream is;
                BufferedReader in;
                // String data = "";

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buffer = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                // data = buffer.toString().trim();
//                Log.e("da", data);

                ok = true;
            } catch (Exception e) {
                error = true;
                message = "네트워크 연결 실패\n3G/4G WIFI 연결을 확인해주세요.";
                return null;
            }
            return null;
        }
    }
}