package com.applozic.mobicomkit.uiwidgets.conversation.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.AttachmentView;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.ApplozicIntentService;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageClientService;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.conversation.selfdestruct.DisappearingMessageTask;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.aqitapartners.sellers.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationListView;
import com.applozic.mobicomkit.uiwidgets.conversation.DeleteConversationAsyncTask;
import com.applozic.mobicomkit.uiwidgets.conversation.MessageCommunicator;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComActivityForFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.SpinnerNavItem;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.DetailedConversationAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.TitleNavigationAdapter;
import com.applozic.mobicomkit.uiwidgets.instruction.InstructionUtil;
import com.applozic.mobicomkit.uiwidgets.schedule.ConversationScheduler;
import com.applozic.mobicomkit.uiwidgets.schedule.ScheduledTimeHolder;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.commons.core.utils.Support;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.emoticon.EmojiconHandler;
import com.applozic.mobicommons.file.FilePathFinder;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.people.group.Group;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

/**
 * reg
 * Created by devashish on 10/2/15.
 */
abstract public class MobiComConversationFragment extends Fragment implements View.OnClickListener {

    //Todo: Increase the file size limit
    public static final int MAX_ALLOWED_FILE_SIZE = 10 * 1024 * 1024;
    private static final String TAG = "MobiComConversation";
    public FrameLayout emoticonsFrameLayout;
    protected String title = "Conversations";
    protected DownloadConversation downloadConversation;
    protected MobiComConversationService conversationService;
    protected TextView infoBroadcast;
    protected Class messageIntentClass;
    protected TextView emptyTextView;
    protected boolean loadMore = true;
    protected Contact contact;
    protected Group group;
    protected Integer currentConversationId;
    protected EditText messageEditText;
    protected ImageButton sendButton;
    protected ImageButton attachButton;
    protected Spinner sendType;
    protected LinearLayout individualMessageSendLayout;
    protected LinearLayout extendedSendingOptionLayout;
    protected RelativeLayout attachmentLayout;
    protected ProgressBar mediaUploadProgressBar;
    protected View spinnerLayout;
    protected SwipeRefreshLayout swipeLayout;
    protected Button scheduleOption;
    protected ScheduledTimeHolder scheduledTimeHolder = new ScheduledTimeHolder();
    protected Spinner selfDestructMessageSpinner;
    protected ImageView mediaContainer;
    protected TextView attachedFile;
    protected String filePath;
    protected boolean firstTimeMTexterFriend;
    protected MessageCommunicator messageCommunicator;
    protected ConversationListView listView = null;
    protected List<Message> messageList = new ArrayList<Message>();
    protected DetailedConversationAdapter conversationAdapter = null;
    protected Drawable sentIcon;
    protected Drawable deliveredIcon;
    protected ImageButton emoticonsBtn;
    protected Support support;
    protected MultimediaOptionFragment multimediaOptionFragment = new MultimediaOptionFragment();
    protected boolean hideExtendedSendingOptionLayout;
    private EmojiconHandler emojiIconHandler;
    private Bitmap previewThumbnail;
    private TextView isTyping;
    private LinearLayout statusMessageLayout;
    private String defaultText;
    private boolean  typingStarted;

    public void setEmojiIconHandler(EmojiconHandler emojiIconHandler) {
        this.emojiIconHandler = emojiIconHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View list = inflater.inflate(R.layout.mobicom_message_list, container, false);
        listView = (ConversationListView) list.findViewById(R.id.messageList);
        listView.setBackgroundColor(getResources().getColor(R.color.conversation_list_background));
        listView.setScrollToBottomOnSizeChange(Boolean.TRUE);
        listView.setDivider(null);
        messageList = new ArrayList<Message>();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        individualMessageSendLayout = (LinearLayout) list.findViewById(R.id.individual_message_send_layout);
        extendedSendingOptionLayout = (LinearLayout) list.findViewById(R.id.extended_sending_option_layout);
        statusMessageLayout = (LinearLayout) list.findViewById(R.id.status_message_layout);
        attachmentLayout = (RelativeLayout) list.findViewById(R.id.attachment_layout);
        isTyping = (TextView) list.findViewById(R.id.isTyping);
        mediaUploadProgressBar = (ProgressBar) attachmentLayout.findViewById(R.id.media_upload_progress_bar);
        emoticonsFrameLayout = (FrameLayout) list.findViewById(R.id.emojicons_frame_layout);
        emoticonsBtn = (ImageButton) list.findViewById(R.id.emoticons_btn);
        if (emojiIconHandler == null && emoticonsBtn != null) {
            emoticonsBtn.setVisibility(View.GONE);
        }
        spinnerLayout = inflater.inflate(R.layout.mobicom_message_list_header_footer, null);
        infoBroadcast = (TextView) spinnerLayout.findViewById(R.id.info_broadcast);
        emptyTextView = (TextView) list.findViewById(R.id.noConversations);
        emoticonsBtn.setOnClickListener(this);
        listView.addHeaderView(spinnerLayout);
        sentIcon = getResources().getDrawable(R.drawable.applozic_ic_action_message_sent);
        deliveredIcon = getResources().getDrawable(R.drawable.applozic_ic_action_message_delivered);

        listView.setLongClickable(true);

        sendButton = (ImageButton) individualMessageSendLayout.findViewById(R.id.conversation_send);
        attachButton = (ImageButton) individualMessageSendLayout.findViewById(R.id.attach_button);
        sendType = (Spinner) extendedSendingOptionLayout.findViewById(R.id.sendTypeSpinner);
        messageEditText = (EditText) individualMessageSendLayout.findViewById(R.id.conversation_message);
        if(!TextUtils.isEmpty(defaultText)){
            messageEditText.setText(defaultText);
            defaultText = "";
        }
        scheduleOption = (Button) extendedSendingOptionLayout.findViewById(R.id.scheduleOption);
        mediaContainer = (ImageView) attachmentLayout.findViewById(R.id.media_container);
        attachedFile = (TextView) attachmentLayout.findViewById(R.id.attached_file);
        ImageView closeAttachmentLayout = (ImageView) attachmentLayout.findViewById(R.id.close_attachment_layout);

        swipeLayout = (SwipeRefreshLayout) list.findViewById(R.id.swipe_container);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        listView.setMessageEditText(messageEditText);

        ArrayAdapter<CharSequence> sendTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.send_type_options, R.layout.mobiframework_custom_spinner);

        sendTypeAdapter.setDropDownViewResource(R.layout.mobiframework_custom_spinner);
        sendType.setAdapter(sendTypeAdapter);


        scheduleOption.setOnClickListener(new View.OnClickListener() {

                                              @Override
                                              public void onClick(View v) {
                                                  ConversationScheduler conversationScheduler = new ConversationScheduler();
                                                  conversationScheduler.setScheduleOption(scheduleOption);
                                                  conversationScheduler.setScheduledTimeHolder(scheduledTimeHolder);
                                                  conversationScheduler.setCancelable(false);
                                                  conversationScheduler.show(getActivity().getSupportFragmentManager(), "conversationScheduler");
                                              }
                                          }
        );

        messageEditText.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // EmojiconHandler.addEmojis(getActivity(), messageEditText.getText(), Utils.dpToPx(30));
                //TODO: write code to emoticons .....

            }

            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString()) && s.toString().trim().length() == 1) {
                    //Log.i(TAG, "typing started event...");
                    typingStarted = true;
                    Intent intent =  new Intent(getActivity(), ApplozicMqttIntentService.class);
                    intent.putExtra(ApplozicMqttIntentService.CONTACT,contact);
                    intent.putExtra(ApplozicMqttIntentService.TYPING, typingStarted);
                    getActivity().startService(intent);
                } else if (s.toString().trim().length() == 0 && typingStarted) {
                    //Log.i(TAG, "typing stopped event...");
                    typingStarted = false;
                    Intent intent =  new Intent(getActivity(), ApplozicMqttIntentService.class);
                    intent.putExtra(ApplozicMqttIntentService.CONTACT,contact);
                    intent.putExtra(ApplozicMqttIntentService.TYPING, typingStarted);
                    getActivity().startService(intent);
                }
                //sendButton.setVisibility((s == null || s.toString().trim().length() == 0) && TextUtils.isEmpty(filePath) ? View.GONE : View.VISIBLE);
                //attachButton.setVisibility(s == null || s.toString().trim().length() == 0 ? View.VISIBLE : View.GONE);
            }
        });

        messageEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                emoticonsFrameLayout.setVisibility(View.GONE);
            }
        });

        messageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if(typingStarted){
                        Intent intent =  new Intent(getActivity(), ApplozicMqttIntentService.class);
                        intent.putExtra(ApplozicMqttIntentService.CONTACT,contact);
                        intent.putExtra(ApplozicMqttIntentService.TYPING, typingStarted);
                        getActivity().startService(intent);
                    }
                    emoticonsFrameLayout.setVisibility(View.GONE);
                }
            }

        });


        sendButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                                              emoticonsFrameLayout.setVisibility(View.GONE);

                                              if (TextUtils.isEmpty(messageEditText.getText().toString()) && TextUtils.isEmpty(filePath)) {
                                                /*final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).
                                                          setPositiveButton(R.string.yes_alert, new DialogInterface.OnClickListener() {
                                                              @Override
                                                              public void onClick(DialogInterface dialogInterface, int i) {
                                                                  sendMessage(messageEditText.getText().toString());
                                                                  messageEditText.setText("");
                                                                  scheduleOption.setText(R.string.ScheduleText);
                                                                  if (scheduledTimeHolder.getTimestamp() != null) {
                                                                      showScheduleMessageToast();
                                                                  }
                                                                  scheduledTimeHolder.resetScheduledTimeHolder();
                                                              }
                                                          });
                                                  alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                      @Override
                                                      public void onClick(DialogInterface dialogInterface, int i) {
                                                      }
                                                  });
                                                  alertDialog.setTitle(getActivity().getString(R.string.alert_for_empty_message));
                                                  alertDialog.setCancelable(true);
                                                  alertDialog.create().show();*/
                                              } else {
                                                  sendMessage(messageEditText.getText().toString());
                                                  messageEditText.setText("");
                                                  scheduleOption.setText(R.string.ScheduleText);
                                                  if (scheduledTimeHolder.getTimestamp() != null) {
                                                      showScheduleMessageToast();
                                                  }
                                                  scheduledTimeHolder.resetScheduledTimeHolder();

                                              }
                                          }
                                      }
        );

        closeAttachmentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filePath = null;
                if (previewThumbnail != null) {
                    previewThumbnail.recycle();
                }
                attachmentLayout.setVisibility(View.GONE);
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener()

                                     {

                                         @Override
                                         public void onScrollStateChanged(AbsListView absListView, int i) {

                                         }

                                         @Override
                                         public void onScroll(AbsListView view, int firstVisibleItem, int amountVisible,
                                                              int totalItems) {
                                             if (loadMore) {
                                                 int topRowVerticalPosition =
                                                         (listView == null || listView.getChildCount() == 0) ?
                                                                 0 : listView.getChildAt(0).getTop();
                                                 swipeLayout.setEnabled(topRowVerticalPosition >= 0);
                                             }
                                         }
                                     }
        );

        //Adding fragment for emoticons...
//        //Fragment emojiFragment = new EmojiconsFragment(this, this);
//        Fragment emojiFragment = new EmojiconsFragment();
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.add(R.id.emojicons_frame_layout, emojiFragment).commit();
        return list;
    }

    public void showScheduleMessageToast() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity(), R.string.info_message_scheduled, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteMessageFromDeviceList(String messageKeyString) {
        try {
            int position;
            boolean updateQuickConversation = false;
            int index;
            for (Message message : messageList) {
                boolean value = message.getKeyString() != null ? message.getKeyString().equals(messageKeyString) : false;
                if (value) {
                    index = messageList.indexOf(message);
                    if (index != -1) {
                        int aboveIndex = index - 1;
                        int belowIndex = index + 1;
                        Message aboveMessage = messageList.get(aboveIndex);
                        if (belowIndex != messageList.size()) {
                            Message belowMessage = messageList.get(belowIndex);
                            if (aboveMessage.isTempDateType() && belowMessage.isTempDateType()) {
                                messageList.remove(aboveMessage);
                            }
                        } else if (belowIndex == messageList.size() && aboveMessage.isTempDateType()) {
                            messageList.remove(aboveMessage);
                        }
                    }
                }
                if (message.getKeyString() != null && message.getKeyString().equals(messageKeyString)) {
                    position = messageList.indexOf(message);

                    if (position == messageList.size() - 1) {
                        updateQuickConversation = true;
                    }
                    if (message.getScheduledAt() != null && message.getScheduledAt() != 0) {
                        new MessageDatabaseService(getActivity()).deleteScheduledMessage(messageKeyString);
                    }
                    messageList.remove(position);
                    conversationAdapter.notifyDataSetChanged();
                    if (messageList.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                        ((MobiComKitActivityInterface) getActivity()).removeConversation(message, contact.getFormattedContactNumber());
                    }
                    break;
                }
            }
            int messageListSize = messageList.size();
            if (messageListSize > 0 && updateQuickConversation) {
                ((MobiComKitActivityInterface) getActivity()).updateLatestMessage(messageList.get(messageListSize - 1), contact.getFormattedContactNumber());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getCurrentUserId() {
        if (contact == null) {
            return "";
        }
        return contact.getUserId() != null ? contact.getUserId() : contact.getFormattedContactNumber();
    }

    public Contact getContact() {
        return contact;
    }

    protected void setContact(Contact contact) {
        this.contact = contact;
    }

    public String getFormattedContactNumber() {
        return contact != null ? contact.getFormattedContactNumber() : null;
    }

    public boolean hasMultiplePhoneNumbers() {
        return contact != null && contact.hasMultiplePhoneNumbers();
    }

    public MultimediaOptionFragment getMultimediaOptionFragment() {
        return multimediaOptionFragment;
    }

    public Spinner getSendType() {
        return sendType;
    }

    public Spinner getSelfDestructMessageSpinner() {
        return selfDestructMessageSpinner;
    }

    public Button getScheduleOption() {
        return scheduleOption;
    }

    public void setFirstTimeMTexterFriend(boolean firstTimeMTexterFriend) {
        this.firstTimeMTexterFriend = firstTimeMTexterFriend;
    }

//    public EmojiconEditText getMessageEditText() {
//        return messageEditText;
//    }

    public void clearList() {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (conversationAdapter != null) {
                    messageList.clear();
                    conversationAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void updateMessage(final Message message) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Note: Removing and adding the same message again as the new sms object will contain the keyString.
                messageList.remove(message);
                messageList.add(message);
                conversationAdapter.notifyDataSetChanged();
            }
        });
    }

    public void addMessage(final Message message) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Todo: Handle disappearing messages.
                boolean added = updateMessageList(message, false);
                if (added) {
                    //Todo: update unread count
                    conversationAdapter.notifyDataSetChanged();
                    listView.smoothScrollToPosition(messageList.size());
                    listView.setSelection(messageList.size());
                    emptyTextView.setVisibility(View.GONE);
                    currentConversationId = message.getConversationId();
                    new MessageDatabaseService(getActivity()).updateReadStatus(message.getTo());
                    if(Message.MessageType.MT_INBOX.getValue().equals(message.getType()) ){
                        Intent intent = new Intent(getActivity(), ApplozicIntentService.class);
                        intent.putExtra(ApplozicIntentService.PAIRED_MESSAGE_KEY_STRING, message.getPairedMessageKeyString());
                        getActivity().startService(intent);
                    }
                }

                selfDestructMessage(message);
            }
        });
    }

    protected abstract void processMobiTexterUserCheck();

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (view.getId() == R.id.messageList) {
            menu.setHeaderTitle(R.string.messageOptions);

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            int positionInSmsList = info.position - 1;
            if (positionInSmsList < 0 || messageList.isEmpty()) {
                return;
            }
            Message message = messageList.get(positionInSmsList);

            if (message.isTempDateType()) {
                return;
            }

            String[] menuItems = getResources().getStringArray(R.array.menu);

            for (int i = 0; i < menuItems.length; i++) {

                if (message.hasAttachment() &&
                        menuItems[i].equals("Copy")) {
                    continue;
                }
                if (message.isCall() && (menuItems[i].equals("Forward") ||
                        menuItems[i].equals("Resend"))) {
                    continue;
                }
                if (menuItems[i].equals("Resend") && (!message.isSentViaApp() || message.getDelivered())) {
                    continue;
                }
                if (menuItems[i].equals("Delete") && (message.isAttachmentUploadInProgress() || TextUtils.isEmpty(message.getKeyString()))) {
                    continue;
                }
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        String contactNumber = contact != null ? contact.getContactNumber() : null;

        if (ApplozicClient.getInstance(getActivity()).isHandleDial() && !TextUtils.isEmpty(contactNumber) && contactNumber.matches("[0-9]+") && contactNumber.length() > 2) {
            menu.findItem(R.id.dial).setVisible(true);
        } else {
//            menu.findItem(R.id.dial).setVisible(false);
        }
        menu.removeItem(R.id.start_new);
    }

    public void loadConversation(final Contact contact, Group group) {
        if (downloadConversation != null) {
            downloadConversation.cancel(true);
        }

        final MessageClientService messageClientService = new MessageClientService(getActivity());
        BroadcastService.currentUserId = contact.getContactIds();
        typingStarted = false;

        Intent readStatusIntent =  new Intent(getActivity(),ApplozicIntentService.class);
        readStatusIntent.putExtra(ApplozicIntentService.CONTACT,contact);
        getActivity().startService(readStatusIntent);

        /*
        filePath = null;*/
        if (TextUtils.isEmpty(filePath)) {
            attachmentLayout.setVisibility(View.GONE);
        }

        infoBroadcast.setVisibility(group != null ? View.VISIBLE : View.GONE);

        setContact(contact);
        setGroup(group);

        individualMessageSendLayout.setVisibility(View.VISIBLE);
        extendedSendingOptionLayout.setVisibility(View.VISIBLE);

        unregisterForContextMenu(listView);
        clearList();
        updateTitle();
        swipeLayout.setEnabled(true);
        loadMore = true;
        if (selfDestructMessageSpinner != null) {
            selfDestructMessageSpinner.setSelection(0);
        }

        if (contact != null) {
            conversationAdapter = new DetailedConversationAdapter(getActivity(),
                    R.layout.mobicom_message_row_view, messageList, contact, messageIntentClass, emojiIconHandler);
        } else if (group != null) {
            conversationAdapter = new DetailedConversationAdapter(getActivity(),
                    R.layout.mobicom_message_row_view, messageList, group, messageIntentClass, emojiIconHandler);
        }

        listView.setAdapter(conversationAdapter);
        registerForContextMenu(listView);

        processMobiTexterUserCheck();

        if (contact != null) {
            processPhoneNumbers();

            if (!TextUtils.isEmpty(contact.getContactIds())) {
                NotificationManager notificationManager =
                        (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(contact.getContactIds().hashCode());
            }
        }

        downloadConversation = new DownloadConversation(listView, true, 1, 0, 0, contact, group);
        downloadConversation.execute();

        if (contact != null && support.isSupportNumber(contact.getFormattedContactNumber())) {
            sendType.setSelection(1);
            extendedSendingOptionLayout.setVisibility(View.GONE);
            messageEditText.setHint(R.string.enter_support_query_hint);
        } else {
            messageEditText.setHint(R.string.enter_mt_message_hint);
        }
        if (hideExtendedSendingOptionLayout) {
            extendedSendingOptionLayout.setVisibility(View.GONE);
        }
        emoticonsFrameLayout.setVisibility(View.GONE);

       Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    messageClientService.processUserStatus(contact);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        InstructionUtil.showInstruction(getActivity(), R.string.instruction_go_back_to_recent_conversation_list, MobiComKitActivityInterface.INSTRUCTION_DELAY, BroadcastService.INTENT_ACTIONS.INSTRUCTION.toString());
    }

    public void updateLastSeenStatus() {
        if (this.getActivity() == null) {
            return;
        }
        contact = new AppContactService(getActivity()).getContactById(contact.getContactIds());
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contact != null) {
                    if (contact.isConnected()) {
                        ((ActionBarActivity) getActivity()).getSupportActionBar().setSubtitle(getActivity().getString(R.string.user_online));
                    } else if (contact.getLastSeenAt() != 0) {
                        ((ActionBarActivity) getActivity()).getSupportActionBar().setSubtitle(getActivity().getString(R.string.last_seen_at_time) + " " + DateUtils.getDateAndTimeForLastSeen(contact.getLastSeenAt()));
                    }
                }
            }

        });
    }

    public boolean isBroadcastedToGroup(Long groupId) {
        return getGroup() != null && getGroup().getGroupId().equals(groupId);
    }

    public Group getGroup() {
        return group;
    }

    protected void setGroup(Group group) {
        this.group = group;
    }

//    public void onEmojiconBackspace() {
//        EmojiconsFragment.backspace(messageEditText);
//    }

    public void updateUploadFailedStatus(Message message) {
        int i = messageList.indexOf(message);
        if (i != -1) {
            messageList.get(i).setCanceled(true);
            conversationAdapter.notifyDataSetChanged();
        }

    }

    public void downloadFailed(final Message message) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = messageList.indexOf(message);
                if (index != -1) {
                    View view = listView.getChildAt(index -
                            listView.getFirstVisiblePosition() + 1);

                    if (view != null) {
                        final LinearLayout attachmentDownloadLayout = (LinearLayout) view.findViewById(R.id.attachment_download_layout);
                        attachmentDownloadLayout.setVisibility(View.VISIBLE);
                    }

                }
            }

        });
    }

    abstract public void attachLocation(Location mCurrentLocation);

    public void updateDeliveryStatusForAllMessages() {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int index = 0; index < messageList.size(); index++) {
                        Message message = messageList.get(index);
                        if (message.getDelivered() || message.isTempDateType() || !message.isTypeOutbox()) {
                            continue;
                        }
                        message.setDelivered(true);
                        View view = listView.getChildAt(index -
                                listView.getFirstVisiblePosition() + 1);
                        if (view != null) {
                            TextView createdAtTime = (TextView) view.findViewById(R.id.createdAtTime);
                            TextView status = (TextView) view.findViewById(R.id.status);
                            //status.setText("Delivered");
                            createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.applozic_ic_action_message_delivered), null);
                        }
                    }
                } catch (Exception ex) {
                    Log.i(TAG, "Exception while updating delivery status in UI.");
                }
            }
        });
    }

    public void updateDeliveryStatus(final Message message) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int index = messageList.indexOf(message);
                    if (index != -1) {
                        if (messageList.get(index).getDelivered() || messageList.get(index).isTempDateType()) {
                            return;
                        }
                        messageList.get(index).setDelivered(true);
                        View view = listView.getChildAt(index -
                                listView.getFirstVisiblePosition() + 1);
                        if (view != null) {
                            TextView createdAtTime = (TextView) view.findViewById(R.id.createdAtTime);
                            /*TextView status = (TextView) view.findViewById(R.id.status);
                            status.setText("Delivered");*/
                            createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.applozic_ic_action_message_delivered), null);
                        }
                    } else {
                        messageList.add(message);
                        listView.smoothScrollToPosition(messageList.size());
                        listView.setSelection(messageList.size());
                        emptyTextView.setVisibility(View.GONE);
                        conversationAdapter.notifyDataSetChanged();
                    }
                } catch (Exception ex) {
                    Log.i(TAG, "Exception while updating delivery status in UI.");
                }
            }
        });
    }

    public void loadFile(Uri uri) {
        if (uri == null) {
            Toast.makeText(getActivity(), R.string.file_not_selected, Toast.LENGTH_LONG).show();
            return;
        }
        this.filePath = FilePathFinder.getPath(getActivity(), uri);
        if (TextUtils.isEmpty(filePath)) {
            Log.i(TAG, "Error while fetching filePath");
            attachmentLayout.setVisibility(View.GONE);
            Toast.makeText(getActivity(), R.string.info_file_attachment_error, Toast.LENGTH_LONG).show();
            return;
        }

        Cursor returnCursor =
                getActivity().getContentResolver().query(uri, null, null, null, null);
        if (returnCursor != null) {
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            Long fileSize = returnCursor.getLong(sizeIndex);
            if (fileSize > MAX_ALLOWED_FILE_SIZE) {
                Toast.makeText(getActivity(), R.string.info_attachment_max_allowed_file_size, Toast.LENGTH_LONG).show();
                return;
            }

            attachedFile.setText(returnCursor.getString(returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
            returnCursor.close();
        }

        attachmentLayout.setVisibility(View.VISIBLE);

        String mimeType = FileUtils.getMimeType(getActivity(), uri);

        if (mimeType != null && mimeType.startsWith("image")) {
            attachedFile.setVisibility(View.GONE);
            int reqWidth = mediaContainer.getWidth();
            int reqHeight = mediaContainer.getHeight();
            if (reqWidth == 0 || reqHeight == 0) {
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                reqHeight = displaymetrics.heightPixels;
                reqWidth = displaymetrics.widthPixels;
            }
            previewThumbnail = FileUtils.getPreview(filePath, reqWidth, reqHeight);
            previewThumbnail = ImageUtils.getImageRotatedBitmap(previewThumbnail, filePath, previewThumbnail.getWidth(), previewThumbnail.getHeight());
            mediaContainer.setImageBitmap(previewThumbnail);
        } else {
            attachedFile.setVisibility(View.VISIBLE);
            mediaContainer.setImageBitmap(null);
        }
    }

    public synchronized boolean updateMessageList(Message message, boolean update) {
        boolean toAdd = !messageList.contains(message);
        if (update) {
            messageList.remove(message);
            messageList.add(message);
        } else if (toAdd) {
            Message firstDateMessage = new Message();
            firstDateMessage.setTempDateType(Short.valueOf("100"));
            firstDateMessage.setCreatedAtTime(message.getCreatedAtTime());
            if (!messageList.contains(firstDateMessage)) {
                messageList.add(firstDateMessage);
            }

            messageList.add(message);
        }
        return toAdd;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        support = new Support(activity);
        try {
            messageCommunicator = (MessageCommunicator) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement interfaceDataCommunicator");
        }
    }

    protected AlertDialog showInviteDialog(int titleId, int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(getString(messageId).replace("[name]", getNameForInviteDialog()))
                .setTitle(titleId);
        builder.setPositiveButton(R.string.invite, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent share = new Intent(Intent.ACTION_SEND);
               /* String textToShare = getActivity().getResources().getString(R.string.invite_message);
                share.setAction(Intent.ACTION_SEND)
                        .setType("text/plain").putExtra(Intent.EXTRA_TEXT, textToShare);*/
                startActivity(Intent.createChooser(share, "Share Via"));
                sendType.setSelection(0);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendType.setSelection(0);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public String getNameForInviteDialog() {
        if (contact != null) {
            return contact.getDisplayName();
        } else if (group != null) {
            return group.getName();
        }
        return "";
    }

    public void forwardMessage(Message messageToForward, Contact contact) {
        this.contact = contact;
        loadConversation(contact);
        if (messageToForward.isAttachmentDownloaded()) {
            filePath = messageToForward.getFilePaths().get(0);
        }
        sendMessage(messageToForward.getMessage(), messageToForward.getFileMetas(), messageToForward.getFileMetaKeyStrings(),Message.ContentType.DEFAULT.getValue());
    }

    public void sendMessage(String message,FileMeta fileMetas, String fileMetaKeyStrings,short messageContentType ) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(getActivity());
        Message messageToSend = new Message();

        if (group != null && group.getGroupId() != null) {
            messageToSend.setBroadcastGroupId(group.getGroupId());
            List<String> contactIds = new ArrayList<String>();
            List<String> toList = new ArrayList<String>();
            for (Contact contact : group.getContacts()) {
                if (!TextUtils.isEmpty(contact.getContactNumber())) {
                    toList.add(contact.getContactNumber());
                    contactIds.add(contact.getFormattedContactNumber());
                }
            }
            messageToSend.setTo(TextUtils.join(",", toList));
            messageToSend.setContactIds(TextUtils.join(",", contactIds));
        } else {
            messageToSend.setTo(contact.getContactIds());
            messageToSend.setContactIds(contact.getContactIds());
        }

        messageToSend.setContentType(messageContentType);
        messageToSend.setRead(Boolean.TRUE);
        messageToSend.setStoreOnDevice(Boolean.TRUE);
        if (messageToSend.getCreatedAtTime() == null) {
            messageToSend.setCreatedAtTime(System.currentTimeMillis() + userPreferences.getDeviceTimeOffset());
        }
        messageToSend.setConversationId(currentConversationId);
        messageToSend.setSendToDevice(Boolean.FALSE);
        messageToSend.setType(sendType.getSelectedItemId() == 1 ? Message.MessageType.MT_OUTBOX.getValue() : Message.MessageType.OUTBOX.getValue());
        messageToSend.setTimeToLive(getTimeToLive());
        messageToSend.setMessage(message);
        messageToSend.setDeviceKeyString(userPreferences.getDeviceKeyString());
        messageToSend.setScheduledAt(scheduledTimeHolder.getTimestamp());
        messageToSend.setSource(Message.Source.MT_MOBILE_APP.getValue());
        if (!TextUtils.isEmpty(filePath)) {
            List<String> filePaths = new ArrayList<String>();
            filePaths.add(filePath);
            messageToSend.setFilePaths(filePaths);
        }
        messageToSend.setFileMetaKeyStrings(fileMetaKeyStrings);
        messageToSend.setFileMetas(fileMetas);

        conversationService.sendMessage(messageToSend, messageIntentClass);

        if (selfDestructMessageSpinner != null) {
            selfDestructMessageSpinner.setSelection(0);
        }
        attachmentLayout.setVisibility(View.GONE);
        filePath = null;
    }

    public void sendProductMessage(final String messageToSend,final FileMeta fileMeta,final Contact contact,final short messageContentType) {
     final Message message = new Message();
        new Thread(new Runnable() {
            @Override
            public void run() {
                MobiComConversationService conversationService = new MobiComConversationService(getActivity());
                MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(getActivity());
                currentConversationId = new MessageClientService(getActivity()).getConversationId(messageToSend,contact.getUserId());
                message.setContactIds(contact.getUserId());
                message.setTo(contact.getUserId());
                message.setMessage(messageToSend);
                message.setRead(Boolean.TRUE);
                message.setStoreOnDevice(Boolean.TRUE);
                message.setSendToDevice(Boolean.FALSE);
                message.setContentType(messageContentType);
                message.setType(Message.MessageType.MT_OUTBOX.getValue());
                message.setDeviceKeyString(userPreferences.getDeviceKeyString());
                message.setSource(Message.Source.MT_MOBILE_APP.getValue());
                message.setTopicId(messageToSend);
                message.setCreatedAtTime(System.currentTimeMillis() + userPreferences.getDeviceTimeOffset());
                message.setConversationId(currentConversationId);
                message.setFileMetas(fileMeta);
                conversationService.sendMessage(message, MessageIntentService.class);
            }
        }).start();

    }

    private Integer getTimeToLive() {
        if (selfDestructMessageSpinner == null || selfDestructMessageSpinner.getSelectedItemPosition() <= 1) {
            return null;
        }
        return Integer.parseInt(selfDestructMessageSpinner.getSelectedItem().toString().replace("mins", "").replace("min", "").trim());
    }

    public void sendMessage(String message) {
        sendMessage(message, null, null, Message.ContentType.DEFAULT.getValue());
    }

    public void sendMessage(String message, short messageContentType) {
        sendMessage(message, null, null, messageContentType);
    }

    public void updateMessageKeyString(final Message message) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = messageList.indexOf(message);
                if (index != -1) {
                    Message messageListItem = messageList.get(index);
                    messageListItem.setKeyString(message.getKeyString());
                    messageListItem.setSentToServer(true);
                    messageListItem.setCreatedAtTime(message.getSentMessageTimeAtServer());
                    messageListItem.setFileMetaKeyStrings(message.getFileMetaKeyStrings());
                    View view = listView.getChildAt(index - listView.getFirstVisiblePosition() + 1);
                    if (view != null) {
                        ProgressBar mediaUploadProgressBarIndividualMessage = (ProgressBar) view.findViewById(R.id.media_upload_progress_bar);
                        if (mediaUploadProgressBarIndividualMessage != null) {
                            mediaUploadProgressBarIndividualMessage.setVisibility(View.GONE);
                        }
                        TextView createdAtTime = (TextView) view.findViewById(R.id.createdAtTime);
                        if (messageListItem.isTypeOutbox() && !messageListItem.isCall() && !messageListItem.getDelivered() && messageListItem.getScheduledAt() == null) {
                            createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, support.isSupportNumber(getCurrentUserId()) ? deliveredIcon : sentIcon, null);
                        }
                    }
                }
            }
        });
    }

    public void updateDownloadStatus(final Message message) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int index = messageList.indexOf(message);
                    if (index != -1) {
                        Message smListItem = messageList.get(index);
                        smListItem.setKeyString(message.getKeyString());
                        smListItem.setFileMetaKeyStrings(message.getFileMetaKeyStrings());
                        View view = listView.getChildAt(index - listView.getFirstVisiblePosition() + 1);
                        if (view != null) {
                            final RelativeLayout attachmentDownloadProgressLayout = (RelativeLayout) view.findViewById(R.id.attachment_download_progress_layout);
                            final AttachmentView attachmentView = (AttachmentView) view.findViewById(R.id.main_attachment_view);
                            final ImageView preview = (ImageView) view.findViewById(R.id.preview);
                            if (message.getFileMetas() != null && message.getFileMetas().getContentType().contains("image")) {
                                attachmentView.setVisibility(View.VISIBLE);
                                preview.setVisibility(View.GONE);
                                attachmentView.setMessage(smListItem);
                                attachmentDownloadProgressLayout.setVisibility(View.GONE);
                            } else if (message.getFileMetas() != null && !message.getFileMetas().getContentType().contains("image")) {
                                attachmentView.setMessage(smListItem);
                                attachmentDownloadProgressLayout.setVisibility(View.GONE);
                                attachmentView.setVisibility(View.GONE);
                                preview.setVisibility(View.GONE);
                            }
                        }

                    }
                } catch (Exception ex) {
                    Log.i(TAG, "Exception while updating download status: " + ex.getMessage());
                }
            }
        });
    }

    public void setDefaultText(String defaultText){
        this.defaultText = defaultText;
    }
    public void updateUserTypingStatus(final String typingUserId, final String isTypingStatus) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isTypingStatus.equals("1")) {
                    statusMessageLayout.setVisibility(View.VISIBLE);
                    isTyping.setVisibility(View.VISIBLE);
                    isTyping.setText(contact.getDisplayName() + " " + getString(R.string.is_typing));
                } else {
                    statusMessageLayout.setVisibility(View.GONE);
                    isTyping.setVisibility(View.GONE);
                    isTyping.setText("");
                }
            }
        });
    }

//    public void onEmojiconClicked(Emojicon emojicon) {
//        //TODO: Move OntextChangeListiner to EmojiEditableTExt
//        int currentPos = messageEditText.getSelectionStart();
//        messageEditText.setTextKeepState(messageEditText.getText().
//                insert(currentPos, emojicon.getEmoji()));
//    }


    @Override
    public LayoutInflater getLayoutInflater(Bundle savedInstanceState) {
        return super.getLayoutInflater(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
    }

    //TODO: Please add onclick events here...  anonymous class are
    // TODO :hard to read and suggested if we have very few event view
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.emoticons_btn) {
            if (emoticonsFrameLayout.getVisibility() == View.VISIBLE) {
                emoticonsFrameLayout.setVisibility(View.GONE);
                Utils.toggleSoftKeyBoard(getActivity(), false);
            } else {
                Utils.toggleSoftKeyBoard(getActivity(), true);
                emoticonsFrameLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BroadcastService.currentUserId = null;
        if(typingStarted){
            Intent intent =  new Intent(getActivity(), ApplozicMqttIntentService.class);
            intent.putExtra(ApplozicMqttIntentService.CONTACT,contact);
            intent.putExtra(ApplozicMqttIntentService.TYPING, false);
            getActivity().startService(intent);
        }
    }

    public void updateTitle() {
        String title = null;
        if (contact != null) {
            title = contact.getDisplayName();
        } else if (group != null) {
            title = group.getName();
        }
        if (title != null) {
            ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(title);
        }

        updateLastSeenStatus();
    }

    public void loadConversation(Group group) {
        loadConversation(null, group);
    }

    public void loadConversation(Contact contact) {
        loadConversation(contact, null);
    }

    public void deleteConversationThread() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).
                setPositiveButton(R.string.delete_conversation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DeleteConversationAsyncTask(new MobiComConversationService(getActivity()), contact, getActivity()).execute();
                    }
                });
        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.setTitle(getActivity().getString(R.string.dialog_delete_conversation_title).replace("[name]", getNameForInviteDialog()));
        alertDialog.setMessage(getActivity().getString(R.string.dialog_delete_conversation_confir).replace("[name]", getNameForInviteDialog()));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    protected void processPhoneNumbers() {
        if (contact.hasMultiplePhoneNumbers()) {
            ArrayList<SpinnerNavItem> navSpinner = new ArrayList<SpinnerNavItem>();
            navSpinner.add(new SpinnerNavItem(contact, contact.getContactNumber(), contact.getPhoneNumbers().get(contact.getContactNumber()), R.drawable.applozic_ic_action_email));

            for (String phoneNumber : contact.getPhoneNumbers().keySet()) {
                if (!PhoneNumberUtils.compare(contact.getContactNumber(), phoneNumber)) {
                    navSpinner.add(new SpinnerNavItem(contact, phoneNumber, contact.getPhoneNumbers().get(phoneNumber), R.drawable.applozic_ic_action_email));
                }
            }
            // title drop down adapter
            MobiComActivityForFragment activity = ((MobiComActivityForFragment) getActivity());
            TitleNavigationAdapter adapter = new TitleNavigationAdapter(getActivity().getApplicationContext(), navSpinner);
            activity.setNavSpinner(navSpinner);
            activity.setAdapter(adapter);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position - 1;
        if (messageList.size() <= position) {
            return true;
        }
        Message message = messageList.get(position);
        if (message.isTempDateType()) {
            return true;
        }

        switch (item.getItemId()) {
            case 0:
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(message.getMessage());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied message", message.getMessage());
                    clipboard.setPrimaryClip(clip);
                }
                break;
            /*case 1:
                new ConversationUIService(getActivity()).startContactActivityForResult(message, null);
                break;*/
            case 1:
                Message messageToResend = new Message(message);
                //messageToResend.setCreatedAtTime(new Date().getTime());
                messageToResend.setCreatedAtTime(System.currentTimeMillis() + MobiComUserPreference.getInstance(getActivity()).getDeviceTimeOffset());
                conversationService.sendMessage(messageToResend, messageIntentClass);
                break;
            case 2:
                String messageKeyString = message.getKeyString();
                new DeleteConversationAsyncTask(conversationService, message, contact).execute();
                deleteMessageFromDeviceList(messageKeyString);
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (contact != null || group != null) {
            BroadcastService.currentUserId = contact.getContactIds();
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager) getActivity().getSystemService(ns);
            nMgr.cancel(BroadcastService.currentUserId.hashCode());

            if (downloadConversation != null) {
                downloadConversation.cancel(true);
            }
            if (messageList.isEmpty()) {
                loadConversation(contact, group);
            } else if (MobiComUserPreference.getInstance(getActivity()).getNewMessageFlag()) {
                loadnewMessageOnResume(contact, group);
            }
            MobiComUserPreference.getInstance(getActivity()).setNewMessageFlag(false);
        }
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                downloadConversation = new DownloadConversation(listView, false, 1, 1, 1, contact, group);
                downloadConversation.execute();
            }
        });
    }

    public void selfDestructMessage(Message sms) {
        if (Message.MessageType.MT_INBOX.getValue().equals(sms.getType()) &&
                sms.getTimeToLive() != null && sms.getTimeToLive() != 0) {
            new Timer().schedule(new DisappearingMessageTask(getActivity(), conversationService, sms), sms.getTimeToLive() * 60 * 1000);
        }
    }

    public void loadnewMessageOnResume(Contact contact, Group group) {
        downloadConversation = new DownloadConversation(listView, true, 1, 0, 0, contact, group);
        downloadConversation.execute();
    }

    public class DownloadConversation extends AsyncTask<Void, Integer, Long> {

        private AbsListView view;
        private int firstVisibleItem;
        private int amountVisible;
        private int totalItems;
        private boolean initial;
        private Contact contact;
        private Group group;
        private List<Message> nextSmsList = new ArrayList<Message>();

        public DownloadConversation(AbsListView view, boolean initial, int firstVisibleItem, int amountVisible, int totalItems, Contact contact, Group group) {
            this.view = view;
            this.initial = initial;
            this.firstVisibleItem = firstVisibleItem;
            this.amountVisible = amountVisible;
            this.totalItems = totalItems;
            this.contact = contact;
            this.group = group;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            emptyTextView.setVisibility(View.GONE);
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(true);
                }
            });
            if (initial) {
                sendButton.setEnabled(false);
                messageEditText.setEnabled(false);
            }

            if (!initial && messageList.isEmpty()) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).
                        setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loadMore = false;
                    }
                });
                //Todo: Move this to mobitexter app
                alertDialog.setTitle(R.string.sync_older_messages);
                alertDialog.setCancelable(true);
                alertDialog.create().show();
            }
        }

        @Override
        protected Long doInBackground(Void... voids) {
            try {
                if (initial) {
                    Long lastConversationloadTime = 1L;
                    if (!messageList.isEmpty()) {
                        for (int i = messageList.size() - 1; i >= 0; i--) {
                            if (messageList.get(i).isTempDateType()) {
                                continue;
                            }
                            lastConversationloadTime = messageList.get(i).getCreatedAtTime();
                            break;
                        }
                    }

                    Log.i(TAG, " loading conversation with  lastConversationloadTime " + lastConversationloadTime);
                    nextSmsList = conversationService.getMessages(lastConversationloadTime + 1L, null, contact, group);
                } else if (firstVisibleItem == 1 && loadMore && !messageList.isEmpty()) {
                    loadMore = false;
                    Long endTime = null;
                    for (Message message : messageList) {
                        if (message.isTempDateType()) {
                            continue;
                        }
                        endTime = messageList.get(0).getCreatedAtTime();
                        break;
                    }
                    nextSmsList = conversationService.getMessages(null, endTime, contact, group);
                }

                List<Message> createAtMessage = new ArrayList<Message>();
                if (nextSmsList != null && !nextSmsList.isEmpty()) {
                    Message firstDateMessage = new Message();
                    firstDateMessage.setTempDateType(Short.valueOf("100"));
                    firstDateMessage.setCreatedAtTime(nextSmsList.get(0).getCreatedAtTime());
                    createAtMessage.add(firstDateMessage);
                    messageList.remove(firstDateMessage);
                    createAtMessage.add(nextSmsList.get(0));
                    for (int i = 1; i <= nextSmsList.size() - 1; i++) {
                        long dayDiffrance = DateUtils.daysBetween(new Date(nextSmsList.get(i - 1).getCreatedAtTime()), new Date(nextSmsList.get(i).getCreatedAtTime()));

                        if (dayDiffrance >= 1) {
                            Message message = new Message();
                            message.setTempDateType(Short.valueOf("100"));
                            message.setCreatedAtTime(nextSmsList.get(i).getCreatedAtTime());
                            createAtMessage.add(message);
                            messageList.remove(message);
                        }
                        createAtMessage.add(nextSmsList.get(i));
                    }
                }
                nextSmsList = createAtMessage;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return 0L;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            //TODO: FIX ME
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(true);
                }
            });
            if (this.contact != null && !PhoneNumberUtils.compare(this.contact.getFormattedContactNumber(), this.contact.getFormattedContactNumber()) || nextSmsList.isEmpty()) {
                swipeLayout.setEnabled(false);
                swipeLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                    }
                });
                if (initial) {
                    sendButton.setEnabled(true);
                    messageEditText.setEnabled(true);
                }
                return;
            }

            if (this.group != null && !this.group.getGroupId().equals(this.group.getGroupId())) {
                return;
            }

            //Note: This is done to avoid duplicates with same timestamp entries
            if (!messageList.isEmpty() && !nextSmsList.isEmpty() &&
                    messageList.get(0).equals(nextSmsList.get(nextSmsList.size() - 1))) {
                nextSmsList.remove(nextSmsList.size() - 1);
            }


            for (Message message : nextSmsList) {
                selfDestructMessage(message);
            }

            if (initial) {
                messageList.addAll(nextSmsList);
                emptyTextView.setVisibility(messageList.isEmpty() ? View.VISIBLE : View.GONE);
                if (!messageList.isEmpty()) {
                    listView.post(new Runnable() {
                        @Override
                        public void run() {
                            listView.setSelection(messageList.size() - 1);
                        }
                    });
                }
            } else if (!nextSmsList.isEmpty()) {
                messageList.addAll(0, nextSmsList);
                listView.setSelection(nextSmsList.size());
            }

            if (!messageList.isEmpty()) {
                for (int i = messageList.size() - 1; i >= 0; i--) {
                    if (!messageList.get(i).isRead() && !messageList.get(i).isTempDateType()) {
                        messageList.get(i).setRead(Boolean.TRUE);
                        new MessageDatabaseService(getActivity()).updateSmsReadFlag(messageList.get(i).getMessageId(), true);
                    } else {
                        break;
                    }
                }
            }
            if (conversationAdapter != null) {
                conversationAdapter.notifyDataSetChanged();
            }
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(false);
                }
            });

            if(!messageList.isEmpty()){
                currentConversationId = messageList.get(messageList.size()-1).getConversationId();
            }
            if (initial) {
                sendButton.setEnabled(true);
                messageEditText.setEnabled(true);
            }
            loadMore = !nextSmsList.isEmpty();
        }
    }
}