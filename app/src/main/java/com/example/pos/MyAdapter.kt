package com.example.pos

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MyAdapter(
    private val context: Context,
    private var productList: MutableList<Product>,
    private var addToCartClickListener: OnAddToCartClickListener
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    private var selectedProduct: Product? = null
    private var selectedSize: String? = null
    private var selectedPrice: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.each_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        Glide.with(context)
            .load(product.imageURL)
            .into(holder.itemImage)

        holder.textProductName.text = product.productName
        holder.textPrices.text = getFormattedPrice(product.hotVariations.values.firstOrNull()) ?: ""
        holder.textCategory.text = "Category: ${product.category.uppercase()}"
        updateStockStatus(holder, product.stockStatus)


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
            selectedProduct?.let { product ->
                selectedSize?.let { size ->
                    val temperature = if (product.isHot) "Hot" else "Iced"
                    if (selectedPrice != 0) {
                        // Pass the selected product details to the listener
                        addToCartClickListener?.onAddToCartClicked(product, temperature, size, selectedPrice)
                    } else {
                        // Handle case where price is unavailable or 0
                        Toast.makeText(context, "Price unavailable for ${product.productName}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateStockStatus(holder: ViewHolder, stockStatus: String) {
        if (stockStatus.equals("Out of Stock", ignoreCase = true)) {
            holder.stockStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
        } else {
            holder.stockStatus.setTextColor(ContextCompat.getColor(context, R.color.green))
        }
        holder.stockStatus.text = stockStatus
    }


    interface OnAddToCartClickListener {
        fun onAddToCartClicked(product: Product, temperature: String, size: String, price: Int)
    }


    private fun updateButtons(holder: ViewHolder, variations: Map<String, Int>) {
        val layoutParams = ViewGroup.LayoutParams(
            50.dpToPx(),
            50.dpToPx()
        )

        variations.forEach { (size, _) ->
            val button = Button(context)
            button.text = size
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            button.setTextColor(ContextCompat.getColor(context, R.color.black))
            button.background = getButtonBackground()
            button.elevation = 10.dpToPx().toFloat()
            button.setOnClickListener {
                selectedProduct = productList[holder.adapterPosition]
                selectedSize = size
                selectedPrice = variations[size] ?: 0
                holder.textPrices.text = getFormattedPrice(variations[size]) ?: ""
            }
            button.layoutParams = layoutParams
            holder.sizeButtonsContainer.addView(button)
        }
    }

    private fun getButtonBackground(): Drawable {
        val shapeDrawable = GradientDrawable()
        shapeDrawable.shape = GradientDrawable.OVAL
        shapeDrawable.setStroke(2.dpToPx(), ContextCompat.getColor(context, R.color.green))
        shapeDrawable.setColor(Color.WHITE)
        shapeDrawable.cornerRadius = 50.dpToPx().toFloat()
        return shapeDrawable
    }

    private fun Int.dpToPx(): Int {
        val scale = context.resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    private fun getFormattedPrice(price: Int?): String? {
        return price?.let { "P$it" }
    }

    init {
        Log.d("MyAdapter", "Product list size: ${productList.size}")
    }

    override fun getItemCount(): Int {
        Log.d("MyAdapter", "getItemCount() called. Product list size: ${productList.size}")
        return productList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textProductName: TextView = itemView.findViewById(R.id.text_product_name)
        val textPrices: TextView = itemView.findViewById(R.id.text_prices)
        val textCategory: TextView = itemView.findViewById(R.id.text_category)
        val sizeButtonsContainer: ViewGroup = itemView.findViewById(R.id.size_buttons_container)
        val buttonHot: Button = itemView.findViewById(R.id.button_hot)
        val buttonIced: Button = itemView.findViewById(R.id.button_iced)
        val addToCartButton: ImageButton = itemView.findViewById(R.id.add_cart)
        val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        val stockStatus: TextView = itemView.findViewById(R.id.stock_status)
    }

    fun updateProductList(newProductList: List<Product>) {
        productList = newProductList.toMutableList()
        notifyDataSetChanged()
    }

}