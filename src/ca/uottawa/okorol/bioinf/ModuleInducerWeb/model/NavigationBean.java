package ca.uottawa.okorol.bioinf.ModuleInducerWeb.model;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class NavigationBean {
	
	private boolean cElegansData = false;
	private boolean customData = false;
	private boolean inducedTheory = false;
	
	private String selectedData = "";
	

	
	public void loadData(){
		if (selectedData != null && !selectedData.isEmpty()){
			if ("Celegans".equals(selectedData)){
				cElegansData = true;
				customData = false;
			} else if ("custom".equals(selectedData)){
				customData = true;
				cElegansData = false;
			} else {
				customData = false;
				cElegansData = false;
			}
			
			inducedTheory = false;
		}
		
	}
	
	
	/***********  Getters/setters   ***********/

	public String getSelectedData() {
		return selectedData;
	}
	public void setSelectedData(String pageToDisplay) {
		this.selectedData = pageToDisplay;
	}

	public boolean getInducedTheory(){
		return inducedTheory;
	}
	
	public boolean getcElegansData(){
		return cElegansData;
	}

	public boolean getCustomData(){
		return customData;
	}
	
	
}
