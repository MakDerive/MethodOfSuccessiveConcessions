# Проект для оптимизации многокритериаьных задач.

Оптимизация происходит с помощью метода последовательных уступок. Необходимо ввести целевые функции, значения уступок и ограничения.
Использована библиотека The Apache Commons Mathematics Library для оптимизации.

Пример работы программы

![метод оптимизации](https://github.com/user-attachments/assets/c3a3e733-0fe4-40db-8a3f-c091ab80aa67)

Проверка тестов.

![image](https://github.com/user-attachments/assets/a191f306-a676-414f-b223-e7ee97550642)

```java
@ParameterizedTest
    @MethodSource("provideTestData")
	void optimize(Objective[] objectives, String[] concessions, String[] constraints, 
            double[] expectedCoefficients, double[] expectedFunctionResults) {

		Data data = new Data(objectives,concessions,constraints);
		
		OutputData result = new OptimizeService().optimize(data);
		assertArrayEquals(expectedCoefficients, result.coefficientsResults);
		assertArrayEquals( expectedFunctionResults, result.functionResults );
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
```
