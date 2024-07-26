// SettingsAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.siddiqui.safedrivealert.R

class SettingsAdapter(
    private val settingsList: List<String>,
    private val clickListener: (String) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_setting, parent, false)
        return SettingsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        val setting = settingsList[position]
        holder.settingName.text = setting
        holder.itemView.setOnClickListener { clickListener(setting) }
    }

    override fun getItemCount() = settingsList.size

    class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val settingName: TextView = itemView.findViewById(R.id.setting_name)
    }
}
