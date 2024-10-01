package ssg.highscore.restcontroller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import ssg.highscore.dao.CronJobDao;
import ssg.highscore.util.ConstantVals;

/**
 * A REST controller class for handling all the scheduled cron job requests.
 * 
 * Note the check on 'X-Appengine-Cron' request header.
 * According to Google Cloud, the presence of this request header guarantees
 * that this request was originated from Google Cloud Scheduler.
 * The logic will run only when this request header exists.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("cronjob")
public class CronJobController {
	
	static Logger logger = LoggerFactory.getLogger(CronJobController.class);
	static Gson gson = new Gson();
	
	@GetMapping("/cleardaily")
	public void clearDaily(@RequestHeader("X-Appengine-Cron") String xAppEngineCron) {
		if(xAppEngineCron.equals("true")) {
			CronJobDao.clearGameHighScore(ConstantVals.TYPE_DAILY);
		}else {
			logger.info("Header 'X-Appengine-Cron' missing for 'cleardaily'.");
		}
	}
	
	@GetMapping("/clearweekly")
	public void clearWeekly(@RequestHeader("X-Appengine-Cron") String xAppEngineCron) {
		if(xAppEngineCron.equals("true")) {
			CronJobDao.clearGameHighScore(ConstantVals.TYPE_WEEKLY);
		}else {
			logger.info("Header 'X-Appengine-Cron' missing for 'clearweekly'.");
		}
	}
	
	@GetMapping("/clearmonthly")
	public void clearMonthly(@RequestHeader("X-Appengine-Cron") String xAppEngineCron) {
		if(xAppEngineCron.equals("true")) {
			CronJobDao.clearGameHighScore(ConstantVals.TYPE_MONTHLY);
		}else {
			logger.info("Header 'X-Appengine-Cron' missing for 'clearmonthly'.");
		}
	}
	
}
