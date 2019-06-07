package com.example.feriproject;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyHttp {

    private static String BASE_URL = "http://192.168.1.12:51333";
    private static final String ADD_TOKEN = "/api/Auth/RequestToken";
    private static final String ADD_IMAGE = "/api/values/uploadImage";
    private static final String CHE_IMAGE = "/api/values/checkImage";

    public static String _GETTOKEN = BASE_URL+ADD_TOKEN;
    public static String _UPLOADIMAGE = BASE_URL+ADD_IMAGE;
    public static String _CHECKIMAGE = BASE_URL+CHE_IMAGE;


    public static String TOKEN = "";


    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    final MediaType JPG = MediaType.parse("image/jpg");
    OkHttpClient client;

    public MyHttp() {
        client = new OkHttpClient();
    }

    public static String doPostRequest(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        return response.body().string();
    }

    public static String doPostRequest(String url, String content, String token) throws IOException {

        RequestBody body = RequestBody.create(JSON, content);
        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + token)
                .url(url)
                .post(body)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        return response.body().string();
    }

    public static String bitmapToImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public static void SetBaseURL(String newBase) {
        BASE_URL = newBase;
        _GETTOKEN = BASE_URL+ADD_TOKEN;
        _UPLOADIMAGE = BASE_URL+ADD_IMAGE;
        _CHECKIMAGE = BASE_URL+CHE_IMAGE;
    }

    /*
    String doGetRequest(String url, String token) throws IOException {
        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + token)
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    } */
}
