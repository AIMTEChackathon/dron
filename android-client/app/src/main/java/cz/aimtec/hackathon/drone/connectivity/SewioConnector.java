package cz.aimtec.hackathon.drone.connectivity;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cz.aimtec.hackathon.drone.models.SevioModel;
import cz.msebera.android.httpclient.Header;

/**
 * Created by klin on 10. 3. 2018.
 */

public class SewioConnector {

    public abstract static class AsyncSewioResponseHandler extends AsyncHttpResponseHandler {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            try {
                String response = new String(responseBody);

                Type modelListType = new TypeToken<ArrayList<SevioModel>>(){}.getType();
                Gson gson = new Gson();
                List<SevioModel> modelList = gson.fromJson(response, modelListType);

                onSuccess(statusCode, modelList, response);
            } catch (Exception e) {
                Log.e("REST", "onSuccess: ", e);
            }
        }

        public abstract void onSuccess(int statusCode, Object parsedJsonObject, String responseText);

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            try {
                String response = new String(responseBody);
            } catch (Exception e) {
                Log.e("REST", "onSuccess: ", e);
            }
        }
    }

    public void getModels(AsyncSewioResponseHandler responseHandler) {
        RestClient restClient = new RestClient();

        RequestParams params = new RequestParams();
        params.add("X-ApiKey", "171555a8fe71148a165392904");
        restClient.get("/models", params, responseHandler);
    }
}
