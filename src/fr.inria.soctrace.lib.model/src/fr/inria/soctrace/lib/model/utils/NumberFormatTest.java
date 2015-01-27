package fr.inria.soctrace.lib.model.utils;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;

public class NumberFormatTest {

	public static void main(String[] args) {
		List<Double> dList = new ArrayList<>();
		dList.add(1.1);
		dList.add(11.1);
		dList.add(111.1);
		dList.add(1111.1);
		dList.add(11111.1);
		dList.add(111111.1);
		dList.add(1111111.1);
		dList.add(11111111.1);
		dList.add(111111111.1);
		dList.add(1111111111.1);
		dList.add(11111111111.1);
		dList.add(Double.MAX_VALUE);

		TimestampFormat tsf = new TimestampFormat(TimeUnit.NANOSECONDS);

		for (Double d : dList) {
			System.out.println(tsf.format(d));
		}

		List<Long> lList = new ArrayList<>();
		lList.add(11111111111L);
		lList.add(Long.MAX_VALUE);

		for (Long d : lList) {
			System.out.println(tsf.format(d));
		}

	}

}
