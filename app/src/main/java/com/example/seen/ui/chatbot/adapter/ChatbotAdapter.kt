package com.example.seen.ui.chatbot.adapter

import android.text.style.LeadingMarginSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.seen.R
import com.example.seen.domain.model.chatbot.ChatMessage
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import org.commonmark.node.Paragraph

class ChatbotAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_BOT = 2
        private const val TYPE_TYPING = 3
    }

    private val messages = mutableListOf<ChatMessage>()

    override fun getItemViewType(position: Int): Int {

        return when {
            messages[position].isTyping -> TYPE_TYPING
            messages[position].isUser -> TYPE_USER
            else -> TYPE_BOT
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return when (viewType) {

            TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_message_user,
                        parent,
                        false
                    )

                UserViewHolder(view)
            }

            TYPE_BOT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_message_bot,
                        parent,
                        false
                    )

                BotViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_message_typing,
                        parent,
                        false
                    )

                TypingViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        when (holder) {

            is UserViewHolder -> holder.bind(messages[position])

            is BotViewHolder -> holder.bind(messages[position])

            is TypingViewHolder -> holder.bind()
        }
    }

//    fun submitList(newList: List<ChatMessage>) {
//        messages.clear()
//        messages.addAll(newList)
//        notifyDataSetChanged()
//    }

    fun submitList(newList: List<ChatMessage>) {
        val oldSize = messages.size
        messages.clear()
        messages.addAll(newList)

        if (newList.size > oldSize) {
            // New items were added
            notifyItemRangeInserted(oldSize, newList.size - oldSize)
        } else {
            notifyDataSetChanged()
        }
    }

    class UserViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvMessage =
            itemView.findViewById<TextView>(R.id.tvMessage)

        fun bind(item: ChatMessage) {
            tvMessage.text = item.message
        }
    }

    class BotViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvMessage =
            itemView.findViewById<TextView>(R.id.tvMessage)

//        private val markwon = Markwon.create(itemView.context)

        private val markwon = Markwon.builder(itemView.context)
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                    // Reduce spacing after paragraphs
                    builder.setFactory(Paragraph::class.java) { _, _ ->
                        LeadingMarginSpan.Standard(0)
                    }
                }
            })
            .build()

        fun bind(item: ChatMessage) {
            markwon.setMarkdown(tvMessage, item.message)
        }
    }

    class TypingViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val dot1 =
            itemView.findViewById<View>(R.id.dot1)

        private val dot2 =
            itemView.findViewById<View>(R.id.dot2)

        private val dot3 =
            itemView.findViewById<View>(R.id.dot3)

        fun bind() {

            dot1.startAnimation(
                AnimationUtils.loadAnimation(
                    itemView.context,
                    R.anim.typing_dot
                )
            )

            dot2.startAnimation(
                AnimationUtils.loadAnimation(
                    itemView.context,
                    R.anim.typing_dot
                ).apply {
                    startOffset = 150
                }
            )

            dot3.startAnimation(
                AnimationUtils.loadAnimation(
                    itemView.context,
                    R.anim.typing_dot
                ).apply {
                    startOffset = 300
                }
            )
        }
    }
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        if(holder is TypingViewHolder){
            holder.itemView.clearAnimation()
        }
    }
}