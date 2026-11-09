package com.phantomgram;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

/**
 * PhantomPreferencesActivity - Main Settings Screen for PhantomGram
 */
public class PhantomPreferencesActivity extends BaseFragment {

    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int rowCount;

    // Ghost Section
    private int ghostHeaderRow;
    private int ghostModeRow;
    private int noReadRow;
    private int noOnlineRow;
    private int noTypingRow;
    private int stealthStoriesRow;
    private int markReadOnReplyRow;
    private int ghostSectionRow;

    // Vault Section
    private int vaultHeaderRow;
    private int saveDeletedRow;
    private int saveEditsRow;
    private int saveDisappearingMediaRow;
    private int allowScreenshotsRow;
    private int vaultSectionRow;

    // Spy Tracker Section
    private int trackerHeaderRow;
    private int onlineTrackerRow;
    private int correlationRow;
    private int trackerSectionRow;

    // iOS Style Section
    private int iosHeaderRow;
    private int iosAnimationsRow;
    private int iosSwipeRow;
    private int iosBlurRow;
    private int iosHapticRow;
    private int iosSectionRow;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        PhantomConfig.init();
        updateRows();
        return true;
    }

    private void updateRows() {
        rowCount = 0;

        // Ghost Mode
        ghostHeaderRow = rowCount++;
        ghostModeRow = rowCount++;
        noReadRow = rowCount++;
        noOnlineRow = rowCount++;
        noTypingRow = rowCount++;
        stealthStoriesRow = rowCount++;
        markReadOnReplyRow = rowCount++;
        ghostSectionRow = rowCount++;

        // Vault
        vaultHeaderRow = rowCount++;
        saveDeletedRow = rowCount++;
        saveEditsRow = rowCount++;
        saveDisappearingMediaRow = rowCount++;
        allowScreenshotsRow = rowCount++;
        vaultSectionRow = rowCount++;

        // Tracker
        trackerHeaderRow = rowCount++;
        onlineTrackerRow = rowCount++;
        correlationRow = rowCount++;
        trackerSectionRow = rowCount++;

        // iOS Styling
        iosHeaderRow = rowCount++;
        iosAnimationsRow = rowCount++;
        iosSwipeRow = rowCount++;
        iosBlurRow = rowCount++;
        iosHapticRow = rowCount++;
        iosSectionRow = rowCount++;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("PhantomGram");
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter = new ListAdapter(context));

        listView.setOnItemClickListener((view, position) -> {
            if (view instanceof TextCheckCell) {
                TextCheckCell cell = (TextCheckCell) view;
                PhantomUI.hapticImpact(view);

                if (position == ghostModeRow) {
                    PhantomConfig.ghostModeEnabled = !PhantomConfig.ghostModeEnabled;
                    cell.setChecked(PhantomConfig.ghostModeEnabled);
                } else if (position == noReadRow) {
                    PhantomConfig.sendReadPackets = !PhantomConfig.sendReadPackets;
                    cell.setChecked(!PhantomConfig.sendReadPackets);
                } else if (position == noOnlineRow) {
                    PhantomConfig.sendOnlinePackets = !PhantomConfig.sendOnlinePackets;
                    cell.setChecked(!PhantomConfig.sendOnlinePackets);
                } else if (position == noTypingRow) {
                    PhantomConfig.sendTypingPackets = !PhantomConfig.sendTypingPackets;
                    cell.setChecked(!PhantomConfig.sendTypingPackets);
                } else if (position == stealthStoriesRow) {
                    PhantomConfig.stealthStories = !PhantomConfig.stealthStories;
                    cell.setChecked(PhantomConfig.stealthStories);
                } else if (position == markReadOnReplyRow) {
                    PhantomConfig.markReadOnReply = !PhantomConfig.markReadOnReply;
                    cell.setChecked(PhantomConfig.markReadOnReply);
                } else if (position == saveDeletedRow) {
                    PhantomConfig.saveDeletedMessages = !PhantomConfig.saveDeletedMessages;
                    cell.setChecked(PhantomConfig.saveDeletedMessages);
                } else if (position == saveEditsRow) {
                    PhantomConfig.saveEditedHistory = !PhantomConfig.saveEditedHistory;
                    cell.setChecked(PhantomConfig.saveEditedHistory);
                } else if (position == saveDisappearingMediaRow) {
                    PhantomConfig.saveDisappearingMedia = !PhantomConfig.saveDisappearingMedia;
                    cell.setChecked(PhantomConfig.saveDisappearingMedia);
                } else if (position == allowScreenshotsRow) {
                    PhantomConfig.allowScreenshotsEverywhere = !PhantomConfig.allowScreenshotsEverywhere;
                    cell.setChecked(PhantomConfig.allowScreenshotsEverywhere);
                } else if (position == onlineTrackerRow) {
                    PhantomConfig.onlineTrackerEnabled = !PhantomConfig.onlineTrackerEnabled;
                    cell.setChecked(PhantomConfig.onlineTrackerEnabled);
                } else if (position == correlationRow) {
                    PhantomConfig.correlationEngineEnabled = !PhantomConfig.correlationEngineEnabled;
                    cell.setChecked(PhantomConfig.correlationEngineEnabled);
                } else if (position == iosAnimationsRow) {
                    PhantomConfig.iosSpringAnimations = !PhantomConfig.iosSpringAnimations;
                    cell.setChecked(PhantomConfig.iosSpringAnimations);
                } else if (position == iosSwipeRow) {
                    PhantomConfig.iosSwipeBack = !PhantomConfig.iosSwipeBack;
                    cell.setChecked(PhantomConfig.iosSwipeBack);
                } else if (position == iosBlurRow) {
                    PhantomConfig.iosBlurEffects = !PhantomConfig.iosBlurEffects;
                    cell.setChecked(PhantomConfig.iosBlurEffects);
                } else if (position == iosHapticRow) {
                    PhantomConfig.iosHapticFeedback = !PhantomConfig.iosHapticFeedback;
                    cell.setChecked(PhantomConfig.iosHapticFeedback);
                }
                PhantomConfig.save();
            }
        });

        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        return fragmentView;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 1; // clickable check cells
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0: // Header
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 1: // Checkbox
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default: // Shadow Divider
                    view = new ShadowSectionCell(mContext);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == ghostHeaderRow) {
                        headerCell.setText("РЕЖИМ ПРИЗРАКА (GHOST MODE)");
                    } else if (position == vaultHeaderRow) {
                        headerCell.setText("ХРАНИЛИЩЕ И АНТИ-УДАЛЕНИЕ (VAULT)");
                    } else if (position == trackerHeaderRow) {
                        headerCell.setText("ШПИОНСКИЙ МОДУЛЬ И АНАЛИЗ");
                    } else if (position == iosHeaderRow) {
                        headerCell.setText("СТИЛЬ IOS И АНИМАЦИИ");
                    }
                    break;
                }
                case 1: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    if (position == ghostModeRow) {
                        checkCell.setTextAndCheck("Режим призрака (Мастер)", PhantomConfig.ghostModeEnabled, true);
                    } else if (position == noReadRow) {
                        checkCell.setTextAndCheck("Не отправлять прочтение сообщений", !PhantomConfig.sendReadPackets, true);
                    } else if (position == noOnlineRow) {
                        checkCell.setTextAndCheck("Не показывать статус «В сети»", !PhantomConfig.sendOnlinePackets, true);
                    } else if (position == noTypingRow) {
                        checkCell.setTextAndCheck("Скрыть «Печатает...» и запись аудио", !PhantomConfig.sendTypingPackets, true);
                    } else if (position == stealthStoriesRow) {
                        checkCell.setTextAndCheck("Скрытный просмотр историй (Stories)", PhantomConfig.stealthStories, true);
                    } else if (position == markReadOnReplyRow) {
                        checkCell.setTextAndCheck("Отмечать прочитанным при отправке ответа", PhantomConfig.markReadOnReply, false);
                    } else if (position == saveDeletedRow) {
                        checkCell.setTextAndCheck("Сохранять удаленные сообщения", PhantomConfig.saveDeletedMessages, true);
                    } else if (position == saveEditsRow) {
                        checkCell.setTextAndCheck("Вести историю изменений сообщений", PhantomConfig.saveEditedHistory, true);
                    } else if (position == saveDisappearingMediaRow) {
                        checkCell.setTextAndCheck("Обход самоуничтожения медиа (View-Once)", PhantomConfig.saveDisappearingMedia, true);
                    } else if (position == allowScreenshotsRow) {
                        checkCell.setTextAndCheck("Разрешить скриншоты во всех чатах", PhantomConfig.allowScreenshotsEverywhere, false);
                    } else if (position == onlineTrackerRow) {
                        checkCell.setTextAndCheck("Логгер онлайна (точный timestamp)", PhantomConfig.onlineTrackerEnabled, true);
                    } else if (position == correlationRow) {
                        checkCell.setTextAndCheck("Анализ совпадений онлайна пар (Correlation)", PhantomConfig.correlationEngineEnabled, false);
                    } else if (position == iosAnimationsRow) {
                        checkCell.setTextAndCheck("Пружинные анимации 120 FPS (Spring)", PhantomConfig.iosSpringAnimations, true);
                    } else if (position == iosSwipeRow) {
                        checkCell.setTextAndCheck("Свайп «Назад» как на iOS", PhantomConfig.iosSwipeBack, true);
                    } else if (position == iosBlurRow) {
                        checkCell.setTextAndCheck("Эффект размытия (Real-time Blur)", PhantomConfig.iosBlurEffects, true);
                    } else if (position == iosHapticRow) {
                        checkCell.setTextAndCheck("Haptic-отклик при взаимодействиях", PhantomConfig.iosHapticFeedback, false);
                    }
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == ghostHeaderRow || position == vaultHeaderRow ||
                    position == trackerHeaderRow || position == iosHeaderRow) {
                return 0; // Header
            }
            if (position == ghostSectionRow || position == vaultSectionRow ||
                    position == trackerSectionRow || position == iosSectionRow) {
                return 2; // Divider
            }
            return 1; // Checkbox
        }
    }
}
