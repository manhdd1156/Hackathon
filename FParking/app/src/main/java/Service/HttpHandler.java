package Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpHandler {
    URL url = null;

    public String getrequiement(String requiementURL) {

        try {
            URL url = new URL(requiementURL);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "ahih";
    }

    public String convertStremToString(InputStream inputStream){
        return "ahihi";
    }
}
