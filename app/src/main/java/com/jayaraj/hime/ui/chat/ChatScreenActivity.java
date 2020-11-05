package com.jayaraj.hime.ui.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jayaraj.hime.R;
import com.jayaraj.hime.adapters.MessagesListAdapter;
import com.jayaraj.hime.emoji.Emojicon;
import com.jayaraj.hime.emoji.EmojiconGridView;
import com.jayaraj.hime.emoji.EmojiconsPopup;
import com.jayaraj.hime.interfaces.APIService;
import com.jayaraj.hime.model.Message;
import com.jayaraj.hime.model.StaticConfig;
import com.jayaraj.hime.model.UserProfile;
import com.jayaraj.hime.notifications.Client;
import com.jayaraj.hime.notifications.Data;
import com.jayaraj.hime.notifications.RetrofitResponse;
import com.jayaraj.hime.notifications.Sender;
import com.jayaraj.hime.ui.quickblox.activities.BaseActivity;
import com.jayaraj.hime.ui.quickblox.activities.CallActivity;
import com.jayaraj.hime.ui.quickblox.activities.PermissionsActivity;
import com.jayaraj.hime.ui.quickblox.adapters.UsersAdapter;
import com.jayaraj.hime.ui.quickblox.db.QbUsersDbManager;
import com.jayaraj.hime.ui.quickblox.services.CallService;
import com.jayaraj.hime.ui.quickblox.services.LoginService;
import com.jayaraj.hime.ui.quickblox.utils.CollectionsUtils;
import com.jayaraj.hime.ui.quickblox.utils.Consts;
import com.jayaraj.hime.ui.quickblox.utils.PermissionsChecker;
import com.jayaraj.hime.ui.quickblox.utils.PushNotificationSender;
import com.jayaraj.hime.ui.quickblox.utils.SharedPrefsHelper;
import com.jayaraj.hime.ui.quickblox.utils.ToastUtils;
import com.jayaraj.hime.ui.quickblox.utils.WebRtcSessionManager;
import com.jayaraj.hime.util.Constants;
import com.jayaraj.hime.util.FileUtil;
import com.jayaraj.hime.util.ImageCompressor;
import com.jayaraj.hime.util.ImageUtils;
import com.jayaraj.hime.util.SharedPrefManager;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.GenericQueryRule;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter;
import com.sangcomz.fishbun.define.Define;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

public class ChatScreenActivity extends BaseActivity implements View.OnClickListener {

    private Object obb;
    private List<QBUser> selectedcaller = new ArrayList<>();

    private static final String LOG_TAG = ChatScreenActivity.class.getSimpleName();
    private PermissionsChecker checker;
    private QBUser currentUser;
    private UsersAdapter usersAdapter;
    private QbUsersDbManager dbManager;
    private static final int PER_PAGE_SIZE_100 = 100;
    private static final String ORDER_RULE = "order";
    private static final String ORDER_DESC_UPDATED = "desc date updated_at";

    private static PopupWindow popWindow;
    RecyclerView recyclerChat;
    RelativeLayout contentRoot;
    MessagesListAdapter adapter;
    String nameFriend;
    String profileImage;
    String chatFriendImage;
    String idFriend;
    String friendPhone;
    String profileId;
    String profileName;
    String profilePhone;
    ImageButton btnSend;
    ImageButton btnAttach;
    ImageButton btnCamera;
    ImageButton btnEmoji;
    EditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;
    public static HashMap<String, Bitmap> bitmapAvataFriend;
    EmojiconEditText emojiconEditText;
    public Uri filePath;
    TextView nameFriendText;
    CircleImageView chat_avatar;
    ImageView back, audio_call, video_call;
    ArrayList<Uri> uriArrayList;
    String uniqueId;
    DatabaseReference mRootReference;
    List<Message> messageitems = new ArrayList<>();
    ValueEventListener seenListener;
    APIService apiService;

    int messagesCount = 0;
    DatabaseReference mDatabaseReference;
    private ValueEventListener valueEventListener;

    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private String mFirstKey = "";
    private static final int TOTAL_ITEMS_TO_LOAD = 30;
    private int mCurrentPage = 1;

    private SwipeRefreshLayout mRefreshLayout;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);
        audio_call = (ImageView) findViewById(R.id.audio_call);
        video_call = (ImageView) findViewById(R.id.video_call);
        mRefreshLayout = findViewById(R.id.swipeRefresh);
        editWriteMessage = (EditText) findViewById(R.id.editWriteMessage);
        nameFriendText = (TextView) findViewById(R.id.nameFriend);
        back = (ImageView) findViewById(R.id.back);
        chat_avatar = (CircleImageView) findViewById(R.id.chat_avatar);
        btnEmoji = (ImageButton) findViewById(R.id.btnEmoji);
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnAttach = (ImageButton) findViewById(R.id.btnAttach);
        btnSend.setOnClickListener(ChatScreenActivity.this);
        btnCamera = (ImageButton) findViewById(R.id.btnCamera);
        recyclerChat = (RecyclerView) findViewById(R.id.recyclerChat);
        contentRoot = (RelativeLayout) findViewById(R.id.contentRoot);
        emojiconEditText = (EmojiconEditText) findViewById(R.id.editEmojicon);
        final View rootView = findViewById(R.id.contentRoot);
        final ImageView emojiButton = (ImageView) findViewById(R.id.btnEmoji);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        audio_call.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (checkIsLoggedInChat()) {
                            startCall(false);
                        }
                        if (checker.lacksPermissions(Consts.PERMISSIONS[1])) {
                            startPermissionsActivity(true);
                        }
                    }
                });

        video_call.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (checkIsLoggedInChat()) {

                            startCall(true);
                        }
                        if (checker.lacksPermissions(Consts.PERMISSIONS)) {
                            startPermissionsActivity(false);
                        }
                    }
                });

        currentUser = SharedPrefsHelper.getInstance().getQbUser();
        dbManager = QbUsersDbManager.getInstance(getApplicationContext());
        checker = new PermissionsChecker(getApplicationContext());
        startLoginService();

        Intent intentData = getIntent();
        idFriend = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_ID);
        nameFriend = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND);
        chatFriendImage = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_AVATA);
        friendPhone = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_PHONE);

        UserProfile userProfile = SharedPrefManager.getInstance(getApplicationContext()).getLoginUser();
        profileId = userProfile.userId;
        profileImage = userProfile.profileImage;
        profileName = userProfile.name;
        profilePhone = userProfile.phonenumber;

        mRootReference = FirebaseDatabase.getInstance().getReference();

        load();
        seenMessage();

        requestPermission();
        mRootReference = FirebaseDatabase.getInstance().getReference();

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerChat.setLayoutManager(linearLayoutManager);
        messageitems = new ArrayList<>();
        recyclerChat.setNestedScrollingEnabled(false);
        recyclerChat.setHasFixedSize(false);
        adapter = new MessagesListAdapter(messageitems, ChatScreenActivity.this, profileId, idFriend);
        recyclerChat.setAdapter(adapter);

        if (idFriend != null && nameFriend != null) {
            nameFriendText.setText(nameFriend);
            try {
                Resources res = getResources();
                Bitmap src;
                if (chatFriendImage.equals("default")) {
                    src = BitmapFactory.decodeResource(res, R.drawable.ic_account_circle_black_24dp);
                } else {
                    byte[] decodedString = Base64.decode(chatFriendImage, Base64.DEFAULT);
                    src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                }

                chat_avatar.setImageDrawable(ImageUtils.roundedImage(this, src));

            } catch (Exception e) {
                Log.e("Exc", e.toString());
            }
        }

        back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

        btnCamera.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cameraIntent();
                    }
                });
        btnAttach.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openOptionsMenu();
                    }
                });

        // EMOJIS DECLARATIONS

        emojiconEditText.setText(""); // set status to current value
        final EmojiconsPopup popup = new EmojiconsPopup(rootView, this);
        popup.setSizeForSoftKeyboard();
        popup.setOnDismissListener(
                new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.smiley);
                    }
                });

        popup.setOnSoftKeyboardOpenCloseListener(
                new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {
                    @Override
                    public void onKeyboardOpen(int keyBoardHeight) {
                    }

                    @Override
                    public void onKeyboardClose() {
                        if (popup.isShowing()) popup.dismiss();
                    }
                });
        popup.setOnEmojiconClickedListener(
                new EmojiconGridView.OnEmojiconClickedListener() {

                    @Override
                    public void onEmojiconClicked(Emojicon emojicon) {
                        if (editWriteMessage == null || emojicon == null) {
                            return;
                        }

                        int start = editWriteMessage.getSelectionStart();
                        int end = editWriteMessage.getSelectionEnd();
                        if (start < 0) {
                            editWriteMessage.append(emojicon.getEmoji());
                        } else {
                            editWriteMessage
                                    .getText()
                                    .replace(
                                            Math.min(start, end),
                                            Math.max(start, end),
                                            emojicon.getEmoji(),
                                            0,
                                            emojicon.getEmoji().length());
                        }
                    }
                });

        popup.setOnEmojiconBackspaceClickedListener(
                new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

                    @Override
                    public void onEmojiconBackspaceClicked(View v) {
                        KeyEvent event =
                                new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                        editWriteMessage.dispatchKeyEvent(event);
                    }
                });
        emojiButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!popup.isShowing()) {
                            if (popup.isKeyBoardOpen()) {
                                popup.showAtBottom();
                                changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                            } else {
                                editWriteMessage.setFocusableInTouchMode(true);
                                editWriteMessage.requestFocus();
                                popup.showAtBottomPending();
                                final InputMethodManager inputMethodManager =
                                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputMethodManager.showSoftInput(
                                        editWriteMessage, InputMethodManager.SHOW_IMPLICIT);
                                changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                            }
                        } else {
                            popup.dismiss();
                        }
                    }
                });

        editWriteMessage.setHint("Message " + nameFriend);
        btnSend.setEnabled(false);
        btnSend.setColorFilter(
                ContextCompat.getColor(ChatScreenActivity.this, R.color.tab_unselected_color), PorterDuff.Mode.SRC_IN);

        editWriteMessage.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // something
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // something
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.length() == 0) {
                            btnSend.setEnabled(false);
                            btnSend.setColorFilter(
                                    ContextCompat.getColor(ChatScreenActivity.this, R.color.tab_unselected_color),
                                    PorterDuff.Mode.SRC_IN);
                        } else {
                            btnSend.setEnabled(true);
                            btnSend.setColorFilter(
                                    ContextCompat.getColor(ChatScreenActivity.this, R.color.colorAccent),
                                    PorterDuff.Mode.SRC_IN);
                        }
                    }
                });

        recyclerChat.addOnLayoutChangeListener(
                new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(
                            View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                        if (i3 < i7) {
                            recyclerChat.postDelayed(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if (messageitems.size() > 0) {
                                                recyclerChat.scrollToPosition(messageitems.size() - 1);
                                            }
                                        }
                                    },
                                    0);
                        }
                    }
                });

        mRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        mCurrentPage++;
                        itemPos = 0;
                        getMoreMessages();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isIncomingCall =
                SharedPrefsHelper.getInstance().get(Consts.EXTRA_IS_INCOMING_CALL, false);
        if (isCallServiceRunning(CallService.class)) {
            Log.d(LOG_TAG, "CallService is running now");
            CallActivity.start(this, isIncomingCall);
        }
        clearAppNotifications();
        loadUsers();
    }

    private void loadUsers() {
        ArrayList<GenericQueryRule> rules = new ArrayList<>();
        rules.add(new GenericQueryRule(ORDER_RULE, ORDER_DESC_UPDATED));

        QBPagedRequestBuilder qbPagedRequestBuilder = new QBPagedRequestBuilder();
        qbPagedRequestBuilder.setRules(rules);
        qbPagedRequestBuilder.setPerPage(PER_PAGE_SIZE_100);

        requestExecutor.loadLastUpdatedUsers(
                qbPagedRequestBuilder,
                new QBEntityCallback<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                        Log.d(LOG_TAG, "Successfully loaded Last 100 created users");
                        dbManager.saveAllUsers(qbUsers, true);
                        initUsersList();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.d(LOG_TAG, "Error load users" + e.getMessage());
                        showErrorSnackbar(R.string.loading_users_error, e, v -> loadUsers());
                    }
                });
    }

    private void initUsersList() {

        if (selectedcaller != null) {
            selectedcaller.clear();
        }
        List<QBUser> currentOpponentsList = dbManager.getAllUsers();
        Log.d(LOG_TAG, "initUsersList currentOpponentsList= " + currentOpponentsList);
        currentOpponentsList.remove(sharedPrefsHelper.getQbUser());

        for (Iterator i = currentOpponentsList.iterator(); i.hasNext(); ) {

            obb = i.next();
            if (obb.toString().contains(friendPhone)) {

                selectedcaller.add((QBUser) obb);
                // Toast.makeText(this, selectedcaller.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isCallServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void clearAppNotifications() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    private void startPermissionsActivity(boolean checkOnlyAudio) {
        PermissionsActivity.startActivity(this, checkOnlyAudio, Consts.PERMISSIONS);
    }

    private boolean checkIsLoggedInChat() {
        if (!QBChatService.getInstance().isLoggedIn()) {
            startLoginService();
            ToastUtils.shortToast(R.string.dlg_relogin_wait);
            return false;
        }
        return true;
    }

    private void startLoginService() {
        if (sharedPrefsHelper.hasQbUser()) {
            QBUser qbUser = sharedPrefsHelper.getQbUser();

            LoginService.start(this, qbUser);
        }
    }

    private void startCall(boolean isVideoCall) {
        Log.d(LOG_TAG, "Starting Call");

        ArrayList<Integer> opponentsList = CollectionsUtils.getIdsSelectedOpponents(selectedcaller);
        QBRTCTypes.QBConferenceType conferenceType =
                isVideoCall
                        ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
                        : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
        Log.d(LOG_TAG, "conferenceType = " + conferenceType);

        QBRTCClient qbrtcClient = QBRTCClient.getInstance(getApplicationContext());
        QBRTCSession newQbRtcSession =
                qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);
        WebRtcSessionManager.getInstance(this).setCurrentSession(newQbRtcSession);
        PushNotificationSender.sendPushMessage(opponentsList, currentUser.getFullName());
        CallActivity.start(this, false);
    }

    private void requestPermission() {
        Dexter.withActivity(ChatScreenActivity.this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(
                        new MultiplePermissionsListener() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    // do you work now

                                }
                                for (int i = 0; i < report.getDeniedPermissionResponses().size(); i++) {
                                    // Do Something
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

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatScreenActivity.this);
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

    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId) {
        iconToBeChanged.setImageResource(drawableResourceId);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        filePath = FileUtil.createCapturedFile(ChatScreenActivity.this);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, Constants.REQUEST_CAMERA);
        }
    }

    public void openOptionsMenu() {
        View view = getLayoutInflater().inflate(R.layout.custom_media_options_menu, null);
        LinearLayout layoutGallery;
        LinearLayout layoutAudio;
        LinearLayout layoutVideo;
        LinearLayout layoutDocument;
        LinearLayout layoutLocation;
        LinearLayout layoutContacts;
        layoutGallery = view.findViewById(R.id.layoutImages);
        layoutAudio = view.findViewById(R.id.layoutAudio);
        layoutVideo = view.findViewById(R.id.layoutVideo);
        layoutDocument = view.findViewById(R.id.layoutDoument);
        //    layoutContacts = inflatedView.findViewById(R.id.layoutcontact);
        //    layoutLocation = inflatedView.findViewById(R.id.layoutlocation);

        final Dialog mBottomSheetDialog =
                new Dialog(ChatScreenActivity.this, R.style.MaterialDialogSheet);
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog
                .getWindow()
                .setLayout(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
        mBottomSheetDialog.show();

        layoutGallery.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mBottomSheetDialog.dismiss();
                        galleryIntent();
                    }
                });

        layoutVideo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBottomSheetDialog.dismiss();
                        launchVideo();
                    }
                });

        layoutDocument.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBottomSheetDialog.dismiss();
                        launchAttachDocument();
                    }
                });

        layoutAudio.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBottomSheetDialog.dismiss();
                        launchAudio();
                    }
                });
    }

    private void galleryIntent() {
        FishBun.with(ChatScreenActivity.this)
                .setImageAdapter(new GlideAdapter())
                .setMaxCount(1)
                .setPickerSpanCount(3)
                .setActionBarColor(
                        getResources().getColor(R.color.colorPrimary),
                        getResources().getColor(R.color.colorPrimaryDark),
                        false)
                .setAlbumSpanCountOnlPortrait(2)
                .setButtonInAlbumActivity(true)
                .setCamera(true)
                .setReachLimitAutomaticClose(false)
                .setAllDoneButtonDrawable(
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_check_black_24dp))
                .setMenuTextColor(Color.BLACK)
                .setHomeAsUpIndicatorDrawable(
                        ContextCompat.getDrawable(
                                getApplicationContext(), R.drawable.ic_arrow_white_black_24dp))
                .textOnImagesSelectionLimitReached("You can't select any more.")
                .textOnNothingSelected("Select image(s) to upload")
                .setActionBarTitleColor(Color.BLACK)
                .startAlbum();
    }

    private void launchVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, Constants.REQUEST_VIDEOS);
    }

    private void launchAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, Constants.REQUEST_AUDIOS);
    }

    private void launchAttachDocument() {
        String[] mimeTypes = {
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                "text/plain",
                "application/vnd.oasis.opendocument.text",
                "application/pdf",
        };

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "ChooseFile"), Constants.REQUEST_DOCUMENTS);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Define.ALBUM_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            uriArrayList = new ArrayList<>();
            uriArrayList = data.getParcelableArrayListExtra(Define.INTENT_PATH);
            convertToBytes(uriArrayList);
        } else if (requestCode == Constants.REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            uriArrayList = new ArrayList<>();
            Uri cameraUri = filePath;
            uriArrayList.add(cameraUri);
            convertToBytes(uriArrayList);
        } else if (requestCode == Constants.REQUEST_DOCUMENTS
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            validateFile(data, "File", "FILE-" + System.currentTimeMillis());
        } else if (requestCode == Constants.REQUEST_AUDIOS
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            onMediaSelected(data, "Audio", "AUD-" + System.currentTimeMillis() + ".mp3");

        } else if (requestCode == Constants.REQUEST_VIDEOS
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            onMediaSelected(data, "Video", "VID-" + System.currentTimeMillis() + ".mp4");
        }
    }

    private void validateFile(Intent data, String mediaType, String mediaName) {
        String[] values =
                new String[]{
                        ".doc", ".docx", ".pdf", ".odt", ".ppt", ".pptx", ".xls", ".xlsx", ".txt", ".zip", ".apk",
                        ".exe", ".html", ".mp4", ".jpg", ".jpeg", ".png", ".mp3"
                };

        final ArrayList<String> mimeList = new ArrayList<>();
        Collections.addAll(mimeList, values);
        String filename = FileUtil.getFileName(ChatScreenActivity.this, data.getData());
        String extension = filename.substring(filename.lastIndexOf('.'));
        if (mimeList.contains(extension)) {
            onDocumentSelected(data, mediaType, mediaName);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setMessage("Selected file does not support to share")
                    .setCancelable(false)
                    .setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
            builder.create().show();
        }
    }

    public void onMediaSelected(Intent data, String mediaType, String mediaName) {
        final Uri fileUri = data.getData();
        try {
            File actualFile = FileUtil.from(getApplicationContext(), fileUri);
            long fileSize = actualFile.length();
            long fileSizeinKb = fileSize / 1024;
            long filesizinMb = fileSizeinKb / 1024;
            if (filesizinMb > 20) {
                Toast.makeText(this, "File Size should not excced 20 Mb", Toast.LENGTH_SHORT).show();
            } else {
                uploadFileToFirebaseStorage(
                        fileUri,
                        mediaName,
                        mediaType,
                        ImageCompressor.getReadableFileSize(filesizinMb),
                        fileUri.toString());
            }
        } catch (IOException e) {
            Log.e("Exc", e.toString());
        }
    }

    public void onDocumentSelected(Intent data, String mediaType, String mediaName) {
        final Uri fileUri = data.getData();

        try {
            File actualFile = FileUtil.from(getApplicationContext(), fileUri);
            long fileSize = actualFile.length();
            long fileSizeinKb = fileSize / 1024;
            long filesizinMb = fileSizeinKb / 1024;
            if (filesizinMb > 5) {
                Toast.makeText(this, "File Size should not excced 5 Mb", Toast.LENGTH_SHORT).show();
            } else {
                uploadFileToFirebaseStorage(
                        fileUri,
                        mediaName,
                        mediaType,
                        ImageCompressor.getReadableFileSize(filesizinMb),
                        fileUri.toString());
            }
        } catch (IOException e) {
            Log.e("Exc", e.toString());
        }
    }

    public static class FloatingView {
        private FloatingView() {
        }

        public static void onShowPopup(AppCompatActivity activity, View inflatedView) {

            // get device size
            Display display = activity.getWindowManager().getDefaultDisplay();
            final Point size = new Point();
            display.getSize(size);
            // fill the data to the list items
            // set height depends on the device size
            popWindow = new PopupWindow(inflatedView, size.x, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            // set a background drawable with rounders corners
            popWindow.setBackgroundDrawable(
                    activity.getResources().getDrawable(R.drawable.bg_input_cursor));
            // make it focusable to show the keyboard to enter in `EditText`
            popWindow.setFocusable(true);
            // make it outside touchable to dismiss the popup window
            popWindow.setOutsideTouchable(true);

            popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            // show the popup at bottom of the screen and set some margin at
            // bottom ie,

            popWindow.showAtLocation(activity.findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
        }

        public static void dismissWindow() {

            popWindow.dismiss();
        }
    }

    public void load() {

        int compare = profileId.compareToIgnoreCase(idFriend);
        if (compare < 0) {
            uniqueId = profileId + idFriend;
        } else {
            uniqueId = idFriend + profileId;
        }

        mDatabaseReference =
                FirebaseDatabase.getInstance().getReference().child("chatsnippets").child(profileId);
        setCountValueTozero();
        getStatus();
        // retriveMessages();
        getMessages();
    }

    private void getStatus() {
        valueEventListener =
                mDatabaseReference.addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    setCountValueTozero();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("Error", error.toString());
                            }
                        });
    }

    private void setCountValueTozero() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child("chatsnippets")
                .child(profileId)
                .child(uniqueId)
                .child("messageStatus")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    FirebaseDatabase.getInstance()
                                            .getReference()
                                            .child("chatsnippets")
                                            .child(profileId)
                                            .child(uniqueId)
                                            .child("messageStatus")
                                            .setValue("read");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("Error", error.toString());
                            }
                        });
    }

    private void getCountValue() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child("chatsnippets")
                .child(idFriend)
                .child(uniqueId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    HashMap mapUserInfo = (HashMap) snapshot.getValue();

                                    String msgCount = (String) mapUserInfo.get("messageCount");
                                    if (msgCount != null) {
                                        messagesCount = Integer.parseInt(msgCount);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("StringErr", error.toString());
                            }
                        });
    }

    public String getCurrentDate() {
        Date currentTime = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTime);
        Date newDate = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
        return simpleDateFormat.format(newDate).toUpperCase();
    }

    public String getCurrentDateWithSeconds() {
        Date currentTime = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTime);
        Date newDate = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm sss");
        return simpleDateFormat.format(newDate).toUpperCase();
    }

    private void sendMessage(
            String imageUrl,
            String fileUrl,
            String name,
            String type,
            String mediaSize,
            String mediaUri) {
        String message = editWriteMessage.getText().toString().trim();
        String notificationMessage = message;
        if (imageUrl != null) {
            message = name;
            notificationMessage = "Shared you an image";
        } else if (fileUrl != null) {
            message = name;
            notificationMessage = "Shared you an file";
        }

        registerUsers(message, type);
        registerUsers1(message, type);

        if (!message.isEmpty()) {
            editWriteMessage.setText("");
            Message message1 = new Message();
            message1.himeID = profileId;
            message1.name = profileName;
            message1.timestamp = getCurrentDate();
            message1.profileImage = profileImage;
            message1.messagetype = type;
            message1.messageBody = message;
            message1.roomId = uniqueId;
            message1.mediaSize = mediaSize;
            message1.fileUrl = fileUrl;
            message1.imageUrl = imageUrl;
            message1.latitude = null;
            message1.address = null;
            message1.longitude = null;
            message1.mediaUri = mediaUri;
            message1.isseen = false;
            message1.phoneNumber = profilePhone;

            DatabaseReference messagesDB =
                    FirebaseDatabase.getInstance().getReference().child("chats").child(uniqueId);
            final String finalMessage = notificationMessage;
            messagesDB
                    .push()
                    .setValue(message1)
                    .addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    sendNotifications(finalMessage);
                                }
                            });
        }
    }

    private void sendNotifications(String message) {

        /// code for push notifications
        DocumentReference docRef =
                FirebaseFirestore.getInstance().collection("users_table").document(idFriend.toLowerCase());
        final String messageBody = message;
        docRef
                .get()
                .addOnCompleteListener(
                        new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document != null) {

                                        final String toToken = document.getString("fcmToken");

                                        Data data = new Data(profileName, messageBody, profileImage, profileId);

                                        Sender sender = new Sender(data, toToken);

                                        apiService
                                                .sendNotification(sender)
                                                .enqueue(
                                                        new Callback<RetrofitResponse>() {
                                                            @Override
                                                            public void onResponse(
                                                                    Call<RetrofitResponse> call,
                                                                    retrofit2.Response<RetrofitResponse> response) {
                                                                // Do Something
                                                                Log.e("Success", "Success");
                                                            }

                                                            @Override
                                                            public void onFailure(Call<RetrofitResponse> call, Throwable t) {
                                                                Log.e("Errp", "err");
                                                            }
                                                        });
                                    }
                                }
                            }
                        });
    }

    public void registerUsers(final String message, final String messageType) {
        Message message1 = new Message();
        message1.name = nameFriend;
        message1.phoneNumber = friendPhone;
        message1.roomId = uniqueId;
        message1.messageBody = message;
        message1.messagetype = messageType;
        message1.profileImage = chatFriendImage;
        message1.timestamp = getCurrentDateWithSeconds();
        message1.himeID = idFriend;
        FirebaseDatabase.getInstance()
                .getReference()
                .child("chatsnippets")
                .child(profileId)
                .child(uniqueId)
                .setValue(message1);
    }

    public void registerUsers1(final String message, final String messageType) {
        Message message1 = new Message();
        message1.name = profileName;
        message1.roomId = uniqueId;
        message1.phoneNumber = profilePhone;
        message1.messageBody = message;
        message1.messagetype = messageType;
        message1.profileImage = profileImage;
        message1.timestamp = getCurrentDateWithSeconds();
        message1.himeID = profileId;
        message1.messageStatus = "unread";
        message1.messageCount = messagesCount;

        FirebaseDatabase.getInstance()
                .getReference()
                .child("chatsnippets")
                .child(idFriend)
                .child(uniqueId)
                .setValue(message1);
    }

    private void seenMessage() {
        Query reference =
                FirebaseDatabase.getInstance().getReference("chats").child(uniqueId).limitToLast(1);
        seenListener =
                reference.addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    Message chat = snapshot.getValue(Message.class);
                                    if (chat.himeID.equals(idFriend)) {
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("isseen", true);
                                        snapshot.getRef().updateChildren(hashMap);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
    }

    public void retriveMessages() {

        DatabaseReference messageRef = mRootReference.child("chats").child(uniqueId);
        messageRef.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {

                            final HashMap mapMessage = (HashMap) dataSnapshot.getValue();

                            final String text = (String) mapMessage.get("messageBody");
                            final String sender = (String) mapMessage.get("name");
                            final String profileImageUrl = (String) mapMessage.get("profileImage");
                            final String imageUrl = (String) mapMessage.get("imageUrl");
                            final String fileUrl = (String) mapMessage.get("fileUrl");
                            final String timeStamp = (String) mapMessage.get("timestamp");
                            final String himeId = mapMessage.get("himeID").toString().toLowerCase();
                            final String messagetype = (String) mapMessage.get("messagetype");
                            final String mediaSize = (String) mapMessage.get("mediaSize");
                            final String latitude = (String) mapMessage.get("latitude");
                            final String longitude = (String) mapMessage.get("latitude");
                            final String address = (String) mapMessage.get("latitude");
                            final String roomId = (String) mapMessage.get("roomId");
                            final boolean isSeen = (boolean) mapMessage.get("isseen");
                            final String mediaUri = (String) mapMessage.get("mediaUri");
                            final String phoneNumber = (String) mapMessage.get("phoneNumber");

                            Message message =
                                    new Message(
                                            text,
                                            imageUrl,
                                            fileUrl,
                                            timeStamp,
                                            messagetype,
                                            address,
                                            latitude,
                                            longitude,
                                            roomId,
                                            mediaSize,
                                            profileImageUrl,
                                            himeId,
                                            sender,
                                            phoneNumber,
                                            isSeen,
                                            mediaUri);

                            messageitems.add(message);

                            if (!messageitems.isEmpty()) {
                                linearLayoutManager.scrollToPosition(messageitems.size() - 1);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        // Do Something
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        // Do Something
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        // Do Something
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Do Something
                        Log.e("Exce", databaseError.toString());
                    }
                });
    }

    private void getMessages() {
        DatabaseReference messageRef = mRootReference.child("chats").child(uniqueId);
        Query getFirstQuery = messageRef.limitToFirst(1);

        getFirstQuery.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        mFirstKey = dataSnapshot.getKey();
                        Log.e("KEYS", "MFIRSTKEY " + mFirstKey);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        // Do Something
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        // Do Something

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        // Do Something

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Err", databaseError.toString());
                    }
                });

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                            final HashMap mapMessage = (HashMap) dataSnapshot.getValue();

                            final String text = (String) mapMessage.get("messageBody");
                            final String sender = (String) mapMessage.get("name");
                            final String profileImageUrl = (String) mapMessage.get("profileImage");
                            final String imageUrl = (String) mapMessage.get("imageUrl");
                            final String fileUrl = (String) mapMessage.get("fileUrl");
                            final String timeStamp = (String) mapMessage.get("timestamp");
                            final String himeId = mapMessage.get("himeID").toString().toLowerCase();
                            final String messagetype = (String) mapMessage.get("messagetype");
                            final String mediaSize = (String) mapMessage.get("mediaSize");
                            final String latitude = (String) mapMessage.get("latitude");
                            final String longitude = (String) mapMessage.get("latitude");
                            final String address = (String) mapMessage.get("latitude");
                            final String roomId = (String) mapMessage.get("roomId");
                            final boolean isSeen = (boolean) mapMessage.get("isseen");
                            final String mediaUri = (String) mapMessage.get("mediaUri");
                            final String phoneNumber = (String) mapMessage.get("phoneNumber");

                            Log.i("KEYS", dataSnapshot.getKey());

                            Message message =
                                    new Message(
                                            text,
                                            imageUrl,
                                            fileUrl,
                                            timeStamp,
                                            messagetype,
                                            address,
                                            latitude,
                                            longitude,
                                            roomId,
                                            mediaSize,
                                            profileImageUrl,
                                            himeId,
                                            sender,
                                            phoneNumber,
                                            isSeen,
                                            mediaUri);

                            itemPos++;

                            if (itemPos == 1) {

                                String messageKey = dataSnapshot.getKey();

                                mLastKey = messageKey;
                                mPrevKey = messageKey;
                            }

                            messageitems.add(message);
                            adapter.notifyDataSetChanged();

                            recyclerChat.scrollToPosition(messageitems.size() - 1);
                            mRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        // Do Something

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        // Do Something

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        // Do Something

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Err", databaseError.toString());
                    }
                });
    }

    private void getMoreMessages() {
        Log.i(
                "TEST_KEYS",
                "FIRST KEY LOAD MESSAGES : " + mFirstKey + "  LAST KEY LOAD MESSAGES : " + mLastKey);
        if (mLastKey.equals(mFirstKey)) {
            mCurrentPage = -1;

            mRefreshLayout.setRefreshing(false);
            return;
        }

        DatabaseReference messageRef = mRootReference.child("Chats").child(uniqueId);

        Query messageQuery =
                messageRef.orderByKey().endAt(mLastKey).limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                            final HashMap mapMessage = (HashMap) dataSnapshot.getValue();

                            final String text = (String) mapMessage.get("messageBody");
                            final String sender = (String) mapMessage.get("name");
                            final String profileImageUrl = (String) mapMessage.get("profileImage");
                            final String imageUrl = (String) mapMessage.get("imageUrl");
                            final String fileUrl = (String) mapMessage.get("fileUrl");
                            final String timeStamp = (String) mapMessage.get("timestamp");
                            final String himeId = mapMessage.get("himeID").toString().toLowerCase();
                            final String messagetype = (String) mapMessage.get("messagetype");
                            final String mediaSize = (String) mapMessage.get("mediaSize");
                            final String latitude = (String) mapMessage.get("latitude");
                            final String longitude = (String) mapMessage.get("latitude");
                            final String address = (String) mapMessage.get("latitude");
                            final String roomId = (String) mapMessage.get("roomId");
                            final boolean isSeen = (boolean) mapMessage.get("isseen");
                            final String mediaUri = (String) mapMessage.get("mediaUri");
                            final String phoneNumber = (String) mapMessage.get("phoneNumber");

                            Log.i("KEYS", dataSnapshot.getKey());

                            Message message =
                                    new Message(
                                            text,
                                            imageUrl,
                                            fileUrl,
                                            timeStamp,
                                            messagetype,
                                            address,
                                            latitude,
                                            longitude,
                                            roomId,
                                            mediaSize,
                                            profileImageUrl,
                                            himeId,
                                            sender,
                                            phoneNumber,
                                            isSeen,
                                            mediaUri);
                            String messageKey = dataSnapshot.getKey();

                            if (!mPrevKey.equals(messageKey)) {
                                messageitems.add(itemPos++, message);
                            } else {
                                mPrevKey = mLastKey;
                            }

                            if (itemPos == 1) {

                                mLastKey = messageKey;
                                Log.i("KEY", "MLASTKEY " + mLastKey);
                            }

                            adapter.notifyDataSetChanged();

                            mRefreshLayout.setRefreshing(false);

                            linearLayoutManager.scrollToPositionWithOffset(mCurrentPage * TOTAL_ITEMS_TO_LOAD, 0);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        // Do Something

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        // Do Something

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        // Do Something

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Err", databaseError.toString());
                    }
                });
    }

    private void convertToBytes(List<Uri> imagesList) {
        for (int i = 0; i < imagesList.size(); i++) {
            Uri uri = imagesList.get(i);
            try {
                final byte[] imagebyte = ImageCompressor.compressImage(ChatScreenActivity.this, uri);
                String mediaSize = ImageCompressor.getReadableFileSize(imagebyte.length);
                uploadImageToFirebaseStorage(imagebyte, mediaSize, uri.toString());

            } catch (IOException e) {
                Log.e("Exce", e.toString());
            }
        }
    }

    private void uploadImageToFirebaseStorage(
            final byte[] selectedImagesList, final String mediaSize, final String mediaUri) {

        final String name = "IMG-" + System.currentTimeMillis() + ".jpg";
        StorageReference storageReference =
                FirebaseStorage.getInstance().getReference().child("chats").child(uniqueId);
        final StorageReference mStorageReference = storageReference.child("images").child(name);
        mStorageReference
                .putBytes(selectedImagesList)
                .addOnSuccessListener(
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                mStorageReference
                                        .getDownloadUrl()
                                        .addOnSuccessListener(
                                                new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {

                                                        String fileUrl = uri.toString();
                                                        FileUtil.deleteDir(getExternalCacheDir());
                                                        sendMessage(fileUrl, null, name, "Photo", mediaSize, mediaUri);
                                                    }
                                                });
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failures
                                Log.e("Exc", e.toString());
                            }
                        });
    }

    private void uploadFileToFirebaseStorage(
            final Uri fileUri,
            final String mediaName,
            final String mediaType,
            final String mediaSize,
            final String mediaUri) {
        StorageReference storageReference =
                FirebaseStorage.getInstance().getReference().child("chats").child(uniqueId);
        final StorageReference mStorageReference =
                storageReference.child(mediaType.toLowerCase()).child(mediaName);
        final String filename = FileUtil.getFileName(ChatScreenActivity.this, fileUri);
        mStorageReference
                .putFile(fileUri)
                .addOnSuccessListener(
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                mStorageReference
                                        .getDownloadUrl()
                                        .addOnSuccessListener(
                                                new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {

                                                        String fileUrl = uri.toString();
                                                        FileUtil.deleteDir(getExternalCacheDir());
                                                        sendMessage(null, fileUrl, filename, mediaType, mediaSize, mediaUri);
                                                    }
                                                });
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failures
                                Log.e("Exc", e.toString());
                            }
                        });
    }

    @Override
    public void onClick(View v) {
        sendMessage(null, null, null, "Text", null, null);
    }
}
