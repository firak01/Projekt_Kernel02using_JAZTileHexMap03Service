package use.thm.web.webservice.axis2.tile;

public class TileDefaulttextPojo {
	private Long lngThiskey;
	private String sShort;
	private String sLong;
	private String sDescription;
	
	public String getShorttext(){
		return this.sShort;
	}
	public void setShorttext(String s){
		this.sShort = s;
	}
	public String getLongtext(){
		return this.sLong;
	}
	public void setLongtext(String s){
		this.sLong = s;
	}
	public String getDescriptiontext(){
		return this.sDescription;
	}
	public void setDescriptiontext(String s){
		this.sDescription = s;
	}
	
	public Long getThiskey(){
		return this.lngThiskey;
	}
	public void setThiskey(Long l){
		this.lngThiskey = l;
	}
}
