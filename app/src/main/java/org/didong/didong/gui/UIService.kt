package org.didong.didong.gui

import android.app.Activity
import com.google.android.material.snackbar.Snackbar
import android.view.View
import org.didong.didong.R

/**
 * Created by vincent.couturier@gmail.com on 01/01/2018.
 */
class UIService {

    fun showMessage(view: View, msg: String) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setAction(view.resources.getString(R.string.action_close), null)
                .show()

    }
    fun showMessage(activity: Activity, msg: String) = showMessage(activity.window.decorView, msg)

    fun showError(view: View, e: Exception) {
        Snackbar.make(view, "${view.resources.getString(R.string.error_occurs)} ${e.message}", Snackbar.LENGTH_LONG)
                .setAction(view.resources.getString(R.string.action_close), null).show()
    }
    fun showError(activity: Activity, e: Exception) = showError(activity.window.decorView, e)

}