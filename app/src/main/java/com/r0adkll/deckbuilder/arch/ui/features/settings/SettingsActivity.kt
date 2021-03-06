package com.r0adkll.deckbuilder.arch.ui.features.settings


import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ShareCompat
import androidx.core.content.IntentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.ftinc.kit.kotlin.extensions.clear
import com.ftinc.kit.util.IntentUtils
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.r0adkll.deckbuilder.BuildConfig
import com.r0adkll.deckbuilder.DeckApp
import com.r0adkll.deckbuilder.R
import com.r0adkll.deckbuilder.arch.data.AppPreferences
import com.r0adkll.deckbuilder.arch.domain.features.account.AccountRepository
import com.r0adkll.deckbuilder.arch.ui.Shortcuts
import com.r0adkll.deckbuilder.arch.ui.components.BaseActivity
import com.r0adkll.deckbuilder.arch.ui.components.BasePreferenceFragment
import com.r0adkll.deckbuilder.arch.ui.features.settings.offline.ManageOfflineActivity
import com.r0adkll.deckbuilder.arch.ui.features.setup.SetupActivity
import com.r0adkll.deckbuilder.internal.analytics.Analytics
import com.r0adkll.deckbuilder.internal.analytics.Event
import com.r0adkll.deckbuilder.internal.di.AppComponent
import com.r0adkll.deckbuilder.util.extensions.plusAssign
import com.r0adkll.deckbuilder.util.extensions.snackbar
import com.r0adkll.deckbuilder.util.extensions.toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_setup.*
import timber.log.Timber
import javax.inject.Inject


class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        appbar?.setNavigationOnClickListener { finish() }
    }


    override fun setupComponent(component: AppComponent) {
    }



    class SettingsFragment : BasePreferenceFragment(), GoogleApiClient.OnConnectionFailedListener {

        private var googleClient: GoogleApiClient? = null

        @Inject lateinit var preferences: AppPreferences
        @Inject lateinit var accountRepository: AccountRepository

        private val disposables = CompositeDisposable()
        private var migrationSnackbar: Snackbar? = null


        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            setupClient()
        }


        override fun onDestroy() {
            super.onDestroy()
            disposables.clear()
            migrationSnackbar?.dismiss()
            migrationSnackbar = null
        }


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == RC_SIGN_IN) {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                handleSignInResult(result)
            }
        }


        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            DeckApp.component.inject(this)
            addPreferencesFromResource(R.xml.settings_preferences)
            setupPreferences()
        }


        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            return when(preference.key) {
                "pref_account_profile" -> {
                    true
                }
                "pref_about_privacy_policy" -> {
                    Analytics.event(Event.SelectContent.Action("settings", "privacy_policy"))
                    startActivity(IntentUtils.openLink(getString(R.string.privacy_policy_url)))
                    true
                }
                "pref_about_developer" -> {
                    Analytics.event(Event.SelectContent.Action("settings", "developer"))
                    startActivity(IntentUtils.openLink(getString(R.string.developer_website_url)))
                    true
                }
                "pref_about_opensource" -> {
                    Analytics.event(Event.SelectContent.Action("settings", "contribute"))
                    startActivity(IntentUtils.openLink(getString(R.string.opensource_repository_url)))
                    true
                }
                "pref_about_oss" -> {
                    Analytics.event(Event.SelectContent.Action("settings", "oss"))
                    val intent = Intent(activity, OssLicensesMenuActivity::class.java)
                    val title = getString(R.string.activity_licenses)
                    intent.putExtra("title", title)
                    startActivity(intent)
                    true
                }
                "pref_help_feedback" -> {
                    Analytics.event(Event.SelectContent.Action("settings", "feedback"))
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val text =
                            "Version: ${BuildConfig.VERSION_NAME} \n" +
                            "UserId: $userId \n\n\n"
                    val emailAddress = getString(R.string.support_email_address)
                    val intent = IntentUtils.sendEmail(emailAddress, "Feedback", text)
                    startActivity(intent)
                    true
                }
                "pref_reset_quickstart" -> {
                    Analytics.event(Event.SelectContent.Action("settings", "reset_quickstart"))
                    preferences.quickStart.set(true)
                    true
                }
                "pref_reset_offline_outline" -> {
                    Analytics.event(Event.SelectContent.Action("settings", "reset_offline_outline"))
                    preferences.offlineOutline.set(true)
                    true
                }
                "pref_account_link" -> {
                    Analytics.event(Event.SelectContent.Action("settings", "link_account"))
                    signIn()
                    true
                }
                "pref_cache_manage" -> {
                    Analytics.event(Event.SelectContent.Action("settings", "manage_cache"))
                    startActivity(ManageOfflineActivity.createIntent(activity!!))
                    true
                }
                "pref_developer_reset_preview" -> {
                    preferences.previewVersion.delete()
                    true
                }
                "pref_developer_user_id" -> {
                    val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    clipboardManager?.let { cm ->
                        cm.primaryClip = ClipData("deckbox user id", arrayOf("text/plain"), ClipData.Item(preference.summary))
                        toast("User Id copied to clipboard")
                    }
                    true
                }
                "pref_account_signout" -> {
                    Analytics.event(Event.Logout)
                    Shortcuts.clearShortcuts(activity!!)
                    preferences.deviceId = null
                    preferences.offlineId.delete()
                    FirebaseAuth.getInstance().signOut()
                    if (googleClient != null && googleClient?.isConnected == true) {
                        googleClient?.clearDefaultAccountAndReconnect()
                    }
                    val intent = SetupActivity.createIntent(activity!!).clear()
                    startActivity(intent)
                    activity?.finish()
                    true
                }
                else -> super.onPreferenceTreeClick(preference)
            }
        }


        override fun onConnectionFailed(p0: ConnectionResult) {
            Timber.e("onConnectionFailed(${p0.errorCode} :: ${p0.errorMessage}")
        }


        private fun setupPreferences() {
            val profilePref = findPreference("pref_account_profile") as ProfilePreference
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                if (user.isAnonymous) {
                    profilePref.isVisible = false
                } else {
                    profilePref.isVisible = true
                    profilePref.avatarUrl = user.photoUrl
                    profilePref.title = user.displayName
                    profilePref.summary = user.email
                }

                val linkAccount = findPreference("pref_account_link")
                linkAccount.isVisible = user.isAnonymous
            } else {
                profilePref.isVisible = false

                val linkAccount = findPreference("pref_account_link")
                linkAccount.isVisible = true
            }

            val versionPref = findPreference("pref_about_version")
            versionPref.summary = BuildConfig.VERSION_NAME

            val resetQuickStart = findPreference("pref_reset_quickstart")
            resetQuickStart.isVisible = !preferences.quickStart.get()

            val resetOfflineOutline = findPreference("pref_reset_offline_outline")
            resetOfflineOutline.isVisible = !preferences.offlineOutline.get()

            @SuppressLint("RxSubscribeOnError")
            disposables += preferences.quickStart.asObservable()
                    .subscribe {
                        resetQuickStart.isVisible = !it
                    }

            @SuppressLint("RxSubscribeOnError")
            disposables += preferences.offlineOutline.asObservable()
                    .subscribe {
                        resetOfflineOutline.isVisible = !it
                    }

            /*
             * Debug options
             */

            val category = findPreference("pref_category_developer")
            category.isVisible = BuildConfig.DEBUG

            val userId = findPreference("pref_developer_user_id")
            userId.summary = FirebaseAuth.getInstance().currentUser?.uid
                    ?: preferences.deviceId
                    ?: preferences.offlineId.get()
        }


        private fun setupClient() {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            googleClient = GoogleApiClient.Builder(activity!!)
                    .enableAutoManage(activity as androidx.fragment.app.FragmentActivity, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build()
        }


        private fun signIn() {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleClient)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }


        private fun handleSignInResult(result: GoogleSignInResult) {
            if (result.isSuccess) {
                val firebaseAuth = FirebaseAuth.getInstance()
                val acct = result.signInAccount
                val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null && currentUser.isAnonymous) {
                    currentUser.linkWithCredential(credential)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Analytics.event(Event.SignUp.Google)
                                    snackbar("${it.result?.user?.email} linked!")
                                    setupPreferences()
                                } else {
                                    Timber.e(it.exception, "Unable to link account")
                                    snackbar("Unable to link account", Snackbar.LENGTH_LONG)
                                }
                            }
                } else {
                    firebaseAuth.signInWithCredential(credential)
                            .addOnCompleteListener { r ->
                                if (r.isSuccessful) {
                                    // Auto-grab the user's name from their account
                                    r.result?.user?.displayName?.let {
                                        preferences.playerName.set(it)
                                    }
                                    r.result?.user?.uid?.let {
                                        Analytics.userId(it)
                                    }
                                    Analytics.event(Event.SignUp.Google)
                                    setupPreferences()

                                    // Now we need to migrate any existing local decks to their account
                                    view?.let {
                                        migrationSnackbar = Snackbar.make(it, R.string.account_migration_started, Snackbar.LENGTH_INDEFINITE)
                                        migrationSnackbar?.show()
                                    }

                                    disposables += accountRepository.migrateAccount()
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                view?.let { v ->
                                                    migrationSnackbar = Snackbar.make(v, R.string.account_migration_finished, Snackbar.LENGTH_SHORT)
                                                    migrationSnackbar?.show()
                                                }
                                            }, { e ->
                                                view?.let { v ->
                                                    migrationSnackbar = Snackbar.make(v, e.localizedMessage, Snackbar.LENGTH_SHORT)
                                                    migrationSnackbar?.show()
                                                }
                                            })


                                } else {
                                    snackbar("Authentication failed")
                                }
                            }
                }
            }
            else {
                Timber.e("Unable to link account: ${result.status}")
                snackbar("Unable to link account.")
            }
        }

        companion object {
            private const val RC_SIGN_IN = 100
        }
    }


    companion object {

        fun createIntent(context: Context): Intent = Intent(context, SettingsActivity::class.java)
    }
}