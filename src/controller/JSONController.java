package controller;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class JSONController
{
    public JSONObject resultObj;
    public JSONObject errorObj;
    public JSONArray jsonArr;
    public Boolean responseSuccess = false;

    public JSONController(String response) throws JSONException
    {
        JSONObject json = new JSONObject(response);
        if (json.has("result"))
        {
            Object result = json.get("result");
            if (result instanceof JSONArray)
            {
                jsonArr = (JSONArray) result;
            }
            else
            {
                resultObj = (JSONObject) result;
            }
            responseSuccess = true;
        }
        else if (json.has("error"))
        {
            Object error = json.get("error");
            errorObj = (JSONObject) error;
            responseSuccess = false;
        }
    }

    public JSONObject getFirstResult() throws JSONException
    {
        if (resultObj != null)
        {
            return resultObj;
        }
        else if (jsonArr != null)
        {
            return (JSONObject) jsonArr.get(0);
        }
        return null;
    }

    public JSONObject getError() throws JSONException
    {
        JSONObject returnObj = new JSONObject();
        returnObj.put("success", false);
        returnObj.put("error", errorObj.toString());
        return returnObj;
    }
}
