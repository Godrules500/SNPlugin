package projectsettings;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

public class ProjectSettingsController
{

    final private String PROJECT_SETTING_EMAIL = "nsProjectEmail";
    final private String PROJECT_SETTING_ENVIRONMENT = "nsEnvironment";
    final private String PROJECT_SETTING_COMPANYCODE = "nsCompanyCode";

    private final PropertiesComponent propertiesComponent;

    public ProjectSettingsController(Project project)
    {
        this.propertiesComponent = PropertiesComponent.getInstance(project);
    }

    public String getUserName()
    {
        return propertiesComponent.getValue(PROJECT_SETTING_EMAIL);
    }

    public void setUserName(String nsEmail)
    {
        if (nsEmail != null && !nsEmail.isEmpty())
        {
            propertiesComponent.setValue(PROJECT_SETTING_EMAIL, nsEmail);
        }
    }

    public void setCompanyCode(String companyCode)
    {
        if (companyCode != null && !companyCode.isEmpty())
        {
            propertiesComponent.setValue(PROJECT_SETTING_COMPANYCODE, companyCode);
        }
    }

    public String getCompanyCode()
    {
        return propertiesComponent.getValue(PROJECT_SETTING_COMPANYCODE);
    }

    public String getUrl()
    {
        return propertiesComponent.getValue(PROJECT_SETTING_ENVIRONMENT);
    }

    public void setNsEnvironment(String nsEnvironment)
    {
        if (nsEnvironment != null && !nsEnvironment.isEmpty())
        {
            propertiesComponent.setValue(PROJECT_SETTING_ENVIRONMENT, nsEnvironment);
        }
    }

    public void saveProjectPassword(String email, String password, String url)
    {
        if (!email.isEmpty() && !password.isEmpty())
        {
            CredentialAttributes attributes = new CredentialAttributes(email + ":" + url, email, this.getClass(), false);
            Credentials saveCredentials = new Credentials(email, password);
            PasswordSafe.getInstance().set(attributes, saveCredentials);
        }
    }

    public String getProjectPassword()
    {
        CredentialAttributes attributes = new CredentialAttributes(this.getUserName() + ":" + this.getUrl(), this.getUserName(), this.getClass(), false);
        return PasswordSafe.getInstance().getPassword(attributes);
    }

    public boolean hasAllProjectSettings()
    {
        return (getUserName() != null && !getUserName().isEmpty() &&
                getUrl() != null && !getUrl().isEmpty());
    }
}
