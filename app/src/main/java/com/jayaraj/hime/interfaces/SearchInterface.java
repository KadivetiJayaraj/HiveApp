package com.jayaraj.hime.interfaces;

public interface SearchInterface {

  interface SearchText {
    void onTextSearched(String seachText);
  }

  void SetSearchListener(SearchText searchListener);

  void AddSearchBar();

  void searchClose();
}
