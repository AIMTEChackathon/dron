package cz.aimtec.hackathon.drone.connectivity;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cz.aimtec.hackathon.drone.models.Position;
import cz.aimtec.hackathon.drone.models.SewioModel;
import cz.msebera.android.httpclient.Header;

/**
 * Created by klin on 10. 3. 2018.
 */

public class SewioConnector {
    private static final String API_URL = "http://192.168.90.54/sensmapserver/api";

    public abstract static class AsyncSewioResponseHandler extends AsyncHttpResponseHandler {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            try {
                String response = new String(responseBody);

                Type modelListType = new TypeToken<ArrayList<SewioModel>>(){}.getType();
                Gson gson = new Gson();
                List<SewioModel> modelList = gson.fromJson(response, modelListType);

                List<Position> positionList = modelList.parallelStream()
                        .filter(m -> m.getName().startsWith("A") && m.getName().length() == 3)
                        .map(m -> new Position(m))
                        .collect(Collectors.toList());

                onSuccess(statusCode, positionList, response);
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

    public void getModels(AsyncHttpResponseHandler responseHandler) {
        RestClient restClient = new RestClient(API_URL);

        RequestParams params = new RequestParams();
        restClient.get("/models", params, responseHandler);
    }
}
