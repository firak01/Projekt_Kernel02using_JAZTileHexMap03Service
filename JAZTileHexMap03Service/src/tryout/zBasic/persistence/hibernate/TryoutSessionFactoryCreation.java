package tryout.zBasic.persistence.hibernate;

import javax.naming.Context;
import javax.naming.spi.ObjectFactory;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import java.io.Serializable;





import javax.naming.InitialContext;


import javax.naming.NamingException;

import org.hibernate.cfg.Configuration;
//import org.hibernate.classic.Session;
//FGL: Verwende diese Session:
import org.hibernate.Session;
import org.hibernate.stat.Statistics;

import tryout.zBasic.persistence.dao.TryoutGeneralDaoZZZ;
import use.thm.persistence.hibernate.HibernateContextProviderSingletonTHM;
import basic.zBasic.ExceptionZZZ;
import basic.zKernel.KernelZZZ;

public class TryoutSessionFactoryCreation {

	// Die Hauptklasse kann in diesem Web Projekt wohl nicht gefunden werden
	// Daher die Methode im nomalen WebService einbauen.
	
//	public static void main(String[] args) {		
//		TryoutSessionFactoryCreation objDebug = new TryoutSessionFactoryCreation();
//		objDebug.tryoutGetSessionFactoryAlternative();				
//	}
	
	public boolean tryoutGetSessionFactoryAlternative(){
		boolean bReturn = false;
		main:{
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
											
			    //Mache die Session und anschliessend alles wieder zu, inklusive der SessionFactory...
				Session session = null;
				if(sf!=null){
					session = sf.openSession();
					//.........
					session.clear();
					session.close();
					sf.close();
				}else{
					System.out.println("SessionFactory kann nicht erstellt werden. Tip: Alternativ den EntityManager verwenden oder ... (Need to specify class name in environment or system property, or as an applet parameter, or in an application resource file:  java.naming.factory.initial). ");
				}
				
			} catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			} catch (ExceptionZZZ e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//end main:
		return bReturn;		
	}

}
