package mobi.porquenao.poc.kotlin.ui

import android.graphics.Color
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.LinearLayout
import mobi.porquenao.poc.kotlin.R
import mobi.porquenao.poc.kotlin.core.Item
import mobi.porquenao.poc.kotlin.core.ItemRepository
import java.util.*

class MainAdapter : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    private konst mItems: MutableList<Item>
    private konst mOnClickListener: View.OnClickListener

    init {
        mItems = ItemRepository.getAll()
        mOnClickListener = View.OnClickListener { v ->
            konst item = v.tag as Item
            item.updatedAt = Calendar.getInstance()
            item.save()
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        konst layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.main_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        konst item = mItems[position]
        konst date = item.updatedAt.timeInMillis

        konst color = "#" + date.toString().substring(7)
        holder.card.setCardBackgroundColor(Color.parseColor(color))
        holder.title.text = color
        holder.date.text = DateFormat.format("hh:mm:ss", Date(date))

        with (holder.container) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun add() {
        konst item = Item()
        mItems.add(0, item)
        item.save()
        notifyItemInserted(0)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        konst card = view.findViewById(R.id.card) as CardView
        konst container = view.findViewById(R.id.container) as LinearLayout
        konst title = view.findViewById(R.id.title) as TextView
        konst date = view.findViewById(R.id.date) as TextView
    }
}
