package org.didong.didong.gui

import android.app.Activity
import android.support.design.widget.Snackbar
import android.view.View
import org.didong.didong.R

/**
 * Created by vincent.couturier@gmail.com on 01/01/2018.
 */
open class UIService {
    open fun showMessage(view: View, msg: String) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setAction("Close", null)
                .show()
    }
    open fun showError(view: View, e: Exception) {
        Snackbar.make(view, "Error occurs ${e?.message}", Snackbar.LENGTH_LONG)
                .setAction("Close", null).show()
    }
}