package me.corv.pillenalarm.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import me.corv.pillenalarm.R
import me.corv.pillenalarm.databinding.FragmentDoneBinding
import me.corv.pillenalarm.util.ReverseInterpolator
import me.corv.pillenalarm.viewmodel.PillenViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random


class DoneFragment : Fragment() {

    private var _binding: FragmentDoneBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PillenViewModel by activityViewModels()
    private var clicks = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoneBinding.inflate(inflater, container, false)
        viewModel.document.observe(viewLifecycleOwner) { document ->
            Glide.with(this).load(document.catGif).into(binding.imageView)
            val periodEnd = Instant.ofEpochMilli(document.periodEnd)
            if (LocalDateTime.now()
                    .isBefore(LocalDateTime.ofInstant(periodEnd, ZoneId.systemDefault()))
            ) {
                binding.textView.setText(R.string.done_text_period)
            } else {
                binding.textView.setText(R.string.done_text)
            }

            binding.buttonLike.setIconResource(if (viewModel.isCurrentGifLiked()) R.drawable.favorite_filled else R.drawable.favorite)

            binding.buttonLike.setOnClickListener {
                val liked = viewModel.toggleLikeCurrentGif()
                binding.buttonLike.setIconResource(if (liked) R.drawable.favorite_filled else R.drawable.favorite)
            }
        }

        binding.buttonGallery.setOnClickListener {
            findNavController().navigate(R.id.action_doneFragment_to_galleryFragment)
        }
        binding.buttonSettings.setOnClickListener {
            findNavController().navigate(R.id.action_doneFragment_to_settingsFragment)
        }

        binding.mainCard.setOnClickListener {
            clicks++
            val animation = AnimationUtils.loadAnimation(context, R.anim.shake)
            if (Math.random() < .5) animation.interpolator = ReverseInterpolator()
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            it.startAnimation(animation)
            if (clicks > 10 && Math.random() < .3) particle(binding.root)
            if (clicks % 100 == 0) flip(it)
            else if (clicks % 75 == 0) spin(it)
            else if (clicks % 50 == 0) bounce(it)
        }

        return binding.root
    }

    private fun particle(container: ViewGroup?) {
        val random = Random(System.currentTimeMillis())
        val particle = ImageView(context)
        particle.setImageResource(if (Math.random() < .5) R.drawable.favorite_filled else R.drawable.favorite)
        val colorFilter = PorterDuffColorFilter(randomColor(random), PorterDuff.Mode.SRC_IN)
        particle.colorFilter = colorFilter

        val minSize = 6
        val maxSize = 20
        val size = random.nextInt((maxSize + 1) - minSize) + minSize
        particle.scaleX = size / 10f
        particle.scaleY = size / 10f
        container?.addView(particle)

        val height = resources.displayMetrics.heightPixels
        val width = resources.displayMetrics.widthPixels
        val xPadding = 75
        val startX = random.nextInt((width - xPadding) - xPadding) + xPadding
        particle.x = startX.toFloat()

        val fromY: Float = height.toFloat() - random.nextInt(200)
        val toY = 0f + random.nextInt(200)
        particle.y = fromY
        val moveUpAnimation = ObjectAnimator.ofFloat(particle, "y", fromY, toY)
        moveUpAnimation.interpolator = DecelerateInterpolator()
        moveUpAnimation.setDuration(4000)
        moveUpAnimation.doOnEnd {
            container?.removeView(particle)
        }

        val minXShift = 100
        val maxXShift = 200
        val fromX = particle.x - random.nextInt((maxXShift + 1) - minXShift) + minXShift
        val toX = particle.x + random.nextInt((maxXShift + 1) - minXShift) + minXShift
        val shakeAnimation = ObjectAnimator.ofFloat(particle, "x", fromX, toX)
        shakeAnimation.duration = 500
        shakeAnimation.repeatMode = ObjectAnimator.REVERSE
        shakeAnimation.repeatCount = ObjectAnimator.INFINITE

        val fadeOutAnimation = ObjectAnimator.ofFloat(particle, "alpha", 1f, 0f)
        fadeOutAnimation.duration = 1000
        fadeOutAnimation.startDelay = 3000

        val fadeInAnimation = ObjectAnimator.ofFloat(particle, "alpha", 0f, 1f)
        fadeInAnimation.duration = 500

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(moveUpAnimation, shakeAnimation, fadeOutAnimation, fadeInAnimation)
        animatorSet.start()
    }

    private fun randomColor(random: Random): Int {
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }

    private fun spin(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
        animator.duration = 1000
        animator.start()
    }

    private fun bounce(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "translationY", 0f, -200f, 0f)
        animator.interpolator = BounceInterpolator()
        animator.duration = 1000
        animator.start()
    }

    private fun flip(view: View) {
        val animatorSet = AnimatorSet()
        val bounce = ObjectAnimator.ofFloat(view, "translationY", 0f, -400f, 0f)
        bounce.interpolator = AccelerateInterpolator()
        bounce.duration = 1500
        val flip = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
        flip.duration = 500
        flip.startDelay = 500
        animatorSet.playTogether(bounce, flip)
        animatorSet.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}