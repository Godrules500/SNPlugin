package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MyConnection extends HttpURLConnection
{
    @Override
    public void connect() throws IOException
    {

    }

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url the specified URL.
     */
    protected MyConnection(URL url)
    {
        super(url);
    }

    @Override
    public void disconnect()
    {

    }

    @Override
    public boolean usingProxy()
    {
        return false;
    }

    private String getResponse() throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(this.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null)
        {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}