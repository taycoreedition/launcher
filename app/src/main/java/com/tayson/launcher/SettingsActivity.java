package com.tayson.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Native, remote-friendly settings page for TAYCOR Launcher. */
public class SettingsActivity extends Activity {
    private android.content.SharedPreferences prefs;
    private LinearLayout list;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        prefs = getSharedPreferences("tayson_launcher_prefs", MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(28), dp(20), dp(28), dp(18));
        root.setBackgroundColor(0xFF030827);

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.HORIZONTAL);
        heading.setGravity(Gravity.CENTER_VERTICAL);

        ImageView headingIcon = icon(R.drawable.ic_launcher_settings, 42);
        heading.addView(headingIcon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout headingText = new LinearLayout(this);
        headingText.setOrientation(LinearLayout.VERTICAL);
        headingText.setPadding(dp(12), 0, 0, 0);
        TextView title = new TextView(this);
        title.setText("Definições do TAYCOR");
        title.setTextColor(Color.WHITE);
        title.setTextSize(27);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        headingText.addView(title);
        TextView subtitle = new TextView(this);
        subtitle.setText("Personaliza a aparência e o funcionamento");
        subtitle.setTextColor(0xFFB9C8F5);
        subtitle.setTextSize(14);
        headingText.addView(subtitle);
        heading.addView(headingText, new LinearLayout.LayoutParams(0, -2, 1f));
        root.addView(heading, new LinearLayout.LayoutParams(-1, dp(58)));

        View line = new View(this);
        line.setBackgroundColor(0xFF273D82);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(-1, dp(1));
        lineParams.topMargin = dp(12);
        lineParams.bottomMargin = dp(10);
        root.addView(line, lineParams);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setHorizontalScrollBarEnabled(false);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        addSection("ECRÃ INICIAL", R.drawable.ic_home);
        addInfo("Banners em destaque", "320 × 180 dp • faixa horizontal compacta",
                R.drawable.ic_apps);
        addOption("Fundo e cores", backgroundSummary(),
                new String[]{"Azul e roxo", "Azul profundo"},
                new String[]{"waves", "deep"}, "background_style", R.drawable.ic_edit);

        addSection("RELÓGIO", R.drawable.ic_settings);
        addOption("Estilo do relógio", clockSummary(),
                new String[]{"Clássico", "Compacto", "Negrito"},
                new String[]{"classic", "compact", "bold"}, "clock_style", R.drawable.ic_edit);
        addOption("Formato da hora", formatSummary(),
                new String[]{"24 horas", "12 horas"},
                new String[]{"24", "12"}, "clock_format", R.drawable.ic_edit);
        addOption("Cor do relógio", colorSummary(),
                new String[]{"Branco", "Azul", "Roxo"},
                new String[]{"white", "blue", "purple"}, "clock_color", R.drawable.ic_edit);

        addSection("APLICAÇÕES", R.drawable.ic_apps);
        addInfo("Gaveta de aplicações", "Ícones de 108 × 108 dp • grelha adaptável",
                R.drawable.ic_apps);
        addInfo("Gerir destaques", "Mantém OK pressionado num banner para gerir",
                R.drawable.ic_edit);

        addSection("SISTEMA", R.drawable.ic_launcher_settings);
        addInfo("Versão", "TAYCOR Launcher 2.2", R.drawable.ic_refresh);
        addInfo("Compatibilidade", "Android 5.0 ou superior • Android 7.1",
                R.drawable.ic_toggle_on);

        addAction("Definições do Android", "Abrir as configurações do dispositivo",
                R.drawable.ic_settings, () -> {
                    try {
                        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    } catch (Exception e) {
                        Toast.makeText(this, "Não foi possível abrir as definições.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        TextView back = actionButton("Voltar ao início", R.drawable.ic_home);
        back.setOnClickListener(v -> finish());
        bottom.addView(back, new LinearLayout.LayoutParams(dp(220), dp(54)));
        LinearLayout.LayoutParams bottomParams = new LinearLayout.LayoutParams(-1, dp(62));
        bottomParams.topMargin = dp(8);
        root.addView(bottom, bottomParams);

        setContentView(root);
        if (list.getChildCount() > 1) list.getChildAt(1).requestFocus();
    }

    private void addSection(String label, int iconRes) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.HORIZONTAL);
        section.setGravity(Gravity.CENTER_VERTICAL);
        section.setPadding(dp(4), dp(12), dp(4), dp(6));
        section.addView(icon(iconRes, 18),
                new LinearLayout.LayoutParams(dp(22), dp(22)));
        TextView text = new TextView(this);
        text.setText(label);
        text.setTextColor(0xFF83A9FF);
        text.setTextSize(13);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(-2, -2);
        textParams.leftMargin = dp(8);
        section.addView(text, textParams);
        list.addView(section);
    }

    private void addOption(String title, String summary, String[] labels,
                           String[] values, String key, int iconRes) {
        LinearLayout row = makeRow(title, summary, iconRes);
        row.setOnClickListener(v -> {
            int selected = 0;
            String current = prefs.getString(key, values[Math.min(1, values.length - 1)]);
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(current)) selected = i;
            }
            new AlertDialog.Builder(this).setTitle(title)
                    .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                        prefs.edit().putString(key, values[which]).apply();
                        dialog.dismiss();
                        recreate();
                    }).setNegativeButton("Cancelar", null).show();
        });
        addRow(row, 68);
    }

    private void addInfo(String title, String summary, int iconRes) {
        addRow(makeRow(title, summary, iconRes), 62);
    }

    private void addAction(String title, String summary, int iconRes, View.OnClickListener action) {
        LinearLayout row = makeRow(title, summary, iconRes);
        row.setOnClickListener(action);
        addRow(row, 64);
    }

    private void addRow(View row, int height) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(height));
        p.bottomMargin = dp(6);
        list.addView(row, p);
    }

    private LinearLayout makeRow(String title, String summary, int iconRes) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(6), dp(16), dp(6));
        row.setFocusable(true);
        row.setClickable(true);
        row.setBackground(rowBackground(false));

        ImageView image = icon(iconRes, 26);
        row.addView(image, new LinearLayout.LayoutParams(dp(30), dp(30)));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(14), 0, 0, 0);
        TextView name = new TextView(this);
        name.setText(title);
        name.setTextColor(Color.WHITE);
        name.setTextSize(16);
        name.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        texts.addView(name);
        TextView detail = new TextView(this);
        detail.setText(summary);
        detail.setTextColor(0xFFABB9E0);
        detail.setTextSize(12);
        detail.setSingleLine(true);
        detail.setEllipsize(android.text.TextUtils.TruncateAt.END);
        texts.addView(detail);
        row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1f));

        row.setOnFocusChangeListener((v, focused) -> row.setBackground(rowBackground(focused)));
        return row;
    }

    private TextView actionButton(String title, int iconRes) {
        TextView button = new TextView(this);
        button.setText("  " + title);
        button.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
        button.setCompoundDrawablePadding(dp(10));
        button.setTextColor(Color.WHITE);
        button.setTextSize(16);
        button.setGravity(Gravity.CENTER);
        button.setFocusable(true);
        button.setBackground(rowBackground(false));
        button.setOnFocusChangeListener((v, focused) -> button.setBackground(rowBackground(focused)));
        return button;
    }

    private ImageView icon(int res, int size) {
        ImageView image = new ImageView(this);
        image.setImageResource(res);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setContentDescription(null);
        return image;
    }

    private GradientDrawable rowBackground(boolean focused) {
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                focused ? new int[]{0xFF123E91, 0xFF32106C}
                        : new int[]{0xFF0A1745, 0xFF101039});
        bg.setCornerRadius(dp(13));
        bg.setStroke(dp(focused ? 2 : 1),
                focused ? 0xFF8BEAFF : 0xFF263D83);
        return bg;
    }

    private String backgroundSummary() {
        return "deep".equals(prefs.getString("background_style", "waves"))
                ? "Azul profundo" : "Azul e roxo";
    }
    private String clockSummary() {
        String v = prefs.getString("clock_style", "classic");
        return "compact".equals(v) ? "Compacto" : ("bold".equals(v) ? "Negrito" : "Clássico");
    }
    private String formatSummary() {
        return "12".equals(prefs.getString("clock_format", "24")) ? "12 horas" : "24 horas";
    }
    private String colorSummary() {
        String v = prefs.getString("clock_color", "white");
        return "blue".equals(v) ? "Azul" : ("purple".equals(v) ? "Roxo" : "Branco");
    }

    private int dp(float n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
