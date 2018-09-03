package org.lrima.simulation;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.lrima.core.UserPrefs;
import org.lrima.espece.Espece;
import org.lrima.espece.network.NetworkPanel;
import org.lrima.map.MapPanel;
import org.lrima.simulation.Interface.actions.*;
import org.lrima.simulation.Interface.EspeceInfoPanel;
import org.lrima.simulation.Interface.GraphicPanel;

public class FrameManager extends JFrame{

    //All the panels
	private NetworkPanel networkPanel;
	private MapPanel mapPanel;
    private GraphicPanel graphicPanel;
    private EspeceInfoPanel especeInfoPanel;

    //the simulation and its information
	private Simulation simulation;

	//Pour le menu
    private JCheckBoxMenuItem checkBoxRealtime;
    private JCheckBoxMenuItem checkBoxGraphique;
    private JCheckBoxMenuItem checkBoxNeuralNet;
    private JCheckBoxMenuItem checkBoxFollowBest;
    private JCheckBoxMenuItem checkBoxEspeceInfo;
	
	public FrameManager(Simulation simulation) {
	    this.setupWindow();

        this.simulation = simulation;

        //setup the panels
		mapPanel = new MapPanel(simulation);
		networkPanel = new NetworkPanel(simulation);
        graphicPanel = new GraphicPanel(simulation);
		especeInfoPanel = new EspeceInfoPanel();

		//The main panel
		this.add(mapPanel, BorderLayout.CENTER);

        //Create menu the menu buttons
        createMenu();
        displaySavedPanel();

        //Start the map panel
        start();
	}

    /**
     * Setup the size of the window, the listeners and basic configuration
     */
	private void setupWindow(){
        //Get the size of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        //The size if the user disable full screen
        setSize(screenSize.width / 2, screenSize.height / 2);
        setResizable(true);

        //Make it full screen
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        //Key listener
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                //Press Q to go to the next generation
                if(e.getKeyCode() == KeyEvent.VK_Q){
                    simulation.goToNextGeneration();
                }
            }
        });

        //Mouse listener
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //requestFocus();
            }
        });

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setAutoRequestFocus(true);
        this.setVisible(true);
    }

    /**
     * Create the menu buttons
     */
	private void createMenu(){
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        setupFileMenu(menuBar);
        setupSimulationMenu(menuBar);
        setupMapMenu(menuBar);
        setupWindowMenu(menuBar);

        setMenuButtonStates();
    }

    /**
     * Add the File menu buttons to the menu bar
     * @param menuBar the menu bar to add the file menu
     */
    private void setupFileMenu(JMenuBar menuBar){
        JMenu file = new JMenu("File");
        menuBar.add(file);

        //Save button
        JMenuItem save = new JMenuItem(new SaveNeuralNetworkFileAction("Save", this, simulation));
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        file.add(save);

        //Load button
        JMenuItem load = new JMenuItem(new LoadNeuralNetworkFileAction("Load", this, simulation));
        load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        file.add(load);
    }

    /**
     * Add the simulation buttons on the menu bar
     * @param menuBar the menu bar to add the simulation menu onto
     */
    private void setupSimulationMenu(JMenuBar menuBar){
        JMenu simulationMenu = new JMenu("Simulation");
        menuBar.add(simulationMenu);

        //Real time checkbox
        checkBoxRealtime = new JCheckBoxMenuItem(new RealTimeAction("Real time"));
        simulationMenu.add(checkBoxRealtime);

        //Follow best checkbox
        checkBoxFollowBest = new JCheckBoxMenuItem(new FollowBestAction("Follow best"));
        simulationMenu.add(checkBoxFollowBest);

        //Pause button
        JMenuItem pause = new JMenuItem(new PauseAction("Pause", simulation));
        pause.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK));
        simulationMenu.add(pause);

        //Options button
        JMenuItem options = new JMenuItem(new MoreOptionsAction("Options", this));
        simulationMenu.add(options);
    }

    /**
     * Add the map menu buttons to the menu bar
     * @param menuBar the menu bar to add the map menu onto
     */
    private void setupMapMenu(JMenuBar menuBar){
        JMenu map = new JMenu("Map");
        menuBar.add(map);

        //Map editor button
        JMenuItem mapEditor = new JMenuItem(new OpenStudioAction("Open Studio", simulation));
        map.add(mapEditor);
    }

    /**
     * Add the window menu buttons to the menu bar
     * @param menuBar the menu bar to add the window menu onto
     */
    private void setupWindowMenu(JMenuBar menuBar){
        JMenu window = new JMenu("Window");
        menuBar.add(window);

        //Show graphic panel button
        checkBoxGraphique = new JCheckBoxMenuItem(new WindowAddPanelAction("Graphiques", this, graphicPanel, "South", UserPrefs.KEY_WINDOW_GRAPHIQUE));
        window.add(checkBoxGraphique);

        //TODO: show the neural network on top of the map and not on the bottom of the window
        //Show neural network panel button
        checkBoxNeuralNet = new JCheckBoxMenuItem(new WindowAddPanelAction("Neural Network", this, networkPanel, "South", UserPrefs.KEY_WINDOW_NEURAL_NET));
        window.add(checkBoxNeuralNet);

        //Show car information panel
        checkBoxEspeceInfo = new JCheckBoxMenuItem(new WindowAddPanelAction("Car info", this, especeInfoPanel, "East", UserPrefs.KEY_WINDOW_ESPECE_INFO));
        window.add(checkBoxEspeceInfo);
    }

    /**
     * Set the state of the check boxes in the menu to the state that is saved
     * in the user preferences
     */
    private void setMenuButtonStates(){
        checkBoxRealtime.setState(UserPrefs.REAL_TIME);
        checkBoxFollowBest.setState(UserPrefs.FOLLOW_BEST);
        checkBoxGraphique.setState(UserPrefs.SHOW_WINDOW_GRAPHIQUE);
        checkBoxNeuralNet.setState(UserPrefs.SHOW_WINDOW_NEURAL_NETWORK);
        checkBoxEspeceInfo.setState(UserPrefs.SHOW_WINDOW_ESPECE_INFO);
    }

    /**
     * Restore the panels that was open in the last session
     */
    private void displaySavedPanel(){
	    if(checkBoxGraphique.getState()){
	        add(graphicPanel, "South");
        }
        if(checkBoxNeuralNet.getState()){
	        add(networkPanel, "South");
        }
        if(checkBoxEspeceInfo.getState()){
	        add(especeInfoPanel, "East");
        }
    }

    /**
     * Make the mapPanel and networkPanel redraw itself regularly
     */
    public void start() {
		mapPanel.start();
		networkPanel.start();
	}

	//TODO: On a tu vraiment besoin de ça?
	public void pack() {
		super.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

    /**
     * Change the car that the networkPanel uses
     * @param e the car
     */
    public void changeNetworkFocus(Espece e) {
		networkPanel.setEspece(e);
	}
}