package com.example.feriproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    Button button_login, button_skip;
    EditText input_name, input_password;
    OkHttpClient client = new OkHttpClient();

    // help
    // https://www.youtube.com/watch?v=cjNW2noShM0
    // https://futurestud.io/tutorials/how-to-run-an-android-app-against-a-localhost-api
    //
    // design help: https://sourcey.com/articles/beautiful-android-login-and-signup-screens-with-material-design
    // get ip win : ipconfig
    // connect to with phone to localhost : 192.168.43.194:8000
    // connect to emulator to localhost : 10.0.3.2:8000

    // connect to my rest api
    // 1. https://johan.driessen.se/posts/Accessing-an-IIS-Express-site-from-a-remote-computer/
    // 2. https://stackoverflow.com/questions/9794985/config-error-this-configuration-section-cannot-be-used-at-this-path/12867753#12867753
    // computer: http://localhost:51333/api/Auth/RequestToken
    // emulator(Genymotion): http://10.0.3.2:51333/api/Auth/RequestToken
    // phone to localhost: http://<IP of computer>:51333/api/Auth/RequestToken

    public static final String BASE_URL = "http://10.0.3.2:51333";
    public static final String ADD_TOKEN = "/api/Auth/RequestToken";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

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
    }

    private void skip() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    private void login() {
        try
        {
            String userName = input_name.getText().toString(), passWord = input_password.getText().toString();

            if(userName == "" || passWord == "") {
                Toast.makeText(this, "Incorrect input!", Toast.LENGTH_SHORT).show();
                return;
            }

            Gson gson = new Gson();
            String json = gson.toJson(new User(userName, passWord));
            String response = doPostRequest(BASE_URL+ADD_TOKEN, json);

            Log.d(MyApplication.TAG, "login(" + BASE_URL + ADD_TOKEN + "): " + response);
            Toast.makeText(this, "response: " + response, Toast.LENGTH_SHORT).show();

            //Bitmap bitmap = BitmapFactory.decodeStream(doGetRequesetImage(BASE_URL+"/image"));
            //response = doGetRequest(BASE_URL+"/api/news", response);
            //Log.d(MyApplication.TAG, "login(" + BASE_URL + "/api/news): " + response);
        }
        catch (Exception e)
        {
            Log.d(MyApplication.TAG, "login(" + BASE_URL + ADD_TOKEN + "): " + e.getMessage() + " | ");
            e.printStackTrace();
        }
    }

    String doPostRequest(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    String doGetRequest(String url, String coockie) throws IOException {
        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + coockie)
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    InputStream doGetRequesetImage(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().byteStream();

        // Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
    }



    /*public String request(String prefix, String json) throws IOException {

        //RequestBody body = RequestBody.create(JSON, json);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", "someValue")
                .addFormDataPart("password", "someValue")
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + prefix)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    } */
}
