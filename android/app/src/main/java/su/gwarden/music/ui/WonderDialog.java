package su.gwarden.music.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import su.gwarden.music.R;

/**
 * Единый стиль диалогов Wonder вместо системных AlertDialog.
 *
 * Тёмная карточка bg_sheet, крупный заголовок, серый подзаголовок,
 * строки списка и белая «пилюля» основной кнопки. Так меню импорта,
 * настройки и выбор плейлистов выглядят одинаково.
 */
public final class WonderDialog {

    public interface OnItem {
        void onItem(int which);
    }

    public interface OnText {
        void onText(String text);
    }

    private WonderDialog() {
    }

    private static int dp(Activity a, int value) {
        return (int) (value * a.getResources().getDisplayMetrics().density);
    }

    /** Список пунктов: «Импорт», «Spotify», выбор плейлиста и т.п. */
    public static Dialog list(Activity activity, String title, String subtitle,
                              String[] items, final OnItem onItem) {
        final Holder h = new Holder(activity, title, subtitle);
        ScrollView scroll = new ScrollView(activity) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int max = (int) (getResources().getDisplayMetrics().heightPixels * 0.58f);
                super.onMeasure(widthMeasureSpec,
                        View.MeasureSpec.makeMeasureSpec(max, View.MeasureSpec.AT_MOST));
            }
        };
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout col = new LinearLayout(activity);
        col.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(col, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        for (int i = 0; i < items.length; i++) {
            final int which = i;
            TextView row = new TextView(activity);
            row.setText(items[i]);
            row.setTextSize(15f);
            row.setTextColor(0xFFF2F2F2);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(activity, 14), dp(activity, 13),
                    dp(activity, 14), dp(activity, 13));
            row.setMaxLines(2);
            row.setEllipsize(TextUtils.TruncateAt.END);
            row.setBackgroundResource(R.drawable.bg_row_ripple);
            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    h.dialog.dismiss();
                    onItem.onItem(which);
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                lp.topMargin = dp(activity, 6);
            }
            col.addView(row, lp);
        }
        if (items.length == 0) {
            TextView empty = new TextView(activity);
            empty.setText("Пока пусто");
            empty.setTextSize(13f);
            empty.setTextColor(0xFF8A8A8A);
            empty.setPadding(dp(activity, 14), dp(activity, 10), 0, dp(activity, 10));
            col.addView(empty);
        }
        h.body(scroll, 12);
        h.buttons("Закрыть", null, null, null);
        return h.show(null);
    }

    /** Поле ввода: адрес сервера, ссылка на плейлист, client id и т.п. */
    public static Dialog input(Activity activity, String title, String message,
                               String hint, String initial, String positive,
                               final OnText onText) {
        final Holder h = new Holder(activity, title, message);
        final EditText input = new EditText(activity);
        input.setHint(hint);
        input.setText(initial == null ? "" : initial);
        input.setSingleLine(true);
        input.setTextSize(15f);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(0xFF8A8A8A);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setPadding(dp(activity, 16), dp(activity, 12),
                dp(activity, 16), dp(activity, 12));
        input.setBackgroundResource(R.drawable.bg_field);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 50));
        lp.topMargin = dp(activity, 12);
        h.card.addView(input, lp);
        h.buttons(positive, new Runnable() {
            @Override
            public void run() {
                onText.onText(input.getText().toString().trim());
            }
        }, "Отмена", null);
        Dialog dialog = h.show(null);
        input.requestFocus();
        input.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    InputMethodManager imm = (InputMethodManager)
                            h.activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(input, 0);
                    }
                } catch (Throwable ignored) {
                }
            }
        }, 160L);
        return dialog;
    }

    /** Многострочный текст: проверка распознанного списка треков. */
    public static Dialog text(Activity activity, String title, String message,
                              String initial, String positive, final OnText onText) {
        final Holder h = new Holder(activity, title, message);
        final EditText input = new EditText(activity);
        input.setText(initial == null ? "" : initial);
        input.setHint("Artist — Title\nArtist — Title");
        input.setTextSize(13.5f);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(0xFF8A8A8A);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setPadding(dp(activity, 14), dp(activity, 12),
                dp(activity, 14), dp(activity, 12));
        input.setBackgroundResource(R.drawable.bg_field);
        input.setMinLines(5);
        input.setMaxLines(10);
        input.setVerticalScrollBarEnabled(true);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 230));
        lp.topMargin = dp(activity, 12);
        h.card.addView(input, lp);
        h.buttons(positive, new Runnable() {
            @Override
            public void run() {
                onText.onText(input.getText().toString().trim());
            }
        }, "Отмена", null);
        Dialog dialog = h.show(null);
        input.requestFocus();
        input.setSelection(input.getText().length());
        input.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    InputMethodManager imm = (InputMethodManager)
                            h.activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(input, 0);
                    }
                } catch (Throwable ignored) {
                }
            }
        }, 160L);
        return dialog;
    }

    /** Свой View внутри такой же карточки: QR, карточка аккаунта, статус VK. */
    public static Dialog custom(Activity activity, String title, View body,
                                String positive, Runnable onPositive,
                                String negative, Runnable onNegative,
                                DialogInterface.OnDismissListener onDismiss) {
        Holder h = new Holder(activity, title, null);
        h.body(body, 10);
        h.buttons(positive, onPositive, negative, onNegative);
        return h.show(onDismiss);
    }

    /** Сообщение с одной кнопкой. */
    public static Dialog message(Activity activity, String title, String message,
                                 String positive, Runnable onPositive) {
        LinearLayout body = new LinearLayout(activity);
        body.setOrientation(LinearLayout.VERTICAL);
        TextView tv = new TextView(activity);
        tv.setText(message == null ? "" : message);
        tv.setTextSize(13f);
        tv.setTextColor(0xFFB0B0B0);
        tv.setLineSpacing(dp(activity, 3), 1f);
        body.addView(tv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return custom(activity, title, body, positive, onPositive, null, null, null);
    }

    private static final class Holder {
        final Activity activity;
        final Dialog dialog;
        final LinearLayout card;

        Holder(Activity activity, String title, String message) {
            this.activity = activity;
            this.dialog = new Dialog(activity);
            try {
                Skin.INSTANCE.prepare(dialog);
            } catch (Throwable ignored) {
            }
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            FrameLayout outer = new FrameLayout(activity);
            outer.setPadding(dp(activity, 20), dp(activity, 8),
                    dp(activity, 20), dp(activity, 8));

            card = new LinearLayout(activity);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_sheet);
            card.setPadding(dp(activity, 20), dp(activity, 18),
                    dp(activity, 20), dp(activity, 16));
            outer.addView(card, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            dialog.setContentView(outer);

            if (!TextUtils.isEmpty(title)) {
                TextView tv = new TextView(activity);
                tv.setText(title);
                tv.setTextSize(20f);
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
                card.addView(tv, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            if (!TextUtils.isEmpty(message)) {
                TextView sub = new TextView(activity);
                sub.setText(message);
                sub.setTextSize(12.5f);
                sub.setTextColor(0xFF8A8A8A);
                sub.setLineSpacing(dp(activity, 3), 1f);
                sub.setPadding(0, dp(activity, 6), 0, 0);
                card.addView(sub, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }

        void body(View view, int topMargin) {
            if (view == null) {
                return;
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(activity, topMargin);
            card.addView(view, lp);
        }

        void buttons(String positive, final Runnable onPositive,
                     String negative, final Runnable onNegative) {
            boolean hasPos = !TextUtils.isEmpty(positive);
            boolean hasNeg = !TextUtils.isEmpty(negative);
            if (!hasPos && !hasNeg) {
                return;
            }
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 50));
            rowLp.topMargin = dp(activity, 16);

            if (hasNeg) {
                TextView cancel = button(negative, false);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (onNegative != null) {
                            onNegative.run();
                        }
                    }
                });
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, hasPos ? 1f : 1f);
                row.addView(cancel, lp);
            }
            if (hasPos) {
                TextView ok = button(positive, true);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (onPositive != null) {
                            onPositive.run();
                        }
                    }
                });
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, 1.4f);
                if (hasNeg) {
                    lp.leftMargin = dp(activity, 10);
                }
                row.addView(ok, lp);
            }
            card.addView(row, rowLp);
        }

        private TextView button(String text, boolean primary) {
            TextView btn = new TextView(activity);
            btn.setText(text);
            btn.setTextSize(14.5f);
            btn.setGravity(Gravity.CENTER);
            btn.setClickable(true);
            btn.setFocusable(true);
            btn.setMaxLines(1);
            btn.setEllipsize(TextUtils.TruncateAt.END);
            if (primary) {
                btn.setTextColor(0xFF101010);
                btn.setTypeface(btn.getTypeface(), android.graphics.Typeface.BOLD);
                btn.setBackgroundResource(R.drawable.bg_btn);
            } else {
                btn.setTextColor(0xFFBEBEBE);
                btn.setBackgroundResource(R.drawable.bg_field);
            }
            return btn;
        }

        Dialog show(DialogInterface.OnDismissListener onDismiss) {
            Window w = dialog.getWindow();
            if (w != null) {
                w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                int width = Math.min(activity.getResources().getDisplayMetrics().widthPixels
                        - dp(activity, 40), dp(activity, 520));
                w.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                w.setGravity(Gravity.CENTER);
                w.setDimAmount(0.6f);
            }
            if (onDismiss != null) {
                dialog.setOnDismissListener(onDismiss);
            }
            dialog.show();
            try {
                Skin.INSTANCE.applyToDialog(dialog);
            } catch (Throwable ignored) {
            }
            return dialog;
        }
    }
}
