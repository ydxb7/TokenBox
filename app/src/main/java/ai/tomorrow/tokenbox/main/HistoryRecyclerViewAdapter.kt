package ai.tomorrow.tokenbox.main

import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.databinding.ListItemHistoryBinding
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.text.SimpleDateFormat

class HistoryRecyclerViewAdapter(private var myData: List<DatabaseHistory>) :
    RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>() {


    public val TAG = "HistoryAdapter"

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
        val history = myData[position]
        (holder as ViewHolder).bind(history)
    }

    override fun getItemCount(): Int {
        return myData.size
    }

    fun setData(data: List<DatabaseHistory>){
        myData = data
        notifyDataSetChanged()
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


                Log.d("HistoryAdapterAdapter", "timestamp = ${history.timeStamp}")
                Log.d("HistoryAdapterAdapter", "hash = ${history.hash}")
                val timeString = SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(history.timeStamp * 1000)

                timeTv.text = timeString

                if (isFrom){
                    addressTv.text = history.from
                    fromToTv.text = "From: "
                } else{
                    addressTv.text = history.to
                    fromToTv.text = "To: "
                }


                if (history.isError == 1) {
                    valueTv.text = "Error"
                    valueTv.setTextColor(Color.RED)
                } else {
                    if (isFrom) {
                        valueTv.text = "+ ${ether} ETH"
                        valueTv.setTextColor(Color.GREEN)
//                        valueTv.textColors
                    } else {
                        valueTv.text = "- ${ether} ETH"
                        valueTv.setTextColor(Color.RED)
                    }
                }

                executePendingBindings()
            }
        }
    }
}
