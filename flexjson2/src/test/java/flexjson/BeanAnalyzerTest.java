package flexjson;

import flexjson.mock.Book;
import flexjson.mock.Employee;
import flexjson.model.Candidate;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;

public class BeanAnalyzerTest extends TestCase {

    public void setUp() {
    }

    public void tearDown() {
    }

    public void testAnalyzer() throws IntrospectionException {
        compare(Introspector.getBeanInfo( Candidate.class ), BeanAnalyzer.analyze( Candidate.class ) );
        compare(Introspector.getBeanInfo(Employee.class), BeanAnalyzer.analyze(Employee.class) );
        compare(Introspector.getBeanInfo( Book.class ), BeanAnalyzer.analyze( Book.class ) );
    }

    public void testPerformance() throws IntrospectionException {
        long averageInspector = 0L;
        for( int i = 0; i < 1000; i++ ) {
            long start = System.nanoTime();
            BeanInfo info = Introspector.getBeanInfo( Candidate.class );
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
            Introspector.getBeanInfo( Employee.class );
            Introspector.getBeanInfo( Book.class );
            long end = System.nanoTime();
            averageInspector += end-start;
            Introspector.flushCaches();
        }
        long averageAnalyzer = 0L;
        for( int i = 0; i < 1000; i++ ) {
            long start = System.nanoTime();
            BeanAnalyzer analyzer = BeanAnalyzer.analyze(Candidate.class);
            Collection<BeanProperty> values = analyzer.getProperties();
            BeanAnalyzer.analyze(Employee.class);
            BeanAnalyzer.analyze(Book.class);
            long end = System.nanoTime();
            averageAnalyzer += end-start;
            BeanAnalyzer.clearCache();
        }
        double improvement = (double)averageInspector / averageAnalyzer;
        System.out.println("Improvement ratio: " +  improvement + " times faster" );
        assertTrue("Make sure we are at least 10x faster than BeanIntrospector", improvement > 10.0 );
    }

    private void compare(BeanInfo beanInfo, BeanAnalyzer analyzer ) {
        Collection<BeanProperty> properties = analyzer.getProperties();
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();

        assertEquals( descriptors.length, properties.size() ) ;
        for( PropertyDescriptor descriptor : descriptors ) {
            BeanProperty property = analyzer.getProperty( descriptor.getName() );
            assertNotNull( descriptor.getName() + " is missing.", property );
            assertEquals( descriptor.getName(), property.getName() );
            assertEquals( descriptor.getReadMethod(), property.getReadMethod() );
            if( descriptor.getWriteMethod() != null ) {
                assertTrue( "Does not contain a method: " + descriptor.getWriteMethod(), property.getWriteMethods().contains( descriptor.getWriteMethod() ) );
                assertEquals( descriptor.getWriteMethod(), property.getWriteMethod() );
            } else {
                assertTrue( "Contains a write method " + descriptor.getWriteMethod() + " when it shouldn't.", property.getWriteMethods().isEmpty() );
            }
        }
    }

    private void printBeanAnalyzer( Class clazz ) {
        BeanAnalyzer analyzer = BeanAnalyzer.analyze( clazz );
        for( BeanProperty property : analyzer.getProperties() ) {
            System.out.print(property.getName() + ": " );
            if( property.getReadMethod() != null ) System.out.print(property.getReadMethod().getName() + "()");
            for( Method method : property.getWriteMethods() ) {
                System.out.print( " " + method.getName() + "( " + method.getParameterTypes()[0].getName() + ")");
            }
            System.out.println();
        }
    }

    private void printBeanInfo( Class clazz ) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo( clazz );
        for( PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors() ) {
            System.out.print(descriptor.getName() + ": " );
            if( descriptor.getReadMethod() != null ) System.out.print(descriptor.getReadMethod().getName() + "()");
            if( descriptor.getWriteMethod() != null ) System.out.println(descriptor.getWriteMethod().getName() + "( " + descriptor.getWriteMethod().getParameterTypes()[0].getName() + ")");
        }
    }

    public static Test suite() {
        return new TestSuite(BeanAnalyzerTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
