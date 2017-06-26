package use.thm.web.webservice.axis2.tile;

public class TroopArmyPojo {
	private String sUniquename;
	private String sType;
	private Integer intPlayer;
	
	public String getUniquename(){
		return this.sUniquename;
	}
	public void setUniquename(String s){
		this.sUniquename = s;
	}
	public String getType(){
		return this.sType;
	}
	public void setType(String sType){
		this.sType = sType;
	}
	public Integer getPlayer(){
		return this.intPlayer;
	}
	public void setPlayer(Integer intPlayer){
		this.intPlayer = intPlayer;
	}
	
	
	
	
}
