package com.gamaruzi.cifras.ui.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

// Tipicamente o LocalContext do Compose é uma Activity (ou wrapper dela),
// mas a API expõe Context — precisamos descer pelos wrappers até achar a
// Activity para usar window/insetsController.
fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
