package tasks;

import actions.ProjectHelper;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.diff.util.Side;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import projectsettings.ProjectSettingsController;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;


import com.intellij.ide.highlighter.ArchiveFileType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompareWithFileCabinetTask implements Runnable
{
    private Project project;
    private VirtualFile[] projectFiles;
    private ProjectHelper projectHelper = new ProjectHelper();
    private serviceNow.SNClient SNClient;
    private ProjectSettingsController projectSettingsController;
    private DecimalFormat decimalFormat = new DecimalFormat("##.##");

    public CompareWithFileCabinetTask(Project project, VirtualFile[] files, serviceNow.SNClient SNClient, ProjectSettingsController projectSettingsController)
    {
        this.project = project;
        this.projectFiles = files;
        this.SNClient = SNClient;
        this.projectSettingsController = projectSettingsController;
    }

    @Override
    public void run()
    {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Preparing to compare selected file(s) to ServiceNow")
        {
            public void run(final ProgressIndicator progressIndicator)
            {
                try
                {
                    ArrayList<String> fileIds = getSelectedFileIds(projectHelper.getProjectRootDirectory(project), projectFiles, SNClient, projectSettingsController);

                    if (fileIds == null || fileIds.isEmpty())
                    {
                        JOptionPane.showMessageDialog(null, "The Selected file does not exists. Please check the file settings.", "ERROR", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    ArrayList<String> files = downloadFiles(fileIds, progressIndicator);

                    if (files == null || files.isEmpty() || files.size() != projectFiles.length)
                    {
                        JOptionPane.showMessageDialog(null, "The selected file does not exists", "ERROR", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    progressIndicator.setFraction(0);
                    progressIndicator.setText("Showing diffs");
                    for (String file : files)
                    {
                        showDiffForFiles(file, getLocalFile(file));
//                        actionPerformed(file, getLocalFile(file));
//                        CompareFilesAction ca = new CompareFilesAction();
//                        ca.getDiffRequest()
                    }
                    progressIndicator.setFraction(1);
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(null, "Exception: " + ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    @NotNull
    public static String read(@NotNull VirtualFile file) throws IOException
    {
        InputStreamReader reader = new InputStreamReader(file.getInputStream());
        BufferedReader bufReader = new BufferedReader(reader);
        StringBuilder builder = new StringBuilder();
        String line = bufReader.readLine();
        while (line != null)
        {
            builder.append(line).append("\n");
            line = bufReader.readLine();
        }
        return builder.toString();
    }

    private ArrayList<String> getSelectedFileIds(String projectBaseDirectory, VirtualFile[] files, serviceNow.SNClient SNClient, ProjectSettingsController projectSettingsController)
    {
        if (files == null || files.length == 0)
        {
            return null;
        }


        ArrayList<String> fileIds = new ArrayList<String>();
        ArrayList<String> liFileData = new ArrayList<String>();


        for (VirtualFile file : files)
        {
//            final DiffContent localFileContent = DiffContentFactory.getInstance().create(project, file);
            if (file.isDirectory())
            {
                getSelectedFileIds(projectBaseDirectory, file.getChildren(), SNClient, projectSettingsController);
            }
            else
            {
                String projectFilePathFromRootDirectory = projectHelper.getProjectFilePathFromRootDirectory(file, projectBaseDirectory);

                if (projectFilePathFromRootDirectory == null)
                {
                    return null;
                }

                //-------------------------------- try above here ------------------------------------
                String[] foldersAndFile = projectFilePathFromRootDirectory.split("/");
//                String currentParentFolder = projectSettingsController.getNsRootFolder();

                for (int i = 0; i < foldersAndFile.length; i++)
                {
                    if ((i + 1) != foldersAndFile.length)
                    {
                        try
                        {
//                            String folderId = SNClient.searchFolder(foldersAndFile[i], currentParentFolder);
//
//                            if (folderId == null)
//                            {
//                                return null;
//                            }
//
//                            currentParentFolder = folderId;
                        }
                        catch (Exception ex)
                        {
                            JOptionPane.showMessageDialog(null, "Exception searching for Folder in Service Now File Cabinet: " + ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                    }
                    else
                    {
                        try
                        {
                            liFileData.add(read(file));
//                            if (currentParentFolder != null)
//                            {
//                                String fileId = SNClient.searchFile(foldersAndFile[i], currentParentFolder, projectSettingsController.getNsRootFolder());
//
//                                if (fileId != null)
//                                {
//                                    fileIds.add(fileId);
//                                }
//                            }
                        }
                        catch (Exception ex)
                        {
                            JOptionPane.showMessageDialog(null, "Exception searching for File in Service Now File Cabinet: " + ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                    }
                }
            }
        }

        return liFileData;
//        return fileIds;
    }

    private ArrayList<String> downloadFiles(ArrayList<String> fileIds, ProgressIndicator progressIndicator)
    {
        ArrayList<String> files = new ArrayList<String>();
        progressIndicator.setText("Downloading selected file(s) from Service Now File Cabinet");

        double numberOfFilesToDownload = fileIds.size();
        double currentFileNumber = 1;

        for (String fileId : fileIds)
        {
            progressIndicator.setFraction(currentFileNumber / numberOfFilesToDownload);
            progressIndicator.setText("Downloading selected file(s) from Service Now File Cabinet: " + decimalFormat.format((currentFileNumber / numberOfFilesToDownload) * 100) + "% Complete");
            try
            {
                String downloadedFile = SNClient.downloadFile(fileId, this.project);

                if (downloadedFile != null)
                {
                    files.add(downloadedFile);
                }
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(null, "Exception downloading file from Service Now File Cabinet: " + ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
            currentFileNumber++;
        }

        progressIndicator.setFraction(1);
        return files;
    }

    private VirtualFile getLocalFile(String remoteFileName)
    {
        for (VirtualFile file : projectFiles)
        {
            return file;
//            if (remoteFileName.equals(file.getName()))
//            {
//                return file;
//            }
        }

        return null;
    }

    private void showDiffForFiles(String remoteFile, VirtualFile localFile) throws IOException
    {
        if (remoteFile == null || localFile == null)
        {
            return;
        }

//        String s = new String(localFile.contentsToByteArray());
//        DocumentContent content1 = DiffContentFactory.getInstance().create(remoteFile, localFile.getFileType());
//        DocumentContent content2 = DiffContentFactory.getInstance().create(s, localFile);
//        SimpleDiffRequest request = new SimpleDiffRequest("Window Title", content1, content2, "Title 1", "Title 2");
//
//
//        ApplicationManager.getApplication().invokeLater(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                ApplicationManager.getApplication().runWriteAction(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        DiffManager.getInstance().showDiff(project, request);
//                    }
//                });
//            }
//        }, ModalityState.NON_MODAL);

        final DiffContent remoteFileContent = DiffContentFactory.getInstance().create(remoteFile);
//        final DiffContent remoteFileContent = DiffContentFactory.getInstance().create(new String(remoteFile.getContent(), StandardCharsets.UTF_8));

        final DiffContent localFileContent = DiffContentFactory.getInstance().create(project, localFile);

        DiffRequest dr = new SimpleDiffRequest("Service Now File Cabinet Compare", remoteFileContent, localFileContent, "Service NowFile Cabinet - " + localFile.getName(), "Local File - " + localFile.getName());
        ApplicationManager.getApplication().invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                ApplicationManager.getApplication().runWriteAction(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        DiffManager.getInstance().showDiff(project, dr);
                    }
                });
            }
        }, ModalityState.NON_MODAL);
    }

    //    public void actionPerformed(VirtualFile selectedFile, String downloadedFile) throws IOException
    public void actionPerformed(String downloadedFile, VirtualFile selectedFile) throws IOException
    {
        DiffRequest diffData = getDiffRequest(downloadedFile, selectedFile);
//        if (diffData == null) return;
//        final DiffContent[] contents = (DiffContent[]) diffData.getContents();
//        final FileDocumentManager documentManager = FileDocumentManager.getInstance();
//        ApplicationManager.getApplication().runWriteAction(() ->
//        {
//            for (DiffContent content : contents)
//            {
//                Document document = (Document) content;
//                if (document != null)
//                {
//                    documentManager.saveDocument(document);
//                }
//            }
//        });
        DiffManager.getInstance().showDiff(this.project, diffData);
    }

    protected DiffRequest getDiffRequest(String downloadedFile, VirtualFile selectedFile) throws IOException
    {
        VirtualFile f = selectedFile;
        f.setBinaryContent(downloadedFile.getBytes());
        assert selectedFile != null && downloadedFile != null;

        ContentDiffRequest request = DiffRequestFactory.getInstance().createFromFiles(project, selectedFile, f);

        DiffContent editorContent = request.getContents().get(1);
        if (editorContent instanceof DocumentContent)
        {
            Editor[] editors = EditorFactory.getInstance().getEditors(((DocumentContent) editorContent).getDocument());
            if (editors.length != 0)
            {
                request.putUserData(DiffUserDataKeys.SCROLL_TO_LINE, Pair.create(Side.RIGHT, editors[0].getCaretModel().getLogicalPosition().line));
            }
        }

        return request;
    }
}