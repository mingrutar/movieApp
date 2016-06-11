package com.coderming.movieapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.coderming.movieapp.utils.Constants;
import com.coderming.movieapp.utils.Utilities;

public class DetailActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);  //show back button

       if (savedInstanceState == null) {
           Intent intent = getIntent();
           Bundle bundle = new Bundle();
           if (intent != null) {
               final Uri uri = intent.getData();
               bundle.putParcelable(Constants.DETAIL_URI, uri);
               DetailFragment df = new DetailFragment();
               df.setArguments(bundle);
               FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
               ft.add(R.id.detail_container, df).commit();
           }
       }
    }
}
