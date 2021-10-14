package com.flagbeat.textpad

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_hash_tag.view.*
import kotlinx.android.synthetic.main.row_people_tag.view.*
import java.util.ArrayList

class AutoCompleteAdapter(
    context: Context?,
    private val tags: MutableList<Tag>
) : ArrayAdapter<String>(context!!, 0, tags.map { it.label }), Filterable {

    private var mFilter: ArrayFilter? = null

    override fun getCount(): Int {
        return tags.size
    }

    override fun getItem(position: Int): String {
        return if (tags[position].tagType == TagType.PEOPLE) (tags[position] as PeopleTag).username!! else tags[position].label
    }

    public fun update(newTags: List<Tag>) {
        tags.clear()
        tags.addAll(newTags)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view = convertView
        val layout =
            if (tags[position] is PeopleTag) R.layout.row_people_tag else R.layout.row_hash_tag
        // Check if an existing view is being reused, otherwise inflate the view
        if (view == null || view.tag != tags[position].tagType) {
            view = LayoutInflater.from(context).inflate(layout, parent, false)
            view?.tag = tags[position].tagType
        }

        if (tags[position] is PeopleTag) {
            bind(tags[position] as PeopleTag, view!!)
        }
        else {
            bind(tags[position] as HashTag, view!!)
        }

        // Lookup view for data population
//        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
//        TextView tvHome = (TextView) convertView.findViewById(R.id.tvHome);
//        // Populate the data into the template view using the data object
//        tvName.setText(user.name);
//        tvHome.setText(user.hometown);
        // Return the completed view to render on screen

        return view
    }

    fun bind(item: HashTag, itemView: View) {
        itemView.tag_name.text = item.label
    }

    fun bind(peopleTag: PeopleTag, itemView: View) {
        itemView.name.text = peopleTag.label
        if (!TextUtils.isEmpty(peopleTag.username)) {
            itemView.username.visibility = View.VISIBLE
            itemView.username.text = peopleTag.username
        }
        else {
            itemView.username.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(peopleTag.iconUrl)) {
            Picasso.get()
                .load(peopleTag.iconUrl)
                .fit()
                .placeholder(R.drawable.avatar_big)
                .error(R.drawable.avatar_big)
                .centerCrop()
                .into(itemView.avatar)
        }
    }

    override fun getFilter(): Filter {
        if (mFilter == null) {
            mFilter = ArrayFilter()
        }
        return mFilter!!
    }

    override fun clear() {
        tags.clear()
        super.clear()
    }

    private inner class ArrayFilter : Filter() {

        override fun performFiltering(prefix: CharSequence?): FilterResults {
            val results = FilterResults()

//            if (mOriginalValues == null) {
//                synchronized(lock!!) { mOriginalValues = ArrayList(fullList) }
//            }
//            if (prefix == null || prefix.length == 0) {
//                synchronized(lock!!) {
//                    val list = ArrayList(mOriginalValues)
//                    results.values = list
//                    results.count = list.size
//                }
//            } else {
//                val prefixString = prefix.toString().toLowerCase()
//                val values = mOriginalValues
//                val count = values!!.size
//                val newValues = ArrayList<String>(count)
//                for (i in 0 until count) {
//                    val item = values[i]
//                    if (item.toLowerCase().contains(prefixString)) {
//                        newValues.add(item)
//                    }
//                }
//                results.values = newValues
//                results.count = newValues.size
//            }
            if (tags.isNotEmpty() &&
                ((prefix?.startsWith("@") == true && tags[0].tagType == TagType.PEOPLE) ||
                (prefix?.startsWith("#") == true && tags[0].tagType == TagType.HASH))) {
                results.values = tags.map { it }
                results.count = tags.size
            }
            else {
                results.count = 0
            }

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
//            fullList = if (results.values != null) {
//                results.values as ArrayList<Tag>
//            } else {
//                ArrayList()
//            }
            if (results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }
}