/**
 * @SNType sys_script_include
 * @SNFileID 622e0c524fb42300855601bda310c77e
 * @SNScope x_osmo2_my_test_gi
 * @SNName test_inc
 */
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
import controller.SNClient;
import tasks.CompareWithFileCabinetTask;

import javax.swing.*;

public class CompareWithFileCabinetAction extends AnAction
{

    @Override
    public void update(AnActionEvent e)
    {
        final Project project = e.getProject();
        final VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        ProjectSettingsController projectSettingsController = new ProjectSettingsController(project);
        e.getPresentation().setVisible(file != null && !file.isDirectory() && projectSettingsController.hasAllProjectSettings());
        e.getPresentation().setEnabled(file != null && !file.isDirectory() && projectSettingsController.hasAllProjectSettings());
    }

    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final Project project = e.getProject();
        final VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);

        ProjectSettingsController projectSettingsController = new ProjectSettingsController(project);

        String currentEnvironment = projectSettingsController.getUrl();
        String currentUserName = projectSettingsController.getUserName();

        final SNClient SNClient;

        try
        {
            SNClient = new SNClient(currentEnvironment, currentUserName);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null, "Error creating SNClient", "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String s = "";

        RunnableBackgroundableWrapper wrapper = new RunnableBackgroundableWrapper(e.getProject(), "", new CompareWithFileCabinetTask(project, files, SNClient, projectSettingsController));
        ProgressWindow progressIndicator = new ProgressWindow(true, project);
        progressIndicator.setIndeterminate(true);

        ProgressManager.getInstance().runProcessWithProgressAsynchronously(wrapper, progressIndicator);
    }
}
