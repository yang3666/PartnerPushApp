package com.tencent.wxcloudrun.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.config.WxConfigure;
import com.tencent.wxcloudrun.controller.task.TaskRunnable;
import com.tencent.wxcloudrun.entity.ScheduledJob;
import com.tencent.wxcloudrun.service.PartnerService;
import com.tencent.wxcloudrun.service.ScheduledTaskService;
import com.tencent.wxcloudrun.util.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class ScheduledTaskServiceImpl implements ScheduledTaskService {

    /**
     * 可重入锁
     */
    private ReentrantLock lock = new ReentrantLock();


    /**
     * 定时任务线程池
     */
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    /**
     * 启动状态的定时任务集合
     */
    public Map<String, ScheduledFuture> scheduledFutureMap = new ConcurrentHashMap<>();

    /**
     * Service
     */
    @Autowired
    public PartnerService partnerService;


    @Autowired
    private WxConfigure wxConfigure;

    @Override
    public Boolean start(ScheduledJob scheduledJob) {
        String jobKey = scheduledJob.getJobKey();
        log.info("启动定时任务" + jobKey);
        //添加锁放一个线程启动，防止多人启动多次
        lock.lock();
        try {
            log.info("加锁完成");
            if (this.isStart(jobKey)) {
                log.info("当前任务在启动状态中");
                return false;
            }
            //任务启动
            this.doStartTask(scheduledJob);
        } finally {
            lock.unlock();
            log.info("解锁完毕");
        }

        return true;
    }

    /**
     * 任务是否已经启动
     */
    private Boolean isStart(String taskKey) {
        //校验是否已经启动
        if (scheduledFutureMap.containsKey(taskKey)) {
            if (!scheduledFutureMap.get(taskKey).isCancelled()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean stop(String jobKey) {
        log.info("停止任务 " + jobKey);
        boolean flag = scheduledFutureMap.containsKey(jobKey);
        log.info("当前实例是否存在 " + flag);
        if (flag) {
            ScheduledFuture scheduledFuture = scheduledFutureMap.get(jobKey);

            scheduledFuture.cancel(true);

            scheduledFutureMap.remove(jobKey);
        }
        return flag;
    }

    @Override
    public Boolean restart(ScheduledJob scheduledJob) {
        log.info("重启定时任务" + scheduledJob.getJobKey());
        //停止
        this.stop(scheduledJob.getJobKey());

        return this.start(scheduledJob);
    }

    @Override
    public void initTask() {
        getAccessToken();
        //获取token
        //启动线程
        List<ScheduledJob> list = partnerService.list();
        for (ScheduledJob sj : list) {
            if (sj.getStatus().intValue() == -1) //未启用
                continue;
            doStartTask(sj);
        }
    }

    /**
     * 设置token
     *
     * @throws Exception
     */
    public void getAccessToken() {
        String getJson = null;
        try {
            getJson = HttpClientUtils.doGet(wxConfigure.getApp_get_access_token() + "&appid=" +
                    wxConfigure.getApp_id() + "&secret=" +
                    wxConfigure.getApp_secret(), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JSONObject jsonObject = JSON.parseObject(getJson);
        String access_token = (String) jsonObject.get("access_token");

        log.info("初始化AccessToken");
        wxConfigure.setAccess_token(access_token);
    }

    /**
     * 执行启动任务
     */
    public void doStartTask(ScheduledJob sj) {
        log.info(sj.getJobKey());
        if (sj.getStatus().intValue() == 1){
            //启用定时器
            ScheduledFuture scheduledFuture = threadPoolTaskScheduler.schedule(new TaskRunnable(sj),
                    (triggerContext -> new CronTrigger(sj.getCronExpression()).nextExecutionTime(triggerContext)));
            //将已经执行的线程放入concurrentMap中。
            scheduledFutureMap.put(sj.getJobKey(), scheduledFuture);
        }

    }
}

