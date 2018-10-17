package ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.codehaus.jettison.json.JSONObject;
import projectsettings.ProjectSettingsController;
import serviceNow.RestServiceController;
import serviceNow.SNClient;

import javax.swing.*;
import java.awt.event.*;

public class CredentialsUI extends JDialog
{
    private JPanel contentPane;
    private JButton nextButton;
    private JButton cancelButton;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel passwordLabel;
    private JLabel emailLabel;
    private JLabel environmentLabel;
    private JTextField txtUrl;
    private Project project;
    private serviceNow.SNClient SNClient;

    public CredentialsUI(Project project)
    {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(nextButton);

        this.SNClient = SNClient;
        this.project = project;
//        this.url = txtUrl.getText();

        ProjectSettingsController projectSettingsController = new ProjectSettingsController(this.project);

        if (projectSettingsController.hasAllProjectSettings())
        {
            String password = projectSettingsController.getProjectPassword();
            if (password != null && !password.isEmpty())
            {
                emailField.setText(projectSettingsController.getUserName());
                txtUrl.setText(projectSettingsController.getUrl());
                passwordField.setText(password);
            }
        }

        nextButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onNext();
            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void saveProjectSettings(String companyCode)
    {
        String email = emailField.getText();
        String url = txtUrl.getText();
        String password = String.valueOf(passwordField.getPassword());

        ProjectSettingsController projectSettingsController = new ProjectSettingsController(this.project);
        projectSettingsController.setUserName(emailField.getText());
        projectSettingsController.setURL(txtUrl.getText());
        projectSettingsController.saveProjectPassword(email, password, url);
        projectSettingsController.setCompanyCode(companyCode);
    }

    private void onNext()
    {
        if (emailField.getText().isEmpty() || String.valueOf(passwordField.getPassword()).isEmpty() || txtUrl.getText().isEmpty())
        {
            JOptionPane.showMessageDialog(null, "Email, Password and Environment are required", "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }
//test
        this.setVisible(false);


        RestServiceController restServiceController = new RestServiceController();
        JSONObject authResults;
        Boolean success = false;
        String companyCode = "";
        String error = "";
        try
        {
            this.SNClient = new SNClient(txtUrl.getText(), emailField.getText());
            authResults = this.SNClient.authenticateApi(emailField.getText(), String.valueOf(passwordField.getPassword()), txtUrl.getText());
            if(authResults != null)
            {
                success = (Boolean)authResults.get("success");
                if(authResults.has("companyCode"))
                {
                    companyCode = authResults.getString("companyCode");
                }
                else if(authResults.has("error"))
                {
                    error = authResults.getString("error");
                }
            }
        }
        catch(Exception e)
        {
            authResults = null;
        }

        if (authResults == null || success.equals(false))
        {
//            JOptionPane.showMessageDialog(null, "Error getting Service Now Accounts from Roles Rest Service.\nPlease verify that your e-mail and password are correct.", "ERROR", JOptionPane.ERROR_MESSAGE);
            JOptionPane.showMessageDialog(null, "Error authenticating Service Now Credentials. Please check your configuration and try again.\n" +
                    error, "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            saveProjectSettings(companyCode);
            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder("<h3>Service Now Project Settings Updated!</h3>", MessageType.INFO, null)
                    .setFadeoutTime(3000)
                    .createBalloon()
                    .show(RelativePoint.getNorthEastOf(WindowManager.getInstance().getIdeFrame(project).getComponent()),
                            Balloon.Position.above);
        }

        dispose();
    }

    private void onCancel()
    {
        dispose();
    }
}
