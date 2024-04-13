    package com.example.pos

    import android.content.Context
    import android.graphics.Bitmap
    import android.graphics.Canvas
    import android.graphics.pdf.PdfDocument
    import android.os.Bundle
    import android.os.CancellationSignal
    import android.os.ParcelFileDescriptor
    import android.print.PageRange
    import android.print.PrintAttributes
    import android.print.PrintDocumentAdapter
    import android.print.PrintDocumentInfo
    import android.print.PrintManager
    import android.util.Log
    import android.view.View
    import android.widget.LinearLayout
    import android.widget.TextView
    import androidx.appcompat.app.AppCompatActivity
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.database.ValueEventListener
    import java.io.FileOutputStream
    import java.text.SimpleDateFormat
    import java.util.Date
    import java.util.Locale
    import kotlin.math.ceil

    class PrintActivity : AppCompatActivity() {

        private lateinit var databaseReference: DatabaseReference
        private val PAGE_HEIGHT = 1000 // Height of each page in pixels
        private var numPages = 0
        private lateinit var pdfAdapter: PdfPrintDocumentAdapter

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.transaction_layout)

            databaseReference = FirebaseDatabase.getInstance().getReference("history")
            Log.d(TAG, "Reference path: ${databaseReference.path}")
            Log.d(TAG, "PrintActivity onCreate() called")

            // Fetch receipt details from Firebase
            fetchReceiptDetails()
        }

        private fun fetchReceiptDetails() {
            val currentDate = getCurrentDate()
            Log.d(TAG, "Current Date: $currentDate")
            val currentDateParts = currentDate.split(" ")
            val currentWeekday = currentDateParts[0]
            val currentMonth = currentDateParts[1].substring(0, 3)
            val currentDay = currentDateParts[2]
            val currentGMT = currentDateParts[4]
            val currentYear = currentDateParts[5]

            Log.d(
                TAG,
                "fetchReceiptDetails() called for date: $currentWeekday $currentMonth $currentDay $currentGMT $currentYear"
            )

            val query = databaseReference.orderByChild("orderDateTime")
                .startAt("$currentWeekday $currentMonth $currentDay 00:00:00 $currentGMT $currentYear")
                .endAt("$currentWeekday $currentMonth $currentDay 23:59:59 $currentGMT $currentYear")
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "Snapshot: $snapshot")
                    if (snapshot.exists()) {
                        Log.d(TAG, "Number of children under history node: ${snapshot.childrenCount}")
                        var grandTotal = 0.0
                        var cashierName: String? = null
                        for (data in snapshot.children) {
                            Log.d(TAG, "Data key: ${data.key}, Data value: ${data.value}")
                            val receipt = data.getValue(ReceiptDetails::class.java)
                            receipt?.let {
                                displayReceipt(it)
                                grandTotal += it.total.toDouble() // Convert total to Double
                                if (cashierName == null) {
                                    // Set cashier name if not already set
                                    cashierName = it.staffName
                                }
                            }
                        }
                        cashierName?.let {
                            displayCashierAndTotal(it, grandTotal)
                        }
                    } else {
                        Log.d(TAG, "No data found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "fetchReceiptDetails() onCancelled: $error")
                }
            })
        }

        private fun displayCashierAndTotal(cashierName: String, grandTotal: Double) {
            val productDetailsLayout2 = findViewById<LinearLayout>(R.id.prod_details2)

            // Display cashier name
            val cashierTextView = TextView(this@PrintActivity)
            cashierTextView.text = "Cashier: $cashierName"
            productDetailsLayout2.addView(cashierTextView)

            // Display grand total
            val totalTextView = TextView(this@PrintActivity)
            totalTextView.text = "Grand Total: $grandTotal"
            productDetailsLayout2.addView(totalTextView)
        }

        private fun displayReceipt(receipt: ReceiptDetails) {
            Log.d(TAG, "displayReceipt() called")
            val dateFormat = SimpleDateFormat("MMMM dd yyyy", Locale.getDefault())
            val dateTime = findViewById<TextView>(R.id.date_time_trans)

            val orderDate =
                SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(receipt.orderDateTime)

            dateTime.text = dateFormat.format(orderDate)

            val productDetailsLayout = findViewById<LinearLayout>(R.id.prod_details)
            val productDetailsLayout2 = findViewById<LinearLayout>(R.id.prod_details2)

            // Iterate over order items and dynamically create TextViews
            for ((_, orderItems) in receipt.orderItems) {
                val itemLayout = LinearLayout(this)
                itemLayout.orientation = LinearLayout.HORIZONTAL

                val itemNameTextView = TextView(this)
                itemNameTextView.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                itemNameTextView.text = orderItems.productName
                itemNameTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                itemLayout.addView(itemNameTextView)

                val itemQuantityTextView = TextView(this)
                itemQuantityTextView.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                itemQuantityTextView.text = orderItems.quantity.toString()
                itemQuantityTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                itemLayout.addView(itemQuantityTextView)

                val itemVariationTextView = TextView(this)
                itemVariationTextView.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                itemVariationTextView.text = orderItems.variation
                itemVariationTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                itemLayout.addView(itemVariationTextView)

                val itemSizeTextView = TextView(this)
                itemSizeTextView.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                itemSizeTextView.text = orderItems.size
                itemSizeTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                itemLayout.addView(itemSizeTextView)

                val itemPriceTextView = TextView(this)
                itemPriceTextView.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                itemPriceTextView.text = orderItems.price.toString()
                itemPriceTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                itemLayout.addView(itemPriceTextView)

                val itemTotalTextView = TextView(this)
                itemTotalTextView.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                val itemTotal = orderItems.quantity * orderItems.price
                itemTotalTextView.text = itemTotal.toString()
                itemTotalTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                itemLayout.addView(itemTotalTextView)

                productDetailsLayout.addView(itemLayout)
            }
            productDetailsLayout.post {
                // Print the receipt after the layout is drawn
                printReceipt()
            }
        }

        private fun getCurrentDate(): String {
            val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val currentDate = Date()
            return dateFormat.format(currentDate)
        }

        private fun printReceipt() {
            Log.d(TAG, "printReceipt() called")

            // Get the root view of the layout
            val rootView = findViewById<View>(R.id.transaction_layout)

            // Create a bitmap of the entire layout
            val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            rootView.draw(canvas)

            // Initialize PDF adapter with the calculated number of pages
            pdfAdapter = PdfPrintDocumentAdapter(this, bitmap)

            // Print the PDF
            val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = getString(R.string.app_name) + " Document"
            printManager.print(jobName, pdfAdapter, null)
        }


        // PDF PrintDocumentAdapter
        private inner class PdfPrintDocumentAdapter(
            private val context: Context,
            private val bitmap: Bitmap
        ) : PrintDocumentAdapter() {

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                // Calculate the number of pages
                val numPages = calculateNumPages()

                // Build PrintDocumentInfo with page count
                val info = PrintDocumentInfo.Builder("transaction.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(numPages)
                    .build()

                // Notify the system about the print job attributes
                callback.onLayoutFinished(info, true)
            }

            private fun calculateNumPages(): Int {
                val printableAreaHeight = PAGE_HEIGHT // Assume the printable area height is the same as the page height
                val contentHeight = bitmap.height // Height of the content to be printed (bitmap)
                return ceil(contentHeight.toDouble() / printableAreaHeight.toDouble()).toInt()
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                if (destination == null) {
                    Log.e(TAG, "Error: ParcelFileDescriptor is null.")
                    callback.onWriteFailed("Error: ParcelFileDescriptor is null.")
                    return
                }

                val document = PdfDocument()
                val pageInfo =
                    PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                document.finishPage(page)

                val output = FileOutputStream(destination.fileDescriptor)
                try {
                    document.writeTo(output)
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    Log.e(TAG, "Error writing PDF to ParcelFileDescriptor: ${e.message}")
                    callback.onWriteFailed("Error writing PDF to ParcelFileDescriptor: ${e.message}")
                } finally {
                    document.close()
                    output.close()
                }
            }
        }

        companion object {
            const val TAG = "PrintActivity"
        }
    }