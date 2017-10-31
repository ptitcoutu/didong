package org.didong.didong.event

import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by Vincent Couturier on 02/07/2017.
 */
class EventDescription(val tags: List<String>, val started: Boolean) {
    companion object {
        fun fromJson(jsonContent : String) : EventDescription {
            try {
                val jsonObject = JSONObject(jsonContent)
                val tagsArray = jsonObject?.get("tags") as JSONArray
                val tags = ArrayList<String>()
                if (tagsArray != null && tagsArray.length()>0) {
                    for (tagInd in  0..tagsArray.length()-1) {
                        tags.add(tagsArray.getString(tagInd))
                    }
                }
                val started = jsonObject.getString("started").toBoolean() ?: false
                return EventDescription(tags = tags, started = started)
            } catch(e:Exception) {
                return EventDescription(tags = listOf(), started = false)
            }
        }
        val EMPTY_DESCRIPTION_JSON = "{\"tags\":[], \"started\":false}"
    }

    fun toJson() : String {
        val jsonObject = JSONObject()
        jsonObject.put("tags", JSONArray(tags))
        jsonObject.put("started", started)
        return jsonObject.toString()
    }
}