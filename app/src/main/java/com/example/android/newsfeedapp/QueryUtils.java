package com.example.android.newsfeedapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving data from The Guardian API.
 */
public final class QueryUtils {
    public static final String LOG_TAG = QueryUtils.class.getName();

    //this have a private constructor because no one should create an instance of this class.
    private QueryUtils() {
    }

    /**
     * Query the Guardian database and return an {@link QueryUtils} object to represent a single news.
     */
    public static List<NewsList> fetchData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);
        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        // Extract relevant fields from the JSON response and create a list of news
        List<NewsList> news = extractData(jsonResponse);
        return news;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the InputStream into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link NewsList} objects that has been built up from parsing a JSON response.
     */
    public static List<NewsList> extractData(String jsonResponse) {
        ArrayList<NewsList> newsList = new ArrayList<>();
        // Try to parse the JSON Response.
        try {
            //This creates the root JSONObject by calling jsonResponse
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);
            JSONObject response = baseJsonResponse.getJSONObject("response");
            //this get the objects contained in the a news Array
            if (response.has("results")) {
                JSONArray newsArray = response.getJSONArray("results");
                //the data are obtained for each news
                for (int i = 0; i < newsArray.length(); i++) {
                    //here there is pulled out the JSON object at the specified position
                    JSONObject singleNews = newsArray.getJSONObject(i);
                    //here there is extracted out the results key.
                    String webTitle = "";//this gets the title of news
                    if (singleNews.has("webTitle")) {
                        webTitle = singleNews.getString("webTitle");
                    }
                    String sectionName = ""; //this gets the section name
                    if (singleNews.has("sectionName")) {
                        sectionName = singleNews.getString("sectionName");
                    }
                    String webPublicationDate = "";  //this gets the publication date
                    if (singleNews.has("webPublicationDate")) {
                        webPublicationDate = singleNews.getString("webPublicationDate");
                    }
                    String webUrl = ""; //this gets the url of the web page
                    if (singleNews.has("webUrl")) {
                        webUrl = singleNews.getString("webUrl");
                    }
                    JSONArray tagsArray;
                    String authorName = ""; //this gets the author name
                    if (singleNews.has("tags")) {
                        tagsArray = singleNews.getJSONArray("tags");
                        if (tagsArray.length() > 0) {
                            for (int author = 0; author < 1; author++) {
                                JSONObject tags = tagsArray.getJSONObject(author);
                                if (tags.has("webTitle")) {
                                    authorName = tags.getString("webTitle");
                                }
                            }
                        }
                    }
                    String trailText = ""; //this gets a short description
                    if (singleNews.has("fields")) {
                        JSONObject fields = singleNews.getJSONObject("fields");
                        if (fields.has("trailText")) {
                            trailText = fields.getString("trailText");
                        }
                    }
                    //here it is create a new NewsList object with all data extracted from JSON
                    NewsList news = new NewsList(webTitle, sectionName, webPublicationDate, webUrl, authorName, trailText);
                    //add the NewsList object to the Array
                    newsList.add(news);
                }
            } else {
                Log.v(LOG_TAG, "No results found");
            }
        } catch (JSONException e) {
            //If there's a problem with the way the JSON is formatted, a JSONException exception object will be thrown.
            // Catch the exception so the app doesn't crash, and print the following error message to the logs.
            Log.e(LOG_TAG, e.getMessage());
        }
        return newsList;
    }
}
