package ssg.highscore.entitydef;

/**
 * A class representing a 'UserAccount' record in Datastore.
 */
public class UserAccount {
	
	private UserAccount() {};
	
	public final static String kind = "UserAccount";
	
	public final static String field_user_name = "user_name";
	public final static String field_user_email = "user_email";
	public final static String field_agreement = "agreement";
	public final static String field_email_verified = "email_verified";
	public final static String field_created_at = "created_at";
	public final static String field_updated_at = "updated_at";
	

}
