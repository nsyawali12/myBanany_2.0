package com.madyapadmaonline.mybanany.mybanany;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.madyapadmaonline.mybanany.mybanany.ClientActivity.MyPagerAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;

public class LocalUtilities extends AsyncTask<Object, Integer, Object> {
    public MyPagerAdapter adapter;
    Context context;
    String copiedFromLocation;
    private ArrayList<String> copyItemsList = new ArrayList();
    ProgressDialog copyprogressDialog;
    private ArrayList<String> deletedItemsList = new ArrayList();
    ProgressDialog deleteprogressDialog;
    private boolean folderCopied = true;
    private boolean folderCreation;
    private boolean folderDeletion = true;
    private boolean folderExists;
    private String folderName;
    ListFragment listFragment;
    private String mode;
    private String newName;
    String pasteLocation;
    private String root;

    public void setMode(String mode, ListFragment listFragment, MyPagerAdapter adapter) {
        this.mode = mode;
        this.listFragment = listFragment;
        this.adapter = adapter;
        this.context = listFragment.getActivity().getBaseContext();
    }

    public void setFolderCreationParameters(String root, String folderName) {
        this.root = root;
        this.folderName = folderName;
    }

    public void setDeleteParameters(String root, ArrayList<String> deletedItemsList) {
        this.root = root;
        this.deletedItemsList = deletedItemsList;
    }

    public void setCopyParameters(String copiedFromLoaction, ArrayList<String> copyItemsList, String pasteLocation) {
        this.copiedFromLocation = copiedFromLoaction;
        this.copyItemsList = copyItemsList;
        this.pasteLocation = pasteLocation;
    }

    public void setRenameParameters(String root, String folderName, String newName) {
        this.root = root;
        this.folderName = folderName;
        this.newName = newName;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        String str = this.mode;
        boolean z = true;
        switch (str.hashCode()) {
            case 2074485:
                if (str.equals("COPY")) {
                    z = true;
                    break;
                }
                break;
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
                this.copyprogressDialog = new ProgressDialog(this.listFragment.getActivity());
                this.copyprogressDialog.setTitle("Copying...");
                this.copyprogressDialog.setCancelable(false);
                this.copyprogressDialog.setProgressStyle(1);
                this.copyprogressDialog.setMax(this.copyItemsList.size());
                this.copyprogressDialog.setProgress(0);
                this.copyprogressDialog.show();
                return;
            case true:
                this.copyprogressDialog = new ProgressDialog(this.listFragment.getActivity());
                this.copyprogressDialog.setTitle("Moving...");
                this.copyprogressDialog.setCancelable(false);
                this.copyprogressDialog.setProgressStyle(1);
                this.copyprogressDialog.setMax(this.copyItemsList.size());
                this.copyprogressDialog.setProgress(0);
                this.copyprogressDialog.show();
                return;
            default:
                return;
        }
    }

    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        String str = this.mode;
        int i = -1;
        switch (str.hashCode()) {
            case 2074485:
                if (str.equals("COPY")) {
                    i = 1;
                    break;
                }
                break;
            case 2372561:
                if (str.equals("MOVE")) {
                    i = 2;
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
                this.copyprogressDialog.setProgress(values[0].intValue());
                return;
            case 2:
                this.copyprogressDialog.setProgress(values[0].intValue());
                return;
            default:
                return;
        }
    }

    protected Object doInBackground(Object[] params) {
        String str = this.mode;
        int i = -1;
        switch (str.hashCode()) {
            case -1881265346:
                if (str.equals("RENAME")) {
                    i = 4;
                    break;
                }
                break;
            case -822008559:
                if (str.equals("CREATE_FOLDER")) {
                    i = 0;
                    break;
                }
                break;
            case 2074485:
                if (str.equals("COPY")) {
                    i = 2;
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
                    i = 1;
                    break;
                }
                break;
        }
        Iterator it;
        File file;
        Integer[] numArr;
        switch (i) {
            case 0:
                createFolder();
                break;
            case 1:
                int deleteProgress = 1;
                it = this.deletedItemsList.iterator();
                while (it.hasNext()) {
                    file = new File(this.root + File.separator + ((String) it.next()));
                    if (file.isDirectory()) {
                        deleteFolder(file);
                    } else if (file.isFile()) {
                        deleteFile(file);
                    }
                    numArr = new Integer[1];
                    int deleteProgress2 = deleteProgress + 1;
                    numArr[0] = Integer.valueOf(deleteProgress);
                    publishProgress(numArr);
                    deleteProgress = deleteProgress2;
                }
                break;
            case 2:
                int copyProgress = 1;
                it = this.copyItemsList.iterator();
                while (it.hasNext()) {
                    file = new File(this.copiedFromLocation + File.separator + ((String) it.next()));
                    if (file.isDirectory()) {
                        copyFolder(file, new File(this.pasteLocation + File.separator + file.getName()));
                    } else if (file.isFile()) {
                        copyFile(file, new File(this.pasteLocation + File.separator + file.getName()));
                    }
                    numArr = new Integer[1];
                    int copyProgress2 = copyProgress + 1;
                    numArr[0] = Integer.valueOf(copyProgress);
                    publishProgress(numArr);
                    copyProgress = copyProgress2;
                }
                break;
            case 3:
                int moveProgress = 1;
                it = this.copyItemsList.iterator();
                while (it.hasNext()) {
                    file = new File(this.copiedFromLocation + File.separator + ((String) it.next()));
                    if (file.isDirectory()) {
                        moveFolder(file, new File(this.pasteLocation + File.separator + file.getName()));
                    } else if (file.isFile()) {
                        moveFile(file, new File(this.pasteLocation + File.separator + file.getName()));
                    }
                    numArr = new Integer[1];
                    int moveProgress2 = moveProgress + 1;
                    numArr[0] = Integer.valueOf(moveProgress);
                    publishProgress(numArr);
                    moveProgress = moveProgress2;
                }
                break;
            case 4:
                file = new File(this.root + File.separator + this.folderName);
                if (!file.isDirectory()) {
                    if (file.isFile()) {
                        moveFile(file, new File(this.root + File.separator + this.newName));
                        break;
                    }
                }
                moveFolder(file, new File(this.root + File.separator + this.newName));
                break;
                break;
        }
        return null;
    }

    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        String str = this.mode;
        int i = -1;
        switch (str.hashCode()) {
            case -1881265346:
                if (str.equals("RENAME")) {
                    i = 4;
                    break;
                }
                break;
            case -822008559:
                if (str.equals("CREATE_FOLDER")) {
                    i = 0;
                    break;
                }
                break;
            case 2074485:
                if (str.equals("COPY")) {
                    i = 2;
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
                    i = 1;
                    break;
                }
                break;
        }
        switch (i) {
            case 0:
                if (this.folderCreation && !this.folderExists) {
                    Toast.makeText(this.context, this.folderName + " created successfully!", 0).show();
                    MyPagerAdapter.changedFragment = this.listFragment;
                    this.adapter.notifyDataSetChanged();
                    return;
                } else if (this.folderExists) {
                    Toast.makeText(this.context, this.folderName + " already exists!", 0).show();
                    return;
                } else {
                    Toast.makeText(this.context, this.folderName + " could not be created!", 0).show();
                    return;
                }
            case 1:
                this.deleteprogressDialog.dismiss();
                if (this.folderDeletion) {
                    Toast.makeText(this.context, this.deletedItemsList.size() + " item(s) deleted!", 0).show();
                } else {
                    Toast.makeText(this.context, "Some items could not be deleted!", 0).show();
                }
                MyPagerAdapter.changedFragment = this.listFragment;
                this.adapter.notifyDataSetChanged();
                return;
            case 2:
                this.copyprogressDialog.dismiss();
                if (this.folderCopied) {
                    Toast.makeText(this.context, this.copyItemsList.size() + " item(s) copied!", 0).show();
                } else {
                    Toast.makeText(this.context, "Some items could not be copied!", 0).show();
                }
                ListFragment.localSelectedItemsCopy.clear();
                MyPagerAdapter.changedFragment = this.listFragment;
                this.adapter.notifyDataSetChanged();
                return;
            case 3:
                this.copyprogressDialog.dismiss();
                if (this.folderCopied) {
                    Toast.makeText(this.context, this.copyItemsList.size() + " item(s) moved!", 0).show();
                } else {
                    Toast.makeText(this.context, "Some items could not be moved!", 0).show();
                }
                ListFragment.localSelectedItemsCopy.clear();
                MyPagerAdapter.changedFragment = this.listFragment;
                this.adapter.notifyDataSetChanged();
                return;
            case 4:
                if (this.folderCopied) {
                    Toast.makeText(this.context, "Item renamed successfully!", 0).show();
                } else {
                    Toast.makeText(this.context, "Item could not be renamed!", 0).show();
                }
                MyPagerAdapter.changedFragment = this.listFragment;
                this.adapter.notifyDataSetChanged();
                return;
            default:
                return;
        }
    }

    public void createFolder() {
        File file = new File(this.root + File.separator + this.folderName);
        if (file.exists()) {
            this.folderExists = true;
        }
        this.folderCreation = file.mkdir();
    }

    public void deleteFolder(File file) {
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            this.folderDeletion = false;
            Log.e("nanjaya", "IOexception,Could not delete " + file.getName());
            e.printStackTrace();
        }
    }

    public void deleteFile(File file) {
        if (!file.delete()) {
            this.folderDeletion = false;
            Log.e("nanjaya", "IOexception,Could not delete " + file.getName());
        }
    }

    public void moveFolder(File src, File dest) {
        try {
            FileUtils.moveDirectory(src, dest);
        } catch (IOException e) {
            this.folderCopied = false;
            Log.e("nanjaya", "IOexception,Could not move " + src.getName());
            e.printStackTrace();
        }
    }

    public void moveFile(File src, File dest) {
        try {
            FileUtils.moveFile(src, dest);
        } catch (IOException e) {
            this.folderCopied = false;
            Log.e("nanjaya", "IOexception,Could not move " + src.getName());
            e.printStackTrace();
        }
    }

    public void copyFolder(File src, File dest) {
        try {
            FileUtils.copyDirectory(src, dest);
        } catch (IOException e) {
            this.folderCopied = false;
            Log.e("nanjaya", "IOexception,Could not copy " + src.getName());
            e.printStackTrace();
        }
    }

    public void copyFile(File src, File dest) {
        try {
            FileUtils.copyFile(src, dest);
        } catch (IOException e) {
            this.folderCopied = false;
            Log.e("nanjaya", "IOexception,Could not copy " + src.getName());
            e.printStackTrace();
        }
    }
}
