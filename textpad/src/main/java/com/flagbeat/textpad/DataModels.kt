package com.flagbeat.textpad

import androidx.annotation.Keep

@Keep
interface Tag {
    val id: String
    val label: String
    val iconUrl: String?
    val tagType: TagType
    val isSelected: Boolean
}

@Keep
data class PeopleTag(override val id: String,
                     override val label: String,
                     override val iconUrl: String?,
                     override val tagType: TagType,
                     override val isSelected: Boolean,
                     val username: String?
) : Tag

@Keep
data class HashTag(override val id: String,
                   override val label: String,
                   override val iconUrl: String?,
                   override val tagType: TagType,
                   override val isSelected: Boolean
) : Tag

enum class TagType {
    HASH, PEOPLE, NONE
}

@Keep
data class SelectedText(
    var text: String,
    var selectionStart: Int,
    var selectionEnd: Int
)

@Keep
data class ContentTheme(
    var themeCode: String,
    var textColor: String,
    var hintColor: String,
    var imageUrl: String? = null,
    var bgColor: String? = null
)
