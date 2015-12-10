package eins.fireballs3;

import android.os.NetworkOnMainThreadException;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class HttpThread extends Thread {
    String addr;
    String xmlDoc;

    HttpThread(String URLaddr) {
        this.addr = URLaddr;
        xmlDoc="";
    }
    public void run() {
        xmlDoc = getXmlDoc();
        Log.e("@xmlDoc",xmlDoc);
    }
    public String getValue(String key) {
        if(xmlDoc.isEmpty()){
            return "JSON doc download error";
        }
        try {

            JSONObject obj = new JSONObject(xmlDoc);

            if(obj == null)
            {
                Log.e("@JsonObject","Null value");
                return "JSON object creation error";
            }
            return obj.getString(key);
        } catch (JSONException e){
            e.printStackTrace();
            Log.e("@JsonParsing","Json Parsing exception");
            return "JSON parsing error";
        }
    }
    String getXmlDoc() {
        StringBuilder html = new StringBuilder();
        try {
            URL url = new URL(addr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            if (conn != null) {
                Log.e("@http","connection is not null");
                conn.setConnectTimeout(2000);
                conn.setUseCaches(false);
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.e("@http","response code is find");
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    for(;;) {
                        Log.e("@http","getting line by line");
                        String line = br.readLine();
                        if(line == null) break;
                        html.append(line);
                    }
                    br.close();

                }
                conn.disconnect();
            }
        } catch (NetworkOnMainThreadException e) {
            return "Error: 메인 스레드 네트워크 작업 에러 - "+e.getMessage();
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
        return html.toString();
    }
}
