package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.SyncCallService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicomkit.uiwidgets.ApplozicApplication;
import com.aqitapartners.sellers.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationListView;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.QuickConversationAdapter;
import com.applozic.mobicomkit.uiwidgets.instruction.InstructionUtil;

import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by devashish on 10/2/15.
 */
public class MobiComQuickConversationFragment extends Fragment {

    public static final String QUICK_CONVERSATION_EVENT = "quick_conversation";
    protected ConversationListView listView = null;
    protected ImageButton fabButton;
    protected TextView emptyTextView;
    protected Button startNewButton;
    protected SwipeRefreshLayout swipeLayout;
    protected int listIndex;
    protected Map<String, Message> latestMessageForEachContact = new HashMap<String, Message>();
    protected List<Message> messageList = new ArrayList<Message>();
    protected QuickConversationAdapter conversationAdapter = null;
    protected boolean loadMore = false;
    protected SyncCallService syncCallService;
    private ApplozicSetting applozicSetting;
    private Long minCreatedAtTime;
    private DownloadConversation downloadConversation;
    private BaseContactService baseContactService;

    public ConversationListView getListView() {
        return listView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applozicSetting = ApplozicSetting.getInstance(getActivity());
        syncCallService = SyncCallService.getInstance(getActivity());
        conversationAdapter = new QuickConversationAdapter(getActivity(),
                messageList, null);
        baseContactService = new AppContactService(getActivity());
       Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                MobiComUserPreference.getInstance(getActivity()).setDeviceTimeOffset(DateUtils.getTimeDiffFromUtc());
            }
        });
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View list = inflater.inflate(R.layout.mobicom_message_list, container, false);
        listView = (ConversationListView) list.findViewById(R.id.messageList);
        listView.setBackgroundColor(getResources().getColor(R.color.conversation_list_all_background));
        listView.setScrollToBottomOnSizeChange(Boolean.FALSE);

        fabButton = (ImageButton) list.findViewById(R.id.fab_start_new);

        LinearLayout individualMessageSendLayout = (LinearLayout) list.findViewById(R.id.individual_message_send_layout);
        LinearLayout extendedSendingOptionLayout = (LinearLayout) list.findViewById(R.id.extended_sending_option_layout);

        individualMessageSendLayout.setVisibility(View.GONE);
        extendedSendingOptionLayout.setVisibility(View.GONE);

        View spinnerLayout = inflater.inflate(R.layout.mobicom_message_list_header_footer, null);
        listView.addFooterView(spinnerLayout);

        //spinner = (ProgressBar) spinnerLayout.findViewById(R.id.spinner);
        emptyTextView = (TextView) list.findViewById(R.id.noConversations);
       // startNewButton = (Button) spinnerLayout.findViewById(R.id.start_new_conversation);

        fabButton.setVisibility(applozicSetting.isStartNewFloatingActionButtonVisible() ? View.VISIBLE : View.GONE);

        swipeLayout = (SwipeRefreshLayout) list.findViewById(R.id.swipe_container);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        listView.setLongClickable(true);
        registerForContextMenu(listView);

        return list;
    }

    protected View.OnClickListener startNewConversation() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MobiComKitActivityInterface) getActivity()).startContactActivityForResult();
            }
        };
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.conversation);

        menu.add(Menu.NONE, Menu.NONE, 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        if (messageList.size() <= position) {
            return true;
        }
        Message message = messageList.get(position);

        switch (item.getItemId()) {
            case 0:
                Contact contact = baseContactService.getContactById(message.getContactIds());
                new ConversationUIService(getActivity()).deleteConversationThread(contact, contact.getDisplayName());
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.removeItem(R.id.dial);
        menu.removeItem(R.id.deleteConversation);
    }

    public void addMessage(final Message message) {
        final Context context = getActivity();
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                message.processContactIds(context);
                Message recentMessage = latestMessageForEachContact.get(message.getContactIds());
                if (recentMessage != null && message.getCreatedAtTime() >= recentMessage.getCreatedAtTime()) {
                    messageList.remove(recentMessage);
                } else if (recentMessage != null) {
                    return;
                }

                latestMessageForEachContact.put(message.getContactIds(), message);
                messageList.add(0, message);
                conversationAdapter.notifyDataSetChanged();
                //listView.smoothScrollToPosition(messageList.size());
                listView.setSelection(0);
                emptyTextView.setVisibility(View.GONE);
               // startNewButton.setVisibility(View.GONE);
            }
        });
    }

    public void updateLastMessage(String keyString, String userId) {
        for (Message message : messageList) {
            if (message.getKeyString() != null && message.getKeyString().equals(keyString)) {
                MessageDatabaseService messageDatabaseService = new MessageDatabaseService(getActivity());
                List<Message> lastMessage = messageDatabaseService.getLatestMessage(userId);
                if (lastMessage.isEmpty()) {
                    removeConversation(message, userId);
                } else {
                    deleteMessage(lastMessage.get(0), userId);
                }
                break;
            }
        }
    }

    public String getLatestContact() {
        if (messageList != null && !messageList.isEmpty()) {
            Message message = messageList.get(0);
            return message.getTo();
        }
        return null;
    }

    public void deleteMessage(final Message message, final String userId) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Message recentMessage = latestMessageForEachContact.get(userId);
                if (recentMessage != null && message.getCreatedAtTime() <= recentMessage.getCreatedAtTime()) {
                    latestMessageForEachContact.put(userId, message);
                    messageList.set(messageList.indexOf(recentMessage), message);

                    conversationAdapter.notifyDataSetChanged();
                    if (messageList.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                       // startNewButton.setVisibility(applozicSetting.isStartNewButtonVisible() ? View.VISIBLE : View.GONE);
                    }
                }
            }
        });
    }

    public void updateLatestMessage(Message message, String userId) {
        deleteMessage(message, userId);
    }

    public void removeConversation(final Message message, final String userId) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                latestMessageForEachContact.remove(userId);
                messageList.remove(message);
                conversationAdapter.notifyDataSetChanged();
                checkForEmptyConversations();
            }
        });
    }

    public void removeConversation(final Contact contact, String response) {

        if ("success".equals(response)) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Message message = latestMessageForEachContact.get(contact.getUserId());
                    messageList.remove(message);
                    latestMessageForEachContact.remove(contact.getUserId());
                    conversationAdapter.notifyDataSetChanged();
                    checkForEmptyConversations();
                }
            });
        } else {

            Toast.makeText(getActivity(), "delete failed ", Toast.LENGTH_SHORT).show();
        }

    }

    public void checkForEmptyConversations() {
        boolean isLodingConversation = (downloadConversation != null && downloadConversation.getStatus() == AsyncTask.Status.RUNNING);
        if (latestMessageForEachContact.isEmpty() && !isLodingConversation) {
            emptyTextView.setVisibility(View.VISIBLE);
            //startNewButton.setVisibility(applozicSetting.isStartNewButtonVisible() ? View.VISIBLE : View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
           // startNewButton.setVisibility(View.GONE);
        }
    }

    public void setLoadMore(boolean loadMore) {
        this.loadMore = loadMore;
    }

    @Override
    public void onPause() {
        super.onPause();
        listIndex = listView.getFirstVisiblePosition();
        BroadcastService.currentUserId = null;
    }

    @Override
    public void onResume() {
        //Assigning to avoid notification in case if quick conversation fragment is opened....
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(ApplozicApplication.TITLE);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setSubtitle("");
        BroadcastService.selectMobiComKitAll();
        super.onResume();
        latestMessageForEachContact.clear();
        messageList.clear();
        if (listView != null) {
            if (listView.getCount() > listIndex) {
                listView.setSelection(listIndex);
            } else {
                listView.setSelection(0);
            }
        }
        downloadConversations();
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                SyncMessages syncMessages = new SyncMessages();
                syncMessages.execute();
            }
        });

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //FlurryAgent.logEvent(QUICK_CONVERSATION_EVENT);
        listView.setAdapter(conversationAdapter);
       // startNewButton.setOnClickListener(startNewConversation());
        fabButton.setOnClickListener(startNewConversation());
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0 && loadMore) {
                    loadMore = false;
                    new DownloadConversation(view, false, firstVisibleItem, visibleItemCount, totalItemCount).execute();
                }
            }
        });
    }


    public void downloadConversations() {
        downloadConversations(false);
    }

    public void downloadConversations(boolean showInstruction) {
        minCreatedAtTime = null;
        downloadConversation = new DownloadConversation(listView, true, 1, 0, 0, showInstruction);
        downloadConversation.execute();
    }

    public void updateLastSeenStatus(final String userId) {
        if (!ApplozicSetting.getInstance(getActivity()).isOnlineStatusInMasterListVisible()) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Message message = latestMessageForEachContact.get(userId);
                    if (message != null) {
                        int index = messageList.indexOf(message);
                        View view = listView.getChildAt(index - listView.getFirstVisiblePosition());
                        if (view != null) {
                            TextView onlineTextView = (TextView) view.findViewById(R.id.onlineTextView);
                            Contact contact = baseContactService.getContactById(userId);
                            onlineTextView.setVisibility((contact != null && contact.isConnected()) ? View.VISIBLE : View.GONE);
                        }
                    }
                } catch(Exception ex) {
                    Log.w("AL", "Exception while updating online status.");
                }
            }
        });

    }

    public class DownloadConversation extends AsyncTask<Void, Integer, Long> {

        private AbsListView view;
        private int firstVisibleItem;
        private int amountVisible;
        private int totalItems;
        private boolean initial;
        private boolean showInstruction;
        private List<Message> nextMessageList = new ArrayList<Message>();
        private Context context;

        public DownloadConversation(AbsListView view, boolean initial, int firstVisibleItem, int amountVisible, int totalItems, boolean showInstruction) {
            this.context = getActivity();
            this.view = view;
            this.initial = initial;
            this.firstVisibleItem = firstVisibleItem;
            this.amountVisible = amountVisible;
            this.totalItems = totalItems;
            this.showInstruction = showInstruction;
        }

        public DownloadConversation(AbsListView view, boolean initial, int firstVisibleItem, int amountVisible, int totalItems) {
            this(view, initial, firstVisibleItem, amountVisible, totalItems, false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadMore = false;
            swipeLayout.setEnabled(true);
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(true);
                }
            });
        }

        protected Long doInBackground(Void... voids) {
            if (initial) {
                nextMessageList = syncCallService.getLatestMessagesGroupByPeople();
                if (!nextMessageList.isEmpty()) {
                    minCreatedAtTime = nextMessageList.get(nextMessageList.size() - 1).getCreatedAtTime();
                }
            } else if (!messageList.isEmpty()) {
                listIndex = firstVisibleItem;
                Long createdAt = messageList.isEmpty() ? null : messageList.get(messageList.size() - 1).getCreatedAtTime();
                minCreatedAtTime = (minCreatedAtTime == null ? createdAt : Math.min(minCreatedAtTime, createdAt));
                nextMessageList = syncCallService.getLatestMessagesGroupByPeople(minCreatedAtTime);
            }

            return 0L;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            swipeLayout.setEnabled(true);
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(false);
                }
            });
            for (Message currentMessage : nextMessageList) {
                if (currentMessage.isSentToMany()) {
                    continue;
                }
                Message recentSms = latestMessageForEachContact.get(currentMessage.getContactIds());
                if (recentSms != null) {
                    if (currentMessage.getCreatedAtTime() >= recentSms.getCreatedAtTime()) {
                        latestMessageForEachContact.put(currentMessage.getContactIds(), currentMessage);
                        Log.d("Current message", "message" + currentMessage);
                        messageList.remove(recentSms);
                        messageList.add(currentMessage);
                    }
                } else {
                    latestMessageForEachContact.put(currentMessage.getContactIds(), currentMessage);
                    messageList.add(currentMessage);
                }
            }
            conversationAdapter.notifyDataSetChanged();
            if (initial) {
                emptyTextView.setVisibility(messageList.isEmpty() ? View.VISIBLE : View.GONE);
                if (applozicSetting.isStartNewButtonVisible()) {
                   // startNewButton.setVisibility(messageList.isEmpty() ? View.VISIBLE : View.GONE);
                }
                if (!messageList.isEmpty()) {
                    listView.setSelection(0);
                }
            } else {
                listView.setSelection(firstVisibleItem);
            }
            /*if (isAdded()) {
                //Utils.isNetworkAvailable(getActivity(), errorMessage);
                if (!Utils.isInternetAvailable(getActivity())) {
                    String errorMessage = getResources().getString(R.string.internet_connection_not_available);
                    ((MobiComKitActivityInterface) getActivity()).showErrorMessageView(errorMessage);
                }
            }*/

            if (context != null && showInstruction) {
                InstructionUtil.showInstruction(context, R.string.instruction_open_conversation_thread, MobiComKitActivityInterface.INSTRUCTION_DELAY, BroadcastService.INTENT_ACTIONS.INSTRUCTION.toString());
            }
        }
    }

    private class SyncMessages extends AsyncTask<Void, Integer, Long>{
        SyncMessages(){
        }
        @Override
        protected Long doInBackground(Void... params) {
            syncCallService.syncMessages(null);
            return 1l;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            swipeLayout.setRefreshing(false);
        }
    }

}