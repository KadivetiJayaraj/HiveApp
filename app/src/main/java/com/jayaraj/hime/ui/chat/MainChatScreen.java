package com.jayaraj.hime.ui.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.jayaraj.hime.R;
import com.jayaraj.hime.ui.dashboard.MoreActvity;
import com.jayaraj.hime.util.Pager;

public class MainChatScreen extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

  private ViewPager viewPager;
  private TabLayout tabLayout;
  Pager mAdapter;
  Toolbar toolbar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_chat_screen);
    viewPager = findViewById(R.id.viewpager);
    toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);
    tabLayout = findViewById(R.id.tabs);
    mAdapter = new Pager(getSupportFragmentManager());
    mAdapter.addFragment(ChatsFragment.newInstance(), "CHATS");
    mAdapter.addFragment(CallsFragment.newInstance(), "CALLS");
    viewPager.setAdapter(mAdapter);
    tabLayout.setupWithViewPager(viewPager);
    viewPager.setCurrentItem(0);
    tabLayout.addOnTabSelectedListener(this);
  }

  @Override
  public void onTabSelected(TabLayout.Tab tab) {}

  @Override
  public void onTabUnselected(TabLayout.Tab tab) {}

  @Override
  public void onTabReselected(TabLayout.Tab tab) {}

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_settings, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {

    if (item.getItemId() == R.id.menu_settings) {
      /// Intent
      Intent intent = new Intent(MainChatScreen.this, MoreActvity.class);
      startActivity(intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
