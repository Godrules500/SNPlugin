package serviceNow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
    final private String NS_WSDL_VERSION = "2017_1";
    final private String NS_ENVIRONMENT_PRODUCTION = "Production";
    final private String NS_ENVIRONMENT_SANDBOX = "Sandbox";
    final private String NS_ROLES_REST_SERVICE_URL_PRODUCTION = "https://rest.netsuite.com/rest/roles";
    final private String NS_ROLES_REST_SERVICE_URL_SANDBOX = "https://rest.sandbox.netsuite.com/rest/roles";
    final private String NS_WEB_SERVICES_END_POINT = "/services/NetSuitePort_" + NS_WSDL_VERSION;
    final private String NS_SANDBOX_WEB_SERVICES_URL = "https://webservices.sandbox.netsuite.com";
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

//    private String getEnvironmentRolesRestServiceURL(String nsEnvironment) {
//        String rolesRestServiceURL = null;
//
//        if (nsEnvironment.equals(NS_ENVIRONMENT_PRODUCTION)) {
//            rolesRestServiceURL = NS_ROLES_REST_SERVICE_URL_PRODUCTION;
//        } else if (nsEnvironment.equals(NS_ENVIRONMENT_SANDBOX)) {
//            rolesRestServiceURL = NS_ROLES_REST_SERVICE_URL_SANDBOX;
//        }
//
//        return rolesRestServiceURL;
//    }

    private ArrayList<NSAccount> getNSAccountsList(String nsRolesRestServiceJSON, String nsEmail, String nsPassword)
    {
        ArrayList<NSAccount> nsAccounts = null;

        if (nsRolesRestServiceJSON != null)
        {

            if (nsRolesRestServiceJSON.length() > 0)
            {
                try
                {
                    nsAccounts = new ArrayList<NSAccount>();

                    JSONArray accountsJSON = new JSONArray(nsRolesRestServiceJSON);

                    for (int i = 0; i < accountsJSON.length(); i++)
                    {
                        JSONObject accountJSON = accountsJSON.getJSONObject(i);

                        if (accountJSON.has("account") && accountJSON.has("dataCenterURLs"))
                        {
                            nsAccounts.add(new NSAccount(accountJSON.getJSONObject("account").get("internalId").toString(),
                                    accountJSON.getJSONObject("account").get("name").toString(),
                                    nsEmail,
                                    nsPassword,
                                    accountJSON.getJSONObject("role").get("internalId").toString(),
                                    accountJSON.getJSONObject("role").get("name").toString(),
                                    accountJSON.getJSONObject("dataCenterURLs").get("restDomain").toString(),
                                    accountJSON.getJSONObject("dataCenterURLs").get("webservicesDomain").toString().concat(NS_WEB_SERVICES_END_POINT),
                                    NS_SANDBOX_WEB_SERVICES_URL.concat(NS_WEB_SERVICES_END_POINT),
                                    accountJSON.getJSONObject("dataCenterURLs").get("systemDomain").toString())
                            );
                        }
                    }
                }
                catch (Exception ex)
                {
                    return null;
                }
            }
        }

        return nsAccounts;
    }

    public String getNSAccounts(String nsEmail, String nsPassword, String url)
    {
        if (nsEmail != null && !nsEmail.isEmpty() && nsPassword != null && !nsPassword.isEmpty())
        {
            return getNSRolesRestServiceJSON(nsEmail, nsPassword, url);
//            return getNSAccountsList(getNSRolesRestServiceJSON(nsEmail, nsPassword, url), nsEmail, nsPassword);
        }

        return null;
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

    public String getNSAccountsApi(String nsEmail, String nsPassword, String url, SendObject sObj)
    {
        if (nsEmail != null && !nsEmail.isEmpty() && nsPassword != null && !nsPassword.isEmpty())
        {
            return getNSRolesRestServiceApiJSON(nsEmail, nsPassword, url, sObj);
//            return getNSAccountsList(getNSRolesRestServiceJSON(nsEmail, nsPassword, url), nsEmail, nsPassword);
        }

        return null;
    }
}