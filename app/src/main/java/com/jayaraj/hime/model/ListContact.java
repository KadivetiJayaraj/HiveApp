package com.jayaraj.hime.model;

import java.util.ArrayList;

public class ListContact {
  private ArrayList<FetchContacts> listContact;

  public ArrayList<FetchContacts> getListContact() {
    return listContact;
  }

  public ListContact() {
    listContact = new ArrayList<>();
  }

  public String getAvataById(String id) {
    for (FetchContacts contact : listContact) {
      if (id.equals(contact.userId)) {
        return contact.profileImage;
      }
    }
    return "";
  }

  public void setListContact(ArrayList<FetchContacts> listContact) {
    this.listContact = listContact;
  }
}
