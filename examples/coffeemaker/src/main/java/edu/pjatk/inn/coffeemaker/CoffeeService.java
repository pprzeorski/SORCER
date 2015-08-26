package edu.pjatk.inn.coffeemaker;

import sorcer.service.Context;
import sorcer.service.ContextException;

import java.rmi.RemoteException;

/**
 * Created by Mike Sobolewski on 8/26/15.
 */
public interface CoffeeService {

    public Context recipes(Context context) throws RemoteException, ContextException;

    public Context makeCoffee(Context context) throws RemoteException, ContextException;

}