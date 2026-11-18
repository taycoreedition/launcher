package com.tayson.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Native settings page for TAYCOR Launcher. */
public class SettingsActivity extends Activity {
    private android.content.SharedPreferences prefs;
    private LinearLayout list;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        prefs = getSharedPreferences("tayson_launcher_prefs", MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(34), dp(24), dp(34), dp(24));
        root.setBackgroundColor(0xFF030827);

        TextView title = new TextView(this);
        title.setText("Definições do Launcher");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("Personaliza os banners e o relógio do TAYCOR");
        subtitle.setTextColor(0xFFB9C8F5);
        subtitle.setTextSize(16);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(-1, -2);
        subParams.topMargin = dp(5);
        subParams.bottomMargin = dp(18);
        root.addView(subtitle, subParams);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        addSection("APARÊNCIA");
        addOption("Tamanho dos banners", bannerSummary(), new String[]{"Pequeno", "Médio", "Grande"},
                new String[]{"small", "medium", "large"}, "banner_size");
        addOption("Fundo e cores", backgroundSummary(), new String[]{"Azul e roxo", "Azul profundo"},
                new String[]{"waves", "deep"}, "background_style");

        addSection("RELÓGIO");
        addOption("Estilo do relógio", clockSummary(), new String[]{"Clássico", "Compacto", "Negrito"},
                new String[]{"classic", "compact", "bold"}, "clock_style");
        addOption("Formato da hora", formatSummary(), new String[]{"24 horas", "12 horas"},
                new String[]{"24", "12"}, "clock_format");
        addOption("Cor do relógio", colorSummary(), new String[]{"Branco", "Azul", "Roxo"},
                new String[]{"white", "blue", "purple"}, "clock_color");

        addSection("APLICAÇÕES");
        addInfo("Gerir destaques", "No início, mantém OK pressionado num banner para substituir, remover ou mover");

        addSection("SISTEMA");
        addInfo("Versão", "TAYCOR Launcher 2.1");
        addInfo("Compatibilidade", "Android 5.0 ou superior • otimizado para Android 7.1");
        TextView androidSettings = makeRow("Definições do Android", "Abrir as configurações gerais do dispositivo");
        androidSettings.setOnClickListener(v -> {
            try { startActivity(new android.content.Intent(android.provider.Settings.ACTION_SETTINGS)); }
            catch (Exception e) { Toast.makeText(this, "Não foi possível abrir as definições.", Toast.LENGTH_SHORT).show(); }
        });
        list.addView(androidSettings, new LinearLayout.LayoutParams(-1, dp(64)));

        TextView back = makeRow("Voltar ao início", "Guardar e regressar ao launcher");
        back.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(-1, dp(64));
        backParams.topMargin = dp(12);
        root.addView(back, backParams);
        setContentView(root);
        if (list.getChildCount() > 1) list.getChildAt(1).requestFocus();
    }

    private void addSection(String label) {
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextColor(0xFF83A9FF);
        t.setTextSize(13);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setLetterSpacing(0.12f);
        t.setPadding(dp(4), dp(13), dp(4), dp(7));
        list.addView(t);
    }

    private void addOption(String title, String summary, String[] labels, String[] values, String key) {
        TextView row = makeRow(title, summary);
        row.setOnClickListener(v -> {
            int selected = 0;
            String current = prefs.getString(key, values[Math.min(1, values.length - 1)]);
            for (int i = 0; i < values.length; i++) if (values[i].equals(current)) selected = i;
            new AlertDialog.Builder(this).setTitle(title).setSingleChoiceItems(labels, selected, (dialog, which) -> {
                prefs.edit().putString(key, values[which]).apply();
                dialog.dismiss();
                recreate();
            }).setNegativeButton("Cancelar", null).show();
        });
        list.addView(row, new LinearLayout.LayoutParams(-1, dp(68)));
    }

    private void addInfo(String title, String summary) {
        list.addView(makeRow(title, summary), new LinearLayout.LayoutParams(-1, dp(64)));
    }

    private TextView makeRow(String title, String summary) {
        TextView row = new TextView(this);
        row.setText(title + "\n" + summary);
        row.setTextColor(Color.WHITE);
        row.setTextSize(17);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), dp(7), dp(18), dp(7));
        row.setFocusable(true);
        row.setClickable(true);
        row.setBackground(rowBackground(false));
        row.setOnFocusChangeListener((v, focused) -> row.setBackground(rowBackground(focused)));
        return row;
    }

    private GradientDrawable rowBackground(boolean focused) {
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                focused ? new int[]{0xFF123E91, 0xFF32106C} : new int[]{0xFF0A1745, 0xFF101039});
        bg.setCornerRadius(dp(15));
        bg.setStroke(dp(focused ? 2 : 1), focused ? 0xFF8BEAFF : 0xFF263D83);
        return bg;
    }

    private String bannerSummary() {
        String v = prefs.getString("banner_size", "medium");
        return "small".equals(v) ? "Pequeno" : ("large".equals(v) ? "Grande" : "Médio");
    }
    private String backgroundSummary() {
        return "deep".equals(prefs.getString("background_style", "waves")) ? "Azul profundo" : "Azul e roxo";
    }
    private String clockSummary() {
        String v = prefs.getString("clock_style", "classic");
        return "compact".equals(v) ? "Compacto" : ("bold".equals(v) ? "Negrito" : "Clássico");
    }
    private String formatSummary() { return "12".equals(prefs.getString("clock_format", "24")) ? "12 horas" : "24 horas"; }
    private String colorSummary() {
        String v = prefs.getString("clock_color", "white");
        return "blue".equals(v) ? "Azul" : ("purple".equals(v) ? "Roxo" : "Branco");
    }
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { finish(); return true; }
        return super.onKeyDown(keyCode, event);
    }
}
