package org.com.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Official multi-colored Google "G" logo vector.
 */
val GoogleIcon: ImageVector
    get() {
        if (_googleIcon != null) return _googleIcon!!
        _googleIcon = ImageVector.Builder(
            name = "GoogleIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Blue right bar & horizontal stem
            path(
                fill = SolidColor(Color(0xFF4285F4)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(23.745f, 12.27f)
                curveTo(23.745f, 11.48f, 23.675f, 10.73f, 23.545f, 10.01f)
                horizontalLineTo(12f)
                verticalLineToRelative(4.51f)
                horizontalLineToRelative(6.6f)
                curveToRelative(-0.29f, 1.52f, -1.14f, 2.8f, -2.4f, 3.65f)
                verticalLineToRelative(3.03f)
                horizontalLineToRelative(3.88f)
                curveToRelative(2.27f, -2.09f, 3.66f, -5.17f, 3.66f, -8.93f)
                close()
            }
            // Green bottom arc
            path(
                fill = SolidColor(Color(0xFF34A853)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 24f)
                curveToRelative(3.24f, 0f, 5.95f, -1.08f, 7.93f, -2.91f)
                lineToRelative(-3.88f, -3.03f)
                curveToRelative(-1.08f, 0.72f, -2.45f, 1.16f, -4.05f, 1.16f)
                curveToRelative(-3.12f, 0f, -5.77f, -2.1f, -6.72f, -4.93f)
                horizontalLineTo(1.25f)
                verticalLineToRelative(3.13f)
                curveTo(3.26f, 21.36f, 7.33f, 24f, 12f, 24f)
                close()
            }
            // Yellow left arc
            path(
                fill = SolidColor(Color(0xFFFBBC05)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(5.28f, 14.29f)
                curveToRelative(-0.25f, -0.72f, -0.38f, -1.49f, -0.38f, -2.29f)
                reflectiveCurveToRelative(0.14f, -1.57f, 0.38f, -2.29f)
                verticalLineTo(6.57f)
                horizontalLineTo(1.25f)
                curveTo(0.45f, 8.16f, 0f, 9.99f, 0f, 12f)
                reflectiveCurveToRelative(0.45f, 3.84f, 1.25f, 5.43f)
                lineToRelative(4.03f, -3.14f)
                close()
            }
            // Red top arc
            path(
                fill = SolidColor(Color(0xFFEA4335)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 4.75f)
                curveToRelative(1.77f, 0f, 3.35f, 0.61f, 4.6f, 1.8f)
                lineToRelative(3.42f, -3.42f)
                curveTo(17.95f, 1.19f, 15.24f, 0f, 12f, 0f)
                curveTo(7.33f, 0f, 3.26f, 2.64f, 1.25f, 6.57f)
                lineToRelative(4.03f, 3.14f)
                curveToRelative(0.95f, -2.83f, 3.6f, -4.96f, 6.72f, -4.96f)
                close()
            }
        }.build()
        return _googleIcon!!
    }

private var _googleIcon: ImageVector? = null

@Composable
fun GoogleLogo(modifier: Modifier = Modifier.size(20.dp)) {
    Image(
        imageVector = GoogleIcon,
        contentDescription = "Google Logo",
        modifier = modifier
    )
}
