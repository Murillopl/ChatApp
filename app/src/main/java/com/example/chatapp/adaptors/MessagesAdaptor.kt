import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.mikhaellopez.circularimageview.CircularImageView

class MessagesAdaptor(
    private val context: Context,
    private val messages: MutableList<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val RECEIVER_TYPE_HOLDER = 1
    private val SENDER_TYPE_HOLDER = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == RECEIVER_TYPE_HOLDER) {
            MeViewHolder(
                LayoutInflater.from(context).inflate(R.layout.me, parent, false)
            )
        } else {
            SenderViewHolder(
                LayoutInflater.from(context).inflate(R.layout.sender, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        if (holder is MeViewHolder) {
            holder.textViewMessage.text = message.message
        }
        else if (holder is SenderViewHolder) {
            holder.textViewSender.text = message.message

            // ✅ imagem fixa do drawable
            holder.senderProfileImage.setImageResource(R.drawable.ic_profile)
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (FirebaseAuth.getInstance().currentUser?.uid == message.sender.uid) {
            RECEIVER_TYPE_HOLDER
        } else {
            SENDER_TYPE_HOLDER
        }
    }

    inner class MeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewMessage: TextView = view.findViewById(R.id.text_view_me)
    }

    inner class SenderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewSender: TextView = view.findViewById(R.id.sender_text_view)
        val senderProfileImage: CircularImageView =
            view.findViewById(R.id.sender_profile_image)
    }
}