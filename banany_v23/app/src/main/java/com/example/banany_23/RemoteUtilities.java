package com.example.banany_23;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.example.banany_23.ClientActivity.MyPagerAdapter;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class RemoteUtilities extends AsyncTask<Object, Integer, Object> {
    MyPagerAdapter adapter;
    Context context;
    ArrayList<String> deletedItemsList = new ArrayList();
    ProgressDialog deleteprogressDialog;
    Boolean fileRename = Boolean.valueOf(true);
    Boolean folderCreation = Boolean.valueOf(true);
    Boolean folderDeletion = Boolean.valueOf(true);
    String folderName;
    String ipAddress;
    ListFragment listFragment;
    String mode;
    ProgressDialog moveProgressDialog;
    String movedFromLocation;
    ArrayList<String> movedItemList = new ArrayList();
    String newName;
    String password;
    String pasteLocation;
    int port;
    String root;
    String username;

    public void setMode(String mode, ListFragment listFragment, MyPagerAdapter adapter) {
        this.mode = mode;
        this.listFragment = listFragment;
        this.adapter = adapter;
        this.context = listFragment.getActivity().getBaseContext();
    }

    public void setDeleteParameters(String root, ArrayList<String> deletedItemsList) {
        this.root = root;
        this.deletedItemsList = deletedItemsList;
    }

    public void setConnectionParameters(String ipAddress, int port, String username, String password) {
        this.ipAddress = ipAddress;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    public void setFolderCreationParameters(String root, String folderName) {
        this.root = root;
        this.folderName = folderName;
    }

    public void setRenameParameters(String root, String folderName, String newName) {
        this.root = root;
        this.folderName = folderName;
        this.newName = newName;
    }

    public void setMoveParameters(String movedFromLocation, ArrayList<String> selectedItemsMove, String pasteLocation) {
        this.root = pasteLocation;
        this.movedFromLocation = movedFromLocation;
        this.movedItemList = selectedItemsMove;
        this.pasteLocation = pasteLocation;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        String str = this.mode;
        boolean z = true;
        switch (str.hashCode()) {
            case 2372561:
                if (str.equals("MOVE")) {
                    z = true;
                    break;
                }
                break;
            case 2012838315:
                if (str.equals("DELETE")) {
                    z = false;
                    break;
                }
                break;
        }
        switch (z) {
            case false:
                this.deleteprogressDialog = new ProgressDialog(this.listFragment.getActivity());
                this.deleteprogressDialog.setTitle("Deleting...");
                this.deleteprogressDialog.setProgressStyle(1);
                this.deleteprogressDialog.setCancelable(false);
                this.deleteprogressDialog.setMax(this.deletedItemsList.size());
                this.deleteprogressDialog.setProgress(0);
                this.deleteprogressDialog.show();
                return;
            case true:
                this.moveProgressDialog = new ProgressDialog(this.listFragment.getActivity());
                this.moveProgressDialog.setTitle("Moving...");
                this.moveProgressDialog.setProgressStyle(1);
                this.moveProgressDialog.setCancelable(false);
                this.moveProgressDialog.setMax(this.movedItemList.size());
                this.moveProgressDialog.setProgress(0);
                this.moveProgressDialog.show();
                return;
            default:
                return;
        }
    }

    protected void onProgressUpdate(Integer... values) {
        String str = this.mode;
        int i = -1;
        switch (str.hashCode()) {
            case 2372561:
                if (str.equals("MOVE")) {
                    i = 1;
                    break;
                }
                break;
            case 2012838315:
                if (str.equals("DELETE")) {
                    i = 0;
                    break;
                }
                break;
        }
        switch (i) {
            case 0:
                this.deleteprogressDialog.setProgress(values[0].intValue());
                return;
            case 1:
                this.moveProgressDialog.setProgress(values[0].intValue());
                return;
            default:
                return;
        }
    }

    protected Object doInBackground(Object[] params) {
        FTPClient client = new FTPClient();
        try {
            client.connect(this.ipAddress, this.port);
            client.enterLocalPassiveMode();
            if (!client.login(this.username, this.password)) {
                return null;
            }
            if (client.changeWorkingDirectory(this.root)) {
                FTPFile[] directoryListing = client.listFiles(client.printWorkingDirectory());
                String str = this.mode;
                Object obj = -1;
                switch (str.hashCode()) {
                    case -1881265346:
                        if (str.equals("RENAME")) {
                            obj = 2;
                            break;
                        }
                        break;
                    case -822008559:
                        if (str.equals("CREATE_FOLDER")) {
                            obj = 3;
                            break;
                        }
                        break;
                    case 2372561:
                        if (str.equals("MOVE")) {
                            obj = 1;
                            break;
                        }
                        break;
                    case 2012838315:
                        if (str.equals("DELETE")) {
                            obj = null;
                            break;
                        }
                        break;
                }
                String i;
                switch (obj) {
                    case null:
                        Iterator it = this.deletedItemsList.iterator();
                        int deleteProgress = 1;
                        while (it.hasNext()) {
                            i = (String) it.next();
                            for (FTPFile f : directoryListing) {
                                if (f.getName().equals(i)) {
                                    if (f.isDirectory()) {
                                        deleteDirectory(client, this.root + File.separator + f.getName(), "");
                                    } else {
                                        deleteFile(client, this.root + File.separator + f.getName());
                                    }
                                }
                            }
                            Integer[] numArr = new Integer[1];
                            int deleteProgress2 = deleteProgress + 1;
                            numArr[0] = Integer.valueOf(deleteProgress);
                            publishProgress(numArr);
                            deleteProgress = deleteProgress2;
                        }
                        break;
                    case 1:
                        Iterator it2 = this.movedItemList.iterator();
                        int moveProgress = 1;
                        while (it2.hasNext()) {
                            i = (String) it2.next();
                            rename(client, this.movedFromLocation + File.separator + i, this.pasteLocation + File.separator + i);
                            Integer[] numArr2 = new Integer[1];
                            int moveProgress2 = moveProgress + 1;
                            numArr2[0] = Integer.valueOf(moveProgress);
                            publishProgress(numArr2);
                            moveProgress = moveProgress2;
                        }
                        break;
                    case 2:
                        rename(client, this.root + File.separator + this.folderName, this.root + File.separator + this.newName);
                        break;
                    case 3:
                        createFolder(client);
                        break;
                }
                return null;
            }
            Log.e("nanjaya", "failed to change directory to " + this.root);
            return null;
        } catch (SocketException e) {
            Log.e("nanjaya", "SocketException");
            e.printStackTrace();
        } catch (IOException e2) {
            Log.e("nanjaya", "IOException");
            e2.printStackTrace();
        }
    }

    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        String str = this.mode;
        int i = -1;
        switch (str.hashCode()) {
            case -1881265346:
                if (str.equals("RENAME")) {
                    i = 2;
                    break;
                }
                break;
            case -822008559:
                if (str.equals("CREATE_FOLDER")) {
                    i = 1;
                    break;
                }
                break;
            case 2372561:
                if (str.equals("MOVE")) {
                    i = 3;
                    break;
                }
                break;
            case 2012838315:
                if (str.equals("DELETE")) {
                    i = 0;
                    break;
                }
                break;
        }
        switch (i) {
            case 0:
                this.deleteprogressDialog.dismiss();
                if (this.folderDeletion.booleanValue()) {
                    Toast.makeText(this.context, this.deletedItemsList.size() + " item(s) deleted!", 0).show();
                } else {
                    Toast.makeText(this.context, "Some items could not be deleted!", 0).show();
                }
                MyPagerAdapter.changedFragment = this.listFragment;
                this.adapter.notifyDataSetChanged();
                return;
            case 1:
                if (this.folderCreation.booleanValue()) {
                    Toast.makeText(this.context, this.folderName + " created successfully!", 0).show();
                    MyPagerAdapter.changedFragment = this.listFragment;
                    this.adapter.notifyDataSetChanged();
                    return;
                }
                Toast.makeText(this.context, this.folderName + " could not be created!", 0).show();
                return;
            case 2:
                if (this.fileRename.booleanValue()) {
                    Toast.makeText(this.context, "Item renamed successfully!", 0).show();
                    MyPagerAdapter.changedFragment = this.listFragment;
                    this.adapter.notifyDataSetChanged();
                    return;
                }
                Toast.makeText(this.context, "Item could not be renamed!", 0).show();
                return;
            case 3:
                this.moveProgressDialog.dismiss();
                if (this.fileRename.booleanValue()) {
                    Toast.makeText(this.context, this.movedItemList.size() + " item(s) moved successfully!", 0).show();
                } else {
                    Toast.makeText(this.context, "Some items could not be moved!", 0).show();
                }
                MyPagerAdapter.changedFragment = this.listFragment;
                this.adapter.notifyDataSetChanged();
                ListFragment.remoteSelectedItemsMove.clear();
                return;
            default:
                return;
        }
    }

    public void createFolder(FTPClient ftpClient) {
        try {
            ftpClient.makeDirectory(this.root + File.separator + this.folderName);
        } catch (IOException e) {
            this.folderCreation = Boolean.valueOf(false);
            e.printStackTrace();
        }
    }

    public void rename(FTPClient ftpClient, String old_name, String new_Name) {
        try {
            ftpClient.rename(old_name, new_Name);
        } catch (IOException e) {
            this.fileRename = Boolean.valueOf(false);
            e.printStackTrace();
        }
    }

    public void deleteDirectory(FTPClient ftpClient, String parentDir, String currentDir) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList = dirToList + "/" + currentDir;
        }
        FTPFile[] subFiles = ftpClient.listFiles(dirToList);
        if (subFiles != null) {
            if (subFiles.length > 0) {
                for (FTPFile aFile : subFiles) {
                    String currentFileName = aFile.getName();
                    if (!(currentFileName.equals(".") || currentFileName.equals(".."))) {
                        String filePath = parentDir + "/" + currentDir + "/" + currentFileName;
                        if (currentDir.equals("")) {
                            filePath = parentDir + "/" + currentFileName;
                        }
                        if (aFile.isDirectory()) {
                            deleteDirectory(ftpClient, dirToList, currentFileName);
                        } else if (!ftpClient.deleteFile(filePath)) {
                            this.folderDeletion = Boolean.valueOf(false);
                        }
                    }
                }
            }
            if (!ftpClient.removeDirectory(dirToList)) {
                this.folderDeletion = Boolean.valueOf(false);
            }
        }
    }

    public void deleteFile(FTPClient ftpClient, String filePath) throws IOException {
        if (!Boolean.valueOf(ftpClient.deleteFile(filePath)).booleanValue()) {
            this.folderDeletion = Boolean.valueOf(false);
        }
    }
}
