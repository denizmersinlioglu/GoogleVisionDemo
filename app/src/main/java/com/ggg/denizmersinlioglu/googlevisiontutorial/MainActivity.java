package com.ggg.denizmersinlioglu.googlevisiontutorial;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.github.kittinunf.fuel.util.Base64;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import kotlin.Pair;

public class MainActivity extends AppCompatActivity {

    public final static int MY_REQUEST_CODE = 1;



    public void takePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, MY_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if(requestCode == MY_REQUEST_CODE && resultCode == RESULT_OK) {

            // Convert image data to bitmap
            Bitmap picture = (Bitmap)data.getExtras().get("data");

            // Set the bitmap as the source of the ImageView
            ((ImageView)findViewById(R.id.previewImage))
                    .setImageBitmap(picture);

            // More code goes here
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            picture.compress(Bitmap.CompressFormat.JPEG, 90, byteStream);

            String base64Data = Base64.encodeToString(byteStream.toByteArray(),
                    Base64.URL_SAFE);

            String requestURL =
                    "https://vision.googleapis.com/v1/images:annotate?key=" +
                            getResources().getString(R.string.mykey);
            // Create an array containing
            // the LABEL_DETECTION feature
            JSONArray features = new JSONArray();
            JSONObject feature = new JSONObject();
            try {
                feature.put("type", "LABEL_DETECTION");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            features.put(feature);

            // Create an object containing
            // the Base64-encoded image data
            JSONObject imageContent = new JSONObject();
            try {
                imageContent.put("content", base64Data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Put the array and object into a single request
            // and then put the request into an array of requests
            JSONArray requests = new JSONArray();
            JSONObject request = new JSONObject();
            try {
                request.put("image", imageContent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                request.put("features", features);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            requests.put(request);
            JSONObject postData = new JSONObject();
            try {
                postData.put("requests", requests);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Convert the JSON into a
            // string
            String body = postData.toString();

            Fuel.post(requestURL)
                    .header(
                            new Pair<String, Object>("content-length", body.length()),
                            new Pair<String, Object>("content-type", "application/json")
                    )
                    .body(body.getBytes())
                    .responseString(new Handler<String>() {
                        @Override
                        public void success(@NotNull Request request,
                                            @NotNull Response response,
                                            String data) {
                            // More code goes here
                            // Access the labelAnnotations arrays
                            JSONArray labels = null;
                            try {
                                labels = new JSONObject(data)
                                        .getJSONArray("responses")
                                        .getJSONObject(0)
                                        .getJSONArray("labelAnnotations");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            String results = "";

                            // Loop through the array and extract the
                            // description key for each item
                            for(int i=0;i<labels.length();i++) {
                                try {
                                    results = results +
                                            labels.getJSONObject(i).getString("description") +
                                            "\n";
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            // Display the annotations inside the TextView
                            ((TextView)findViewById(R.id.resultsText)).setText(results);
                        }

                        @Override
                        public void failure(@NotNull Request request,
                                            @NotNull Response response,
                                            @NotNull FuelError fuelError) {}
                    });
        }
    }
}
