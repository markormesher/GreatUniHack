package uk.co.markormesher.guh.objects;

public class Player {

	private String playerId;
	private String photoUrl;
	private String role;

	public Player(String playerId, String photoUrl, String role) {
		this.playerId = playerId;
		this.photoUrl = photoUrl;
		this.role = role;
	}

	public String getPlayerId() {
		return playerId;
	}

	public String getPhotoUrl() {
		return photoUrl == null || photoUrl.equals("") || photoUrl.equals("null") ? "" : photoUrl;
	}

	public String getRole() {
		return role;
	}
}
