package com.utsman.kemana.driver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.utsman.kemana.auth.AuthListener
import com.utsman.kemana.auth.User
import com.utsman.kemana.auth.adapter.AuthPagerAdapter
import com.utsman.kemana.auth.fragment.LoginFragment
import com.utsman.kemana.auth.fragment.RegisterFragment
import com.utsman.kemana.auth.userToString
import com.utsman.kemana.base.ext.ProgressHelper
import com.utsman.kemana.base.ext.intentTo
import com.utsman.kemana.base.ext.logi
import com.utsman.kemana.base.ext.preferences
import com.utsman.kemana.base.ext.toast
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {

    private lateinit var progressHelper: ProgressHelper

    private val authListener = object : AuthListener {
        override fun toRegister() {
            auth_view_pager.setCurrentItem(1, true)
        }

        override fun onLoaded() {
            progressHelper.showProgressDialog()
        }

        override fun onLoginSuccess(user: User, password: String, fromRegister: Boolean) {
            progressHelper.hideProgressDialog()

            if (fromRegister) {
                finish()
                intentTo(LauncherActivity::class.java)
            } else {
                val preferences = preferences("account")
                logi("${user.email} -- ${user.password}")
                logi("token is --> ${user.token}")
                preferences.edit().apply {
                    putString("email", user.email)
                    putString("password", password)
                    putString("token", user.token)
                    putString("user-id", user.userId)
                }.apply()
            }

            intentTo(MapsActivity::class.java, bundleOf("user" to user.userToString()))
            finish()
        }

        override fun onLoginUnauthorized() {
            progressHelper.hideProgressDialog()
            toast("login unautorized")
            auth_view_pager.setCurrentItem(1, true)
        }

        override fun onLoginFailed(throwable: Throwable) {
            progressHelper.hideProgressDialog()
            toast("login failed --> ${throwable.message}")
            throwable.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        progressHelper = ProgressHelper(this)

        val loginFragment = LoginFragment(authListener)
        val registerFragment = RegisterFragment(authListener, true)

        val authPagerAdapter = AuthPagerAdapter(supportFragmentManager)
        authPagerAdapter.addFragment(loginFragment, registerFragment)

        auth_view_pager.adapter = authPagerAdapter
    }
}