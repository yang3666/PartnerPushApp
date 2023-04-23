package com.tencent.wxcloudrun.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.wxcloudrun.entity.ScheduledJob;
import com.tencent.wxcloudrun.mapper.TaskMapper;
import com.tencent.wxcloudrun.service.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 作者： Juran on 2022-09-01 19:06
 * 作者博客：iit.la
 */
@Service
public class PartnerServiceImpl extends ServiceImpl<TaskMapper, ScheduledJob> implements PartnerService {

    @Autowired
    TaskMapper taskMapper;

    /**
     * 开始任务
     * @param scheduledJob
     * @return
     */
    @Override
    public Boolean start(ScheduledJob scheduledJob) {
        return null;
    }

    /**
     * 结束任务
     * @param jobKey
     * @return
     */
    @Override
    public Boolean stop(String jobKey) {
        return null;
    }

    /**
     * 重新启动任务
     * @param scheduledJob
     * @return
     */
    @Override
    public Boolean restart(ScheduledJob scheduledJob) {
        return null;
    }

    /**
     * 初始化所有任务
     */
    @Override
    public void initTask() {

    }
}
