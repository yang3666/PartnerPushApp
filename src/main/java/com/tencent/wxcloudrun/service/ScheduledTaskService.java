package com.tencent.wxcloudrun.service;


import com.tencent.wxcloudrun.entity.ScheduledJob;

public interface ScheduledTaskService{
 
    Boolean start(ScheduledJob scheduledJob);
 
    Boolean stop(String jobKey);
 
    Boolean restart(ScheduledJob scheduledJob);
 
    void initTask();
}