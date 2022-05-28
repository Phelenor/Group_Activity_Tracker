package com.rafaelboban.groupactivitytracker.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.res.ResourcesCompat
import com.rafaelboban.groupactivitytracker.R


object IconHelper {

    fun getUserBitmap(context: Context, username: String, isCurrentUser: Boolean, needsHelp: Boolean): Bitmap {
        val dimension = DisplayHelper.convertDpToPx(context, 36)
        val radius = DisplayHelper.convertDpToPx(context, 16)
        val bitmap = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val userIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = context.getColor(R.color.icon_purple)
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = DisplayHelper.convertDpToPx(context, 2).toFloat()
            color = context.getColor(R.color.md_theme_light_primary)
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.getColor(R.color.md_theme_light_primary)
            textSize = DisplayHelper.convertSpToPx(context, 16).toFloat()
            typeface = ResourcesCompat.getFont(context, R.font.roboto_black)
            textAlign = Paint.Align.CENTER
        }

        userIconPaint.color = if (needsHelp) {
            context.getColor(R.color.help_red)
        } else if (isCurrentUser) {
            context.getColor(R.color.light_yellow)
        } else {
            context.getColor(R.color.icon_purple)
        }

        val center = dimension / 2f
        val character = username.first().toString().uppercase()
        canvas.drawCircle(center, center, radius.toFloat(), userIconPaint)
        canvas.drawCircle(center, center, radius.toFloat(), strokePaint)
        canvas.drawText(character, center, center - ((textPaint.descent() + textPaint.ascent()) / 2), textPaint)
        return bitmap
    }
}