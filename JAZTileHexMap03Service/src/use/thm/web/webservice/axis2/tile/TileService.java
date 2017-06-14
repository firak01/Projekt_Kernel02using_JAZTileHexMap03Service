package use.thm.web.webservice.axis2.tile;

import java.util.Calendar;
import java.util.Date;

import basic.zBasic.ExceptionZZZ;
import basic.zKernel.KernelZZZ;
import use.thm.persistence.dao.TroopArmyDao;
import use.thm.persistence.hibernate.HibernateContextProviderSingletonTHM;

public class TileService{
	public String getVersion(){
		String sVersion = "0.01";
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
}
