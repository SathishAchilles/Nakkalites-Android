package `in`.nakkalites.mediaclient.view

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils

abstract class BaseActivity : AppCompatActivity() {

    protected fun setupToolbar(
        toolbar: Toolbar, showHomeAsUp: Boolean = true, upIsBack: Boolean = false
    ) {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(showHomeAsUp)
        if (showHomeAsUp) {
            if (upIsBack) { // calls either overridden onBackPressed() or super.onBackPressed()
                toolbar.setNavigationOnClickListener { v: View? -> onBackPressed() }
            } else {
                toolbar.setNavigationOnClickListener { v: View? ->
                    val intent = NavUtils.getParentActivityIntent(this@BaseActivity)
                    intent!!.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    NavUtils.navigateUpTo(this@BaseActivity, intent)
                }
            }
        }
    }

}
