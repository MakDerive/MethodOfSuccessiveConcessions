package com.example.demo.service;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.springframework.stereotype.Service;

import com.example.demo.model.Data;
import com.example.demo.model.OutputData;

@Service
public class OptimizeService {
	private static final DecimalFormat df = new DecimalFormat("#.##");
	
	public OutputData optimize(Data data) {
		String[] functions =new String[data.getObjectives().length];
		for(int i = 0; i < data.getObjectives().length;i++) {
			functions[i] = data.getObjectives()[i].getFunction();
		}
		int maxIndex = findMaxIndex(functions);
		double[][] coefficients = new double[functions.length][maxIndex];
		
		for (int i = 0; i < functions.length; i++) {
            coefficients[i] = extractCoefficients(functions[i], maxIndex);
        }
		List<LinearConstraint> constraints = new ArrayList<>();
		ConstraintData constraintData;
		for (String constraint : data.getConstraints()) {
			constraintData = parseConstraint(constraint, maxIndex);
			if (constraintData.inequalitySign.equals("<=")) {
				constraints.add(new LinearConstraint(constraintData.coefficients, Relationship.LEQ, constraintData.rightHandSide));
			} else if(constraintData.inequalitySign.equals(">=")) {
				constraints.add(new LinearConstraint(constraintData.coefficients, Relationship.GEQ, constraintData.rightHandSide));
			} else if (constraintData.inequalitySign.equals("=")){
				constraints.add(new LinearConstraint(constraintData.coefficients, Relationship.EQ, constraintData.rightHandSide));
			} 
		}
		PointValuePair solution = null;
		for ( int i = 0;i < coefficients.length;i++) {
			LinearObjectiveFunction f = new LinearObjectiveFunction(coefficients[i], 0);
			solution = optimizeFunction(f, constraints, data.getObjectives()[i].getOptimization());
			if (i < coefficients.length -1) {
				constraints.add(new LinearConstraint (coefficients[i],Relationship.GEQ,solution.getValue() - Double.parseDouble(data.getConcessions()[i])));
			}
		}
		
		double[] optimalValues = solution.getPoint();
		for(int i = 0;i < optimalValues.length;i++) {
			optimalValues[i] = optimalValues[i];
		}
		double[] resultFunctions = new double[coefficients.length];
		for(int i = 0;i < resultFunctions.length;i++) {
			resultFunctions[i] = getValueFunction(optimalValues, coefficients[i]);
		}
		optimalValues = round(optimalValues);
		resultFunctions = round(resultFunctions);
		System.out.println(Arrays.toString(optimalValues));
		return new OutputData(resultFunctions,optimalValues);
	}
	public double[] round(double[] arr) {
		for (int i = 0; i < arr.length; i++) {
            arr[i] = Math.round(arr[i] * 100.0) / 100.0;
        }
		return arr;
	}
	
	private double getValueFunction(double[] values, double[] coefficients) {
		double result = 0;
		for (int i = 0; i < coefficients.length;i++) {
			result+=values[i] * coefficients[i] ;
		}
		return result;
	}
	
	private PointValuePair optimizeFunction(
			LinearObjectiveFunction function,
			List<LinearConstraint> constraints,
			String goalType) 
	{
		LinearObjectiveFunction f = function;
		SimplexSolver solver = new SimplexSolver();
		PointValuePair solutionF;
		if (goalType.equals("max")) {
			solutionF = solver.optimize(
	                f,
	                new LinearConstraintSet(constraints),
	                GoalType.MAXIMIZE,
	                new NonNegativeConstraint(true)
	        );
		} else {
			solutionF = solver.optimize(
	                f,
	                new LinearConstraintSet(constraints),
	                GoalType.MINIMIZE,
	                new NonNegativeConstraint(true)
	        );
		}
		
		return solutionF;
	}
	
	
	private  ConstraintData parseConstraint(String constraint,int maxIndex) {

        constraint = constraint.replaceAll("\\s+", "");
        String[] parts = constraint.split("<=|>=|<|>|=");
        String leftPart = parts[0]; 
        String rightPart = constraint.substring(leftPart.length());


        String inequalitySign = rightPart.replaceAll("[^<=>]", ""); 
        rightPart = rightPart.replaceAll("[<=>]", ""); 

        double rightHandSide = Double.parseDouble(rightPart);
        double[] coefficients = extractCoefficients(leftPart,maxIndex);
        return new ConstraintData(coefficients, inequalitySign, rightHandSide);
    }
	
	private int findMaxIndex(String[] functions) {
	    int maxIndex = 0;
	    for (String function : functions) {
	        function = function.replaceAll("\\s+", ""); 
	        String[] terms = function.split("(?=[+-])"); 
	        for (String term : terms) {
	            int index;
	            if (term.contains("*")) {
	                
	                index = Integer.parseInt(term.split("\\*x_")[1]);
	            } else {
	                index = Integer.parseInt(term.split("x_")[1]);
	            }
	            if (index > maxIndex) {
	                maxIndex = index;
	            }
	        }
	    }
	    return maxIndex;
	}	
	
	
	private double[] extractCoefficients(String function, int maxIndex) {
        function = function.replaceAll("\\s+", "");
        String[] terms = function.split("(?=[+-])"); 

        double[] coefficients = new double[maxIndex];

        for (String term : terms) {
            double coefficient;
            int index;
            
            if (term.contains("*")) {
                String[] parts = term.split("\\*x_");
                coefficient = Double.parseDouble(parts[0]);
                index = Integer.parseInt(parts[1]);
            } else {
                if (term.startsWith("-")) {
                    coefficient = -1;
                    index = Integer.parseInt(term.split("x_")[1]);
                } else {
                    coefficient = 1;
                    index = Integer.parseInt(term.split("x_")[1]);
                }
            }

            coefficients[index - 1] = coefficient;
        }

        return coefficients;
    }
	
	
	static class ConstraintData {
        double[] coefficients; 
        String inequalitySign;
        double rightHandSide;  

        public ConstraintData(double[] coefficients, String inequalitySign, double rightHandSide) {
            this.coefficients = coefficients;
            this.inequalitySign = inequalitySign;
            this.rightHandSide = rightHandSide;
        }
    }

	
	
}
