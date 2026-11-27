package com.ghalibxcheats.guestreset;

import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import rikka.shizuku.Shizuku;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE = 4091;
    private static final String TARGET_DIR =
            "/storage/emulated/0/Android/data/com.dts.freefireth/files";
    private static final String TARGET =
            TARGET_DIR + "/localconfig.json";
    private static final String BACKUP =
            TARGET_DIR + "/localconfig.json.ghalibxcheats.bak";

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private TextView status;
    private TextView shizukuStatus;
    private Button installButton;
    private Button removeButton;
    private Button checkButton;

    private final Shizuku.OnRequestPermissionResultListener permissionListener =
            (requestCode, grantResult) -> runOnUiThread(() -> {
                if (requestCode == REQUEST_CODE) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        setShizukuReady();
                    } else {
                        setStatus("Shizuku permission denied.");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(9, 9, 13));
        getWindow().setNavigationBarColor(Color.rgb(9, 9, 13));
        Shizuku.addRequestPermissionResultListener(permissionListener);
        buildUi();
        refreshShizukuState();
    }

    private void buildUi() {
        int bg = Color.rgb(9, 9, 13);
        int card = Color.rgb(23, 23, 32);
        int red = Color.rgb(255, 51, 77);
        int white = Color.rgb(245, 245, 248);
        int muted = Color.rgb(164, 164, 178);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(26), dp(20), dp(28));
        root.setBackgroundColor(bg);
        scroll.addView(root);

        TextView brand = text("GHALIBXCHEATS", 24, red, true);
        root.addView(brand);
        TextView subtitle = text("GUEST RESET  •  ANDROID 14", 11, muted, true);
        LinearLayout.LayoutParams subP = lp(-2, -2);
        subP.topMargin = dp(5);
        root.addView(subtitle, subP);

        LinearLayout stateCard = panel(card);
        LinearLayout.LayoutParams cardP = lp(-1, -2);
        cardP.topMargin = dp(22);
        root.addView(stateCard, cardP);
        stateCard.addView(text("SHIZUKU CONNECTION", 12, red, true));
        shizukuStatus = text("Checking…", 14, white, true);
        LinearLayout.LayoutParams ssp = lp(-1, -2);
        ssp.topMargin = dp(8);
        stateCard.addView(shizukuStatus, ssp);

        LinearLayout fileCard = panel(card);
        LinearLayout.LayoutParams fileP = lp(-1, -2);
        fileP.topMargin = dp(12);
        root.addView(fileCard, fileP);
        fileCard.addView(text("TARGET FILE", 12, red, true));
        TextView path = text(TARGET, 12, white, false);
        path.setTextIsSelectable(true);
        LinearLayout.LayoutParams pathP = lp(-1, -2);
        pathP.topMargin = dp(8);
        fileCard.addView(path, pathP);
        TextView backupNote = text("Existing localconfig.json is backed up before replacement. Remove restores that backup when available.", 11, muted, false);
        LinearLayout.LayoutParams bn = lp(-1, -2);
        bn.topMargin = dp(9);
        fileCard.addView(backupNote, bn);

        installButton = button("INSTALL / REPLACE FILE", red);
        LinearLayout.LayoutParams ip = lp(-1, dp(52));
        ip.topMargin = dp(20);
        root.addView(installButton, ip);
        installButton.setOnClickListener(v -> runAction(true));

        removeButton = button("REMOVE / RESTORE BACKUP", 0xFF33333D);
        LinearLayout.LayoutParams rp = lp(-1, dp(52));
        rp.topMargin = dp(10);
        root.addView(removeButton, rp);
        removeButton.setOnClickListener(v -> runAction(false));

        checkButton = button("CHECK FILE STATUS", 0xFF252530);
        LinearLayout.LayoutParams cp = lp(-1, dp(48));
        cp.topMargin = dp(10);
        root.addView(checkButton, cp);
        checkButton.setOnClickListener(v -> checkFile());

        LinearLayout resultCard = panel(card);
        LinearLayout.LayoutParams resultP = lp(-1, -2);
        resultP.topMargin = dp(18);
        root.addView(resultCard, resultP);
        resultCard.addView(text("STATUS", 12, red, true));
        status = text("Waiting for Shizuku…", 13, white, false);
        LinearLayout.LayoutParams statusP = lp(-1, -2);
        statusP.topMargin = dp(8);
        resultCard.addView(status, statusP);

        TextView warning = text(
                "This writes the supplied localconfig.json into the selected Free Fire folder. Guest-reset behavior depends on the game reading these keys; the file itself cannot guarantee a reset. Keep your account details safe.",
                11, muted, false);
        warning.setPadding(dp(14), dp(14), dp(14), dp(14));
        warning.setBackground(round(0xFF211B20, 12));
        LinearLayout.LayoutParams wp = lp(-1, -2);
        wp.topMargin = dp(18);
        root.addView(warning, wp);

        setContentView(scroll);
    }

    private void refreshShizukuState() {
        if (!Shizuku.pingBinder()) {
            shizukuStatus.setText("Shizuku service is not running.");
            return;
        }
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            setShizukuReady();
        } else {
            shizukuStatus.setText("Permission required — tap a file action to request it.");
        }
    }

    private void setShizukuReady() {
        int uid = Shizuku.getUid();
        shizukuStatus.setText("Connected • " + (uid == 0 ? "Root" : "ADB / Shell"));
        setStatus("Shizuku is ready.");
    }

    private void runAction(boolean install) {
        if (!Shizuku.pingBinder()) {
            setStatus("Start Shizuku first, then return here.");
            refreshShizukuState();
            return;
        }
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(REQUEST_CODE);
            setStatus("Approve the GhalibXCheats permission prompt.");
            return;
        }

        setButtons(false);
        setStatus(install ? "Installing localconfig.json…" : "Removing file / restoring backup…");
        worker.execute(() -> {
            try {
                String result = install ? installFile() : removeFile();
                runOnUiThread(() -> {
                    setStatus(result);
                    setButtons(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setStatus("Failed: " + safeMessage(e));
                    setButtons(true);
                });
            }
        });
    }

    private String installFile() throws Exception {
        byte[] payload;
        try (InputStream in = getAssets().open("localconfig.json");
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            payload = out.toByteArray();
        }

        String cmd = "mkdir -p '" + TARGET_DIR + "'"
                + " && if [ -f '" + TARGET + "' ] && [ ! -f '" + BACKUP + "' ]; then cp '" + TARGET + "' '" + BACKUP + "'; fi"
                + " && cat > '" + TARGET + ".ghalibxcheats.tmp'"
                + " && mv '" + TARGET + ".ghalibxcheats.tmp' '" + TARGET + "'";

        Process p = startShizukuProcess(new String[]{"sh", "-c", cmd});
        try (OutputStream os = p.getOutputStream()) {
            os.write(payload);
            os.flush();
        }
        String output = readAll(p.getInputStream()) + readAll(p.getErrorStream());
        int exit = p.waitFor();
        if (exit != 0) throw new Exception("Shell exit " + exit + (output.isEmpty() ? "" : ": " + output));
        return "Installed: " + TARGET;
    }

    private String removeFile() throws Exception {
        String cmd = "if [ -f '" + BACKUP + "' ]; then mv '" + BACKUP + "' '" + TARGET + "'; "
                + "elif [ -f '" + TARGET + "' ]; then rm -f '" + TARGET + "'; fi";
        Process p = startShizukuProcess(new String[]{"sh", "-c", cmd});
        String output = readAll(p.getInputStream()) + readAll(p.getErrorStream());
        int exit = p.waitFor();
        if (exit != 0) throw new Exception("Shell exit " + exit + (output.isEmpty() ? "" : ": " + output));
        return "Remove / restore command completed.";
    }

    private void checkFile() {
        if (!Shizuku.pingBinder() ||
                Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            refreshShizukuState();
            setStatus("Shizuku permission is required to check the target folder.");
            return;
        }
        setButtons(false);
        worker.execute(() -> {
            try {
                String cmd = "[ -f '" + TARGET + "' ] && echo PRESENT || echo MISSING";
                Process p = startShizukuProcess(new String[]{"sh", "-c", cmd});
                String output = readAll(p.getInputStream()).trim();
                int exit = p.waitFor();
                String result = exit == 0 ? "Target file: " + output : "Could not read target status.";
                runOnUiThread(() -> {
                    setStatus(result);
                    setButtons(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setStatus("Check failed: " + safeMessage(e));
                    setButtons(true);
                });
            }
        });
    }

    private Process startShizukuProcess(String[] command) throws Exception {
        Method method = Shizuku.class.getDeclaredMethod(
                "newProcess", String[].class, String[].class, String.class);
        method.setAccessible(true);
        return (Process) method.invoke(null, command, null, null);
    }

    private String readAll(InputStream input) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = input.read(buf)) != -1) out.write(buf, 0, n);
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private void setButtons(boolean enabled) {
        installButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
        checkButton.setEnabled(enabled);
        installButton.setAlpha(enabled ? 1f : 0.55f);
        removeButton.setAlpha(enabled ? 1f : 0.55f);
        checkButton.setAlpha(enabled ? 1f : 0.55f);
    }

    private void setStatus(String message) {
        if (status != null) status.setText(message);
    }

    private String safeMessage(Exception e) {
        String m = e.getMessage();
        return m == null ? e.getClass().getSimpleName() : m;
    }

    private LinearLayout panel(int color) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(15), dp(15), dp(15), dp(15));
        box.setBackground(round(color, 14));
        return box;
    }

    private Button button(String label, int color) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setBackground(round(color, 12));
        return b;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w < 0 ? w : dp(w), h < 0 ? h : dp(h));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener);
        worker.shutdownNow();
        super.onDestroy();
    }
}
