package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.gson.Gson;

public class httpsTranslator {
    public final static String USER_AGENT = "Mozila/5.0";

    static String requestURI = "https://dmtcloud.kr/translate-text";
    
    public static String translateRequest(JSONObject jsonVal) throws Exception {
        String resultText = "";
        try {
            URL url = new URL(requestURI);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setDefaultUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Content-Type", "application/json");
            
            Gson g = new Gson();
            String json = g.toJson(jsonVal);

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(json.toString());
            out.close();

            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                System.out.println("HTTPS 연결 실패");
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONArray jResponseArray = JSONArray.fromObject(response.toString());
            JSONObject jResponse = jResponseArray.getJSONObject(0);
            String to = jResponse.get("to").toString();
            String text = jResponse.get("translations").toString();

            if (!"".equals(text)) {
                resultText = text;
            } else {
                resultText = "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultText;
    }
}
