package ssg.highscore.restcontroller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import ssg.highscore.dao.HighScoreDao;
import ssg.highscore.data.BoardData;
import ssg.highscore.data.ScoreData;
import ssg.highscore.data.UserAccountData;
import ssg.highscore.restresultbean.BaseResult;
import ssg.highscore.restresultbean.CreateAccountResult;
import ssg.highscore.restresultbean.CreateBoardResult;
import ssg.highscore.restresultbean.HighScoreListResult;

/**
 * A REST Controller class for all the APIs.
 */
@CrossOrigin
@RestController
@RequestMapping("api/v1")
public class HighScoreController {
	
	static Logger logger = LoggerFactory.getLogger(HighScoreController.class);
	static Gson gson = new Gson();

	@PostMapping("/create-account")
	public CreateAccountResult createAccount(@RequestBody UserAccountData userData) {
		return HighScoreDao.createAccount(userData);
	}
		
	@PostMapping("/create-highscore")
	public CreateBoardResult createBoard(@RequestBody BoardData boardData) {
		return HighScoreDao.createBoard(boardData);
	}
	
	@PostMapping("/clear-scores") 
	public BaseResult clearScores(@RequestBody BoardData gameData) {
		return HighScoreDao.clearScores(gameData);
	}
	
	@PostMapping("/add-score")
	public HighScoreListResult addScore(@RequestBody ScoreData scoreData) {
		return HighScoreDao.addScore(scoreData);
	}
	
	@GetMapping("/scores/{id}")
	public HighScoreListResult getScores(@PathVariable String id) {
		return HighScoreDao.getScores(id);
	}
	
}
