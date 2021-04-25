package com.flagbeat.flagbeatedittext

import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flagbeat.textpad.*
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.text.StringEscapeUtils


class MainActivity : AppCompatActivity(), OnInitTagListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        edit_text.setOnInitTagListener(this)

        done.setOnClickListener{
            var text = "#ram #rahim"
            if (!TextUtils.isEmpty(edit_text.getActualContentText())) {
                text = edit_text.getTemplateContentText()
            }
            edit_text.setContent(text, edit_text.getPeopleTagsInContent(), edit_text.getHashTagsInContent())

            display.text =
                TextPad.getDecoratedContentText(
                    edit_text.getTemplateContentText(),
                    "#662C4F",
                    edit_text.getPeopleTagsInContent(),
                    edit_text.getHashTagsInContent(),
                    object : OnClickTagListener {
                        override fun onClickTag(tag: Tag) {
                            Toast.makeText(applicationContext, tag.label, Toast.LENGTH_SHORT).show()
                        }
                    })

            display.highlightColor = resources.getColor(android.R.color.transparent)
            display.movementMethod = LinkMovementMethod.getInstance()


        }
        edit_text.setHashTagsSuggestion(getHashTags())


        val contentThemes: ArrayList<ContentTheme> = ArrayList()
        contentThemes.add(
            ContentTheme(
                "theme_1",
                "https://flagbeat.s3.ap-south-1.amazonaws.com/themes/theme_1.jpeg",
                "#FFFFFF",
                "#FFFF00"
        ))
        contentThemes.add(
            ContentTheme(
                "theme_2",
                "https://flagbeat.s3.ap-south-1.amazonaws.com/themes/theme_2.jpeg",
                "#e6e600",
                "#ffffcc"
            ))
        contentThemes.add(
            ContentTheme(
                "theme_3",
                "https://flagbeat.s3.ap-south-1.amazonaws.com/themes/theme_3.jpeg",
                "#ff1a1a",
                "#ffcccc"
            ))
        contentThemes.add(
            ContentTheme(
                "theme_4",
                "https://flagbeat.s3.ap-south-1.amazonaws.com/themes/theme_4.jpeg",
                "#0099ff",
                "#ccebff"
            ))
        edit_text.setContentThemes(contentThemes)

        val polylineStr = "ixzmA{lwxMwEgJm@oERGiB{MnQvAVmI|BFFKDm@v@D}"
        val escaped = StringEscapeUtils.unescapeJava(polylineStr);
        Log.e("sumit escaped", escaped)
    }

    override fun onTypeTag(searchText: String, tagType: TagType, tagSuggestionResult: HashTagSuggestionResult) {
        Log.e("server call", "sent--------------------->>>>>>>>>>")
        if (tagType == TagType.HASH) {
            tagSuggestionResult.onReady(searchText, tagType, getHashTags())
        }
        else if (tagType == TagType.PEOPLE){
            tagSuggestionResult.onReady(searchText, tagType, getPeopleTags())
        }
    }

    fun getHashTags(): ArrayList<Tag> {
        val tags: ArrayList<Tag> = ArrayList()
        val tag: Tag = HashTag("cricket", "cricket", "", TagType.HASH, false)
        tags += HashTag("match", "match", "", TagType.HASH, false)
        tags += HashTag("ind-vs-aus", "ind-vs-aus", "", TagType.HASH, false)
        tags += HashTag("masti", "masti", "", TagType.HASH, false)
        tags += HashTag("flagbeat", "flagbeat", "", TagType.HASH, false)
        tags += HashTag("fun-with-friends", "fun-with-friends", "", TagType.HASH, false)
        tags += HashTag("weekend", "weekend", "", TagType.HASH, false)
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        return tags
    }

    fun getPeopleTags(): ArrayList<Tag> {
        val tags: ArrayList<Tag> = ArrayList()
        val tag: Tag =
            PeopleTag("13237682623", "Sumit Saurabh",
                "", TagType.PEOPLE, false, "sumitsaurabh" )

        tags += PeopleTag("23237682623", "Nitesh Garg",
            "", TagType.PEOPLE, false, "nitesh321" )
        tags += PeopleTag("33237682623", "Amit Kumar",
            "", TagType.PEOPLE, false, "amit_kumar" )
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        tags += tag
        return tags
    }
}
