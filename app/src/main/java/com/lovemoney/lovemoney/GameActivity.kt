package com.lovemoney.lovemoney

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.*
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lovemoney.lovemoney.utils.Logger

class GameActivity : AppCompatActivity() {

    private var webView: WebView? = null
    private var progressBar: ProgressBar? = null
    private var loadingContainer: FrameLayout? = null
    private var tutorialButton: Button? = null
    private var floatingHelpButton: FloatingActionButton? = null
    private var isLoading = true
    private var isInTutorial = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    companion object {
        private const val GAME_URL = "https://bloodmoneygame.io/game/bloodmoney/"
        private const val TUTORIAL_URL = "https://lovemoney-game.com"
        private const val PREFS_NAME = "LoveMoneyGamePrefs"
        private const val KEY_GAME_DATA = "game_data"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.lifecycle("GameActivity", "onCreate")

        try {
            // Set landscape orientation first
            Logger.d("Setting landscape orientation")
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            Logger.d("Setting content view")
            setContentView(R.layout.activity_game)

            // Setup fullscreen after setContentView
            Logger.d("Setting up fullscreen mode")
            setupFullscreen()

            // Initialize views
            Logger.d("Initializing views")
            webView = findViewById<WebView>(R.id.gameWebView)
            progressBar = findViewById<ProgressBar>(R.id.progressBar)
            loadingContainer = findViewById<FrameLayout>(R.id.loadingContainer)
            tutorialButton = findViewById<Button>(R.id.tutorialButton)
            floatingHelpButton = findViewById<FloatingActionButton>(R.id.floatingHelpButton)
            Logger.d("Views initialized successfully")

            // Setup tutorial functionality
            Logger.d("Setting up tutorial functionality")
            setupTutorialFeatures()

            // Configure WebView
            Logger.d("Setting up WebView")
            setupWebView()

            // Setup network monitoring
            Logger.d("Setting up network monitor")
            setupNetworkMonitor()

            // Load game
            Logger.d("Starting to load game")
            loadGame()

            Logger.d("GameActivity onCreate completed successfully")
        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error in onCreate", e)
            // If initialization fails, show error and exit
            showErrorDialog("Initialization Failed", "An error occurred during app startup. Please try again.")
        }
    }

    private fun setupFullscreen() {
        Logger.d("Setting up fullscreen mode")
        try {
            window.apply {
                // Hide status bar
                Logger.v("Adding fullscreen flags")
                addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

                // Hide navigation bar
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Logger.v("Using Android R+ window insets controller")
                    try {
                        insetsController?.let { controller ->
                            controller.hide(android.view.WindowInsets.Type.statusBars())
                            controller.hide(android.view.WindowInsets.Type.navigationBars())
                            Logger.v("Window insets controller applied successfully")
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
                        Logger.logException("GameActivity", "Error with window insets controller, falling back", e)
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
                    Logger.v("Using legacy system UI visibility flags")
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
                Logger.v("Setting keep screen on flag")
                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            Logger.d("Fullscreen setup completed successfully")
        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error setting up fullscreen", e)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        Logger.webView("Setting up WebView configuration")
        try {
            val webViewInstance = webView ?: throw IllegalStateException("WebView is not initialized")
            webViewInstance.apply {
                Logger.webView("Configuring WebView settings")
                settings.apply {
                    // Basic settings
                    Logger.v("Enabling JavaScript and DOM storage")
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    @Suppress("DEPRECATION")
                    databaseEnabled = true

                    // Cache strategy
                    Logger.v("Setting cache mode to LOAD_DEFAULT")
                    cacheMode = WebSettings.LOAD_DEFAULT

                    // Performance optimization
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        Logger.v("Enabling hardware acceleration")
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    }

                    // Viewport settings
                    Logger.v("Configuring viewport settings")
                    useWideViewPort = true
                    loadWithOverviewMode = true

                    // Disable zoom
                    Logger.v("Disabling zoom controls")
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false

                    // Media playback
                    Logger.v("Allowing media playback without user gesture")
                    mediaPlaybackRequiresUserGesture = false

                    // Mixed content
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Logger.v("Setting mixed content mode to compatibility")
                        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    }

                    // File access
                    Logger.v("Enabling file access")
                    allowFileAccess = true
                    allowContentAccess = true

                    // User agent
                    val originalUA = userAgentString
                    userAgentString = "$originalUA LoveMoneyApp/1.0"
                    Logger.v("User agent set to: $userAgentString")
                }

                // WebView client
                Logger.webView("Setting WebViewClient")
                webViewClient = GameWebViewClient()

                // Chrome client (handle fullscreen etc.)
                Logger.webView("Setting WebChromeClient")
                webChromeClient = GameWebChromeClient()

                // JS interface
                Logger.webView("Adding JavaScript interface")
                addJavascriptInterface(GameJSInterface(this@GameActivity), "LoveMoneyNative")

                // Disable long press menu
                Logger.v("Disabling long click menu")
                setOnLongClickListener { true }
                isLongClickable = false
                isHapticFeedbackEnabled = false
            }
            Logger.webView("WebView setup completed successfully")
        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error setting up WebView", e)
            throw e // Rethrow as WebView configuration failure is serious
        }
    }

    private fun loadGame() {
        Logger.d("Starting to load game")
        try {
            val webViewInstance = webView ?: run {
                Logger.e("WebView is not initialized, cannot load game")
                return
            }

            if (isNetworkAvailable()) {
                Logger.d("Network available, loading game URL: $GAME_URL")
                webViewInstance.loadUrl(GAME_URL)
                Logger.d("Game URL load initiated")
            } else {
                Logger.w("No network connection available")
                showNoInternetDialog()
            }
        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error loading game", e)
            showErrorPage()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        Logger.network("Checking network availability")
        try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val isAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Logger.network("Using Android M+ network capabilities check")
                val network = connectivityManager.activeNetwork
                if (network == null) {
                    Logger.network("No active network found")
                    false
                } else {
                    val capabilities = connectivityManager.getNetworkCapabilities(network)
                    if (capabilities == null) {
                        Logger.network("No network capabilities found")
                        false
                    } else {
                        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        Logger.network("Network has internet capability: $hasInternet")
                        hasInternet
                    }
                }
            } else {
                Logger.network("Using legacy network info check")
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                val connected = networkInfo?.isConnected == true
                Logger.network("Network connected (legacy): $connected")
                connected
            }

            Logger.network("Network availability result: $isAvailable")
            return isAvailable
        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error checking network availability", e)
            return false
        }
    }

    private fun setupNetworkMonitor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    runOnUiThread {
                        webView?.let { webViewInstance ->
                            if (webViewInstance.url == null || webViewInstance.url == "about:blank") {
                                loadGame()
                            }
                        } ?: loadGame()
                    }
                }

                override fun onLost(network: Network) {
                    runOnUiThread {
                        showNoInternetDialog()
                    }
                }
            }

            connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
        }
    }

    private fun showNoInternetDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Network Connection")
            .setMessage("Please check your network connection and try again")
            .setPositiveButton("Retry") { _, _ ->
                if (isNetworkAvailable()) {
                    loadGame()
                } else {
                    showNoInternetDialog()
                }
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    inner class GameWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Logger.d("Page started loading: $url")
            isLoading = true

            // Tutorial is no longer loaded in WebView
            isInTutorial = false

            // Show loading for game load
            loadingContainer?.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            // Inject optimization script
            view?.evaluateJavascript("""
                (function() {
                    // Disable text selection
                    document.documentElement.style.userSelect = 'none';
                    document.documentElement.style.webkitUserSelect = 'none';
                    document.documentElement.style.webkitTouchCallout = 'none';

                    // Disable long press menu
                    document.addEventListener('contextmenu', function(e) {
                        e.preventDefault();
                        return false;
                    });

                    // Optimize touch response
                    document.addEventListener('touchstart', function() {}, {passive: true});

                    // Listen for game events
                    window.addEventListener('message', function(e) {
                        if (e.data && e.data.type) {
                            LoveMoneyNative.handleGameEvent(JSON.stringify(e.data));
                        }
                    });

                    // Fullscreen API support
                    if (!document.fullscreenEnabled) {
                        document.fullscreenEnabled = true;
                        document.documentElement.requestFullscreen = function() {
                            LoveMoneyNative.requestFullscreen();
                        };
                    }
                })();
            """.trimIndent(), null)

            // Handle page-specific UI updates
            val currentUrl = url ?: ""
            Logger.d("Page finished loading: $currentUrl")

            // Tutorial is no longer loaded in WebView
            isInTutorial = false

            // Delay hiding loading screen to ensure page is fully rendered
            Handler(Looper.getMainLooper()).postDelayed({
                isLoading = false
                loadingContainer?.visibility = View.GONE

                // Show floating help button after game loads
                showFloatingHelpButton()
            }, 500)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            Logger.d("URL loading requested: $url")

            // Check if it's the tutorial URL - open in external browser
            if (url.contains("lovemoney-game.com")) {
                Logger.d("Tutorial link clicked, opening in external browser")
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    startActivity(browserIntent)
                } catch (e: ActivityNotFoundException) {
                    Logger.e("No browser found to open URL")
                    Toast.makeText(this@GameActivity, "No browser found", Toast.LENGTH_SHORT).show()
                }
                return true // Prevent loading in WebView
            }

            // Allow only game domain
            val isAllowedDomain = url.contains("bloodmoneygame.io") || url.startsWith("about:")

            if (!isAllowedDomain) {
                Logger.d("Blocking external URL: $url")
            }

            return !isAllowedDomain
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (request?.isForMainFrame == true) {
                    showErrorPage()
                }
            }
        }

        @Suppress("DEPRECATION")
        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            showErrorPage()
        }
    }

    inner class GameWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressBar?.progress = newProgress

            progressBar?.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            // For debugging
            return super.onConsoleMessage(consoleMessage)
        }
    }

    private fun showErrorPage() {
        webView?.loadData("""
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        font-family: sans-serif;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        height: 100vh;
                        margin: 0;
                        text-align: center;
                    }
                    .error-container {
                        padding: 20px;
                    }
                    h1 {
                        font-size: 2em;
                        margin-bottom: 10px;
                    }
                    p {
                        font-size: 1.2em;
                        opacity: 0.9;
                    }
                    button {
                        background: white;
                        color: #667eea;
                        border: none;
                        padding: 12px 30px;
                        font-size: 1.1em;
                        border-radius: 25px;
                        margin-top: 20px;
                        cursor: pointer;
                    }
                </style>
            </head>
            <body>
                <div class="error-container">
                    <h1>Loading Failed</h1>
                    <p>Game loading encountered an issue</p>
                    <button onclick="location.reload()">Reload</button>
                </div>
            </body>
            </html>
        """.trimIndent(), "text/html", "UTF-8") ?: Logger.e("Cannot show error page, WebView is not initialized")
    }

    @Suppress("DEPRECATION", "MissingSuperCall")
    override fun onBackPressed() {
        webView?.let { webViewInstance ->
            if (webViewInstance.canGoBack()) {
                webViewInstance.goBack()
            } else {
                showGameMenu()
            }
        } ?: showGameMenu()
    }

    private fun showGameMenu() {
        Logger.d("Showing game menu")
        try {
            val menuItems = arrayOf("Tutorial & Help", "Exit")

            AlertDialog.Builder(this)
                .setTitle("Game Menu")
                .setItems(menuItems) { _, which ->
                    when (which) {
                        0 -> loadTutorial()
                        1 -> showExitConfirmation()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error showing game menu", e)
            showExitConfirmation()
        }
    }

    private fun showExitConfirmation() {
        try {
            AlertDialog.Builder(this)
                .setTitle("Exit Game")
                .setMessage("Are you sure you want to exit the game?")
                .setPositiveButton("Exit") { _, _ ->
                    @Suppress("DEPRECATION")
                    super.onBackPressed()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error showing exit confirmation", e)
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    private fun showErrorDialog(title: String, message: String) {
        Logger.d("Showing error dialog: $title - $message")
        try {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Retry") { _, _ ->
                    Logger.d("User clicked retry in error dialog")
                    recreate()
                }
                .setNegativeButton("Exit") { _, _ ->
                    Logger.d("User clicked exit in error dialog")
                    finish()
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error showing error dialog", e)
            // If dialog display fails, exit directly
            finish()
        }
    }

    // Tutorial functionality methods
    private fun setupTutorialFeatures() {
        Logger.d("Setting up tutorial features")
        try {
            // Tutorial button click listener
            tutorialButton?.setOnClickListener {
                Logger.d("Tutorial button clicked")
                loadTutorial()
            }

            // Floating help button click listener
            floatingHelpButton?.setOnClickListener {
                Logger.d("Floating help button clicked")
                // Always open tutorial in external browser now
                loadTutorial()
            }

            Logger.d("Tutorial features setup completed")
        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error setting up tutorial features", e)
        }
    }

    private fun loadTutorial() {
        Logger.d("Opening tutorial in external browser: $TUTORIAL_URL")
        try {
            // Create intent to open URL in external browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(TUTORIAL_URL))

            // Add flags to open in a new task
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Try to start the browser activity
            try {
                startActivity(browserIntent)
                Logger.d("Tutorial opened in external browser successfully")

                // Show a toast to inform user
                Toast.makeText(this, "Opening tutorial in browser...", Toast.LENGTH_SHORT).show()
            } catch (e: ActivityNotFoundException) {
                Logger.e("No browser found to open tutorial URL")
                // If no browser is available, show an error dialog
                AlertDialog.Builder(this)
                    .setTitle("Browser Not Found")
                    .setMessage("No web browser found to open the tutorial. Please install a browser and try again.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error opening tutorial in browser", e)
            Toast.makeText(this, "Failed to open tutorial", Toast.LENGTH_SHORT).show()
        }
    }

    private fun returnToGame() {
        Logger.d("Returning to game: $GAME_URL")
        try {
            webView?.let { webViewInstance ->
                isInTutorial = false
                // Check if we're already on the game URL
                if (webViewInstance.url?.contains("bloodmoneygame.io") != true) {
                    webViewInstance.loadUrl(GAME_URL)
                }

                // Update floating button back to help icon
                floatingHelpButton?.let { fab ->
                    fab.setImageResource(R.drawable.ic_help)
                }

                Logger.d("Returned to game successfully")
            } ?: Logger.e("Cannot return to game, WebView is not initialized")
        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error returning to game", e)
        }
    }

    private fun showFloatingHelpButton() {
        floatingHelpButton?.let { fab ->
            fab.visibility = View.VISIBLE
            fab.animate()
                .alpha(0.8f)
                .setDuration(300)
                .start()
        }
    }

    private fun hideFloatingHelpButton() {
        floatingHelpButton?.let { fab ->
            fab.animate()
                .alpha(0.3f)
                .setDuration(300)
                .start()
        }
    }

    override fun onStart() {
        super.onStart()
        Logger.lifecycle("GameActivity", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Logger.lifecycle("GameActivity", "onResume")
        try {
            webView?.let {
                it.onResume()
                it.resumeTimers()
                Logger.d("WebView resumed")
            } ?: Logger.w("WebView is not initialized in onResume")
        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error resuming WebView", e)
        }
    }

    override fun onPause() {
        super.onPause()
        Logger.lifecycle("GameActivity", "onPause")
        try {
            webView?.let {
                it.onPause()
                it.pauseTimers()
                Logger.d("WebView paused")
            } ?: Logger.w("WebView is not initialized in onPause")
        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error pausing WebView", e)
        }
    }

    override fun onStop() {
        super.onStop()
        Logger.lifecycle("GameActivity", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.lifecycle("GameActivity", "onDestroy")

        try {
            // Clean up network monitoring
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                networkCallback?.let {
                    Logger.d("Unregistering network callback")
                    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    connectivityManager.unregisterNetworkCallback(it)
                    Logger.d("Network callback unregistered")
                }
            }

            // Clean up WebView
            webView?.let { webViewInstance ->
                Logger.d("Destroying WebView")
                webViewInstance.apply {
                    loadUrl("about:blank")
                    stopLoading()
                    settings.javaScriptEnabled = false
                    clearHistory()
                    removeAllViews()
                    destroy()
                }
                webView = null
                Logger.d("WebView destroyed successfully")
            } ?: Logger.d("WebView was not initialized, skipping destroy")

        } catch (e: Exception) {
            Logger.logException("GameActivity", "Error in onDestroy", e)
        }
    }
}