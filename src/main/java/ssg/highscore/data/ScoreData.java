package ssg.highscore.data;

/**
 * Used when adding a new score to a high score list.
 */
public record ScoreData(String highscore_id, String name, long score) {

}
