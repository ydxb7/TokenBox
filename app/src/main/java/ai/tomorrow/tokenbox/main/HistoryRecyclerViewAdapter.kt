package ai.tomorrow.tokenbox.main

import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.databinding.ListItemHistoryBinding
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.web3j.utils.Convert
import java.math.BigDecimal

class HistoryRecyclerViewAdapter :
    ListAdapter<DatabaseHistory, HistoryRecyclerViewAdapter.ViewHolder>(HistoryDiffCallback()) {

    public val TAG = "HistoryAdapterAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(TAG, "onCreateViewHolder")
        return ViewHolder(
            ListItemHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder")
        val history = getItem(position)
        (holder as ViewHolder).bind(history)
    }

    class ViewHolder(
        private val binding: ListItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(history: DatabaseHistory) {
            Log.d("HistoryAdapter", "bind")

            binding.apply {
                val isFrom = (history.myAddress == history.to)
                Log.d("HistoryAdapterAdapter", "isFrom = $isFrom")
                val ether = Convert.fromWei(BigDecimal(history.value), Convert.Unit.ETHER).toFloat()

                if (isFrom){
                    addressTv.text = history.from
                    fromToTv.text = "From: "
                } else{
                    addressTv.text = history.to
                    fromToTv.text = "To: "
                }


                if (history.isError == 1) {
                    valueTv.text = "Error"
                } else {
                    if (isFrom) {
                        valueTv.text = "+ ${ether} ETH"
//                        valueTv.textColors
                    } else {
                        valueTv.text = "- ${ether} ETH"
                    }
                }





                executePendingBindings()
            }
        }
    }
}

private class HistoryDiffCallback : DiffUtil.ItemCallback<DatabaseHistory>() {

    override fun areItemsTheSame(
        oldItem: DatabaseHistory,
        newItem: DatabaseHistory
    ): Boolean {
        return oldItem.hash == newItem.hash
    }

    override fun areContentsTheSame(
        oldItem: DatabaseHistory,
        newItem: DatabaseHistory
    ): Boolean {
        return oldItem == newItem
    }
}