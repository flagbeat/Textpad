package com.flagbeat.textpad
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_people_tag.view.*
import kotlinx.android.synthetic.main.row_hash_tag.view.*

class TagAdapter(
    private val tagList: MutableList<Tag>, private val listener: (Tag) -> Unit
): RecyclerView.Adapter<TagAdapter.BaseViewHolder<*>>() {

    override fun getItemCount() = tagList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            TagType.PEOPLE.ordinal -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.row_people_tag, parent, false)
                PeopleTagViewHolder(view)
            }
            TagType.HASH.ordinal -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.row_hash_tag, parent, false)
                HashTagViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return tagList[position].tagType.ordinal
    }

    fun refresh(tags: List<Tag>) {
        tagList.clear()
        tagList.addAll(tags)
        notifyDataSetChanged()
    }

    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }

    inner class PeopleTagViewHolder(itemView: View) : BaseViewHolder<Tag>(itemView) {
        override fun bind(item: Tag) {
            val peopleTag = item as PeopleTag
            itemView.name.text = peopleTag.label
            if (!TextUtils.isEmpty(peopleTag.username)) {
                itemView.username.visibility = View.VISIBLE
                itemView.username.text = "@" + peopleTag.username
            }
            else {
                itemView.username.visibility = View.GONE
            }
            if (!TextUtils.isEmpty(item.iconUrl)) {
                Picasso.get()
                    .load(peopleTag.iconUrl)
                    .fit()
                    .placeholder(R.drawable.avatar_big)
                    .error(R.drawable.avatar_big)
                    .centerCrop()
                    .into(itemView.avatar)
            }
            itemView.setOnClickListener{
                listener(item)
            }
        }
    }

    inner class HashTagViewHolder(itemView: View) : BaseViewHolder<Tag>(itemView) {
        override fun bind(item: Tag) {
            itemView.tag_name.text = if (item.label.startsWith("#")) item.label else "#" + item.label
            itemView.setOnClickListener{
                listener(item)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val tag = tagList[position]
        when (holder) {
            is PeopleTagViewHolder -> holder.bind(tag)
            is HashTagViewHolder -> holder.bind(tag)
            else -> throw IllegalArgumentException()

        }
    }
}