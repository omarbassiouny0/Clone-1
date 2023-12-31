package mega.privacy.android.data.mapper.photos

import mega.privacy.android.data.extensions.decodeBase64
import mega.privacy.android.data.extensions.encodeBase64
import mega.privacy.android.data.extensions.getValueFor
import mega.privacy.android.domain.entity.photos.TimelinePreferencesJSON
import nz.mega.sdk.MegaStringMap
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

internal class ContentConsumptionMegaStringMapMapper @Inject constructor() {
    operator fun invoke(
        currentPreferences: MegaStringMap?,
        newPreferences: Map<String, String>,
    ): MegaStringMap {
        val latestPreferences = currentPreferences?.getValueFor(
            TimelinePreferencesJSON.JSON_KEY_CONTENT_CONSUMPTION.value
        )?.decodeBase64()?.let {
            JSONObject(it)
        } ?: JSONObject()

        val asJSON = JSONObject(newPreferences)
        val androidPreferences = try {
            latestPreferences.getJSONObject(TimelinePreferencesJSON.JSON_KEY_ANDROID.value)
        } catch (exception: JSONException) {
            JSONObject()
        }

        androidPreferences.put(TimelinePreferencesJSON.JSON_KEY_TIMELINE.value, asJSON)
        latestPreferences.put(TimelinePreferencesJSON.JSON_KEY_ANDROID.value, androidPreferences)

        val preferences = currentPreferences ?: MegaStringMap.createInstance()
        preferences[TimelinePreferencesJSON.JSON_KEY_CONTENT_CONSUMPTION.value] =
            latestPreferences.toString().encodeBase64()
        return preferences
    }
}