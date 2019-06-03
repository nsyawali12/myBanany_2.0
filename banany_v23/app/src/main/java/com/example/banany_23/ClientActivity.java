package com.example.banany_23;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.PageTransformer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.widget.Toolbar;
import android.view.View;
import android.widget.Toolbar;

import java.util.List;
import java.util.Stack;

public class ClientActivity extends AppCompatActivity {

    public static String HOST_NAME;
    public static String LOCAL_DIRECTORY;
    public static String PASSWORD;
    public static int PORT;
    public static String REMOTE_DIRECTORY;
    public static String SERVER_NAME;
    public static String USERNAME;
    public static MyPagerAdapter adapterViewPager;
    public static Toolbar myToolbar;
    public static Stack<String> stackPage0 = new Stack();
    public static Stack<String> stackPage1 = new Stack();
    public static ViewPager vpPager;
    FtpDatabaseHelper helper = new FtpDatabaseHelper(this);

    class C03101 implements OnClickListener {
        C03101() {
        }

        public void onClick(DialogInterface dialog, int which) {
            while (ClientActivity.stackPage0.size() > 0) {
                ClientActivity.stackPage0.pop();
            }
            while (ClientActivity.stackPage1.size() > 0) {
                ClientActivity.stackPage1.pop();
            }
            ClientActivity.this.helper.deleteAll();
            ClientActivity.this.finish();
        }
    }

    public static class ZoomOutPageTransformer implements PageTransformer{
        private static final float MIN_ALPHA = 0.5f;
        private static final float MIN_SCALE = 0.85f;

        public void transformPage(View view, float position){
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();
            if (position < -1.0f){
                view.setAlpha(0.0f);
            } else if (position <= 1.0f){
                float scaleFactor = Math.max(MIN_SCALE, 1.0f - Math.abs(position));
                float vertMargin = (((float) pageHeight) * (1.0f - scaleFactor)) / 2.0f;
                float horzMargin = (((float) pageWidth) * (1.0f - scaleFactor)) / 2.0f;

                if (position < 0.0f) {
                    view.setTranslationX(horzMargin - (vertMargin / 2.0f));
                } else {
                    view.setTranslationX((-horzMargin) + (vertMargin / 2.0f));
                }
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
                view.setAlpha((((scaleFactor - MIN_SCALE) / 0.14999998f) * MIN_ALPHA) + MIN_ALPHA);
            } else {
                view.setAlpha(0.0f);
            }
        }
    }

    public static class MyPagerAdapter extends FragmentStatePagerAdapter {

        private static int NUM_ITEMS = 2;
        public static Bundle bundle1;
        public static Bundle bundle2;
        public static ListFragment changedFragment;
        public static ListFragment localFragment;
        public static ListFragment remoteFragment;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public static void setRootFirstPage(Bundle b) {
            bundle1 = b;
            ClientActivity.stackPage0.push(bundle1.getString("path"));
        }

        public static void setRootSecondPage(Bundle b) {
            bundle2 = b;
            ClientActivity.stackPage1.push(bundle2.getString("path"));
        }

        public int getCount() {
            return NUM_ITEMS;
        }

        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    remoteFragment = ListFragment.newInstance(bundle1, ClientActivity.adapterViewPager, ClientActivity.vpPager);
                    remoteFragment.setChangedFragmentID(remoteFragment.toString());
                    return remoteFragment;
                case 1:
                    localFragment = ListFragment.newInstance(bundle2, ClientActivity.adapterViewPager, ClientActivity.vpPager);
                    localFragment.setChangedFragmentID(localFragment.toString());
                    return localFragment;
                default:
                    return null;
            }
        }

        public int getItemPosition(Object object) {
            if (object.toString().equals(remoteFragment.toString()) && remoteFragment == changedFragment) {
                return -2;
            }
            if (object.toString().equals(localFragment.toString()) && localFragment == changedFragment) {
                return -2;
            }
            return -1;
        }

        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Remote";
            }
            return "Local";
        }

    }

    public void initializeCredentials(Bundle bundle) {
        SERVER_NAME = bundle.getString("SERVER_NAME");
        HOST_NAME = bundle.getString("HOST_NAME");
        PORT = bundle.getInt("PORT");
        USERNAME = bundle.getString("USERNAME");
        PASSWORD = bundle.getString("PASSWORD");
        LOCAL_DIRECTORY = bundle.getString("LOCAL_DIRECTORY");
        REMOTE_DIRECTORY = bundle.getString("REMOTE_DIRECTORY");
    }

    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView((int) C0322R.layout.activity_main);
       initializeCredentials(getIntent().getExtras());
       myToolbar = (Toolbar) findViewById(C0322R.id.my_toolbar);
       myToolbar.setTitle(SERVER_NAME);
       setSupportActionBar(myToolbar);
       vpPager = (ViewPager) findViewById(C0322R.id.pager);
       Bundle bundle = new Bundle();
       bundle.putString("path", REMOTE_DIRECTORY);
       bundle.putString("pageType", "remote");
       Bundle bundle2 = new Bundle();
       bundle2.putString("path", LOCAL_DIRECTORY);
       bundle2.putString("pageType", "local");
       MyPagerAdapter.setRootFirstPage(bundle);
       MyPagerAdapter.setRootSecondPage(bundle2);
       adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
       vpPager.setAdapter(adapterViewPager);
       vpPager.setPageTransformer(true, new ZoomOutPageTransformer());
    }

    public void onBackPressed(){
        Bundle bundle;
        if(vpPager.getCurrentItem() == 0){
            if (stackPage0.size() == 1){
                exitHandler();
                return;
            }
            stackPage0.pop();
            bundle = new Bundle();
            bundle.putString("path", (String) stackPage0.peek());
            bundle.putString("pageType", "remote");
            MyPagerAdapter.setRootFirstPage(bundle);
            MyPagerAdapter.changedFragment = MyPagerAdapter.remoteFragment;
            stackPage0.pop();
            adapterViewPager.notifyDataSetChanged();
        } else if (stackPage1.size() == 1){
            exitHandler();
        } else {
            stackPage1.pop();
            bundle = new Bundle();
            bundle.putString("path", (String) stackPage1.peek());
            bundle.putString("pageType", "local");
            MyPagerAdapter.setRootSecondPage(bundle);
            MyPagerAdapter.changedFragment = MyPagerAdapter.localFragment;
            stackPage1.pop();
            adapterViewPager.notifyDataSetChanged();
        }
    }

    public void exitHandler(){
        new Builder(this).setIcon(C0322R.drawable.alert).setTitle("Disconnect?").setMessage("Are you sure want to disconnect from the server").setPositiveButton("Yes", new C03101()).setNegativeButton("No", null).show();
    }

}
