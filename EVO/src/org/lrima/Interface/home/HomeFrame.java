/*
 * Created by JFormDesigner on Sun Sep 16 19:44:39 EDT 2018
 */

package org.lrima.Interface.home;

import org.lrima.Interface.FrameManager;
import org.lrima.espece.network.algorithms.AlgorithmManager;
import org.lrima.espece.network.annotations.AlgorithmInformation;
import org.lrima.espece.network.interfaces.NeuralNetwork;
import org.lrima.espece.network.interfaces.NeuralNetworkModel;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * @author unknown
 */
public class HomeFrame extends JFrame implements ActionListener, ItemListener {

    private JButton okButton = new JButton("Simulate");
    JTextPane algorithmDescriptionPane;
    private JComboBox<String> neuralNetworkComboBox = new JComboBox<>();
    private JSpinner simulationPerBatchesSpinner;

    private ArrayList<String> algorithmsNames;


    private final int FRAME_MARGIN = 20;
    private final int MARGIN_BETWEEN_COMPONENTS = 10;

    /**
     * A dialog that describe the EVO program and lets the user choose which algorithm to use.
     */
    public HomeFrame() {
        super();
        this.setTitle("Welcome to EVO");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();

        this.neuralNetworkComboBox.addItemListener(this);
    }


    /**
     * Inits the components of the dialog and adds them to the panel
     */
    private void initComponents() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension panelSize = new Dimension((int)(screenSize.width / 2.5), screenSize.height / 2);

        JPanel panel = new JPanel();
        this.setResizable(false);
        this.setPreferredSize(new Dimension(panelSize.width, panelSize.height));
        this.setBounds(screenSize.width / 2 - panelSize.width / 2, screenSize.height / 2 - panelSize.height / 2, panelSize.width, panelSize.height);

        this.okButton.addActionListener(this);

        loadAlgorithms();

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowWeights = new double[]{1.0, 0.0};
        gridBagLayout.columnWeights = new double[]{1.0, 0.0};

        panel.setLayout(gridBagLayout);

        //INTRODUCTION TEXT
        JEditorPane introductionText = new JTextPane();
        introductionText.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
                    if(Desktop.isDesktopSupported()){
                        try{
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        }catch (Exception error){
                            error.printStackTrace();
                        }

                    }
                }
            }
        });

        introductionText.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        String text =   "Welcome to EVO, a program that lets you test different artificial intelligence";
        text +=         " algorithms with the task of making a car learn to drive by itself without driving into obstacles.";
        text +=         " This program was developed for LRIMa, <a href=\"http://www-etud.iro.umontreal.ca/~rezguiji/lrima.php\">click here to learn more</a>";
        introductionText.setText(text);
        introductionText.setEditable(false);

        GridBagConstraints textPaneGBC = new GridBagConstraints();
        textPaneGBC.gridx = 0;
        textPaneGBC.gridy = 0;
        textPaneGBC.fill = GridBagConstraints.BOTH;
        textPaneGBC.gridwidth = 4;
        textPaneGBC.insets = new Insets(FRAME_MARGIN, FRAME_MARGIN, MARGIN_BETWEEN_COMPONENTS, FRAME_MARGIN);

        panel.add(introductionText, textPaneGBC);

        //ALGORITHM TO USE COMBOBOX AND LABEL
        JLabel algorithmToUseLabel = new JLabel("Algorithm to use:");

        GridBagConstraints algorithmToUseLabelGBC = new GridBagConstraints();
        algorithmToUseLabelGBC.gridx = 0;
        algorithmToUseLabelGBC.gridy = 1;
        algorithmToUseLabelGBC.gridwidth = 2;
        algorithmToUseLabelGBC.insets = new Insets(MARGIN_BETWEEN_COMPONENTS, FRAME_MARGIN, MARGIN_BETWEEN_COMPONENTS, MARGIN_BETWEEN_COMPONENTS);

        panel.add(algorithmToUseLabel, algorithmToUseLabelGBC);

        GridBagConstraints algorithmToUseComboBoxGBC = new GridBagConstraints();
        algorithmToUseComboBoxGBC.gridx = 0;
        algorithmToUseComboBoxGBC.gridwidth = 2;
        algorithmToUseComboBoxGBC.gridy = 2;
        //algorithmToUseComboBoxGBC.fill = GridBagConstraints.HORIZONTAL;
        algorithmToUseComboBoxGBC.insets = new Insets(MARGIN_BETWEEN_COMPONENTS, MARGIN_BETWEEN_COMPONENTS, MARGIN_BETWEEN_COMPONENTS, FRAME_MARGIN);

        panel.add(this.neuralNetworkComboBox, algorithmToUseComboBoxGBC);

        //NUMBER OF SIMULATION PER BATCH
        JLabel numberOfSimulationLabel = new JLabel("Simulations per batches: ");
        numberOfSimulationLabel.setHorizontalTextPosition(JLabel.LEFT);
        GridBagConstraints numberOfSimulationLabelGBC = new GridBagConstraints();
        numberOfSimulationLabelGBC.gridx = 1;
        numberOfSimulationLabelGBC.gridy = 1;
        numberOfSimulationLabelGBC.gridwidth = 2;
        numberOfSimulationLabelGBC.insets = new Insets(MARGIN_BETWEEN_COMPONENTS, MARGIN_BETWEEN_COMPONENTS, MARGIN_BETWEEN_COMPONENTS, MARGIN_BETWEEN_COMPONENTS);

        panel.add(numberOfSimulationLabel, numberOfSimulationLabelGBC);

        SpinnerNumberModel numberModel = new SpinnerNumberModel(1, 1, 999, 1);
        this.simulationPerBatchesSpinner = new JSpinner(numberModel);
        GridBagConstraints simulationPerBatchesSpinnerGBC = new GridBagConstraints();
        simulationPerBatchesSpinnerGBC.gridx = 1;
        simulationPerBatchesSpinnerGBC.gridy = 2;
        simulationPerBatchesSpinnerGBC.gridwidth = 3;
        simulationPerBatchesSpinnerGBC.insets = new Insets(MARGIN_BETWEEN_COMPONENTS, MARGIN_BETWEEN_COMPONENTS, MARGIN_BETWEEN_COMPONENTS, FRAME_MARGIN);

        panel.add(simulationPerBatchesSpinner, simulationPerBatchesSpinnerGBC);

        //Algorithm description
        algorithmDescriptionPane = new JTextPane();
        String descriptionText = AlgorithmManager.algorithms.get(neuralNetworkComboBox.getSelectedIndex()).getAnnotation(AlgorithmInformation.class).description();
        this.algorithmDescriptionPane.setText(descriptionText);
        algorithmDescriptionPane.setEditable(false);

        GridBagConstraints algorithmDescriotionGBC = new GridBagConstraints();
        algorithmDescriotionGBC.gridx = 0;
        algorithmDescriotionGBC.gridy = 3;
        algorithmDescriotionGBC.gridwidth = 4;
        algorithmDescriotionGBC.insets = new Insets(MARGIN_BETWEEN_COMPONENTS, FRAME_MARGIN, MARGIN_BETWEEN_COMPONENTS, FRAME_MARGIN);
        algorithmDescriotionGBC.fill = GridBagConstraints.BOTH;

        panel.add(algorithmDescriptionPane, algorithmDescriotionGBC);

        //SIMULATE BUTTON
        GridBagConstraints okButtonGBC = new GridBagConstraints();

        okButtonGBC.gridy = 4;
        okButtonGBC.gridx = 3;
        okButtonGBC.insets = new Insets(MARGIN_BETWEEN_COMPONENTS, MARGIN_BETWEEN_COMPONENTS, FRAME_MARGIN, FRAME_MARGIN);

        panel.add(okButton, okButtonGBC);

        this.pack();

        this.getContentPane().add(panel);
    }

    /**
     * Get all the the algorithms registered in the AlgorithmManager class
     */
    private void loadAlgorithms(){
        //resets the combo box with the new information

        //Adds a item "All algorithms" to the end of the combo box
        ArrayList<String> names = AlgorithmManager.algorithmsName;
        names.add("All algorithms");
        this.neuralNetworkComboBox = new JComboBox(names.toArray());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == this.okButton){
            try {
                loadSelectedAlgorithm();
            } catch (InstantiationException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() != ItemEvent.SELECTED)
            return;

        if(neuralNetworkComboBox.getSelectedItem().toString().equals("All algorithms")) {
            try {
                String descriptionText = AlgorithmManager.algorithms.get(neuralNetworkComboBox.getSelectedIndex()).getAnnotation(AlgorithmInformation.class).description();
                this.algorithmDescriptionPane.setText(descriptionText);
            }catch (NullPointerException exception){
                System.err.println("The class '" + AlgorithmManager.algorithms.get(neuralNetworkComboBox.getSelectedIndex()).getName() + "' registered in AlgorithmManager does not have the @AlgorithmInformation annotation");
                this.algorithmDescriptionPane.setText("Error");
            }
        }else{
            //Set the description of the "All Algorithms" combo box item
            String description = "Simulate all algorithms at the same time. The graphics are disabled and you only see a chart of each algorithms with the fitness over time.";
            this.algorithmDescriptionPane.setText(description);
        }

    }

    private void loadSelectedAlgorithm() throws InstantiationException {
        FrameManager frameManager = new FrameManager();

        int simulationPerBatches;

        //If the user did not enter a integer
        if(this.simulationPerBatchesSpinner.getValue() instanceof Integer) {
            simulationPerBatches = (int) this.simulationPerBatchesSpinner.getValue();
        }
        else{
            simulationPerBatches = 1;
        }

        if(simulationPerBatches <= 0){
            simulationPerBatches = 1;
        }

        if(!neuralNetworkComboBox.getSelectedItem().toString().equals("All algorithms")) {
            try {
                NeuralNetworkModel algorithmChosen = AlgorithmManager.algorithms.get(neuralNetworkComboBox.getSelectedIndex()).getConstructor().newInstance();
                frameManager.addBatch(algorithmChosen, simulationPerBatches);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            this.dispose();
        }
        else{
            //All algorithms
            for(Class<?extends NeuralNetworkModel> algorithm : AlgorithmManager.algorithms){
                try {
                    NeuralNetworkModel algorithmInstance = algorithm.getConstructor().newInstance();
                    frameManager.addBatch(algorithmInstance,simulationPerBatches);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                this.dispose();
            }
        }

        frameManager.startBatches();
    }
}