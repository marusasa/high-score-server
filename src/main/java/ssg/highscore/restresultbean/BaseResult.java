package ssg.highscore.restresultbean;

import java.util.ArrayList;
import java.util.List;

/**
 * Every result data bean should inherit this.
 */
public class BaseResult {
	
	public List<String> errors = new ArrayList<>();
	public boolean success = false;

}
