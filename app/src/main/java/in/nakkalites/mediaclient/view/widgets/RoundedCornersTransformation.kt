package `in`.nakkalites.mediaclient.view.widgets

/**
 * Copyright (C) 2018 Wasabeef
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.graphics.*
import com.squareup.picasso.Transformation


class RoundedCornersTransformation(
    radius: Int, margin: Int, cornerType: CornerType = CornerType.ALL
) : Transformation {

    enum class CornerType {
        ALL,
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
        TOP, BOTTOM, LEFT, RIGHT,
        OTHER_TOP_LEFT, OTHER_TOP_RIGHT, OTHER_BOTTOM_LEFT, OTHER_BOTTOM_RIGHT,
        DIAGONAL_FROM_TOP_LEFT, DIAGONAL_FROM_TOP_RIGHT
    }

    private val mRadius = radius
    private val mDiameter = mRadius * 2
    private val mMargin = margin
    private val mCornerType: CornerType = cornerType

    override fun transform(source: Bitmap): Bitmap? {
        val width = source.width
        val height = source.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        drawRoundRect(canvas, paint, width.toFloat(), height.toFloat())
        source.recycle()
        return bitmap
    }

    private fun drawRoundRect(
        canvas: Canvas, paint: Paint, width: Float, height: Float
    ) {
        val right = width - mMargin
        val bottom = height - mMargin
        when (mCornerType) {
            CornerType.ALL -> canvas.drawRoundRect(
                RectF(mMargin.toFloat(), mMargin.toFloat(), right, bottom),
                mRadius.toFloat(),
                mRadius.toFloat(),
                paint
            )
            CornerType.TOP_LEFT -> drawTopLeftRoundRect(canvas, paint, right, bottom)
            CornerType.TOP_RIGHT -> drawTopRightRoundRect(canvas, paint, right, bottom)
            CornerType.BOTTOM_LEFT -> drawBottomLeftRoundRect(canvas, paint, right, bottom)
            CornerType.BOTTOM_RIGHT -> drawBottomRightRoundRect(canvas, paint, right, bottom)
            CornerType.TOP -> drawTopRoundRect(canvas, paint, right, bottom)
            CornerType.BOTTOM -> drawBottomRoundRect(canvas, paint, right, bottom)
            CornerType.LEFT -> drawLeftRoundRect(canvas, paint, right, bottom)
            CornerType.RIGHT -> drawRightRoundRect(canvas, paint, right, bottom)
            CornerType.OTHER_TOP_LEFT -> drawOtherTopLeftRoundRect(canvas, paint, right, bottom)
            CornerType.OTHER_TOP_RIGHT -> drawOtherTopRightRoundRect(canvas, paint, right, bottom)
            CornerType.OTHER_BOTTOM_LEFT ->
                drawOtherBottomLeftRoundRect(canvas, paint, right, bottom)
            CornerType.OTHER_BOTTOM_RIGHT ->
                drawOtherBottomRightRoundRect(canvas, paint, right, bottom)
            CornerType.DIAGONAL_FROM_TOP_LEFT ->
                drawDiagonalFromTopLeftRoundRect(canvas, paint, right, bottom)
            CornerType.DIAGONAL_FROM_TOP_RIGHT ->
                drawDiagonalFromTopRightRoundRect(canvas, paint, right, bottom)
        }
    }

    private fun drawTopLeftRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(
                mMargin.toFloat(),
                mMargin.toFloat(),
                (mMargin + mDiameter).toFloat(),
                (mMargin + mDiameter).toFloat()
            ),
            mRadius.toFloat(), mRadius.toFloat(), paint
        )
        canvas.drawRect(
            RectF(
                mMargin.toFloat(),
                (mMargin + mRadius).toFloat(),
                (mMargin + mRadius).toFloat(), bottom
            ), paint
        )
        canvas.drawRect(
            RectF((mMargin + mRadius).toFloat(), mMargin.toFloat(), right, bottom),
            paint
        )
    }

    private fun drawTopRightRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(right - mDiameter, mMargin.toFloat(), right, (mMargin + mDiameter).toFloat()),
            mRadius.toFloat(),
            mRadius.toFloat(), paint
        )
        canvas.drawRect(RectF(mMargin.toFloat(), mMargin.toFloat(), right - mRadius, bottom), paint)
        canvas.drawRect(RectF(right - mRadius, (mMargin + mRadius).toFloat(), right, bottom), paint)
    }

    private fun drawBottomLeftRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(mMargin.toFloat(), bottom - mDiameter, (mMargin + mDiameter).toFloat(), bottom),
            mRadius.toFloat(), mRadius.toFloat(), paint
        )
        canvas.drawRect(
            RectF(
                mMargin.toFloat(),
                mMargin.toFloat(),
                (mMargin + mDiameter).toFloat(), bottom - mRadius
            ), paint
        )
        canvas.drawRect(
            RectF((mMargin + mRadius).toFloat(), mMargin.toFloat(), right, bottom),
            paint
        )
    }

    private fun drawBottomRightRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(right - mDiameter, bottom - mDiameter, right, bottom), mRadius.toFloat(),
            mRadius.toFloat(), paint
        )
        canvas.drawRect(RectF(mMargin.toFloat(), mMargin.toFloat(), right - mRadius, bottom), paint)
        canvas.drawRect(RectF(right - mRadius, mMargin.toFloat(), right, bottom - mRadius), paint)
    }

    private fun drawTopRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(mMargin.toFloat(), mMargin.toFloat(), right, (mMargin + mDiameter).toFloat()),
            mRadius.toFloat(),
            mRadius.toFloat(),
            paint
        )
        canvas.drawRect(
            RectF(mMargin.toFloat(), (mMargin + mRadius).toFloat(), right, bottom),
            paint
        )
    }

    private fun drawBottomRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(mMargin.toFloat(), bottom - mDiameter, right, bottom),
            mRadius.toFloat(),
            mRadius.toFloat(),
            paint
        )
        canvas.drawRect(RectF(mMargin.toFloat(), mMargin.toFloat(), right, bottom - mRadius), paint)
    }

    private fun drawLeftRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(mMargin.toFloat(), mMargin.toFloat(), (mMargin + mDiameter).toFloat(), bottom),
            mRadius.toFloat(),
            mRadius.toFloat(),
            paint
        )
        canvas.drawRect(
            RectF((mMargin + mRadius).toFloat(), mMargin.toFloat(), right, bottom),
            paint
        )
    }

    private fun drawRightRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(right - mDiameter, mMargin.toFloat(), right, bottom),
            mRadius.toFloat(),
            mRadius.toFloat(),
            paint
        )
        canvas.drawRect(RectF(mMargin.toFloat(), mMargin.toFloat(), right - mRadius, bottom), paint)
    }

    private fun drawOtherTopLeftRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(mMargin.toFloat(), bottom - mDiameter, right, bottom),
            mRadius.toFloat(),
            mRadius.toFloat(),
            paint
        )
        canvas.drawRoundRect(
            RectF(right - mDiameter, mMargin.toFloat(), right, bottom),
            mRadius.toFloat(),
            mRadius.toFloat(),
            paint
        )
        canvas.drawRect(
            RectF(
                mMargin.toFloat(),
                mMargin.toFloat(),
                right - mRadius,
                bottom - mRadius
            ), paint
        )
    }

    private fun drawOtherTopRightRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(mMargin.toFloat(), mMargin.toFloat(), (mMargin + mDiameter).toFloat(), bottom),
            mRadius.toFloat(),
            mRadius.toFloat(),
            paint
        )
        canvas.drawRoundRect(
            RectF(mMargin.toFloat(), bottom - mDiameter, right, bottom),
            mRadius.toFloat(),
            mRadius.toFloat(),
            paint
        )
        canvas.drawRect(
            RectF(
                (mMargin + mRadius).toFloat(),
                mMargin.toFloat(),
                right,
                bottom - mRadius
            ), paint
        )
    }

    private fun drawOtherBottomLeftRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(mMargin.toFloat(), mMargin.toFloat(), right, (mMargin + mDiameter).toFloat()),
            mRadius.toFloat(),
            mRadius.toFloat(),
            paint
        )
        canvas.drawRoundRect(
            RectF(right - mDiameter, mMargin.toFloat(), right, bottom),
            mRadius.toFloat(),
            mRadius.toFloat(),
            paint
        )
        canvas.drawRect(
            RectF(
                mMargin.toFloat(),
                (mMargin + mRadius).toFloat(), right - mRadius, bottom
            ), paint
        )
    }

    private fun drawOtherBottomRightRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(mMargin.toFloat(), mMargin.toFloat(), right, (mMargin + mDiameter).toFloat()),
            mRadius.toFloat(),
            mRadius.toFloat(),
            paint
        )
        canvas.drawRoundRect(
            RectF(mMargin.toFloat(), mMargin.toFloat(), (mMargin + mDiameter).toFloat(), bottom),
            mRadius.toFloat(),
            mRadius.toFloat(),
            paint
        )
        canvas.drawRect(
            RectF(
                (mMargin + mRadius).toFloat(),
                (mMargin + mRadius).toFloat(), right, bottom
            ), paint
        )
    }

    private fun drawDiagonalFromTopLeftRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(
                mMargin.toFloat(),
                mMargin.toFloat(),
                (mMargin + mDiameter).toFloat(),
                (mMargin + mDiameter).toFloat()
            ),
            mRadius.toFloat(), mRadius.toFloat(), paint
        )
        canvas.drawRoundRect(
            RectF(right - mDiameter, bottom - mDiameter, right, bottom), mRadius.toFloat(),
            mRadius.toFloat(), paint
        )
        canvas.drawRect(
            RectF(
                mMargin.toFloat(),
                (mMargin + mRadius).toFloat(), right - mDiameter, bottom
            ), paint
        )
        canvas.drawRect(
            RectF(
                (mMargin + mDiameter).toFloat(),
                mMargin.toFloat(),
                right,
                bottom - mRadius
            ), paint
        )
    }

    private fun drawDiagonalFromTopRightRoundRect(
        canvas: Canvas, paint: Paint, right: Float, bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(right - mDiameter, mMargin.toFloat(), right, (mMargin + mDiameter).toFloat()),
            mRadius.toFloat(),
            mRadius.toFloat(), paint
        )
        canvas.drawRoundRect(
            RectF(mMargin.toFloat(), bottom - mDiameter, (mMargin + mDiameter).toFloat(), bottom),
            mRadius.toFloat(), mRadius.toFloat(), paint
        )
        canvas.drawRect(
            RectF(
                mMargin.toFloat(),
                mMargin.toFloat(),
                right - mRadius,
                bottom - mRadius
            ), paint
        )
        canvas.drawRect(
            RectF(
                (mMargin + mRadius).toFloat(),
                (mMargin + mRadius).toFloat(), right, bottom
            ), paint
        )
    }

    override fun key(): String {
        return "RoundedTransformation(radius=$mRadius, margin=$mMargin, diameter=$mDiameter , cornerType=  ${mCornerType.name})"
    }
}
