package com.wavemaker.runtime.scheduler.quartz.trigger;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.impl.triggers.CronTriggerImpl;

import com.wavemaker.studio.common.WMRuntimeException;

/**
 * Created by saddhamp on 30/5/16.
 */
public class WMCronTriggerImpl extends CronTriggerImpl{
    public static final int REPEAT_INDEFINITELY = -1;

    private int repeatCount = 0;
    private int timesTriggered = 0;

    /**
     * <p>
     * Set the the number of time the <code>CronTrigger</code> should
     * repeat, after which it will be automatically deleted.
     * </p>
     *
     * @see #REPEAT_INDEFINITELY
     * @exception WMRuntimeException
     *              if repeatCount is < 0
     */
    public void setRepeatCount(int repeatCount) {
        if (repeatCount < 0 && repeatCount != REPEAT_INDEFINITELY) {
            throw new WMRuntimeException(
                    "Repeat count must be >= 0, use the "
                            + "constant REPEAT_INDEFINITELY for infinite.");
        }

        this.repeatCount = repeatCount;
    }

    /**
     * <p>
     * Called when the <code>{@link Scheduler}</code> has decided to 'fire'
     * the trigger (execute the associated <code>Job</code>), in order to
     * give the <code>Trigger</code> a chance to update itself for its next
     * triggering (if any).
     * </p>
     *
     * @see #executionComplete(JobExecutionContext, JobExecutionException)
     */
    @Override
    public void triggered(org.quartz.Calendar calendar) {
        timesTriggered++;
        super.triggered(calendar);
    }

    /**
     * <p>
     * Returns the next time at which the <code>CronTrigger</code> will fire,
     * after the given time. If the trigger will not fire after the given time,
     * <code>null</code> will be returned.
     * </p>
     *
     * <p>
     * Note that the date returned is NOT validated against the related
     * org.quartz.Calendar (if any)
     * </p>
     */
    @Override
    public Date getFireTimeAfter(Date afterTime) {
        if ((timesTriggered > repeatCount) && (repeatCount != REPEAT_INDEFINITELY)) {
            return null;
        }

        if (afterTime == null) {
            afterTime = new Date();
        }

        if (repeatCount == 0 && afterTime.compareTo(getStartTime()) >= 0) {
            return null;
        }

        if (getStartTime().after(afterTime)) {
            afterTime = new Date(getStartTime().getTime() - 1000l);
        }

        if (getEndTime() != null && (afterTime.compareTo(getEndTime()) >= 0)) {
            return null;
        }

        Date pot = getTimeAfter(afterTime);
        if (getEndTime() != null && pot != null && pot.after(getEndTime())) {
            return null;
        }

        return pot;
    }

    /**
     * <p>
     * Returns the final time at which the
     * <code>CronTrigger</code> will fire.
     * </p>
     *
     * <p>
     * Note that the return time *may* be in the past. and the date returned is
     * not validated against org.quartz.calendar
     * </p>
     */
    @Override
    public Date getFinalFireTime() {
        if (repeatCount == 0) {
            return getStartTime();
        }
        
        return super.getFinalFireTime();
    }
}
