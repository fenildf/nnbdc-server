package beidanci;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Aspect
public class CacheMonitor {
	private final static Logger LOG = LoggerFactory.getLogger(CacheMonitor.class);
	private final static NumberFormat NF = new DecimalFormat("0.0###");

	@Autowired
	private SessionFactory sessionFactory;

	@Around("execution(* beidanci.bo..*.*(..))")
	public Object log(ProceedingJoinPoint pjp) throws Throwable {
		if (!LOG.isDebugEnabled()) {
			return pjp.proceed();
		}

		Statistics statistics = sessionFactory.getStatistics();
		statistics.setStatisticsEnabled(true);

		long hit0 = statistics.getSecondLevelCacheHitCount();
		long miss0 = statistics.getSecondLevelCacheMissCount();
		long qhit0 = statistics.getQueryCacheHitCount();
		long qmiss0 = statistics.getQueryCacheMissCount();

		Object result = pjp.proceed();

		long hit1 = statistics.getSecondLevelCacheHitCount();
		long miss1 = statistics.getSecondLevelCacheMissCount();
		long qhit1 = statistics.getQueryCacheHitCount();
		long qmiss1 = statistics.getQueryCacheMissCount();

		double ratio = (double) hit1 / (hit1 + miss1);

		if (hit1 > hit0) {
			LOG.debug(String.format("CACHE HIT; Ratio=%s; Signature=%s#%s()", NF.format(ratio),
					pjp.getTarget().getClass().getName(), pjp.getSignature().toShortString()));
		} else if (miss1 > miss0) {
			LOG.debug(String.format("CACHE MISS; Ratio=%s; Signature=%s#%s()", NF.format(ratio),
					pjp.getTarget().getClass().getName(), pjp.getSignature().toShortString()));
		}

		if (qhit1 > qhit0) {
			LOG.debug(String.format("QUREY CACHE HIT; Ratio=%s; Signature=%s#%s()", NF.format(ratio),
					pjp.getTarget().getClass().getName(), pjp.getSignature().toShortString()));
		} else if (qmiss1 > qmiss0) {
			LOG.debug(String.format("QUREY CACHE MISS; Ratio=%s; Signature=%s#%s()", NF.format(ratio),
					pjp.getTarget().getClass().getName(), pjp.getSignature().toShortString()));
		}

		return result;
	}
}
