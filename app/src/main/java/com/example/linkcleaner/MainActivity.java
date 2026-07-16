package com.example.linkcleaner;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String sharedText = null;
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction())) {
            sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        if (sharedText == null || sharedText.trim().isEmpty()) {
            Toast.makeText(this, "متنی برای پردازش پیدا نشد", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String cleanedText = cleanLinks(sharedText);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("cleaned_link", cleanedText);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "لینک تمیز شد و کپی شد ✅", Toast.LENGTH_SHORT).show();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, cleanedText);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent chooser = Intent.createChooser(shareIntent, "ارسال لینک تمیزشده");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(chooser);

        finish();
    }

    private String cleanLinks(String input) {
        Matcher matcher = URL_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String url = matcher.group();
            String cleanedUrl = stripQueryAndTrailingPunctuation(url);
            matcher.appendReplacement(result, Matcher.quoteReplacement(cleanedUrl));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String stripQueryAndTrailingPunctuation(String url) {
        int queryIndex = url.indexOf('?');
        if (queryIndex != -1) {
            url = url.substring(0, queryIndex);
        }
        while (url.length() > 0 && ".,!?؟،".indexOf(url.charAt(url.length() - 1)) != -1) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
