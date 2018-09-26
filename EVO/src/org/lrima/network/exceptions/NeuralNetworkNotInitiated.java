package org.lrima.network.exceptions;

public class NeuralNetworkNotInitiated extends Exception {

    @Override
    public String getMessage() {
        return "The neural network hasn't been initiated";
    }
}
