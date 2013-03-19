package flexjson.factories;

import flexjson.ObjectFactory;
import flexjson.JSONException;
import flexjson.ObjectBinder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * DateObjectFactory instantiates java.lang.Date using a list of known java.text.DateFormat instances for json
 * string types, or milliseconds from Jan 1, 1970 GMT for json numeric types.
 *
 * By default it uses the following patterns to recognize dates from json strings:
 *
 * DateFormat.getDateTimeInstance()
 * DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG )
 * DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.MEDIUM )
 * DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT )
 * EEE MMM d hh:mm:ss a z yyyy
 * EEE MMM d HH:mm:ss z yyyy
 * MM/dd/yy hh:mm:ss a
 * MM/dd/yy
 *
 */
public class DateObjectFactory implements ObjectFactory {

    private static List<String> defaultFormats;

    protected List<String> dateFormats;
    protected boolean fromDefaults = false;
    protected ThreadLocal<List<DateFormat>> dateFormatters = new ThreadLocal<List<DateFormat>>();

    static {
        defaultFormats = new ArrayList<String>();
        defaultFormats.add( "EEE MMM d hh:mm:ss a z yyyy" );
        defaultFormats.add( "EEE MMM d HH:mm:ss z yyyy" );
        defaultFormats.add( "MM/dd/yy hh:mm:ss a" );
        defaultFormats.add( "MM/dd/yy" );
    }

    /**
     * This method adds a default format for all DateObjectFactory.  This can be used to add
     * a new DateFormat to every instance created after this call.
     *
     * @param formats one or more date formats to add to the known default formats.  All formats must conform to the SimpleDateFormat syntax.
     */
    public static void addDefaultFormat( String... formats ) {
        defaultFormats.addAll( Arrays.asList(formats) );
    }

    /**
     * This constructor constructs a DateObjectFactory using the default formats known
     * to all instances of the DateObjectFactory.
     */
    public DateObjectFactory() {
        fromDefaults = true;
        dateFormats = new ArrayList<String>(defaultFormats);
    }

    /**
     * This constructs a DateObjectFactory that only knows how to recognize the given
     * DateFormat instances.  This instance ignores any default formats.  This can be
     * used to as a way to optimize performance because it doesn't have to test each
     * date against formats that will never occur in your program.  This is great to use
     * if you can predict all formats you might parse up front.
     *
     * @param dateFormats a list of the date formats you want to recognize.
     */
    public DateObjectFactory(List<String> dateFormats) {
        this.dateFormats = dateFormats;
    }

    /**
     * Add a new DateFormat to the known formats this instance will recognise.
     *
     * @param formats one or more DateFormat instances to add to the list of
     * known formats.
     * @return this instance for chaining calls.
     */
    public DateObjectFactory add( String... formats ) {
        dateFormats.addAll( Arrays.asList( formats ) );
        return this;
    }

    /**
     * This is a method is used by Flexjson deserialization process to instantiate and bind all
     * data into a Date instance.  You shouldn't need to call this method directly.
     *
     * @param context the object binding context to keep track of where we are in the object graph
     * and used for binding into objects.
     * @param value This is the value from the json object at the current path.
     * @param targetType This is the type pulled from the object introspector.  Used for Collections and generic types.
     * @param targetClass concrete class pulled from the configuration of the deserializer.
     *
     * @return a properly initialized java.lang.Date object.
     */
    public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
        try {
            if( value instanceof Number ) {
                return instantiateDate( (Class)targetType, ((Number)value).longValue(), context );
            } else {
                for( DateFormat format : getDateFormats() ) {
                    try {
                        return format.parse( value.toString() );
                    } catch (ParseException e) {
                        // try next format
                    }
                }
                throw new JSONException( String.format("%s:  Parsing date %s was not recognized as a date format", context.getCurrentPath(), value ) );
            }
        } catch (IllegalAccessException e) {
            throw new JSONException( String.format("%s:  Error encountered trying to instantiate %s", context.getCurrentPath(), ((Class)targetType).getName() ), e);
        } catch (InstantiationException e) {
            throw new JSONException( String.format("%s:  Error encountered trying to instantiate %s.  Make sure there is a public constructor that accepts a single Long.", context.getCurrentPath(), ((Class)targetType).getName() ), e);
        } catch (InvocationTargetException e) {
            throw new JSONException( String.format("%s:  Error encountered trying to instantiate %s.  Make sure there is a public constructor that accepts a single Long.", context.getCurrentPath(), ((Class)targetType).getName() ), e);
        }
    }

    private Date instantiateDate( Class targetType, Long value, ObjectBinder context ) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        try {
            Constructor constructor = targetType.getConstructor(Long.TYPE);
            return (Date)constructor.newInstance( value );
        } catch (NoSuchMethodException e) {
            Date d = (Date)targetType.newInstance();
            d.setTime( value );
            return d;
        }
    }

    protected List<DateFormat> getDateFormats() {
        if( this.dateFormatters.get() == null ) {
            List<DateFormat> dateFormatList = new ArrayList<DateFormat>();
            if( fromDefaults ) {
                dateFormatList.add( DateFormat.getDateTimeInstance() );
                dateFormatList.add( DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG ) );
                dateFormatList.add( DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.MEDIUM ) );
                dateFormatList.add( DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT ) );
            }

            for( String format : dateFormats ) {
                dateFormatList.add( new SimpleDateFormat( format ) );
            }

            this.dateFormatters.set( dateFormatList );
        }
        return this.dateFormatters.get();
    }
}
