package ssg.highscore.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.gson.Gson;

import ssg.highscore.data.JsonScoreAndNameList;
import ssg.highscore.entitydef.Board;

/**
 * A class containing DAO methods for scheduled CRON jobs.
 */
public class CronJobDao {

	static Logger logger = LoggerFactory.getLogger(CronJobDao.class);
	static Gson gson = new Gson();
	
	/**
	 * This clears the high score list for all the record with a given type.
	 * 
	 * @param type - 'M', 'W' or 'D'
	 */
	public static void clearGameHighScore(String type) {	
		try {
			Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
			Query<Entity> query = Query.newEntityQueryBuilder()
				    .setKind(Board.kind)
				    .setFilter(PropertyFilter.eq(Board.field_type, type))
				    .build();
				QueryResults<Entity> results = datastore.run(query);
				while (results.hasNext()) {
					Entity game = results.next();
					var newEntity = new JsonScoreAndNameList();
					game = Entity.newBuilder(game)
							.set(Board.field_scores, gson.toJson(newEntity))
							.set(Board.field_top_score, 0)
							.set(Board.field_bottom_score, 0)
							.set(Board.field_updated_at, Timestamp.now())
							.set(Board.field_scores_size, 0)
							.build();
					datastore.update(game);
					logger.info("Game Entity '%s' scores cleared.".formatted(game.getKey().getName()));
				}
		}catch(Exception e) {
			logger.error("Error in clearGameHighScore. Type: %s".formatted(type),e);			
		}
	}
}
