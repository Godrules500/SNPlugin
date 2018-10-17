package serviceNow;

public class FileParser
{
    public String FileName;
    public String AppName;
    public String ApiName;
    public String FileID;
    public String Table;
    public String SNScriptType = "";
    public String UpdateName = "";

    public FileParser(String fileData)
    {
        getJSDocs(fileData);
    }

    /**
     * Read and parse the JSDocs
     * @param fileData
     */
    private void getJSDocs(String fileData)
    {
        int start = fileData.indexOf("/**") + 3;
        int end = fileData.indexOf("*/") - 1;
        String[] settings = fileData.substring(start, end).split("\\*");

        for (int i = 0; i < settings.length; i++)
        {
            String setting = settings[i].trim();
            if (!setting.isEmpty())
            {
                String col = setting.split(" ")[0];
                String val = setting.split(" ")[1];
                if (col.equals("@SNFileID"))
                {
                    FileID = val;
                }
                else if (col.equals("@SNType"))
                {
                    Table = val;
                }
                else if (col.equals("@SNApiName"))
                {
                    ApiName = val;
                }
                else if (col.equals("@SNApp"))
                {
                    AppName = val;
                }
                else if (col.equals("@SNName"))
                {
                    FileName = val;
                }
                else if (col.equals("@SNScriptType"))
                {
                    SNScriptType = val;
                }
                else if (col.equals("@SNUpdateName"))
                {
                    UpdateName = val;
                }
            }
        }
    }

    /**
     * Get the column that holds the script code.
     * @return name of script column
     */
    public String getScriptColumn()
    {
        switch (SNScriptType.toUpperCase())
        {
            case "CLIENT":
            {
                return "client_script";
            }
            case "HTML":
            {
                return "template";
            }
            case "CSS":
            {
                return "css";
            }
            case "CONTROLLER":
            {
                return "controller_as";
            }
            case "XML":
            {
                return "xml";
            }
            case "REST":
            {
                return "operation_script";
            }
            default:
            {
                return "script";
            }
        }
    }
}