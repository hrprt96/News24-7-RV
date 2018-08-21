package com.sdzshn3.android.news247;

import android.text.TextUtils;
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

public final class QueryUtils {

    public static final int readTimeout = 10000;
    public static final int connectTimeout = 15000;

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private QueryUtils() {
    }

    public static ArrayList<News> fetchNewsData(String requestUrl) {

        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        return extractFeatureFromJson(jsonResponse);
    }

    private static ArrayList<News> extractFeatureFromJson(String newsJSON) {
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        ArrayList<News> news = new ArrayList<>();

        try {
            JSONObject baseJasonResponse = new JSONObject(newsJSON);
            JSONObject response = baseJasonResponse.getJSONObject("response");
            String status = response.getString("status");
            if (status.equals("ok")) {
                int pageSize = response.getInt("pageSize");
                JSONArray articlesArray = response.getJSONArray("results");

                for (int i = 0; i < pageSize; i++) {
                    JSONObject currentNewsArticle = articlesArray.getJSONObject(i);
                    String sectionName = currentNewsArticle.optString("sectionName");
                    String title = currentNewsArticle.optString("webTitle");
                    String articleUrl = currentNewsArticle.optString("webUrl");
                    String publishedAt = currentNewsArticle.optString("webPublicationDate");
                    String thumbnail;
                    try {
                        JSONObject fields = currentNewsArticle.optJSONObject("fields");
                        thumbnail = fields.optString("thumbnail");
                    } catch (NullPointerException e) {
                        Log.e(LOG_TAG, "Images not found from JSON or field not requested in website");
                        thumbnail = "moImage";
                    }

                    String firstName = "";
                    String lastName = "";
                    try {
                        JSONArray tags = currentNewsArticle.getJSONArray("tags");
                        JSONObject currentTags = tags.getJSONObject(0);
                        firstName = currentTags.optString("firstName");
                        lastName = currentTags.optString("lastName");
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "No info found about author");
                    }

                    News newsResult = new News(sectionName, title, articleUrl, publishedAt, firstName, lastName, thumbnail);
                    news.add(newsResult);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the news JSON results", e);
        }
        return news;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL");
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(readTimeout/*Milliseconds*/);
            urlConnection.setConnectTimeout(connectTimeout/*Milliseconds*/);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
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
}
