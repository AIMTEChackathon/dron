package cz.aimtec.hackathon.drone.connectivity;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.aimtec.hackathon.drone.models.Package;
import cz.aimtec.hackathon.drone.models.Point3D;
import cz.aimtec.hackathon.drone.models.VoiceCommand;
import cz.msebera.android.httpclient.Header;

/**
 * Created by klin on 10. 3. 2018.
 */

public class DBConnector {

    private static final String API_URL = "https://g3d9b0efef.execute-api.eu-west-1.amazonaws.com/production";

    public abstract static class AsyncDBResponseHandler extends AsyncHttpResponseHandler {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            try {
                String response = new String(responseBody);

                Gson gson = new Gson();
                VoiceCommand voiceCommand = gson.fromJson(response, VoiceCommand.class);

                onSuccess(statusCode, voiceCommand, response);
            } catch (Exception e) {
                Log.e("REST", "onSuccess: ", e);
            }
        }

        public abstract void onSuccess(int statusCode, Object parsedJsonObject, String responseText);

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            try {
                String response = new String(responseBody);
                Log.d("REST", "response" + response);
            } catch (Exception e) {
                Log.e("REST", "onSuccess: ", e);
            }
        }
    }

    public void getVoiceCommands(AsyncHttpResponseHandler responseHandler) {
        RestClient restClient = new RestClient(API_URL);

        RequestParams params = new RequestParams();
        restClient.get("/command", params, responseHandler);
    }

    public void postPackage(Context context, Package pPackage, AsyncHttpResponseHandler responseHandler) {
        RestClient restClient = new RestClient(API_URL);

        Gson gson = new Gson();
        String content = gson.toJson(pPackage);

        restClient.post(context,"/package", content, responseHandler);
    }

    public void deleteAllPackages(AsyncHttpResponseHandler responseHandler) {
        RestClient restClient = new RestClient(API_URL);

        RequestParams params = new RequestParams();
        restClient.delete("/package", params, responseHandler);
    }

    public void postPosition(Context context, Point3D point3D, AsyncHttpResponseHandler responseHandler){
        RestClient restClient = new RestClient(API_URL);

        Gson gson = new Gson();
        String content = gson.toJson(point3D);

        restClient.post(context,"/position", content, responseHandler);
    }
}
