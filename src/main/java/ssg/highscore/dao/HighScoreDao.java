package ssg.highscore.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.gson.Gson;

import ssg.highscore.data.BoardData;
import ssg.highscore.data.JsonScoreAndName;
import ssg.highscore.data.JsonScoreAndNameList;
import ssg.highscore.data.ScoreData;
import ssg.highscore.data.UserAccountData;
import ssg.highscore.entitydef.Board;
import ssg.highscore.entitydef.UserAccount;
import ssg.highscore.restresultbean.BaseResult;
import ssg.highscore.restresultbean.CreateAccountResult;
import ssg.highscore.restresultbean.CreateBoardResult;
import ssg.highscore.restresultbean.HighScoreListResult;
import ssg.highscore.util.ConstantVals;

/**
 * Class containing all the methods of REST API.
 */
public class HighScoreDao {
	
	static Logger logger = LoggerFactory.getLogger(HighScoreDao.class);
	static Gson gson = new Gson();

	/**
	 * Method for creating user account.
	 * 
	 * @param userData
	 * @return
	 */
	public static CreateAccountResult createAccount(UserAccountData userData) {
		var r = new CreateAccountResult();
		
		//check for user agreement
		if(userData.agreement() == null || !userData.agreement().equals("I agree to the user agreement")) {
			r.message = "You must agree to the user agreement to use this service.";
			return r;
		}
		
		try {
			Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
			
			KeyFactory keyFactory = datastore.newKeyFactory().setKind(UserAccount.kind);
			String id = UUID.randomUUID().toString();
			Key key = keyFactory.newKey(id);
			var entity = Entity.newBuilder(key)
					.set(UserAccount.field_user_name, userData.name())
					.set(UserAccount.field_user_email, userData.email())
					.set(UserAccount.field_agreement, userData.agreement())
					.set(UserAccount.field_email_verified, false)
					.set(UserAccount.field_created_at,Timestamp.now())
					.set(UserAccount.field_updated_at, Timestamp.now())
					.build();
			datastore.put(entity);
			r.account_id = id;
			r.success = true;
			r.message = "Save the account_id. Use it to create a high score table. Use the same account_id when creating multiple high score tables.";
		}catch(Exception e) {
			r.errors.add("Error while creating account record.");
			logger.error("Error in createaccount.",e);
		}
		return r;
	}
	
	
	/**
	 * Method for creating a high score list.
	 * 
	 * @param boardData
	 * @return
	 */
	public static CreateBoardResult createBoard(BoardData boardData) {
		var r = new CreateBoardResult();
		
		try {
			Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
			
			Key userAccountKey = datastore.newKeyFactory().setKind(UserAccount.kind).newKey(boardData.account_id());
			
			Entity userAccount = datastore.get(userAccountKey);
			
			//validation
			if(userAccount == null) {
				r.errors.add("User Account not found.");
			}
			if(boardData.name().isBlank()) {
				r.errors.add("Name missing.");
			}
			if(!(boardData.type().equals(ConstantVals.TYPE_DAILY) || 
					boardData.type().equals(ConstantVals.TYPE_WEEKLY) || 
					boardData.type().equals(ConstantVals.TYPE_MONTHLY))) {
				r.errors.add("Invalid Type. It has to be one of 'D','W','M'.");
			}
			if(r.errors.isEmpty()) {
				//no error, proceed to create record.
				KeyFactory keyFactory = datastore.newKeyFactory().setKind(Board.kind);
				String id = UUID.randomUUID().toString();
				Key key = keyFactory.newKey(id);
				String json = gson.toJson(new JsonScoreAndNameList());
				var entity = Entity.newBuilder(key)
						.set(Board.field_user_account_id, userAccountKey)
						.set(Board.field_board_name, boardData.name())
						.set(Board.field_type, boardData.type())
						.set(Board.field_score_to_keep, 10)
						.set(Board.field_top_score, 0)
						.set(Board.field_bottom_score, 0)
						.set(Board.field_scores_size, 0)
						.set(Board.field_scores,json)
						.set(Board.field_created_at,Timestamp.now())
						.set(Board.field_updated_at, Timestamp.now())
						.build();
				datastore.put(entity);
				r.success = true;
				r.highscore_id = id;
				r.account_id = boardData.account_id();
				r.message = "Save the highscore_id. Use it to add/get/clear scores.";
			}
		}catch(Exception e) {
			r.success = false;
			r.errors.add("Exception occurred.");
			logger.error("Error in createboard.",e);			
		}
		return r;
	}
	
	/**
	 * A method for clearing all the scores for a given high score list.
	 * 
	 * @param gameData
	 * @return
	 */
	public static BaseResult clearScores(BoardData gameData) {
		var r = new BaseResult();		
		try {
			Datastore datastore = DatastoreOptions.getDefaultInstance().getService();			
			Key gameKey = datastore.newKeyFactory().setKind(Board.kind).newKey(gameData.highscore_id());
			Entity game = datastore.get(gameKey);
			
			//validation
			if(game == null) {
				r.errors.add("Invalid highscore_id.");
			}else {
				//game exists. Do other validations.
				if(!game.getKey(Board.field_user_account_id).getName().equals(gameData.account_id())) {
					r.errors.add("user_account_id does not match.");
				}
			}
			if(r.errors.isEmpty()) {
				//no error, proceed to update record				
				var newEntity = new JsonScoreAndNameList();
				game = Entity.newBuilder(game)				
						.set(Board.field_scores, gson.toJson(newEntity))
						.set(Board.field_top_score, 0)
						.set(Board.field_bottom_score, 0)
						.set(Board.field_updated_at, Timestamp.now())
						.set(Board.field_scores_size, 0)
						.build();
				//save
				datastore.update(game);
				//return updated data
				r.success = true;
			}
		}catch(Exception e) {
			r.success = false;
			r.errors.add("Exception occurred.");
			logger.error("Error in slearscores.",e);			
		}
		return r;
	}
	
	/**
	 * Use this concurrent hash map to manage 'lock' for thread safe operation
	 * when adding score to a high score list. 
	 */
	private static ConcurrentHashMap<String,String> boardHashMap = new ConcurrentHashMap<>();
	
	/**
	 * A method for adding score to a give high score list.
	 * Multiple users could be adding a score at the same time. To prevent issues, 
	 * data update logic is 'synchronized'.
	 * 
	 * @param scoreData
	 * @return
	 */
	public static HighScoreListResult addScore(ScoreData scoreData) {
		var r = new HighScoreListResult();
	
		try {
			Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
			
			Key boardKey = datastore.newKeyFactory().setKind(Board.kind).newKey(scoreData.highscore_id());
			
			boardHashMap.putIfAbsent(scoreData.highscore_id(), scoreData.highscore_id());

			//Per game, allow one thread running the process.
			String syncObj = boardHashMap.get(scoreData.highscore_id()); 
			synchronized(syncObj) {
				Entity board = datastore.get(boardKey);
				
				//validation				
				if(scoreData.score() == 0) {
					r.errors.add("Score missing.");
				}
				if(scoreData.name().isBlank()) {
					r.errors.add("Name missing.");
				}else if(scoreData.name().length() > 10) {
					r.errors.add("Name longer than 10 characters.");
				}
				if(board == null) {
					r.errors.add("Invalid board id.");
				}else {
					if(r.errors.isEmpty()) {
						//no error, proceed to update record
						var scores = gson.fromJson(board.getString(Board.field_scores), JsonScoreAndNameList.class);
						boolean scoreAdded = false;
						List<JsonScoreAndName> newList = new ArrayList<>();
						
						//scores.scores is a list ordered from high to low scores.
						//This loop creates the updated score list.
						for(JsonScoreAndName s:scores.scores) {
							if(!scoreAdded && s.score < scoreData.score()) {
								var n = new JsonScoreAndName();
								n.name = scoreData.name();
								n.score = scoreData.score();
								newList.add(n);
								scoreAdded = true;						
							}
							if(newList.size() < board.getLong(Board.field_score_to_keep)) {
								newList.add(s);
							}
						}
						if(!scoreAdded && newList.size() < board.getLong(Board.field_score_to_keep)) {
							//this situation can happen for adding score for the first time.
							var n = new JsonScoreAndName();
							n.name = scoreData.name();
							n.score = scoreData.score();
							newList.add(n);
							scoreAdded = true;
						}
						if(scoreAdded) {
							var newEntity = new JsonScoreAndNameList();
							newEntity.scores = newList;
							//what is bottom score?
							long new_bottom_score = getBottomScore(newList,board.getLong(Board.field_score_to_keep));		
							board = Entity.newBuilder(board)				
									.set(Board.field_scores, gson.toJson(newEntity))
									.set(Board.field_top_score, newList.get(0).score)
									.set(Board.field_bottom_score, new_bottom_score)
									.set(Board.field_updated_at, Timestamp.now())
									.set(Board.field_scores_size, newList.size())
									.build();
							//save
							datastore.update(board);
						}
						r.success = true;
					}
				}
			}//end synchronize
			if(r.success) {
				//return updated data
				r = getScores(scoreData.highscore_id());
			}
		}catch(Exception e) {
			r.success = false;
			r.errors.add("Exception occurred.");
			logger.error("Error in addscore.",e);			
		}
		return r;
	}
	
	/**
	 * A method that returns the lowest score of the list.
	 * If number of scores in a list is less than the max a list can have,
	 * it will return 0, since any new score can be added to the list. 
	 * 
	 * @param scores
	 * @param scoreToKeep - number of scores a high score list can have.
	 * @return
	 */
	private static long getBottomScore(List<JsonScoreAndName> scores, long scoreToKeep) {
		long new_bottom_score = 0;
		if(scores.size() < scoreToKeep) {
			//do nothing.
		}else {
			new_bottom_score = scores.get(scores.size()-1).score;
		}
		return new_bottom_score;
	}
	
	/**
	 * A method that returns the high score list for a given highscore_id
	 * 
	 * @param highscore_id
	 * @return
	 */
	public static HighScoreListResult getScores(String highscore_id) {
		var r = new HighScoreListResult();
		try {
			Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
			Key boardKey = datastore.newKeyFactory().setKind(Board.kind).newKey(highscore_id);
			Entity board = datastore.get(boardKey);
			
			if(board == null) {
				r.errors.add("Invalid board id.");
				r.success = false;
			}else {			
				var scores = gson.fromJson(board.getString(Board.field_scores), JsonScoreAndNameList.class);
				r.scores = scores.scores;
				r.game_name = board.getString(Board.field_board_name);
				long new_bottom_score = getBottomScore(scores.scores,board.getLong(Board.field_score_to_keep));
				r.bottom_score = new_bottom_score;
				r.top_score = board.getLong(Board.field_top_score);
				r.score_count = board.getLong(Board.field_scores_size);
				r.score_to_keep = board.getLong(Board.field_score_to_keep);
				r.success = true;
			}
		} catch(Exception e) {
			r.success = false;
			r.errors.add("Exception occurred in getScores. Check server log..");
			logger.error("Error in getScores.",e);
		}
		return r;
	}
}
