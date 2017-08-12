package org.didong.didong

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)