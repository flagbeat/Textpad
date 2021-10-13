package com.flagbeat.textpad

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.SystemClock
import android.text.*
import android.text.style.*
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.chip.Chip
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_people_tag.view.*
import kotlinx.android.synthetic.main.text_pad.view.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class TextPad(
	context: Context,
	attrs: AttributeSet
): RelativeLayout(context, attrs) {
	private var onInitTagListener: OnInitTagListener? = null
	private var runnable: Runnable? = null
	private var attrs: AttributeSet
	private var textViewUndoRedo: TextViewUndoRedo
	private var defaultHashTags: List<Tag> = emptyList()
	private var defaultPeopleTags: List<Tag> = emptyList()
	private var contentThemes: List<ContentTheme> = emptyList()
	private var selectedThemeIndex: Int = -1
	private var inflater: LayoutInflater
	private var handlr: Handler = Handler()
	private var textChangeInProgress: Boolean = false
	private var selectedHashTags: List<HashTag>
	private var selectedPeopleTags: List<PeopleTag>
	private var hashRegex: String = "(#[a-zA-Z0-9_-]+)"
	private var mentionRegex: String = "(@[a-zA-Z0-9_]+)"
	private val hashPattern: Pattern = Pattern.compile(hashRegex)
	private val mentionPattern: Pattern = Pattern.compile(mentionRegex)
	private val defaultTagColor: String

	private val textColors = arrayOf(
		R.color.inc_red,
		R.color.inc_blue,
		R.color.inc_green,
		R.color.inc_violet,
		R.color.primary_text,
		R.color.secondary_text,
		R.color.tertiary_text,
		R.color.fb_yellow,
		R.color.fb_violet,
		R.color.dark_yellow,
		R.color.dark_red,
		R.color.primary_dark
	)

	private var textWatcher: TextWatcher = object : TextWatcher {
		override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

		}

		override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//			Log.e("onTextChanged", "--------------------------")
//			Log.e("onTextChanged", "actual = " + Html.toHtml(content.text))
//			Log.e("onTextChanged", "CharSequence = " + s.toString())
//			Log.e("onTextChanged", "start = " + start.toString())
//			Log.e("onTextChanged", "before = " + before.toString())
//			Log.e("onTextChanged", "count = " + count.toString())
			if (!textChangeInProgress) {
				showTaggingView(start)
			}
		}

		override fun afterTextChanged(et: Editable) {

		}
	}

	private var textColorIndex: Int = -1
	get() {
		field++
		if (field >= textColors.size) {
			field = 0
		}
		return field
	}

	fun setOnInitTagListener(tagSuggestionListener: OnInitTagListener) {
		this.onInitTagListener = tagSuggestionListener
	}

    init {
        inflate(context, R.layout.text_pad, this)
	    this.attrs = attrs
		selectedHashTags = emptyList()
		selectedPeopleTags = emptyList()
	    initAttributes()
		textViewUndoRedo = TextViewUndoRedo(content)
		inflater = context.getSystemService( Context.LAYOUT_INFLATER_SERVICE ) as LayoutInflater
		defaultTagColor = "#" + Integer.toHexString(context.resources.getColor(R.color.hash_tag_color)).substring(2).toUpperCase()
		renderView()
    }

	private fun initAttributes() {
		val attributes = context.obtainStyledAttributes(attrs, R.styleable.TextPadView)
		content.hint = attributes.getString(R.styleable.TextPadView_hintText)
		val padHeight: Int = attributes.getDimensionPixelSize(R.styleable.TextPadView_padHeight, -1)
		val padExpandedHeight: Int = attributes.getDimensionPixelSize(R.styleable.TextPadView_padExpandedHeight, -1)
		content.layoutParams.height = padHeight

		expand_collapse_view.setOnClickListener {
			if (content.layoutParams.height > padHeight) {
				content.layoutParams.height = padExpandedHeight
			}
			else {
				content.layoutParams.height = padHeight
			}
		}

		edit_option_wrapper.visibility =
			if (attributes.getBoolean(R.styleable.TextPadView_moreOptionEnabled, true)) View.VISIBLE else View.GONE
		attributes.recycle()
	}

	fun setContent(text: String, peopleTags: List<PeopleTag>?, hashTags: List<HashTag>?) {
		textChangeInProgress = true
		peopleTags?.let {
			this.selectedPeopleTags = it
		}
		hashTags?.let {
			this.selectedHashTags = it
		}
		val contentText = getDecoratedContentText(text, defaultTagColor, selectedPeopleTags, selectedHashTags, null)
		content.setText(contentText)

		content.setSelection(content.text.length)
		textChangeInProgress = false
	}

	fun setEditTextHint(hint: String) {
		content.hint = hint
	}

	fun setHashTagsSuggestion(defaultHashTags: List<Tag>) {
		this.defaultHashTags = defaultHashTags
		renderDefaultHashTags(defaultHashTags)
	}

	fun setPeopleTagsSuggestion(defaultPeopleTags: List<Tag>) {
		this.defaultPeopleTags = defaultPeopleTags
	}

	fun setContentThemes(contentThemes: List<ContentTheme>) {
		this.contentThemes = contentThemes
	}

	fun getActualContentText(): String {
		 return Html.toHtml(content.text)
	}

	private fun getUnspannedTagsContentText(content: String, tagColor: String): String {
		//Log.e("Input text ", content)
		var contentText = content
		if (!TextUtils.isEmpty(contentText)) {
			val regex = "((<span style=\"color:#$tagColor;\">)([#|@][a-zA-Z0-9_\\s-]+)(<\\/span>))"
			//Log.e("regex", regex)
			val regexPattern: Pattern = Pattern.compile(regex)

			val matcher: Matcher = regexPattern.matcher(contentText)
			while (matcher.find()) {
				contentText = contentText.replace((matcher.group(1)),  matcher.group(3))
			}
		}
		//Log.e("Unspanned text ", contentText)
		return contentText
	}



	fun getTemplateContentText(): String {
		var contentText = getUnspannedTagsContentText(Html.toHtml(content.text), defaultTagColor)
		if (!TextUtils.isEmpty(contentText)) {
			for (peopleTag in selectedPeopleTags) {
				contentText = contentText.replace(("@" + peopleTag.username), "@"+ peopleTag.id)
			}
		}

		//Log.e("template html -", contentText)
		return trimTrailingWhitespace(contentText)
	}

	/** Trims trailing whitespace. Removes any of these characters:
	 * 0009, HORIZONTAL TABULATION
	 * 000A, LINE FEED
	 * 000B, VERTICAL TABULATION
	 * 000C, FORM FEED
	 * 000D, CARRIAGE RETURN
	 * 001C, FILE SEPARATOR
	 * 001D, GROUP SEPARATOR
	 * 001E, RECORD SEPARATOR
	 * 001F, UNIT SEPARATOR
	 * @return "" if source is null, otherwise string with all trailing whitespace removed
	 */
	fun trimTrailingWhitespace(source: CharSequence?): String {

		if (source == null) return ""
		var i = source.length
		// loop back to the first non-whitespace character
		while (--i >= 0 && Character.isWhitespace(source[i])) {
		}
		return source.subSequence(0, i + 1).toString()
	}

	private fun getHashTagByLabel(id: String) : HashTag? {
		for (hashTag in selectedHashTags) {
			if (id == "#" + hashTag.label) {
				return hashTag
			}
		}
		return null
	}

	fun getHashTagsInContent() : List<HashTag> {
		val hashTagsStrList: List<String> = getTagsFromContent(hashPattern)
		var hashTags: List<HashTag> = emptyList()
		for (hashTagStr in hashTagsStrList) {
			var hashTag: HashTag? = getHashTagByLabel(hashTagStr)
			if (null == hashTag) {
				hashTag = HashTag(hashTagStr, hashTagStr, null, TagType.HASH, false)
			}
			hashTags = hashTags + hashTag
		}
		return hashTags
	}

	private fun getPeopleTagByUserName(username: String) : PeopleTag? {
		for (peopleTag in selectedPeopleTags) {
			if (username == "@" + peopleTag.username) {
				return peopleTag
			}
		}
		return null
	}

	fun getPeopleTagsInContent() : List<PeopleTag>{
		val peopleTagStrList: List<String> = getTagsFromContent(mentionPattern)
		var peopleTags: List<PeopleTag> = emptyList()
		for (peopleTagStr in peopleTagStrList) {
			val peopleTag = getPeopleTagByUserName(peopleTagStr)
			if (null != peopleTag) {
				peopleTags = peopleTags + peopleTag
			}
		}
		return peopleTags
	}

	private fun renderView()  {

		hash_tag_button.clickWithDebounce{
			content.text.insert(content.selectionStart, "#")
		}
		mention_button.clickWithDebounce{
			content.text.insert(content.selectionStart, "@")
		}
		clear_editor_button.setOnClickListener{
			content.text.clear()
		}
		undo_button.setOnClickListener{
			textViewUndoRedo.undo()
		}
		redo_button.setOnClickListener{
			textViewUndoRedo.redo()
		}
		remove_style_button.setOnClickListener{
			removeStyle()
		}
		bold_button.setOnClickListener {
			changeTextStyle(android.graphics.Typeface.BOLD)
		}
		italic_button.setOnClickListener {
			changeTextStyle(android.graphics.Typeface.ITALIC)
		}
		font_color_button.setOnClickListener{
			changeTextColor(textColors[textColorIndex])
		}
		font_size_button.setOnClickListener{
			changeTextSize()
		}
		strikethrough_button.setOnClickListener {
			strikethroughText()
		}
		underline_button.setOnClickListener {
			underlineText()
		}
		highlighted_button.setOnClickListener{
			highlightText()
		}
		option_button.clickWithDebounce{
			// background theme
			enableBackgroundTheme()
			renderOptionView(edit_option.visibility == View.VISIBLE)
		}

		val layoutManager = FlexboxLayoutManager(context)
		layoutManager.flexDirection = FlexDirection.ROW
		tagging_recycler_view.layoutManager = layoutManager
		tagging_recycler_view.adapter = TagAdapter(mutableListOf()) {
			insertTagInView(it)
			dropdown_view.visibility = View.GONE
		}

		content.addTextChangedListener(textWatcher)

		renderOptionView(true)
	}

 	private fun enableBackgroundTheme() {
		if (contentThemes.isEmpty()) {
			change_bg_button.visibility = View.GONE
		}
		else {
			change_bg_button.visibility = View.VISIBLE

			change_bg_button.setOnClickListener{
				selectedThemeIndex = (selectedThemeIndex + 1) % contentThemes.size

				Picasso.get()
					.load(contentThemes[selectedThemeIndex].imageUrl)
					.fit()
					.error(R.drawable.textpad_bg)
					.into(content_bg)

				content.setTextColor(Color.parseColor(contentThemes[selectedThemeIndex].textColor))
				content.setHintTextColor(Color.parseColor(contentThemes[selectedThemeIndex].hintColor))
			}
		}
 	}

	fun getBackgroundTheme(): ContentTheme? {
		return if (contentThemes.isEmpty() || selectedThemeIndex < 0) {
			null
		} else {
			contentThemes[selectedThemeIndex]
		}
	}

	private fun renderOptionView(showSuggestionView: Boolean) {
		if (showSuggestionView) {
			option_button_icon.setImageResource(R.drawable.ic_style_black_24dp)
			auto_tag_suggestion.visibility = View.VISIBLE
			edit_option.visibility = View.GONE
		}
		else {
			option_button_icon.setImageResource(R.drawable.ic_close_black_24dp)
			auto_tag_suggestion.visibility = View.GONE
			edit_option.visibility = View.VISIBLE
		}
	}

	private fun debounceTaggingView(text: String) {
		// Remove all previous callbacks.
		runnable?.let {
			handlr.removeCallbacks(runnable)
		}

		runnable = Runnable {
			fetchTags(text)
		}
		handlr.postDelayed(runnable, 400)
	}

	private fun isAllowedTagInitializer(text: String): Boolean {
		return !TextUtils.isEmpty(text) && (text.startsWith("#") || text.startsWith("@"))
	}

	private fun getTagType(startChar: Char): TagType {
		var tagType: TagType = TagType.NONE
		if (startChar == '#') {
			tagType = TagType.HASH
		}
		else if (startChar == '@') {
			tagType = TagType.PEOPLE
		}
		return tagType
	}

	private fun getDefaultTags(tagType: TagType): List<Tag> {
		return when(tagType) {
			TagType.HASH -> defaultHashTags
			else -> defaultPeopleTags
		}
	}

	private fun fetchTags(text: String) {
		val selectedText = getSelectedText(content.selectionStart, true, false)
		if (isAllowedHash(selectedText.text)) {
			onInitTagListener?.onTypeTag(
				selectedText.text,
				getTagType(selectedText.text[0]),
				object : HashTagSuggestionResult {
					override fun onReady(searchText: String, tagType: TagType, tags: List<Tag>) {
						(tagging_recycler_view.adapter as TagAdapter).refresh(if (tags.isEmpty()) getDefaultTags(tagType) else tags)
						dropdown_view.visibility = View.VISIBLE
					}
				})
		}
	}

	private fun showTaggingView(position: Int) {
		var selectedText = getSelectedText(position, true, false)
		//Log.e("onTextChanged", "text = " + selectedText.text)
		if (isAllowedTagInitializer(selectedText.text)){
			if (isAllowedHash(selectedText.text)) {
				changeTextColor(R.color.hash_tag_color)
				debounceTaggingView(selectedText.text)
			}
			else {
				removeColorSpan(selectedText)
			}
		}
		else
		{
			dropdown_view.visibility = View.GONE

//			if ("" == selectedText.text) {
//				if (position + 1 < content.text.length) {
//					selectedText = getSelectedText(position + 1, true, false)
//					if (isAllowedTagInitializer(selectedText.text) && isAllowedHash(selectedText.text)) {
//						changeTextColor(R.color.hash_tag_color, selectedText)
//					}
//					else if (selectedText.text.isNotEmpty()){
//						removeColorSpan(selectedText)
//					}
//				}
//
//				if (position - 2 > 0) {
//					selectedText = getSelectedText(position - 2, true, false)
//					if (isAllowedTagInitializer(selectedText.text) && isAllowedHash(selectedText.text)) {
//						changeTextColor(R.color.hash_tag_color, selectedText)
//					}
//				}
//			}
		}
	}

	private fun isAllowedHash(tag: String): Boolean {
		val isAllowed = isAllowedTagInitializer(tag)
//		if (isAllowed) {
//			isAllowed = mentionPattern.matcher(tag).matches() || hashPattern.matcher(tag).matches()
//		}
		//Log.e("isAllowed", isAllowed.toString())
		return isAllowed
	}

	private fun getTagIdentifierCharByType(tagType: TagType): String {
		var char = " "
		if (tagType == TagType.HASH) {
			char = "#"
		}
		else if (tagType == TagType.PEOPLE){
			char = "@"
		}
		return char
	}

	private fun insertTagInView(tag: Tag) {
		val selectedText: SelectedText? = getSelectedText()

		if (null != selectedText) {
			val spannable = SpannableStringBuilder(content.text)

			val tagLabel: String = getTagIdentifierCharByType(tag.tagType) + if (tag.tagType == TagType.PEOPLE) (tag as PeopleTag).username else tag.label

			content.text = spannable.replace(selectedText.selectionStart, selectedText.selectionEnd, "$tagLabel ")

			selectedText.text = tagLabel
			selectedText.selectionEnd = selectedText.selectionStart + tagLabel.length
			changeTextColor(R.color.hash_tag_color, selectedText)

			val cursorPosition: Int = selectedText.selectionEnd + 1
			content.setSelection(cursorPosition, cursorPosition)

			// add into selected tag buffer
			if (tag.tagType == TagType.PEOPLE) {
				selectedPeopleTags = selectedPeopleTags + tag as PeopleTag
			}
			else {
				selectedHashTags += tag as HashTag
			}
		}
	}

	private fun removeStyle() {
		val contentText = content.text.toString()
		content.setText(Html.fromHtml(contentText))
		content.setSelection(content.text.length, content.text.length)
	}

	private fun changeTextStyle(style: Int) {
		val selectedText: SelectedText? = getSelectedText(true, true)

		if (null != selectedText) {
			val spannable: Spannable = content.text

			var styleExists: Boolean = false

			val styleSpans =
				spannable.getSpans(
					selectedText.selectionStart,
					selectedText.selectionEnd,
					StyleSpan::class.java
				)

			// If the selected text-part already has BOLD style on it, then
			// we need to disable it
			for (i in styleSpans.indices) {
				if (styleSpans[i].style == style) {
					spannable.removeSpan(styleSpans[i])
					styleExists = true
				}
			}

			if (!styleExists) {
				spannable.setSpan(
					StyleSpan(style),
					selectedText.selectionStart,
					selectedText.selectionEnd,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
				)
			}
		}
	}

	private fun isDelimiter(char: Char): Boolean {
		return char == ' ' || char == '\n' || char == '\t' || char == '.' || char == ','
	}

	private fun getSelectedText(position: Int, preCursorSelection: Boolean = true, postCursorSelection: Boolean = false): SelectedText {
		var selectionStart = position
		var selectionEnd = position
		val text = content.text
		var selectedWord = ""
		if ((text.length > position /*&& !isDelimiter(text[position])*/) || text.length == position) {

			// finding start
			if (preCursorSelection) {
				for (i in position - 1 downTo 0 step 1) {
					if (!isDelimiter(text[i])) {
						selectionStart = i
						selectedWord = text[i] + selectedWord
					} else {
						break
					}
				}
			}

			// finding end
			if (postCursorSelection) {
				for (i in position until text.length) {
					if (!isDelimiter(text[i])) {
						selectedWord += text[i]
					} else {
						break
					}
					selectionEnd = i
				}
				if (selectionEnd != text.length) {
					selectionEnd++
				}
			}
		}
		return SelectedText(selectedWord, selectionStart, selectionEnd)
	}

	private fun getSelectedText(preCursorSelection: Boolean = true, postCursorSelection: Boolean = false): SelectedText? {
		var selectionStart = content.selectionStart
		var selectionEnd = content.selectionEnd
		var selectedText: SelectedText? = null
		if (selectionStart == selectionEnd) {
			selectedText = getSelectedText(selectionStart, preCursorSelection, postCursorSelection)
		} else {
			if (selectionStart > selectionEnd) {
				selectionEnd = content.selectionStart
				selectionStart = content.selectionEnd
			}
			selectedText = SelectedText(
				content.text.substring(selectionStart, selectionEnd),
				selectionStart,
				selectionEnd
			)
		}
		return selectedText
	}

	private fun underlineText() {
		val selectedText: SelectedText? = getSelectedText(true, true)

		if (!TextUtils.isEmpty(selectedText?.text)) {
			val spannable: Spannable = content.text
			var spansExists: Boolean = false
			val spans =
				spannable.getSpans(
					selectedText!!.selectionStart,
					selectedText.selectionEnd,
					UnderlineSpan::class.java
				)

			for (i in 0 until spans.size) {
				spannable.removeSpan(spans[i])
				spansExists = true

			}

			if (!spansExists) {
				// set the span
				spannable.setSpan(
					UnderlineSpan(),
					selectedText.selectionStart,
					selectedText.selectionEnd,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
				)
			}
		}
	}

	private fun strikethroughText() {
		val selectedText: SelectedText? = getSelectedText(true, true)

		if (!TextUtils.isEmpty(selectedText?.text)) {
			val spannable: Spannable = content.text

			var spansExists: Boolean = false
			val spans =
				spannable.getSpans(
					selectedText!!.selectionStart,
					selectedText.selectionEnd,
					StrikethroughSpan::class.java
				)

			for (element in spans) {
				spannable.removeSpan(element)
				spansExists = true

			}

			// set the span
			if (!spansExists) {
				spannable.setSpan(
					StrikethroughSpan(),
					selectedText.selectionStart,
					selectedText.selectionEnd,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
				)
			}
		}
	}

	private fun changeTextSize() {
		val selectedText: SelectedText? = getSelectedText(true, true)

		if (!TextUtils.isEmpty(selectedText?.text)) {
			val spannable: Spannable = content.text

			var spansExists: Boolean = false
			val spans =
				spannable.getSpans(
					selectedText!!.selectionStart,
					selectedText.selectionEnd,
					RelativeSizeSpan::class.java
				)

			for (element in spans) {
				spannable.removeSpan(element)
				spansExists = true

			}

			if (!spansExists) {
				// set the span
				spannable.setSpan(
					RelativeSizeSpan(1.2f),
					selectedText.selectionStart,
					selectedText.selectionEnd,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
				)
			}
		}
	}

	private fun highlightText() {
		val selectedText: SelectedText? = getSelectedText(true, true)

		if (!TextUtils.isEmpty(selectedText?.text)) {
			val spannable: Spannable = content.text
			val spans =
				spannable.getSpans(
					selectedText!!.selectionStart,
					selectedText.selectionEnd,
					BackgroundColorSpan::class.java
				)

			for (element in spans) {
				spannable.removeSpan(element)
			}

			spannable.setSpan(
				BackgroundColorSpan(context.resources.getColor(textColors[textColorIndex])),
				selectedText.selectionStart,
				selectedText.selectionEnd,
				Spannable.SPAN_INCLUSIVE_EXCLUSIVE
			)
		}
	}

	private fun removeColorSpan(selectedText: SelectedText) {
		val spannable: Spannable = content.text
		val spans =
			spannable.getSpans(
				selectedText.selectionStart,
				selectedText.selectionEnd,
				ForegroundColorSpan::class.java
			)

		for (element in spans) {
			spannable.removeSpan(element)
		}
	}

	private fun changeTextColor(resId: Int) {
		val selectedText: SelectedText? = getSelectedText(true, true)
		changeTextColor(resId, selectedText)
	}

	private fun changeTextColor(resId: Int, selectedText: SelectedText?) {
		if (!TextUtils.isEmpty(selectedText?.text)) {
			val spannable: Spannable = content.text

			removeColorSpan(selectedText!!)

			spannable.setSpan(
				ForegroundColorSpan(context.resources.getColor(resId)),
				selectedText.selectionStart,
				selectedText.selectionEnd,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
			)
		}
	}

	private fun View.clickWithDebounce(debounceTime: Long = 500L, action: () -> Unit) {
		this.setOnClickListener(object : OnClickListener {
			private var lastClickTime: Long = 0

			override fun onClick(v: View) {
				if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
				else action()

				lastClickTime = SystemClock.elapsedRealtime()
			}
		})
	}

	private fun renderDefaultHashTags(tags: List<Tag>) {
		tag_suggestion_chip_group.removeAllViews()
		for (tag in tags) {
			tag_suggestion_chip_group.addView(getChipView(tag))
		}
	}

	private fun getChipView(tag: Tag): Chip? {
		val chip = inflater.inflate(R.layout.suggestion_tag, null) as Chip
		chip.text = if (tag.label.startsWith("#")) tag.label else "#${tag.label}"
		chip.tag = tag
		chip.setOnClickListener {
			content.requestFocus()
			insertTagInView(it.tag as Tag)
		}
		return chip
	}

	private fun getTagsFromContent(regexPattern: Pattern): List<String> {
		var tags: List<String> = emptyList()
		val contentText = content.text.toString()
		val matcher: Matcher = regexPattern.matcher(contentText)
		while (matcher.find()) {
			val tag: String?  = matcher.group(1)
			tag?.let {
				tags = tags + it
			}
		}
		//Log.e("tags", tags.toString())
		return tags
	}

	companion object{
		private var TAG: String? = "TextPad"

		@JvmStatic
		fun getActualContentLength(text: String): Int {
			val htmlDescription = Html.fromHtml(text)
			return htmlDescription.toString().trim().length
		}

		private fun getSpannedContent(text: String): SpannableString {
			val htmlDescription = Html.fromHtml(text.trim())
			//val buffer: Spannable = SpannableString(htmlDescription)
			val length =  htmlDescription.toString().trim().length
			return SpannableString(htmlDescription.subSequence(0, length))
		}

		@JvmStatic
		fun getDecoratedContentText(text: String,
									tagColor: String,
									selectedPeopleTags: List<PeopleTag>,
									selectedHashTags: List<HashTag>,
									onClickTagListener: OnClickTagListener?): SpannableString {

			val contentText = getActualContentFromTemplateContent(text, selectedPeopleTags)
			return getDecoratedTagContentText(contentText, tagColor, selectedPeopleTags, selectedHashTags, onClickTagListener)
		}

		private fun getActualContentFromTemplateContent(text: String,
														selectedPeopleTags: List<PeopleTag>): String {
			var contentText = text
			if (!TextUtils.isEmpty(text)) {
				for (peopleTag in selectedPeopleTags) {
					contentText = contentText.replace(("@" + peopleTag.id), "@"+ peopleTag.username)
				}
			}
			return contentText
		}

		private fun  getTagByLabel(label: String,
								   selectedPeopleTags: List<PeopleTag>,
								   selectedHashTags: List<HashTag>): Tag? {
			var tag: Tag? = null
			if (label.isNotEmpty()) {
				if (label[0] == '#') {
					val hashTagLabel = label.substring(1, label.length)
					for (hashTag in selectedHashTags) {
						if (hashTag.label == hashTagLabel) {
							tag = hashTag
							break
						}
					}
				}
				else if (label[0] == '@') {
					val mentionTagLabel = label.substring(1, label.length)
					for (mentionTag in selectedPeopleTags) {
						if (mentionTag.username == mentionTagLabel) {
							tag = mentionTag
							break
						}
					}
				}
			}
			return tag
		}

		private fun getDecoratedTagContentText(content: String,
											   tagColor: String,
											   selectedPeopleTags: List<PeopleTag>,
											   selectedHashTags: List<HashTag>,
											   onClickTagListener: OnClickTagListener?): SpannableString {
			val contentText =  getSpannedContent(content)

			if (!TextUtils.isEmpty(contentText)) {
			//	val tagPlaceholder = "__TAG__"
			//	val tagColorHex = tagColor.toUpperCase(Locale.ROOT)
			//	val spannedTag = "<span style=\"color:$tagColorHex;\">$tagPlaceholder</span>"
				val regexPattern: Pattern = Pattern.compile("([#|@][a-zA-Z0-9_-]+)")
				val matcher: Matcher = regexPattern.matcher(contentText)
				while (matcher.find()) {
					val matchedText = matcher.group(1)!!.trim()
					if (selectedHashTags.any { "#" + it.label == matchedText} || selectedPeopleTags.any{ "@" + it.username == matchedText}) {
						val startIndex = contentText.indexOf(matchedText)
						val endIndex = startIndex + matchedText.length
						val tag: Tag? = getTagByLabel(matchedText, selectedPeopleTags, selectedHashTags)
						val cs: ClickableSpan = object : ClickableSpan() {
							override fun onClick(v: View) {
								if (null != tag) {
									onClickTagListener?.onClickTag(tag)
								}
								else {
									Log.e(TAG, "No tag found")
								}
							}

							override fun updateDrawState(ds: TextPaint) {
								super.updateDrawState(ds)
								ds.color = Color.parseColor(tagColor)
								ds.isUnderlineText = false
							}
						}
						contentText.setSpan(cs, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
					}
				}
			}
			// Log.e("Spanned text ", contentText.toString())
			return contentText
		}

	}
}

interface OnClickTagListener {
	fun onClickTag(tag: Tag)
}