package su.gwarden.music;

/* compiled from: MainActivity.kt */
/* loaded from: classes7.dex */
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    public static final java.lang.String APP_VER = "12.45";
    private final int REQ_COVER;
    private su.gwarden.music.ui.TrackAdapter adapter;
    private java.lang.String backTab;
    private java.lang.String beatTrackId;
    private su.gwarden.music.ui.LiquidBlob chipBlob;
    private kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> coverCb;
    private boolean crashedLastRun;
    private boolean hangWatchOn;
    private android.widget.EditText editQuery;
    private android.widget.TextView emptyText;
    private android.widget.LinearLayout feedList;
    private android.view.View feedScroll;
    private android.widget.TextView hSub;
    private android.widget.TextView hTitle;
    private boolean hideBlocked;
    private su.gwarden.music.ui.LiquidNav liquidNav;
    /** шестерёнка поверх контента (на вкладках шапки нет) */
    private android.view.View settingsBtn;
    /** поиск: треки / артисты / альбомы / плейлисты */
    private android.view.View searchTypes;
    private java.lang.String searchTab = "tracks";
    private su.gwarden.music.api.ApiClient.SearchAll searchData;
    private java.util.List<su.gwarden.music.api.ApiClient.LibraryArtist> myArtists;
    private su.gwarden.music.ui.RowAdapter rowAdapter;
    private android.view.View settingsScroll;
    private android.widget.LinearLayout settingsBox;
    private android.view.View artistScroll;
    private android.widget.ScrollView libScroll;
    private android.widget.LinearLayout libBox;
    private android.widget.LinearLayout libArtistsBox;
    private int artistReq = 0;
    private boolean artistReqDone = true;
    private android.view.View pullHint;
    private boolean homeRefreshing = false;
    private float pullStartY = -1f;
    private float pullDist = 0f;
    private int libTracksCount = -1;
    private long libTracksAt = 0L;
    private int libPlCount = -1;
    private long libPlAt = 0L;
    private android.widget.LinearLayout artistBox;
    private boolean loading;
    /** импорт из Spotify/по ссылке: поллинг задачи и защита от старых ответов */
    private final android.os.Handler uiHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private int importToken = 0;
    private android.widget.FrameLayout loginOverlay;
    private java.lang.Runnable loginPoll;
    private long vkFixAt = 0L;
    private boolean vkFixing = false;
    private int chipBlobTries = 0;
    private boolean homeLoading = false;
    private int beatRetries = 0;
    private android.view.View sortRow;
    private android.widget.ImageView sortBtn;
    private android.widget.TextView scChip;
    private boolean onlySc = false;
    private java.lang.String mineSort = "new";
    private boolean mineSortRemember = false;
    private long mainBeat;
    private final int[] navColors;
    private final int[] navIcons;
    private final int[] navLabels;
    private android.view.View nowAdd;
    private android.widget.ImageButton nowLike;
    private kotlin.jvm.functions.Function1<? super su.gwarden.music.model.Track, kotlin.Unit> nowBind;
    private android.view.View nowNext;
    private boolean onWave;
    private su.gwarden.music.ui.PlaylistAdapter plAdapter;
    private su.gwarden.music.player.PlayerController player;
    private su.gwarden.music.ui.PlayingHighlight playingHighlight;
    private int playlistReq;
    private su.gwarden.music.ui.IosSpinner progress;
    private android.app.AlertDialog qrDialog;
    private androidx.recyclerview.widget.RecyclerView recycler;
    private java.lang.Runnable searchJob;
    private android.widget.LinearLayout searchRow;
    private boolean similarEnded;
    private boolean similarRefilling;
    private java.lang.String similarArtist;
    private java.lang.String similarSeed;
    private java.lang.String similarTitle;
    private android.widget.TextView waveArtist;
    private android.widget.TextView waveBack;
    private android.widget.ImageView waveCover;
    private boolean waveEnded;
    private su.gwarden.music.ui.EqBackground waveEq;
    private int waveGen;
    private android.widget.TextView waveHint;
    private boolean waveLoading;
    private android.view.View waveOrb;
    private android.view.View wavePanel;
    private android.widget.TextView wavePlay;
    private boolean waveRefilling;
    private int waveRetry;
    private android.widget.TextView waveTitle;
    private java.lang.String source = "vk";
    private java.lang.String tab = "home";
    private java.util.List<su.gwarden.music.model.Track> myTracks = kotlin.collections.CollectionsKt.emptyList();
    private java.util.List<su.gwarden.music.model.Track> waveQueue = kotlin.collections.CollectionsKt.emptyList();
    private java.util.List<su.gwarden.music.model.Track> similarQueue = kotlin.collections.CollectionsKt.emptyList();
    private final android.os.Handler ui = new android.os.Handler(android.os.Looper.getMainLooper());
    private final java.util.HashSet<java.lang.String> mineIds = new java.util.HashSet<>();
    private java.util.List<su.gwarden.music.model.HomeBlock> feedBlocks = kotlin.collections.CollectionsKt.emptyList();
    private final int REQ_EQ = 7;
    private int tintColor;
    private boolean tintReady;
    private android.animation.ValueAnimator tintAnim;
    private final su.gwarden.music.ui.ArtworkTint.Listener tintListener =
        new su.gwarden.music.ui.ArtworkTint.Listener() {
            @Override
            public void onColor(int color) {
                su.gwarden.music.MainActivity.this.applyAppTint(color);
            }
        };
    private final java.util.List<java.lang.String> iconsOrder = java.util.Arrays.asList("home", "search", "my", "lib");

    public MainActivity() {
        int[] iArr = new int[4];
        for (int i = 0; i < 4; i++) {
            iArr[i] = -9737365;
        }
        this.navColors = iArr;
        this.navIcons = new int[]{su.gwarden.music.R.id.nav_home_ic, su.gwarden.music.R.id.nav_search_ic, su.gwarden.music.R.id.nav_my_ic, su.gwarden.music.R.id.nav_lib_ic};
        this.navLabels = new int[]{su.gwarden.music.R.id.nav_home_tx, su.gwarden.music.R.id.nav_search_tx, su.gwarden.music.R.id.nav_my_tx, su.gwarden.music.R.id.nav_lib_tx};
        this.REQ_COVER = 7717;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(android.os.Bundle savedInstanceState) {
        // Шрифт/тема читаются при создании вьюх — строго до super.onCreate.
        su.gwarden.music.ui.Skin.INSTANCE.applyTheme(this);
        su.gwarden.music.audio.Fx.INSTANCE.load(this);
        super.onCreate(savedInstanceState);
        setContentView(su.gwarden.music.R.layout.activity_main);
        su.gwarden.music.ui.UpdateBanner.INSTANCE.attach(this);
        initSwipe();
        this.searchTypes = findViewById(su.gwarden.music.R.id.search_types);
        this.rowAdapter = new su.gwarden.music.ui.RowAdapter(
                item -> { onRowClick(item); return kotlin.Unit.INSTANCE; });
        bindSearchChips();
        this.settingsScroll = findViewById(su.gwarden.music.R.id.settings_scroll);
        this.settingsBox = (android.widget.LinearLayout) findViewById(su.gwarden.music.R.id.settings_box);
        this.artistScroll = findViewById(su.gwarden.music.R.id.artist_scroll);
        this.artistBox = (android.widget.LinearLayout) findViewById(su.gwarden.music.R.id.artist_box);
        // парящая шестерёнка больше не нужна: настройки в верхней плашке
        su.gwarden.music.ui.ArtworkTint.INSTANCE.addListener(this.tintListener);
        android.view.View startHeader = findViewById(su.gwarden.music.R.id.app_header);
        if (startHeader != null) {
            startHeader.setVisibility(8);
        }
        su.gwarden.music.api.ApiClient.INSTANCE.init(this);
        installCrashLogger();
        installHangDetector();
        try {
            su.gwarden.music.MainActivity $this$onCreate_u24lambda_u240 = this;
            android.content.SharedPreferences prefs = $this$onCreate_u24lambda_u240.getSharedPreferences("music_player", 0);
            if (prefs.getBoolean("running", false)) {
                $this$onCreate_u24lambda_u240.crashedLastRun = true;
            }
            prefs.edit().putBoolean("running", true).apply();
        } catch (java.lang.Throwable th) {
        }
        su.gwarden.music.ui.ImageLoader.INSTANCE.init(this);
        su.gwarden.music.offline.Offline.INSTANCE.init(this);
        su.gwarden.music.eco.Eco.INSTANCE.init(this);
        su.gwarden.music.model.LocalLibrary.INSTANCE.init(this, libPrefix());
        su.gwarden.music.model.LocalPlaylists.INSTANCE.init(this, libPrefix());
        su.gwarden.music.api.ApiClient.INSTANCE.log("start", "v" + APP_VER + " android");
        if (android.os.Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
            requestPermissions(new java.lang.String[]{"android.permission.POST_NOTIFICATIONS"}, 12);
        }
        su.gwarden.music.vk.VkLocal.INSTANCE.init(this);
        su.gwarden.music.vk.VkApi.INSTANCE.init(this);
        // токен VK живёт несколько часов: протух - тихо обновляем его из веб-клиента
        // раз в 90 минут тихо обновляем токен, пока он ещё живой -
        // так он не успевает протухнуть и не упираемся в лимиты VK
        final android.os.Handler _tokenTick = this.ui;
        _tokenTick.postDelayed(new java.lang.Runnable() {
            @Override public void run() {
                try {
                    if (su.gwarden.music.api.ApiClient.INSTANCE.getLoggedIn()) {
                        su.gwarden.music.vk.VkTokenRefresher.INSTANCE.refresh(MainActivity.this,
                            new kotlin.jvm.functions.Function1<java.lang.Boolean, kotlin.Unit>() {
                                @Override public kotlin.Unit invoke(java.lang.Boolean ok) {
                                    return kotlin.Unit.INSTANCE;
                                }
                            });
                    }
                } catch (java.lang.Throwable ignored) {
                }
                _tokenTick.postDelayed(this, 90L * 60L * 1000L);
            }
        }, 90L * 60L * 1000L);
        su.gwarden.music.vk.VkApi.INSTANCE.setOnTokenRejected(new kotlin.jvm.functions.Function0<kotlin.Unit>() {
            @Override
            public kotlin.Unit invoke() {
                su.gwarden.music.vk.VkTokenRefresher.INSTANCE.refresh(MainActivity.this,
                    new kotlin.jvm.functions.Function1<java.lang.Boolean, kotlin.Unit>() {
                        @Override
                        public kotlin.Unit invoke(java.lang.Boolean ok) {
                            if (ok) {
                                setTab(tab);
                            } else {
                                // тихо не получилось - открываем WebView сами:
                                // если сессия VK жива в куках, он закроется сам
                                MainActivity.this.onVkSessionLost();
                            }
                            return kotlin.Unit.INSTANCE;
                        }
                    });
                return kotlin.Unit.INSTANCE;
            }
        });
        // Сессия Wonder истекла (401 «need login»): показываем ОБЩИЙ экран
        // входа (VK / Google / пароль), а не сразу VK. Раньше Google-аккаунт
        // без VK при любом 401 выбрасывало именно на VK-вход.
        su.gwarden.music.api.ApiClient.INSTANCE.setOnAuthExpired(new kotlin.jvm.functions.Function0<kotlin.Unit>() {
            @Override
            public kotlin.Unit invoke() {
                MainActivity.this.showLoginScreen();
                return kotlin.Unit.INSTANCE;
            }
        });
        // Токен SoundCloud (JWT) живёт ~1 час. Раз в 40 минут тихо обновляем
        // его в невидимом WebView, иначе лайки SC «пропадают» из Моей музыки.
        final android.os.Handler scTick = new android.os.Handler(android.os.Looper.getMainLooper());
        scTick.postDelayed(new java.lang.Runnable() {
            @Override
            public void run() {
                if (su.gwarden.music.api.ApiClient.INSTANCE.getLoggedIn()) {
                    // тихое обновление токена SoundCloud: невидимый WebView 1x1
                    // прямо в этом окне (без переключения активити и мигания)
                    su.gwarden.music.vk.ScTokenRefresher.INSTANCE.refresh(
                        MainActivity.this, false, new kotlin.jvm.functions.Function1<java.lang.Boolean, kotlin.Unit>() {
                            @Override
                            public kotlin.Unit invoke(java.lang.Boolean ok) {
                                return kotlin.Unit.INSTANCE;
                            }
                        });
                }
                scTick.postDelayed(this, 40L * 60L * 1000L);
            }
        }, 5000L);
        // сортировка «Моей музыки»: если человек нажал «Запомнить», выбор
        // переживает и перезапуск, и краш приложения
        this.sortRow = findViewById(su.gwarden.music.R.id.sort_row);
        this.sortBtn = (android.widget.ImageView) findViewById(su.gwarden.music.R.id.sort_btn);
        this.scChip = (android.widget.TextView) findViewById(su.gwarden.music.R.id.sc_chip);
        android.content.SharedPreferences sortSp = getSharedPreferences("music_player", 0);
        this.mineSortRemember = sortSp.getBoolean("mine_sort_remember", false);
        if (this.mineSortRemember) {
            this.mineSort = sortSp.getString("mine_sort", "new");
        }
        final android.view.View navDiag = findViewById(su.gwarden.music.R.id.nav_wrap);
        if (navDiag != null) {
            navDiag.post(new java.lang.Runnable() {
                @Override
                public void run() {
                    dumpLayout();
                }
            });
            navDiag.postDelayed(new java.lang.Runnable() {
                @Override
                public void run() {
                    dumpLayout();
                }
            }, 3000L);
        }
        this.hideBlocked = getSharedPreferences("music_player", 0).getBoolean("hide_blocked", false);
        su.gwarden.music.player.PlayerController it = su.gwarden.music.player.PlayerHub.INSTANCE.getController();
        if (it == null) {
            android.content.Context applicationContext = getApplicationContext();
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(applicationContext, "getApplicationContext(...)");
            it = new su.gwarden.music.player.PlayerController(applicationContext);
            su.gwarden.music.player.PlayerHub.INSTANCE.setController(it);
        }
        this.player = it;
        su.gwarden.music.player.PlayerController playerController = this.player;
        su.gwarden.music.ui.LiquidNav liquidNav = null;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        playerController.setFindReplacement(new kotlin.jvm.functions.Function2() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda109
            @Override // kotlin.jvm.functions.Function2
            public final java.lang.Object invoke(java.lang.Object obj, java.lang.Object obj2) {
                kotlin.Unit onCreate$lambda$3;
                onCreate$lambda$3 = su.gwarden.music.MainActivity.onCreate$lambda$3((su.gwarden.music.model.Track) obj, (kotlin.jvm.functions.Function1) obj2);
                return onCreate$lambda$3;
            }
        });
        su.gwarden.music.player.PlayerController playerController2 = this.player;
        if (playerController2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController2 = null;
        }
        playerController2.setLocalVkUrl(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda122
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                java.lang.String onCreate$lambda$4;
                onCreate$lambda$4 = su.gwarden.music.MainActivity.onCreate$lambda$4((su.gwarden.music.model.Track) obj);
                return onCreate$lambda$4;
            }
        });
        this.adapter = new su.gwarden.music.ui.TrackAdapter(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda134
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit onCreate$lambda$5;
                onCreate$lambda$5 = su.gwarden.music.MainActivity.onCreate$lambda$5(su.gwarden.music.MainActivity.this, (su.gwarden.music.model.Track) obj);
                return onCreate$lambda$5;
            }
        }, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda141
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit onCreate$lambda$6;
                onCreate$lambda$6 = su.gwarden.music.MainActivity.onCreate$lambda$6(su.gwarden.music.MainActivity.this, (su.gwarden.music.model.Track) obj);
                return onCreate$lambda$6;
            }
        });
        su.gwarden.music.ui.TrackAdapter trackAdapter = this.adapter;
        if (trackAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter = null;
        }
        trackAdapter.setOnPlayingMoved(new kotlin.jvm.functions.Function0() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda142
            @Override // kotlin.jvm.functions.Function0
            public final java.lang.Object invoke() {
                kotlin.Unit onCreate$lambda$7;
                onCreate$lambda$7 = su.gwarden.music.MainActivity.onCreate$lambda$7(su.gwarden.music.MainActivity.this);
                return onCreate$lambda$7;
            }
        });
        su.gwarden.music.ui.TrackAdapter trackAdapter2 = this.adapter;
        if (trackAdapter2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter2 = null;
        }
        trackAdapter2.setOnMenu(new kotlin.jvm.functions.Function2() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda144
            @Override // kotlin.jvm.functions.Function2
            public final java.lang.Object invoke(java.lang.Object obj, java.lang.Object obj2) {
                kotlin.Unit onCreate$lambda$8;
                onCreate$lambda$8 = su.gwarden.music.MainActivity.onCreate$lambda$8(su.gwarden.music.MainActivity.this, (su.gwarden.music.model.Track) obj, (android.view.View) obj2);
                return onCreate$lambda$8;
            }
        });
        this.plAdapter = new su.gwarden.music.ui.PlaylistAdapter(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda145
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit onCreate$lambda$9;
                onCreate$lambda$9 = su.gwarden.music.MainActivity.onCreate$lambda$9(su.gwarden.music.MainActivity.this, (su.gwarden.music.model.VkPlaylist) obj);
                return onCreate$lambda$9;
            }
        });
        this.recycler = (androidx.recyclerview.widget.RecyclerView) findViewById(su.gwarden.music.R.id.recycler);
        androidx.recyclerview.widget.RecyclerView recyclerView = this.recycler;
        if (recyclerView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView = null;
        }
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        androidx.recyclerview.widget.RecyclerView recyclerView2 = this.recycler;
        if (recyclerView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView2 = null;
        }
        androidx.recyclerview.widget.DefaultItemAnimator $this$onCreate_u24lambda_u2410 = new androidx.recyclerview.widget.DefaultItemAnimator();
        $this$onCreate_u24lambda_u2410.setSupportsChangeAnimations(false);
        $this$onCreate_u24lambda_u2410.setAddDuration(180L);
        $this$onCreate_u24lambda_u2410.setMoveDuration(220L);
        recyclerView2.setItemAnimator($this$onCreate_u24lambda_u2410);
        attachTrackTouchHelper();
        androidx.recyclerview.widget.RecyclerView recyclerView3 = this.recycler;
        if (recyclerView3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView3 = null;
        }
        su.gwarden.music.ui.TrackAdapter trackAdapter3 = this.adapter;
        if (trackAdapter3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter3 = null;
        }
        recyclerView3.setAdapter(trackAdapter3);
        this.editQuery = (android.widget.EditText) findViewById(su.gwarden.music.R.id.edit_query);
        this.progress = (su.gwarden.music.ui.IosSpinner) findViewById(su.gwarden.music.R.id.progress);
        this.emptyText = (android.widget.TextView) findViewById(su.gwarden.music.R.id.empty_text);
        this.liquidNav = (su.gwarden.music.ui.LiquidNav) findViewById(su.gwarden.music.R.id.nav_bubble);
        this.chipBlob = (su.gwarden.music.ui.LiquidBlob) findViewById(su.gwarden.music.R.id.chip_blob);
        su.gwarden.music.ui.TrackAdapter trackAdapter4 = this.adapter;
        if (trackAdapter4 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter4 = null;
        }
        this.playingHighlight = new su.gwarden.music.ui.PlayingHighlight(trackAdapter4);
        androidx.recyclerview.widget.RecyclerView recyclerView4 = this.recycler;
        if (recyclerView4 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView4 = null;
        }
        su.gwarden.music.ui.PlayingHighlight playingHighlight = this.playingHighlight;
        if (playingHighlight == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("playingHighlight");
            playingHighlight = null;
        }
        recyclerView4.addItemDecoration(playingHighlight);
        this.searchRow = (android.widget.LinearLayout) findViewById(su.gwarden.music.R.id.search_row);
        this.hTitle = (android.widget.TextView) findViewById(su.gwarden.music.R.id.h_title);
        this.hSub = (android.widget.TextView) findViewById(su.gwarden.music.R.id.h_sub);
        this.wavePanel = findViewById(su.gwarden.music.R.id.wave_panel);
        this.waveOrb = findViewById(su.gwarden.music.R.id.wave_orb);
        this.waveCover = (android.widget.ImageView) findViewById(su.gwarden.music.R.id.wave_cover);
        this.waveTitle = (android.widget.TextView) findViewById(su.gwarden.music.R.id.wave_title);
        this.waveArtist = (android.widget.TextView) findViewById(su.gwarden.music.R.id.wave_artist);
        this.wavePlay = (android.widget.TextView) findViewById(su.gwarden.music.R.id.wave_play);
        this.waveHint = (android.widget.TextView) findViewById(su.gwarden.music.R.id.wave_hint);
        this.waveBack = (android.widget.TextView) findViewById(su.gwarden.music.R.id.wave_back);
        android.widget.TextView textView = this.waveBack;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveBack");
            textView = null;
        }
        textView.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda146
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.onCreate$lambda$11(su.gwarden.music.MainActivity.this, view);
            }
        });
        this.waveEq = (su.gwarden.music.ui.EqBackground) findViewById(su.gwarden.music.R.id.wave_eq);
        android.view.View waveAddBtn = findViewById(su.gwarden.music.R.id.wave_add);
        if (waveAddBtn != null) {
            waveAddBtn.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    su.gwarden.music.MainActivity.this.onWaveAdd();
                }
            });
        }
        if (this.waveEq != null) {
            this.waveEq.setOrbSync(new kotlin.jvm.functions.Function0<kotlin.Unit>() {
                @Override // kotlin.jvm.functions.Function0
                public final kotlin.Unit invoke() {
                    MainActivity.this.syncEqOrb();
                    return kotlin.Unit.INSTANCE;
                }
            });
        }
        this.feedScroll = findViewById(su.gwarden.music.R.id.feed_scroll);
        this.feedList = (android.widget.LinearLayout) findViewById(su.gwarden.music.R.id.feed_list);
        setupPullRefresh();
        this.libScroll = (android.widget.ScrollView) findViewById(su.gwarden.music.R.id.lib_scroll);
        this.libBox = (android.widget.LinearLayout) findViewById(su.gwarden.music.R.id.lib_box);
        android.widget.ImageView imageView = this.waveCover;
        if (imageView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveCover");
            imageView = null;
        }
        imageView.addOnLayoutChangeListener(new android.view.View.OnLayoutChangeListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda147
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(android.view.View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                su.gwarden.music.MainActivity.this.syncEqOrb();
            }
        });
        android.view.View.OnLayoutChangeListener orbSync = new android.view.View.OnLayoutChangeListener() {
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(android.view.View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                su.gwarden.music.MainActivity.this.syncEqOrb();
            }
        };
        android.view.View panelForSync = this.wavePanel;
        if (panelForSync != null) {
            panelForSync.addOnLayoutChangeListener(orbSync);
        }
        android.view.View orbForSync = this.waveOrb;
        if (orbForSync != null) {
            orbForSync.addOnLayoutChangeListener(orbSync);
        }
        su.gwarden.music.ui.EqBackground eqBackground = this.waveEq;
        if (eqBackground == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveEq");
            eqBackground = null;
        }
        eqBackground.setPositionProvider(new kotlin.jvm.functions.Function0() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda148
            @Override // kotlin.jvm.functions.Function0
            public final java.lang.Object invoke() {
                int onCreate$lambda$13;
                onCreate$lambda$13 = su.gwarden.music.MainActivity.onCreate$lambda$13(su.gwarden.music.MainActivity.this);
                return java.lang.Integer.valueOf(onCreate$lambda$13);
            }
        });
        su.gwarden.music.ui.EqBackground eqBackground2 = this.waveEq;
        if (eqBackground2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveEq");
            eqBackground2 = null;
        }
        eqBackground2.setOnBeat(new kotlin.jvm.functions.Function0() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda111
            @Override // kotlin.jvm.functions.Function0
            public final java.lang.Object invoke() {
                kotlin.Unit onCreate$lambda$15;
                onCreate$lambda$15 = su.gwarden.music.MainActivity.onCreate$lambda$15(su.gwarden.music.MainActivity.this);
                return onCreate$lambda$15;
            }
        });
        android.widget.TextView textView2 = this.wavePlay;
        if (textView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePlay");
            textView2 = null;
        }
        textView2.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda112
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.toggleWave();
            }
        });
        ((android.widget.ImageButton) findViewById(su.gwarden.music.R.id.btn_back)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda113
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.goBack();
            }
        });
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) { // from class: su.gwarden.music.MainActivity$onCreate$17

            @Override // androidx.activity.OnBackPressedCallback
            public void handleOnBackPressed() {
                java.lang.String bt = su.gwarden.music.MainActivity.this.backTab;
                if (bt != null) {
                    su.gwarden.music.MainActivity.this.goBack();
                    return;
                }
                // ГРАБЛЯ: на поиске/шортсах/волне/библиотеке backTab = null, и
                // системная «назад» выходила из приложения. Теперь любая
                // не-главная вкладка возвращает на главную.
                java.lang.String tab = su.gwarden.music.MainActivity.this.tab;
                if (tab != null && !"home".equals(tab)) {
                    su.gwarden.music.MainActivity.this.setTab("home");
                    return;
                }
                setEnabled(false);
                su.gwarden.music.MainActivity.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });
        ((android.widget.ImageButton) findViewById(su.gwarden.music.R.id.btn_search_top)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda114
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.setTab("search");
            }
        });
        android.view.View profileBtn = findViewById(su.gwarden.music.R.id.profile_btn);
        if (profileBtn != null) {
            profileBtn.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View view) {
                    try {
                        startActivity(new android.content.Intent(
                                MainActivity.this, su.gwarden.music.ProfileActivity.class));
                    } catch (java.lang.Throwable ignored) {
                    }
                }
            });
        }
        android.view.View topSettings = findViewById(su.gwarden.music.R.id.btn_settings);
        if (topSettings != null) {
            topSettings.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View view) {
                    su.gwarden.music.MainActivity.this.showSettings();
                }
            });
        }
        ((android.widget.ImageButton) findViewById(su.gwarden.music.R.id.btn_more)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda115
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.showSettings();
            }
        });
        ((android.widget.TextView) findViewById(su.gwarden.music.R.id.chip_home)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda116
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.setTab("home");
            }
        });
        ((android.widget.TextView) findViewById(su.gwarden.music.R.id.chip_search)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda117
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.setTab("search");
            }
        });
        ((android.widget.TextView) findViewById(su.gwarden.music.R.id.chip_my)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda118
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.setTab("my");
            }
        });
        ((android.widget.TextView) findViewById(su.gwarden.music.R.id.chip_lib)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda119
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.setTab("lib");
            }
        });
        ((android.widget.LinearLayout) findViewById(su.gwarden.music.R.id.nav_home)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda120
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.setTab("home");
            }
        });
        ((android.widget.LinearLayout) findViewById(su.gwarden.music.R.id.nav_search)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda123
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.setTab("search");
            }
        });
        ((android.widget.LinearLayout) findViewById(su.gwarden.music.R.id.nav_shorts)).setOnClickListener(new android.view.View.OnClickListener() {
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.startActivity(new android.content.Intent(
                    su.gwarden.music.MainActivity.this, su.gwarden.music.ShortsActivity.class));
                su.gwarden.music.MainActivity.this.overridePendingTransition(
                    android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        ((android.widget.LinearLayout) findViewById(su.gwarden.music.R.id.nav_my)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda124
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.setTab("my");
            }
        });
        android.view.View libNavForMenu = findViewById(su.gwarden.music.R.id.nav_lib);
        if (libNavForMenu != null) {
            libNavForMenu.setOnLongClickListener(new android.view.View.OnLongClickListener() {
                @Override
                public boolean onLongClick(android.view.View v) {
                    su.gwarden.music.MainActivity.this.showSettings();
                    return true;
                }
            });
        }
        ((android.widget.LinearLayout) findViewById(su.gwarden.music.R.id.nav_lib)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda125
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.setTab("lib");
            }
        });
        ((android.widget.TextView) findViewById(su.gwarden.music.R.id.source_label)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda126
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.toggleSource();
            }
        });
        ((android.widget.TextView) findViewById(su.gwarden.music.R.id.source_label)).setText("VK");
        final android.widget.ImageButton miniPlay = (android.widget.ImageButton) findViewById(su.gwarden.music.R.id.mini_play);
        android.widget.ImageButton miniNext = (android.widget.ImageButton) findViewById(su.gwarden.music.R.id.mini_next);
        miniPlay.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda127
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.onCreate$lambda$29(su.gwarden.music.MainActivity.this, view);
            }
        });
        miniNext.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda128
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.onCreate$lambda$30(su.gwarden.music.MainActivity.this, view);
            }
        });
        findViewById(su.gwarden.music.R.id.mini_add).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda129
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.onCreate$lambda$32(su.gwarden.music.MainActivity.this, view);
            }
        });
        ((android.widget.LinearLayout) findViewById(su.gwarden.music.R.id.mini)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda130
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.showNowPlaying();
            }
        });
        // крестик справа: закрыть трек и убрать плашку (как в Spotify)
        findViewById(su.gwarden.music.R.id.mini_close).setOnClickListener(new android.view.View.OnClickListener() {
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.closeCurrentTrack();
            }
        });
        su.gwarden.music.player.PlayerController playerController3 = this.player;
        if (playerController3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController3 = null;
        }
        playerController3.setOnState(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda131
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit onCreate$lambda$35;
                onCreate$lambda$35 = su.gwarden.music.MainActivity.onCreate$lambda$35(su.gwarden.music.MainActivity.this, miniPlay, ((java.lang.Boolean) obj).booleanValue());
                return onCreate$lambda$35;
            }
        });
        su.gwarden.music.player.PlayerController playerController4 = this.player;
        if (playerController4 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController4 = null;
        }
        playerController4.setOnQueueEnd(new kotlin.jvm.functions.Function0() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda133
            @Override // kotlin.jvm.functions.Function0
            public final java.lang.Object invoke() {
                kotlin.Unit onCreate$lambda$36;
                onCreate$lambda$36 = su.gwarden.music.MainActivity.onCreate$lambda$36(su.gwarden.music.MainActivity.this);
                return onCreate$lambda$36;
            }
        });
        su.gwarden.music.player.PlayerController playerController5 = this.player;
        if (playerController5 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController5 = null;
        }
        // Прямая ссылка на VK-трек С ТЕЛЕФОНА: когда сервер не может отдать
        // поток (мост занят/не ответил/трек закрыт для Германии), плеер
        // спрашивает ссылку у VK локально и играет напрямую. Раньше этот
        // обработчик НЕ БЫЛ подключён - фоллбэк существовал только в коде.
        try {
            playerController5.setLocalVkUrl(new kotlin.jvm.functions.Function1<su.gwarden.music.model.Track, java.lang.String>() {
                @Override
                public java.lang.String invoke(su.gwarden.music.model.Track t) {
                    try {
                        return su.gwarden.music.vk.VkApi.INSTANCE.trackUrl(t.getId(), t.getAccessKey());
                    } catch (java.lang.Throwable e) {
                        return null;
                    }
                }
            });
        } catch (java.lang.Throwable ignored) {
        }
        su.gwarden.music.player.PlayerController playerController6x = this.player;
        if (playerController6x == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController6x = null;
        }
        playerController6x.setOnTrackChanged(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda135
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit onCreate$lambda$38;
                onCreate$lambda$38 = su.gwarden.music.MainActivity.onCreate$lambda$38(su.gwarden.music.MainActivity.this, (su.gwarden.music.model.Track) obj);
                return onCreate$lambda$38;
            }
        });
        su.gwarden.music.player.PlayerController playerController6 = this.player;
        if (playerController6 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController6 = null;
        }
        playerController6.setOnReadyChanged(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda136
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit onCreate$lambda$39;
                onCreate$lambda$39 = su.gwarden.music.MainActivity.onCreate$lambda$39(su.gwarden.music.MainActivity.this, ((java.lang.Boolean) obj).booleanValue());
                return onCreate$lambda$39;
            }
        });
        su.gwarden.music.player.PlayerController playerController7 = this.player;
        if (playerController7 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController7 = null;
        }
        playerController7.setOnError(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda137
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit onCreate$lambda$40;
                onCreate$lambda$40 = su.gwarden.music.MainActivity.onCreate$lambda$40(su.gwarden.music.MainActivity.this, (java.lang.String) obj);
                return onCreate$lambda$40;
            }
        });
        su.gwarden.music.player.PlayerController playerController8 = this.player;
        if (playerController8 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController8 = null;
        }
        playerController8.setOnProgress(new kotlin.jvm.functions.Function2() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda138
            @Override // kotlin.jvm.functions.Function2
            public final java.lang.Object invoke(java.lang.Object obj, java.lang.Object obj2) {
                kotlin.Unit onCreate$lambda$41;
                onCreate$lambda$41 = su.gwarden.music.MainActivity.onCreate$lambda$41(su.gwarden.music.MainActivity.this, ((java.lang.Integer) obj).intValue(), (java.lang.String) obj2);
                return onCreate$lambda$41;
            }
        });
        android.widget.EditText editText = this.editQuery;
        if (editText == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("editQuery");
            editText = null;
        }
        editText.addTextChangedListener(new android.text.TextWatcher() { // from class: su.gwarden.music.MainActivity$onCreate$39
            @Override // android.text.TextWatcher
            public void beforeTextChanged(java.lang.CharSequence s, int a, int b, int c) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(java.lang.CharSequence s, int a, int b, int c) {
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(android.text.Editable s) {
                if (s != null && s.length() >= 2) {
                    su.gwarden.music.MainActivity.this.scheduleSearch();
                } else if (s != null && s.length() == 0) {
                    su.gwarden.music.MainActivity.this.clearSearchResults();
                }
            }
        });
        android.widget.EditText editText2 = this.editQuery;
        if (editText2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("editQuery");
            editText2 = null;
        }
        editText2.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda139
            @Override // android.widget.TextView.OnEditorActionListener
            public final boolean onEditorAction(android.widget.TextView textView3, int i, android.view.KeyEvent keyEvent) {
                boolean onCreate$lambda$42;
                onCreate$lambda$42 = su.gwarden.music.MainActivity.onCreate$lambda$42(su.gwarden.music.MainActivity.this, textView3, i, keyEvent);
                return onCreate$lambda$42;
            }
        });
        su.gwarden.music.ui.LiquidNav liquidNav2 = this.liquidNav;
        if (liquidNav2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("liquidNav");
        } else {
            liquidNav = liquidNav2;
        }
        liquidNav.bindIcons(java.util.Arrays.asList(findViewById(su.gwarden.music.R.id.nav_home_ic), findViewById(su.gwarden.music.R.id.nav_search_ic), findViewById(su.gwarden.music.R.id.nav_my_ic), findViewById(su.gwarden.music.R.id.nav_lib_ic)));
        su.gwarden.music.WonderAuth.INSTANCE.initLang(this);
        su.gwarden.music.api.ApiClient.INSTANCE.setOnServerWarning(
            new kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit>() {
                @Override
                public kotlin.Unit invoke(java.lang.String msg) {
                    showPill(msg);
                    return kotlin.Unit.INSTANCE;
                }
            });
        if (su.gwarden.music.api.ApiClient.INSTANCE.getLoggedIn()) {
            su.gwarden.music.vk.VkRelay.INSTANCE.start();
            setTab("home");
            openArtistFromIntent(getIntent());
            prefetchMineIds();
            // первый запуск новой версии: слайд с выбором языка
            if (!su.gwarden.music.WonderAuth.INSTANCE.isOnboarded(this)) {
                su.gwarden.music.WonderAuth.INSTANCE.show(this);
            }
            su.gwarden.music.vk.VkLocal.INSTANCE.ensure(this, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda140
                @Override // kotlin.jvm.functions.Function1
                public final java.lang.Object invoke(java.lang.Object obj) {
                    kotlin.Unit onCreate$lambda$45;
                    onCreate$lambda$45 = su.gwarden.music.MainActivity.onCreate$lambda$45(su.gwarden.music.MainActivity.this, ((java.lang.Boolean) obj).booleanValue());
                    return onCreate$lambda$45;
                }
            });
            return;
        }
        showLoginScreen();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$3(su.gwarden.music.model.Track t, final kotlin.jvm.functions.Function1 cb) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(t, "t");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(cb, "cb");
        su.gwarden.music.api.ApiClient.INSTANCE.replace(t.getArtist(), t.getTitle(), t.getDuration(), new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda12
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit onCreate$lambda$3$lambda$2;
                onCreate$lambda$3$lambda$2 = su.gwarden.music.MainActivity.onCreate$lambda$3$lambda$2(cb, (su.gwarden.music.model.Track) obj);
                return onCreate$lambda$3$lambda$2;
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$3$lambda$2(kotlin.jvm.functions.Function1 $cb, su.gwarden.music.model.Track sub) {
        $cb.invoke(sub);
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final java.lang.String onCreate$lambda$4(su.gwarden.music.model.Track t) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(t, "t");
        java.lang.String vkId = t.getVkId();
        java.lang.String key = vkId == null || vkId.length() == 0 ? t.getAccessKey() : "";
        su.gwarden.music.vk.VkLocal vkLocal = su.gwarden.music.vk.VkLocal.INSTANCE;
        java.lang.String vkId2 = t.getVkId();
        if (vkId2 == null) {
            vkId2 = t.getId();
        }
        return vkLocal.urlFor(vkId2, key);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$5(su.gwarden.music.MainActivity this$0, su.gwarden.music.model.Track track) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(track, "track");
        playTrack$default(this$0, track, null, false, 6, null);
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$6(su.gwarden.music.MainActivity this$0, su.gwarden.music.model.Track track) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(track, "track");
        this$0.addToMyMusic(track);
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$7(su.gwarden.music.MainActivity this$0) {
        su.gwarden.music.ui.PlayingHighlight playingHighlight = this$0.playingHighlight;
        androidx.recyclerview.widget.RecyclerView recyclerView = null;
        if (playingHighlight == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("playingHighlight");
            playingHighlight = null;
        }
        androidx.recyclerview.widget.RecyclerView recyclerView2 = this$0.recycler;
        if (recyclerView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
        } else {
            recyclerView = recyclerView2;
        }
        playingHighlight.play(recyclerView);
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$8(su.gwarden.music.MainActivity this$0, su.gwarden.music.model.Track track, android.view.View anchor) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(track, "track");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(anchor, "anchor");
        this$0.showTrackMenu(track, anchor);
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$9(su.gwarden.music.MainActivity this$0, su.gwarden.music.model.VkPlaylist pl) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(pl, "pl");
        this$0.openPlaylist(pl);
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$11(su.gwarden.music.MainActivity this$0, android.view.View it) {
        this$0.clearSimilar();
        this$0.loadWave(true, true);
        this$0.showPill("Своя волна");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final int onCreate$lambda$13(su.gwarden.music.MainActivity this$0) {
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        return playerController.getPosition();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$15(final su.gwarden.music.MainActivity this$0) {
        android.widget.ImageView imageView = this$0.waveCover;
        if (imageView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveCover");
            imageView = null;
        }
        imageView.animate().scaleX(1.035f).scaleY(1.035f).setDuration(80L).withEndAction(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda153
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.onCreate$lambda$15$lambda$14(this$0);
            }
        }).start();
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$15$lambda$14(su.gwarden.music.MainActivity this$0) {
        android.widget.ImageView imageView = this$0.waveCover;
        if (imageView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveCover");
            imageView = null;
        }
        imageView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(220L).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$29(su.gwarden.music.MainActivity this$0, android.view.View it) {
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        su.gwarden.music.player.PlayerController playerController2 = null;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        if (playerController.isPlaying()) {
            su.gwarden.music.player.PlayerController playerController3 = this$0.player;
            if (playerController3 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            } else {
                playerController2 = playerController3;
            }
            playerController2.pause();
            return;
        }
        su.gwarden.music.player.PlayerController playerController4 = this$0.player;
        if (playerController4 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
        } else {
            playerController2 = playerController4;
        }
        playerController2.play();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$30(su.gwarden.music.MainActivity this$0, android.view.View it) {
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        playerController.next(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$32(su.gwarden.music.MainActivity this$0, android.view.View it) {
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        su.gwarden.music.model.Track t = playerController.current();
        if (t != null) {
            this$0.addToMyMusic(t);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$35(su.gwarden.music.MainActivity this$0, android.widget.ImageButton $miniPlay, boolean playing) {
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        android.widget.TextView textView = null;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        su.gwarden.music.model.Track t = playerController.current();
        if (t != null) {
            su.gwarden.music.player.PlaybackService.Companion.show(this$0, t.getTitle(), t.getArtist(), su.gwarden.music.api.ApiClient.INSTANCE.img(t.getArtwork(), 600), playing);
        }
        su.gwarden.music.ui.TrackAdapter trackAdapter = this$0.adapter;
        if (trackAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter = null;
        }
        trackAdapter.setPlayingState(playing);
        $miniPlay.setImageResource(playing ? su.gwarden.music.R.drawable.ic_pause : su.gwarden.music.R.drawable.ic_play);
        android.widget.TextView textView2 = this$0.wavePlay;
        if (textView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePlay");
        } else {
            textView = textView2;
        }
        textView.setText(playing ? "Пауза" : "Слушать");
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$36(su.gwarden.music.MainActivity this$0) {
        if (this$0.onWave) {
            this$0.waveEnded = true;
            android.widget.TextView textView = this$0.hSub;
            if (textView == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                textView = null;
            }
            textView.setText("Волна · ищу ещё");
            refillWave$default(this$0, 0, 1, null);
        } else if (this$0.similarSeed != null) {
            this$0.similarEnded = true;
            refillSimilar$default(this$0, 0, 1, null);
        }
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$38(su.gwarden.music.MainActivity this$0, su.gwarden.music.model.Track track) {
        if (track != null) {
            this$0.bindTrackUi(track);
        } else {
            // трек закрыли из нижнего бара - плашку тоже убираем
            this$0.hideMiniBar();
        }
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$39(su.gwarden.music.MainActivity this$0, boolean ready) {
        this$0.updateNextButtons(ready);
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$40(su.gwarden.music.MainActivity this$0, java.lang.String msg) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(msg, "msg");
        android.widget.TextView textView = this$0.hSub;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            textView = null;
        }
        textView.setText(msg);
        android.widget.Toast.makeText(this$0, msg, 1).show();
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$41(su.gwarden.music.MainActivity this$0, int pct, java.lang.String label) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(label, "label");
        android.widget.TextView textView = null;
        if (kotlin.jvm.internal.Intrinsics.areEqual(this$0.tab, "my")) {
            android.widget.TextView textView2 = this$0.hSub;
            if (textView2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            } else {
                textView = textView2;
            }
            textView.setText(pct >= 0 ? "Волна · загружаю " + pct + "%" : "Волна · загружаю");
        } else if (!kotlin.jvm.internal.Intrinsics.areEqual(this$0.tab, "search")) {
            android.widget.TextView textView3 = this$0.hSub;
            if (textView3 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            } else {
                textView = textView3;
            }
            textView.setText(pct >= 0 ? label + " (" + pct + "%)" : label);
        }
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean onCreate$lambda$42(su.gwarden.music.MainActivity this$0, android.widget.TextView textView, int i, android.view.KeyEvent keyEvent) {
        this$0.doSearch();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onCreate$lambda$45(final su.gwarden.music.MainActivity this$0, boolean ok) {
        if (ok) {
            this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda159
                @Override // java.lang.Runnable
                public final void run() {
                    su.gwarden.music.MainActivity.onCreate$lambda$45$lambda$44(this$0);
                }
            });
        }
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$45$lambda$44(su.gwarden.music.MainActivity this$0) {
        try {
            su.gwarden.music.ui.TrackAdapter trackAdapter = this$0.adapter;
            if (trackAdapter == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter = null;
            }
            trackAdapter.notifyDataSetChanged();
        } catch (java.lang.Throwable th) {
        }
    }

    static /* synthetic */ android.widget.TextView whiteButton$default(su.gwarden.music.MainActivity mainActivity, java.lang.String str, boolean z, int i, java.lang.Object obj) {
        if ((i & 2) != 0) {
            z = true;
        }
        return mainActivity.whiteButton(str, z);
    }

    private final android.widget.TextView whiteButton(java.lang.String text, boolean dark) {
        android.widget.TextView $this$whiteButton_u24lambda_u2447 = new android.widget.TextView(this);
        $this$whiteButton_u24lambda_u2447.setText(text);
        $this$whiteButton_u24lambda_u2447.setTextSize(16.0f);
        $this$whiteButton_u24lambda_u2447.setGravity(17);
        $this$whiteButton_u24lambda_u2447.setTextColor(dark ? -15724528 : -1);
        $this$whiteButton_u24lambda_u2447.setPadding(dp(30), dp(16), dp(30), dp(16));
        android.graphics.drawable.GradientDrawable $this$whiteButton_u24lambda_u2447_u24lambda_u2446 = new android.graphics.drawable.GradientDrawable();
        $this$whiteButton_u24lambda_u2447_u24lambda_u2446.setCornerRadius(dp(30));
        $this$whiteButton_u24lambda_u2447_u24lambda_u2446.setColor(dark ? -1 : 1052688);
        if (!dark) {
            $this$whiteButton_u24lambda_u2447_u24lambda_u2446.setStroke(dp(1), -12961222);
        }
        $this$whiteButton_u24lambda_u2447.setBackground($this$whiteButton_u24lambda_u2447_u24lambda_u2446);
        $this$whiteButton_u24lambda_u2447.setClickable(true);
        return $this$whiteButton_u24lambda_u2447;
    }

    private final void showLoginScreen() {
        // новый экран входа: лого + ВК / по паролю / регистрация
        hideLoginScreen();
        su.gwarden.music.WonderAuth.INSTANCE.show(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showLoginScreen$lambda$55$lambda$54(su.gwarden.music.MainActivity this$0, android.view.View it) {
        this$0.startActivityForResult(new android.content.Intent(this$0, (java.lang.Class<?>) su.gwarden.music.vk.VkLoginActivity.class), 77);
    }

    /** Вход без VK: анонимный аккаунт, дальше - выбор интересов. */
    private final void startGuestLogin() {
        hideLoginScreen();
        showPill("Вхожу как гость...");
        su.gwarden.music.api.ApiClient.INSTANCE.guestLogin(r -> {
            if (mp.MpResult.m323isSuccessimpl(r)) {
                onLoggedIn("Вы вошли как гость");
                showGuestInterests();
            } else {
                java.lang.Throwable e = (java.lang.Throwable) mp.MpResult.m319exceptionOrNullimpl(r);
                showPill("Не получилось: " + (e == null ? "нет связи" : e.getMessage()));
                showLoginScreen();
            }
            return kotlin.Unit.INSTANCE;
        });
    }

    /**
     * Онбординг гостя: жанры + любимые артисты. Сохраняем на сервере,
     * оттуда волна и берёт семена.
     */
    private final void showGuestInterests() {
        // Новый онбординг: каталог интересов карточками с обложками
        // (группы + поиск, 70+ жанров). Старые чипсы убраны.
        su.gwarden.music.ui.InterestPicker.INSTANCE.show(
                this,
                new java.util.function.Consumer<java.lang.String>() {
                    @Override
                    public void accept(java.lang.String s) {
                        MainActivity.this.showPill(s);
                    }
                },
                new java.lang.Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.feedBlocks = new java.util.ArrayList<>();
                        MainActivity.this.loadHome();
                        MainActivity.this.setTab("home");
                    }
                });
    }

    /** Мостики для WonderAuth (Kotlin): оверлей входа/онбординга. */
    public final void authBridgeOverlay(android.widget.FrameLayout v) {
        this.loginOverlay = v;
    }

    public final void authBridgeClose() {
        hideLoginScreen();
    }

    public final void authBridgeDone(java.lang.String hello) {
        onLoggedIn(hello);
    }

    public final void authBridgeInterests() {
        showGuestInterests();
    }

    public final void authBridgeVk() {
        startActivityForResult(
            new android.content.Intent(this, (java.lang.Class<?>) su.gwarden.music.vk.VkLoginActivity.class), 77);
    }

    private final void hideLoginScreen() {
        java.lang.Runnable it = this.loginPoll;
        if (it != null) {
            new android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(it);
        }
        this.loginPoll = null;
        android.widget.FrameLayout it2 = this.loginOverlay;
        if (it2 != null) {
            android.view.ViewParent parent = it2.getParent();
            android.view.ViewGroup viewGroup = parent instanceof android.view.ViewGroup ? (android.view.ViewGroup) parent : null;
            if (viewGroup != null) {
                viewGroup.removeView(it2);
            }
        }
        this.loginOverlay = null;
    }

    /**
     * Сессия VK умерла (сервер ответил 401). Один раз пробуем тихо
     * перелогиниться через WebView (там живы куки VK), иначе - экран входа.
     */
    private final void onVkSessionLost() {
        if (this.vkFixing || this.loginOverlay != null) return;
        long now = System.currentTimeMillis();
        if (now - this.vkFixAt < 120000L) {
            showPill("VK: нужен вход");
            showLoginScreen();
            return;
        }
        this.vkFixAt = now;
        this.vkFixing = true;
        showPill("Обновляю доступ к VK...");
        try {
            startActivityForResult(new android.content.Intent(this,
                (java.lang.Class<?>) su.gwarden.music.vk.VkLoginActivity.class), 77);
        } catch (java.lang.Throwable t) {
            this.vkFixing = false;
            showLoginScreen();
        }
    }

    /** Диагностика разметки: кто где стоит и какого размера (шлём на сервер). */
    private final void dumpLayout() {
        try {
            android.view.ViewGroup content =
                (android.view.ViewGroup) findViewById(android.R.id.content);
            android.view.View root = content == null ? null : content.getChildAt(0);
            StringBuilder sb = new StringBuilder("dump ");
            if (root instanceof android.view.ViewGroup) {
                android.view.ViewGroup vg = (android.view.ViewGroup) root;
                sb.append("root=").append(vg.getClass().getSimpleName())
                    .append(" h=").append(vg.getHeight()).append(" :: ");
                for (int i = 0; i < vg.getChildCount(); i++) {
                    android.view.View c = vg.getChildAt(i);
                    sb.append(i).append(":").append(idName(c.getId()))
                        .append(" t=").append(c.getTop())
                        .append(" h=").append(c.getHeight())
                        .append(" v=").append(c.getVisibility())
                        .append(" a=").append(c.getAlpha()).append(" | ");
                }
            }
            sb.append(" || fs h=").append(this.feedScroll == null ? -1 : this.feedScroll.getHeight())
                .append(" a=").append(this.feedScroll == null ? -1f : this.feedScroll.getAlpha())
                .append(" v=").append(this.feedScroll == null ? -1 : this.feedScroll.getVisibility())
                .append(" listKids=").append(this.feedList == null ? -1 : this.feedList.getChildCount())
                .append(" listH=").append(this.feedList == null ? -1 : this.feedList.getHeight());
            su.gwarden.music.api.ApiClient.INSTANCE.log("ui", sb.toString());
        } catch (java.lang.Throwable t) {
            su.gwarden.music.api.ApiClient.INSTANCE.log("ui", "dump fail " + t);
        }
    }

    private final java.lang.String idName(int id) {
        if (id == -1) {
            return "no-id";
        }
        try {
            return getResources().getResourceEntryName(id);
        } catch (java.lang.Throwable t) {
            return "#" + id;
        }
    }

    private final void startVkLogin() {
        showPill("Готовлю QR...");
        su.gwarden.music.api.ApiClient.INSTANCE.authStart(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda22
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit startVkLogin$lambda$62;
                startVkLogin$lambda$62 = su.gwarden.music.MainActivity.startVkLogin$lambda$62(su.gwarden.music.MainActivity.this, (kotlin.Result) obj);
                return startVkLogin$lambda$62;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit startVkLogin$lambda$62(final su.gwarden.music.MainActivity this$0, final kotlin.Result res) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda82
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.startVkLogin$lambda$62$lambda$61(res, this$0);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void startVkLogin$lambda$62$lambda$61(kotlin.Result $res, su.gwarden.music.MainActivity this$0) {
        java.lang.Object value = mp.MpResult.getValue($res);
        if (mp.MpResult.m323isSuccessimpl(value)) {
            java.lang.String body = (java.lang.String) value;
            try {
                com.google.gson.JsonObject o = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
                java.lang.String id = o.get("id").getAsString();
                java.lang.String url = o.get("url").getAsString();
                this$0.getSharedPreferences("music_player", 0).edit().putString("qr_id", id).apply();
                kotlin.jvm.internal.Intrinsics.checkNotNull(id);
                kotlin.jvm.internal.Intrinsics.checkNotNull(url);
                this$0.showQrDialog(id, url);
            } catch (java.lang.Exception e) {
                this$0.showPill("VK: непонятный ответ");
            }
        }
        java.lang.Throwable it = (java.lang.Throwable) mp.MpResult.m319exceptionOrNullimpl(value);
        if (it != null) {
            this$0.showPill("VK недоступен: " + it.getMessage());
        }
    }

    private final void showQrDialog(java.lang.String id, final java.lang.String url) {
        android.app.AlertDialog alertDialog = this.qrDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        android.widget.LinearLayout box = new android.widget.LinearLayout(this);
        box.setOrientation(1);
        box.setPadding(dp(22), dp(14), dp(22), dp(6));
        android.widget.TextView hint = new android.widget.TextView(this);
        hint.setText("Открой VK и подтверди вход.\nЖдём подтверждения...");
        hint.setTextColor(-4671304);
        hint.setTextSize(13.0f);
        hint.setLineSpacing(dp(3), 1.0f);
        box.addView(hint);
        this.qrDialog = new android.app.AlertDialog.Builder(this).setTitle("Вход по QR-коду").setView(box).setPositiveButton("Открыть VK", new android.content.DialogInterface.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda44
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(android.content.DialogInterface dialogInterface, int i) {
                su.gwarden.music.MainActivity.this.openUrl(url);
            }
        }).setNegativeButton("Отмена", new android.content.DialogInterface.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda55
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(android.content.DialogInterface dialogInterface, int i) {
                su.gwarden.music.MainActivity.this.stopQr();
            }
        }).setOnDismissListener(new android.content.DialogInterface.OnDismissListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda66
            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(android.content.DialogInterface dialogInterface) {
                su.gwarden.music.MainActivity.showQrDialog$lambda$67(dialogInterface);
            }
        }).create();
        android.app.AlertDialog alertDialog2 = this.qrDialog;
        if (alertDialog2 != null) {
            alertDialog2.show();
        }
        openUrl(url);
        pollQr(id, 90, hint);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showQrDialog$lambda$67(android.content.DialogInterface it) {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void openUrl(java.lang.String url) {
        java.lang.Object m316constructorimpl;
        try {
            su.gwarden.music.MainActivity $this$openUrl_u24lambda_u2468 = this;
            $this$openUrl_u24lambda_u2468.startActivity(new android.content.Intent("android.intent.action.VIEW", android.net.Uri.parse(url)));
            m316constructorimpl = mp.MpResult.m316constructorimpl(kotlin.Unit.INSTANCE);
        } catch (java.lang.Throwable th) {
            m316constructorimpl = mp.MpResult.m316constructorimpl(kotlin.ResultKt.createFailure((java.lang.Throwable) th));
        }
        if (mp.MpResult.m319exceptionOrNullimpl(m316constructorimpl) != null) {
            copyToClipboard("VK", url);
            showPill("Открой ссылку из буфера и подтверди вход");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void stopQr() {
        getSharedPreferences("music_player", 0).edit().remove("qr_id").apply();
        java.lang.Runnable it = this.loginPoll;
        if (it != null) {
            new android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(it);
        }
        this.loginPoll = null;
        android.app.AlertDialog alertDialog = this.qrDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        this.qrDialog = null;
    }

    static /* synthetic */ void pollQr$default(su.gwarden.music.MainActivity mainActivity, java.lang.String str, int i, android.widget.TextView textView, int i2, java.lang.Object obj) {
        if ((i2 & 4) != 0) {
            textView = null;
        }
        mainActivity.pollQr(str, i, textView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void pollQr(java.lang.String id, int left, android.widget.TextView hint) {
        if (left <= 0) {
            if (hint != null) {
                hint.setText("Не подтвердилось. Попробуй «Войти через ВК» - там обычная страница VK.");
            }
            getSharedPreferences("music_player", 0).edit().remove("qr_id").apply();
        } else if (hint != null) {
            hint.setText("Вход по QR отключён - нажми «Войти через ВК»");
        }
    }

    private final java.lang.String libPrefix() {
        int id = su.gwarden.music.api.ApiClient.INSTANCE.getUserId();
        return id <= 1 ? "" : "u" + id;
    }

    static /* synthetic */ void onLoggedIn$default(su.gwarden.music.MainActivity mainActivity, java.lang.String str, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = "Готово";
        }
        mainActivity.onLoggedIn(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void onLoggedIn(java.lang.String hello) {
        hideLoginScreen();
        showPill(hello);
        // Смена аккаунта: старые списки в памяти принадлежали ПРЕДЫДУЩЕМУ
        // пользователю. Без очистки «Моя музыка» показывала кэш myTracks
        // (loadMyMusic при непустом списке вообще не ходил на сервер),
        // и после входа через Google человек видел чужие треки.
        this.myTracks = new java.util.ArrayList<>();
        this.mineIds.clear();
        this.waveQueue = kotlin.collections.CollectionsKt.emptyList();
        this.similarQueue = kotlin.collections.CollectionsKt.emptyList();
        this.similarSeed = null;
        this.onWave = false;
        this.feedBlocks = new java.util.ArrayList<>();
        try {
            if (this.adapter != null) {
                this.adapter.setMineIds(this.mineIds);
            }
        } catch (java.lang.Throwable ignored) {
        }
        su.gwarden.music.vk.VkRelay.INSTANCE.start();
        su.gwarden.music.model.LocalLibrary.INSTANCE.reset(this, libPrefix());
        su.gwarden.music.model.LocalPlaylists.INSTANCE.reset(this, libPrefix());
        setTab("home");
        prefetchMineIds();
        su.gwarden.music.vk.VkLocal.INSTANCE.ensure(this, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda76
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit onLoggedIn$lambda$73;
                onLoggedIn$lambda$73 = su.gwarden.music.MainActivity.onLoggedIn$lambda$73(su.gwarden.music.MainActivity.this, ((java.lang.Boolean) obj).booleanValue());
                return onLoggedIn$lambda$73;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit onLoggedIn$lambda$73(final su.gwarden.music.MainActivity this$0, boolean ok) {
        if (ok) {
            this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda105
                @Override // java.lang.Runnable
                public final void run() {
                    su.gwarden.music.MainActivity.onLoggedIn$lambda$73$lambda$72(this$0);
                }
            });
        }
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onLoggedIn$lambda$73$lambda$72(su.gwarden.music.MainActivity this$0) {
        try {
            su.gwarden.music.ui.TrackAdapter trackAdapter = this$0.adapter;
            if (trackAdapter == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter = null;
            }
            trackAdapter.notifyDataSetChanged();
        } catch (java.lang.Throwable th) {
        }
    }

    private final void showAccount() {
        android.widget.LinearLayout col = new android.widget.LinearLayout(this);
        col.setOrientation(1);
        int pad = dp(20);
        col.setPadding(pad, dp(8), pad, dp(4));

        final android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText("Загружаю аккаунт...");
        tv.setTextSize(14.0f);
        tv.setTextColor(0xFFCFCFCF);
        tv.setLineSpacing(dp(3), 1.0f);
        col.addView(tv);

        // VK: вход/перепривязка прямо из карточки (раньше при мёртвой сессии
        // кнопки не было вообще - только «Выйти»)
        final android.widget.TextView vkBtn = new android.widget.TextView(this);
        vkBtn.setText("Войти в VK");
        vkBtn.setTextSize(14.0f);
        vkBtn.setTextColor(0xFFEDEDED);
        vkBtn.setGravity(android.view.Gravity.CENTER);
        vkBtn.setPadding(dp(16), dp(13), dp(16), dp(13));
        android.graphics.drawable.GradientDrawable vkGd = new android.graphics.drawable.GradientDrawable();
        vkGd.setCornerRadius(dp(24));
        vkGd.setColor(0xFF1E1E1E);
        vkGd.setStroke(dp(1), 0xFF333333);
        vkBtn.setBackground(vkGd);
        vkBtn.setClickable(true);

        final android.widget.TextView scBtn = new android.widget.TextView(this);
        scBtn.setText("Подключить SoundCloud");
        scBtn.setTextSize(14.0f);
        scBtn.setTextColor(0xFF101010);
        scBtn.setGravity(android.view.Gravity.CENTER);
        scBtn.setPadding(dp(16), dp(13), dp(16), dp(13));
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setCornerRadius(dp(24));
        gd.setColor(0xFFFFFFFF);
        scBtn.setBackground(gd);
        scBtn.setClickable(true);
        android.widget.LinearLayout.LayoutParams lp =
            new android.widget.LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(16);
        col.addView(vkBtn, new android.widget.LinearLayout.LayoutParams(-1, -2) {
            {
                topMargin = dp(14);
            }
        });
        col.addView(scBtn, lp);

        final android.app.AlertDialog dlg = new android.app.AlertDialog.Builder(this)
            .setTitle("Аккаунт")
            .setView(col)
            .setPositiveButton("Закрыть", (android.content.DialogInterface.OnClickListener) null)
            .setNegativeButton("Выйти", (android.content.DialogInterface.OnClickListener) null)
            .create();
        dlg.show();
        vkBtn.setOnClickListener(new android.view.View.OnClickListener() {
            @Override public void onClick(android.view.View v) {
                dlg.dismiss();
                startActivityForResult(new android.content.Intent(
                    MainActivity.this, su.gwarden.music.vk.VkLoginActivity.class), 77);
            }
        });
        scBtn.setOnClickListener(new android.view.View.OnClickListener() {
            @Override public void onClick(android.view.View v) {
                dlg.dismiss();
                startActivityForResult(new android.content.Intent(
                    MainActivity.this, su.gwarden.music.vk.ScLoginActivity.class), 78);
            }
        });
        dlg.getButton(-2).setOnClickListener(new android.view.View.OnClickListener() {
            @Override public void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showAccount$lambda$75(dlg, MainActivity.this, view);
            }
        });
        su.gwarden.music.api.ApiClient.INSTANCE.authMeText(new kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit>() {
            @Override
            public kotlin.Unit invoke(java.lang.String body) {
                tv.setText(accountCardText(body));
                if (!vkHasToken(body)) {
                    vkBtn.setText("Войти в VK");
                } else if (vkReady(body)) {
                    vkBtn.setText("Обновить доступ к VK");
                } else {
                    vkBtn.setText("VK не отвечает — перепривязать");
                }
                scBtn.setText(scConnected(body) ? "SoundCloud подключён" : "Подключить SoundCloud");
                return kotlin.Unit.INSTANCE;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showAccount$lambda$74(su.gwarden.music.MainActivity this$0, android.content.DialogInterface dialogInterface, int i) {
        this$0.startActivityForResult(new android.content.Intent(this$0, (java.lang.Class<?>) su.gwarden.music.vk.ScLoginActivity.class), 78);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showAccount$lambda$75(android.app.AlertDialog $dlg, su.gwarden.music.MainActivity this$0, android.view.View it) {
        $dlg.dismiss();
        su.gwarden.music.api.ApiClient.INSTANCE.logout();
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        playerController.pause();
        su.gwarden.music.vk.VkRelay.INSTANCE.stop();
        // после выхода - заново слайды языка и вход
        su.gwarden.music.WonderAuth.INSTANCE.resetOnboarding(this$0);
        this$0.showLoginScreen();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit showAccount$lambda$80(su.gwarden.music.MainActivity this$0, final android.widget.TextView $tv, final kotlin.Result res) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda18
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.showAccount$lambda$80$lambda$79($tv, res);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /** Есть ли VK-токен у аккаунта (по телу /api/auth/me). */
    static boolean vkHasToken(java.lang.String body) {
        try {
            com.google.gson.JsonObject o = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
            com.google.gson.JsonObject vk = o.getAsJsonObject("vk");
            return vk != null && vk.has("has_token") && vk.get("has_token").getAsBoolean();
        } catch (java.lang.Throwable e) {
            return false;
        }
    }

    /** Отвечает ли VK прямо сейчас (по телу /api/auth/me). */
    static boolean vkReady(java.lang.String body) {
        try {
            com.google.gson.JsonObject o = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
            com.google.gson.JsonObject vk = o.getAsJsonObject("vk");
            return vk != null && vk.has("ready") && vk.get("ready").getAsBoolean();
        } catch (java.lang.Throwable e) {
            return false;
        }
    }

    /** Подключён ли SoundCloud (по телу /api/auth/me). */
    static boolean scConnected(java.lang.String body) {
        try {
            com.google.gson.JsonObject o = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
            com.google.gson.JsonObject sc = o.getAsJsonObject("sc");
            return sc != null && sc.has("connected") && sc.get("connected").getAsBoolean();
        } catch (java.lang.Throwable e) {
            return false;
        }
    }

    /** Текст карточки аккаунта из JSON /api/auth/me. */
    static java.lang.String accountCardText(java.lang.String body) {
        try {
            com.google.gson.JsonObject o = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
            com.google.gson.JsonObject u = o.getAsJsonObject("user");
            com.google.gson.JsonObject vk = o.getAsJsonObject("vk");
            com.google.gson.JsonObject t = o.getAsJsonObject("taste");
            com.google.gson.JsonObject sc = o.getAsJsonObject("sc");
            if (u == null) return "Не вошёл";
            java.lang.String name = u.has("name") && !u.get("name").isJsonNull()
                ? u.get("name").getAsString() : "";
            if (name.length() == 0) name = "Без имени";
            boolean hasTok = vk != null && vk.has("has_token") && vk.get("has_token").getAsBoolean();
            boolean vkReady = vk != null && vk.has("ready") && vk.get("ready").getAsBoolean();
            boolean scOn = sc != null && sc.has("connected") && sc.get("connected").getAsBoolean();
            int feats = t != null && t.has("features") ? t.get("features").getAsInt() : 0;
            int evs = t != null && t.has("events") ? t.get("events").getAsInt() : 0;
            return "Вошёл как " + name + "\n"
                + "VK Музыка: " + (!hasTok ? "не подключена"
                    : (vkReady ? "подключена" : "подключена, но не отвечает — войдите заново")) + "\n"
                + "SoundCloud: " + (scOn ? "подключён" : "не подключён") + "\n"
                + "Вкус: " + feats + " треков, " + evs + " событий";
        } catch (java.lang.Throwable e) {
            return "Не удалось прочитать аккаунт";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x0057, code lost:
    
        if (r7 == null) goto L20;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final void showAccount$lambda$80$lambda$79(android.widget.TextView tv, kotlin.Result res) {
        tv.setText(accountCardText(mp.MpResult.getValue(res) == null ? "" : mp.MpResult.getValue(res).toString()));
    }

    private final void tintNavIcons(java.lang.String t) {
        java.util.List order;
        java.lang.Iterable $this$forEachIndexed$iv;
        java.util.List order2 = java.util.Arrays.asList("home", "search", "my", "lib");
        final android.animation.ArgbEvaluator evaluator = new android.animation.ArgbEvaluator();
        java.util.List $this$forEachIndexed$iv2 = order2;
        int i = 0;
        for (java.lang.Object item$iv : $this$forEachIndexed$iv2) {
            int index$iv = i + 1;
            if (i < 0) {
                kotlin.collections.CollectionsKt.throwIndexOverflow();
            }
            java.lang.String k = (java.lang.String) item$iv;
            boolean on = kotlin.jvm.internal.Intrinsics.areEqual(k, t);
            final int from = this.navColors[i];
            final int to = on ? su.gwarden.music.ui.Skin.INSTANCE.onAccent() : -9737365;
            this.navColors[i] = to;
            final android.widget.ImageView iv = (android.widget.ImageView) findViewById(this.navIcons[i]);
            if (from != to) {
                order = order2;
                $this$forEachIndexed$iv = $this$forEachIndexed$iv2;
                android.animation.ValueAnimator $this$tintNavIcons_u24lambda_u2483_u24lambda_u2482 = android.animation.ValueAnimator.ofFloat(0.0f, 1.0f);
                $this$tintNavIcons_u24lambda_u2483_u24lambda_u2482.setDuration(280L);
                    $this$tintNavIcons_u24lambda_u2483_u24lambda_u2482.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda100
                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public final void onAnimationUpdate(android.animation.ValueAnimator valueAnimator) {
                        su.gwarden.music.MainActivity.tintNavIcons$lambda$83$lambda$82$lambda$81(iv, evaluator, from, to, valueAnimator);
                    }
                });
                $this$tintNavIcons_u24lambda_u2483_u24lambda_u2482.start();
            } else {
                order = order2;
                $this$forEachIndexed$iv = $this$forEachIndexed$iv2;
                iv.setColorFilter(to);
            }
            ((android.widget.TextView) findViewById(this.navLabels[i])).setTextColor(on ? su.gwarden.music.ui.Skin.INSTANCE.getAccent() : -9737365);
            i = index$iv;
            order2 = order;
            $this$forEachIndexed$iv2 = kotlin.collections.CollectionsKt.toList($this$forEachIndexed$iv);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void tintNavIcons$lambda$83$lambda$82$lambda$81(android.widget.ImageView $iv, android.animation.ArgbEvaluator $evaluator, int $from, int $to, android.animation.ValueAnimator it) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(it, "it");
        java.lang.Object animatedValue = it.getAnimatedValue();
        kotlin.jvm.internal.Intrinsics.checkNotNull(animatedValue, "null cannot be cast to non-null type kotlin.Float");
        java.lang.Object evaluate = $evaluator.evaluate(((java.lang.Float) animatedValue).floatValue(), java.lang.Integer.valueOf($from), java.lang.Integer.valueOf($to));
        kotlin.jvm.internal.Intrinsics.checkNotNull(evaluate, "null cannot be cast to non-null type kotlin.Int");
        $iv.setColorFilter(((java.lang.Integer) evaluate).intValue());
    }

    private final void moveBubble(int idx) {
        if (idx < 0) {
            return;
        }
        su.gwarden.music.ui.LiquidNav liquidNav = this.liquidNav;
        if (liquidNav == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("liquidNav");
            liquidNav = null;
        }
        liquidNav.setActive(idx, true, 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    private android.view.GestureDetector swipeDetector;
    private boolean tabSwiping = false;

    /** Свайп влево/вправо по экрану = следующая/предыдущая вкладка. */
    private final void initSwipe() {
        this.swipeDetector = new android.view.GestureDetector(this,
                new android.view.GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(android.view.MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onFling(android.view.MotionEvent e1, android.view.MotionEvent e2,
                                           float vx, float vy) {
                        if (e1 == null || e2 == null) {
                            return false;
                        }
                        float dx = e2.getX() - e1.getX();
                        float dy = e2.getY() - e1.getY();
                        // только уверенный горизонтальный свайп: вертикальные
                        // ленты и мелкие движения не должны листать вкладки
                        if (Math.abs(dx) < dp(80)) {
                            return false;
                        }
                        if (Math.abs(dx) < Math.abs(dy) * 1.7f) {
                            return false;
                        }
                        if (Math.abs(vx) < 300f) {
                            return false;
                        }
                        // палец пошёл с горизонтальной ленты (артисты, карточки,
                        // плейлисты) - это её собственный скролл, вкладки не трогаем
                        if (horizontallyScrollable(hitAt(e1.getRawX(), e1.getRawY()))) {
                            return false;
                        }
                        swipeTab(dx < 0 ? 1 : -1);
                        return true;
                    }
                });
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (this.swipeDetector != null) {
            try {
                this.swipeDetector.onTouchEvent(ev);
            } catch (java.lang.Throwable ignored) {
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /** Самое глубокое видимое view под точкой экрана (raw-координаты). */
    private final android.view.View hitAt(float rawX, float rawY) {
        android.view.View decor = getWindow() == null ? null : getWindow().getDecorView();
        if (decor == null) {
            return null;
        }
        int[] loc = new int[2];
        decor.getLocationOnScreen(loc);
        return hitTest(decor, rawX - loc[0], rawY - loc[1]);
    }

    private final android.view.View hitTest(android.view.View v, float x, float y) {
        if (v == null || v.getVisibility() != 0) {
            return null;
        }
        if (!(v instanceof android.view.ViewGroup)) {
            return v;
        }
        android.view.ViewGroup g = (android.view.ViewGroup) v;
        for (int i = g.getChildCount() - 1; i >= 0; i--) {
            android.view.View c = g.getChildAt(i);
            if (c == null || c.getVisibility() != 0) {
                continue;
            }
            float cx = x + g.getScrollX() - c.getLeft();
            float cy = y + g.getScrollY() - c.getTop();
            if (cx < 0 || cy < 0 || cx >= c.getWidth() || cy >= c.getHeight()) {
                continue;
            }
            android.view.View deeper = hitTest(c, cx, cy);
            if (deeper != null) {
                return deeper;
            }
        }
        return v;
    }

    /** Можно ли эту view (или кого-то из родителей) скроллить вбок. */
    private final boolean horizontallyScrollable(android.view.View v) {
        int guard = 0;
        while (v != null && guard++ < 60) {
            if (v.canScrollHorizontally(-1) || v.canScrollHorizontally(1)) {
                return true;
            }
            android.view.ViewParent p = v.getParent();
            v = (p instanceof android.view.View) ? (android.view.View) p : null;
        }
        return false;
    }

    /** Куда ведёт свайп (null - дальше некуда или открыт подэкран). */
    private final java.lang.String swipeTarget(int dir) {
        if (this.backTab != null) {
            return null;   // открыт подэкран (артист/настройки/плейлист)
        }
        if (this.settingsScroll != null && this.settingsScroll.getVisibility() == 0) {
            return null;
        }
        if (this.artistScroll != null && this.artistScroll.getVisibility() == 0) {
            return null;
        }
        java.util.List<java.lang.String> order =
                java.util.Arrays.asList("home", "search", "my", "lib");
        int i = order.indexOf(this.tab);
        if (i < 0) {
            return null;
        }
        int j = i + dir;
        if (j < 0 || j >= order.size()) {
            return null;
        }
        return order.get(j);
    }

    /** Видимые «страницы» вкладок - их и двигаем при переходе. */
    private final java.util.List<android.view.View> visibleTabViews() {
        java.util.List<android.view.View> out = new java.util.ArrayList<>();
        android.view.View[] all = new android.view.View[]{
                this.feedScroll, this.recycler, this.wavePanel, this.waveEq, this.libScroll};
        for (android.view.View v : all) {
            if (v != null && v.getVisibility() == 0 && v.getAlpha() > 0f) {
                out.add(v);
            }
        }
        return out;
    }

    /** Листаем вкладки свайпом с переходом: старая уезжает, новая приезжает. */
    private final void swipeTab(int dir) {
        final java.lang.String target = swipeTarget(dir);
        if (target == null || this.tabSwiping) {
            return;
        }
        final int w = getResources().getDisplayMetrics().widthPixels;
        // влево (следующая) - контент уезжает влево, новая приезжает справа
        final float shift = dir > 0 ? -w * 0.22f : w * 0.22f;
        final java.util.List<android.view.View> out = visibleTabViews();
        if (out.isEmpty()) {
            setTab(target);
            return;
        }
        this.tabSwiping = true;
        for (android.view.View v : out) {
            v.animate().translationX(shift).alpha(0.15f).setDuration(120).start();
        }
        this.uiHandler.postDelayed(new java.lang.Runnable() {
            @Override
            public void run() {
                for (android.view.View v : out) {
                    v.animate().cancel();
                    v.setTranslationX(0f);
                    v.setAlpha(1f);
                }
                setTab(target);
                for (android.view.View v : visibleTabViews()) {
                    v.setTranslationX(-shift);
                    v.setAlpha(0.2f);
                    v.animate().translationX(0f).alpha(1f).setDuration(200).start();
                }
                MainActivity.this.uiHandler.postDelayed(new java.lang.Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.tabSwiping = false;
                    }
                }, 210L);
            }
        }, 110L);
    }

    public final void setTab(java.lang.String t) {
        android.widget.TextView textView;
        su.gwarden.music.api.ApiClient.INSTANCE.log("ui", "tab=" + t);
        hideBack();
        if (this.settingsScroll != null) {
            this.settingsScroll.setVisibility(8);
        }
        if (this.artistScroll != null) {
            this.artistScroll.setVisibility(8);
        }
        this.tab = t;
        android.view.View sortView = findViewById(su.gwarden.music.R.id.sort_row);
        if (sortView != null) {
            sortView.setVisibility(8);
        }
        android.widget.LinearLayout linearLayout = this.searchRow;
        if (linearLayout == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("searchRow");
            linearLayout = null;
        }
        linearLayout.setVisibility(kotlin.jvm.internal.Intrinsics.areEqual(t, "search") ? 0 : 8);
        if (this.searchTypes != null) {
            this.searchTypes.setVisibility(kotlin.jvm.internal.Intrinsics.areEqual(t, "search") ? 0 : 8);
        }
        if (kotlin.jvm.internal.Intrinsics.areEqual(t, "search")) {
            if (this.searchData != null) {
                renderSearch();
            } else {
                // ГРАБЛЯ: при пустом запросе в списке оставался предыдущий
                // экран (волна/плейлист/библиотека) - выглядело как результаты
                // поиска, а часть строк была без названий и обложек.
                clearSearchResults();
            }
        }
        android.view.View view = this.wavePanel;
        if (view == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePanel");
            view = null;
        }
        view.setVisibility(kotlin.jvm.internal.Intrinsics.areEqual(t, "my") ? 0 : 8);
        su.gwarden.music.ui.EqBackground eqBackground = this.waveEq;
        if (eqBackground == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveEq");
            eqBackground = null;
        }
        eqBackground.setVisibility(kotlin.jvm.internal.Intrinsics.areEqual(t, "my") ? 0 : 8);
        if (kotlin.jvm.internal.Intrinsics.areEqual(t, "my")) {
            su.gwarden.music.ui.EqBackground eqBackground2 = this.waveEq;
            if (eqBackground2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveEq");
                eqBackground2 = null;
            }
            eqBackground2.setActive(true);
            android.view.View view2 = this.wavePanel;
            if (view2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePanel");
                view2 = null;
            }
            view2.post(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda101
                @Override // java.lang.Runnable
                public final void run() {
                    su.gwarden.music.MainActivity.this.syncEqOrb();
                }
            });
        } else {
            su.gwarden.music.ui.EqBackground eqBackground3 = this.waveEq;
            if (eqBackground3 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveEq");
                eqBackground3 = null;
            }
            eqBackground3.setActive(false);
        }
        androidx.recyclerview.widget.RecyclerView recyclerView = this.recycler;
        if (recyclerView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView = null;
        }
        recyclerView.setVisibility((kotlin.jvm.internal.Intrinsics.areEqual(t, "my") || kotlin.jvm.internal.Intrinsics.areEqual(t, "home") || kotlin.jvm.internal.Intrinsics.areEqual(t, "lib")) ? 8 : 0);
        if (this.libScroll != null) {
            this.libScroll.setVisibility(kotlin.jvm.internal.Intrinsics.areEqual(t, "lib") ? 0 : 8);
        }
        android.view.View view3 = this.feedScroll;
        if (view3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("feedScroll");
            view3 = null;
        }
        view3.setVisibility(kotlin.jvm.internal.Intrinsics.areEqual(t, "home") ? 0 : 8);
        // ВАЖНО: showLoading() гасит прозрачность контента до 0.12, а если ответ
        // пришёл уже на другой вкладке, вернуть её было некому - экран оставался
        // почти чёрным («пустой интерфейс»). При любом переключении сбрасываем.
        if (this.feedScroll != null) {
            this.feedScroll.setAlpha(1.0f);
        }
        if (this.wavePanel != null) {
            this.wavePanel.setAlpha(1.0f);
        }
        if (this.recycler != null) {
            this.recycler.setAlpha(1.0f);
        }
        java.util.Map chips = kotlin.collections.MapsKt.mapOf(kotlin.TuplesKt.to("home", java.lang.Integer.valueOf(su.gwarden.music.R.id.chip_home)), kotlin.TuplesKt.to("search", java.lang.Integer.valueOf(su.gwarden.music.R.id.chip_search)), kotlin.TuplesKt.to("my", java.lang.Integer.valueOf(su.gwarden.music.R.id.chip_my)), kotlin.TuplesKt.to("lib", java.lang.Integer.valueOf(su.gwarden.music.R.id.chip_lib)));
        for (java.util.Map.Entry element$iv : (java.util.Set<java.util.Map.Entry>) chips.entrySet()) {
            java.lang.String k = (java.lang.String) element$iv.getKey();
            int id = ((java.lang.Number) element$iv.getValue()).intValue();
            ((android.widget.TextView) findViewById(id)).setSelected(kotlin.jvm.internal.Intrinsics.areEqual(k, t));
            chips = chips;
        }
        moveChipBlob(true);
        moveBubble(this.iconsOrder.indexOf(this.tab));
        tintNavIcons(t);
        switch (t.hashCode()) {
            case -906336856:
                textView = null;
                if (t.equals("search")) {
                    android.widget.TextView textView2 = this.hTitle;
                    if (textView2 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hTitle");
                        textView2 = null;
                    }
                    textView2.setText("Поиск");
                    android.widget.TextView textView3 = this.hSub;
                    if (textView3 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                        textView3 = null;
                    }
                    textView3.setText("VK Музыка и SoundCloud");
                    android.widget.EditText editText = this.editQuery;
                    if (editText == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("editQuery");
                        editText = null;
                    }
                    editText.requestFocus();
                    androidx.recyclerview.widget.RecyclerView recyclerView2 = this.recycler;
                    if (recyclerView2 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
                        recyclerView2 = null;
                    }
                    su.gwarden.music.ui.TrackAdapter trackAdapter = this.adapter;
                    if (trackAdapter == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                        trackAdapter = null;
                    }
                    recyclerView2.setAdapter(trackAdapter);
                    su.gwarden.music.ui.TrackAdapter trackAdapter2 = this.adapter;
                    if (trackAdapter2 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                        trackAdapter2 = null;
                    }
                    if (trackAdapter2.getItemCount() == 0) {
                        showEmpty("Введи запрос — минимум 2 символа");
                        break;
                    }
                }
                break;
            case 3500:
                if (t.equals("my")) {
                    android.widget.TextView textView4 = this.hTitle;
                    if (textView4 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hTitle");
                        textView4 = null;
                    }
                    textView4.setText("Моя волна");
                    applyWaveMode();
                    textView = null;
                    showEmpty(null);
                    hideLoading();
                    if (this.onWave) {
                        su.gwarden.music.player.PlayerController playerController = this.player;
                        if (playerController == null) {
                            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                            playerController = null;
                        }
                        bindWave(playerController.current());
                        break;
                    } else {
                        android.widget.TextView textView5 = this.waveTitle;
                        if (textView5 == null) {
                            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveTitle");
                            textView5 = null;
                        }
                        textView5.setText("Моя волна");
                        android.widget.TextView textView6 = this.waveArtist;
                        if (textView6 == null) {
                            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveArtist");
                            textView6 = null;
                        }
                        textView6.setText("Моя волна");
                        break;
                    }
                }
                textView = null;
                break;
            case 107141:
                if (t.equals("lib")) {
                    android.widget.TextView textView7 = this.hTitle;
                    if (textView7 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hTitle");
                        textView7 = null;
                    }
                    textView7.setText("Библиотека");
                    android.widget.TextView textView8 = this.hSub;
                    if (textView8 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                        textView8 = null;
                    }
                    textView8.setText("Твоя музыка, артисты и плейлисты");
                    showEmpty(null);
                    hideLoading();
                    buildLibrary();
                    if (this.libScroll != null) {
                        this.libScroll.scrollTo(0, 0);
                    }
                    textView = null;
                    break;
                }
                textView = null;
                break;
            case 3208415:
                if (t.equals("home")) {
                    android.widget.TextView textView9 = this.hTitle;
                    if (textView9 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hTitle");
                        textView9 = null;
                    }
                    textView9.setText("Wonder");
                    android.widget.TextView textView10 = this.hSub;
                    if (textView10 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                        textView10 = null;
                    }
                    textView10.setText("Твоя музыка и подборки");
                    loadHome();
                    textView = null;
                    break;
                }
                textView = null;
                break;
            default:
                textView = null;
                break;
        }
        android.widget.TextView textView11 = this.hTitle;
        if (textView11 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hTitle");
            textView11 = textView;
        }
        textView11.animate().cancel();
        android.widget.TextView textView12 = this.hTitle;
        if (textView12 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hTitle");
            textView12 = textView;
        }
        textView12.setAlpha(0.0f);
        android.widget.TextView textView13 = this.hTitle;
        if (textView13 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hTitle");
            textView13 = textView;
        }
        textView13.animate().alpha(1.0f).setDuration(190L).start();
        fadeIn(contentView());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void syncEqOrb() {
        su.gwarden.music.ui.EqBackground eq = this.waveEq;
        if (eq == null || eq.getWidth() == 0) {
            return;
        }
        android.view.View glass = this.waveOrb;
        boolean useGlass = glass != null && glass.getWidth() != 0;
        android.view.View target = useGlass ? glass : this.waveCover;
        if (target == null || target.getWidth() == 0) {
            return;
        }
        int[] eqLoc = new int[2];
        int[] loc = new int[2];
        eq.getLocationOnScreen(eqLoc);
        target.getLocationOnScreen(loc);
        float cx = (loc[0] - eqLoc[0]) + (target.getWidth() / 2.0f);
        float cy = (loc[1] - eqLoc[1]) + (target.getHeight() / 2.0f);
        float r = java.lang.Math.min(target.getWidth(), target.getHeight()) / 2.0f;
        if (!useGlass) {
            r += dp(6);
        }
        eq.setOrb(cx, cy, r);
    }

    private final java.util.List<su.gwarden.music.model.Track> visible(java.util.List<su.gwarden.music.model.Track> list) {
        if (!this.hideBlocked) {
            return list;
        }
        java.util.List<su.gwarden.music.model.Track> $this$filter$iv = list;
        java.util.Collection destination$iv$iv = new java.util.ArrayList();
        for (java.lang.Object element$iv$iv : $this$filter$iv) {
            su.gwarden.music.model.Track it = (su.gwarden.music.model.Track) element$iv$iv;
            if (!kotlin.jvm.internal.Intrinsics.areEqual((java.lang.Object) it.getPlayable(), (java.lang.Object) false)) {
                destination$iv$iv.add(element$iv$iv);
            }
        }
        return (java.util.List) destination$iv$iv;
    }

    private final android.view.View contentView() {
        android.view.View view = this.feedScroll;
        androidx.recyclerview.widget.RecyclerView recyclerView = null;
        if (view == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("feedScroll");
            view = null;
        }
        if (view.getVisibility() == 0) {
            android.view.View view2 = this.feedScroll;
            if (view2 != null) {
                return view2;
            }
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("feedScroll");
            return null;
        }
        android.view.View view3 = this.wavePanel;
        if (view3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePanel");
            view3 = null;
        }
        if (view3.getVisibility() == 0) {
            android.view.View view4 = this.wavePanel;
            if (view4 != null) {
                return view4;
            }
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePanel");
            return null;
        }
        androidx.recyclerview.widget.RecyclerView recyclerView2 = this.recycler;
        if (recyclerView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
        } else {
            recyclerView = recyclerView2;
        }
        return recyclerView;
    }

    private final void showLoading() {
        this.loading = true;
        android.view.View c = contentView();
        androidx.recyclerview.widget.RecyclerView recyclerView = this.recycler;
        su.gwarden.music.ui.IosSpinner iosSpinner = null;
        if (recyclerView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView = null;
        }
        if (c == recyclerView) {
            su.gwarden.music.ui.PlayingHighlight playingHighlight = this.playingHighlight;
            if (playingHighlight == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("playingHighlight");
                playingHighlight = null;
            }
            playingHighlight.reset();
        }
        c.animate().cancel();
        if (c.getVisibility() == 0) {
            c.animate().alpha(0.45f).setDuration(170L).start();
        }
        android.widget.TextView textView = this.emptyText;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("emptyText");
            textView = null;
        }
        textView.animate().cancel();
        android.widget.TextView textView2 = this.emptyText;
        if (textView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("emptyText");
            textView2 = null;
        }
        textView2.setVisibility(8);
        su.gwarden.music.ui.IosSpinner iosSpinner2 = this.progress;
        if (iosSpinner2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("progress");
            iosSpinner2 = null;
        }
        iosSpinner2.animate().cancel();
        su.gwarden.music.ui.IosSpinner iosSpinner3 = this.progress;
        if (iosSpinner3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("progress");
            iosSpinner3 = null;
        }
        iosSpinner3.setVisibility(0);
        su.gwarden.music.ui.IosSpinner iosSpinner4 = this.progress;
        if (iosSpinner4 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("progress");
            iosSpinner4 = null;
        }
        iosSpinner4.setAlpha(0.0f);
        su.gwarden.music.ui.IosSpinner iosSpinner5 = this.progress;
        if (iosSpinner5 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("progress");
            iosSpinner5 = null;
        }
        iosSpinner5.setScaleX(0.86f);
        su.gwarden.music.ui.IosSpinner iosSpinner6 = this.progress;
        if (iosSpinner6 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("progress");
            iosSpinner6 = null;
        }
        iosSpinner6.setScaleY(0.86f);
        su.gwarden.music.ui.IosSpinner iosSpinner7 = this.progress;
        if (iosSpinner7 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("progress");
        } else {
            iosSpinner = iosSpinner7;
        }
        iosSpinner.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(210L).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
    }

    private final void hideLoading() {
        this.loading = false;
        su.gwarden.music.ui.IosSpinner iosSpinner = this.progress;
        su.gwarden.music.ui.IosSpinner iosSpinner2 = null;
        if (iosSpinner == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("progress");
            iosSpinner = null;
        }
        iosSpinner.animate().cancel();
        su.gwarden.music.ui.IosSpinner iosSpinner3 = this.progress;
        if (iosSpinner3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("progress");
        } else {
            iosSpinner2 = iosSpinner3;
        }
        iosSpinner2.animate().alpha(0.0f).setDuration(150L).withEndAction(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.hideLoading$lambda$87(su.gwarden.music.MainActivity.this);
            }
        }).start();
        fadeIn(contentView());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void hideLoading$lambda$87(su.gwarden.music.MainActivity this$0) {
        su.gwarden.music.ui.IosSpinner iosSpinner = this$0.progress;
        if (iosSpinner == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("progress");
            iosSpinner = null;
        }
        iosSpinner.setVisibility(8);
    }

    private final void fadeIn(android.view.View v) {
        if (this.loading) {
            return;
        }
        v.animate().cancel();
        // НИКОГДА не в 0: если главный поток занят сборкой ленты (40+ карточек),
        // кадры анимации не успевают - экран остаётся полностью прозрачным на
        // несколько секунд («пропал интерфейс, потом сам вернулся»).
        if (v.getAlpha() < 0.2f) {
            v.setAlpha(0.2f);
        }
        v.setTranslationY(dp(8));
        v.animate().alpha(1.0f).translationY(0.0f).setDuration(260L).setInterpolator(new android.view.animation.DecelerateInterpolator(1.4f)).start();
    }

    private final void attachTrackTouchHelper() {
        androidx.recyclerview.widget.ItemTouchHelper itemTouchHelper = new androidx.recyclerview.widget.ItemTouchHelper(new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(3, 0) { // from class: su.gwarden.music.MainActivity$attachTrackTouchHelper$cb$1

            @Override // androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback, androidx.recyclerview.widget.ItemTouchHelper.Callback
            public int getMovementFlags(androidx.recyclerview.widget.RecyclerView rv, androidx.recyclerview.widget.RecyclerView.ViewHolder vh) {
                su.gwarden.music.ui.TrackAdapter trackAdapter;
                su.gwarden.music.ui.TrackAdapter trackAdapter2;
                kotlin.jvm.internal.Intrinsics.checkNotNullParameter(rv, "rv");
                kotlin.jvm.internal.Intrinsics.checkNotNullParameter(vh, "vh");
                trackAdapter = su.gwarden.music.MainActivity.this.adapter;
                su.gwarden.music.ui.TrackAdapter trackAdapter3 = null;
                if (trackAdapter == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                    trackAdapter = null;
                }
                if (!trackAdapter.getEditable()) {
                    return 0;
                }
                trackAdapter2 = su.gwarden.music.MainActivity.this.adapter;
                if (trackAdapter2 == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                } else {
                    trackAdapter3 = trackAdapter2;
                }
                if (trackAdapter3.trackAt(vh.getAdapterPosition()) == null) {
                    return 0;
                }
                return androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback.makeMovementFlags(3, 0);
            }

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public boolean onMove(androidx.recyclerview.widget.RecyclerView rv, androidx.recyclerview.widget.RecyclerView.ViewHolder vh, androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
                su.gwarden.music.ui.TrackAdapter trackAdapter;
                su.gwarden.music.ui.TrackAdapter trackAdapter2;
                kotlin.jvm.internal.Intrinsics.checkNotNullParameter(rv, "rv");
                kotlin.jvm.internal.Intrinsics.checkNotNullParameter(vh, "vh");
                kotlin.jvm.internal.Intrinsics.checkNotNullParameter(target, "target");
                trackAdapter = su.gwarden.music.MainActivity.this.adapter;
                su.gwarden.music.ui.TrackAdapter trackAdapter3 = null;
                if (trackAdapter == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                    trackAdapter = null;
                }
                boolean ok = trackAdapter.moveItem(vh.getAdapterPosition(), target.getAdapterPosition());
                if (ok) {
                    su.gwarden.music.model.LocalLibrary localLibrary = su.gwarden.music.model.LocalLibrary.INSTANCE;
                    trackAdapter2 = su.gwarden.music.MainActivity.this.adapter;
                    if (trackAdapter2 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                    } else {
                        trackAdapter3 = trackAdapter2;
                    }
                    localLibrary.saveOrder(trackAdapter3.currentQueue());
                }
                return ok;
            }

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public void onSwiped(androidx.recyclerview.widget.RecyclerView.ViewHolder vh, int direction) {
                kotlin.jvm.internal.Intrinsics.checkNotNullParameter(vh, "vh");
            }

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public boolean isItemViewSwipeEnabled() {
                return false;
            }
        });
        androidx.recyclerview.widget.RecyclerView recyclerView = this.recycler;
        if (recyclerView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView = null;
        }
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /* JADX WARN: Type inference failed for: r0v8, types: [T, android.widget.PopupWindow] */
    private final void showTrackMenu(su.gwarden.music.model.Track t, android.view.View anchor) {
        kotlin.jvm.internal.Ref.ObjectRef pw;
        boolean mine = su.gwarden.music.model.LocalLibrary.INSTANCE.contains(t) || t.getMine() || this.mineIds.contains(t.getId());
        android.widget.LinearLayout $this$showTrackMenu_u24lambda_u2488 = new android.widget.LinearLayout(this);
        $this$showTrackMenu_u24lambda_u2488.setOrientation(1);
        $this$showTrackMenu_u24lambda_u2488.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_menu));
        $this$showTrackMenu_u24lambda_u2488.setPadding(dp(6), dp(6), dp(6), dp(6));
        $this$showTrackMenu_u24lambda_u2488.setElevation(dp(16));
        kotlin.jvm.internal.Ref.ObjectRef pw2 = new kotlin.jvm.internal.Ref.ObjectRef();
        if (mine) {
            pw = pw2;
            showTrackMenu$addRow(this, $this$showTrackMenu_u24lambda_u2488, pw2, t, "Удалить из моей музыки", su.gwarden.music.R.drawable.ic_trash, 1);
        } else {
            pw = pw2;
            showTrackMenu$addRow(this, $this$showTrackMenu_u24lambda_u2488, pw, t, "Добавить в мою музыку", su.gwarden.music.R.drawable.ic_plus, 2);
        }
        kotlin.jvm.internal.Ref.ObjectRef objectRef = pw;
        showTrackMenu$addRow(this, $this$showTrackMenu_u24lambda_u2488, objectRef, t, "Добавить в плейлист", su.gwarden.music.R.drawable.ic_playlist_add, 3);
        showTrackMenu$addRow(this, $this$showTrackMenu_u24lambda_u2488, objectRef, t, "Волна по треку", su.gwarden.music.R.drawable.ic_wave, 5);
        showTrackMenu$addRow(this, $this$showTrackMenu_u24lambda_u2488, objectRef, t, "Волна по артисту", su.gwarden.music.R.drawable.ic_wave, 6);
        showTrackMenu$addRow(this, $this$showTrackMenu_u24lambda_u2488, objectRef, t, "Не нравится", su.gwarden.music.R.drawable.ic_dislike, 4);
        android.widget.PopupWindow popupWindow = new android.widget.PopupWindow((android.view.View) $this$showTrackMenu_u24lambda_u2488, -2, -2, true);
        pw.element = popupWindow;
        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(dp(16));
        $this$showTrackMenu_u24lambda_u2488.measure(android.view.View.MeasureSpec.makeMeasureSpec(0, 0), android.view.View.MeasureSpec.makeMeasureSpec(0, 0));
        int w = $this$showTrackMenu_u24lambda_u2488.getMeasuredWidth();
        int hgt = $this$showTrackMenu_u24lambda_u2488.getMeasuredHeight();
        int[] loc = new int[2];
        anchor.getLocationOnScreen(loc);
        int screenH = getResources().getDisplayMetrics().heightPixels;
        boolean above = ((loc[1] + anchor.getHeight()) + hgt) + dp(20) > screenH;
        int yOff = above ? ((-anchor.getHeight()) - hgt) - dp(6) : dp(4);
        popupWindow.showAsDropDown(anchor, anchor.getWidth() - w, yOff);
        $this$showTrackMenu_u24lambda_u2488.setPivotX(w);
        $this$showTrackMenu_u24lambda_u2488.setPivotY(above ? hgt : 0.0f);
        $this$showTrackMenu_u24lambda_u2488.setAlpha(0.0f);
        $this$showTrackMenu_u24lambda_u2488.setScaleX(0.92f);
        $this$showTrackMenu_u24lambda_u2488.setScaleY(0.92f);
        $this$showTrackMenu_u24lambda_u2488.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(150L).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
    }

    private static final void showTrackMenu$addRow(final su.gwarden.music.MainActivity this$0, android.widget.LinearLayout card, final kotlin.jvm.internal.Ref.ObjectRef<android.widget.PopupWindow> objectRef, final su.gwarden.music.model.Track $t, java.lang.String label, int iconRes, final int actionId) {
        android.widget.LinearLayout row = new android.widget.LinearLayout(this$0);
        row.setOrientation(0);
        row.setGravity(16);
        row.setPadding(this$0.dp(13), this$0.dp(12), this$0.dp(20), this$0.dp(12));
        row.setClickable(true);
        row.setBackgroundResource(su.gwarden.music.R.drawable.bg_menu_item);
        android.widget.ImageView $this$showTrackMenu_u24addRow_u24lambda_u2491 = new android.widget.ImageView(this$0);
        $this$showTrackMenu_u24addRow_u24lambda_u2491.setImageResource(iconRes);
        $this$showTrackMenu_u24addRow_u24lambda_u2491.setColorFilter(-3881788);
        android.widget.LinearLayout.LayoutParams $this$showTrackMenu_u24addRow_u24lambda_u2491_u24lambda_u2490 = new android.widget.LinearLayout.LayoutParams(this$0.dp(17), this$0.dp(17));
        $this$showTrackMenu_u24addRow_u24lambda_u2491_u24lambda_u2490.setMarginEnd(this$0.dp(12));
        $this$showTrackMenu_u24addRow_u24lambda_u2491.setLayoutParams($this$showTrackMenu_u24addRow_u24lambda_u2491_u24lambda_u2490);
        row.addView($this$showTrackMenu_u24addRow_u24lambda_u2491);
        android.widget.TextView $this$showTrackMenu_u24addRow_u24lambda_u2492 = new android.widget.TextView(this$0);
        $this$showTrackMenu_u24addRow_u24lambda_u2492.setText(label);
        $this$showTrackMenu_u24addRow_u24lambda_u2492.setTextColor(-855310);
        $this$showTrackMenu_u24addRow_u24lambda_u2492.setTextSize(14.0f);
        $this$showTrackMenu_u24addRow_u24lambda_u2492.setSingleLine(true);
        row.addView($this$showTrackMenu_u24addRow_u24lambda_u2492);
        row.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda64
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showTrackMenu$addRow$lambda$93(objectRef, actionId, this$0, $t, view);
            }
        });
        card.addView(row);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public static final void showTrackMenu$addRow$lambda$93(kotlin.jvm.internal.Ref.ObjectRef $pw, int $actionId, su.gwarden.music.MainActivity this$0, su.gwarden.music.model.Track $t, android.view.View it) {
        android.widget.PopupWindow popupWindow = (android.widget.PopupWindow) $pw.element;
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        switch ($actionId) {
            case 1:
                this$0.removeFromMyMusic($t);
                return;
            case 2:
            default:
                this$0.addToMyMusic($t);
                return;
            case 3:
                this$0.showPlaylistPicker($t);
                return;
            case 5:
                this$0.startSimilarWave($t);
                return;
            case 6:
                if ("own".equals($t.getSource()) && $t.getOwner() > 0) {
                    this$0.startActivity(new android.content.Intent(this$0,
                            su.gwarden.music.ProfileActivity.class)
                            .putExtra("user_id", $t.getOwner()));
                    return;
                }
                this$0.openArtistByName($t.getArtist(), true);
                return;
            case 4:
                su.gwarden.music.player.PlayerController playerController = this$0.player;
                if (playerController == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                    playerController = null;
                }
                playerController.feedbackDislike($t);
                this$0.showPill("Понял: такого поменьше");
                return;
        }
    }

    private final void removeFromMyMusic(su.gwarden.music.model.Track t) {
        if (su.gwarden.music.model.LocalLibrary.INSTANCE.contains(t)) {
            su.gwarden.music.model.LocalLibrary.INSTANCE.remove(t);
        } else {
            su.gwarden.music.model.LocalLibrary.INSTANCE.hide(t);
        }
        this.mineIds.remove(t.getId());
        java.lang.Iterable $this$filter$iv = this.myTracks;
        java.util.Collection destination$iv$iv = new java.util.ArrayList();
        for (java.lang.Object element$iv$iv : $this$filter$iv) {
            su.gwarden.music.model.Track it = (su.gwarden.music.model.Track) element$iv$iv;
            if ((kotlin.jvm.internal.Intrinsics.areEqual(it.getId(), t.getId()) && kotlin.jvm.internal.Intrinsics.areEqual(it.getSource(), t.getSource())) ? false : true) {
                destination$iv$iv.add(element$iv$iv);
            }
        }
        this.myTracks = (java.util.List) destination$iv$iv;
        su.gwarden.music.ui.TrackAdapter trackAdapter = this.adapter;
        su.gwarden.music.ui.TrackAdapter trackAdapter2 = null;
        if (trackAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter = null;
        }
        trackAdapter.removeTrack(t);
        su.gwarden.music.ui.TrackAdapter trackAdapter3 = this.adapter;
        if (trackAdapter3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter3 = null;
        }
        trackAdapter3.setMineIds(this.mineIds);
        su.gwarden.music.model.LocalLibrary localLibrary = su.gwarden.music.model.LocalLibrary.INSTANCE;
        su.gwarden.music.ui.TrackAdapter trackAdapter4 = this.adapter;
        if (trackAdapter4 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
        } else {
            trackAdapter2 = trackAdapter4;
        }
        localLibrary.saveOrder(trackAdapter2.currentQueue());
        refreshAddButtons();
        showPill("Удалено из моей музыки");
    }

    private final boolean isMine(su.gwarden.music.model.Track t) {
        if (t == null) {
            return false;
        }
        return su.gwarden.music.model.LocalLibrary.INSTANCE.contains(t) || t.getMine() || this.mineIds.contains(t.getId());
    }

    private final void refreshAddButtons() {
        su.gwarden.music.player.PlayerController playerController = this.player;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        boolean show = !isMine(playerController.current());
        findViewById(su.gwarden.music.R.id.mini_add).setVisibility(show ? 0 : 4);
        android.view.View view = this.nowAdd;
        if (view != null) {
            view.setVisibility(show ? 0 : 4);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:12:0x0033, code lost:
    
        if (r1.getHeight() != r0.getHeight()) goto L14;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final void moveChipBlob(boolean animated) {
        final android.view.View row = findViewById(su.gwarden.music.R.id.chips_row);
        if (row == null || this.chipBlob == null) return;
        // ГРАБЛИ: LiquidBlob лежит в контейнере wrap_content с layout_height=
        // match_parent, из-за чего контейнер чипсов растягивается на весь экран,
        // контент получает высоту 0, а таб-бар уезжает за нижнюю границу.
        // Жёстко приравниваем высоту контейнера к реальной высоте ряда чипсов.
        android.view.ViewParent rowParent = row.getParent();
        if (rowParent instanceof android.view.ViewGroup && row.getHeight() > 0) {
            android.view.ViewGroup parent = (android.view.ViewGroup) rowParent;
            if (parent.getHeight() != row.getHeight()) {
                android.view.ViewGroup.LayoutParams plp = parent.getLayoutParams();
                if (plp != null) {
                    plp.height = row.getHeight();
                    parent.setLayoutParams(plp);
                }
            }
        }
        if (row.getHeight() > 0) {
            android.view.ViewGroup.LayoutParams blp = this.chipBlob.getLayoutParams();
            if (blp != null && blp.height != row.getHeight()) {
                blp.height = row.getHeight();
                this.chipBlob.setLayoutParams(blp);
            }
        }
        android.view.View chip;
        if ("search".equals(this.tab)) chip = findViewById(su.gwarden.music.R.id.chip_search);
        else if ("my".equals(this.tab)) chip = findViewById(su.gwarden.music.R.id.chip_my);
        else if ("lib".equals(this.tab)) chip = findViewById(su.gwarden.music.R.id.chip_lib);
        else chip = findViewById(su.gwarden.music.R.id.chip_home);
        if (chip == null || chip.getWidth() == 0) {
            // при первом показе разметка ещё не измерена - повторяем после layout,
            // иначе белая плашка не появляется и текст активного чипа не виден
            if (this.chipBlobTries < 8) {
                this.chipBlobTries++;
                row.postDelayed(new java.lang.Runnable() {
                    @Override
                    public void run() {
                        moveChipBlob(false);
                    }
                }, 120L);
            }
            return;
        }
        this.chipBlobTries = 0;
        int[] p1 = new int[2];
        int[] p2 = new int[2];
        chip.getLocationInWindow(p1);
        this.chipBlob.getLocationInWindow(p2);
        if (p2[0] == 0 && p2[1] == 0) return;
        android.graphics.RectF rect = new android.graphics.RectF(
            p1[0] - p2[0], p1[1] - p2[1],
            p1[0] - p2[0] + chip.getWidth(), p1[1] - p2[1] + chip.getHeight());
        this.chipBlob.setTarget(rect, dp(16), animated);
    }

    private final void syncMineIds(java.util.List<su.gwarden.music.model.Track> list) {
        this.mineIds.clear();
        java.util.List<su.gwarden.music.model.Track> $this$forEach$iv = list;
        for (java.lang.Object element$iv : $this$forEach$iv) {
            su.gwarden.music.model.Track it = (su.gwarden.music.model.Track) element$iv;
            this.mineIds.add(it.getId());
        }
        su.gwarden.music.ui.TrackAdapter trackAdapter = this.adapter;
        if (trackAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter = null;
        }
        trackAdapter.setMineIds(this.mineIds);
        refreshAddButtons();
    }

    private final void prefetchMineIds() {
        su.gwarden.music.api.ApiClient.INSTANCE.myAudio(500, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda86
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit prefetchMineIds$lambda$100;
                prefetchMineIds$lambda$100 = su.gwarden.music.MainActivity.prefetchMineIds$lambda$100(su.gwarden.music.MainActivity.this, (kotlin.Result) obj);
                return prefetchMineIds$lambda$100;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit prefetchMineIds$lambda$100(final su.gwarden.music.MainActivity this$0, final kotlin.Result res) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda68
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.prefetchMineIds$lambda$100$lambda$99(res, this$0);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void prefetchMineIds$lambda$100$lambda$99(kotlin.Result $res, su.gwarden.music.MainActivity this$0) {
        java.lang.Object value = mp.MpResult.getValue($res);
        if (mp.MpResult.m323isSuccessimpl(value)) {
            java.util.List it = (java.util.List) value;
            this$0.syncMineIds(su.gwarden.music.model.LocalLibrary.INSTANCE.merge(it));
        }
    }

    /** Режим отладки: авто-копирование логов в буфер, отчёты о зависаниях.
     *  По умолчанию ВЫКЛЮЧЕН, чтобы буфер обмена не забивался сам. */
    private final boolean debugMode() {
        try {
            return getSharedPreferences("music_player", 0)
                    .getBoolean("debug_mode", false);
        } catch (java.lang.Throwable ignored) {
            return false;
        }
    }

    private final void copyToClipboard(java.lang.String label, java.lang.String text) {
        kotlin.Unit unit;
        try {
            su.gwarden.music.MainActivity $this$copyToClipboard_u24lambda_u24101 = this;
            android.content.ClipboardManager cm = (android.content.ClipboardManager) $this$copyToClipboard_u24lambda_u24101.getSystemService(android.content.ClipboardManager.class);
            if (cm != null) {
                cm.setPrimaryClip(android.content.ClipData.newPlainText(label, text));
                unit = kotlin.Unit.INSTANCE;
            } else {
                unit = null;
            }
        } catch (java.lang.Throwable th) {
        }
    }

    private final java.lang.String buildReport(java.lang.String kind, java.lang.String extra) {
        java.lang.StringBuilder $this$buildReport_u24lambda_u24102 = new java.lang.StringBuilder();
        java.lang.StringBuilder append = $this$buildReport_u24lambda_u24102.append("=== Wonder " + APP_VER + " · " + kind + " ===");
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append, "append(...)");
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append.append('\n'), "append(...)");
        java.lang.StringBuilder append2 = $this$buildReport_u24lambda_u24102.append(su.gwarden.music.Diag.INSTANCE.device());
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append2, "append(...)");
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append2.append('\n'), "append(...)");
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue($this$buildReport_u24lambda_u24102.append('\n'), "append(...)");
        java.lang.StringBuilder append3 = $this$buildReport_u24lambda_u24102.append("--- последние события ---");
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append3, "append(...)");
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append3.append('\n'), "append(...)");
        java.lang.StringBuilder append4 = $this$buildReport_u24lambda_u24102.append(su.gwarden.music.Diag.INSTANCE.dump());
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append4, "append(...)");
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append4.append('\n'), "append(...)");
        if (extra.length() > 0) {
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue($this$buildReport_u24lambda_u24102.append('\n'), "append(...)");
            java.lang.StringBuilder append5 = $this$buildReport_u24lambda_u24102.append("--- подробности ---");
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append5, "append(...)");
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(append5.append('\n'), "append(...)");
            $this$buildReport_u24lambda_u24102.append(extra);
        }
        java.lang.String sb = $this$buildReport_u24lambda_u24102.toString();
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(sb, "toString(...)");
        return sb;
    }

    private final void installCrashLogger() {
        final java.lang.Thread.UncaughtExceptionHandler prev = java.lang.Thread.getDefaultUncaughtExceptionHandler();
        java.lang.Thread.setDefaultUncaughtExceptionHandler(new java.lang.Thread.UncaughtExceptionHandler() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda27
            @Override // java.lang.Thread.UncaughtExceptionHandler
            public final void uncaughtException(java.lang.Thread thread, java.lang.Throwable th) {
                su.gwarden.music.MainActivity.installCrashLogger$lambda$104(su.gwarden.music.MainActivity.this, prev, thread, th);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void installCrashLogger$lambda$104(su.gwarden.music.MainActivity this$0, java.lang.Thread.UncaughtExceptionHandler $prev, java.lang.Thread t, java.lang.Throwable e) {
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            java.lang.String report = this$0.buildReport("КРАШ", "поток: " + t.getName() + "\n\n" + sw);
            this$0.getSharedPreferences("music_player", 0).edit().putString("last_crash", report).apply();
            if (this$0.debugMode()) {
                this$0.copyToClipboard("Wonder crash", report);
            }
            su.gwarden.music.api.ApiClient.INSTANCE.logSync("crash", kotlin.text.StringsKt.take(report, 1800));
            java.lang.Thread.sleep(400L);
        } catch (java.lang.Throwable th) {
        }
        if ($prev != null) {
            $prev.uncaughtException(t, e);
        }
    }

    private final void installHangDetector() {
        if (!debugMode() || this.hangWatchOn) {
            return;
        }
        this.hangWatchOn = true;
        final android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
        h.post(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$installHangDetector$tick$1
            @Override // java.lang.Runnable
            public void run() {
                su.gwarden.music.MainActivity.this.mainBeat = java.lang.System.currentTimeMillis();
                h.postDelayed(this, 1000L);
            }
        });
        java.lang.Thread $this$installHangDetector_u24lambda_u24108 = new java.lang.Thread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.installHangDetector$lambda$107(su.gwarden.music.MainActivity.this);
            }
        });
        $this$installHangDetector_u24lambda_u24108.setDaemon(true);
        $this$installHangDetector_u24lambda_u24108.setName("hang-watch");
        $this$installHangDetector_u24lambda_u24108.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void installHangDetector$lambda$107(su.gwarden.music.MainActivity this$0) {
        java.lang.String str = "";
        java.lang.Object element$iv;
        java.lang.StackTraceElement[] stackTraceElementArr;
        while (true) {
            try {
                java.lang.Thread.sleep(4000L);
                if (!this$0.debugMode()) {
                    continue;   // отладка выключена - наблюдатель просто спит
                }
                long frozenMs = java.lang.System.currentTimeMillis() - this$0.mainBeat;
                if (this$0.mainBeat > 0 && frozenMs > 12000) {
                    java.lang.Iterable $this$firstOrNull$iv = java.lang.Thread.getAllStackTraces().entrySet();
                    java.util.Iterator it = $this$firstOrNull$iv.iterator();
                    while (true) {
                        str = null;
                        if (it.hasNext()) {
                            element$iv = it.next();
                            java.util.Map.Entry it2 = (java.util.Map.Entry) element$iv;
                            if (kotlin.jvm.internal.Intrinsics.areEqual(((java.lang.Thread) it2.getKey()).getName(), "main")) {
                                break;
                            }
                        } else {
                            element$iv = null;
                            break;
                        }
                    }
                    java.util.Map.Entry entry = (java.util.Map.Entry) element$iv;
                    if (entry != null && (stackTraceElementArr = (java.lang.StackTraceElement[]) entry.getValue()) != null) {
                        java.lang.StringBuilder _sb = new java.lang.StringBuilder();
                        for (java.lang.StackTraceElement _el : stackTraceElementArr) {
                            if (_sb.length() > 0) _sb.append("\n");
                            _sb.append(su.gwarden.music.MainActivity.installHangDetector$lambda$107$lambda$106(_el));
                        }
                        str = _sb.toString();
                    }
                    if (str == null) {
                        str = "";
                    }
                    java.lang.String mainStack = str;
                    java.lang.String report = this$0.buildReport("ЗАВИС", "главный поток стоял " + (frozenMs / 1000) + "с\n\n" + mainStack);
                    if (this$0.debugMode()) {
                        this$0.copyToClipboard("Wonder hang", report);
                    }
                    su.gwarden.music.api.ApiClient.INSTANCE.logSync("hang", kotlin.text.StringsKt.take(report, 1800));
                    java.lang.Thread.sleep(90000L);
                }
            } catch (java.lang.Exception e) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final java.lang.CharSequence installHangDetector$lambda$107$lambda$106(java.lang.StackTraceElement it) {
        return "  at " + it;
    }

    private final void showEmpty(java.lang.String text) {
        android.widget.TextView textView = null;
        if (text == null) {
            android.widget.TextView textView2 = this.emptyText;
            if (textView2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("emptyText");
            } else {
                textView = textView2;
            }
            textView.setVisibility(8);
            return;
        }
        android.widget.TextView textView3 = this.emptyText;
        if (textView3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("emptyText");
            textView3 = null;
        }
        textView3.setText(text);
        android.widget.TextView textView4 = this.emptyText;
        if (textView4 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("emptyText");
        } else {
            textView = textView4;
        }
        textView.setVisibility(0);
    }

    /** Тянем главную вниз - лента пересобирается по текущему вкусу. */
    private final void setupPullRefresh() {
        if (this.feedScroll == null || this.feedList == null) {
            return;
        }
        if (this.pullHint == null) {
            android.widget.FrameLayout hint = new android.widget.FrameLayout(this);
            hint.setBackgroundResource(su.gwarden.music.R.drawable.bg_glass_circle);
            hint.setAlpha(0.0f);
            su.gwarden.music.ui.IosSpinner sp = new su.gwarden.music.ui.IosSpinner(this);
            hint.addView(sp, new android.widget.FrameLayout.LayoutParams(
                    dp(22), dp(22), android.view.Gravity.CENTER));
            android.view.ViewParent parent = this.feedScroll.getParent();
            if (parent instanceof android.view.ViewGroup) {
                android.widget.FrameLayout.LayoutParams lp = new android.widget.FrameLayout.LayoutParams(
                        dp(44), dp(44),
                        android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL);
                lp.topMargin = dp(6);
                ((android.view.ViewGroup) parent).addView(hint, lp);
            }
            this.pullHint = hint;
        }
        this.feedScroll.setOnTouchListener(new android.view.View.OnTouchListener() {
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(android.view.View v, android.view.MotionEvent ev) {
                switch (ev.getActionMasked()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        if (MainActivity.this.feedScroll.getScrollY() <= 0
                                && !MainActivity.this.homeRefreshing) {
                            MainActivity.this.pullStartY = ev.getRawY();
                            MainActivity.this.pullDist = 0.0f;
                        } else {
                            MainActivity.this.pullStartY = -1.0f;
                        }
                        break;
                    case android.view.MotionEvent.ACTION_MOVE: {
                        if (MainActivity.this.pullStartY < 0.0f || MainActivity.this.homeRefreshing) {
                            break;
                        }
                        float dy = ev.getRawY() - MainActivity.this.pullStartY;
                        if (dy > 0.0f && MainActivity.this.feedScroll.getScrollY() <= 0) {
                            MainActivity.this.pullDist = java.lang.Math.min(dy * 0.45f, dp(96));
                            MainActivity.this.applyPull();
                        } else if (dy <= 0.0f && MainActivity.this.pullDist > 0.0f) {
                            MainActivity.this.pullDist = 0.0f;
                            MainActivity.this.applyPull();
                        }
                        break;
                    }
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        if (MainActivity.this.pullStartY >= 0.0f) {
                            if (MainActivity.this.pullDist > dp(54)) {
                                MainActivity.this.startHomeRefresh();
                            } else {
                                MainActivity.this.resetPull();
                            }
                        }
                        MainActivity.this.pullStartY = -1.0f;
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private final void applyPull() {
        if (this.feedList != null) {
            this.feedList.setTranslationY(this.pullDist);
        }
        if (this.pullHint != null) {
            float k = java.lang.Math.min(0.95f, this.pullDist / (float) dp(54));
            this.pullHint.setAlpha(k);
        }
    }

    private final void resetPull() {
        this.pullDist = 0.0f;
        if (this.feedList != null) {
            this.feedList.animate().translationY(0.0f).setDuration(180L).start();
        }
        if (this.pullHint != null) {
            this.pullHint.animate().alpha(0.0f).setDuration(140L).start();
        }
    }

    private final void startHomeRefresh() {
        if (this.homeRefreshing) {
            return;
        }
        this.homeRefreshing = true;
        if (this.feedList != null) {
            this.feedList.animate().translationY((float) dp(54)).setDuration(160L).start();
        }
        if (this.pullHint != null) {
            this.pullHint.animate().alpha(1.0f).setDuration(120L).start();
        }
        showPill("Обновляю по твоему вкусу");
        su.gwarden.music.ui.UpdateBanner.INSTANCE.checkSilent(this);
        this.feedBlocks = new java.util.ArrayList<>();
        this.myArtists = null;
        loadHome(true);
        if (this.feedScroll != null) {
            this.feedScroll.postDelayed(new java.lang.Runnable() {
                @Override // java.lang.Runnable
                public final void run() {
                    if (MainActivity.this.homeRefreshing) {
                        MainActivity.this.finishHomeRefresh();
                    }
                }
            }, 15000L);
        }
    }

    private final void finishHomeRefresh() {
        if (!this.homeRefreshing) {
            return;
        }
        this.homeRefreshing = false;
        this.pullDist = 0.0f;
        if (this.feedList != null) {
            this.feedList.animate().translationY(0.0f).setDuration(220L).start();
        }
        if (this.pullHint != null) {
            this.pullHint.animate().alpha(0.0f).setDuration(180L).start();
        }
    }

    private final void loadHome() {
        loadHome(false);
    }

    /**
     * «Недавно слушали»: подтягиваем историю аккаунта и обновляем блок на
     * главной. Без этого после прослушивания трек появлялся в разделе
     * только после полного pull-to-refresh.
     */
    private final void syncRecent() {
        su.gwarden.music.api.ApiClient.INSTANCE.recentTracks(
            new java.util.function.Consumer<java.util.List<su.gwarden.music.model.Track>>() {
                @Override
                public void accept(java.util.List<su.gwarden.music.model.Track> items) {
                    final java.util.List<su.gwarden.music.model.Track> list =
                        items == null ? new java.util.ArrayList<>() : items;
                    runOnUiThread(new java.lang.Runnable() {
                        @Override
                        public void run() {
                            if (feedBlocks == null || feedBlocks.isEmpty()) return;
                            java.util.List<su.gwarden.music.model.HomeBlock> blocks =
                                new java.util.ArrayList<>(feedBlocks);
                            int idx = -1;
                            for (int i = 0; i < blocks.size(); i++) {
                                if ("recent".equals(blocks.get(i).getKey())) {
                                    idx = i;
                                    break;
                                }
                            }
                            java.util.List<su.gwarden.music.model.Track> old = idx >= 0
                                ? blocks.get(idx).getTracks()
                                : java.util.Collections.emptyList();
                            boolean same = old.size() == list.size();
                            if (same) {
                                for (int i = 0; i < old.size(); i++) {
                                    if (!old.get(i).getId().equals(list.get(i).getId())) {
                                        same = false;
                                        break;
                                    }
                                }
                            }
                            if (same) return;
                            if (list.isEmpty()) {
                                if (idx >= 0) blocks.remove(idx);
                            } else {
                                su.gwarden.music.model.HomeBlock b =
                                    new su.gwarden.music.model.HomeBlock(
                                        "recent", "Недавно слушали", "tracks", list,
                                        java.util.Collections.emptyList());
                                if (idx >= 0) {
                                    blocks.set(idx, b);
                                } else {
                                    // свежий контекст - первым блоком, как и на сервере
                                    blocks.add(0, b);
                                }
                            }
                            feedBlocks = blocks;
                            if ("home".equals(tab)) buildFeed(blocks);
                        }
                    });
                }
            });
    }

    private final void loadHome(final boolean refresh) {
        if (this.myArtists == null) {
            su.gwarden.music.api.ApiClient.INSTANCE.artists((list, err) -> {
                this.myArtists = list == null
                        ? new java.util.ArrayList<>()
                        : list;
                if (!this.feedBlocks.isEmpty() && "home".equals(this.tab)) {
                    buildFeed(this.feedBlocks);
                }
                return kotlin.Unit.INSTANCE;
            });
        }
        syncRecent();
        try {
            su.gwarden.music.api.ApiClient.INSTANCE.log("home", "load cached=" + this.feedBlocks.size());
        } catch (java.lang.Throwable ignored) {
        }
        if (refresh && this.homeLoading) {
            // уже грузится - повторим чуть позже, чтобы pull не «залип»
            if (this.feedScroll != null) {
                this.feedScroll.postDelayed(new java.lang.Runnable() {
                    @Override // java.lang.Runnable
                    public final void run() {
                        if (MainActivity.this.homeRefreshing) {
                            MainActivity.this.loadHome(true);
                        }
                    }
                }, 800L);
            }
            return;
        }
        if (!refresh && !this.feedBlocks.isEmpty()) {
            buildFeed(this.feedBlocks);
            showEmpty(null);
        } else if (!this.homeLoading) {
            // один запрос за раз: повторный (переключение вкладок, ретраи)
            // перестраивал ленту дважды и гасил её прозрачностью
            this.homeLoading = true;
            showLoading();
            kotlin.jvm.functions.Function1 homeCb = new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda91
                @Override // kotlin.jvm.functions.Function1
                public final java.lang.Object invoke(java.lang.Object obj) {
                    kotlin.Unit loadHome$lambda$112;
                    loadHome$lambda$112 = su.gwarden.music.MainActivity.loadHome$lambda$112(su.gwarden.music.MainActivity.this, (kotlin.Result) obj);
                    return loadHome$lambda$112;
                }
            };
            if (refresh) {
                su.gwarden.music.api.ApiClient.INSTANCE.homeRefresh(homeCb);
            } else {
                su.gwarden.music.api.ApiClient.INSTANCE.home(homeCb);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit loadHome$lambda$112(final su.gwarden.music.MainActivity this$0, final kotlin.Result res) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda29
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.loadHome$lambda$112$lambda$111(this$0, res);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void loadHome$lambda$112$lambda$111(su.gwarden.music.MainActivity this$0, kotlin.Result $res) {
        this$0.homeLoading = false;
        this$0.hideLoading();
        this$0.finishHomeRefresh();
        java.lang.Object value = mp.MpResult.getValue($res);
        try {
            su.gwarden.music.api.ApiClient.INSTANCE.log("home", "res cls="
                + (value == null ? "null" : value.getClass().getName())
                + " n=" + (value instanceof java.util.List ? ((java.util.List) value).size() : -1));
        } catch (java.lang.Throwable ignored) {
        }
        android.widget.TextView textView = null;
        if (mp.MpResult.m323isSuccessimpl(value)) {
            java.util.List blocks = (java.util.List) value;
            this$0.feedBlocks = blocks;
            this$0.buildFeed(blocks);
            this$0.showEmpty(blocks.isEmpty() ? "Подборок нет" : null);
        }
        java.lang.Throwable e = (java.lang.Throwable) mp.MpResult.m319exceptionOrNullimpl(value);
        if (e != null) {
            android.widget.TextView textView2 = this$0.hSub;
            if (textView2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            } else {
                textView = textView2;
            }
            java.lang.String message = e.getMessage();
            if (message == null) {
                message = "Нет связи";
            }
            textView.setText(message);
            this$0.showEmpty("Не удалось загрузить подборки");
        }
    }

    private final void buildFeed(java.util.List<su.gwarden.music.model.HomeBlock> blocks) {
        android.view.View view;
        android.widget.LinearLayout linearLayout = this.feedList;
        if (linearLayout == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("feedList");
            linearLayout = null;
        }
        linearLayout.removeAllViews();
        android.widget.LinearLayout linearLayout2 = this.feedList;
        if (linearLayout2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("feedList");
            linearLayout2 = null;
        }
        linearLayout2.addView(waveHero());
        java.util.List<su.gwarden.music.model.HomeBlock> $this$forEach$iv = blocks;
        for (java.lang.Object element$iv : $this$forEach$iv) {
            final su.gwarden.music.model.HomeBlock b = (su.gwarden.music.model.HomeBlock) element$iv;
            if (kotlin.jvm.internal.Intrinsics.areEqual(b.getKey(), "mine")) {
                java.util.List mine = su.gwarden.music.model.LocalLibrary.INSTANCE.merge(b.getTracks());
                android.widget.LinearLayout feedListMine = this.feedList;
                if (feedListMine == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("feedList");
                    feedListMine = null;
                }
                if (!mine.isEmpty()) {
                    feedListMine.addView(myTracksPlaque(mine));
                }
                if (this.myArtists != null && !this.myArtists.isEmpty()) {
                    feedListMine.addView(artistsPlaque(this.myArtists));
                }
            } else {
                if (kotlin.jvm.internal.Intrinsics.areEqual(b.getKind(), "playlists") && !b.getPlaylists().isEmpty()) {
                    view = feedSection(b.getTitle(), kotlin.jvm.internal.Intrinsics.areEqual(b.getKey(), "mine"), new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda13
                        @Override // kotlin.jvm.functions.Function1
                        public final java.lang.Object invoke(java.lang.Object obj) {
                            kotlin.Unit buildFeed$lambda$119$lambda$114;
                            buildFeed$lambda$119$lambda$114 = su.gwarden.music.MainActivity.buildFeed$lambda$119$lambda$114(b, MainActivity.this, (android.widget.LinearLayout) obj);
                            return buildFeed$lambda$119$lambda$114;
                        }
                    });
                } else if (!b.getTracks().isEmpty()) {
                    view = feedSection(b.getTitle(), kotlin.jvm.internal.Intrinsics.areEqual(b.getKey(), "mine"), new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda14
                        @Override // kotlin.jvm.functions.Function1
                        public final java.lang.Object invoke(java.lang.Object obj) {
                            kotlin.Unit buildFeed$lambda$119$lambda$117;
                            buildFeed$lambda$119$lambda$117 = su.gwarden.music.MainActivity.buildFeed$lambda$119$lambda$117(b, MainActivity.this, (android.widget.LinearLayout) obj);
                            return buildFeed$lambda$119$lambda$117;
                        }
                    });
                } else {
                    view = null;
                }
                if (view != null) {
                    android.view.View it = view;
                    android.widget.LinearLayout linearLayout4 = this.feedList;
                    if (linearLayout4 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("feedList");
                        linearLayout4 = null;
                    }
                    linearLayout4.addView(it);
                }
            }
        }
        try {
            su.gwarden.music.api.ApiClient.INSTANCE.log("home",
                "built views=" + this.feedList.getChildCount());
            dumpLayout();
        } catch (java.lang.Throwable ignored) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit buildFeed$lambda$119$lambda$114(su.gwarden.music.model.HomeBlock $b, su.gwarden.music.MainActivity this$0, android.widget.LinearLayout row) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(row, "row");
        java.lang.Iterable $this$forEach$iv = $b.getPlaylists();
        for (java.lang.Object element$iv : $this$forEach$iv) {
            su.gwarden.music.model.VkPlaylist pl = (su.gwarden.music.model.VkPlaylist) element$iv;
            row.addView(this$0.playlistCard(pl));
        }
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit buildFeed$lambda$119$lambda$117(final su.gwarden.music.model.HomeBlock $b, final su.gwarden.music.MainActivity this$0, android.widget.LinearLayout row) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(row, "row");
        java.lang.Iterable $this$forEach$iv = $b.getTracks();
        for (java.lang.Object element$iv : $this$forEach$iv) {
            final su.gwarden.music.model.Track t = (su.gwarden.music.model.Track) element$iv;
            row.addView(this$0.trackCard(t, new kotlin.jvm.functions.Function0() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda17
                @Override // kotlin.jvm.functions.Function0
                public final java.lang.Object invoke() {
                    kotlin.Unit buildFeed$lambda$119$lambda$117$lambda$116$lambda$115;
                    buildFeed$lambda$119$lambda$117$lambda$116$lambda$115 = su.gwarden.music.MainActivity.buildFeed$lambda$119$lambda$117$lambda$116$lambda$115(this$0, t, $b);
                    return buildFeed$lambda$119$lambda$117$lambda$116$lambda$115;
                }
            }));
        }
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit buildFeed$lambda$119$lambda$117$lambda$116$lambda$115(su.gwarden.music.MainActivity this$0, su.gwarden.music.model.Track $t, su.gwarden.music.model.HomeBlock $b) {
        playTrack$default(this$0, $t, $b.getTracks(), false, 4, null);
        return kotlin.Unit.INSTANCE;
    }

    private final android.view.View waveHero() {
        android.widget.LinearLayout card = new android.widget.LinearLayout(this);
        card.setOrientation(0);
        card.setGravity(16);
        card.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_hero_wave));
        card.setPadding(dp(13), dp(12), dp(14), dp(12));
        android.widget.LinearLayout.LayoutParams $this$waveHero_u24lambda_u24122_u24lambda_u24120 = new android.widget.LinearLayout.LayoutParams(-1, -2);
        $this$waveHero_u24lambda_u24122_u24lambda_u24120.leftMargin = dp(16);
        $this$waveHero_u24lambda_u24122_u24lambda_u24120.rightMargin = dp(16);
        $this$waveHero_u24lambda_u24122_u24lambda_u24120.topMargin = dp(8);
        card.setLayoutParams($this$waveHero_u24lambda_u24122_u24lambda_u24120);
        card.setClickable(true);
        card.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda28
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.toggleWave();
            }
        });
        android.widget.ImageView $this$waveHero_u24lambda_u24123 = new android.widget.ImageView(this);
        $this$waveHero_u24lambda_u24123.setImageResource(su.gwarden.music.R.drawable.ic_play);
        $this$waveHero_u24lambda_u24123.setColorFilter(androidx.core.view.ViewCompat.MEASURED_STATE_MASK);
        $this$waveHero_u24lambda_u24123.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_circle_white));
        $this$waveHero_u24lambda_u24123.setTag("accent_play");
        $this$waveHero_u24lambda_u24123.setPadding(dp(16), dp(16), dp(16), dp(16));
        $this$waveHero_u24lambda_u24123.setLayoutParams(new android.widget.LinearLayout.LayoutParams(dp(48), dp(48)));
        card.addView($this$waveHero_u24lambda_u24123);
        android.widget.LinearLayout col = new android.widget.LinearLayout(this);
        col.setOrientation(1);
        col.setPadding(dp(13), 0, 0, 0);
        col.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, -2, 1.0f));
        android.widget.TextView $this$waveHero_u24lambda_u24125 = new android.widget.TextView(this);
        $this$waveHero_u24lambda_u24125.setText("Моя волна");
        $this$waveHero_u24lambda_u24125.setTextSize(17.0f);
        $this$waveHero_u24lambda_u24125.setTextColor(-1);
        $this$waveHero_u24lambda_u24125.setTypeface($this$waveHero_u24lambda_u24125.getTypeface(), 1);
        col.addView($this$waveHero_u24lambda_u24125);
        android.widget.TextView $this$waveHero_u24lambda_u24126 = new android.widget.TextView(this);
        $this$waveHero_u24lambda_u24126.setText("Моя волна");
        $this$waveHero_u24lambda_u24126.setTextSize(12.0f);
        $this$waveHero_u24lambda_u24126.setTextColor(-6645094);
        $this$waveHero_u24lambda_u24126.setPadding(0, dp(2), 0, 0);
        col.addView($this$waveHero_u24lambda_u24126);
        card.addView(col);
        android.widget.ImageView $this$waveHero_u24lambda_u24127 = new android.widget.ImageView(this);
        $this$waveHero_u24lambda_u24127.setImageResource(su.gwarden.music.R.drawable.ic_chevron);
        $this$waveHero_u24lambda_u24127.setColorFilter(-9737365);
        $this$waveHero_u24lambda_u24127.setLayoutParams(new android.widget.LinearLayout.LayoutParams(dp(18), dp(18)));
        card.addView($this$waveHero_u24lambda_u24127);
        return card;
    }

    private final android.view.View playlistHeader(final su.gwarden.music.model.VkPlaylist pl, final java.util.List<su.gwarden.music.model.Track> tracks) {
        int count = tracks.isEmpty() ? pl.getCount() : tracks.size();
        android.widget.LinearLayout $this$playlistHeader_u24lambda_u24128 = new android.widget.LinearLayout(this);
        $this$playlistHeader_u24lambda_u24128.setOrientation(1);
        $this$playlistHeader_u24lambda_u24128.setGravity(1);
        $this$playlistHeader_u24lambda_u24128.setPadding(dp(8), dp(6), dp(8), dp(16));
        $this$playlistHeader_u24lambda_u24128.setLayoutParams(new androidx.recyclerview.widget.RecyclerView.LayoutParams(-1, -2));
        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        final int size = java.lang.Math.min((int) (dm.widthPixels * 0.46f), dp(176));
        final java.lang.String seed = pl.getId() + pl.getOwnerId() + pl.getTitle();
        final android.widget.ImageView $this$playlistHeader_u24lambda_u24129 = new android.widget.ImageView(this);
        $this$playlistHeader_u24lambda_u24129.setLayoutParams(new android.widget.LinearLayout.LayoutParams(size, size));
        $this$playlistHeader_u24lambda_u24129.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_cover_big));
        $this$playlistHeader_u24lambda_u24129.setClipToOutline(true);
        $this$playlistHeader_u24lambda_u24129.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        if (pl.getArt()) {
            $this$playlistHeader_u24lambda_u24129.setImageBitmap(su.gwarden.music.ui.PlaylistArt.INSTANCE.bitmap(this, seed, pl.getTitle(), size));
        } else {
            su.gwarden.music.ui.PlaylistArt.INSTANCE.into($this$playlistHeader_u24lambda_u24129, pl.getCover(), seed, pl.getTitle(), size);
        }
        if (kotlin.jvm.internal.Intrinsics.areEqual(pl.getOwnerId(), "local")) {
            $this$playlistHeader_u24lambda_u24129.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda165
                @Override // android.view.View.OnClickListener
                public final void onClick(android.view.View view) {
                    su.gwarden.music.MainActivity.playlistHeader$lambda$131(su.gwarden.music.MainActivity.this, pl, $this$playlistHeader_u24lambda_u24129, seed, size, view);
                }
            });
        }
        $this$playlistHeader_u24lambda_u24128.addView($this$playlistHeader_u24lambda_u24129);
        android.widget.TextView $this$playlistHeader_u24lambda_u24133 = new android.widget.TextView(this);
        $this$playlistHeader_u24lambda_u24133.setText(pl.getTitle());
        $this$playlistHeader_u24lambda_u24133.setTextSize(21.0f);
        $this$playlistHeader_u24lambda_u24133.setTextColor(-1);
        $this$playlistHeader_u24lambda_u24133.setTypeface($this$playlistHeader_u24lambda_u24133.getTypeface(), 1);
        $this$playlistHeader_u24lambda_u24133.setGravity(17);
        $this$playlistHeader_u24lambda_u24133.setMaxLines(2);
        $this$playlistHeader_u24lambda_u24133.setEllipsize(android.text.TextUtils.TruncateAt.END);
        android.widget.LinearLayout.LayoutParams $this$playlistHeader_u24lambda_u24133_u24lambda_u24132 = new android.widget.LinearLayout.LayoutParams(-1, -2);
        $this$playlistHeader_u24lambda_u24133_u24lambda_u24132.topMargin = dp(16);
        $this$playlistHeader_u24lambda_u24133.setLayoutParams($this$playlistHeader_u24lambda_u24133_u24lambda_u24132);
        $this$playlistHeader_u24lambda_u24128.addView($this$playlistHeader_u24lambda_u24133);
        java.lang.String[] strArr = new java.lang.String[2];
        java.lang.String author = pl.getAuthor();
        if (author.length() == 0) {
            author = null;
        }
        strArr[0] = author;
        strArr[1] = count + " треков";
        java.lang.String sub = android.text.TextUtils.join(" · ", kotlin.collections.CollectionsKt.listOfNotNull((java.lang.Object[]) strArr));
        android.widget.TextView $this$playlistHeader_u24lambda_u24136 = new android.widget.TextView(this);
        $this$playlistHeader_u24lambda_u24136.setText(sub);
        $this$playlistHeader_u24lambda_u24136.setTextSize(12.5f);
        $this$playlistHeader_u24lambda_u24136.setTextColor(-6645094);
        $this$playlistHeader_u24lambda_u24136.setGravity(17);
        android.widget.LinearLayout.LayoutParams $this$playlistHeader_u24lambda_u24136_u24lambda_u24135 = new android.widget.LinearLayout.LayoutParams(-1, -2);
        $this$playlistHeader_u24lambda_u24136_u24lambda_u24135.topMargin = dp(6);
        $this$playlistHeader_u24lambda_u24136.setLayoutParams($this$playlistHeader_u24lambda_u24136_u24lambda_u24135);
        $this$playlistHeader_u24lambda_u24128.addView($this$playlistHeader_u24lambda_u24136);
        if (pl.getDescription().length() > 0) {
            android.widget.TextView $this$playlistHeader_u24lambda_u24138 = new android.widget.TextView(this);
            $this$playlistHeader_u24lambda_u24138.setText(pl.getDescription());
            $this$playlistHeader_u24lambda_u24138.setTextSize(12.0f);
            $this$playlistHeader_u24lambda_u24138.setTextColor(-8750470);
            $this$playlistHeader_u24lambda_u24138.setGravity(17);
            $this$playlistHeader_u24lambda_u24138.setMaxLines(3);
            $this$playlistHeader_u24lambda_u24138.setEllipsize(android.text.TextUtils.TruncateAt.END);
            android.widget.LinearLayout.LayoutParams $this$playlistHeader_u24lambda_u24138_u24lambda_u24137 = new android.widget.LinearLayout.LayoutParams(-1, -2);
            $this$playlistHeader_u24lambda_u24138_u24lambda_u24137.topMargin = dp(9);
            $this$playlistHeader_u24lambda_u24138.setLayoutParams($this$playlistHeader_u24lambda_u24138_u24lambda_u24137);
            $this$playlistHeader_u24lambda_u24128.addView($this$playlistHeader_u24lambda_u24138);
        }
        if (!tracks.isEmpty()) {
            boolean foreign = !pl.getMine() && !"local".equals(pl.getOwnerId());
            android.widget.LinearLayout btns = new android.widget.LinearLayout(this);
            btns.setOrientation(0);
            btns.setGravity(17);
            android.widget.LinearLayout.LayoutParams blp =
                new android.widget.LinearLayout.LayoutParams(-1, -2);
            blp.topMargin = dp(16);
            btns.setLayoutParams(blp);

            android.widget.TextView play = new android.widget.TextView(this);
            play.setText("Слушать");
            play.setTextSize(14.0f);
            play.setTextColor(androidx.core.view.ViewCompat.MEASURED_STATE_MASK);
            play.setTypeface(play.getTypeface(), 1);
            play.setGravity(17);
            play.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_btn));
            play.setClickable(true);
            android.widget.LinearLayout.LayoutParams plp =
                new android.widget.LinearLayout.LayoutParams(dp(foreign ? 150 : 184), dp(46));
            if (foreign) {
                plp.rightMargin = dp(10);
            }
            play.setLayoutParams(plp);
            play.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View view) {
                    su.gwarden.music.MainActivity.playlistHeader$lambda$141$lambda$140(tracks, MainActivity.this, view);
                }
            });
            btns.addView(play);

            if (foreign) {
                // чужой плейлист: можно добавить к себе в «Мою музыку»
                final android.widget.TextView add = new android.widget.TextView(this);
                add.setText("Добавить к себе");
                add.setTextSize(13.0f);
                add.setTextColor(-1);
                add.setTypeface(add.getTypeface(), 1);
                add.setGravity(17);
                add.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_row));
                add.setClickable(true);
                add.setLayoutParams(new android.widget.LinearLayout.LayoutParams(dp(150), dp(46)));
                add.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View view) {
                        add.setEnabled(false);
                        add.setText("Добавляю...");
                        su.gwarden.music.api.ApiClient.INSTANCE.followPlaylist(
                            pl.getOwnerId(), pl.getId(), pl.getAccessKey(),
                            new kotlin.jvm.functions.Function1() {
                                @Override
                                public java.lang.Object invoke(java.lang.Object obj) {
                                    final kotlin.Result res = (kotlin.Result) obj;
                                    MainActivity.this.runOnUiThread(new java.lang.Runnable() {
                                        @Override
                                        public void run() {
                                            java.lang.Object v = mp.MpResult.getValue(res);
                                            if (v instanceof kotlin.Result.Failure) {
                                                add.setEnabled(true);
                                                add.setText("Добавить к себе");
                                                MainActivity.this.showPill("Не вышло добавить плейлист");
                                            } else {
                                                add.setText("Добавлено");
                                                MainActivity.this.showPill("Плейлист добавлен в Мою музыку");
                                            }
                                        }
                                    });
                                    return kotlin.Unit.INSTANCE;
                                }
                            });
                    }
                });
                btns.addView(add);
            }
            $this$playlistHeader_u24lambda_u24128.addView(btns);
        }
        return $this$playlistHeader_u24lambda_u24128;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void playlistHeader$lambda$131(final su.gwarden.music.MainActivity this$0, final su.gwarden.music.model.VkPlaylist $pl, final android.widget.ImageView $coverView, final java.lang.String $seed, final int $size, android.view.View it) {
        this$0.pickCover(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda77
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit playlistHeader$lambda$131$lambda$130;
                playlistHeader$lambda$131$lambda$130 = su.gwarden.music.MainActivity.playlistHeader$lambda$131$lambda$130($pl, $coverView, $seed, $size, this$0, (java.lang.String) obj);
                return playlistHeader$lambda$131$lambda$130;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit playlistHeader$lambda$131$lambda$130(su.gwarden.music.model.VkPlaylist $pl, android.widget.ImageView $coverView, java.lang.String $seed, int $size, su.gwarden.music.MainActivity this$0, java.lang.String path) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(path, "path");
        su.gwarden.music.model.LocalPlaylists.INSTANCE.setCover($pl.getId(), path);
        su.gwarden.music.ui.PlaylistArt.INSTANCE.into($coverView, path, $seed, $pl.getTitle(), $size);
        this$0.showPill("Обложка обновлена");
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void playlistHeader$lambda$141$lambda$140(java.util.List $tracks, su.gwarden.music.MainActivity this$0, android.view.View it) {
        su.gwarden.music.model.Track first = (su.gwarden.music.model.Track) kotlin.collections.CollectionsKt.firstOrNull($tracks);
        if (first == null) {
            return;
        }
        playTrack$default(this$0, first, $tracks, false, 4, null);
    }

    /** «Ваши артисты»: кружки с аватарами, тап открывает карточку. */
    private final android.view.View artistsPlaque(
            java.util.List<su.gwarden.music.api.ApiClient.LibraryArtist> artists) {
        android.widget.LinearLayout wrap = new android.widget.LinearLayout(this);
        wrap.setOrientation(1);
        wrap.setPadding(dp(16), dp(18), 0, 0);

        android.widget.TextView title = new android.widget.TextView(this);
        title.setText("Ваши артисты");
        title.setTextSize(17.0f);
        title.setTextColor(-1);
        title.setTypeface(title.getTypeface(), 1);
        title.setPadding(0, 0, 0, dp(10));
        wrap.addView(title);

        android.widget.HorizontalScrollView hs = new android.widget.HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        hs.setClipToPadding(false);
        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
        row.setOrientation(0);
        row.setPadding(0, 0, dp(16), 0);

        for (int i = 0; i < artists.size(); i++) {
            final su.gwarden.music.api.ApiClient.LibraryArtist a = artists.get(i);
            android.widget.LinearLayout item = new android.widget.LinearLayout(this);
            item.setOrientation(1);
            item.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            android.widget.LinearLayout.LayoutParams ilp =
                    new android.widget.LinearLayout.LayoutParams(dp(86), -2);
            ilp.setMarginEnd(dp(10));
            item.setLayoutParams(ilp);

            android.widget.ImageView av = new android.widget.ImageView(this);
            int sz = dp(66);
            av.setLayoutParams(new android.widget.LinearLayout.LayoutParams(sz, sz));
            av.setBackgroundResource(su.gwarden.music.R.drawable.bg_avatar_dark);
            av.setClipToOutline(true);
            av.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            if (a.getCover() != null && !a.getCover().isEmpty()) {
                com.bumptech.glide.Glide.with(this)
                        .load(a.getCover()).circleCrop().into(av);
            } else {
                av.setImageResource(su.gwarden.music.R.drawable.ic_music_note);
            }
            item.addView(av);

            android.widget.TextView nm = new android.widget.TextView(this);
            nm.setText(a.getName());
            nm.setTextColor(0xFFB4B4B4);
            nm.setTextSize(11.5f);
            nm.setGravity(android.view.Gravity.CENTER);
            nm.setMaxLines(1);
            nm.setEllipsize(android.text.TextUtils.TruncateAt.END);
            nm.setPadding(0, dp(6), 0, 0);
            item.addView(nm, new android.widget.LinearLayout.LayoutParams(-1, -2));

            item.setOnClickListener(v -> openArtist(a.getSource(), a.getId(), a.getName(), a.getCover(), false));
            row.addView(item);
        }
        hs.addView(row);
        wrap.addView(hs);
        return wrap;
    }

    /** Двигаем белую пилюлю под выбранный сегмент поиска. */
    private final void moveSearchPill(int index) {
        final android.view.View pill = findViewById(su.gwarden.music.R.id.st_pill);
        final android.view.View host = this.searchTypes;
        if (pill == null || host == null) {
            return;
        }
        host.post(() -> {
            int w = (host.getWidth() - dp(8)) / 4;
            android.view.View chip0 = findViewById(su.gwarden.music.R.id.st_tracks);
            int h = chip0 != null && chip0.getHeight() > 0 ? chip0.getHeight() : dp(34);
            if (w <= 0) {
                return;
            }
            android.view.ViewGroup.LayoutParams lp = pill.getLayoutParams();
            if (lp.width != w || lp.height != h) {
                lp.width = w;
                lp.height = h;
                pill.setLayoutParams(lp);
            }
            pill.animate()
                    .translationX(index * (float) w)
                    .setDuration(220L)
                    .setInterpolator(new android.view.animation.PathInterpolator(0.2f, 1.0f, 0.3f, 1.0f))
                    .start();
        });
    }

    // ================================================================ БИБЛИОТЕКА

    private final void hideLib() {
        if (this.libScroll != null) {
            this.libScroll.setVisibility(8);
        }
    }

    /** Прячем все полноэкранные подэкраны: они прозрачные и иначе просвечивают. */
    private final void hideSubScreens() {
        if (this.artistScroll != null) {
            this.artistScroll.setVisibility(8);
        }
        if (this.settingsScroll != null) {
            this.settingsScroll.setVisibility(8);
        }
        hideLib();
    }

    static java.lang.String plural(int n, java.lang.String one, java.lang.String few, java.lang.String many) {
        int m10 = n % 10;
        int m100 = n % 100;
        if (m10 == 1 && m100 != 11) {
            return one;
        }
        if (m10 >= 2 && m10 <= 4 && (m100 < 12 || m100 > 14)) {
            return few;
        }
        return many;
    }

    /** Хаб «Библиотека»: крупные разделы вместо голого списка плейлистов. */
    private final void buildLibrary() {
        final android.widget.LinearLayout box = this.libBox;
        if (box == null) {
            return;
        }
        box.removeAllViews();

        android.widget.TextView title = new android.widget.TextView(this);
        title.setText("Библиотека");
        title.setTextColor(-1);
        title.setTextSize(28.0f);
        title.setTypeface(title.getTypeface(), 1);
        title.setPadding(dp(18), dp(16), dp(18), 0);
        box.addView(title);

        android.widget.TextView sub = new android.widget.TextView(this);
        sub.setText("Твоя музыка, артисты и плейлисты");
        sub.setTextColor(0xFF8A8A8A);
        sub.setTextSize(13.0f);
        sub.setPadding(dp(18), dp(4), dp(18), 0);
        box.addView(sub);

        android.widget.LinearLayout tiles = new android.widget.LinearLayout(this);
        tiles.setOrientation(1);
        tiles.setPadding(dp(16), dp(18), dp(16), 0);
        tiles.addView(libTile("Моя музыка", "Твои треки", su.gwarden.music.R.drawable.ic_music_note,
                "tracks", v -> showMyTracks()));
        tiles.addView(libTile("Лайки", "Сердечки из волны и шортсов", su.gwarden.music.R.drawable.ic_heart_fill,
                "likes", v -> showLikes()));
        tiles.addView(libTile("Артисты", "Кого ты слушаешь", su.gwarden.music.R.drawable.ic_artist,
                "artists", v -> showArtistsScreen()));
        tiles.addView(libTile("Плейлисты", "Свои и из сервисов", su.gwarden.music.R.drawable.ic_playlist,
                "playlists", v -> showPlaylistsScreen()));
        tiles.addView(libTile("Моя волна", "Бесконечный поток по вкусу", su.gwarden.music.R.drawable.ic_wave,
                "wave", v -> setTab("my")));
        box.addView(tiles);

        this.libArtistsBox = new android.widget.LinearLayout(this);
        this.libArtistsBox.setOrientation(1);
        box.addView(this.libArtistsBox);
        fillLibArtists();
        refreshLibraryStats();
    }

    private final android.view.View libTile(java.lang.String title, java.lang.String sub,
                                            int icon, java.lang.String tag,
                                            android.view.View.OnClickListener click) {
        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
        row.setOrientation(0);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), 0, dp(14), 0);
        row.setBackgroundResource(su.gwarden.music.R.drawable.bg_tile);
        row.setClipToOutline(true);
        android.widget.LinearLayout.LayoutParams lp =
                new android.widget.LinearLayout.LayoutParams(-1, dp(84));
        lp.bottomMargin = dp(10);
        row.setLayoutParams(lp);
        row.setClickable(true);
        // отклик по форме плитки, а не квадратом поверх скруглений
        row.setForeground(getDrawable(su.gwarden.music.R.drawable.bg_ripple_22));

        android.widget.FrameLayout ic = new android.widget.FrameLayout(this);
        ic.setBackgroundResource(su.gwarden.music.R.drawable.bg_glass_circle);
        android.widget.LinearLayout.LayoutParams ilp =
                new android.widget.LinearLayout.LayoutParams(dp(46), dp(46));
        ilp.setMarginEnd(dp(14));
        row.addView(ic, ilp);

        android.widget.ImageView im = new android.widget.ImageView(this);
        im.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                dp(21), dp(21), android.view.Gravity.CENTER));
        im.setImageResource(icon);
        im.setColorFilter(0xFFEDEDED);
        ic.addView(im);

        android.widget.LinearLayout col = new android.widget.LinearLayout(this);
        col.setOrientation(1);
        row.addView(col, new android.widget.LinearLayout.LayoutParams(0, -2, 1.0f));

        android.widget.TextView t = new android.widget.TextView(this);
        t.setText(title);
        t.setTextColor(-1);
        t.setTextSize(17.0f);
        t.setTypeface(t.getTypeface(), 1);
        col.addView(t);

        android.widget.TextView s = new android.widget.TextView(this);
        s.setText(sub);
        s.setTextColor(0xFF8A8A8A);
        s.setTextSize(12.5f);
        s.setPadding(0, dp(2), 0, 0);
        s.setTag("libsub_" + tag);
        col.addView(s);

        android.widget.ImageView ar = new android.widget.ImageView(this);
        android.widget.LinearLayout.LayoutParams alp =
                new android.widget.LinearLayout.LayoutParams(dp(16), dp(16));
        alp.setMarginStart(dp(8));
        ar.setLayoutParams(alp);
        ar.setImageResource(su.gwarden.music.R.drawable.ic_chevron);
        ar.setColorFilter(0xFF5E5E5E);
        row.addView(ar);

        row.setOnClickListener(click);
        return row;
    }

    private final void setLibSub(java.lang.String tag, java.lang.String text) {
        if (this.libBox == null) {
            return;
        }
        android.view.View v = this.libBox.findViewWithTag("libsub_" + tag);
        if (v instanceof android.widget.TextView) {
            ((android.widget.TextView) v).setText(text);
        }
    }

    private final void refreshLibraryStats() {
        long now = System.currentTimeMillis();
        try {
            su.gwarden.music.api.ApiClient.INSTANCE.libraryStats(stats -> {
                if (stats != null && this.libTracksCount <= 0) {
                    setLibSub("tracks", stats.getTracks() + " "
                            + plural(stats.getTracks(), "трек", "трека", "треков"));
                }
                return kotlin.Unit.INSTANCE;
            });
        } catch (java.lang.Throwable ignored) {
        }
        try {
            su.gwarden.music.api.ApiClient.INSTANCE.artists((list, err) -> {
                this.myArtists = list == null
                        ? new java.util.ArrayList<>()
                        : list;
                int n = this.myArtists.size();
                setLibSub("artists", n + " " + plural(n, "артист", "артиста", "артистов"));
                fillLibArtists();
                return kotlin.Unit.INSTANCE;
            });
        } catch (java.lang.Throwable ignored) {
        }

        // ТРЕКИ: считаем всю «Мою музыку» (VK + SC + импорт), а не только файлы импорта
        if (this.libTracksCount > 0 && now - this.libTracksAt < 120000L) {
            setLibSub("tracks", this.libTracksCount + " "
                    + plural(this.libTracksCount, "трек", "трека", "треков"));
        } else {
            try {
                su.gwarden.music.api.ApiClient.INSTANCE.myAudioCount(n -> {
                    if (n > 0) {
                        this.libTracksCount = n;
                        this.libTracksAt = System.currentTimeMillis();
                        setLibSub("tracks", n + " " + plural(n, "трек", "трека", "треков"));
                    }
                    return kotlin.Unit.INSTANCE;
                });
            } catch (java.lang.Throwable ignored) {
            }
        }

        // ПЛЕЙЛИСТЫ: свои (хранение в приложении) + все VK-плейлисты
        final int local = su.gwarden.music.model.LocalPlaylists.INSTANCE.all().size();
        if (this.libPlCount > 0 && now - this.libPlAt < 120000L) {
            setLibSub("playlists", this.libPlCount + " "
                    + plural(this.libPlCount, "плейлист", "плейлиста", "плейлистов"));
        } else {
            if (local > 0) {
                setLibSub("playlists", local + " " + plural(local, "плейлист", "плейлиста", "плейлистов"));
            }
            try {
                su.gwarden.music.api.ApiClient.INSTANCE.myPlaylistsCount(n -> {
                    if (n >= 0) {
                        int total = local + n;
                        this.libPlCount = total;
                        this.libPlAt = System.currentTimeMillis();
                        setLibSub("playlists", total + " "
                                + plural(total, "плейлист", "плейлиста", "плейлистов"));
                    }
                    return kotlin.Unit.INSTANCE;
                });
            } catch (java.lang.Throwable ignored) {
            }
        }
    }

    private final void fillLibArtists() {
        if (this.libArtistsBox == null) {
            return;
        }
        this.libArtistsBox.removeAllViews();
        java.util.List<su.gwarden.music.api.ApiClient.LibraryArtist> list = this.myArtists;
        if (list == null) {
            list = new java.util.ArrayList<>();
        }
        if (list.isEmpty()) {
            android.widget.LinearLayout wrap = new android.widget.LinearLayout(this);
            wrap.setOrientation(1);
            wrap.setPadding(dp(16), dp(22), dp(16), 0);
            android.widget.TextView h = new android.widget.TextView(this);
            h.setText("Ваши артисты");
            h.setTextColor(-1);
            h.setTextSize(17.0f);
            h.setTypeface(h.getTypeface(), 1);
            wrap.addView(h);
            android.widget.TextView hint = new android.widget.TextView(this);
            hint.setText("Пока никого. Открой любого артиста и нажми «Добавить в библиотеку» — он появится здесь.");
            hint.setTextColor(0xFF8A8A8A);
            hint.setTextSize(12.5f);
            hint.setPadding(0, dp(6), 0, 0);
            wrap.addView(hint);
            this.libArtistsBox.addView(wrap);
            return;
        }
        this.libArtistsBox.addView(artistsPlaque(list));
    }

    /** Полный список «Ваших артистов» (из хаба библиотеки). */
    private final void showArtistsScreen() {
        final android.widget.LinearLayout box = this.libBox;
        if (box == null || this.libScroll == null) {
            return;
        }
        this.libScroll.setVisibility(0);
        hideRecyclerAndWave();
        showBack("lib");
        if (this.hTitle != null) {
            this.hTitle.setText("Артисты");
        }
        if (this.hSub != null) {
            int n = this.myArtists == null ? 0 : this.myArtists.size();
            this.hSub.setText(n + " " + plural(n, "артист", "артиста", "артистов"));
        }
        box.removeAllViews();

        if (this.myArtists == null || this.myArtists.isEmpty()) {
            android.widget.LinearLayout wrap = new android.widget.LinearLayout(this);
            wrap.setOrientation(1);
            wrap.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            wrap.setPadding(dp(24), dp(40), dp(24), 0);
            android.widget.TextView hint = new android.widget.TextView(this);
            hint.setText("Здесь появятся артисты, которых ты добавил.\nОткрой артиста и нажми «Добавить в библиотеку».");
            hint.setTextColor(0xFF8A8A8A);
            hint.setTextSize(13.5f);
            hint.setGravity(android.view.Gravity.CENTER);
            wrap.addView(hint);
            android.widget.TextView find = new android.widget.TextView(this);
            find.setText("Найти артистов");
            find.setTextColor(-1);
            find.setTextSize(14.0f);
            find.setTypeface(find.getTypeface(), 1);
            find.setGravity(android.view.Gravity.CENTER);
            find.setBackgroundResource(su.gwarden.music.R.drawable.bg_row);
            android.widget.LinearLayout.LayoutParams flp =
                    new android.widget.LinearLayout.LayoutParams(dp(210), dp(46));
            flp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
            flp.topMargin = dp(18);
            find.setOnClickListener(v -> setTab("search"));
            wrap.addView(find, flp);
            box.addView(wrap);
            return;
        }

        for (int i = 0; i < this.myArtists.size(); i++) {
            box.addView(libArtistRow(this.myArtists.get(i)));
        }
    }

    private final android.view.View libArtistRow(final su.gwarden.music.api.ApiClient.LibraryArtist a) {
        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
        row.setOrientation(0);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(9), dp(16), dp(9));
        row.setClickable(true);
        // скруглённый отклик вместо системного прямоугольника
        row.setForeground(getDrawable(su.gwarden.music.R.drawable.bg_ripple_14));

        android.widget.ImageView av = new android.widget.ImageView(this);
        int sz = dp(48);
        android.widget.LinearLayout.LayoutParams alp =
                new android.widget.LinearLayout.LayoutParams(sz, sz);
        alp.setMarginEnd(dp(14));
        av.setLayoutParams(alp);
        av.setBackgroundResource(su.gwarden.music.R.drawable.bg_avatar_dark);
        av.setClipToOutline(true);
        av.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        if (a.getCover() != null && !a.getCover().isEmpty()) {
            com.bumptech.glide.Glide.with(this).load(a.getCover()).circleCrop().into(av);
        } else {
            av.setImageResource(su.gwarden.music.R.drawable.ic_music_note);
            av.setColorFilter(0xFF8A8A8A);
        }
        row.addView(av);

        android.widget.TextView nm = new android.widget.TextView(this);
        nm.setText(a.getName());
        nm.setTextColor(-1);
        nm.setTextSize(16.0f);
        nm.setSingleLine(true);
        nm.setEllipsize(android.text.TextUtils.TruncateAt.END);
        row.addView(nm, new android.widget.LinearLayout.LayoutParams(0, -2, 1.0f));

        android.widget.ImageView ar = new android.widget.ImageView(this);
        android.widget.LinearLayout.LayoutParams arp =
                new android.widget.LinearLayout.LayoutParams(dp(16), dp(16));
        arp.setMarginStart(dp(8));
        ar.setLayoutParams(arp);
        ar.setImageResource(su.gwarden.music.R.drawable.ic_chevron);
        ar.setColorFilter(0xFF5E5E5E);
        row.addView(ar);

        row.setOnClickListener(v -> openArtist(a.getSource(), a.getId(), a.getName(), a.getCover(), false));
        return row;
    }

    /** Плейлисты (свои + VK) в общем списке. */
    private final void showPlaylistsScreen() {
        hideLib();
        if (this.feedScroll != null) {
            this.feedScroll.setVisibility(8);
        }
        if (this.wavePanel != null) {
            this.wavePanel.setVisibility(8);
        }
        if (this.waveEq != null) {
            this.waveEq.setActive(false);
            this.waveEq.setVisibility(8);
        }
        if (this.recycler != null) {
            this.recycler.setVisibility(0);
        }
        showBack("lib");
        if (this.hTitle != null) {
            this.hTitle.setText("Плейлисты");
        }
        if (this.hSub != null) {
            this.hSub.setText("Свои и из сервисов");
        }
        loadPlaylists();
    }

    private final void hideRecyclerAndWave() {
        if (this.recycler != null) {
            this.recycler.setVisibility(8);
        }
        if (this.feedScroll != null) {
            this.feedScroll.setVisibility(8);
        }
        if (this.wavePanel != null) {
            this.wavePanel.setVisibility(8);
        }
        if (this.waveEq != null) {
            this.waveEq.setActive(false);
            this.waveEq.setVisibility(8);
        }
    }

    private final android.view.View myTracksPlaque(java.util.List<su.gwarden.music.model.Track> tracks) {
        android.widget.LinearLayout wrap = new android.widget.LinearLayout(this);
        wrap.setOrientation(1);
        wrap.setPadding(dp(16), dp(16), dp(16), 0);
        android.widget.TextView $this$myTracksPlaque_u24lambda_u24143 = new android.widget.TextView(this);
        $this$myTracksPlaque_u24lambda_u24143.setText("Мои треки");
        $this$myTracksPlaque_u24lambda_u24143.setTextSize(17.0f);
        $this$myTracksPlaque_u24lambda_u24143.setTextColor(-1);
        $this$myTracksPlaque_u24lambda_u24143.setTypeface($this$myTracksPlaque_u24lambda_u24143.getTypeface(), 1);
        $this$myTracksPlaque_u24lambda_u24143.setPadding(0, 0, 0, dp(10));
        wrap.addView($this$myTracksPlaque_u24lambda_u24143);
        android.widget.FrameLayout card = new android.widget.FrameLayout(this);
        card.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_plaque));
        card.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, dp(164)));
        androidx.recyclerview.widget.RecyclerView rv = new androidx.recyclerview.widget.RecyclerView(this);
        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this, 0, false));
        rv.setAdapter(new su.gwarden.music.MainActivity.MinePages(this, tracks));
        rv.setLayoutParams(new android.widget.FrameLayout.LayoutParams(-1, -1));
        rv.setPadding(dp(4), dp(4), dp(4), dp(4));
        rv.setClipToPadding(false);
        rv.setHorizontalScrollBarEnabled(false);
        rv.setOverScrollMode(2);
        new androidx.recyclerview.widget.PagerSnapHelper().attachToRecyclerView(rv);
        card.addView(rv);
        wrap.addView(card);
        android.widget.TextView $this$myTracksPlaque_u24lambda_u24148 = new android.widget.TextView(this);
        $this$myTracksPlaque_u24lambda_u24148.setText("Перейти в мою музыку");
        $this$myTracksPlaque_u24lambda_u24148.setTextSize(13.0f);
        $this$myTracksPlaque_u24lambda_u24148.setGravity(17);
        $this$myTracksPlaque_u24lambda_u24148.setTextColor(-1644826);
        $this$myTracksPlaque_u24lambda_u24148.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_row));
        android.widget.LinearLayout.LayoutParams $this$myTracksPlaque_u24lambda_u24148_u24lambda_u24146 = new android.widget.LinearLayout.LayoutParams(-1, dp(44));
        $this$myTracksPlaque_u24lambda_u24148_u24lambda_u24146.topMargin = dp(10);
        $this$myTracksPlaque_u24lambda_u24148.setLayoutParams($this$myTracksPlaque_u24lambda_u24148_u24lambda_u24146);
        $this$myTracksPlaque_u24lambda_u24148.setClickable(true);
        $this$myTracksPlaque_u24lambda_u24148.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda79
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.this.showMyTracks();
            }
        });
        wrap.addView($this$myTracksPlaque_u24lambda_u24148);
        return wrap;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: MainActivity.kt */
    /* loaded from: classes7.dex */
    public final class MinePages extends androidx.recyclerview.widget.RecyclerView.Adapter<su.gwarden.music.MainActivity.MinePages.VH> {
        final /* synthetic */ su.gwarden.music.MainActivity this$0;
        private final java.util.List<su.gwarden.music.model.Track> tracks;

        public MinePages(su.gwarden.music.MainActivity outer, java.util.List<su.gwarden.music.model.Track> tracks) {
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(tracks, "tracks");
            this.this$0 = outer;
            this.tracks = tracks;
        }

        /* compiled from: MainActivity.kt */
        /* loaded from: classes7.dex */
        public final class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            final /* synthetic */ su.gwarden.music.MainActivity.MinePages this$0;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            public VH(su.gwarden.music.MainActivity.MinePages outer, android.view.View v) {
                super(v);
                kotlin.jvm.internal.Intrinsics.checkNotNullParameter(v, "v");
                this.this$0 = outer;
            }
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return (this.tracks.size() + 2) / 3;
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public su.gwarden.music.MainActivity.MinePages.VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(parent, "parent");
            android.widget.LinearLayout $this$onCreateViewHolder_u24lambda_u240 = new android.widget.LinearLayout(this.this$0);
            su.gwarden.music.MainActivity mainActivity = this.this$0;
            $this$onCreateViewHolder_u24lambda_u240.setOrientation(1);
            $this$onCreateViewHolder_u24lambda_u240.setLayoutParams(new androidx.recyclerview.widget.RecyclerView.LayoutParams($this$onCreateViewHolder_u24lambda_u240.getResources().getDisplayMetrics().widthPixels - mainActivity.dp(40), -1));
            return new su.gwarden.music.MainActivity.MinePages.VH(this, $this$onCreateViewHolder_u24lambda_u240);
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public void onBindViewHolder(su.gwarden.music.MainActivity.MinePages.VH holder, int position) {
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(holder, "holder");
            android.view.View view = holder.itemView;
            kotlin.jvm.internal.Intrinsics.checkNotNull(view, "null cannot be cast to non-null type android.widget.LinearLayout");
            android.widget.LinearLayout col = (android.widget.LinearLayout) view;
            col.removeAllViews();
            int min = java.lang.Math.min((position * 3) + 3, this.tracks.size());
            for (int i = position * 3; i < min; i++) {
                col.addView(this.this$0.mineRow(this.tracks.get(i), this.tracks));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final android.view.View mineRow(final su.gwarden.music.model.Track t, final java.util.List<su.gwarden.music.model.Track> queue) {
        boolean z;
        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
        row.setOrientation(0);
        row.setGravity(16);
        row.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, dp(52)));
        row.setPadding(dp(5), 0, dp(6), 0);
        row.setClickable(true);
        row.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda149
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.playTrack$default(su.gwarden.music.MainActivity.this, t, queue, false, 4, null);
            }
        });
        android.widget.FrameLayout box = new android.widget.FrameLayout(this);
        box.setLayoutParams(new android.widget.LinearLayout.LayoutParams(dp(40), dp(40)));
        box.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_cover));
        box.setClipToOutline(true);
        android.widget.ImageView art = new android.widget.ImageView(this);
        art.setLayoutParams(new android.widget.FrameLayout.LayoutParams(-1, -1));
        art.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        box.addView(art);
        java.lang.String artwork = t.getArtwork();
        if (artwork != null && artwork.length() != 0) {
            z = false;
        } else {
            z = true;
        }
        if (!z) {
            su.gwarden.music.ui.ImageLoader.INSTANCE.load(art, su.gwarden.music.api.ApiClient.INSTANCE.img(t.getArtwork(), 600), 0, 0, null);
        } else {
            art.setImageResource(su.gwarden.music.R.drawable.ic_music_note);
        }
        android.widget.LinearLayout col = new android.widget.LinearLayout(this);
        col.setOrientation(1);
        col.setPadding(dp(11), 0, 0, 0);
        col.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, -2, 1.0f));
        android.widget.TextView $this$mineRow_u24lambda_u24154 = new android.widget.TextView(this);
        $this$mineRow_u24lambda_u24154.setText(t.getTitle());
        $this$mineRow_u24lambda_u24154.setTextSize(14.0f);
        $this$mineRow_u24lambda_u24154.setTextColor(-855310);
        $this$mineRow_u24lambda_u24154.setSingleLine(true);
        $this$mineRow_u24lambda_u24154.setEllipsize(android.text.TextUtils.TruncateAt.END);
        col.addView($this$mineRow_u24lambda_u24154);
        android.widget.TextView $this$mineRow_u24lambda_u24155 = new android.widget.TextView(this);
        $this$mineRow_u24lambda_u24155.setText(t.getArtist());
        $this$mineRow_u24lambda_u24155.setTextSize(11.5f);
        $this$mineRow_u24lambda_u24155.setTextColor(-7697782);
        $this$mineRow_u24lambda_u24155.setSingleLine(true);
        $this$mineRow_u24lambda_u24155.setEllipsize(android.text.TextUtils.TruncateAt.END);
        $this$mineRow_u24lambda_u24155.setPadding(0, dp(1), 0, 0);
        col.addView($this$mineRow_u24lambda_u24155);
        row.addView(box);
        row.addView(col);
        return row;
    }

    private final android.view.View feedSection(java.lang.String title, boolean withAll, kotlin.jvm.functions.Function1<? super android.widget.LinearLayout, kotlin.Unit> fill) {
        android.widget.LinearLayout sec = new android.widget.LinearLayout(this);
        sec.setOrientation(1);
        sec.setPadding(0, dp(16), 0, dp(2));
        android.widget.LinearLayout head = new android.widget.LinearLayout(this);
        head.setOrientation(0);
        head.setGravity(16);
        head.setPadding(dp(16), 0, dp(14), dp(10));
        android.widget.TextView $this$feedSection_u24lambda_u24158 = new android.widget.TextView(this);
        $this$feedSection_u24lambda_u24158.setText(title);
        $this$feedSection_u24lambda_u24158.setTextSize(17.0f);
        $this$feedSection_u24lambda_u24158.setTextColor(-1);
        $this$feedSection_u24lambda_u24158.setTypeface($this$feedSection_u24lambda_u24158.getTypeface(), 1);
        $this$feedSection_u24lambda_u24158.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, -2, 1.0f));
        $this$feedSection_u24lambda_u24158.setSingleLine(true);
        $this$feedSection_u24lambda_u24158.setEllipsize(android.text.TextUtils.TruncateAt.END);
        head.addView($this$feedSection_u24lambda_u24158);
        if (withAll) {
            android.widget.TextView $this$feedSection_u24lambda_u24160 = new android.widget.TextView(this);
            $this$feedSection_u24lambda_u24160.setText("Все");
            $this$feedSection_u24lambda_u24160.setTextSize(13.0f);
            $this$feedSection_u24lambda_u24160.setTextColor(-6250336);
            $this$feedSection_u24lambda_u24160.setPadding(dp(10), dp(4), 0, dp(4));
            $this$feedSection_u24lambda_u24160.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda20
                @Override // android.view.View.OnClickListener
                public final void onClick(android.view.View view) {
                    su.gwarden.music.MainActivity.this.showMyTracks();
                }
            });
            head.addView($this$feedSection_u24lambda_u24160);
        }
        android.widget.HorizontalScrollView hs = new android.widget.HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        hs.setClipToPadding(false);
        hs.setPadding(dp(16), 0, dp(16), 0);
        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
        row.setOrientation(0);
        hs.addView(row);
        fill.invoke(row);
        sec.addView(head);
        sec.addView(hs);
        return sec;
    }

    private final android.widget.FrameLayout coverBox(int size) {
        android.widget.FrameLayout $this$coverBox_u24lambda_u24163 = new android.widget.FrameLayout(this);
        $this$coverBox_u24lambda_u24163.setLayoutParams(new android.widget.LinearLayout.LayoutParams(dp(size), dp(size)));
        $this$coverBox_u24lambda_u24163.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_cover_card));
        $this$coverBox_u24lambda_u24163.setClipToOutline(true);
        return $this$coverBox_u24lambda_u24163;
    }

    private final android.view.View trackCard(su.gwarden.music.model.Track t, final kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
        android.widget.LinearLayout col = new android.widget.LinearLayout(this);
        col.setOrientation(1);
        android.widget.LinearLayout.LayoutParams $this$trackCard_u24lambda_u24166_u24lambda_u24164 = new android.widget.LinearLayout.LayoutParams(dp(132), -2);
        $this$trackCard_u24lambda_u24166_u24lambda_u24164.rightMargin = dp(12);
        col.setLayoutParams($this$trackCard_u24lambda_u24166_u24lambda_u24164);
        col.setClickable(true);
        col.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda90
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                onClick.invoke();
            }
        });
        android.widget.FrameLayout box = coverBox(132);
        android.widget.ImageView art = new android.widget.ImageView(this);
        art.setLayoutParams(new android.widget.FrameLayout.LayoutParams(-1, -1));
        art.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        box.addView(art);
        java.lang.String artwork = t.getArtwork();
        if (!(artwork == null || artwork.length() == 0)) {
            su.gwarden.music.ui.ImageLoader.INSTANCE.load(art, su.gwarden.music.api.ApiClient.INSTANCE.img(t.getArtwork(), 600), 0, 0, null);
        } else {
            art.setImageResource(su.gwarden.music.R.drawable.ic_music_note);
        }
        android.widget.TextView $this$trackCard_u24lambda_u24168 = new android.widget.TextView(this);
        $this$trackCard_u24lambda_u24168.setText(t.getTitle());
        $this$trackCard_u24lambda_u24168.setTextSize(13.0f);
        $this$trackCard_u24lambda_u24168.setTextColor(-855310);
        $this$trackCard_u24lambda_u24168.setSingleLine(true);
        $this$trackCard_u24lambda_u24168.setEllipsize(android.text.TextUtils.TruncateAt.END);
        $this$trackCard_u24lambda_u24168.setPadding(0, dp(7), 0, 0);
        android.widget.TextView $this$trackCard_u24lambda_u24169 = new android.widget.TextView(this);
        $this$trackCard_u24lambda_u24169.setText(t.getArtist());
        $this$trackCard_u24lambda_u24169.setTextSize(11.5f);
        $this$trackCard_u24lambda_u24169.setTextColor(-7697782);
        $this$trackCard_u24lambda_u24169.setSingleLine(true);
        $this$trackCard_u24lambda_u24169.setEllipsize(android.text.TextUtils.TruncateAt.END);
        $this$trackCard_u24lambda_u24169.setPadding(0, dp(1), 0, 0);
        col.addView(box);
        col.addView($this$trackCard_u24lambda_u24168);
        col.addView($this$trackCard_u24lambda_u24169);
        return col;
    }

    /* JADX WARN: Removed duplicated region for block: B:10:0x0107  */
    /* JADX WARN: Removed duplicated region for block: B:20:0x013e  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x0143  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0148  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x0140  */
    /* JADX WARN: Removed duplicated region for block: B:25:0x0109  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final android.view.View playlistCard(final su.gwarden.music.model.VkPlaylist pl) {
        android.widget.LinearLayout col = new android.widget.LinearLayout(this);
        col.setOrientation(1);
        android.widget.LinearLayout.LayoutParams lp =
            new android.widget.LinearLayout.LayoutParams(dp(148), -2);
        lp.rightMargin = dp(12);
        col.setLayoutParams(lp);
        col.setClickable(true);
        col.setOnClickListener(new android.view.View.OnClickListener() {
            @Override public void onClick(android.view.View v) { openPlaylist(pl); }
        });
        android.widget.FrameLayout box = coverBox(148);
        android.widget.ImageView art = new android.widget.ImageView(this);
        art.setLayoutParams(new android.widget.FrameLayout.LayoutParams(-1, -1));
        art.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        box.addView(art);
        java.lang.String cover = pl.getCover();
        if (!pl.getArt() && cover != null && cover.length() > 0) {
            su.gwarden.music.ui.ImageLoader.INSTANCE.load(art,
                su.gwarden.music.api.ApiClient.INSTANCE.img(cover, 600), 0, 0,
                (android.graphics.Bitmap) null);
        } else {
            art.setImageBitmap(su.gwarden.music.ui.PlaylistArt.INSTANCE.bitmap(
                this, pl.getId() + pl.getOwnerId() + pl.getTitle(), pl.getTitle(), 320));
        }
        android.widget.TextView title = new android.widget.TextView(this);
        title.setText(pl.getTitle());
        title.setTextSize(13.0f);
        title.setTextColor(0xFFF2F2F2);
        title.setMaxLines(2);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setPadding(0, dp(7), 0, 0);
        java.lang.String sub;
        java.lang.String author = pl.getAuthor();
        if (author != null && author.length() > 0 && pl.getMatch() > 0) {
            sub = pl.getMatch() + "% · " + author;
        } else if (author != null && author.length() > 0) {
            sub = author;
        } else {
            sub = "Плейлист · " + pl.getCount();
        }
        android.widget.TextView st = new android.widget.TextView(this);
        st.setText(sub);
        st.setTextSize(11.0f);
        st.setTextColor(0xFF8A8A8A);
        st.setMaxLines(1);
        st.setEllipsize(android.text.TextUtils.TruncateAt.END);
        st.setPadding(0, dp(3), 0, 0);
        col.addView(box);
        col.addView(title);
        col.addView(st);
        return col;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void showPill(java.lang.String text) {
        final android.view.ViewGroup root = (android.view.ViewGroup) findViewById(android.R.id.content);
        if (root == null) {
            return;
        }
        final android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText(text);
        tv.setTextSize(13.0f);
        tv.setTextColor(androidx.core.view.ViewCompat.MEASURED_STATE_MASK);
        tv.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_btn));
        tv.setGravity(17);
        tv.setPadding(dp(20), dp(11), dp(20), dp(11));
        android.widget.FrameLayout.LayoutParams $this$showPill_u24lambda_u24177 = new android.widget.FrameLayout.LayoutParams(-2, -2);
        $this$showPill_u24lambda_u24177.gravity = 81;
        $this$showPill_u24lambda_u24177.bottomMargin = dp(androidx.core.location.LocationRequestCompat.QUALITY_LOW_POWER);
        root.addView(tv, $this$showPill_u24lambda_u24177);
        tv.setAlpha(0.0f);
        tv.setScaleX(0.8f);
        tv.setScaleY(0.8f);
        tv.setTranslationY(dp(14));
        tv.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).translationY(0.0f).setDuration(190L).withEndAction(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda95
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.showPill$lambda$180(tv, MainActivity.this, root);
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPill$lambda$180(final android.widget.TextView $tv, final su.gwarden.music.MainActivity this$0, final android.view.ViewGroup $root) {
        $tv.animate().alpha(0.0f).translationY(-this$0.dp(18)).setStartDelay(950L).setDuration(230L).withEndAction(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda84
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.showPill$lambda$180$lambda$179(this$0, $root, $tv);
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPill$lambda$180$lambda$179(su.gwarden.music.MainActivity this$0, android.view.ViewGroup $root, android.widget.TextView $tv) {
        try {
            $root.removeView($tv);
        } catch (java.lang.Throwable th) {
        }
    }

    /** Плюсик в «Моей волне»: добавить играющий трек к себе. */
    private final void onWaveAdd() {
        su.gwarden.music.player.PlayerController p = this.player;
        if (p == null) {
            return;
        }
        su.gwarden.music.model.Track t = p.current();
        if (t == null) {
            return;
        }
        addToMyMusic(t);
        markWaveAdd(t);
    }

    /** Плюсик превращается в галочку, если трек уже в «Моей музыке». */
    private final void markWaveAdd(su.gwarden.music.model.Track t) {
        android.widget.ImageView ic =
            (android.widget.ImageView) findViewById(su.gwarden.music.R.id.wave_add_ic);
        if (ic == null) {
            return;
        }
        boolean has = t != null && su.gwarden.music.model.LocalLibrary.INSTANCE.contains(t);
        ic.setImageResource(has
            ? su.gwarden.music.R.drawable.ic_check
            : su.gwarden.music.R.drawable.ic_plus);
        ic.setAlpha(has ? 0.45f : 1.0f);
    }

    /** Сердечко: залито, если трек реально лайкнут (таблица likes на сервере). */
    private final void setLikeIcon(android.widget.ImageButton btn, boolean liked) {
        if (btn == null) {
            return;
        }
        btn.setImageResource(liked
                ? su.gwarden.music.R.drawable.ic_heart_fill
                : su.gwarden.music.R.drawable.ic_heart_outline);
        btn.setColorFilter(liked ? su.gwarden.music.ui.Skin.INSTANCE.getAccent() : -1);
        btn.setSelected(liked);
    }

    /** Перепроверить лайк при открытии плеера и при смене трека. */
    private final void bindNowLike(final android.widget.ImageButton btn,
                                   final su.gwarden.music.model.Track t) {
        if (btn == null || t == null) {
            return;
        }
        setLikeIcon(btn, false);
        su.gwarden.music.api.ApiClient.INSTANCE.likeState(t,
                new kotlin.jvm.functions.Function1<java.lang.Boolean, kotlin.Unit>() {
                    @Override
                    public kotlin.Unit invoke(java.lang.Boolean isLiked) {
                        final boolean liked = isLiked != null && isLiked;
                        MainActivity.this.runOnUiThread(new java.lang.Runnable() {
                            @Override
                            public void run() {
                                su.gwarden.music.model.Track cur = null;
                                try {
                                    cur = MainActivity.this.player.current();
                                } catch (java.lang.Throwable ignored) {
                                }
                                if (cur != null && cur.getId().equals(t.getId())) {
                                    setLikeIcon(btn, liked);
                                }
                            }
                        });
                        return kotlin.Unit.INSTANCE;
                    }
                });
    }

    /** Раздел «Лайки»: всё, что лайкнули в волне, плеере и шортсах. */
    private final void showLikes() {
        showPill("Загружаю лайки…");
        su.gwarden.music.api.ApiClient.INSTANCE.likes(
                new kotlin.jvm.functions.Function1<java.util.List<su.gwarden.music.model.Track>, kotlin.Unit>() {
                    @Override
                    public kotlin.Unit invoke(final java.util.List<su.gwarden.music.model.Track> items) {
                        MainActivity.this.runOnUiThread(new java.lang.Runnable() {
                            @Override
                            public void run() {
                                if (items == null || items.isEmpty()) {
                                    showPill("Лайков пока нет");
                                } else {
                                    showTracksList("Лайки", items);
                                }
                            }
                        });
                        return kotlin.Unit.INSTANCE;
                    }
                });
    }

    private final void addToMyMusic(su.gwarden.music.model.Track t) {
        if (su.gwarden.music.model.LocalLibrary.INSTANCE.add(t)) {
            su.gwarden.music.player.PlayerController playerController = this.player;
            su.gwarden.music.ui.TrackAdapter trackAdapter = null;
            if (playerController == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                playerController = null;
            }
            playerController.feedbackAdd(t);
            // и на сервер: иначе добавленное не влияет на волну и не видно
            // на других устройствах (у гостя вообще нет VK/SC-лайков)
            try {
                su.gwarden.music.api.ApiClient.INSTANCE.libraryAdd(t, null);
            } catch (java.lang.Throwable ignored) {
            }
            this.mineIds.add(t.getId());
            su.gwarden.music.ui.TrackAdapter trackAdapter2 = this.adapter;
            if (trackAdapter2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter2 = null;
            }
            trackAdapter2.setMineIds(this.mineIds);
            su.gwarden.music.ui.TrackAdapter trackAdapter3 = this.adapter;
            if (trackAdapter3 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            } else {
                trackAdapter = trackAdapter3;
            }
            trackAdapter.markAdded(t.getId());
            refreshAddButtons();
            this.myTracks = kotlin.collections.CollectionsKt.emptyList();
            showPill("Добавлено в мою музыку");
            return;
        }
        showPill("Уже в моей музыке");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void showMyTracks() {
        android.view.View view = this.feedScroll;
        if (view == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("feedScroll");
            view = null;
        }
        view.setVisibility(8);
        hideLib();
        androidx.recyclerview.widget.RecyclerView recyclerView = this.recycler;
        if (recyclerView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView = null;
        }
        hideSubScreens();
        recyclerView.setVisibility(0);
        showBack(kotlin.jvm.internal.Intrinsics.areEqual(this.tab, "lib") ? "lib" : "home");
        android.widget.TextView textView = this.hTitle;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hTitle");
            textView = null;
        }
        textView.setText("Мои треки");
        if (this.sortRow == null) {
            this.sortRow = findViewById(su.gwarden.music.R.id.sort_row);
        }
        if (this.sortRow != null) {
            this.sortRow.setVisibility(0);
        }
        buildSortRow();
        // Если в закэшированном списке нет ни одного трека SoundCloud (в прошлый
        // раз сервер не успел их отдать), сбрасываем кэш и грузим заново.
        boolean hasSc = false;
        for (int i = 0; i < this.myTracks.size(); i++) {
            su.gwarden.music.model.Track t = (su.gwarden.music.model.Track) this.myTracks.get(i);
            if ("soundcloud".equals(t.getSource())) {
                hasSc = true;
                break;
            }
        }
        if (!hasSc && !this.myTracks.isEmpty()) {
            this.myTracks = new java.util.ArrayList();
        }
        loadMyMusic$default(this, null, 1, null);
    }

    private final java.lang.String sortLabel() {
        if ("title".equals(this.mineSort)) {
            return "По названию";
        }
        if ("old".equals(this.mineSort)) {
            return "Старые";
        }
        return "Новые";
    }

    private final void buildSortRow() {
        if (this.sortBtn == null) {
            this.sortBtn = (android.widget.ImageView) findViewById(su.gwarden.music.R.id.sort_btn);
        }
        if (this.sortBtn == null) {
            return;
        }
        this.sortBtn.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                showSortSheet();
            }
        });
        // чип SoundCloud: один тап - и в списке только лайки SoundCloud
        if (this.scChip != null) {
            int scN = 0;
            for (int i = 0; i < this.myTracks.size(); i++) {
                su.gwarden.music.model.Track t = (su.gwarden.music.model.Track) this.myTracks.get(i);
                if (t.getScLike()) {
                    scN++;
                }
            }
            if (scN > 0) {
                this.scChip.setVisibility(0);
                this.scChip.setText("SoundCloud · " + scN);
                this.scChip.setSelected(this.onlySc);
                this.scChip.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View v) {
                        onlySc = !onlySc;
                        buildSortRow();
                        refreshMineList();
                    }
                });
            } else {
                this.scChip.setVisibility(8);
            }
        }
    }

    /** Фильтр «только SoundCloud» из чипа над списком. */
    private final java.util.List applyOnlySc(java.util.List in) {
        if (!this.onlySc) {
            return in;
        }
        java.util.List out = new java.util.ArrayList();
        for (int i = 0; i < in.size(); i++) {
            su.gwarden.music.model.Track t = (su.gwarden.music.model.Track) in.get(i);
            if (t.getScLike()) {
                out.add(t);
            }
        }
        return out;
    }

    /** Красивая шторка сортировки снизу (тёмная, с галочками). */
    private final void showSortSheet() {
        final android.app.Dialog d = new android.app.Dialog(
            this, su.gwarden.music.R.style.Theme_Wonder_Sheet);
        d.setCanceledOnTouchOutside(true);
        android.view.Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
            w.setGravity(android.view.Gravity.BOTTOM);
            w.setLayout(-1, -1);
            w.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            android.view.WindowManager.LayoutParams lp = w.getAttributes();
            lp.dimAmount = 0.62f;
            w.setAttributes(lp);
        }

        android.widget.LinearLayout col = new android.widget.LinearLayout(this);
        col.setOrientation(1);
        col.setBackgroundResource(su.gwarden.music.R.drawable.bg_sheet);
        col.setPadding(dp(22), dp(6), dp(22), dp(18));

        android.view.View grip = new android.view.View(this);
        grip.setBackgroundColor(0x26FFFFFF);
        android.widget.LinearLayout.LayoutParams glp =
            new android.widget.LinearLayout.LayoutParams(dp(38), dp(4));
        glp.topMargin = dp(12);
        glp.bottomMargin = dp(16);
        glp.gravity = 1;
        grip.setLayoutParams(glp);
        col.addView(grip);
        android.view.View.OnTouchListener swipeClose = new android.view.View.OnTouchListener() {
            private float startY = 0f;
            @Override
            public boolean onTouch(android.view.View v, android.view.MotionEvent e) {
                if (e.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    startY = e.getRawY();
                    return true;
                }
                if (e.getAction() == android.view.MotionEvent.ACTION_UP) {
                    if (e.getRawY() - startY > dp(45)) {
                        d.dismiss();
                    }
                    return true;
                }
                return true;
            }
        };
        grip.setOnTouchListener(swipeClose);

        android.widget.TextView title = new android.widget.TextView(this);
        title.setText("Сортировка");
        title.setTextColor(-1);
        title.setTextSize(17.0f);
        title.setTypeface(title.getTypeface(), 1);
        title.setPadding(0, 0, 0, dp(6));
        title.setOnTouchListener(swipeClose);
        col.addView(title);

        addSortRow(col, d, "title", "По названию");
        addSortRow(col, d, "new", "Новые");
        addSortRow(col, d, "old", "Старые");

        android.view.View line = new android.view.View(this);
        line.setBackgroundColor(0x14FFFFFF);
        android.widget.LinearLayout.LayoutParams llp =
            new android.widget.LinearLayout.LayoutParams(-1, dp(1));
        llp.topMargin = dp(8);
        llp.bottomMargin = dp(2);
        line.setLayoutParams(llp);
        col.addView(line);

        addSortRow(col, d, "remember", "Запомнить выбор");

        // корень на весь экран: тап по свободному месту закрывает шторку
        android.widget.FrameLayout root = new android.widget.FrameLayout(this);
        root.setBackgroundColor(0);
        root.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                d.dismiss();
            }
        });
        col.setClickable(true);
        android.widget.FrameLayout.LayoutParams clp =
            new android.widget.FrameLayout.LayoutParams(-1, -2, 80);
        clp.leftMargin = dp(10);
        clp.rightMargin = dp(10);
        clp.bottomMargin = dp(12);
        root.addView(col, clp);
        d.setContentView(root);
        d.show();
        try { su.gwarden.music.ui.Skin.INSTANCE.applyToDialog(d); } catch (java.lang.Throwable ignored) {}
    }

    private final void addSortRow(android.widget.LinearLayout col, final android.app.Dialog d,
                                  final java.lang.String key, java.lang.String text) {
        boolean on = "remember".equals(key)
            ? this.mineSortRemember
            : key.equals(this.mineSort);

        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
        row.setOrientation(0);
        row.setGravity(16);
        row.setPadding(0, dp(14), 0, dp(14));
        row.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                if ("remember".equals(key)) {
                    mineSortRemember = !mineSortRemember;
                    getSharedPreferences("music_player", 0).edit()
                        .putBoolean("mine_sort_remember", mineSortRemember)
                        .putString("mine_sort", mineSort).apply();
                } else {
                    mineSort = key;
                    if (mineSortRemember) {
                        getSharedPreferences("music_player", 0).edit()
                            .putString("mine_sort", key).apply();
                    }
                    refreshMineList();
                }
                d.dismiss();
                buildSortRow();
            }
        });

        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText(text);
        tv.setTextSize(15.5f);
        tv.setTextColor(on ? -1 : 0xFF9A9A9A);
        tv.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, -2, 1.0f));
        row.addView(tv);

        android.widget.ImageView check = new android.widget.ImageView(this);
        check.setImageResource(su.gwarden.music.R.drawable.ic_check);
        check.setColorFilter(-1);
        check.setLayoutParams(new android.widget.LinearLayout.LayoutParams(dp(18), dp(18)));
        check.setVisibility(on ? 0 : 4);
        row.addView(check);

        col.addView(row);
    }

    /** Пересортировать уже загруженный список без похода на сервер. */
    private final void refreshMineList() {
        if (this.myTracks.isEmpty()) {
            return;
        }
        java.util.List shown = applyMineSort(applyOnlySc(visible(this.myTracks)));
        if (this.adapter != null) {
            this.adapter.submit(shown, false);
        }
        if (this.hSub != null) {
            this.hSub.setText("Твои треки · " + shown.size());
        }
    }

    /** Порядок треков: по названию / новые / старые. */
    private final java.util.List applyMineSort(java.util.List in) {
        java.util.List out = new java.util.ArrayList(in);
        java.util.Comparator cmp;
        if ("title".equals(this.mineSort)) {
            cmp = new java.util.Comparator() {
                @Override
                public int compare(java.lang.Object a, java.lang.Object b) {
                    su.gwarden.music.model.Track x = (su.gwarden.music.model.Track) a;
                    su.gwarden.music.model.Track y = (su.gwarden.music.model.Track) b;
                    java.lang.String sx = (x.getArtist() + " " + x.getTitle()).toLowerCase();
                    java.lang.String sy = (y.getArtist() + " " + y.getTitle()).toLowerCase();
                    return sx.compareTo(sy);
                }
            };
        } else {
            final boolean asc = "old".equals(this.mineSort);
            cmp = new java.util.Comparator() {
                @Override
                public int compare(java.lang.Object a, java.lang.Object b) {
                    long da = ((su.gwarden.music.model.Track) a).getDate();
                    long db = ((su.gwarden.music.model.Track) b).getDate();
                    if (da == db) {
                        return 0;
                    }
                    boolean aNewer = da > db;
                    return (aNewer == asc) ? 1 : -1;
                }
            };
        }
        try {
            java.util.Collections.sort(out, cmp);
        } catch (java.lang.Throwable ignored) {
        }
        return out;
    }

    private final void showBack(java.lang.String to) {
        this.backTab = to;
        android.view.View topBar = findViewById(su.gwarden.music.R.id.top_bar);
        if (topBar != null) {
            topBar.setVisibility(8);
        }
        if (this.settingsBtn != null) {
            this.settingsBtn.setVisibility(8);
        }
        findViewById(su.gwarden.music.R.id.btn_back).setVisibility(0);
        // шапка подэкрана снова полная (настройки её урезали)
        android.view.View sBtn = findViewById(su.gwarden.music.R.id.btn_search_top);
        if (sBtn != null) {
            sBtn.setVisibility(0);
        }
        android.view.View mBtn = findViewById(su.gwarden.music.R.id.btn_more);
        if (mBtn != null) {
            mBtn.setVisibility(0);
        }
        android.view.View header = findViewById(su.gwarden.music.R.id.app_header);
        if (header != null) {
            header.setVisibility(0);
        }
    }

    private final void hideBack() {
        this.backTab = null;
        android.view.View topBar = findViewById(su.gwarden.music.R.id.top_bar);
        if (topBar != null) {
            topBar.setVisibility(0);
        }
        findViewById(su.gwarden.music.R.id.btn_back).setVisibility(8);
        // настройки могли спрятать эти кнопки - возвращаем на место
        android.view.View sBtn = findViewById(su.gwarden.music.R.id.btn_search_top);
        if (sBtn != null) {
            sBtn.setVisibility(0);
        }
        android.view.View mBtn = findViewById(su.gwarden.music.R.id.btn_more);
        if (mBtn != null) {
            mBtn.setVisibility(0);
        }
        // на обычных вкладках верхней панели нет вообще
        android.view.View header = findViewById(su.gwarden.music.R.id.app_header);
        if (header != null) {
            header.setVisibility(8);
        }
        if (this.settingsBtn != null) {
            this.settingsBtn.setVisibility(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void goBack() {
        java.lang.String t = this.backTab;
        if (t == null) {
            return;
        }
        hideBack();
        setTab(t);
    }

    /* JADX WARN: Multi-variable type inference failed */
    static /* synthetic */ void loadMyMusic$default(su.gwarden.music.MainActivity mainActivity, kotlin.jvm.functions.Function1 function1, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            function1 = null;
        }
        mainActivity.loadMyMusic(function1);
    }

    private final void loadMyMusic(final kotlin.jvm.functions.Function1<? super java.util.List<su.gwarden.music.model.Track>, kotlin.Unit> then) {
        su.gwarden.music.ui.TrackAdapter trackAdapter = this.adapter;
        android.widget.TextView textView = null;
        if (trackAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter = null;
        }
        trackAdapter.setEditable(true);
        if (!this.myTracks.isEmpty() && then == null) {
            java.util.List shown = applyMineSort(applyOnlySc(visible(this.myTracks)));
            androidx.recyclerview.widget.RecyclerView recyclerView = this.recycler;
            if (recyclerView == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
                recyclerView = null;
            }
            su.gwarden.music.ui.TrackAdapter trackAdapter2 = this.adapter;
            if (trackAdapter2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter2 = null;
            }
            recyclerView.setAdapter(trackAdapter2);
            su.gwarden.music.ui.TrackAdapter trackAdapter3 = this.adapter;
            if (trackAdapter3 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter3 = null;
            }
            trackAdapter3.submit(shown, false);
            showEmpty(null);
            android.widget.TextView textView2 = this.hSub;
            if (textView2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            } else {
                textView = textView2;
            }
            textView.setText("Твои треки · " + shown.size());
            refreshAddButtons();
            return;
        }
        showLoading();
        su.gwarden.music.api.ApiClient.INSTANCE.myAudio(500, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda81
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit loadMyMusic$lambda$186;
                loadMyMusic$lambda$186 = su.gwarden.music.MainActivity.loadMyMusic$lambda$186(su.gwarden.music.MainActivity.this, then, (kotlin.Result) obj);
                return loadMyMusic$lambda$186;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit loadMyMusic$lambda$186(final su.gwarden.music.MainActivity this$0, final kotlin.jvm.functions.Function1 $then, final kotlin.Result result) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda160
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.loadMyMusic$lambda$186$lambda$185(this$0, result, $then);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void loadMyMusic$lambda$186$lambda$185(su.gwarden.music.MainActivity this$0, kotlin.Result $result, kotlin.jvm.functions.Function1 $then) {
        this$0.hideLoading();
        java.lang.Object value = mp.MpResult.getValue($result);
        if (mp.MpResult.m323isSuccessimpl(value)) {
            java.util.List serverTracks = (java.util.List) value;
            int i = 0;
            java.util.List tracks = su.gwarden.music.model.LocalLibrary.INSTANCE.applyOrder(su.gwarden.music.model.LocalLibrary.INSTANCE.merge(serverTracks));
            this$0.myTracks = tracks;
            try {
                int scN = 0;
                int vkN = 0;
                for (int k = 0; k < tracks.size(); k++) {
                    su.gwarden.music.model.Track tt = (su.gwarden.music.model.Track) tracks.get(k);
                    if ("soundcloud".equals(tt.getSource())) scN++;
                    else if ("vk".equals(tt.getSource())) vkN++;
                }
                su.gwarden.music.api.ApiClient.INSTANCE.log("mine",
                    "server=" + serverTracks.size() + " merged=" + tracks.size()
                    + " vk=" + vkN + " sc=" + scN + " sort=" + this$0.mineSort);
            } catch (java.lang.Throwable ignored) {
            }
            this$0.syncMineIds(tracks);
            this$0.syncMineIds(tracks);
            java.util.List shown = this$0.applyMineSort(this$0.applyOnlySc(this$0.visible(tracks)));
            androidx.recyclerview.widget.RecyclerView recyclerView = this$0.recycler;
            if (recyclerView == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
                recyclerView = null;
            }
            su.gwarden.music.ui.TrackAdapter trackAdapter = this$0.adapter;
            if (trackAdapter == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter = null;
            }
            recyclerView.setAdapter(trackAdapter);
            su.gwarden.music.ui.TrackAdapter trackAdapter2 = this$0.adapter;
            if (trackAdapter2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter2 = null;
            }
            trackAdapter2.submit(shown, false);
            android.widget.TextView textView = this$0.hSub;
            if (textView == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                textView = null;
            }
            textView.setText("Твои треки · " + shown.size());
            this$0.showEmpty(tracks.isEmpty() ? "В VK пока нет музыки" : null);
            if (!tracks.isEmpty()) {
                su.gwarden.music.api.ApiClient apiClient = su.gwarden.music.api.ApiClient.INSTANCE;
                java.util.Collection destination$iv$iv = new java.util.ArrayList();
                for (java.lang.Object element$iv$iv : tracks) {
                    java.util.List serverTracks2 = serverTracks;
                    su.gwarden.music.model.Track it = (su.gwarden.music.model.Track) element$iv$iv;
                    int i2 = i;
                    if (kotlin.jvm.internal.Intrinsics.areEqual(it.getSource(), "vk")) {
                        destination$iv$iv.add(element$iv$iv);
                    }
                    serverTracks = serverTracks2;
                    i = i2;
                }
                apiClient.prefetch((java.util.List) destination$iv$iv, "vk");
                su.gwarden.music.api.ApiClient apiClient2 = su.gwarden.music.api.ApiClient.INSTANCE;
                java.util.List $this$filter$iv = tracks;
                java.util.Collection destination$iv$iv2 = new java.util.ArrayList();
                for (java.lang.Object element$iv$iv2 : $this$filter$iv) {
                    su.gwarden.music.model.Track it2 = (su.gwarden.music.model.Track) element$iv$iv2;
                    java.lang.Iterable $this$filter$iv2 = $this$filter$iv;
                    if (kotlin.jvm.internal.Intrinsics.areEqual(it2.getSource(), "soundcloud")) {
                        destination$iv$iv2.add(element$iv$iv2);
                    }
                    $this$filter$iv = kotlin.collections.CollectionsKt.toList($this$filter$iv2);
                }
                apiClient2.prefetch((java.util.List) destination$iv$iv2, "soundcloud");
            }
            if ($then != null) {
                $then.invoke(tracks);
            }
        }
        java.lang.Throwable e = (java.lang.Throwable) mp.MpResult.m319exceptionOrNullimpl(value);
        if (e != null) {
            android.widget.TextView textView2 = this$0.hSub;
            if (textView2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                textView2 = null;
            }
            java.lang.String message = e.getMessage();
            if (message == null) {
                message = "Нет связи";
            }
            textView2.setText(message);
            this$0.showEmpty("Не удалось загрузить: " + e.getMessage());
        }
    }

    private final void loadBeat(final su.gwarden.music.model.Track track) {
        if (kotlin.jvm.internal.Intrinsics.areEqual(this.beatTrackId, track.getId())) {
            return;
        }
        this.beatTrackId = track.getId();
        this.beatRetries = 0;
        su.gwarden.music.ui.EqBackground eqBackground = this.waveEq;
        if (eqBackground == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveEq");
            eqBackground = null;
        }
        eqBackground.setBeatData(new float[0], 25.0f);
        su.gwarden.music.api.ApiClient.INSTANCE.beat(track.getSource(), track.getId(), new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda150
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit loadBeat$lambda$189;
                loadBeat$lambda$189 = su.gwarden.music.MainActivity.loadBeat$lambda$189(su.gwarden.music.MainActivity.this, track, (kotlin.Result) obj);
                return loadBeat$lambda$189;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit loadBeat$lambda$189(final su.gwarden.music.MainActivity this$0, final su.gwarden.music.model.Track $track, kotlin.Result res) {
        java.lang.Object value = mp.MpResult.getValue(res);
        if (mp.MpResult.m323isSuccessimpl(value)) {
            final su.gwarden.music.api.ApiClient.BeatMap bm = (su.gwarden.music.api.ApiClient.BeatMap) value;
            this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda107
                @Override // java.lang.Runnable
                public final void run() {
                    su.gwarden.music.MainActivity.loadBeat$lambda$189$lambda$188$lambda$187(this$0, $track, bm);
                }
            });
        }
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void loadBeat$lambda$189$lambda$188$lambda$187(su.gwarden.music.MainActivity this$0, su.gwarden.music.model.Track $track, su.gwarden.music.api.ApiClient.BeatMap $bm) {
        if (kotlin.jvm.internal.Intrinsics.areEqual(this$0.beatTrackId, $track.getId())) {
            su.gwarden.music.ui.EqBackground eqBackground = this$0.waveEq;
            if (eqBackground == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveEq");
                eqBackground = null;
            }
            eqBackground.setBeatData($bm.getEnergy(), $bm.getRate(), $bm.getBeats(), $bm.getBands());
            try {
                su.gwarden.music.api.ApiClient.INSTANCE.log("beat",
                    "rate=" + $bm.getRate() + " energy=" + $bm.getEnergy().length
                    + " beats=" + $bm.getBeats().length + " track=" + $track.getId());
            } catch (java.lang.Throwable ignored) {
            }
            if ($bm.getEnergy().length == 0 && this$0.beatRetries < 4) {
                // карта ещё не готова (трек только скачался) - спросим позже
                this$0.beatRetries++;
                final su.gwarden.music.model.Track again = $track;
                eqBackground.postDelayed(new java.lang.Runnable() {
                    @Override
                    public void run() {
                        this$0.beatTrackId = null;
                        this$0.loadBeat(again);
                    }
                }, 7000L);
            }
        }
    }

    static /* synthetic */ void loadWave$default(su.gwarden.music.MainActivity mainActivity, boolean z, boolean z2, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            z = false;
        }
        if ((i & 2) != 0) {
            z2 = true;
        }
        mainActivity.loadWave(z, z2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void loadWave(final boolean play, boolean refresh) {
        if (this.waveLoading) {
            return;
        }
        clearSimilar();
        this.waveGen++;
        final int gen = this.waveGen;
        this.waveLoading = true;
        android.widget.TextView textView = this.wavePlay;
        android.widget.TextView textView2 = null;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePlay");
            textView = null;
        }
        textView.setEnabled(false);
        android.widget.TextView textView3 = this.wavePlay;
        if (textView3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePlay");
        } else {
            textView2 = textView3;
        }
        textView2.setText("Собираю...");
        this.ui.postDelayed(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda92
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.loadWave$lambda$190(gen, MainActivity.this);
            }
        }, 30000L);
        su.gwarden.music.api.ApiClient.INSTANCE.wave(40, refresh, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda93
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit loadWave$lambda$198;
                loadWave$lambda$198 = su.gwarden.music.MainActivity.loadWave$lambda$198(su.gwarden.music.MainActivity.this, gen, play, (kotlin.Result) obj);
                return loadWave$lambda$198;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void loadWave$lambda$190(int $gen, su.gwarden.music.MainActivity this$0) {
        if ($gen == this$0.waveGen && this$0.waveLoading) {
            this$0.waveLoading = false;
            android.widget.TextView textView = this$0.wavePlay;
            su.gwarden.music.player.PlayerController playerController = null;
            if (textView == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePlay");
                textView = null;
            }
            textView.setEnabled(true);
            android.widget.TextView textView2 = this$0.wavePlay;
            if (textView2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePlay");
                textView2 = null;
            }
            su.gwarden.music.player.PlayerController playerController2 = this$0.player;
            if (playerController2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            } else {
                playerController = playerController2;
            }
            textView2.setText(playerController.isPlaying() ? "Пауза" : "Слушать");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit loadWave$lambda$198(final su.gwarden.music.MainActivity this$0, final int $gen, final boolean $play, final kotlin.Result result) {
        this$0.ui.post(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda73
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.loadWave$lambda$198$lambda$197($gen, this$0, result, $play);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void loadWave$lambda$198$lambda$197(int $gen, final su.gwarden.music.MainActivity this$0, kotlin.Result $result, boolean $play) {
        android.widget.TextView textView;
        if ($gen != this$0.waveGen || this$0.isFinishing() || this$0.isDestroyed()) {
            return;
        }
        this$0.waveLoading = false;
        android.widget.TextView textView2 = this$0.wavePlay;
        if (textView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePlay");
            textView2 = null;
        }
        textView2.setEnabled(true);
        android.widget.TextView textView3 = this$0.wavePlay;
        if (textView3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePlay");
            textView3 = null;
        }
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        textView3.setText(playerController.isPlaying() ? "Пауза" : "Слушать");
        java.lang.Object value = mp.MpResult.getValue($result);
        if (mp.MpResult.m323isSuccessimpl(value)) {
            java.util.List tracks = (java.util.List) value;
            this$0.waveQueue = kotlin.collections.CollectionsKt.shuffled(tracks);
            if (!$play) {
                java.lang.Iterable $this$forEach$iv = kotlin.collections.CollectionsKt.take(this$0.waveQueue, 3);
                for (java.lang.Object element$iv : $this$forEach$iv) {
                    su.gwarden.music.model.Track it = (su.gwarden.music.model.Track) element$iv;
                    su.gwarden.music.player.PlayerController playerController2 = this$0.player;
                    if (playerController2 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                        playerController2 = null;
                    }
                    playerController2.prefetch(it);
                }
            }
            if (!this$0.waveQueue.isEmpty()) {
                this$0.waveRetry = 0;
                this$0.waveEnded = false;
                android.widget.TextView textView4 = this$0.hSub;
                if (textView4 == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                    textView4 = null;
                }
                textView4.setText("Моя волна");
                if ($play) {
                    playTrack$default(this$0, (su.gwarden.music.model.Track) kotlin.collections.CollectionsKt.first((java.util.List) this$0.waveQueue), this$0.waveQueue, false, 4, null);
                } else {
                    android.widget.TextView textView5 = this$0.waveTitle;
                    if (textView5 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveTitle");
                        textView5 = null;
                    }
                    textView5.setText("Моя волна");
                    android.widget.TextView textView6 = this$0.waveArtist;
                    if (textView6 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveArtist");
                        textView6 = null;
                    }
                    textView6.setText("Подборка готова · нажми «Слушать»");
                }
            } else {
                android.widget.TextView textView7 = this$0.hSub;
                if (textView7 == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                    textView7 = null;
                }
                textView7.setText("Волна · пробую ещё");
                if (this$0.onWave) {
                    this$0.ui.postDelayed(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda54
                        @Override // java.lang.Runnable
                        public final void run() {
                            su.gwarden.music.MainActivity.refillWave$default(this$0, 0, 1, null);
                        }
                    }, androidx.media3.exoplayer.ExoPlayer.DEFAULT_DETACH_SURFACE_TIMEOUT_MS);
                } else if ($play && this$0.waveRetry < 6) {
                    this$0.waveRetry++;
                    this$0.ui.postDelayed(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda56
                        @Override // java.lang.Runnable
                        public final void run() {
                            this$0.loadWave(true, true);
                        }
                    }, 2500L);
                } else {
                    android.widget.TextView textView8 = this$0.hSub;
                    if (textView8 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                        textView8 = null;
                    }
                    textView8.setText("Не удалось собрать волну");
                }
            }
        }
        java.lang.Throwable e = (java.lang.Throwable) mp.MpResult.m319exceptionOrNullimpl(value);
        if (e != null) {
            android.widget.TextView textView9 = this$0.hSub;
            if (textView9 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                textView = null;
            } else {
                textView = textView9;
            }
            textView.setText("Волна: " + e.getMessage());
            if (this$0.onWave) {
                this$0.ui.postDelayed(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda57
                    @Override // java.lang.Runnable
                    public final void run() {
                        su.gwarden.music.MainActivity.refillWave$default(this$0, 0, 1, null);
                    }
                }, 2500L);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void refillWave$default(su.gwarden.music.MainActivity mainActivity, int i, int i2, java.lang.Object obj) {
        if ((i2 & 1) != 0) {
            i = 0;
        }
        mainActivity.refillWave(i);
    }

    private final void refillWave(final int attempt) {
        if (this.waveRefilling || isFinishing() || isDestroyed()) {
            return;
        }
        this.waveRefilling = true;
        final boolean playNext = this.waveEnded;
        su.gwarden.music.api.ApiClient.INSTANCE.wave(15, true, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda74
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit refillWave$lambda$204;
                refillWave$lambda$204 = su.gwarden.music.MainActivity.refillWave$lambda$204(su.gwarden.music.MainActivity.this, playNext, attempt, (kotlin.Result) obj);
                return refillWave$lambda$204;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit refillWave$lambda$204(final su.gwarden.music.MainActivity this$0, final boolean $playNext, final int $attempt, final kotlin.Result result) {
        this$0.ui.post(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda23
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.refillWave$lambda$204$lambda$203(this$0, result, $playNext, $attempt);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void refillWave$lambda$204$lambda$203(final su.gwarden.music.MainActivity this$0, kotlin.Result $result, boolean $playNext, final int $attempt) {
        this$0.waveRefilling = false;
        if (this$0.isFinishing() || this$0.isDestroyed()) {
            return;
        }
        if (!this$0.onWave) {
            this$0.waveEnded = false;
            return;
        }
        java.util.HashSet taken = new java.util.HashSet();
        java.lang.Iterable $this$forEach$iv = this$0.waveQueue;
        for (java.lang.Object element$iv : $this$forEach$iv) {
            taken.add(((su.gwarden.music.model.Track) element$iv).getId());
        }
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        android.widget.TextView textView = null;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        su.gwarden.music.model.Track it = playerController.current();
        if (it != null) {
            taken.add(it.getId());
        }
        java.lang.Object value = mp.MpResult.getValue($result);
        if (mp.MpResult.m322isFailureimpl(value)) {
            value = null;
        }
        java.lang.Iterable iterable = (java.util.List) value;
        if (iterable == null) {
            iterable = kotlin.collections.CollectionsKt.emptyList();
        }
        java.lang.Iterable $this$filter$iv = iterable;
        java.util.Collection destination$iv$iv = new java.util.ArrayList();
        for (java.lang.Object element$iv$iv : $this$filter$iv) {
            if (!taken.contains(((su.gwarden.music.model.Track) element$iv$iv).getId())) {
                destination$iv$iv.add(element$iv$iv);
            }
        }
        java.util.List fresh = (java.util.List) destination$iv$iv;
        if (!fresh.isEmpty()) {
            this$0.waveQueue = kotlin.collections.CollectionsKt.takeLast(kotlin.collections.CollectionsKt.plus((java.util.Collection) this$0.waveQueue, (java.lang.Iterable) fresh), 400);
            su.gwarden.music.player.PlayerController playerController2 = this$0.player;
            if (playerController2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                playerController2 = null;
            }
            playerController2.append(fresh);
            this$0.waveEnded = false;
            this$0.waveRetry = 0;
            if ($playNext || this$0.waveEnded) {
                su.gwarden.music.player.PlayerController playerController3 = this$0.player;
                if (playerController3 == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                    playerController3 = null;
                }
                if (playerController3.next(false) == null) {
                    playTrack$default(this$0, (su.gwarden.music.model.Track) kotlin.collections.CollectionsKt.first(fresh), this$0.waveQueue, false, 4, null);
                }
                android.widget.TextView textView2 = this$0.hSub;
                if (textView2 == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                } else {
                    textView = textView2;
                }
                textView.setText("Моя волна");
                return;
            }
            return;
        }
        if ($attempt < 5) {
            android.widget.TextView textView3 = this$0.hSub;
            if (textView3 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            } else {
                textView = textView3;
            }
            textView.setText("Волна · ищу ещё");
            this$0.ui.postDelayed(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda83
                @Override // java.lang.Runnable
                public final void run() {
                    su.gwarden.music.MainActivity.refillWave$lambda$204$lambda$203$lambda$202(this$0, $attempt);
                }
            }, ($attempt * 1500) + 1500);
            return;
        }
        this$0.waveEnded = false;
        if (!this$0.waveQueue.isEmpty()) {
            java.util.List again = kotlin.collections.CollectionsKt.shuffled(this$0.waveQueue);
            this$0.waveQueue = again;
            playTrack$default(this$0, (su.gwarden.music.model.Track) kotlin.collections.CollectionsKt.first(again), again, false, 4, null);
            android.widget.TextView textView4 = this$0.hSub;
            if (textView4 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            } else {
                textView = textView4;
            }
            textView.setText("Волна · по кругу");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void refillWave$lambda$204$lambda$203$lambda$202(su.gwarden.music.MainActivity this$0, int $attempt) {
        this$0.refillWave($attempt + 1);
    }

    /* ---------------------------------------------- поиск: типы и результаты */

    private final void bindSearchChips() {
        android.view.View.OnClickListener l = v -> {
            int id = v.getId();
            if (id == su.gwarden.music.R.id.st_artists) {
                this.searchTab = "artists";
            } else if (id == su.gwarden.music.R.id.st_albums) {
                this.searchTab = "albums";
            } else if (id == su.gwarden.music.R.id.st_playlists) {
                this.searchTab = "playlists";
            } else {
                this.searchTab = "tracks";
            }
            renderSearch();
            if (isEmptySearchTab()) {
                doSearch();
            }
        };
        int[] ids = {
                su.gwarden.music.R.id.st_tracks,
                su.gwarden.music.R.id.st_artists,
                su.gwarden.music.R.id.st_albums,
                su.gwarden.music.R.id.st_playlists,
        };
        for (int id : ids) {
            android.view.View chip = findViewById(id);
            if (chip != null) {
                chip.setOnClickListener(l);
            }
        }
    }

    private final boolean isEmptySearchTab() {
        su.gwarden.music.api.ApiClient.SearchAll d = this.searchData;
        if (d == null) {
            return false;
        }
        java.lang.String t = this.searchTab;
        if ("artists".equals(t)) {
            return d.getArtists().isEmpty();
        }
        if ("albums".equals(t)) {
            return d.getAlbums().isEmpty();
        }
        if ("playlists".equals(t)) {
            return d.getPlaylists().isEmpty();
        }
        return d.getTracks().isEmpty();
    }

    private final void renderSearch() {
        su.gwarden.music.api.ApiClient.SearchAll d = this.searchData;
        if (d == null || this.recycler == null) {
            return;
        }
        if (this.artistScroll != null) {
            this.artistScroll.setVisibility(8);
        }
        if (this.settingsScroll != null) {
            this.settingsScroll.setVisibility(8);
        }
        if (this.feedScroll != null) {
            this.feedScroll.setVisibility(8);
        }
        hideLib();
        if (this.wavePanel != null) {
            this.wavePanel.setVisibility(8);
        }
        if (this.waveEq != null) {
            this.waveEq.setActive(false);
            this.waveEq.setVisibility(8);
        }
        if (this.recycler != null) {
            this.recycler.setVisibility(0);
            this.recycler.bringToFront();
        }
        hideBack();
        java.lang.String t = this.searchTab == null ? "tracks" : this.searchTab;
        if ("tracks".equals(t)) {
            this.recycler.setAdapter(this.adapter);
            this.adapter.submit(d.getTracks(), false);
        } else {
            java.util.List<su.gwarden.music.ui.RowItem> items;
            if ("artists".equals(t)) {
                items = d.getArtists();
            } else if ("albums".equals(t)) {
                items = d.getAlbums();
            } else {
                items = d.getPlaylists();
            }
            this.recycler.setAdapter(this.rowAdapter);
            this.rowAdapter.submit(items);
            if (items.isEmpty()) {
                showPill("Ничего не нашлось");
            }
        }
        moveSearchPill("artists".equals(t) ? 1 : "albums".equals(t) ? 2 : "playlists".equals(t) ? 3 : 0);
        for (int id : new int[]{
                su.gwarden.music.R.id.st_tracks,
                su.gwarden.music.R.id.st_artists,
                su.gwarden.music.R.id.st_albums,
                su.gwarden.music.R.id.st_playlists,
        }) {
            android.view.View chip = findViewById(id);
            if (chip instanceof android.widget.TextView) {
                boolean on = (id == su.gwarden.music.R.id.st_tracks && "tracks".equals(t))
                        || (id == su.gwarden.music.R.id.st_artists && "artists".equals(t))
                        || (id == su.gwarden.music.R.id.st_albums && "albums".equals(t))
                        || (id == su.gwarden.music.R.id.st_playlists && "playlists".equals(t));
                ((android.widget.TextView) chip).setTextColor(on ? 0xFF000000 : 0xFFB4B4B4);
                chip.setBackground(on
                        ? getDrawable(su.gwarden.music.R.drawable.bg_listen)
                        : null);
            }
        }
    }

    /** Тап по артисту/альбому/плейлисту в результатах поиска. */
    private final void onRowClick(su.gwarden.music.ui.RowItem item) {
        if ("artist".equals(item.getKind())) {
            openArtist(item.getSource(), item.getId(), item.getTitle(), item.getCover());
            return;
        }
        if ("album".equals(item.getKind())) {
            openCollectionNow(item.getSource(), "album", item.getId(), item.getTitle());
            return;
        }
        openCollectionNow(item.getSource(), "playlist", item.getId(), item.getTitle());
    }

    /* ---------------------------------------------------- карточка артиста */

    /** Переход к артисту из шортсов (шортсы - отдельная Activity, шлют intent). */
    private final void openArtistFromIntent(android.content.Intent intent) {
        if (intent == null) {
            return;
        }
        java.lang.String raw = intent.getStringExtra("artist_name");
        if (raw == null || raw.trim().isEmpty()) {
            return;
        }
        intent.removeExtra("artist_name");
        final java.lang.String nm = raw.trim();
        android.view.View root = findViewById(su.gwarden.music.R.id.app_root);
        if (root == null) {
            showArtistPicker(nm, null);
            return;
        }
        root.postDelayed(new java.lang.Runnable() {
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.showArtistPicker(nm, null);
            }
        }, 240L);
    }

    /** Имя артиста в плеере: лёгкий шеврон, чтобы было видно - можно нажать. */
    static android.text.SpannableStringBuilder artistLink(java.lang.String raw) {
        android.text.SpannableStringBuilder sb =
                new android.text.SpannableStringBuilder(raw == null ? "" : raw);
        int at = sb.length();
        sb.append("  \u203a");
        sb.setSpan(new android.text.style.ForegroundColorSpan(0xFF6E6E6E),
                at, sb.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

    /** «A, B; C x D» -> список артистов (запятая, точка с запятой, «x»). */
    static java.util.List<java.lang.String> splitArtists(java.lang.String raw) {
        java.util.ArrayList<java.lang.String> out = new java.util.ArrayList<>();
        if (raw == null) {
            return out;
        }
        for (java.lang.String part : raw.split("\\s*[,;]\\s*|\\s+[x\u00d7]\\s+")) {
            java.lang.String nm = part == null ? "" : part.trim();
            if (nm.isEmpty()) {
                continue;
            }
            boolean dup = false;
            for (java.lang.String e : out) {
                if (e.equalsIgnoreCase(nm)) {
                    dup = true;
                    break;
                }
            }
            if (!dup) {
                out.add(nm);
            }
        }
        return out;
    }

    /**
     * Тап по артисту в плеере. Один артист - сразу карточка. Несколько
     * («A, B, C») - шторка снизу, где выбираем, к кому идём.
     */
    private final void showArtistPicker(java.lang.String raw, final android.app.Dialog parent) {
        final java.util.List<java.lang.String> names = splitArtists(raw);
        if (names.isEmpty()) {
            showPill("Не понял артиста");
            return;
        }
        if (names.size() == 1) {
            if (parent != null) {
                try { parent.dismiss(); } catch (java.lang.Throwable ignored) {}
            }
            openArtistByName(names.get(0), false);
            return;
        }

        final android.app.Dialog d = new android.app.Dialog(this, su.gwarden.music.R.style.Theme_Wonder_Sheet);
        su.gwarden.music.ui.Skin.INSTANCE.prepare(d);
        final android.widget.FrameLayout root = new android.widget.FrameLayout(this);
        root.setBackgroundColor(0x99000000);

        final android.widget.LinearLayout card = new android.widget.LinearLayout(this);
        card.setOrientation(1);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0xFF111111);
        float rr = dp(26);
        bg.setCornerRadii(new float[]{rr, rr, rr, rr, 0f, 0f, 0f, 0f});
        card.setBackground(bg);
        card.setPadding(dp(14), dp(10), dp(14), dp(22));

        android.widget.FrameLayout.LayoutParams clp =
                new android.widget.FrameLayout.LayoutParams(-1, -2);
        clp.gravity = android.view.Gravity.BOTTOM;
        root.addView(card, clp);

        android.view.View handle = new android.view.View(this);
        android.graphics.drawable.GradientDrawable hb = new android.graphics.drawable.GradientDrawable();
        hb.setColor(0xFF3A3A3A);
        hb.setCornerRadius(dp(2));
        handle.setBackground(hb);
        android.widget.LinearLayout.LayoutParams hlp =
                new android.widget.LinearLayout.LayoutParams(dp(38), dp(4));
        hlp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        hlp.bottomMargin = dp(16);
        card.addView(handle, hlp);

        android.widget.TextView title = new android.widget.TextView(this);
        title.setText("ВЫБЕРИТЕ АРТИСТА");
        title.setTextColor(0xFF8A8A8A);
        title.setTextSize(11.5f);
        title.setTypeface(title.getTypeface(), 1);
        title.setLetterSpacing(0.08f);
        title.setPadding(dp(10), 0, dp(10), dp(8));
        card.addView(title);

        android.widget.ScrollView sc = new android.widget.ScrollView(this);
        sc.setVerticalScrollBarEnabled(false);
        final android.widget.LinearLayout rows = new android.widget.LinearLayout(this);
        rows.setOrientation(1);
        sc.addView(rows, new android.widget.FrameLayout.LayoutParams(-1, -2));

        for (int i = 0; i < names.size(); i++) {
            final java.lang.String nm = names.get(i);
            android.widget.LinearLayout row = new android.widget.LinearLayout(this);
            row.setOrientation(0);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(dp(8), dp(11), dp(12), dp(11));
            android.graphics.drawable.GradientDrawable rb = new android.graphics.drawable.GradientDrawable();
            rb.setColor(0xFF1A1A1A);
            rb.setCornerRadius(dp(18));
            row.setBackground(rb);

            android.widget.TextView ini = new android.widget.TextView(this);
            ini.setText(nm.substring(0, 1).toUpperCase());
            ini.setTextColor(-1);
            ini.setTextSize(15.0f);
            ini.setTypeface(ini.getTypeface(), 1);
            ini.setGravity(android.view.Gravity.CENTER);
            android.graphics.drawable.GradientDrawable ib = new android.graphics.drawable.GradientDrawable();
            ib.setColor(0xFF2C2C2C);
            ib.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            ini.setBackground(ib);
            android.widget.LinearLayout.LayoutParams ilp =
                    new android.widget.LinearLayout.LayoutParams(dp(40), dp(40));
            ilp.setMarginEnd(dp(12));
            row.addView(ini, ilp);

            android.widget.TextView tx = new android.widget.TextView(this);
            tx.setText(nm);
            tx.setTextColor(-1);
            tx.setTextSize(15.0f);
            tx.setSingleLine(true);
            tx.setEllipsize(android.text.TextUtils.TruncateAt.END);
            row.addView(tx, new android.widget.LinearLayout.LayoutParams(0, -2, 1.0f));

            android.widget.TextView ar = new android.widget.TextView(this);
            ar.setText("\u203a");
            ar.setTextColor(0xFF8A8A8A);
            ar.setTextSize(20.0f);
            ar.setPadding(dp(8), 0, 0, 0);
            row.addView(ar);

            row.setOnClickListener(v -> {
                d.dismiss();
                if (parent != null) {
                    try { parent.dismiss(); } catch (java.lang.Throwable ignored) {}
                }
                openArtistByName(nm, false);
            });

            android.widget.LinearLayout.LayoutParams rlp =
                    new android.widget.LinearLayout.LayoutParams(-1, -2);
            rlp.bottomMargin = dp(8);
            rows.addView(row, rlp);
        }
        card.addView(sc);

        root.setOnClickListener(v -> d.dismiss());
        card.setOnClickListener(v -> { /* клик по самой шторке не закрывает */ });

        d.setContentView(root, new android.widget.FrameLayout.LayoutParams(-1, -1));
        android.view.Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
            w.setLayout(-1, -1);
        }
        d.show();
        try { su.gwarden.music.ui.Skin.INSTANCE.applyToDialog(d); } catch (java.lang.Throwable ignored) {}
        card.setTranslationY(dp(90));
        card.setAlpha(0.0f);
        card.animate()
                .translationY(0.0f)
                .alpha(1.0f)
                .setDuration(240L)
                .setInterpolator(new android.view.animation.PathInterpolator(0.2f, 1.0f, 0.3f, 1.0f))
                .start();
    }

    private final void openArtistByName(java.lang.String name) {
        openArtistByName(name, false);
    }

    private final void openArtistByName(java.lang.String name, boolean autoPlay) {
        if (name == null || name.trim().isEmpty()) {
            showPill("Не понял артиста");
            return;
        }
        openArtist("", "", name.trim(), "", autoPlay);
    }

    private final void openArtist(final java.lang.String source, final java.lang.String id,
                                  final java.lang.String name, final java.lang.String cover) {
        openArtist(source, id, name, cover, false);
    }

    /** Открыть полноэкранную карточку: аватар, имя, Слушать, релиз, треки. */
    private final void openArtist(final java.lang.String source, final java.lang.String id,
                                  final java.lang.String name, final java.lang.String cover,
                                  final boolean autoPlay) {
        if (this.artistScroll != null) {
            this.artistScroll.setVisibility(0);
            this.artistScroll.bringToFront();
        }
        if (this.settingsScroll != null) {
            this.settingsScroll.setVisibility(8);
        }
        if (this.feedScroll != null) {
            this.feedScroll.setVisibility(8);
        }
        hideLib();
        if (this.recycler != null) {
            this.recycler.setVisibility(8);
        }
        if (this.wavePanel != null) {
            this.wavePanel.setVisibility(8);
        }
        if (this.waveEq != null) {
            this.waveEq.setActive(false);
            this.waveEq.setVisibility(8);
        }
        showBack(this.tab == null ? "search" : this.tab);
        if (this.hTitle != null) {
            this.hTitle.setText("Артист");
        }
        if (this.hSub != null) {
            this.hSub.setText("");
        }
        showEmpty(null);
        hideLoading();
        buildArtistSkeleton(name, cover);
        final int req = ++this.artistReq;
        this.artistReqDone = false;
        loadArtistPage(req, source, id, name, cover, autoPlay, 0);
        // страховка: если ответа нет 22 секунды - показываем ошибку с кнопкой,
        // а не вечный спиннер
        if (this.artistBox != null) {
            this.artistBox.postDelayed(new java.lang.Runnable() {
                @Override // java.lang.Runnable
                public final void run() {
                    if (req != MainActivity.this.artistReq || MainActivity.this.artistReqDone) {
                        return;
                    }
                    MainActivity.this.artistReqDone = true;
                    MainActivity.this.buildArtistError(source, id, name, cover, autoPlay);
                }
            }, 22000L);
        }
    }

    /** Запрос карточки с одним тихим повтором: сервер мог перезапускаться. */
    private final void loadArtistPage(final int req, final java.lang.String source, final java.lang.String id,
                                      final java.lang.String name, final java.lang.String cover,
                                      final boolean autoPlay, final int attempt) {
        su.gwarden.music.api.ApiClient.INSTANCE.artistPage(source, id, name, 300, (page, err) -> {
            if (req != this.artistReq) {
                return kotlin.Unit.INSTANCE;
            }
            if (page == null) {
                if (attempt < 1) {
                    if (this.artistBox != null) {
                        this.artistBox.postDelayed(new java.lang.Runnable() {
                            @Override // java.lang.Runnable
                            public final void run() {
                                MainActivity.this.loadArtistPage(req, source, id, name, cover, autoPlay, attempt + 1);
                            }
                        }, 1300L);
                    }
                    return kotlin.Unit.INSTANCE;
                }
                this.artistReqDone = true;
                buildArtistError(source, id, name, cover, autoPlay);
                showPill("Артист: " + (err == null ? "нет ответа" : err));
                return kotlin.Unit.INSTANCE;
            }
            this.artistReqDone = true;
            showArtistScreen(page);
            if (autoPlay && page.getTracks() != null && !page.getTracks().isEmpty()) {
                playTrack(page.getTracks().get(0), page.getTracks(), false);
            }
            return kotlin.Unit.INSTANCE;
        });
    }

    /** Ошибка карточки: без вечного «Загружаю…», с кнопкой «Повторить». */
    private final void buildArtistError(final java.lang.String source, final java.lang.String id,
                                        final java.lang.String name, final java.lang.String cover,
                                        final boolean autoPlay) {
        final android.widget.LinearLayout box = this.artistBox;
        if (box == null) {
            return;
        }
        box.removeAllViews();
        box.setPadding(dp(16), dp(8), dp(16), dp(28));
        if (cover != null && !cover.isEmpty()) {
            android.widget.ImageView av = new android.widget.ImageView(this);
            int sz = dp(132);
            android.widget.LinearLayout.LayoutParams alp =
                    new android.widget.LinearLayout.LayoutParams(sz, sz);
            alp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
            alp.topMargin = dp(10);
            av.setLayoutParams(alp);
            av.setBackgroundResource(su.gwarden.music.R.drawable.bg_avatar_dark);
            av.setClipToOutline(true);
            av.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            com.bumptech.glide.Glide.with(this).load(cover).circleCrop().into(av);
            box.addView(av);
        }
        if (name != null && !name.isEmpty()) {
            android.widget.TextView nm = new android.widget.TextView(this);
            nm.setText(name);
            nm.setTextColor(-1);
            nm.setTextSize(24.0f);
            nm.setTypeface(nm.getTypeface(), 1);
            nm.setGravity(android.view.Gravity.CENTER);
            nm.setPadding(0, dp(16), 0, 0);
            box.addView(nm);
        }
        android.widget.TextView msg = new android.widget.TextView(this);
        msg.setText("Не удалось загрузить артиста.\nПроверь связь и попробуй ещё раз.");
        msg.setTextColor(0xFF8A8A8A);
        msg.setTextSize(13.5f);
        msg.setGravity(android.view.Gravity.CENTER);
        msg.setPadding(dp(24), dp(26), dp(24), 0);
        box.addView(msg);

        android.widget.TextView retry = new android.widget.TextView(this);
        retry.setText("Повторить");
        retry.setTextColor(-1);
        retry.setTextSize(14.0f);
        retry.setTypeface(retry.getTypeface(), 1);
        retry.setGravity(android.view.Gravity.CENTER);
        retry.setBackgroundResource(su.gwarden.music.R.drawable.bg_row);
        android.widget.LinearLayout.LayoutParams rlp =
                new android.widget.LinearLayout.LayoutParams(dp(200), dp(46));
        rlp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        rlp.topMargin = dp(18);
        retry.setOnClickListener(v -> openArtist(source, id, name, cover, autoPlay));
        box.addView(retry, rlp);
    }

    /** Пока грузится - аватар, имя и спиннер вместо надписи «Загружаю...». */
    private final void buildArtistSkeleton(java.lang.String name, java.lang.String cover) {
        final android.widget.LinearLayout box = this.artistBox;
        if (box == null) {
            return;
        }
        box.removeAllViews();
        if (cover != null && !cover.isEmpty()) {
            android.widget.ImageView av = new android.widget.ImageView(this);
            int sz = dp(132);
            android.widget.LinearLayout.LayoutParams alp =
                    new android.widget.LinearLayout.LayoutParams(sz, sz);
            alp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
            alp.topMargin = dp(10);
            av.setLayoutParams(alp);
            av.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_cover_big));
            av.setClipToOutline(true);
            av.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            com.bumptech.glide.Glide.with(this).load(cover).circleCrop().into(av);
            box.addView(av);
        }
        if (name != null && !name.isEmpty()) {
            android.widget.TextView nm = new android.widget.TextView(this);
            nm.setText(name);
            nm.setTextColor(-1);
            nm.setTextSize(24.0f);
            nm.setTypeface(nm.getTypeface(), 1);
            nm.setGravity(android.view.Gravity.CENTER);
            nm.setPadding(0, dp(16), 0, 0);
            box.addView(nm);
        }
        su.gwarden.music.ui.IosSpinner spinner = new su.gwarden.music.ui.IosSpinner(this);
        android.widget.LinearLayout.LayoutParams slp =
                new android.widget.LinearLayout.LayoutParams(dp(34), dp(34));
        slp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        slp.topMargin = dp(30);
        box.addView(spinner, slp);
    }

    private final void showArtistScreen(su.gwarden.music.api.ApiClient.ArtistPage page) {
        final android.widget.LinearLayout box = this.artistBox;
        if (box == null) {
            return;
        }
        box.removeAllViews();
        box.setPadding(0, 0, 0, dp(28));
        final java.util.List<su.gwarden.music.model.Track> tracks = page.getTracks();

        // ---------- шапка: большое фото артиста с плавным спуском в фон приложения
        android.widget.FrameLayout head = new android.widget.FrameLayout(this);
        int headH = dp(340);
        head.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, headH));
        head.setClipChildren(true);

        su.gwarden.music.ui.FadeImageView bg = new su.gwarden.music.ui.FadeImageView(this);
        bg.setFadeStart(0.48f);
        bg.setFadeEnd(1.0f);
        bg.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        if (page.getCover() != null && !page.getCover().isEmpty()) {
            com.bumptech.glide.Glide.with(this).load(page.getCover()).centerCrop().into(bg);
        } else {
            bg.setBackgroundColor(0xFF141414);
        }
        head.addView(bg, new android.widget.FrameLayout.LayoutParams(-1, -1));

        android.view.View scrim = new android.view.View(this);
        scrim.setBackground(new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x4D000000, 0x0F000000, 0x00000000}));
        head.addView(scrim, new android.widget.FrameLayout.LayoutParams(-1, -1));

        // круглый аватар поверх фото
        android.widget.ImageView av = new android.widget.ImageView(this);
        int sz = dp(132);
        android.widget.FrameLayout.LayoutParams alp = new android.widget.FrameLayout.LayoutParams(sz, sz);
        alp.gravity = android.view.Gravity.CENTER_HORIZONTAL | android.view.Gravity.BOTTOM;
        alp.bottomMargin = dp(96);
        av.setLayoutParams(alp);
        av.setBackgroundResource(su.gwarden.music.R.drawable.bg_avatar_dark);
        av.setClipToOutline(true);
        av.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        if (page.getCover() != null && !page.getCover().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(page.getCover()).circleCrop().into(av);
        } else {
            av.setImageResource(su.gwarden.music.R.drawable.ic_music_note);
        }
        head.addView(av);

        // имя на градиенте
        android.widget.TextView name = new android.widget.TextView(this);
        name.setText(page.getName() == null ? "" : page.getName());
        name.setTextColor(-1);
        name.setTextSize(26.0f);
        name.setTypeface(name.getTypeface(), 1);
        name.setGravity(android.view.Gravity.CENTER);
        android.widget.FrameLayout.LayoutParams nlp =
                new android.widget.FrameLayout.LayoutParams(-1, -2);
        nlp.gravity = android.view.Gravity.BOTTOM;
        nlp.bottomMargin = dp(54);
        nlp.leftMargin = dp(24);
        nlp.rightMargin = dp(24);
        name.setLayoutParams(nlp);
        head.addView(name);

        box.addView(head);

        // ---------- всё остальное с обычными отступами
        android.widget.LinearLayout content = new android.widget.LinearLayout(this);
        content.setOrientation(1);
        content.setPadding(dp(16), 0, dp(16), 0);
        box.addView(content);

        int nAlbums = page.getAlbums() == null ? 0 : page.getAlbums().size();
        android.widget.TextView sub = new android.widget.TextView(this);
        sub.setText(tracks.size() + " " + plural(tracks.size(), "трек", "трека", "треков")
                + (nAlbums > 0
                    ? " · " + nAlbums + " " + plural(nAlbums, "релиз", "релиза", "релизов")
                    : ""));
        sub.setTextColor(0xFF8A8A8A);
        sub.setTextSize(13.0f);
        sub.setGravity(android.view.Gravity.CENTER);
        sub.setPadding(0, dp(10), 0, 0);
        content.addView(sub);

        // Слушать
        android.widget.TextView listen = new android.widget.TextView(this);
        listen.setText("Слушать");
        listen.setTextColor(0xFF000000);
        listen.setTextSize(15.0f);
        listen.setTypeface(listen.getTypeface(), 1);
        listen.setGravity(android.view.Gravity.CENTER);
        listen.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_listen));
        android.widget.LinearLayout.LayoutParams llp =
                new android.widget.LinearLayout.LayoutParams(dp(168), dp(46));
        llp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        llp.topMargin = dp(16);
        listen.setOnClickListener(v -> {
            if (tracks.isEmpty()) {
                showPill("Треков не нашлось");
                return;
            }
            playTrack(tracks.get(0), tracks, false);
            showPill("Играет: " + page.getName());
        });
        content.addView(listen, llp);

        // добавить/убрать артиста
        final boolean[] inLib = {page.getInLibrary()};
        final android.widget.TextView libBtn = new android.widget.TextView(this);
        libBtn.setText(inLib[0] ? "В библиотеке" : "Добавить в библиотеку");
        libBtn.setTextColor(inLib[0] ? 0xFF000000 : 0xFFEBEBEB);
        libBtn.setTextSize(13.5f);
        libBtn.setTypeface(libBtn.getTypeface(), 1);
        libBtn.setGravity(android.view.Gravity.CENTER);
        libBtn.setSingleLine(true);
        libBtn.setPadding(dp(24), 0, dp(24), 0);
        libBtn.setTextSize(13.0f);
        libBtn.setBackgroundResource(inLib[0]
                ? su.gwarden.music.R.drawable.bg_listen
                : su.gwarden.music.R.drawable.bg_pill_outline);
        android.widget.LinearLayout.LayoutParams blp =
                new android.widget.LinearLayout.LayoutParams(-2, dp(40));
        blp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        blp.topMargin = dp(10);
        libBtn.setOnClickListener(v -> {
            final java.lang.String src = page.getSource() == null ? "" : page.getSource();
            final java.lang.String pid = page.getId() == null ? "" : page.getId();
            final java.lang.String nm = page.getName() == null ? "" : page.getName();
            final java.lang.String cv = page.getCover() == null ? "" : page.getCover();
            if (inLib[0]) {
                su.gwarden.music.api.ApiClient.INSTANCE.artistRemove(src, pid, nm, (ok, err) -> {
                    if (err != null) {
                        showPill("Не убралось: " + err);
                        return kotlin.Unit.INSTANCE;
                    }
                    inLib[0] = false;
                    libBtn.setText("Добавить в библиотеку");
                    libBtn.setTextColor(0xFFEBEBEB);
                    libBtn.setBackgroundResource(su.gwarden.music.R.drawable.bg_pill_outline);
                    this.myArtists = null;
                    showPill("Убрали из «Ваших артистов»");
                    return kotlin.Unit.INSTANCE;
                });
            } else {
                su.gwarden.music.api.ApiClient.INSTANCE.artistAdd(src, pid, nm, cv, (ok, err) -> {
                    if (err != null) {
                        showPill("Не добавилось: " + err);
                        return kotlin.Unit.INSTANCE;
                    }
                    inLib[0] = true;
                    libBtn.setText("В библиотеке");
                    libBtn.setTextColor(0xFF000000);
                    libBtn.setBackgroundResource(su.gwarden.music.R.drawable.bg_listen);
                    this.myArtists = null;
                    showPill("Добавили в «Ваши артисты»");
                    return kotlin.Unit.INSTANCE;
                });
            }
        });
        content.addView(libBtn, blp);

        // последний релиз
        su.gwarden.music.api.ApiClient.ArtistAlbum last = page.getLast();
        if (last != null && last.getId() != null && !last.getId().isEmpty()) {
            content.addView(settingsSection("ПОСЛЕДНИЙ РЕЛИЗ"));
            android.widget.LinearLayout row = new android.widget.LinearLayout(this);
            row.setOrientation(0);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(dp(6), dp(8), dp(6), dp(8));
            row.setBackgroundResource(su.gwarden.music.R.drawable.bg_row);
            android.widget.ImageView c = new android.widget.ImageView(this);
            int cs = dp(64);
            android.widget.LinearLayout.LayoutParams clp =
                    new android.widget.LinearLayout.LayoutParams(cs, cs);
            clp.setMarginEnd(dp(14));
            c.setLayoutParams(clp);
            c.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_cover));
            c.setClipToOutline(true);
            c.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            if (last.getCover() != null && !last.getCover().isEmpty()) {
                com.bumptech.glide.Glide.with(this).load(last.getCover()).centerCrop().into(c);
            }
            row.addView(c);
            android.widget.LinearLayout col = new android.widget.LinearLayout(this);
            col.setOrientation(1);
            col.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, -2, 1.0f));
            android.widget.TextView t1 = new android.widget.TextView(this);
            t1.setText(last.getTitle());
            t1.setTextColor(-1);
            t1.setTextSize(16.0f);
            t1.setTypeface(t1.getTypeface(), 1);
            android.widget.TextView t2 = new android.widget.TextView(this);
            t2.setText((last.getYear() > 0 ? last.getYear() + " · " : "")
                    + (last.getCount() > 0 ? last.getCount() + " треков" : "Релиз"));
            t2.setTextColor(0xFF8A8A8A);
            t2.setTextSize(12.0f);
            t2.setPadding(0, dp(2), 0, 0);
            col.addView(t1);
            col.addView(t2);
            row.addView(col);
            row.setOnClickListener(v -> openCollectionNow(
                    "yandex", "album", last.getId(), last.getTitle()));
            content.addView(row);
        }

        // треки: показываем по 40 и даём догрузить остальные (у активных
        // артистов их сотни - рисовать всё сразу = тормоза на скролле)
        if (!tracks.isEmpty()) {
            content.addView(settingsSection("ТРЕКИ"));
            final android.widget.LinearLayout rowsBox = new android.widget.LinearLayout(this);
            rowsBox.setOrientation(1);
            content.addView(rowsBox);
            final int chunk = 40;
            final int[] shown = {0};
            final android.widget.TextView more = new android.widget.TextView(this);
            more.setText("Показать ещё");
            more.setTextColor(0xFFEBEBEB);
            more.setTextSize(13.5f);
            more.setTypeface(more.getTypeface(), 1);
            more.setGravity(android.view.Gravity.CENTER);
            more.setBackgroundResource(su.gwarden.music.R.drawable.bg_pill_outline);
            more.setOnClickListener(v -> {
                int to = Math.min(shown[0] + chunk, tracks.size());
                for (int i = shown[0]; i < to; i++) {
                    rowsBox.addView(artistTrackRow(tracks.get(i), tracks));
                }
                shown[0] = to;
                more.setVisibility(to < tracks.size()
                        ? android.view.View.VISIBLE : android.view.View.GONE);
            });
            android.widget.LinearLayout.LayoutParams mlp =
                    new android.widget.LinearLayout.LayoutParams(-1, dp(44));
            mlp.topMargin = dp(10);
            content.addView(more, mlp);
            more.performClick();
        }
    }

    private final android.view.View artistTrackRow(
            final su.gwarden.music.model.Track t,
            final java.util.List<su.gwarden.music.model.Track> queue) {
        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
        row.setOrientation(0);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), dp(7), dp(4), dp(7));

        android.widget.ImageView cover = new android.widget.ImageView(this);
        int sz = dp(46);
        android.widget.LinearLayout.LayoutParams clp =
                new android.widget.LinearLayout.LayoutParams(sz, sz);
        clp.setMarginEnd(dp(12));
        cover.setLayoutParams(clp);
        cover.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_cover));
        cover.setClipToOutline(true);
        cover.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        if (t.getArtwork() != null && !t.getArtwork().isEmpty()) {
            com.bumptech.glide.Glide.with(this).load(t.getArtwork()).centerCrop().into(cover);
        }
        row.addView(cover);

        android.widget.LinearLayout col = new android.widget.LinearLayout(this);
        col.setOrientation(1);
        col.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, -2, 1.0f));
        android.widget.TextView title = new android.widget.TextView(this);
        title.setText(t.getTitle());
        title.setTextColor(-1);
        title.setTextSize(14.5f);
        title.setTypeface(title.getTypeface(), 1);
        title.setMaxLines(1);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        android.widget.TextView meta = new android.widget.TextView(this);
        int d = t.getDuration();
        java.lang.String dur = d > 0
                ? ((d / 60) + ":" + (d % 60 < 10 ? "0" : "") + (d % 60))
                : "";
        meta.setText((t.getArtist() == null ? "" : t.getArtist())
                + (dur.isEmpty() ? "" : " · " + dur));
        meta.setTextColor(0xFF8A8A8A);
        meta.setTextSize(12.0f);
        meta.setMaxLines(1);
        meta.setEllipsize(android.text.TextUtils.TruncateAt.END);
        col.addView(title);
        col.addView(meta);
        row.addView(col);

        row.setOnClickListener(v -> playTrack(t, queue, false));
        return row;
    }

    /** Волна по артисту: его треки + похожие, всё играбельное. */
    private final void startArtistWaveByTrack(su.gwarden.music.model.Track t) {
        java.lang.String name = t.getArtist();
        if (name == null || name.trim().isEmpty()) {
            showPill("Не понял артиста");
            return;
        }
        startArtistWave(new su.gwarden.music.ui.RowItem(
                "artist", "soundcloud", "", name.trim(), "Исполнитель", ""));
    }

    private final void startArtistWave(final su.gwarden.music.ui.RowItem artist) {
        showLoading();
        showPill("Волна: " + artist.getTitle());
        su.gwarden.music.api.ApiClient.INSTANCE.artistWave(
                artist.getSource(), artist.getId(), artist.getTitle(), 40, (list, err) -> {
                    hideLoading();
                    if (err != null || list == null || list.isEmpty()) {
                        showPill("Волна: " + (err == null ? "ничего не нашлось" : err));
                        return kotlin.Unit.INSTANCE;
                    }
                    showTracksList("Волна · " + artist.getTitle(), list);
                    playTrack(list.get(0), list, false);
                    return kotlin.Unit.INSTANCE;
                });
    }

    /** Открыть альбом/плейлист: SC - сразу, Яндекс - подбор в фоне. */
    private final void openCollectionNow(java.lang.String source, java.lang.String kind,
                                         java.lang.String id, java.lang.String title) {
        this.importToken++;
        final int token = this.importToken;
        showLoading();
        showPill("Открываю: " + title);
        su.gwarden.music.api.ApiClient.INSTANCE.openCollection(source, kind, id, 60, (res, err) -> {
            if (token != this.importToken) {
                return kotlin.Unit.INSTANCE;
            }
            if (err != null || res == null) {
                hideLoading();
                showPill("Не открылось: " + (err == null ? "пусто" : err));
                return kotlin.Unit.INSTANCE;
            }
            if (res.getReady()) {
                hideLoading();
                showTracksList(res.getTitle(), res.getItems());
                return kotlin.Unit.INSTANCE;
            }
            pollOpen(res.getJob(), res.getTitle(), 0, token);
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void pollOpen(final java.lang.String job, final java.lang.String title,
                                final int attempt, final int token) {
        su.gwarden.music.api.ApiClient.INSTANCE.importJobStatus(job, (st, err) -> {
            if (token != this.importToken) {
                return kotlin.Unit.INSTANCE;
            }
            if (st == null) {
                hideLoading();
                showPill("Не открылось: " + (err == null ? "нет ответа" : err));
                return kotlin.Unit.INSTANCE;
            }
            if ("done".equals(st.getState())) {
                hideLoading();
                showTracksList(
                        st.getTitle() == null || st.getTitle().isEmpty() ? title : st.getTitle(),
                        st.getItems());
            } else if ("error".equals(st.getState())) {
                hideLoading();
                showPill("Не открылось: " + st.getError());
            } else {
                if (attempt % 3 == 0) {
                    showPill("Подбираю треки: " + st.getDone() + "/" + st.getTotal());
                }
                if (attempt < 600) {
                    uiHandler.postDelayed(
                            () -> pollOpen(job, title, attempt + 1, token), 1000L);
                } else {
                    hideLoading();
                    showPill("Слишком долго, попробуй ещё раз");
                }
            }
            return kotlin.Unit.INSTANCE;
        });
    }

    /** Показать произвольный список треков (альбом, волна артиста и т.п.). */
    private final void showTracksList(java.lang.String title,
                                      java.util.List<su.gwarden.music.model.Track> items) {
        if (items == null || items.isEmpty()) {
            showPill("Треков нет");
            return;
        }
        if (this.settingsScroll != null) {
            this.settingsScroll.setVisibility(8);
        }
        if (this.artistScroll != null) {
            this.artistScroll.setVisibility(8);
        }
        if (this.feedScroll != null) {
            this.feedScroll.setVisibility(8);
        }
        hideLib();
        if (this.wavePanel != null) {
            this.wavePanel.setVisibility(8);
        }
        if (this.waveEq != null) {
            this.waveEq.setActive(false);
            this.waveEq.setVisibility(8);
        }
        if (this.recycler != null) {
            this.recycler.setVisibility(0);
            this.recycler.setAdapter(this.adapter);
        }
        this.adapter.submit(items, false);
        // Возврат туда, откуда пришли: из библиотеки — в библиотеку, с главной —
        // на главную, из поиска — в поиск. Раньше здесь жёстко стоял "search",
        // поэтому любой плейлист/список из библиотеки возвращал в поиск.
        showBack(this.tab == null ? "search" : this.tab);
        if (this.hTitle != null) {
            this.hTitle.setText(title);
        }
        if (this.hSub != null) {
            this.hSub.setText(items.size() + " треков");
        }
    }

    private final void startSimilarWave(su.gwarden.music.model.Track seed) {
        this.similarSeed = seed.getId();
        this.similarArtist = seed.getArtist();
        this.similarTitle = seed.getTitle();
        this.similarQueue = kotlin.collections.CollectionsKt.emptyList();
        this.similarEnded = false;
        setTab("my");
        showPill("Волна по треку");
        su.gwarden.music.api.ApiClient.INSTANCE.similar(seed.getId(), seed.getArtist(),
            seed.getTitle(), 30, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda102
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit startSimilarWave$lambda$207;
                startSimilarWave$lambda$207 = su.gwarden.music.MainActivity.startSimilarWave$lambda$207(su.gwarden.music.MainActivity.this, (kotlin.Result) obj);
                return startSimilarWave$lambda$207;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit startSimilarWave$lambda$207(final su.gwarden.music.MainActivity this$0, final kotlin.Result result) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda58
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.startSimilarWave$lambda$207$lambda$206(this$0, result);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void startSimilarWave$lambda$207$lambda$206(su.gwarden.music.MainActivity this$0, kotlin.Result $result) {
        if (this$0.isFinishing() || this$0.isDestroyed()) {
            return;
        }
        java.lang.Object value = mp.MpResult.getValue($result);
        java.lang.Object obj = null;
        if (mp.MpResult.m322isFailureimpl(value)) {
            value = null;
        }
        java.util.List items = (java.util.List) value;
        if (items == null) {
            items = kotlin.collections.CollectionsKt.emptyList();
        }
        if (items.isEmpty()) {
            this$0.clearSimilar();
            android.widget.Toast.makeText(this$0, "Похожих треков не нашлось", 0).show();
            return;
        }
        this$0.similarQueue = items;
        java.lang.Iterable $this$firstOrNull$iv = items;
        java.util.Iterator it = $this$firstOrNull$iv.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            java.lang.Object element$iv = it.next();
            su.gwarden.music.model.Track it2 = (su.gwarden.music.model.Track) element$iv;
            if (((!kotlin.jvm.internal.Intrinsics.areEqual((java.lang.Object) it2.getPlayable(), (java.lang.Object) false) || (kotlin.jvm.internal.Intrinsics.areEqual(it2.getSource(), "vk") && su.gwarden.music.vk.VkLocal.INSTANCE.getReady())) ? 1 : null) != null) {
                obj = element$iv;
                break;
            }
        }
        su.gwarden.music.model.Track track = (su.gwarden.music.model.Track) obj;
        if (track == null) {
            track = (su.gwarden.music.model.Track) kotlin.collections.CollectionsKt.first(items);
        }
        su.gwarden.music.model.Track first = track;
        this$0.playTrack(first, items, true);
    }

    static /* synthetic */ void refillSimilar$default(su.gwarden.music.MainActivity mainActivity, int i, int i2, java.lang.Object obj) {
        if ((i2 & 1) != 0) {
            i = 0;
        }
        mainActivity.refillSimilar(i);
    }

    private final void refillSimilar(final int attempt) {
        final java.lang.String seed = this.similarSeed;
        if (seed == null || this.similarRefilling || isFinishing() || isDestroyed()) {
            return;
        }
        this.similarRefilling = true;
        su.gwarden.music.api.ApiClient.INSTANCE.similar(seed,
            this.similarArtist == null ? "" : this.similarArtist,
            this.similarTitle == null ? "" : this.similarTitle,
            20, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda157
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit refillSimilar$lambda$212;
                refillSimilar$lambda$212 = su.gwarden.music.MainActivity.refillSimilar$lambda$212(su.gwarden.music.MainActivity.this, seed, attempt, (kotlin.Result) obj);
                return refillSimilar$lambda$212;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit refillSimilar$lambda$212(final su.gwarden.music.MainActivity this$0, final java.lang.String $seed, final int $attempt, final kotlin.Result result) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda121
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.refillSimilar$lambda$212$lambda$211(this$0, $seed, result, $attempt);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void refillSimilar$lambda$212$lambda$211(final su.gwarden.music.MainActivity this$0, java.lang.String $seed, kotlin.Result $result, final int $attempt) {
        this$0.similarRefilling = false;
        if (!kotlin.jvm.internal.Intrinsics.areEqual(this$0.similarSeed, $seed) || this$0.isFinishing() || this$0.isDestroyed()) {
            return;
        }
        boolean wasEnded = this$0.similarEnded;
        java.lang.Iterable $this$mapTo$iv = this$0.similarQueue;
        java.util.Collection destination$iv = new java.util.HashSet();
        for (java.lang.Object item$iv : $this$mapTo$iv) {
            su.gwarden.music.model.Track it = (su.gwarden.music.model.Track) item$iv;
            destination$iv.add(it.getId());
        }
        java.util.HashSet taken = (java.util.HashSet) destination$iv;
        java.lang.Object value = mp.MpResult.getValue($result);
        if (mp.MpResult.m322isFailureimpl(value)) {
            value = null;
        }
        java.lang.Iterable iterable = (java.util.List) value;
        if (iterable == null) {
            iterable = kotlin.collections.CollectionsKt.emptyList();
        }
        java.lang.Iterable $this$filter$iv = iterable;
        java.util.Collection destination$iv$iv = new java.util.ArrayList();
        for (java.lang.Object element$iv$iv : $this$filter$iv) {
            su.gwarden.music.model.Track it2 = (su.gwarden.music.model.Track) element$iv$iv;
            if (!taken.contains(it2.getId())) {
                destination$iv$iv.add(element$iv$iv);
            }
        }
        java.util.List fresh = (java.util.List) destination$iv$iv;
        if (!fresh.isEmpty()) {
            this$0.similarQueue = kotlin.collections.CollectionsKt.takeLast(kotlin.collections.CollectionsKt.plus((java.util.Collection) this$0.similarQueue, (java.lang.Iterable) fresh), 150);
            su.gwarden.music.player.PlayerController playerController = this$0.player;
            if (playerController == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                playerController = null;
            }
            playerController.append(fresh);
            this$0.similarEnded = false;
            if (wasEnded) {
                su.gwarden.music.player.PlayerController playerController2 = this$0.player;
                if (playerController2 == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                    playerController2 = null;
                }
                if (playerController2.next(false) == null) {
                    this$0.playTrack((su.gwarden.music.model.Track) kotlin.collections.CollectionsKt.first(fresh), this$0.similarQueue, true);
                    return;
                }
                return;
            }
            return;
        }
        if ($attempt < 4) {
            this$0.ui.postDelayed(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda33
                @Override // java.lang.Runnable
                public final void run() {
                    su.gwarden.music.MainActivity.refillSimilar$lambda$212$lambda$211$lambda$210(this$0, $attempt);
                }
            }, ($attempt * 800) + 1200);
        } else {
            this$0.clearSimilar();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void refillSimilar$lambda$212$lambda$211$lambda$210(su.gwarden.music.MainActivity this$0, int $attempt) {
        this$0.refillSimilar($attempt + 1);
    }

    private final void findRemixes(su.gwarden.music.model.Track track) {
        su.gwarden.music.ui.TrackAdapter trackAdapter = this.adapter;
        if (trackAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter = null;
        }
        trackAdapter.setEditable(false);
        android.view.View view = this.feedScroll;
        if (view == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("feedScroll");
            view = null;
        }
        view.setVisibility(8);
        androidx.recyclerview.widget.RecyclerView recyclerView = this.recycler;
        if (recyclerView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView = null;
        }
        hideSubScreens();
        recyclerView.setVisibility(0);
        android.view.View view2 = this.wavePanel;
        if (view2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePanel");
            view2 = null;
        }
        view2.setVisibility(8);
        su.gwarden.music.ui.EqBackground eqBackground = this.waveEq;
        if (eqBackground == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveEq");
            eqBackground = null;
        }
        eqBackground.setActive(false);
        su.gwarden.music.ui.EqBackground eqBackground2 = this.waveEq;
        if (eqBackground2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveEq");
            eqBackground2 = null;
        }
        eqBackground2.setVisibility(8);
        android.widget.LinearLayout linearLayout = this.searchRow;
        if (linearLayout == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("searchRow");
            linearLayout = null;
        }
        linearLayout.setVisibility(8);
        showBack(kotlin.jvm.internal.Intrinsics.areEqual(this.tab, "lib") ? "lib" : "home");
        android.widget.TextView textView = this.hTitle;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hTitle");
            textView = null;
        }
        textView.setText("Ремиксы");
        android.widget.TextView textView2 = this.hSub;
        if (textView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            textView2 = null;
        }
        textView2.setText("Ищу версии: " + kotlin.text.StringsKt.take(track.getTitle(), 30));
        androidx.recyclerview.widget.RecyclerView recyclerView2 = this.recycler;
        if (recyclerView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView2 = null;
        }
        su.gwarden.music.ui.TrackAdapter trackAdapter2 = this.adapter;
        if (trackAdapter2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter2 = null;
        }
        recyclerView2.setAdapter(trackAdapter2);
        su.gwarden.music.ui.TrackAdapter trackAdapter3 = this.adapter;
        if (trackAdapter3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter3 = null;
        }
        trackAdapter3.submit(kotlin.collections.CollectionsKt.emptyList(), false);
        showLoading();
        su.gwarden.music.api.ApiClient.INSTANCE.remixes(track.getArtist(), track.getTitle(), 80, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda164
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit findRemixes$lambda$216;
                findRemixes$lambda$216 = su.gwarden.music.MainActivity.findRemixes$lambda$216(su.gwarden.music.MainActivity.this, (kotlin.Result) obj);
                return findRemixes$lambda$216;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit findRemixes$lambda$216(final su.gwarden.music.MainActivity this$0, final kotlin.Result result) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda94
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.findRemixes$lambda$216$lambda$215(this$0, result);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void findRemixes$lambda$216$lambda$215(su.gwarden.music.MainActivity this$0, kotlin.Result $result) {
        if (this$0.isFinishing() || this$0.isDestroyed()) {
            return;
        }
        this$0.hideLoading();
        java.lang.Object value = mp.MpResult.getValue($result);
        android.widget.TextView textView = null;
        if (mp.MpResult.m323isSuccessimpl(value)) {
            java.util.List list = (java.util.List) value;
            androidx.recyclerview.widget.RecyclerView recyclerView = this$0.recycler;
            if (recyclerView == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
                recyclerView = null;
            }
            su.gwarden.music.ui.TrackAdapter trackAdapter = this$0.adapter;
            if (trackAdapter == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter = null;
            }
            recyclerView.setAdapter(trackAdapter);
            su.gwarden.music.ui.TrackAdapter trackAdapter2 = this$0.adapter;
            if (trackAdapter2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter2 = null;
            }
            trackAdapter2.submit(list, false);
            android.widget.TextView textView2 = this$0.hSub;
            if (textView2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                textView2 = null;
            }
            textView2.setText("Версий: " + list.size());
            this$0.showEmpty(list.isEmpty() ? "Ремиксов не нашёл" : null);
            if (!list.isEmpty()) {
                su.gwarden.music.api.ApiClient.INSTANCE.prefetch(list, "soundcloud");
            }
        }
        java.lang.Throwable e = (java.lang.Throwable) mp.MpResult.m319exceptionOrNullimpl(value);
        if (e != null) {
            android.widget.TextView textView3 = this$0.hSub;
            if (textView3 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            } else {
                textView = textView3;
            }
            textView.setText("Не удалось получить список");
            java.lang.String message = e.getMessage();
            if (message == null) {
                message = "Ошибка поиска";
            }
            this$0.showEmpty(message);
        }
    }

    private final java.lang.String waveModeSubtitle() {
        java.lang.String it;
        java.lang.String it2 = this.similarTitle;
        return (it2 == null || (it = new java.lang.StringBuilder().append("Волна по ").append(it2).toString()) == null) ? "Моя волна" : it;
    }

    private final void applyWaveMode() {
        java.lang.String str = "";
        android.widget.TextView textView = this.hSub;
        android.widget.TextView textView2 = null;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            textView = null;
        }
        textView.setText(waveModeSubtitle());
        java.lang.String seed = this.similarTitle;
        android.widget.TextView textView3 = this.waveBack;
        if (textView3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveBack");
            textView3 = null;
        }
        textView3.setVisibility(seed != null ? 0 : 8);
        android.widget.TextView textView4 = this.waveHint;
        if (textView4 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveHint");
        } else {
            textView2 = textView4;
        }
        if (seed != null) {
            str = "Похожие на «" + seed + "»";
        }
        textView2.setText(str);
    }

    private final void clearSimilar() {
        this.similarSeed = null;
        this.similarTitle = null;
        this.similarQueue = kotlin.collections.CollectionsKt.emptyList();
        this.similarEnded = false;
        if (kotlin.jvm.internal.Intrinsics.areEqual(this.tab, "my")) {
            applyWaveMode();
        }
    }

    private final void bindWave(su.gwarden.music.model.Track track) {
        if (track == null) {
            return;
        }
        markWaveAdd(track);
        android.widget.TextView textView = this.waveTitle;
        android.widget.ImageView imageView = null;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveTitle");
            textView = null;
        }
        textView.setText(track.getTitle());
        android.widget.TextView textView2 = this.waveArtist;
        if (textView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveArtist");
            textView2 = null;
        }
        textView2.setText(track.getArtist());
        android.widget.TextView textView3 = this.wavePlay;
        if (textView3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePlay");
            textView3 = null;
        }
        su.gwarden.music.player.PlayerController playerController = this.player;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        textView3.setText(playerController.isPlaying() ? "Пауза" : "Слушать");
        java.lang.String artwork = track.getArtwork();
        if (artwork == null || artwork.length() == 0) {
            android.widget.ImageView imageView2 = this.waveCover;
            if (imageView2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveCover");
            } else {
                imageView = imageView2;
            }
            imageView.setImageResource(su.gwarden.music.R.drawable.ic_music_note);
            return;
        }
        su.gwarden.music.ui.ImageLoader imageLoader = su.gwarden.music.ui.ImageLoader.INSTANCE;
        android.widget.ImageView imageView3 = this.waveCover;
        if (imageView3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveCover");
            imageView3 = null;
        }
        imageLoader.load(imageView3, su.gwarden.music.api.ApiClient.INSTANCE.img(track.getArtwork(), 900), 0, 0, null);
        // цвета для «дыма» берём из этой же обложки
        su.gwarden.music.ui.ImageLoader.INSTANCE.bitmap(
            su.gwarden.music.api.ApiClient.INSTANCE.img(track.getArtwork(), 300), 120,
            new kotlin.jvm.functions.Function1<android.graphics.Bitmap, kotlin.Unit>() {
                @Override
                public kotlin.Unit invoke(android.graphics.Bitmap bmp) {
                    if (bmp != null && MainActivity.this.waveEq != null) {
                        MainActivity.this.waveEq.setArtwork(bmp);
                    }
                    return kotlin.Unit.INSTANCE;
                }
            });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void toggleWave() {
        setTab("my");
        su.gwarden.music.player.PlayerController playerController = null;
        if (this.similarSeed != null) {
            su.gwarden.music.player.PlayerController playerController2 = this.player;
            if (playerController2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                playerController2 = null;
            }
            if (playerController2.isPlaying()) {
                su.gwarden.music.player.PlayerController playerController3 = this.player;
                if (playerController3 == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                } else {
                    playerController = playerController3;
                }
                playerController.pause();
                return;
            }
            su.gwarden.music.player.PlayerController playerController4 = this.player;
            if (playerController4 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            } else {
                playerController = playerController4;
            }
            playerController.play();
            return;
        }
        if (!this.waveQueue.isEmpty()) {
            if (this.onWave) {
                su.gwarden.music.player.PlayerController playerController5 = this.player;
                if (playerController5 == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                    playerController5 = null;
                }
                if (playerController5.isPlaying()) {
                    su.gwarden.music.player.PlayerController playerController6 = this.player;
                    if (playerController6 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                    } else {
                        playerController = playerController6;
                    }
                    playerController.pause();
                    return;
                }
            }
            if (!this.onWave) {
                playTrack$default(this, (su.gwarden.music.model.Track) kotlin.collections.CollectionsKt.first((java.util.List) this.waveQueue), this.waveQueue, false, 4, null);
                return;
            }
            su.gwarden.music.player.PlayerController playerController7 = this.player;
            if (playerController7 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            } else {
                playerController = playerController7;
            }
            playerController.play();
            return;
        }
        loadWave(true, false);
    }

    /** Плейлист, созданный человеком в Wonder (лежит на сервере). */
    public final void openAppPlaylist(final su.gwarden.music.model.VkPlaylist pl) {
        if (this.settingsScroll != null) {
            this.settingsScroll.setVisibility(8);
        }
        if (this.feedScroll != null) {
            this.feedScroll.setVisibility(8);
        }
        if (this.recycler != null) {
            this.recycler.setVisibility(0);
            if (this.adapter != null) {
                this.adapter.setEditable(false);
                this.adapter.submit(kotlin.collections.CollectionsKt.emptyList(), false);
                this.adapter.setHeader(playlistHeader(pl, kotlin.collections.CollectionsKt.emptyList()));
                this.recycler.setAdapter(this.adapter);
            }
        }
        hideSubScreens();
        showBack(kotlin.jvm.internal.Intrinsics.areEqual(this.tab, "lib") ? "lib" : "home");
        if (this.hTitle != null) {
            this.hTitle.setText(pl.getTitle());
        }
        if (this.hSub != null) {
            this.hSub.setText(pl.getAuthor() + " · " + pl.getCount() + " треков");
        }
        showLoading();
        su.gwarden.music.api.ApiClient.INSTANCE.appPlaylistTracks(pl.getId(),
            new kotlin.jvm.functions.Function1<java.util.List<su.gwarden.music.model.Track>, kotlin.Unit>() {
                @Override
                public kotlin.Unit invoke(final java.util.List<su.gwarden.music.model.Track> tracks) {
                    runOnUiThread(new java.lang.Runnable() {
                        @Override
                        public void run() {
                            hideLoading();
                            if (adapter != null) {
                                adapter.submit(tracks, false);
                                adapter.setHeader(playlistHeader(pl, tracks));
                            }
                            if (hSub != null) {
                                hSub.setText(pl.getAuthor() + " · " + tracks.size() + " треков");
                            }
                            if (tracks.isEmpty()) {
                                showPill("Плейлист пока пустой");
                            }
                        }
                    });
                    return kotlin.Unit.INSTANCE;
                }
            });
    }

    /** Плейлисты аккаунта в пикере «В плейлист» + создание нового. */
    private final void loadCloudPlaylists(
            final android.widget.LinearLayout box,
            final su.gwarden.music.model.Track track,
            final android.app.Dialog d) {
        su.gwarden.music.api.ApiClient.INSTANCE.appPlaylists(
            new kotlin.jvm.functions.Function1<java.util.List<su.gwarden.music.model.VkPlaylist>, kotlin.Unit>() {
                @Override
                public kotlin.Unit invoke(final java.util.List<su.gwarden.music.model.VkPlaylist> list) {
                    runOnUiThread(new java.lang.Runnable() {
                        @Override
                        public void run() {
                            box.removeAllViews();
                            for (final su.gwarden.music.model.VkPlaylist p : list) {
                                box.addView(playlistPickRow(
                                    p.getTitle(),
                                    p.getCount() + " треков",
                                    p.getCover(),
                                    "cloud" + p.getId(),
                                    new kotlin.jvm.functions.Function0<kotlin.Unit>() {
                                        @Override
                                        public kotlin.Unit invoke() {
                                            su.gwarden.music.api.ApiClient.INSTANCE.addToAppPlaylist(
                                                p.getId(), track,
                                                new kotlin.jvm.functions.Function1<java.lang.Boolean, kotlin.Unit>() {
                                                    @Override
                                                    public kotlin.Unit invoke(java.lang.Boolean ok) {
                                                        runOnUiThread(new java.lang.Runnable() {
                                                            @Override
                                                            public void run() {
                                                                d.dismiss();
                                                                showPill(ok ? "Добавлено в «" + p.getTitle() + "»" : "Не получилось");
                                                            }
                                                        });
                                                        return kotlin.Unit.INSTANCE;
                                                    }
                                                });
                                            return kotlin.Unit.INSTANCE;
                                        }
                                    }));
                            }
                            box.addView(playlistPickRow(
                                "+  Создать плейлист",
                                "его увидят другие слушатели",
                                "",
                                "newcloud",
                                new kotlin.jvm.functions.Function0<kotlin.Unit>() {
                                    @Override
                                    public kotlin.Unit invoke() {
                                        createCloudPlaylist(track, d);
                                        return kotlin.Unit.INSTANCE;
                                    }
                                }));
                        }
                    });
                    return kotlin.Unit.INSTANCE;
                }
            });
    }

    private final void createCloudPlaylist(
            final su.gwarden.music.model.Track track,
            final android.app.Dialog picker) {
        final android.app.Dialog d = new android.app.Dialog(this);
        try {
            su.gwarden.music.ui.Skin.INSTANCE.prepare(d);
        } catch (java.lang.Throwable ignored) {
        }
        d.requestWindowFeature(1);

        final android.widget.FrameLayout outer = new android.widget.FrameLayout(this);
        outer.setPadding(dp(20), dp(8), dp(20), dp(8));

        final android.widget.LinearLayout card = new android.widget.LinearLayout(this);
        card.setOrientation(1);
        card.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_sheet));
        card.setPadding(dp(20), dp(18), dp(20), dp(16));
        outer.addView(card, new android.widget.FrameLayout.LayoutParams(-1, -2));

        android.widget.TextView title = new android.widget.TextView(this);
        title.setText("Новый плейлист");
        title.setTextSize(20.0f);
        title.setTextColor(-1);
        title.setTypeface(title.getTypeface(), 1);
        card.addView(title);

        android.widget.TextView sub = new android.widget.TextView(this);
        sub.setText("Дай название — добавим трек и покажем плейлист другим");
        sub.setTextSize(12.0f);
        sub.setTextColor(0xFF8A8A8A);
        sub.setLineSpacing(dp(2), 1.0f);
        sub.setPadding(0, dp(4), 0, dp(14));
        card.addView(sub);

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Название плейлиста");
        input.setSingleLine(true);
        input.setTextSize(15.0f);
        input.setTextColor(-1);
        input.setHintTextColor(0xFF8A8A8A);
        input.setPadding(dp(16), dp(12), dp(16), dp(12));
        input.setBackgroundResource(su.gwarden.music.R.drawable.bg_field);
        card.addView(input, new android.widget.LinearLayout.LayoutParams(-1, dp(50)));

        final android.widget.TextView err = new android.widget.TextView(this);
        err.setText("Название можно поменять потом");
        err.setTextSize(11.0f);
        err.setTextColor(0xFF6E6E6E);
        err.setPadding(dp(4), dp(8), 0, 0);
        card.addView(err);

        final android.widget.TextView cancel = new android.widget.TextView(this);
        cancel.setText("Отмена");
        cancel.setTextSize(14.5f);
        cancel.setTextColor(0xFFBEBEBE);
        cancel.setGravity(17);
        cancel.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_field));
        cancel.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                d.dismiss();
            }
        });

        final android.widget.TextView create = new android.widget.TextView(this);
        create.setText("Создать");
        create.setTextSize(14.5f);
        create.setTypeface(create.getTypeface(), 1);
        create.setTextColor(0xFF101010);
        create.setGravity(17);
        create.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_btn));

        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
        row.setOrientation(0);
        android.widget.LinearLayout.LayoutParams rowLp =
                new android.widget.LinearLayout.LayoutParams(-1, dp(50));
        rowLp.topMargin = dp(14);
        row.addView(cancel, new android.widget.LinearLayout.LayoutParams(0, -1, 1.0f));
        android.widget.LinearLayout.LayoutParams createLp =
                new android.widget.LinearLayout.LayoutParams(0, -1, 1.4f);
        createLp.leftMargin = dp(10);
        row.addView(create, createLp);
        card.addView(row, rowLp);

        create.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                final String titleText = input.getText().toString().trim();
                if (titleText.isEmpty()) {
                    err.setText("Введи название плейлиста");
                    err.setTextColor(0xFFE06A6A);
                    input.requestFocus();
                    return;
                }
                create.setEnabled(false);
                create.setText("Создаю…");
                err.setTextColor(0xFF6E6E6E);
                err.setText("Создаю плейлист…");
                su.gwarden.music.api.ApiClient.INSTANCE.createPlaylist(titleText, "",
                    new kotlin.jvm.functions.Function1<su.gwarden.music.model.VkPlaylist, kotlin.Unit>() {
                        @Override
                        public kotlin.Unit invoke(final su.gwarden.music.model.VkPlaylist pl) {
                            if (pl == null) {
                                runOnUiThread(new java.lang.Runnable() {
                                    @Override
                                    public void run() {
                                        create.setEnabled(true);
                                        create.setText("Создать");
                                        err.setTextColor(0xFFE06A6A);
                                        err.setText("Не получилось создать — проверь связь");
                                    }
                                });
                                return kotlin.Unit.INSTANCE;
                            }
                            su.gwarden.music.api.ApiClient.INSTANCE.addToAppPlaylist(
                                pl.getId(), track,
                                new kotlin.jvm.functions.Function1<java.lang.Boolean, kotlin.Unit>() {
                                    @Override
                                    public kotlin.Unit invoke(java.lang.Boolean ok) {
                                        runOnUiThread(new java.lang.Runnable() {
                                            @Override
                                            public void run() {
                                                if (ok != null && ok) {
                                                    d.dismiss();
                                                    try {
                                                        picker.dismiss();
                                                    } catch (java.lang.Throwable ignored) {
                                                    }
                                                    showPill("Плейлист «" + pl.getTitle() + "» создан");
                                                } else {
                                                    create.setEnabled(true);
                                                    create.setText("Создать");
                                                    err.setTextColor(0xFFE06A6A);
                                                    err.setText("Плейлист создан, но трек не добавился");
                                                }
                                            }
                                        });
                                        return kotlin.Unit.INSTANCE;
                                    }
                                });
                            return kotlin.Unit.INSTANCE;
                        }
                    });
            }
        });

        d.setContentView(outer);
        android.view.Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
            w.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                    | android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            int width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(40), dp(520));
            w.setLayout(width, -2);
            w.setGravity(17);
            w.setDimAmount(0.6f);
        }
        d.show();
        try {
            su.gwarden.music.ui.Skin.INSTANCE.applyToDialog(d);
        } catch (java.lang.Throwable ignored) {
        }
        input.requestFocus();
        input.postDelayed(new java.lang.Runnable() {
            @Override
            public void run() {
                try {
                    android.view.inputmethod.InputMethodManager imm =
                            (android.view.inputmethod.InputMethodManager)
                                    getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(input, 0);
                    }
                } catch (java.lang.Throwable ignored) {
                }
            }
        }, 160L);
    }

    private final su.gwarden.music.model.VkPlaylist localAsVk(su.gwarden.music.model.LocalPlaylist pl) {
        return new su.gwarden.music.model.VkPlaylist(pl.getId(), "local", pl.getTitle(), pl.getTracks().size(), su.gwarden.music.model.LocalPlaylists.INSTANCE.coverOf(pl), "", "", 0, pl.getDesc(), false, false, "wonder");
    }

    private final void loadPlaylists() {
        su.gwarden.music.ui.TrackAdapter trackAdapter = this.adapter;
        if (trackAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter = null;
        }
        trackAdapter.setEditable(false);
        showLoading();
        java.lang.Iterable $this$map$iv = su.gwarden.music.model.LocalPlaylists.INSTANCE.all();
        java.util.Collection destination$iv$iv = new java.util.ArrayList(kotlin.collections.CollectionsKt.collectionSizeOrDefault($this$map$iv, 10));
        for (java.lang.Object item$iv$iv : $this$map$iv) {
            su.gwarden.music.model.LocalPlaylist it = (su.gwarden.music.model.LocalPlaylist) item$iv$iv;
            destination$iv$iv.add(localAsVk(it));
        }
        final java.util.List local = (java.util.List) destination$iv$iv;
        if (!su.gwarden.music.vk.VkLocal.INSTANCE.getReady()) {
            loadPlaylistsFromServer(local, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda63
                @Override // kotlin.jvm.functions.Function1
                public final java.lang.Object invoke(java.lang.Object obj) {
                    kotlin.Unit loadPlaylists$lambda$224;
                    loadPlaylists$lambda$224 = su.gwarden.music.MainActivity.loadPlaylists$lambda$224(local, MainActivity.this, (java.util.List) obj);
                    return loadPlaylists$lambda$224;
                }
            });
            return;
        }
        java.lang.Thread $this$loadPlaylists_u24lambda_u24223 = new java.lang.Thread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda62
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.loadPlaylists$lambda$222(su.gwarden.music.MainActivity.this, local);
            }
        });
        $this$loadPlaylists_u24lambda_u24223.setDaemon(true);
        $this$loadPlaylists_u24lambda_u24223.setName("vk-playlists");
        $this$loadPlaylists_u24lambda_u24223.start();
    }

    private static final void loadPlaylists$show(java.util.List<su.gwarden.music.model.VkPlaylist> list, su.gwarden.music.MainActivity this$0, java.util.List<su.gwarden.music.model.VkPlaylist> list2) {
        java.util.List all = kotlin.collections.CollectionsKt.plus((java.util.Collection) list, (java.lang.Iterable) list2);
        androidx.recyclerview.widget.RecyclerView recyclerView = this$0.recycler;
        if (recyclerView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView = null;
        }
        su.gwarden.music.ui.PlaylistAdapter playlistAdapter = this$0.plAdapter;
        if (playlistAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("plAdapter");
            playlistAdapter = null;
        }
        recyclerView.setAdapter(playlistAdapter);
        su.gwarden.music.ui.PlaylistAdapter playlistAdapter2 = this$0.plAdapter;
        if (playlistAdapter2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("plAdapter");
            playlistAdapter2 = null;
        }
        playlistAdapter2.submit(all);
        android.widget.TextView textView = this$0.hSub;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            textView = null;
        }
        textView.setText("Плейлистов: " + all.size());
        this$0.showEmpty(all.isEmpty() ? "Плейлистов нет" : null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void loadPlaylists$lambda$222(final su.gwarden.music.MainActivity this$0, final java.util.List $local) {
        java.lang.Object m316constructorimpl;
        try {
            m316constructorimpl = mp.MpResult.m316constructorimpl(su.gwarden.music.vk.VkApi.INSTANCE.myPlaylists(60));
        } catch (java.lang.Throwable th) {
            m316constructorimpl = mp.MpResult.m316constructorimpl(kotlin.ResultKt.createFailure((java.lang.Throwable) th));
        }
        java.util.List emptyList = kotlin.collections.CollectionsKt.emptyList();
        if (mp.MpResult.m322isFailureimpl(m316constructorimpl)) {
            m316constructorimpl = emptyList;
        }
        final java.util.List lists = (java.util.List) m316constructorimpl;
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda75
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.loadPlaylists$lambda$222$lambda$221(this$0, lists, $local);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void loadPlaylists$lambda$222$lambda$221(final su.gwarden.music.MainActivity this$0, java.util.List $lists, final java.util.List $local) {
        if (this$0.isFinishing() || this$0.isDestroyed()) {
            return;
        }
        this$0.hideLoading();
        if (!$lists.isEmpty()) {
            loadPlaylists$show($local, this$0, $lists);
        } else {
            this$0.loadPlaylistsFromServer($local, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda88
                @Override // kotlin.jvm.functions.Function1
                public final java.lang.Object invoke(java.lang.Object obj) {
                    kotlin.Unit loadPlaylists$lambda$222$lambda$221$lambda$220;
                    loadPlaylists$lambda$222$lambda$221$lambda$220 = su.gwarden.music.MainActivity.loadPlaylists$lambda$222$lambda$221$lambda$220($local, this$0, (java.util.List) obj);
                    return loadPlaylists$lambda$222$lambda$221$lambda$220;
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit loadPlaylists$lambda$222$lambda$221$lambda$220(java.util.List $local, su.gwarden.music.MainActivity this$0, java.util.List it) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(it, "it");
        loadPlaylists$show($local, this$0, it);
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit loadPlaylists$lambda$224(java.util.List $local, su.gwarden.music.MainActivity this$0, java.util.List it) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(it, "it");
        loadPlaylists$show($local, this$0, it);
        return kotlin.Unit.INSTANCE;
    }

    private final void loadPlaylistsFromServer(final java.util.List<su.gwarden.music.model.VkPlaylist> local, final kotlin.jvm.functions.Function1<? super java.util.List<su.gwarden.music.model.VkPlaylist>, kotlin.Unit> show) {
        su.gwarden.music.api.ApiClient.INSTANCE.myPlaylists(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda106
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit loadPlaylistsFromServer$lambda$228;
                loadPlaylistsFromServer$lambda$228 = su.gwarden.music.MainActivity.loadPlaylistsFromServer$lambda$228(su.gwarden.music.MainActivity.this, show, local, (kotlin.Result) obj);
                return loadPlaylistsFromServer$lambda$228;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit loadPlaylistsFromServer$lambda$228(final su.gwarden.music.MainActivity this$0, final kotlin.jvm.functions.Function1 $show, final java.util.List $local, final kotlin.Result result) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda61
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.loadPlaylistsFromServer$lambda$228$lambda$227(this$0, result, $show, $local);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void loadPlaylistsFromServer$lambda$228$lambda$227(su.gwarden.music.MainActivity this$0, kotlin.Result $result, kotlin.jvm.functions.Function1 $show, java.util.List $local) {
        this$0.hideLoading();
        java.lang.Object value = mp.MpResult.getValue($result);
        if (mp.MpResult.m323isSuccessimpl(value)) {
            java.util.List lists = (java.util.List) value;
            $show.invoke(lists);
        }
        java.lang.Throwable e = (java.lang.Throwable) mp.MpResult.m319exceptionOrNullimpl(value);
        if (e != null) {
            $show.invoke(kotlin.collections.CollectionsKt.emptyList());
            if ($local.isEmpty()) {
                android.widget.TextView textView = this$0.hSub;
                if (textView == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                    textView = null;
                }
                java.lang.String message = e.getMessage();
                if (message == null) {
                    message = "Нет связи";
                }
                textView.setText(message);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void openPlaylist(final su.gwarden.music.model.VkPlaylist pl) {
        if (this.settingsScroll != null) {
            this.settingsScroll.setVisibility(8);
        }
        if (kotlin.jvm.internal.Intrinsics.areEqual(pl.getOwnerId(), "local")) {
            openLocalPlaylist(pl);
            return;
        }
        if (kotlin.jvm.internal.Intrinsics.areEqual(pl.getOwnerId(), "soundcloud")) {
            openCollectionNow("soundcloud", "playlist", pl.getId(), pl.getTitle());
            return;
        }
        if (kotlin.jvm.internal.Intrinsics.areEqual(pl.getOwnerId(), "w")) {
            openAppPlaylist(pl);
            return;
        }
        su.gwarden.music.ui.TrackAdapter trackAdapter = this.adapter;
        su.gwarden.music.ui.TrackAdapter trackAdapter2 = null;
        if (trackAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter = null;
        }
        trackAdapter.setEditable(false);
        android.view.View view = this.feedScroll;
        if (view == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("feedScroll");
            view = null;
        }
        view.setVisibility(8);
        androidx.recyclerview.widget.RecyclerView recyclerView = this.recycler;
        if (recyclerView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView = null;
        }
        hideSubScreens();
        recyclerView.setVisibility(0);
        showBack(kotlin.jvm.internal.Intrinsics.areEqual(this.tab, "lib") ? "lib" : "home");
        android.widget.TextView textView = this.hTitle;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hTitle");
            textView = null;
        }
        textView.setText(pl.getTitle());
        android.widget.TextView textView2 = this.hSub;
        if (textView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            textView2 = null;
        }
        textView2.setText("Загружаю...");
        showLoading();
        androidx.recyclerview.widget.RecyclerView recyclerView2 = this.recycler;
        if (recyclerView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView2 = null;
        }
        su.gwarden.music.ui.TrackAdapter trackAdapter3 = this.adapter;
        if (trackAdapter3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter3 = null;
        }
        recyclerView2.setAdapter(trackAdapter3);
        su.gwarden.music.ui.TrackAdapter trackAdapter4 = this.adapter;
        if (trackAdapter4 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter4 = null;
        }
        trackAdapter4.submit(kotlin.collections.CollectionsKt.emptyList(), false);
        su.gwarden.music.ui.TrackAdapter trackAdapter5 = this.adapter;
        if (trackAdapter5 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
        } else {
            trackAdapter2 = trackAdapter5;
        }
        trackAdapter2.setHeader(playlistHeader(pl, kotlin.collections.CollectionsKt.emptyList()));
        this.playlistReq++;
        final int token = this.playlistReq;
        if (su.gwarden.music.vk.VkLocal.INSTANCE.getReady()) {
            java.lang.Thread $this$openPlaylist_u24lambda_u24232 = new java.lang.Thread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda158
                @Override // java.lang.Runnable
                public final void run() {
                    su.gwarden.music.MainActivity.openPlaylist$lambda$231(su.gwarden.music.MainActivity.this, pl, token);
                }
            });
            $this$openPlaylist_u24lambda_u24232.setDaemon(true);
            $this$openPlaylist_u24lambda_u24232.setName("vk-playlist");
            $this$openPlaylist_u24lambda_u24232.start();
            return;
        }
        loadPlaylistFromServer(pl, token);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void openPlaylist$lambda$231(final su.gwarden.music.MainActivity this$0, final su.gwarden.music.model.VkPlaylist $pl, final int $token) {
        java.lang.Object m316constructorimpl;
        try {
            m316constructorimpl = mp.MpResult.m316constructorimpl(su.gwarden.music.vk.VkApi.INSTANCE.playlistTracks($pl.getOwnerId(), $pl.getId(), $pl.getAccessKey(), 150));
        } catch (java.lang.Throwable th) {
            m316constructorimpl = mp.MpResult.m316constructorimpl(kotlin.ResultKt.createFailure((java.lang.Throwable) th));
        }
        java.util.List emptyList = kotlin.collections.CollectionsKt.emptyList();
        if (mp.MpResult.m322isFailureimpl(m316constructorimpl)) {
            m316constructorimpl = emptyList;
        }
        final java.util.List tracks = (java.util.List) m316constructorimpl;
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda156
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.openPlaylist$lambda$231$lambda$230($token, this$0, tracks, $pl);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void openPlaylist$lambda$231$lambda$230(int $token, su.gwarden.music.MainActivity this$0, java.util.List $tracks, su.gwarden.music.model.VkPlaylist $pl) {
        if ($token != this$0.playlistReq || this$0.isFinishing() || this$0.isDestroyed()) {
            return;
        }
        if (!$tracks.isEmpty()) {
            this$0.applyPlaylistTracks($pl, $tracks, $token);
        } else {
            this$0.loadPlaylistFromServer($pl, $token);
        }
    }

    private final void loadPlaylistFromServer(final su.gwarden.music.model.VkPlaylist pl, final int token) {
        su.gwarden.music.api.ApiClient.INSTANCE.playlistTracks(pl.getOwnerId(), pl.getId(), pl.getAccessKey(), new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda72
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit loadPlaylistFromServer$lambda$236;
                loadPlaylistFromServer$lambda$236 = su.gwarden.music.MainActivity.loadPlaylistFromServer$lambda$236(su.gwarden.music.MainActivity.this, token, pl, (kotlin.Result) obj);
                return loadPlaylistFromServer$lambda$236;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit loadPlaylistFromServer$lambda$236(final su.gwarden.music.MainActivity this$0, final int $token, final su.gwarden.music.model.VkPlaylist $pl, final kotlin.Result result) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda15
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.loadPlaylistFromServer$lambda$236$lambda$235($token, this$0, result, $pl);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void loadPlaylistFromServer$lambda$236$lambda$235(int $token, su.gwarden.music.MainActivity this$0, kotlin.Result $result, su.gwarden.music.model.VkPlaylist $pl) {
        if ($token != this$0.playlistReq) {
            return;
        }
        this$0.hideLoading();
        java.lang.Object value = mp.MpResult.getValue($result);
        if (mp.MpResult.m323isSuccessimpl(value)) {
            java.util.List tracks = (java.util.List) value;
            this$0.applyPlaylistTracks($pl, tracks, $token);
        }
        java.lang.Throwable e = (java.lang.Throwable) mp.MpResult.m319exceptionOrNullimpl(value);
        if (e != null) {
            android.widget.TextView textView = this$0.hSub;
            if (textView == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                textView = null;
            }
            java.lang.String message = e.getMessage();
            if (message == null) {
                message = "Нет связи";
            }
            textView.setText(message);
        }
    }

    private final void applyPlaylistTracks(su.gwarden.music.model.VkPlaylist pl, java.util.List<su.gwarden.music.model.Track> tracks, int token) {
        if (token != this.playlistReq) {
            return;
        }
        hideLoading();
        java.util.List shown = visible(tracks);
        androidx.recyclerview.widget.RecyclerView recyclerView = this.recycler;
        if (recyclerView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView = null;
        }
        su.gwarden.music.ui.TrackAdapter trackAdapter = this.adapter;
        if (trackAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter = null;
        }
        recyclerView.setAdapter(trackAdapter);
        su.gwarden.music.ui.TrackAdapter trackAdapter2 = this.adapter;
        if (trackAdapter2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter2 = null;
        }
        trackAdapter2.submit(shown, false);
        su.gwarden.music.ui.TrackAdapter trackAdapter3 = this.adapter;
        if (trackAdapter3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter3 = null;
        }
        trackAdapter3.setHeader(playlistHeader(pl, shown));
        android.widget.TextView textView = this.hTitle;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hTitle");
            textView = null;
        }
        textView.setText(pl.getTitle());
        android.widget.TextView textView2 = this.hSub;
        if (textView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            textView2 = null;
        }
        textView2.setText(shown.size() + " треков");
        if (!tracks.isEmpty()) {
            su.gwarden.music.api.ApiClient apiClient = su.gwarden.music.api.ApiClient.INSTANCE;
            java.util.List<su.gwarden.music.model.Track> $this$filter$iv = tracks;
            java.util.Collection destination$iv$iv = new java.util.ArrayList();
            for (java.lang.Object element$iv$iv : $this$filter$iv) {
                su.gwarden.music.model.Track it = (su.gwarden.music.model.Track) element$iv$iv;
                if (kotlin.jvm.internal.Intrinsics.areEqual(it.getSource(), "vk")) {
                    destination$iv$iv.add(element$iv$iv);
                }
            }
            apiClient.prefetch((java.util.List) destination$iv$iv, "vk");
            su.gwarden.music.api.ApiClient apiClient2 = su.gwarden.music.api.ApiClient.INSTANCE;
            java.util.List<su.gwarden.music.model.Track> $this$filter$iv2 = tracks;
            java.util.Collection destination$iv$iv2 = new java.util.ArrayList();
            for (java.lang.Object element$iv$iv2 : $this$filter$iv2) {
                su.gwarden.music.model.Track it2 = (su.gwarden.music.model.Track) element$iv$iv2;
                if (kotlin.jvm.internal.Intrinsics.areEqual(it2.getSource(), "soundcloud")) {
                    destination$iv$iv2.add(element$iv$iv2);
                }
            }
            apiClient2.prefetch((java.util.List) destination$iv$iv2, "soundcloud");
        }
        showEmpty(tracks.isEmpty() ? "Плейлист пуст" : null);
    }

    private final void openLocalPlaylist(su.gwarden.music.model.VkPlaylist pl) {
        if (this.settingsScroll != null) {
            this.settingsScroll.setVisibility(8);
        }
        su.gwarden.music.model.LocalPlaylist local = su.gwarden.music.model.LocalPlaylists.INSTANCE.byId(pl.getId());
        java.util.List tracks = local != null ? local.getTracks() : null;
        if (tracks == null) {
            tracks = kotlin.collections.CollectionsKt.emptyList();
        }
        su.gwarden.music.ui.TrackAdapter trackAdapter = this.adapter;
        if (trackAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter = null;
        }
        trackAdapter.setEditable(false);
        android.view.View view = this.feedScroll;
        if (view == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("feedScroll");
            view = null;
        }
        view.setVisibility(8);
        androidx.recyclerview.widget.RecyclerView recyclerView = this.recycler;
        if (recyclerView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView = null;
        }
        hideSubScreens();
        recyclerView.setVisibility(0);
        android.view.View view2 = this.wavePanel;
        if (view2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("wavePanel");
            view2 = null;
        }
        view2.setVisibility(8);
        su.gwarden.music.ui.EqBackground eqBackground = this.waveEq;
        if (eqBackground == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveEq");
            eqBackground = null;
        }
        eqBackground.setActive(false);
        su.gwarden.music.ui.EqBackground eqBackground2 = this.waveEq;
        if (eqBackground2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("waveEq");
            eqBackground2 = null;
        }
        eqBackground2.setVisibility(8);
        android.widget.LinearLayout linearLayout = this.searchRow;
        if (linearLayout == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("searchRow");
            linearLayout = null;
        }
        linearLayout.setVisibility(8);
        showBack(kotlin.jvm.internal.Intrinsics.areEqual(this.tab, "lib") ? "lib" : "home");
        android.widget.TextView textView = this.hTitle;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hTitle");
            textView = null;
        }
        textView.setText(pl.getTitle());
        android.widget.TextView textView2 = this.hSub;
        if (textView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            textView2 = null;
        }
        textView2.setText(tracks.size() + " треков");
        androidx.recyclerview.widget.RecyclerView recyclerView2 = this.recycler;
        if (recyclerView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView2 = null;
        }
        su.gwarden.music.ui.TrackAdapter trackAdapter2 = this.adapter;
        if (trackAdapter2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter2 = null;
        }
        recyclerView2.setAdapter(trackAdapter2);
        su.gwarden.music.ui.TrackAdapter trackAdapter3 = this.adapter;
        if (trackAdapter3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter3 = null;
        }
        trackAdapter3.submit(tracks, false);
        su.gwarden.music.ui.TrackAdapter trackAdapter4 = this.adapter;
        if (trackAdapter4 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter4 = null;
        }
        trackAdapter4.setHeader(playlistHeader(pl, tracks));
        showEmpty(tracks.isEmpty() ? "Плейлист пуст" : null);
        if (!tracks.isEmpty()) {
            su.gwarden.music.api.ApiClient apiClient = su.gwarden.music.api.ApiClient.INSTANCE;
            java.lang.Iterable $this$filter$iv = tracks;
            java.util.Collection destination$iv$iv = new java.util.ArrayList();
            for (java.lang.Object element$iv$iv : $this$filter$iv) {
                su.gwarden.music.model.Track it = (su.gwarden.music.model.Track) element$iv$iv;
                if (kotlin.jvm.internal.Intrinsics.areEqual(it.getSource(), "vk")) {
                    destination$iv$iv.add(element$iv$iv);
                }
            }
            apiClient.prefetch((java.util.List) destination$iv$iv, "vk");
            su.gwarden.music.api.ApiClient apiClient2 = su.gwarden.music.api.ApiClient.INSTANCE;
            java.lang.Iterable $this$filter$iv2 = tracks;
            java.util.Collection destination$iv$iv2 = new java.util.ArrayList();
            for (java.lang.Object element$iv$iv2 : $this$filter$iv2) {
                su.gwarden.music.model.Track it2 = (su.gwarden.music.model.Track) element$iv$iv2;
                if (kotlin.jvm.internal.Intrinsics.areEqual(it2.getSource(), "soundcloud")) {
                    destination$iv$iv2.add(element$iv$iv2);
                }
            }
            apiClient2.prefetch((java.util.List) destination$iv$iv2, "soundcloud");
        }
    }

    private final android.view.View playlistPickRow(java.lang.String title, java.lang.String subtitle, java.lang.String cover, java.lang.String seed, final kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
        row.setOrientation(0);
        row.setGravity(16);
        row.setPadding(dp(4), dp(8), dp(4), dp(8));
        row.setBackgroundResource(su.gwarden.music.R.drawable.bg_menu_item);
        row.setClickable(true);
        android.widget.ImageView $this$playlistPickRow_u24lambda_u24242 = new android.widget.ImageView(this);
        $this$playlistPickRow_u24lambda_u24242.setLayoutParams(new android.widget.LinearLayout.LayoutParams(dp(46), dp(46)));
        $this$playlistPickRow_u24lambda_u24242.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_cover_big));
        $this$playlistPickRow_u24lambda_u24242.setClipToOutline(true);
        $this$playlistPickRow_u24lambda_u24242.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        su.gwarden.music.ui.PlaylistArt.INSTANCE.into($this$playlistPickRow_u24lambda_u24242, cover, seed, title, dp(46));
        row.addView($this$playlistPickRow_u24lambda_u24242);
        android.widget.LinearLayout txt = new android.widget.LinearLayout(this);
        txt.setOrientation(1);
        txt.setPadding(dp(12), 0, 0, 0);
        android.widget.TextView $this$playlistPickRow_u24lambda_u24244 = new android.widget.TextView(this);
        $this$playlistPickRow_u24lambda_u24244.setText(title);
        $this$playlistPickRow_u24lambda_u24244.setTextSize(14.5f);
        $this$playlistPickRow_u24lambda_u24244.setTextColor(-986896);
        $this$playlistPickRow_u24lambda_u24244.setMaxLines(1);
        $this$playlistPickRow_u24lambda_u24244.setEllipsize(android.text.TextUtils.TruncateAt.END);
        txt.addView($this$playlistPickRow_u24lambda_u24244);
        android.widget.TextView $this$playlistPickRow_u24lambda_u24245 = new android.widget.TextView(this);
        $this$playlistPickRow_u24lambda_u24245.setText(subtitle);
        $this$playlistPickRow_u24lambda_u24245.setTextSize(12.0f);
        $this$playlistPickRow_u24lambda_u24245.setTextColor(-7697782);
        txt.addView($this$playlistPickRow_u24lambda_u24245);
        android.widget.LinearLayout.LayoutParams $this$playlistPickRow_u24lambda_u24246 = new android.widget.LinearLayout.LayoutParams(0, -2);
        $this$playlistPickRow_u24lambda_u24246.weight = 1.0f;
        kotlin.Unit unit = kotlin.Unit.INSTANCE;
        row.addView(txt, $this$playlistPickRow_u24lambda_u24246);
        row.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda154
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                onClick.invoke();
            }
        });
        return row;
    }

    private final android.view.View pickerLabel(java.lang.String text) {
        android.widget.TextView $this$pickerLabel_u24lambda_u24248 = new android.widget.TextView(this);
        $this$pickerLabel_u24lambda_u24248.setText(text);
        $this$pickerLabel_u24lambda_u24248.setTextSize(11.5f);
        $this$pickerLabel_u24lambda_u24248.setTextColor(-8750470);
        $this$pickerLabel_u24lambda_u24248.setPadding(dp(4), dp(14), dp(4), dp(4));
        return $this$pickerLabel_u24lambda_u24248;
    }

    private final void showPlaylistPicker(final su.gwarden.music.model.Track track) {
        android.util.DisplayMetrics dm;
        final android.app.Dialog d = new android.app.Dialog(this);
        su.gwarden.music.ui.Skin.INSTANCE.prepare(d);
        d.requestWindowFeature(1);
        android.widget.LinearLayout $this$showPlaylistPicker_u24lambda_u24249 = new android.widget.LinearLayout(this);
        $this$showPlaylistPicker_u24lambda_u24249.setOrientation(1);
        $this$showPlaylistPicker_u24lambda_u24249.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_sheet));
        $this$showPlaylistPicker_u24lambda_u24249.setPadding(dp(16), dp(16), dp(16), dp(16));
        android.widget.TextView $this$showPlaylistPicker_u24lambda_u24250 = new android.widget.TextView(this);
        $this$showPlaylistPicker_u24lambda_u24250.setText("Добавить в плейлист");
        $this$showPlaylistPicker_u24lambda_u24250.setTextSize(17.0f);
        $this$showPlaylistPicker_u24lambda_u24250.setTextColor(-1);
        $this$showPlaylistPicker_u24lambda_u24250.setTypeface($this$showPlaylistPicker_u24lambda_u24250.getTypeface(), 1);
        $this$showPlaylistPicker_u24lambda_u24250.setPadding(dp(4), 0, dp(4), dp(4));
        $this$showPlaylistPicker_u24lambda_u24249.addView($this$showPlaylistPicker_u24lambda_u24250);
        android.widget.TextView $this$showPlaylistPicker_u24lambda_u24251 = new android.widget.TextView(this);
        $this$showPlaylistPicker_u24lambda_u24251.setText(track.getTitle());
        $this$showPlaylistPicker_u24lambda_u24251.setTextSize(12.5f);
        $this$showPlaylistPicker_u24lambda_u24251.setTextColor(-7566196);
        $this$showPlaylistPicker_u24lambda_u24251.setMaxLines(1);
        $this$showPlaylistPicker_u24lambda_u24251.setEllipsize(android.text.TextUtils.TruncateAt.END);
        $this$showPlaylistPicker_u24lambda_u24251.setPadding(dp(4), 0, dp(4), dp(10));
        $this$showPlaylistPicker_u24lambda_u24249.addView($this$showPlaylistPicker_u24lambda_u24251);
        final android.widget.LinearLayout $this$showPlaylistPicker_u24lambda_u24252 = new android.widget.LinearLayout(this);
        $this$showPlaylistPicker_u24lambda_u24252.setOrientation(1);
        $this$showPlaylistPicker_u24lambda_u24252.addView(pickerLabel("ПЛЕЙЛИСТЫ WONDER"));
        java.util.List local = su.gwarden.music.model.LocalPlaylists.INSTANCE.all();
        if (local.isEmpty()) {
            android.widget.TextView $this$showPlaylistPicker_u24lambda_u24253 = new android.widget.TextView(this);
            $this$showPlaylistPicker_u24lambda_u24253.setText("Пока нет - создай первый");
            $this$showPlaylistPicker_u24lambda_u24253.setTextSize(13.0f);
            $this$showPlaylistPicker_u24lambda_u24253.setTextColor(-8750470);
            $this$showPlaylistPicker_u24lambda_u24253.setPadding(dp(4), dp(6), dp(4), dp(10));
            $this$showPlaylistPicker_u24lambda_u24252.addView($this$showPlaylistPicker_u24lambda_u24253);
        }
        java.util.List $this$forEach$iv = local;
        for (java.lang.Object element$iv : $this$forEach$iv) {
            final su.gwarden.music.model.LocalPlaylist pl = (su.gwarden.music.model.LocalPlaylist) element$iv;
            $this$showPlaylistPicker_u24lambda_u24252.addView(playlistPickRow(pl.getTitle(), pl.getTracks().size() + " треков", su.gwarden.music.model.LocalPlaylists.INSTANCE.coverOf(pl), pl.getId() + pl.getTitle(), new kotlin.jvm.functions.Function0() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda161
                @Override // kotlin.jvm.functions.Function0
                public final java.lang.Object invoke() {
                    kotlin.Unit showPlaylistPicker$lambda$255$lambda$254;
                    showPlaylistPicker$lambda$255$lambda$254 = su.gwarden.music.MainActivity.showPlaylistPicker$lambda$255$lambda$254(pl, track, d, MainActivity.this);
                    return showPlaylistPicker$lambda$255$lambda$254;
                }
            }));
        }
        // плейлисты АККАУНТА (сервер): доступны на любом устройстве
        $this$showPlaylistPicker_u24lambda_u24252.addView(pickerLabel("ПЛЕЙЛИСТЫ В АККАУНТЕ"));
        final android.widget.LinearLayout cloudBox = new android.widget.LinearLayout(this);
        cloudBox.setOrientation(1);
        android.widget.TextView cloudLoad = new android.widget.TextView(this);
        cloudLoad.setText("Загружаю...");
        cloudLoad.setTextSize(12.5f);
        cloudLoad.setTextColor(-8750470);
        cloudLoad.setPadding(dp(4), dp(8), dp(4), dp(8));
        cloudBox.addView(cloudLoad);
        $this$showPlaylistPicker_u24lambda_u24252.addView(cloudBox);
        loadCloudPlaylists(cloudBox, track, d);

        final android.util.DisplayMetrics dm2 = getResources().getDisplayMetrics();
        final int maxH = (int) (dm2.heightPixels * 0.66f);
        if (su.gwarden.music.vk.VkLocal.INSTANCE.getReady() && kotlin.jvm.internal.Intrinsics.areEqual(track.getSource(), "vk")) {
            $this$showPlaylistPicker_u24lambda_u24252.addView(pickerLabel("ПЛЕЙЛИСТЫ VK · СИНХРОНИЗАЦИЯ"));
            final android.widget.LinearLayout $this$showPlaylistPicker_u24lambda_u24256 = new android.widget.LinearLayout(this);
            $this$showPlaylistPicker_u24lambda_u24256.setOrientation(1);
            android.widget.TextView $this$showPlaylistPicker_u24lambda_u24257 = new android.widget.TextView(this);
            $this$showPlaylistPicker_u24lambda_u24257.setText("Загружаю...");
            $this$showPlaylistPicker_u24lambda_u24257.setTextSize(12.5f);
            $this$showPlaylistPicker_u24lambda_u24257.setTextColor(-8750470);
            $this$showPlaylistPicker_u24lambda_u24257.setPadding(dp(4), dp(8), dp(4), dp(8));
            $this$showPlaylistPicker_u24lambda_u24256.addView($this$showPlaylistPicker_u24lambda_u24257);
            $this$showPlaylistPicker_u24lambda_u24252.addView($this$showPlaylistPicker_u24lambda_u24256);
            dm = dm2;
            java.lang.Thread $this$showPlaylistPicker_u24lambda_u24268 = new java.lang.Thread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda162
                @Override // java.lang.Runnable
                public final void run() {
                    su.gwarden.music.MainActivity.showPlaylistPicker$lambda$267(su.gwarden.music.MainActivity.this, $this$showPlaylistPicker_u24lambda_u24256, d, track, $this$showPlaylistPicker_u24lambda_u24252, dm2, maxH);
                }
            });
            $this$showPlaylistPicker_u24lambda_u24268.setDaemon(true);
            $this$showPlaylistPicker_u24lambda_u24268.setName("vk-playlists-pick");
            $this$showPlaylistPicker_u24lambda_u24268.start();
        } else {
            dm = dm2;
        }
        android.widget.ScrollView $this$showPlaylistPicker_u24lambda_u24269 = new android.widget.ScrollView(this);
        $this$showPlaylistPicker_u24lambda_u24269.addView($this$showPlaylistPicker_u24lambda_u24252);
        $this$showPlaylistPicker_u24lambda_u24269.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, -2));
        $this$showPlaylistPicker_u24lambda_u24249.addView($this$showPlaylistPicker_u24lambda_u24269);
        android.widget.TextView $this$showPlaylistPicker_u24lambda_u24272 = new android.widget.TextView(this);
        $this$showPlaylistPicker_u24lambda_u24272.setText("+  Новый плейлист");
        $this$showPlaylistPicker_u24lambda_u24272.setGravity(17);
        $this$showPlaylistPicker_u24lambda_u24272.setTextSize(14.5f);
        $this$showPlaylistPicker_u24lambda_u24272.setTypeface($this$showPlaylistPicker_u24lambda_u24272.getTypeface(), 1);
        $this$showPlaylistPicker_u24lambda_u24272.setTextColor(-16119286);
        $this$showPlaylistPicker_u24lambda_u24272.setBackground(getDrawable(su.gwarden.music.R.drawable.bg_btn));
        android.widget.LinearLayout.LayoutParams $this$showPlaylistPicker_u24lambda_u24272_u24lambda_u24270 = new android.widget.LinearLayout.LayoutParams(-1, dp(48));
        $this$showPlaylistPicker_u24lambda_u24272_u24lambda_u24270.topMargin = dp(14);
        $this$showPlaylistPicker_u24lambda_u24272.setLayoutParams($this$showPlaylistPicker_u24lambda_u24272_u24lambda_u24270);
        $this$showPlaylistPicker_u24lambda_u24272.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda163
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showPlaylistPicker$lambda$272$lambda$271(d, MainActivity.this, track, view);
            }
        });
        $this$showPlaylistPicker_u24lambda_u24249.addView($this$showPlaylistPicker_u24lambda_u24272);
        android.widget.FrameLayout $this$showPlaylistPicker_u24lambda_u24273 = new android.widget.FrameLayout(this);
        $this$showPlaylistPicker_u24lambda_u24273.setPadding(dp(12), dp(12), dp(12), dp(22));
        $this$showPlaylistPicker_u24lambda_u24273.addView($this$showPlaylistPicker_u24lambda_u24249);
        d.setContentView($this$showPlaylistPicker_u24lambda_u24273);
        android.view.Window window = d.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        }
        d.show();
        try { su.gwarden.music.ui.Skin.INSTANCE.applyToDialog(d); } catch (java.lang.Throwable ignored) {}
        android.view.Window window2 = d.getWindow();
        if (window2 != null) {
            window2.setGravity(81);
        }
        android.view.Window window3 = d.getWindow();
        if (window3 != null) {
            window3.setDimAmount(0.5f);
        }
        showPlaylistPicker$fit(this, $this$showPlaylistPicker_u24lambda_u24252, track, d, dm, maxH);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit showPlaylistPicker$lambda$255$lambda$254(su.gwarden.music.model.LocalPlaylist $pl, su.gwarden.music.model.Track $track, android.app.Dialog $d, su.gwarden.music.MainActivity this$0) {
        java.lang.String str;
        boolean added = su.gwarden.music.model.LocalPlaylists.INSTANCE.addTrack($pl.getId(), $track);
        $d.dismiss();
        if (added) {
            str = "Добавлено в «" + $pl.getTitle() + "»";
        } else {
            str = "Уже есть в «" + $pl.getTitle() + "»";
        }
        this$0.showPill(str);
        if (kotlin.jvm.internal.Intrinsics.areEqual(this$0.tab, "lib") && this$0.backTab == null) {
            this$0.loadPlaylists();
        }
        return kotlin.Unit.INSTANCE;
    }

    private static final void showPlaylistPicker$fit(su.gwarden.music.MainActivity this$0, android.widget.LinearLayout col, su.gwarden.music.model.Track $track, android.app.Dialog d, android.util.DisplayMetrics dm, int maxH) {
        int i;
        int want = this$0.dp(150) + (col.getChildCount() * this$0.dp(58)) + ((su.gwarden.music.vk.VkLocal.INSTANCE.getReady() && kotlin.jvm.internal.Intrinsics.areEqual($track.getSource(), "vk")) ? this$0.dp(40) : 0);
        android.view.Window window = d.getWindow();
        if (window != null) {
            int i2 = dm.widthPixels;
            if (want < maxH) {
                i = -2;
            } else {
                i = maxH;
            }
            window.setLayout(i2, i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPlaylistPicker$lambda$267(final su.gwarden.music.MainActivity this$0, final android.widget.LinearLayout $vkBox, final android.app.Dialog $d, final su.gwarden.music.model.Track $track, final android.widget.LinearLayout $col, final android.util.DisplayMetrics $dm, final int $maxH) {
        java.lang.Object m316constructorimpl;
        try {
            m316constructorimpl = mp.MpResult.m316constructorimpl(su.gwarden.music.vk.VkApi.INSTANCE.myPlaylists(60));
        } catch (java.lang.Throwable th) {
            m316constructorimpl = mp.MpResult.m316constructorimpl(kotlin.ResultKt.createFailure((java.lang.Throwable) th));
        }
        java.util.List emptyList = kotlin.collections.CollectionsKt.emptyList();
        if (mp.MpResult.m322isFailureimpl(m316constructorimpl)) {
            m316constructorimpl = emptyList;
        }
        final java.util.List lists = (java.util.List) m316constructorimpl;
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda104
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.showPlaylistPicker$lambda$267$lambda$266(this$0, $vkBox, lists, $d, $track, $col, $dm, $maxH);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPlaylistPicker$lambda$267$lambda$266(final su.gwarden.music.MainActivity this$0, android.widget.LinearLayout $vkBox, java.util.List $lists, final android.app.Dialog $d, final su.gwarden.music.model.Track $track, android.widget.LinearLayout $col, android.util.DisplayMetrics $dm, int $maxH) {
        if (!this$0.isFinishing() && !this$0.isDestroyed()) {
            $vkBox.removeAllViews();
            if ($lists.isEmpty()) {
                android.widget.TextView $this$showPlaylistPicker_u24lambda_u24267_u24lambda_u24266_u24lambda_u24259 = new android.widget.TextView(this$0);
                $this$showPlaylistPicker_u24lambda_u24267_u24lambda_u24266_u24lambda_u24259.setText("Не удалось получить");
                $this$showPlaylistPicker_u24lambda_u24267_u24lambda_u24266_u24lambda_u24259.setTextSize(12.5f);
                $this$showPlaylistPicker_u24lambda_u24267_u24lambda_u24266_u24lambda_u24259.setTextColor(-8750470);
                $this$showPlaylistPicker_u24lambda_u24267_u24lambda_u24266_u24lambda_u24259.setPadding(this$0.dp(4), this$0.dp(8), this$0.dp(4), this$0.dp(8));
                $vkBox.addView($this$showPlaylistPicker_u24lambda_u24267_u24lambda_u24266_u24lambda_u24259);
                return;
            }
            java.util.List $this$forEach$iv = $lists;
            for (java.lang.Object element$iv : $this$forEach$iv) {
                final su.gwarden.music.model.VkPlaylist vpl = (su.gwarden.music.model.VkPlaylist) element$iv;
                $vkBox.addView(this$0.playlistPickRow(vpl.getTitle(), vpl.getCount() + " треков · VK", vpl.getCover(), vpl.getId() + vpl.getOwnerId() + vpl.getTitle(), new kotlin.jvm.functions.Function0() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda5
                    @Override // kotlin.jvm.functions.Function0
                    public final java.lang.Object invoke() {
                        kotlin.Unit showPlaylistPicker$lambda$267$lambda$266$lambda$265$lambda$264;
                        showPlaylistPicker$lambda$267$lambda$266$lambda$265$lambda$264 = su.gwarden.music.MainActivity.showPlaylistPicker$lambda$267$lambda$266$lambda$265$lambda$264($d, this$0, vpl, $track);
                        return showPlaylistPicker$lambda$267$lambda$266$lambda$265$lambda$264;
                    }
                }));
            }
            showPlaylistPicker$fit(this$0, $col, $track, $d, $dm, $maxH);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit showPlaylistPicker$lambda$267$lambda$266$lambda$265$lambda$264(android.app.Dialog $d, final su.gwarden.music.MainActivity this$0, final su.gwarden.music.model.VkPlaylist $vpl, final su.gwarden.music.model.Track $track) {
        $d.dismiss();
        java.lang.Thread $this$showPlaylistPicker_u24lambda_u24267_u24lambda_u24266_u24lambda_u24265_u24lambda_u24264_u24lambda_u24263 = new java.lang.Thread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda89
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.showPlaylistPicker$lambda$267$lambda$266$lambda$265$lambda$264$lambda$262(this$0, $vpl, $track);
            }
        });
        $this$showPlaylistPicker_u24lambda_u24267_u24lambda_u24266_u24lambda_u24265_u24lambda_u24264_u24lambda_u24263.setDaemon(true);
        $this$showPlaylistPicker_u24lambda_u24267_u24lambda_u24266_u24lambda_u24265_u24lambda_u24264_u24lambda_u24263.setName("vk-add");
        $this$showPlaylistPicker_u24lambda_u24267_u24lambda_u24266_u24lambda_u24265_u24lambda_u24264_u24lambda_u24263.start();
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPlaylistPicker$lambda$267$lambda$266$lambda$265$lambda$264$lambda$262(final su.gwarden.music.MainActivity this$0, final su.gwarden.music.model.VkPlaylist $vpl, su.gwarden.music.model.Track $track) {
        java.lang.Object m316constructorimpl;
        try {
            m316constructorimpl = mp.MpResult.m316constructorimpl(java.lang.Boolean.valueOf(su.gwarden.music.vk.VkApi.INSTANCE.addToPlaylist($vpl.getOwnerId(), $vpl.getId(), $track.getId(), $track.getAccessKey())));
        } catch (java.lang.Throwable th) {
            m316constructorimpl = mp.MpResult.m316constructorimpl(kotlin.ResultKt.createFailure((java.lang.Throwable) th));
        }
        if (mp.MpResult.m322isFailureimpl(m316constructorimpl)) {
            m316constructorimpl = false;
        }
        final boolean ok = ((java.lang.Boolean) m316constructorimpl).booleanValue();
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda108
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.showPlaylistPicker$lambda$267$lambda$266$lambda$265$lambda$264$lambda$262$lambda$261(this$0, ok, $vpl);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPlaylistPicker$lambda$267$lambda$266$lambda$265$lambda$264$lambda$262$lambda$261(su.gwarden.music.MainActivity this$0, boolean $ok, su.gwarden.music.model.VkPlaylist $vpl) {
        java.lang.String str;
        if ($ok) {
            str = "Добавлено в VK «" + $vpl.getTitle() + "»";
        } else {
            str = "VK не дал добавить";
        }
        this$0.showPill(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPlaylistPicker$lambda$272$lambda$271(android.app.Dialog $d, su.gwarden.music.MainActivity this$0, su.gwarden.music.model.Track $track, android.view.View it) {
        $d.dismiss();
        this$0.showNewPlaylistDialog($track);
    }

    private final void showNewPlaylistDialog(final su.gwarden.music.model.Track track) {
        final android.app.Dialog d = new android.app.Dialog(this);
        su.gwarden.music.ui.Skin.INSTANCE.prepare(d);
        d.requestWindowFeature(1);
        android.view.View root = getLayoutInflater().inflate(su.gwarden.music.R.layout.dialog_new_playlist, (android.view.ViewGroup) null);
        final android.widget.ImageView cover = (android.widget.ImageView) root.findViewById(su.gwarden.music.R.id.np_cover);
        final android.widget.EditText title = (android.widget.EditText) root.findViewById(su.gwarden.music.R.id.np_title);
        final android.widget.EditText desc = (android.widget.EditText) root.findViewById(su.gwarden.music.R.id.np_desc);
        android.widget.TextView count = (android.widget.TextView) root.findViewById(su.gwarden.music.R.id.np_count);
        android.widget.LinearLayout list = (android.widget.LinearLayout) root.findViewById(su.gwarden.music.R.id.np_tracks);
        final kotlin.jvm.internal.Ref.ObjectRef pickedCover = new kotlin.jvm.internal.Ref.ObjectRef();
        su.gwarden.music.ui.PlaylistArt playlistArt = su.gwarden.music.ui.PlaylistArt.INSTANCE;
        kotlin.jvm.internal.Intrinsics.checkNotNull(cover);
        java.lang.String artwork = track.getArtwork();
        if (artwork == null) {
            artwork = "";
        }
        playlistArt.into(cover, artwork, "new" + java.lang.System.currentTimeMillis(), "Новый", dp(androidx.core.location.LocationRequestCompat.QUALITY_LOW_POWER));
        cover.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda65
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNewPlaylistDialog$lambda$275(su.gwarden.music.MainActivity.this, pickedCover, cover, view);
            }
        });
        count.setText("1 трек");
        list.addView(showNewPlaylistDialog$row(this, track));
        ((android.widget.TextView) root.findViewById(su.gwarden.music.R.id.np_create)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda67
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNewPlaylistDialog$lambda$277(title, desc, pickedCover, track, d, MainActivity.this, view);
            }
        });
        android.widget.FrameLayout wrap = new android.widget.FrameLayout(this);
        wrap.setPadding(dp(12), dp(12), dp(12), dp(22));
        wrap.addView(root);
        d.setContentView(wrap);
        android.view.Window window = d.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        }
        d.show();
        try { su.gwarden.music.ui.Skin.INSTANCE.applyToDialog(d); } catch (java.lang.Throwable ignored) {}
        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        android.view.Window window2 = d.getWindow();
        if (window2 != null) {
            window2.setLayout(dm.widthPixels, -2);
        }
        android.view.Window window3 = d.getWindow();
        if (window3 != null) {
            window3.setGravity(81);
        }
        android.view.Window window4 = d.getWindow();
        if (window4 != null) {
            window4.setDimAmount(0.5f);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNewPlaylistDialog$lambda$275(final su.gwarden.music.MainActivity this$0, final kotlin.jvm.internal.Ref.ObjectRef $pickedCover, final android.widget.ImageView $cover, android.view.View it) {
        this$0.pickCover(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda70
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit showNewPlaylistDialog$lambda$275$lambda$274;
                showNewPlaylistDialog$lambda$275$lambda$274 = su.gwarden.music.MainActivity.showNewPlaylistDialog$lambda$275$lambda$274($pickedCover, $cover, this$0, (java.lang.String) obj);
                return showNewPlaylistDialog$lambda$275$lambda$274;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public static final kotlin.Unit showNewPlaylistDialog$lambda$275$lambda$274(kotlin.jvm.internal.Ref.ObjectRef $pickedCover, android.widget.ImageView $cover, su.gwarden.music.MainActivity this$0, java.lang.String path) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(path, "path");
        $pickedCover.element = path;
        su.gwarden.music.ui.PlaylistArt playlistArt = su.gwarden.music.ui.PlaylistArt.INSTANCE;
        kotlin.jvm.internal.Intrinsics.checkNotNull($cover);
        playlistArt.into($cover, path, "new", "Новый", this$0.dp(androidx.core.location.LocationRequestCompat.QUALITY_LOW_POWER));
        return kotlin.Unit.INSTANCE;
    }

    private static final android.view.View showNewPlaylistDialog$row(su.gwarden.music.MainActivity this$0, su.gwarden.music.model.Track t) {
        android.widget.TextView $this$showNewPlaylistDialog_u24row_u24lambda_u24276 = new android.widget.TextView(this$0);
        $this$showNewPlaylistDialog_u24row_u24lambda_u24276.setText(t.getArtist() + " — " + t.getTitle());
        $this$showNewPlaylistDialog_u24row_u24lambda_u24276.setTextSize(12.5f);
        $this$showNewPlaylistDialog_u24row_u24lambda_u24276.setMaxLines(1);
        $this$showNewPlaylistDialog_u24row_u24lambda_u24276.setEllipsize(android.text.TextUtils.TruncateAt.END);
        $this$showNewPlaylistDialog_u24row_u24lambda_u24276.setTextColor(-4342339);
        $this$showNewPlaylistDialog_u24row_u24lambda_u24276.setPadding(this$0.dp(2), this$0.dp(6), this$0.dp(2), this$0.dp(6));
        return $this$showNewPlaylistDialog_u24row_u24lambda_u24276;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public static final void showNewPlaylistDialog$lambda$277(android.widget.EditText $title, android.widget.EditText $desc, kotlin.jvm.internal.Ref.ObjectRef $pickedCover, su.gwarden.music.model.Track $track, android.app.Dialog $d, su.gwarden.music.MainActivity this$0, android.view.View it) {
        su.gwarden.music.model.LocalPlaylists localPlaylists = su.gwarden.music.model.LocalPlaylists.INSTANCE;
        java.lang.String obj = $title.getText().toString();
        java.lang.String obj2 = $desc.getText().toString();
        java.lang.String str = (java.lang.String) $pickedCover.element;
        if (str == null && (str = $track.getArtwork()) == null) {
            str = "";
        }
        su.gwarden.music.model.LocalPlaylist pl = localPlaylists.create(obj, obj2, str, kotlin.collections.CollectionsKt.listOf($track));
        $d.dismiss();
        this$0.showPill("Плейлист «" + pl.getTitle() + "» создан");
        if (kotlin.jvm.internal.Intrinsics.areEqual(this$0.tab, "lib")) {
            this$0.loadPlaylists();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void scheduleSearch() {
        java.lang.Runnable it = this.searchJob;
        android.widget.EditText editText = null;
        if (it != null) {
            android.widget.EditText editText2 = this.editQuery;
            if (editText2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("editQuery");
                editText2 = null;
            }
            editText2.removeCallbacks(it);
        }
        java.lang.Runnable job = new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda103
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.this.doSearch();
            }
        };
        this.searchJob = job;
        android.widget.EditText editText3 = this.editQuery;
        if (editText3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("editQuery");
        } else {
            editText = editText3;
        }
        editText.postDelayed(job, 550L);
    }

    /** Пустой поиск: чистим список и показываем подсказку. */
    private final void clearSearchResults() {
        this.searchData = null;
        if (this.adapter != null) {
            this.adapter.submit(java.util.Collections.emptyList(), false);
        }
        if (this.recycler != null) {
            this.recycler.setAdapter(this.adapter);
            this.recycler.setVisibility(0);
        }
        showEmpty("Введите название, артиста или плейлист");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void doSearch() {
        su.gwarden.music.ui.TrackAdapter trackAdapter = this.adapter;
        android.widget.TextView textView = null;
        if (trackAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter = null;
        }
        trackAdapter.setEditable(false);
        android.widget.EditText editText = this.editQuery;
        if (editText == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("editQuery");
            editText = null;
        }
        final java.lang.String q = kotlin.text.StringsKt.trim((java.lang.CharSequence) editText.getText().toString()).toString();
        if (q.length() < 2) {
            return;
        }
        android.widget.TextView textView2 = this.hSub;
        if (textView2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
        } else {
            textView = textView2;
        }
        textView.setText("Ищу: " + q);
        showLoading();

        su.gwarden.music.api.ApiClient.INSTANCE.searchAll(q, (res, err) -> {
            hideLoading();
            if (res == null) {
                showPill("Поиск: " + (err == null ? "нет ответа" : err));
                return kotlin.Unit.INSTANCE;
            }
            this.searchData = res;
            renderSearch();
            return kotlin.Unit.INSTANCE;
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void doSearch$lambda$283(final su.gwarden.music.MainActivity this$0, java.lang.String $q) {
        java.lang.Object m316constructorimpl;
        try {
            m316constructorimpl = mp.MpResult.m316constructorimpl(su.gwarden.music.vk.VkApi.INSTANCE.search($q, 60));
        } catch (java.lang.Throwable th) {
            m316constructorimpl = mp.MpResult.m316constructorimpl(kotlin.ResultKt.createFailure((java.lang.Throwable) th));
        }
        java.util.List emptyList = kotlin.collections.CollectionsKt.emptyList();
        if (mp.MpResult.m322isFailureimpl(m316constructorimpl)) {
            m316constructorimpl = emptyList;
        }
        final java.util.List items = (java.util.List) m316constructorimpl;
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda155
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.doSearch$lambda$283$lambda$282(this$0, items);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void doSearch$lambda$283$lambda$282(su.gwarden.music.MainActivity this$0, java.util.List $items) {
        this$0.hideLoading();
        androidx.recyclerview.widget.RecyclerView recyclerView = this$0.recycler;
        if (recyclerView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
            recyclerView = null;
        }
        su.gwarden.music.ui.TrackAdapter trackAdapter = this$0.adapter;
        if (trackAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter = null;
        }
        recyclerView.setAdapter(trackAdapter);
        su.gwarden.music.ui.TrackAdapter trackAdapter2 = this$0.adapter;
        if (trackAdapter2 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter2 = null;
        }
        trackAdapter2.submit($items, false);
        android.widget.TextView textView = this$0.hSub;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            textView = null;
        }
        textView.setText("Найдено: " + $items.size() + " · VK");
        this$0.showEmpty($items.isEmpty() ? "Ничего не найдено" : null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit doSearch$lambda$288(final su.gwarden.music.MainActivity this$0, final kotlin.Result result) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda69
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.doSearch$lambda$288$lambda$287(this$0, result);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void doSearch$lambda$288$lambda$287(su.gwarden.music.MainActivity this$0, kotlin.Result $result) {
        this$0.hideLoading();
        java.lang.Object value = mp.MpResult.getValue($result);
        android.widget.TextView textView = null;
        if (mp.MpResult.m323isSuccessimpl(value)) {
            java.util.List items = (java.util.List) value;
            androidx.recyclerview.widget.RecyclerView recyclerView = this$0.recycler;
            if (recyclerView == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
                recyclerView = null;
            }
            su.gwarden.music.ui.TrackAdapter trackAdapter = this$0.adapter;
            if (trackAdapter == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter = null;
            }
            recyclerView.setAdapter(trackAdapter);
            su.gwarden.music.ui.TrackAdapter trackAdapter2 = this$0.adapter;
            if (trackAdapter2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter2 = null;
            }
            trackAdapter2.submit(items, false);
            android.widget.TextView textView2 = this$0.hSub;
            if (textView2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                textView2 = null;
            }
            int size = items.size();
            java.lang.String upperCase = this$0.source.toUpperCase(java.util.Locale.ROOT);
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(upperCase, "toUpperCase(...)");
            textView2.setText("Найдено: " + size + " · " + upperCase);
            this$0.showEmpty(items.isEmpty() ? "Ничего не найдено" : null);
            if (!items.isEmpty()) {
                su.gwarden.music.api.ApiClient.INSTANCE.prefetch(items, this$0.source);
            }
        }
        java.lang.Throwable e = (java.lang.Throwable) mp.MpResult.m319exceptionOrNullimpl(value);
        if (e != null) {
            android.widget.TextView textView3 = this$0.hSub;
            if (textView3 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            } else {
                textView = textView3;
            }
            textView.setText("Ошибка: " + e.getMessage());
            this$0.showEmpty("Ошибка поиска");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void toggleSource() {
        this.source = kotlin.jvm.internal.Intrinsics.areEqual(this.source, "vk") ? "soundcloud" : "vk";
        ((android.widget.TextView) findViewById(su.gwarden.music.R.id.source_label)).setText(kotlin.jvm.internal.Intrinsics.areEqual(this.source, "vk") ? "VK" : "SC");
        doSearch();
    }

    private final void startWave() {
        toggleWave();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Multi-variable type inference failed */
    public static /* synthetic */ void playTrack$default(su.gwarden.music.MainActivity mainActivity, su.gwarden.music.model.Track track, java.util.List list, boolean z, int i, java.lang.Object obj) {
        if ((i & 2) != 0) {
            su.gwarden.music.ui.TrackAdapter trackAdapter = mainActivity.adapter;
            if (trackAdapter == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter = null;
            }
            list = trackAdapter.currentQueue();
        }
        if ((i & 4) != 0) {
            z = false;
        }
        mainActivity.playTrack(track, list, z);
    }

    private final void playTrack(su.gwarden.music.model.Track track, java.util.List<su.gwarden.music.model.Track> queue, boolean keepSeed) {
        java.lang.String str;
        if (kotlin.jvm.internal.Intrinsics.areEqual((java.lang.Object) track.getPlayable(), (java.lang.Object) false) && (!kotlin.jvm.internal.Intrinsics.areEqual(track.getSource(), "vk") || !su.gwarden.music.vk.VkLocal.INSTANCE.getReady())) {
            // Раньше здесь был тупик с враньём «Недоступно в вашем регионе».
            // Теперь сразу ищем играбельную версию той же песни: у SoundCloud
            // часть треков только превью, но реаплоады у других людей играют.
            final su.gwarden.music.model.Track orig = track;
            final java.util.List<su.gwarden.music.model.Track> q0 = queue;
            final boolean keep = keepSeed;
            showPill("Ищу другую версию…");
            su.gwarden.music.api.ApiClient.INSTANCE.replace(
                    orig.getArtist(), orig.getTitle(), orig.getDuration(),
                    new kotlin.jvm.functions.Function1<su.gwarden.music.model.Track, kotlin.Unit>() {
                        @Override
                        public kotlin.Unit invoke(su.gwarden.music.model.Track sub) {
                            if (sub == null) {
                                MainActivity.this.showPill("На SoundCloud только превью — подключи VK");
                                return kotlin.Unit.INSTANCE;
                            }
                            java.lang.String cover = orig.getArtwork();
                            if (cover == null || cover.isEmpty()) cover = sub.getArtwork();
                            su.gwarden.music.model.Track rep = new su.gwarden.music.model.Track(
                                    sub.getId(), sub.getSource(), orig.getTitle(), orig.getArtist(),
                                    orig.getDuration() > 0 ? orig.getDuration() : sub.getDuration(),
                                    cover, sub.getPermalink(), java.lang.Boolean.TRUE, null, false,
                                    "", orig.getAuthor(), orig.getMatch(), orig.getDescription(),
                                    false, orig.getMine(), orig.getDate(), false, true, 1.0f, 0);
                            java.util.List<su.gwarden.music.model.Track> q2 =
                                    new java.util.ArrayList<>();
                            for (su.gwarden.music.model.Track x : q0) {
                                if (x.getId().equals(orig.getId())
                                        && x.getSource().equals(orig.getSource())) {
                                    q2.add(rep);
                                } else {
                                    q2.add(x);
                                }
                            }
                            if (q2.isEmpty()) q2.add(rep);
                            MainActivity.this.playTrack(rep, q2, keep);
                            return kotlin.Unit.INSTANCE;
                        }
                    });
            return;
        }
        if (!keepSeed && queue != this.similarQueue) {
            clearSimilar();
        }
        this.onWave = queue == this.waveQueue;
        su.gwarden.music.player.PlayerController playerController = this.player;
        su.gwarden.music.player.PlayerController playerController2 = null;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        if (this.onWave) {
            str = "wave";
        } else if (this.similarSeed != null) {
            str = "similar";
        } else if (kotlin.jvm.internal.Intrinsics.areEqual(this.tab, "search")) {
            str = "search";
        } else {
            str = isMine(track) ? "my" : "playlist";
        }
        playerController.setPlaySource(str);
        su.gwarden.music.ui.TrackAdapter trackAdapter = this.adapter;
        if (trackAdapter == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
            trackAdapter = null;
        }
        trackAdapter.setPlayingId(track.getId());
        java.util.List<su.gwarden.music.model.Track> $this$filter$iv = queue;
        java.util.Collection destination$iv$iv = new java.util.ArrayList();
        for (java.lang.Object element$iv$iv : $this$filter$iv) {
            su.gwarden.music.model.Track it = (su.gwarden.music.model.Track) element$iv$iv;
            if (!kotlin.jvm.internal.Intrinsics.areEqual((java.lang.Object) it.getPlayable(), (java.lang.Object) false) || (kotlin.jvm.internal.Intrinsics.areEqual(it.getSource(), "vk") && su.gwarden.music.vk.VkLocal.INSTANCE.getReady())) {
                destination$iv$iv.add(element$iv$iv);
            }
        }
        java.util.List playableQueue = (java.util.List) destination$iv$iv;
        su.gwarden.music.player.PlayerController playerController3 = this.player;
        if (playerController3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
        } else {
            playerController2 = playerController3;
        }
        playerController2.play(track, playableQueue.isEmpty() ? kotlin.collections.CollectionsKt.listOf(track) : playableQueue);
    }

    private final com.bumptech.glide.load.resource.bitmap.RoundedCorners rounded(int dp) {
        return new com.bumptech.glide.load.resource.bitmap.RoundedCorners((int) (dp * getResources().getDisplayMetrics().density));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        su.gwarden.music.ui.ArtworkTint.INSTANCE.removeListener(this.tintListener);
        try {
            su.gwarden.music.MainActivity $this$onDestroy_u24lambda_u24290 = this;
            $this$onDestroy_u24lambda_u24290.getSharedPreferences("music_player", 0).edit().putBoolean("running", false).apply();
        } catch (java.lang.Throwable th) {
        }
        super.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Code restructure failed: missing block: B:38:0x0097, code lost:
    
        r3.crashedLastRun = false;
        r3.ui.postDelayed(new su.gwarden.music.MainActivity$$ExternalSyntheticLambda31(r3, r1), 900);
     */
    /* JADX WARN: Removed duplicated region for block: B:24:0x006d A[Catch: all -> 0x00ab, TryCatch #1 {all -> 0x00ab, blocks: (B:16:0x0043, B:18:0x005f, B:24:0x006d, B:25:0x00a4, B:29:0x0084, B:31:0x0088, B:33:0x008d, B:38:0x0097), top: B:15:0x0043 }] */
    /* JADX WARN: Removed duplicated region for block: B:29:0x0084 A[Catch: all -> 0x00ab, TryCatch #1 {all -> 0x00ab, blocks: (B:16:0x0043, B:18:0x005f, B:24:0x006d, B:25:0x00a4, B:29:0x0084, B:31:0x0088, B:33:0x008d, B:38:0x0097), top: B:15:0x0043 }] */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onPause() {
        super.onPause();
        try {
            su.gwarden.music.ui.Skin.INSTANCE.pauseBackground(this);
        } catch (java.lang.Throwable ignored) {
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void onResume() {
        super.onResume();
        // оформление могло поменяться в «Внешнем виде» — перечитываем
        try {
            su.gwarden.music.ui.Skin.INSTANCE.apply(this);
            su.gwarden.music.ui.Skin.INSTANCE.resumeBackground(this);
            if (this.tab != null) tintNavIcons(this.tab);
            su.gwarden.music.ui.UpdateBanner.INSTANCE.onResume(this);
        } catch (java.lang.Throwable ignored) {
        }        loadMyAvatar();
        // трек удалили в профиле - лента не должна показывать его из кэша
        try {
            android.content.SharedPreferences sp = getSharedPreferences("music_player", 0);
            if (sp.getBoolean("home_dirty", false)) {
                sp.edit().putBoolean("home_dirty", false).apply();
                if ("home".equals(this.tab)) {
                    loadHome(true);
                }
            }
        } catch (java.lang.Throwable ignored) {
        }
        if (this.tintReady) {
            renderTint(this.tintColor);
        }
        try {
            if (this.player != null && this.player.current() != null
                    && findViewById(su.gwarden.music.R.id.mini).getVisibility() == 0) {
                syncUiFromPlayer(false);
            }
        } catch (java.lang.Throwable ignored) {
        }
        try {
            android.content.SharedPreferences sp = getSharedPreferences("music_player", 0);
            final java.lang.String crash = sp.getString("last_crash", null);
            final java.lang.String state = sp.getString("last_state", null);
            if (crash != null && crash.length() > 0) {
                sp.edit().remove("last_crash").apply();
                // в буфер обмена - ТОЛЬКО в режиме отладки, иначе молча
                if (debugMode()) {
                    this.ui.postDelayed(new java.lang.Runnable() {
                        @Override public void run() {
                            onResume$lambda$294$lambda$292(MainActivity.this, crash);
                        }
                    }, 900L);
                }
            } else if (this.crashedLastRun) {
                if (state != null && state.length() > 0) {
                    this.crashedLastRun = false;
                    if (debugMode()) {
                        this.ui.postDelayed(new java.lang.Runnable() {
                            @Override public void run() {
                                onResume$lambda$294$lambda$293(MainActivity.this, state);
                            }
                        }, 900L);
                    }
                }
            }
        } catch (java.lang.Throwable ignored) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onResume$lambda$294$lambda$292(su.gwarden.music.MainActivity $this_runCatching, java.lang.String $last) {
        $this_runCatching.copyToClipboard("Wonder crash", $last);
        $this_runCatching.showPill("Лог падения скопирован в буфер");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onResume$lambda$294$lambda$293(su.gwarden.music.MainActivity $this_runCatching, java.lang.String $state) {
        $this_runCatching.copyToClipboard("Wonder state", $state);
        $this_runCatching.showPill("Прошлый запуск оборвался · лог в буфере");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private final void pickCover(kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> cb) {
        java.lang.Object m316constructorimpl;
        this.coverCb = cb;
        android.content.Intent intent = new android.content.Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        intent.addCategory("android.intent.category.OPENABLE");
        try {
            su.gwarden.music.MainActivity $this$pickCover_u24lambda_u24296 = this;
            $this$pickCover_u24lambda_u24296.startActivityForResult(intent, $this$pickCover_u24lambda_u24296.REQ_COVER);
            m316constructorimpl = mp.MpResult.m316constructorimpl(kotlin.Unit.INSTANCE);
        } catch (java.lang.Throwable th) {
            m316constructorimpl = mp.MpResult.m316constructorimpl(kotlin.ResultKt.createFailure((java.lang.Throwable) th));
        }
        if (mp.MpResult.m319exceptionOrNullimpl(m316constructorimpl) != null) {
            android.widget.Toast.makeText(this, "Не открылось хранилище", 0).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        final android.net.Uri uri;
        final kotlin.jvm.functions.Function1 cb;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 79) {
            return;   // тихое обновление токена SoundCloud: ничего не показываем
        }
        if (requestCode == 80) {
            if (resultCode == -1) {
                spotifyRunPending();
            } else {
                this.spotifyPending = "";
                showPill("Spotify: вход не завершён");
            }
            return;
        }
        if (requestCode == 81) {
            if (resultCode == -1) {
                java.lang.String a = this.spotifyPending == null ? "" : this.spotifyPending;
                this.spotifyPending = "";
                if (a.endsWith("playlists")) {
                    loadYandexPlaylists();
                } else {
                    startYandexImport("liked", "", "Яндекс · Мне нравится");
                }
            } else {
                this.spotifyPending = "";
                showPill("Яндекс: вход не завершён");
            }
            return;
        }
        if (requestCode == 77) {
            this.vkFixing = false;
            if (resultCode == -1) {
                onLoggedIn("VK подключён");
            } else {
                showPill("VK: нужен вход");
                showLoginScreen();
            }
            return;
        }
        if (requestCode == 78) {
            if (resultCode == -1) {
                showPill("SoundCloud подключён");
            }
        } else {
            if (requestCode != this.REQ_COVER || resultCode != -1 || data == null || (uri = data.getData()) == null || (cb = this.coverCb) == null) {
                return;
            }
            java.lang.Thread $this$onActivityResult_u24lambda_u24304 = new java.lang.Thread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda7
                @Override // java.lang.Runnable
                public final void run() {
                    su.gwarden.music.MainActivity.onActivityResult$lambda$303(su.gwarden.music.MainActivity.this, uri, cb);
                }
            });
            $this$onActivityResult_u24lambda_u24304.setDaemon(true);
            $this$onActivityResult_u24lambda_u24304.setName("cover-copy");
            $this$onActivityResult_u24lambda_u24304.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:41:0x00ac  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final void onActivityResult$lambda$303(final su.gwarden.music.MainActivity this$0, android.net.Uri uri, final kotlin.jvm.functions.Function1 cb) {
        java.lang.String path = null;
        try {
            java.io.File dir = new java.io.File(this$0.getFilesDir(), "covers");
            dir.mkdirs();
            java.io.File f = new java.io.File(dir, "cover_" + System.currentTimeMillis() + ".jpg");
            java.io.InputStream in = this$0.getContentResolver().openInputStream(uri);
            if (in != null) {
                java.io.FileOutputStream out = new java.io.FileOutputStream(f);
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                out.close();
                in.close();
            }
            if (f.length() > 0) path = f.getAbsolutePath();
        } catch (java.lang.Throwable ignored) {
        }
        final java.lang.String p = path;
        this$0.runOnUiThread(new java.lang.Runnable() {
            @Override public void run() {
                onActivityResult$lambda$303$lambda$302(p, cb, this$0);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onActivityResult$lambda$303$lambda$302(java.lang.String $path, kotlin.jvm.functions.Function1 $cb, su.gwarden.music.MainActivity this$0) {
        if ($path != null) {
            $cb.invoke($path);
        } else {
            android.widget.Toast.makeText(this$0, "Не удалось прочитать картинку", 0).show();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private final void updateNextButtons(boolean ready) {
        float a = ready ? 1.0f : 0.3f;
        android.widget.ImageButton[] imageButtonArr = new android.widget.ImageButton[2];
        imageButtonArr[0] = findViewById(su.gwarden.music.R.id.mini_next);
        android.view.View view = this.nowNext;
        imageButtonArr[1] = view instanceof android.widget.ImageButton ? (android.widget.ImageButton) view : null;
        java.lang.Iterable $this$forEach$iv = kotlin.collections.CollectionsKt.listOfNotNull((java.lang.Object[]) imageButtonArr);
        for (java.lang.Object element$iv : $this$forEach$iv) {
            android.widget.ImageButton it = (android.widget.ImageButton) element$iv;
            it.setAlpha(a);
            it.setEnabled(ready);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:45:0x0144  */
    /* JADX WARN: Removed duplicated region for block: B:55:0x014a  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    /* ---------------- цвет приложения из обложки ---------------- */

    private void applyAppTint(int color) {
        // 0 = чб обложка: красим в нейтральный монохром, а не в цвет прошлого трека
        int target = color == 0 ? 0xFF3C3C3C : color;
        if (target == this.tintColor && this.tintReady) {
            return;
        }
        int from = this.tintColor;
        this.tintColor = target;
        boolean first = !this.tintReady;
        this.tintReady = true;
        android.animation.ValueAnimator prev = this.tintAnim;
        if (prev != null) {
            prev.cancel();
        }
        if (first || from == 0) {
            renderTint(target);
            su.gwarden.music.api.ApiClient.INSTANCE.log("tint", "render сразу #" + java.lang.Integer.toHexString(target));
            return;
        }
        final int f = from;
        final int t = target;
        android.animation.ValueAnimator anim = android.animation.ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.setDuration(700L);
        anim.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(android.animation.ValueAnimator a) {
                float k = ((java.lang.Float) a.getAnimatedValue()).floatValue();
                su.gwarden.music.MainActivity.this.renderTint(
                    su.gwarden.music.MainActivity.this.blend(f, t, k));
            }
        });
        this.tintAnim = anim;
        anim.start();
        su.gwarden.music.api.ApiClient.INSTANCE.log("tint", "render анимация #" + java.lang.Integer.toHexString(target));
    }

    private int blend(int a, int b, float t) {
        float k = t < 0.0f ? 0.0f : (t > 1.0f ? 1.0f : t);
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;
        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * k);
        int g = (int) (ag + (bg - ag) * k);
        int bl = (int) (ab + (bb - ab) * k);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    private void renderTint(int c) {
        android.view.View root = findViewById(su.gwarden.music.R.id.app_root);
        if (root != null) {
            // Когда включены обои/GIF/видео — НЕ закрашиваем их непрозрачным
            // градиентом, иначе фон «исчезает» на главной (в плеере он живёт
            // в отдельном окне и поэтому был виден). Здесь только тонировка.
            boolean bgOn = su.gwarden.music.ui.Skin.INSTANCE.isBgActive();
            android.graphics.drawable.GradientDrawable base;
            if (bgOn) {
                int tint = c & 0x00FFFFFF;
                base = new android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{ (0x59 << 24) | tint, (0x33 << 24) | tint, 0xCC000000 });
            } else {
                base = new android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{blend(0xFF000000, c, 0.80f), blend(0xFF000000, c, 0.34f), 0xFF000000});
            }
            android.graphics.drawable.GradientDrawable glow =
                new android.graphics.drawable.GradientDrawable();
            glow.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            glow.setGradientType(android.graphics.drawable.GradientDrawable.RADIAL_GRADIENT);
            glow.setGradientRadius((float) dp(330));
            glow.setGradientCenter(0.5f, 0.0f);
            glow.setColors(new int[]{
                ((bgOn ? 0x22 : 0x3A) << 24) | (c & 0x00FFFFFF),
                (0x00 << 24) | (c & 0x00FFFFFF)});
            android.graphics.drawable.LayerDrawable layer =
                new android.graphics.drawable.LayerDrawable(
                    new android.graphics.drawable.Drawable[]{base, glow});
            layer.setLayerHeight(1, dp(560));
            layer.setLayerGravity(1, android.view.Gravity.TOP);
            root.setBackground(layer);
        }
        tintPanel(findViewById(su.gwarden.music.R.id.nav_wrap), c, 20, 0.36f);
        tintPanel(findViewById(su.gwarden.music.R.id.mini), c, 18, 0.28f);
    }

    private void tintPanel(android.view.View v, int c, int radiusDp, float strength) {
        if (v == null) {
            return;
        }
        android.graphics.drawable.GradientDrawable g =
            new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{blend(0xFF0A0A0A, c, strength), blend(0xFF060606, c, strength * 0.5f)});
        g.setCornerRadius((float) dp(radiusDp));
        g.setStroke(dp(1), 0x14FFFFFF);
        v.setBackground(g);
    }

    /** Аватар в верхней плашке: тот, что человек поставил в профиле. */
    private final void loadMyAvatar() {
        if (!su.gwarden.music.api.ApiClient.INSTANCE.getLoggedIn()) {
            return;
        }
        try {
            su.gwarden.music.api.ApiClient.INSTANCE.profile(0, json -> {
                try {
                    if (json == null || json.isEmpty()) {
                        return kotlin.Unit.INSTANCE;
                    }
                    com.google.gson.JsonObject o = com.google.gson.JsonParser
                            .parseString(json).getAsJsonObject();
                    com.google.gson.JsonElement a = o.get("avatar");
                    java.lang.String av = (a == null || a.isJsonNull()) ? "" : a.getAsString();
                    android.widget.ImageView v = findViewById(su.gwarden.music.R.id.profile_avatar);
                    if (v == null) {
                        return kotlin.Unit.INSTANCE;
                    }
                    if (av != null && !av.isEmpty()) {
                        v.setImageTintList(null);
                        v.setPadding(0, 0, 0, 0);
                        v.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                        su.gwarden.music.ui.ImageLoader.INSTANCE.load(v,
                                su.gwarden.music.api.ApiClient.INSTANCE.img(av, 160), 0,
                                su.gwarden.music.R.drawable.ic_artist,
                                (android.graphics.Bitmap) null);
                    } else {
                        v.setPadding(dp(8), dp(8), dp(8), dp(8));
                        v.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
                        v.setImageResource(su.gwarden.music.R.drawable.ic_artist);
                        v.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFDEDEDE));
                    }
                } catch (java.lang.Throwable ignored) {
                }
                return kotlin.Unit.INSTANCE;
            });
        } catch (java.lang.Throwable ignored) {
        }
    }

    /** Крестик в нижнем баре: остановить трек и убрать плашку. */
    private final void closeCurrentTrack() {
        try {
            su.gwarden.music.player.PlayerController p = this.player;
            if (p != null) {
                p.stop();
            }
        } catch (java.lang.Throwable ignored) {
        }
        hideMiniBar();
        showPill("Остановлено");
    }

    /** Нижняя плашка «сейчас играет» больше не нужна. */
    private final void hideMiniBar() {
        android.view.View m = findViewById(su.gwarden.music.R.id.mini);
        if (m != null) {
            m.setVisibility(8);
        }
        if (this.adapter != null) {
            this.adapter.setPlayingId(null);
        }
        // уведомление о треке тоже убираем
        try {
            su.gwarden.music.player.PlaybackService.Companion.hide(this);
        } catch (java.lang.Throwable ignored) {
        }
    }

    private final void bindTrackUi(final su.gwarden.music.model.Track track) {
        // фон приложения красим в цвет обложки
        su.gwarden.music.ui.ArtworkTint.INSTANCE.applyUrl(
            su.gwarden.music.api.ApiClient.INSTANCE.img(track.getArtwork(), 300));
        ((android.widget.LinearLayout) findViewById(su.gwarden.music.R.id.mini)).setVisibility(0);
        ((android.widget.TextView) findViewById(su.gwarden.music.R.id.mini_title)).setText(track.getTitle());
        ((android.widget.TextView) findViewById(su.gwarden.music.R.id.mini_artist)).setText(track.getArtist());
        android.widget.ImageView art = (android.widget.ImageView) findViewById(su.gwarden.music.R.id.mini_art);
        java.lang.String artwork = track.getArtwork();
        if (artwork == null || artwork.length() == 0) {
            art.setImageResource(su.gwarden.music.R.drawable.ic_music_note);
        } else {
            su.gwarden.music.ui.ImageLoader.INSTANCE.load(art,
                su.gwarden.music.api.ApiClient.INSTANCE.img(artwork, 400), 10,
                su.gwarden.music.R.drawable.ic_music_note, (android.graphics.Bitmap) null);
        }
        ((android.widget.ImageButton) findViewById(su.gwarden.music.R.id.mini_play)).setImageResource(
            this.player.isPlaying() ? su.gwarden.music.R.drawable.ic_pause : su.gwarden.music.R.drawable.ic_play);
        boolean artistOpen = this.artistScroll != null && this.artistScroll.getVisibility() == 0;
        if (!kotlin.jvm.internal.Intrinsics.areEqual(this.tab, "my") && !artistOpen) {
            this.hTitle.setText("Музыка");
            this.hSub.setText("Сейчас играет");
        }
        this.adapter.setPlayingId(track.getId());
        this.adapter.setPlayingState(this.player.isPlaying());
        su.gwarden.music.player.PlaybackService.Companion.show(this, track.getTitle(), track.getArtist(),
            su.gwarden.music.api.ApiClient.INSTANCE.img(track.getArtwork(), 400), this.player.isPlaying());
        refreshAddButtons();
        bindWave(track);
        if (this.onWave) {
            if (this.player.getRemaining() <= 3) {
                refillWave(0);
            } else if (this.similarSeed != null && this.player.getRemaining() <= 3) {
                refillSimilar(0);
            }
        }
        if (this.nowBind != null) {
            this.nowBind.invoke(track);
        }
        bindNowLike(this.nowLike, track);
        loadBeat(track);
        try {
            getSharedPreferences("music_player", 0).edit()
                .putString("last_state", buildReport("СОСТОЯНИЕ", "")).apply();
        } catch (java.lang.Throwable ignored) {
        }
    }

    private final void syncUiFromPlayer(boolean openNow) {
        su.gwarden.music.player.PlayerController playerController = this.player;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        su.gwarden.music.model.Track t = playerController.current();
        if (t == null) {
            return;
        }
        bindTrackUi(t);
        if (openNow) {
            this.ui.post(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda21
                @Override // java.lang.Runnable
                public final void run() {
                    su.gwarden.music.MainActivity.syncUiFromPlayer$lambda$308(su.gwarden.music.MainActivity.this);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void syncUiFromPlayer$lambda$308(su.gwarden.music.MainActivity this$0) {
        try {
            this$0.showNowPlaying();
        } catch (java.lang.Throwable th) {
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        openArtistFromIntent(intent);
        boolean openNow = false;
        if (intent != null && intent.getBooleanExtra(su.gwarden.music.player.PlaybackService.EXTRA_OPEN_NOW, false)) {
            openNow = true;
        }
        syncUiFromPlayer(openNow);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r8v11, types: [java.lang.Runnable] */
    public final void showNowPlaying() {
        su.gwarden.music.player.PlayerController playerController = this.player;
        su.gwarden.music.player.PlayerController playerController2 = null;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        su.gwarden.music.model.Track track = playerController.current();
        if (track == null) {
            return;
        }
        final android.app.Dialog d = new android.app.Dialog(this, su.gwarden.music.R.style.Theme_Wonder_Fullscreen);
        su.gwarden.music.ui.Skin.INSTANCE.prepare(d);
        d.setContentView(su.gwarden.music.R.layout.dialog_now);
        android.view.Window $this$showNowPlaying_u24lambda_u24309 = d.getWindow();
        if ($this$showNowPlaying_u24lambda_u24309 != null) {
            $this$showNowPlaying_u24lambda_u24309.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
            $this$showNowPlaying_u24lambda_u24309.addFlags(2);
            $this$showNowPlaying_u24lambda_u24309.setDimAmount(su.gwarden.music.ui.Skin.INSTANCE.isBgActive() ? 0.22f : 0.45f);
        }
        final android.view.View card = d.findViewById(su.gwarden.music.R.id.now_card);
        final android.view.View coverBox = d.findViewById(su.gwarden.music.R.id.now_cover_box);
        final android.widget.ImageView cover = (android.widget.ImageView) d.findViewById(su.gwarden.music.R.id.now_cover);
        final android.widget.TextView title = (android.widget.TextView) d.findViewById(su.gwarden.music.R.id.now_title);
        final android.widget.TextView artist = (android.widget.TextView) d.findViewById(su.gwarden.music.R.id.now_artist);
        artist.setOnClickListener(new android.view.View.OnClickListener() { // из класса MainActivity
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.model.Track cur = null;
                try {
                    cur = MainActivity.this.player.current();
                } catch (java.lang.Throwable ignored) {
                }
                // свой трек: открываем профиль АВТОРА, а не поиск артиста
                // по имени (иначе кидало на случайного тёзку из SoundCloud)
                if (cur != null && "own".equals(cur.getSource()) && cur.getOwner() > 0) {
                    try {
                        d.dismiss();
                    } catch (java.lang.Throwable ignored) {
                    }
                    try {
                        startActivity(new android.content.Intent(MainActivity.this,
                                su.gwarden.music.ProfileActivity.class)
                                .putExtra("user_id", cur.getOwner()));
                    } catch (java.lang.Throwable ignored) {
                    }
                    return;
                }
                MainActivity.this.showArtistPicker(cur == null ? null : cur.getArtist(), d);
            }
        });
        final su.gwarden.music.ui.ProgressStrip strip = (su.gwarden.music.ui.ProgressStrip) d.findViewById(su.gwarden.music.R.id.now_strip);
        final android.widget.TextView cur = (android.widget.TextView) d.findViewById(su.gwarden.music.R.id.now_cur);
        final android.widget.TextView durTx = (android.widget.TextView) d.findViewById(su.gwarden.music.R.id.now_dur);
        this.nowAdd = d.findViewById(su.gwarden.music.R.id.now_add);
        this.nowNext = d.findViewById(su.gwarden.music.R.id.now_next);
        refreshAddButtons();
        su.gwarden.music.player.PlayerController playerController3 = this.player;
        if (playerController3 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
        } else {
            playerController2 = playerController3;
        }
        updateNextButtons(playerController2.getNextReady());
        final android.widget.ImageButton btnPlay = (android.widget.ImageButton) d.findViewById(su.gwarden.music.R.id.now_play);
        final android.widget.ImageButton btnAdd = (android.widget.ImageButton) d.findViewById(su.gwarden.music.R.id.now_add);
        btnAdd.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda35
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNowPlaying$lambda$310(su.gwarden.music.MainActivity.this, btnAdd, view);
            }
        });
        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        int coverSize = java.lang.Math.min(dm.widthPixels - dp(64), java.lang.Math.min(dp(260), (int) (dm.heightPixels * 0.3f)));
        android.view.ViewGroup.LayoutParams $this$showNowPlaying_u24lambda_u24311 = coverBox.getLayoutParams();
        $this$showNowPlaying_u24lambda_u24311.width = coverSize;
        $this$showNowPlaying_u24lambda_u24311.height = coverSize;
        coverBox.setLayoutParams($this$showNowPlaying_u24lambda_u24311);
        final kotlin.jvm.internal.Ref.ObjectRef bgDrawable = new kotlin.jvm.internal.Ref.ObjectRef();
        showNowPlaying$bind(title, artist, cover, card, this, bgDrawable, strip, cur, durTx, track);
        this.nowBind = new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda45
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit showNowPlaying$lambda$314;
                showNowPlaying$lambda$314 = su.gwarden.music.MainActivity.showNowPlaying$lambda$314(title, artist, cover, card, MainActivity.this, bgDrawable, strip, cur, durTx, (su.gwarden.music.model.Track) obj);
                return showNowPlaying$lambda$314;
            }
        };
        d.findViewById(su.gwarden.music.R.id.now_similar).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda46
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNowPlaying$lambda$315(su.gwarden.music.MainActivity.this, d, view);
            }
        });
        d.findViewById(su.gwarden.music.R.id.now_playlist).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda47
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNowPlaying$lambda$316(su.gwarden.music.MainActivity.this, view);
            }
        });
        d.findViewById(su.gwarden.music.R.id.now_remixes).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda48
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNowPlaying$lambda$317(su.gwarden.music.MainActivity.this, d, view);
            }
        });
        // Звук: 10-полосный эквалайзер и вся обработка поверх играющего трека
        try {
            android.view.View fxBtn = d.findViewById(su.gwarden.music.R.id.now_fx);
            if (fxBtn != null) {
                fxBtn.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public final void onClick(android.view.View view) {
                        su.gwarden.music.ui.FxSheet.INSTANCE.show(
                                MainActivity.this, MainActivity.this.player);
                    }
                });
            }
        } catch (java.lang.Throwable ignored) {
        }
        // лайк / комментарии по таймкодам / текст / скачать
        try {
            su.gwarden.music.ui.SocialUi.INSTANCE.bind(
                this, d, track, this.player,
                new kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit>() {
                    @Override
                    public kotlin.Unit invoke(java.lang.String s) {
                        su.gwarden.music.MainActivity.this.showPill(s);
                        return kotlin.Unit.INSTANCE;
                    }
                });
        } catch (java.lang.Throwable ignored) {
        }
        final kotlin.jvm.internal.Ref.FloatRef sx = new kotlin.jvm.internal.Ref.FloatRef();
        final kotlin.jvm.internal.Ref.FloatRef sy = new kotlin.jvm.internal.Ref.FloatRef();
        coverBox.setOnTouchListener(new android.view.View.OnTouchListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda49
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(android.view.View view, android.view.MotionEvent motionEvent) {
                boolean showNowPlaying$lambda$319;
                showNowPlaying$lambda$319 = su.gwarden.music.MainActivity.showNowPlaying$lambda$319(sx, sy, MainActivity.this, coverBox, title, artist, cover, card, bgDrawable, strip, cur, durTx, view, motionEvent);
                return showNowPlaying$lambda$319;
            }
        });
        ((android.widget.ImageButton) d.findViewById(su.gwarden.music.R.id.now_next_hint)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda50
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNowPlaying$flip(su.gwarden.music.MainActivity.this, coverBox, title, artist, cover, card, bgDrawable, strip, cur, durTx, true);
            }
        });
        ((android.widget.ImageButton) d.findViewById(su.gwarden.music.R.id.now_prev_hint)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda51
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNowPlaying$flip(su.gwarden.music.MainActivity.this, coverBox, title, artist, cover, card, bgDrawable, strip, cur, durTx, false);
            }
        });
        strip.setOnSeek(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda52
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit showNowPlaying$lambda$322;
                showNowPlaying$lambda$322 = su.gwarden.music.MainActivity.showNowPlaying$lambda$322(su.gwarden.music.MainActivity.this, ((java.lang.Float) obj).floatValue());
                return showNowPlaying$lambda$322;
            }
        });
        btnPlay.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda53
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNowPlaying$lambda$323(su.gwarden.music.MainActivity.this, btnPlay, view);
            }
        });
        ((android.widget.ImageButton) d.findViewById(su.gwarden.music.R.id.now_next)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda36
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNowPlaying$flip(su.gwarden.music.MainActivity.this, coverBox, title, artist, cover, card, bgDrawable, strip, cur, durTx, true);
            }
        });
        ((android.widget.ImageButton) d.findViewById(su.gwarden.music.R.id.now_prev)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda37
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNowPlaying$flip(su.gwarden.music.MainActivity.this, coverBox, title, artist, cover, card, bgDrawable, strip, cur, durTx, false);
            }
        });
        // лайк / дизлайк прямо у кнопок управления — сигналы для рекомендаций
        try {
            final su.gwarden.music.model.Track liked = track;
            android.view.View dislikeBtn = d.findViewById(su.gwarden.music.R.id.now_dislike);
            if (dislikeBtn != null) {
                dislikeBtn.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public final void onClick(android.view.View view) {
                        try {
                            MainActivity.this.player.feedbackDislike(liked);
                        } catch (java.lang.Throwable ignored) {
                        }
                        showPill("Не понравилось — учту");
                        try {
                            d.dismiss();
                        } catch (java.lang.Throwable ignored) {
                        }
                        try {
                            MainActivity.this.player.next(true);
                        } catch (java.lang.Throwable ignored) {
                        }
                    }
                });
            }
            final android.widget.ImageButton likeBtn =
                    (android.widget.ImageButton) d.findViewById(su.gwarden.music.R.id.now_like);
            this.nowLike = likeBtn;
            bindNowLike(likeBtn, liked);
            if (likeBtn != null) {
                likeBtn.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public final void onClick(android.view.View view) {
                        su.gwarden.music.api.ApiClient.INSTANCE.toggleLikeSimple(
                                liked, new kotlin.jvm.functions.Function1<java.lang.Boolean, kotlin.Unit>() {
                                    @Override
                                    public kotlin.Unit invoke(java.lang.Boolean isLiked) {
                                        final boolean likedNow = isLiked != null && isLiked;
                                        MainActivity.this.runOnUiThread(new java.lang.Runnable() {
                                            @Override
                                            public void run() {
                                                setLikeIcon(likeBtn, likedNow);
                                                showPill(likedNow
                                                        ? "В лайках — рекомендации обновятся"
                                                        : "Убрано из лайков");
                                            }
                                        });
                                        return kotlin.Unit.INSTANCE;
                                    }
                                });
                    }
                });
            }
        } catch (java.lang.Throwable ignored) {
        }

        final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        final java.lang.Runnable r8 = new java.lang.Runnable() { // from class: java.lang.Runnable
            @Override // java.lang.Runnable
            public void run() {
                su.gwarden.music.player.PlayerController playerController4;
                su.gwarden.music.player.PlayerController playerController5;
                int total;
                su.gwarden.music.player.PlayerController playerController6;
                su.gwarden.music.player.PlayerController playerController7;
                su.gwarden.music.player.PlayerController playerController8;
                java.lang.String showNowPlaying$fmt;
                java.lang.String showNowPlaying$fmt2;
                su.gwarden.music.player.PlayerController playerController9;
                playerController4 = su.gwarden.music.MainActivity.this.player;
                su.gwarden.music.player.PlayerController playerController10 = null;
                if (playerController4 == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                    playerController4 = null;
                }
                if (playerController4.getDuration() > 0) {
                    playerController9 = su.gwarden.music.MainActivity.this.player;
                    if (playerController9 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                        playerController9 = null;
                    }
                    total = playerController9.getDuration();
                } else {
                    playerController5 = su.gwarden.music.MainActivity.this.player;
                    if (playerController5 == null) {
                        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                        playerController5 = null;
                    }
                    su.gwarden.music.model.Track current = playerController5.current();
                    total = (current != null ? current.getDuration() : 0) * 1000;
                }
                playerController6 = su.gwarden.music.MainActivity.this.player;
                if (playerController6 == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                    playerController6 = null;
                }
                int pos = playerController6.getPosition();
                if (total > 0) {
                    if (!strip.isDragging()) {
                        // ВАЖНО: делим во float, иначе int/int = 0 и ползунок
                        // всегда стоит в начале (и после перемотки прыгает назад)
                        strip.setPlayed(((float) pos) / ((float) total));
                    }
                    android.widget.TextView textView = cur;
                    showNowPlaying$fmt = su.gwarden.music.MainActivity.showNowPlaying$fmt(pos);
                    textView.setText(showNowPlaying$fmt);
                    android.widget.TextView textView2 = durTx;
                    showNowPlaying$fmt2 = su.gwarden.music.MainActivity.showNowPlaying$fmt(total);
                    textView2.setText(showNowPlaying$fmt2);
                }
                su.gwarden.music.ui.ProgressStrip progressStrip = strip;
                playerController7 = su.gwarden.music.MainActivity.this.player;
                if (playerController7 == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                    playerController7 = null;
                }
                progressStrip.setBuffered(playerController7.getBufferedPct() / 100.0f);
                android.widget.ImageButton imageButton = btnPlay;
                playerController8 = su.gwarden.music.MainActivity.this.player;
                if (playerController8 == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                } else {
                    playerController10 = playerController8;
                }
                imageButton.setImageResource(playerController10.isPlaying() ? su.gwarden.music.R.drawable.ic_pause : su.gwarden.music.R.drawable.ic_play);
                handler.postDelayed(this, 400L);
            }
        };
        android.view.View drag = d.findViewById(su.gwarden.music.R.id.now_drag);
        final kotlin.jvm.internal.Ref.FloatRef downY = new kotlin.jvm.internal.Ref.FloatRef();
        drag.setOnTouchListener(new android.view.View.OnTouchListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda38
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(android.view.View view, android.view.MotionEvent motionEvent) {
                boolean showNowPlaying$lambda$327;
                showNowPlaying$lambda$327 = su.gwarden.music.MainActivity.showNowPlaying$lambda$327(downY, card, d, MainActivity.this, view, motionEvent);
                return showNowPlaying$lambda$327;
            }
        });
        ((android.widget.ImageButton) d.findViewById(su.gwarden.music.R.id.now_close)).setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda39
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNowPlaying$lambda$329(card, d, view);
            }
        });
        android.view.View root = d.findViewById(su.gwarden.music.R.id.now_root);
        root.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda40
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNowPlaying$close(card, d);
            }
        });
        card.setOnClickListener(new android.view.View.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda41
            @Override // android.view.View.OnClickListener
            public final void onClick(android.view.View view) {
                su.gwarden.music.MainActivity.showNowPlaying$lambda$332(view);
            }
        });
        d.setOnDismissListener(new android.content.DialogInterface.OnDismissListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda42
            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(android.content.DialogInterface dialogInterface) {
                MainActivity.this.nowLike = null;
                su.gwarden.music.MainActivity.showNowPlaying$lambda$333(handler, r8, MainActivity.this, dialogInterface);
            }
        });
        handler.post((java.lang.Runnable) r8);
        d.setOnShowListener(new android.content.DialogInterface.OnShowListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda43
            @Override // android.content.DialogInterface.OnShowListener
            public final void onShow(android.content.DialogInterface dialogInterface) {
                su.gwarden.music.MainActivity.showNowPlaying$lambda$336(su.gwarden.music.MainActivity.this, card, d, dialogInterface);
            }
        });
        d.show();
        try { su.gwarden.music.ui.Skin.INSTANCE.applyToDialog(d); } catch (java.lang.Throwable ignored) {}
        try {
            su.gwarden.music.ui.Skin.INSTANCE.installDialogBackground(d);
            su.gwarden.music.ui.Skin.INSTANCE.applyAccent(
                    d.getWindow() != null ? d.getWindow().getDecorView() : null);
        } catch (java.lang.Throwable ignored) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNowPlaying$lambda$310(su.gwarden.music.MainActivity this$0, android.widget.ImageButton $btnAdd, android.view.View it) {
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        su.gwarden.music.model.Track t = playerController.current();
        if (t == null) {
            return;
        }
        this$0.addToMyMusic(t);
        $btnAdd.setColorFilter(-1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final java.lang.String showNowPlaying$fmt(int ms) {
        int s = kotlin.ranges.RangesKt.coerceAtLeast(ms / 1000, 0);
        java.lang.String format = java.lang.String.format("%d:%02d", java.util.Arrays.copyOf(new java.lang.Object[]{java.lang.Integer.valueOf(s / 60), java.lang.Integer.valueOf(s % 60)}, 2));
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(format, "format(...)");
        return format;
    }

    private static final void showNowPlaying$bind(android.widget.TextView title, android.widget.TextView artist, android.widget.ImageView cover, final android.view.View card, final su.gwarden.music.MainActivity this$0, final kotlin.jvm.internal.Ref.ObjectRef<android.graphics.drawable.Drawable> objectRef, su.gwarden.music.ui.ProgressStrip strip, android.widget.TextView cur, android.widget.TextView durTx, su.gwarden.music.model.Track t) {
        title.setText(t.getTitle());
        artist.setText(su.gwarden.music.MainActivity.artistLink(t.getArtist()));
        java.lang.String artwork = t.getArtwork();
        if (!(artwork == null || artwork.length() == 0)) {
            su.gwarden.music.ui.ImageLoader imageLoader = su.gwarden.music.ui.ImageLoader.INSTANCE;
            kotlin.jvm.internal.Intrinsics.checkNotNull(cover);
            imageLoader.load(cover, su.gwarden.music.api.ApiClient.INSTANCE.img(t.getArtwork(), 1000), 0, 0, null);
            java.lang.String artUrl = su.gwarden.music.api.ApiClient.INSTANCE.img(t.getArtwork(), 600);
            su.gwarden.music.ui.ImageLoader.INSTANCE.blurred(artUrl, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda71
                @Override // kotlin.jvm.functions.Function1
                public final java.lang.Object invoke(java.lang.Object obj) {
                    kotlin.Unit showNowPlaying$bind$lambda$313;
                    showNowPlaying$bind$lambda$313 = su.gwarden.music.MainActivity.showNowPlaying$bind$lambda$313(this$0, objectRef, card, (android.graphics.Bitmap) obj);
                    return showNowPlaying$bind$lambda$313;
                }
            });
        } else {
            cover.setImageResource(su.gwarden.music.R.drawable.ic_music_note);
            card.setBackground(androidx.core.content.ContextCompat.getDrawable(this$0, su.gwarden.music.R.drawable.bg_sheet));
            objectRef.element = null;
        }
        strip.setPlayed(0.0f);
        strip.setBuffered(0.0f);
        cur.setText("0:00");
        durTx.setText(showNowPlaying$fmt(t.getDuration() * 1000));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [T, androidx.core.graphics.drawable.RoundedBitmapDrawable, java.lang.Object] */
    public static final kotlin.Unit showNowPlaying$bind$lambda$313(su.gwarden.music.MainActivity this$0, kotlin.jvm.internal.Ref.ObjectRef $bgDrawable, android.view.View $card, android.graphics.Bitmap bmp) {
        if (bmp == null) {
            return kotlin.Unit.INSTANCE;
        }
        androidx.core.graphics.drawable.RoundedBitmapDrawable create = androidx.core.graphics.drawable.RoundedBitmapDrawableFactory.create(this$0.getResources(), bmp);
        create.setCornerRadius(this$0.dp(28));
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(create, "apply(...)");
        android.graphics.drawable.Drawable prev = (android.graphics.drawable.Drawable) $bgDrawable.element;
        if (prev == null) {
            prev = $card.getBackground();
        }
        if (prev != null && prev != create) {
            android.graphics.drawable.TransitionDrawable td = new android.graphics.drawable.TransitionDrawable(new android.graphics.drawable.Drawable[]{prev, create});
            td.setCrossFadeEnabled(true);
            $card.setBackground(td);
            td.startTransition(150);
        } else {
            $card.setBackground((android.graphics.drawable.Drawable) create);
        }
        $bgDrawable.element = create;
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit showNowPlaying$lambda$314(android.widget.TextView $title, android.widget.TextView $artist, android.widget.ImageView $cover, android.view.View $card, su.gwarden.music.MainActivity this$0, kotlin.jvm.internal.Ref.ObjectRef $bgDrawable, su.gwarden.music.ui.ProgressStrip $strip, android.widget.TextView $cur, android.widget.TextView $durTx, su.gwarden.music.model.Track t) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(t, "t");
        showNowPlaying$bind($title, $artist, $cover, $card, this$0, $bgDrawable, $strip, $cur, $durTx, t);
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNowPlaying$lambda$315(su.gwarden.music.MainActivity this$0, android.app.Dialog $d, android.view.View it) {
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        su.gwarden.music.model.Track t = playerController.current();
        if (t == null) {
            return;
        }
        $d.dismiss();
        this$0.startSimilarWave(t);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNowPlaying$lambda$316(su.gwarden.music.MainActivity this$0, android.view.View it) {
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        su.gwarden.music.model.Track t = playerController.current();
        if (t == null) {
            return;
        }
        this$0.showPlaylistPicker(t);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNowPlaying$lambda$317(su.gwarden.music.MainActivity this$0, android.app.Dialog $d, android.view.View it) {
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        su.gwarden.music.model.Track t = playerController.current();
        if (t == null) {
            return;
        }
        $d.dismiss();
        this$0.findRemixes(t);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNowPlaying$flip(final su.gwarden.music.MainActivity this$0, final android.view.View coverBox, final android.widget.TextView title, final android.widget.TextView artist, final android.widget.ImageView cover, final android.view.View card, final kotlin.jvm.internal.Ref.ObjectRef<android.graphics.drawable.Drawable> objectRef, final su.gwarden.music.ui.ProgressStrip strip, final android.widget.TextView cur, final android.widget.TextView durTx, final boolean forward) {
        java.lang.String str;
        if (forward) {
            su.gwarden.music.player.PlayerController playerController = this$0.player;
            su.gwarden.music.player.PlayerController playerController2 = null;
            if (playerController == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                playerController = null;
            }
            if (!playerController.getNextReady()) {
                su.gwarden.music.player.PlayerController playerController3 = this$0.player;
                if (playerController3 == null) {
                    kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                } else {
                    playerController2 = playerController3;
                }
                su.gwarden.music.model.Track nxt = playerController2.peekNext();
                if (nxt != null) {
                    str = "Качаю: " + nxt.getTitle();
                } else {
                    str = "Качаю следующий трек";
                }
                this$0.showPill(str);
                this$0.updateNextButtons(false);
                return;
            }
        }
        float width = coverBox.getWidth();
        if (forward) {
            width = -width;
        }
        final float out = width;
        coverBox.animate().translationX(out).alpha(0.0f).setDuration(140L).withEndAction(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda16
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.showNowPlaying$flip$lambda$318(forward, this$0, coverBox, out, title, artist, cover, card, objectRef, strip, cur, durTx);
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNowPlaying$flip$lambda$318(boolean $forward, su.gwarden.music.MainActivity this$0, android.view.View $coverBox, float $out, android.widget.TextView $title, android.widget.TextView $artist, android.widget.ImageView $cover, android.view.View $card, kotlin.jvm.internal.Ref.ObjectRef $bgDrawable, su.gwarden.music.ui.ProgressStrip $strip, android.widget.TextView $cur, android.widget.TextView $durTx) {
        su.gwarden.music.model.Track prev;
        su.gwarden.music.player.PlayerController playerController = null;
        su.gwarden.music.player.PlayerController playerController2 = this$0.player;
        if ($forward) {
            if (playerController2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                playerController2 = null;
            }
            prev = playerController2.next(false);
        } else {
            if (playerController2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            } else {
                playerController = playerController2;
            }
            prev = playerController.prev();
        }
        su.gwarden.music.model.Track t = prev;
        if (t != null) {
            showNowPlaying$bind($title, $artist, $cover, $card, this$0, $bgDrawable, $strip, $cur, $durTx, t);
        }
        $coverBox.setTranslationX(-$out);
        $coverBox.setAlpha(0.0f);
        $coverBox.animate().translationX(0.0f).alpha(1.0f).setDuration(190L).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean showNowPlaying$lambda$319(kotlin.jvm.internal.Ref.FloatRef $sx, kotlin.jvm.internal.Ref.FloatRef $sy, su.gwarden.music.MainActivity this$0, android.view.View $coverBox, android.widget.TextView $title, android.widget.TextView $artist, android.widget.ImageView $cover, android.view.View $card, kotlin.jvm.internal.Ref.ObjectRef $bgDrawable, su.gwarden.music.ui.ProgressStrip $strip, android.widget.TextView $cur, android.widget.TextView $durTx, android.view.View v, android.view.MotionEvent e) {
        switch (e.getActionMasked()) {
            case 0:
                $sx.element = e.getRawX();
                $sy.element = e.getRawY();
                return true;
            case 1:
                float dx = e.getRawX() - $sx.element;
                if (java.lang.Math.abs(dx) > this$0.dp(38)) {
                    showNowPlaying$flip(this$0, $coverBox, $title, $artist, $cover, $card, $bgDrawable, $strip, $cur, $durTx, dx < 0.0f);
                } else {
                    v.animate().translationX(0.0f).setDuration(150L).start();
                }
                return true;
            case 2:
                float dx2 = e.getRawX() - $sx.element;
                float dy = e.getRawY() - $sy.element;
                if (java.lang.Math.abs(dx2) > java.lang.Math.abs(dy)) {
                    v.setTranslationX(0.45f * dx2);
                }
                return true;
            default:
                return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit showNowPlaying$lambda$322(su.gwarden.music.MainActivity this$0, float p) {
        try {
            su.gwarden.music.api.ApiClient.INSTANCE.log("seek", "p=" + p);
        } catch (java.lang.Throwable ignored) {
        }
        int total;
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        su.gwarden.music.player.PlayerController playerController2 = null;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        if (playerController.getDuration() > 0) {
            su.gwarden.music.player.PlayerController playerController3 = this$0.player;
            if (playerController3 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                playerController3 = null;
            }
            total = playerController3.getDuration();
        } else {
            su.gwarden.music.player.PlayerController playerController4 = this$0.player;
            if (playerController4 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                playerController4 = null;
            }
            su.gwarden.music.model.Track current = playerController4.current();
            total = (current != null ? current.getDuration() : 0) * 1000;
        }
        if (total > 0) {
            su.gwarden.music.player.PlayerController playerController5 = this$0.player;
            if (playerController5 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            } else {
                playerController2 = playerController5;
            }
            playerController2.seekTo((int) (total * p));
        }
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNowPlaying$lambda$323(su.gwarden.music.MainActivity this$0, android.widget.ImageButton $btnPlay, android.view.View it) {
        su.gwarden.music.player.PlayerController playerController = this$0.player;
        su.gwarden.music.player.PlayerController playerController2 = null;
        if (playerController == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
            playerController = null;
        }
        if (playerController.isPlaying()) {
            su.gwarden.music.player.PlayerController playerController3 = this$0.player;
            if (playerController3 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                playerController3 = null;
            }
            playerController3.pause();
        } else {
            su.gwarden.music.player.PlayerController playerController4 = this$0.player;
            if (playerController4 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
                playerController4 = null;
            }
            playerController4.play();
        }
        su.gwarden.music.player.PlayerController playerController5 = this$0.player;
        if (playerController5 == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("player");
        } else {
            playerController2 = playerController5;
        }
        $btnPlay.setImageResource(playerController2.isPlaying() ? su.gwarden.music.R.drawable.ic_pause : su.gwarden.music.R.drawable.ic_play);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean showNowPlaying$lambda$327(kotlin.jvm.internal.Ref.FloatRef $downY, android.view.View $card, final android.app.Dialog $d, su.gwarden.music.MainActivity this$0, android.view.View view, android.view.MotionEvent e) {
        switch (e.getActionMasked()) {
            case 0:
                $downY.element = e.getRawY();
                return true;
            case 1:
                if (kotlin.ranges.RangesKt.coerceAtLeast(e.getRawY() - $downY.element, 0.0f) > this$0.dp(80)) {
                    $card.animate().translationY($card.getHeight()).setDuration(200L).withEndAction(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda99
                        @Override // java.lang.Runnable
                        public final void run() {
                            $d.dismiss();
                        }
                    }).start();
                    return true;
                }
                $card.animate().translationY(0.0f).setDuration(180L).start();
                android.view.Window window = $d.getWindow();
                if (window == null) {
                    return true;
                }
                window.setDimAmount(0.45f);
                return true;
            case 2:
                float dy = kotlin.ranges.RangesKt.coerceAtLeast(e.getRawY() - $downY.element, 0.0f);
                $card.setTranslationY(dy);
                float f = 1.0f - kotlin.ranges.RangesKt.coerceIn(dy / kotlin.ranges.RangesKt.coerceAtLeast($card.getHeight(), 1), 0.0f, 1.0f);
                android.view.Window window2 = $d.getWindow();
                if (window2 == null) {
                    return true;
                }
                window2.setDimAmount(0.45f * f);
                return true;
            default:
                return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNowPlaying$lambda$329(android.view.View $card, final android.app.Dialog $d, android.view.View it) {
        $card.animate().translationY($card.getHeight()).alpha(0.4f).setDuration(220L).withEndAction(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                $d.dismiss();
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNowPlaying$close(android.view.View card, final android.app.Dialog d) {
        card.animate().translationY(card.getHeight()).alpha(0.4f).setDuration(220L).withEndAction(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                d.dismiss();
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNowPlaying$lambda$332(android.view.View it) {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNowPlaying$lambda$333(android.os.Handler $handler, java.lang.Runnable $tick, su.gwarden.music.MainActivity this$0, android.content.DialogInterface it) {
        $handler.removeCallbacks($tick);
        this$0.nowBind = null;
        this$0.nowAdd = null;
        this$0.nowNext = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNowPlaying$lambda$336(su.gwarden.music.MainActivity this$0, android.view.View $card, final android.app.Dialog $d, android.content.DialogInterface it) {
        float screenH = this$0.getResources().getDisplayMetrics().heightPixels;
        $card.setTranslationY(screenH);
        $card.setAlpha(0.25f);
        android.view.Window window = $d.getWindow();
        if (window != null) {
            window.setDimAmount(0.0f);
        }
        $card.animate().translationY(0.0f).alpha(1.0f).setDuration(380L).setInterpolator(new android.view.animation.PathInterpolator(0.2f, 1.0f, 0.3f, 1.0f)).start();
        android.animation.ValueAnimator $this$showNowPlaying_u24lambda_u24336_u24lambda_u24335 = android.animation.ValueAnimator.ofFloat(0.0f, 0.45f);
        $this$showNowPlaying_u24lambda_u24336_u24lambda_u24335.setDuration(380L);
        $this$showNowPlaying_u24lambda_u24336_u24lambda_u24335.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(android.animation.ValueAnimator valueAnimator) {
                su.gwarden.music.MainActivity.showNowPlaying$lambda$336$lambda$335$lambda$334($d, valueAnimator);
            }
        });
        $this$showNowPlaying_u24lambda_u24336_u24lambda_u24335.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showNowPlaying$lambda$336$lambda$335$lambda$334(android.app.Dialog $d, android.animation.ValueAnimator it) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(it, "it");
        android.view.Window window = $d.getWindow();
        if (window != null) {
            java.lang.Object animatedValue = it.getAnimatedValue();
            kotlin.jvm.internal.Intrinsics.checkNotNull(animatedValue, "null cannot be cast to non-null type kotlin.Float");
            window.setDimAmount(((java.lang.Float) animatedValue).floatValue());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* ------------------------------------------------------------- настройки */

    private final void addSettingsButton() {
        try {
            android.view.ViewGroup content = (android.view.ViewGroup) findViewById(android.R.id.content);
            if (content == null) {
                return;
            }
            android.widget.ImageButton gear = new android.widget.ImageButton(this);
            gear.setImageResource(su.gwarden.music.R.drawable.ic_gear);
            gear.setBackgroundColor(0);
            gear.setColorFilter(-1);
            gear.setAlpha(0.72f);
            gear.setContentDescription("Настройки");
            int pad = dp(10);
            gear.setPadding(pad, pad, pad, pad);
            gear.setOnClickListener(v -> showSettings());
            int sb = 0;
            int rid = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (rid > 0) {
                sb = getResources().getDimensionPixelSize(rid);
            }
            android.widget.FrameLayout.LayoutParams lp = new android.widget.FrameLayout.LayoutParams(
                    dp(44), dp(44), android.view.Gravity.TOP | android.view.Gravity.END);
            lp.topMargin = sb + dp(4);
            lp.rightMargin = dp(4);
            content.addView(gear, lp);
            this.settingsBtn = gear;
            gear.setVisibility(8);
        } catch (java.lang.Throwable ignored) {
        }
    }

    /** Скачанные на телефон треки: играют без интернета и без VK. */
    private final void showDownloads() {
        final android.app.Dialog d = new android.app.Dialog(this, su.gwarden.music.R.style.Theme_Wonder_Sheet);
        su.gwarden.music.ui.Skin.INSTANCE.prepare(d);
        d.setCanceledOnTouchOutside(true);
        android.view.Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
            w.setGravity(android.view.Gravity.BOTTOM);
            w.setLayout(-1, -1);
            w.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            android.view.WindowManager.LayoutParams lp = w.getAttributes();
            lp.dimAmount = 0.62f;
            w.setAttributes(lp);
        }
        final android.widget.LinearLayout col = new android.widget.LinearLayout(this);
        col.setOrientation(1);
        col.setBackgroundResource(su.gwarden.music.R.drawable.bg_sheet);
        col.setPadding(dp(20), dp(16), dp(20), dp(18));
        android.widget.TextView title = new android.widget.TextView(this);
        title.setText("Скачано на телефон");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(16.0f);
        title.setTypeface(title.getTypeface(), 1);
        title.setPadding(0, 0, 0, dp(10));
        col.addView(title);
        java.util.List<su.gwarden.music.offline.Offline.Item> items =
                su.gwarden.music.offline.Offline.INSTANCE.list();
        if (items.isEmpty()) {
            android.widget.TextView none = new android.widget.TextView(this);
            none.setText("Пока ничего не скачано. Открой трек и нажми «Скачать» — он будет играть без интернета.");
            none.setTextColor(0xFF8A8A8A);
            none.setTextSize(12.5f);
            none.setPadding(0, dp(6), 0, dp(6));
            col.addView(none);
        } else {
            android.widget.ScrollView sc = new android.widget.ScrollView(this);
            android.widget.LinearLayout box = new android.widget.LinearLayout(this);
            box.setOrientation(1);
            for (final su.gwarden.music.offline.Offline.Item it : items) {
                android.widget.LinearLayout row = new android.widget.LinearLayout(this);
                row.setOrientation(0);
                row.setGravity(android.view.Gravity.CENTER_VERTICAL);
                row.setPadding(dp(4), dp(10), dp(4), dp(10));
                android.widget.TextView tx = new android.widget.TextView(this);
                tx.setText(it.getTitle() + "\n" + it.getArtist());
                tx.setTextSize(13.0f);
                tx.setTextColor(0xFFFFFFFF);
                android.widget.LinearLayout.LayoutParams tlp =
                        new android.widget.LinearLayout.LayoutParams(0, -2, 1.0f);
                row.addView(tx, tlp);
                row.setOnClickListener(v -> {
                    d.dismiss();
                    su.gwarden.music.model.Track t = new su.gwarden.music.model.Track(
                            it.getId(), it.getSource(), it.getTitle(), it.getArtist(),
                            0, it.getArtwork(), "", null, null, false, "", "", 0, "",
                            false, false, 0L, false, false, 1.0f, 0);
                    playTrack(t, java.util.Collections.singletonList(t), true);
                });
                row.setOnLongClickListener(v -> {
                    su.gwarden.music.model.Track t = new su.gwarden.music.model.Track(
                            it.getId(), it.getSource(), it.getTitle(), it.getArtist(),
                            0, it.getArtwork(), "", null, null, false, "", "", 0, "",
                            false, false, 0L, false, false, 1.0f, 0);
                    su.gwarden.music.offline.Offline.INSTANCE.remove(t);
                    showPill("Удалено с телефона");
                    showDownloads();
                    d.dismiss();
                    return true;
                });
                box.addView(row);
            }
            sc.addView(box);
            col.addView(sc, new android.widget.LinearLayout.LayoutParams(-1, 0, 1.0f));
        }
        android.widget.FrameLayout root = new android.widget.FrameLayout(this);
        root.setOnClickListener(v -> d.dismiss());
        col.setClickable(true);
        col.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, -2));
        root.addView(col);
        d.setContentView(root);
        d.show();
        try { su.gwarden.music.ui.Skin.INSTANCE.applyToDialog(d); } catch (java.lang.Throwable ignored) {}
        if (w != null) {
            int h = (int) (getResources().getDisplayMetrics().heightPixels * 0.72f);
            w.setLayout(-1, h);
        }
    }

    private final void showSettings() {
        android.view.View topBar = findViewById(su.gwarden.music.R.id.top_bar);
        if (topBar != null) {
            topBar.setVisibility(8);
        }
        if (this.settingsScroll == null) {
            this.settingsScroll = findViewById(su.gwarden.music.R.id.settings_scroll);
        }
        if (this.settingsBox == null) {
            this.settingsBox = (android.widget.LinearLayout) findViewById(su.gwarden.music.R.id.settings_box);
        }
        if (this.settingsScroll != null) {
            this.settingsScroll.setVisibility(0);
            this.settingsScroll.bringToFront();
        }
        if (this.feedScroll != null) {
            this.feedScroll.setVisibility(8);
        }
        hideLib();
        if (this.recycler != null) {
            this.recycler.setVisibility(8);
        }
        if (this.wavePanel != null) {
            this.wavePanel.setVisibility(8);
        }
        if (this.waveEq != null) {
            this.waveEq.setActive(false);
            this.waveEq.setVisibility(8);
        }
        if (this.searchRow != null) {
            this.searchRow.setVisibility(8);
        }
        showBack(kotlin.jvm.internal.Intrinsics.areEqual(this.tab, "lib") ? "lib" : "home");
        // ВАЖНО: прячем ПОСЛЕ showBack - он снова показывает кнопки шапки.
        // Именно из-за порядка «спрятал -> showBack вернул» они и оставались.
        android.view.View sBtn = findViewById(su.gwarden.music.R.id.btn_search_top);
        if (sBtn != null) {
            sBtn.setVisibility(8);
        }
        android.view.View mBtn = findViewById(su.gwarden.music.R.id.btn_more);
        if (mBtn != null) {
            mBtn.setVisibility(8);
        }
        if (this.hTitle != null) {
            this.hTitle.setText("Настройки");
        }
        if (this.hSub != null) {
            this.hSub.setText("Wonder v" + APP_VER);
        }
        if (this.settingsBox != null) {
            this.settingsBox.removeAllViews();
            android.widget.TextView ld = new android.widget.TextView(this);
            ld.setText("Загружаю...");
            ld.setTextColor(0xFF8A8A8A);
            ld.setTextSize(14.0f);
            ld.setPadding(dp(4), dp(10), 0, 0);
            this.settingsBox.addView(ld);
        }
        su.gwarden.music.api.ApiClient.INSTANCE.importSources((src, err) -> {
            buildSettingsScreen(src);
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void buildSettingsScreen(su.gwarden.music.api.ApiClient.ImportSources src) {
        final android.widget.LinearLayout box = this.settingsBox;
        if (box == null) {
            return;
        }
        box.removeAllViews();

        box.addView(settingsSection("ИМПОРТ"));

        final boolean vkOn = src != null && src.getVk().getConnected();
        box.addView(settingsRow(
                su.gwarden.music.R.drawable.logo_vk,
                "VK Музыка",
                vkOn ? "Подключён · основа «Моей музыки»" : "Не подключён",
                vkOn,
                v -> {
                    if (vkOn) {
                        showVkStatus();
                    } else {
                        startActivityForResult(new android.content.Intent(
                                MainActivity.this, su.gwarden.music.vk.VkLoginActivity.class), 77);
                    }
                }));

        final boolean scOn = src != null && src.getSoundcloud().getConnected();
        final boolean scDone = src != null && src.getSoundcloud().getImported() > 0;
        box.addView(settingsRow(
                su.gwarden.music.R.drawable.logo_sc,
                "SoundCloud",
                scOn ? src.getSoundcloud().getImported() + " лайков в «Моей музыке»" : "Не подключён",
                scDone,
                v -> {
                    if (scOn) {
                        scImportNow();
                    } else {
                        startActivityForResult(new android.content.Intent(
                                MainActivity.this, su.gwarden.music.vk.ScLoginActivity.class), 78);
                    }
                }));

        final boolean yaOn = src != null && src.getYandex().getConnected();
        final boolean yaDone = src != null && src.getYandex().getImported() > 0;
        java.lang.String yaSub = "Не подключён";
        if (yaOn) {
            java.lang.String nm = src.getYandex().getName();
            yaSub = (nm == null || nm.isEmpty() ? "Подключён" : nm)
                    + " · " + src.getYandex().getImported() + " треков";
        }
        box.addView(settingsRow(
                su.gwarden.music.R.drawable.logo_yandex,
                "Яндекс Музыка",
                yaSub,
                yaDone,
                v -> showYandexSettingsMenu()));

        final boolean spOn = src != null && src.getSpotify().getConnected();
        final boolean spDone = src != null && src.getSpotify().getImported() > 0;
        box.addView(settingsRow(
                su.gwarden.music.R.drawable.logo_spotify,
                "Spotify",
                spOn ? "Подключён" : "Только по ссылке (Premium не нужен)",
                spDone,
                v -> showSpotifySettingsMenu()));

        box.addView(settingsTextRow(
                "Обновления",
                "проверить новую версию Wonder",
                v -> su.gwarden.music.ui.UpdateBanner.INSTANCE.checkNow(MainActivity.this)));

        box.addView(settingsTextRow(
                "Импорт по ссылке",
                "Плейлист Deezer / SoundCloud / Spotify",
                v -> showImportDialog("https://www.deezer.com/playlist/...")));

        box.addView(settingsSection("ЗВУК"));
        box.addView(settingsRow(
                su.gwarden.music.R.drawable.ic_eq,
                "Эквалайзер и звук",
                "10 полос · басы · реверб · дистошн · темп · тон · кроссфейд",
                su.gwarden.music.audio.Fx.INSTANCE.getSettings().getEnabled(),
                v -> su.gwarden.music.ui.FxSheet.INSTANCE.show(MainActivity.this, this.player)));

        box.addView(settingsSection("ВНЕШНИЙ ВИД"));
        box.addView(settingsRow(
                su.gwarden.music.R.drawable.ic_palette,
                "Акцент, фон и шрифт",
                su.gwarden.music.ui.Skin.INSTANCE.fontTitle()
                        + " · фон: " + su.gwarden.music.ui.Skin.INSTANCE.bgTitle(),
                false,
                v -> startActivity(new android.content.Intent(
                        MainActivity.this, su.gwarden.music.AppearanceActivity.class))));

        box.addView(settingsSection("ПРИЛОЖЕНИЕ"));
        box.addView(settingsSwitchRow(
                "Режим отладки",
                "копировать логи крашей и зависаний в буфер обмена",
                debugMode(),
                (btn, on) -> {
                    getSharedPreferences("music_player", 0).edit()
                            .putBoolean("debug_mode", on).apply();
                    if (on) {
                        this.hangWatchOn = false;
                        installHangDetector();
                    }
                    showPill(on ? "Режим отладки включён" : "Режим отладки выключен");
                }));
        box.addView(settingsSwitchRow(
                "Энергосбережение",
                "реже обновления · меньше расход батареи",
                su.gwarden.music.eco.Eco.INSTANCE.enabled(),
                (btn, on) -> {
                    su.gwarden.music.eco.Eco.INSTANCE.set(on);
                    showPill(on ? "Энергосбережение включено" : "Выключено · максимум скорости");
                }));
        box.addView(settingsSection("ЕЩЁ"));
        box.addView(settingsTextRow(
                "Профиль автора",
                "Аватар, статистика, свои треки",
                v -> {
                    startActivity(new android.content.Intent(
                            MainActivity.this, su.gwarden.music.ProfileActivity.class));
                }));
        box.addView(settingsTextRow(
                "Интересы для волны",
                "Жанры и любимые артисты",
                v -> showGuestInterests()));
        box.addView(settingsTextRow("Обновить", null, v -> setTab(this.tab)));
        box.addView(settingsTextRow("Аккаунт", null, v -> showAccount()));
        box.addView(settingsTextRow("Статус VK", null, v -> showVkStatus()));
        box.addView(settingsTextRow("Заменить заблокированные", null, v -> fixBlockedNow()));
        box.addView(settingsTextRow(
                this.hideBlocked ? "Показать недоступные" : "Скрыть недоступные", null, v -> {
                    this.hideBlocked = !this.hideBlocked;
                    getSharedPreferences("music_player", 0).edit()
                            .putBoolean("hide_blocked", this.hideBlocked).apply();
                    setTab(this.tab);
                }));
        box.addView(settingsTextRow("Адрес сервера", null, v -> showServerDialog()));
        box.addView(settingsTextRow("Скопировать лог", null, v -> {
            copyToClipboard("Wonder log", buildReport("ЛОГ", ""));
            showPill("Лог скопирован в буфер");
        }));

        android.widget.TextView foot = new android.widget.TextView(this);
        foot.setText("Wonder v" + APP_VER);
        foot.setTextSize(12.0f);
        foot.setTextColor(0xFF6E6E6E);
        foot.setGravity(android.view.Gravity.CENTER);
        foot.setPadding(0, dp(20), 0, dp(8));
        box.addView(foot);
    }

    /** Ряд с настоящим переключателем (Switch), а не «галочкой». */
    private final android.view.View settingsSwitchRow(
            java.lang.String title, java.lang.String sub, boolean checked,
            java.util.function.BiConsumer<android.widget.Switch, java.lang.Boolean> onChange) {
        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
        row.setOrientation(0);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(12), dp(10), dp(12));
        row.setBackgroundResource(su.gwarden.music.R.drawable.bg_row);

        android.widget.LinearLayout col = new android.widget.LinearLayout(this);
        col.setOrientation(1);
        col.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, -2, 1.0f));

        android.widget.TextView t = new android.widget.TextView(this);
        t.setText(title);
        t.setTextColor(0xFFFFFFFF);
        t.setTextSize(15.0f);
        t.setTypeface(t.getTypeface(), 1);
        col.addView(t);

        android.widget.TextView st = new android.widget.TextView(this);
        st.setText(sub);
        st.setTextColor(0xFF8A8A8A);
        st.setTextSize(11.5f);
        st.setPadding(0, dp(2), 0, 0);
        col.addView(st);
        row.addView(col);

        final android.widget.Switch sw = new android.widget.Switch(this);
        sw.setChecked(checked);
        sw.setShowText(false);
        sw.setThumbTintList(android.content.res.ColorStateList.valueOf(
                checked ? 0xFF0A0A0A : 0xFF8A8A8A));
        sw.setTrackTintList(android.content.res.ColorStateList.valueOf(
                checked ? 0xFFFFFFFF : 0xFF2A2A2A));
        sw.setOnCheckedChangeListener((btn, isChecked) -> {
            sw.setThumbTintList(android.content.res.ColorStateList.valueOf(
                    isChecked ? 0xFF0A0A0A : 0xFF8A8A8A));
            sw.setTrackTintList(android.content.res.ColorStateList.valueOf(
                    isChecked ? 0xFFFFFFFF : 0xFF2A2A2A));
            onChange.accept(sw, isChecked);
        });
        row.addView(sw);
        row.setOnClickListener(v -> sw.setChecked(!sw.isChecked()));
        return row;
    }

    private final android.view.View settingsSection(java.lang.String text) {
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFF6E6E6E);
        tv.setTextSize(11.0f);
        tv.setLetterSpacing(0.08f);
        tv.setPadding(dp(4), dp(18), 0, dp(6));
        return tv;
    }

    private final android.view.View settingsRow(int icon, java.lang.String title,
                                                java.lang.String sub, boolean done,
                                                android.view.View.OnClickListener click) {
        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
        row.setOrientation(0);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(6), dp(10), dp(6), dp(10));
        row.setBackgroundResource(su.gwarden.music.R.drawable.bg_row);

        android.widget.ImageView ic = new android.widget.ImageView(this);
        ic.setImageResource(icon);
        // логотипы сервисов (VK/SoundCloud/Spotify/Яндекс) - ЦВЕТНЫЕ.
        // Раньше тут стоял setColorFilter(0xFFBDBDBD), и все четыре иконки
        // превращались в одинаковые серые квадраты - «логотипы пропали».
        ic.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        android.widget.LinearLayout.LayoutParams ilp =
                new android.widget.LinearLayout.LayoutParams(dp(24), dp(24));
        ilp.setMarginEnd(dp(14));
        ilp.setMarginStart(dp(6));
        row.addView(ic, ilp);

        android.widget.LinearLayout col = new android.widget.LinearLayout(this);
        col.setOrientation(1);
        col.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, -2, 1.0f));
        android.widget.TextView t = new android.widget.TextView(this);
        t.setText(title);
        t.setTextColor(-1);
        t.setTextSize(16.0f);
        t.setTypeface(t.getTypeface(), 1);
        col.addView(t);
        if (sub != null && !sub.isEmpty()) {
            android.widget.TextView s = new android.widget.TextView(this);
            s.setText(sub);
            s.setTextColor(0xFF8A8A8A);
            s.setTextSize(12.0f);
            s.setPadding(0, dp(2), 0, 0);
            col.addView(s);
        }
        row.addView(col);

        if (done) {
            android.widget.ImageView chk = new android.widget.ImageView(this);
            chk.setImageResource(su.gwarden.music.R.drawable.ic_check);
            chk.setColorFilter(-1);
            chk.setAlpha(0.9f);
            row.addView(chk, new android.widget.LinearLayout.LayoutParams(dp(18), dp(18)));
        }
        row.setOnClickListener(click);
        android.widget.LinearLayout.LayoutParams rlp =
                new android.widget.LinearLayout.LayoutParams(-1, -2);
        rlp.bottomMargin = dp(6);
        row.setLayoutParams(rlp);
        return row;
    }

    private final android.view.View settingsTextRow(java.lang.String title, java.lang.String sub,
                                                    android.view.View.OnClickListener click) {
        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
        row.setOrientation(0);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(6), dp(12), dp(6), dp(12));

        android.widget.LinearLayout col = new android.widget.LinearLayout(this);
        col.setOrientation(1);
        col.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, -2, 1.0f));
        android.widget.TextView t = new android.widget.TextView(this);
        t.setText(title);
        t.setTextColor(-1);
        t.setTextSize(15.0f);
        col.addView(t);
        if (sub != null && !sub.isEmpty()) {
            android.widget.TextView s = new android.widget.TextView(this);
            s.setText(sub);
            s.setTextColor(0xFF8A8A8A);
            s.setTextSize(12.0f);
            s.setPadding(0, dp(2), 0, 0);
            col.addView(s);
        }
        row.addView(col);

        android.widget.TextView arrow = new android.widget.TextView(this);
        arrow.setText("›");
        arrow.setTextColor(0xFF6E6E6E);
        arrow.setTextSize(20.0f);
        row.addView(arrow);
        row.setOnClickListener(click);
        return row;
    }

    private final void scImportNow() {
        showLoading();
        showPill("SoundCloud: синхронизирую лайки...");
        su.gwarden.music.api.ApiClient.INSTANCE.scImport((n, err) -> {
            hideLoading();
            showPill(err != null
                    ? "SoundCloud: " + err
                    : "SoundCloud: " + n + " лайков в «Моей музыке»");
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void showYandexSettingsMenu() {
        final java.lang.String[] items = {"Моя музыка (лайки)", "Мои плейлисты"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Яндекс Музыка")
                .setItems(items, (dlg, which) -> {
                    if (which == 0) {
                        yandexImportLiked();
                    } else {
                        yandexPickPlaylist();
                    }
                })
                .show();
    }

    private final void showSpotifySettingsMenu() {
        final java.lang.String[] items = {"Моя музыка (лайки)", "Мои плейлисты", "Плейлист по ссылке"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Spotify")
                .setItems(items, (dlg, which) -> {
                    if (which == 0) {
                        spotifyImportLiked();
                    } else if (which == 1) {
                        spotifyPickPlaylist();
                    } else {
                        showImportDialog("https://open.spotify.com/playlist/...");
                    }
                })
                .show();
    }

    private final void fixBlockedNow() {
        android.widget.Toast.makeText(this, "Ищу замены в SoundCloud...", 0).show();
        su.gwarden.music.api.ApiClient.INSTANCE.fixBlocked(false, (obj) -> {
            java.lang.Object v = mp.MpResult.getValue(obj);
            if (mp.MpResult.m323isSuccessimpl(v)) {
                android.widget.Toast.makeText(this, (java.lang.String) v, 0).show();
            }
            return kotlin.Unit.INSTANCE;
        });
    }

    public final void showMoreMenu() {
        java.lang.String[] items = new java.lang.String[8];
        items[0] = "Обновить";
        items[1] = "Аккаунт";
        items[2] = "Импорт";
        items[3] = "Статус VK";
        items[4] = "Заменить заблокированные";
        items[5] = this.hideBlocked ? "Показать недоступные" : "Скрыть недоступные";
        items[6] = "Адрес сервера";
        items[7] = "Скопировать лог";
        new android.app.AlertDialog.Builder(this).setTitle("Меню · v" + APP_VER).setItems(items, new android.content.DialogInterface.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda152
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(android.content.DialogInterface dialogInterface, int i) {
                su.gwarden.music.MainActivity.showMoreMenu$lambda$340(su.gwarden.music.MainActivity.this, dialogInterface, i);
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showMoreMenu$lambda$340(final su.gwarden.music.MainActivity this$0, android.content.DialogInterface dialogInterface, int which) {
        switch (which) {
            case 0:
                this$0.setTab(this$0.tab);
                return;
            case 1:
                this$0.showAccount();
                return;
            case 2:
                this$0.showImportMenu();
                return;
            case 3:
                this$0.showVkStatus();
                return;
            case 4:
                android.widget.Toast.makeText(this$0, "Ищу замены в SoundCloud...", 0).show();
                su.gwarden.music.api.ApiClient.INSTANCE.fixBlocked(false, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda6
                    @Override // kotlin.jvm.functions.Function1
                    public final java.lang.Object invoke(java.lang.Object obj) {
                        kotlin.Unit showMoreMenu$lambda$340$lambda$339;
                        showMoreMenu$lambda$340$lambda$339 = su.gwarden.music.MainActivity.showMoreMenu$lambda$340$lambda$339(this$0, (kotlin.Result) obj);
                        return showMoreMenu$lambda$340$lambda$339;
                    }
                });
                return;
            case 5:
                this$0.hideBlocked = !this$0.hideBlocked;
                this$0.getSharedPreferences("music_player", 0).edit().putBoolean("hide_blocked", this$0.hideBlocked).apply();
                this$0.setTab(this$0.tab);
                return;
            case 6:
                this$0.showServerDialog();
                return;
            case 7:
                this$0.copyToClipboard("Wonder log", this$0.buildReport("ЛОГ", ""));
                this$0.showPill("Лог скопирован в буфер");
                return;
            default:
                return;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit showMoreMenu$lambda$340$lambda$339(final su.gwarden.music.MainActivity this$0, final kotlin.Result res) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda9
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.showMoreMenu$lambda$340$lambda$339$lambda$338(res, this$0);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showMoreMenu$lambda$340$lambda$339$lambda$338(kotlin.Result $res, su.gwarden.music.MainActivity this$0) {
        java.lang.Object value = mp.MpResult.getValue($res);
        if (mp.MpResult.m323isSuccessimpl(value)) {
            java.lang.String it = (java.lang.String) value;
            android.widget.Toast.makeText(this$0, it, 0).show();
        }
    }

    private final void showServerDialog() {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(su.gwarden.music.api.ApiClient.INSTANCE.getBaseUrl());
        input.setSingleLine();
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);
        new android.app.AlertDialog.Builder(this).setTitle("Адрес сервера").setView(input).setPositiveButton("Сохранить", new android.content.DialogInterface.OnClickListener() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda78
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(android.content.DialogInterface dialogInterface, int i) {
                su.gwarden.music.MainActivity.showServerDialog$lambda$341(input, MainActivity.this, dialogInterface, i);
            }
        }).setNegativeButton("Отмена", (android.content.DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showServerDialog$lambda$341(android.widget.EditText $input, su.gwarden.music.MainActivity this$0, android.content.DialogInterface dialogInterface, int i) {
        su.gwarden.music.api.ApiClient.INSTANCE.setBaseUrl($input.getText().toString());
        android.widget.TextView textView = this$0.hSub;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            textView = null;
        }
        textView.setText("Сервер: " + su.gwarden.music.api.ApiClient.INSTANCE.getBaseUrl());
        this$0.setTab(this$0.tab);
    }

    private final void showVkStatus() {
        final android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText("Проверяю VK...");
        tv.setTextSize(13.0f);
        tv.setTextColor(-5197648);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad, pad, pad);
        new android.app.AlertDialog.Builder(this).setTitle("VK Музыка").setView(tv).setPositiveButton("Закрыть", (android.content.DialogInterface.OnClickListener) null).show();
        final kotlin.jvm.internal.Ref.ObjectRef base = new kotlin.jvm.internal.Ref.ObjectRef();
        base.element = "";
        final kotlin.jvm.internal.Ref.ObjectRef fixes = new kotlin.jvm.internal.Ref.ObjectRef();
        fixes.element = "";
        su.gwarden.music.api.ApiClient.INSTANCE.vkStatus(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda26
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit showVkStatus$lambda$349;
                showVkStatus$lambda$349 = su.gwarden.music.MainActivity.showVkStatus$lambda$349(su.gwarden.music.MainActivity.this, base, tv, fixes, (kotlin.Result) obj);
                return showVkStatus$lambda$349;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showVkStatus$render(android.widget.TextView tv, kotlin.jvm.internal.Ref.ObjectRef<java.lang.String> objectRef, kotlin.jvm.internal.Ref.ObjectRef<java.lang.String> objectRef2) {
        tv.setText(((java.lang.Object) objectRef.element) + "\n\nЛокальный VK: " + su.gwarden.music.vk.VkLocal.INSTANCE.getStatus() + (objectRef2.element.length() > 0 ? "\n\n" + ((java.lang.Object) objectRef2.element) : ""));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit showVkStatus$lambda$349(final su.gwarden.music.MainActivity this$0, final kotlin.jvm.internal.Ref.ObjectRef $base, final android.widget.TextView $tv, final kotlin.jvm.internal.Ref.ObjectRef $fixes, final kotlin.Result res) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda85
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.showVkStatus$lambda$349$lambda$348($base, res, this$0, $tv, $fixes);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public static final void showVkStatus$lambda$349$lambda$348(final kotlin.jvm.internal.Ref.ObjectRef objectRef, kotlin.Result result, final su.gwarden.music.MainActivity mainActivity, final android.widget.TextView textView, final kotlin.jvm.internal.Ref.ObjectRef objectRef2) {
        java.lang.Object value = mp.MpResult.getValue(result);
        java.lang.Throwable m319exceptionOrNullimpl = (java.lang.Throwable) mp.MpResult.m319exceptionOrNullimpl(value);
        java.lang.Object t = value;
        if (m319exceptionOrNullimpl != null) {
            t = "Ошибка: " + m319exceptionOrNullimpl.getMessage();
        }
        objectRef.element = t;
        showVkStatus$render(textView, objectRef, objectRef2);
        su.gwarden.music.vk.VkLocal.INSTANCE.ensure(mainActivity, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda24
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit showVkStatus$lambda$349$lambda$348$lambda$344;
                showVkStatus$lambda$349$lambda$348$lambda$344 = su.gwarden.music.MainActivity.showVkStatus$lambda$349$lambda$348$lambda$344(mainActivity, textView, objectRef, objectRef2, ((java.lang.Boolean) obj).booleanValue());
                return showVkStatus$lambda$349$lambda$348$lambda$344;
            }
        });
        su.gwarden.music.api.ApiClient.INSTANCE.fixStatus(new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda25
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit showVkStatus$lambda$349$lambda$348$lambda$347;
                showVkStatus$lambda$349$lambda$348$lambda$347 = su.gwarden.music.MainActivity.showVkStatus$lambda$349$lambda$348$lambda$347(mainActivity, objectRef2, textView, objectRef, (kotlin.Result) obj);
                return showVkStatus$lambda$349$lambda$348$lambda$347;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit showVkStatus$lambda$349$lambda$348$lambda$344(su.gwarden.music.MainActivity this$0, final android.widget.TextView $tv, final kotlin.jvm.internal.Ref.ObjectRef $base, final kotlin.jvm.internal.Ref.ObjectRef $fixes, boolean it) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda60
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.showVkStatus$render($tv, $base, $fixes);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit showVkStatus$lambda$349$lambda$348$lambda$347(su.gwarden.music.MainActivity this$0, final kotlin.jvm.internal.Ref.ObjectRef $fixes, final android.widget.TextView $tv, final kotlin.jvm.internal.Ref.ObjectRef $base, final kotlin.Result f) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda151
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.showVkStatus$lambda$349$lambda$348$lambda$347$lambda$346(f, $fixes, $tv, $base);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v1, types: [T, java.lang.String] */
    public static final void showVkStatus$lambda$349$lambda$348$lambda$347$lambda$346(kotlin.Result $f, kotlin.jvm.internal.Ref.ObjectRef $fixes, android.widget.TextView $tv, kotlin.jvm.internal.Ref.ObjectRef $base) {
        java.lang.Object value = mp.MpResult.getValue($f);
        if (mp.MpResult.m323isSuccessimpl(value)) {
            $fixes.element = (java.lang.String) value;
            showVkStatus$render($tv, $base, $fixes);
        }
    }

    private final void showImportDialog() {
        showImportDialog("https://open.spotify.com/playlist/...");
    }

    /* ------------------------------------------------------ импорт: меню/Spotify */

    private final void showImportMenu() {
        final java.lang.String[] items = {
                "Spotify — моя музыка",
                "Spotify — мои плейлисты",
                "Spotify — по ссылке",
                "Яндекс — моя музыка",
                "Яндекс — мои плейлисты",
                "Deezer / SoundCloud — по ссылке",
                "Сбросить входы (Spotify/Яндекс)",
        };
        new android.app.AlertDialog.Builder(this)
                .setTitle("Импорт")
                .setItems(items, (dialogInterface, which) -> {
                    switch (which) {
                        case 0:
                            spotifyImportLiked();
                            return;
                        case 1:
                            spotifyPickPlaylist();
                            return;
                        case 2:
                            showImportDialog("https://open.spotify.com/playlist/...");
                            return;
                        case 3:
                            yandexImportLiked();
                            return;
                        case 4:
                            yandexPickPlaylist();
                            return;
                        case 5:
                            showImportDialog("https://www.deezer.com/playlist/...");
                            return;
                        default:
                            resetImports();
                    }
                })
                .show();
    }

    private final void showImportDialog(java.lang.String hint) {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint(hint);
        input.setSingleLine();
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);
        new android.app.AlertDialog.Builder(this)
                .setTitle("Импорт по ссылке")
                .setMessage("Spotify / Deezer / SoundCloud — подберём треки в SoundCloud")
                .setView(input)
                .setPositiveButton("Импортировать", (dialogInterface, i) -> {
                    java.lang.String url = input.getText().toString().trim();
                    if (url.length() == 0) {
                        return;
                    }
                    startUrlImport(url);
                })
                .setNegativeButton("Отмена", (android.content.DialogInterface.OnClickListener) null)
                .show();
    }

    private final void startUrlImport(java.lang.String url) {
        this.importToken++;
        final int token = this.importToken;
        showLoading();
        showPill("Импорт: читаю плейлист...");
        su.gwarden.music.api.ApiClient.INSTANCE.importJob(url, 200, (start, err) -> {
            if (token != this.importToken) {
                return kotlin.Unit.INSTANCE;
            }
            if (err != null || start == null) {
                hideLoading();
                showPill("Импорт: " + (err == null ? "пустой ответ" : err));
                return kotlin.Unit.INSTANCE;
            }
            showPill("Импорт «" + start.getTitle() + "» · 0/" + start.getTotal());
            pollImport(start.getJob(), start.getTitle(), start.getCover(), 0, token);
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void pollImport(final java.lang.String job, final java.lang.String title,
                                  final java.lang.String cover, final int attempt, final int token) {
        su.gwarden.music.api.ApiClient.INSTANCE.importJobStatus(job, (st, err) -> {
            if (token != this.importToken) {
                return kotlin.Unit.INSTANCE;
            }
            if (st == null) {
                hideLoading();
                showPill("Импорт: " + (err == null ? "нет ответа" : err));
                return kotlin.Unit.INSTANCE;
            }
            if ("done".equals(st.getState())) {
                hideLoading();
                importDone(
                        st.getTitle() == null || st.getTitle().isEmpty() ? title : st.getTitle(),
                        st.getCover() == null || st.getCover().isEmpty() ? cover : st.getCover(),
                        st.getItems());
            } else if ("error".equals(st.getState())) {
                hideLoading();
                showPill("Импорт: " + st.getError());
            } else {
                if (attempt % 4 == 0) {
                    showPill("Импорт " + st.getDone() + "/" + st.getTotal());
                }
                if (attempt < 600) {
                    uiHandler.postDelayed(
                            () -> pollImport(job, title, cover, attempt + 1, token), 1000L);
                } else {
                    hideLoading();
                    showPill("Импорт: слишком долго, попробуй ещё раз");
                }
            }
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void importDone(java.lang.String title, java.lang.String cover,
                                  java.util.List<su.gwarden.music.model.Track> items) {
        if (items == null || items.isEmpty()) {
            showPill("Импорт: не удалось подобрать треки");
            return;
        }
        try {
            su.gwarden.music.model.LocalPlaylist pl = su.gwarden.music.model.LocalPlaylists.INSTANCE
                    .create(
                            title == null || title.isEmpty() ? "Импорт" : title,
                            "Импортировано в Wonder",
                            cover == null ? "" : cover,
                            items);
            showPill("Плейлист «" + pl.getTitle() + "» · " + items.size() + " треков");
            openPlaylist(localAsVk(pl));
        } catch (java.lang.Throwable e) {
            showPill("Импорт: " + e.getMessage());
        }
    }

    /** Что делаем после входа в Spotify: liked | playlists | playlist:<id>. */
    private java.lang.String spotifyPending = "";

    /* --------------------------------------------------------- Яндекс Музыка */

    private final void yandexImportLiked() {
        showLoading();
        su.gwarden.music.api.ApiClient.INSTANCE.yandexStatus((st, err) -> {
            hideLoading();
            if (st == null || !st.getConnected()) {
                this.spotifyPending = "ya:liked";
                startActivityForResult(new android.content.Intent(
                        this, su.gwarden.music.spotify.YandexLoginActivity.class), 81);
                return kotlin.Unit.INSTANCE;
            }
            startYandexImport("liked", "", "Яндекс · Мне нравится");
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void yandexPickPlaylist() {
        showLoading();
        su.gwarden.music.api.ApiClient.INSTANCE.yandexStatus((st, err) -> {
            hideLoading();
            if (st == null || !st.getConnected()) {
                this.spotifyPending = "ya:playlists";
                startActivityForResult(new android.content.Intent(
                        this, su.gwarden.music.spotify.YandexLoginActivity.class), 81);
                return kotlin.Unit.INSTANCE;
            }
            loadYandexPlaylists();
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void loadYandexPlaylists() {
        showLoading();
        su.gwarden.music.api.ApiClient.INSTANCE.yandexPlaylists((list, err) -> {
            hideLoading();
            if (list == null || list.isEmpty()) {
                showPill("Яндекс: " + (err == null ? "плейлистов нет" : err));
                return kotlin.Unit.INSTANCE;
            }
            final java.lang.String[] names = new java.lang.String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                names[i] = list.get(i).getTitle() + " · " + list.get(i).getCount();
            }
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Плейлисты Яндекс Музыки")
                    .setItems(names, (dialogInterface, which) -> {
                        su.gwarden.music.api.ApiClient.SpotifyPlaylist pl = list.get(which);
                        startYandexImport("playlist", pl.getId(), pl.getTitle());
                    })
                    .show();
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void startYandexImport(java.lang.String kind, java.lang.String id,
                                         java.lang.String title) {
        this.importToken++;
        final int token = this.importToken;
        showLoading();
        showPill("Яндекс: подбираю треки...");
        su.gwarden.music.api.ApiClient.INSTANCE.yandexImport(kind, id, 300, (start, err) -> {
            if (token != this.importToken) {
                return kotlin.Unit.INSTANCE;
            }
            if (err != null || start == null) {
                hideLoading();
                showPill("Яндекс: " + (err == null ? "пустой ответ" : err));
                return kotlin.Unit.INSTANCE;
            }
            showPill("Импорт «" + start.getTitle() + "» · 0/" + start.getTotal());
            pollImport(start.getJob(), start.getTitle(), start.getCover(), 0, token);
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void resetImports() {
        su.gwarden.music.spotify.SpotifyToken.INSTANCE.clear(this);
        su.gwarden.music.api.ApiClient.INSTANCE.spotifyLogout((ok, err) -> {
            return kotlin.Unit.INSTANCE;
        });
        su.gwarden.music.api.ApiClient.INSTANCE.yandexLogout((ok, err) -> {
            showPill("Входы Spotify/Яндекс сброшены");
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void spotifyImportLiked() {
        showLoading();
        su.gwarden.music.api.ApiClient.INSTANCE.spotifyStatus((st, err) -> {
            hideLoading();
            if (spotifyTokenFresh(st)) {
                startSpotifyImport("liked", "", "Spotify · Мне нравится");
            } else {
                spotifyAsk("liked");
            }
            return kotlin.Unit.INSTANCE;
        });
    }

    /** Токен с сервера ещё живой (живёт ~час, берём с запасом 10 минут). */
    private final boolean spotifyTokenFresh(su.gwarden.music.api.ApiClient.SpotifyStatus st) {
        if (st == null || !st.getConnected()) {
            return false;
        }
        long now = System.currentTimeMillis() / 1000L;
        return st.getExpiresAt() > now + 600L;
    }

    private final void spotifyAsk(java.lang.String action) {
        this.spotifyPending = action;
        startActivityForResult(new android.content.Intent(
                this, su.gwarden.music.spotify.SpotifyLoginActivity.class), 80);
    }

    private final void spotifyRunPending() {
        java.lang.String a = this.spotifyPending == null ? "" : this.spotifyPending;
        this.spotifyPending = "";
        if ("playlists".equals(a)) {
            loadSpotifyPlaylists();
            return;
        }
        if (a.startsWith("playlist:")) {
            startSpotifyImport("playlist", a.substring(9), "Spotify · Плейлист");
            return;
        }
        startSpotifyImport("liked", "", "Spotify · Мне нравится");
    }

    private final void spotifyLogin() {
        startActivityForResult(new android.content.Intent(
                this, su.gwarden.music.spotify.SpotifyLoginActivity.class), 80);
    }

    /**
     * Свежий токен веб-плеера с телефона (живёт ~час) -> серверу. Потом run().
     * Токен добываем КАЖДЫЙ раз: сервер не может обновить его сам (Spotify
     * блокирует get_access_token с датацентровых IP).
     */
    private final void refreshSpotify(final java.lang.Runnable then) {
        java.lang.String sp = su.gwarden.music.spotify.SpotifyToken.INSTANCE.spDc(this);
        if (sp.isEmpty()) {
            spotifyLogin();
            return;
        }
        su.gwarden.music.spotify.SpotifyToken.INSTANCE.fetch(sp, (tok, exp, err) -> {
            if (tok == null) {
                hideLoading();
                showPill("Spotify: " + (err == null ? "нужен вход" : err));
                spotifyLogin();
                return kotlin.Unit.INSTANCE;
            }
            su.gwarden.music.api.ApiClient.INSTANCE.pushSpotifyToken(tok, exp, (ok, err2) -> {
                if (err2 != null) {
                    hideLoading();
                    showPill("Spotify: " + err2);
                    return kotlin.Unit.INSTANCE;
                }
                then.run();
                return kotlin.Unit.INSTANCE;
            });
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void spotifyReset() {
        su.gwarden.music.spotify.SpotifyToken.INSTANCE.clear(this);
        su.gwarden.music.api.ApiClient.INSTANCE.spotifyLogout((ok, err) -> {
            showPill("Spotify: вход сброшен");
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void spotifyConnect() {
        su.gwarden.music.api.ApiClient.INSTANCE.spotifyAuthUrl((url, err) -> {
            if (url == null) {
                showPill("Spotify: " + (err == null ? "не настроен" : err));
                return kotlin.Unit.INSTANCE;
            }
            try {
                startActivity(new android.content.Intent(
                        android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)));
            } catch (java.lang.Throwable e) {
                showPill("Spotify: нет браузера");
                return kotlin.Unit.INSTANCE;
            }
            showPill("Войди в Spotify в браузере...");
            spotifyWaitAuth(0);
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void spotifyWaitAuth(final int attempt) {
        uiHandler.postDelayed(
                () -> su.gwarden.music.api.ApiClient.INSTANCE.spotifyStatus((st, err) -> {
                    if (st != null && st.getConnected()) {
                        showPill("Spotify подключён"
                                + (st.getName() == null || st.getName().isEmpty()
                                ? "" : " · " + st.getName()));
                        spotifyImportLiked();
                        return kotlin.Unit.INSTANCE;
                    }
                    if (attempt < 90) {
                        spotifyWaitAuth(attempt + 1);
                    } else {
                        showPill("Spotify: вход не завершён");
                    }
                    return kotlin.Unit.INSTANCE;
                }),
                2000L);
    }

    private final void spotifyPickPlaylist() {
        showLoading();
        su.gwarden.music.api.ApiClient.INSTANCE.spotifyStatus((st, err) -> {
            hideLoading();
            if (spotifyTokenFresh(st)) {
                loadSpotifyPlaylists();
            } else {
                spotifyAsk("playlists");
            }
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void loadSpotifyPlaylists() {
        showLoading();
        su.gwarden.music.api.ApiClient.INSTANCE.spotifyPlaylists((list, err) -> {
            hideLoading();
            if (list == null || list.isEmpty()) {
                showPill("Spotify: " + (err == null ? "плейлистов нет" : err));
                return kotlin.Unit.INSTANCE;
            }
            final java.lang.String[] names = new java.lang.String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                names[i] = list.get(i).getTitle() + " · " + list.get(i).getCount();
            }
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Плейлисты Spotify")
                    .setItems(names, (dialogInterface, which) -> {
                        su.gwarden.music.api.ApiClient.SpotifyPlaylist pl = list.get(which);
                        startSpotifyImport("playlist", pl.getId(), pl.getTitle());
                    })
                    .show();
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void startSpotifyImport(java.lang.String kind, java.lang.String id,
                                          java.lang.String title) {
        this.importToken++;
        final int token = this.importToken;
        showLoading();
        showPill("Spotify: подбираю треки...");
        su.gwarden.music.api.ApiClient.INSTANCE.spotifyImport(kind, id, 300, (start, err) -> {
            if (token != this.importToken) {
                return kotlin.Unit.INSTANCE;
            }
            if (err != null || start == null) {
                hideLoading();
                showPill("Spotify: " + (err == null ? "пустой ответ" : err));
                return kotlin.Unit.INSTANCE;
            }
            showPill("Импорт «" + start.getTitle() + "» · 0/" + start.getTotal());
            pollImport(start.getJob(), start.getTitle(), start.getCover(), 0, token);
            return kotlin.Unit.INSTANCE;
        });
    }

    private final void spotifySetupDialog() {
        float d = getResources().getDisplayMetrics().density;
        android.widget.LinearLayout box = new android.widget.LinearLayout(this);
        box.setOrientation(1);
        box.setPadding((int) (20 * d), (int) (8 * d), (int) (20 * d), 0);

        android.widget.TextView hint = new android.widget.TextView(this);
        hint.setText("1. developer.spotify.com → Create app\n"
                + "2. Redirect URI: https://example.com/music/spotify/callback\n"
                + "3. User Management → добавь свой аккаунт Spotify\n"
                + "4. Вставь Client ID и Client Secret из Settings");
        hint.setTextSize(13.0f);
        hint.setTextColor(-7829368);
        box.addView(hint);

        final android.widget.EditText cid = new android.widget.EditText(this);
        cid.setHint("Client ID");
        cid.setSingleLine();
        box.addView(cid);

        final android.widget.EditText sec = new android.widget.EditText(this);
        sec.setHint("Client Secret");
        sec.setSingleLine();
        box.addView(sec);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Настройки Spotify")
                .setView(box)
                .setPositiveButton("Сохранить", (dialogInterface, i) -> {
                    java.lang.String a = cid.getText().toString().trim();
                    java.lang.String b = sec.getText().toString().trim();
                    if (a.isEmpty() || b.isEmpty()) {
                        showPill("Spotify: нужны Client ID и Secret");
                        return;
                    }
                    su.gwarden.music.api.ApiClient.INSTANCE.spotifySetApp(a, b, (redirect, err) -> {
                        if (err != null) {
                            showPill("Spotify: " + err);
                            return kotlin.Unit.INSTANCE;
                        }
                        showPill("Spotify настроен, входи");
                        spotifyConnect();
                        return kotlin.Unit.INSTANCE;
                    });
                })
                .setNegativeButton("Отмена", (android.content.DialogInterface.OnClickListener) null)
                .show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showImportDialog$lambda$354(android.widget.EditText $input, final su.gwarden.music.MainActivity this$0, android.content.DialogInterface dialogInterface, int i) {
        java.lang.String url = kotlin.text.StringsKt.trim((java.lang.CharSequence) $input.getText().toString()).toString();
        if (url.length() == 0) {
            return;
        }
        android.widget.TextView textView = this$0.hSub;
        if (textView == null) {
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            textView = null;
        }
        textView.setText("Импорт: читаю плейлист...");
        su.gwarden.music.api.ApiClient.INSTANCE.importPlaylist(url, "vk", 40, new kotlin.jvm.functions.Function1() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda10
            @Override // kotlin.jvm.functions.Function1
            public final java.lang.Object invoke(java.lang.Object obj) {
                kotlin.Unit showImportDialog$lambda$354$lambda$353;
                showImportDialog$lambda$354$lambda$353 = su.gwarden.music.MainActivity.showImportDialog$lambda$354$lambda$353(this$0, (kotlin.Result) obj);
                return showImportDialog$lambda$354$lambda$353;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final kotlin.Unit showImportDialog$lambda$354$lambda$353(final su.gwarden.music.MainActivity this$0, final kotlin.Result res) {
        this$0.runOnUiThread(new java.lang.Runnable() { // from class: su.gwarden.music.MainActivity$$ExternalSyntheticLambda80
            @Override // java.lang.Runnable
            public final void run() {
                su.gwarden.music.MainActivity.showImportDialog$lambda$354$lambda$353$lambda$352(res, this$0);
            }
        });
        return kotlin.Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showImportDialog$lambda$354$lambda$353$lambda$352(kotlin.Result $res, su.gwarden.music.MainActivity this$0) {
        java.lang.Object value = mp.MpResult.getValue($res);
        android.widget.TextView textView = null;
        if (mp.MpResult.m323isSuccessimpl(value)) {
            java.util.List d = (java.util.List) value;
            androidx.recyclerview.widget.RecyclerView recyclerView = this$0.recycler;
            if (recyclerView == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("recycler");
                recyclerView = null;
            }
            su.gwarden.music.ui.TrackAdapter trackAdapter = this$0.adapter;
            if (trackAdapter == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter = null;
            }
            recyclerView.setAdapter(trackAdapter);
            su.gwarden.music.ui.TrackAdapter trackAdapter2 = this$0.adapter;
            if (trackAdapter2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("adapter");
                trackAdapter2 = null;
            }
            trackAdapter2.submit(d, false);
            android.widget.TextView textView2 = this$0.hTitle;
            if (textView2 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hTitle");
                textView2 = null;
            }
            textView2.setText("Импорт");
            android.widget.TextView textView3 = this$0.hSub;
            if (textView3 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
                textView3 = null;
            }
            textView3.setText("Найдено " + d.size() + " треков");
            this$0.showEmpty(d.isEmpty() ? "Не удалось подобрать" : null);
        }
        java.lang.Throwable e = (java.lang.Throwable) mp.MpResult.m319exceptionOrNullimpl(value);
        if (e != null) {
            android.widget.TextView textView4 = this$0.hSub;
            if (textView4 == null) {
                kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("hSub");
            } else {
                textView = textView4;
            }
            textView.setText("Импорт: " + e.getMessage());
        }
    }
}
