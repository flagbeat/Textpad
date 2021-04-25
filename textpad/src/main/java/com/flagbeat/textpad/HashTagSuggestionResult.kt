package com.flagbeat.textpad

interface HashTagSuggestionResult {
    fun onReady(searchText: String, tagType: TagType, tags: List<Tag>)
}