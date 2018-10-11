package serviceNow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import projectsettings.ProjectSettingsController;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

//import jdk.nashorn.internal.parser.JSONParser;
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;

public class SNClient
{
    private NSAccount nsAccount;

    private String nsEnvironment;
    private String nsUserName;
    NSRolesRestServiceController nsRolesRestServiceController = new NSRolesRestServiceController();
    ProjectSettingsController settings;

    public SNClient(String environment, String userName)
    {
//        this.nsAccount = account;
        this.nsEnvironment = environment;
        this.nsUserName = userName;

        String nsWebServiceURL = environment;
//        nsWebServiceURL = this.nsAccount.getProductionWebServicesDomain();

        // In order to use SSL forwarding for SOAP messages. Refer to FAQ for details
        System.setProperty("axis.socketSecureFactory", "org.apache.axis.components.net.SunFakeTrustSocketFactory");
    }

    public NSAccount getNSAccount()
    {
        return this.nsAccount;
    }

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
    public void tryToLogin() throws RemoteException
    {
//        Passport passport = createPassport();
//        Status status = (_port.login(passport)).getStatus();
//
//        if (!status.isIsSuccess()) {
//            throw new IllegalStateException(new Throwable("Netsuite SuiteTalk login request call was unsuccessful."));
//        }
    }

    public String authenticate(String userName, String password, String url) throws RemoteException
    {
        try
        {
            SendObject sObj = new SendObject("authenticate", "");
            String response = nsRolesRestServiceController.getNSAccounts(userName, password, url, sObj);

//            JSONObject jsonObj = new JSONObject(response);

            // print result
            return getJsonObject(response).getString("success");

        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public static void writeFile1() throws IOException
    {
        File fout = new File("out.txt");
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(fout);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for (int i = 0; i < 10; i++)
        {
            bw.write("something");
            bw.newLine();
        }

        bw.close();
    }

    public Boolean authenticateApi(String userName, String password, String url) throws RemoteException
    {
        try
        {
            SendObject sObj = new SendObject("authenticate", "");
            url += "/api/now/table/sys_db_object?sysparm_limit=1";
            String response = nsRolesRestServiceController.getNSAccountsApi(userName, password, url, sObj);

//            JSONObject jsonObj = new JSONObject(response);

            // print result
            if(getJsonObjectApi(response).length() >= 1)
            {
                return true;
            }
            else
            {
                return false;
            }

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

            SendObject sObj = new SendObject("compare", fileData);
            String url = this.nsEnvironment + "/api/now/table/" + fp.Table;
            String queryParam = "?sysparm_query=sys_scope.name=" + URLEncoder.encode(fp.AppName + "^sys_name=" + fp.FileName, "UTF-8");
            url += queryParam;
            String response = nsRolesRestServiceController.getNSAccountsApi(this.nsUserName, password, url, sObj);
            JSONObject obj = (JSONObject)(getJsonObjectApi(response).get(0));
            return obj.getString("script");
//            return getJsonObjectApi(response).getString(0);//.getString("fileData");
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public String downloadFileOld(String fileData, Project project) throws RemoteException
    {
        try
        {
            settings = new ProjectSettingsController(project);
            String password = settings.getProjectPassword();

            SendObject sObj = new SendObject("compare", fileData);
            String response = nsRolesRestServiceController.getNSAccounts(this.nsUserName, password, this.nsEnvironment, sObj);

//            File myF = new File(getJsonObject(response).getString("fileData"), "test.js");

//            PrintWriter writer = new PrintWriter(response, "UTF-8");
//            writer.println(response);
//            writer.close();

//            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(myF), "UTF8"));
//            String str;
//
//            while ((str = in.readLine()) != null)
//            {
//                System.out.println(str);
//            }
//
//            in.close();

//            String s = new String(myF.toString(), StandardCharsets.UTF_8)
//            String d = new String(myF.getCon.getContent(), StandardCharsets.UTF_8)

//            final DiffContent remoteFileContent = DiffContentFactory.getInstance().create(new String(remoteFile.getContent(), StandardCharsets.UTF_8));
//            JSONObject jsnobject = new JSONObject(myF.toString());
//            JSONArray jsonArray = jsnobject.getJSONArray();
//            for (int i = 0; i < jsonArray.length(); i++)
//            {
//                JSONObject explrObject = jsonArray.getJSONObject(i);
//            }

//            JSONObject jsonObj = new JSONObject(response);

            // print result
//            return new File(getJsonObject(response).getString("fileData"));
            return getJsonObject(response).getString("fileData");
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

    public JSONObject uploadFile(VirtualFile file, Project project) throws RemoteException
    {
        try
        {
            File vFile = new File(file.getPath());
            String fileData = readLineByLineJava8(vFile.getPath());
//            String fileData = loadFile(vFile.getName()).toString();
            settings = new ProjectSettingsController(project);
            String password = settings.getProjectPassword();

            SendObject sObj = new SendObject("upload", fileData);
            String response = nsRolesRestServiceController.getNSAccounts(this.nsUserName, password, this.nsEnvironment, sObj);

            return getJsonObject(response);

            // print result
//            return getJsonData(jsonObj, "result");

        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private JSONArray getJsonObjectApi(String response) throws JSONException
    {
        JSONObject obj = new JSONObject(response);
        JSONArray jsonObj = obj.getJSONArray("result");
        return jsonObj;
    }

    private JSONObject getJsonObject(String response) throws JSONException
    {
        JSONObject obj = new JSONObject(response);
        JSONObject jsonObj = obj.getJSONObject("result");
        return jsonObj;
    }

    private byte[] loadFile(String sFileName)
    {
        InputStream inFile = null;
        byte[] data = null;

        try
        {
            File file = new File(sFileName);
            inFile = new FileInputStream(file);
            data = new byte[(int) file.length()];
            inFile.read(data, 0, (int) file.length());
            inFile.close();
        }
        catch (Exception ex)
        {
            return null;
        }

        return data;
    }

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
}
