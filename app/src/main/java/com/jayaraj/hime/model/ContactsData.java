package com.jayaraj.hime.model;

public class ContactsData {

  public String contactnumber;
  public String contactname;
  public String contactId;

  public ContactsData(String contactnumber, String contactname, String contactId) {
    this.contactnumber = contactnumber;
    this.contactname = contactname;
    this.contactId = contactId;
  }

  public ContactsData() {}

  public String getContactnumber() {
    return contactnumber;
  }

  public void setContactnumber(String contactnumber) {
    this.contactnumber = contactnumber;
  }

  public String getContactname() {
    return contactname;
  }

  public void setContactname(String contactname) {
    this.contactname = contactname;
  }

  public String getContactId() {
    return contactId;
  }

  public void setContactId(String contactId) {
    this.contactId = contactId;
  }
}
