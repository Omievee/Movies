package com.mobile.history.StarsRating

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.moviepass.R
import kotlinx.android.synthetic.main.stars_layout.view.*
import com.moviepass.R.id.view
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation


class StarsRatingView(context: Context, attributeSet: AttributeSet? = null): ConstraintLayout(context,attributeSet), View.OnClickListener{

    var listener : StarsRatingClickListener? = null

    init {
        View.inflate(context, R.layout.stars_layout,this)
    }

    fun setOnClickListener(listener: StarsRatingClickListener){
        starOne.setOnClickListener(this)
        starTwo.setOnClickListener(this)
        starThree.setOnClickListener(this)
        starFour.setOnClickListener(this)
        starFive.setOnClickListener(this)
        this.listener = listener
    }

    override fun onClick(p0: View?) {
        when(p0){
            starOne ->{
                fillStars(Rating.ONE_STAR)
                listener?.onStarClicked(Rating.ONE_STAR)
            }
            starTwo -> {
                fillStars(Rating.TWO_STAR)
                listener?.onStarClicked(Rating.TWO_STAR)
            }
            starThree -> {
                fillStars(Rating.THREE_STAR)
                listener?.onStarClicked(Rating.THREE_STAR)
            }
            starFour -> {
                fillStars(Rating.FOUR_STAR)
                listener?.onStarClicked(Rating.FOUR_STAR)
            }
            starFive -> {
                fillStars(Rating.FIVE_STAR)
                listener?.onStarClicked(Rating.FIVE_STAR)
            }
        }
        expandAndShrinkAnimation()
    }

    fun fillStars(rating: Rating?){
        val position = rating?.getStarPosition() ?: return
        emptyAllStars()
        val starsArray = arrayListOf<ImageView>(starOne,starTwo,starThree,starFour,starFive)
        for(i in 0..position){
            starsArray[i].setFillStar()
        }
    }

    fun emptyAllStars(){
        arrayListOf<ImageView>(starOne,starTwo,starThree,starFour,starFive).forEach { it.setEmptyStar() }
    }

    private fun View.setFillStar(){
        this.setBackgroundResource(R.drawable.ic_star_filled)
    }

    private fun View.setEmptyStar(){
        this.setBackgroundResource(R.drawable.ic_star_empty)
    }

    private fun expandAndShrinkAnimation(){
        val expandAndShrink = AnimationSet(true)
        val expand = ScaleAnimation(
                1f, 1.1f,
                1f, 1.1f,
                root.width/2.toFloat(),
                root.height/2.toFloat())
        expand.duration = 100

        val shrink = ScaleAnimation(
                1f, 0.9f,
                1f, 0.9f,
                root.width/2.toFloat(),
                root.height/2.toFloat())
        shrink.startOffset = 100
        shrink.duration = 100

        expandAndShrink.addAnimation(expand)
        expandAndShrink.addAnimation(shrink)
        expandAndShrink.fillAfter = true
        expandAndShrink.interpolator = AccelerateInterpolator(1.0f)

        root.startAnimation(expandAndShrink)
    }

}

interface StarsRatingClickListener{
    fun onStarClicked(rating: Rating)
}


enum class Rating(val starRating: String){
    ONE_STAR("OneStar"),
    TWO_STAR("TwoStar"),
    THREE_STAR("ThreeStar"),
    FOUR_STAR("FourStar"),
    FIVE_STAR("FiveStar"),
    UNKNOWN("Unknown");

    fun getStarPosition() : Int?{
        return when(this){
            ONE_STAR -> 0
            TWO_STAR -> 1
            THREE_STAR -> 2
            FOUR_STAR -> 3
            FIVE_STAR -> 4
            else -> null
        }
    }
}