package com.flagbeat.flagbeatedittext;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity2 extends Activity {
    private BackgroundColorSpan color = null;
    private ArrayList<Boolean> list = new ArrayList<Boolean>();
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == LinkMovementMethodExt.LinkMovementMethod_Down) {
                MessageSpan ms = (MessageSpan) msg.obj;
                Object[] spans = (Object[]) ms.getObj();
                TextView view = ms.getView();

                for (Object span : spans) {
                    if (span instanceof URLSpan) {
                        int start = Selection.getSelectionStart(view.getText());
                        int end = Selection.getSelectionEnd(view.getText());
                        System.out.println(((URLSpan) span).getURL());
                        Toast.makeText(MainActivity2.this, ((URLSpan) span).getURL(), Toast.LENGTH_SHORT).show();
                        Spannable _span = (Spannable) view.getText();
                        color = new BackgroundColorSpan(view.getLinkTextColors().getDefaultColor());
                        _span.setSpan(color, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        view.setText(_span);
                    }
                }
            } else if (what == LinkMovementMethodExt.LinkMovementMethod_Up) {
                MessageSpan ms = (MessageSpan) msg.obj;
                TextView view = ms.getView();
                Spannable _span = (Spannable) view.getText();
                _span.removeSpan(color);
                view.setText(_span);
            }
        }

        ;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String a = "<a href='@4234234234234'>Sumit Saurabh</a>123456   \n <a href='#hashtag'>hashtag<a/>";
        View span = findViewById(R.id.display_content);
        Spanned text = Html.fromHtml(a);
        ((TextView) span).setText(text);
        ((TextView) span).setMovementMethod(LinkMovementMethodExt.getInstance(handler, URLSpan.class));
    }
}
