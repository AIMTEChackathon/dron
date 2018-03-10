package cz.aimtec.hackathon.drone.connectivity;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by klin on 10. 3. 2018.
 */

public class RestClient {
   private String baseUrl;

    private static AsyncHttpClient client = new AsyncHttpClient();
    static {
        client.addHeader("X-ApiKey", "171555a8fe71148a165392904");
    }

    public RestClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void delete(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.delete(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(Context context, String url, HttpEntity httpEntity, AsyncHttpResponseHandler responseHandler) {
        client.post(context, getAbsoluteUrl(url), httpEntity, "application/json", responseHandler);
    }

    public void post(Context context, String url, String content, AsyncHttpResponseHandler responseHandler) {
        HttpEntity httpEntity = new StringEntity(content, ContentType.APPLICATION_JSON);
        client.post(context, getAbsoluteUrl(url), httpEntity, ContentType.APPLICATION_JSON.toString(), responseHandler);
    }

    public void getByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public void postByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return baseUrl + relativeUrl;
    }
}
