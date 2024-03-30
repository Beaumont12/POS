package com.example.pos

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MyAdapter(
    private val context: Context,
    private var productList: MutableList<Product>,
    private var addToCartClickListener: OnAddToCartClickListener

) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.each_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        this.addToCartClickListener = addToCartClickListener

        Glide.with(context)
            .load(product.imageURL)
            .into(holder.itemImage)

        holder.textProductName.text = "${product.productName}  ${getFormattedPrice(product.hotVariations.values.firstOrNull()) ?: "-"}"
        holder.textCategory.text = "Category: ${product.category.uppercase()}"

        // Set sizes buttons
        holder.sizeButtonsContainer.removeAllViews()
        if (product.isHot) {
            updateButtons(holder, product.hotVariations)
        } else {
            updateButtons(holder, product.icedVariations)
        }

        // Set up buttons to switch between hot and iced variations
        holder.buttonHot.setOnClickListener {
            product.isHot = true
            updateButtons(holder, product.hotVariations)
            notifyDataSetChanged()
        }

        holder.buttonIced.setOnClickListener {
            product.isHot = false
            updateButtons(holder, product.icedVariations)
            notifyDataSetChanged()
        }

        // Add to cart button click listener
        holder.addToCartButton.setOnClickListener {
            addToCartClickListener?.onAddToCartClicked(product)
        }
    }
    interface OnAddToCartClickListener {
        fun onAddToCartClicked(product: Product)
    }

    private fun updateButtons(holder: ViewHolder, variations: Map<String, Int>) {
        val layoutParams = ViewGroup.LayoutParams(
            50.dpToPx(), // Set the width to a fixed value (60dp in this case)
            50.dpToPx() // Set the height to a fixed value (60dp in this case)
        )

        variations.forEach { (size, _) ->
            val button = Button(context)
            button.text = size
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            button.setTextColor(ContextCompat.getColor(context, R.color.black))
            button.background = getButtonBackground()
            button.elevation = 10.dpToPx().toFloat()
            button.setOnClickListener {
                holder.textProductName.text = "${holder.textProductName.text.split("  ")[0]}  ${getFormattedPrice(variations[size]) ?: "-"}"
            }
            button.layoutParams = layoutParams // Set equal width and height
            holder.sizeButtonsContainer.addView(button)
        }
    }

    private fun getButtonBackground(): Drawable {
        // Create a shape drawable for the button background
        val shapeDrawable = GradientDrawable()
        shapeDrawable.shape = GradientDrawable.OVAL
        shapeDrawable.setStroke(2.dpToPx(), ContextCompat.getColor(context, R.color.green)) // Green border color
        shapeDrawable.setColor(Color.WHITE) // Button background color
        shapeDrawable.cornerRadius = 50.dpToPx().toFloat() // Button corner radius
        return shapeDrawable
    }

    private fun Int.dpToPx(): Int {
        val scale = context.resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    private fun getFormattedPrice(price: Int?): String? {
        return price?.let { "P$it" }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textProductName: TextView = itemView.findViewById(R.id.text_product_name)
        val textCategory: TextView = itemView.findViewById(R.id.text_category)
        val sizeButtonsContainer: ViewGroup = itemView.findViewById(R.id.size_buttons_container)
        val buttonHot: Button = itemView.findViewById(R.id.button_hot)
        val buttonIced: Button = itemView.findViewById(R.id.button_iced)
        val addToCartButton: ImageButton = itemView.findViewById(R.id.add_cart)
        val itemImage: ImageView = itemView.findViewById(R.id.item_image)
    }

    fun updateProductList(newProductList: List<Product>) {
        productList.clear()
        if (newProductList.isNotEmpty()) {
            productList.addAll(newProductList)
        }
        notifyDataSetChanged()
    }

}
