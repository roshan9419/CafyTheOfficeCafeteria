package adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.programmingtech.cafy_theofficecafeteria.R
import datamodels.SavedCardItem

class RecyclerSavedCardsAdapter(
    val context: Context,
    private val items: ArrayList<SavedCardItem>,
    private val amountToPay: Float,
    private val listener: OnItemClickListener
): RecyclerView.Adapter<RecyclerSavedCardsAdapter.ItemListViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemPayButtonClick(position: Int)
    }

    class ItemListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardHolderNameTV: TextView = itemView.findViewById(R.id.saved_card_item_card_holder_name_tv)
        val cardNumberTV: TextView = itemView.findViewById(R.id.saved_card_item_card_number_tv)
        val cardExpDateTV: TextView = itemView.findViewById(R.id.saved_card_item_exp_date_tv)
        val cardCVVET: EditText = itemView.findViewById(R.id.saved_card_item_cvv_et)
        val cardPayBtn: Button = itemView.findViewById(R.id.saved_card_item_pay_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.saved_card_item, parent, false)
        return ItemListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {
        val currentItem = items[position]

        val cardNo = currentItem.cardNumber
        holder.cardNumberTV.text = "${cardNo.substring(0,4)} xxxx xxxx ${cardNo.substring(12, 16)}"
        holder.cardHolderNameTV.text = currentItem.cardHolderName
        holder.cardExpDateTV.text = currentItem.cardExpiryDate
        holder.cardPayBtn.text = "Pay \$%.2f".format(amountToPay)

        if(currentItem.isSelected) {
            holder.cardCVVET.visibility = ViewGroup.VISIBLE
            holder.cardPayBtn.visibility = ViewGroup.VISIBLE
        } else {
            holder.cardCVVET.visibility = ViewGroup.GONE
            holder.cardPayBtn.visibility = ViewGroup.GONE
        }

        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }

        holder.cardPayBtn.setOnClickListener {
            if(validateCVV(holder.cardCVVET.text.toString()))
            listener.onItemPayButtonClick(position)
        }
    }

    private fun validateCVV(cvv: String): Boolean {
        if(cvv.isEmpty()) {
            Toast.makeText(context, "ENTER CVV", Toast.LENGTH_SHORT).show()
            return false
        }
        if(cvv.length != 3) {
            Toast.makeText(context, "INVALID CVV", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun getItemCount(): Int = items.size
}