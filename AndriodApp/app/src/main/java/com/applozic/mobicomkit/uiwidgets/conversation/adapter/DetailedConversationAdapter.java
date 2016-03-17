package com.applozic.mobicomkit.uiwidgets.conversation.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.AttachmentManager;
import com.applozic.mobicomkit.api.attachment.AttachmentView;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.aqitapartners.sellers.R;
import com.applozic.mobicomkit.uiwidgets.alphanumbericcolor.AlphaNumberColorUtil;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.FullScreenImageActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;

import com.applozic.mobicommons.commons.core.utils.ContactNumberUtils;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.commons.core.utils.Support;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.emoticon.EmojiconHandler;
import com.applozic.mobicommons.emoticon.EmoticonUtils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.people.group.Group;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by adarsh on 4/7/15.
 */
public class DetailedConversationAdapter extends ArrayAdapter<Message> {
    private static final int FILE_THRESOLD_SIZE = 400;
    private static Map<Short, Integer> messageTypeColorMap = new HashMap<Short, Integer>();

    static {
        messageTypeColorMap.put(Message.MessageType.INBOX.getValue(), R.color.message_type_inbox);
        messageTypeColorMap.put(Message.MessageType.OUTBOX.getValue(), R.color.message_type_outbox);
        messageTypeColorMap.put(Message.MessageType.OUTBOX_SENT_FROM_DEVICE.getValue(), R.color.message_type_outbox_sent_from_device);
        messageTypeColorMap.put(Message.MessageType.MT_INBOX.getValue(), R.color.message_type_mt_inbox);
        messageTypeColorMap.put(Message.MessageType.MT_OUTBOX.getValue(), R.color.message_type_mt_outbox);
        messageTypeColorMap.put(Message.MessageType.CALL_INCOMING.getValue(), R.color.message_type_incoming_call);
        messageTypeColorMap.put(Message.MessageType.CALL_OUTGOING.getValue(), R.color.message_type_outgoing_call);
    }

    private ImageLoader contactImageLoader, loadImage;
    private Context context;
    private Contact contact;
    private Group group;
    private boolean individual;
    private Drawable sentIcon;
    private Drawable deliveredIcon;
    private Drawable pendingIcon;
    private Drawable scheduledIcon;
    private ImageLoader imageThumbnailLoader;
    private TextView downloadSizeTextView;
    private EmojiconHandler emojiconHandler;
    private FileClientService fileClientService;
    private MessageDatabaseService messageDatabaseService;
    private BaseContactService contactService;
    private Contact senderContact;
    private long deviceTimeOffset = 0;
    private Class<?> messageIntentClass;
    private MobiComConversationService conversationService;

    public DetailedConversationAdapter(final Context context, int textViewResourceId, List<Message> messageList, Group group, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        this(context, textViewResourceId, messageList, null, group, messageIntentClass, emojiconHandler);
    }

    public DetailedConversationAdapter(final Context context, int textViewResourceId, List<Message> messageList, Contact contact, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        this(context, textViewResourceId, messageList, contact, null, messageIntentClass, emojiconHandler);
    }

    public DetailedConversationAdapter(final Context context, int textViewResourceId, List<Message> messageList, final Contact contact, Group group, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        super(context, textViewResourceId, messageList);
        this.messageIntentClass = messageIntentClass;
        this.context = context;
        this.contact = contact;
        this.group = group;
        this.emojiconHandler = emojiconHandler;
        this.individual = (contact != null || group != null);
        this.fileClientService = new FileClientService(context);
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.conversationService = new MobiComConversationService(context);
        this.contactService = new AppContactService(context);
        this.senderContact = contactService.getContactById(MobiComUserPreference.getInstance(context).getUserId());
        contactImageLoader = new ImageLoader(getContext(), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadContactImage((Activity) getContext(), (Contact) data);
            }
        };
        contactImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_180_holo_light);
        contactImageLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        contactImageLoader.setImageFadeIn(false);

        loadImage = new ImageLoader(getContext(), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadMessageImage(getContext(), (String) data);
            }
        };
        loadImage.setImageFadeIn(false);
        loadImage.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        imageThumbnailLoader = new ImageLoader(getContext(), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadThumbnailImage(getContext(), (FileMeta) data, getImageLayoutParam(false).width, getImageLayoutParam(false).height);
            }
        };
        imageThumbnailLoader.setImageFadeIn(false);
        imageThumbnailLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);

        sentIcon = getContext().getResources().getDrawable(R.drawable.applozic_ic_action_message_sent);
        deliveredIcon = getContext().getResources().getDrawable(R.drawable.applozic_ic_action_message_delivered);
        pendingIcon = getContext().getResources().getDrawable(R.drawable.applozic_ic_action_message_pending);
        scheduledIcon = getContext().getResources().getDrawable(R.drawable.applozic_ic_action_message_schedule);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View customView;
        deviceTimeOffset = MobiComUserPreference.getInstance(context).getDeviceTimeOffset();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Message message = getItem(position);
        int type = getItemViewType(position);
        if (type == 2) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            SimpleDateFormat simpleDateFormatDay = new SimpleDateFormat("EEEE");
            customView = inflater.inflate(R.layout.mobicom_date_layout, parent, false);
            TextView dateView = (TextView) customView.findViewById(R.id.chat_screen_date);
            TextView dayTextView = (TextView) customView.findViewById(R.id.chat_screen_day);
            Date date = new Date(message.getCreatedAtTime());
            if (DateUtils.isSameDay(message.getCreatedAtTime())) {
                dayTextView.setVisibility(View.VISIBLE);
                dayTextView.setText("Today");
            } else {
                dayTextView.setVisibility(View.VISIBLE);
                dateView.setVisibility(View.VISIBLE);
                dayTextView.setText(simpleDateFormatDay.format(date));
                dateView.setText(simpleDateFormat.format(date));
            }
            return customView;

        } else if (type == 0) {
            customView = inflater.inflate(R.layout.mobicom_received_message_list_view, parent, false);
        } else {
            customView = inflater.inflate(R.layout.mobicom_sent_message_list_view, parent, false);
        }
        if (message != null) {
        List<String> items = Arrays.asList(message.getContactIds().split("\\s*,\\s*"));
        List<String> userIds = null;
        if (!TextUtils.isEmpty(message.getContactIds())) {
            userIds = Arrays.asList(message.getContactIds().split("\\s*,\\s*"));
        }
        final Contact receiverContact;
        if (group != null) {
            receiverContact = null;
        } else if (individual) {
            receiverContact = contact;
            contact.setContactNumber(items.get(0));
            if (userIds != null) {
                contact.setUserId(userIds.get(0));
            }
            contact.setFormattedContactNumber(ContactNumberUtils.getPhoneNumber(items.get(0), MobiComUserPreference.getInstance(context).getCountryCode()));
        } else {
            receiverContact = contactService.getContactReceiver(items, userIds);
        }

            View messageTextLayout = customView.findViewById(R.id.messageTextLayout);
            TextView smReceivers = (TextView) customView.findViewById(R.id.smReceivers);
            TextView status = (TextView) customView.findViewById(R.id.status);
            TextView createdAtTime = (TextView) customView.findViewById(R.id.createdAtTime);
            TextView messageTextView = (TextView) customView.findViewById(R.id.message);
            CircleImageView contactImage = (CircleImageView) customView.findViewById(R.id.contactImage);
            //ImageView contactImage = (ImageView) customView.findViewById(R.id.contactImage);
            TextView alphabeticTextView = (TextView) customView.findViewById(R.id.alphabeticImage);
            ImageView sentOrReceived = (ImageView) customView.findViewById(R.id.sentOrReceivedIcon);
            TextView deliveryStatus = (TextView) customView.findViewById(R.id.status);
            TextView selfDestruct = (TextView) customView.findViewById(R.id.selfDestruct);
            final ImageView preview = (ImageView) customView.findViewById(R.id.preview);
            final AttachmentView attachmentView = (AttachmentView) customView.findViewById(R.id.main_attachment_view);
            TextView attachedFile = (TextView) customView.findViewById(R.id.attached_file);
            final ImageView attachmentIcon = (ImageView) customView.findViewById(R.id.attachmentIcon);
            downloadSizeTextView = (TextView) customView.findViewById(R.id.attachment_size_text);
            final LinearLayout attachmentDownloadLayout = (LinearLayout) customView.findViewById(R.id.attachment_download_layout);
            final LinearLayout attachmentRetry = (LinearLayout) customView.findViewById(R.id.attachment_retry_layout);
            final RelativeLayout attachmentDownloadProgressLayout = (RelativeLayout) customView.findViewById(R.id.attachment_download_progress_layout);
            final RelativeLayout mainAttachmentLayout = (RelativeLayout) customView.findViewById(R.id.attachment_preview_layout);
            final ProgressBar mediaDownloadProgressBar = (ProgressBar) customView.findViewById(R.id.media_download_progress_bar);
            final ProgressBar mediaUploadProgressBar = (ProgressBar) customView.findViewById(R.id.media_upload_progress_bar);
            if (attachedFile != null) {
                attachedFile.setText("");
                attachedFile.setVisibility(View.GONE);
            }

            if (attachmentIcon != null) {
                attachmentIcon.setVisibility(View.GONE);
            }

            attachmentDownloadLayout.setVisibility(View.GONE);
            preview.setVisibility(message.hasAttachment() ? View.VISIBLE : View.GONE);
            attachmentView.setVisibility(View.GONE);
            if (message.isTypeOutbox() && !message.isCanceled()) {
                mediaUploadProgressBar.setVisibility(message.isAttachmentUploadInProgress() ? View.VISIBLE : View.GONE);
            } else {
                mediaUploadProgressBar.setVisibility(View.GONE);
            }
            if (attachedFile != null) {
                attachedFile.setVisibility(message.hasAttachment() ? View.VISIBLE : View.GONE);
            }
            //Todo: show progress for download image of type inbox

            if (individual && message.getTimeToLive() != null) {
                selfDestruct
                        .setText("Self destruct in " + message.getTimeToLive() + " mins");
                selfDestruct.setVisibility(View.VISIBLE);
            } else {
                selfDestruct.setText("");
                selfDestruct.setVisibility(View.GONE);
            }

            if (sentOrReceived != null) {
                if ((!message.isCall()) || message.isDummyEmptyMessage()) {
                    sentOrReceived.setVisibility(View.GONE);
                } else if (message.isCall()) {
                    sentOrReceived.setImageResource(R.drawable.applozic_ic_action_call_holo_light);
                } else if (getItemViewType(position) == 0) {
                    sentOrReceived.setImageResource(R.drawable.mobicom_social_forward);
                } else {
                    sentOrReceived.setImageResource(R.drawable.mobicom_social_reply);
                }

                if (message.isCall()) {
                    messageTextView.setTextColor(context.getResources().getColor(message.isIncomingCall() ? R.color.incoming_call : R.color.outgoing_call));
                }
            }

            if (message.isCall() || message.isDummyEmptyMessage()) {
                createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            } else if (message.getKeyString() == null && !message.isSentToServer() && message.isTypeOutbox()) {
                createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, message.getScheduledAt() != null ? scheduledIcon : pendingIcon, null);
            } else if (message.getKeyString() != null && message.isTypeOutbox() && message.isSentToServer()) {
                createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, message.getDelivered() || (contact != null && new Support(context).isSupportNumber(contact.getFormattedContactNumber())) ? deliveredIcon : (message.getScheduledAt() != null ? scheduledIcon : sentIcon), null);
            }

            if (message.isCall()) {
                deliveryStatus.setText("");
            }/* else if (message.getType().equals(Message.MessageType.MT_OUTBOX.getValue()) || message.getType().equals(Message.MessageType.MT_INBOX.getValue())) {
                deliveryStatus.setText("via MT");
            } else {
                deliveryStatus.setText("via Carrier");
            }*/

            ApplozicSetting applozicSetting = ApplozicSetting.getInstance(context);
            if (message.isTypeOutbox()) {
                loadContactImage(senderContact, contactImage, alphabeticTextView);
            } else {
                loadContactImage(receiverContact, contactImage, alphabeticTextView);
            }
            if (message.hasAttachment() && attachedFile != null & !(message.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                mainAttachmentLayout.setLayoutParams(getImageLayoutParam(false));
                if (message.getFileMetas() != null && message.getFileMetas().getContentType().contains("image")) {
                    attachedFile.setVisibility(View.GONE);
                }
                if (message.isAttachmentDownloaded()) {
                    preview.setVisibility(View.GONE);
                    String[] filePaths = new String[message.getFilePaths().size()];
                    int i = 0;
                    for (final String filePath : message.getFilePaths()) {
                        filePaths[i++] = filePath;
                        final String mimeType = FileUtils.getMimeType(filePath);
                        if (mimeType != null && mimeType.startsWith("image")) {
                            attachmentView.setMessage(message);
                            attachmentView.setProressBar(mediaDownloadProgressBar);
                            attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                            attachmentView.setVisibility(View.VISIBLE);
                            attachedFile.setVisibility(View.GONE);
                        } else {
                            showAttachmentIconAndText(attachedFile, message, mimeType);
                        }
                    }
                } else if (message.isAttachmentUploadInProgress()) {
                    //showPreview(smListItem, preview, attachmentDownloadLayout);
                    attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                    attachmentView.setProressBar(mediaDownloadProgressBar);
                    attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                    attachmentView.setMessage(message);
                    attachmentView.setVisibility(View.VISIBLE);
                } else if (AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
                    //ondraw is called and thread is assigned to the attachment view...
                    attachmentView.setMessage(message);
                    attachmentView.setVisibility(View.VISIBLE);
                    attachmentView.setProressBar(mediaDownloadProgressBar);
                    attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                    showPreview(message, preview, attachmentDownloadLayout);
                    FileMeta fileMeta = message.getFileMetas();
                    final String mimeType = FileUtils.getMimeType(fileMeta.getName());
                    if (!fileMeta.getContentType().contains("image")) {
                        showAttachmentIconAndText(attachedFile, message, mimeType);
                    }
                    downloadSizeTextView.setText(fileMeta.getSizeInReadableFormat());
                    attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                    attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                } else {
                    String fileKeys = message.getFileMetaKeyStrings();
                    int i = 0;
                    showPreview(message, preview, attachmentDownloadLayout);
                    //TODO: while doing multiple image support in single sms ...we might improve this
                    // for (String fileKey : message.getFileMetaKeyStrings()) {
                    if (message.getFileMetas() != null) {
                        FileMeta fileMeta = message.getFileMetas();
                        attachmentDownloadLayout.setVisibility(View.VISIBLE);
                        attachmentDownloadProgressLayout.setVisibility(View.GONE);
                        downloadSizeTextView.setText(fileMeta.getSizeInReadableFormat());
                        final String mimeType = FileUtils.getMimeType(fileMeta.getName());
                        if (!fileMeta.getContentType().contains("image")) {
                            showAttachmentIconAndText(attachedFile, message, mimeType);
                        }

                    }

                    //  }

                }
            }
            if (message.isCanceled()) {
                attachmentRetry.setVisibility(View.VISIBLE);
            }
            attachmentRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Resending attachment....", Toast.LENGTH_LONG).show();
                    mediaUploadProgressBar.setVisibility(View.VISIBLE);
                    attachmentRetry.setVisibility(View.GONE);
                    //updating Cancel Flag to smListItem....
                    message.setCanceled(false);
                    messageDatabaseService.updateCanceledFlag(message.getMessageId(), 0);
                    conversationService.sendMessage(message, messageIntentClass);
                }
            });
            attachmentDownloadProgressLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attachmentView.setVisibility(View.GONE);
                    attachmentView.cancelDownload();
                    attachmentDownloadProgressLayout.setVisibility(View.GONE);
                    message.setAttDownloadInProgress(false);
                }
            });

            //final ProgressBar mediaDownloadProgressBar = mediaDownloadProgressBar;
            preview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: 1. get the image Size and decide if we can download directly
                    //2. if already downloaded to ds card show it directly ....
                    //3. if deleted from sd crad only ...ask user to download it again or skip ...
                    if (message.getContentType() == Message.ContentType.TEXT_URL.getValue()) {
                        return;
                    }
                    if (message.isAttachmentDownloaded()) {
                        showFullView(message);
                    } else {
                        attachmentDownloadLayout.setVisibility(View.GONE);
                        attachmentView.setProressBar(mediaDownloadProgressBar);
                        attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                        attachmentView.setMessage(message);
                        attachmentView.setVisibility(View.VISIBLE);
                        attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                    }

                }
            });
            preview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
            attachmentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFullView(message);
                }
            });
            attachmentView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
            if (attachedFile != null) {
                attachedFile.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return false;
                    }
                });
            }
            if (message.getScheduledAt() != null) {
                createdAtTime.setText(DateUtils.getFormattedDate(message.getScheduledAt()));
            } else if (createdAtTime != null && message.isDummyEmptyMessage()) {
                createdAtTime.setText("");
            } else if (createdAtTime != null) {
                createdAtTime.setText(DateUtils.getFormattedDate(message.getCreatedAtTime()));
            }
            String mimeType = "";
            if (messageTextView != null) {
                if (message.getContentType() == Message.ContentType.TEXT_URL.getValue()) {
                    try {
                        attachedFile.setVisibility(View.GONE);
                        preview.setVisibility(View.VISIBLE);
                        messageTextView.setText(message.getMessage());
                        loadImage.setImageFadeIn(false);
                        loadImage.loadImage(message.getFileMetas().getBlobKeyString(), preview);
                        attachmentDownloadLayout.setVisibility(View.GONE);
                    } catch (Exception e) {
                    }
                } else if (message.getContentType() == Message.ContentType.PRICE.getValue()) {
                    messageTextView.setText(ConversationUIService.FINAL_PRICE_TEXT + message.getMessage());
                } else {
                    messageTextView.setText(EmoticonUtils.getSmiledText(context, message.getMessage(), emojiconHandler));
                    if (mimeType != null && attachmentIcon != null) {
                        if (mimeType.startsWith("image")) {
                            attachmentIcon.setImageResource(R.drawable.applozic_ic_action_camera);
                        } else if (mimeType.startsWith("video")) {
                            attachmentIcon.setImageResource(R.drawable.applozic_ic_action_video);
                        }
                    }
                }
               /* if (messageTextLayout != null) {
                    //messageTextLayout.setBackgroundResource(messageTypeColorMap.get(message.getType()));
                    *//*messageTextLayout.setBackgroundColor(message.isTypeOutbox() ?
                            applozicSetting.getSentMessageBackgroundColor() : applozicSetting.getReceivedMessageBackgroundColor());*//*

                    if (message.hasAttachment()) {
                        if (TextUtils.isEmpty(message.getMessage())) {
                            messageTextView.setBackgroundColor(context.getResources().getColor(R.color.conversation_list_background));
                        } else {
                            if (message.isTypeOutbox()) {
                                messageTextView.setBackgroundColor(context.getResources().getColor(R.color.sent_message_bg_color));
                            } else {
                                messageTextView.setBackgroundColor(context.getResources().getColor(R.color.received_message_bg_color));
                            }
                        }
                        //messageTextLayout.setLayoutParams(getImageLayoutParam(message.isTypeOutbox()));
                        //messageTextLayout.setBackgroundResource(R.drawable.send_sms_background);
                        customView.findViewById(R.id.messageTextInsideLayout).setBackgroundResource(R.color.attachment_background_color);
                    }
                }*/
            }

        }
        return customView;
    }

    private void loadContactImage(Contact contact, ImageView contactImage, TextView alphabeticTextView) {
        ApplozicSetting applozicSetting = ApplozicSetting.getInstance(context);
        if (!applozicSetting.isConversationContactImageVisible()) {
            return;
        }

        if (alphabeticTextView != null) {
            String contactNumber = contact.getContactNumber().toUpperCase();
            char firstLetter = contact.getDisplayName().toUpperCase().charAt(0);
            if (firstLetter != '+') {
                alphabeticTextView.setText(String.valueOf(firstLetter));
            } else if (contactNumber.length() >= 2) {
                alphabeticTextView.setText(String.valueOf(contactNumber.charAt(1)));
            }

            Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
            /*alphabeticTextView.setTextColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetTextColorMap.get(colorKey)));
            alphabeticTextView.setBackgroundResource(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey));*/
            GradientDrawable bgShape = (GradientDrawable) alphabeticTextView.getBackground();
            bgShape.setColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));
        }

        if (contact.isDrawableResources() && contactImage != null) {
            int drawableResourceId = context.getResources().getIdentifier(contact.getrDrawableName(), "drawable", context.getPackageName());
            contactImage.setImageResource(drawableResourceId);
            contactImage.setVisibility(View.VISIBLE);
            alphabeticTextView.setVisibility(View.GONE);
        } else if (contactImage != null) {
            contactImageLoader.loadImage(contact, contactImage, alphabeticTextView);
        }

    }

    private void showAttachmentIconAndText(TextView attachedFile, final Message message, final String mimeType) {

        String fileName = "";
        if (message.getFileMetas() == null && message.getFilePaths() != null) {
            fileName = message.getFilePaths().get(0).substring(message.getFilePaths().get(0).lastIndexOf("/") + 1);
        } else if (message.getFileMetas() != null) {
            fileName = message.getFileMetas().getName();
        }
        attachedFile.setText(fileName);
        attachedFile.setVisibility(View.VISIBLE);
        attachedFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.isAttachmentDownloaded()) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(message.getFilePaths().get(0))), mimeType);
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                    }
                }
            }

        });
    }

    private void showPreview(Message smListItem, ImageView preview, LinearLayout attachmentDownloadLayout) {
        FileMeta fileMeta = smListItem.getFileMetas();
        imageThumbnailLoader.setImageFadeIn(false);
        imageThumbnailLoader.setLoadingImage(R.id.media_upload_progress_bar);
        imageThumbnailLoader.loadImage(fileMeta, preview);
        attachmentDownloadLayout.setVisibility(View.GONE);
    }

    private void showFullView(Message smListItem) {
        Intent intent = new Intent(context, FullScreenImageActivity.class);
        intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(smListItem, Message.class));
        ((MobiComKitActivityInterface) context).startActivityForResult(intent, MobiComKitActivityInterface.REQUEST_CODE_FULL_SCREEN_ACTION);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        if (message == null) {
            return 0;
        }
        if (message.isTempDateType()) {
            return 2;
        }
        return message.isTypeOutbox() ? 1 : 0;
    }


    public ViewGroup.LayoutParams getImageLayoutParam(boolean outBoxType) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        float wt_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
        ViewGroup.MarginLayoutParams params;
        if (outBoxType) {
            params = new RelativeLayout.LayoutParams(metrics.widthPixels + (int) wt_px * 2, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins((int) wt_px, 0, (int) wt_px, 0);
        } else {
            params = new LinearLayout.LayoutParams(metrics.widthPixels - (int) wt_px * 2, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 0);

        }
        return params;
    }
}
