package Service;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class HttpHandler {
    private static final String TAG = HttpHandler.class.getSimpleName();
    public String getrequiement(String requirementUrl) {

        String result = null;

        try {
            URL url = new URL(requirementUrl);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            result = convertStremToString(httpURLConnection.getInputStream());

        } catch (MalformedURLException e) {
            Log.e("MalformedURLException: ", e.getMessage());
        } catch (IOException ioe) {
            Log.e("IOException: ", ioe.getMessage());
        }
        return result;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String post(String reqUrl, String urlParameters) {
        String response = null;
        try {
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches(false);
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData);
            }
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStremToString(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

    private String convertStremToString(InputStream inputStream) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(bufferedReader.readLine()).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
