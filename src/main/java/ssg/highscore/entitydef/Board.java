package ssg.highscore.entitydef;

/**
 * A class representing a 'Board' record in Datastore.
 * A 'Board' represents one high score list.
 */
public class Board {
	
	private Board() {};
	
	public final static String kind = "Board";
	
	public final static String field_user_account_id = "user_account_Id";
	public final static String field_board_name = "board_name";
	public final static String field_score_to_keep = "score_to_keep";
	public final static String field_top_score = "top_score";
	public final static String field_bottom_score = "bottom_score";
	public final static String field_scores_size = "scores_size";
	public final static String field_scores = "scores";
	public final static String field_type = "type";
	public final static String field_created_at = "created_at";
	public final static String field_updated_at = "updated_at";
	

}
