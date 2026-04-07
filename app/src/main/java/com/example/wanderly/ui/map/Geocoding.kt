package com.example.wanderly.ui.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun geocodeAddress(context: Context, addressString: String): Address? =
    withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context)
        try {
            val addresses: MutableList<Address>? = geocoder
                .getFromLocationName(addressString, 1)
            addresses?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): Address? =
    withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context)
        try {
            val addresses: MutableList<Address>? = geocoder
                .getFromLocation(lat, lng, 1)
            addresses?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

fun formatAddress(address: Address?): String {
    return address?.let {
        val addressLines = (0..it.maxAddressLineIndex).mapNotNull {
            i -> it.getAddressLine(i)
        }
        addressLines.joinToString(separator = ", ")
    } ?: "No Address"
}

fun getBestLocationName(address: Address?): String? {
    if (address == null) return null

    val candidates = arrayOf<String?>(
        address.locality,
        address.subAdminArea,
        address.adminArea,
        address.countryName
    )

    for (value in candidates) {
        if (value != null && !value.trim { it <= ' ' }.isEmpty()) {
            return value
        }
    }

    return null
}