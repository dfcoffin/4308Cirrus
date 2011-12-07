package cx.it.cirrus;

import java.util.Calendar;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class MeterUtils {
    public static final String METER_READINGS_BASE_URL = "https://dev-program.tendrildemo.com/api/rest/meter/read;account=Jenkins;";
    public static final String CONSUMPTION_BASE_URL = "https://dev-program.tendrildemo.com/api/rest/meter/consumption;account=Jenkins;";
    public static final String meterReadingTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
    
    private static final String username = "jason@tendrilinc.com";
    private static final String password = "password";
    
    public static String getCurrentMeterReading() {
        
        String response = "";
        String line = "";
        HttpsURLConnection c = null;
        
        String currentMeterReadURL = METER_READINGS_BASE_URL
                + ";from=2000-01-01T00:00:00-0000;limitToLatest=1";
        try {
            URL u = new URL(currentMeterReadURL);
            
            c = (HttpsURLConnection) u.openConnection();
            
            c.setRequestProperty("Emsauthtoken", username + ":" + password);
            c.setRequestProperty("Accept", "application/json");
            
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    c.getInputStream()));
            while ((line = reader.readLine()) != null) {
                response += line;
            }
        } catch (MalformedURLException e) {
            Log.e("MeterUtils ", "MalformedURLException  " + e.toString());
        } catch (IOException e) {
            Log.e("MeterUtils ", "IOException   " + e.toString());
            e.printStackTrace();
        } finally {
            if (c != null)
                c.disconnect();
        }
        
        return response;
    }
    
    public static JSONObject getDateRangeReadings(String baseURL, String start,
            String end) throws JSONException {
        
        String response = "";
        String line = "";
        HttpsURLConnection c = null;
        
        String currentMeterReadURL = baseURL + ";from=" + start + ";to=" + end
                + ";limitToLatest=10";
        Log.i("url: ", currentMeterReadURL);
        
        try {
            URL u = new URL(currentMeterReadURL);
            
            c = (HttpsURLConnection) u.openConnection();
            
            c.setRequestProperty("Emsauthtoken", username + ":" + password);
            c.setRequestProperty("Accept", "application/json");
            
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    c.getInputStream()));
            while ((line = reader.readLine()) != null) {
                response += line;
            }
        } catch (MalformedURLException e) {
            Log.e("MeterUtils ", "MalformedURLException  " + e.toString());
        } catch (IOException e) {
            Log.e("MeterUtils ", "IOException   " + e.toString());
            e.printStackTrace();
        } finally {
            if (c != null)
                c.disconnect();
        }
        // System.out.println(response);
        return new JSONObject(response);
        
    }
    
    // 2010-01-01T00:00:00-0000
    
    // 12-06 16:06:32.253: I/url:(308):
    // https://dev-program.tendrildemo.com/api/rest/meter/read;account=Jenkins;
    // from=2011-12-06T16:06:16.061-07:00;
    // to=2011-12-06T16:06:16.062-07:00
    
    public static String calendarToURLString(Calendar cal, boolean includeTime) {
        DateTime d = new DateTime(cal);
        DateTimeFormatter dtf;
        if (includeTime)
            dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss-0000");
        else
            dtf = DateTimeFormat.forPattern("yyyy-MM-dd-0000");
        
        return d.toString(dtf);
    }
    
    public static Number[][] formatMeterData(JSONObject meterData) {
        
        int numReadings;
        Number[][] returnArray;
        // Format that the API returns for the date
        DateTimeFormatter dtf = DateTimeFormat
                .forPattern(meterReadingTimePattern);
        
        try {
            
            numReadings = meterData.getJSONArray("MeterReading")
                    .getJSONObject(0).getJSONArray("Readings").length();
            returnArray = new Number[2][numReadings];
            for (int i = 0; i < numReadings; i++) {
                // Set the values
                String value = meterData.getJSONArray("MeterReading")
                        .getJSONObject(0).getJSONArray("Readings")
                        .getJSONObject(i).getString("value");
                returnArray[0][i] = Float.parseFloat(value);
                // Set the timestamps
                String timestamp = meterData.getJSONArray("MeterReading")
                        .getJSONObject(0).getJSONArray("Readings")
                        .getJSONObject(i).getString("timeStamp");
                
                // convert timestamp to epoc time
                returnArray[1][i] = DateTime.parse(timestamp, dtf)
                        .toCalendar(null).getTimeInMillis();
                
                //System.out.println(returnArray[0][i] + " : "
                //        + returnArray[1][i]);
            }
            
            return returnArray;
            
        } catch (Exception e) {
            System.out.println("JSONException: " + e);
        }
        
        return null;
        
    }
    
}