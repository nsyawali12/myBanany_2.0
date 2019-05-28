package com.madyapadmaonline.mybanany.mybanany;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.madyapadmaonline.mybanany.mybanany.ClientActivity.MyPagerAdapter;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.apache.commons.net.ftp.FTPFile;

public class ListFragment extends Fragment {
    public static MyPagerAdapter adapter;
    static String localCopiedFromLocation;
    static Boolean localGlobalCopyFlag = Boolean.valueOf(false);
    static Boolean localGlobalMoveFlag = Boolean.valueOf(false);
    static String localPasteLocation;
    static ArrayList<String> localSelectedItemsCopy = new ArrayList();
    static Boolean remoteGlobalMoveFlag = Boolean.valueOf(false);
    static String remoteMovedFromLocation;
    static String remotePasteLocation;
    static ArrayList<String> remoteSelectedItemsMove = new ArrayList();
    public static ViewPager viewPager;
    String backup;
    String changedFragmentID;
    ListFragment current = this;
    File file;
    View fragmentView;
    ListView listView;
    CustomArrayAdapter localAdapter;
    ArrayList<Integer> localSelectedIndex = new ArrayList();
    ArrayList<String> localSelectedItems = new ArrayList();
    File[] localfileList;
    Menu menu;
    public Handler messageHandler = new MessageHandler();
    public Handler messageHandlerUpload = new MessageHandlerUpload();
    String pageType;
    CustomArrayAdapter remoteAdapter;
    ArrayList<Integer> remoteSelectedIndex = new ArrayList();
    ArrayList<String> remoteSelectedItems = new ArrayList();
    FTPFile[] remotefileList;
    String root;
    TextView textView;

    /* renamed from: com.madyapadmaonline.mybanany.mybanany.ListFragment$1 */
    class C03121 implements OnItemClickListener {
        C03121() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (!ListFragment.this.localfileList[position].isDirectory()) {
                ListFragment.this.localItemClickHandler(position);
                ListFragment.this.localAdapter.notifyDataSetChanged();
                ListFragment.this.updateLocalActionBar();
            } else if (ListFragment.this.localSelectedItems.size() > 0) {
                ListFragment.this.localItemClickHandler(position);
                ListFragment.this.localAdapter.notifyDataSetChanged();
                ListFragment.this.updateLocalActionBar();
            } else {
                ListFragment.this.backup = ListFragment.this.root;
                if (ListFragment.this.root.equals("/")) {
                    ListFragment.this.root = "/" + ListFragment.this.localfileList[position].getName();
                } else {
                    ListFragment.this.root += "/" + ListFragment.this.localfileList[position].getName();
                }
                Bundle bundle = new Bundle();
                bundle.putString("path", ListFragment.this.root);
                bundle.putString("pageType", "local");
                MyPagerAdapter.setRootSecondPage(bundle);
                MyPagerAdapter.changedFragment = ListFragment.this.current;
                ListFragment.adapter.notifyDataSetChanged();
            }
        }
    }

    /* renamed from: com.madyapadmaonline.mybanany.mybanany.ListFragment$2 */
    class C03132 implements OnItemLongClickListener {
        C03132() {
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            ListFragment.this.localItemClickHandler(position);
            ListFragment.this.localAdapter.notifyDataSetChanged();
            ListFragment.this.updateLocalActionBar();
            return true;
        }
    }

    /* renamed from: com.madyapadmaonline.mybanany.mybanany.ListFragment$3 */
    class C03143 implements OnItemClickListener {
        C03143() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (!ListFragment.this.remotefileList[position].isDirectory()) {
                ListFragment.this.remoteItemClickHandler(position);
                ListFragment.this.remoteAdapter.notifyDataSetChanged();
                ListFragment.this.updateRemoteActionBar();
            } else if (ListFragment.this.remoteSelectedItems.size() > 0) {
                ListFragment.this.remoteItemClickHandler(position);
                ListFragment.this.remoteAdapter.notifyDataSetChanged();
                ListFragment.this.updateRemoteActionBar();
            } else {
                ListFragment.this.root += "/" + ListFragment.this.remotefileList[position].getName();
                Bundle bundle = new Bundle();
                bundle.putString("path", ListFragment.this.root);
                bundle.putString("pageType", "remote");
                MyPagerAdapter.setRootFirstPage(bundle);
                MyPagerAdapter.changedFragment = ListFragment.this.current;
                ListFragment.adapter.notifyDataSetChanged();
            }
        }
    }

    /* renamed from: com.madyapadmaonline.mybanany.mybanany.ListFragment$4 */
    class C03154 implements OnItemLongClickListener {
        C03154() {
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            ListFragment.this.remoteItemClickHandler(position);
            ListFragment.this.remoteAdapter.notifyDataSetChanged();
            ListFragment.this.updateRemoteActionBar();
            return true;
        }
    }

    /* renamed from: com.madyapadmaonline.mybanany.mybanany.ListFragment$5 */
    class C03165 implements OnClickListener {
        C03165() {
        }

        public void onClick(DialogInterface dialog, int which) {
        }
    }

    /* renamed from: com.madyapadmaonline.mybanany.mybanany.ListFragment$8 */
    class C03198 implements OnClickListener {
        C03198() {
        }

        public void onClick(DialogInterface dialog, int which) {
            LocalUtilities task = new LocalUtilities();
            task.setMode("DELETE", ListFragment.this.current, ListFragment.adapter);
            task.setDeleteParameters(ListFragment.this.root, ListFragment.this.localSelectedItems);
            task.execute(new Object[0]);
        }
    }

    /* renamed from: com.madyapadmaonline.mybanany.mybanany.ListFragment$9 */
    class C03209 implements OnClickListener {
        C03209() {
        }

        public void onClick(DialogInterface dialog, int which) {
            ListFragment.localGlobalCopyFlag = Boolean.valueOf(false);
            ListFragment.localSelectedItemsCopy.clear();
            ListFragment.this.updateLocalActionBar();
        }
    }

    public static class MessageHandler extends Handler {
        public void handleMessage(Message message) {
            switch (message.arg1) {
                case 0:
                    MyPagerAdapter.changedFragment = MyPagerAdapter.remoteFragment;
                    Toast.makeText(MyPagerAdapter.remoteFragment.getActivity().getBaseContext(), "Some items could not be downloaded!", 0).show();
                    ListFragment.adapter.notifyDataSetChanged();
                    MyPagerAdapter.changedFragment = MyPagerAdapter.localFragment;
                    ListFragment.adapter.notifyDataSetChanged();
                    return;
                case 1:
                    MyPagerAdapter.changedFragment = MyPagerAdapter.remoteFragment;
                    Toast.makeText(MyPagerAdapter.remoteFragment.getActivity().getBaseContext(), "All items have finished downloading!", 0).show();
                    ListFragment.adapter.notifyDataSetChanged();
                    MyPagerAdapter.changedFragment = MyPagerAdapter.localFragment;
                    ListFragment.adapter.notifyDataSetChanged();
                    return;
                default:
                    return;
            }
        }
    }

    public static class MessageHandlerUpload extends Handler {
        public void handleMessage(Message message) {
            switch (message.arg1) {
                case 0:
                    MyPagerAdapter.changedFragment = MyPagerAdapter.remoteFragment;
                    ListFragment.adapter.notifyDataSetChanged();
                    Toast.makeText(MyPagerAdapter.remoteFragment.getActivity().getBaseContext(), "Some items could not be uploaded!", 0).show();
                    MyPagerAdapter.changedFragment = MyPagerAdapter.localFragment;
                    ListFragment.adapter.notifyDataSetChanged();
                    return;
                case 1:
                    MyPagerAdapter.changedFragment = MyPagerAdapter.remoteFragment;
                    ListFragment.adapter.notifyDataSetChanged();
                    Toast.makeText(MyPagerAdapter.remoteFragment.getActivity().getBaseContext(), "All items have finished uploading!", 0).show();
                    MyPagerAdapter.changedFragment = MyPagerAdapter.localFragment;
                    ListFragment.adapter.notifyDataSetChanged();
                    return;
                case 2:
                    MyPagerAdapter.changedFragment = MyPagerAdapter.remoteFragment;
                    ListFragment.adapter.notifyDataSetChanged();
                    Toast.makeText(MyPagerAdapter.remoteFragment.getActivity().getBaseContext(), "Some files skipped since they already existed on the server!", 0).show();
                    MyPagerAdapter.changedFragment = MyPagerAdapter.localFragment;
                    ListFragment.adapter.notifyDataSetChanged();
                    return;
                default:
                    return;
            }
        }
    }

    public static ListFragment newInstance(Bundle args, MyPagerAdapter m, ViewPager v) {
        ListFragment fragment = new ListFragment();
        fragment.setArguments(args);
        adapter = m;
        viewPager = v;
        return fragment;
    }

    public void setChangedFragmentID(String changedFragmentID) {
        this.changedFragmentID = changedFragmentID;
    }

    public void onCreateOptionsMenu(Menu menu1, MenuInflater inflater) {
        this.menu = menu1;
        inflater.inflate(C0322R.menu.action_bar, menu1);
        if (getArguments().getString("pageType").equals("local")) {
            updateLocalActionBar();
        } else {
            updateRemoteActionBar();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == C0322R.id.plus) {
            if (getArguments().getString("pageType").equals("local")) {
                createNewFolderLocal();
                return true;
            }
            createNewFolderRemote();
            return true;
        } else if (item.getItemId() == C0322R.id.trash) {
            if (getArguments().getString("pageType").equals("local")) {
                deleteSelectedItemsLocal();
                return true;
            }
            deleteSelectedItemsRemote();
            return true;
        } else if (item.getItemId() == C0322R.id.clearselection) {
            if (getArguments().getString("pageType").equals("local")) {
                clearLocalSelection();
                return true;
            }
            clearRemoteSelection();
            return true;
        } else if (item.getItemId() == C0322R.id.selectall) {
            int i;
            if (getArguments().getString("pageType").equals("local")) {
                this.localSelectedItems.clear();
                this.localSelectedIndex.clear();
                if (this.localfileList == null) {
                    return true;
                }
                for (i = 0; i < this.localfileList.length; i++) {
                    localItemClickHandler(i);
                }
                this.localAdapter.notifyDataSetChanged();
                updateLocalActionBar();
                return true;
            }
            this.remoteSelectedItems.clear();
            this.remoteSelectedIndex.clear();
            if (this.remotefileList == null) {
                return true;
            }
            for (i = 0; i < this.remotefileList.length; i++) {
                remoteItemClickHandler(i);
            }
            this.remoteAdapter.notifyDataSetChanged();
            updateRemoteActionBar();
            return true;
        } else if (item.getItemId() == C0322R.id.copy) {
            localCopiedFromLocation = this.root;
            r2 = this.localSelectedItems.iterator();
            while (r2.hasNext()) {
                localSelectedItemsCopy.add((String) r2.next());
            }
            localGlobalCopyFlag = Boolean.valueOf(true);
            clearLocalSelection();
            updateLocalActionBar();
            return true;
        } else if (item.getItemId() == C0322R.id.copyPaste) {
            localPasteLocation = this.root;
            localGlobalCopyFlag = Boolean.valueOf(false);
            copyPasteSelectedItems();
            updateLocalActionBar();
            return true;
        } else if (item.getItemId() == C0322R.id.cancelCopy) {
            localGlobalCopyFlag = Boolean.valueOf(false);
            localSelectedItemsCopy.clear();
            updateLocalActionBar();
            return true;
        } else if (item.getItemId() == C0322R.id.move) {
            if (getArguments().getString("pageType").equals("local")) {
                localCopiedFromLocation = this.root;
                r2 = this.localSelectedItems.iterator();
                while (r2.hasNext()) {
                    localSelectedItemsCopy.add((String) r2.next());
                }
                localGlobalMoveFlag = Boolean.valueOf(true);
                clearLocalSelection();
                updateLocalActionBar();
                return true;
            }
            remoteMovedFromLocation = this.root;
            r2 = this.remoteSelectedItems.iterator();
            while (r2.hasNext()) {
                remoteSelectedItemsMove.add((String) r2.next());
            }
            remoteGlobalMoveFlag = Boolean.valueOf(true);
            clearRemoteSelection();
            updateRemoteActionBar();
            return true;
        } else if (item.getItemId() == C0322R.id.cancelMove) {
            if (getArguments().getString("pageType").equals("local")) {
                localGlobalMoveFlag = Boolean.valueOf(false);
                localSelectedItemsCopy.clear();
                updateLocalActionBar();
                return true;
            }
            remoteGlobalMoveFlag = Boolean.valueOf(false);
            remoteSelectedItemsMove.clear();
            updateRemoteActionBar();
            return true;
        } else if (item.getItemId() != C0322R.id.movePaste) {
            if (item.getItemId() == C0322R.id.rename) {
                if (getArguments().getString("pageType").equals("local")) {
                    renameSelectedItemLocal();
                } else {
                    renameSelectedItemRemote();
                }
            }
            if (item.getItemId() == C0322R.id.download) {
                downloadSelectedItems();
            }
            if (item.getItemId() == C0322R.id.upload) {
                uploadSelectedItems();
            }
            return super.onOptionsItemSelected(item);
        } else if (getArguments().getString("pageType").equals("local")) {
            localPasteLocation = this.root;
            localGlobalMoveFlag = Boolean.valueOf(false);
            movePasteSelectedItemsLocal();
            updateLocalActionBar();
            return true;
        } else {
            remotePasteLocation = this.root;
            remoteGlobalMoveFlag = Boolean.valueOf(false);
            movePasteSelectedItemsRemote();
            updateRemoteActionBar();
            return true;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.fragmentView = inflater.inflate(C0322R.layout.fragment, container, false);
        this.root = getArguments().getString("path");
        this.pageType = getArguments().getString("pageType");
        this.listView = (ListView) this.fragmentView.findViewById(C0322R.id.list_view);
        this.listView.setChoiceMode(2);
        this.textView = (TextView) this.fragmentView.findViewById(C0322R.id.textView);
        setHasOptionsMenu(true);
        if (this.pageType.equals("local")) {
            LocalListHandler();
        } else {
            RemoteListHandler();
        }
        return this.fragmentView;
    }

    public void setLocalLists(File[] gotFileList) {
        this.localfileList = gotFileList;
    }

    public void setLocalAdapter(CustomArrayAdapter gotcustomArrayAdapter) {
        this.localAdapter = gotcustomArrayAdapter;
    }

    public void LocalListHandler() {
        this.file = new File(this.root);
        if (this.file.list() == null) {
            this.textView.setText("Elevated Privileges may be required!");
            Toast.makeText(getActivity().getBaseContext(), "Could not change Directory!", 0).show();
            this.root = this.backup;
            return;
        }
        this.textView.setText(this.root);
        new LocalDirectoryListing(this.current, this.root, this.listView).execute(new Object[0]);
        this.listView.setOnItemClickListener(new C03121());
        this.listView.setOnItemLongClickListener(new C03132());
    }

    public void setRemoteLists(FTPFile[] gotFilelist) {
        this.remotefileList = gotFilelist;
    }

    public void setRemoteAdapter(CustomArrayAdapter customArrayAdapter) {
        this.remoteAdapter = customArrayAdapter;
    }

    public void RemoteListHandler() {
        if (!isNetworkAvailable()) {
            noNetworkHandler();
        }
        RemoteDirectoryListing task = new RemoteDirectoryListing(this.current, this.root, this.listView, this.textView);
        task.setConnectionParameters(ClientActivity.HOST_NAME, ClientActivity.PORT, ClientActivity.USERNAME, ClientActivity.PASSWORD);
        task.execute(new Object[0]);
        this.listView.setOnItemClickListener(new C03143());
        this.listView.setOnItemLongClickListener(new C03154());
    }

    public void noNetworkHandler() {
        new Builder(getActivity()).setIcon(C0322R.drawable.alert).setTitle("Unable to access the network!").setMessage("You must be connected to the Internet for myBANANY to work..Please connect to a network and try again. ").setPositiveButton("OK", new C03165()).show();
    }

    private boolean isNetworkAvailable() {
        return ((ConnectivityManager) getActivity().getSystemService("connectivity")).getActiveNetworkInfo() != null;
    }

    public void localItemClickHandler(int position) {
        if (this.localSelectedItems.contains(this.localfileList[position].getName())) {
            this.localSelectedItems.remove(this.localfileList[position].getName());
            this.localSelectedIndex.remove(this.localSelectedIndex.indexOf(Integer.valueOf(position)));
            this.localAdapter.setSelectedItem(this.localSelectedIndex);
            return;
        }
        this.localSelectedItems.add(this.localfileList[position].getName());
        this.localSelectedIndex.add(Integer.valueOf(position));
        this.localAdapter.setSelectedItem(this.localSelectedIndex);
    }

    public void remoteItemClickHandler(int position) {
        if (this.remoteSelectedItems.contains(this.remotefileList[position].getName())) {
            this.remoteSelectedItems.remove(this.remotefileList[position].getName());
            this.remoteSelectedIndex.remove(this.remoteSelectedIndex.indexOf(Integer.valueOf(position)));
            this.remoteAdapter.setSelectedItem(this.remoteSelectedIndex);
            return;
        }
        this.remoteSelectedItems.add(this.remotefileList[position].getName());
        this.remoteSelectedIndex.add(Integer.valueOf(position));
        this.remoteAdapter.setSelectedItem(this.remoteSelectedIndex);
    }

    public static String getStringFileSize(double bytes) {
        double kilobytes = bytes / 1024.0d;
        double megabytes = kilobytes / 1024.0d;
        double gigabytes = megabytes / 1024.0d;
        String rval = Double.toString(bytes) + " Bytes";
        if (kilobytes < 1.0d) {
            return rval;
        }
        if (megabytes < 1.0d) {
            return new DecimalFormat("#.00").format(kilobytes).toString() + " KB";
        } else if (gigabytes < 1.0d) {
            return new DecimalFormat("#.00").format(megabytes).toString() + " MB";
        } else {
            return new DecimalFormat("#.00").format(gigabytes).toString() + " GB";
        }
    }

    public void updateLocalActionBar() {
        this.menu.findItem(C0322R.id.download).setVisible(false);
        this.menu.findItem(C0322R.id.copyPaste).setVisible(false);
        this.menu.findItem(C0322R.id.cancelCopy).setVisible(false);
        this.menu.findItem(C0322R.id.movePaste).setVisible(false);
        this.menu.findItem(C0322R.id.cancelMove).setVisible(false);
        this.menu.findItem(C0322R.id.plus).setVisible(true);
        if (localGlobalCopyFlag.booleanValue()) {
            this.menu.findItem(C0322R.id.rename).setVisible(false);
            this.menu.findItem(C0322R.id.clearselection).setVisible(false);
            this.menu.findItem(C0322R.id.selectall).setVisible(false);
            this.menu.findItem(C0322R.id.trash).setVisible(false);
            this.menu.findItem(C0322R.id.move).setVisible(false);
            this.menu.findItem(C0322R.id.copy).setVisible(false);
            this.menu.findItem(C0322R.id.plus).setVisible(false);
            this.menu.findItem(C0322R.id.movePaste).setVisible(false);
            this.menu.findItem(C0322R.id.cancelMove).setVisible(false);
            this.menu.findItem(C0322R.id.upload).setVisible(false);
            this.menu.findItem(C0322R.id.copyPaste).setVisible(true);
            this.menu.findItem(C0322R.id.cancelCopy).setVisible(true);
        } else if (localGlobalMoveFlag.booleanValue()) {
            this.menu.findItem(C0322R.id.rename).setVisible(false);
            this.menu.findItem(C0322R.id.clearselection).setVisible(false);
            this.menu.findItem(C0322R.id.selectall).setVisible(false);
            this.menu.findItem(C0322R.id.trash).setVisible(false);
            this.menu.findItem(C0322R.id.move).setVisible(false);
            this.menu.findItem(C0322R.id.copy).setVisible(false);
            this.menu.findItem(C0322R.id.plus).setVisible(false);
            this.menu.findItem(C0322R.id.copyPaste).setVisible(false);
            this.menu.findItem(C0322R.id.cancelCopy).setVisible(false);
            this.menu.findItem(C0322R.id.upload).setVisible(false);
            this.menu.findItem(C0322R.id.movePaste).setVisible(true);
            this.menu.findItem(C0322R.id.cancelMove).setVisible(true);
        } else {
            if (this.localSelectedItems.size() > 0) {
                if (this.localSelectedItems.size() == 1) {
                    this.menu.findItem(C0322R.id.rename).setVisible(true);
                    ClientActivity.myToolbar.setTitle(this.localSelectedItems.size() + " item selected");
                } else {
                    this.menu.findItem(C0322R.id.rename).setVisible(false);
                    ClientActivity.myToolbar.setTitle(this.localSelectedItems.size() + " items selected");
                }
                if (this.localSelectedItems.size() == this.localfileList.length) {
                    this.menu.findItem(C0322R.id.selectall).setVisible(false);
                } else {
                    this.menu.findItem(C0322R.id.selectall).setVisible(true);
                }
                this.menu.findItem(C0322R.id.clearselection).setVisible(true);
                this.menu.findItem(C0322R.id.move).setVisible(true);
                this.menu.findItem(C0322R.id.copy).setVisible(true);
                this.menu.findItem(C0322R.id.trash).setVisible(true);
                this.menu.findItem(C0322R.id.upload).setVisible(true);
            }
            if (this.localSelectedItems.size() == 0) {
                this.menu.findItem(C0322R.id.rename).setVisible(false);
                this.menu.findItem(C0322R.id.clearselection).setVisible(false);
                this.menu.findItem(C0322R.id.selectall).setVisible(true);
                this.menu.findItem(C0322R.id.trash).setVisible(false);
                this.menu.findItem(C0322R.id.move).setVisible(false);
                this.menu.findItem(C0322R.id.copy).setVisible(false);
                this.menu.findItem(C0322R.id.upload).setVisible(false);
                ClientActivity.myToolbar.setTitle(ClientActivity.SERVER_NAME);
            }
        }
    }

    public void updateRemoteActionBar() {
        this.menu.findItem(C0322R.id.copy).setVisible(false);
        this.menu.findItem(C0322R.id.copyPaste).setVisible(false);
        this.menu.findItem(C0322R.id.cancelCopy).setVisible(false);
        this.menu.findItem(C0322R.id.upload).setVisible(false);
        this.menu.findItem(C0322R.id.movePaste).setVisible(false);
        this.menu.findItem(C0322R.id.cancelMove).setVisible(false);
        this.menu.findItem(C0322R.id.plus).setVisible(true);
        if (remoteGlobalMoveFlag.booleanValue()) {
            this.menu.findItem(C0322R.id.rename).setVisible(false);
            this.menu.findItem(C0322R.id.clearselection).setVisible(false);
            this.menu.findItem(C0322R.id.selectall).setVisible(false);
            this.menu.findItem(C0322R.id.trash).setVisible(false);
            this.menu.findItem(C0322R.id.move).setVisible(false);
            this.menu.findItem(C0322R.id.plus).setVisible(false);
            this.menu.findItem(C0322R.id.download).setVisible(false);
            this.menu.findItem(C0322R.id.movePaste).setVisible(true);
            this.menu.findItem(C0322R.id.cancelMove).setVisible(true);
            return;
        }
        if (this.remoteSelectedItems.size() > 0) {
            if (this.remoteSelectedItems.size() == 1) {
                this.menu.findItem(C0322R.id.rename).setVisible(true);
                ClientActivity.myToolbar.setTitle(this.remoteSelectedItems.size() + " item selected");
            } else {
                this.menu.findItem(C0322R.id.rename).setVisible(false);
                ClientActivity.myToolbar.setTitle(this.remoteSelectedItems.size() + " items selected");
            }
            if (this.remoteSelectedItems.size() == this.remotefileList.length) {
                this.menu.findItem(C0322R.id.selectall).setVisible(false);
            } else {
                this.menu.findItem(C0322R.id.selectall).setVisible(true);
            }
            this.menu.findItem(C0322R.id.clearselection).setVisible(true);
            this.menu.findItem(C0322R.id.download).setVisible(true);
            this.menu.findItem(C0322R.id.move).setVisible(true);
            this.menu.findItem(C0322R.id.trash).setVisible(true);
        }
        if (this.remoteSelectedItems.size() == 0) {
            this.menu.findItem(C0322R.id.rename).setVisible(false);
            this.menu.findItem(C0322R.id.selectall).setVisible(true);
            this.menu.findItem(C0322R.id.clearselection).setVisible(false);
            this.menu.findItem(C0322R.id.download).setVisible(false);
            this.menu.findItem(C0322R.id.trash).setVisible(false);
            this.menu.findItem(C0322R.id.move).setVisible(false);
            ClientActivity.myToolbar.setTitle(ClientActivity.SERVER_NAME);
        }
    }

    public void clearLocalSelection() {
        this.localSelectedItems.clear();
        this.localSelectedIndex.clear();
        this.localAdapter.notifyDataSetChanged();
        updateLocalActionBar();
    }

    public void clearRemoteSelection() {
        this.remoteSelectedItems.clear();
        this.remoteSelectedIndex.clear();
        this.remoteAdapter.notifyDataSetChanged();
        updateRemoteActionBar();
    }

    public void createNewFolderLocal() {
        Builder builder = new Builder(this.current.getActivity());
        builder.setTitle("New Folder Name...");
        final EditText input = new EditText(this.current.getActivity());
        builder.setView(input);
        builder.setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LocalUtilities task = new LocalUtilities();
                task.setMode("CREATE_FOLDER", ListFragment.this.current, ListFragment.adapter);
                task.setFolderCreationParameters(ListFragment.this.root, input.getText().toString());
                task.execute(new Object[0]);
            }
        });
        builder.setNegativeButton("Cancel", null);
        final AlertDialog dialog = builder.show();
        input.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(5);
                }
            }
        });
    }

    public void deleteSelectedItemsLocal() {
        new Builder(getActivity()).setIcon(C0322R.drawable.alert).setTitle("Delete?...").setMessage("Are you sure you want to delete " + this.localSelectedItems.size() + " item(s)").setPositiveButton("Yes", new C03198()).setNegativeButton("No", null).show();
    }

    public void copyPasteSelectedItems() {
        new Builder(getActivity()).setTitle("Copy?...").setMessage("Are you sure you want to copy " + localSelectedItemsCopy.size() + " item(s) to " + localPasteLocation).setPositiveButton("Yes", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LocalUtilities task = new LocalUtilities();
                task.setMode("COPY", ListFragment.this.current, ListFragment.adapter);
                task.setCopyParameters(ListFragment.localCopiedFromLocation, ListFragment.localSelectedItemsCopy, ListFragment.localPasteLocation);
                task.execute(new Object[0]);
            }
        }).setNegativeButton("No", new C03209()).show();
    }

    public void movePasteSelectedItemsLocal() {
        new Builder(getActivity()).setTitle("Move?...").setMessage("Are you sure you want to move " + localSelectedItemsCopy.size() + " item(s) to " + localPasteLocation).setPositiveButton("Yes", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LocalUtilities task = new LocalUtilities();
                task.setMode("MOVE", ListFragment.this.current, ListFragment.adapter);
                task.setCopyParameters(ListFragment.localCopiedFromLocation, ListFragment.localSelectedItemsCopy, ListFragment.localPasteLocation);
                task.execute(new Object[0]);
            }
        }).setNegativeButton("No", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ListFragment.localGlobalMoveFlag = Boolean.valueOf(false);
                ListFragment.localSelectedItemsCopy.clear();
                ListFragment.this.updateLocalActionBar();
            }
        }).show();
    }

    public void renameSelectedItemLocal() {
        Builder builder = new Builder(this.current.getActivity());
        builder.setTitle("New Name...");
        final EditText input = new EditText(this.current.getActivity());
        builder.setView(input);
        builder.setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LocalUtilities task = new LocalUtilities();
                task.setMode("RENAME", ListFragment.this.current, ListFragment.adapter);
                task.setRenameParameters(ListFragment.this.root, (String) ListFragment.this.localSelectedItems.get(0), input.getText().toString());
                task.execute(new Object[0]);
            }
        });
        builder.setNegativeButton("Cancel", null);
        final AlertDialog dialog = builder.show();
        input.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(5);
                }
            }
        });
    }

    public void deleteSelectedItemsRemote() {
        new Builder(getActivity()).setIcon(C0322R.drawable.alert).setTitle("Delete?...").setMessage("Are you sure you want to delete " + this.remoteSelectedItems.size() + " item(s)").setPositiveButton("Yes", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                RemoteUtilities task = new RemoteUtilities();
                task.setConnectionParameters(ClientActivity.HOST_NAME, ClientActivity.PORT, ClientActivity.USERNAME, ClientActivity.PASSWORD);
                task.setMode("DELETE", ListFragment.this.current, ListFragment.adapter);
                task.setDeleteParameters(ListFragment.this.root, ListFragment.this.remoteSelectedItems);
                task.execute(new Object[0]);
            }
        }).setNegativeButton("No", null).show();
    }

    public void downloadSelectedItems() {
        new Builder(getActivity()).setIcon(C0322R.drawable.download_dialog).setTitle("Download?...").setMessage("Are you sure you want to download " + this.remoteSelectedItems.size() + " item(s)").setPositiveButton("Yes", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ListFragment.this.current.getActivity().getBaseContext(), ListFragment.this.remoteSelectedItems.size() + " item(s) added to download queue...", 0).show();
                Intent intent = new Intent(ListFragment.this.getActivity().getBaseContext(), RemoteDownloadService.class);
                intent.putExtra("MESSENGER", new Messenger(ListFragment.this.messageHandler));
                intent.putExtra("IP", ClientActivity.HOST_NAME);
                intent.putExtra("USERNAME", ClientActivity.USERNAME);
                intent.putExtra("PASSWORD", ClientActivity.PASSWORD);
                intent.putExtra("PORT", ClientActivity.PORT);
                intent.putExtra("ROOT", ListFragment.this.root);
                intent.putExtra("SAVE_PATH", ClientActivity.LOCAL_DIRECTORY);
                intent.putStringArrayListExtra("DOWNLOAD_LIST", ListFragment.this.remoteSelectedItems);
                ListFragment.this.getActivity().startService(intent);
            }
        }).setNegativeButton("No", null).show();
    }

    public void uploadSelectedItems() {
        new Builder(getActivity()).setIcon(C0322R.drawable.upload_dialog).setTitle("Upload?...").setMessage("Are you sure you want to upload " + this.localSelectedItems.size() + " item(s)").setPositiveButton("Yes", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ListFragment.this.current.getActivity().getBaseContext(), ListFragment.this.localSelectedItems.size() + " item(s) added to upload queue...", 0).show();
                Intent intent = new Intent(ListFragment.this.getActivity().getBaseContext(), RemoteUploadService.class);
                intent.putExtra("MESSENGER", new Messenger(ListFragment.this.messageHandlerUpload));
                intent.putExtra("IP", ClientActivity.HOST_NAME);
                intent.putExtra("USERNAME", ClientActivity.USERNAME);
                intent.putExtra("PASSWORD", ClientActivity.PASSWORD);
                intent.putExtra("PORT", ClientActivity.PORT);
                intent.putExtra("LOCAL_DIR", ListFragment.this.root);
                intent.putExtra("REMOTE_SAVE_PATH", ClientActivity.REMOTE_DIRECTORY);
                intent.putStringArrayListExtra("UPLOAD_LIST", ListFragment.this.localSelectedItems);
                ListFragment.this.getActivity().startService(intent);
            }
        }).setNegativeButton("No", null).show();
    }

    public void createNewFolderRemote() {
        Builder builder = new Builder(this.current.getActivity());
        builder.setTitle("New Folder Name...");
        final EditText input = new EditText(this.current.getActivity());
        builder.setView(input);
        builder.setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                RemoteUtilities task = new RemoteUtilities();
                task.setMode("CREATE_FOLDER", ListFragment.this.current, ListFragment.adapter);
                task.setConnectionParameters(ClientActivity.HOST_NAME, ClientActivity.PORT, ClientActivity.USERNAME, ClientActivity.PASSWORD);
                task.setFolderCreationParameters(ListFragment.this.root, input.getText().toString());
                task.execute(new Object[0]);
            }
        });
        builder.setNegativeButton("Cancel", null);
        final AlertDialog dialog = builder.show();
        input.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(5);
                }
            }
        });
    }

    public void renameSelectedItemRemote() {
        Builder builder = new Builder(this.current.getActivity());
        builder.setTitle("New Name...");
        final EditText input = new EditText(this.current.getActivity());
        builder.setView(input);
        builder.setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                RemoteUtilities task = new RemoteUtilities();
                task.setMode("RENAME", ListFragment.this.current, ListFragment.adapter);
                task.setConnectionParameters(ClientActivity.HOST_NAME, ClientActivity.PORT, ClientActivity.USERNAME, ClientActivity.PASSWORD);
                task.setRenameParameters(ListFragment.this.root, (String) ListFragment.this.remoteSelectedItems.get(0), input.getText().toString());
                task.execute(new Object[0]);
            }
        });
        builder.setNegativeButton("Cancel", null);
        final AlertDialog dialog = builder.show();
        input.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(5);
                }
            }
        });
    }

    public void movePasteSelectedItemsRemote() {
        new Builder(getActivity()).setTitle("Move?...").setMessage("Are you sure you want to move " + remoteSelectedItemsMove.size() + " item(s) to " + remotePasteLocation).setPositiveButton("Yes", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                RemoteUtilities task = new RemoteUtilities();
                task.setMode("MOVE", ListFragment.this.current, ListFragment.adapter);
                task.setConnectionParameters(ClientActivity.HOST_NAME, ClientActivity.PORT, ClientActivity.USERNAME, ClientActivity.PASSWORD);
                task.setMoveParameters(ListFragment.remoteMovedFromLocation, ListFragment.remoteSelectedItemsMove, ListFragment.remotePasteLocation);
                task.execute(new Object[0]);
            }
        }).setNegativeButton("No", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ListFragment.remoteGlobalMoveFlag = Boolean.valueOf(false);
                ListFragment.remoteSelectedItemsMove.clear();
                ListFragment.this.updateLocalActionBar();
            }
        }).show();
    }
}
