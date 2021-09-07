package `in`.nakkalites.mediaclient.viewmodel.utils

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class StyleFormatTextAdapter : JsonAdapter<StyleFormatText>() {
    override fun fromJson(reader: JsonReader): StyleFormatText {
        return StyleFormatText(reader.nextString())
    }

    override fun toJson(writer: JsonWriter, value: StyleFormatText?) {
        when {
            value == null -> writer.value(null as String?)
            value.plainText != null -> writer.value(value.plainText)
            else -> throw UnsupportedOperationException()
        }
    }

}
