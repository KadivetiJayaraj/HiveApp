package com.jayaraj.hime.adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jayaraj.hime.R;
import com.jayaraj.hime.model.Message;
import com.jayaraj.hime.ui.chat.FileDownloadActivity;
import com.jayaraj.hime.ui.chat.ImageFullScreenActivity;
import com.jayaraj.hime.ui.chat.VideoPlayerActivity;
import com.jayaraj.hime.util.GlideLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder> {

    private List<Message> mItems;
    private final Activity context;
    String profileHimeID;
    String friendHimeId;
    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;
    private static final int RIGHT_MSG_FILE = 4;
    private static final int LEFT_MSG_FILE = 5;

    public MessagesListAdapter(
            List<Message> items, Activity context, String profileHimeID, String friendHimeId) {
        this.mItems = items;
        this.context = context;
        this.profileHimeID = profileHimeID;
        this.friendHimeId = friendHimeId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == RIGHT_MSG) {
            view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_outgoing_message, parent, false);
            return new ViewHolder(view);
        } else if (viewType == LEFT_MSG) {
            view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_incoming_message, parent, false);
            return new ViewHolder(view);
        } else if (viewType == RIGHT_MSG_IMG) {
            view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_outgoing_img_msg, parent, false);
            return new ViewHolder(view);
        } else if (viewType == LEFT_MSG_IMG) {
            view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_incoming_img_msg, parent, false);
            return new ViewHolder(view);
        } else if (viewType == RIGHT_MSG_FILE) {
            view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_outgoing_file_msg, parent, false);
            return new ViewHolder(view);
        } else {
            view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_incoming_file_msg, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.setData(mItems.get(position), position);
    }

    @Override
    public int getItemViewType(int position) {

        Message message = mItems.get(position);
        if (message.imageUrl != null) {
            if (message.himeID.equals(profileHimeID)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else if (message.fileUrl != null) {
            if (message.himeID.equals(profileHimeID)) {
                return RIGHT_MSG_FILE;
            } else {
                return LEFT_MSG_FILE;
            }
        } else if (message.himeID.equals(profileHimeID)) {
            return RIGHT_MSG;
        } else {
            return LEFT_MSG;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView chatMessage;
        TextView chatTimeStamp;
        TextView chatFileDocument;
        ImageView chatImageView;
        TextView timeheader;
        ImageView fileImage;
        RelativeLayout fileCardView;
        String mimeType;
        TextView textSeen;

        ViewHolder(View itemView) {
            super(itemView);
            chatMessage = itemView.findViewById(R.id.message_text_view);
            chatTimeStamp = itemView.findViewById(R.id.timestamp_text_view);
            chatImageView = itemView.findViewById(R.id.message_image_view);
            chatFileDocument = itemView.findViewById(R.id.chatFiledocument);
            timeheader = itemView.findViewById(R.id.dateheader);
            fileImage = itemView.findViewById(R.id.file_image);
            fileCardView = itemView.findViewById(R.id.fileCardView);
            textSeen = itemView.findViewById(R.id.txt_seen);
        }

        private String getOnlyTimeFromDateTime(String time) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
            Date parseTime = null;
            try {
                parseTime = simpleDateFormat.parse(time);

            } catch (ParseException e) {
                Log.e("Exc", e.toString());
            }
            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a");

            return simpleTimeFormat.format(parseTime).toUpperCase();
        }

        private String getOnlyDateFromDateTime(String date) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");

            Date mDate = null;
            try {
                mDate = simpleDateFormat.parse(date);
            } catch (ParseException e) {
                Log.e("Exc", e.toString());
            }
            long timeInMilliseconds = mDate.getTime();

            Calendar messageTime = Calendar.getInstance();
            messageTime.setTimeInMillis(timeInMilliseconds);

            Calendar now = Calendar.getInstance();
            final String dateFormatString = "dd/MM/yyyy";

            if (now.get(Calendar.DATE) == messageTime.get(Calendar.DATE)) {
                return "Today";
            } else if (now.get(Calendar.DATE) - messageTime.get(Calendar.DATE) == 1) {
                return "Yesterday";
            } else if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)) {
                return DateFormat.format(dateFormatString, messageTime).toString();
            } else {
                return DateFormat.format(dateFormatString, messageTime).toString();
            }
        }

        public String getTimeStamp(String date) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");

            Date mDate = null;
            try {
                mDate = simpleDateFormat.parse(date);
            } catch (ParseException e) {
                Log.e("Exc", e.toString());
            }
            long timeInMilliseconds = mDate.getTime();

            Calendar messageTime = Calendar.getInstance();
            messageTime.setTimeInMillis(timeInMilliseconds);

            Calendar now = Calendar.getInstance();
            final String dateFormatString = "MMMM dd, yyyy";
            final String timeFormatString = "hh:mm a";
            final String dateFormatYearString = "MMMM dd, hh:mm a";

            if (now.get(Calendar.DATE) == messageTime.get(Calendar.DATE)) {
                return "Today, "
                        + DateFormat.format(timeFormatString, messageTime).toString().toUpperCase();
            } else if (now.get(Calendar.DATE) - messageTime.get(Calendar.DATE) == 1) {
                return "Yesterday, "
                        + DateFormat.format(timeFormatString, messageTime).toString().toUpperCase();
            } else if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)) {
                return DateFormat.format(dateFormatYearString, messageTime).toString();
            } else {
                return DateFormat.format(dateFormatString, messageTime).toString();
            }
        }

        private void setData(final Message item, final int position) {

            final String time = getOnlyTimeFromDateTime(item.timestamp);
            chatTimeStamp.setText(time);
            final String curentDate = getOnlyDateFromDateTime(item.timestamp);
            timeheader.setText(curentDate);

            if (position > 0) {
                Message previousMessage = mItems.get(position - 1);
                String previousDate = getOnlyDateFromDateTime(previousMessage.timestamp);
                boolean sameDate = curentDate.equalsIgnoreCase(previousDate);
                if (sameDate) {
                    timeheader.setVisibility(View.GONE);

                } else {
                    timeheader.setVisibility(View.VISIBLE);
                }
            } else {
                timeheader.setVisibility(View.VISIBLE);
            }

            if (position == mItems.size() - 1) {
                if (item.isIsseen()) {
                    textSeen.setText("Seen");
                } else {
                    textSeen.setText("Delivered");
                }
            } else {
                textSeen.setVisibility(View.GONE);
            }

            if (item.messagetype.equals("Photo")) {

                if (item.imageUrl != null) {
                    GlideLoader.loadImage(
                            chatImageView, context.getApplicationContext(), R.drawable.gif_image, item.imageUrl);
                    chatImageView.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String name;

                                    if (item.himeID.equals(profileHimeID)) {
                                        name = "You";
                                    } else {
                                        name = item.name;
                                    }
                                    Intent intent = new Intent(context, ImageFullScreenActivity.class);
                                    intent.putExtra("urlPhotoClick", item.imageUrl);
                                    intent.putExtra("chatId", item.himeID);
                                    intent.putExtra("sharedDate", getTimeStamp(item.timestamp));
                                    intent.putExtra("sharedName", name);
                                    context.startActivity(intent);
                                }
                            });
                }

            } else if (item.messagetype.equals("File")) {

                if (item.fileUrl != null) {

                    final String fileDownloaded = item.fileUrl;
                    final String fileName = item.messageBody;
                    chatFileDocument.setText(fileName);
                    final String extension = fileName.substring(fileName.lastIndexOf('.'));
                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                    if (extension.equals(".doc") || extension.equals(".docx")) {

                        fileImage.setImageResource(R.drawable.doc);

                    } else if (extension.equals(".pdf")) {

                        fileImage.setImageResource(R.drawable.pdf);

                    } else if (extension.equals(".odt")) {

                        fileImage.setImageResource(R.drawable.odt_icon_white);

                    } else if (extension.equals(".ppt") || extension.equals(".pptx")) {

                        fileImage.setImageResource(R.drawable.ppt);

                    } else if (extension.equals(".xls") || extension.equals(".xlsx")) {

                        fileImage.setImageResource(R.drawable.xls);

                    } else if (extension.equals(".txt")) {

                        fileImage.setImageResource(R.drawable.txt);

                    } else if (extension.equals(".zip")) {

                        fileImage.setImageResource(R.drawable.zip);

                    } else if (extension.equals(".mp4")) {

                        fileImage.setImageResource(R.drawable.mp4);

                    } else if (extension.equals(".mp3")) {

                        fileImage.setImageResource(R.drawable.mp3);

                    } else if (extension.equals(".html")) {

                        fileImage.setImageResource(R.drawable.html);

                    } else if (extension.equals(".jpg") || extension.equals(".jpeg")) {

                        fileImage.setImageResource(R.drawable.jpg);

                    } else if (extension.equals(".png")) {

                        fileImage.setImageResource(R.drawable.png);

                    } else if (extension.equals(".exe")) {

                        fileImage.setImageResource(R.drawable.exe);

                    } else if (extension.equals(".apk")) {

                        fileImage.setImageResource(R.drawable.apk);

                    } else {
                        fileImage.setImageResource(R.drawable.file);
                    }

                    fileCardView.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String name;
                                    if (item.himeID.equals(profileHimeID)) {
                                        name = "You";
                                    } else {
                                        name = item.name;
                                    }
                                    Intent intent = new Intent(context, FileDownloadActivity.class);
                                    intent.putExtra("fileUrl", fileDownloaded);
                                    intent.putExtra("chatId", item.himeID);
                                    intent.putExtra("sharedDate", getTimeStamp(item.timestamp));
                                    intent.putExtra("sharedName", name);
                                    intent.putExtra("fileSize", item.mediaSize);
                                    intent.putExtra("fileName", fileName);
                                    intent.putExtra("fileExtension", extension);
                                    context.startActivity(intent);
                                }
                            });
                }

            } else if (item.messagetype.equals("Video")) {
                if (item.fileUrl != null) {

                    final String fileDownloaded = item.fileUrl;
                    final String fileName = item.messageBody;
                    chatFileDocument.setText(fileName);
                    fileImage.setImageResource(R.drawable.chat_video);

                    fileCardView.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String name;
                                    if (item.himeID.equals(profileHimeID)) {
                                        name = "You";
                                    } else {
                                        name = item.name;
                                    }
                                    Intent intent = new Intent(context, VideoPlayerActivity.class);
                                    intent.putExtra("VideoURL", fileDownloaded);
                                    intent.putExtra("sharedDate", getTimeStamp(item.timestamp));
                                    intent.putExtra("sharedName", name);
                                    intent.putExtra("type", "Video");
                                    context.startActivity(intent);
                                }
                            });
                }

            } else if (item.messagetype.equals("Audio")) {
                if (item.fileUrl != null) {

                    final String fileDownloaded = item.fileUrl;
                    final String fileName = item.messageBody;
                    chatFileDocument.setText(fileName);
                    fileImage.setImageResource(R.drawable.chat_audio);

                    fileCardView.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String name;
                                    if (item.himeID.equals(profileHimeID)) {
                                        name = "You";
                                    } else {
                                        name = item.name;
                                    }
                                    Intent intent = new Intent(context, VideoPlayerActivity.class);
                                    intent.putExtra("VideoURL", fileDownloaded);
                                    intent.putExtra("sharedDate", getTimeStamp(item.timestamp));
                                    intent.putExtra("sharedName", name);
                                    intent.putExtra("type", "Audio");
                                    context.startActivity(intent);
                                }
                            });
                }

            } else if (item.messagetype.equals("Text")) {
                chatMessage.setText(item.messageBody);

                itemView.setOnLongClickListener(
                        new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {

                                Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

                                vibe.vibrate(100);
                                ClipboardManager clipboard =
                                        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("ChatMessage", item.messageBody);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(context, "Copied to Clipboard", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        });
            }
        }
    }
}
