import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MaxFlowCalcTest {
	
	private static String testdir = "src/test/resources/";

	@Test
	@DisplayName("Test 1")
	public void test1() {
		File file = new File(testdir + "test1.network");
		MaxFlowCalc mflow = new MaxFlowCalc(file);
		assertEquals(9, mflow.getMaximumFlow());
	}
	
	@Test
	@DisplayName("Test 2")
	public void test2() {
		File file = new File(testdir + "test2.network");
		MaxFlowCalc mflow = new MaxFlowCalc(file);
		assertEquals(3, mflow.getMaximumFlow());
	}

	@Test
	@DisplayName("Test 3")
	public void test3() {
		File file = new File(testdir + "test3.network");
		MaxFlowCalc mflow = new MaxFlowCalc(file);
		assertEquals(12, mflow.getMaximumFlow());
	}

	@Test
	@DisplayName("Test 4")
	public void test4() {
		File file = new File(testdir + "test4.network");
		MaxFlowCalc mflow = new MaxFlowCalc(file);
		assertEquals(12, mflow.getMaximumFlow());
	}

	@Test
	@DisplayName("Test 5")
	public void test5() {
		File file = new File(testdir + "test5.network");
		MaxFlowCalc mflow = new MaxFlowCalc(file);
		assertEquals(3.141, mflow.getMaximumFlow());
	}
	
}
