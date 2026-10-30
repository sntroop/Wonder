package su.gwarden.music

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import su.gwarden.music.api.ApiClient

/**
 * Онбординг и вход Wonder.
 *
 * Экран 1 - выбор языка (RU/EN), тап по экрану листает дальше.
 * Экран 2 - объяснение (API/Cookie) и вход: ВК, по паролю, регистрация
 *            по @юзернейму (пароль хранится только хешем).
 *
 * Всё монохромное: чёрный фон, белые кнопки, серые подписи.
 */
object WonderAuth {

    private const val PREFS = "wonder"
    private const val K_LANG = "wonder_lang"
    private const val K_DONE = "wonder_onboarded"

    private const val BG = 0xFF0A0A0A.toInt()
    private const val CARD = 0xFF141414.toInt()
    private const val STROKE = 0xFF2A2A2A.toInt()
    private const val TXT = 0xFFF2F2F2.toInt()
    private const val DIM = 0xFF8A8A8A.toInt()
    private const val ERR = 0xFFE06A6A.toInt()

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, 0)

    fun lang(ctx: Context): String = prefs(ctx).getString(K_LANG, "ru") ?: "ru"
    fun isEn(ctx: Context): Boolean = lang(ctx) == "en"

    fun setLang(ctx: Context, code: String) {
        prefs(ctx).edit().putString(K_LANG, code).apply()
        ApiClient.lang = code
        applyLang(ctx)
    }

    fun isOnboarded(ctx: Context): Boolean = prefs(ctx).getBoolean(K_DONE, false)
    fun markOnboarded(ctx: Context) = prefs(ctx).edit().putBoolean(K_DONE, true).apply()

    fun initLang(ctx: Context) {
        ApiClient.lang = lang(ctx)
        applyLang(ctx)
    }

    /** Подписи нижней навигации и мелочи, которые строятся в layout. */
    fun applyLang(ctx: Context) {
        val en = isEn(ctx)
        fun set(id: Int, ru: String, eng: String) {
            (ctx as? Activity)?.findViewById<TextView>(id)?.text = if (en) eng else ru
        }
        set(R.id.nav_home_tx, "Главная", "Home")
        set(R.id.nav_search_tx, "Поиск", "Search")
        set(R.id.nav_my_tx, "Моя волна", "My wave")
        set(R.id.nav_lib_tx, "Библиотека", "Library")
        set(R.id.st_tracks, "Треки", "Tracks")
        set(R.id.st_artists, "Артисты", "Artists")
        set(R.id.st_albums, "Альбомы", "Albums")
        set(R.id.st_playlists, "Плейлисты", "Playlists")
        (ctx as? Activity)?.findViewById<android.widget.EditText>(R.id.edit_query)?.hint =
            if (en) "Tracks, artists..." else "Треки, исполнители..."
    }

    /** Выход: онбординг показываем заново. */
    fun resetOnboarding(ctx: Context) {
        prefs(ctx).edit().putBoolean(K_DONE, false).apply()
    }

    private fun t(ctx: Context, ru: String, en: String) = if (isEn(ctx)) en else ru

    /** Перевод из Java-кода: tr(this, "русский", "english"). */
    fun tr(ctx: Context, ru: String, en: String): String = t(ctx, ru, en)

    // ------------------------------------------------------------------ utils

    private fun dp(ctx: Context, v: Int): Int =
        (v * ctx.resources.displayMetrics.density).toInt()

    private fun rounded(fill: Int, stroke: Int = 0, radiusDp: Int = 26): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusDp * 3f
            setColor(fill)
            if (stroke != 0) setStroke(3, stroke)
        }

    private fun txt(ctx: Context, s: CharSequence, size: Float, color: Int = TXT): TextView =
        TextView(ctx).apply {
            text = s
            textSize = size
            setTextColor(color)
            includeFontPadding = false
        }

    private fun logo(ctx: Context, sizeDp: Int): ImageView =
        ImageView(ctx).apply {
            setImageResource(R.drawable.logo_wonder)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LinearLayout.LayoutParams(dp(ctx, sizeDp), dp(ctx, sizeDp))
        }

    private fun button(ctx: Context, label: String, primary: Boolean): TextView =
        TextView(ctx).apply {
            text = label
            textSize = 15.5f
            gravity = Gravity.CENTER
            setTextColor(if (primary) Color.BLACK else TXT)
            background = rounded(if (primary) Color.WHITE else 0x14FFFFFF, if (primary) 0 else STROKE)
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 54)
            ).apply { topMargin = dp(ctx, 10) }
        }

    private fun content(act: Activity): ViewGroup? =
        act.findViewById(android.R.id.content)

    private fun frame(ctx: Context): FrameLayout = FrameLayout(ctx).apply {
        setBackgroundColor(BG)
        isClickable = true
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    // ------------------------------------------------------------------- вход

    /** Точка входа: онбординг (если ещё не было) или экран входа. */
    fun show(act: MainActivity) {
        hideOverlay(act)
        val root = frame(act)
        if (!isOnboarded(act)) {
            buildSlides(act, root, loggedIn = ApiClient.loggedIn)
        } else {
            buildLogin(act, root)
        }
        content(act)?.addView(root)
        act.authBridgeOverlay(root)
    }

    private fun hideOverlay(act: Activity) {
        (act as? MainActivity)?.authBridgeClose()
    }

    // ---------------------------------------------------------------- слайды

    private fun buildSlides(act: MainActivity, root: FrameLayout, loggedIn: Boolean) {
        val ctx = act
        val p1 = FrameLayout(ctx).apply { layoutParams = full() }

        // ---------- слайд 1: язык
        val c1 = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(ctx, 28), dp(ctx, 40), dp(ctx, 28), dp(ctx, 36))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        c1.addView(logo(ctx, 96))
        c1.addView(txt(ctx, "Wonder", 40f).apply {
            typeface = su.gwarden.music.ui.Skin.typeface(ctx)
            letterSpacing = 0.04f
            gravity = Gravity.CENTER
            setPadding(0, dp(ctx, 14), 0, 0)
        })
        val title1 = txt(ctx, t(ctx, "Выберите язык", "Choose a language"), 17f, DIM).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(ctx, 6), 0, dp(ctx, 26))
        }
        c1.addView(title1)

        val langRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        lateinit var ruBtn: TextView
        lateinit var enBtn: TextView
        ruBtn = button(ctx, "Русский", lang(ctx) != "en").apply {
            layoutParams = LinearLayout.LayoutParams(dp(ctx, 138), dp(ctx, 52))
            setOnClickListener {
                setLang(ctx, "ru")
                ruBtn.background = rounded(Color.WHITE)
                ruBtn.setTextColor(Color.BLACK)
                enBtn.background = rounded(0x14FFFFFF, STROKE)
                enBtn.setTextColor(TXT)
                title1.text = "Выберите язык"
            }
        }
        enBtn = button(ctx, "English", lang(ctx) == "en").apply {
            layoutParams = LinearLayout.LayoutParams(dp(ctx, 138), dp(ctx, 52)).apply {
                leftMargin = dp(ctx, 10)
            }
            setOnClickListener {
                setLang(ctx, "en")
                ruBtn.background = rounded(0x14FFFFFF, STROKE)
                ruBtn.setTextColor(TXT)
                enBtn.background = rounded(Color.WHITE)
                enBtn.setTextColor(Color.BLACK)
                title1.text = "Choose a language"
            }
        }
        langRow.addView(ruBtn)
        langRow.addView(enBtn)
        c1.addView(langRow)

        c1.addView(txt(
            ctx,
            t(ctx, "Нажмите в любом месте, чтобы продолжить", "Tap anywhere to continue"),
            12f, 0xFF5E5E5E.toInt()
        ).apply { gravity = Gravity.CENTER },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(ctx, 26) })
        p1.addView(c1)

        // ---------- слайд 2: зачем входить.
        // Строим ЛЕНИВО (при переходе), чтобы выбранный на первом слайде язык
        // сразу применился к тексту и кнопкам.
        fun makeSlide2(): FrameLayout {
            val page = FrameLayout(ctx).apply {
                layoutParams = full()
                alpha = 0f
                visibility = View.INVISIBLE
            }
            val c2 = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(dp(ctx, 26), dp(ctx, 34), dp(ctx, 26), dp(ctx, 30))
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            c2.addView(logo(ctx, 72))
            c2.addView(txt(ctx, "Wonder", 30f).apply {
                typeface = su.gwarden.music.ui.Skin.typeface(ctx)
                letterSpacing = 0.04f
                gravity = Gravity.CENTER
                setPadding(0, dp(ctx, 10), 0, 0)
            })
            val body = txt(
                ctx,
                t(
                    ctx,
                    "Wonder не хранит треки сам — мы используем API и Cookie. " +
                        "Поэтому мы настоятельно просим вас войти через ВК, даже если у вас там нет музыки. " +
                        "Если ВК нет — зарегистрируйтесь по @юзернейму. После этого вы сможете привязать " +
                        "популярные сервисы и правильно настроить рекомендации.",
                    "Wonder doesn't host music itself — we use APIs and cookies. " +
                        "That's why we strongly recommend signing in with VK, even if you have no music there. " +
                        "No VK? Create an account with a @username. After that you can connect popular services " +
                        "and tune your recommendations."
                ),
                12.5f, 0xFFB0B0B0.toInt()
            ).apply {
                gravity = Gravity.CENTER
                setLineSpacing(dp(ctx, 4).toFloat(), 1f)
                setPadding(dp(ctx, 4), dp(ctx, 16), dp(ctx, 4), dp(ctx, 18))
            }
            c2.addView(body)

            if (loggedIn) {
                val go = button(ctx, t(ctx, "Продолжить", "Continue"), true)
                go.setOnClickListener { finish(act, null) }
                c2.addView(go, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 54)
                ))
            } else {
                val vk = button(ctx, t(ctx, "Войти по ВК", "Sign in with VK"), true)
                vk.setOnClickListener { act.authBridgeVk() }
                c2.addView(vk, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 54)
                ))

                val google = button(ctx, t(ctx, "Продолжить с Google", "Continue with Google"), false)
                google.setOnClickListener { signInGoogle(act) }
                c2.addView(google, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 54)
                ).apply { topMargin = dp(ctx, 10) })

                val byPass = button(ctx, t(ctx, "Войти по паролю", "Sign in with password"), false)
                byPass.setOnClickListener { openSheet(act, register = false) }
                c2.addView(byPass, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 54)
                ).apply { topMargin = dp(ctx, 10) })

                val reg = button(ctx, t(ctx, "Зарегистрировать", "Create account"), false)
                reg.setOnClickListener { openSheet(act, register = true) }
                c2.addView(reg, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 54)
                ).apply { topMargin = dp(ctx, 10) })

                c2.addView(txt(
                    ctx,
                    t(ctx, "Пароль хранится только в виде хеша. Треки не храним.",
                        "Your password is stored only as a hash. We don't host tracks."),
                    10.5f, 0xFF5C5C5C.toInt()
                ).apply {
                    gravity = Gravity.CENTER
                    setPadding(0, dp(ctx, 14), 0, 0)
                })
            }
            page.addView(c2)
            return page
        }

        root.addView(p1)

        var moved = false
        var p2: FrameLayout? = null
        root.setOnClickListener {
            if (moved) return@setOnClickListener
            moved = true
            if (loggedIn) {
                finish(act, null)
                return@setOnClickListener
            }
            val page = p2 ?: makeSlide2().also { p2 = it; root.addView(it) }
            page.visibility = View.VISIBLE
            page.translationY = dp(ctx, 60).toFloat()
            p1.animate().alpha(0f).translationY(-dp(ctx, 40).toFloat())
                .setDuration(280).start()
            page.animate().alpha(1f).translationY(0f).setDuration(320).start()
        }
    }

    /** Завершить онбординг (и закрыть оверлей). */
    private fun finish(act: MainActivity, hello: String?) {
        markOnboarded(act)
        act.authBridgeClose()
        if (hello != null) act.authBridgeDone(hello)
    }

    // ------------------------------------------------------------- экран входа

    private fun buildLogin(act: MainActivity, root: FrameLayout) {
        val ctx = act
        val col = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(ctx, 30), dp(ctx, 40), dp(ctx, 30), dp(ctx, 36))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        col.addView(logo(ctx, 104))
        col.addView(txt(ctx, "Wonder", 42f).apply {
            typeface = su.gwarden.music.ui.Skin.typeface(ctx)
            letterSpacing = 0.04f
            gravity = Gravity.CENTER
            setPadding(0, dp(ctx, 12), 0, 0)
        })
        col.addView(txt(ctx, t(ctx, "музыка, которая слышит тебя", "music that hears you"),
            12.5f, DIM).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.05f
            setPadding(0, dp(ctx, 8), 0, dp(ctx, 30))
        })

        val vk = button(ctx, t(ctx, "Войти по ВК", "Sign in with VK"), true)
        vk.setOnClickListener { act.authBridgeVk() }
        col.addView(vk, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 54)
        ))

        val google = button(ctx, t(ctx, "Продолжить с Google", "Continue with Google"), false)
        google.setOnClickListener { signInGoogle(act) }
        col.addView(google, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 54)
        ).apply { topMargin = dp(ctx, 10) })

        val byPass = button(ctx, t(ctx, "Войти по паролю", "Sign in with password"), false)
        byPass.setOnClickListener { openSheet(act, register = false) }
        col.addView(byPass, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 54)
        ).apply { topMargin = dp(ctx, 10) })

        val reg = button(ctx, t(ctx, "Зарегистрировать", "Create account"), false)
        reg.setOnClickListener { openSheet(act, register = true) }
        col.addView(reg, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 54)
        ).apply { topMargin = dp(ctx, 10) })

        col.addView(txt(
            ctx,
            t(ctx, "Пароль вводится в VK и нам не передаётся.\nПосле входа можно привязать SoundCloud (меню ⋮ → Аккаунт).",
                "The password is entered in VK and never shared with us.\nAfter signing in you can connect SoundCloud (menu ⋮ → Account)."),
            10.5f, 0xFF5C5C5C.toInt()
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(ctx, 18), 0, 0)
            setLineSpacing(dp(ctx, 3).toFloat(), 1f)
        })

        root.addView(col, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.CENTER })
    }

    // ------------------------------------------------------- формы входа/рег.

    /**
     * Вход через Google: системный выбор аккаунта (Credential Manager).
     * Наружу уходит только id_token, подпись проверяет сервер.
     */
    private fun signInGoogle(act: MainActivity) {
        // Публичная сборка: сетевой вход через Google отключён.
        toast(act, t(act, "Вход через Google недоступен в публичной сборке Wonder.",
            "Google sign-in is unavailable in the public Wonder build."))
    }

    private fun maybeShowInterests(act: MainActivity) {
        // Сначала смотрим, не выбирал ли человек жанры/артистов раньше
        // (например, слушал гостем, а потом привязал Google). Если выбирал —
        // не заставляем проходить онбординг заново.
        ApiClient.guestPrefs { genres, artists ->
            if (!genres.isNullOrEmpty() || !artists.isNullOrEmpty()) return@guestPrefs
            ApiClient.authMeText { body ->
                val events = runCatching {
                    com.google.gson.JsonParser.parseString(body ?: "")
                        .asJsonObject.getAsJsonObject("taste")
                        ?.get("events")?.asInt ?: 0
                }.getOrDefault(0)
                if (events <= 0 && !act.isFinishing && !act.isDestroyed) {
                    act.runOnUiThread { act.authBridgeInterests() }
                }
            }
        }
    }

    private fun toast(act: MainActivity, msg: String) {
        android.widget.Toast.makeText(act, msg, android.widget.Toast.LENGTH_LONG).show()
    }

    private fun openSheet(act: MainActivity, register: Boolean) {
        val ctx = act
        val scrim = FrameLayout(ctx).apply {
            setBackgroundColor(0xE6000000.toInt())
            isClickable = true
            layoutParams = full()
            setOnClickListener { closeSheet(it) }
        }
        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(CARD, STROKE, 24)
            setPadding(dp(ctx, 22), dp(ctx, 20), dp(ctx, 22), dp(ctx, 18))
        }
        card.addView(txt(ctx, if (register) t(ctx, "Регистрация", "Create account")
        else t(ctx, "Вход по паролю", "Sign in"), 22f).apply {
            setTypeface(typeface, Typeface.BOLD)
        })
        card.addView(txt(
            ctx,
            if (register) t(ctx, "Придумайте @юзернейм и пароль", "Pick a @username and a password")
            else t(ctx, "@юзернейм и пароль", "@username and password"),
            12f, DIM
        ).apply { setPadding(0, dp(ctx, 6), 0, dp(ctx, 14)) })

        val user = field(ctx, "@username", InputType.TYPE_CLASS_TEXT)
        val pass = field(ctx, t(ctx, "Пароль", "Password"),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        card.addView(user, lp(ctx))
        card.addView(pass, lp(ctx, top = 10))

        var repeat: EditText? = null
        if (register) {
            repeat = field(ctx, t(ctx, "Повторите пароль", "Repeat password"),
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            card.addView(repeat, lp(ctx, top = 10))
        }

        val err = txt(ctx, "", 11.5f, ERR).apply {
            setPadding(dp(ctx, 2), dp(ctx, 8), dp(ctx, 2), 0)
            visibility = View.GONE
        }
        card.addView(err)

        val ok = button(ctx, if (register) t(ctx, "Создать аккаунт", "Create account")
        else t(ctx, "Войти", "Sign in"), true).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 52)
            ).apply { topMargin = dp(ctx, 14) }
        }
        card.addView(ok)

        val link = txt(
            ctx,
            if (register) t(ctx, "Уже есть аккаунт? Войти", "Already have an account? Sign in")
            else t(ctx, "Нет аккаунта? Зарегистрировать", "No account? Create one"),
            12.5f, 0xFFBEBEBE.toInt()
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(ctx, 12), 0, dp(ctx, 2))
            setOnClickListener {
                closeSheet(scrim)
                openSheet(act, register = !register)
            }
        }
        card.addView(link)

        // Клавиатура закрывала всю форму: держим карточку в ScrollView и
        // поднимаем её ровно на высоту IME (adjustResize в манифесте +
        // слушатель insets - работает и с edge-to-edge).
        val scrollCard = android.widget.ScrollView(ctx).apply {
            isFillViewport = false
            isVerticalScrollBarEnabled = false
            addView(card)
        }
        val bottom = FrameLayout(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.BOTTOM }
            setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 16))
            addView(scrollCard, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ))
        }
        scrim.addView(bottom)
        ViewCompat.setOnApplyWindowInsetsListener(scrim) { _, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            bottom.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 16) + ime)
            insets
        }
        ViewCompat.requestApplyInsets(scrim)
        content(act)?.addView(scrim)
        act.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        user.post { user.requestFocus() }

        fun fail(msg: String?) {
            err.text = msg ?: ""
            err.visibility = if (msg.isNullOrBlank()) View.GONE else View.VISIBLE
        }

        var busy = false
        ok.setOnClickListener {
            if (busy) return@setOnClickListener
            val u = user.text.toString().trim().removePrefix("@").lowercase()
            val p = pass.text.toString()
            if (!u.matches(Regex("^[a-z0-9_]{3,20}$"))) {
                fail(t(ctx, "Юзернейм: 3-20 символов, латиница, цифры и _",
                    "Username: 3-20 chars, latin letters, digits and _"))
                return@setOnClickListener
            }
            if (p.length < 6) {
                fail(t(ctx, "Пароль: минимум 6 символов", "Password: at least 6 characters"))
                return@setOnClickListener
            }
            if (register && repeat?.text?.toString() != p) {
                fail(t(ctx, "Пароли не совпадают", "Passwords don't match"))
                return@setOnClickListener
            }
            busy = true
            ok.alpha = 0.6f
            val done = { success: Boolean, msg: String? ->
                busy = false
                ok.alpha = 1f
                if (!success) {
                    fail(msg ?: t(ctx, "Не получилось", "Something went wrong"))
                } else {
                    closeSheet(scrim)
                    markOnboarded(act)
                    act.authBridgeDone(t(ctx, "Готово", "Done"))
                    if (register) act.authBridgeInterests()
                }
            }
            if (register) ApiClient.register(u, p) { s, m -> done(s, m) }
            else ApiClient.loginPassword(u, p) { s, m -> done(s, m) }
        }
    }

    private fun closeSheet(scrim: View) {
        val ctx = scrim.context
        try {
            val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(scrim.windowToken, 0)
        } catch (_: Exception) {
        }
        (scrim.parent as? ViewGroup)?.removeView(scrim)
    }

    private fun field(ctx: Context, hint: String, type: Int): EditText =
        EditText(ctx).apply {
            this.hint = hint
            setHintTextColor(0xFF6E6E6E.toInt())
            setTextColor(TXT)
            textSize = 15f
            inputType = type
            typeface = su.gwarden.music.ui.Skin.typeface(ctx)
            setSingleLine(true)
            background = rounded(0xFF1C1C1C.toInt(), STROKE, 16)
            setPadding(dp(ctx, 14), dp(ctx, 12), dp(ctx, 14), dp(ctx, 12))
        }

    private fun lp(ctx: Context, top: Int = 0): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { if (top > 0) topMargin = dp(ctx, top) }

    private fun full(): FrameLayout.LayoutParams = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
    )
}
