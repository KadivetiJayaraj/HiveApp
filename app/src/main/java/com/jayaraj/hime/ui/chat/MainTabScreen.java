package com.jayaraj.hime.ui.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.jayaraj.hime.R;
import com.jayaraj.hime.interfaces.SearchInterface;
import com.jayaraj.hime.util.Animation;
import com.jayaraj.hime.util.Pager;

import java.lang.reflect.Field;

public class MainTabScreen extends AppCompatActivity
    implements TabLayout.OnTabSelectedListener,
        SearchView.OnClickListener,
        SearchView.OnCloseListener,
        SearchView.OnQueryTextListener,
        SearchInterface {

  ViewPager chatViewPager;
  TabLayout chatTablayout;
  Pager adapter;
  Toolbar toolbar;
  ImageView chatBack;

  RelativeLayout toolbarLayout;
  AppBarLayout.LayoutParams params;

  public SearchView searchView;
  public Toolbar searchtollbar;
  Menu search_menu;
  public MenuItem item_search;
  SearchInterface.SearchText mSearchListener;
  private FloatingActionButton floatButton;

  int chatMessageCount = 0;
  int groupMessageCount = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_tab_screen);

    toolbar = findViewById(R.id.toolbar);
    toolbarLayout = findViewById(R.id.toolbarId);
    params = (AppBarLayout.LayoutParams) toolbarLayout.getLayoutParams();
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);
    setSearchtollbar();
    chatTablayout = findViewById(R.id.chatsTablayout);
    chatViewPager = findViewById(R.id.viewpager);
    chatBack = findViewById(R.id.back);
    floatButton = findViewById(R.id.get_friendsList);

    adapter = new Pager(getSupportFragmentManager());
    adapter.addFragment(ChatsFragment.newInstance(), "Chats");
    adapter.addFragment(GroupsFragment.newInstance(), "Groups");

    // Adding adapter to pager
    floatButton.setOnClickListener(
        ((ChatsFragment) adapter.getItem(0)).onClickFloatButton.getInstance(MainTabScreen.this));

    chatViewPager.setAdapter(adapter);
    chatTablayout.setupWithViewPager(chatViewPager);
    chatViewPager.setCurrentItem(0);

    chatViewPager.setOffscreenPageLimit(2);
    chatTablayout.addOnTabSelectedListener(this);
    chatViewPager.addOnPageChangeListener(
        new ViewPager.OnPageChangeListener() {
          @Override
          public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // Do Something
            if (adapter.getItem(position) instanceof ChatsFragment) {
              floatButton.setVisibility(View.VISIBLE);
              floatButton.setOnClickListener(
                  ((ChatsFragment) adapter.getItem(position))
                      .onClickFloatButton.getInstance(MainTabScreen.this));

              floatButton.setImageResource(R.drawable.ic_person_add_black_24dp);
            } else if (adapter.getItem(position) instanceof GroupsFragment) {
              floatButton.setVisibility(View.VISIBLE);
              floatButton.setOnClickListener(
                  ((GroupsFragment) adapter.getItem(position))
                      .onClickFloatButton.getInstance(MainTabScreen.this));
              floatButton.setImageResource(R.drawable.ic_group_add_black_24dp);
            }
          }

          @Override
          public void onPageSelected(int position) {
            if (adapter.getItem(position) instanceof ChatsFragment) {
              floatButton.setVisibility(View.VISIBLE);
              floatButton.setOnClickListener(
                  ((ChatsFragment) adapter.getItem(position))
                      .onClickFloatButton.getInstance(MainTabScreen.this));

              floatButton.setImageResource(R.drawable.ic_person_add_black_24dp);
            } else if (adapter.getItem(position) instanceof GroupsFragment) {
              floatButton.setVisibility(View.VISIBLE);
              floatButton.setOnClickListener(
                  ((GroupsFragment) adapter.getItem(position))
                      .onClickFloatButton.getInstance(MainTabScreen.this));
              floatButton.setImageResource(R.drawable.ic_group_add_black_24dp);
            }
          }

          @Override
          public void onPageScrollStateChanged(int state) {
            // Do Something
          }
        });
  }

  @Override
  public void onClick(View v) {}

  @Override
  public boolean onClose() {
    return false;
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    return false;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    return false;
  }

  @Override
  public void onTabSelected(TabLayout.Tab tab) {

    chatViewPager.setCurrentItem(tab.getPosition());
    chatViewPager.setVisibility(View.VISIBLE);
    if (searchtollbar.getVisibility() == View.VISIBLE) {
      if (!searchView.isIconified()) {
        searchView.setIconified(false);
      }
      item_search.collapseActionView();
      showTabLayout();
    }
  }

  @Override
  public void onTabUnselected(TabLayout.Tab tab) {
    // Do Something for Tab Unselected
  }

  @Override
  public void onTabReselected(TabLayout.Tab tab) {
    // Do Something for Tab Unselected
  }

  public void showTabLayout() {
    chatTablayout.setVisibility(View.VISIBLE);
    params.setScrollFlags(
        AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
            | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
  }

  public void hideTabLayout() {
    chatTablayout.setVisibility(View.GONE);
    params.setScrollFlags(0);
  }

  public void setSearchtollbar() {
    searchtollbar = findViewById(R.id.searchtoolbar);
    if (searchtollbar != null) {
      searchtollbar.inflateMenu(R.menu.menu_search);
      search_menu = searchtollbar.getMenu();

      searchtollbar.setNavigationOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                Animation.circleReveal(R.id.searchtoolbar, 1, true, false, MainTabScreen.this);
              else searchtollbar.setVisibility(View.GONE);
            }
          });

      item_search = search_menu.findItem(R.id.action_filter_search);

      initSearchView();

    } else Log.d("toolbar", "setSearchtollbar: NULL");
  }

  public void initSearchView() {
    searchView = (SearchView) search_menu.findItem(R.id.action_filter_search).getActionView();

    // Enable/Disable Submit button in the keyboard

    searchView.setSubmitButtonEnabled(false);

    // Change search close button image

    ImageView closeButton = searchView.findViewById(R.id.search_close_btn);
    closeButton.setImageResource(R.drawable.ic_close_black_24dp);

    // set hint and the text colors

    EditText txtSearch = searchView.findViewById(R.id.search_src_text);
    txtSearch.setHint("Search...");
    txtSearch.setHintTextColor(Color.LTGRAY);
    txtSearch.setTextSize(17);
    txtSearch.setTextColor(getResources().getColor(R.color.black));

    // set the cursor

    AutoCompleteTextView searchTextView = searchView.findViewById(R.id.search_src_text);
    try {
      Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
      mCursorDrawableRes.setAccessible(true);
      mCursorDrawableRes.set(
          searchTextView,
          R.drawable
              .my_cursor); // This sets the cursor resource ID to 0 or @null which will make it
      // visible on white background
    } catch (Exception e) {
      Log.e("exc", e.toString());
    }
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_search:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
          Animation.circleReveal(R.id.searchtoolbar, 1, true, true, MainTabScreen.this);
        else searchtollbar.setVisibility(View.VISIBLE);

        item_search.setOnActionExpandListener(
            new MenuItem.OnActionExpandListener() {
              @Override
              public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
              }

              @Override
              public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                  Animation.circleReveal(R.id.searchtoolbar, 1, true, false, MainTabScreen.this);
                } else searchtollbar.setVisibility(View.GONE);
                showTabLayout();
                return true;
              }
            });
        hideTabLayout();

        item_search.expandActionView();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void SetSearchListener(SearchText searchListener) {}

  @Override
  public void AddSearchBar() {}

  @Override
  public void searchClose() {}
}
