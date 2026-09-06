package `in`.hridayan.settingsgraph.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A radio button option within a radio group item.
 */
class RadioButtonOption internal constructor(
    val value: Int,
    @StringRes val labelResId: Int? = null,
    val labelString: String? = null,
) {
    constructor(value: Int, label: String) : this(value, labelResId = null, labelString = label)
    constructor(value: Int, @StringRes labelResId: Int) : this(
        value,
        labelResId = labelResId,
        labelString = null
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RadioButtonOption
        if (value != other.value) return false
        if (labelResId != other.labelResId) return false
        if (labelString != other.labelString) return false
        return true
    }

    override fun hashCode(): Int {
        var result = value
        result = 31 * result + (labelResId ?: 0)
        result = 31 * result + (labelString?.hashCode() ?: 0)
        return result
    }
}

/**
 * A segmented button option within a button group item.
 */
class ButtonGroupOption internal constructor(
    val value: Int,
    @StringRes val labelResId: Int? = null,
    @DrawableRes val iconResId: Int? = null,
    val labelString: String? = null,
    val iconVector: ImageVector? = null,
) {
    constructor(value: Int, label: String, @DrawableRes iconResId: Int? = null) : this(
        value,
        labelResId = null,
        iconResId = iconResId,
        labelString = label
    )

    constructor(value: Int, @StringRes labelResId: Int, @DrawableRes iconResId: Int? = null) : this(
        value,
        labelResId = labelResId,
        iconResId = iconResId,
        labelString = null
    )

    constructor(value: Int, label: String, iconVector: ImageVector) : this(
        value,
        labelResId = null,
        iconResId = null,
        labelString = label,
        iconVector = iconVector
    )

    constructor(value: Int, @StringRes labelResId: Int, iconVector: ImageVector) : this(
        value,
        labelResId = labelResId,
        iconResId = null,
        labelString = null,
        iconVector = iconVector
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ButtonGroupOption
        if (value != other.value) return false
        if (labelResId != other.labelResId) return false
        if (iconResId != other.iconResId) return false
        if (labelString != other.labelString) return false
        if (iconVector != other.iconVector) return false
        return true
    }

    override fun hashCode(): Int {
        var result = value
        result = 31 * result + (labelResId ?: 0)
        result = 31 * result + (iconResId ?: 0)
        result = 31 * result + (labelString?.hashCode() ?: 0)
        result = 31 * result + (iconVector?.hashCode() ?: 0)
        return result
    }
}

