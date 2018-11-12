package controller;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import projectsettings.ProjectSettingsController;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.stream.Stream;

public class SNClient
{
    private String nsEnvironment;
    private String nsUserName;
    RestServiceController snRestService = new RestServiceController();
    ProjectSettingsController settings;

    public SNClient(String environment, String userName)
    {
        this.nsEnvironment = environment;
        this.nsUserName = userName;

        // In order to use SSL forwarding for SOAP messages. Refer to FAQ for details
        System.setProperty("axis.socketSecureFactory", "org.apache.axis.components.net.SunFakeTrustSocketFactory");
    }

    public JSONObject authenticateApi(String userName, String password, String url)
    {
        JSONObject returnObj = new JSONObject();
        try
        {
            url += "/api/now/table/sys_properties?sysparm_query=name%3Dglide.appcreator.company.code&sysparm_limit=1&sysparm_limit=1";
            String response = snRestService.Get(userName, password, url);

            JSONController jsonController = new JSONController(response);
            JSONObject resultObj = jsonController.getFirstResult();
            if (resultObj != null)
            {
                returnObj.put("success", true);
                returnObj.put("companyCode", resultObj.getString("value"));
                return returnObj;
            }
            else if (jsonController.errorObj != null)
            {
                return jsonController.getError();
            }
            else
            {
                return returnObj.put("success", false);
            }
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public String downloadTable(FileParser fp, String password) throws RemoteException
    {
        try
        {
            String url = getFileUrl(fp);
            String response = snRestService.Get(this.nsUserName, password, url);
            JSONObject obj = (JSONObject) (getJsonObjectApi(response).get(0));
            return obj.getString("script");
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public String downloadFile(String fileData, Project project) throws RemoteException
    {
        try
        {
            FileParser fp = new FileParser(fileData);
            settings = new ProjectSettingsController(project);
            String password = settings.getProjectPassword();

            if (fp.Table.equals("sys_metadata"))
            {

            }

            String url = getFileUrl(fp);
            String response = snRestService.Get(this.nsUserName, password, url);
            JSONObject obj = (JSONObject) (getJsonObjectApi(response).get(0));
            return obj.getString(fp.getScriptColumn());
//            return obj.getString("script");
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private static String readLineByLineJava8(String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    public String getFileUrl(FileParser fp) throws UnsupportedEncodingException
    {
        String url = this.nsEnvironment + "/api/now/table/" + fp.Table;
//        "sysparm_query=sys_scope=8d6379554f812300855601bda310c70e^sys_class_name!=sys_metadata_delete^sys_nameLIKEtruck^ORsys_update_nameLIKEtruck"
//        String url = "sys_scope.nameSTARTSWITHApplication^sys_class_name!=sys_metadata_delete^sys_nameSTARTSWITHx_174422_vehicle_truck";
        String queryParam = "?sysparm_query=sys_scope.name=" + URLEncoder.encode(fp.AppName + "^sys_name=" + fp.FileName, "UTF-8");
        url += queryParam;
        return url;
    }

    public String getTableUrl(FileParser fp) throws UnsupportedEncodingException
    {
        String url = this.nsEnvironment + "/api/now/table/" + fp.Table;
        String queryParam = "?sysparm_query=sys_scope.name=" + URLEncoder.encode(fp.AppName + "^sys_name=" + fp.FileName, "UTF-8");
        url += queryParam;
        return url;
    }

    public String getFileIdUrl(FileParser fp) throws UnsupportedEncodingException
    {
        String url = this.nsEnvironment + "/api/now/table/" + fp.Table;
        String queryParam = "?sysparm_query=sys_scope.name=" + URLEncoder.encode(fp.AppName + "^sys_name=" + fp.FileName, "UTF-8");
        String fields = "&sysparm_fields=sys_id";
        url += queryParam + fields;

        return url;
    }

    public String getAppUrl(FileParser fp) throws UnsupportedEncodingException
    {
        String url = this.nsEnvironment + "/api/now/table/sys_app";
        String queryParam = "?sysparm_query=name=" + URLEncoder.encode(fp.AppName, "UTF-8");
        String fields = "&sysparm_fields=sys_id";
        url += queryParam + fields;

        return url;
    }

    public String uploadFileUrl(FileParser fp, String sys_id) throws UnsupportedEncodingException
    {
        String url = this.nsEnvironment + "/api/now/table/" + fp.Table;
        if (!sys_id.isEmpty())
        {
            url += "/" + sys_id;
        }

        return url;
    }

    public String uploadAppUrl() throws UnsupportedEncodingException
    {
        String url = this.nsEnvironment + "/api/now/table/sys_app";
        return url;
    }

    public String getAppID(FileParser fp, String fileData, String password, String companyCode) throws JSONException, UnsupportedEncodingException
    {
        String appUrl = getAppUrl(fp);
//        String appSysId = checkIfAppScopeExists(password, appUrl).getString("sys_id");
        String appSysId = getObjectFromSN(password, appUrl).getString("sys_id");
        if (appSysId.isEmpty())
        {
            appSysId = AddApp(fileData, password, fp, companyCode);
        }
        return appSysId;
    }

    public boolean startUploadFile(VirtualFile file, Project project) throws RemoteException
    {
        try
        {
            String fileData = readLineByLineJava8(file.getPath());
            settings = new ProjectSettingsController(project);
            String password = settings.getProjectPassword();
            FileParser fp = new FileParser(fileData);
            String url = getFileIdUrl(fp);

            String appSysId = getAppID(fp, fileData, password, settings.getCompanyCode());

            JSONObject obj = checkIfFileExists(password, url);

            String sys_id = obj.getString("sys_id");
            if (!sys_id.isEmpty())
            {
                return ModifyFile(fileData, password, fp, sys_id, appSysId);
            }
            else
            {
                return AddFile(fileData, password, fp, sys_id, settings.getCompanyCode(), appSysId);
            }
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    private String GetAppPrefix(String companyCode)
    {
        return "x_" + companyCode + "_";
    }

    private Boolean AddFile(String fileData, String password, FileParser fp, String sys_id, String companyCode, String appSysId) throws UnsupportedEncodingException, JSONException
    {
        String url;
        String response;
        JSONObject obj;
        url = uploadFileUrl(fp, sys_id);

        String scope = GetAppPrefix(companyCode) + fp.AppName + "_" + fp.FileName;

        JSONObject sendObj = new JSONObject();
        sendObj.put(fp.getScriptColumn(), fileData);
        sendObj.put("source", scope);
        sendObj.put("scope", scope);
        sendObj.put("name", fp.FileName);
        sendObj.put("runtime_access_tracking", "permissive");
        sendObj.put("sys_package", appSysId);
        sendObj.put("sys_scope", appSysId);

        response = snRestService.Post(this.nsUserName, password, url, sendObj);
        return true;
//        return Boolean.parseBoolean(response);
    }

    private String AddApp(String fileData, String password, FileParser fp, String companyCode) throws UnsupportedEncodingException, JSONException
    {
        String url;
        String response;
        String appId = "";
        JSONObject obj;
        url = uploadAppUrl();

        String scope = GetAppPrefix(companyCode) + fp.AppName + "_" + fp.FileName;

        JSONObject sendObj = new JSONObject();
        sendObj.put(fp.getScriptColumn(), fileData);
        sendObj.put("source", scope);
        sendObj.put("scope", scope);
        sendObj.put("name", fp.AppName);
        sendObj.put("runtime_access_tracking", "permissive");

        response = snRestService.Post(this.nsUserName, password, url, sendObj);
        appId = new JSONController(response).getFirstResult().getString("sys_id");
        return appId;
//        if (getJsonObjectApi(response).length() >= 1)
//        {
//            obj = (JSONObject) (getJsonObjectApi(response).get(0));
//            appId = obj.getString("sys_id");
//        }
//        return appId;
    }

    private boolean ModifyFile(String fileData, String password, FileParser fp, String sys_id, String appSysId) throws UnsupportedEncodingException, JSONException
    {
        String url;
        String response;
        JSONObject obj;
        url = uploadFileUrl(fp, sys_id);

        JSONObject sendObj = new JSONObject();
        sendObj.put(fp.getScriptColumn(), fileData);

        response = snRestService.Put(this.nsUserName, password, url, sendObj);
        if (response.equals("true"))
        {
            return true;
        }
        return false;
    }

    private JSONObject checkIfFileExists(String password, String url) throws JSONException
    {
        String response = snRestService.Get(this.nsUserName, password, url);
        if (getJsonObjectApi(response).length() >= 1)
        {
            return (JSONObject) (getJsonObjectApi(response).get(0));
        }
        else
        {
            return new JSONObject().put("sys_id", "");
        }
    }

    private JSONObject getObjectFromSN(String password, String url) throws JSONException
    {
        String response = snRestService.Get(this.nsUserName, password, url);
        JSONObject myObj;
        if (getJsonObjectApi(response).length() >= 1)
        {
            myObj = (JSONObject) (getJsonObjectApi(response).get(0));
        }
        else
        {
            myObj = new JSONObject().put("sys_id", "");
        }
        return myObj;

    }

    private JSONObject checkIfAppScopeExists(String password, String url) throws JSONException
    {
        String response = snRestService.Get(this.nsUserName, password, url);
        if (getJsonObjectApi(response).length() >= 1)
        {
            return (JSONObject) (getJsonObjectApi(response).get(0));
        }
        else
        {
            return new JSONObject().put("sys_id", "");
        }
    }

    private JSONArray getJsonObjectApi(String response) throws JSONException
    {
        JSONObject obj = new JSONObject(response);
        JSONArray jsonObj = obj.getJSONArray("result");
        return jsonObj;
    }

//    private JSONObject getJsonObject(String response) throws JSONException
//    {
//        JSONObject obj = new JSONObject(response);
//        JSONObject jsonObj = obj.getJSONObject("result");
//        return jsonObj;
//    }
//
//    private byte[] loadFile(String sFileName)
//    {
//        InputStream inFile = null;
//        byte[] data = null;
//
//        try
//        {
//            File file = new File(sFileName);
//            inFile = new FileInputStream(file);
//            data = new byte[(int) file.length()];
//            inFile.read(data, 0, (int) file.length());
//            inFile.close();
//        }
//        catch (Exception ex)
//        {
//            return null;
//        }
//
//        return data;
//    }

//    public String searchFile(String fileName, String parentFolderId, String projectSettingsRootFolderId) throws RemoteException {
//        RecordRef parentFolderRef = new RecordRef();
//        parentFolderRef.setInternalId(parentFolderId);
//
//        RecordRef[] rr = new RecordRef[1];
//        rr[0] = parentFolderRef;
//
//        SearchMultiSelectField smsf = new SearchMultiSelectField();
//        smsf.setSearchValue(rr);
//        smsf.setOperator(SearchMultiSelectFieldOperator.anyOf);
//
//        SearchStringField nameField = new SearchStringField();
//        nameField.setOperator(SearchStringFieldOperator.is);
//        nameField.setSearchValue(fileName);
//
//        FileSearchBasic fileSearchBasic = new FileSearchBasic();
//        fileSearchBasic.setFolder(smsf);
//        fileSearchBasic.setName(nameField);
//
//        SearchResult results = null;
//
//        try {
//            results = _port.search(fileSearchBasic);
//        } catch (Exception ex) {
//            return null;
//        }
//
//        if (results != null && results.getStatus().isIsSuccess()) {
//            RecordList myRecordlist = results.getRecordList();
//
//            if (myRecordlist != null && myRecordlist.getRecord() != null) {
//                File foundFile = null;
//
//                if (parentFolderId.equals(projectSettingsRootFolderId)) {
//                    foundFile = (File) myRecordlist.getRecord(results.getTotalRecords()-1);
//                } else {
//                    foundFile = (File) myRecordlist.getRecord(0);
//                }
//
//                if (foundFile != null) {
//                    return foundFile.getInternalId();
//                }
//            }
//        }
//
//        return null;
//    }


    //    private Passport createPassport() {
//        RecordRef role = new RecordRef();
//        role.setInternalId(this.nsAccount.getRoleId());
//
//        Passport passport = new Passport();
//        passport.setEmail(this.nsAccount.getAccountEmail());
//        passport.setPassword(this.nsAccount.getAccountPassword());
//        passport.setAccount(this.nsAccount.getAccountId());
//        passport.setRole(role);
//        return passport;
//    }
//
//    public void tryToLogin() throws RemoteException
//    {
////        Passport passport = createPassport();
////        Status status = (_port.login(passport)).getStatus();
////
////        if (!status.isIsSuccess()) {
////            throw new IllegalStateException(new Throwable("Service Now login request call was unsuccessful."));
////        }
//    }

//    public String authenticate(String userName, String password, String url) throws RemoteException
//    {
//        try
//        {
//            SendObject sObj = new SendObject("authenticate", "");
//            String response = snRestService.getNSAccounts(userName, password, url, sObj);
//
////            JSONObject resultObj = new JSONObject(response);
//
//            // print result
//            return getJsonObject(response).getString("success");
//
//        }
//        catch (Exception ex)
//        {
//            return null;
//        }
//    }
//    public String downloadFileOld(String fileData, Project project) throws RemoteException
//    {
//        try
//        {
//            settings = new ProjectSettingsController(project);
//            String password = settings.getProjectPassword();
//
//            SendObject sObj = new SendObject("compare", fileData);
//            String response = snRestService.getNSAccounts(this.nsUserName, password, this.nsEnvironment, sObj);
//
////            File myF = new File(getJsonObject(response).getString("fileData"), "test.js");
//
////            PrintWriter writer = new PrintWriter(response, "UTF-8");
////            writer.println(response);
////            writer.close();
//
////            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(myF), "UTF8"));
////            String str;
////
////            while ((str = in.readLine()) != null)
////            {
////                System.out.println(str);
////            }
////
////            in.close();
//
////            String s = new String(myF.toString(), StandardCharsets.UTF_8)
////            String d = new String(myF.getCon.getContent(), StandardCharsets.UTF_8)
//
////            final DiffContent remoteFileContent = DiffContentFactory.getInstance().create(new String(remoteFile.getContent(), StandardCharsets.UTF_8));
////            JSONObject jsnobject = new JSONObject(myF.toString());
////            JSONArray jsonArray = jsnobject.getJSONArray();
////            for (int i = 0; i < jsonArray.length(); i++)
////            {
////                JSONObject explrObject = jsonArray.getJSONObject(i);
////            }
//
////            JSONObject resultObj = new JSONObject(response);
//
//            // print result
////            return new File(getJsonObject(response).getString("fileData"));
//            return getJsonObject(response).getString("fileData");
//        }
//        catch (Exception ex)
//        {
//            return null;
//        }
//    }
//    public JSONObject uploadFileOld(VirtualFile file, Project project) throws RemoteException
//    {
//        try
//        {
//            File vFile = new File(file.getPath());
//            String fileData = readLineByLineJava8(vFile.getPath());
////            String fileData = loadFile(vFile.getName()).toString();
//            settings = new ProjectSettingsController(project);
//            String password = settings.getProjectPassword();
//
//            SendObject sObj = new SendObject("upload", fileData);
//            String response = snRestService.getNSAccounts(this.nsUserName, password, this.nsEnvironment, sObj);
//
//            return getJsonObject(response);
//
//            // print result
////            return getJsonData(resultObj, "result");
//
//        }
//        catch (Exception ex)
//        {
//            return null;
//        }
//    }
}
