package com.voronnenok.flyhttp;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by voronnenok on 03.06.15.
 */
public class HttpUrlClient implements HttpClient {
    @Override
    public HttpResponse fireRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException {
        HttpURLConnection httpURLConnection = openConnection(request);
        Map<String, String> headers = new HashMap<>();

        headers.putAll(request.getHeaders());
        headers.putAll(additionalHeaders);

        setConnectionProperties(httpURLConnection, headers);

        setConnectionParams(request, httpURLConnection);

        int statusCode;
        try {
            statusCode = httpURLConnection.getResponseCode();
        } catch (IOException e) {
            statusCode = httpURLConnection.getResponseCode();
        }
        if(statusCode == -1) {
            throw new IOException("Cannot retrieve status code from response");
        }

        String message = httpURLConnection.getResponseMessage();
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);

        BasicHttpResponse httpResponse = new BasicHttpResponse(protocolVersion, statusCode, message);

        httpResponse.setEntity(getEntityFromConnection(httpURLConnection));

        for(Map.Entry<String, List<String>> entry : httpURLConnection.getHeaderFields().entrySet()) {
            if(entry.getKey() != null) {
                for (String value : entry.getValue()) {
                    httpResponse.addHeader(entry.getKey(), value);
                }
            }
        }

        return httpResponse;
    }

    private static HttpURLConnection openConnection(Request<?> request) throws IOException {
        URL url = new URL(request.getUrl());
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
        urlConnection.setUseCaches(false);
        urlConnection.setConnectTimeout(request.getDefaultTimeout());
        urlConnection.setReadTimeout(request.getDefaultTimeout());
        urlConnection.setDoInput(true);

        return urlConnection;
    }

    static void setConnectionParams(Request<?> request, HttpURLConnection httpURLConnection) throws IOException {
        switch (request.getMethod()) {
            case POST:
                addBodyIfExists(request, httpURLConnection);
                break;
        }
    }

    private static void addBodyIfExists(Request<?> request, HttpURLConnection httpURLConnection) throws IOException {
        byte[] body = request.getBody();
        if(body != null) {
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setFixedLengthStreamingMode(body.length);
            httpURLConnection.setRequestProperty(Headers.CONTENT_TYPE, request.getBodyContentType());
            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.write(body, 0, body.length);
            dos.flush();
            dos.close();
        }
    }

    private static void setConnectionProperties(HttpURLConnection httpURLConnection, Map<String, String> headers) {
        for(Map.Entry<String, String> entry : headers.entrySet()) {
            httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    private static HttpEntity getEntityFromConnection(HttpURLConnection httpURLConnection) {
        BasicHttpEntity httpEntity = new BasicHttpEntity();
        httpEntity.setContentLength(httpURLConnection.getContentLength());
        try {
            httpEntity.setContent(httpURLConnection.getInputStream());
        } catch (IOException e) {
            httpEntity.setContent(httpURLConnection.getErrorStream());
        }
        return httpEntity;
    }

}
