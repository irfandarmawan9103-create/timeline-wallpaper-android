
package id.timelinewallpaper

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 28)
            setBackgroundColor(Color.BLACK)
        }

        fun addLine(text: String, size: Float, color: Int, bold: Boolean = false) {
            root.addView(TextView(this).apply {
                this.text = text
                textSize = size
                setTextColor(color)
                gravity = Gravity.START
                if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 8, 0, 8)
            })
        }

        addLine("TIMELINE WALLPAPER", 22f, Color.WHITE, true)
        addLine("Widget timeline harian", 16f, Color.rgb(85, 228, 219))
        addLine("Cara menambahkan widget:", 16f, Color.WHITE, true)
        addLine("1. Kembali ke homescreen.", 14f, Color.LTGRAY)
        addLine("2. Tekan lama area kosong.", 14f, Color.LTGRAY)
        addLine("3. Pilih Widget > Timeline Wallpaper.", 14f, Color.LTGRAY)

        setContentView(root)
    }
}
