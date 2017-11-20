package tryout.zBasic.persistence.hibernate;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import basic.zBasic.ReflectCodeZZZ;
import use.thm.persistence.hibernate.HibernateContextProviderSingletonTHM;

/**
 * Die SessionFactory Klasse, die über JNDI angebunden worden ist.
 * Siehe context.xml des Servers und web.xml Dokument des jeweiligen WebService.
 * Hier wird dann eine neue SessionFactory gebaut.
 * Dabei wird für die Datenbank die URL der RessourceLink Konfiguration nach "hibernate.connection.url" kopiert.
 * Somit hat man dann als Datenbank ggfs. eine andere als in der Standalone Applikation definiert wurde.  
 * @author lindhaueradmin
 *
 */
public class HibernateSessionFactoryTomcatFactory implements ObjectFactory{
//FGL: 20171112: Das funktioniert. Die Methoden des Interface DataSource braucht es aber scheinbar nicht, weil ich die Ressource als <ResourceLink> in der Context.xml eingebunden habe:
                 // public class HibernateSessionFactoryTomcatFactory implements DataSource, ObjectFactory{
//Merke: Das ist keine Lösung: Die Tomcat Klasse wird hier nicht gefunden:
	            //public class HibernateSessionFactoryTomcatFactory extends org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory{
		
	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
			Hashtable<?, ?> environment) throws Exception {

		SessionFactory objReturn = null;  
		 try{
			  
			  //Fehlermedlung: org.hibernate.HibernateException: Connection cannot be null when 'hibernate.dialect' not set
			  //Lösungsanasatz 1: SETZE TESTWEISE ALLES MÖGLICHE, das funktioniert.			 
//			  Configuration cfgNew = new Configuration();
//			  cfgNew.configure("hibernate.cfg.xml"); //Ohne das HibernateContextprovider-Objekt funktionierrtt das auch ohne Endlosschleife.
//			  ServiceRegistry sr = new ServiceRegistryBuilder().applySettings(cfgNew.getProperties()).buildServiceRegistry();
//           SessionFactory sf = cfgNew.buildSessionFactory(sr);
				
		  //Lösungsansatz 2: Kann man hier ggfs. aus meiner HibernateContextProviderZZZ - als Singleton - die Konfiguration verwenden?
			 HibernateContextProviderSingletonTHM objContextHibernate = HibernateContextProviderSingletonTHM.getInstance();				 
			 if(objContextHibernate.hasSessionFactory_open()){
				 System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Es gibt eine offene SessionFactory.");
				 objReturn = (SessionFactoryImpl) objContextHibernate.getSessionFactory();
			 }else{
				 System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Es gibt keine offene SessionFactory.");
				 Configuration cfgNew = objContextHibernate.getConfiguration();
				 
				 if(objContextHibernate.hasSessionFactory()){
					 System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Es gibt eine geschlossene SessionFactory.");
					 
					 //SessionFactory vorhanden, aber nicht geöffnet... d.h. die Configuration nicht neu bauen, sondern nur die SessionFactory					 
					 //Grund Fehler: javax.naming.NamingException: Duplicate collection role mapping use.thm.persistence.model.HexCell.objbagTile
					 //Merke: cfg.configure() macht immer "configuring from ressource /hibernate.cfg.xml"
					 ServiceRegistry sr = new ServiceRegistryBuilder().applySettings(cfgNew.getProperties()).buildServiceRegistry();					 
					 SessionFactory sf = cfgNew.buildSessionFactory(sr); 
					 objContextHibernate.setSessionFactoryWithNewSession((SessionFactoryImpl)sf);//wichtig, sonst wird immer wieder eine neue SessionFactory geholt. Z.B. im Dao: Diese neue SessionFactory wäre dann für die J2SE Standard Applikation gedacht, was aber bei JNDI nicht gewünscht wäre.
				     objReturn = (SessionFactoryImpl) sf;
				     
				     //Problem Fehler... es soll schon etwas registriert sein.
				     //Lösungsansatz: Wiederverwendung der ServiceRegistry...
//				     SessionFactoryImpl sfOld = (SessionFactoryImpl) objContextHibernate.getSessionFactory();
//				     ServiceRegistry serviceRegistry = sfOld.getServiceRegistry();
//				     SessionFactory sf = cfgNew.buildSessionFactory(serviceRegistry); 
//				     objContextHibernate.setSessionFactory((SessionFactoryImpl)sf);//wichtig, sonst wird immer wieder eine neue SessionFactory geholt. Z.B. im Dao: Diese neue SessionFactory wäre dann für die J2SE Standard Applikation gedacht, was aber bei JNDI nicht gewünscht wäre.
//				     objReturn = (SessionFactoryImpl) sf;
				     
				 }else{
					 System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": Es gibt überhaupt keine SessionFactory.");
					 
					 //IDEE: Nimm die übergebene Konfiguration. Diese kommt aus dem Context.xml des Servers und übernimm diese Werte in die bisherige Configuration.
					 Enumeration addrs = ((Reference)(obj)).getAll(); 
					 Configuration cfgTemp = new Configuration();
					 RefAddr addr =null;
					 while(addrs.hasMoreElements()){  
				            addr = (RefAddr) addrs.nextElement();  			          			            
				            String sKey = addr.getType();
				            Object objValue = addr.getContent();
				            
				            if(objValue!=null){
				            	if(objValue instanceof String){
				            		String sValue = (String) objValue;
				            		cfgTemp.setProperty(sKey, sValue);
				            	}
				            }
					 }
					 
				     //Diese durchgehen. und in neue kopieren
					 Properties objProperties = cfgTemp.getProperties(); 
					 //Aber das ist java 1.7...  Enumeration<String> enums = (Enumeration<String>) objProperties.propertyNames();
					 //Dann lieber herkömmlich durchgehen.
					 for(Object objProperty : objProperties.keySet()){
						 String sKey = (String) objProperty;
						 String sValue = objProperties.getProperty(sKey);					 
						 cfgNew.setProperty(sKey, sValue);
						 if(sKey.equalsIgnoreCase("url")){
							 System.out.println(ReflectCodeZZZ.getPositionCurrent()+ ": Verwende als Url aus Argumenten übergeben '" + sValue + "'");
						 }
						 if(sKey.equalsIgnoreCase("hibernate.connection.url")){
							 System.out.println(ReflectCodeZZZ.getPositionCurrent()+ ": Verwende als  hibernate.connection.url '" + cfgNew.getProperty(" hibernate.connection.url") + "'");
						 }						 
					 }
			 				 								 
					 System.out.println(ReflectCodeZZZ.getPositionCurrent()+ ": Verwende als Url: '" + cfgNew.getProperty("url") + "'");
					 System.out.println(ReflectCodeZZZ.getPositionCurrent()+ ": Verwende als hibernate.connection.url noch: '" + cfgNew.getProperty(" hibernate.connection.url") + "'");
					 
					 //Durch die Eingeschaften aus der Context.xml Datei, wird hibernate.connection.url nicht definiert. Sondern nur URL.
					 //Man muss daher an dieser Stelle die konfigurierte URL nehmen und damit die für eine Standalone Konfigurierte Datei überschreiben.
					 cfgNew.setProperty("hibernate.connection.url",  cfgNew.getProperty("url"));
					 				 
					 //So wird bisher die SessionFactory geholt. Merke: Das ist anlalog aus meiner HibernateContextProviderZZZ - Klasse
	                  //Methode: public SessionFactoryImpl getSessionFactory(){				
					 //Hier nicht verwenden: SessionFactory sf = objContextHibernate.getSessionFactory();//ACHTUNG: ENDLOSSCHLEIFENGEFAHR, DARIN RUFT BUILD FACTORY WIEDER DIESE FACTORY AUF.
					 cfgNew.configure(); //ist wahrscheinlich nicht notwendig, nur wenn man als Argument eine xml.cfg - Datei angibt.				 
					 ServiceRegistry sr = new ServiceRegistryBuilder().applySettings(cfgNew.getProperties()).buildServiceRegistry();
					 SessionFactory sf = cfgNew.buildSessionFactory(sr); 
					 
					 objContextHibernate.setSessionFactoryWithNewSession((SessionFactoryImpl)sf);//wichtig, sonst wird immer wieder eine neue SessionFactory geholt. Z.B. im Dao: Diese neue SessionFactory wäre dann für die J2SE Standard Applikation gedacht, was aber bei JNDI nicht gewünscht wäre.
					 objReturn = (SessionFactoryImpl) sf;	
					 
				}							
			 }
		}catch(Exception ex){  
	        //throw new javax.naming.NamingException(ex.getMessage());
			ex.printStackTrace();
			throw ex;
	     }  
	     return objReturn;
		 		 
		 /* original aus dem Beispiel:
		  * https://stackoverflow.com/questions/2475950/hibernate-sessionfactory-how-to-configure-jndi-in-tomcat
		  
	      RefAddr addr = null;  
	        
	      try{  
	         Enumeration addrs = ((Reference)(obj)).getAll();  
	              
	         while(addrs.hasMoreElements()){  
	            addr = (RefAddr) addrs.nextElement();  
	            if("configuration".equals((String)(addr.getType()))){  
	               sessionFactory = (new Configuration())  
	                    .configure((String)addr.getContent()).buildSessionFactory();  
	            }  
	         }  
	      }catch(Exception ex){  
	         throw new javax.naming.NamingException(ex.getMessage());  
	      }  
	      return sessionFactory; 
	  	*/
	}  
	}  
