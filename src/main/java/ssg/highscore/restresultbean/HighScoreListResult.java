package ssg.highscore.restresultbean;

import java.util.ArrayList;
import java.util.List;

import ssg.highscore.data.JsonScoreAndName;

/**
 * A result data bean used for representing a high score list.
 */
public class HighScoreListResult extends BaseResult{
	public List<JsonScoreAndName> scores = new ArrayList<>();
	public long score_to_keep = 0;
	public long bottom_score = 0;
	public long top_score = 0;
	public long score_count = 0;
	public String game_name = "";
}
