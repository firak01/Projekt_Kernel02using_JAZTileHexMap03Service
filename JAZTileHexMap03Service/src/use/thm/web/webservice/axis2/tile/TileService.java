package use.thm.web.webservice.axis2.tile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.KernelZZZ;
import tryout.zBasic.persistence.hibernate.TryoutSessionFactoryCreation;
import use.thm.persistence.dao.TileDefaulttextDao;
import use.thm.persistence.dao.TroopArmyDao;
import use.thm.persistence.hibernate.HibernateContextProviderSingletonTHM;
import use.thm.persistence.model.Key;
import use.thm.persistence.model.TileDefaulttext;
import use.thm.persistence.model.TroopArmy;

public class TileService{
	public String getVersion(){
		String sVersion = "0.02";
		
		//Missbrauch dieser Methode:
		//Tryout eine SessionFactory per JNDI zu erzeugen
		TryoutSessionFactoryCreation objTryout = new TryoutSessionFactoryCreation();
		objTryout.tryoutGetSessionFactoryAlternative();
		
		return sVersion;
	}
	public String getNow(){
		Calendar cal = Calendar.getInstance();
		//Date date = new Date();
		Date date = cal.getTime();
		String sReturn = new Integer(date.getYear()).toString() + new Integer(date.getMonth()).toString() + new Integer(date.getDay()).toString();
		return sReturn;
	}
	
	/* Hier wird dann erstmalig ein Hibernate basiertes Objekt verwendet, aus einem anderen Projekt*/
	public Integer getTroopArmyCount(){
		Integer intReturn = null;					
		try {
			//TEST TESTE
			KernelZZZ objKernel = new KernelZZZ(); //Merke: Die Service Klasse selbst kann wohl nicht das KernelObjekt extenden!
			
			//funktioniert, wenn dies Datei als .jar Datei in das lib-Verzeichnis des Servers gepackt wird.
//			WebDeploymentTest objTest = new WebDeploymentTest();
//			objTest.doIt();
//			intReturn = new Integer(0);
			
			//TODO GOON:
//			//HibernateContextProviderSingletonTHM objContextHibernate = new HibernateContextProviderSingletonTHM(this.getKernelObject());
			HibernateContextProviderSingletonTHM objContextHibernate;
			
			objContextHibernate = HibernateContextProviderSingletonTHM.getInstance(objKernel);					
			objContextHibernate.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
			TroopArmyDao daoTroop = new TroopArmyDao(objContextHibernate);
			int iTroopCounted = daoTroop.count();
			System.out.println("Es gibt platzierte Armeen: " + iTroopCounted);
			
			intReturn = new Integer(iTroopCounted);
			
		} catch (ExceptionZZZ e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return intReturn;
				
	}
	
	
	/* Hier wird dann erstmalig eine eigens dafür erstellte HQL Abfrage ausgeführt und das Ergebnis soll zurückgeliefert werden. */
	public List<TroopArmyPojo> getTroopArmiesByHexCell(String sMap, String sX, String sY){
		List<TroopArmyPojo> listReturn = null;	
		try {
			KernelZZZ objKernel = new KernelZZZ(); //Merke: Die Service Klasse selbst kann wohl nicht das KernelObjekt extenden!
			
			//TODO GOON:
//			//HibernateContextProviderSingletonTHM objContextHibernate = new HibernateContextProviderSingletonTHM(this.getKernelObject());
			HibernateContextProviderSingletonTHM objContextHibernate;
			
			objContextHibernate = HibernateContextProviderSingletonTHM.getInstance(objKernel);					
			objContextHibernate.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
			TroopArmyDao daoTroop = new TroopArmyDao(objContextHibernate);
			List<TroopArmy>listTroopArmy = daoTroop.searchTileCollectionByHexCell(sMap, sX, sY);//.searchTileIdCollectionByHexCell(sMap, sX, sY);
			System.out.println("Es gibt auf der Karte '" + sMap + " an X/Y (" + sX + "/" + sY + ") platzierte Armeen: " + listTroopArmy.size());
			
			if(listTroopArmy.size()>=1){
				listReturn = new ArrayList<TroopArmyPojo>();
			}
			for(TroopArmy objTroop : listTroopArmy){
				TroopArmyPojo objPojo = new TroopArmyPojo();
				objPojo.setUniquename(objTroop.getUniquename());
				objPojo.setPlayer(new Integer(objTroop.getPlayer()));
				objPojo.setType(objTroop.getTroopType());
				listReturn.add(objPojo);
			}
			
		} catch (ExceptionZZZ e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listReturn;
				
	}
	
	/* Hier wird per DAO der Defaulttext für eine Army geholt. Dabei wird der (momentan noch) der Thiskey direkt angegeben.
	 * TODO GOON 20171115: Das soll eigentlich über eine noch zu erstellende Armeetyp - Tabelle passieren, in welcher der thiskey abgelegt ist.
	 */
	public TileDefaulttextPojo getTileDefaulttextByThiskey(Long lngThiskey){
		TileDefaulttextPojo objReturn = null;	
		try {
			KernelZZZ objKernel = new KernelZZZ(); //Merke: Die Service Klasse selbst kann wohl nicht das KernelObjekt extenden!
			
			//TODO GOON:
//			//HibernateContextProviderSingletonTHM objContextHibernate = new HibernateContextProviderSingletonTHM(this.getKernelObject());
			HibernateContextProviderSingletonTHM objContextHibernate;
			
			objContextHibernate = HibernateContextProviderSingletonTHM.getInstance(objKernel);					
			objContextHibernate.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
			TileDefaulttextDao daoText = new TileDefaulttextDao(objContextHibernate);
			Key objKey = daoText.searchThiskey(lngThiskey);
			if(objKey==null){
				System.out.println("Thiskey='"+lngThiskey.toString()+"' NICHT gefunden.");
			}else{
				TileDefaulttext objValue = (TileDefaulttext) objKey;
				
				String sDescription = objValue.getDescription();
				String sShorttext = objValue.getShorttext();				
				String sLongtext = objValue.getLongtext();
				
				System.out.println("Thiskey='"+lngThiskey.toString()+"' gefunden. ("+sShorttext+"|"+sLongtext+"|"+sDescription+")");
				
				//### Übergib nun die gefundenen Werte an das POJO - Objekt
				objReturn = new TileDefaulttextPojo();
				objReturn.setThiskey(lngThiskey);
				objReturn.setShorttext(sShorttext);
				objReturn.setLongtext(sLongtext);
				objReturn.setDescriptiontext(sDescription);
			}													
		} catch (ExceptionZZZ e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objReturn;
				
	}
}
