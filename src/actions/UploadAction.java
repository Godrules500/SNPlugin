package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.util.ProgressWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.RunnableBackgroundableWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import projectsettings.ProjectSettingsController;
import serviceNow.SNClient;
import tasks.UploadTask;

import javax.swing.*;

public class UploadAction extends AnAction
{

    @Override
    public void update(AnActionEvent e)
    {
        final Project project = e.getProject();
        ProjectSettingsController projectSettingsController = new ProjectSettingsController(project);
        e.getPresentation().setVisible(projectSettingsController.hasAllProjectSettings());
        e.getPresentation().setEnabled(projectSettingsController.hasAllProjectSettings());
    }

    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final Project project = e.getProject();
        final VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);

        ProjectSettingsController projectSettingsController = new ProjectSettingsController(project);

        String currentEnvironment = projectSettingsController.getUrl();
        String currentUser = projectSettingsController.getUserName();

//        NSAccount nsAccount = projectSettingsController.getNSAccountForProject();
//
//        if (nsAccount == null)
//        {
//            return;
//        }

        final SNClient SNClient;

        try
        {
            SNClient = new SNClient(currentEnvironment, currentUser);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null, "Error creating SNClient", "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }

        RunnableBackgroundableWrapper wrapper = new RunnableBackgroundableWrapper(e.getProject(), "", new UploadTask(project, files, SNClient));
        ProgressWindow progressIndicator = new ProgressWindow(true, project);
        progressIndicator.setIndeterminate(true);

        ProgressManager.getInstance().runProcessWithProgressAsynchronously(wrapper, progressIndicator);
    }
}