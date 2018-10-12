package ui;

import com.intellij.openapi.project.Project;
import projectsettings.ProjectSettingsController;

import javax.swing.*;
import java.awt.event.*;

public class ProjectSettingsUI extends JDialog {

    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField txtUrl;
    private JTextField txtUserName;
    private JLabel lblUrl;

    private JLabel lblUserName;
    private JPanel contentPane;
    private JTextField txtCompanyCode;

    private Project project;

    public ProjectSettingsUI(Project project) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        this.project = project;

        setProjectSettingsUIFields();

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void setProjectSettingsUIFields() {
        ProjectSettingsController projectSettingsController = new ProjectSettingsController(this.project);
        this.txtUrl.setText(projectSettingsController.getUrl());
        this.txtUserName.setText(projectSettingsController.getUserName());
        this.txtCompanyCode.setText(projectSettingsController.getCompanyCode());
    }
}
