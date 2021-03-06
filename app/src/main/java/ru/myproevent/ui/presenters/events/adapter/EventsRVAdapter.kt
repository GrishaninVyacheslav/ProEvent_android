package ru.myproevent.ui.presenters.events.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.myproevent.databinding.ItemEventBinding
import ru.myproevent.domain.utils.load
import ru.myproevent.ui.presenters.events.IEventItemView


class EventsRVAdapter(val presenter: IEventsListPresenter) :
    RecyclerView.Adapter<EventsRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = presenter.getCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        presenter.bindView(holder.apply { pos = position })

    inner class ViewHolder(private val vb: ItemEventBinding) : RecyclerView.ViewHolder(vb.root),
        IEventItemView {

        init {
            itemView.setOnClickListener { presenter.onItemClick(this) }
            vb.ivEditEvent.setOnClickListener { (presenter.onEditButtonClick(this)) }
        }

        override fun setName(name: String) {
            vb.tvEventName.text = name
        }

        override fun setTime(time: String) {
            vb.tvTime.text = time
        }

        override fun loadImg(url: String) {
            vb.ivImg.load(url)
        }

        override var pos = -1
    }
}