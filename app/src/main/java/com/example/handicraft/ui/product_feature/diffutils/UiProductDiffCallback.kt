import androidx.recyclerview.widget.DiffUtil
import com.example.handicraft_graduation_project_2025.ui.product_feature.adapters.UiProduct

class UiProductDiffCallback(
    private val oldList: List<UiProduct>,
    private val newList: List<UiProduct>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].product.productId == newList[newItemPosition].product.productId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
