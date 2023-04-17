package org.ole.planet.myplanet.ui.dashboard

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import org.ole.planet.myplanet.MainApplication
import org.ole.planet.myplanet.R
import org.ole.planet.myplanet.base.BaseResourceFragment
import org.ole.planet.myplanet.model.RealmCertification
import org.ole.planet.myplanet.model.RealmCourseProgress
import org.ole.planet.myplanet.model.RealmExamQuestion
import org.ole.planet.myplanet.ui.course.CourseFragment
import org.ole.planet.myplanet.ui.course.MyProgressFragment
import org.ole.planet.myplanet.ui.feedback.FeedbackListFragment
import org.ole.planet.myplanet.ui.library.AddResourceFragment
import org.ole.planet.myplanet.ui.library.LibraryFragment
import org.ole.planet.myplanet.ui.mylife.LifeFragment
import org.ole.planet.myplanet.ui.survey.SurveyFragment
import org.ole.planet.myplanet.ui.team.TeamFragment
import org.ole.planet.myplanet.utilities.TimeUtils
import java.util.*


import org.ole.planet.myplanet.databinding.CardProfileBinding
import org.ole.planet.myplanet.databinding.FragmentHomeBellBinding
import org.ole.planet.myplanet.databinding.HomeCardCoursesBinding
import org.ole.planet.myplanet.databinding.HomeCardLibraryBinding
import org.ole.planet.myplanet.databinding.HomeCardMylifeBinding
import org.ole.planet.myplanet.databinding.HomeCardTeamsBinding

class BellDashboardFragment : BaseDashboardFragment() {

    private var _binding: FragmentHomeBellBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeBellBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _binding?.txtDate?.text = TimeUtils.formatDate(Date().time)
        _binding?.txtCommunityName?.text = model.planetCode
        (activity as DashboardActivity?)?.supportActionBar?.hide()
        _binding?.addResource?.setOnClickListener { v: View? -> AddResourceFragment().show(childFragmentManager, "Add Resource") }
        showBadges()
        if (!model.id.startsWith("guest") && TextUtils.isEmpty(model.key) && MainApplication.showHealthDialog) {
            AlertDialog.Builder(activity!!).setMessage("Health record not available, Sync health data?").setPositiveButton("Sync") { dialogInterface: DialogInterface?, i: Int ->
                syncKeyId()
                MainApplication.showHealthDialog = false
            }.setNegativeButton("Cancel", null).show()
        }
        // forceDownloadNewsImages();
    }


    private fun showBadges() {
        ll_badges.removeAllViews()
        val list = RealmCourseProgress.getPassedCourses(mRealm, BaseResourceFragment.settings.getString("userId", ""))
        for (sub in list) {
            val star = LayoutInflater.from(activity).inflate(R.layout.image_start, null) as ImageView
            val examId = if (sub.parentId.contains("@")) sub.parentId.split("@").toTypedArray()[0] else sub.parentId
            val courseId = if (sub.parentId.contains("@")) sub.parentId.split("@").toTypedArray()[1] else ""
            val questions = mRealm.where(RealmExamQuestion::class.java).equalTo("examId", examId).count()
            setColor(questions, courseId, star)
            ll_badges.addView(star)
        }
    }

    private fun setColor(questions: Long, courseId: String, star: ImageView) =
            if (RealmCertification.isCourseCertified(mRealm, courseId)) {
                star.setColorFilter(resources.getColor(R.color.colorPrimary))
            } else {
                star.setColorFilter(resources.getColor(R.color.md_blue_grey_300))
            }

    private fun declareElements(view: View) {
        initView(view)
        view.ll_home_team.setOnClickListener { homeItemClickListener.openCallFragment(TeamFragment()) }
        view.myLibraryImageButton.setOnClickListener { openHelperFragment(LibraryFragment()) }
        view.myCoursesImageButton.setOnClickListener { openHelperFragment(CourseFragment()) }
        view.fab_my_progress.setOnClickListener { openHelperFragment(MyProgressFragment()) }
        view.fab_my_activity.setOnClickListener { openHelperFragment(MyActivityFragment()) }
        view.fab_survey.setOnClickListener { openHelperFragment(SurveyFragment()) }
        view.fab_feedback.setOnClickListener { openHelperFragment(FeedbackListFragment()) }
        view.myLifeImageButton.setOnClickListener { homeItemClickListener.openCallFragment(LifeFragment()) }
        view.fab_notification.setOnClickListener { showNotificationFragment() }
    }

    private fun openHelperFragment(f: Fragment) {
        val b = Bundle()
        b.putBoolean("isMyCourseLib", true)
        f.arguments = b
        homeItemClickListener.openCallFragment(f)
    }

    companion object {
        const val PREFS_NAME = "OLE_PLANET"
    }
}