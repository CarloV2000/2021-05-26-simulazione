package it.polito.tdp.yelp.model;

import java.util.ArrayList;
import java.util.List;

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

	public Model() {
		this.dao = new YelpDao();
		this.cities = new ArrayList<>(dao.getAllCities());
		this.allBusinesses = new ArrayList<>();
		
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
	
}
