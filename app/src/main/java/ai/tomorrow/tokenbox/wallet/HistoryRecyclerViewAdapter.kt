package ai.tomorrow.tokenbox.wallet

import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.databinding.ListItemHistoryBinding
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.text.SimpleDateFormat

enum class Direction {
    IN, OUT, SELF
}

class HistoryRecyclerViewAdapter(private var myData: List<DatabaseHistory>) :
    RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>() {

    private val TAG = "HistoryAdapter"

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

    fun setData(data: List<DatabaseHistory>) {
        myData = data
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ListItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: DatabaseHistory) {
            Log.d("HistoryAdapter", "bind")

            binding.apply {
                val isFrom = (history.myAddress == history.to)
                Log.d("HistoryAdapterAdapter", "isFrom = $isFrom")
                val ether = Convert.fromWei(BigDecimal(history.value), Convert.Unit.ETHER).toFloat()

                Log.d("HistoryAdapterAdapter", "timestamp = ${history.timeStamp}")
                Log.d("HistoryAdapterAdapter", "hash = ${history.hash}")
                val timeString =
                    SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(history.timeStamp * 1000)

                timeTv.text = timeString

                var state: Direction

                // setup widgets text and color
                if (history.from == history.myAddress && history.to == history.myAddress) {
                    state = Direction.SELF
                    addressTv.text = history.myAddress
                    fromToTv.text = "SELF"
                } else if (history.myAddress == history.to) {
                    state = Direction.IN
                    addressTv.text = history.from
                    fromToTv.text = "IN"
                } else {
                    state = Direction.OUT
                    addressTv.text = history.to
                    fromToTv.text = "OUT"
                }

                if (history.isError == 1) {
                    pendingLo.visibility = View.GONE
                    valueTv.text = "ERROR"
                    valueTv.setTextColor(Color.RED) // red
                    colorBar.setBackgroundColor(Color.RED) // red
                } else {
                    pendingLo.visibility = View.GONE
                    when (state) {
                        Direction.SELF -> {
                            valueTv.text = "  ${ether} ETH"
                            valueTv.setTextColor(Color.GRAY)
                            colorBar.setBackgroundColor(Color.GRAY)
                        }
                        Direction.OUT -> {
                            valueTv.text = "- ${ether} ETH"
                            valueTv.setTextColor(Color.RED) // red
                            colorBar.setBackgroundColor(Color.RED)
                        }
                        Direction.IN -> {
                            valueTv.text = "+ ${ether} ETH"
                            valueTv.setTextColor(Color.GREEN)
                            colorBar.setBackgroundColor(Color.GREEN)
                        }
                    }

                    if (history.isError == 2) {
                        pendingLo.visibility = View.VISIBLE
                    }
                }
                executePendingBindings()
            }
        }
    }
}
