package com.jayaraj.hime.ui.contacts;

import android.Manifest;
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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jayaraj.hime.R;
import com.jayaraj.hime.adapters.ContactsAdapter;
import com.jayaraj.hime.model.ContactsData;
import com.jayaraj.hime.model.FetchContacts;
import com.jayaraj.hime.model.StaticConfig;
import com.jayaraj.hime.ui.chat.ChatScreenActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class RegisteredContacts extends AppCompatActivity implements ContactsAdapter.ItemListener {

    private RecyclerView recyclerListContact;
    private ContactsAdapter adapter;
    ArrayList<ContactsData> selectUsers = new ArrayList<>();
    Cursor phones;
    public ArrayList<FetchContacts> dataListContact = new ArrayList<>();
    LinearLayout back;
    public ArrayList<String> listContactID = new ArrayList<>();
    RelativeLayout textvisibiliy;
    FloatingActionButton shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_contacts);
        back = findViewById(R.id.backview);
        back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });

        selectUsers = new ArrayList<>();
        dataListContact = new ArrayList<>();
        recyclerListContact = findViewById(R.id.contacts_list);
        textvisibiliy = findViewById(R.id.textvisibiliy);
        shareButton = findViewById(R.id.share_app);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do something
            }
        });

        requestPermission();
    }

    private void requestPermission() {
        Dexter.withActivity(RegisteredContacts.this)
                .withPermissions(Manifest.permission.READ_CONTACTS)
                .withListener(
                        new MultiplePermissionsListener() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    // do you work now
                                    getListContactUid();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisteredContacts.this);
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
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    void getListContactUid() {

        phones =
                getApplicationContext()
                        .getContentResolver()
                        .query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                null,
                                null,
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if (phones != null) {
            Log.e("count", "" + phones.getCount());
            if (phones.getCount() == 0) {
            }

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
                getRegisteredContacts();
            }
        } else {
            Log.e("Cursor close 1", "----------------");
        }
    }

    void getRegisteredContacts() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        listContactID = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                        if (postSnapshot.getValue() != null) {
                                            HashMap mapUserInfo = (HashMap) postSnapshot.getValue();
                                            FetchContacts fetchContacts = new FetchContacts();
                                            fetchContacts.phonenumber = (String) mapUserInfo.get("phonenumber");
                                            fetchContacts.profileImage = (String) mapUserInfo.get("profileImage");
                                            fetchContacts.userId = (String) mapUserInfo.get("userId");
                                            fetchContacts.about = (String) mapUserInfo.get("about");

                                            for (ContactsData contactsData : selectUsers) {
                                                if (contactsData.contactnumber.equals(fetchContacts.phonenumber)
                                                        && !user.getPhoneNumber().equals(fetchContacts.phonenumber)) {
                                                    fetchContacts.name = contactsData.contactname;
                                                    if (!listContactID.contains(fetchContacts.userId)) {
                                                        dataListContact.add(fetchContacts);
                                                        listContactID.add(fetchContacts.userId);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (dataListContact.isEmpty()) {
                                        textvisibiliy.setVisibility(View.VISIBLE);
                                        recyclerListContact.setVisibility(View.GONE);
                                        shareButton.setVisibility(View.VISIBLE);
                                    } else {
                                        setUpListView();
                                        textvisibiliy.setVisibility(View.GONE);
                                        recyclerListContact.setVisibility(View.VISIBLE);
                                        shareButton.setVisibility(View.VISIBLE);
                                        shareButton.setVisibility(View.GONE);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("Exc", databaseError.toString());
                            }
                        });
    }

    private void setUpListView() {

        Collections.sort(
                dataListContact,
                new Comparator<FetchContacts>() {
                    @Override
                    public int compare(FetchContacts s1, FetchContacts s2) {
                        return (s1.name).compareToIgnoreCase(s2.name);
                    }
                });
        adapter = new ContactsAdapter(dataListContact, this, RegisteredContacts.this);
        LinearLayoutManager llm = new LinearLayoutManager(RegisteredContacts.this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerListContact.setLayoutManager(llm);
        recyclerListContact.setHasFixedSize(true);
        recyclerListContact.setItemViewCacheSize(20);
        recyclerListContact.setAdapter(adapter);
    }

    @Override
    public void onItemClick(
            String friendName, String firendId, String friendImage, String friendPhone) {

        Intent intent = new Intent(RegisteredContacts.this, ChatScreenActivity.class);
        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND, friendName);
        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_AVATA, friendImage);
        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ID, firendId);
        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_PHONE, friendPhone);
        ChatScreenActivity.bitmapAvataFriend = new HashMap<>();
        if (!friendImage.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            byte[] decodedString = Base64.decode(friendImage, Base64.DEFAULT);
            ChatScreenActivity.bitmapAvataFriend.put(
                    firendId, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
        } else {
            ChatScreenActivity.bitmapAvataFriend.put(
                    firendId,
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_account_circle_black_24dp));
        }

        startActivity(intent);
        finish();
    }
}
