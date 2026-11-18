package com.tayson.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * TV Launcher
 * Pins are stored as package names in SharedPreferences.
 * The six banner slots are favorites, not an automatically generated app list.
 */
public class MainActivity extends Activity {
    private static final String PREFS = "tayson_launcher_prefs";
    private static final String KEY_PINS = "pinned_packages";
    private static final int MAX_PINS = 6;

    private final Handler handler = new Handler();
    private final ArrayList<String> pinnedPackages = new ArrayList<>();
    private LinearLayout bannerRow;
    private TextView clockView, dateView;
    private SharedPreferences preferences;
    private BroadcastReceiver packageReceiver;
    private boolean drawerOpen = false;
    private FrameLayout appsOverlay;
    private GridView appsGrid;
    private BaseAdapter appsAdapter;
    private final ArrayList<ApplicationInfo> appsList = new ArrayList<>();

    private final int[][] gradients = {
        {0xFF071C58, 0xFF075BE8},
        {0xFF21070D, 0xFFB20B1B},
        {0xFF0757A8, 0xFF13A9F4},
        {0xFF07184F, 0xFF0876C9},
        {0xFF171717, 0xFF424242},
        {0xFF4300A8, 0xFFB000F5}
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        loadPins();
        buildInterface();
        registerPackageReceiver();
        updateClock();
    }

    private void loadPins() {
        pinnedPackages.clear();
        String saved = preferences.getString(KEY_PINS, "");
        if (!saved.isEmpty()) {
            String[] values = saved.split("\\n");
            PackageManager pm = getPackageManager();
            for (String pkg : values) {
                if (pkg == null || pkg.trim().isEmpty() || pinnedPackages.contains(pkg)) continue;
                try {
                    pm.getApplicationInfo(pkg, 0);
                    if (pinnedPackages.size() < MAX_PINS) pinnedPackages.add(pkg);
                } catch (PackageManager.NameNotFoundException ignored) {
                    // Remove apps that were uninstalled while the launcher was closed.
                }
            }
        }
        savePins();
    }

    private void savePins() {
        StringBuilder value = new StringBuilder();
        for (String pkg : pinnedPackages) {
            if (value.length() > 0) value.append('\n');
            value.append(pkg);
        }
        preferences.edit().putString(KEY_PINS, value.toString()).apply();
    }

    private void buildInterface() {
        FrameLayout root = new FrameLayout(this);
        root.setFocusable(true);
        root.addView(new WaveBackground(this), new FrameLayout.LayoutParams(-1, -1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(30), dp(20), dp(30), dp(18));
        root.addView(content, new FrameLayout.LayoutParams(-1, -1));

        FrameLayout header = new FrameLayout(this);
        content.addView(header, new LinearLayout.LayoutParams(-1, 0, 1.05f));

        LinearLayout timeBox = new LinearLayout(this);
        timeBox.setGravity(Gravity.CENTER);
        timeBox.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams timeParams = new FrameLayout.LayoutParams(-1, -1);
        timeParams.leftMargin = dp(200);
        timeParams.rightMargin = dp(200);
        header.addView(timeBox, timeParams);

        dateView = new TextView(this);
        dateView.setTextColor(Color.WHITE);
        dateView.setTextSize(22);
        dateView.setGravity(Gravity.CENTER);
        dateView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        timeBox.addView(dateView);

        clockView = new TextView(this);
        clockView.setTextColor(Color.WHITE);
        clockView.setTextSize(105);
        clockView.setGravity(Gravity.CENTER);
        clockView.setLetterSpacing(-0.035f);
        clockView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        clockView.setShadowLayer(dp(16), 0, 0, 0xFF167BFF);
        timeBox.addView(clockView);

        LinearLayout quick = new LinearLayout(this);
        quick.setGravity(Gravity.TOP | Gravity.RIGHT);
        FrameLayout.LayoutParams quickParams = new FrameLayout.LayoutParams(-2, -2,
                Gravity.TOP | Gravity.RIGHT);
        header.addView(quick, quickParams);
        quick.addView(quickButton(R.drawable.ic_wifi, "Wi-Fi", v -> openWifi()),
                new LinearLayout.LayoutParams(dp(86), dp(90)));
        View spacer = new View(this);
        quick.addView(spacer, new LinearLayout.LayoutParams(dp(10), 1));
        quick.addView(quickButton(R.drawable.ic_settings, "Definições", v -> openSettings()),
                new LinearLayout.LayoutParams(dp(102), dp(90)));

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(false);
        horizontal.setClipToPadding(false);
        horizontal.setFillViewport(false);
        content.addView(horizontal, new LinearLayout.LayoutParams(-1, dp(190)));

        bannerRow = new LinearLayout(this);
        bannerRow.setOrientation(LinearLayout.HORIZONTAL);
        bannerRow.setGravity(Gravity.CENTER_VERTICAL);
        horizontal.addView(bannerRow, new HorizontalScrollView.LayoutParams(-2, -1));

        View breathingRoom = new View(this);
        content.addView(breathingRoom, new LinearLayout.LayoutParams(-1, 0, 0.82f));

        LinearLayout footer = new LinearLayout(this);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        content.addView(footer, new LinearLayout.LayoutParams(-1, dp(64)));

        ImageView appsIcon = new ImageView(this);
        appsIcon.setImageResource(R.drawable.ic_apps);
        appsIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        appsIcon.setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable iconBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF3D00B7, 0xFF8900F2, 0xFF1800A8});
        iconBg.setCornerRadius(dp(18));
        iconBg.setStroke(dp(2), 0xFF9C38FF);
        appsIcon.setBackground(iconBg);
        appsIcon.setFocusable(true);
        footer.addView(appsIcon, new LinearLayout.LayoutParams(dp(66), dp(60)));

        TextView appsText = new TextView(this);
        appsText.setText("Apps");
        appsText.setTextColor(Color.WHITE);
        appsText.setTextSize(20);
        appsText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        LinearLayout.LayoutParams appsTextParams = new LinearLayout.LayoutParams(dp(78), -2);
        appsTextParams.leftMargin = dp(14);
        footer.addView(appsText, appsTextParams);

        View footerLine = new View(this);
        footerLine.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF182EFF, 0xFF9B20FF, 0xFF2515A8}));
        footer.addView(footerLine, new LinearLayout.LayoutParams(0, dp(2), 1));

        View.OnClickListener openDrawer = v -> showAppDrawer();
        appsIcon.setOnClickListener(openDrawer);
        appsText.setOnClickListener(openDrawer);
        appsIcon.setOnFocusChangeListener((v, focused) -> animateFocus(appsIcon, focused));

        buildAppsOverlay();
        root.addView(appsOverlay, new FrameLayout.LayoutParams(-1, -1));

        setContentView(root);
        refreshPinnedBanners();
        root.requestFocus();
    }

    private void refreshPinnedBanners() {
        if (bannerRow == null) return;
        bannerRow.removeAllViews();
        PackageManager pm = getPackageManager();

        for (int i = 0; i < pinnedPackages.size(); i++) {
            final String pkg = pinnedPackages.get(i);
            ApplicationInfo info;
            try { info = pm.getApplicationInfo(pkg, 0); }
            catch (Exception e) { continue; }

            final int index = i;
            LinearLayout card = createBanner(pkg, info, i);
            card.setOnClickListener(v -> launchPackage(pkg));
            card.setOnLongClickListener(v -> {
                showPinnedOptions(pkg, index);
                return true;
            });

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dp(242), dp(150));
            cardParams.rightMargin = dp(14);
            if (i == 0) cardParams.leftMargin = dp(2);
            bannerRow.addView(card, cardParams);

            // Subtle low-cost reflection strip below the banner.
            View reflection = new View(this);
            GradientDrawable reflectionBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{0x553B7CFF, 0x002020FF});
            reflectionBg.setCornerRadius(dp(14));
            reflection.setBackground(reflectionBg);
            LinearLayout.LayoutParams reflectionParams = new LinearLayout.LayoutParams(dp(242), dp(12));
            reflectionParams.rightMargin = dp(14);
            reflection.setAlpha(0.55f);
            bannerRow.addView(reflection, reflectionParams);
        }

        if (pinnedPackages.size() < MAX_PINS) {
            LinearLayout add = new LinearLayout(this);
            add.setOrientation(LinearLayout.VERTICAL);
            add.setGravity(Gravity.CENTER);
            GradientDrawable addBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    new int[]{0xCC07133D, 0xCC15105C});
            addBg.setCornerRadius(dp(22));
            addBg.setStroke(dp(2), 0xFF536DFF);
            add.setBackground(addBg);
            add.setFocusable(true);
            add.setOnClickListener(v -> showAppPicker(null));

            ImageView addIcon = new ImageView(this);
            addIcon.setImageResource(R.drawable.ic_add);
            addIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            add.addView(addIcon, new LinearLayout.LayoutParams(dp(56), dp(56)));

            TextView addText = new TextView(this);
            addText.setText("Adicionar");
            addText.setTextColor(Color.WHITE);
            addText.setTextSize(18);
            addText.setGravity(Gravity.CENTER);
            addText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            LinearLayout.LayoutParams addTextParams = new LinearLayout.LayoutParams(-2, -2);
            addTextParams.topMargin = dp(6);
            add.addView(addText, addTextParams);

            add.setOnFocusChangeListener((v, focused) -> {
                animateFocus(add, focused);
                addBg.setStroke(focused ? dp(3) : dp(2), focused ? 0xFF8BEAFF : 0xFF536DFF);
            });
            LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(dp(150), dp(150));
            addParams.leftMargin = dp(3);
            bannerRow.addView(add, addParams);
        }
    }

    private LinearLayout createBanner(String pkg, ApplicationInfo info, int index) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setFocusable(true);
        card.setClickable(true);
        card.setLongClickable(true);
        card.setPadding(dp(8), dp(8), dp(8), dp(7));

        int[] colors = gradients[index % gradients.length];
        GradientDrawable background = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{colors[0], colors[1]});
        background.setCornerRadius(dp(22));
        background.setStroke(dp(1), 0xFF526DFF);
        card.setBackground(background);
        card.setElevation(dp(4));

        ImageView icon = new ImageView(this);
        try { icon.setImageDrawable(getPackageManager().getApplicationIcon(info)); }
        catch (Exception ignored) { }
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        card.addView(icon, new LinearLayout.LayoutParams(dp(62), dp(62)));

        TextView label = new TextView(this);
        label.setText(getPackageManager().getApplicationLabel(info));
        label.setTextColor(Color.WHITE);
        label.setTextSize(18);
        label.setGravity(Gravity.CENTER);
        label.setSingleLine(true);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        label.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-1, -2);
        labelParams.topMargin = dp(9);
        card.addView(label, labelParams);

        card.setOnFocusChangeListener((v, focused) -> {
            animateFocus(card, focused);
            GradientDrawable focusedBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    new int[]{colors[0], colors[1]});
            focusedBg.setCornerRadius(dp(22));
            focusedBg.setStroke(focused ? dp(3) : dp(1), focused ? 0xFF8BEAFF : 0xFF526DFF);
            card.setBackground(focusedBg);
            card.setElevation(focused ? dp(12) : dp(4));
        });
        return card;
    }

    private void animateFocus(View view, boolean focused) {
        view.animate().scaleX(focused ? 1.055f : 1f).scaleY(focused ? 1.055f : 1f)
                .setDuration(150).start();
    }

    private LinearLayout quickButton(int iconRes, String label, View.OnClickListener listener) {
        LinearLayout button = new LinearLayout(this);
        button.setOrientation(LinearLayout.VERTICAL);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(4), dp(8), dp(4), dp(6));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xDD07113D, 0xDD090B35});
        bg.setCornerRadius(dp(18));
        bg.setStroke(dp(1), 0xFF354DFF);
        button.setBackground(bg);
        button.setFocusable(true);
        button.setOnClickListener(listener);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        button.addView(icon, new LinearLayout.LayoutParams(dp(34), dp(34)));

        TextView text = new TextView(this);
        text.setText(label);
        text.setTextColor(Color.WHITE);
        text.setTextSize(14);
        text.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(-2, -2);
        textParams.topMargin = dp(4);
        button.addView(text, textParams);

        button.setOnFocusChangeListener((v, focused) -> {
            animateFocus(button, focused);
            bg.setStroke(focused ? dp(3) : dp(1), focused ? 0xFF8BEAFF : 0xFF354DFF);
        });
        return button;
    }

    private void showAppPicker(String replacePackage) {
        List<ApplicationInfo> available = getLaunchableApps();
        ArrayList<ApplicationInfo> filtered = new ArrayList<>();
        for (ApplicationInfo info : available) {
            String pkg = info.packageName;
            if (replacePackage != null && replacePackage.equals(pkg)) continue;
            if (replacePackage == null && pinnedPackages.contains(pkg)) continue;
            filtered.add(info);
        }
        if (filtered.isEmpty()) {
            Toast.makeText(this, "Não existem outras aplicações disponíveis.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] labels = new String[filtered.size()];
        for (int i = 0; i < filtered.size(); i++)
            labels[i] = String.valueOf(getPackageManager().getApplicationLabel(filtered.get(i)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(replacePackage == null ? "Adicionar aplicação" : "Substituir aplicação")
                .setItems(labels, (d, which) -> {
                    String selected = filtered.get(which).packageName;
                    if (replacePackage == null) {
                        if (pinnedPackages.size() < MAX_PINS && !pinnedPackages.contains(selected))
                            pinnedPackages.add(selected);
                    } else {
                        int at = pinnedPackages.indexOf(replacePackage);
                        if (at >= 0 && !pinnedPackages.contains(selected))
                            pinnedPackages.set(at, selected);
                    }
                    savePins();
                    refreshPinnedBanners();
                })
                .setNegativeButton("Cancelar", null).create();
        dialog.show();
    }

    private void showPinnedOptions(String pkg, int index) {
        String[] actions = {"Substituir", "Remover", "Mover para a esquerda", "Mover para a direita"};
        new AlertDialog.Builder(this).setTitle("Gerir aplicação")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        showAppPicker(pkg);
                    } else if (which == 1) {
                        pinnedPackages.remove(pkg);
                        savePins();
                        refreshPinnedBanners();
                    } else if (which == 2 && index > 0) {
                        Collections.swap(pinnedPackages, index, index - 1);
                        savePins();
                        refreshPinnedBanners();
                    } else if (which == 3 && index < pinnedPackages.size() - 1) {
                        Collections.swap(pinnedPackages, index, index + 1);
                        savePins();
                        refreshPinnedBanners();
                    }
                }).setNegativeButton("Cancelar", null).show();
    }

    private List<ApplicationInfo> getLaunchableApps() {
        ArrayList<ApplicationInfo> result = new ArrayList<>();
        PackageManager pm = getPackageManager();
        for (ApplicationInfo info : pm.getInstalledApplications(0)) {
            if (pm.getLaunchIntentForPackage(info.packageName) != null) result.add(info);
        }
        Collections.sort(result, (a, b) -> String.valueOf(pm.getApplicationLabel(a))
                .compareToIgnoreCase(String.valueOf(pm.getApplicationLabel(b))));
        return result;
    }

    private void showAppDrawer() {
        refreshAppsGrid();
        appsOverlay.setVisibility(View.VISIBLE);
        appsGrid.requestFocus();
    }

    private void hideAppDrawer() {
        if (appsOverlay == null) return;
        appsOverlay.setVisibility(View.GONE);
        if (appsGrid != null) appsGrid.setSelection(0);
    }

    private boolean isAppDrawerOpen() {
        return appsOverlay != null && appsOverlay.getVisibility() == View.VISIBLE;
    }

    private void buildAppsOverlay() {
        appsOverlay = new FrameLayout(this);
        appsOverlay.setBackgroundColor(0xFF020526);
        appsOverlay.setVisibility(View.GONE);
        appsOverlay.setFocusable(true);
        appsOverlay.setClickable(true);

        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.apps_background);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        appsOverlay.addView(background, new FrameLayout.LayoutParams(-1, -1));

        View scrim = new View(this);
        scrim.setBackgroundColor(0xA6020526);
        appsOverlay.addView(scrim, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(30), dp(24), dp(30), dp(18));
        appsOverlay.addView(column, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this);
        title.setText("Aplicações");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        top.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView close = new TextView(this);
        close.setText("Fechar");
        close.setTextColor(Color.WHITE);
        close.setTextSize(18);
        close.setGravity(Gravity.CENTER);
        close.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        GradientDrawable closeBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xDD07113D, 0xDD090B35});
        closeBg.setCornerRadius(dp(18));
        closeBg.setStroke(dp(1), 0xFF354DFF);
        close.setBackground(closeBg);
        close.setPadding(dp(28), 0, dp(28), 0);
        close.setFocusable(true);
        close.setOnClickListener(v -> hideAppDrawer());
        close.setOnFocusChangeListener((v, focused) -> {
            animateFocus(close, focused);
            closeBg.setStroke(focused ? dp(3) : dp(1), focused ? 0xFF8BEAFF : 0xFF354DFF);
        });
        top.addView(close, new LinearLayout.LayoutParams(-2, dp(56)));
        column.addView(top, new LinearLayout.LayoutParams(-1, -2));

        View gap = new View(this);
        column.addView(gap, new LinearLayout.LayoutParams(-1, dp(18)));

        appsGrid = new GridView(this);
        appsGrid.setNumColumns(4);
        appsGrid.setHorizontalSpacing(dp(16));
        appsGrid.setVerticalSpacing(dp(16));
        appsGrid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        appsGrid.setSelector(android.R.color.transparent);
        appsGrid.setOnItemClickListener((parent, view, position, id) -> {
            String pkg = appsList.get(position).packageName;
            hideAppDrawer();
            launchPackage(pkg);
        });
        column.addView(appsGrid, new LinearLayout.LayoutParams(-1, 0, 1f));
    }

    private void refreshAppsGrid() {
        appsList.clear();
        appsList.addAll(getLaunchableApps());
        if (appsAdapter == null) {
            appsAdapter = new BaseAdapter() {
                @Override public int getCount() { return appsList.size(); }
                @Override public Object getItem(int position) { return appsList.get(position); }
                @Override public long getItemId(int position) { return position; }
                @Override public View getView(int position, View convertView, ViewGroup parent) {
                    return createAppCard(appsList.get(position), position);
                }
            };
            appsGrid.setAdapter(appsAdapter);
        } else {
            appsAdapter.notifyDataSetChanged();
        }
    }

    private LinearLayout createAppCard(ApplicationInfo info, int index) {
        PackageManager pm = getPackageManager();
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setFocusable(true);
        card.setClickable(true);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.setLayoutParams(new AbsListView.LayoutParams(-1, dp(150)));

        int[] colors = gradients[index % gradients.length];
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{colors[0], colors[1]});
        bg.setCornerRadius(dp(22));
        bg.setStroke(dp(1), 0xFF526DFF);
        card.setBackground(bg);

        ImageView icon = new ImageView(this);
        try { icon.setImageDrawable(pm.getApplicationIcon(info)); }
        catch (Exception ignored) { }
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        card.addView(icon, new LinearLayout.LayoutParams(dp(64), dp(64)));

        TextView label = new TextView(this);
        label.setText(pm.getApplicationLabel(info));
        label.setTextColor(Color.WHITE);
        label.setTextSize(18);
        label.setGravity(Gravity.CENTER);
        label.setSingleLine(true);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        label.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-1, -2);
        labelParams.topMargin = dp(9);
        card.addView(label, labelParams);

        card.setOnFocusChangeListener((v, focused) -> {
            animateFocus(card, focused);
            bg.setStroke(focused ? dp(3) : dp(1), focused ? 0xFF8BEAFF : 0xFF526DFF);
            card.setElevation(focused ? dp(12) : dp(4));
        });
        return card;
    }

    private void launchPackage(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try { startActivity(intent); }
            catch (Exception e) { Toast.makeText(this, "Não foi possível abrir a aplicação.", Toast.LENGTH_SHORT).show(); }
        } else {
            Toast.makeText(this, "Esta aplicação já não está instalada.", Toast.LENGTH_SHORT).show();
            pinnedPackages.remove(packageName);
            savePins();
            refreshPinnedBanners();
        }
    }

    private void registerPackageReceiver() {
        packageReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                if (intent == null) return;
                String pkg = intent.getData() != null ? intent.getData().getSchemeSpecificPart() : null;
                if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction()) && pkg != null
                        && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    pinnedPackages.remove(pkg);
                    savePins();
                }
                refreshPinnedBanners();
                if (isAppDrawerOpen()) refreshAppsGrid();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        registerReceiver(packageReceiver, filter);
    }

    private void updateClock() {
        Date now = new Date();
        clockView.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
        dateView.setText(new SimpleDateFormat("EEE, dd 'de' MMMM", new Locale("pt", "PT"))
                .format(now).replace(".", ""));
        handler.postDelayed(this::updateClock, 1000);
    }

    private void openWifi() {
        try { startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)); }
        catch (Exception e) { openSettings(); }
    }
    private void openSettings() {
        try { startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)); }
        catch (Exception ignored) { }
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Back closes the apps page first; at launcher home it stays here.
            if (isAppDrawerOpen()) hideAppDrawer();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (packageReceiver != null) {
            try { unregisterReceiver(packageReceiver); } catch (Exception ignored) { }
        }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private class WaveBackground extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        WaveBackground(Context context) { super(context); }
        @Override protected void onDraw(Canvas canvas) {
            int w = getWidth(), h = getHeight();
            canvas.drawColor(0xFF020526);
            paint.setShader(new RadialGradient(w * .52f, h * .42f, w * .8f,
                    new int[]{0xFF092B9B, 0xFF06145A, 0xFF02031D}, null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);
            drawWave(canvas, h * .23f, 0xFF174BFF, 0xFF00A8FF, 6f, .13f);
            drawWave(canvas, h * .31f, 0xFF8A18FF, 0xFF1B5CFF, 3f, .17f);
            drawWave(canvas, h * .78f, 0xFF075CFF, 0xFF8C18FF, 5f, .10f);
            drawWave(canvas, h * .86f, 0xFF1A5DFF, 0xFFB000FF, 3f, .13f);
        }
        private void drawWave(Canvas canvas, float base, int c1, int c2, float width, float amp) {
            float w = getWidth(), h = getHeight();
            path.reset();
            path.moveTo(0, base);
            path.cubicTo(w * .18f, base - h * amp, w * .25f, base + h * amp,
                    w * .48f, base);
            path.cubicTo(w * .70f, base + h * amp * .75f, w * .80f,
                    base - h * amp * 1.3f, w, base - h * amp * .25f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(width);
            paint.setShader(new android.graphics.LinearGradient(0, base, w, base + h * .1f,
                    c1, c2, Shader.TileMode.CLAMP));
            paint.setShadowLayer(width * 3, 0, 0, c2);
            canvas.drawPath(path, paint);
            paint.clearShadowLayer();
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
        }
    }
}
