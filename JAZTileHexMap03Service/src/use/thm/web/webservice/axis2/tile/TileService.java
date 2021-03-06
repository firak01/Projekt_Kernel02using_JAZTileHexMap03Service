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
		String sVersion = "0.07";			
		return sVersion;
	}
	public String getNow(){
		Calendar cal = Calendar.getInstance();
		//Date date = new Date();
		Date date = cal.getTime();
		String sReturn = new Integer(date.getYear()).toString() + new Integer(date.getMonth()).toString() + new Integer(date.getDay()).toString();
		return sReturn;
	}
	
	public boolean getProofJndiResourceUsedAvailable(){
		
		//Missbrauch dieser Methode:
		//Tryout eine SessionFactory per JNDI zu erzeugen
		TryoutSessionFactoryCreation objTryout = new TryoutSessionFactoryCreation();
		boolean bReturn = objTryout.tryoutGetSessionFactoryAlternative();
		
		return bReturn;
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
			//HOLE DIE SESSIONFACTORY PER JNDI:
			//Merke: DAS FUNKTIONIERT NUR, WENN DIE ANWENDUNG IN EINEM SERVER (z.B. Tomcat läuft).
			
			KernelZZZ objKernel = new KernelZZZ(); //Merke: Die Service Klasse selbst kann wohl nicht das KernelObjekt extenden!			
			HibernateContextProviderSingletonTHM objContextHibernate = HibernateContextProviderSingletonTHM.getInstance(objKernel);					
			objContextHibernate.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
			//############################
			//MERKE: DAS IST DER WEG wei bisher die SessionFactory direkt in einer Standalone J2SE Anwendung geholt wird
			//ServiceRegistry sr = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();		    
		    //SessionFactory sf = cfg.buildSessionFactory(sr);
			//################################
		
			//xxxx Da wird mit normalen JDBC Datenbanenk als DataSource gearbeitet. Das ist mit Hibernate so nicht möglich
			//holds config elements
			//DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/yourdb");
			//Connection conn = ds.getConnection();
										
			//### Ansatz Session-Factory über die Utility Funktion zu holen, die dann in der Hibernate Konfiguration nachsieht.
			//1. Versuch: In der Hibernate Configuration definiert
			//    Fehler: SessionFactory creation failed! javax.naming.NoInitialContextException: Need to specify class name in environment or system property, or as an applet parameter, or in an application resource file:  java.naming.factory.initial
			
			//2. Versuch: In der Hibernate Configuration Erstellung per Java definiert
			//Die hier genannte SessionFactory muss tatsächlich als Klasse an der Stelle existieren.
										
			//3. Versuch:
			Context jndiContext = (Context) new InitialContext();
			//Betzemeier Original:  //SessionFactory sf = HibernateUtilByAnnotation.getHibernateUtil().getSessionFactory();
			//Betzemeier Original:  Hier wird JNDI für eine fest vorgegebeen Klasse verwendet. //SessionFactory sf = (SessionFactory) jndiContext.lookup("hibernate.session-factory.ServicePortal");
			
			//Mein Ansatz: Verwende eine eigene SessionFactory und nimm die erstellte Konfiguration (aus HibernateContextProviderTHM) weiterhin und überschreibe diese ggfs. aus der Konfiguration.
			//Merke: Damit diese Resource bekannt ist im Web Service, muss er neu gebaut werden. Nur dann ist die web.xml aktuell genug.
			//Merke: java:comp/env/ ist der JNDI "Basis" Pfad, der vorangestellt werden muss. Das ist also falsch: //SessionFactory sf = (SessionFactory) jndiContext.lookup("java:jdbc/ServicePortal");
			//Merke: /jdbc/ServicePortal ist in der context.xml im <RessourceLink>-Tag definiert UND in der web.xml im <resource-env-ref>-Tag
			SessionFactory sf = (SessionFactory) jndiContext.lookup("java:comp/env/jdbc/ServicePortal");
						
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
			
		} catch (ExceptionZZZ | NamingException e) {
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
			//HOLE DIE SESSIONFACTORY PER JNDI:
			//Merke: DAS FUNKTIONIERT NUR, WENN DIE ANWENDUNG IN EINEM SERVER (z.B. Tomcat läuft).
			
			KernelZZZ objKernel = new KernelZZZ(); //Merke: Die Service Klasse selbst kann wohl nicht das KernelObjekt extenden!				
			HibernateContextProviderSingletonTHM objContextHibernate = HibernateContextProviderSingletonTHM.getInstance(objKernel);					
			objContextHibernate.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
			//############################
			//MERKE: DAS IST DER WEG wei bisher die SessionFactory direkt in einer Standalone J2SE Anwendung geholt wird
			//ServiceRegistry sr = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();		    
		    //SessionFactory sf = cfg.buildSessionFactory(sr);
			//################################
		
			//xxxx Da wird mit normalen JDBC Datenbanenk als DataSource gearbeitet. Das ist mit Hibernate so nicht möglich
			//holds config elements
			//DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/yourdb");
			//Connection conn = ds.getConnection();
										
			//### Ansatz Session-Factory über die Utility Funktion zu holen, die dann in der Hibernate Konfiguration nachsieht.
			//1. Versuch: In der Hibernate Configuration definiert
			//    Fehler: SessionFactory creation failed! javax.naming.NoInitialContextException: Need to specify class name in environment or system property, or as an applet parameter, or in an application resource file:  java.naming.factory.initial
			
			//2. Versuch: In der Hibernate Configuration Erstellung per Java definiert
			//Die hier genannte SessionFactory muss tatsächlich als Klasse an der Stelle existieren.
										
			//3. Versuch:
			Context jndiContext = (Context) new InitialContext();
			//Betzemeier Original:  //SessionFactory sf = HibernateUtilByAnnotation.getHibernateUtil().getSessionFactory();
			//Betzemeier Original:  Hier wird JNDI für eine fest vorgegebeen Klasse verwendet. //SessionFactory sf = (SessionFactory) jndiContext.lookup("hibernate.session-factory.ServicePortal");
			
			//Mein Ansatz: Verwende eine eigene SessionFactory und nimm die erstellte Konfiguration (aus HibernateContextProviderTHM) weiterhin und überschreibe diese ggfs. aus der Konfiguration.
			//Merke: Damit diese Resource bekannt ist im Web Service, muss er neu gebaut werden. Nur dann ist die web.xml aktuell genug.
			//Merke: java:comp/env/ ist der JNDI "Basis" Pfad, der vorangestellt werden muss. Das ist also falsch: //SessionFactory sf = (SessionFactory) jndiContext.lookup("java:jdbc/ServicePortal");
			//Merke: /jdbc/ServicePortal ist in der context.xml im <RessourceLink>-Tag definiert UND in der web.xml im <resource-env-ref>-Tag
			SessionFactory sf = (SessionFactory) jndiContext.lookup("java:comp/env/jdbc/ServicePortal");
								
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
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objReturn;
				
	}
}
