package fr.inria.soctrace.test.junit.lib.query;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AnalysisResultQueryTest.class, EPHierarchyDescMapTest.class,
		EPHierarchyDescTest.class, EventIteratorTest.class,
		EventParamTypeQueryTest.class, EventProducerQueryTest.class,
		EventQueryTest.class, EventTypeQueryTest.class, FileQueryTest.class,
		HistogramLoaderFactoryTest.class, HistogramLoaderTest.class,
		HistogramTest.class, LogicalConditionTest.class,
		ParamLogicalConditionTest.class, ToolQueryTest.class,
		TraceParamTypeQueryTest.class, TraceQueryTest.class,
		TraceTypeQueryTest.class, ValueListStringTest.class })
public class AllTests {

}
