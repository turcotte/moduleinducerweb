package ca.uottawa.okorol.bioinf.ModuleInducerWeb.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.sun.faces.config.FacesConfigInfo;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.RegulatoryElementPWM;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryElementService;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.CElegansRegRegionService;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.CustomDataRegRegionService;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.Explorer;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.MemeSuiteRegElementService;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.PatserRegElementService;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.DataFormatter;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FeaturesTools;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FileHandling;

@ManagedBean
@SessionScoped
public class CustomDataBean {
	
	//page navigation
	private boolean theoryInduced = false;
	private String negExType = NEG_EX_SHOW_RADIO_VALUE;
	private String bioMarkersType = "useDreme";
//	private String customNegExDivVisibility = SHOW;
	private String customNegExDivVisibility = getCustomNegExDivVisibility();
	private String customPSSMsDivDivVisibility = getCustomPSSMsDivDivVisibility();
	

	//Page constants
	private static String SHOW = "block";
	private static String HIDE = "none";
	private static String NEG_EX_SHOW_RADIO_VALUE = "customNegEx";
	private static String BIO_MARKERS_SHOW_RADIO_VALUE = "customPSSMs";
	

	//data
	private String theory;
	private String posSequences;
	private String negSequences;
	private String pwms;
	
	//Sample C.elegans data
	private String cElPosSeqs; 
	private String cElNegSeqs; 
	private String cElPwms; 
	
//	private UploadedFile posFile;
//	private UploadedFile negFile;
//	private UploadedFile pwmFile;
	
	
	
	public CustomDataBean(){
		

		System.out.println("The index.xhtml is running at this location: " 
				+ FacesContext.getCurrentInstance().getExternalContext().getRealPath(""));
		
		
		//initialize example data		
		try {
			
			CElegansRegRegionService regRegionService = new CElegansRegRegionService(1);
			
			File pwmDir = new File(SystemVariables.getInstance().getString("C.elegans.PWMs.dir"));
			PatserRegElementService patserRegElService = new PatserRegElementService(pwmDir, "");
		
			
			cElPosSeqs = formatSequences(regRegionService.getPositiveRegulatoryRegions());
			cElNegSeqs = formatSequences(regRegionService.getNegativeRegulatoryRegions());
			cElPwms = formatPwms(patserRegElService.getRegulatoryElementsPWMs());
			
			//TODO: for testing
//			posSequences = cElPosSeqs;
//			negSequences = cElNegSeqs;
//			pwms = cElPwms;
			
			
			
		}catch (DataFormatException e) {
			
			//FileHandling.deleteDirectory(theoryOutputDir);
			FacesMessage message = new FacesMessage();
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			message.setSummary(e.getMessage());
			message.setDetail(e.getMessage());
			FacesContext.getCurrentInstance().addMessage("Unable to load example data.", message);			
			
			e.printStackTrace();
			
			
		}
		
	}
	
	public String induceTheory() throws IOException{

//		theoryInduced = false;
		
		String theoryOutputDir = "";
		String resultPage = "";
		
		try {

			//Properties prop = System.getProperties();
			//System.out.println(prop.toString());

			
			// Directory to hold all the job temp files
			theoryOutputDir = FileHandling.createTempWebIlpOutputDirectory();
			System.out.println("Writing new job to directory: " + theoryOutputDir);
			//FileHandling.writeLogFile("sysInfo.txt", "\n All vars: \n\n" + prop.toString() + "\n\n Deploy dir: " + deployDir);
			
			int lenToRemove = theoryOutputDir.indexOf(SystemVariables.getInstance().getString("job.tmp.output.dir.prefix"));
			String theoryOutputDirName =  theoryOutputDir.substring(lenToRemove);
			
			resultPage = "work/" + theoryOutputDirName + FileHandling.createPreliminaryResultsWebPage(theoryOutputDir);
			
			

			CustomDataRegRegionService customRegRegService;
			/************ Positive Sequences ****/
			if (null == posSequences || posSequences.isEmpty()){
				throw new DataFormatException("Please specify experiment sequences.");
			}
			ArrayList<Feature> posRegRegions = CustomDataRegRegionService.formatRegulatoryRegions(posSequences);
			
			/************ Negative Sequences ****/
			ArrayList<Feature> negRegRegions;
			
			if ("mc0".equals(negExType)){
				negRegRegions = FeaturesTools.generateSimulatedMC0RegulatoryRegions(posRegRegions, 1, "mc0_");
				
			} else if ("mc1".equals(negExType)) {
				negRegRegions = FeaturesTools.generateSimulatedMC1RegulatoryRegions(posRegRegions, 1, "mc1_");
				
			} else {
				if (null == negSequences || negSequences.isEmpty()){
					throw new DataFormatException("Please specify control sequences.");
				}
				
				negRegRegions = CustomDataRegRegionService.formatRegulatoryRegions(negSequences);
			}
			
			customRegRegService = new CustomDataRegRegionService(posRegRegions, negRegRegions);

			
			/************ Bio Markers ****/
			RegulatoryElementService regElService;
			
			if ("customPSSMs".equals(bioMarkersType)){
				if (null == pwms  || pwms.isEmpty()){
					throw new DataFormatException("Please specify biological markers (PSSMs).");
				}
				
				regElService = new PatserRegElementService(pwms, theoryOutputDir);
				
			} else { 	// use DREME
				regElService = new MemeSuiteRegElementService(theoryOutputDir);
				
			}
			
			/************ Find and load up everything (sequences, biomarkers) ****/
			Explorer explorer = new Explorer(customRegRegService, regElService, theoryOutputDir);
			
			/************ Create ilp files and induce ****/
			ILPRunner ilpRunner = new ILPRunner(explorer); 
			Thread myThread = new Thread(ilpRunner);
			myThread.start();
			
			// *** Old way, with waiting for the results
			//String completeOutput = explorer.induceRules();
			//theory = DataFormatter.extractTheoryAndPerformance(completeOutput);
			
			//FileHandling.deleteDirectory(new File(tmpPwmDirName));  /should be done by a script
		
		}catch (DataFormatException e) {
			
			FileHandling.deleteDirectory(theoryOutputDir);
			
			FacesMessage message = new FacesMessage();
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			message.setSummary(e.getMessage());
			message.setDetail(e.getMessage());
			FacesContext.getCurrentInstance().addMessage("", message);
			
			e.printStackTrace();
			
			return ""; //stay on the same page
			
		} catch (IOException e) {
			
			FileHandling.deleteDirectory(theoryOutputDir);
			
			FacesMessage message = new FacesMessage();
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			message.setSummary(e.getMessage());
			message.setDetail(e.getMessage());
			FacesContext.getCurrentInstance().addMessage("", message);
			
			e.printStackTrace();
			
			return ""; //stay on the same page
		}
		
		
//		String resultPath =  workPath + "index.html";
//		System.out.println("Trying to forward result to this location: " + resultPath);

		FacesContext.getCurrentInstance().getExternalContext().redirect(resultPage);
		
		return "";

	}
	
	
	public String loadCelegansData(){
		theoryInduced = false;
		negExType = NEG_EX_SHOW_RADIO_VALUE;
		bioMarkersType = BIO_MARKERS_SHOW_RADIO_VALUE;
		customNegExDivVisibility = getCustomNegExDivVisibility();
		customPSSMsDivDivVisibility = getCustomPSSMsDivDivVisibility();

		theory = "";
		posSequences = cElPosSeqs;
		negSequences = cElNegSeqs;
		pwms = cElPwms;
		
		return "";
		
	}
	
	
	public String clearData(){
		theory = "";
		posSequences = "";
		negSequences = "";
		pwms = "";
		
		theoryInduced = false;
		negExType = NEG_EX_SHOW_RADIO_VALUE;
		bioMarkersType = "useDreme";
		customNegExDivVisibility = getCustomNegExDivVisibility();
		customPSSMsDivDivVisibility = getCustomPSSMsDivDivVisibility();
		
		return "";
	}
	
	
	
	/***********  Thread class   ***********/

	
	class ILPRunner implements Runnable{
		private Explorer explorer;
		
		public ILPRunner(Explorer explorer){
			this.explorer = explorer;
		}
		
		public void run(){
			try {
				explorer.induceRules();
			} catch (DataFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}


	/***********  Validators   ***********/

	public void validateBioSequence(FacesContext context, UIComponent toValidate, Object value) throws ValidatorException{
		
		String bioSequence = (String) value;
		
		String fastaNameSeqRegEx = ">[ \t]*\\w+.*";
		String oneFastaSection = "\\s*" + fastaNameSeqRegEx + "\\s*\n+" + "[ACGTacgt]+" ;
		String fastaSeqRegEx = "(" + oneFastaSection + "[ \t\r]*\n" + ")*" + oneFastaSection + "\\s*" ;
		
		if (!bioSequence.matches(fastaSeqRegEx)){
			FacesMessage msg = new FacesMessage("Supplied sequences are not in the correct format.\n " +
					"Make sure each sequence has a name, which precedes the sequence and starts with \"> \".\n" +
					"Also verify that each sequece only contains {A,C,G,T} or {a,c,g,t} letters.\n " +
					"For a format example, see C.elegans data.");
			throw new ValidatorException(msg);
		}
		
	}
	
	public void validatePwmSequence(FacesContext context, UIComponent toValidate, Object value) throws ValidatorException{
		String pwmSequence = (String) value;
		
		String fastaNameSeqRegEx = ">[ \t]*\\w+.*";
		
		//for A | 2 3... types
		String pwmRegEx1 = "[Aa][ \t]*\\|([ \t]*\\d+)+[ \t]*\\s*\n" +
		"[Cc][ \t]*\\|([ \t]*\\d+)+[ \t]*\\s*\n" +
		"[Gg][ \t]*\\|([ \t]*\\d+)+[ \t]*\\s*\n" +
		"[Tt][ \t]*\\|([ \t]*\\d+)+[ \t]*\\s*";
		
		//for A [1 3]... types
		String pwmRegEx2 = "[Aa][ \t]*\\[([ \t]*\\d+)+[ \t]*\\][ \t]*\\s*\n" +
		"[Cc][ \t]*\\[([ \t]*\\d+)+[ \t]*\\][ \t]*\\s*\n" +
		"[Gg][ \t]*\\[([ \t]*\\d+)+[ \t]*\\][ \t]*\\s*\n" +
		"[Tt][ \t]*\\[([ \t]*\\d+)+[ \t]*\\][ \t]*\\s*";
		
		String onePwmSection1 = "\\s*" + fastaNameSeqRegEx + "\\s*\n+" + pwmRegEx1;
		String onePwmSection2 = "\\s*" + fastaNameSeqRegEx + "\\s*\n+" + pwmRegEx2;
		
		String fullPwmSeqRegEx1 = "(" + onePwmSection1 + "[ \t\r]*\n" + ")*" + onePwmSection1 + "\\s*";
		String fullPwmSeqRegEx2 = "(" + onePwmSection2 + "[ \t\r]*\n" + ")*" + onePwmSection2 + "\\s*";
		
		if (pwmSequence == null || !pwmSequence.matches(fullPwmSeqRegEx1) || !pwmSequence.matches(fullPwmSeqRegEx2)) {
			FacesMessage msg = new FacesMessage("Supplied PWM sequences are not in the correct format.\n " +
					"Make sure each PWM has a name, which precedes the it and starts with \"> \".\n" +
					"For a format example, see C.elegans data.");
			throw new ValidatorException(msg);
		}
		
		
	}
	
	
	/*********** Helper methods *************/
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
	
	/***********  Getters/setters   ***********/

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
	
	public String getNegExType() {
		return negExType;
	}
	public void setNegExType(String negExType) {
		this.negExType = negExType;
	}
	
	public String getBioMarkersType() {
		return bioMarkersType;
	}
	public void setBioMarkersType(String bioMarkersType) {
		this.bioMarkersType = bioMarkersType;
	}

	public String getCustomNegExDivVisibility() {
		if (NEG_EX_SHOW_RADIO_VALUE.equals(negExType)){
			return SHOW;
		}
		return HIDE;
	}
	
	public String getCustomPSSMsDivDivVisibility() {
		if (BIO_MARKERS_SHOW_RADIO_VALUE.equals(bioMarkersType)){
			return SHOW;
		}
		return HIDE;
	}

	public boolean getTheoryInduced(){
		return theoryInduced;
	}
	
	
	
}

