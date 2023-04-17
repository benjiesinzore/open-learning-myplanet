package org.ole.planet.myplanet.ui.dashboard

import android.os.Bundle
import android.text.Html
import org.ole.planet.myplanet.R
import org.ole.planet.myplanet.base.BaseActivity
import org.ole.planet.myplanet.databinding.ActivityDisclaimerBinding
import org.ole.planet.myplanet.utilities.Constants

class DisclaimerActivity : BaseActivity() {

    private var binding : ActivityDisclaimerBinding = ActivityDisclaimerBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disclaimer)
        initActionBar()
        binding.tvDisclaimer.text = Html.fromHtml(Constants.DISCLAIMER)
    }


}
