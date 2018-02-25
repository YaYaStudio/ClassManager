package com.cls.manager.iview

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.angcyo.bmob.RBmob
import com.angcyo.uiview.container.ContentLayout
import com.angcyo.uiview.dialog.UIItemSelectorDialog
import com.angcyo.uiview.dialog.UILoading
import com.angcyo.uiview.kotlin.add
import com.angcyo.uiview.kotlin.have
import com.angcyo.uiview.kotlin.onSizeChanged
import com.angcyo.uiview.kotlin.remove
import com.angcyo.uiview.model.TitleBarPattern
import com.angcyo.uiview.net.RLoadingSubscriber
import com.angcyo.uiview.recycler.RBaseItemDecoration
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.recycler.RRecyclerView
import com.angcyo.uiview.rsen.RefreshLayout
import com.angcyo.uiview.skin.SkinHelper
import com.angcyo.uiview.utils.RUtils
import com.angcyo.uiview.utils.Tip
import com.cls.manager.R
import com.cls.manager.bean.LessonBean
import com.cls.manager.bean.StudentBean
import com.cls.manager.bean.TeacherBean
import com.cls.manager.control.UserControl

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：老师课程安排, 周一 到 周五, 第一节课 到 第十节课
 * 创建人员：Robi
 * 创建时间：2018/02/23 15:09
 * 修改人员：Robi
 * 修改时间：2018/02/23 15:09
 * 修改备注：
 * Version: 1.0.0
 */
open class AddTeacherUIView(val isTeacher: Boolean = true) : BaseClassUIView<TeacherBean>() {

    var teacherBean = TeacherBean().apply {
        name = UserControl.loginUserBean!!.name
    }

    var studentBean = StudentBean().apply {
        name = UserControl.loginUserBean!!.name
    }

    private val name: String
        get() {
            return if (isTeacher) "老师名称" else "学生名称"
        }
    private val titleName: String
        get() {
            return if (isTeacher) "添加老师课表" else "添加学生课表"
        }

    private val titleNameTip: String
        get() {
            return if (isTeacher) "请输入老师名称" else "请输入学生名称"
        }

    private val existTip: String
        get() {
            return if (isTeacher) "老师已存在, 是否更新数据?" else "学生已存在, 是否更新数据?"
        }


    override fun getTitleBar(): TitleBarPattern {
        return super.getTitleBar()
                .setTitleString(titleName)
                .addRightItem(TitleBarPattern.TitleBarItem("保存") {
                    if (isTeacher) {
                        saveTeacher()
                    } else {
                        saveStudent()
                    }
                }.setVisibility(View.GONE))
    }

    protected open fun saveTeacher() {
        if (teacherBean.name.isEmpty()) {
            Tip.tip(titleNameTip)
        } else {
            UILoading.show2(mParentILayout)
            val query = BmobQuery<TeacherBean>()
            query.addWhereEqualTo("name", teacherBean.name)
            query.findObjects(object : FindListener<TeacherBean>() {
                override fun done(p0: MutableList<TeacherBean>?, p1: BmobException?) {
                    if (p0?.isNotEmpty() != false) {
                        //Tip.tip("老师已存在, 是否更新数据?")
//                        UILoading.hide()
//                        UIDialog.build()
//                                .setDialogContent(existTip)
//                                .setOkListener {
//                                    teacherBean.updateObservable(p0!![0].objectId)
//                                            .subscribe(object : RLoadingSubscriber<Void>(mParentILayout) {
//
//                                                override fun onSucceed(bean: Void?) {
//                                                    super.onSucceed(bean)
//                                                    Tip.tip("更新课表成功")
//                                                    finishIView()
//                                                }
//
//                                                override fun onError(code: Int, msg: String?) {
//                                                    super.onError(code, msg)
//                                                    Tip.tip(msg)
//                                                }
//                                            })
//                                }
//                                .showDialog(mParentILayout)

                        teacherBean.updateObservable(p0!![0].objectId)
                                .subscribe(object : RLoadingSubscriber<Void>(mParentILayout) {

                                    override fun onSucceed(bean: Void?) {
                                        super.onSucceed(bean)
                                        Tip.tip("更新课表成功")
                                        finishIView()
                                    }

                                    override fun onError(code: Int, msg: String?) {
                                        super.onError(code, msg)
                                        Tip.tip(msg)
                                    }
                                })
                    } else {
                        teacherBean.saveObservable()
                                .subscribe(object : RLoadingSubscriber<String>(mParentILayout) {

                                    override fun onSucceed(bean: String?) {
                                        super.onSucceed(bean)
                                        Tip.tip("添加课表成功")
                                        finishIView()
                                    }

                                    override fun onError(code: Int, msg: String?) {
                                        super.onError(code, msg)
                                        Tip.tip(msg)
                                    }
                                })
                    }
                }
            })
        }
    }

    protected open fun saveStudent() {
        UILoading.progress(mParentILayout).setLoadingTipText("保存中...")
        RBmob.update(StudentBean::class.java, studentBean, "name:${studentBean.name}") {
            if (it.isEmpty()) {
                Tip.tip("保存失败")
            } else {
                finishIView()
                Tip.tip("保存成功")
            }
            UILoading.hide()
        }
    }

    override fun onBindClassView(holder: RBaseViewHolder, position: Int, bean: TeacherBean?) {
        super.onBindClassView(holder, position, bean)
        if (isTeacher) {
            initTeacher(holder, position)
        } else {
            initStudent(holder, position)
        }
    }

    private fun initStudent(holder: RBaseViewHolder, position: Int) {
        val rowIndex = position / 6 - 1//横向第几行

        fun setBg(list: List<String>) {
            if (list[rowIndex].isEmpty()) {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                holder.tv(R.id.text_view).text = ""
            } else {
                holder.itemView.setBackgroundColor(SkinHelper.getSkin().themeSubColor)
                holder.tv(R.id.text_view).text = list[rowIndex]
                holder.tv(R.id.text_view).setTextColor(Color.WHITE)
            }
        }

        when (position.rem(6)) {
            1 -> {//周一
                val wList = studentBean.w1List()
                setBg(wList)
                holder.clickItem {
                    startIView(UIItemSelectorDialog(lessonBeanList).apply {
                        onInitItemLayout = { holder, _, dataBean ->
                            holder.tv(R.id.base_text_view).text = dataBean.name
                        }
                        onItemSelector = { _, bean ->
                            studentBean.w1 = RUtils.connect(wList.apply {
                                set(rowIndex, bean.name)
                            })
                            mExBaseAdapter.notifyItemChanged(position)
                        }
                    })
                }
            }
            2 -> {
                val wList = studentBean.w2List()
                setBg(wList)
                holder.clickItem {
                    startIView(UIItemSelectorDialog(lessonBeanList).apply {
                        onInitItemLayout = { holder, _, dataBean ->
                            holder.tv(R.id.base_text_view).text = dataBean.name
                        }
                        onItemSelector = { _, bean ->
                            studentBean.w2 = RUtils.connect(wList.apply {
                                set(rowIndex, bean.name)
                            })
                            mExBaseAdapter.notifyItemChanged(position)
                        }
                    })
                }
            }
            3 -> {
                val wList = studentBean.w3List()
                setBg(wList)
                holder.clickItem {
                    startIView(UIItemSelectorDialog(lessonBeanList).apply {
                        onInitItemLayout = { holder, _, dataBean ->
                            holder.tv(R.id.base_text_view).text = dataBean.name
                        }
                        onItemSelector = { _, bean ->
                            studentBean.w3 = RUtils.connect(wList.apply {
                                set(rowIndex, bean.name)
                            })
                            mExBaseAdapter.notifyItemChanged(position)
                        }
                    })
                }
            }
            4 -> {
                val wList = studentBean.w4List()
                setBg(wList)
                holder.clickItem {
                    startIView(UIItemSelectorDialog(lessonBeanList).apply {
                        onInitItemLayout = { holder, _, dataBean ->
                            holder.tv(R.id.base_text_view).text = dataBean.name
                        }
                        onItemSelector = { _, bean ->
                            studentBean.w4 = RUtils.connect(wList.apply {
                                set(rowIndex, bean.name)
                            })
                            mExBaseAdapter.notifyItemChanged(position)
                        }
                    })
                }
            }
            5 -> {
                val wList = studentBean.w5List()
                setBg(wList)
                holder.clickItem {
                    startIView(UIItemSelectorDialog(lessonBeanList).apply {
                        onInitItemLayout = { holder, _, dataBean ->
                            holder.tv(R.id.base_text_view).text = dataBean.name
                        }
                        onItemSelector = { _, bean ->
                            studentBean.w5 = RUtils.connect(wList.apply {
                                set(rowIndex, bean.name)
                            })
                            mExBaseAdapter.notifyItemChanged(position)
                        }
                    })
                }
            }
        }
    }


    open fun setTeacherItem(holder: RBaseViewHolder, w: Int, position: Int) {
        val rowShl = 0b1.shl(position / 6 - 1)

        if (w.have(rowShl)) {
            holder.itemView.setBackgroundColor(SkinHelper.getSkin().themeSubColor)
            holder.tv(R.id.text_view).text = "√"
            holder.tv(R.id.text_view).setTextColor(Color.WHITE)
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            holder.tv(R.id.text_view).text = ""
        }
    }

    protected open fun initTeacher(holder: RBaseViewHolder, position: Int) {
        val rowShl = 0b1.shl(position / 6 - 1)

        when (position.rem(6)) {
            1 -> {//周一
                setTeacherItem(holder, teacherBean.w1, position)
                holder.clickItem {
                    if (teacherBean.w1.have(rowShl)) {
                        teacherBean.w1 = teacherBean.w1.remove(rowShl)
                    } else {
                        teacherBean.w1 = teacherBean.w1.add(rowShl)
                    }
                    mExBaseAdapter.notifyItemChanged(position)
                }
            }
            2 -> {
                setTeacherItem(holder, teacherBean.w2, position)
                holder.clickItem {
                    if (teacherBean.w2.have(rowShl)) {
                        teacherBean.w2 = teacherBean.w2.remove(rowShl)
                    } else {
                        teacherBean.w2 = teacherBean.w2.add(rowShl)
                    }
                    mExBaseAdapter.notifyItemChanged(position)
                }
            }
            3 -> {
                setTeacherItem(holder, teacherBean.w3, position)
                holder.clickItem {
                    if (teacherBean.w3.have(rowShl)) {
                        teacherBean.w3 = teacherBean.w3.remove(rowShl)
                    } else {
                        teacherBean.w3 = teacherBean.w3.add(rowShl)
                    }
                    mExBaseAdapter.notifyItemChanged(position)
                }
            }
            4 -> {
                setTeacherItem(holder, teacherBean.w4, position)
                holder.clickItem {
                    if (teacherBean.w4.have(rowShl)) {
                        teacherBean.w4 = teacherBean.w4.remove(rowShl)
                    } else {
                        teacherBean.w4 = teacherBean.w4.add(rowShl)
                    }
                    mExBaseAdapter.notifyItemChanged(position)
                }
            }
            5 -> {
                setTeacherItem(holder, teacherBean.w5, position)
                holder.clickItem {
                    if (teacherBean.w5.have(rowShl)) {
                        teacherBean.w5 = teacherBean.w5.remove(rowShl)
                    } else {
                        teacherBean.w5 = teacherBean.w5.add(rowShl)
                    }
                    mExBaseAdapter.notifyItemChanged(position)
                }
            }
        }
    }

    override fun initRefreshLayout(refreshLayout: RefreshLayout?, baseContentLayout: ContentLayout?) {
        super.initRefreshLayout(refreshLayout, baseContentLayout)
        refreshLayout?.isEnabled = false
    }

    override fun initRecyclerView(recyclerView: RRecyclerView?, baseContentLayout: ContentLayout?) {
        super.initRecyclerView(recyclerView, baseContentLayout)
        recyclerView?.let {
            it.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            it.layoutManager = GridLayoutManager(mActivity, 6)
            it.onSizeChanged { w, h, oldw, oldh ->
                if (w > 0 && h > 0) {
                    it.adapter?.notifyDataSetChanged()
                }
            }
            it.addItemDecoration(RBaseItemDecoration(getDimensionPixelOffset(R.dimen.base_line), getColor(R.color.base_chat_bg_color)))
        }
    }

    override fun getDefaultRequestedOrientation(): Int {
        return ActivityInfo.SCREEN_ORIENTATION_USER
    }

    override fun getDefaultLayoutState(): LayoutState {
        return LayoutState.LOAD
    }

    private val lessonBeanList = mutableListOf<LessonBean>()
    override fun onViewShowFirst(bundle: Bundle?) {
        super.onViewShowFirst(bundle)
    }

    override fun onUILoadData(page: Int) {
        super.onUILoadData(page)
        RBmob.query<LessonBean>().apply {
            add(findObjects(object : FindListener<LessonBean>() {
                override fun done(p0: MutableList<LessonBean>?, p1: BmobException?) {
                    p0?.let {
                        lessonBeanList.addAll(it)
                    }
                }
            }))
        }

        if (isTeacher) {
            val query = BmobQuery<TeacherBean>()
            query.addWhereEqualTo("name", teacherBean.name)
            query.findObjects(object : FindListener<TeacherBean>() {
                override fun done(p0: MutableList<TeacherBean>?, p1: BmobException?) {
                    if (!RUtils.isListEmpty(p0)) {
                        teacherBean = p0!!.first()
                    }
                    onShowContentData()
                }
            })
        } else {
            RBmob.query<StudentBean>(StudentBean::class.java, "name:${studentBean.name}") {
                if (!RUtils.isListEmpty(it)) {
                    studentBean = it.first()
                }
                onShowContentData()
            }
        }
    }

    protected open fun onShowContentData() {
        showContentLayout()
        resetUI()
        uiTitleBarContainer.showRightItem(0)
    }
}
