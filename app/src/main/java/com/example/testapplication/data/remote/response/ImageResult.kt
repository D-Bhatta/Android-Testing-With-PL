// ImageResult.kt
package com.example.testapplication.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Representation of a image in the Pixabay API response.
 *
 * Images are full width and height, or large with a
 * maximum width or height of 1280px, or preview
 * with a maximum width or height of 150px, or
 * profile picture with dimensions of 250px, or
 * web format with a variable maximum width and
 * height that can be requested for 24 hours.
 *
 * @property[comments] Number of comments.
 * @property[downloads] Number of downloads.
 * @property[likes] Number of likes.
 * @property[views] Number of views.
 * @property[id] UID for the image.
 * @property[tags] Comma separated string of tags for this image.
 * @property[type] Type of image.
 * @property[imageHeight] Height of the image.
 * @property[imageSize] Size of time image.
 * @property[imageWidth] Width of the image.
 * @property[imageURL] URL for the image.
 * @property[largeImageURL] URL for the large version of the image.
 * @property[pageUrl] Source page on Pixabay.
 * @property[previewHeight] Height of the preview image.
 * @property[previewWidth] Width of the preview image.
 * @property[previewURL] URL for the preview image.
 * @property[user] Name of the image contributor on Pixabay.
 * @property[userId] User ID of the image contributor on Pixabay.
 * @property[userImageURL] URL for the profile picture (250px) image.
 * @property[webFormatHeight] Height of the web format image.
 * @property[webFormatWidth] Width of the web format image.
 * @property[webformatURL] URL for the web format image.
 */
@Suppress("SpellCheckingInspection")
data class ImageResult(
    val comments: Int,
    val downloads: Int,
    val likes: Int,
    val views: Int,
    val id: Int,
    val tags: String,
    val type: String,
    val imageHeight: Int,
    val imageSize: Int,
    val imageWidth: Int,
    val imageURL: String,
    val largeImageURL: String,
    val pageUrl: String,
    val previewHeight: Int,
    val previewWidth: Int,
    val previewURL: String,
    val user: String,
    @Suppress("HardCodedStringLiteral") @SerializedName("user_id")
    val userId: Int,
    val userImageURL: String,
    val webFormatHeight: Int,
    val webFormatWidth: Int,
    val webformatURL: String
)