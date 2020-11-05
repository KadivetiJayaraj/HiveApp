package com.jayaraj.hime.ui.chat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jayaraj.hime.R;
import com.jayaraj.hime.adapters.ChatRoomAdapter;
import com.jayaraj.hime.interfaces.SearchInterface;
import com.jayaraj.hime.model.Chats;
import com.jayaraj.hime.model.ContactsData;
import com.jayaraj.hime.model.StaticConfig;
import com.jayaraj.hime.model.UserProfile;
import com.jayaraj.hime.ui.contacts.RegisteredContacts;
import com.jayaraj.hime.util.SharedPrefManager;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ChatsFragment extends Fragment implements ChatRoomAdapter.ItemListener {
  private RecyclerView recyclerListFrends;
  private ChatRoomAdapter adapter;

  ArrayList<ContactsData> selectUsers = new ArrayList<>();
  ArrayList<Chats> chatItems = new ArrayList<>();

  private boolean shouldRefreshOnResume = false;
  RelativeLayout textvisibiliy;
  public FragFriendClickFloatButton onClickFloatButton;

  public static ChatsFragment newInstance() {

    return new ChatsFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  public ChatsFragment() {
    onClickFloatButton = new FragFriendClickFloatButton();
  }

  public class FragFriendClickFloatButton implements View.OnClickListener {
    Context context;

    public FragFriendClickFloatButton() {}

    public FragFriendClickFloatButton getInstance(Context context) {
      this.context = context;
      return this;
    }

    @Override
    public void onClick(final View view) {
      Intent intent = new Intent(getContext(), RegisteredContacts.class);
      startActivity(intent);
    }
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View layout = inflater.inflate(R.layout.fragment_chats, container, false);
    recyclerListFrends = layout.findViewById(R.id.chatRoomList);
    textvisibiliy = layout.findViewById(R.id.textvisibiliy);
    setHasOptionsMenu(true);

    requestPermission();

    return layout;
  }

  @Override
  public void onResume() {
    super.onResume();
    // Check should we need to refresh the fragment
    if (shouldRefreshOnResume) {
      // refresh fragment
      requestPermission();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    shouldRefreshOnResume = true;
  }

  private void requestPermission() {
    Dexter.withActivity(getActivity())
        .withPermissions(Manifest.permission.READ_CONTACTS)
        .withListener(
            new MultiplePermissionsListener() {
              @RequiresApi(api = Build.VERSION_CODES.M)
              @Override
              public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                  // do you work now
                  getContactsList();
                }
                for (int i = 0; i < report.getDeniedPermissionResponses().size(); i++) {
                  // check for permanent denial of any permission

                }
                // check for permanent denial of any permission
                if (report.isAnyPermissionPermanentlyDenied()) {
                  // permission is denied permenantly, navigate user to app settings
                  showSettingsDialog();
                }
              }

              @Override
              public void onPermissionRationaleShouldBeShown(
                  List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
              }
            })
        .check();
  }

  private void showSettingsDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Need Permissions");
    builder.setMessage(
        "This app needs permission to use this feature. You can grant them in app settings.");
    builder.setPositiveButton(
        "GOTO SETTINGS",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            openSettings();
          }
        });
    builder.setNegativeButton(
        "Cancel",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });
    builder.show();
  }

  private void openSettings() {
    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
    intent.setData(uri);
    startActivityForResult(intent, 101);
  }

  private void getContactsList() {
    selectUsers = new ArrayList<>();
    chatItems = new ArrayList<>();
    Cursor phones =
        getActivity()
            .getApplicationContext()
            .getContentResolver()
            .query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

    if (phones != null) {
      Log.e("count", "" + phones.getCount());
      if (phones.getCount() == 0) {}

      while (phones.moveToNext()) {
        String contactId =
            phones.getString(
                phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
        String contactName =
            phones.getString(
                phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        String contactNumber =
            phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

        contactNumber = contactNumber.replaceAll("\\s+", "");
        if (!contactNumber.startsWith("+91")) {
          contactNumber = contactNumber.replaceFirst("", "+91");
        }

        ContactsData contactsData = new ContactsData();
        contactsData.contactId = contactId;
        contactsData.contactname = contactName;
        contactsData.contactnumber = contactNumber;
        selectUsers.add(contactsData);
      }
      if (selectUsers != null) {
        getChats();
      }
    } else {
      Log.e("Cursor close 1", "----------------");
    }
  }

  private void getChats() {
    chatItems = new ArrayList<>();

    shouldRefreshOnResume = false;

    UserProfile userProfile =
        SharedPrefManager.getInstance(getActivity().getApplicationContext()).getLoginUser();
    String profileId = userProfile.userId;

    final DatabaseReference databaseReference =
        FirebaseDatabase.getInstance().getReference().child("chatsnippets").child(profileId);

    databaseReference.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {

            if (snapshot.exists()) {

              for (final DataSnapshot postSnapshot : snapshot.getChildren()) {
                if (postSnapshot.getValue() != null) {
                  HashMap mapUserInfo = (HashMap) postSnapshot.getValue();
                  Chats user = new Chats();
                  user.profileImage = (String) mapUserInfo.get("profileImage");
                  user.messageType = (String) mapUserInfo.get("messagetype");
                  user.messageBody = (String) mapUserInfo.get("messageBody");
                  user.timestamp = (String) mapUserInfo.get("timestamp");
                  user.idRoom = (String) mapUserInfo.get("roomId");
                  user.userId = (String) mapUserInfo.get("himeID");
                  user.phonenumber = (String) mapUserInfo.get("phoneNumber");
                  user.name = (String) mapUserInfo.get("name");
                  chatItems.add(user);
                  for (ContactsData contactsData : selectUsers) {
                    if (contactsData.contactnumber.equals(user.phonenumber)) {
                      user.name = contactsData.contactname;
                    }
                  }
                }
              }
              if (chatItems.isEmpty()) {
                textvisibiliy.setVisibility(View.VISIBLE);
                recyclerListFrends.setVisibility(View.GONE);
              } else {
                Collections.sort(
                    chatItems,
                    new Comparator<Chats>() {
                      SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy HH:mm sss");

                      @Override
                      public int compare(Chats lhs, Chats rhs) {
                        try {
                          return f.parse(rhs.timestamp).compareTo(f.parse(lhs.timestamp));
                        } catch (ParseException e) {
                          throw new IllegalArgumentException(e);
                        }
                      }
                    });

                textvisibiliy.setVisibility(View.GONE);
                recyclerListFrends.setVisibility(View.VISIBLE);
              }

            } else {
              textvisibiliy.setVisibility(View.VISIBLE);
              recyclerListFrends.setVisibility(View.GONE);
            }
            setUpListView();
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {
            Log.e("StrErr", error.toString());
          }
        });

    /* databaseReference.addListenerForSingleValueEvent(
    new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        if (dataSnapshot.exists()) {
          for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            if (postSnapshot.getValue() != null) {
              HashMap mapUserInfo = (HashMap) postSnapshot.getValue();
              Chats user = new Chats();
              user.profileImage = (String) mapUserInfo.get("profileImage");
              user.messageType = (String) mapUserInfo.get("messagetype");
              user.messageBody = (String) mapUserInfo.get("messageBody");
              user.timestamp = (String) mapUserInfo.get("timestamp");
              user.idRoom = (String) mapUserInfo.get("roomId");
              user.userId = (String) mapUserInfo.get("himeID");
              user.phonenumber = (String) mapUserInfo.get("phoneNumber");
              user.name = (String) mapUserInfo.get("name");
              chatItems.add(user);
              for (ContactsData contactsData : selectUsers) {
                if (contactsData.contactnumber.equals(user.phonenumber)) {
                  user.name = contactsData.contactname;
                }
              }
            }
          }

          if (chatItems.isEmpty()) {

          } else {
            setUpListView();
            adapter.notifyDataSetChanged();
          }
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.e("Exc", databaseError.toString());
      }
    });*/
  }

  private void setUpListView() {
    adapter = new ChatRoomAdapter(chatItems, this, getActivity());
    LinearLayoutManager llm = new LinearLayoutManager(getContext());
    llm.setOrientation(LinearLayoutManager.VERTICAL);
    recyclerListFrends.setLayoutManager(llm);
    recyclerListFrends.setHasFixedSize(true);
    recyclerListFrends.setItemViewCacheSize(20);
    RecyclerView.ItemAnimator animator = recyclerListFrends.getItemAnimator();
    if (animator instanceof SimpleItemAnimator) {
      ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
    }
    recyclerListFrends.setAdapter(adapter);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_home, menu);
    ((MainTabScreen) getActivity()).AddSearchBar();

    SearchInterface.SearchText searchedText =
        new SearchInterface.SearchText() {
          @Override
          public void onTextSearched(String seachText) {
            final List<Chats> filtermodelist = filter(chatItems, seachText);
            adapter.setfilter(filtermodelist);
            adapter.notifyDataSetChanged();
          }
        };
    ((MainTabScreen) getActivity()).SetSearchListener(searchedText);
  }

  private List<Chats> filter(List<Chats> pl, String query) {
    final List<Chats> filteredModeList = new ArrayList<>();
    for (Chats model : pl) {
      final String text = model.name.toLowerCase();
      if (text.contains(query.toLowerCase())) {
        filteredModeList.add(model);
      }
    }
    return filteredModeList;
  }

  @Override
  public void onItemClick(
      String friendName, String friendId, String friendImage, String friendPhone) {

    Intent intent = new Intent(getActivity(), ChatScreenActivity.class);
    intent.putExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND, friendName);
    intent.putExtra(StaticConfig.INTENT_KEY_CHAT_AVATA, friendImage);
    intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ID, friendId);
    intent.putExtra(StaticConfig.INTENT_KEY_CHAT_PHONE, friendPhone);
    ChatScreenActivity.bitmapAvataFriend = new HashMap<>();
    if (!friendImage.equals(StaticConfig.STR_DEFAULT_BASE64)) {
      byte[] decodedString = Base64.decode(friendImage, Base64.DEFAULT);
      ChatScreenActivity.bitmapAvataFriend.put(
          friendId, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
    } else {
      ChatScreenActivity.bitmapAvataFriend.put(
          friendId,
          BitmapFactory.decodeResource(
              getContext().getResources(), R.drawable.ic_account_circle_black_24dp));
    }

    startActivity(intent);
  }
}
