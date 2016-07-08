package com.wavemaker.runtime.scheduler.quartz.trigger;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Map;
import java.util.TimeZone;

import org.joda.time.LocalDateTime;
import org.quartz.CalendarIntervalTrigger;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by saddhamp on 24/5/16.
 */
public class WMCronTriggerFactoryBean implements FactoryBean<CronTrigger>, BeanNameAware, InitializingBean {
    private static String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private String name;
    private String group;
    private JobDetail jobDetail;
    private JobDataMap jobDataMap = new JobDataMap();
    private String startTime;
    private String endTime;
    private String cronExpression;
    private TimeZone timeZone;
    private String calendarName;
    private int priority;
    private String description;
    private String beanName;
    private int repeatCount = WMCronTriggerImpl.REPEAT_INDEFINITELY;
    private int misfireInstruction = CalendarIntervalTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;
    private CronTrigger cronTrigger;

    public void setName(String name) {
        this.name = name;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setJobDetail(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
    }

    public void setJobDataMap(JobDataMap jobDataMap) {
        this.jobDataMap = jobDataMap;
    }

    public void setJobDataAsMap(Map<String, ?> jobDataAsMap) {
        this.jobDataMap.putAll(jobDataAsMap);
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setTimeZone(String timeZoneId) {
        this.timeZone = TimeZone.getTimeZone(timeZoneId);
    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void setMisfireInstruction(int misfireInstruction) {
        this.misfireInstruction = misfireInstruction;
    }

    @Override
    public void afterPropertiesSet() throws ParseException {
        if (name == null) {
            name = this.beanName;
        }
        if (group == null) {
            group = Scheduler.DEFAULT_GROUP;
        }
        if (jobDetail != null) {
            jobDataMap.put("jobDetail", this.jobDetail);
        }
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        Timestamp startTimestamp = Timestamp.valueOf(LocalDateTime.parse(startTime).toString(DEFAULT_TIMESTAMP_FORMAT));

        Timestamp endTimestamp = null;
        if(endTime != null){
            endTimestamp = Timestamp.valueOf(LocalDateTime.parse(endTime).toString(DEFAULT_TIMESTAMP_FORMAT));
        }

        WMCronTriggerImpl wmCronTriggerImpl = new WMCronTriggerImpl();
        wmCronTriggerImpl.setName(name);
        wmCronTriggerImpl.setGroup(group);
        wmCronTriggerImpl.setJobKey(jobDetail.getKey());
        wmCronTriggerImpl.setJobDataMap(jobDataMap);
        wmCronTriggerImpl.setStartTime(startTimestamp);
        wmCronTriggerImpl.setCronExpression(cronExpression);
        wmCronTriggerImpl.setTimeZone(timeZone);
        wmCronTriggerImpl.setCalendarName(calendarName);
        wmCronTriggerImpl.setPriority(priority);
        wmCronTriggerImpl.setDescription(description);
        wmCronTriggerImpl.setRepeatCount(repeatCount);
        wmCronTriggerImpl.setMisfireInstruction(misfireInstruction);
        wmCronTriggerImpl.setEndTime(endTimestamp);
        this.cronTrigger = wmCronTriggerImpl;
    }

    @Override
    public CronTrigger getObject() {
        return this.cronTrigger;
    }

    @Override
    public Class<?> getObjectType() {
        return CronTrigger.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
