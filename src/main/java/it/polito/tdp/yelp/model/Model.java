package it.polito.tdp.yelp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	
	private List<String>cities;
	private YelpDao dao;
	private Graph<Business, DefaultWeightedEdge>grafo;
	private List<Business>allBusinesses;
	private Double bilancioMax;
	
	private List<Business>migliore;
	private int dimensioneMIN;
	private Map<String, Business>nameMapBusinesses;

	public Model() {
		this.dao = new YelpDao();
		this.cities = new ArrayList<>(dao.getAllCities());
		this.allBusinesses = new ArrayList<>();
		this.nameMapBusinesses = new HashMap<>();
		
	}

	public List<String> getCities() {
		return cities;
	}
	

	public Graph<Business, DefaultWeightedEdge> getGrafo() {
		return grafo;
	}

	public List<Business> getAllBusinesses() {
		return allBusinesses;
	}

	public Double getBilancioMax() {
		return bilancioMax;
	}

	public String creaGrafo(String city, Integer anno) {
		
		this.grafo = new SimpleDirectedWeightedGraph<Business, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		this.allBusinesses = dao.getAllBusiness(city, anno);
		Graphs.addAllVertices(grafo, this.allBusinesses);
		
		for(Business b : this.allBusinesses) {
			this.nameMapBusinesses.put(b.getBusinessName(), b);
		}
		
		for(Business x : this.allBusinesses) {
			for(Business y : this.allBusinesses) {
				if(!x.equals(y)) {
					Double mediaRecensioniX = dao.getMediaRecensioni(x, anno);
					Double mediaRecensioniY = dao.getMediaRecensioni(y, anno);
				
					if(mediaRecensioniX > mediaRecensioniY) {
						Double peso = mediaRecensioniX - mediaRecensioniY;
						Graphs.addEdge(grafo, y, x, peso);
						
					}else if(mediaRecensioniX < mediaRecensioniY) {
						Double peso = mediaRecensioniY - mediaRecensioniX;
						Graphs.addEdge(grafo, x, y, peso);
					}
				}	
			}
		}
		return "Grafo creato con "+grafo.vertexSet().size()+" vertici e "+grafo.edgeSet().size()+" archi.\n";
	}
	
	
	public Double getBilancio(Business a){
		
		double bilancio = 0;
		List<DefaultWeightedEdge>entranti = new ArrayList<>(this.grafo.incomingEdgesOf(a));
		List<DefaultWeightedEdge>uscenti = new ArrayList<>(this.grafo.outgoingEdgesOf(a));
		
		for(DefaultWeightedEdge x : entranti) {
			bilancio += this.grafo.getEdgeWeight(x);
		}
		for(DefaultWeightedEdge x : uscenti) {
			bilancio -= this.grafo.getEdgeWeight(x);
		}
		
		return bilancio;
	}
	
	
	
	public Business getMigliore(Integer anno){
		Business best = null;
		this.bilancioMax = 0.0;
		for(Business b : grafo.vertexSet()){
			Double bil = this.getBilancio(b);
			if(bil > this.bilancioMax) {
				this.bilancioMax = bil;
				best = b;
			}
		}
		return best;
	}
	

	
	public void calcolaCammino(String businessName, Double sogliaMINPesoArchi, int anno) {
		
		Business partenza = this.nameMapBusinesses.get(businessName);
		Business arrivo = this.getMigliore(anno);
		List<Business>rimanenti = new ArrayList<>(grafo.vertexSet());
		List<Business>parziale = new ArrayList<>();
		parziale.add(partenza);
		rimanenti.remove(partenza);
		
		this.dimensioneMIN = 1;//perche ho gia aggiunto il business di partenza
		this.migliore = new ArrayList<Business>(parziale);

		ricorsione(parziale, rimanenti, sogliaMINPesoArchi, arrivo);
		
	}

	private void ricorsione(List<Business> parziale, List<Business> rimanenti, Double sogliaMINPesoArchi, Business arrivo){

		// Condizione Terminale
		if (rimanenti.isEmpty()) {

			int dimensione = parziale.size();
			if (parziale.get(parziale.size()-1).equals(arrivo) && dimensione < this.dimensioneMIN) {
				this.dimensioneMIN = dimensione;
				this.migliore = new ArrayList<>(parziale);
			}
			//return;
		}
		
       	for (Business p : rimanenti) {
       		if(!parziale.contains(p)) {//se il business non e gia in parzale
       			DefaultWeightedEdge e = grafo.getEdge(parziale.get(parziale.size()-1), p);
       			if(e != null && grafo.getEdgeWeight(e) >= sogliaMINPesoArchi) {//e se l'arco pesa piu della soglia
       				List<Business> currentRimanenti = new ArrayList<>(rimanenti);
       				parziale.add(p);
       				currentRimanenti.remove(p);
 					ricorsione(parziale, currentRimanenti, sogliaMINPesoArchi, arrivo);
 					parziale.remove(parziale.size()-1);
 				}
 			}
 		}
	}

	public List<Business> getMigliore() {
		return migliore;
	}

	public int getDimensioneMIN() {
		return dimensioneMIN;
	}

	public Map<String, Business> getNameMapBusinesses() {
		return nameMapBusinesses;
	}

		
	
}
