package controller;

import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class RestServiceController
{
    final private String UTF_8_ENCODING = "UTF-8";

    private String buildNLAuthString(String userName, String password)
    {
        try
        {
            return "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());
        }
        catch (Exception ex)
        {
            return null;
        }
//        try {
//            return "NLAuth nlauth_email=" + URLEncoder.encode(userName, UTF_8_ENCODING) + ", nlauth_signature=" + URLEncoder.encode(password, UTF_8_ENCODING);
//        } catch (Exception ex) {
//            return null;
//        }
    }

    private HttpsURLConnection SendRequest(String userName, String password, String url, String httpMethod, JSONObject sObj) throws IOException
    {
        URL myUrl = new URL(url);
        HttpsURLConnection httpCon = (HttpsURLConnection) myUrl.openConnection();
        httpCon.setRequestMethod(httpMethod);

        httpCon.setRequestProperty("Content-Type", "application/json");
        httpCon.setRequestProperty("Accept", "application/json");
        httpCon.setRequestProperty("Authorization", buildNLAuthString(userName, password));

        if (!httpMethod.toUpperCase().equals("GET"))
        {
            httpCon.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
            out.write(sObj.toString());
            out.close();
        }
        httpCon.getInputStream();
        return httpCon;
    }

    private HttpsURLConnection SendRequest(String userName, String password, String url, String httpMethod) throws IOException
    {
        URL myUrl = new URL(url);
        HttpsURLConnection httpCon = (HttpsURLConnection) myUrl.openConnection();
        httpCon.setRequestMethod(httpMethod);

        httpCon.setRequestProperty("Content-Type", "application/json");
        httpCon.setRequestProperty("Accept", "application/json");
        httpCon.setRequestProperty("Authorization", buildNLAuthString(userName, password));

        httpCon.getInputStream();
        return httpCon;
    }

    private String getCall(String userName, String password, String url, String httpMethod)
    {
        try
        {
            HttpURLConnection httpCon = SendRequest(userName, password, url, httpMethod);
            return getHttpResponse(httpCon);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private String makePutCall(String userName, String password, String url, JSONObject sObj, String httpMethod)
    {
        return makePutOrPatchCall(userName, password, url, sObj, httpMethod);
    }

    private String makePatchCall(String userName, String password, String url, JSONObject sObj, String httpMethod)
    {
        return makePutOrPatchCall(userName, password, url, sObj, httpMethod);
    }

    /**
     * Make Post Call
     * @param userName
     * @param password
     * @param url
     * @param sObj
     * @param httpMethod
     * @return
     */
    private String makePostCall(String userName, String password, String url, JSONObject sObj, String httpMethod)
    {
        try
        {
            HttpURLConnection httpCon = SendRequest(userName, password, url, httpMethod, sObj);

            if (httpCon.getResponseCode() == 201)
            {
                return "true";
            }
            else
            {
                return "false";
            }
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * Make Put or Patch Calls
     * @param usrName
     * @param password
     * @param url
     * @param sObj
     * @param httpMethod
     * @return
     */
    @Nullable
    private String makePutOrPatchCall(String usrName, String password, String url, JSONObject sObj, String httpMethod)
    {
        try
        {
            HttpURLConnection httpCon = SendRequest(usrName, password, url, httpMethod, sObj);

            if (httpCon.getResponseCode() == 200)
            {
                return "true";
            }
            else
            {
                return "false";
            }
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * Read Response
     * @param httpCon
     * @return string
     * @throws IOException
     */
    private String getHttpResponse(HttpURLConnection httpCon) throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null)
        {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public String Get(String userName, String password, String url)
    {
        if (CheckCredentialsExists(userName, password))
        {
            return getCall(userName, password, url, "GET");
        }

        return null;
    }

    public String Put(String userName, String password, String url, JSONObject sObj)
    {
        if (CheckCredentialsExists(userName, password))
        {
            return makePutCall(userName, password, url, sObj, "PUT");
        }

        return null;
    }

    public String Post(String userName, String password, String url, JSONObject sObj)
    {
        if (CheckCredentialsExists(userName, password))
        {
            return makePostCall(userName, password, url, sObj, "POST");
        }

        return null;
    }

    public String Patch(String userName, String password, String url, JSONObject sObj)
    {
        if (CheckCredentialsExists(userName, password))
        {
            return makePatchCall(userName, password, url, sObj, "PATCH");
        }

        return null;
    }

    private boolean CheckCredentialsExists(String userName, String password)
    {
        if (userName != null && !userName.isEmpty() && password != null && !password.isEmpty())
        {
            return true;
        }
        return false;
    }

    /**
     * non api version
     * @param userName
     * @param password
     * @param url
     * @param sObj
     * @return
     */
//    public String getNSAccounts(String userName, String password, String url, SendObject sObj)
//    {
//        if (CheckCredentialsExists(userName, password))
//        {
//            return getNSRolesRestServiceJSON(userName, password, url, sObj);
//        }
//
//        return null;
//    }
//    private String getNSRolesRestServiceJSON2(String userName, String password, String url, SendObject sObj)
//    {
//        try
//        {
//            ObjectMapper mapper = new ObjectMapper();
//            String jsonInString = mapper.writeValueAsString(sObj);
//
//            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
//            connection.setAllowUserInteraction(Boolean.FALSE);
//            connection.setInstanceFollowRedirects(Boolean.FALSE);
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Authorization", buildNLAuthString(userName, password));
//            connection.setRequestProperty("Content-Type", "application/json");
//            OutputStream os = connection.getOutputStream();
//            JSONObject obj = new JSONObject();
//            os.close();
//
//            String bla = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
//
//            return bla;
//        }
//        catch (Exception ex)
//        {
//            return null;
//        }
//    }
//
//    private String getRESTAPIData(String userName, String password, String table, String url)
//    {
//        try
//        {
//            ObjectMapper mapper = new ObjectMapper();
////            String jsonInString = mapper.writeValueAsString(sObj);
//
//            CloseableHttpClient client = HttpClients.createDefault();
//            HttpGet httpPost = new HttpGet(url);
//
////            StringEntity entity = new StringEntity(jsonInString);
////            httpPost.setEntity(entity);
//            httpPost.setHeader("Accept", "application/json");
//            httpPost.setHeader("Content-type", "application/json");
//            httpPost.setHeader("Authorization", buildNLAuthString(userName, password));
//
//            CloseableHttpResponse httpResponse = client.execute(httpPost);
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(
//                    httpResponse.getEntity().getContent()));
//
//            String inputLine;
//            StringBuffer response = new StringBuffer();
//
//            while ((inputLine = reader.readLine()) != null)
//            {
//                response.append(inputLine);
//            }
//            reader.close();
//            client.close();
//            return response.toString();
//        }
//        catch (Exception ex)
//        {
//            return null;
//        }
//    }
//
//    private String getNSRolesRestServiceJSON(String userName, String password, String url, SendObject sObj)
//    {
//        try
//        {
//            ObjectMapper mapper = new ObjectMapper();
//            String jsonInString = mapper.writeValueAsString(sObj);
//
//            CloseableHttpClient client = HttpClients.createDefault();
//            HttpPost httpPost = new HttpPost(url);
//
//            StringEntity entity = new StringEntity(jsonInString);
//            httpPost.setEntity(entity);
//            httpPost.setHeader("Accept", "application/json");
//            httpPost.setHeader("Content-type", "application/json");
//            httpPost.setHeader("Authorization", buildNLAuthString(userName, password));
//
//            CloseableHttpResponse httpResponse = client.execute(httpPost);
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(
//                    httpResponse.getEntity().getContent()));
//
//            String inputLine;
//            StringBuffer response = new StringBuffer();
//
//            while ((inputLine = reader.readLine()) != null)
//            {
//                response.append(inputLine);
//            }
//            reader.close();
//            client.close();
//            return response.toString();
//        }
//        catch (Exception ex)
//        {
//            return null;
//        }
//    }
//    private String getNSRolesRestServiceApiJSON(String userName, String password, String url, SendObject sObj)
//    {
//        try
//        {
//            CloseableHttpClient client = HttpClients.createDefault();
//            HttpGet httpGet = new HttpGet(url);
//
//            httpGet.setHeader("Accept", "application/json");
//            httpGet.setHeader("Content-type", "application/json");
//            httpGet.setHeader("Authorization", buildNLAuthString(userName, password));
//
//            CloseableHttpResponse httpResponse = client.execute(httpGet);
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
//
//            String inputLine;
//            StringBuffer response = new StringBuffer();
//
//            while ((inputLine = reader.readLine()) != null)
//            {
//                response.append(inputLine);
//            }
//            reader.close();
//            client.close();
//            return response.toString();
//        }
//        catch (Exception ex)
//        {
//            return null;
//        }
//    }
}