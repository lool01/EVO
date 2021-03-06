package org.lrima.espece;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.*;

import org.lrima.Interface.FrameManager;
import org.lrima.annotations.DisplayInfo;
import org.lrima.core.UserPrefs;

import org.lrima.espece.capteur.Capteur;
import org.lrima.network.interfaces.NeuralNetwork;
import org.lrima.network.interfaces.NeuralNetworkModel;
import org.lrima.network.interfaces.NeuralNetworkReceiver;
import org.lrima.map.Studio.Drawables.Line;
import org.lrima.map.Studio.Drawables.Obstacle;
import org.lrima.simulation.Simulation;
import org.lrima.map.Map;

public class Espece implements Comparable<Espece>, NeuralNetworkReceiver {

	@DisplayInfo
	private int NB_CAPTEUR;
	//Width has to be bigger than the height
	private static final int ESPECES_WIDTH = 74, ESPECES_HEIGHT = 50;

	//Stores the position of the car
	private double x, y;

	private double rightSpeed, leftSpeed;

	//Is the car still alive?
	@DisplayInfo
	private boolean alive;

	//Orientation of the car in radian
	@DisplayInfo
	private double orientationRad;

	//Speed of the car
	@DisplayInfo
	private double vitesse = 0.0;

	//Acceleration of the car
	@DisplayInfo
	private double acceleration;

	//The fitness of the car.
	//The fitness is used to classify the car based on how good it did
	@DisplayInfo
	private double fitness = 0.0;

	//Array to store all the sensors
	private ArrayList<Capteur> capteurs = new ArrayList<>();

	//The neural network of the car
	private NeuralNetwork neuralNetwork;

	//Is the car currently selected ?
	public boolean selected;

	//Used to get the average speed
	private double totalSpeed;

	//Used to calculate the fitness
	@DisplayInfo
	private double maxDistanceFromStart;

	//Also used to calculate the fitness
	@DisplayInfo
	private double totalDistanceTraveled;

	//Also used to calculate the fitness
	@DisplayInfo(textRepresentation = "Right")
	private float numberOfTimeWentRight = 0;
	@DisplayInfo(textRepresentation = "Left")
	private float numberOfTimeWentLeft = 0;

	//Used to calculate the total distance
	private Point lastPointTraveled;

	//Used to know at what generation the car was created (higher number = the car is good)
	@DisplayInfo
	private int bornOnGeneration = 0;

	//Used to store the color of the car
	private Color voitureColor = Color.RED;
	private final Color SELECTED_CAR_BORDER_COLOR = new Color(83, 75, 255, 158);

	//Stores the simulation reference
	private Simulation simulation;
	private NeuralNetworkModel algorithmModel;

	private double diedAtTime;

	private AffineTransform transform = new AffineTransform();

	/**
	 * Initialize the car with the map to put him into
	 * The starting position and orientation is determined by the map
	 * @param simulation the simulation the car is into
	 */
	public Espece(Simulation simulation) {
		this.algorithmModel = simulation.getAlgorithm();

		//Do the base configuration
		this.simulation = simulation;
		this.bornOnGeneration = simulation.getGeneration();
		this.alive = true;

		//Get the starting parameters from the map
		this.x = simulation.getMap().getDepart().x;
		this.y = simulation.getMap().getDepart().y;
		this.orientationRad = 0;

		//Used to calculate the total distance traveled
		this.lastPointTraveled = new Point((int)x, (int)y);

		//Setup a base neural network for the car to have
		//If you want to choose which neural network to use, call Espece(Map, NeuralNetwork)
		//neuralNetwork = new NeuralNetwork(NB_CAPTEUR, 2, true);

    }

    public void setNumberSensor(int numberSensor){
		this.NB_CAPTEUR = numberSensor;
		setupSensors();

		if(this.algorithmModel != null) {
			this.neuralNetwork = this.algorithmModel.getInstance();
			this.neuralNetwork.init(this.capteurs, this);
		} // 5 simulations, 20 generations,
	}

    //TODO: do a real copy
	/**
	 * Used to clone a car
	 * ONLY WORKS FOR FITNESS AND THE NEURALNETWORK
	 * @param e the car to clone
	 */
	public Espece(Espece e){
			this.fitness = e.getFitness();
			this.neuralNetwork = e.getNeuralNetwork();
	}

	/*public Espece copy(){
		Espece e = new Espece(this.simulation);

		e.fitness = this.fitness;
		e.x = this.x;
		e.y = this.y;
		e.orientationRad = this.orientationRad;
		e.neuralNetwork = this.neuralNetwork;
		e.alive = this.alive;

		return e;
	}*/

	/**
	 * The function used to calculate the fitness of the car
	 * @return the fitness of the car
	 */
	private double fitnessFunction(){
		double fitness;

        totalSpeed = this.totalSpeed;
        maxDistanceFromStart = this.maxDistanceFromStart;
        totalDistanceTraveled = this.totalDistanceTraveled;
        double diedAtTime = Simulation.simulationTime;


		//TODO: Cr�ez votre propre fonction de fitness ici


		fitness = (getMaxDistanceFromStart()) / 10000;


		//

		return fitness;
	}

	/**
	 * Creates NB_CAPTEUR number of sensors equally distanced from each other
	 */
	private void setupSensors(){
		//Setup the sensors base on the number of sensors
		//Note that the maximum number of sensors is limited to 180 because of this

		double sensorEveryDeg = 180 / (NB_CAPTEUR - 1);

		for(double i = 90 ; i >= -90 ; i -= sensorEveryDeg){
			//TODO: Il faut ajouter +1, car � un angle de 90 deg �a bug !
			capteurs.add(new Capteur(this, i + 1, 0, 0));
		}
	}

	/**
	 * Set the color of the car depending on its fitness
	 * The better the fitness, the greener it gets. The worst the fitness is, the more it is red
	 */
	private void setColor(){
		double bestFitness = this.simulation.getBestFitness();
		double percentageFitness = fitness / bestFitness;

		if(fitness <= bestFitness) {
			this.voitureColor = new Color(255 - (int) (255 * percentageFitness), (int) (255 * percentageFitness), 124, 124);
		}
	}


	/**
	 * Transfer all the value of the sensors into a single array
	 * @return an array containing all the values of the sensors
	 */
	private double[] capteursToArray() {
		double[] capteursValue = new double[this.capteurs.size()];
		for (int i = 0; i < capteursValue.length; i++) {
			capteursValue[i] = this.capteurs.get(i).getValue();
		}
		
		return capteursValue;
	}

	/**
	 * Get the fitness and update the speed of the wheels from the neural network
	 * @param timePassed the time that passed since the last call of update
	 */
	public void update(double timePassed) {
		//No need to update if car is not alive
		if(!alive) {
			return;
		}

		//Set the fitness to the car
		this.calculateFitnessScore();
		this.neuralNetwork.setFitness(this.fitness);

		neuralNetwork.feedForward();

		//Get the car speed and turn rate from the settings
        int savedCarSpeed = UserPrefs.getInt(UserPrefs.KEY_CAR_SPEED);
        double savedTurnRate = UserPrefs.getDouble(UserPrefs.KEY_TURN_RATE);

		//Applies the speed of each side of the car to move it to the next position
		orientationRad -= Math.toRadians(leftSpeed*timePassed - rightSpeed*timePassed)*savedTurnRate;
		acceleration = rightSpeed*timePassed*savedCarSpeed/100 + leftSpeed*timePassed*savedCarSpeed/100;
		vitesse += acceleration - vitesse;

		double accelerationInX = Math.cos(orientationRad) * acceleration;
		double accelerationInY = Math.sin(orientationRad) * acceleration;
		double speedInX = Math.cos(orientationRad) * vitesse;
		double speedInY = Math.cos(orientationRad) * vitesse;

		//this.x += speedInX*timePassed + (accelerationInX*Math.pow(timePassed, 2))/2;
		//this.y += speedInY*timePassed + (accelerationInY*Math.pow(timePassed, 2))/2;

		//this.vitesse += acceleration*timePassed;

		this.x += vitesse*Math.cos(orientationRad);
		this.y += vitesse*Math.sin(orientationRad);

		//The car can't go backward
		if(vitesse < 0) {
			vitesse = 0;
		}

		//To get the average speed
		totalSpeed += vitesse;


		//Reset the sensors
		this.resetCapteur();

	}

	/**
	 * Resets the car in case it gets to the next generation
	 */
	public void resetEspece(){
		this.numberOfTimeWentRight = 0;
		this.numberOfTimeWentLeft = 0;
		this.maxDistanceFromStart = 0;
	}

	public void draw(Graphics2D g) {
		//Set the color of the car based on its fitness
		setColor();
		g.setColor(voitureColor);

		//Get all the points of the car and draws it
		g.fill(this.getShape());

		//Draw the contour of the car.
		//If the car is selected. Set the color of the contour to blue
		g.setStroke(new BasicStroke(5));
		g.setColor(Color.BLACK);
		if(selected){
			g.setColor(SELECTED_CAR_BORDER_COLOR);
		}
		g.draw(this.getShape());

		g.setStroke(new BasicStroke(3));

		//Draw the sensors
		if(selected) {
			for (Capteur c : capteurs) {
				g.setColor(Color.CYAN);
				c.draw(g);
			}
		}
	}

	/**
	 * Calculate all the variables required to get the fitness of the car
	 * It calculates the biggest distance it gets from the start and
	 * the total distance traveled.
	 * The fitness is then assigned to this car
	 */
	private void calculateFitnessScore(){
		//Calculate the biggest distance the car gets to from the start
		if(this.simulation.getMap() != null){
			double currentDistance = distanceFrom(this.simulation.getMap().getDepart());
			if(maxDistanceFromStart < currentDistance){
				maxDistanceFromStart = currentDistance;
			}
		}

		//Calculate the total distance of the car
		totalDistanceTraveled += distanceFrom(lastPointTraveled);
		lastPointTraveled = new Point((int)x, (int)y);

		this.fitness = fitnessFunction();
	}

	/**
	 * Get the position of a point based on the orientation of the car
	 * @param point the point to rotate
	 * @return the true position based on the rotation of the car
	 */
	private Point rotatePoint(Point point){
		int centerX = (int)x;
		int centerY = (int)y;

		int newX = (int)((point.x-centerX)*Math.cos(orientationRad) - (point.y-centerY) * Math.sin(orientationRad)) + centerX;
		int newY = (int)((point.x-centerX)*Math.sin(orientationRad) + (point.y-centerY) * Math.cos(orientationRad)) + centerY;

		return new Point(newX, newY);
	}

	/**
	 * Set the alive status of the car to false
	 */
	public void kill() {
		alive = false;
		this.diedAtTime = Simulation.simulationTime;
	}

	/**
	 * Reset all the sensors to a value of 1
	 */
	private void resetCapteur() {
		for(Capteur c : capteurs) {
			c.reset();
		}
	}

	/**
	 * Get the distance from a certain point to the car
	 * @param point the point
	 * @return the distance from the point to the car
	 */
	public int distanceFrom(Point point) {
		return (int) ((point.x - this.x)*(point.x - this.x) + (point.y - this.y)*(point.y - this.y));
	}

	//TODO: Is it really necessary?
	/**
	 * Tp la voiure au point d?part et lui donne une orientation orientation.
	 *
	 * De plus, l'acceleration et la vitesse sont mise a z?ro
	 *
	 */
	public void tpLikeNew() {
		this.acceleration = 0;
		this.vitesse = 0;
		this.x = this.simulation.getMap().getDepart().x;
		this.y = this.simulation.getMap().getDepart().y;
		this.orientationRad = 0;
		this.alive = true;
		this.resetCapteur();

	}

	Path2D.Double mapShape;

	/**
	 * If the car is alive, it checks if it is coliding with an obstale.
	 * If it is, it adds it to the closed set and kills it
	 * @param map the map to retreive the walls from the map
	 * @return true if it should die, false otherwise
	 */
	public boolean shouldDie(Map map){
		//Regarde si l'esp�ce meurt
		if(this.alive) {
			if(mapShape == null)
				mapShape = map.getShape();
			Path2D especeShape = this.getShape();

			Area intersection = new Area(mapShape);
			intersection.intersect(new Area(especeShape));

			Area substract = new Area(especeShape);
			substract.subtract(new Area(mapShape));

			if(!substract.isEmpty() && !(intersection.getBounds().equals(especeShape.getBounds()))) {
				try {
					this.simulation.addEspeceToClosed(this);
					this.kill();

					return true;
				} catch (Exception e1) {
					e1.printStackTrace();
					simulation.setRunning(false);
				}
			}
			else
				return false;
		}
		return true; // If the car is dead
	}

	public Path2D.Double getShape(){
		Path2D.Double path = new Path2D.Double();
		path.moveTo(getTopLeft().x, getTopLeft().y);
		path.lineTo(getTopRight().x, getTopRight().y);
		path.lineTo(getBottomRight().x, getBottomRight().y);
		path.lineTo(getBottomLeft().x, getBottomLeft().y);
		path.closePath();

		return path;
	}

	//*******===========================================================================
	// * ACCESSORS AND MUTATORS
	// * ACCESSORS AND MUTATORS
	// ********============================================================================/

	/**
	 * Get the position of the top left of the car based on its position and orientation
	 * @return the position of the top left point of the car
	 */
	public Point getTopLeft() {
		double x = this.x;
		double y = this.y;

		return rotatePoint(new Point((int)x, (int)y));
	}

	/**
	 * Get the position of the top right of the car based on its position and orientation
	 * @return the position of the top right point of the car
	 */
	public Point getTopRight() {
		double x = this.x;
		double y = this.y;

		return rotatePoint(new Point((int)x + Espece.ESPECES_WIDTH, (int)y));
	}

	/**
	 * Get the position of the bottom right of the car based on its position and orientation
	 * @return the position of the bottom right point of the car
	 */
	public Point getBottomRight() {
		double x = this.x;
		double y = this.y;

		return rotatePoint(new Point((int)x + Espece.ESPECES_WIDTH, (int)y + Espece.ESPECES_HEIGHT));
	}

	/**
	 * Get the position of the bottom left of the car based on its position and orientation
	 * @return the position of the bottom left point of the car
	 */
	public Point getBottomLeft() {
		double x = this.x;
		double y = this.y;

		return rotatePoint(new Point((int)x, (int)y + Espece.ESPECES_HEIGHT));
	}
	
	public double getOrientation() {
		return orientationRad;
	}

	public double getX() {
		return this.rotatePoint(new Point((int)x + ESPECES_WIDTH / 2, (int)y + ESPECES_HEIGHT / 2)).x;
	}

	public double getY() {
		return this.rotatePoint(new Point((int)x + ESPECES_WIDTH / 2, (int)y + ESPECES_HEIGHT / 2)).y;
	}

	public double getFitness() {
		return this.fitness;
	}
	
	public NeuralNetwork getNeuralNetwork() {
		return this.neuralNetwork;
	}

	public ArrayList<Capteur> getCapteursList() {
		return capteurs;
	}


    /*public void setFitness(double fitness) {
		this.fitness = fitness;
		this.neuralNetwork.setFitness(fitness);
    }*/


	/**
	 * Set the car to selected and make sure it is the only car selected by deselecting all the others
	 * @param selected Should this car be selected?
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public double getTotalSpeed() {
		return totalSpeed;
	}

	private double getMaxDistanceFromStart() {
		return maxDistanceFromStart;
	}

	public double getTotalDistanceTraveled() {
		return totalDistanceTraveled;
	}

	@Override
	public int compareTo(Espece o) {
		return Double.compare(o.getFitness(), this.getFitness());
	}

	@Override
	public void setNeuralNetworkOutput(double... outputs) {
		this.rightSpeed = outputs[0];
		this.leftSpeed = outputs[1];
		//this.orientationRad += outputs[0];
		//this.acceleration = outputs[1];

	}

	@Override
	public int getSize() {
		return 2;
	}

	public void setNeuralNetwork(NeuralNetwork network) {
		this.neuralNetwork = network;
		this.neuralNetwork.init(this.capteurs, this);
		//this.neuralNetwork.setTransmitters(this.capteurs);
		//this.neuralNetwork.setReceiver(this);
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public NeuralNetworkModel getAlgorithmModel() {
		return algorithmModel;
	}
}
