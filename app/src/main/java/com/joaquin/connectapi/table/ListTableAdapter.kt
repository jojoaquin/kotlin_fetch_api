package com.joaquin.connectapi.table

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.joaquin.connectapi.R

class ListTableAdapter(private val tableItem: TableItem) : RecyclerView.Adapter<ListTableAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tableName: TextView = itemView.findViewById(R.id.tvTableName)
        val tableCode: TextView = itemView.findViewById(R.id.tvTableCode)
        val tableSubTotal: TextView = itemView.findViewById(R.id.tvTableSubTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.table_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return tableItem.data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tableItems = tableItem.data[position]

        holder.tableName.text = tableItems.name.toString()
        holder.tableCode.text = tableItems.code.toString()
        holder.tableSubTotal.text = "Rp. ${tableItems.sub_total.toString()}"

    }
}