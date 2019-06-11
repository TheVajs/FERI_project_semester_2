package com.example.feriproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.gson.Gson;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    Button button_login, button_skip;
    EditText input_name, input_password, input_link;
    Handler mHandler;

    // help
    // https://www.youtube.com/watch?v=cjNW2noShM0
    // https://futurestud.io/tutorials/how-to-run-an-android-app-against-a-localhost-api
    //
    // design help: https://sourcey.com/articles/beautiful-android-login-and-signup-screens-with-material-design
    // get ip win : ipconfig
    // connect to with phone to localhost : 192.168.43.194:8000
    // connect to emulator to localhost : 10.0.3.2:8000

    // connect to my rest api
    // maybe not needed?
    // 1. https://johan.driessen.se/posts/Accessing-an-IIS-Express-site-from-a-remote-computer/
    // 2. https://stackoverflow.com/questions/9794985/config-error-this-configuration-section-cannot-be-used-at-this-path/12867753#12867753
    //
    // computer: http://localhost:51333/api/Auth/RequestToken
    // emulator(Genymotion): http://10.0.3.2:51333/api/Auth/RequestToken
    // phone to localhost: http://<IP of computer>:51333/api/Auth/RequestToken

    final class User {
        public String username;
        public String password;

        public User(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // NEED TO ADD ? = yes
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        input_name = findViewById(R.id.input_email);
        input_password = findViewById(R.id.input_password);
        input_link = findViewById(R.id.input_link);
        button_login = findViewById(R.id.btn_login);
        button_skip = findViewById(R.id.btn_skip);

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        button_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skip();
            }
        });

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Toast.makeText(getBaseContext(), message.obj + " :)", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void skip() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    private void login() {
        try
        {
            String userName = input_name.getText().toString(), passWord = input_password.getText().toString(), link = input_link.getText().toString();

            if(userName.equals("") || passWord.equals("")) {
                throw new IllegalStateException();
            }

            if (!link.equals("")) MyHttp.SetBaseURL(link);

            Gson gson = new Gson();
            String json = gson.toJson(new User(userName, passWord));
            String response = MyHttp.doPostRequest(MyHttp._GETTOKEN, json);

            Log.d(MyApplication.TAG, "LOGIN : " + response);
            if(response.length() < 40 || response.contains("Could not verify")) throw new IllegalStateException();

            MyHttp.TOKEN = response.substring(10, response.length()-2);

            Log.d(MyApplication.TAG, "login(" + MyHttp._GETTOKEN + "): " + MyHttp.TOKEN);
            Toast.makeText(this,  "Logged in!", Toast.LENGTH_SHORT).show();

            takePicture();

            //Bitmap bitmap = BitmapFactory.decodeStream(doGetRequesetImage(BASE_URL+"/image"));
            //response = doGetRequest(BASE_URL+"/api/news", response);
            //Log.d(MyApplication.TAG, "login(" + BASE_URL + "/api/news): " + response);
        }
        catch (Exception e)
        {
            Log.d(MyApplication.TAG, "login(" + MyHttp._GETTOKEN + "): " + e.getMessage() + " | ");
            Toast.makeText(this, "Invalid input!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, MyApplication.EVENT_CODE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case MyApplication.EVENT_CODE_PICTURE:
                if (resultCode == RESULT_OK) {

                    /* SEND IMAGE TO SERVER */
                    Runnable runable = new Runnable(){
                        public void run(){
                            Log.d(MyApplication.TAG, "THREAD RUNNING!");
                            try
                            {
                                /* CONVERT IMAGE TO STRING */
                                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                                if(bitmap==null) {
                                    Log.d(MyApplication.TAG, "onActivityResult: bitmap null");
                                    return;
                                }
                                Log.d(MyApplication.TAG, "onActivityResult: bitmap dimensions: " + bitmap.getHeight() + "x" + bitmap.getWidth());
                                String encodedImage = MyHttp.bitmapToImage(bitmap);

                                /* CHECK IMAGE */
                                Gson gson = new Gson();
                                String json = gson.toJson(new ImageMessage("newSimon.jpg", encodedImage));
                                String request = MyHttp.doPostRequest(MyHttp._CHECKIMAGE, json, MyHttp.TOKEN);

                                String results = "", data = "";

                                if(request.contains("_")) {
                                    String split[] = request.split("_");
                                    results = split[0] + "\n OK!";
                                    data = split[1];
                                    Log.d(MyApplication.TAG, "sendImage(" + MyHttp._CHECKIMAGE + ") contains(|): " + data);
                                    Gson g = new Gson();
                                    List<Event> eventList = g.fromJson(data, MyApplication.eventType);
                                    for (int i = 0; i < eventList.size(); i++) {
                                        long time = eventList.get(i).getTimeInMillis();
                                        Object o = eventList.get(i).getData();
                                        Event nEvent = new Event(Color.MAGENTA, time, o);
                                        eventList.set(i, nEvent);
                                    }
                                    // save new DATA
                                    MyData myData = ((MyApplication)getApplication()).getData();
                                    for(Event event: eventList) myData.addEvent(event);
                                    ((MyApplication)getApplication()).saveMain();

                                    Log.d(MyApplication.TAG, "SERVER RESULT: " + data + "\n" + eventList.size());
                                } else {
                                    results = request +"\n NOPE!";
                                }
                                Message message = mHandler.obtainMessage(0,results);
                                message.sendToTarget();
                            }
                            catch (Exception e)
                            {
                                mHandler.obtainMessage(0,"Can't send image").sendToTarget();
                                Log.d(MyApplication.TAG, "sendImage(" + MyHttp._CHECKIMAGE + "): " + e.getMessage() + " | ");
                                //Toast.makeText(getBaseContext(), "Can't sent image", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    };
                    new Thread(runable).start();
                }
                break;
        }
    }
}
