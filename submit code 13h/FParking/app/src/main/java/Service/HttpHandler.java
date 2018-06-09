package Service;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpHandler {

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
