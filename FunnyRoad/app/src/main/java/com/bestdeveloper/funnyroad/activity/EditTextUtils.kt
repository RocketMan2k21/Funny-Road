import android.content.Context
import android.graphics.Color
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.addTextChangedListener
import com.bestdeveloper.funnyroad.R

object EditTextUtils {
    fun highlightFieldAndSetError(context: Context, s: EditText, errorText: TextView, errorMessage: String) {
        s.background = AppCompatResources.getDrawable(context, R.drawable.log_in_error_input)
        errorText.text = errorMessage
            }
    fun trimInput(s: EditText): String{
        return s.getText().toString().trim { it <= ' ' }
    }
    fun setDefaultStrokeOnChangedEditText(ctx: Context, e: EditText){
        e.addTextChangedListener {
            e.background = AppCompatResources.getDrawable(ctx, R.drawable.custom_welcome_edit)
        }
    }
}
