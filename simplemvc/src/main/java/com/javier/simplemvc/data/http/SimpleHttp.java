package com.javier.simplemvc.data.http;

import com.javier.simplemvc.util.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@SuppressWarnings("unused")
public final class SimpleHttp {

    private static String MODE = "UTF-8";

    private HttpURLConnection httpConnection;
    private HttpsURLConnection httpsConnection;

    private String accessToken = "";

    public ResponseEntity post(String url, String data, boolean encrypt) {
        return post(url, data, encrypt, null);
    }

    public ResponseEntity post(String url, String data, boolean encrypt, Headers headers) {
        if (encrypt) {
            return httpsRequest(url, data, "POST", headers);
        }

        return httpRequest(url, data, "POST", headers);
    }

    public ResponseEntity get(String url, String data, boolean encrypt) {
        return get(url, data, encrypt, null);
    }

    public ResponseEntity get(String url, String data, boolean encrypt, Headers headers) {
        if (encrypt) {
            return httpsRequest(url, data, "GET",headers);
        }

        return httpRequest(url, data, "GET",headers);
    }

    private ResponseEntity httpRequest(String requestUrl, String data, String method, Headers headers) {
        try {
            URL url;

            if (method.equalsIgnoreCase("GET")) {
                url = new URL(requestUrl + "?" + data);
            } else {
                url = new URL(requestUrl);
            }

            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod(method);

            // 设置连接属性
            attachCommonAttribute(httpConnection, headers);

            httpConnection.connect();

            if (method.equalsIgnoreCase("post")) {
                // 发送数据
                writeRequest(httpConnection, data);
            }

            int responseCode = httpConnection.getResponseCode();

            // 认证token
            String author = httpConnection.getHeaderField("Authorization");

            Logger.getLogger().i("response code : " + responseCode);

            String responseBody = "";

            if (HttpURLConnection.HTTP_OK == responseCode) {
                responseBody = readResponse(httpConnection);
            }

            destroy();

            return createResponseEntity(author, responseBody, responseCode);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

            destroy();
            return createResponseEntity("", "", HttpURLConnection.HTTP_CLIENT_TIMEOUT);
        }

        destroy();
        return createResponseEntity("", "", HttpURLConnection.HTTP_BAD_REQUEST);
    }

    private ResponseEntity httpsRequest(String requestUrl, String data, String method, Headers headers) {
        try {
            URL url;

            if (method.equalsIgnoreCase("get")) {
                url = new URL(requestUrl + "?" + data);
            } else {
                url = new URL(requestUrl);
            }

            //设置SSLContext
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{new TrustAllManager()}, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });

            httpsConnection = (HttpsURLConnection) url.openConnection();
            httpsConnection.setRequestMethod(method);

            attachCommonAttribute(httpsConnection, headers);

            httpsConnection.connect();

            if (method.equalsIgnoreCase("post")) {
                writeRequest(httpsConnection, data);
            }

            //读取服务器的响应内容并显示
            int responseCode = httpsConnection.getResponseCode();

            // 认证信息
            String author = httpsConnection.getHeaderField("Authorization");

            Logger.getLogger().i("response code : " + responseCode);

            String responseBody = "";

            if (HttpsURLConnection.HTTP_OK == responseCode) {
                responseBody = readResponse(httpsConnection);
            }

            destroy();

            return createResponseEntity(author, responseBody, responseCode);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return createResponseEntity("", "", HttpURLConnection.HTTP_BAD_REQUEST);

        } catch (KeyManagementException e) {
            e.printStackTrace();
            return createResponseEntity("", "", HttpURLConnection.HTTP_BAD_REQUEST);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return createResponseEntity("", "", HttpURLConnection.HTTP_BAD_REQUEST);

        } catch (IOException e) {
            e.printStackTrace();
            return createResponseEntity("", "", HttpURLConnection.HTTP_CLIENT_TIMEOUT);

        } finally {
            destroy();
        }
    }


    public String download(String downUrl) {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader buffer = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(downUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            buffer = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            while ((line = buffer.readLine()) != null) {
                sb.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            try {
                if (buffer != null)
                    buffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString().trim().replaceAll("\r", "").replace("\n", "")
                .replaceAll("\t", "");
    }


    private void attachCommonAttribute(URLConnection connection, Headers headers) {
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Charset", MODE);
        connection.setRequestProperty("Authorization", accessToken);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);

        if (headers == null) {
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        } else {
            connection.setUseCaches(headers.isUserCache());
            connection.setRequestProperty("Content-type", headers.getContentType());

            if (headers.getCustomHeader() != null) {
                Iterator iterator = headers.getCustomHeader().entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();

                    connection.setRequestProperty(String.valueOf(entry.getKey()), String.valueOf(entry.getKey()));
                }
            }
        }
    }

    private void writeRequest(URLConnection connection, String data) throws IOException {
        OutputStreamWriter outputStream = new OutputStreamWriter(connection.getOutputStream());
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
    }

    private String readResponse(URLConnection connection) throws IOException {
        StringBuilder sb = new StringBuilder();
        String readLine;
        InputStreamReader input = new InputStreamReader(connection.getInputStream(), MODE);
        BufferedReader responseReader = new BufferedReader(input);

        while ((readLine = responseReader.readLine()) != null) {
            sb.append(readLine).append("\n");
        }

        input.close();
        responseReader.close();

        return sb.toString().replaceAll("[\\r\\n\\t]", "");
    }

    /**
     * 创建相应结果对象
     *
     * @param author 授权token id
     * @param body   相应消息内容
     * @return ResponseEntity 对象
     */
    private ResponseEntity createResponseEntity(String author, String body, int code) {
        ResponseEntity entity = new ResponseEntity();
        entity.setTokenId(author);
        entity.setContent(body);
        entity.setResponseCode(code);

        Logger.getLogger().i("ResponseEntity : " + entity.toString());

        return entity;
    }

    public class TrustAllManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        new Thread(closeRunnable).start();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    private Runnable closeRunnable = new Runnable() {
        @Override
        public void run() {
            if (httpConnection != null) {
                httpConnection.disconnect();
                httpConnection = null;
            }

            if (httpsConnection != null) {
                httpsConnection.disconnect();
                httpsConnection = null;
            }
        }
    };
}