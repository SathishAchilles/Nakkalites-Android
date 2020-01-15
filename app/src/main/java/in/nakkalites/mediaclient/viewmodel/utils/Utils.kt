package `in`.nakkalites.mediaclient.viewmodel.utils


fun String.beginWithUpperCase(): String {
    return when (this.length) {
        0 -> ""
        1 -> this.toUpperCase()
        else -> this[0].toUpperCase() + this.substring(1)
    }
}

fun String.toCamelCase(): String {
    return this.split('_').joinToString("") {
        it.beginWithUpperCase()
    }
}
