import android.content.Context
import android.graphics.Color
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast

class Hashtag(var context: Context) : ClickableSpan() {
    override fun updateDrawState(ds: TextPaint) {
        ds.setARGB(255, 51, 51, 51)
        ds.color = Color.RED
    }

    override fun onClick(widget: View) {
        val tv = widget as TextView
        val s = tv.text as Spanned
        val start = s.getSpanStart(this)
        val end = s.getSpanEnd(this)
        val theWord = s.subSequence(start + 1, end).toString()
        // you can start another activity here
        Toast.makeText(context, String.format("Here's a cool person: %s", theWord), 10)
            .show()
    }

}