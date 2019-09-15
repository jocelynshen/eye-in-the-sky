package com.codepath.apps.restclienttemplate;
//package URL_Req;

import com.codepath.apps.restclienttemplate.Objects.Tornado;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class WeatherClient{
        public static void main(String[] args) {
        try {
            String url = "https://www.ncdc.noaa.gov/swdiws/json/nx3tvs/201902020000:201902022359";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            } in .close();

            String responseString = response.toString();
            int first = responseString.indexOf("[");
            int last = responseString.lastIndexOf("]");
            System.out.println(last);
            System.out.println(responseString.length());
            String responseJSONString = '{' + responseString.substring(224,last+1) + '}';
            String fixedString = responseJSONString.replaceAll("\"","\\\"");

            String testJSONString = "{\"result\": [{\"CELL_TYPE\":\"TVS\"}]}";
//            String testJSONString = "{"result": [{\"CELL_TYPE\":\"TVS\"}]}";
            System.out.println(fixedString);

            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(fixedString).getAsJsonObject();
            JsonElement jsonObjects = jsonObject.get("result");
            JsonArray jsonArray = jsonObjects.getAsJsonArray();

            ArrayList<Tornado> tornadoList = new ArrayList<Tornado>();
            for (int i=0; i < jsonArray.size(); i++) {
                Tornado newTornado = gson.fromJson(jsonArray.get(i), Tornado.class);
                newTornado.fixCoordinates();
                tornadoList.add(newTornado);
            }
            System.out.println(tornadoList.get(50).latitude);

        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public static ArrayList<Tornado> getDataPoints() {
        ArrayList<Tornado> tornadoList = new ArrayList<Tornado>();
        try {
            String url = "https://www.ncdc.noaa.gov/swdiws/json/nx3tvs/201902020000:201902022359";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            } in .close();

            String responseString = response.toString();
            int first = responseString.indexOf("[");
            int last = responseString.lastIndexOf("]");
//            System.out.println(last);
//            System.out.println(responseString.length());
            String responseJSONString = '{' + responseString.substring(224,last+1) + '}';
            String fixedString = responseJSONString.replaceAll("\"","\\\"");

            String testJSONString = "{\"result\": [{\"CELL_TYPE\":\"TVS\"}]}";
//            String testJSONString = "{"result": [{\"CELL_TYPE\":\"TVS\"}]}";
//            System.out.println(fixedString);

            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(fixedString).getAsJsonObject();
            JsonElement jsonObjects = jsonObject.get("result");
            JsonArray jsonArray = jsonObjects.getAsJsonArray();

            for (int i=0; i < jsonArray.size(); i++) {
                Tornado newTornado = gson.fromJson(jsonArray.get(i), Tornado.class);
                newTornado.fixCoordinates();
                tornadoList.add(newTornado);
            }

        } catch(Exception e) {
            System.out.println(e);
        }
        return tornadoList;
    };
}