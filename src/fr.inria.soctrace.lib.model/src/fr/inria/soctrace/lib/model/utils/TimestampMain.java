package fr.inria.soctrace.lib.model.utils;

import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;

public class TimestampMain {

	public static void main(String[] args) {
		
		TimestampFormat f = new TimestampFormat(TimeUnit.NANOSECONDS);
		
		long ts = 123123123;
		long te = 123123123123L;
		
		System.out.println(f.format(ts));
		System.out.println(f.format(te));
		
		f.setFixContext(ts, te);

		System.out.println(f.format(ts));
		System.out.println(f.format(te));
		
		
	}

}
