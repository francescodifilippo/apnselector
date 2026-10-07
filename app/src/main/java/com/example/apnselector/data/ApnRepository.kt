package com.example.apnselector.data

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build

object ApnRepository {

    private val URI_APN_LIST_LEGACY: Uri = Uri.parse("content://telephony/carriers")
    private val URI_APN_LIST_SAFE: Uri = Uri.parse("content://telephony/carriers/sim_apn_list")
    private val URI_PREFERRED_APN: Uri = Uri.parse("content://telephony/carriers/preferapn")

    fun getApnList(context: Context): List<ApnInfo> {
        val preferredId = getPreferredApnId(context)
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                queryApns(context, URI_APN_LIST_SAFE, null, null, null, preferredId)
            } else {
                queryApns(context, URI_APN_LIST_LEGACY,
                    arrayOf("_id", "name", "apn", "type"),
                    "current IS NOT NULL", "name ASC", preferredId)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun getPreferredApnId(context: Context): Int {
        return try {
            context.contentResolver.query(
                URI_PREFERRED_APN, arrayOf("_id"), null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getIntSafe("_id", default = -1) else -1
            } ?: -1
        } catch (_: Exception) {
            -1
        }
    }

    private fun queryApns(
        context: Context,
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        sortOrder: String?,
        preferredId: Int
    ): List<ApnInfo> {
        val apns = mutableListOf<ApnInfo>()
        context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getIntSafe("_id")
                apns.add(ApnInfo(
                    id = id,
                    name = cursor.getStringSafe("name"),
                    apn = cursor.getStringSafe("apn"),
                    type = cursor.getStringSafe("type"),
                    isActive = id == preferredId
                ))
            }
        }
        return apns
    }

    private fun Cursor.getIntSafe(column: String, default: Int = 0): Int {
        val idx = getColumnIndex(column)
        return if (idx >= 0) getInt(idx) else default
    }

    private fun Cursor.getStringSafe(column: String, default: String = ""): String {
        val idx = getColumnIndex(column)
        return if (idx >= 0) getString(idx) ?: default else default
    }
}
