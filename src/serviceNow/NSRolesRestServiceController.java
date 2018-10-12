package serviceNow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;

public class NSRolesRestServiceController
{
    final private String UTF_8_ENCODING = "UTF-8";

    private String buildNLAuthString(String nsEmail, String nsPassword)
    {
        try
        {
            return "Basic " + Base64.getEncoder().encodeToString((nsEmail + ":" + nsPassword).getBytes());
        }
        catch (Exception ex)
        {
            return null;
        }
//        try {
//            return "NLAuth nlauth_email=" + URLEncoder.encode(nsEmail, UTF_8_ENCODING) + ", nlauth_signature=" + URLEncoder.encode(nsPassword, UTF_8_ENCODING);
//        } catch (Exception ex) {
//            return null;
//        }
    }

    private String getNSRolesRestServiceJSON2(String nsEmail, String nsPassword, String url, SendObject sObj)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(sObj);

            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setAllowUserInteraction(Boolean.FALSE);
            connection.setInstanceFollowRedirects(Boolean.FALSE);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", buildNLAuthString(nsEmail, nsPassword));
            connection.setRequestProperty("Content-Type", "application/json");
            OutputStream os = connection.getOutputStream();
            JSONObject obj = new JSONObject();
            os.close();

            String bla = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();

            return bla;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private String getRESTAPIData(String nsEmail, String nsPassword, String table, String url)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
//            String jsonInString = mapper.writeValueAsString(sObj);

            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet httpPost = new HttpGet(url);

//            StringEntity entity = new StringEntity(jsonInString);
//            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", buildNLAuthString(nsEmail, nsPassword));

            CloseableHttpResponse httpResponse = client.execute(httpPost);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent()));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = reader.readLine()) != null)
            {
                response.append(inputLine);
            }
            reader.close();
            client.close();
            return response.toString();
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private String getNSRolesRestServiceJSON(String nsEmail, String nsPassword, String url, SendObject sObj)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(sObj);

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);

            StringEntity entity = new StringEntity(jsonInString);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", buildNLAuthString(nsEmail, nsPassword));

            CloseableHttpResponse httpResponse = client.execute(httpPost);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent()));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = reader.readLine()) != null)
            {
                response.append(inputLine);
            }
            reader.close();
            client.close();
            return response.toString();
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private String getNSRolesRestServiceApiJSON(String nsEmail, String nsPassword, String url, SendObject sObj)
    {
        try
        {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);

            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-type", "application/json");
            httpGet.setHeader("Authorization", buildNLAuthString(nsEmail, nsPassword));

            CloseableHttpResponse httpResponse = client.execute(httpGet);

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = reader.readLine()) != null)
            {
                response.append(inputLine);
            }
            reader.close();
            client.close();
            return response.toString();
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private String makePutCall(String nsEmail, String nsPassword, String url, JSONObject sObj)
    {
        try
        {
            HttpPut httpPut = new HttpPut(url);

            CloseableHttpClient client = HttpClients.createDefault();
            StringEntity entity = new StringEntity(sObj.toString());
            entity.setContentType("application/json");
            httpPut.setEntity(entity);

            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-type", "application/json");
            httpPut.setHeader("Authorization", buildNLAuthString(nsEmail, nsPassword));

            CloseableHttpResponse httpResponse = client.execute(httpPut);

            if(httpResponse.getStatusLine().getStatusCode() == 200)
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

    private String makePatchCall(String nsEmail, String nsPassword, String url, JSONObject sObj)
    {
        try
        {
            HttpPatch httpPatch = new HttpPatch(url);

            CloseableHttpClient client = HttpClients.createDefault();
            StringEntity entity = new StringEntity(sObj.toString());
            entity.setContentType("application/json");
            httpPatch.setEntity(entity);

            httpPatch.setHeader("Accept", "application/json");
            httpPatch.setHeader("Content-type", "application/json");
            httpPatch.setHeader("Authorization", buildNLAuthString(nsEmail, nsPassword));

            CloseableHttpResponse httpResponse = client.execute(httpPatch);

            if(httpResponse.getStatusLine().getStatusCode() == 200)
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

    private String makePostCall(String nsEmail, String nsPassword, String url, JSONObject sObj)
    {
        try
        {
//            ObjectMapper mapper = new ObjectMapper();
//            String jsonInString = mapper.writeValueAsString(sObj);
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);

            StringEntity entity = new StringEntity(sObj.toString());
            entity.setContentType("application/json");
            httpPost.setEntity(entity);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", buildNLAuthString(nsEmail, nsPassword));

            CloseableHttpResponse httpResponse = client.execute(httpPost);

            if(httpResponse.getStatusLine().getStatusCode() == 201)
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

    private String getNSRolesRestServiceJSON(String nsEmail, String nsPassword, String url)
    {
        try
        {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setAllowUserInteraction(Boolean.FALSE);
            connection.setInstanceFollowRedirects(Boolean.FALSE);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", buildNLAuthString(nsEmail, nsPassword));


            String bla = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            return bla;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public String getNSAccounts(String nsEmail, String nsPassword, String url, SendObject sObj)
    {
        if (nsEmail != null && !nsEmail.isEmpty() && nsPassword != null && !nsPassword.isEmpty())
        {
            return getNSRolesRestServiceJSON(nsEmail, nsPassword, url, sObj);
//            return getNSAccountsList(getNSRolesRestServiceJSON(nsEmail, nsPassword, url), nsEmail, nsPassword);
        }

        return null;
    }

    public String Get(String nsEmail, String nsPassword, String url, SendObject sObj)
    {
        if (nsEmail != null && !nsEmail.isEmpty() && nsPassword != null && !nsPassword.isEmpty())
        {
            return getNSRolesRestServiceApiJSON(nsEmail, nsPassword, url, sObj);
//            return getNSAccountsList(getNSRolesRestServiceJSON(nsEmail, nsPassword, url), nsEmail, nsPassword);
        }

        return null;
    }

    public String Put(String nsEmail, String nsPassword, String url, JSONObject sObj)
    {
        if (nsEmail != null && !nsEmail.isEmpty() && nsPassword != null && !nsPassword.isEmpty())
        {
            return makePutCall(nsEmail, nsPassword, url, sObj);
//            return getNSAccountsList(getNSRolesRestServiceJSON(nsEmail, nsPassword, url), nsEmail, nsPassword);
        }

        return null;
    }

    public String Post(String nsEmail, String nsPassword, String url, JSONObject sObj)
    {
        if (nsEmail != null && !nsEmail.isEmpty() && nsPassword != null && !nsPassword.isEmpty())
        {
            return makePostCall(nsEmail, nsPassword, url, sObj);
//            return getNSAccountsList(getNSRolesRestServiceJSON(nsEmail, nsPassword, url), nsEmail, nsPassword);
        }

        return null;
    }

    public String Patch(String nsEmail, String nsPassword, String url, JSONObject sObj)
    {
        if (nsEmail != null && !nsEmail.isEmpty() && nsPassword != null && !nsPassword.isEmpty())
        {
            return makePatchCall(nsEmail, nsPassword, url, sObj);
//            return getNSAccountsList(getNSRolesRestServiceJSON(nsEmail, nsPassword, url), nsEmail, nsPassword);
        }

        return null;
    }
}