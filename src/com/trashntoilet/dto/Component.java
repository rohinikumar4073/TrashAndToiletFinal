package com.trashntoilet.dto;

public class Component {
    private double distance;
	private  String results;
	private  double latitude;
	private  double longitude;
	private  String iconLink;
	private  String id;
	private  String name;
	private  String openingHours;
    private String rankings;
	public boolean isToilet() {
		return toilet;
	}
	public void setToilet(boolean toilet) {
		this.toilet = toilet;
	}
	public boolean isTrash() {
		return trash;
	}
	public void setTrash(boolean trash) {
		this.trash = trash;
	}
	private  String types;
	private  String reference;
	private  Photo photos;
	private  boolean toilet;
	private boolean trash;
	private Boolean openNow;
	public Boolean getOpenNow() {
		return openNow;
	}
	public String getResults() {
		return results;
	}
	public void setResults(String results) {
		this.results = results;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getIconLink() {
		return iconLink;
	}
	public void setIconLink(String iconLink) {
		this.iconLink = iconLink;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOpeningHours() {
		return openingHours;
	}
	public void setOpeningHours(String openingHours) {
		this.openingHours = openingHours;
	}
	public String getTypes() {
		return types;
	}
	public void setTypes(String types) {
		this.types = types;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public Photo getPhotos() {
		return photos;
	}
	public void setPhotos(Photo photos) {
		this.photos = photos;
	}
	public void setOpenNow(Boolean openNow) {
		this.openNow = openNow;
	}
	public String getRankings() {
		return rankings;
	}
	public void setRankings(String rankings) {
		this.rankings = rankings;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	

}
