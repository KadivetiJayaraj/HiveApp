package com.jayaraj.hime.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jayaraj.hime.R;
import com.jayaraj.hime.interfaces.SearchInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass. Use the {@link GroupsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupsFragment extends Fragment {

  public FragGroupClickFloatButton onClickFloatButton;

  public static GroupsFragment newInstance() {
    return new GroupsFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  public GroupsFragment() {
    onClickFloatButton = new FragGroupClickFloatButton();
  }

  public class FragGroupClickFloatButton implements View.OnClickListener {
    Context context;

    public FragGroupClickFloatButton getInstance(Context context) {
      this.context = context;
      return this;
    }

    @Override
    public void onClick(View view) {
      //      Intent intent = new Intent(getContext(), AddGroupParticipants.class);
      //      startActivity(intent);
    }
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_groups, container, false);
    setHasOptionsMenu(true);

    return view;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_home, menu);
    ((MainTabScreen) getActivity()).AddSearchBar();

    SearchInterface.SearchText searchedText =
        new SearchInterface.SearchText() {
          @Override
          public void onTextSearched(String seachText) {}
        };
    ((MainTabScreen) getActivity()).SetSearchListener(searchedText);
  }
}
