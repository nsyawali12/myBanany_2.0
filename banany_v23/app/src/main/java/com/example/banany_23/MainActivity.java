package com.example.banany_23;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
// import android.databinding.DataBindingUtil;
// import android.support.v7.widget.LinearLayoutManager;

import com.example.banany_23.R;
// import com.example.banany_23.databinding.ActivityMainBanding;
// import com.example.banany_23.presentation.model.MainViewModel;


/*
* The Main activity, which display 2 Explorer
*
* */

public class MainActivity extends AppCompatActivity {

    /*
    *
    * Method called at activity Creation
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.Activity_main);
        //MainViewModel viewModel = new MainViewModel(this);
        //binding.setModel(viewModel);
    }


}
