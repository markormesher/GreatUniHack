package uk.co.markormesher.guh.objects;

public class Player {

	private String playerId;
	private String photoUrl;
	private String role;
	private int jobId;
	private boolean alive = true;

	public Player(String playerId, String photoUrl, String role, String jobId) {
		this.playerId = playerId;
		this.photoUrl = photoUrl;
		this.role = role;
		try {
			this.jobId = Integer.parseInt(jobId);
		} catch (NumberFormatException e) {
			this.jobId = (int) Math.round(Math.random() * 19);
		}
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

	public int getJobId() {
		return jobId;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}
}
