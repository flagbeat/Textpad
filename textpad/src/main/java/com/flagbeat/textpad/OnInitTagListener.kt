package com.flagbeat.textpad

interface OnInitTagListener {
    fun onTypeTag(searchText: String, tagType: TagType, tagSuggestionResult: HashTagSuggestionResult)
}