package ca.uottawa.okorol.bioinf.ModuleInducerWeb.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.RegulatoryElementPWM;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryElementService;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryRegionService;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.CElegansRegRegionService;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.Explorer;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.PatserRegElementService;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.DataFormatter;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FileHandling;

@ManagedBean
@SessionScoped
public class CelegansDataBean {
	
	//page navigation
	private boolean theoryInduced = false;
	
	//data
	private String theory;
	private String posSequences;
	private String negSequences;
	private String pwms;
	private String tempDataDir;
	
	private RegulatoryRegionService regRegionService;
	private RegulatoryElementService patserRegElService;

	
	public CelegansDataBean(){
		
		try {
			
			tempDataDir = FileHandling.createTempIlpOutputDirectory();
			
			regRegionService = new CElegansRegRegionService(1);
			
			File pwmDir = new File(SystemVariables.getInstance().getString("C.elegans.PWMs.dir"));
			patserRegElService = new PatserRegElementService(pwmDir, tempDataDir);
		
			
			posSequences = formatSequences(regRegionService.getPositiveRegulatoryRegions());
			negSequences = formatSequences(regRegionService.getNegativeRegulatoryRegions());
			pwms = formatPwms(patserRegElService.getRegulatoryElementsPWMs());
			
			
		}catch (DataFormatException e) {
			
			FacesMessage message = new FacesMessage();
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			message.setSummary(e.getMessage());
			message.setDetail(e.getMessage());
			FacesContext.getCurrentInstance().addMessage("", message);			
			
			e.printStackTrace();
		}
		
	}
	
	
	public String induceTheory(){
		theoryInduced = false;
		
		try {

			Explorer explorer = new Explorer(regRegionService, patserRegElService, tempDataDir);
	
						
			String completeOutput = explorer.induceRules();
			theory = DataFormatter.extractTheoryAndPerformance(completeOutput);
		
		}catch (DataFormatException e) {
			
			FacesMessage message = new FacesMessage();
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			message.setSummary(e.getMessage());
			message.setDetail(e.getMessage());
			FacesContext.getCurrentInstance().addMessage("", message);
			
			e.printStackTrace();
		} finally {
			//FileHandling.deleteDirectory(tempDataDir);
		}
		
		
		theoryInduced = true;
					
		return ""; //stay on the same page
		
	}
	
	
	private String formatSequences(ArrayList<Feature> regulatoryRegions){
		String sequences = "";
		
		for (Iterator<Feature> iterator = regulatoryRegions.iterator(); iterator.hasNext();) {
			Feature regRegion = (Feature) iterator.next();
			sequences = sequences + "\n> " + regRegion.getId() + "\n" + regRegion.getSequence().toUpperCase() + "\n";
			
		}
		
		return sequences;
	}
	
	private String formatPwms(ArrayList<RegulatoryElementPWM> pwms){
		String pwmSeq = "";
		
		for (Iterator<RegulatoryElementPWM> iterator = pwms.iterator(); iterator.hasNext();) {
			RegulatoryElementPWM pwmEl = (RegulatoryElementPWM) iterator.next();
			
			pwmSeq = pwmSeq + "\n> " + pwmEl.getName()+ "\n" + pwmEl.getPwmString() + "\n";
			
		}
		
		return pwmSeq;
	}



	public String getPosSequences() {
		return posSequences;
	}
	public void setPosSequences(String posSequences) {
		this.posSequences = posSequences;
	}

	public String getNegSequences() {
		return negSequences;
	}
	public void setNegSequences(String negSequences) {
		this.negSequences = negSequences;
	}

	public String getPwms() {
		return pwms;
	}
	public void setPwms(String pwms) {
		this.pwms = pwms;
	}

	public String getTheory() {
		return theory;
	}
	public void setTheory(String theory) {
		this.theory = theory;
	}
	
	public boolean getTheoryInduced(){
		return theoryInduced;
	}
	
}

