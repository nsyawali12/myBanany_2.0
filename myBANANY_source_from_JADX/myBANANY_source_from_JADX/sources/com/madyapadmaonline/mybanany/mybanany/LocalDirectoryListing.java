package com.madyapadmaonline.mybanany.mybanany;

import android.os.AsyncTask;
import android.widget.ListView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class LocalDirectoryListing extends AsyncTask {
    File file;
    int[] icon = new int[]{C0322R.drawable.file, C0322R.drawable.folder, C0322R.drawable.audio, C0322R.drawable.video, C0322R.drawable.image, C0322R.drawable.executable, C0322R.drawable.code, C0322R.drawable.pdf, C0322R.drawable.compressed, C0322R.drawable.android, C0322R.drawable.disk, C0322R.drawable.java, C0322R.drawable.text};
    ListFragment listFragment;
    ListView listView;
    File[] returnFileList;
    String root;

    public LocalDirectoryListing(ListFragment listFragment1, String root1, ListView listView1) {
        this.listFragment = listFragment1;
        this.root = root1;
        this.listView = listView1;
    }

    protected Object doInBackground(Object[] params) {
        this.file = new File(this.root);
        this.returnFileList = this.file.listFiles();
        Arrays.sort(this.returnFileList);
        this.listFragment.setLocalLists(this.returnFileList);
        return null;
    }

    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        ArrayList<String> filename = new ArrayList();
        ArrayList<Integer> iconid = new ArrayList();
        ArrayList<String> fileSize = new ArrayList();
        for (File i : this.returnFileList) {
            filename.add(i.getName());
            if (i.isDirectory()) {
                iconid.add(Integer.valueOf(this.icon[1]));
                fileSize.add("Folder");
            } else {
                fileSize.add(ListFragment.getStringFileSize((double) i.length()));
                String fileExtension = i.getName().substring(i.getName().lastIndexOf(".") + 1);
                if (fileExtension.matches("pdf|PDF")) {
                    iconid.add(Integer.valueOf(this.icon[7]));
                } else if (fileExtension.matches("mp3|wav|wma|mpa|m4a|aif|ra|MP3|WAV|WMA|MPA|M4A|AIF|RA")) {
                    iconid.add(Integer.valueOf(this.icon[2]));
                } else if (fileExtension.matches("mp4|mkv|3gp|avi|m4v|mov|mpg|swf|vob|3g2|flv|asf|asx|wmv")) {
                    iconid.add(Integer.valueOf(this.icon[3]));
                } else if (fileExtension.matches("png|jpg|bmp|gif|psd|dds|tif|JPG|PNG|BMP|GIF|PSD|DDS|TIF")) {
                    iconid.add(Integer.valueOf(this.icon[4]));
                } else if (fileExtension.matches("sh|exe|out|bat|jar|SH|EXE|OUT|BAT|JAR")) {
                    iconid.add(Integer.valueOf(this.icon[5]));
                } else if (fileExtension.matches("c|cpp|py|sql|php|html")) {
                    iconid.add(Integer.valueOf(this.icon[6]));
                } else if (fileExtension.matches("apk|APK")) {
                    iconid.add(Integer.valueOf(this.icon[9]));
                } else if (fileExtension.matches("zip|7z|tar|gz|deb|rar|ZIP|7Z|TAR|GZ|DEB|RAR")) {
                    iconid.add(Integer.valueOf(this.icon[8]));
                } else if (fileExtension.matches("iso|bin|vcd|dmg|img|ISO|BIN|VCD|DMG|IMG")) {
                    iconid.add(Integer.valueOf(this.icon[10]));
                } else if (fileExtension.matches("txt|doc|docx|odt|TXT|DOC|DOCX|ODT")) {
                    iconid.add(Integer.valueOf(this.icon[12]));
                } else if (fileExtension.matches("java|class|jar|JAVA|CLASS|JAR")) {
                    iconid.add(Integer.valueOf(this.icon[11]));
                } else {
                    iconid.add(Integer.valueOf(this.icon[0]));
                }
            }
        }
        CustomArrayAdapter customArrayAdapter = new CustomArrayAdapter(this.listFragment.getActivity(), filename, fileSize, iconid);
        this.listView.setAdapter(customArrayAdapter);
        this.listFragment.setLocalAdapter(customArrayAdapter);
    }
}
