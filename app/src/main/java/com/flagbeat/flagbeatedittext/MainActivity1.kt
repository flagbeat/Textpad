package com.flagbeat.flagbeatedittext

import CalloutLink
import Hashtag
import android.app.Activity
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView

import java.util.*
import java.util.regex.Pattern


class MainActivity1 : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) { // TODO Auto-generated method stub
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val str =
            "@People , You've gotta #dance like there's nobody watching,#Love like you'll never be #hurt,#Sing like there's @nobody listening,And live like it's #heaven on #earth."
        val txt = findViewById<View>(R.id.display) as TextView
        val hashtagSpans1 = getSpans(str, '#')
        val calloutSpans1 = getSpans(str, '@')
        val commentsContent1 = SpannableString(str)
        setSpanComment(commentsContent1, hashtagSpans1)
        setSpanUname(commentsContent1, calloutSpans1)
        txt.movementMethod = LinkMovementMethod.getInstance()

        
        txt.text = commentsContent1


    }

    fun getSpans(body: String?, prefix: Char): ArrayList<IntArray> {
        val spans = ArrayList<IntArray>()
        val pattern =
            Pattern.compile("$prefix\\w+")
        val matcher = pattern.matcher(body)
        // Check all occurrences
        while (matcher.find()) {
            val currentSpan = IntArray(2)
            currentSpan[0] = matcher.start()
            currentSpan[1] = matcher.end()
            spans.add(currentSpan)
        }
        return spans
    }

    private fun setSpanComment(
        commentsContent: SpannableString,
        hashtagSpans: ArrayList<IntArray>
    ) {
        for (i in hashtagSpans.indices) {
            val span = hashtagSpans[i]
            val hashTagStart = span[0]
            val hashTagEnd = span[1]
            commentsContent.setSpan(
                Hashtag(this),
                hashTagStart,
                hashTagEnd, 0
            )
        }
    }

    private fun setSpanUname(
        commentsUname: SpannableString,
        calloutSpans: ArrayList<IntArray>
    ) {
        for (i in calloutSpans.indices) {
            val span = calloutSpans[i]
            val calloutStart = span[0]
            val calloutEnd = span[1]
            commentsUname.setSpan(
                CalloutLink(this),
                calloutStart,
                calloutEnd, 0
            )
        }
    }
}
