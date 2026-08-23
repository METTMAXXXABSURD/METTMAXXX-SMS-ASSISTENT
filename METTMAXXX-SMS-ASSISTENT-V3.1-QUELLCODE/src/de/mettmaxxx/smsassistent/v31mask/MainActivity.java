package de.mettmaxxx.smsassistent.v31mask;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_SMS = 1001;
    private static final int RED = Color.rgb(255, 18, 42);
    private static final int RED_HOT = Color.rgb(255, 54, 70);
    private static final int PANEL = Color.rgb(8, 9, 12);
    private static final int WHITE = Color.rgb(239, 241, 245);
    private static final int MUTED = Color.rgb(148, 152, 160);

    private EditText phone;
    private EditText message;
    private TextView charCount;
    private TextView sentCountView;
    private TextView maskedPhone;
    private int sentCount = 0;
    private boolean phoneHidden = false;
    private String phoneCache = "";
    private ArrowDecor arrowDecor;
    private SendGlow sendGlow;
    private CounterFrame counterFrame;
    private ImageButton sendButton;
    private boolean soundEnabled = true;
    private TextView soundOn;
    private TextView soundOff;
    private ToneGenerator toneGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        setContentView(buildScreen());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) {
            try { toneGenerator.release(); } catch (Exception ignored) {}
            toneGenerator = null;
        }
    }

    private View buildScreen() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(1, 2, 4));

        CircuitBackground bg = new CircuitBackground(this);
        root.addView(bg, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        root.addView(scroll, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(8), dp(14), dp(28));
        scroll.addView(body, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        FrameLayout header = new FrameLayout(this);
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(122));
        headerLp.bottomMargin = dp(12);
        body.addView(header, headerLp);

        HeaderDecor headerDecor = new HeaderDecor(this);
        header.addView(headerDecor, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        ImageView skullLeft = new ImageView(this);
        Bitmap sl = loadAsset("skull_left.png");
        if (sl != null) skullLeft.setImageBitmap(sl);
        skullLeft.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        FrameLayout.LayoutParams slp = new FrameLayout.LayoutParams(dp(92), dp(108));
        slp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        slp.leftMargin = dp(1);
        header.addView(skullLeft, slp);

        ImageView skullRight = new ImageView(this);
        Bitmap sr = loadAsset("skull_right.png");
        if (sr != null) skullRight.setImageBitmap(sr);
        skullRight.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        FrameLayout.LayoutParams srp = new FrameLayout.LayoutParams(dp(92), dp(108));
        srp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        srp.rightMargin = dp(1);
        header.addView(skullRight, srp);

        LinearLayout headText = new LinearLayout(this);
        headText.setOrientation(LinearLayout.VERTICAL);
        headText.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams htlp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        htlp.leftMargin = dp(72);
        htlp.rightMargin = dp(72);
        header.addView(headText, htlp);

        TextView brand = text("METTMAXXX", 26, WHITE, true);
        brand.setGravity(Gravity.CENTER);
        brand.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        brand.setLetterSpacing(.06f);
        brand.setShadowLayer(dpF(12), 0, 0, Color.argb(210, 255, 5, 34));
        headText.addView(brand, matchWrap());

        TextView title = text("SMS ASSISTENT V3.1.1", 13, RED_HOT, true);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        title.setLetterSpacing(.11f);
        LinearLayout.LayoutParams titleLp = matchWrap();
        titleLp.topMargin = dp(1);
        headText.addView(title, titleLp);

        TextView channel = text("// RED SYSTEM ONLINE //", 8, Color.rgb(144, 46, 57), true);
        channel.setGravity(Gravity.CENTER);
        channel.setTypeface(Typeface.MONOSPACE);
        LinearLayout.LayoutParams chLp = matchWrap();
        chLp.topMargin = dp(5);
        headText.addView(channel, chLp);

        body.addView(sectionLabel("EMPFÄNGER"));

        FrameLayout phoneFrame = cyberPanel(62, 13);
        body.addView(phoneFrame, matchHeight(dp(62)));

        LinearLayout phoneRow = new LinearLayout(this);
        phoneRow.setOrientation(LinearLayout.HORIZONTAL);
        phoneRow.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams prlp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        prlp.setMargins(dp(2), dp(2), dp(2), dp(2));
        phoneFrame.addView(phoneRow, prlp);

        FrameLayout phoneSlot = new FrameLayout(this);
        phoneRow.addView(phoneSlot, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        phone = new EditText(this);
        phone.setTextColor(WHITE);
        phone.setHintTextColor(Color.rgb(92, 96, 103));
        phone.setHint("Telefonnummer eingeben...");
        phone.setSingleLine(true);
        phone.setTextSize(18);
        phone.setTypeface(Typeface.MONOSPACE);
        phone.setPadding(dp(14), 0, dp(8), 0);
        phone.setBackgroundColor(Color.TRANSPARENT);
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneSlot.addView(phone, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        maskedPhone = text("", 18, WHITE, false);
        maskedPhone.setTypeface(Typeface.MONOSPACE);
        maskedPhone.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        maskedPhone.setPadding(dp(14), 0, dp(8), 0);
        maskedPhone.setVisibility(View.GONE);
        phoneSlot.addView(maskedPhone, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        ImageButton eye = new ImageButton(this);
        eye.setBackgroundColor(Color.TRANSPARENT);
        eye.setPadding(dp(13), dp(13), dp(13), dp(13));
        Bitmap eyeBitmap = loadAsset("eye.png");
        if (eyeBitmap != null) eye.setImageBitmap(eyeBitmap);
        eye.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        eye.setContentDescription("Telefonnummer anzeigen oder verbergen");
        eye.setOnClickListener(v -> togglePhoneVisibility());
        phoneRow.addView(eye, new LinearLayout.LayoutParams(dp(58), dp(58)));

        LinearLayout.LayoutParams msgLabelLp = matchWrap();
        msgLabelLp.topMargin = dp(20);
        body.addView(sectionLabel("NACHRICHT"), msgLabelLp);

        FrameLayout msgFrame = cyberPanel(132, 13);
        body.addView(msgFrame, matchHeight(dp(132)));
        message = new EditText(this);
        message.setTextColor(WHITE);
        message.setHintTextColor(Color.rgb(92, 96, 103));
        message.setHint("Nachricht eingeben...");
        message.setTextSize(17);
        message.setGravity(Gravity.TOP | Gravity.START);
        message.setPadding(dp(14), dp(12), dp(14), dp(12));
        message.setMinLines(4);
        message.setMaxLines(5);
        message.setBackgroundColor(Color.TRANSPARENT);
        message.setFilters(new InputFilter[]{new InputFilter.LengthFilter(160)});
        FrameLayout.LayoutParams mlp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mlp.setMargins(dp(2), dp(2), dp(2), dp(2));
        msgFrame.addView(message, mlp);

        charCount = text("000 / 160", 11, MUTED, true);
        charCount.setTypeface(Typeface.MONOSPACE);
        charCount.setGravity(Gravity.END);
        LinearLayout.LayoutParams ccLp = matchWrap();
        ccLp.topMargin = dp(6);
        body.addView(charCount, ccLp);
        message.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCount.setText(String.format(Locale.US, "%03d / 160", s.length()));
                charCount.setTextColor(s.length() >= 150 ? RED_HOT : MUTED);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        LinearLayout.LayoutParams soundLabelLp = matchWrap();
        soundLabelLp.topMargin = dp(16);
        body.addView(sectionLabel("TON BEIM SENDEN"), soundLabelLp);

        FrameLayout soundFrame = cyberPanel(56, 13);
        body.addView(soundFrame, matchHeight(dp(56)));
        LinearLayout soundRow = new LinearLayout(this);
        soundRow.setOrientation(LinearLayout.HORIZONTAL);
        soundRow.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams srlp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        srlp.setMargins(dp(8), dp(6), dp(8), dp(6));
        soundFrame.addView(soundRow, srlp);

        TextView soundLabel = text("BEEP", 12, WHITE, true);
        soundLabel.setTypeface(Typeface.MONOSPACE);
        soundLabel.setLetterSpacing(.11f);
        soundRow.addView(soundLabel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        soundOn = soundPill("AN", true);
        soundOff = soundPill("AUS", false);
        soundRow.addView(soundOn, new LinearLayout.LayoutParams(dp(74), dp(32)));
        LinearLayout.LayoutParams offLp = new LinearLayout.LayoutParams(dp(74), dp(32));
        offLp.leftMargin = dp(8);
        soundRow.addView(soundOff, offLp);
        updateSoundButtons();

        FrameLayout counterBox = new FrameLayout(this);
        LinearLayout.LayoutParams cbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(96));
        cbLp.topMargin = dp(18);
        cbLp.leftMargin = dp(24);
        cbLp.rightMargin = dp(24);
        body.addView(counterBox, cbLp);

        counterFrame = new CounterFrame(this);
        counterBox.addView(counterFrame, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout counterText = new LinearLayout(this);
        counterText.setOrientation(LinearLayout.VERTICAL);
        counterText.setGravity(Gravity.CENTER);
        counterBox.addView(counterText, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        TextView sentLabel = text("GESENDETE SMS", 11, Color.rgb(205, 208, 214), true);
        sentLabel.setTypeface(Typeface.MONOSPACE);
        sentLabel.setLetterSpacing(.14f);
        sentLabel.setGravity(Gravity.CENTER);
        counterText.addView(sentLabel, matchWrap());

        sentCountView = text("000", 34, RED, true);
        sentCountView.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        sentCountView.setGravity(Gravity.CENTER);
        sentCountView.setLetterSpacing(.12f);
        sentCountView.setShadowLayer(dpF(9), 0, 0, Color.argb(190, 255, 0, 32));
        counterText.addView(sentCountView, matchWrap());

        FrameLayout sendArea = new FrameLayout(this);
        LinearLayout.LayoutParams sendAreaLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(196));
        sendAreaLp.topMargin = dp(4);
        body.addView(sendArea, sendAreaLp);

        sendGlow = new SendGlow(this);
        sendArea.addView(sendGlow, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        arrowDecor = new ArrowDecor(this);
        sendArea.addView(arrowDecor, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        sendButton = new ImageButton(this);
        sendButton.setBackgroundColor(Color.TRANSPARENT);
        sendButton.setPadding(dp(6), dp(6), dp(6), dp(6));
        Bitmap logo = loadAsset("logo_button.png");
        if (logo != null) sendButton.setImageBitmap(logo);
        sendButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        sendButton.setContentDescription("SMS senden");
        sendButton.setSoundEffectsEnabled(false);
        sendButton.setOnClickListener(v -> {
            pulseVisuals();
            playToneIfEnabled();
            sendOneSms();
        });
        FrameLayout.LayoutParams sendLp = new FrameLayout.LayoutParams(dp(166), dp(166));
        sendLp.gravity = Gravity.CENTER;
        sendArea.addView(sendButton, sendLp);

        TextView hint = text("▶  AUF DAS LOGO DRÜCKEN  //  GENAU 1 SMS SENDEN  ◀", 10, RED_HOT, true);
        hint.setTypeface(Typeface.MONOSPACE);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintLp = matchWrap();
        hintLp.topMargin = dp(1);
        hintLp.bottomMargin = dp(6);
        body.addView(hint, hintLp);

        return root;
    }

    private TextView soundPill(String label, boolean onValue) {
        TextView t = text(label, 12, WHITE, true);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        t.setOnClickListener(v -> {
            soundEnabled = onValue;
            updateSoundButtons();
        });
        return t;
    }

    private void updateSoundButtons() {
        soundOn.setBackground(pillBackground(soundEnabled));
        soundOff.setBackground(pillBackground(!soundEnabled));
        soundOn.setTextColor(soundEnabled ? WHITE : Color.rgb(175, 179, 185));
        soundOff.setTextColor(!soundEnabled ? WHITE : Color.rgb(175, 179, 185));
        soundOn.setShadowLayer(soundEnabled ? dpF(8) : 0f, 0, 0, Color.argb(180, 255, 0, 32));
        soundOff.setShadowLayer(!soundEnabled ? dpF(8) : 0f, 0, 0, Color.argb(180, 255, 0, 32));
    }

    private GradientDrawable pillBackground(boolean active) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(dp(9));
        gd.setColor(active ? Color.rgb(62, 5, 15) : Color.rgb(16, 18, 22));
        gd.setStroke(dp(1), active ? RED : Color.rgb(74, 42, 48));
        return gd;
    }

    private void playToneIfEnabled() {
        if (!soundEnabled) return;
        try {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90);
            }
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 120);
        } catch (Throwable ignored) {
        }
    }

    private void pulseVisuals() {
        arrowDecor.setActive(true);
        sendGlow.setActive(true);
        counterFrame.setActive(true);
        sentCountView.setTextColor(RED_HOT);
        sendButton.animate().scaleX(.94f).scaleY(.94f).setDuration(70).withEndAction(() ->
                sendButton.animate().scaleX(1f).scaleY(1f).setDuration(110).start()).start();
        sendButton.postDelayed(() -> {
            arrowDecor.setActive(false);
            sendGlow.setActive(false);
            counterFrame.setActive(false);
            sentCountView.setTextColor(RED);
        }, 190);
    }

    private FrameLayout cyberPanel(int height, int radius) {
        FrameLayout f = new FrameLayout(this);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.rgb(5, 6, 8));
        gd.setCornerRadius(dp(4));
        f.setBackground(gd);
        f.setElevation(dpF(2));
        PanelDecor d = new PanelDecor(this);
        f.addView(d, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        return f;
    }

    private void togglePhoneVisibility() {
        if (!phoneHidden) {
            phoneCache = phone.getText().toString();
            if (phoneCache.length() == 0) {
                toast("Keine Nummer eingegeben");
                return;
            }
            StringBuilder stars = new StringBuilder(phoneCache.length());
            for (int i = 0; i < phoneCache.length(); i++) stars.append('*');

            // Robuste Maskierung: Das echte Eingabefeld wird vollständig ausgeblendet.
            // Ein separates Textfeld zeigt ausschließlich Sterne an.
            maskedPhone.setText(stars.toString());
            phone.setVisibility(View.INVISIBLE);
            maskedPhone.setVisibility(View.VISIBLE);
            phoneHidden = true;
            toast("Nummer ausgeblendet");
        } else {
            maskedPhone.setVisibility(View.GONE);
            phone.setVisibility(View.VISIBLE);
            phone.setText(phoneCache);
            phone.setSelection(phone.length());
            phoneHidden = false;
            toast("Nummer sichtbar");
        }
    }

    private void sendOneSms() {
        String number = (phoneHidden ? phoneCache : phone.getText().toString()).trim();
        String text = message.getText().toString().trim();
        if (number.length() == 0 || text.length() == 0) {
            toast("Bitte Nummer und Text eingeben");
            return;
        }
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, REQ_SMS);
            return;
        }
        try {
            SmsManager.getDefault().sendTextMessage(number, null, text, null, null);
            if (sentCount < 999) sentCount++;
            sentCountView.setText(String.format(Locale.US, "%03d", sentCount));
            counterFrame.flashSuccess();
            toast("SMS gesendet");
        } catch (Exception e) {
            toast("SMS konnte nicht gesendet werden");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendOneSms();
            } else {
                toast("Bitte SMS-Berechtigung zulassen");
            }
        }
    }

    private TextView sectionLabel(String s) {
        TextView t = text("// " + s, 11, Color.rgb(194, 198, 205), true);
        t.setTypeface(Typeface.MONOSPACE);
        t.setLetterSpacing(.11f);
        t.setPadding(dp(4), 0, 0, dp(7));
        return t;
    }

    private TextView text(String s, float sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchHeight(int h) {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, h);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private float dpF(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private Bitmap loadAsset(String name) {
        try {
            InputStream in = getAssets().open(name);
            Bitmap b = BitmapFactory.decodeStream(in);
            in.close();
            return b;
        } catch (Exception e) {
            return null;
        }
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private static class HeaderDecor extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        HeaderDecor(Activity a) { super(a); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w=getWidth(), h=getHeight();
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.4f);
            p.setColor(Color.rgb(105, 5, 20));
            p.setShadowLayer(7f,0,0,Color.rgb(170,0,25));
            Path top=new Path();
            top.moveTo(5,h*.18f); top.lineTo(w*.22f,h*.18f); top.lineTo(w*.27f,h*.08f);
            top.lineTo(w*.73f,h*.08f); top.lineTo(w*.78f,h*.18f); top.lineTo(w-5,h*.18f);
            c.drawPath(top,p);
            Path bot=new Path();
            bot.moveTo(5,h*.86f); bot.lineTo(w*.25f,h*.86f); bot.lineTo(w*.30f,h*.94f);
            bot.lineTo(w*.70f,h*.94f); bot.lineTo(w*.75f,h*.86f); bot.lineTo(w-5,h*.86f);
            c.drawPath(bot,p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(RED);
            p.setShadowLayer(12f,0,0,RED);
            c.drawCircle(w/2f,h*.10f,2.6f,p);
            c.drawCircle(w*.08f,h*.52f,1.8f,p);
            c.drawCircle(w*.92f,h*.52f,1.8f,p);
        }
    }

    private static class PanelDecor extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        PanelDecor(Activity a) { super(a); setLayerType(View.LAYER_TYPE_SOFTWARE,null); }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w=getWidth(), h=getHeight(), cut=12f;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.3f);
            p.setColor(Color.rgb(115,115,120));
            Path outer=new Path();
            outer.moveTo(cut,1); outer.lineTo(w-cut,1); outer.lineTo(w-1,cut);
            outer.lineTo(w-1,h-cut); outer.lineTo(w-cut,h-1); outer.lineTo(cut,h-1);
            outer.lineTo(1,h-cut); outer.lineTo(1,cut); outer.close();
            c.drawPath(outer,p);
            p.setStrokeWidth(1.2f);
            p.setColor(Color.rgb(120,0,19));
            p.setShadowLayer(7f,0,0,Color.rgb(190,0,28));
            Path inner=new Path();
            float m=5, ci=9;
            inner.moveTo(m+ci,m); inner.lineTo(w-m-ci,m); inner.lineTo(w-m,m+ci);
            inner.lineTo(w-m,h-m-ci); inner.lineTo(w-m-ci,h-m); inner.lineTo(m+ci,h-m);
            inner.lineTo(m,h-m-ci); inner.lineTo(m,m+ci); inner.close();
            c.drawPath(inner,p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(RED);
            c.drawCircle(12,12,1.5f,p); c.drawCircle(w-12,12,1.5f,p);
            c.drawCircle(12,h-12,1.5f,p); c.drawCircle(w-12,h-12,1.5f,p);
        }
    }

    private static class CircuitBackground extends View {
        private final Paint circuit = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);
        CircuitBackground(Activity a) {
            super(a);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            circuit.setColor(Color.rgb(66, 3, 16));
            circuit.setStrokeWidth(1.3f);
            circuit.setStyle(Paint.Style.STROKE);
            grid.setColor(Color.rgb(18, 20, 25));
            grid.setStrokeWidth(1f);
            grid.setStyle(Paint.Style.STROKE);
        }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            int w = getWidth(), h = getHeight();
            grid.setAlpha(58);
            for (int x = 18; x < w; x += 32) c.drawLine(x, 0, x, h, grid);
            for (int y = 18; y < h; y += 32) c.drawLine(0, y, w, y, grid);
            circuit.setAlpha(150);
            circuit.setShadowLayer(7f, 0, 0, Color.argb(125, 255, 0, 30));
            for (int y = 46; y < h; y += 92) {
                Path a = new Path();
                a.moveTo(0, y);
                a.lineTo(w * .12f, y);
                a.lineTo(w * .18f, y + 16);
                a.lineTo(w * .30f, y + 16);
                a.lineTo(w * .34f, y + 28);
                c.drawPath(a, circuit);
                c.drawCircle(w * .34f, y + 28, 2.2f, circuit);

                Path b = new Path();
                b.moveTo(w, y + 44);
                b.lineTo(w * .87f, y + 44);
                b.lineTo(w * .82f, y + 28);
                b.lineTo(w * .71f, y + 28);
                b.lineTo(w * .67f, y + 15);
                c.drawPath(b, circuit);
                c.drawCircle(w * .67f, y + 15, 2.2f, circuit);
            }
        }
    }

    private static class CounterFrame extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean active = false;
        CounterFrame(Activity a) {
            super(a);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            p.setStyle(Paint.Style.STROKE);
        }
        void setActive(boolean on) { active = on; invalidate(); }
        void flashSuccess() {
            setActive(true);
            postDelayed(() -> setActive(false), 250);
        }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w = getWidth(), h = getHeight();
            float pad = 4f;
            RectF r = new RectF(pad, pad, w-pad, h-pad);
            p.setStrokeWidth(active ? 3.2f : 1.8f);
            p.setColor(active ? RED_HOT : Color.rgb(145, 5, 27));
            p.setShadowLayer(active ? 15f : 7f, 0, 0, active ? RED_HOT : Color.rgb(130, 0, 22));
            c.drawRoundRect(r, 15f, 15f, p);
            p.setStrokeWidth(active ? 4f : 2.5f);
            p.setColor(active ? Color.WHITE : RED);
            float len = 28f;
            c.drawLine(12, 4, 12 + len, 4, p);
            c.drawLine(w - 12 - len, 4, w - 12, 4, p);
            c.drawLine(12, h - 4, 12 + len, h - 4, p);
            c.drawLine(w - 12 - len, h - 4, w - 12, h - 4, p);
            p.setStyle(Paint.Style.FILL);
            p.setShadowLayer(active ? 10f : 4f, 0, 0, RED);
            p.setColor(active ? RED_HOT : RED);
            c.drawCircle(18, h / 2f, active ? 4.0f : 2.7f, p);
            c.drawCircle(w - 18, h / 2f, active ? 4.0f : 2.7f, p);
            for (int i=0;i<4;i++) {
                float yy = h/2f - 18f + i*12f;
                c.drawRoundRect(new RectF(30, yy, 35, yy+6), 2, 2, p);
                c.drawRoundRect(new RectF(w-35, yy, w-30, yy+6), 2, 2, p);
            }
            p.setStyle(Paint.Style.STROKE);
        }
    }

    private static class SendGlow extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean active = false;
        SendGlow(Activity a) {
            super(a);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            p.setStyle(Paint.Style.STROKE);
        }
        void setActive(boolean on) { active = on; invalidate(); }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float cx = getWidth()/2f, cy = getHeight()/2f;
            float r = Math.min(getWidth(), getHeight()) * .39f;
            p.setColor(active ? RED_HOT : Color.rgb(72, 0, 13));
            p.setStrokeWidth(active ? 4.5f : 1.4f);
            p.setAlpha(active ? 235 : 105);
            p.setShadowLayer(active ? 24f : 8f, 0, 0, RED);
            c.drawCircle(cx, cy, r, p);
            p.setStrokeWidth(active ? 2.2f : 1.1f);
            p.setAlpha(active ? 190 : 62);
            c.drawCircle(cx, cy, r + 10f, p);
            p.setAlpha(active ? 125 : 35);
            c.drawCircle(cx, cy, r + 19f, p);
            p.setAlpha(active ? 80 : 24);
            c.drawCircle(cx, cy, r + 27f, p);
        }
    }

    private static class ArrowDecor extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean active = false;
        ArrowDecor(Activity a) {
            super(a);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
        }
        void setActive(boolean on) { active = on; invalidate(); }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float cy = getHeight() / 2f;
            float center = getWidth() / 2f;
            drawSide(c, center - 93, cy, -1);
            drawSide(c, center + 93, cy, 1);
        }
        private void drawSide(Canvas c, float anchor, float cy, int dir) {
            for (int i = 0; i < 6; i++) {
                float gap = 15 + i * 17;
                float half = 23 - i * 2.25f;
                float x = anchor + dir * gap;
                p.setStrokeWidth((active ? 4.7f : 3.0f) - i * .38f);
                p.setColor(active ? RED_HOT : Color.rgb(255, 18 + i*4, 38 + i*4));
                p.setAlpha(active ? 255 - i*12 : 225 - i*28);
                p.setShadowLayer(active ? 18f : 7f, 0, 0, RED);

                Path q = new Path();
                float back = x - dir * (half * 1.15f);
                q.moveTo(back, cy - half);
                q.quadTo(x - dir * half * .36f, cy - half * .35f, x, cy);
                q.quadTo(x - dir * half * .36f, cy + half * .35f, back, cy + half);
                c.drawPath(q, p);
                float tail = back - dir * (10 + i*2);
                c.drawLine(back, cy, tail, cy, p);
                p.setStyle(Paint.Style.FILL);
                c.drawCircle(tail - dir*4, cy, active ? 2.8f : 1.8f, p);
                p.setStyle(Paint.Style.STROKE);
            }
        }
    }
}
