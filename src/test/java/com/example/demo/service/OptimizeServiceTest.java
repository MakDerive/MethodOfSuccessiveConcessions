package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.example.demo.model.Data;
import com.example.demo.model.Objective;
import com.example.demo.model.OutputData;

public class OptimizeServiceTest {
	
	@ParameterizedTest
    @MethodSource("provideTestData")
	void optimize(Objective[] objectives, String[] concessions, String[] constraints, 
            double[] expectedCoefficients, double[] expectedFunctionResults) {

		Data data = new Data(objectives,concessions,constraints);
		
		OutputData result = new OptimizeService().optimize(data);
		assertArrayEquals(expectedCoefficients, result.coefficientsResults);
		assertArrayEquals( expectedFunctionResults, result.functionResults );
		System.out.println("hello");
	}
	 
	static Stream<Arguments> provideTestData() {
		return Stream.of(
				Arguments.of(new Objective[] { new Objective("2*x_1 + x_2 - 3*x_3", "max"),
						new Objective("x_1 + 3*x_2 - 2*x_3", "min"), new Objective("-x_1 + 2*x_2 + 4*x_3", "max") },
						new String[] { "4", "5" },
						new String[] { "x_1 + 3*x_2 + 2*x_3 >= 1", "2*x_1 -x_2 + x_3 <= 16", "x_1 + 2*x_2 <= 24" },
						new double[] { 10.76, 6.62, 1.11 }, new double[] { 24.8, 28.4, 6.93 }),

				Arguments.of(
						new Objective[] { new Objective("x_1 + 2*x_2", "max"), new Objective("x_1 + x_2", "min"), },
						new String[] { "3.5" }, new String[] { "x_1 + 2*x_2 >= 6", "x_1 <= 4", "x_2 <= 5", },

						new double[] { 0.5, 5.0 }, new double[] { 10.5, 5.5 }));
	}
	
}
