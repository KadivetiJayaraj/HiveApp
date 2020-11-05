package com.jayaraj.hime.util;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

// Extending FragmentStatePagerAdapter
public class Pager extends FragmentStatePagerAdapter {

  // integer to count number of tabs
  private final List<Fragment> fragmentList = new ArrayList<>();
  private final List<String> fragmentTitleList = new ArrayList<>();

  // Constructor to the class
  public Pager(FragmentManager fm) {
    super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    // Initializing tab count

  }

  @Override
  public Fragment getItem(int i) {
    return fragmentList.get(i);
  }

  @Override
  public int getCount() {
    return fragmentList.size();
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return fragmentTitleList.get(position);
  }

  public void addFragment(Fragment fragment, String title) {
    fragmentList.add(fragment);
    fragmentTitleList.add(title);
  }

  @Override
  public int getItemPosition(@NonNull Object object) {
    return super.getItemPosition(object);
  }
}
