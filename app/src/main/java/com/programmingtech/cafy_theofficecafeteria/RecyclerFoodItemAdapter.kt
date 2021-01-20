package com.programmingtech.cafy_theofficecafeteria

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class RecyclerFoodItemAdapter(
    var context: Context,
    private var itemList: ArrayList<MenuItem>,
    private val loadDefaultImage: Int,
    listener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerFoodItemAdapter.ItemListViewHolder>(), Filterable {

    private var totalPrice = 0F
    private var totalItems = 0

    private val orderedList = ArrayList<MenuItem>() //contains the list of ordered items
    private var fullItemList = ArrayList<MenuItem>(itemList)

    private val adapterListener = listener

    interface OnItemClickListener {
        fun onItemClick(item: MenuItem)
    }

    class ItemListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImageIV: ImageView = itemView.findViewById(R.id.item_image)
        val itemNameTV: TextView = itemView.findViewById(R.id.item_name)
        val itemPriceTV: TextView = itemView.findViewById(R.id.item_price)
        val itemStarsTV: TextView = itemView.findViewById(R.id.item_stars)
        val itemShortDesc: TextView = itemView.findViewById(R.id.item_short_desc)
        val itemQuantityTV: TextView = itemView.findViewById(R.id.item_quantity_tv)
        val itemQuantityIncreaseIV: ImageView = itemView.findViewById(R.id.increase_item_quantity_iv)
        val itemQuantityDecreaseIV: ImageView = itemView.findViewById(R.id.decrease_item_quantity_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerFoodItemAdapter.ItemListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_menu_item, parent, false)
        fullItemList = ArrayList<MenuItem>(itemList)
        return ItemListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerFoodItemAdapter.ItemListViewHolder, position: Int) {
        val currentItem = itemList[position]

        if(loadDefaultImage == 1) holder.itemImageIV.setImageResource(R.drawable.default_item_image)
        else Picasso.get().load(currentItem.imageUrl).into(holder.itemImageIV)

        holder.itemNameTV.text = currentItem.itemName
        holder.itemPriceTV.text = "$${currentItem.itemPrice}"
        holder.itemStarsTV.text = currentItem.itemStars.toString()
        holder.itemShortDesc.text = currentItem.itemShortDesc
        holder.itemQuantityTV.text = currentItem.quantity.toString()

        holder.itemQuantityIncreaseIV.setOnClickListener {
            val n = holder.itemQuantityTV.text.toString().toInt()
            holder.itemQuantityTV.text = (n+1).toString()
            totalPrice += currentItem.itemPrice
            totalItems++
            currentItem.quantity = n+1
            if(n==0) {
                //add the new item in the ordered list
                orderedList.add(currentItem)
            }
        }
        holder.itemQuantityDecreaseIV.setOnClickListener {
            val n = holder.itemQuantityTV.text.toString().toInt()
            if(n!=0) {
                holder.itemQuantityTV.text = (n-1).toString()
                totalPrice -= currentItem.itemPrice
                totalItems--
                currentItem.quantity = n-1

                if(n-1 == 0) orderedList.remove(currentItem) //means quantity of item is now 0, so remove ordered from list
            }
        }

        holder.itemView.setOnClickListener {
            adapterListener.onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun getTotalPrice(): Float = totalPrice
    fun getTotalItems(): Int = totalItems

    fun getOrderedList(): ArrayList<MenuItem> = orderedList

    fun filterList(filteredList : ArrayList<MenuItem>) {
        itemList = filteredList
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return searchFilter;
    }

    private val searchFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<MenuItem>()
            if(constraint!!.isEmpty()) {
                filteredList.addAll(fullItemList)
            } else {
                val filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim()

                for(item in fullItemList) {
                    if(item.itemName.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            itemList.clear()
            itemList.addAll(results!!.values as ArrayList<MenuItem>)
            notifyDataSetChanged()
        }

    }
}