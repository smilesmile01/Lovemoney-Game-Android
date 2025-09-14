package com.lovemoney.lovemoney

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.webkit.WebView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lovemoney.lovemoney.utils.CrashHandler
import com.lovemoney.lovemoney.utils.Logger

class SplashActivity : AppCompatActivity() {

    private lateinit var logoImage: ImageView
    private lateinit var gameTitle: TextView
    private lateinit var loadingText: TextView
    private lateinit var progressBar: ProgressBar

    companion object {
        private const val SPLASH_DURATION = 2500L
        private var preloadedWebView: WebView? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.lifecycle("SplashActivity", "onCreate")

        try {
            // Initialize crash handler
            CrashHandler.getInstance().init(applicationContext)
            Logger.logAppStart()

            // Set landscape orientation first
            Logger.d("Setting landscape orientation")
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            Logger.d("Setting content view")
            setContentView(R.layout.activity_splash)

            // Setup fullscreen after setContentView
            Logger.d("Setting up fullscreen mode")
            setupFullscreen()

            // Initialize views
            Logger.d("Initializing views")
            initViews()

            // Start animations
            Logger.d("Starting animations")
            startAnimations()

            // Preload WebView
            Logger.d("Preloading WebView")
            preloadWebView()

            // Delay transition to game screen
            Logger.d("Scheduling transition to GameActivity in ${SPLASH_DURATION}ms")
            Handler(Looper.getMainLooper()).postDelayed({
                startGameActivity()
            }, SPLASH_DURATION)

        } catch (e: Exception) {
            Logger.logException("SplashActivity", "Error in onCreate", e)
            // If startup fails, jump directly to game screen
            startGameActivity()
        }
    }

    private fun setupFullscreen() {
        Logger.d("Setting up fullscreen mode")
        try {
            window.apply {
                // Hide status bar
                Logger.d("Adding fullscreen flags")
                addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

                // Hide navigation bar
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Logger.d("Using Android R+ window insets controller")
                    try {
                        insetsController?.let { controller ->
                            controller.hide(android.view.WindowInsets.Type.statusBars())
                            controller.hide(android.view.WindowInsets.Type.navigationBars())
                            Logger.d("Window insets controller applied successfully")
                        } ?: run {
                            Logger.w("Window insets controller is null, falling back to legacy method")
                            // Fallback to legacy method
                            @Suppress("DEPRECATION")
                            decorView.systemUiVisibility = (
                                View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            )
                        }
                    } catch (e: Exception) {
                        Logger.logException("SplashActivity", "Error with window insets controller, falling back", e)
                        // Fallback to legacy method
                        @Suppress("DEPRECATION")
                        decorView.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        )
                    }
                } else {
                    Logger.d("Using legacy system UI visibility flags")
                    @Suppress("DEPRECATION")
                    decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
                }

                // Keep screen on
                Logger.d("Setting keep screen on flag")
                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            Logger.d("Fullscreen setup completed successfully")
        } catch (e: Exception) {
            Logger.logException("SplashActivity", "Error setting up fullscreen", e)
        }
    }

    private fun initViews() {
        Logger.d("Initializing UI views")
        try {
            logoImage = findViewById(R.id.logoImage)
            Logger.v("logoImage initialized")

            gameTitle = findViewById(R.id.gameTitle)
            Logger.v("gameTitle initialized")

            loadingText = findViewById(R.id.loadingText)
            Logger.v("loadingText initialized")

            progressBar = findViewById(R.id.progressBar)
            Logger.v("progressBar initialized")

            Logger.d("All views initialized successfully")
        } catch (e: Exception) {
            Logger.logException("SplashActivity", "Error initializing views", e)
            throw e // Rethrow exception as view initialization failure should cause crash
        }
    }

    private fun startAnimations() {
        Logger.d("Starting splash animations")
        try {
            // Logo animation
            Logger.v("Creating logo scale and alpha animations")
            val scaleAnimation = ScaleAnimation(
                0.5f, 1.0f,
                0.5f, 1.0f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 800
                fillAfter = true
            }

            val alphaAnimation = AlphaAnimation(0.0f, 1.0f).apply {
                duration = 800
                fillAfter = true
            }

            val animationSet = AnimationSet(true).apply {
                addAnimation(scaleAnimation)
                addAnimation(alphaAnimation)
            }

            Logger.v("Starting logo animation")
            logoImage.startAnimation(animationSet)

            // Title animation
            Logger.v("Starting title animation")
            gameTitle.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setDuration(1000)
                .setStartDelay(300)
                .start()

            // Loading text animation
            Logger.v("Starting loading text animation")
            loadingText.animate()
                .alpha(1.0f)
                .setDuration(500)
                .setStartDelay(800)
                .start()

            // Progress bar animation
            Logger.v("Starting progress bar animation")
            progressBar.animate()
                .alpha(1.0f)
                .setDuration(500)
                .setStartDelay(800)
                .start()

            // Simulate loading progress
            Logger.d("Starting loading progress simulation")
            simulateLoadingProgress()

            Logger.d("All animations started successfully")
        } catch (e: Exception) {
            Logger.logException("SplashActivity", "Error starting animations", e)
        }
    }

    private fun simulateLoadingProgress() {
        Logger.v("Simulating loading progress")
        try {
            val handler = Handler(Looper.getMainLooper())
            var progress = 0

            val runnable = object : Runnable {
                override fun run() {
                    progress += 10
                    progressBar.progress = progress
                    Logger.v("Progress updated to $progress%")

                    if (progress < 100) {
                        handler.postDelayed(this, 200)
                    } else {
                        Logger.d("Progress simulation completed")
                    }
                }
            }

            handler.postDelayed(runnable, 500)
        } catch (e: Exception) {
            Logger.logException("SplashActivity", "Error simulating progress", e)
        }
    }

    private fun preloadWebView() {
        Logger.performance("Starting WebView preload in background thread")
        // Preload WebView in background to speed up game loading
        Thread {
            try {
                Logger.performance("Preload thread started")
                if (preloadedWebView == null) {
                    Logger.performance("Creating new WebView instance")
                    preloadedWebView = WebView(applicationContext).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        Logger.performance("WebView settings configured")

                        // Preload blank page to initialize WebView engine
                        loadUrl("about:blank")
                        Logger.performance("WebView blank page loaded")
                    }
                    Logger.performance("WebView preload completed successfully")
                } else {
                    Logger.performance("WebView already preloaded, skipping")
                }
            } catch (e: Exception) {
                Logger.logException("SplashActivity", "Error preloading WebView", e)
            }
        }.start()
    }

    private fun startGameActivity() {
        Logger.d("Starting GameActivity transition")
        try {
            val intent = Intent(this, GameActivity::class.java)
            Logger.d("GameActivity intent created")

            startActivity(intent)
            Logger.d("GameActivity started")

            // Add transition animation
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Logger.d("Transition animation applied")

            // Finish splash screen
            finish()
            Logger.d("SplashActivity finished")

        } catch (e: Exception) {
            Logger.logException("SplashActivity", "Error starting GameActivity", e)
            // If startup fails, still need to finish splash screen
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        Logger.lifecycle("SplashActivity", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Logger.lifecycle("SplashActivity", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Logger.lifecycle("SplashActivity", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Logger.lifecycle("SplashActivity", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.lifecycle("SplashActivity", "onDestroy")

        try {
            // Clean up preloaded WebView
            preloadedWebView?.let {
                Logger.performance("Destroying preloaded WebView")
                it.destroy()
                preloadedWebView = null
                Logger.performance("Preloaded WebView destroyed")
            }
        } catch (e: Exception) {
            Logger.logException("SplashActivity", "Error destroying preloaded WebView", e)
        }
    }
}