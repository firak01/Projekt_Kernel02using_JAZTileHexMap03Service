package use.thm.web.webservice.axis2.tile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionFactoryImpl;

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
		String sVersion = "0.05";
		
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
			//funktioniert, wenn dies Datei als .jar Datei in das lib-Verzeichnis des Servers gepackt wird.
//			WebDeploymentTest objTest = new WebDeploymentTest();
//			objTest.doIt();
//			intReturn = new Integer(0);
			
			KernelZZZ objKernel = new KernelZZZ(); //Merke: Die Service Klasse selbst kann wohl nicht das KernelObjekt extenden!			
			 HibernateContextProviderSingletonTHM objContextHibernate = HibernateContextProviderSingletonTHM.getInstance(objKernel);					
			objContextHibernate.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
			//TODO GOON 20171119: Bisher wird die SessionFactory und die daraus resultierende Session auch im WebService aufgebaut wie für eine J2SE Standalone Anwendung.
			//                                     Nun wird die SessionFactory per JNDI geholt. Anschliessend an meinen Context...Provider übergeben.
			//                                     So wird eine Session nun mit den spezifischen Server Einstellungen / Angaben gemacht.
			
			//Dafür ist es wichtig für JNDI: Die SessionFactory an den Context zu binden
			//objContextHibernate.getConfiguration().setProperty("hibernate.session_factory_name", "tryout.zBasic.persistence.hibernate.HibernateSessionFactoryTomcatFactory");
//			objContextHibernate.getConfiguration().setProperty("hibernate.session_factory_name","hibernate.session-factory.ServicePortal");	//derselbe Name wird dann in jndiContext.lookup(...) gebraucht.
//			objContextHibernate.getConfiguration().setProperty("hibernate.connection.datasource", "java:comp/env/jdbc/ServicePortal");//siehe context.xml <RessourceLink> Tag.  //Merke comp/env ist fester Bestandteil für alle JNDI Pfade
			
			
			Context jndiContext = (Context) new InitialContext();		
			//SessionFactory sf = (SessionFactory) jndiContext.lookup("hibernate.session-factory.ServicePortal");

			//Mein Ansatz: Verwende eine eigene SessionFactory und nimm die erstellte Konfiguration (aus HibernateContextProviderTHM) weiterhin und überschreibe diese ggfs. aus der Konfiguration.
			//                   Die hier erzeugte SessionFactory wird dann in das ContextHibernateProviderTHM-Objekt gespeichert. Dadurch wird die SessionFactory nur einmal erzeugt.
			//Merke: Damit diese Resource bekannt ist im Web Service, muss er neu gebaut werden. Nur dann ist die web.xml aktuell genug.
			//Merke: java:comp/env/ ist der JNDI "Basis" Pfad, der vorangestellt werden muss. Das ist also falsch: //SessionFactory sf = (SessionFactory) jndiContext.lookup("java:jdbc/ServicePortal");
			//Merke: /jdbc/ServicePortal ist in der context.xml im <RessourceLink>-Tag definiert UND in der web.xml im <resource-env-ref>-Tag
			SessionFactory sf = (SessionFactory) jndiContext.lookup("java:comp/env/jdbc/ServicePortal");
												
			TroopArmyDao daoTroop = new TroopArmyDao(objContextHibernate);
			int iTroopCounted = daoTroop.count();
			System.out.println("Es gibt platzierte Armeen: " + iTroopCounted);
			
			intReturn = new Integer(iTroopCounted);
			
		    //Mache die Session und anschliessend alles wieder zu, inklusive der SessionFactory...
//			Session session = null;
//			if(sf!=null){
//				session = sf.openSession();
//				//.........
//				session.clear();
//				session.close();
//			    sf.close();
//			}
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
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
